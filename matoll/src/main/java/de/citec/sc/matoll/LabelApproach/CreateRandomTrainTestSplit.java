package de.citec.sc.matoll.LabelApproach;


import static de.citec.sc.matoll.LabelApproach.GenerateArff.listFilesForFolder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.io.IOUtils;


/*
 * Good description for using Weka with Java:
 * http://stackoverflow.com/questions/21674522/get-prediction-percentage-in-weka-using-own-java-code-and-a-model/21678307#21678307
 */
public class CreateRandomTrainTestSplit {

    public static void main(String[] args) throws Exception {
            // TODO Auto-generated method stub

            String path_annotatedFiles = "resources/annotatedAdjectives/";
            String path_raw_files = "resources/plainAdjectives/";
            String path_to_write_arff = "adjective.arff";
            String path_weka_model = path_to_write_arff.replace(".arff", ".model");
            String path_to_wordnet = "/Users/swalter/Backup/Software/WordNet-3.0";
            String path_to_objects = "/Users/swalter/Downloads/tmp_extractPropertiesWithData/results/ontology/";
            List<AdjectiveObject> annotated = readCSV(path_annotatedFiles);
            List<AdjectiveObject> adjectives = readCSV(path_raw_files);
            joinAnnotation(annotated,adjectives);
            List<AdjectiveObject> postive = new ArrayList<>();
            List<AdjectiveObject> negative = new ArrayList<>();
            for(AdjectiveObject a : adjectives){
                if(a.getAnnotation().contains("0")) negative.add(a);
                if(a.getAnnotation().contains("1")) postive.add(a);
            }
            System.out.println(negative.size());
            System.out.println(postive.size());
            List<AdjectiveObject> train = new ArrayList<>();
            List<AdjectiveObject> test = new ArrayList<>();
            
            /*
            Train
            */
            int counter = 0;
            Set<Integer> negative_ids = new HashSet<>();
            do{
                int value = (int) (Math.random() * negative.size());
                if(!negative_ids.contains(value)){
                    negative_ids.add(value);
                    counter+=1;
                    train.add(negative.get(value));
                }
                
            }
            while(counter<525);
            
            counter = 0;
            Set<Integer> postive_ids = new HashSet<>();
            do{
                int value = (int) (Math.random() * postive.size());
                if(!postive_ids.contains(value)){
                    postive_ids.add(value);
                    counter+=1;
                    train.add(postive.get(value));
                }
                
            }
            while(counter<525);
            
            /*
            Test
            */
            counter =0;
            for(int i=0;i<negative.size();i++){
                if(!negative_ids.contains(i)){
                    if(counter<525){
                        test.add(negative.get(i));
                    }
                    counter+=1;
                }
            }
            counter = 0;
            for(int i=0;i<postive.size();i++){
                if(!postive_ids.contains(i)) {
                    if(counter<525){
                        test.add(postive.get(i));
                    }
                    counter+=1;
                }
            }
            System.out.println(train.size());
            System.out.println(test.size());
            exportAdjectiveEntries(train,"resources/train_split_adjectives.tsv");
            exportAdjectiveEntries(test,"resources/test_split_adjectives.tsv");
    }
        
