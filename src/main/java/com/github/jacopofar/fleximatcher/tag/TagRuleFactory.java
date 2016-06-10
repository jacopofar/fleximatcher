package com.github.jacopofar.fleximatcher.tag;

import com.github.jacopofar.fleximatcher.rules.MatchingRule;
import com.github.jacopofar.fleximatcher.FlexiMatcher;
import com.github.jacopofar.fleximatcher.rule.RuleFactory;

import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;


public class TagRuleFactory implements RuleFactory {
    
    private int maximumNesting=5;
    private boolean throwExceptionWhenTooDeep=false;
    private final FlexiMatcher matcher;
    private final ConcurrentHashMap<String,HashSet<RuleDefinition>> rules=new ConcurrentHashMap<>();
    
    public TagRuleFactory(FlexiMatcher flexiMatcher) {
        this.matcher=flexiMatcher;
    }
    
    @Override
    public MatchingRule getRule(String tagName) {
        return new TagRule(this,tagName);
    }
    
    public Stream<RuleDefinition> getTagPatterns(String name) {
        return rules.getOrDefault(name, new HashSet<>()).stream();
    }
    
    public FlexiMatcher getMatcher() {
        return matcher;
        
    }
    
    public int getMaximumNesting() {
        return maximumNesting;
    }
    
    /**
     * Add a rule to the existing ones
     * @param tag the tag of the rule, which will be used in the tag [tag:name]
     * @param pattern the pattern that will be matched and tagged by this rule
     * @param identifier identifier an optional identifier for the rule, will be used to remove it
     * @param annotationTemplate
     * @return true if a rule with the same tag and identifier was replaced by the given one, false otherwise
     * If the identifier is null, it will be added and will always return false
     * */
    public boolean addTagRule(String tag, String pattern, String identifier, String annotationTemplate) {
        if(pattern.equals("[tag:"+tag+"]"))
            throw new RuntimeException("Circular definition of a rule");
        if(rules.containsKey(tag)){
            boolean removed=false;
            if(identifier!=null)
                removed= rules.get(tag).removeIf(p->p.getIdentifier().equals(identifier));
            rules.get(tag).add(new RuleDefinition(pattern,identifier,annotationTemplate));
            return removed;
        }
        else{
            HashSet<RuleDefinition> p = new HashSet<>();
            p.add(new RuleDefinition(pattern,identifier,annotationTemplate));
            rules.put(tag, p);
            return true;
        }
    }
    
    /**
     * Add a rule to the existing ones
     * @param tag the tag of the rule, which will be used in the tag [tag:name]
     * @param rule the rule definition object, containing a pattern and
     * @param identifier identifier an optional identifier for the rule, will be used to remove it
     * @return true if a rule with the same tag and identifier was replaced by the given one, false otherwise
     * If the identifier is null, it will be added and will always return false
     */
    public boolean addTagRule(String tag, String identifier,RuleDefinition rule) {
        if(rules.containsKey(tag)){
            boolean removed=false;
            if(identifier!=null)
                removed= rules.get(tag).removeIf(p->p.getIdentifier().equals(identifier));
            rules.get(tag).add(rule);
            return removed;
        }
        else{
            HashSet<RuleDefinition> p = new HashSet<>();
            p.add(rule);
            rules.put(tag, p);
            return true;
        }
    }
    
    
    /**
     * Remove the tag rule with the given identifier
     * @param tag the tag of the pattern to forget
     * @param identifier the identifier of the pattern to forget
     * @return true if a rule was removed, false otherwise
     * @throws RuntimeException if the tag is unknown or a parameter is null
     * */
    public boolean removeTagRule(String tag, String identifier) {
        if(tag==null || identifier==null)
            throw new RuntimeException("identifier and tag cannot be null");
        if(!rules.containsKey(tag))
            throw new RuntimeException("tag "+tag+" unknown!");
        return rules.get(tag).removeIf(p->p.getIdentifier().equals(identifier));
    }
    
    public void clearRules() {
        rules.clear();
    }
    
    public void setMaximumNesting(int maxDepth) {
        maximumNesting=maxDepth;
    }
    public void throwExceptionWhenReachingMaximumDepth(boolean doIt){
        throwExceptionWhenTooDeep=doIt;
    }
    public boolean throwExceptionWhenReachingMaximumDepth(){
       return throwExceptionWhenTooDeep;
    }
    
}
