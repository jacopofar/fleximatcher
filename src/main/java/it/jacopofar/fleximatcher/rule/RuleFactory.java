package it.jacopofar.fleximatcher.rule;

import it.jacopofar.fleximatcher.rules.MatchingRule;

public interface RuleFactory {
	/**
	 * Returns a rule following the given parameter string, which could be empty
	 *
     * @param parameter the parameter used in pattern definition, if any, or an empty string in case of no parameter
     * @return a MatchingRule which annotates using the given parameter
     */
	public MatchingRule getRule(String parameter);
}
