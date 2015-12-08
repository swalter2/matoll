package de.citec.sc.matoll.LabelApproach;



import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import de.citec.sc.matoll.core.Language;
import static de.citec.sc.matoll.core.Language.EN;

import de.citec.sc.matoll.core.LexicalEntry;
import de.citec.sc.matoll.core.Lexicon;
import de.citec.sc.matoll.core.Provenance;
import de.citec.sc.matoll.core.Reference;
import de.citec.sc.matoll.core.Restriction;
import de.citec.sc.matoll.core.Sense;
import de.citec.sc.matoll.core.SenseArgument;
import de.citec.sc.matoll.core.SimpleReference;
import de.citec.sc.matoll.core.SyntacticArgument;
import de.citec.sc.matoll.core.SyntacticBehaviour;
import de.citec.sc.matoll.io.LexiconSerialization;
import de.citec.sc.matoll.utils.OntologyImporter;
import de.citec.sc.matoll.utils.StanfordLemmatizer;
import de.citec.sc.matoll.utils.Wordnet;
import edu.stanford.nlp.process.Morphology;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import weka.classifiers.Classifier;
import weka.classifiers.functions.SMO;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

/*
 * Good description for using Weka with Java:
 * http://stackoverflow.com/questions/21674522/get-prediction-percentage-in-weka-using-own-java-code-and-a-model/21678307#21678307
 */
public class Process {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub

		String path_annotatedFiles = "resources/annotatedAdjectives/";
		String path_raw_files = "resources/plainAdjectives/";
		String path_to_write_arff = "adjective.arff";
		String path_weka_model = path_to_write_arff.replace(".arff", ".model");
		String path_to_wordnet = "/Users/swalter/Backup/Software/WordNet-3.0";
		String path_to_objects = "/Users/swalter/Downloads/tmp_extractPropertiesWithData/results/ontology/";
		/*
		 * TODO: Automatically import via maven
		 */
		
		//String path_to_tagger_model ="resources/english-left3words/english-caseless-left3words-distsim.tagger";
                String path_to_tagger_model ="resources/english-caseless-left3words-distsim.tagger";
		SMO smo = new SMO();
                smo.setOptions(weka.core.Utils.splitOptions("-M"));
                Classifier cls = smo; 
                
                final StanfordLemmatizer sl = new StanfordLemmatizer(EN);
		
		List<String> csv_output = new ArrayList<String>();
		
		OntologyImporter importer = new OntologyImporter("../dbpedia_2014.owl","RDF/XML");
		
		ExtractData adjectiveExtractor = new ExtractData(path_to_wordnet);
		
		MaxentTagger tagger = new MaxentTagger(path_to_tagger_model);
		Morphology mp = new Morphology();
                
                Wordnet wordnet = new Wordnet(path_to_wordnet);
		
