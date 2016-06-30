package com.github.jacopofar.fleximatcher.tag;

import com.github.jacopofar.fleximatcher.annotations.AnnotationHandler;
import com.github.jacopofar.fleximatcher.annotations.TextAnnotation;
import com.github.jacopofar.fleximatcher.rules.MatchingRule;
import opennlp.tools.util.Span;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class TagRule extends MatchingRule {

    private final TagRuleFactory ruleFactory;
    private final String name;

    public TagRule(TagRuleFactory tagRuleFactory, String tagName) {
        this.ruleFactory=tagRuleFactory;
        this.name=tagName;
    }

    @Override
    public boolean annotate(String text, AnnotationHandler ah) {
        //System.out.println("--"+ah.getNestingLevel()+" TAG RULE: asked to annotate '"+text+"', I'm the rule "+name);
        if(ah.getNestingLevel()>ruleFactory.getMaximumNesting()){

            if(ruleFactory.throwExceptionWhenReachingMaximumDepth())
                throw new RuntimeException("Reached maximum tag expansion depth with "+ah.getNestingLevel()+" for tag '"+name+"'.Annotator hierarchy:"+ah.getAncestorsMatchers().toString()+"  number of annotations:"+ah.getAncestorsAnnotationCountersAtCreationTime().toString()+" Check the tag patterns, increase the maximum nestign level, or set the matcher to not throw this exception");
            else
                return false;
        }
        List<String> ancMats = ah.getAncestorsMatchers();
        for(int i=ah.getNestingLevel()-1;i>0;i--){
            if(ancMats.get(i).equals("[tag:"+name+"]")
                    && ah.getAncestorsAnnotationCountersAtCreationTime().get(i)==ah.getAnnotationsCount()){
                //ancestor with the same pattern and same amount of annotations and same pattern
                //don't waste time matching it again
                //and don't throw maximum nesting exceptions
                return false;

            }
        }
        ruleFactory.getTagPatterns(name).forEach(pat->{
            //System.out.println("--"+ah.getNestingLevel()+" about to try pattern: "+pat);
            AnnotationHandler sa = ah.getSubHandler(pat.getPattern());
            //to avoid cycles during matching, let's look for ancestor matchers with the same pattern and the same amount of annotations at beginning
            //if there is another one, this one will not produce anything and we'll discard it
            //this is done to avoid useless operations and make meaningful the exception thrown when reaching maximum depth

            Optional<Set<LinkedList<TextAnnotation>>> subMatches = ruleFactory.getMatcher().matches(text, pat.getPattern(), sa, false, false,true).getAnnotations();
            if(subMatches.isPresent()){
                subMatches.get().stream().forEach((matchSequence) -> {
                    JSONObject annotation=pat.getResultingAnnotation(text,matchSequence);
                    if(ruleFactory.isExplainTagging()){
                        if (annotation == null){
                            annotation = new JSONObject();
                        }
                        try {
                            JSONObject explanation = new JSONObject();
                            explanation.put("id",pat.getIdentifier());
                            for(TextAnnotation ta: matchSequence){
                                JSONObject childExplanation = new JSONObject();
                                childExplanation.put("start",ta.getSpan().getStart());
                                childExplanation.put("end",ta.getSpan().getEnd());
                                childExplanation.put("type",ta.getType());

                                ta.getJSON().flatMap(j -> Optional.ofNullable(j.optJSONObject("__explain")))
                                        .ifPresent(e -> {
                                            try {
                                                childExplanation.put("__explain", e);
                                            } catch (JSONException e1) {
                                                e1.printStackTrace();
                                            }
                                        });
                                explanation.accumulate("child", childExplanation);
                            }
                            annotation.put("__explain", explanation);

                        } catch (JSONException e) {
                            //can never happen...
                            e.printStackTrace();
                        }
                    }
                    //System.out.println("--+"+ah.getNestingLevel()+" that pattern ("+pat+") matches with the sequence: "+matchSequence+(annotation==null?"":" annotation: "+annotation.toString()));
                    //there's a match, let's annotate it
                    ah.addAnnotation(new Span(matchSequence.getFirst().getSpan().getStart(),matchSequence.getLast().getSpan().getEnd()), annotation);
                });
            }
        });
        return ah.checkAnnotationSequence(new String[]{"[tag:"+name+"]"}, text.length(), true,false).isMatching();
        //now all depending rules were annotated
    }

    @Override
    public String toString(){
        return "tag "+this.name;
    }

}
