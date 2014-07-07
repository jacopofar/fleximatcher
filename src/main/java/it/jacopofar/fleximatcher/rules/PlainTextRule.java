package it.jacopofar.fleximatcher.rules;

import it.jacopofar.fleximatcher.annotations.AnnotationHandler;
import opennlp.tools.util.Span;

public class PlainTextRule extends MatchingRule {

	private String string;

	public PlainTextRule(String s) {
		this.string=s;
	}

	@Override
	public boolean annotate(String text, AnnotationHandler ah) {
		if(text.equals(string)){
			ah.addAnnotation(new Span(0,string.length()),null);
			return true;
		}
		int lastIndex=0;
		while(true){
			int ind=text.indexOf(string, lastIndex);
			if(ind==-1)
				break;
			ah.addAnnotation(new Span(ind,ind+string.length()),null);
			lastIndex=ind+string.length();
		}
		//no perfect match
		return false;
	}

	@Override
	public String toString() {
		return "plain text:'"+string+"'";
	}

}
