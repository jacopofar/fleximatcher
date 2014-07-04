package it.jacopofar.fleximatcher.annotations;

import opennlp.tools.util.Span;

import org.json.JSONObject;

public class ResultPrintingAnnotationHandler extends DefaultAnnotationHandler {

	private String text;
	public ResultPrintingAnnotationHandler(String s) {
		super();
		this.text=s;
	}
	@Override
	public void addAnnotation(Span span,JSONObject json) {
		super.addAnnotation(span,json);
		System.out.println("Added an annotation for the tag '"+this.getCurrentMatcher()+ "' at "+span+": to '"+span.getCoveredText(text)+"'"+json);
	}
	
	public void setCurrentMatcher(String rule){
		System.out.println("set current matcher to "+rule);
		super.setCurrentMatcher(rule);
	}
}
