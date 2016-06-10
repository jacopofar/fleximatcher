package com.github.jacopofar.fleximatcher.rules;

import com.github.jacopofar.fleximatcher.FlexiMatcher;
import com.github.jacopofar.fleximatcher.rule.MultiRule;
import com.github.jacopofar.fleximatcher.rule.RuleFactory;

public class MultiRuleFactory implements RuleFactory {


	private FlexiMatcher fm;

	public MultiRuleFactory(FlexiMatcher flexiMatcher) {
		this.fm=flexiMatcher;
	}

	public MatchingRule getRule(String parameter) {
		return new MultiRule(parameter,fm);
	}

}
