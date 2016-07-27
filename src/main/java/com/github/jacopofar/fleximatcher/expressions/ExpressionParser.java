package com.github.jacopofar.fleximatcher.expressions;

import java.util.HashSet;
import java.util.LinkedList;

public class ExpressionParser {


	/**
	 * Split a rule expression and returns an array of chunks
	 * For example:
	 * "the dog [pos: verb] the wall"
	 * will become:
	 * "the dog ","[pos: verb]"," the wall"
	 * spaces are preserved, the concatenation of the chunks is the original String
	 * */
	public static String[] split(String pattern) {
		//is this just a string?
		if(!pattern.contains("[")){
			return new String[]{pattern};
		}
		LinkedList<String> ret=new LinkedList<String>();
		int last=0;
		int openSquares=0;
		for(int k=0;k<pattern.length();k++){
			if(pattern.charAt(k)=='[') openSquares++;
			if(pattern.charAt(k)==']') openSquares--;

			if(pattern.charAt(k)=='[' && openSquares==1){
				//just entered an new chunk, store the previous one and update the starting point
				ret.add(pattern.substring(last,k));
				last=k;
			}
			if(pattern.charAt(k)==']' && openSquares==0){
				//just finished a chunk, store it if it's not empty
				ret.add(pattern.substring(last,k+1));
				last=k+1;
			}
		}
		//System.out.println("pattern '" + pattern + "' open brackets " + openSquares);
		if(openSquares != 0){
			//the last pattern was a "fake" one, for example 'aB[r:x'
			//it is very likely an user error, refuse it
			throw new RuntimeException("Square brackets are unbalanced in pattern '" + pattern + "'");
		}
		ret.add(pattern.substring(last));

		return ret.stream().filter(p->p!=null && p.length()>0).toArray(count->new String[count]);
	}

	/**
	 * returns the name of the rule expressed in the chunk
	 * for example for "[it-pos: Ss]" will return "it-pos"
	 * it will remove the first squared bracket
	 * */
	public static String ruleName(String s) {
		if(s.indexOf(':') == -1)
			return s.substring(1, s.length()-1);
		return s.substring(1, s.indexOf(':'));
	}
	/**
	 * returns the parameter of the rule expressed in the chunk
	 * for example for "[it-pos: Ss]" will return "Ss"
	 * it will remove the last squared bracket
	 * */
	public static String getParameter(String s) {
		if(s.indexOf(':')==-1)
			return "";
		return s.substring(s.indexOf(':')+1,s.length()-1);
	}

	/**
	 * Returns the set of tags used by this rule
	 * */
	public static HashSet<String> usedTags(String string){
		HashSet<String> res=new HashSet<String>();
		for(String s:split(string)){
			if(ruleName(s).equals("tag"))
				res.add(getParameter(s));
			if(ruleName(s).equals("multi"))
				res.addAll(usedTags(s));

		}
		return res;
	}
}
