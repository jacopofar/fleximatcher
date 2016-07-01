package com.github.jacopofar.fleximatcher.rule;

import com.github.jacopofar.fleximatcher.FlexiMatcher;
import com.github.jacopofar.fleximatcher.annotations.AnnotationHandler;
import com.github.jacopofar.fleximatcher.expressions.ExpressionParser;
import com.github.jacopofar.fleximatcher.rules.MatchingRule;
import opennlp.tools.util.Span;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;

public class MultiRule extends MatchingRule {

	private LinkedList<String> matchers;
	private FlexiMatcher fm;

	public MultiRule(String matchers, FlexiMatcher fm) {
		this.matchers=new LinkedList<String>();
		this.matchers.addAll(Arrays.asList(ExpressionParser.split(matchers)));
		this.fm=fm;
	}

	@Override
	public boolean annotate(String text, AnnotationHandler ah) {
		boolean matchAll=true;
		final HashSet<Span> candidateSpans=new HashSet<Span> ();
		boolean firstStep=true;
		for(String condition:matchers){
			AnnotationHandler sa;
			sa=ah.getSubHandler(condition);
			matchAll&=fm.matches(text, condition,sa, true,true,false).isMatching();
			if(firstStep==false && candidateSpans.size()==0){
				//we already know that the match failed
				return false;
			}
			else{
				if(firstStep){
					
					
					Span[] added = sa.getAnnotationsPositionalStream()
					.flatMap(t->t.getValue().stream())
					.filter(a->a.getType().equals(condition))
					.map(ann->ann.getSpan()).toArray(count->new Span[count]);//.forEach(az->candidateSpans.add(az));
					candidateSpans.addAll(Arrays.asList(added));
					firstStep=false;
				}
				else{
					//keep only the TextAnnotations matching with the current ones
					HashSet<Span> newSet=new HashSet<Span>();
					
					sa.getAnnotationsPositionalStream()
					.flatMap(t->t.getValue().stream())
					.filter(a->a.getType().equals(condition))
					.map(ann->ann.getSpan())
					.filter(s->candidateSpans.contains(s))
					.forEach(az->newSet.add(az));
					
					candidateSpans.clear();
					candidateSpans.addAll(newSet);
				}
			}
		}
		for(Span s:candidateSpans)
			ah.addAnnotation(s, null);
		return matchAll;

	}

	@Override
	public String toString() {
		return "multi matcher:"+Arrays.deepToString(matchers.toArray());
	}

}
