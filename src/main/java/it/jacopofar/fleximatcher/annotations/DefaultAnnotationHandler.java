package it.jacopofar.fleximatcher.annotations;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import opennlp.tools.util.Span;

import org.json.JSONObject;


public class DefaultAnnotationHandler extends AnnotationHandler {
	private ConcurrentHashMap<Integer,Set<TextAnnotation>> annotationsStored =new ConcurrentHashMap<Integer,Set<TextAnnotation>>();
	private int numAnnotations;
	@Override
	public void addAnnotation(Span span, JSONObject attributes) {

		numAnnotations++;
		if(!annotationsStored.containsKey(span.getStart())){
			annotationsStored.put(span.getStart(), new HashSet<TextAnnotation>());
		}
		TextAnnotation ta = new TextAnnotation(span,currentMatcher,attributes);
		annotationsStored.get(span.getStart()).add(ta);
	}
	@Override
	public void addAnnotationFromSubHandler(Span span, JSONObject attributes) {
		numAnnotations++;
		if(!annotationsStored.containsKey(span.getStart())){
			annotationsStored.put(span.getStart(), new HashSet<TextAnnotation>());
		}
		annotationsStored.get(span.getStart()).add(new TextAnnotation(span,currentMatcher,attributes));
	}
	@Override
	public int getAnnotationsCount() {
		return numAnnotations;
	}
	@Override
	public MatchingResults checkAnnotationSequence(String[] ruleParts, int length,boolean matchWhole,boolean populateResult) {
		if(ruleParts.length==0)
			if(length==0 || matchWhole)
				return MatchingResults.emptyMacth();
			else
				return MatchingResults.noMatch();
		/*
		 * The method is very simple:
		 * the startPosition set contains the starting points (initially just 0, or any possible position for matchWhole=false)
		 * Iterate on the rules to match and look for ones that start frome one of the starting position
		 * for each of them add at the ending positions to a new set of positions
		 * if this new set stays empty, return false
		 * if this set has some element, swap it with the startint positions set and go to the next rule
		 * 
		 * If after checking all the rules the set contains length (that is, the last rule ends with at the expected position)
		 * */
		HashSet<Integer> startPositions=new HashSet<Integer>();
		ConcurrentHashMap<Integer,HashSet<LinkedList<TextAnnotation>>> candidateAnnotations=null;
		if(populateResult){
			//this will map a final positions with the lists of length K when matching the K+1 part
			candidateAnnotations=new ConcurrentHashMap<Integer,HashSet<LinkedList<TextAnnotation>>>(30);

		}
		startPositions.add(0);
		if(!matchWhole){
			startPositions.addAll(annotationsStored.keySet());
		}
		boolean firstStep=true;
		for(String p:ruleParts){
			HashSet<Integer> newStartPositions=new HashSet<Integer>();
			ConcurrentHashMap<Integer,HashSet<LinkedList<TextAnnotation>>> newCandidateAnnotations=new ConcurrentHashMap<Integer,HashSet<LinkedList<TextAnnotation>>>(30);
			for(int startCandidate:startPositions){
				for(TextAnnotation ann:annotationsStored.getOrDefault(startCandidate, new HashSet<TextAnnotation>())){
					if(ann.getType().equals(p)){
						newStartPositions.add(ann.getSpan().getEnd());
						if(populateResult){
							if(firstStep){
								HashSet<LinkedList<TextAnnotation>> expandMe = newCandidateAnnotations.getOrDefault(ann.getSpan().getEnd(), new HashSet<LinkedList<TextAnnotation>>());
								expandMe.add(new LinkedList<TextAnnotation>(Arrays.asList(new TextAnnotation[]{ann})));
								newCandidateAnnotations.put(ann.getSpan().getEnd(),expandMe);
							}
							else{
								HashSet<LinkedList<TextAnnotation>> expandUs = candidateAnnotations.get(ann.getSpan().getStart());
								if(expandUs==null)
									continue;
								for(LinkedList<TextAnnotation> l:expandUs){
									l.addLast(ann);
								}
								if(newCandidateAnnotations.containsKey(ann.getSpan().getEnd())){
									newCandidateAnnotations.get(ann.getSpan().getEnd()).addAll(expandUs);
								}
								else
									newCandidateAnnotations.put(ann.getSpan().getEnd(),expandUs);
							}
						}
					}
				}
			}
			firstStep=false;
			if(newStartPositions.isEmpty())
				return MatchingResults.noMatch();
			else{
				startPositions=newStartPositions;
				candidateAnnotations=newCandidateAnnotations;
			}
		}
		//all of the rules, in sequence, matched. Check that the last one ends at the correct length
		HashSet<LinkedList<TextAnnotation>> result;
		if(startPositions.contains(length)||!matchWhole){
			result = new HashSet<LinkedList<TextAnnotation>>();
			for(Entry<Integer, HashSet<LinkedList<TextAnnotation>>> e:candidateAnnotations.entrySet()){
				result.addAll(e.getValue());
			}
			return MatchingResults.matchWithAnnotations(result);
		}
		return MatchingResults.noMatch();
	}

	@Override
	public Stream<Entry<Integer,Set<TextAnnotation>>> getAnnotationsPositionalStream() {
		return annotationsStored.entrySet().stream();
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
