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
import de.citec.sc.matoll.io.LexiconLoader;
import de.citec.sc.matoll.utils.RelationshipEdge;
import de.citec.sc.matoll.utils.Stopwords;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.jgrapht.GraphPath;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.KShortestPaths;
import org.jgrapht.graph.SimpleGraph;
import org.xml.sax.SAXException;

/**
 *
 * @author swalter
 */
public class Matoll_BaselineBlub {
    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException, InstantiationException, IllegalAccessException, ClassNotFoundException, Exception {



        String directory;
        String gold_standard_lexicon;
        String output_lexicon;
        String configFile;
        Language language;
        String output;

        Stopwords stopwords = new Stopwords();



        gold_standard_lexicon = "../lexica/dbpedia_en.rdf";



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



        Set<String> results = new HashSet<>();

        try {
            String content = new String(Files.readAllBytes(Paths.get("baseline_PropClassWordNet.txt")));
            String[] lines = content.split("\n");
            
            for(String line : lines){
                line = line.replace("\n","").trim();
               results.add(line);
            }
            
            
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        try {
            String content = new String(Files.readAllBytes(Paths.get("baseline_shortestPath.txt")));
            String[] lines = content.split("\n");
            
            for(String line : lines){
                line = line.replace("\n","").trim();
               results.add(line);
            }
            
            
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        try {
            String content = new String(Files.readAllBytes(Paths.get("baseline_plainSentence.txt")));
            String[] lines = content.split("\n");
            
            for(String line : lines){
                line = line.replace("\n","").trim();
               results.add(line);
            }
            
            
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
   
        
//        for(String x: results)System.out.println(x);
        
        /*
        Calculate recall
        */
        int overall_entries = 0;
        int correct_entries = 0;
        for(String uri:uris){
            for(String g: gold_entries){
                if(g.contains(uri)){
                    overall_entries+=1;
                    for(String r:results){
                        if(g.equals(r)){
                            correct_entries+=1;
                            break;
                        }
                    }
                }
            }
        }
        System.out.println(overall_entries);
        System.out.println(correct_entries);
        System.out.println("Baseline1: "+(correct_entries+0.0)/overall_entries);
        
        
        overall_entries = 0;
        correct_entries = 0;
        for(String g: gold_entries){
            overall_entries+=1;
            for(String r:results){
                if(g.equals(r)){
                    correct_entries+=1;
                    break;
                }
            }            
        }
        System.out.println(overall_entries);
        System.out.println(correct_entries);
        System.out.println("Baseline1b: "+(correct_entries+0.0)/overall_entries);
        
        

        
        

        
        
        
        
        
        
        


        
        


    }
    


    private static String firstClean(String tmp) {
        tmp = tmp.replace(",","");
        tmp = tmp.replace(".","");
        tmp = tmp.replace(";","");
        tmp = tmp.replace("?","");
        tmp = tmp.replace("!","");
        tmp = tmp.replace("'", "");
        tmp = tmp.replace("-lrb-", "");
        tmp = tmp.replace("-rbr-", "");
        tmp = tmp.trim();
        return tmp;
    }

    private static String secondClean(String tmp) {
        tmp = tmp.replace("with","");
        tmp = tmp.replace("to","");
        tmp = tmp.replace("from","");
        tmp = tmp.replace("by","");
        tmp = tmp.replace("after","");
        tmp = tmp.replace("of","");
        tmp = tmp.replace("and","");
        tmp = tmp.replace("on","");
        tmp = tmp.replace("all","");
        tmp = tmp.replace("at","");
        tmp = tmp.replace("up","");
        tmp = tmp.replace("as","");
        tmp = tmp.toLowerCase();
        tmp = tmp.trim();
        if(tmp.contains(" ")){
            String tmp2 = "";
            for(String z: tmp.split(" ")){
                if(z.length()>1) tmp2+=" "+z;
            }
            tmp = tmp2.trim();
        }
        return tmp;
    }
    
    
    
    
    
    
    
}