    private static List<AdjectiveObject> readCSV(String path_normalPath2) {
        //System.out.println(path_normalPath2);
        List<AdjectiveObject> adj_list = new ArrayList<AdjectiveObject>();
        for(File file : listFilesForFolder(new File(path_normalPath2))){
                try {
                        //System.out.println(file.toString());
                        adj_list.addAll(readCSV(file));
                } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                }
        }
        return adj_list;
    }
	
    private static void joinAnnotation(List<AdjectiveObject> annotated,
			List<AdjectiveObject> adjectives) {
        for(AdjectiveObject adjectiveobject : adjectives){
                String signature = adjectiveobject.getAdjectiveTerm()+adjectiveobject.getObject()+adjectiveobject.getUri();
                for(AdjectiveObject annoadjective : annotated){
                        String anno_signature = annoadjective.getAdjectiveTerm()+annoadjective.getObject()+annoadjective.getUri();
                        if(signature.equals(anno_signature))adjectiveobject.setAnnotation(annoadjective.getAnnotation());
                }
        }		
    }
    private static List<AdjectiveObject> readCSV(File file) throws IOException {
        List<AdjectiveObject> adj_list = new ArrayList<AdjectiveObject>();
        String everything = "";
        FileInputStream inputStream = new FileInputStream(file);
        try {
               everything = IOUtils.toString(inputStream);
        } finally {
            inputStream.close();
        }

        String[] adjectives = everything.split("\n");
        int counter = 0;
        for(String line: adjectives){

                String[] tmp = line.split("\t");
                AdjectiveObject adjectiveobject = new AdjectiveObject();
                adjectiveobject.setAnnotation(tmp[0]);
                adjectiveobject.setAdjectiveTerm(tmp[1]);
                adjectiveobject.setUri(tmp[2]);
                adjectiveobject.setObject(tmp[3]);
                try{
                        adjectiveobject.setFrequency(Integer.valueOf(tmp[4]));
                        adjectiveobject.setPattern(tmp[5]);
                        adjectiveobject.setPos_Pattern(tmp[6]);
                        adjectiveobject.setPos_adj_Pattern(tmp[7]);
                        adjectiveobject.setRatio(Double.valueOf(tmp[8]));
                        adjectiveobject.setRatio_pattern(Double.valueOf(tmp[9]));
                        adjectiveobject.setRatio_pos_pattern(Double.valueOf(tmp[10]));
                        adjectiveobject.setNormalizedFrequency(Double.valueOf(tmp[11]));
                        adjectiveobject.setNormalizedObjectFrequency(Double.valueOf(tmp[12]));
                        adjectiveobject.setNormalizedObjectOccurrences(Double.valueOf(tmp[13]));
                        adjectiveobject.setSublabel(tmp[14]);
                        adjectiveobject.setNld(Double.valueOf(tmp[15]));
                        if(tmp[16].contains("1")) adjectiveobject.setFirstPosition(true);
                        else adjectiveobject.setFirstPosition(false);
                        if(tmp[17].contains("1")) adjectiveobject.setLastPosition(true);
                        else adjectiveobject.setLastPosition(false);
                        adjectiveobject.setPosition(Integer.valueOf(tmp[18]));
                        //adjectiveobject.setEntropy(Double.valueOf(tmp[19]));
                }
                catch(Exception e){
                        /*
                         * In case of loading the annotated adjective, the part above will fail; because not all features are given
                         */
                        //e.printStackTrace();
                }

                adj_list.add(adjectiveobject);
                ////System.out.println("Annotation:"+adjectiveobject.getAnnotation());
                ////System.out.println();
            }


        return adj_list;
    }
    
    
    
    private static void exportAdjectiveEntries(List<AdjectiveObject> adjectiveentries, String path) throws FileNotFoundException{
        PrintWriter writer = new PrintWriter(path);
        StringBuilder string_builder = new StringBuilder();
        for(AdjectiveObject a : adjectiveentries){
            string_builder.append(a.getAnnotation());
            string_builder.append("\t");

            string_builder.append(a.getAdjectiveTerm());
            string_builder.append("\t");

            string_builder.append(a.getUri());
            string_builder.append("\t");

            string_builder.append(a.getObject());
            string_builder.append("\t");

            string_builder.append(a.getFrequency());
            string_builder.append("\t");

            string_builder.append(a.getPattern());
            string_builder.append("\t");

            string_builder.append(a.getPos_Pattern());
            string_builder.append("\t");

            string_builder.append(a.getPos_adj_Pattern());
            string_builder.append("\t");

            string_builder.append(a.getRatio());
            string_builder.append("\t");

            string_builder.append(a.getRatio_pattern());
            string_builder.append("\t");

            string_builder.append(a.getRatio_pos_pattern());
            string_builder.append("\t");

            string_builder.append(a.getNormalizedFrequency());
            string_builder.append("\t");

            string_builder.append(a.getNormalizedObjectFrequency());
            string_builder.append("\t");

            string_builder.append(a.getNormalizedObjectOccurrences());
            string_builder.append("\t");

            string_builder.append(a.getSublabel());
            string_builder.append("\t");

            string_builder.append(a.getNld());
            string_builder.append("\t");

            if(a.isFirstPosition()) string_builder.append("1");
            else string_builder.append("0");
            string_builder.append("\t");

            if(a.isLastPosition()) string_builder.append("1");
            else string_builder.append("0");
            string_builder.append("\t");

            string_builder.append(a.getPosition());
            string_builder.append("\n");
        }
        writer.write(string_builder.toString());
        writer.close();
    }
    
    /*
    For import
    			a.getAnnotation(tmp[0]);
			a.getAdjectiveTerm(tmp[1]);
			a.getUri(tmp[2]);
			a.getObject(tmp[3]);
                        a.getFrequency(Integer.valueOf(tmp[4]));
                        a.getPattern(tmp[5]);
                        a.getPos_Pattern(tmp[6]);
                        a.getPos_adj_Pattern(tmp[7]);
                        a.getRatio(Double.valueOf(tmp[8]));
                        a.getRatio_pattern(Double.valueOf(tmp[9]));
                        a.getRatio_pos_pattern(Double.valueOf(tmp[10]));
                        a.getNormalizedFrequency(Double.valueOf(tmp[11]));
                        a.getNormalizedObjectFrequency(Double.valueOf(tmp[12]));
                        a.getNormalizedObjectOccurrences(Double.valueOf(tmp[13]));
                        a.getSublabel(tmp[14]);
                        a.getNld(Double.valueOf(tmp[15]));
                        a.getFirstPosition(true);
                        a.getLastPosition(true);
                        a.getPosition(Integer.valueOf(tmp[18]));
    */
	


}