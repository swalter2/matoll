package de.citec.sc.matoll.test;



import de.citec.sc.matoll.core.LexicalEntry;
import de.citec.sc.matoll.core.Lexicon;
import de.citec.sc.matoll.io.LexiconLoader;

import de.citec.sc.matoll.patterns.PatternLibrary;
import de.citec.sc.matoll.patterns.SparqlPattern;
import de.citec.sc.matoll.patterns.english.SparqlPattern_EN_DatatypeNoun;
import de.citec.sc.matoll.patterns.english.SparqlPattern_EN_DatatypeNoun_2;
import de.citec.sc.matoll.patterns.english.SparqlPattern_EN_Intransitive_PP;
import de.citec.sc.matoll.patterns.english.SparqlPattern_EN_Noun_PP_appos;
import de.citec.sc.matoll.patterns.english.SparqlPattern_EN_Noun_PP_copulative;
import de.citec.sc.matoll.patterns.english.SparqlPattern_EN_Noun_PP_player;
import de.citec.sc.matoll.patterns.english.SparqlPattern_EN_Noun_PP_possessive;
import de.citec.sc.matoll.patterns.english.SparqlPattern_EN_Predicative_Participle_copulative;
import de.citec.sc.matoll.patterns.english.SparqlPattern_EN_Predicative_Participle_passive;
import de.citec.sc.matoll.patterns.english.SparqlPattern_EN_Transitive_Passive;
import de.citec.sc.matoll.patterns.english.SparqlPattern_EN_Transitive_Verb;
import de.citec.sc.matoll.patterns.german.SparqlPattern_DE_Predicative_Adjective;
import de.citec.sc.matoll.patterns.german.SparqlPattern_DE_Noun_PP;
import de.citec.sc.matoll.patterns.german.SparqlPattern_DE_Noun_Possessive;
import de.citec.sc.matoll.patterns.german.SparqlPattern_DE_Noun_Possessive_b;
import de.citec.sc.matoll.patterns.german.SparqlPattern_DE_Transitive;
import de.citec.sc.matoll.patterns.german.SparqlPattern_DE_Transitive_Passive;
import de.citec.sc.matoll.patterns.german.SparqlPattern_DE_Intransitive_PP;
import de.citec.sc.matoll.patterns.german.SparqlPattern_DE_Refelexive_Transitive_PP;
import de.citec.sc.matoll.patterns.german.SparqlPattern_DE_Noun_PP_appos;
import de.citec.sc.matoll.patterns.german.SparqlPattern_DE_Noun_Possessive_appos;
import de.citec.sc.matoll.patterns.spanish.SparqlPattern_ES_Intransitive_PP;
import de.citec.sc.matoll.patterns.spanish.SparqlPattern_ES_Noun_PP_appos;
import de.citec.sc.matoll.patterns.spanish.SparqlPattern_ES_Noun_PP_appos_b;
import de.citec.sc.matoll.patterns.spanish.SparqlPattern_ES_Noun_PP_copulative;
import de.citec.sc.matoll.patterns.spanish.SparqlPattern_ES_Noun_PP_copulative_b;
import de.citec.sc.matoll.patterns.spanish.SparqlPattern_ES_Noun_PP_copulative_withHop;
import de.citec.sc.matoll.patterns.spanish.SparqlPattern_ES_Predicative_Participle_Copulative;
import de.citec.sc.matoll.patterns.spanish.SparqlPattern_ES_Predicative_Participle_Passive;
import de.citec.sc.matoll.patterns.spanish.SparqlPattern_ES_Reflexive_Transitive_PP;
import de.citec.sc.matoll.patterns.spanish.SparqlPattern_ES_Transitive;
import de.citec.sc.matoll.patterns.spanish.SparqlPattern_ES_Transitive_Reciprocal;
import de.citec.sc.matoll.patterns.spanish.SparqlPattern_ES_Transitive_passive;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author swalter
 */
public class RunAnalysis {
    
