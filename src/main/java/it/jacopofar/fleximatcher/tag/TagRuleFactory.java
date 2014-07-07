package it.jacopofar.fleximatcher.tag;

import it.jacopofar.fleximatcher.FlexiMatcher;
import it.jacopofar.fleximatcher.rule.RuleFactory;
import it.jacopofar.fleximatcher.rules.MatchingRule;

import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;


public class TagRuleFactory implements RuleFactory {

	private FlexiMatcher matcher;
	private ConcurrentHashMap<String,HashSet<String>> rules=new ConcurrentHashMap<String,HashSet<String>>();
	public TagRuleFactory(FlexiMatcher flexiMatcher) {
		this.matcher=flexiMatcher;
	}

	@Override
	public MatchingRule getRule(String tagName) {
		return new TagRule(this,tagName);
	}

	public Stream<String> getTagPatterns(String name) {
		return rules.getOrDefault(name, new HashSet<String>()).stream();
	}

	public FlexiMatcher getMatcher() {
		return matcher;

	}

	public int getMaximumNesting() {
		return 5;
	}

	public void addTagRule(String tag, String pattern, String identifier) {
		if(rules.containsKey(tag)){
			rules.get(tag).add(pattern);
		}
		else{
			HashSet<String> p = new HashSet<String>();
			p.add(pattern);
			rules.put(tag, p);
		}
	}

}
