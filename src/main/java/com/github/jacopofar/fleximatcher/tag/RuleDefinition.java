package com.github.jacopofar.fleximatcher.tag;

import com.github.jacopofar.fleximatcher.annotations.TextAnnotation;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.NoSuchElementException;
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
    /**
     * Defines a rule with a given pattern, rule identifier and annotationExpression
     * An annotation expression is a JSON string with (optional) elements delimited by #
     * for example {"note":"meadow", height":"#1.number#"}
     *
     * When generating an annotation, these placeholders will be replaced with the corresponding value.
     * For example, #1.height# refers to the height value of the annotation with index 1
     * An index with no property, like #4#, refers to the text covered by a span, not its annotation
     *
     * Examples:
     *
     *
     * */
    public RuleDefinition(String pattern, String identifier, String annotationExpression) {
        try {
            //check it now, it will be stored as a String but it's better to find issues earlier
            if(annotationExpression!=null)
                new JSONObject(annotationExpression.replaceAll("(#([0-9]+[^#]*)#)+", "\"\""));
        } catch (JSONException e) {
            throw new RuntimeException("Error, the string '" + annotationExpression + "' was transformed into '" + annotationExpression.replaceAll("(#([0-9]+[^#]*)#) + ", "''")+"' which is not a valid JSON string", e);
        }
        //TODO maybe check that the expression doesn't try to annotate something out of index, for example the pattern 'a[r:b]c' and the annotation {x:#4#}
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
     *
     * @param text the text this pattern has matched
     * @param matchSequence the list of annotations resulting from the match
     * @return  a JSONObject representing the annotation content, null if no annotation expression was specified constructing the object */
    public JSONObject getResultingAnnotation(String text,LinkedList<TextAnnotation> matchSequence) {
        if(annotationExpression == null)
            return null;
        String result = annotationExpression;
        Pattern p = Pattern.compile("#([0-9]+[^#]*)#");
        Matcher m = p.matcher(annotationExpression);

        while(m.find()){
            String expr = m.group(1);
            if(expr.matches("[0-9]+")){
                String content=JSONObject.quote(
                        matchSequence.get(Integer.parseInt(expr)).getSpan().getCoveredText(text).toString()
                );
                //the string was quoted to escape quotes, newlines and special characters, but the delimiting quotes have to be removed
                result = result.replace(m.group(), content.substring(1, content.length() - 1));
            }
            else {
                if(expr.matches("[0-9]+\\..+")) {
                    int position = Integer.parseInt(expr.replaceAll("\\..+", ""));
                    try {
                        JSONObject sourceAnnotation = matchSequence.get(position).getJSON().get();
                        String value = JSONHelper.extract(sourceAnnotation, expr.replaceFirst("^[0-9]+\\.", ""));
                        result = result.replace(m.group(), value);
                    }
                    catch (NoSuchElementException e){
                        throw new RuntimeException("error while retrieving the existing annotations in " + expr.replaceAll("[0-9]+\\.", "") + " using expression " + annotationExpression +  " : " + e.getMessage());
                    }
                    catch(IndexOutOfBoundsException e) {
                        throw new RuntimeException("cannot generate the annotation because there are " + matchSequence.size() + " elements and " + expr + " is out of index.");
                    }
                }
            }
        }
        try {
            return new JSONObject(result);
        } catch (JSONException e) {
            throw new RuntimeException("error while creating the annotation " + this.annotationExpression + " for " + text + " \n Obtained: " + result + ", which is not a valid JSON");
        }
    }
}
