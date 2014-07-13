package it.jacopofar.fleximatcher.rule;

import it.jacopofar.fleximatcher.rules.MatchingRule;
import it.jacopofar.fleximatcher.rules.RegexRule;

public class RegexRuleFactory implements RuleFactory {

	public MatchingRule getRule(String parameter) {
		return new RegexRule(parameter);
	}

}
