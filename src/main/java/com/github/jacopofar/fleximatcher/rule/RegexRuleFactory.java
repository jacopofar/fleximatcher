package com.github.jacopofar.fleximatcher.rule;

import com.github.jacopofar.fleximatcher.rules.MatchingRule;
import com.github.jacopofar.fleximatcher.rules.RegexRule;

public class RegexRuleFactory implements RuleFactory {

	public MatchingRule getRule(String parameter) {
		return new RegexRule(parameter);
	}

}
