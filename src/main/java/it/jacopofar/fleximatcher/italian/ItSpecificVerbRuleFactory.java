package it.jacopofar.fleximatcher.italian;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONException;

import com.github.jacopofar.italib.ItalianModel;

import it.jacopofar.fleximatcher.rule.RuleFactory;
import it.jacopofar.fleximatcher.rules.MatchingRule;

public class ItSpecificVerbRuleFactory implements RuleFactory {

	private ItalianModel im;
	private ConcurrentHashMap<String,ItSpecificVerbRule> cache= new  ConcurrentHashMap<String,ItSpecificVerbRule>(100);
	public ItSpecificVerbRuleFactory(){
		try {
			im = new ItalianModel();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public ItSpecificVerbRuleFactory(ItalianModel im2) {
		this.im=im2;
	}
	@Override
	public MatchingRule getRule(String parameter) {
		if(cache.containsKey(parameter))
			return cache.get(parameter);
		try {
			ItSpecificVerbRule v = new ItSpecificVerbRule(im,parameter);
			if(cache.size()>100)
				cache.clear();
			cache.put(parameter, v);
			return v; 
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

}
