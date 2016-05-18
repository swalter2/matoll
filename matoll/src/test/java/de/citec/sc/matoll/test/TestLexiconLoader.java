package de.citec.sc.matoll.test;


import de.citec.sc.matoll.core.LexicalEntry;
import de.citec.sc.matoll.core.Lexicon;
import de.citec.sc.matoll.core.Provenance;
import de.citec.sc.matoll.core.Reference;
import de.citec.sc.matoll.core.Sense;
import de.citec.sc.matoll.core.Sentence;
import de.citec.sc.matoll.io.LexiconLoader;
import de.citec.sc.matoll.io.LexiconSerialization;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author swalter
 */
public class TestLexiconLoader {
    
    public static void main(String[] args) throws FileNotFoundException, IOException{
        LexiconLoader loader = new LexiconLoader();
        
        Lexicon lexicon = loader.loadFromFile("/Users/swalter/Git/matoll/lexica/dbpedia_en.rdf");
        
        System.out.println("Loaded "+lexicon.size()+" entries");
        for(LexicalEntry entry : lexicon.getEntries()){
            for(Sense sense: entry.getSenseBehaviours().keySet()){
                Reference ref = sense.getReference();
                try{
                    String uri = ref.getURI();
                    if(uri.equals("http://dbpedia.org/ontology/spouse")){
                        System.out.println(entry.getCanonicalForm());
                        System.out.println(entry.getPreposition().getCanonicalForm());
                        System.out.println();
                    }
                }
                catch(Exception e){
                    
                }
                
            }
//            System.out.println("#################");
//            System.out.println();
        }
        
        LexiconSerialization serial = new LexiconSerialization(true);
        Model model = ModelFactory.createDefaultModel();
        serial.serialize(lexicon, model);		
        FileOutputStream out = new FileOutputStream(new File("spouse_new.ttl"));
        RDFDataMgr.write(out, model, RDFFormat.TURTLE) ;
        out.close();
        
    }
    
    
}
