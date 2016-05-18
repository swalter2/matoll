/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.matoll.test;

import de.citec.sc.matoll.core.LexicalEntry;
import de.citec.sc.matoll.core.Lexicon;
import de.citec.sc.matoll.io.LexiconLoader;
import de.citec.sc.matoll.io.LexiconSerialization;
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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;

/**
 *
 * @author swalter
 */
public class JoinLexica {
    public static void main(String[] args) throws FileNotFoundException, IOException{
        LexiconLoader loader = new LexiconLoader();
        Lexicon lexicon1 = loader.loadFromFile("/Users/swalter/Downloads/ResultsJanuary2016/dbpedia2014Full_new_beforeTraining.ttl");
        lexicon1.setBaseURI("http://localhost:8080/");
        Lexicon lexicon2 = loader.loadFromFile("/Users/swalter/Downloads/ResultsJanuary2016/new_adjectives.ttl");
        for(LexicalEntry entry : lexicon2.getEntries()){
            lexicon1.addEntry(entry);
        }
        
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
                                        
        PatternLibrary library = new PatternLibrary();
        library.setPatterns(patterns);
        System.out.println(lexicon1.size());
        LexiconSerialization serial = new LexiconSerialization(library.getPatternSparqlMapping(),true);
        Model model = ModelFactory.createDefaultModel();
        serial.serialize(lexicon1, model);		
        FileOutputStream out = new FileOutputStream(new File("joined.ttl"));
        RDFDataMgr.write(out, model, RDFFormat.TURTLE) ;
        out.close();
        
    }
}
