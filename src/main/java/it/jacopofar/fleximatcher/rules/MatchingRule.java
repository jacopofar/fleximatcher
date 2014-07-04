package it.jacopofar.fleximatcher.rules;

import it.jacopofar.fleximatcher.annotations.AnnotationHandler;

import java.util.Optional;
/**
 * A matching rule is a class which can annotate a String.
 * An annotation is a JSON object applied to a span, a matching rule can produce any number of annotations, read annotations from other rules and apply other rules.
 * */
public abstract class MatchingRule {
	/**
	 * Returns true whether this rule can be cached. This means the result could be stored and used for further requests.
	 * It should be put to false only when the rule retrieve data rapidly changing
	 * */
	public boolean isCacheable(){
		return true;
	}
	/**
	 * Annotate the text, then returns whether the whole given text corresponds to this rule
	 * */
	public abstract boolean annotate(String text, AnnotationHandler ah);
	public abstract String getDescription();
	/**
	 * Return the tag, if any, associated to this rule
	 * */
	public Optional<String> getTag(){
		return Optional.empty();
	}
}
