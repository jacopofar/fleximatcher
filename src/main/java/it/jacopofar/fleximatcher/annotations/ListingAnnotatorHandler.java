package it.jacopofar.fleximatcher.annotations;

import java.util.LinkedList;

import opennlp.tools.util.Span;

import org.json.JSONObject;
/**
 * DefaultAnnotationHandler which keeps a list of TextAnnotations in the same order were stored
 * */
public class ListingAnnotatorHandler extends DefaultAnnotationHandler {
	private LinkedList<TextAnnotation> added=new LinkedList<TextAnnotation>();
	@Override
	public void addAnnotation(Span span, JSONObject attributes) {
		added.add(new TextAnnotation(span,currentMatcher,attributes));
		super.addAnnotation(span,attributes);
		
	}
	
	/**
	 * Returns the TextAnnotation object in the same order they were added
	 * */
	public LinkedList<TextAnnotation> getAnnotations(){
		return added;
	}

}
