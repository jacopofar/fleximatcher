package it.jacopofar.fleximatcher.annotations;

import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

import opennlp.tools.util.Span;

import org.json.JSONObject;


public class DefaultSubHandler extends AnnotationHandler {
    
    private final AnnotationHandler wrappedHandler;
    private final int nestingLevel;
    public DefaultSubHandler(AnnotationHandler annotationHandler,
            String currentMatcher) {
        this.wrappedHandler=annotationHandler;
        this.currentMatcher=currentMatcher;
        this.nestingLevel=annotationHandler.getNestingLevel()+1;
        //TODO memorizzare oltre al nestingLevel anche la lista dei conteggi delle annotazioni e i currentMatcher di tutti i padri
        //servirà alla TagRule per beccare cicli
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
    
}
