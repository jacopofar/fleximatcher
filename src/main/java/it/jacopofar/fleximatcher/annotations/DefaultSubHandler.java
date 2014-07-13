package it.jacopofar.fleximatcher.annotations;

import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Stream;

import org.json.JSONObject;

import opennlp.tools.util.Span;


public class DefaultSubHandler extends AnnotationHandler {

	private AnnotationHandler wrappedHandler;
	private final HashSet<TextAnnotation> addedSub=new HashSet<TextAnnotation>();
	private int nestingLevel;
	public DefaultSubHandler(AnnotationHandler annotationHandler,
			String currentMatcher) {
		this.wrappedHandler=annotationHandler;
		this.currentMatcher=currentMatcher;
		this.nestingLevel=annotationHandler.getNestingLevel()+1;
	}

	@Override
	public void addAnnotation(Span span, JSONObject attributes) {
		addedSub.add(new TextAnnotation(span, currentMatcher, attributes));
		synchronized(wrappedHandler){
			String beforeMatcher=wrappedHandler.getCurrentMatcher();
			wrappedHandler.setCurrentMatcher(currentMatcher);
			wrappedHandler.addAnnotationFromSubHandler(span, attributes);
			wrappedHandler.setCurrentMatcher(beforeMatcher);
		}
	}
	@Override
	public void addAnnotationFromSubHandler(Span span, JSONObject attributes) {
		synchronized(wrappedHandler){
			String beforeMatcher=wrappedHandler.getCurrentMatcher();
			wrappedHandler.setCurrentMatcher(currentMatcher);
			wrappedHandler.addAnnotationFromSubHandler(span, attributes);
			wrappedHandler.setCurrentMatcher(beforeMatcher);
		}
	}
	@Override
	public int getAnnotationsCount() {
		return wrappedHandler.getAnnotationsCount();
	}

	@Override
	public MatchingResults checkAnnotationSequence(String[] ruleParts, int length, boolean matchWhole,boolean populateResult) {
		return wrappedHandler.checkAnnotationSequence(ruleParts, length,matchWhole,populateResult);
	}

	@Override
	public Stream<Entry<Integer,Set<TextAnnotation>>> getAnnotationsPositionalStream() {
		return wrappedHandler.getAnnotationsPositionalStream();
	}

	@Override
	public AnnotationHandler getSubHandler(String newCurrentMatcher) {
		return new DefaultSubHandler(this,newCurrentMatcher);
	}
	public int getNestingLevel() {
		return nestingLevel;
	}

	public void setCurrentMatcher(String rule){
		currentMatcher=rule;
	}

	public boolean hasBeenUsed(String string) {
		return wrappedHandler.hasBeenUsed(string);
	}
	public void rememberUse(String string) {
		wrappedHandler.rememberUse(string);
	}

}
