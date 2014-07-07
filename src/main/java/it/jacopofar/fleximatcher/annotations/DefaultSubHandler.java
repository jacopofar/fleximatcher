package it.jacopofar.fleximatcher.annotations;

import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Stream;

import org.json.JSONObject;

import opennlp.tools.util.Span;


public class DefaultSubHandler extends DefaultAnnotationHandler {

	private DefaultAnnotationHandler wrappedHandler;
	private String subCurrentMatcher;
	private HashSet<TextAnnotation> added=new HashSet<TextAnnotation>();
	private int nestingLevel;
	public DefaultSubHandler(DefaultAnnotationHandler annotationHandler,
			String currentMatcher) {
		this.wrappedHandler=annotationHandler;
		this.subCurrentMatcher=currentMatcher;
		this.nestingLevel=annotationHandler.getNestingLevel()+1;
	}

	@Override
	public void addAnnotation(Span span, JSONObject attributes) {
		String beforeMatcher=wrappedHandler.getCurrentMatcher();
		added.add(new TextAnnotation(span, subCurrentMatcher, attributes));
		synchronized(wrappedHandler){
			wrappedHandler.setCurrentMatcher(subCurrentMatcher);
			
			wrappedHandler.addAnnotationFromSubHandler(span, attributes);
			wrappedHandler.setCurrentMatcher(beforeMatcher);
		}
	}
	@Override
	public void addAnnotationFromSubHandler(Span span, JSONObject attributes) {
		wrappedHandler.addAnnotationFromSubHandler(span, attributes);
	}
	@Override
	public int getAnnotationNumbers() {
		return wrappedHandler.getAnnotationNumbers();
	}

	@Override
	public boolean checkAnnotationSequence(String[] ruleParts, int length, boolean matchWhole) {
		return wrappedHandler.checkAnnotationSequence(ruleParts, length,matchWhole);
	}

	@Override
	public Stream<Entry<Integer,Set<TextAnnotation>>> getAnnotationsPositionalStream() {
		return wrappedHandler.getAnnotationsPositionalStream();
	}

	@Override
	public Stream<TextAnnotation> getAnnotationsAtThisLevelStream() {
		return added.stream();
	}

	@Override
	public AnnotationHandler getSubHandler(String newCurrentMatcher) {
		return new DefaultSubHandler(this,newCurrentMatcher);
	}
	public int getNestingLevel() {
		return nestingLevel;
	}

}
