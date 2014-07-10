package it.jacopofar.fleximatcher.annotations;

import java.util.Optional;

import org.json.JSONObject;

import opennlp.tools.util.Span;

/**
 * A text annotation is an immutable object which marks a span of a text with a type String and optionally a JSON object
 * 
 * */
public final class TextAnnotation {
	private Span span;
	private JSONObject json;
	private String type;
	/**
	 * 
	 * */
	public TextAnnotation(Span s, String type,JSONObject j){
		if (s.length()==0)
			throw new RuntimeException("Error, an annotation cannot be of length 0");
		this.span=s;
		this.type=type;
		this.json=j;
	}

	public int hashCode(){
		//for now, tostring() due to some troubles in the JSONObject hashCode()
		return span.hashCode()+type.hashCode()+(json==null?0:11*json.toString().hashCode());
	}

	public boolean equals(Object o){
		if(!(o instanceof TextAnnotation))
			return false;
		//for now, tostring() due to some troubles in the JSONObject equals()
		return (json==null?(((TextAnnotation)o).json==null):((TextAnnotation)o).json.toString().equals(json.toString()))
				&& ((TextAnnotation)o).span.equals(span)
				&& ((TextAnnotation)o).type.equals(type);
	}

	/**
	 * Return the span covered by this annotation
	 * */
	public Span getSpan() {
		return span;	
	}
	/**
	 * Return an Optional containing the JSON annotation, if any
	 * */
	public Optional<JSONObject> getJSON() {
		return Optional.ofNullable(json);
	}
	/**
	 * Return the rule which matched this annotation
	 * */
	public String getType(){
		return type;
	}
	public String toString(){
		return (json==null?type+":"+span.toString():type+":"+span.toString()+" => "+json.toString());
	}
	/**
	 * Return
	 * */
	public int startPosition() {
		return span.getStart();
	}
}