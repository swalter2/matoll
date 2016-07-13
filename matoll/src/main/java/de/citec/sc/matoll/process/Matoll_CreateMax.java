/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.matoll.process;

import de.citec.sc.matoll.core.Language;
import de.citec.sc.matoll.core.LexicalEntry;
import de.citec.sc.matoll.core.Lexicon;
import de.citec.sc.matoll.core.Sense;
import de.citec.sc.matoll.core.Sentence;
import de.citec.sc.matoll.io.Config;
import de.citec.sc.matoll.io.LexiconLoader;
import de.citec.sc.matoll.utils.RelationshipEdge;
import de.citec.sc.matoll.utils.Stopwords;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;
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



        Set<String> gold_entries = new HashSet<>();
        Set<String> uris = new HashSet<>();
        
        //consider only properties
        for(LexicalEntry entry: gold.getEntries()){
            try{
                 for(Sense sense: entry.getSenseBehaviours().keySet()){
                     String tmp_uri = sense.getReference().getURI().replace("http://dbpedia.org/ontology/", "");
                     if(!Character.isUpperCase(tmp_uri.charAt(0))){
                        gold_entries.add(entry.getCanonicalForm().toLowerCase()+" "+sense.getReference().getURI());
                        uris.add(sense.getReference().getURI());
                     }
                 }
            }
            catch(Exception e){};
        }
//        for(String x: gold_entries) System.out.println(x);
        Lexicon automatic_lexicon = new Lexicon();
        automatic_lexicon.setBaseURI(config.getBaseUri());


        String subj;
        String obj;

        String reference = null;

        List<Model> sentences = new ArrayList<>();

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



        Map<String,Set<String>> bag_words = new HashMap<>();
        for(File file:list_files){
            Model model = RDFDataMgr.loadModel(file.toString());
            sentences.clear();
            sentences = getSentences(model);
            for(Model sentence: sentences){
                reference = getReference(sentence);
                Set words = getBagOfWords(sentence);
                if(bag_words.containsKey(reference)){
                    Set<String> tmp = bag_words.get(reference);
                    tmp.addAll(words);
                    bag_words.put(reference, tmp);
                }
                else{
                    bag_words.put(reference, words);
                }
            }      
            model.close();
        }
        
       PrintWriter writer = new PrintWriter("bag_of_words.tsv");
       StringBuilder string_builder = new StringBuilder();
        for(String r:bag_words.keySet()) {
            string_builder.append(r);
            for(String s: bag_words.get(r)){
                string_builder.append("\t");
                string_builder.append(s);
            }
            string_builder.append("\n");
            }
        writer.write(string_builder.toString());
        writer.close();

    }
    
    private static Sentence returnSentence(Model model) {
        /* In the model is always only one plain_sentence*/
		
        String plain_sentence = "";
        String subjOfProp = "";
        String objOfProp = "";

        StmtIterator iter = model.listStatements(null,model.createProperty("conll:sentence"), (RDFNode) null);
        Statement stmt;
        stmt = iter.next();
        plain_sentence = stmt.getObject().toString();

        iter = model.listStatements(null,model.createProperty("own:subj"), (RDFNode) null);
        stmt = iter.next();
        subjOfProp = stmt.getObject().toString();

        iter = model.listStatements(null,model.createProperty("own:obj"), (RDFNode) null);
        stmt = iter.next();
        objOfProp = stmt.getObject().toString();

        Sentence sentence = new Sentence(plain_sentence,subjOfProp,objOfProp);

        try{
            iter = model.listStatements(null,model.createProperty("own:subjuri"), (RDFNode) null);
            stmt = iter.next();
            sentence.setSubjOfProp_uri(stmt.getObject().toString());
        }catch (Exception e){}


        try{
            iter = model.listStatements(null,model.createProperty("own:objuri"), (RDFNode) null);
            stmt = iter.next();
            sentence.setObjOfProp_uri(stmt.getObject().toString());
        }catch (Exception e){}

        return sentence;

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
    
    
    private static String getReference(Model model) {
		StmtIterator iter = model.listStatements(null,model.getProperty("conll:reference"), (RDFNode) null);
		Statement stmt;
		while (iter.hasNext()) {
			stmt = iter.next();
                   return stmt.getObject().toString();
                }
		
		return null;
	}

 

    private static Set getBagOfWords(Model sentence) {
        String query = "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "
                + "SELECT ?number ?form WHERE {"
                + "?sentence <conll:wordnumber> ?number . "
                + "?sentence <conll:form> ?form ."
                + "} ORDER BY ASC(xsd:integer(?number))";
        QueryExecution qExec = QueryExecutionFactory.create(query, sentence) ;
        ResultSet rs = qExec.execSelect() ;
        String result = "";
        Map<Integer,String> bla = new HashMap<>();
        Set<String> words = new HashSet<>();
        while ( rs.hasNext() ) {
                QuerySolution qs = rs.next();
                try{
                        words.add(qs.get("?form").toString());
                 }
                catch(Exception e){
               e.printStackTrace();
               }
            }
        qExec.close() ;
       return words;
    }
    
    
    
    
    
    
    
}

