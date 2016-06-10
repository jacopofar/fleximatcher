package com.github.jacopofar.fleximatcher;

import com.github.jacopofar.fleximatcher.annotations.AnnotationHandler;
import com.github.jacopofar.fleximatcher.annotations.DefaultAnnotationHandler;
import com.github.jacopofar.fleximatcher.annotations.MatchingResults;
import com.github.jacopofar.fleximatcher.annotations.ResultPrintingAnnotationHandler;
import com.github.jacopofar.fleximatcher.expressions.ExpressionParser;
import com.github.jacopofar.fleximatcher.italian.ItSpecificVerbRuleFactory;
import com.github.jacopofar.fleximatcher.italian.ItVerbFormRuleFactory;
import com.github.jacopofar.fleximatcher.rule.RegexRuleFactory;
import com.github.jacopofar.fleximatcher.rule.RuleFactory;
import com.github.jacopofar.fleximatcher.rules.InsensitiveCaseRuleFactory;
import com.github.jacopofar.fleximatcher.rules.MatchingRule;
import com.github.jacopofar.fleximatcher.rules.MultiRuleFactory;
import com.github.jacopofar.fleximatcher.rules.PlainTextRule;
import com.github.jacopofar.fleximatcher.tag.RuleDefinition;
import com.github.jacopofar.fleximatcher.tag.TagRuleFactory;
import com.github.jacopofar.fleximatcher.italian.ItPosRuleFactory;

import java.util.concurrent.ConcurrentHashMap;


public final class FlexiMatcher {
    
    private final ConcurrentHashMap<String,RuleFactory> rules=new ConcurrentHashMap<>();
    private final TagRuleFactory factory;
    /**
     * Add or replace a rule, binding it to a name.
     * @param ruleName the name which will be used to identify the rule
     * @param rf the ruleFactory to be bound to this name
     * @return true if the same name was already in use and the corresponding RuleFactory has been replaced, false otherwise */
    public boolean bind(String ruleName,RuleFactory rf){
        return (rules.put(ruleName, rf)!=null);
    }
    
    
    /**
     * Matches the text against the given pattern, using the given annotator
     * @param text the string to match (e.g.: "the dog")
     * @param pattern the pattern to search for (e.g. "the [r:[a-z]+]")
     * @param ah the AnnotationHandler which will be used to store annotations
     * @param fullyAnnotate if true, will give the annotation handler any annotation found, if false will stop as soon as is sure that the string doesn't match
     * @param matchWhole if true, will match the pattern against the whole text, if false will search for it inside the string.
     * @param populateResult if true, will populate the results, if false it will only check whether there is a match or not.
     * The difference between fullyAnnotate and populateResult is that the former can stop the annotation process, the latter stops the generation of the results but let the annotation handler receive the annotations found.
     * @return a MatchingResults reporting whether there was a match or not and, when requested, the annotations and 
     * */
    public MatchingResults matches(String text, String pattern, AnnotationHandler ah, boolean fullyAnnotate, boolean matchWhole, boolean populateResult){
        String[] ruleParts = ExpressionParser.split(pattern);
        MatchingRule[] ruleset=new MatchingRule[ruleParts.length];
        int i=0;
        boolean isSurelyWrong=false;
        for(String s:ruleParts){
            if(!s.startsWith("[")){
                if(!text.contains(s))
                    if(!fullyAnnotate)
                        return MatchingResults.noMatch();
                    else
                        isSurelyWrong=true;
                ruleset[i++]=new PlainTextRule(s);
            }
            else{
                if(!rules.containsKey(ExpressionParser.ruleName(s)))
                    throw new RuntimeException("Error, the rule '"+ExpressionParser.ruleName(s)+"' is unknown, check it was bound and the pattern is written correctly");
                ruleset[i++]=rules.get(ExpressionParser.ruleName(s)).getRule(ExpressionParser.getParameter(s));
            }
        }
        //now the ruleset is created, annotate using it
        
        for(i=0;i<ruleset.length;i++){
            if(!ruleset[i].isCacheable() || !ah.hasBeenUsed(ruleParts[i])){
                ah.setCurrentMatcher(ruleParts[i]);
                ruleset[i].annotate(text, ah);
            }
            ah.rememberUse(ruleParts[i]);
        }
        
        //the text is annotated, is it surely wrong? skip the matching
        if(isSurelyWrong)
            return MatchingResults.noMatch();
        return ah.checkAnnotationSequence(ruleParts, text.length(),matchWhole,populateResult);
    }
    
    /**
     * Check whether the pattern matches the text
     *
     * @param text the text to examine
     * @param pattern the pattern to be matched with the text
     * @return true if the pattern completely matches the text, false otherwise
     */
    public boolean matches(String text,String pattern){
        return matches(text,pattern,getDefaultAnnotator(),false,true,false).isMatching();
    }
    /**
     * Check whether the pattern matches the text
     *
     * @param text the text to examine
     * @param pattern the pattern to be searched in the text
     * @return true if the pattern appears in the text, false otherwise
     */
    public boolean contains(String text,String pattern){
        return matches(text,pattern, getDefaultAnnotator(),false,false,false).isMatching();
    }
    
