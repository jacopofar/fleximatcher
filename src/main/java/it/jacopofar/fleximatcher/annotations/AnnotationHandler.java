package it.jacopofar.fleximatcher.annotations;

import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Stream;

import opennlp.tools.util.Span;

import org.json.JSONObject;

public abstract class AnnotationHandler {

	protected String currentMatcher;
	private HashSet<String> matchedRules=new HashSet<String>();
	private HashSet<String> changedTags=new HashSet<String>();


	/**
	 * Annotate a text span with the given attributes
	 * @param span the text span to annotate
	 * @attributes the JSON attributes to assign, can be null
	 * */
	public abstract void addAnnotation(Span span,JSONObject attributes); 
	/**
	 * Set a String identifier for the rule currently matching.
	 * This method has to be called before invoking a rule, the rule should not call
	 * it because may not known with which name was bound to
	 *  */
	public void setCurrentMatcher(String rule){
		currentMatcher=rule;
	}
	public String getCurrentMatcher(){
		return currentMatcher;
	}
	public boolean hasBeenUsed(String string) {
		return matchedRules.contains(string);
	}
	public void rememberUse(String string) {
		matchedRules.add(string);
	}

	public abstract int getAnnotationNumbers();
	
	public void notifyChangedTag(String string) {
		changedTags.add(string);
	}
	/**
	 * Check whether exist a sequence of contiguous annotations with the given tag covering the whole length of the string.
	 * This is a sequence of annotations with the following characteristics:
	 * 1. Their getType() values are exactly the values contained in ruleParts, in the same order
	 * 2. The span of an element ends exactly where the next one starts
	 * 3. The first element span starts at 0, the last element span ends at length (if matchWhole is true)
	 * @param matchWhole false if the sequence can not cover the whole string (that is, the rukle 3 is not applied)
	 * @param rulePars an array of expected rules
	 * @lenth the length of the text to match, which will be the end of the last span
	 * */
	public abstract boolean checkAnnotationSequence(String[] ruleParts, int length, boolean matchWhole);
	/**
	 * Returns an annotation handler to manage subrequests (that is, match requests made by rules while annotating)
	 * The annotations given to the new AnnotationHandler will be stored in the current AnnotationHandler, the real difference is that the 
	 * current matcher will stay the same and the returned AnnotationHandler will use the given one
	 * */
	public AnnotationHandler getSubHandler(String newCurrentMatcher){
		return new SubHandler(this,newCurrentMatcher);
	}
	/**
	 * Return an unsorted stream of 
	 * */
	public abstract Stream<Entry<Integer,Set<TextAnnotation>>> getAnnotationsPositionalStream();

}
