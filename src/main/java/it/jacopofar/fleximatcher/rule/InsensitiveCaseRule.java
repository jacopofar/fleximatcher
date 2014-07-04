package it.jacopofar.fleximatcher.rule;

import it.jacopofar.fleximatcher.annotations.AnnotationHandler;
import it.jacopofar.fleximatcher.rules.MatchingRule;
import opennlp.tools.util.Span;

public class InsensitiveCaseRule extends MatchingRule {

	private String string;

	public InsensitiveCaseRule(String s) {
		this.string=s.toLowerCase();
	}

	@Override
	public boolean annotate(String text, AnnotationHandler ah) {
		text=text.toLowerCase();
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
		//no perfect match, return nothing
		return false;
	}

	@Override
	public String getDescription() {
		return "plain text:'"+string+"'";
	}

}