    public FlexiMatcher(){
        this.bind("r", new RegexRuleFactory());
        this.bind("i", new InsensitiveCaseRuleFactory());
        this.bind("multi", new MultiRuleFactory(this));
        factory=new TagRuleFactory(this);
        this.bind("tag", factory);
    }
    
    public static void main(String argc[]){
        FlexiMatcher fm=new FlexiMatcher();
        fm.bind("it-pos", new ItPosRuleFactory());
        fm.bind("it-verb-conjugated", new ItSpecificVerbRuleFactory());
        fm.bind("it-verb-form", new ItVerbFormRuleFactory());
        fm.addTagRule("frutto","pera","id_pera");
        fm.addTagRule("frutto","la [tag:frutto]","id_nested");
        String analyzeThis=" il cane mangia la pera";
        fm.matches(analyzeThis,"la [i:pera]", new ResultPrintingAnnotationHandler(analyzeThis),true,false,true).getAnnotations().get().stream().forEach((k) -> {
            System.out.println(">>"+k);
        });
        System.out.println(fm.matches(analyzeThis,"la [tag:frutto]", new ResultPrintingAnnotationHandler(analyzeThis),true,false,true));
        System.out.println(fm.matches(analyzeThis,"[it-pos:RD] [it-pos:Ss] è [it-pos:As]", new ResultPrintingAnnotationHandler(analyzeThis),true,false,true));
        System.out.println(fm.matches(analyzeThis,"[it-verb-conjugated:mangiare]", new ResultPrintingAnnotationHandler(analyzeThis),true,false,true));
    }
    
    /**
     * Add a rule to this matcher, which will be used to macth [tag:rulename]
     * @param tag the tag of the rule, which will be used in the tag [tag:name]
     * @param pattern the pattern that will be matched and tagged by this rule
     * @param identifier identifier an optional identifier for the rule, will be used to remove it
     * @return true if a rule with the same tag and identifier was replaced by the given one, false otherwise
     * If the identifier is null, it will be added and will always return false
     * */
    public boolean addTagRule(String tag, String pattern, String identifier) {
        return factory.addTagRule(tag,pattern,identifier,null);
    }
    
    /**
     * Add a rule to this matcher, which will be used to match [tag:rulename]
     * @param tag the tag of the rule, which will be used in the tag [tag:name]
     * @param pattern the pattern that will be matched and tagged by this rule
     * @param identifier identifier an optional identifier for the rule, will be used to remove it
     * @param annotationTemplate a JSON string which can contain parameters in the form #N#, which will be replaced by the string at position N, then used as the generated annotation
     * @return true if a rule with the same tag and identifier was replaced by the given one, false otherwise
     * If the identifier is null, it will be added and will always return false
     * e.g.
     * */
    public boolean addTagRule(String tag, String pattern, String identifier,String annotationTemplate) {
        return factory.addTagRule(tag,pattern,identifier,annotationTemplate);
    }
    
     /**
     * Add a rule to this matcher, which will be used to match [tag:rulename]
     * @param tag the tag of the rule, which will be used in the tag [tag:name]
     * @param identifier identifier an optional identifier for the rule, will be used to remove it
     * @param annotationRule an annotation rule, containing the pattern to match and the method to generate the annotation, if any
     * @return true if a rule with the same tag and identifier was replaced by the given one, false otherwise
     * If the identifier is null, it will be added and will always return false
     * e.g.
     * */
    public boolean addTagRule(String tag, String identifier, RuleDefinition annotationRule) {
        return factory.addTagRule(tag, identifier, annotationRule);
    }
    
    
    /**
     * Remove the rule with the given tag and identifier
     * @return true if a rule was removed
     * */
    public boolean removeTagRule(String tag,String identifier) {
        return factory.removeTagRule(tag,identifier);
        
    }
    
    public static AnnotationHandler getDefaultAnnotator() {
        return new DefaultAnnotationHandler();
    }
    
    
    public boolean isBoundRule(String ruleName) {
        return rules.containsKey(ruleName);
    }
    
    
    public void clearTags() {
        factory.clearRules();
    }
    /**
     * Set the maximum nesting level that can be used when matching tags.
     * Since a tag definition can call other tags, recursively, fleximatcher could find loops when matching them.
     * Instead of checking the rules, a maximum recursion level is applied, when it's reached the matching process stops.
     * 
     * @param maxDepth the new maximum nesting level to be applied
     */
    public void setMaximumTagNesting(int maxDepth){
        factory.setMaximumNesting(maxDepth);
    }
    /**
     *  Set the maximum nesting level that can be used when matchign tags.
     * Since a tag definition can call other tags, recursively, fleximatcher could find loops when matching them.
     * Instead of checking the rules, a maximum recursion level is applied, when it's reached the matching process stops.
     * @return the current maximum nesting level
     */
    public int getMaximumTagNesting(){
        return factory.getMaximumNesting();
    }
    /**
     * Throws an exception when the matching process reaches the maximum allowed depth
     * It happens when rules recursively try to annotate (that is, expand grammar rules)
     */
    public void throwExceptionWhenReachingMaximumDepth(boolean throwIt){
        factory.throwExceptionWhenReachingMaximumDepth(throwIt);
    }
}
