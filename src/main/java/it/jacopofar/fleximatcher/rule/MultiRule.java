package it.jacopofar.fleximatcher.rule;

import it.jacopofar.fleximatcher.FlexiMatcher;
import it.jacopofar.fleximatcher.annotations.AnnotationHandler;
import it.jacopofar.fleximatcher.annotations.TextAnnotation;
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
		for(String condition:matchers){
			AnnotationHandler sa;
			sa=ah.getSubHandler(condition);
			fm.matches(text, condition,sa, true,false);
		}
		HashSet<Span> toAdd=new HashSet<Span>();
		ah.getAnnotationsPositionalStream().forEach(candidates->{
			for(TextAnnotation ta:candidates.getValue()){
				if(ta.getType().equals(matchers.get(0))){
					int expectedEnd=ta.getSpan().getEnd();
					//we found an annotation corresponding to the first one in the list of the matchers
					//let's look if any annotation in the list is present in this set with that ending position
					boolean notMatching=false;
					for(String mat:matchers){
						if(!candidates.getValue().stream().anyMatch(k->k.getType().equals(mat) && k.getSpan().getEnd()==expectedEnd)){
							notMatching=true;
						}
					}
					if(!notMatching){
						toAdd.add(ta.getSpan());	
					}
				}
			}
		});
		for(Span s:toAdd)
			ah.addAnnotation(s, null);
		return toAdd.stream().anyMatch(
				p->(p.getStart()==0 && p.getEnd()==text.length())
				);

	}

	@Override
	public String getDescription() {
		return "multi matcher:"+Arrays.deepToString(matchers.toArray());
	}

}
