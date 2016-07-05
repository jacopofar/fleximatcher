package com.github.jacopofar.fleximatcher.tag;

import com.github.jacopofar.fleximatcher.annotations.TextAnnotation;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RuleDefinition {
    
    private final String pattern;
    private final String identifier;

    public String getAnnotationExpression() {
        return annotationExpression;
    }

    private String annotationExpression;
    public RuleDefinition(String pattern, String identifier) {
        this.pattern=pattern;
        this.identifier=identifier;
    }
    public RuleDefinition(String pattern, String identifier,String annotationExpression) {
        try {
            //check it now, it will be stored as a String but it's better to find issues early
            if(annotationExpression!=null)
                new JSONObject(annotationExpression.replaceAll("(#([0-9]+[^#]*)#)+", "''"));
        } catch (JSONException e) {
            throw new RuntimeException("Error, the string '"+annotationExpression+"' is not a valid JSON string!");
        }
        this.pattern=pattern;
        this.identifier=identifier;
        this.annotationExpression=annotationExpression;
    }
    
    public String getPattern() {
        return pattern;
    }
    
    public String getIdentifier() {
        return identifier;
    }
    
    @Override
    public String toString(){
        return "rule ID="+identifier+" pattern='"+pattern+"'"+ (annotationExpression==null?"":"annotation="+annotationExpression);
    }
    @Override
    public int hashCode(){
        return (pattern.hashCode()+11*(identifier==null?0:identifier.hashCode())+37*(annotationExpression==null?0:annotationExpression.hashCode()));
    }
    
    @Override
    public boolean equals(Object o){
        if(this==o) return true;
        if(!(o instanceof RuleDefinition))
            return false;
        return (this.pattern.equals(((RuleDefinition)o).pattern))
                && (this.identifier==null?((RuleDefinition)o).identifier==null : this.identifier.equals(((RuleDefinition)o).identifier))
                && (this.annotationExpression==null?((RuleDefinition)o).annotationExpression==null : this.annotationExpression.equals(((RuleDefinition)o).identifier));
    }
    
    /**
     * Get an annotation representing the tag, given the matching sequence
     *
     * @param text the text this pattern has matched
     * @param matchSequence the list of annotations resulting from the match
     * @return  a JSONObject representing the annotation content*/
    public JSONObject getResultingAnnotation(String text,LinkedList<TextAnnotation> matchSequence) {
        if(annotationExpression==null)
            return null;
        String result=annotationExpression;
        Pattern p = Pattern.compile("#([0-9]+[^#]*)#");
        Matcher m = p.matcher(annotationExpression);
        while(m.find()){
            String expr=m.group(1);
            if(expr.matches("[0-9]+")){
                String content=JSONObject.quote(matchSequence.get(Integer.parseInt(expr)).getSpan().getCoveredText(text).toString());
                result=result.replace(m.group(), content);
            }
            else
                if(expr.matches("[0-9]+\\..+")){
                    String content;
                    int position=Integer.parseInt(expr.replaceAll("\\..+", ""));
                    try {
                        content = JSONObject.quote(matchSequence.get(position).getJSON().get().getString(expr.replaceAll("[0-9]+\\.", "")));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        throw new RuntimeException("---------error while creating the annotation for "+expr.replaceAll("[0-9]+\\.", ""));
                    }
                    //if the content is empty, explicitly use an empty string
                    //we could have patterns in the form #x##y#, that have been transformed in 'string1''string2', we have to remove the double quotes between them
                    //System.err.println("   pre:"+result);
                    if(content.isEmpty())
                        result=result.replace(m.group(), "''");
                    else
                        result=result.replace(m.group(), content.replaceAll("([^:\\\\])\"\"([^,])", "$1$2").replaceAll("([^:])''([^,])", "$1$2"));
                    //  System.err.println("   post:"+result);
                    // System.err.println("   content was:"+content);
                }
        }
        //		for(int i=0;i<matchSequence.size();i++){
        //			String c=JSONObject.quote(matchSequence.get(i).getSpan().getCoveredText(text).toString());
        //			result=result.replace("#"+i+"#", c.substring(1,c.length()-1));
        //		}
        try {
            while(!result.equals(result.replaceAll("([^:\\\\])\"\"([^,])", "$1$2").replaceAll("([^:])''([^,])", "$1$2"))){
                result=result.replaceAll("([^:\\\\])\"\"([^,])", "$1$2").replaceAll("([^:])''([^,])", "$1$2");
            }
            return new JSONObject(result);
        } catch (JSONException e) {
            e.printStackTrace();
            throw new RuntimeException("++++++++++error while creating the annotation "+this.annotationExpression+" for "+text+" \n\n Obtained:\n"+result+"\n which is not a valid JSON");
        }
    }
    
}
