package it.jacopofar.fleximatcher.italian;

import it.jacopofar.fleximatcher.annotations.AnnotationHandler;
import it.jacopofar.fleximatcher.rules.MatchingRule;
import opennlp.tools.util.Span;

import org.json.JSONException;
import org.json.JSONObject;

import com.github.jacopofar.italib.ItalianModel;
import com.github.jacopofar.italib.postagger.POSUtils;

public class ItPosRule extends MatchingRule {

	private ItalianModel im;
	private String tag;

	public ItPosRule(ItalianModel im, String tag) {
		this.im=im;
		if(!tag.matches("[A-Za-z0-9_]+"))
			throw new RuntimeException("tag "+tag+" not valid, must contain only letters, digits and underscores");
		if(POSUtils.getDescription(tag).equals("UNKNOWN TAG"))
			throw new RuntimeException("tag "+tag+" not recognized");
		this.tag=tag;
	}

	@Override
	public boolean annotate(String text,AnnotationHandler ah) {
		Span[] tags = im.getPosTags(text);
		if(tags.length==1){
			if(tags[0].length()==text.length() && tags[0].getType().equals(tag))
				try {
					ah.addAnnotation(tags[0],new JSONObject("{'tag':'"+tag+"'}"));
				} catch (JSONException e) {
					e.printStackTrace();
				}
		}

		for(Span t:tags){
			if(t.getType().equals(tag))
				try {
					ah.addAnnotation(t,new JSONObject("{'tag':'"+tag+"'}"));
				} catch (JSONException e) {
					e.printStackTrace();
				}
		}
		return false;
	}

	@Override
	public String toString() {
		return "matches the italian POS tag "+tag+" ("+POSUtils.getDescription(tag)+")";
	}
}