    public static void main(String[] args) throws FileNotFoundException, IOException{
        LexiconLoader loader = new LexiconLoader();
        
        Lexicon zwei = loader.loadFromFile("dbpedia2014_DE_Full_beforeTraining.ttl");
        System.out.println("Loaded first lexicon: ");
        Lexicon eins = loader.loadFromFile("dbpedia2014Full_new_beforeTraining.ttl");
        System.out.println("Loaded second lexicon");
        zwei.setBaseURI("http://localhost:8080/");
        Lexicon drei = loader.loadFromFile("dbpedia2014_ES_Full_beforeTraining.ttl");
        System.out.println("Loaded third lexicon");//System.out.println(eins.getEntries().size());
//        System.out.println(zwei.getEntries().size());
        //System.out.println(drei.getEntries().size());
//        
////        zwei.addLexicon(eins);
////        zwei.addLexicon(drei);
        for(LexicalEntry lex : eins.getEntries())zwei.addEntry(lex);
        for(LexicalEntry lex : drei.getEntries())zwei.addEntry(lex);
        System.out.println(zwei.getEntries().size());
//        
        List<SparqlPattern> patterns = new ArrayList<>();
					
        patterns.add(new SparqlPattern_EN_Intransitive_PP());
        patterns.add(new SparqlPattern_EN_Noun_PP_appos());
        patterns.add(new SparqlPattern_EN_Noun_PP_copulative());
        patterns.add(new SparqlPattern_EN_Predicative_Participle_passive());
        patterns.add(new SparqlPattern_EN_Transitive_Verb());
        patterns.add(new SparqlPattern_EN_Predicative_Participle_copulative());
        patterns.add(new SparqlPattern_EN_Transitive_Passive());
        patterns.add(new SparqlPattern_EN_Noun_PP_possessive());
        patterns.add(new SparqlPattern_EN_DatatypeNoun());
        patterns.add(new SparqlPattern_EN_DatatypeNoun_2());
        patterns.add((new SparqlPattern_EN_Noun_PP_player()));
        patterns.add(new SparqlPattern_DE_Predicative_Adjective());
        patterns.add(new SparqlPattern_DE_Noun_PP());
        patterns.add(new SparqlPattern_DE_Noun_Possessive());
        patterns.add(new SparqlPattern_DE_Noun_Possessive_b());
        patterns.add(new SparqlPattern_DE_Transitive());
        patterns.add(new SparqlPattern_DE_Transitive_Passive());
        //Patterns.add(new SparqlPattern_DE_Transitive_Passive_optional());
        patterns.add(new SparqlPattern_DE_Intransitive_PP());
        patterns.add(new SparqlPattern_DE_Refelexive_Transitive_PP());
        patterns.add(new SparqlPattern_DE_Noun_PP_appos());
        patterns.add(new SparqlPattern_DE_Noun_Possessive_appos());					
        patterns.add(new SparqlPattern_ES_Transitive());
        patterns.add(new SparqlPattern_ES_Noun_PP_copulative_b());
        patterns.add(new SparqlPattern_ES_Noun_PP_copulative_withHop());
        patterns.add(new SparqlPattern_ES_Noun_PP_copulative());
        patterns.add(new SparqlPattern_ES_Noun_PP_appos_b());
        patterns.add(new SparqlPattern_ES_Noun_PP_appos());
        patterns.add(new SparqlPattern_ES_Predicative_Participle_Copulative());
        patterns.add(new SparqlPattern_ES_Predicative_Participle_Passive());
        patterns.add(new SparqlPattern_ES_Intransitive_PP());
        patterns.add(new SparqlPattern_ES_Transitive_Reciprocal());
        patterns.add(new SparqlPattern_ES_Reflexive_Transitive_PP());
        patterns.add(new SparqlPattern_ES_Transitive_passive());
                                        
        PatternLibrary library = new PatternLibrary();
        library.setPatterns(patterns);
        
        
////        /*
////        Build lexicon only with senses with a frequency > 1
////        */
//        Lexicon lexicon = new Lexicon();
////        
//        zwei.getEntries().stream().forEach((entry) -> {
//                entry.getSenseBehaviours().keySet().stream().forEach((sense) -> {
//                    Provenance provenance = entry.getProvenance(sense);
//                        if(provenance.getFrequency()>1 && provenance.getSentences().size()>1){
//                            LexicalEntry newEntry = new LexicalEntry(entry.getLanguage());
//                            newEntry.setPOS(entry.getPOS());
//                            newEntry.setURI(entry.getURI());
//                            newEntry.addAllSyntacticBehaviour(entry.getSenseBehaviours().get(sense), sense);
//                            newEntry.addProvenance(provenance, sense);
//                            if(entry.getPreposition()!=null){
//                                newEntry.setPreposition(entry.getPreposition());
//                            }
//                            newEntry.setCanonicalForm(entry.getCanonicalForm());
//                            lexicon.addEntry(entry);
//                        }
//                });
//            });
//        System.out.println(lexicon.getEntries().size());
//        
        
        
        
        for(SparqlPattern x:patterns){
            String path = "/Users/swalter/Desktop/PatternAnalyseJanuary2016/";
            String output =  "";
            int min = 1;
            int max = 1;
            for(String s: zwei.getTopKEntriesForPattern(x.getID(),100,min,max,path)){
                output+=s+"\n";
            }
            PrintWriter writer;
            try {
                    writer = new PrintWriter(path+"TSV/"+x.getID()+"_"+Integer.toString(min)+"_"+Integer.toString(max)+".tsv");
                    writer.println("#Eintrag\t#Semantik\t#CannonicalForm\t#Preposition\t#URI\t#Sentence1\t#Sentence2\t#Sentence3\t#Sentence4\t#Sentence5\t#name\t#Frame");
                    writer.println(output);
                    writer.close();
            } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
            }
            
            
            output =  "";
            min = 2;
            max = 10;
            for(String s: zwei.getTopKEntriesForPattern(x.getID(),100,min,max,path)){
                output+=s+"\n";
            }
            try {
                    writer = new PrintWriter(path+"TSV/"+x.getID()+"_"+Integer.toString(min)+"_"+Integer.toString(max)+".tsv");
                    writer.println("#Eintrag\t#Semantik\t#CannonicalForm\t#Preposition\t#URI\t#Sentence1\t#Sentence2\t#Sentence3\t#Sentence4\t#Sentence5\t#name\t#Frame");
                    writer.println(output);
                    writer.close();
            } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
            }
            
