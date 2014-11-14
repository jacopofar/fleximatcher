package it.jacopofar.fleximatcher.annotations;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import opennlp.tools.util.Span;

import org.json.JSONObject;


public class DefaultAnnotationHandler extends AnnotationHandler {
    private final ConcurrentHashMap<Integer,Set<TextAnnotation>> annotationsStored =new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String,Set<TextAnnotation>> annotationsForTag =new ConcurrentHashMap<>();
    private int numAnnotations;
    private static final List<Integer> zeroList=new ArrayList<>(1);
    static{
        zeroList.add(0);
    }
    @Override
    public void addAnnotation(Span span, JSONObject attributes) {
        numAnnotations++;
        if(!annotationsStored.containsKey(span.getStart())){
            annotationsStored.put(span.getStart(), new HashSet<>());
        }
        TextAnnotation ta = new TextAnnotation(span,currentMatcher,attributes);
        annotationsStored.get(span.getStart()).add(ta);
        if(!annotationsForTag.contains(currentMatcher))
            annotationsForTag.put(currentMatcher, new HashSet<>());
        annotationsForTag.get(currentMatcher).add(ta);
        
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
                    if(ann.getType().equals(p)){
                        newStartPositions.add(ann.getSpan().getEnd());
                        if(populateResult){
                            if(firstStep){
                                HashSet<LinkedList<TextAnnotation>> expandMe = newCandidateAnnotations.getOrDefault(ann.getSpan().getEnd(), new HashSet<>());
                                expandMe.add(new LinkedList<>(Arrays.asList(new TextAnnotation[]{ann})));
                                newCandidateAnnotations.put(ann.getSpan().getEnd(),expandMe);
                            }
                            else{
                                HashSet<LinkedList<TextAnnotation>> expandUs = candidateAnnotations.get(ann.getSpan().getStart());
                                if(expandUs==null)
                                    continue;
                                expandUs.stream().forEach((l) -> {
                                    l.addLast(ann);
                                });
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