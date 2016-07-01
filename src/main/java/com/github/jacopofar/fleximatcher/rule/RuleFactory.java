package com.github.jacopofar.fleximatcher.rule;

import com.github.jacopofar.fleximatcher.rules.MatchingRule;

public interface RuleFactory {
	/**
	 * Returns a rule following the given parameter string, which could be empty
	 *
     * @param parameter the parameter used in pattern definition, if any, or an empty string in case of no parameter
     * @return a MatchingRule which annotates using the given parameter
     */
	 MatchingRule getRule(String parameter);
    /**
     * Returns an example of text matching the parameter, if possible, or null
     * @param parameter the parameter used in pattern definition, if any, or an empty string in case of no parameter

     * */
     String generateSample(String parameter);
}
