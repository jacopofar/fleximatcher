package com.github.jacopofar.fleximatcher.rules;

import com.github.jacopofar.fleximatcher.annotations.AnnotationHandler;

/**
 * A matching rule is a class which can annotate a String.
 * An annotation is a JSON object applied to a span, a matching rule can produce any number of annotations, read annotations from other rules and apply other rules.
 * */
public abstract class MatchingRule {

	/**
	 * Annotate the text, then returns whether the whole given text corresponds to this rule
	 * @text the text to annotate
	 * @ah the AnnotationHandler to use to annotate the text
	 * */
	public abstract boolean annotate(String text, AnnotationHandler ah);

}
