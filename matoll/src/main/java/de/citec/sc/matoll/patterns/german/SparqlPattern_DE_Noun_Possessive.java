package de.citec.sc.matoll.patterns.german;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.citec.sc.matoll.core.Language;
import de.citec.sc.matoll.core.Lexicon;
import de.citec.sc.matoll.core.Sentence;
import de.citec.sc.matoll.patterns.SparqlPattern;
import de.citec.sc.matoll.patterns.Templates;
import java.util.List;

public class SparqlPattern_DE_Noun_Possessive extends SparqlPattern{

	
	Logger logger = LogManager.getLogger(SparqlPattern_DE_Noun_Possessive.class.getName());
	
        /*
        Noun Possessive
        */
        @Override
        public String getQuery() {
            String query = "SELECT ?lemma  ?e1_arg ?e2_arg  WHERE {"
                            + "?e1 <conll:deprel> \"subj\" . "
                            + "?e1 <conll:head> ?sein. "
                            + "?sein <conll:lemma> \"sein\". "
                            + "?noun1 <conll:lemma> ?lemma . "
                            + "?noun1 <conll:head> ?sein . "
                            + "?noun1 <conll:cpostag> \"N\" . "
                            + "?noun1 <conll:deprel> \"pred\" . "
                            + "?noun2 <conll:head> ?noun1 ."
                            + "?noun2 <conll:cpostag> \"N\" . "
                            + "?noun2 <conll:deprel> \"gmod\" ."
                            + "?e2 <conll:deprel> \"app\" . "
                            + "?e2 <conll:head> ?noun2. "
                            + "?e1 <own:senseArg> ?e1_arg. "
                            + "?e2 <own:senseArg> ?e2_arg. "
                            + "}";
            return query;
        }
	
	
	@Override
	public String getID() {
		return "SparqlPattern_DE_Noun_Possessive";
	}

	@Override
	public int extractLexicalEntries(Model model, Lexicon lexicon,List<String>exported_entries) {

		QueryExecution qExec = QueryExecutionFactory.create(getQuery(), model) ;
                ResultSet rs = qExec.execSelect() ;
                String noun = null;
                String e1_arg = null;
                String e2_arg = null;
                int updated_entry  = 0;

                 while ( rs.hasNext() ) {
                     QuerySolution qs = rs.next();

                     try{
                             noun = qs.get("?lemma").toString();
                             e1_arg = qs.get("?e1_arg").toString();
                             e2_arg = qs.get("?e2_arg").toString();
                             if(noun!=null && e1_arg!=null && e2_arg!=null) {
                                 Sentence sentence = this.returnSentence(model);
                                 Templates.getNounPossessive(model, lexicon, sentence, noun, e1_arg, e2_arg, this.getReference(model), logger, this.getLemmatizer(),Language.DE,getID(),exported_entries);
                                 updated_entry += 1;
                             }
                     }
                     catch(Exception e){
                    e.printStackTrace();
                    }
                 }

                 
                qExec.close() ;
                return updated_entry;

		
	}

}
