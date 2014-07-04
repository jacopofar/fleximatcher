package it.jacopofar.fleximatcher;

import it.jacopofar.fleximatcher.annotations.AnnotationHandler;
import it.jacopofar.fleximatcher.annotations.DefaultAnnotationHandler;
import it.jacopofar.fleximatcher.annotations.ResultPrintingAnnotationHandler;
import it.jacopofar.fleximatcher.expressions.ExpressionParser;
import it.jacopofar.fleximatcher.italian.ItPosRuleFactory;
import it.jacopofar.fleximatcher.italian.ItVerbRuleFactory;
import it.jacopofar.fleximatcher.rule.RegexRuleFactory;
import it.jacopofar.fleximatcher.rule.RuleFactory;
import it.jacopofar.fleximatcher.rules.InsensitiveCaseRuleFactory;
import it.jacopofar.fleximatcher.rules.MatchingRule;
import it.jacopofar.fleximatcher.rules.MultiRuleFactory;
import it.jacopofar.fleximatcher.rules.PlainTextRule;

import java.util.concurrent.ConcurrentHashMap;


public class FlexiMatcher {

	private ConcurrentHashMap<String,RuleFactory> rules=new ConcurrentHashMap<String,RuleFactory>();
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
	public boolean matches(String text,String pattern, AnnotationHandler ah, boolean fullyAnnotate,boolean matchWhole){
		String[] ruleParts = ExpressionParser.split(pattern);
		MatchingRule[] ruleset=new MatchingRule[ruleParts.length];
		int i=0;
		boolean isSurelyWrong=false;
		for(String s:ruleParts){
			if(!s.startsWith("[")){
				if(!text.contains(s))
					if(!fullyAnnotate)
						return false;
					else
						isSurelyWrong=true;
				ruleset[i++]=new PlainTextRule(s);
			}
			else{
				ruleset[i++]=rules.get(ExpressionParser.ruleName(s)).getRule(ExpressionParser.getParameter(s));
			}
		}
		//now the ruleset is created, annotate using it
		i=0;
		for(MatchingRule mr:ruleset){
			if(!mr.isCacheable() || !ah.hasBeenUsed(ruleParts[i++])){
				ah.setCurrentMatcher(ruleParts[i-1]);
				int annotationBefore=ah.getAnnotationNumbers();
				boolean singleMatch = mr.annotate(text,ah);
				if(ruleParts.length==1 && singleMatch)
					return true;
				if(annotationBefore<ah.getAnnotationNumbers() && mr.getTag().isPresent()){
					//the current rule changed some tag, remember it
					ah.notifyChangedTag(mr.getTag().get());
				}
			}
			ah.rememberUse(ruleParts[i-1]);
		}

		//the text is annotated, is it surely wrong? skip the matching
		if(isSurelyWrong)
			return false;
		return ah.checkAnnotationSequence(ruleParts, text.length(),matchWhole);
		
	}



	public boolean matches(String text,String pattern){
		return matches(text,pattern, new DefaultAnnotationHandler(),false,true);
	}

	public FlexiMatcher(){
		this.bind("r", new RegexRuleFactory());
		this.bind("i", new InsensitiveCaseRuleFactory());
		this.bind("multi", new MultiRuleFactory(this));
	}

	public static void main(String argc[]){
		FlexiMatcher fm=new FlexiMatcher();
		fm.bind("it-pos", new ItPosRuleFactory());
		fm.bind("it-verb", new ItVerbRuleFactory());
		
		String analyzeThis=" il cane è alto!";
		System.out.println(fm.matches("[it-pos:RD] [it-pos:Ss] è [it-pos:As]",analyzeThis, new ResultPrintingAnnotationHandler(analyzeThis),true,false));
	}
}
