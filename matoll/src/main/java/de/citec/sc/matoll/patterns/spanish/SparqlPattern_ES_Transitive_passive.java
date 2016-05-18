package de.citec.sc.matoll.patterns.spanish;

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

public class SparqlPattern_ES_Transitive_passive extends SparqlPattern{

	Logger logger = LogManager.getLogger(SparqlPattern_ES_Transitive_passive.class.getName());
	
	
	// should be subsumed by 3 or 4 or 6...
	
	// fue + es

// ser o estar
//está casado con / estuvo casado / ha actuado en...
//
//New Parse:
//1	El	el	d	DA0MS0	_	2	SPEC	_	_
//2	proyecto	proyecto	n	NCMS000	_	3	SUBJ	_	_
//3	está	estar	v	VAIP3S0	_	0	ROOT	_	_
//4	liderado	liderar	v	VMP00SM	_	3	ATR	_	_
//5	por	por	s	SPS00	_	4	BYAG	_	_
//6	Theo_de_Raadt	theo_de_raadt	n	NP00000	_	5	COMP	_	_
//7	,	,	f	Fc	_	6	punct	_	_
//8	residente	residente	n	NCCS000	_	6	_	_	_
//9	en	en	s	SPS00	_	8	MOD	_	_
//10	Calgary	calgary	n	NP00000	_	9	COMP	_	_
//11	.	.	f	Fp	_	10	punct	_	_

        @Override
        public String getQuery() {
            String query = "SELECT ?lemma ?e1_arg ?e2_arg   WHERE {"

                            + "?copula <conll:lemma> \"estar\" ."
                            + "?copula <conll:postag> ?copula_pos ."
                            + "FILTER regex(?copula_pos, \"VAIP\") ."


                            + "?participle <conll:postag> ?participle_pos . "
                            + "FILTER regex(?participle_pos, \"VMP\") ."
                            + "?participle <conll:lemma> ?lemma . "
                            + "?participle <conll:head> ?copula . "
                            + "?participle <conll:deprel> \"ATR\" . "

                            + "?e1 <conll:head> ?copula . "
                            + "?e1 <conll:deprel> \"SUBJ\". "

                            + "?p <conll:head> ?participle . "
                            + "?p <conll:postag> ?prep_pos . "
                            + "FILTER regex(?prep_pos, \"SPS\") ."
                            + "?p <conll:lemma> \"por\" . "
                            + "{?p <conll:deprel> \"BYAG\" .} UNION "
                            + "{?p <conll:deprel> \"OBLC\" .}"

                            + "?e2 <conll:head> ?p . "
                            + "?e2 <conll:deprel> \"COMP\" . "

                            + "?e1 <own:senseArg> ?e1_arg. "
                            + "?e2 <own:senseArg> ?e2_arg. "
                            + "}";
            return query;
        }
			
	@Override
	public String getID() {
		return "SPARQLPattern_ES_Transitive_passive";
	}

	@Override
	public int extractLexicalEntries(Model model, Lexicon lexicon) {
		
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
                        if(noun!=null && e1_arg!=null && e2_arg!=null) {
                            Sentence sentence = this.returnSentence(model);
                            Templates.getTransitiveVerb(model, lexicon, sentence, noun, e1_arg, e2_arg, this.getReference(model), logger, this.getLemmatizer(),Language.ES,getID());
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
