package it.jacopofar.fleximatcher;

import it.jacopofar.fleximatcher.annotations.AnnotationHandler;
import it.jacopofar.fleximatcher.annotations.DefaultAnnotationHandler;
import it.jacopofar.fleximatcher.annotations.MatchingResults;
import it.jacopofar.fleximatcher.annotations.ResultPrintingAnnotationHandler;
import it.jacopofar.fleximatcher.annotations.TextAnnotation;
import it.jacopofar.fleximatcher.expressions.ExpressionParser;
import it.jacopofar.fleximatcher.italian.ItPosRuleFactory;
import it.jacopofar.fleximatcher.italian.ItSpecificVerbRuleFactory;
import it.jacopofar.fleximatcher.italian.ItVerbFormRuleFactory;
import it.jacopofar.fleximatcher.rule.RegexRuleFactory;
import it.jacopofar.fleximatcher.rule.RuleFactory;
import it.jacopofar.fleximatcher.rules.InsensitiveCaseRuleFactory;
import it.jacopofar.fleximatcher.rules.MatchingRule;
import it.jacopofar.fleximatcher.rules.MultiRuleFactory;
import it.jacopofar.fleximatcher.rules.PlainTextRule;
import it.jacopofar.fleximatcher.tag.TagRuleFactory;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;


public class FlexiMatcher {

	private ConcurrentHashMap<String,RuleFactory> rules=new ConcurrentHashMap<String,RuleFactory>();
	private TagRuleFactory factory;
	/**
	 * Add or replace a rule, binding it to a name.
	 * Returns true if the same name was already in use and the RuleFactory has been replaced, false otherwise
	 * */
	public boolean bind(String ruleName,RuleFactory rf){
		return (rules.put(ruleName, rf)!=null);
	}


	/**
	 * Matches the text against the given pattern, using the given annotator
	 * @param text the string to match (e.g.: "the dog"
	 * @param pattern the pattern to search for (e.g. "the [r:[a-z]+]")
	 * @param ah the AnnotationHandler which will be used to store annotations
	 * @param fullyAnnotate if true, will annotate the whole string, if false will stop as soon as is sure that the string doesn't match
	 * @param matchWhole if true, will match the pattern against the whole text, if false will search for it inside the string.
	 * For example matches("this is the dog!","the [r:[a-z]+]",ah, true, false) will return true ignoring the leading "this is " and the trailing "!"
	 * */
	public MatchingResults matches(String text,String pattern, AnnotationHandler ah, boolean fullyAnnotate,boolean matchWhole,boolean populateResult){
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
	 * */
	public boolean matches(String text,String pattern){
		return matches(text,pattern, new DefaultAnnotationHandler(),false,true,false).isMatching();
	}
	/**
	 * Check whether the pattern is contained in the text. That is, it matches with a substring of it
	 * */
	public boolean contains(String text,String pattern){
		return matches(text,pattern, new DefaultAnnotationHandler(),false,false,false).isMatching();
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
		for(LinkedList<TextAnnotation> k:fm.matches(analyzeThis,"la [i:pera]", new ResultPrintingAnnotationHandler(analyzeThis),true,false,true).getAnnotations().get())
			System.out.println(">>"+k);
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
	 * Add a rule to this matcher, which will be used to macth [tag:rulename]
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
	 * Remove the rule with the given tag and identifier
	 * @return true if a rule was removed
	 * */
	public boolean removeTagRule(String tag,String identifier) {
		return factory.removeTagRule(tag,identifier);
		
	}
}
