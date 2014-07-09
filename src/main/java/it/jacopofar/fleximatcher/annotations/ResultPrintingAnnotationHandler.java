package it.jacopofar.fleximatcher.annotations;

import org.json.JSONObject;

import opennlp.tools.util.Span;

/**
 * An annotation handler which prints debug information on stdout
 * */
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
	public void addAnnotationFromSubHandler(Span span, JSONObject attributes) {
		super.addAnnotationFromSubHandler(span,attributes);
		System.out.println("Added an annotation from a subhandler the tag '"+this.getCurrentMatcher()+ "' at "+span+": to '"+span.getCoveredText(text)+"'"+attributes);
	}

	public void setCurrentMatcher(String rule){
		System.out.println("set current matcher to "+rule);
		super.setCurrentMatcher(rule);
	}
}
