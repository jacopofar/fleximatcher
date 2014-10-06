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
     * Create a text annotation, which is immutable
     * @param s the Apache OpenNLP Span object corresponding to the covered text span
     * @param type the type of annotation, e.g.: the name of the rule
     * @param j the JSONObject containing the annotation, if any, or null
     */
    public TextAnnotation(Span s, String type,JSONObject j){
        if (s.length()==0)
            throw new RuntimeException("Error, an annotation cannot be of length 0");
        this.span=s;
        this.type=type;
        this.json=j;
    }
    
    @Override
    public int hashCode(){
        //for now, tostring() due to some troubles in the JSONObject hashCode()
        return span.hashCode()+type.hashCode()+(json==null?0:11*json.toString().hashCode());
    }
    
    @Override
    public boolean equals(Object o){
        if(this==o)
            return true;
        if(!(o instanceof TextAnnotation))
            return false;
        //for now, tostring() due to some troubles in the JSONObject equals()
        return (json==null?(((TextAnnotation)o).json==null):((TextAnnotation)o).json.toString().equals(json.toString()))
                && ((TextAnnotation)o).span.equals(span)
                && ((TextAnnotation)o).type.equals(type);
    }
    
    /**
     * Return the span covered by this annotation
     * @return  an OpenNLP Span instance*/
    public Span getSpan() {
        return span;
    }
    /**
     * Return an Optional containing the JSON annotation, if any
     *
     * @return the JSONObject or an empty Optional */
    public Optional<JSONObject> getJSON() {
        return Optional.ofNullable(json);
    }
    /**
     * Return the rule which matched this annotation
     *
     * @return the rule name used to create the annotation */
    public String getType(){
        return type;
    }
    @Override
    public String toString(){
        return (json==null?type+":"+span.toString():type+":"+span.toString()+" => "+json.toString());
    }
    /**
     * Return the start position of this annotation
     *
     * @return  the start position*/
    public int startPosition() {
        return span.getStart();
    }
    
    public String toJSON() {
        return "{\"type\":"+JSONObject.quote(type)+",\"span_start\":"+span.getStart()+",\"span_end\":"+span.getEnd()+ (json==null?"":",\"annotation\":"+json.toString())+"}";
    }
}