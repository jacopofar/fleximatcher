package it.jacopofar.fleximatcher.italian;

import it.jacopofar.fleximatcher.annotations.AnnotationHandler;
import it.jacopofar.fleximatcher.rules.MatchingRule;

import java.util.Set;

import opennlp.tools.util.Span;

import org.json.JSONException;
import org.json.JSONObject;

import com.github.jacopofar.italib.ItalianModel;
import com.github.jacopofar.italib.ItalianVerbConjugation;

public class ItVerbFormRule extends MatchingRule {

	private ItalianModel im;
	private JSONObject params;
	public ItVerbFormRule(ItalianModel im, String parameter) throws JSONException {
		this.im=im;
		try {
			this.params = new JSONObject(parameter.startsWith("{")?parameter:"{'form':'"+parameter+"'");
		} catch (JSONException e1) {
			throw new RuntimeException("Invalid JSON: "+parameter);
		}
	}

	@Override
	public boolean annotate(String text, AnnotationHandler ah) {
		boolean complete=false;
		for(Span token:im.getTokens(text)){
			Set<ItalianVerbConjugation> possibleVerbs = im.getVerbs(token.getCoveredText(text).toString(), true);
			for(ItalianVerbConjugation v:possibleVerbs){
				try {
					if(params.has("mode") && !v.getMode().equals(params.getString("mode")))
						continue;
					if(params.has("person") && v.getPerson()!=params.getInt("person"))
						continue;
					if(params.has("number") && v.getNumber()!=params.getString("number").charAt(0))
						continue;
					ah.addAnnotation(token, new JSONObject(v.toJSON()));
				} catch (JSONException e) {
					e.printStackTrace();
					throw new RuntimeException("JSON format error");
				}
				if(token.length()==text.length())
					complete=true;
			}

		}
		return complete;

	}

	@Override
	public String toString() {
		return "forms of an Italian verb, "+params.toString()+" forms accepted";
	}

}
