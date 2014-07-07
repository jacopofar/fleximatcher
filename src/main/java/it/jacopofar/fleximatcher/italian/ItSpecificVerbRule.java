package it.jacopofar.fleximatcher.italian;

import java.util.HashSet;

import opennlp.tools.util.Span;

import org.json.JSONException;
import org.json.JSONObject;

import com.github.jacopofar.italib.ItalianModel;
import com.github.jacopofar.italib.ItalianVerbConjugation;
import com.github.jacopofar.italib.ItalianVerbConjugation.ConjugationException;

import it.jacopofar.fleximatcher.annotations.AnnotationHandler;
import it.jacopofar.fleximatcher.rules.MatchingRule;

public class ItSpecificVerbRule extends MatchingRule {

	private HashSet<String> accepted;
	private ItalianModel im;
	public ItSpecificVerbRule(ItalianModel im, String parameter) throws JSONException {
		this.im=im;
		JSONObject params;
		try {
			params = new JSONObject(parameter.startsWith("{")?parameter:"{'infinitive':'"+parameter+"'}");
		} catch (JSONException e1) {
			throw new RuntimeException("Invalid JSON: "+parameter);
		}

		HashSet<String> res=new HashSet<String> ();
		ItalianVerbConjugation v = new ItalianVerbConjugation(im);
		v.setInfinitive(params.getString("infinitive"));
		for(String mode:ItalianVerbConjugation.getImpersonalModes()){
			if(params.has("mode") && !params.getString("mode").equals(mode))
				continue;
			v.setMode(mode);
			try {
				res.add(v.getConjugated());
			} catch (ConjugationException e) {
				e.printStackTrace();
			}
		}
		for(String mode:ItalianVerbConjugation.getPersonalModes()){
			if(params.has("mode") && !params.getString("mode").equals(mode))
				continue;
			for(int person:new Integer[]{1,2,3}){
				if(params.has("person") && params.getInt("person")!=person)
					continue;
				for(char num:new Character[]{'s','p'}){
					if(params.has("number") && !params.getString("number").equals(num+""))
						continue;
					if(mode.equals("imperative") && person==1 && num=='s')
						continue;
					v.setMode(mode);
					v.setNumber(num);
					v.setPerson(person);
					try {
						res.add(v.getConjugated());
					} catch (ConjugationException e) {
						e.printStackTrace();
					}
				}
			}
		}
		accepted=res;
	}

	@Override
	public boolean annotate(String text, AnnotationHandler ah) {
		for(Span token:im.getTokens(text)){
			if(accepted.contains(token.getCoveredText(text.toLowerCase()))){
				ah.addAnnotation(token, null);
			}
		}
		return accepted.contains(text.toLowerCase());

	}

	@Override
	public String toString() {
		return "forms of an Italian verb, "+accepted.size()+" forms accepted";
	}

}
