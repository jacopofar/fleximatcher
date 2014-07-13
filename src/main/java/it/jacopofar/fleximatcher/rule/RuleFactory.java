package it.jacopofar.fleximatcher.rule;

import it.jacopofar.fleximatcher.rules.MatchingRule;

public interface RuleFactory {
	/**
	 * Returns a rule following the given parameter string, which could be empty
	 * */
	public MatchingRule getRule(String parameter);
}
