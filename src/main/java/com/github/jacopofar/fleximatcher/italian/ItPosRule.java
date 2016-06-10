package com.github.jacopofar.fleximatcher.italian;

import java.util.HashSet;

import com.github.jacopofar.fleximatcher.annotations.AnnotationHandler;
import com.github.jacopofar.fleximatcher.rules.MatchingRule;
import opennlp.tools.util.Span;

import org.json.JSONException;
import org.json.JSONObject;

import com.github.jacopofar.italib.ItalianModel;
import com.github.jacopofar.italib.postagger.POSUtils;

public class ItPosRule extends MatchingRule {

	private final ItalianModel im;
	private HashSet<String> acceptedTags;

	public ItPosRule(ItalianModel im, String tag) {
		this.im=im;
		if(tag.isEmpty())
			throw new RuntimeException("tag not valid, mustn't be empty");
		acceptedTags=new HashSet<>();
		for(String candidateTag:POSUtils.getPossibleTags()){
			if(candidateTag.matches(tag))
				acceptedTags.add(candidateTag);		
		}
		
		if(acceptedTags.isEmpty())
			throw new RuntimeException("tag "+tag+" not recognized");
	}

	@Override
	public boolean annotate(String text,AnnotationHandler ah) {
		Span[] tags = im.getPosTags(text);
		if(tags.length==1){
			if(tags[0].length()==text.length() && acceptedTags.contains(tags[0].getType()))
				try {
					ah.addAnnotation(tags[0],new JSONObject("{'tag':'"+tags[0].getType()+"'}"));
				} catch (JSONException e) {
					e.printStackTrace();
				}
		}

		for(Span t:tags){
			if(acceptedTags.contains(t.getType()))
				try {
					ah.addAnnotation(t,new JSONObject("{'tag':'"+t.getType()+"'}"));
				} catch (JSONException e) {
					e.printStackTrace();
				}
		}
		return false;
	}

	@Override
	public String toString() {
		return "matches the italian POS tags "+acceptedTags.toString();
	}
}
