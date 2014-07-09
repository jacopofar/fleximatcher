package it.jacopofar.fleximatcher.rule;

import it.jacopofar.fleximatcher.FlexiMatcher;
import it.jacopofar.fleximatcher.annotations.AnnotationHandler;
import it.jacopofar.fleximatcher.expressions.ExpressionParser;
import it.jacopofar.fleximatcher.rules.MatchingRule;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;

import opennlp.tools.util.Span;

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
		HashSet<Span> candidateSpans=null;
		for(String condition:matchers){
			AnnotationHandler sa;
			sa=ah.getSubHandler(condition);
			matchAll&=fm.matches(text, condition,sa, true,true,false).isMatching();
			if(candidateSpans!=null && candidateSpans.size()==0){
				//we already know that the match failed
				return false;
			}
			else{
				if(candidateSpans==null){
					candidateSpans=new HashSet<Span>();
					for(Span t:sa.getAnnotationsAtThisLevelStream().map(t->t.getSpan()).toArray(a->new Span[a]))
						candidateSpans.add(t);
				}
				else{
					//keep only the TextAnnotations matching with the current ones
					HashSet<Span> newSet=new HashSet<Span>();
					for(Span t:sa.getAnnotationsAtThisLevelStream().map(t->t.getSpan()).toArray(a->new Span[a])){
						if(candidateSpans.contains(t))
							newSet.add(t);
					}
					candidateSpans=newSet;
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
