package de.citec.sc.matoll.LabelApproach;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import org.apache.commons.io.IOUtils;

public class GenerateArff {
	
	
	/*
    TODO: Add some new feature, for example which is the range of the property, and also create a bag of words for all terms in the object, in order to learn some "context"
    */
	
	
	public static void run(String path_annotatedFiles,String path_normalPath,String path_to_write,
			HashSet<String> subLabelList,HashSet<String> subLabelList_2,HashSet<String> posPatternList,HashSet<String> posAdjPatternList,MaxentTagger tagger) throws FileNotFoundException{
		
		
		List<AdjectiveObject> annotated = readCSV(path_annotatedFiles);
		List<AdjectiveObject> adjectives = readCSV(path_normalPath);
		joinAnnotation(annotated,adjectives);
		
		generateSubLabelList(adjectives,subLabelList,subLabelList_2);
		generatePosPatternList(adjectives,posPatternList);
		generatePosAdjPatternList(adjectives,posAdjPatternList);
		
		
		createArrf(subLabelList,subLabelList_2,posPatternList,posAdjPatternList,adjectives,path_to_write,tagger);
		
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

	

	private static void createArrf(HashSet<String> subLabelList,HashSet<String> subLabelList_2,
			HashSet<String> posPatternList, HashSet<String> posAdjPatternList,
			List<AdjectiveObject> adjectives, String path_to_write,MaxentTagger tagger) throws FileNotFoundException {
		List<AdjectiveObject> correctAdjectives = new ArrayList<AdjectiveObject>();
		List<AdjectiveObject> wrongAdjectives = new ArrayList<AdjectiveObject>();
		getRightWrongEntries(correctAdjectives,wrongAdjectives,adjectives);
		System.out.println("#correct entries"+correctAdjectives.size());
		System.out.println("#wrong entries"+wrongAdjectives.size());
		List<String> lines = new ArrayList<String>();
		getCsvLine(lines,correctAdjectives,subLabelList,subLabelList_2,posPatternList,posAdjPatternList,tagger);
		//List<AdjectiveObject> randomised_wrongAdjectives = new ArrayList<AdjectiveObject>();
		//getRandomisedWrongEntries(randomised_wrongAdjectives,wrongAdjectives,correctAdjectives.size());
		getCsvLine(lines,wrongAdjectives,subLabelList,subLabelList_2,posPatternList,posAdjPatternList,tagger);
		writeArff(lines,path_to_write,subLabelList,subLabelList_2,posPatternList,posAdjPatternList);
		System.out.println("wrote #"+lines.size()+" lines");
		System.out.println();
		
	}


	public static void writeArff(List<String> lines, String path_to_write,
			HashSet<String> subLabelList, HashSet<String> subLabelList_2, HashSet<String> posPatternList,
			HashSet<String> posAdjPatternList) throws FileNotFoundException {
		PrintWriter writer = new PrintWriter(path_to_write);
		String first_line =""
			+"@relation adjectives\n";
//			+"@attribute 'normalizedFrequency' numeric\n";
//			+"@attribute 'JJ' {0,1}\n"
//			+"@attribute 'normalizedObjectFrequency' numeric\n";
//			+"@attribute 'normalizedObjectOccourences' numeric\n";
//			+"@attribute 'ratio' numeric\n";
//			+"@attribute 'ratioPattern' numeric\n";
//			+"@attribute 'ratioPosPattern' numeric\n";
//			//+"@attribute 'entropy' numeric\n"
//			+"@attribute 'position' numeric\n";
//			+"@attribute 'firstPosition' {0,1}\n";
//			+"@attribute 'lastPosition' {0,1}\n";
//			+"@attribute 'nld' numeric\n";
		int counter=0;
//		for(String label:subLabelList){
//			counter+=1;
//			first_line+="@attribute 'l"+Integer.toString(counter)+"' {0,1}\n";
//		}
//		for(String label:subLabelList_2){
//			counter+=1;
//			first_line+="@attribute 'l2"+Integer.toString(counter)+"' {0,1}\n";
//		}
//		for(String label:posPatternList){
//			counter+=1;
//			first_line+="@attribute 'p"+Integer.toString(counter)+"' {0,1}\n";
//		}
		for(String label:posAdjPatternList){
			counter+=1;
			first_line+="@attribute 'pa"+Integer.toString(counter)+"' {0,1}\n";
		}
		
		first_line+="@attribute 'class' {0,1}\n"
				+"@data\n";
		writer.println(first_line);
		for(String line:lines)writer.println(line);

//		System.out.println("Wrote to "+path_to_write);
		
		writer.close();
		
	}


	/*private static void getRandomisedWrongEntries(
			List<AdjectiveObject> randomised_wrongAdjectives,
			List<AdjectiveObject> wrongAdjectives, int size) {
		// TODO Auto-generated method stub
		
		if(size>=randomised_wrongAdjectives.size())randomised_wrongAdjectives=wrongAdjectives;
		else{
			for(int i=0;i<size;i++)randomised_wrongAdjectives.add(wrongAdjectives.get(i));
		}
		System.out.println("Change getRandomisedWrongEntries()");
	}*/


	public static void getCsvLine(List<String> lines,
			List<AdjectiveObject> correctAdjectives, HashSet<String> subLabelList,HashSet<String> subLabelList_2, HashSet<String> posPatternList, HashSet<String> posAdjPatternList,MaxentTagger tagger) {
		// TODO Auto-generated method stub
		for(AdjectiveObject adjectiveobject : correctAdjectives){

			int jj = 0;
			if(tagger.tagString(adjectiveobject.getAdjectiveTerm().toLowerCase()).contains("JJ")){
				jj=1;
			}
//			String line = Double.toString(adjectiveobject.getNormalizedFrequency());
//					+","+Integer.toString(jj)
//				String line =Double.toString(adjectiveobject.getNormalizedObjectFrequency());
//					String line = Double.toString(adjectiveobject.getNormalizedObjectOccurrences());
//					String line = Double.toString(adjectiveobject.getRatio());
//					String line = Double.toString(adjectiveobject.getRatio_pattern());
//					String line = Double.toString(adjectiveobject.getRatio_pos_pattern());
//					//+","+Double.toString(adjectiveobject.getEntropy())
//					String line = Integer.toString(adjectiveobject.getPosition());
                        String line = "";
//			if(adjectiveobject.isFirstPosition())line+=","+"1";
//			else line+=","+"0";
//			if(adjectiveobject.isLastPosition())line+=","+"1";
//			else line+=","+"0";
//			line+=","+Double.toString(adjectiveobject.getNld());
//			for(String label:subLabelList){
//				if(adjectiveobject.getSublabel().equals(label))line+=","+"1";
//				else line+=","+"0";
//			}
//			for(String label:subLabelList_2){
//				if(adjectiveobject.getSublabel_2().equals(label))line+=","+"1";
//				else line+=","+"0";
//			}
//                                                

//			for(String pospattern:posPatternList){
//				if(adjectiveobject.getPos_Pattern().equals(pospattern))line+=","+"1";
//				else line+=","+"0";
//			}
                        
			for(String posadjpattern:posAdjPatternList){
				if(adjectiveobject.getPos_adj_Pattern().equals(posadjpattern))line+=","+"1";
				else line+=","+"0";
			}
                        line = line.replaceFirst(",", "");
			line+=","+adjectiveobject.getAnnotation();
			lines.add(line);
		}
		
	}


	private static void getRightWrongEntries(
			List<AdjectiveObject> correctAdjectives,
			List<AdjectiveObject> wrongAdjectives,
			List<AdjectiveObject> adjectives) {
		/*
		 * filter out entries without annotation
		 */
		for(AdjectiveObject adjectiveobject : adjectives){
			if(adjectiveobject.getAnnotation().contains("1"))correctAdjectives.add(adjectiveobject);
			if(adjectiveobject.getAnnotation().contains("0"))wrongAdjectives.add(adjectiveobject);
		}
	
		
	}


	private static void generatePosAdjPatternList(
			List<AdjectiveObject> adjectives, HashSet<String> posAdjPatternList) {
		for(AdjectiveObject adjectiveobject: adjectives){
			if(adjectiveobject.getAnnotation().equals("1")||adjectiveobject.getAnnotation().equals("0")){
				posAdjPatternList.add(adjectiveobject.getPos_adj_Pattern());
			}
			
		}
	}


	private static void generatePosPatternList(
			List<AdjectiveObject> adjectives, HashSet<String> posPatternList) {
		for(AdjectiveObject adjectiveobject: adjectives){
			if(adjectiveobject.getAnnotation().equals("1")||adjectiveobject.getAnnotation().equals("0")){
				posPatternList.add(adjectiveobject.getPos_Pattern());
			}
			
		}
	}


	private static void generateSubLabelList(
			List<AdjectiveObject> adjectives,HashSet<String> label_3, HashSet<String> label_2) {
		
		for(AdjectiveObject adjectiveobject: adjectives) {
			if(adjectiveobject.getAnnotation().equals("1")||adjectiveobject.getAnnotation().equals("0")){
				label_3.add(adjectiveobject.getSublabel());
				String tmp_label = getSublabel(adjectiveobject.getSublabel(),2);
				label_2.add(tmp_label);
				adjectiveobject.setSublabel_2(tmp_label);
			}
			
		}

	}
	
	
	private static String getSublabel(String x, int value) {
		/*
		 * add last x characters of string.
		 */
		String sublabel = "";
		if(x.length()>=value) sublabel=x.toLowerCase().substring(x.length()-value);
		else sublabel=x.toLowerCase();
		return sublabel;
	}
	


	private static List<AdjectiveObject> readCSV(String path_normalPath2) {
		System.out.println(path_normalPath2);
		List<AdjectiveObject> adj_list = new ArrayList<AdjectiveObject>();
		for(File file : listFilesForFolder(new File(path_normalPath2))){
			try {
				System.out.println(file.toString());
				adj_list.addAll(readCSV(file));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return adj_list;
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
			//System.out.println("Annotation:"+adjectiveobject.getAnnotation());
			//System.out.println();
		}
		return adj_list;
	}
	
	
	public static List<File> listFilesForFolder(final File folder) {
		List<File> filelist = new ArrayList<File>();
	    for (final File fileEntry : folder.listFiles()) {
	        if (fileEntry.isDirectory()) {
	        	filelist.addAll(listFilesForFolder(fileEntry));
	        } else {
	            filelist.add(fileEntry);
	        }
	    }
	    return filelist;
	}

	
	
	
}
