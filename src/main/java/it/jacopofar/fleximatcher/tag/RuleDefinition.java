package it.jacopofar.fleximatcher.tag;

import it.jacopofar.fleximatcher.annotations.TextAnnotation;

import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

public class RuleDefinition {

	private String pattern;
	private String identifier;
	private String annotationExpression;
	public RuleDefinition(String pattern, String identifier) {
		this.pattern=pattern;
		this.identifier=identifier;
	}
	public RuleDefinition(String pattern, String identifier,String annotationExpression) {
		try {
			//check it now, will however be stored as a String
			if(annotationExpression!=null)
				new JSONObject(annotationExpression.replaceAll("#([0-9]+[^#]*)#", "''"));
		} catch (JSONException e) {
			e.printStackTrace();
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

	public String toString(){
		return "rule ID="+identifier+" pattern='"+pattern+"'"+ (annotationExpression==null?"":"annotation="+annotationExpression);
	}
	public int hashCode(){
		return (pattern.hashCode()+11*(identifier==null?0:identifier.hashCode())+37*(annotationExpression==null?0:annotationExpression.hashCode()));
	}

	public boolean equals(Object o){
		if(!(o instanceof RuleDefinition))
			return false;
		return (this.pattern.equals(((RuleDefinition)o).pattern))
				&& (this.identifier==null?((RuleDefinition)o).identifier==null : this.identifier.equals(((RuleDefinition)o).identifier))
				&& (this.annotationExpression==null?((RuleDefinition)o).annotationExpression==null : this.annotationExpression.equals(((RuleDefinition)o).identifier));
	}

	/**
	 * Get an annotation representing the tag, given the matching sequence
	 * */
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
			if(expr.matches("[1-9][0-9]*\\..+")){
				String content;
				int position=Integer.parseInt(expr.replaceAll("\\..+", ""));
				try {
					content = matchSequence.get(position).getJSON().get().getString(expr.replaceAll("[0-9]+\\.", "")).toString();
				} catch (JSONException e) {
					e.printStackTrace();
					throw new RuntimeException("error while creating the annotation");
				}
				result=result.replace(m.group(), content);
			}
		}
//		for(int i=0;i<matchSequence.size();i++){
//			String c=JSONObject.quote(matchSequence.get(i).getSpan().getCoveredText(text).toString());
//			result=result.replace("#"+i+"#", c.substring(1,c.length()-1));
//		}
		try {
			return new JSONObject(result);
		} catch (JSONException e) {
			e.printStackTrace();
			throw new RuntimeException("error while creating the annotation");
		}
	}

}
