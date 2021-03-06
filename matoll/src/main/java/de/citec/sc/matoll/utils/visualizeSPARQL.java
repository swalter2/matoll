/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.matoll.utils;

import de.citec.sc.matoll.core.Language;
import de.citec.sc.matoll.patterns.SparqlPattern;
import de.citec.sc.matoll.patterns.english.SparqlPattern_EN_Intransitive_PP;
import de.citec.sc.matoll.patterns.english.SparqlPattern_EN_Noun_PP_appos;
import de.citec.sc.matoll.patterns.english.SparqlPattern_EN_Noun_PP_copulative;
import de.citec.sc.matoll.patterns.english.SparqlPattern_EN_Noun_PP_possessive;
import de.citec.sc.matoll.patterns.english.SparqlPattern_EN_Predicative_Participle_passive;
import de.citec.sc.matoll.patterns.english.SparqlPattern_EN_Transitive_Verb;
import de.citec.sc.matoll.patterns.english.SparqlPattern_EN_Predicative_Participle_copulative;
import de.citec.sc.matoll.patterns.english.SparqlPattern_EN_Transitive_Passive;
import de.citec.sc.matoll.patterns.german.SparqlPattern_DE_Predicative_Adjective;
import de.citec.sc.matoll.patterns.german.SparqlPattern_DE_Noun_PP;
import de.citec.sc.matoll.patterns.german.SparqlPattern_DE_Noun_Possessive;
import de.citec.sc.matoll.patterns.german.SparqlPattern_DE_Noun_Possessive_b;
import de.citec.sc.matoll.patterns.german.SparqlPattern_DE_Transitive;
import de.citec.sc.matoll.patterns.german.SparqlPattern_DE_Transitive_Passive;
import de.citec.sc.matoll.patterns.german.SparqlPattern_DE_Intransitive_PP;
import de.citec.sc.matoll.patterns.german.SparqlPattern_DE_Noun_PP_appos;
import de.citec.sc.matoll.patterns.german.SparqlPattern_DE_Noun_Possessive_appos;
import de.citec.sc.matoll.patterns.german.SparqlPattern_DE_Refelexive_Transitive_PP;
import de.citec.sc.matoll.patterns.spanish.SparqlPattern_ES_Transitive;
import de.citec.sc.matoll.patterns.spanish.SparqlPattern_ES_Noun_PP_copulative_b;
import de.citec.sc.matoll.patterns.spanish.SparqlPattern_ES_Noun_PP_copulative_withHop;
import de.citec.sc.matoll.patterns.spanish.SparqlPattern_ES_Noun_PP_copulative;
import de.citec.sc.matoll.patterns.spanish.SparqlPattern_ES_Noun_PP_appos_b;
import de.citec.sc.matoll.patterns.spanish.SparqlPattern_ES_Noun_PP_appos;
import de.citec.sc.matoll.patterns.spanish.SparqlPattern_ES_Intransitive_PP;
import de.citec.sc.matoll.patterns.spanish.SparqlPattern_ES_Predicative_Participle_Copulative;
import de.citec.sc.matoll.patterns.spanish.SparqlPattern_ES_Predicative_Participle_Passive;
import de.citec.sc.matoll.patterns.spanish.SparqlPattern_ES_Transitive_Reciprocal;
import de.citec.sc.matoll.patterns.spanish.SparqlPattern_ES_Reflexive_Transitive_PP;
import de.citec.sc.matoll.patterns.spanish.SparqlPattern_ES_Transitive_passive;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author swalter
 */