            output =  "";
            min = 11;
            max = 100;
            for(String s: zwei.getTopKEntriesForPattern(x.getID(),100,min,max,path)){
                output+=s+"\n";
            }
            try {
                    writer = new PrintWriter(path+"TSV/"+x.getID()+"_"+Integer.toString(min)+"_"+Integer.toString(max)+".tsv");
                    writer.println("#Eintrag\t#Semantik\t#CannonicalForm\t#Preposition\t#URI\t#Sentence1\t#Sentence2\t#Sentence3\t#Sentence4\t#Sentence5\t#name\t#Frame");
                    writer.println(output);
                    writer.close();
            } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
            }
            
            
            output =  "";
            min = 101;
            max = 100000;
            for(String s: zwei.getTopKEntriesForPattern(x.getID(),100,min,max,path)){
                output+=s+"\n";
            }
            try {
                    writer = new PrintWriter(path+"TSV/"+x.getID()+"_"+Integer.toString(min)+"_"+Integer.toString(max)+".tsv");
                    writer.println("#Eintrag\t#Semantik\t#CannonicalForm\t#Preposition\t#URI\t#Sentence1\t#Sentence2\t#Sentence3\t#Sentence4\t#Sentence5\t#name\t#Frame");
                    writer.println(output);
                    writer.close();
            } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
            }
              
        }
        
//        for(Reference ref:zwei.getReferences()){
//            
//            String path = "/Users/swalter/Desktop/UriAnalyse/";
//            
//            Language language = Language.EN;
//            String output =  "";
//            int value = 0;
//            for(String s: zwei.getTopKEntriesForURI(ref.getURI(), 100,language, path)){
//                value+=Integer.valueOf(s.split("\t")[2]);
//                output+=s+"\n";
//            }
//            PrintWriter writer;
//            try {
//                    writer = new PrintWriter(path+"CSV/"+language.toString()+"_"+Integer.toString(value)+"_"+ref.getURI().replaceAll("http:\\/\\/","").replaceAll("\\/","_").replaceAll("\\.","_")+".csv");
//                    writer.println(output);
//                    writer.close();
//            } catch (FileNotFoundException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//            }
//            
//            language = Language.DE;
//            output =  "";
//            value = 0;
//            for(String s: zwei.getTopKEntriesForURI(ref.getURI(), 100,language, path)){
//                output+=s+"\n";
//                value+=Integer.valueOf(s.split("\t")[2]);
//            }
//            try {
//                    writer = new PrintWriter(path+"CSV/"+language.toString()+"_"+Integer.toString(value)+"_"+ref.getURI().replaceAll("http:\\/\\/","").replaceAll("\\/","_").replaceAll("\\.","_")+".csv");
//                    writer.println(output);
//                    writer.close();
//            } catch (FileNotFoundException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//            }
//            
//            
//            language = Language.ES;
//            output =  "";
//            value = 0;
//            for(String s: zwei.getTopKEntriesForURI(ref.getURI(), 100,language, path)){
//                output+=s+"\n";
//                value+=Integer.valueOf(s.split("\t")[2]);
//            }
//            try {
//                    writer = new PrintWriter(path+"CSV/"+language.toString()+"_"+Integer.toString(value)+"_"+ref.getURI().replaceAll("http:\\/\\/","").replaceAll("\\/","_").replaceAll("\\.","_")+".csv");
//                    writer.println(output);
//                    writer.close();
//            } catch (FileNotFoundException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//            }
//            
//            
//            
//        }
        
//        System.out.println("Write");
//        LexiconSerialization serial = new LexiconSerialization(library.getPatternSparqlMapping(),true);
//        Model model = ModelFactory.createDefaultModel();
//        serial.serialize(lexicon, model);		
//        FileOutputStream out = new FileOutputStream(new File("joined.ttl"));
//        RDFDataMgr.write(out, model, RDFFormat.TURTLE) ;
//        out.close();
        
    }
}
