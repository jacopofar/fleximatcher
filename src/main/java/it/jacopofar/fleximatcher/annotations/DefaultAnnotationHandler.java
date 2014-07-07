package it.jacopofar.fleximatcher.annotations;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

import org.json.JSONObject;

import opennlp.tools.util.Span;


public class DefaultAnnotationHandler extends AnnotationHandler {
	private HashMap<Integer,Set<TextAnnotation>> annotationsStored =new HashMap<Integer,Set<TextAnnotation>>();
	private int numAnnotations;
	private HashSet<TextAnnotation> added=new HashSet<TextAnnotation>();
	@Override
	public void addAnnotation(Span span, JSONObject attributes) {
		numAnnotations++;
		if(!annotationsStored.containsKey(span.getStart())){
			annotationsStored.put(span.getStart(), new HashSet<TextAnnotation>());
		}
		TextAnnotation ta = new TextAnnotation(span,currentMatcher,attributes);
		annotationsStored.get(span.getStart()).add(ta);
		added.add(ta);
	}
	public void addAnnotationFromSubHandler(Span span, JSONObject attributes) {
		numAnnotations++;
		if(!annotationsStored.containsKey(span.getStart())){
			annotationsStored.put(span.getStart(), new HashSet<TextAnnotation>());
		}
		annotationsStored.get(span.getStart()).add(new TextAnnotation(span,currentMatcher,attributes));
	}
	@Override
	public int getAnnotationNumbers() {
		return numAnnotations;
	}
	@Override
	public boolean checkAnnotationSequence(String[] ruleParts, int length,boolean matchWhole) {
		if(ruleParts.length==0)
			return length==0 || matchWhole;
		/*
		 * The method is very simple:
		 * the startPosition set contains the starting points (initially just 0, or any possible position for matchWhole=false)
		 * Iterate on the rules to match and look for ones that start frome one of the starting position
		 * for each of them add athe ending positions to a new set of positions
		 * if this new set stays empty, return false
		 * if this set has some element, swap it with the startint positions set and go to the next rule
		 * 
		 * If after checking all the rules the set contains length (that is, the last rule ends with at the expected poistion)
		 * */
		HashSet<Integer> startPositions=new HashSet<Integer>();
		startPositions.add(0);
		if(!matchWhole){
			startPositions.addAll(annotationsStored.keySet());
		}
		for(String p:ruleParts){
			HashSet<Integer> newStartPositions=new HashSet<Integer>();
			for(int startCandidate:startPositions){
				for(TextAnnotation ann:annotationsStored.getOrDefault(startCandidate, new HashSet<TextAnnotation>())){
					if(ann.getType().equals(p))
						newStartPositions.add(ann.getSpan().getEnd());
				}
			}
			if(newStartPositions.isEmpty())
				return false;
			else
				startPositions=newStartPositions;
		}
		//all of the rules, in sequence, matched. Check that the last one ends at the correct length
		return startPositions.contains(length)||!matchWhole;
	}

	@Override
	public Stream<Entry<Integer,Set<TextAnnotation>>> getAnnotationsPositionalStream() {
		return annotationsStored.entrySet().stream();
	}
	@Override
	public Stream<TextAnnotation> getAnnotationsAtThisLevelStream() {
		return added.stream();
	}
	@Override
	public AnnotationHandler getSubHandler(String newCurrentMatcher) {
		return new DefaultSubHandler(this,newCurrentMatcher);
	}
	@Override
	public int getNestingLevel() {
		return 0;
	}
	

}