public class visualizeSPARQL {
    private static int variable_counter = 0;
    public static void main(String[] args){
        List<SparqlPattern> Patterns_EN = new ArrayList<SparqlPattern>();
        List<SparqlPattern> Patterns_ES = new ArrayList<SparqlPattern>();
        List<SparqlPattern> Patterns_DE = new ArrayList<SparqlPattern>();
        Patterns_EN.add(new SparqlPattern_EN_Intransitive_PP());
        Patterns_EN.add(new SparqlPattern_EN_Noun_PP_appos());
        Patterns_EN.add(new SparqlPattern_EN_Noun_PP_copulative());
        Patterns_EN.add(new SparqlPattern_EN_Predicative_Participle_passive());
        Patterns_EN.add(new SparqlPattern_EN_Transitive_Verb());
        Patterns_EN.add(new SparqlPattern_EN_Predicative_Participle_copulative());
        Patterns_EN.add(new SparqlPattern_EN_Noun_PP_possessive());
        Patterns_EN.add(new SparqlPattern_EN_Transitive_Passive());
					
        Patterns_ES.add(new SparqlPattern_ES_Transitive());
        Patterns_ES.add(new SparqlPattern_ES_Noun_PP_copulative_b());
        Patterns_ES.add(new SparqlPattern_ES_Noun_PP_copulative_withHop());
        Patterns_ES.add(new SparqlPattern_ES_Noun_PP_copulative());
        Patterns_ES.add(new SparqlPattern_ES_Noun_PP_appos_b());
        Patterns_ES.add(new SparqlPattern_ES_Noun_PP_appos());
        Patterns_ES.add(new SparqlPattern_ES_Predicative_Participle_Copulative());
        Patterns_ES.add(new SparqlPattern_ES_Predicative_Participle_Passive());
        Patterns_ES.add(new SparqlPattern_ES_Intransitive_PP());
        Patterns_ES.add(new SparqlPattern_ES_Transitive_Reciprocal());
        Patterns_ES.add(new SparqlPattern_ES_Reflexive_Transitive_PP());
        Patterns_ES.add(new SparqlPattern_ES_Transitive_passive());
        
        Patterns_DE.add(new SparqlPattern_DE_Predicative_Adjective());
        Patterns_DE.add(new SparqlPattern_DE_Noun_PP());
        Patterns_DE.add(new SparqlPattern_DE_Noun_Possessive());
        Patterns_DE.add(new SparqlPattern_DE_Noun_Possessive_b());
        Patterns_DE.add(new SparqlPattern_DE_Transitive());
        Patterns_DE.add(new SparqlPattern_DE_Transitive_Passive());
        //Patterns.add(new SparqlPattern_DE_Transitive_Passive_optional());
        Patterns_DE.add(new SparqlPattern_DE_Intransitive_PP());
        Patterns_DE.add(new SparqlPattern_DE_Refelexive_Transitive_PP());
        Patterns_DE.add(new SparqlPattern_DE_Noun_PP_appos());
        Patterns_DE.add(new SparqlPattern_DE_Noun_Possessive_appos());
        
        writePatterns(Patterns_EN, Language.EN);
        writePatterns(Patterns_ES, Language.ES);
        writePatterns(Patterns_DE, Language.DE);
        
    }
    
    
    public static String doVisual(String query,String name){
        
        String prefix = "\\begin{figure}\n"
                    +"\\centering\n"
                    +"\\begin{tikzpicture}[\n" +
                    "    scale = 0.7, transform shape, thick,\n" +
                    "    every node/.style = {draw, circle, minimum size = 10mm},\n" +
                    "    grow = down,  % alignment of characters\n" +
                    "    level 1/.style = {sibling distance=3cm},\n" +
                    "    level 2/.style = {sibling distance=4cm}, \n" +
                    "    level 3/.style = {sibling distance=2cm}, \n" +
                    "    level distance = 2.3cm\n" +
                    "  ]\n";
        
        String suffix = "\\end{tikzpicture}\n"
                    +"\\caption{Visualisation for "+name+"}\n"
                    +"\\label{fig:"+name.replace("\\_","")+"}\n"
                    +"\\end{figure}";
        
        /*
        Split into parts; each triple is seperated with a .
        */
        String[] parts = query.split("\\.");        

        List<List<String>> relations = new ArrayList<>();
        Map<String,Integer> variables = new HashMap<>();
        Map<String,String> deprel = new HashMap<>();
        Map<String,String> node_name = new HashMap<>();
        
        
        for(String s: parts){
            /*
            ignore everything which starts with optional or union (TODO: fix this)
            */
            if(s.contains("<conll:head>")&& !s.contains("OPTIONAL") && !s.contains("UNION") &&!s.contains("SELECT")) {
                String value = (s.replace("}", "")).trim();
                String[] tmp = value.split("<conll:head>");
                String subj = tmp[0].trim();
                String obj = tmp[1].trim();
                variables.put(subj, 0);
                variables.put(obj, 0);
                List<String> relation = new ArrayList<>();
                relation.add(subj);
                relation.add(obj);
                relations.add(relation);
            }
            
            if(s.contains("<conll:deprel>")&& !s.contains("OPTIONAL") && !s.contains("UNION") &&!s.contains("SELECT")) {
                String value = (s.replace("}", "")).trim();
                String[] tmp = value.split("<conll:deprel>");
                String subj = tmp[0].trim();
                String obj = tmp[1].trim();
                /*
                TODO: Be aware that sometimes the deprel is hiden in an additional variable.
                In the moment ignore it, but make sure it is taken correctly
                */
                if(!obj.contains("?"))
                deprel.put(subj, obj.replace("\"",""));
            }
        }
        

//
        String head_variable = findRoot(relations);
        if(head_variable!=null){
            variables.put(head_variable,1);
            String latex_output = "\\node[fill = gray!40, "
                    + "shape = rectangle, rounded corners, "
                    +"minimum width = 2cm, font = \\sffamily] "
                    + "("+Integer.toString(variable_counter)+") {"+head_variable+"}\n";
            node_name.put(head_variable, Integer.toString(variable_counter));
            int counter = 0;
            for(List<String> relation:relations){
                String subj = relation.get(0);
                if(!node_name.containsKey(subj)){
                    counter+=1;
                    latex_output+= "child {node[fill = blue!40, shape = rectangle, rounded corners, " 
                            +"minimum width = 1cm, font = \\sffamily]  "
                            + "("+Integer.toString(variable_counter+1)+") {"+subj+"}\n";
                    latex_output+=recursiveLoop(subj,relations,node_name);
                }
            }
//            //Set final number of}
            int oc_1 = StringUtils.countMatches(latex_output+"};\n","}");
            int oc_2 = StringUtils.countMatches(latex_output,"{");
            if(oc_2>oc_1){
                for(int i=0;i<oc_2-oc_1;i++)latex_output+="}";
            }
            String output = "";
            if(oc_1>oc_2){
                output = prefix+latex_output+";\n";
            }
            else{
                output = prefix+latex_output+"};\n";
            }
            
            
            
            
            
            output+="\\begin{scope}[nodes = {draw = none}]\n";
            for(List<String> relation:relations){
                String subj = node_name.get(relation.get(0));
                String obj = node_name.get(relation.get(1));
                String dep = deprel.get(relation.get(0));
                output+= "\\path ("+subj+")     -- ("+obj+") node [near start, left]  {\\text{"+dep+"}};\n";
            }
            output+="\\draw[densely dashed, rounded corners, thin];\n";
            output+="\\end{scope} \n";
            
            
// \begin{scope}[nodes = {draw = none}]
//    \path (3)     -- (0) node [near start, left]  {\text{prep}};
//    \path (4)     -- (3) node [near start, right] {\text{nsubj}};
//    \path (1)     -- (0) node [near start, right] {\text{cop}};
//    \draw[densely dashed, rounded corners, thin];
//  \end{scope}     
            
            
            return output+suffix;
        }
        else{
            return null;
        }
    }

