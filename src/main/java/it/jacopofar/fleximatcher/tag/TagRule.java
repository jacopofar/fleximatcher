package it.jacopofar.fleximatcher.tag;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Set;

import org.json.JSONObject;

import opennlp.tools.util.Span;
import it.jacopofar.fleximatcher.FlexiMatcher;
import it.jacopofar.fleximatcher.annotations.AnnotationHandler;
import it.jacopofar.fleximatcher.annotations.TextAnnotation;
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
		//System.out.println("--"+ah.getNestingLevel()+" TAGRULE: asked to annotate '"+text+"', I'm the rule "+name);
		if(ah.getNestingLevel()>ruleFactory.getMaximumNesting())
			return false;

		ruleFactory.getTagPatterns(name).forEach(pat->{
			//System.out.println("--"+ah.getNestingLevel()+" about to try pattern: "+pat);
			AnnotationHandler sa = ah.getSubHandler(pat.getPattern());
			Optional<Set<LinkedList<TextAnnotation>>> subMatches = ruleFactory.getMatcher().matches(text, pat.getPattern(), sa, false, false,true).getAnnotations();
			if(subMatches.isPresent()){
				for(LinkedList<TextAnnotation> matchSequence:subMatches.get()){
					JSONObject annotation=pat.getResultingAnnotation(text,matchSequence);
					//System.out.println("--+"+ah.getNestingLevel()+" that pattern ("+pat+") matches with the sequence: "+matchSequence+(annotation==null?"":" annotation: "+annotation.toString()));
					//there's a match, let's annotate it
					ah.addAnnotation(new Span(matchSequence.getFirst().getSpan().getStart(),matchSequence.getLast().getSpan().getEnd()),annotation );
				}
			}
		});
		return ah.checkAnnotationSequence(new String[]{"[tag:"+name+"]"}, text.length(), true,false).isMatching();
		//now all depending rules were annotated
	}

	public String toString(){
		return "tag "+this.name;
	}

}
