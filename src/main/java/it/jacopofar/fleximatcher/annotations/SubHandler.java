package it.jacopofar.fleximatcher.annotations;

import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Stream;

import opennlp.tools.util.Span;

import org.json.JSONObject;

public class SubHandler extends AnnotationHandler {

	private AnnotationHandler wrappedHandler;
	private String subCurrentMatcher;

	public SubHandler(AnnotationHandler annotationHandler,
			String currentMatcher) {
		this.wrappedHandler=annotationHandler;
		this.subCurrentMatcher=currentMatcher;
	}

	@Override
	public void addAnnotation(Span span, JSONObject attributes) {
		String beforeMatcher=wrappedHandler.getCurrentMatcher();
		wrappedHandler.setCurrentMatcher(subCurrentMatcher);
		wrappedHandler.addAnnotation(span, attributes);
		wrappedHandler.setCurrentMatcher(beforeMatcher);
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

}