		/*
		 * Overall Feature
		 */
		HashSet<String> posAdj = new HashSet<String>();
		HashSet<String> pos = new HashSet<String>();
		HashSet<String> label_3 = new HashSet<String>();
		HashSet<String> label_2 = new HashSet<String>();
		
		
		/*
		 * Lexicon
		 */
		Lexicon lexicon = new Lexicon();
                lexicon.setBaseURI("http://localhost:8080/");
		
		
		/*
		 * Generate ARFF File (Training)
		 */
		System.out.println("Generate ARFF File (Training)");
		try {
			GenerateArff.run(path_annotatedFiles, path_raw_files, path_to_write_arff,label_3,label_2,pos,posAdj);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(label_3.size());
		System.out.println(label_2.size());
		System.out.println(pos.size());
		System.out.println(posAdj.size());
		
		System.out.println("Generate model");
		/*
		 * Generate model
		 */
		try {
			generateModel(cls,path_to_write_arff);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Load model for prediction");
		/*
		 * Load model for prediction
		 */
		Prediction prediction = new Prediction(path_weka_model);
		
		
		/*
		 * Get Test Data features
		 */
		
		System.out.println("Done preprosessing");
		
		HashSet<String> properties = importer.getProperties();
                runWornetPropertyApproach(properties,lexicon,wordnet,sl);
		runAdjectiveApproach(properties,adjectiveExtractor,posAdj,pos,label_3,label_2, prediction,tagger, lexicon, mp,path_to_objects,csv_output);
                
                HashSet<String> classes = importer.getClasses();
		runWornetClassApproach(classes,lexicon,wordnet);
		
		Model model = ModelFactory.createDefaultModel();
		
		LexiconSerialization serializer = new LexiconSerialization(false);
		
		serializer.serialize(lexicon, model);
		
		FileOutputStream out = new FileOutputStream(new File("new_adjectives.ttl"));
		
		RDFDataMgr.write(out, model, RDFFormat.TURTLE) ;
		
		
		/*
		 * write csv
		 */
		
		PrintWriter writer;
		try {
			writer = new PrintWriter(path_to_write_arff.replace(".arff", ".csv"));
			for(String line:csv_output) writer.print(line);
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
		
		 
		 
		 
		
	}

	private static void createRestrictionClassEntry(Lexicon lexicon,String adjective, String object_uri, String uri, int frequency, double distribution) {
                LexicalEntry entry = new LexicalEntry(Language.EN);
		entry.setCanonicalForm(adjective);
                
		
		Sense sense = new Sense();
                Reference ref = new Restriction(lexicon.getBaseURI()+"RestrictionClass_"+frag(uri)+"_"+frag(object_uri),object_uri,uri);
		//Reference ref = new Restriction(lexicon.getBaseURI()+"RestrictionClass_"+frag(uri)+"_"+frag(adjective),object_uri,uri);
                //Reference ref = new Restriction(lexicon.getBaseURI()+"RestrictionClass",object_uri,uri);

                sense.setReference(ref);
                
                //System.out.println(adjective);
                entry.setURI(lexicon.getBaseURI()+"LexicalEntry_"+adjective.replace(" ","_")+"_as_AdjectiveRestriction");
				
		entry.setPOS("http://www.lexinfo.net/ontology/2.0/lexinfo#adjective");
		
		SyntacticBehaviour behaviour = new SyntacticBehaviour();
		
		behaviour.setFrame("http://www.lexinfo.net/ontology/2.0/lexinfo#AdjectivePredicativeFrame");
				
		behaviour.add(new SyntacticArgument("http://www.lexinfo.net/ontology/2.0/lexinfo#copulativeSubject","subject",null));
		
		sense.addSenseArg(new SenseArgument("http://lemon-model.net/lemon#subjOfProp","subject"));
		
		entry.addSyntacticBehaviour(behaviour,sense);

				
		SyntacticBehaviour behaviour2 = new SyntacticBehaviour();
		
		behaviour2.setFrame("http://www.lexinfo.net/ontology/2.0/lexinfo#AdjectiveAttributiveFrame");
				
		behaviour2.add(new SyntacticArgument("http://www.lexinfo.net/ontology/2.0/lexinfo#attributiveArg","object",null));
		
		sense.addSenseArg(new SenseArgument("http://lemon-model.net/lemon#objOfProp","object"));
		
		entry.addSyntacticBehaviour(behaviour2,sense);
                
                //entry.addSense(sense);
                
                Provenance provenance = new Provenance();
		
		//provenance.setAgent("Distribution");
		provenance.setConfidence(distribution);
		
		//provenance.setAgent("Frequency");
                provenance.setFrequency(frequency);
		
		entry.addProvenance(provenance,sense);
		
		if(distribution>=0.5 && isAlpha(adjective) && isAlpha(frag(object_uri))){
                    lexicon.addEntry(entry);
                }
		
	}
        
         private static boolean isAlpha(String label) {
             label = label.replace("-","");
             label = label.replace("_", "");
             label = label.replace(" ","");
            char[] chars = label.toCharArray();

            for (char c : chars) {
                if(!Character.isLetter(c)) {
                    System.out.println("false Label:"+label);
                    return false;
                }
            }

            return true;
        }

	private static void writeSingleArffFile(String path, String arff_prefix,
			AdjectiveObject adjectiveobject,HashSet<String> subLabelList,
			HashSet<String> subLabelList_2,
			HashSet<String> posPatternList,HashSet<String> posAdjPatternList) {
		String line = Double.toString(adjectiveobject.getNormalizedFrequency())
				+","+Double.toString(adjectiveobject.getNormalizedObjectFrequency())
				+","+Double.toString(adjectiveobject.getNormalizedObjectOccurrences())
				+","+Double.toString(adjectiveobject.getRatio())
				+","+Double.toString(adjectiveobject.getRatio_pattern())
				+","+Double.toString(adjectiveobject.getRatio_pos_pattern())
				//+","+Double.toString(adjectiveobject.getEntropy())
				+","+Integer.toString(adjectiveobject.getPosition());
		if(adjectiveobject.isFirstPosition())line+=","+"1";
		else line+=","+"0";
		if(adjectiveobject.isLastPosition())line+=","+"1";
		else line+=","+"0";
		line+=","+Double.toString(adjectiveobject.getNld());
		for(String label:subLabelList){
			if(adjectiveobject.getSublabel().equals(label))line+=","+"1";
			else line+=","+"0";
		}
		for(String label:subLabelList_2){
			if(adjectiveobject.getSublabel_2().equals(label))line+=","+"1";
			else line+=","+"0";
		}
		for(String pospattern:posPatternList){
			if(adjectiveobject.getPos_Pattern().equals(pospattern))line+=","+"1";
			else line+=","+"0";
		}
		for(String posadjpattern:posAdjPatternList){
			if(adjectiveobject.getPos_adj_Pattern().equals(posadjpattern))line+=","+"1";
			else line+=","+"0";
		}
		/*
		 * we test always, if the line is true
		 */
		line+=",1";
		arff_prefix+=line;
		PrintWriter writer;
		try {
			writer = new PrintWriter(path);
			writer.println(arff_prefix);
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	private static String createArffPrefix(HashSet<String> subLabelList,HashSet<String> subLabelList_2,
			HashSet<String> posPatternList,HashSet<String> posAdjPatternList) {
		String first_line =""
				+"@relation adjectives\n"
				+"@attribute 'normalizedFrequency' numeric\n"
				+"@attribute 'normalizedObjectFrequency' numeric\n"
				+"@attribute 'normalizedObjectOccourences' numeric\n"
				+"@attribute 'ratio' numeric\n"
				+"@attribute 'ratioPattern' numeric\n"
				+"@attribute 'ratioPosPattern' numeric\n"
				//+"@attribute 'entropy' numeric\n"
				+"@attribute 'position' numeric\n"
				+"@attribute 'firstPosition' {0,1}\n"
				+"@attribute 'lastPosition' {0,1}\n"
				+"@attribute 'nld' numeric\n";
			int counter=0;
			for(String label:subLabelList){
				counter+=1;
				first_line+="@attribute 'l"+Integer.toString(counter)+"' {0,1}\n";
			}
			for(String label:subLabelList_2){
				counter+=1;
				first_line+="@attribute 'l2"+Integer.toString(counter)+"' {0,1}\n";
			}
			for(String label:posPatternList){
				counter+=1;
				first_line+="@attribute 'p"+Integer.toString(counter)+"' {0,1}\n";
			}
			for(String label:posAdjPatternList){
				counter+=1;
				first_line+="@attribute 'pa"+Integer.toString(counter)+"' {0,1}\n";
			}
			
			first_line+="@attribute 'class' {0,1}\n"
					+"@data\n";
			
			
			return first_line;
	}

	private static void generateModel(Classifier cls,String path_to_arff) throws FileNotFoundException, IOException {
		 Instances inst = new Instances(new BufferedReader(new FileReader(path_to_arff)));
		 inst.setClassIndex(inst.numAttributes() - 1);
		 try {
			cls.buildClassifier(inst);
			// serialize model
			 ObjectOutputStream oos = new ObjectOutputStream(
			                            new FileOutputStream(path_to_arff.replace(".arff", ".model")));
			 oos.writeObject(cls);
			 oos.flush();
			 oos.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		 
		
	}
        
        private static String frag(String uri) {
            
            uri = uri.replace("=","");
            uri = uri.replace("!","");
            uri = uri.replace("?","");
            uri = uri.replace("*","");
            uri = uri.replace(",","");
            uri = uri.replace("(","");
            uri = uri.replace(")","");
            uri = uri.replace(">","");
            uri = uri.replace("<","");
            uri = uri.replace("|"," ");
            String  pattern =  ".+(/|#)(\\w+)";
            Matcher matcher = (Pattern.compile(pattern)).matcher(uri);
        
            while (matcher.find()) {
                  return matcher.group(2).replace(" ","_");
            }
            
            return uri.replace(" ","_");
        }

    private static void runAdjectiveApproach(HashSet<String> properties,ExtractData adjectiveExtractor,
            HashSet<String> posAdj, HashSet<String> pos, HashSet<String> label_3, HashSet<String> label_2, 
            Prediction prediction,MaxentTagger tagger, Lexicon lexicon, Morphology mp, String path_to_objects,
            List<String> csv_output ) {
        int counter = 0;
        int uri_counter = 0;
        int uri_used = 0;
        String arff_prefix = createArffPrefix(label_2, label_3, pos, posAdj);
        for(String uri:properties){
            uri_counter+=1;
            System.out.println("URI:"+uri);
            System.out.println(uri_counter+"/"+properties.size());
            try{
                List<AdjectiveObject> object_list = adjectiveExtractor.start(path_to_objects, uri, tagger, mp);
                System.out.println(object_list.size());
                if(object_list.size()>0)uri_used+=1;
                for(AdjectiveObject adjectiveObject : object_list){
                    /*
                     * ignore "adjectives", which start with a digit
                     */
                    if(adjectiveObject.isAdjective() && !Character.isDigit(adjectiveObject.getAdjectiveTerm().charAt(0))){
                            String tmp = "/tmp/tmp.arff";
                            writeSingleArffFile(tmp,arff_prefix,adjectiveObject,label_2, label_3, pos, posAdj);
                            /*
                             * Load instances to predict.
                             */
                             ArffLoader loader = new ArffLoader();
                             loader.setFile(new File(tmp));
                             Instances structure = loader.getStructure();
                             structure.setClassIndex(structure.numAttributes() - 1);

                             Instance current;
                             while ((current = loader.getNextInstance(structure)) != null){
                                 /*
                                  * predict
                                  */
                                 HashMap<Integer, Double> result = prediction.predict(current);
                                 for(int key : result.keySet()){
                                    if(key==1){
                                         counter+=1;
                                         /*System.out.println("Add to lexica");
                                         System.out.println("Adjective:"+adjectiveObject.getAdjectiveTerm());
                                         System.out.println("Object:"+adjectiveObject.getObject());
                                         System.out.println("Frequency:"+adjectiveObject.getFrequency());
                                         System.out.println();*/
                                         try{
                                             createRestrictionClassEntry(lexicon,adjectiveObject.getAdjectiveTerm(),adjectiveObject.getObjectURI(),uri, adjectiveObject.getFrequency(),result.get(key));
                                            csv_output.add(adjectiveObject.getAdjectiveTerm()+";"+adjectiveObject.getObject()+";"+uri+"\n");
                                         }
                                         catch(Exception e){
                                                e.printStackTrace();
                                         }
                                    }
//                                    else{
//                                        System.out.println("Preidction for "+adjectiveObject.getAdjectiveTerm()+" was "+key);
//                                    }
                                }


                            }
                    }
//                    else{
//                        System.out.println("no entry created for adjectiveObject:"+adjectiveObject.getAdjectiveTerm()+"DONE");
//                        System.out.println("adjectiveObject.isAdjective():"+adjectiveObject.isAdjective());
//                        System.out.println("Character.isDigit(adjectiveObject.getAdjectiveTerm().charAt(0)):"+Character.isDigit(adjectiveObject.getAdjectiveTerm().charAt(0)));
//                        System.out.println("adjectiveObject.isAdjective() && !Character.isDigit(adjectiveObject.getAdjectiveTerm().charAt(0)):"+(adjectiveObject.isAdjective() && !Character.isDigit(adjectiveObject.getAdjectiveTerm().charAt(0))));
//                        System.out.println("");
//                        System.out.println("");
//                    }
                    
                }
            }
            catch(Exception e){
                    e.printStackTrace();
            }
        }
     
        System.out.println("Properties:"+Integer.toString(properties.size()));
        System.out.println("Created entries:"+Integer.toString(counter));
        System.out.println("Average Entries per Property:"+Double.toString((double) counter/properties.size()));
        System.out.println("Properties with Data:"+Integer.toString(uri_used));
        System.out.println("Average Entries per Property with data:"+Double.toString((double) counter/uri_used));
                
    }

    private static void runWornetClassApproach(HashSet<String> classes, Lexicon lexicon, Wordnet wordnet) {
        for(String uri : classes){
            String[] tmp = uri.split("/");
            String label = tmp[tmp.length-1].toLowerCase();
            label = label.replace("_"," ");
            Set<String> canonicalForms = new HashSet<>();
            canonicalForms.add(label);
            canonicalForms.addAll(wordnet.getAllSynonyms(label));
            for(String c : canonicalForms){
                c = c.replace("_"," ");
                createWordnetClassEntry(c,lexicon,uri);
            }
            
        }
    }

    private static void createWordnetClassEntry(String label, Lexicon lexicon, String uri) {
        LexicalEntry entry = new LexicalEntry(Language.EN);
        entry.setCanonicalForm(label);


        Sense sense = new Sense();
        Reference ref = new SimpleReference(uri);
        sense.setReference(ref);

        Provenance provenance = new Provenance();
        provenance.setFrequency(1);

        sense.setReference(ref);

        //System.out.println(adjective);
        entry.setURI(lexicon.getBaseURI()+"LexicalEntry_"+label.replace(" ","_")+"_as_WordnetClassEntry");

        entry.setPOS("http://www.lexinfo.net/ontology/2.0/lexinfo#commonNoun");

        SyntacticBehaviour behaviour = new SyntacticBehaviour();
        behaviour.setFrame("http://www.lexinfo.net/ontology/2.0/lexinfo#NounPPFrame");

        behaviour.add(new SyntacticArgument("http://www.lexinfo.net/ontology/2.0/lexinfo#directObject","object",null));
        behaviour.add(new SyntacticArgument("http://www.lexinfo.net/ontology/2.0/lexinfo#subject","subject",null));

        sense.addSenseArg(new SenseArgument("http://lemon-model.net/lemon#subjOfProp","subject"));
        sense.addSenseArg(new SenseArgument("http://lemon-model.net/lemon#objOfProp","object"));

        entry.addSyntacticBehaviour(behaviour,sense);

        entry.addProvenance(provenance,sense);

        lexicon.addEntry(entry);
        
    }
    
    private static void createWordnetNounEntry(String label, Lexicon lexicon, String uri) {
        LexicalEntry entry = new LexicalEntry(Language.EN);
        entry.setCanonicalForm(label);


        Sense sense = new Sense();
        Reference ref = new SimpleReference(uri);
        sense.setReference(ref);

        Provenance provenance = new Provenance();
        provenance.setFrequency(1);

        sense.setReference(ref);

        //System.out.println(adjective);
        entry.setURI(lexicon.getBaseURI()+"LexicalEntry_"+label.replace(" ","_")+"_as_WordnetNounEntry");

        entry.setPOS("http://www.lexinfo.net/ontology/2.0/lexinfo#commonNoun");

        SyntacticBehaviour behaviour = new SyntacticBehaviour();
        behaviour.setFrame("http://www.lexinfo.net/ontology/2.0/lexinfo#NounPPFrame");

        behaviour.add(new SyntacticArgument("http://www.lexinfo.net/ontology/2.0/lexinfo#directObject","object",null));
        behaviour.add(new SyntacticArgument("http://www.lexinfo.net/ontology/2.0/lexinfo#subject","subject",null));

        sense.addSenseArg(new SenseArgument("http://lemon-model.net/lemon#subjOfProp","subject"));
        sense.addSenseArg(new SenseArgument("http://lemon-model.net/lemon#objOfProp","object"));

        entry.addSyntacticBehaviour(behaviour,sense);

        entry.addProvenance(provenance,sense);

        lexicon.addEntry(entry);
        
    }
    
    private static void createWordnetVerbEntry(String label, Lexicon lexicon, String uri) {
        LexicalEntry entry = new LexicalEntry(Language.EN);
        entry.setCanonicalForm(label);


        Sense sense = new Sense();
        Reference ref = new SimpleReference(uri);
        sense.setReference(ref);

        Provenance provenance = new Provenance();
        provenance.setFrequency(1);

        sense.setReference(ref);

        //System.out.println(adjective);
        entry.setURI(lexicon.getBaseURI()+"LexicalEntry_"+label.replace(" ","_")+"_as_WordnetVerbEntry");

        entry.setPOS("http://www.lexinfo.net/ontology/2.0/lexinfo#verb");

        SyntacticBehaviour behaviour = new SyntacticBehaviour();
        behaviour.setFrame("http://www.lexinfo.net/ontology/2.0/lexinfo#TransitiveFrame");

        behaviour.add(new SyntacticArgument("http://www.lexinfo.net/ontology/2.0/lexinfo#directObject","object",null));
        behaviour.add(new SyntacticArgument("http://www.lexinfo.net/ontology/2.0/lexinfo#subject","subject",null));

        sense.addSenseArg(new SenseArgument("http://lemon-model.net/lemon#subjOfProp","subject"));
        sense.addSenseArg(new SenseArgument("http://lemon-model.net/lemon#objOfProp","object"));

        entry.addSyntacticBehaviour(behaviour,sense);

        entry.addProvenance(provenance,sense);

        lexicon.addEntry(entry);
        
    }
    
    private static void createWordnetAdjectiveEntry(String label, Lexicon lexicon, String uri) {
        LexicalEntry entry = new LexicalEntry(Language.EN);
        entry.setCanonicalForm(label);


        Sense sense = new Sense();
        Reference ref = new SimpleReference(uri);
        sense.setReference(ref);

        Provenance provenance = new Provenance();
        provenance.setFrequency(1);

        sense.setReference(ref);

        //System.out.println(adjective);
        entry.setURI(lexicon.getBaseURI()+"LexicalEntry_"+label.replace(" ","_")+"_as_WordnetAdjectiveEntry");

        entry.setPOS("http://www.lexinfo.net/ontology/2.0/lexinfo#adjective");

        SyntacticBehaviour behaviour = new SyntacticBehaviour();
        behaviour.setFrame("http://www.lexinfo.net/ontology/2.0/lexinfo#AdjectivePredicateFrame");

        behaviour.add(new SyntacticArgument("http://www.lexinfo.net/ontology/2.0/lexinfo#prepositionalObject","object",null));
        behaviour.add(new SyntacticArgument("http://www.lexinfo.net/ontology/2.0/lexinfo#subject","subject",null));

        sense.addSenseArg(new SenseArgument("http://lemon-model.net/lemon#subjOfProp","subject"));
        sense.addSenseArg(new SenseArgument("http://lemon-model.net/lemon#objOfProp","object"));

        entry.addSyntacticBehaviour(behaviour,sense);

        entry.addProvenance(provenance,sense);

        lexicon.addEntry(entry);
        
    }

    private static void runWornetPropertyApproach(HashSet<String> properties, Lexicon lexicon, Wordnet wordnet, StanfordLemmatizer sl) {
        for(String uri : properties){
            boolean b_nouns = false;
            boolean b_adverbs = false;
            boolean b_verbs = false;
            boolean b_adjectives = false;
            String[] tmp = uri.split("/");
            String label = tmp[tmp.length-1].toLowerCase();
            label = label.replace("_"," ");
            String lemma = sl.getLemma(label);
            
            Set<String> canonicalForms = new HashSet<>();
            canonicalForms.addAll(wordnet.getNounSynonyms(lemma));
            if(!canonicalForms.isEmpty()){
                b_nouns = true;
                canonicalForms.add(lemma);
                for(String c : canonicalForms){
                    c = c.replace("_"," ");
                    createWordnetNounEntry(c,lexicon,uri);
                }
            }
            
            canonicalForms.clear();
            canonicalForms.addAll(wordnet.getAdjectiveSynonyms(label));
            if(!canonicalForms.isEmpty()){
                b_adjectives = true;
                canonicalForms.add(label);
                for(String c : canonicalForms){
                    c = c.replace("_"," ");
                    createWordnetAdjectiveEntry(c,lexicon,uri);
                }
            }
            
            canonicalForms.clear();
            canonicalForms.addAll(wordnet.getAdverbSynonyms(lemma));
            if(!canonicalForms.isEmpty()){
                b_adverbs = true;
                canonicalForms.add(lemma);
                for(String c : canonicalForms){
                    c = c.replace("_"," ");
                    createWordnetVerbEntry(c,lexicon,uri);
                }
            }
            
            canonicalForms.clear();
            canonicalForms.addAll(wordnet.getVerbSynonyms(lemma));
            if(!canonicalForms.isEmpty()){
                b_verbs = true;
                canonicalForms.add(lemma);
                for(String c : canonicalForms){
                    c = c.replace("_"," ");
                    createWordnetVerbEntry(c,lexicon,uri);
                }
            }
            
            if(!b_nouns && !b_adverbs && !b_verbs && !b_adjectives){
                 createWordnetVerbEntry(lemma,lexicon,uri);
                 createWordnetAdjectiveEntry(label,lexicon,uri);
                 createWordnetNounEntry(lemma,lexicon,uri);
            }
            
            
        }
    }

}