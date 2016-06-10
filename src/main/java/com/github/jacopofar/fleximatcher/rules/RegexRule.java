package com.github.jacopofar.fleximatcher.rules;

import com.github.jacopofar.fleximatcher.annotations.AnnotationHandler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import opennlp.tools.util.Span;

public class RegexRule extends MatchingRule {

	private Pattern pattern;

	public RegexRule(String s) {
		this.pattern = Pattern.compile(s);
	}

	@Override
	public boolean annotate(String text, AnnotationHandler ah) {
		synchronized(pattern){
			Matcher m=pattern.matcher(text);
			if(m.matches()){
				ah.addAnnotation(new Span(0,text.length()),null);
				for(int start=m.start();start<m.end()+1;start++)
					for(int end=start;end<m.end()+1;end++)
						if(pattern.matcher(text.substring(start, end)).matches())
							ah.addAnnotation(new Span(start,end),null);
				return true;
			}
			m.reset();
			while(m.find()){
				ah.addAnnotation(new Span(m.start(),m.end()),null);
				for(int start=m.start();start<m.end()+1;start++)
					for(int end=start;end<m.end()+1;end++)
						if(pattern.matcher(text.substring(start, end)).matches())
							ah.addAnnotation(new Span(start,end),null);

			}
		}
		//no full match
		return false;
	}

	@Override
	public String toString() {
		return "regular expression:'"+pattern+"'";
	}


}
