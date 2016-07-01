package com.github.jacopofar.fleximatcher.rules;

import com.github.jacopofar.fleximatcher.rule.RuleFactory;
import com.github.jacopofar.fleximatcher.rule.InsensitiveCaseRule;

public class InsensitiveCaseRuleFactory implements RuleFactory {

	public MatchingRule getRule(String parameter) {
		return new InsensitiveCaseRule(parameter);
	}
	@Override
	public String generateSample(String parameter) {
		if(Math.random() < 0.3) return parameter.toUpperCase();
		if(Math.random() < 0.5) return parameter.toLowerCase();
		return parameter;
	}
}
