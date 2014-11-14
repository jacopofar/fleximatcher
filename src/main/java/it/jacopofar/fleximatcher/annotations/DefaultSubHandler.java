package it.jacopofar.fleximatcher.annotations;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;
import opennlp.tools.util.Span;

import org.json.JSONObject;


public class DefaultSubHandler extends AnnotationHandler {
    
    private final AnnotationHandler wrappedHandler;
    private final int nestingLevel;
    private final int annotationsAtStart;
    public DefaultSubHandler(AnnotationHandler annotationHandler,
            String currentMatcher) {
        this.wrappedHandler=annotationHandler;
        this.currentMatcher=currentMatcher;
        this.nestingLevel=annotationHandler.getNestingLevel()+1;
        //store the number of annotation in the ancestor and what it was matching
        //since annotations can only be added, this is enough to identify cycles in the form:
        //(sub)handler 1 matches X
        //sub-handler 2 of 1 matches Y
        //sub-handler 3 of 2 matches X again
        //when this happen, if sub-handler 3 is started with the same amount of annotations, we can be sure this subhandler will not match anything new
        annotationsAtStart=wrappedHandler.getAnnotationsCount();
    }
    
    @Override
    public void addAnnotation(Span span, JSONObject attributes) {
        synchronized(wrappedHandler){
            String beforeMatcher=wrappedHandler.getCurrentMatcher();
            wrappedHandler.setCurrentMatcher(currentMatcher);
            wrappedHandler.addAnnotation(span, attributes);
            wrappedHandler.setCurrentMatcher(beforeMatcher);
        }
    }
    
    @Override
    public int getAnnotationsCount() {
        return wrappedHandler.getAnnotationsCount();
    }
    
    @Override
    public MatchingResults checkAnnotationSequence(String[] ruleParts, int length, boolean matchWhole,boolean populateResult) {
        return wrappedHandler.checkAnnotationSequence(ruleParts, length,matchWhole,populateResult);
    }
    
    @Override
    public Stream<Entry<Integer,Set<TextAnnotation>>> getAnnotationsPositionalStream() {
        return wrappedHandler.getAnnotationsPositionalStream();
    }
    
    @Override
    public AnnotationHandler getSubHandler(String newCurrentMatcher) {
        return new DefaultSubHandler(this,newCurrentMatcher);
    }
    @Override
    public int getNestingLevel() {
        return nestingLevel;
    }
    
    @Override
    public void setCurrentMatcher(String rule){
        currentMatcher=rule;
    }
    
    @Override
    public boolean hasBeenUsed(String string) {
        return wrappedHandler.hasBeenUsed(string);
    }
    @Override
    public void rememberUse(String string) {
        wrappedHandler.rememberUse(string);
    }
    
    @Override
    public List<String> getAncestorsMatchers() {
        ImmutableList.Builder<String> v = ImmutableList.builder();
        return v.addAll(wrappedHandler.getAncestorsMatchers()).add(currentMatcher).build();
    }
    
    @Override
    public List<Integer> getAncestorsAnnotationCountersAtCreationTime() {
        ImmutableList.Builder<Integer> v = ImmutableList.builder();
        return v.addAll(wrappedHandler.getAncestorsAnnotationCountersAtCreationTime()).add(annotationsAtStart).build();
    }
    
}
