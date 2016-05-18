package de.citec.sc.matoll.patterns;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.rdf.model.Model;

import de.citec.sc.matoll.core.Lexicon;
import de.citec.sc.matoll.utils.Lemmatizer;
import java.util.HashMap;
import java.util.Map;

public class PatternLibrary {

	List<SparqlPattern> Patterns;
        Map<String,String> PatternSparqlMapping = new HashMap<>();

	

	Lemmatizer Lemmatizer;
        
	
        /**
         * Initialization of PatternLibrary 
         * @param debugger 
         */
	public PatternLibrary()
	{
		Patterns = new ArrayList<SparqlPattern>();
		Lemmatizer = null;
                
	}

	
        /**
         * Sets Lemmatizer
         * @param lemmatizer Lemmatizer 
         */
	public void setLemmatizer(Lemmatizer lemmatizer)
	{
		Lemmatizer = lemmatizer;
	}
	
        /**
         * Adds pattern to list of patterns
         * @param pattern  Pattern
         */
	public void addPattern(SparqlPattern pattern)
	{
		Patterns.add(pattern);
		
		if (Lemmatizer != null)
			pattern.setLemmatizer(Lemmatizer);
	}
	
        /**
         * For each Sparql-query this function calls the extractLexicalEntries function, implemented in the pattern 
         * and adds the generated entry to the overall lexicon
         * @param model Model, containing a parsed sentence
         * @param lexicon Lexicon
         * @param pattern_counter
         */
	public boolean extractLexicalEntries(Model model, Lexicon lexicon, Map<String,Integer> pattern_counter)
	{
                boolean matched_pattern = false;
		for (SparqlPattern pattern: Patterns)
		{
			if (Lemmatizer != null)
				pattern.setLemmatizer(Lemmatizer);
			int value = pattern.extractLexicalEntries(model, lexicon);
                        if(value>0){
                            matched_pattern = true;
                        }
                        if(pattern_counter.containsKey(pattern.getID())){
                            pattern_counter.put(pattern.getID(), pattern_counter.get(pattern.getID()) + value);
                        }
                        else{
                            pattern_counter.put(pattern.getID(),value);
                        }
                        
		}
                return matched_pattern;
		
	}

	public void setPatterns(List<SparqlPattern> patterns) {
		Patterns = patterns;
                patterns.stream().forEach((p) -> {
                    PatternSparqlMapping.put(p.getID(), p.getQuery());
            });
		
	}
        
        public Map<String, String> getPatternSparqlMapping() {
            return PatternSparqlMapping;
        }
	
	
}
