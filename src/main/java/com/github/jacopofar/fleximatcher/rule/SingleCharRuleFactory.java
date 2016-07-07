package com.github.jacopofar.fleximatcher.rule;

import com.github.jacopofar.fleximatcher.rules.MatchingRule;
import com.github.jacopofar.fleximatcher.rules.PlainTextRule;

/**
 * Matches a given Unicode codepoint, as a decimal or as a "0x" prefixed hex string.
 * Works with surrogate pairs
 * Created on 2016-07-06.
 */
public class SingleCharRuleFactory implements RuleFactory {

    public MatchingRule getRule(String parameter) {
        if(parameter.isEmpty()){
            //this is surely an error, annotations cannot be empty
            throw new RuntimeException("Cannot create a character annotator with an empty pattern");
        }
        String thisChar = getChar(parameter);
        return new PlainTextRule(thisChar);
    }

    @Override
    public String generateSample(String parameter) {
        return getChar(parameter);
    }

    //a String to deal with surrogate pairs, just in case...
    static private String getChar(String parameter){
        int codePoint;
        if(parameter.startsWith("0x"))
            codePoint = Integer.parseInt(parameter.substring(2), 16);
        else
            codePoint = Integer.parseInt(parameter);
        return new String(Character.toChars(codePoint));
    }
}
