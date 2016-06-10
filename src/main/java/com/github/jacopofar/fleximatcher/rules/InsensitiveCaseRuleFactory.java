package com.github.jacopofar.fleximatcher.rules;

import com.github.jacopofar.fleximatcher.rule.RuleFactory;
import com.github.jacopofar.fleximatcher.rule.InsensitiveCaseRule;

public class InsensitiveCaseRuleFactory implements RuleFactory {

	public MatchingRule getRule(String parameter) {
		return new InsensitiveCaseRule(parameter);
	}

}
