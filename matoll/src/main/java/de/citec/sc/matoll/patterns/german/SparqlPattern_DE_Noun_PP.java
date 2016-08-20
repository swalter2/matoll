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

public class SparqlPattern_DE_Noun_PP extends SparqlPattern{

	
	Logger logger = LogManager.getLogger(SparqlPattern_DE_Noun_PP.class.getName());
	
        /*
        NounPP
        */
        @Override
        public String getQuery() {
            String query = "SELECT ?lemma ?prep ?e1_arg ?e2_arg  WHERE {"
                            + "?e1 <conll:deprel> \"subj\" . "
                            + "?e1 <conll:head> ?sein. "
                            + "?sein <conll:lemma> \"sein\". "
                            + "?noun <conll:lemma> ?lemma . "
                            + "?noun <conll:head> ?sein . "
                            + "?noun <conll:cpostag> \"N\" . "
                            + "?noun <conll:deprel> \"pred\" . "
                            + "?preposition <conll:head> ?noun ."
                            + "?preposition <conll:cpostag> \"PREP\" . "
                            + "?preposition <conll:deprel> \"pp\" ."
                            + "?preposition <conll:lemma> ?prep ."
                            + "?e2 <conll:deprel> \"pn\" . "
                            + "?e2 <conll:head> ?preposition. "
                            + "?e1 <own:senseArg> ?e1_arg. "
                            + "?e2 <own:senseArg> ?e2_arg. "
                            + "}";
            return query;
        }
	
	
	@Override
	public String getID() {
		return "SparqlPattern_DE_Noun_PP";
	}

	@Override
	public int extractLexicalEntries(Model model, Lexicon lexicon,List<String> exported_entries) {

		QueryExecution qExec = QueryExecutionFactory.create(getQuery(), model) ;
                ResultSet rs = qExec.execSelect() ;
                String noun = null;
                String e1_arg = null;
                String e2_arg = null;
                String preposition = null;
                int updated_entry = 0;
                 while ( rs.hasNext() ) {
                     QuerySolution qs = rs.next();


                     try{
                             noun = qs.get("?lemma").toString();
                             e1_arg = qs.get("?e1_arg").toString();
                             e2_arg = qs.get("?e2_arg").toString();	
                             preposition = qs.get("?prep").toString();
                             if(noun!=null && e1_arg!=null && e2_arg!=null && preposition!=null) {
                                 Sentence sentence = this.returnSentence(model);
                                 Templates.getNounWithPrep(model, lexicon, sentence, noun, e1_arg, e2_arg, preposition, this.getReference(model), logger, this.getLemmatizer(),Language.DE,getID(),exported_entries);
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
