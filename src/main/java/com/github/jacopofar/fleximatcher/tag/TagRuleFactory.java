package com.github.jacopofar.fleximatcher.tag;

import com.github.jacopofar.fleximatcher.FlexiMatcher;
import com.github.jacopofar.fleximatcher.expressions.ExpressionParser;
import com.github.jacopofar.fleximatcher.rule.RuleFactory;
import com.github.jacopofar.fleximatcher.rules.MatchingRule;

import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;


public class TagRuleFactory implements RuleFactory {

    private int maximumNesting=15;
    private boolean throwExceptionWhenTooDeep=false;
    private final FlexiMatcher matcher;
    private final ConcurrentHashMap<String,LinkedList<RuleDefinition>> rules=new ConcurrentHashMap<>();

    public TagRuleFactory(FlexiMatcher flexiMatcher) {
        this.matcher=flexiMatcher;
    }

    @Override
    public MatchingRule getRule(String tagName) {
        return new TagRule(this,tagName);
    }

    public Stream<RuleDefinition> getTagPatterns(String name) {
        return rules.getOrDefault(name, new LinkedList<>()).stream();
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
                removed = rules.get(tag).removeIf(p->p.getIdentifier().equals(identifier));
            //insert the terminal patterns first, otherwise recursive matching using annotation counter and subhandlers will not work
            //see the case "an an an an apple" with the rules "tag:fruit => apple" and "tag:fruit => an [tag:fruit]"
            //in that case matching the second rule first it matches all the "an" on the first swipe and further recursive calls appear to not add anything
            if(pattern.contains("[tag:"))
                rules.get(tag).addLast(new RuleDefinition(pattern,identifier,annotationTemplate));
            else
                rules.get(tag).addFirst(new RuleDefinition(pattern,identifier,annotationTemplate));
            return removed;
        }
        else{
            LinkedList<RuleDefinition> p = new LinkedList<>();
            p.add(new RuleDefinition(pattern,identifier,annotationTemplate));
            rules.put(tag, p);
            return false;
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
            LinkedList<RuleDefinition> p = new LinkedList<>();
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
        boolean retVal = rules.get(tag).removeIf(p->p.getIdentifier().equals(identifier));
        //if the rule was the last one, delete the tag name too
        if (rules.get(tag).size() == 0){
            rules.remove(tag);
        }
        return retVal;
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

    public Stream<String> getTagNames() {
        return Collections.list(rules.keys()).stream();
    }

    public Stream<RuleDefinition> getTagDefinitions(String tagName) {
        LinkedList<RuleDefinition> rs = rules.get(tagName);
        if (rs == null){
            return Stream.empty();
        }
        return rules.get(tagName).stream();
    }

    @Override
    public String generateSample(String parameter) {
        if (!rules.containsKey(parameter)){
            return("[tag:" + parameter + "]");
        }
        int numCandidates = rules.get(parameter).size();
        if(numCandidates == 0) return null;
        int chosen = (int) Math.floor(Math.random() * (numCandidates));
        RuleDefinition c = rules.get(parameter).get(chosen);
        String result = "";
        for(String part:ExpressionParser.split(c.getPattern())){
            if(!part.startsWith("[") || !part.endsWith("]")){
                result += part;
                continue;
            }
            String ruleName = ExpressionParser.ruleName(part);
            if(ruleName.isEmpty()){
                //it's plain text, just use it
                result += ruleName;
                continue;
            }
            RuleFactory boundRule = matcher.getBoundRule(ruleName);
            //no bound rule? leave the rule part as a placeholder
            if(boundRule == null){
                result += part;
                continue;
            }
            String samplePart = boundRule.generateSample(ExpressionParser.getParameter(part));
            //use the generated part, or the original pattern as a placeholder
            if (samplePart == null)
                result += part;
            else
                result += samplePart;
        }
        return result;

    }
}
