package it.jacopofar.fleximatcher.italian;

import it.jacopofar.fleximatcher.annotations.AnnotationHandler;
import it.jacopofar.fleximatcher.rules.MatchingRule;
import opennlp.tools.util.Span;

import com.github.jacopofar.italib.ItalianModel;

public class ItTokenRule extends MatchingRule {

	private ItalianModel im;

	public ItTokenRule(ItalianModel im) {
		this.im=im;
	}

	@Override
	public boolean annotate(String text, AnnotationHandler ah) {
		Span[] tags = im.getTokens(text);
		if(tags.length==1){
			if(tags[0].length()==text.length())
				ah.addAnnotation(tags[0],null);

		}

		for(Span t:tags){
			ah.addAnnotation(t,null);

		}
		return false;
	}

}
