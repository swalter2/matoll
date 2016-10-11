package de.citec.sc.matoll.patterns.english_modified;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

import de.citec.sc.matoll.core.Language;
import de.citec.sc.matoll.core.Lexicon;
import de.citec.sc.matoll.core.Sentence;
import de.citec.sc.matoll.patterns.SparqlPattern;
import de.citec.sc.matoll.patterns.Templates;
import java.util.List;
import org.apache.jena.query.ResultSetFormatter;




public class Modified_SparqlPattern_EN_Intransitive_PP extends SparqlPattern {

	Logger logger = LogManager.getLogger(Modified_SparqlPattern_EN_Intransitive_PP.class.getName());
	

        @Override
        public String getQuery() {
            String query = "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "
                    + "SELECT ?lemma ?prt_form ?prep ?dobj_form ?e1_arg ?e2_arg  WHERE {"
                    + "{?y <conll:cpostag> \"VB\" .}"
                    + "UNION"
                    + "{?y <conll:cpostag> \"VBD\" .}"
                    + "UNION"
                    + "{?y <conll:cpostag> \"VBP\" .}"
                    + "UNION"
                    + "{?y <conll:cpostag> \"VBZ\" .}"
                    + "?y <conll:form> ?lemma . "
                    
                    
                    + "?e1 <conll:head> ?y . "
                    + "?e1 <conll:deprel> ?deprel. "
                    
                    + "{?e1 <own:senseArg> ?e1_arg. }"
                    + " UNION "
                    + "{"
                    + "?e1_tmp <conll:head> ?e1 ."
                    + "?e1_tmp <conll:deprel> \"nn\". "
                    + "?e1_tmp <own:senseArg> ?e1_arg."
                    + "}"
                    
                    
                    + "FILTER regex(?deprel, \"subj\") ."
                    + "?p <conll:head> ?y . "
                    + "?p <conll:deprel> \"prep\" . "
                    + "?p <conll:form> ?prep . "
                    + "?e2 <conll:head> ?p . "
                    + "?e2 <conll:deprel> \"pobj\". "
                    
                    + "{?e2 <own:senseArg> ?e2_arg. }"
                    + " UNION "
                    + "{"
                    + "?e2_tmp <conll:head> ?e2 ."
                    + "?e2_tmp <conll:deprel> \"nn\". "
                    + "?e2_tmp <own:senseArg> ?e2_arg."
                    + "}"
                    
                    
                    + "OPTIONAL {"
                        + "?dobj <conll:head> ?y . "
                        + "?dobj <conll:form> ?dobj_form ."
                        + "?dobj <conll:deprel> \"dobj\" ."
                    + "}"
                    + "OPTIONAL {"
                         + "?prt <conll:head> ?y . "
                         + "?prt <conll:form> ?prt_form ."
                         + "?prt <conll:deprel> \"prt\" ."
                    + "}"
                    + "}";
            return query;
        }
	
        @Override
	public int extractLexicalEntries(Model model, Lexicon lexicon,List<String> exported_entries) {
  
                QueryExecution qExec = QueryExecutionFactory.create(getQuery(), model) ;
                ResultSet rs = qExec.execSelect() ;
                String verb = null;
                String e1_arg = null;
                String e2_arg = null;
                String preposition = null;
                String dobj_form = null;
                String prt_form = null;
                int updated_entry = 0;

                 while ( rs.hasNext() ) {
                         QuerySolution qs = rs.next();
                         try{
                                 verb = qs.get("?lemma").toString();
                                 e1_arg = qs.get("?e1_arg").toString();
                                 e2_arg = qs.get("?e2_arg").toString();	
                                 preposition = qs.get("?prep").toString();
                                 try{
                                     dobj_form = qs.get("?dobj_form").toString();
                                 }catch(Exception e){
                                    }
                                 try{
                                     prt_form = qs.get("?prt_form").toString();
                                 }catch(Exception e){
                                 }
                                 if(verb!=null && e1_arg!=null && e2_arg!=null && preposition!=null) {
                                     Sentence sentence = this.returnSentence(model);
                                     String cannonicalform = "";
                                     if(dobj_form!=null && prt_form!=null){
                                         cannonicalform = verb+" "+dobj_form +" "+prt_form;
                                     }
                                     else if(dobj_form==null && prt_form!=null){
                                         cannonicalform = verb+" "+prt_form;
                                     }
                                     else if(dobj_form!=null && prt_form==null){
                                         cannonicalform = verb+" "+dobj_form;
                                     }
                                     else{
                                         cannonicalform = verb;
                                     }
                                     Templates.getIntransitiveVerb(model, lexicon, sentence, cannonicalform, e1_arg, e2_arg, preposition, this.getReference(model), logger, this.getLemmatizer(),Language.EN,getID(),exported_entries);
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

	public String getID() {
		return "SPARQLPattern_EN_Intransitive_PP";
	}


}
