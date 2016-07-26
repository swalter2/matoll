/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.matoll.process;

import de.citec.sc.matoll.core.Language;
import static de.citec.sc.matoll.core.Language.EN;
import de.citec.sc.matoll.core.LexicalEntry;
import de.citec.sc.matoll.core.Lexicon;
import de.citec.sc.matoll.core.Sense;
import de.citec.sc.matoll.core.Sentence;
import de.citec.sc.matoll.io.Config;
import de.citec.sc.matoll.io.LexiconLoader;
import de.citec.sc.matoll.preprocessor.ModelPreprocessor;
import de.citec.sc.matoll.utils.Stopwords;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.lang.StringUtils;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.RDFDataMgr;
import org.xml.sax.SAXException;

/**
 *
 * @author swalter
 */
public class Matoll_CreateMax {
    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException, InstantiationException, IllegalAccessException, ClassNotFoundException, Exception {



        String directory;
        String gold_standard_lexicon;
        String output_lexicon;
        String configFile;
        Language language;
        String output;

        Stopwords stopwords = new Stopwords();

        HashMap<String,Double> maxima; 
        maxima = new HashMap<String,Double>();


        if (args.length < 3)
        {
                System.out.print("Usage: Matoll --mode=train/test <DIRECTORY> <CONFIG>\n");
                return;

        }

//		Classifier classifier;

        directory = args[1];
        configFile = args[2];

        final Config config = new Config();

        config.loadFromFile(configFile);

        gold_standard_lexicon = config.getGoldStandardLexicon();

        String model_file = config.getModel();

        output_lexicon = config.getOutputLexicon();
        output = config.getOutput();


        language = config.getLanguage();

        LexiconLoader loader = new LexiconLoader();
        Lexicon gold = loader.loadFromFile(gold_standard_lexicon);



        Set<String> uris = new HashSet<>();
//        Map<Integer,String> sentence_list = new HashMap<>();
        Map<Integer,Set<Integer>> mapping_words_sentences = new HashMap<>();
        
        //consider only properties
        for(LexicalEntry entry: gold.getEntries()){
            try{
                 for(Sense sense: entry.getSenseBehaviours().keySet()){
                     String tmp_uri = sense.getReference().getURI().replace("http://dbpedia.org/ontology/", "");
                     if(!Character.isUpperCase(tmp_uri.charAt(0))){
                        uris.add(sense.getReference().getURI());
                     }
                 }
            }
            catch(Exception e){};
        }


        ModelPreprocessor preprocessor = new ModelPreprocessor(language);
        preprocessor.setCoreferenceResolution(false);
        Set<String> dep = new HashSet<>();
        dep.add("prep");
        dep.add("appos");
        dep.add("nn");
        dep.add("dobj");
        dep.add("pobj");
        dep.add("num");
        preprocessor.setDEP(dep);

        List<File> list_files = new ArrayList<>();

        if(config.getFiles().isEmpty()){
            File folder = new File(directory);
            File[] files = folder.listFiles();
            for(File file : files){
                if (file.toString().contains(".ttl")) list_files.add(file);
            }
        }
        else{
            list_files.addAll(config.getFiles());
        }
        System.out.println(list_files.size());

        int sentence_counter = 0;
        Map<String,Set<Integer>> bag_words_uri = new HashMap<>();
        Map<String,Integer> mapping_word_id = new HashMap<>();
        for(File file:list_files){
            Model model = RDFDataMgr.loadModel(file.toString());
            for(Model sentence: getSentences(model)){
                String reference = getReference(sentence);
                reference = reference.replace("http://dbpedia/","http://dbpedia.org/");
                if(uris.contains(reference)){
                    sentence_counter += 1;
                    Set<Integer> words_ids = getBagOfWords(sentence,stopwords,mapping_word_id);
                    //TODO: add sentence preprocessing
                    String obj = getObject(sentence);
                    String subj = getSubject(sentence);
                    preprocessor.preprocess(sentence,subj,obj,language);
                    //TODO: also return marker if object or subject of property (in SPARQL this has to be optional of course)
                    String parsed_sentence = getParsedSentence(sentence);
                    try(FileWriter fw = new FileWriter("mapping_sentences_to_ids_goldstandard.tsv", true);
                        BufferedWriter bw = new BufferedWriter(fw);
                        PrintWriter out = new PrintWriter(bw))
                        {
                            out.println(sentence_counter+"\t"+parsed_sentence);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    for(Integer word_id : words_ids){
                        if(mapping_words_sentences.containsKey(word_id)){
                            Set<Integer> tmp_set = mapping_words_sentences.get(word_id);
                            tmp_set.add(sentence_counter);
                            mapping_words_sentences.put(word_id, tmp_set);

                        }
                        else{
                            Set<Integer> tmp_set = new HashSet<>();
                            tmp_set.add(sentence_counter); 
                            mapping_words_sentences.put(word_id, tmp_set);    
                        }
                        
                    }
                    if(bag_words_uri.containsKey(reference)){
                        Set<Integer> tmp = bag_words_uri.get(reference);
                        for(Integer w: words_ids){
                            tmp.add(w);
                            
                        }
                        bag_words_uri.put(reference, tmp);
                    }
                    else{
                        Set<Integer> tmp = new HashSet<>();
                        for(Integer w: words_ids){
                            tmp.add(w);
                        }
                        bag_words_uri.put(reference, tmp);
                    }
                }
                
            }      
            model.close();
            
        }
        
       PrintWriter writer = new PrintWriter("bag_of_words_only_goldstandard.tsv");
       StringBuilder string_builder = new StringBuilder();
        for(String r:bag_words_uri.keySet()) {
            string_builder.append(r);
            for(Integer i: bag_words_uri.get(r)){
                string_builder.append("\t");
                string_builder.append(i);
            }
            string_builder.append("\n");
            }
        writer.write(string_builder.toString());
        writer.close();
        
        
        writer = new PrintWriter("mapping_words_to_sentenceids_goldstandard.tsv");
        string_builder = new StringBuilder();
        for(Integer w:mapping_words_sentences.keySet()) {
            string_builder.append(w);
            for(int i : mapping_words_sentences.get(w)){
                string_builder.append("\t");
                string_builder.append(i);
            }
            string_builder.append("\n");
            }
        writer.write(string_builder.toString());
        writer.close();

    }
    
    private static String getParsedSentence(Model sentence) {
        String query = "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "
                + "SELECT ?number ?form ?pos ?e1_arg ?e2_arg ?deprel ?head WHERE {"
                + "?sentence <conll:wordnumber> ?number . "
                + "?sentence <conll:form> ?form ."
                + "?sentence <conll:cpostag> ?pos ."
                + "?sentence <conll:deprel> ?deprel ."
                + "?sentence <conll:head> ?head ."
                + "OPTIONAL{"
                + "?sentence <own:senseArg> ?e1_arg. }"
                + "OPTIONAL{"
                + "?sentence <own:senseArg> ?e2_arg. }"
                + "} ORDER BY ASC(xsd:integer(?number))";
        QueryExecution qExec = QueryExecutionFactory.create(query, sentence) ;
        ResultSet rs = qExec.execSelect() ;
        String result = "";
        while ( rs.hasNext() ) {
            QuerySolution qs = rs.next();
            try{
                    String e1_arg = "";
                    String e2_arg = "";
                    try{
                        e1_arg = qs.get("?e1_arg").toString();
                        e1_arg = e1_arg.replace("http://lemon-model.net/lemon#","");
                    }
                    catch(Exception e){
                    }
                    try{
                        e2_arg = qs.get("?e2_arg").toString();
                        e2_arg = e2_arg.replace("http://lemon-model.net/lemon#","");
                    }
                    catch(Exception e){
                    }
                    result+= qs.get("?number").toString()+"_"+qs.get("?form").toString()+"_"+qs.get("?pos").toString()+"_"+qs.get("?deprel").toString()+"_"+qs.get("?head").toString();
                    if(!e1_arg.equals("")){
                        result+="_arg1:"+e1_arg;
                    }
                    if(!e2_arg.equals("")){
                        result+="_arg2:"+e2_arg;
                    }
                    result+=" ";
             }
            catch(Exception e){
           e.printStackTrace();
           }
        }
        qExec.close() ;
       return result;
    }
    
    private static List<Model> getSentences(Model model) throws FileNotFoundException {
		
        // get all ?res <conll:sentence> 

        List<Model> sentences = new ArrayList<Model>();

        StmtIterator iter, iter2, iter3;

        Statement stmt, stmt2, stmt3;

        Resource resource;

        Resource token;

        iter = model.listStatements(null,model.getProperty("conll:language"), (RDFNode) null);

        while (iter.hasNext()) {

                Model sentence = ModelFactory.createDefaultModel();

                stmt = iter.next();

                resource = stmt.getSubject();

                iter2 = model.listStatements(resource , null, (RDFNode) null);

                while (iter2.hasNext())
                {
                        stmt2 = iter2.next();

                        sentence.add(stmt2);

                }

                iter2 = model.listStatements(null , model.getProperty("own:partOf"), (RDFNode) resource);

                while (iter2.hasNext())
                {
                        stmt2 = iter2.next();

                        token = stmt2.getSubject();

                        iter3 = model.listStatements(token , null, (RDFNode) null);

                        while (iter3.hasNext())
                        {
                                stmt3 = iter3.next();

                                sentence.add(stmt3);

                        }
                }

                sentences.add(sentence);

                // RDFDataMgr.write(new FileOutputStream(new File(resource+".ttl")), sentence, RDFFormat.TURTLE) ;

        }


        return sentences;
		
	}
    
    private static String getSubject(Model model) {

            StmtIterator iter = model.listStatements(null,model.getProperty("own:subj"), (RDFNode) null);

            Statement stmt;

            while (iter.hasNext()) {

                    stmt = iter.next();

            return stmt.getObject().toString();
        }

            return null;
    }

    /**
     * 
     * @param model
     * @return 
     */
    private static String getObject(Model model) {
            StmtIterator iter = model.listStatements(null,model.getProperty("own:obj"), (RDFNode) null);

            Statement stmt;

            while (iter.hasNext()) {

                    stmt = iter.next();

            return stmt.getObject().toString();
        }

            return null;
    }
    
    private static String getReference(Model model) {
		StmtIterator iter = model.listStatements(null,model.getProperty("conll:reference"), (RDFNode) null);
		Statement stmt;
		while (iter.hasNext()) {
			stmt = iter.next();
                   return stmt.getObject().toString();
                }
		
		return null;
	}

 

    private static Set<Integer> getBagOfWords(Model sentence, Stopwords stopwords, Map<String,Integer> mapping_word_id) {
        String query = "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "
                + "SELECT ?number ?form WHERE {"
                + "?sentence <conll:wordnumber> ?number . "
                + "?sentence <conll:form> ?form ."
                + "}";
        QueryExecution qExec = QueryExecutionFactory.create(query, sentence) ;
        ResultSet rs = qExec.execSelect() ;
        Set<Integer> words = new HashSet<>();
        while ( rs.hasNext() ) {
                QuerySolution qs = rs.next();
                try{
                    String term = qs.get("?form").toString();
                    if(StringUtils.isAlpha(term) && !stopwords.isStopword(term, EN)) {
                        //words.add(term);
                        if(!mapping_word_id.containsKey(term)){
                            int value = mapping_word_id.size();
                            value +=1;
                            mapping_word_id.put(term, value);
                            words.add(value);
                            try(FileWriter fw = new FileWriter("mapping_words_to_ids_goldstandard.tsv", true);
                            BufferedWriter bw = new BufferedWriter(fw);
                            PrintWriter out = new PrintWriter(bw))
                            {
                                out.println(term+"\t"+value);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        else words.add(mapping_word_id.get(term));
                    }
                        
                 }
                catch(Exception e){
               e.printStackTrace();
               }
            }
        qExec.close() ;
       return words;
    }
    
    
    
    
    
    
    
}


