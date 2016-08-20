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
import java.util.List;

public class SparqlPattern_ES_Reflexive_Transitive_PP extends SparqlPattern{

	Logger logger = LogManager.getLogger(SparqlPattern_ES_Reflexive_Transitive_PP.class.getName());
	
	// Pattern 9 seems to work
	
//	deathplace
//	ID:f
//	property subject: Lehri
//	property object: Karachi
//	sentence:: 
//	1	Lehri	lehri	n	NP00000	_	2	SUBJ
//	2	murió	morir	v	VMIS3S0	_	0	ROOT
//	3	el	el	d	DA0MS0	_	4	SPEC
//	4	13_de_septiembre_de_2012_a_las_9	[??:13/9/2012:9.00:??]	w	W	_	2	MOD
//	5	am	ametro	n	NCMN000	_	4	COMP
//	6	en	en	s	SPS00	_	2	MOD
//	7	Karachi	karachi	n	NP00000	_	6	COMP
//	8	.	.	f	Fp	_	7	punct

//	ID:11
//	property subject: K. C. Dey
//	property object: Calcuta
//	sentence:: 
//	1	Dey	dey	n	NP00000	_	2	SUBJ
//	2	falleció	fallecer	v	VMIS3S0	_	0	ROOT
//	3	cuando	cuando	c	CS	_	2	MOD
//	4	el	el	d	DA0MS0	_	5	SPEC
//	5	obtenía	obtener	v	VMII3S0	_	3	COMP
//	6	una	uno	d	DI0FS0	_	7	SPEC
//	7	licenciatura	licenciatura	n	NCFS000	_	0	ROOT
//	8	en	en	s	SPS00	_	7	MOD
//	9	Calcuta	calcuta	n	NP00000	_	8	COMP
//	10	en	en	s	SPS00	_	7	MOD
//	11	1962	1962	z	Z	_	10	COMP
//	12	.	.	f	Fp	_	11	punct


	
	/*
	 * sentence:Actualmente Glebova vive en Tailandia y está casada con Paradorn Srichaphan . 
1	Actualmente	_	r	RG	_	3	MOD
2	Glebova	_	n	NP00000	_	3	SUBJ
3	vive	_	v	VMIP3S0	_	0	ROOT
4	en	_	s	SPS00	_	3	MOD
5	Tailandia	_	n	NP00000	_	4	COMP
Verb+prep

Neuer parse:
1	Actualmente	actualmente	r	RG	_	3	MOD	_	_
2	Glebova	glebova	n	NP00000	_	3	SUBJ	_	_
3	vive	vivir	v	VMIP3S0	_	0	ROOT	_	_
4	en	en	s	SPS00	_	3	MOD	_	_
5	Tailandia	tailandia	n	NP00000	_	4	COMP	_	_
6	y	y	c	CC	_	3	COORD	_	_
7	está	estar	v	VAIP3S0	_	6	CONJ	_	_
8	casada	casar	v	VMP00SF	_	7	ATR	_	_
9	con	con	s	SPS00	_	8	OBLC	_	_
10	Paradorn_Srichaphan	paradorn_srichaphan	n	NP00000	_	9	COMP	_	_
11	.	.	f	Fp	_	10	punct	_	_


	 */
	
	// intransitive + pp
	// Constraint: no direct object Kein Verb unterm Objekt und wenn doch mit ins lemma nehmen.
        //<Wenn "se" (siehe Beispiel papier) drunter hängt, dann ins lemma, also lemma+se
        /*
        ID:532
property subject: Manal
property subject uri: http://dbpedia.org/resource/Manal
property object: 1971
property object uri: 1971-01-01^^http://www.w3.org/2001/XMLSchema#gYear
sentence:: 
1	En	en	s	SPS00	_	5	MOD	_	_
2	1971	1971	z	Z	_	1	COMP	_	_
3	Manal	manal	n	NP00000	_	5	SUBJ	_	_
4	se	se	p	P00CN000	_	5	DO	_	_
5	separó	separar	v	VMIS3S0	_	0	ROOT	_	_
6	.	.	f	Fp	_	5	punct	_	_


        */
        @Override
        public String getQuery() {
            String query = "SELECT ?lemma ?e1_arg ?se_form ?e2_arg ?prep  WHERE {"

                            + "?verb <conll:postag> ?verb_pos . "
                            + "FILTER regex(?verb_pos, \"VMI\") ."
                            + "?verb <conll:lemma> ?lemma . "
                            + "?se <conll:head> ?verb. "
                            + "?se <conll:deprel> \"DO\"."
                            + "?se <conll:form> ?se_form. "
                            + "?se <conll:form> \"se\". "
                            + "?e1 <conll:head> ?verb . "
                            + "?e1 <conll:deprel> \"SUBJ\". "

                            + "?p <conll:head> ?verb . "
                            + "?p <conll:postag> ?prep_pos . "
                            + "FILTER regex(?prep_pos, \"SPS\") ."
                            + "?p <conll:lemma> ?prep . "
                            + "{?p <conll:deprel> \"MOD\" .}"
                            + "UNION "
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
		return "SparqlPattern_ES_Reflexive_Transitive_PP";
	}

	@Override
	public int extractLexicalEntries(Model model, Lexicon lexicon,List<String> exported_entries) {
		
		
		QueryExecution qExec = QueryExecutionFactory.create(getQuery(), model) ;
                ResultSet rs = qExec.execSelect() ;
                String verb = null;
                String e1_arg = null;
                String e2_arg = null;
                String preposition = null;
                String se_form = null;
                int updated_entry = 0;

                while ( rs.hasNext() ) {
                    QuerySolution qs = rs.next();

                    try{
                        verb = qs.get("?lemma").toString();
                        e1_arg = qs.get("?e1_arg").toString();
                        e2_arg = qs.get("?e2_arg").toString();
                        preposition = qs.get("?prep").toString();
                        try{
                            se_form = qs.get("?se_form").toString();
                        } catch(Exception e){}
                        if(verb!=null && e1_arg!=null && e2_arg!=null && preposition!=null) {
                            Sentence sentence = this.returnSentence(model);
                            if(se_form!=null){
                                Templates.getReflexiveTransitiveVerb(model, lexicon, sentence, verb+"+"+se_form, e1_arg, e2_arg, preposition, this.getReference(model), logger, this.getLemmatizer(),Language.ES,getID(),exported_entries);
                                updated_entry += 1;
                            }
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