    /*
    find the root variable;
    that the variable, which is not on the subject side, but only on the object side
    */
    private static String findRoot(List<List<String>> relations) {
        String head = "";
        Set<String> subj_list = new HashSet<>();
        Set<String> obj_list = new HashSet<>();
        relations.stream().map((list) -> {
            subj_list.add(list.get(0));
            return list;
        }).forEach((list) -> {
            obj_list.add(list.get(1));
        });
        obj_list.removeAll(subj_list);
        if(obj_list.size()==1){
            return obj_list.toString().replace("[","").replace("]","");
        }
        else{
            return null;
        }
    }

    public static String recursiveLoop(String subj,List<List<String>>relations,Map<String,String> node_name){
        variable_counter+=1;
        node_name.put(subj, Integer.toString(variable_counter));
        String output = "";
        boolean go_on = false;
        for(List<String> r: relations){
            String obj = r.get(1);
            if(subj.equals(obj)){
                String new_subj = r.get(0);
                output+="child {node[fill = blue!40, shape = rectangle, rounded corners, " 
                      +"minimum width = 1cm, font = \\sffamily] "
                      + "("+Integer.toString(variable_counter+1)+") {"+new_subj+"}\n";
                go_on=true;
                output+=recursiveLoop(new_subj,relations,node_name);
            }
        }
            
            
        if(go_on)return output;
        else return "}\n";
        
    }

    private static void writePatterns(List<SparqlPattern> Patterns, Language language) {
        String prefix = "\\documentclass{scrartcl}\n" +
        "\\usepackage{mathtools}\n" +
        "\\usepackage{tikz}\n" +
        "\\usetikzlibrary{trees,positioning}\n" +
        "\n" +
        "\\begin{document}\n";
        
        String suffix = "\\end{document}";
        String output = "";
        System.out.println("Starting visualisation");
        for(SparqlPattern pattern : Patterns){
            String tmp =doVisual(pattern.getQuery(),pattern.getID().replace("_","\\_"))+"\n\n\n";
            String[] triple = pattern.getQuery().split("\n");
            for(String t : triple){
                tmp = "%"+t+"\n"+tmp;
            }
            if (tmp!=null)output+=tmp;
            else System.out.println(pattern.getID()+" could not be visualized");
        }
            
        
        PrintWriter writer;
        try {
                writer = new PrintWriter("sparql_tree_"+language.toString()+".tex");
                writer.write(prefix+output+suffix);
                writer.close();
        } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
        }
    }
}
