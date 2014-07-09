package it.jacopofar.fleximatcher.tag;

import it.jacopofar.fleximatcher.annotations.TextAnnotation;

import java.util.LinkedList;

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
				new JSONObject(annotationExpression);
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
		return "rule ID="+identifier+" pattern='"+pattern+"'";
	}
	public int hashCode(){
		return (pattern.hashCode()+11*(identifier==null?0:identifier.hashCode())+37*(annotationExpression==null?0:annotationExpression.hashCode()));
	}

	/**
	 * Get an annotation representing the tag, given the matching sequence
	 * */
	public JSONObject getResultingAnnotation(String text,LinkedList<TextAnnotation> matchSequence) {
		if(annotationExpression==null)
			return null;
		String result=annotationExpression;
		for(int i=0;i<matchSequence.size();i++){
			result.replace("#"+i+"#", JSONObject.quote(matchSequence.get(i).getSpan().getCoveredText(text).toString()));
		}
		try {
			return new JSONObject(result);
		} catch (JSONException e) {
			e.printStackTrace();
			throw new RuntimeException("error while creating the annotation");
		}
	}

}
