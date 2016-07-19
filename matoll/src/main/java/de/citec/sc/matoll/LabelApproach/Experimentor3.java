package de.citec.sc.matoll.LabelApproach;
import com.google.common.collect.Sets;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashSet;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

public class Experimentor3 {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub

		String path_input = "resources/train_split_adjectives.tsv";
                String path_to_tagger_model ="resources/english-caseless-left3words-distsim.tagger";
                
                //classifier
		RandomForest classifier = new RandomForest();
                Classifier cls = classifier; 
                //J48 classifier = new J48();
                //SMO classifier = new SMO();
                
                String classifier_name = classifier.getClass().toString().replace("class ", "");
                Set<String> feature_list = new HashSet<String>();
                //[PP, PR, Trigrams, NLD, AFP, AP, AR, POSPR, Bigrams, PAP]	89.80952380952381

                feature_list.add("Trigrams");
                feature_list.add("Bigrams");
                feature_list.add("PR");
                feature_list.add("POSPR");
                feature_list.add("AR");
                feature_list.add("PAP");
                feature_list.add("PP");
                feature_list.add("NLD");
                feature_list.add("AP");
                feature_list.add("AFP");
                
                List<String> output = new ArrayList<>();

                MaxentTagger tagger = new MaxentTagger(path_to_tagger_model);
                LabelFeature label_feature = new LabelFeature();
                label_feature.setFeature(feature_list);
                String feature_name = "feature";
                for(String s: feature_list) feature_name += "_"+s;

                File f = new File("resources/arff_combinations/"+feature_name+".arff");
                if(!f.exists()) { 
                    /*
                     * Overall Feature
                     */
                    HashSet<String> posAdj = new HashSet<String>();
                    HashSet<String> pos = new HashSet<String>();
                    HashSet<String> label_3 = new HashSet<String>();
                    HashSet<String> label_2 = new HashSet<String>();

                    try {
                            GenerateArff.run_experimentator2(path_input, feature_name+".arff",label_3,label_2,pos,posAdj,tagger,label_feature);
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
                    String path_weka_model = feature_name+".model";
                    try {
                            generateModel(cls,feature_name+".arff");
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
                    
                    List<AdjectiveObject> object_list = new ArrayList<>();
                    object_list = GenerateArff.load_adjectives_for_experimentator3("resources/test_split_adjectives.tsv", tagger);
                    int correct_annotated = 0;
                    int wrong_annotated = 0;
                    for(AdjectiveObject adjectiveObject : object_list){
                        String tmp = "/tmp/tmp.arff";
//                        writeSingleArffFile(tmp,arff_prefix,adjectiveObject,label_2, label_3, pos, posAdj);
                        List<String> lines = new ArrayList<String>();
                        List<AdjectiveObject> small_object_list = new ArrayList<>();
                        /*
                        we want to pretict that it is a correct entry
                        */
                        String original_annotation = adjectiveObject.getAnnotation();
                        adjectiveObject.setAnnotation("1");
                        small_object_list.add(adjectiveObject);
                        GenerateArff.getCsvLine(lines,small_object_list,label_2,label_3,pos,posAdj,tagger,label_feature);
                        GenerateArff.writeArff(lines,tmp,label_2,label_3,pos,posAdj,label_feature);
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
                                if(key==1 && result.get(key)>0.70){
                                     if(original_annotation.contains("1")) correct_annotated+=1;
                                     else wrong_annotated+=1;
                                }
                                else{
                                    if(original_annotation.contains("0")) correct_annotated+=1;
                                    else wrong_annotated+=1;
                                }
                            }
                        }
                    }
                    System.out.println(correct_annotated);
                    System.out.println(wrong_annotated);
                    System.out.println(object_list.size());
                    System.out.println((correct_annotated+0.0)/object_list.size());
                    System.out.println((wrong_annotated+0.0)/object_list.size());
                }
		
	}




        public static BufferedReader readDataFile(String filename) {
            BufferedReader inputReader = null;

            try {
                inputReader = new BufferedReader(new FileReader(filename));
            } catch (FileNotFoundException ex) {
                System.err.println("File not found: " + filename);
            }

            return inputReader;
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

    
}