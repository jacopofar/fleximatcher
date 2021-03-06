package com.github.jacopofar.fleximatcher.annotations;

import com.google.common.collect.ImmutableList;
import opennlp.tools.util.Span;
import org.json.JSONObject;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class DefaultAnnotationHandler extends AnnotationHandler {
    private final ConcurrentHashMap<Integer,Set<TextAnnotation>> annotationsStored =new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String,Set<TextAnnotation>> annotationsForTag =new ConcurrentHashMap<>();
    private int numAnnotations;
    private static final List<Integer> zeroList=new ArrayList<>(1);
    static{
        zeroList.add(0);
    }
    public DefaultAnnotationHandler() {}
    public DefaultAnnotationHandler(boolean explain) {
        this.requiresExplanation = explain;
    }

    @Override
    public void addAnnotation(Span span, JSONObject attributes) {
        if(!annotationsStored.containsKey(span.getStart())){
            annotationsStored.put(span.getStart(), new HashSet<>());
        }
        TextAnnotation ta = new TextAnnotation(span,currentMatcher,attributes);
        annotationsStored.get(span.getStart()).add(ta);
        if(!annotationsForTag.containsKey(currentMatcher))
            annotationsForTag.put(currentMatcher, new HashSet<>());
        //System.out.println("tag: " + currentMatcher + " - annotations before:" + annotationsForTag);

        if(annotationsForTag.get(currentMatcher).add(ta)){
            numAnnotations++;
            //System.out.println("adding a new annotation for " + currentMatcher + " " + ta.toString());
            //System.out.println("annotations now:" + annotationsForTag.get(currentMatcher));
        }

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
        * Iterate on the rules to match and look for ones that start from one of the starting position
        * for each of them add at the ending positions to a new set of positions
        * if this new set stays empty, return false
        * if this set has some element, swap it with the starting positions set and go to the next rule
        *
        * If after checking all the rules the set contains length (that is, the last rule ends with at the expected position)
        * */
        HashSet<Integer> startPositions=new HashSet<>();
        ConcurrentHashMap<Integer,HashSet<LinkedList<TextAnnotation>>> candidateAnnotations=null;
        if(populateResult){
            //this will map a final positions with the lists of length K when matching the K+1 part
            candidateAnnotations=new ConcurrentHashMap<>(30);
        }
        startPositions.add(0);
        if(!matchWhole){
            startPositions.addAll(annotationsStored.keySet());
        }
        boolean firstStep=true;
        for(String p:ruleParts){
            HashSet<Integer> newStartPositions=new HashSet<>();
            ConcurrentHashMap<Integer,HashSet<LinkedList<TextAnnotation>>> newCandidateAnnotations=new ConcurrentHashMap<>(30);
            for(int startCandidate:startPositions){
                for(TextAnnotation ann:annotationsStored.getOrDefault(startCandidate, new HashSet<>())){
                    if(!ann.getType().equals(p))
                        continue;
                    newStartPositions.add(ann.getSpan().getEnd());
                    if(!populateResult)
                        continue;
                    if(firstStep){
                        //first step: for each possible first element, create an interpretation
                        HashSet<LinkedList<TextAnnotation>> expandMe = newCandidateAnnotations.getOrDefault(ann.getSpan().getEnd(), new HashSet<>());
                        expandMe.add(new LinkedList<>(Arrays.asList(ann)));
                        newCandidateAnnotations.put(ann.getSpan().getEnd(),expandMe);
                    }
                    else{
                        //TODO here's a bug: I can have more than 1 continuation, and have to create multiple candidates for them
                        HashSet<LinkedList<TextAnnotation>> expandible = candidateAnnotations.get(startCandidate);
                        if(expandible==null)
                            continue;
                        List<LinkedList<TextAnnotation>> expansions = expandible.stream().map((l) -> {
                            LinkedList<TextAnnotation> expansion = (LinkedList<TextAnnotation>) l.clone();
                            expansion.addLast(ann);
                            return expansion;
                        }).collect(Collectors.toList());
                        if(!newCandidateAnnotations.containsKey(ann.getSpan().getEnd())){
                            newCandidateAnnotations.put(ann.getSpan().getEnd(), new HashSet<>());
                        }
                        newCandidateAnnotations.get(ann.getSpan().getEnd()).addAll(expansions);

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
            result = new HashSet<>();
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

    @Override
    public List<String> getAncestorsMatchers() {
        return ImmutableList.of(currentMatcher);
    }

    @Override
    public List<Integer> getAncestorsAnnotationCountersAtCreationTime() {
        return ImmutableList.of(0);
    }

}       