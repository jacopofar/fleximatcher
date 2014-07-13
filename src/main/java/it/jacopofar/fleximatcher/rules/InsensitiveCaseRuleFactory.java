package it.jacopofar.fleximatcher.rules;

import it.jacopofar.fleximatcher.rule.InsensitiveCaseRule;
import it.jacopofar.fleximatcher.rule.RuleFactory;

public class InsensitiveCaseRuleFactory implements RuleFactory {

	public MatchingRule getRule(String parameter) {
		return new InsensitiveCaseRule(parameter);
	}

}
