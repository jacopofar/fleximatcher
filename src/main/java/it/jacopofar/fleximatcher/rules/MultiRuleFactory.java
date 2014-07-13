package it.jacopofar.fleximatcher.rules;

import it.jacopofar.fleximatcher.FlexiMatcher;
import it.jacopofar.fleximatcher.rule.MultiRule;
import it.jacopofar.fleximatcher.rule.RuleFactory;

public class MultiRuleFactory implements RuleFactory {


	private FlexiMatcher fm;

	public MultiRuleFactory(FlexiMatcher flexiMatcher) {
		this.fm=flexiMatcher;
	}

	public MatchingRule getRule(String parameter) {
		return new MultiRule(parameter,fm);
	}

}
