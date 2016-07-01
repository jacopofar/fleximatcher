package com.github.jacopofar.fleximatcher.rule;

import com.github.jacopofar.fleximatcher.rules.MatchingRule;
import com.github.jacopofar.fleximatcher.rules.RegexRule;

public class RegexRuleFactory implements RuleFactory {

	public MatchingRule getRule(String parameter) {
		if(parameter.isEmpty()){
			//this is surely an error, annotations cannot be empty
			throw new RuntimeException("Cannot create a regex annotator with an empty pattern");
		}
		return new RegexRule(parameter);
	}

	@Override
	public String generateSample(String parameter) {
		return null;
	}

}
