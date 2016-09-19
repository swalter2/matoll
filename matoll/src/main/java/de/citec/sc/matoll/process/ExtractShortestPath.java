/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.matoll.process;

import de.citec.sc.matoll.core.Language;
import static de.citec.sc.matoll.core.Language.EN;
import de.citec.sc.matoll.core.Sentence;
import de.citec.sc.matoll.utils.RelationshipEdge;
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
import org.jgrapht.GraphPath;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.KShortestPaths;
import org.jgrapht.graph.SimpleGraph;
import org.xml.sax.SAXException;

/**
 *
 * @author swalter
 */
public class ExtractShortestPath {
    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException, InstantiationException, IllegalAccessException, ClassNotFoundException, Exception {

        Language language = EN;
        String path_to_files = "/Users/swalter/Desktop/test_input";

        Stopwords stopwords = new Stopwords();

        String subj;
        String obj;

        List<Model> sentences = new ArrayList<>();

        List<File> list_files = new ArrayList<>();


        File folder = new File(path_to_files);
        File[] files = folder.listFiles();
        for(File file : files){
            if (file.toString().contains(".ttl")) list_files.add(file);
        }

        
        FileWriter fw = new FileWriter("shortest_path.tsv", true);
        BufferedWriter bw = new BufferedWriter(fw);
        PrintWriter out = new PrintWriter(bw);
//        {
//            out.println(sentence_counter+"\t"+parsed_sentence);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        System.out.println("Start");
        for(File file:list_files){
            Model model = RDFDataMgr.loadModel(file.toString());
            sentences.clear();
            sentences = getSentences(model);
            for(Model sentence: sentences){
                Sentence sentenceObject = returnSentence(sentence);
                subj = sentenceObject.getSubjOfProp();
                obj = sentenceObject.getObjOfProp();
                try{
                     if (!stopwords.isStopword(obj, language) 
                        && !stopwords.isStopword(subj, language) 
                        && !subj.equals(obj) 
                        && !subj.contains(obj) 
                        && !obj.contains(subj)) {
                    //reference = getReference(sentence);
                    doShortestPathExtraction(sentence,sentenceObject,out);
           
                }       
                }
                catch(Exception e){
                    
                
                }
            }
            model.close();
        }
        
        out.close();
        bw.close();
        fw.close();

        


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

    private static void doShortestPathExtraction(Model sentence, Sentence sentenceObject, PrintWriter out) throws IOException, InterruptedException {
        
//        DirectedGraph<Integer, RelationshipEdge> g = new DefaultDirectedGraph<Integer, RelationshipEdge>(RelationshipEdge.class);
        UndirectedGraph<Integer, RelationshipEdge> g = new SimpleGraph<Integer, RelationshipEdge>(RelationshipEdge.class);


        Statement stmt;
        int id_subject = 0;
        int id_object = 0;
        Set<Integer> hm = new HashSet<>();
        Map<String,String> relations = new HashMap<>();
        Map<String,String> forms = new HashMap<>();
        String subj = sentenceObject.getSubjOfProp();
        String obj = sentenceObject.getObjOfProp();
        
        StmtIterator iter = sentence.listStatements(null, sentence.createProperty("conll:form"), (RDFNode) null);
         while (iter.hasNext()) {
            stmt = iter.next();
            String subject = stmt.getSubject().toString();
            subject = subject.replace("token:token","").replace("_","");
            Integer subject_value = Integer.valueOf(subject);
            String object = stmt.getObject().toString();
            if(subj.contains(object)){
                id_subject = subject_value;
            }
            if(obj.contains(object)){
                id_object = subject_value;
            }
            forms.put(subject, object);
         }
         
         iter = sentence.listStatements(null, sentence.createProperty("conll:deprel"), (RDFNode) null);
         while (iter.hasNext()) {
            stmt = iter.next();
            String subject = stmt.getSubject().toString();
            subject = subject.replace("token:token","").replace("_","");
            String object = stmt.getObject().toString();
            relations.put(subject, object);
         }
         
        iter = sentence.listStatements(null, sentence.createProperty("conll:head"), (RDFNode) null);

         while (iter.hasNext()) {
            stmt = iter.next();
            String subject = stmt.getSubject().toString();
            String object = stmt.getObject().toString();
            subject = subject.replace("token:token","").replace("_","");
            object = object.replace("token:token","").replace("_","");
            int object_value = Integer.valueOf(object);
            int subject_value = Integer.valueOf(subject);
            if(!hm.contains(subject_value)){
                g.addVertex(subject_value);
                hm.add(subject_value);
            }
            
            if(!hm.contains(object_value)){
                g.addVertex(object_value);
                hm.add(object_value);
            }
            
            g.addEdge(subject_value, object_value,new RelationshipEdge<String>(subject, object, relations.get(subject), forms.get(subject)));
         }
         
         try{
             
                if(id_subject>0 && id_object>0 && id_subject!=id_object){
                    KShortestPaths ksp = new KShortestPaths(g, id_subject, 2); 
                    List<GraphPath> paths = ksp.getPaths(id_object);
                    if (paths != null){
                        paths.stream().forEach((p) -> {
                            out.println(p.getEdgeList().size()+"\t"+p.toString()+"\t"+subj+"\t"+obj+"\t"+sentenceObject.getSentence()+getParsedSentence(sentence));
                        });
                    }
                }
                
 
         }
         catch(Exception e){
             e.printStackTrace();
         }
        


    
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
    
    
    
    
    
    
    
}


