package it.jacopofar.fleximatcher.tag;

import it.jacopofar.fleximatcher.FlexiMatcher;
import it.jacopofar.fleximatcher.annotations.AnnotationHandler;
import it.jacopofar.fleximatcher.rules.MatchingRule;

public class TagRule extends MatchingRule {

	private TagRuleFactory ruleFactory;
	private String name;

	public TagRule(FlexiMatcher matcher, String tagName) {
		
	}

	public TagRule(TagRuleFactory tagRuleFactory, String tagName) {
		this.ruleFactory=tagRuleFactory;
		this.name=tagName;
	}

	@Override
	public boolean annotate(String text, AnnotationHandler ah) {
		if(ah.getNestingLevel()>ruleFactory.getMaximumNesting())
			return false;
		ruleFactory.getTagPatterns(name).parallel().forEach(pat->{
			AnnotationHandler sa = ah.getSubHandler("[tag:"+name+"]");
			ruleFactory.getMatcher().matches(text, pat, sa, false, true);
			sa.getAnnotationsAtThisLevelStream().forEach(a->ah.addAnnotation(a.getSpan(), null));
		});
		return ah.checkAnnotationSequence(new String[]{"[tag:"+name+"]"}, text.length(), true);
		//now all depending rules were annotated
	}


}
