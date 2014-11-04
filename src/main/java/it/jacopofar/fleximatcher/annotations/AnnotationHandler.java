package it.jacopofar.fleximatcher.annotations;

import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Stream;



import org.json.JSONObject;

import opennlp.tools.util.Span;


public abstract class AnnotationHandler {

	protected String currentMatcher;
	private HashSet<String> matchedRules=new HashSet<String>();


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

	public abstract int getAnnotationsCount();

	/**
	 * Check whether exist a sequence of contiguous annotations with the given tag covering the whole length of the string and returns the corresponding annotations if required.
	 * This is a sequence of annotations with the following characteristics:
	 * <ol>
	 * <li>Their getType() values are exactly the values contained in ruleParts, in the same order</li>
	 * <li>The span of an element ends exactly where the next one starts</li>
	 * <li>The first element span starts at 0, the last element span ends at length (if matchWhole is true)</li>
	 * </ol>
	 * @param matchWhole false if the sequence can not cover the whole string (that is, the rule 3 is not applied)
	 * @param rulePars an array of expected rules
	 * @lenth the length of the text to match, which will be the end of the last span
	 * @param populateResult whether ot not populate the resulting object with the sequences. If those are not necessary, a fair amount of calculation can be avoided
	 * */
	public abstract MatchingResults checkAnnotationSequence(String[] ruleParts, int length, boolean matchWhole,boolean populateResult);
	/**
	 * Returns an annotation handler to manage sub requests (that is, match requests made by rules while annotating)
	 * The annotations given to the new AnnotationHandler will be stored in the current AnnotationHandler, the real difference is that the 
	 * current matcher will stay the same and the returned AnnotationHandler will use the given one
	 * */
	public abstract AnnotationHandler getSubHandler(String newCurrentMatcher);
	/**
	 * Return a stream of positions with sets of annotations starting at that position.
	 * This includes all of the annotations
	 * 
	 * */
	public abstract Stream<Entry<Integer,Set<TextAnnotation>>> getAnnotationsPositionalStream();

	/**
	 * Return the nesting level of this AnnotationHandler, that is, the number of subhandlers levels
	 * The root AnnotationHandler is at level 0, a subhandler has level 1, the a subhandler obtained from it has level 2, and so on.
	 * */
	public abstract int getNestingLevel();
	

}
