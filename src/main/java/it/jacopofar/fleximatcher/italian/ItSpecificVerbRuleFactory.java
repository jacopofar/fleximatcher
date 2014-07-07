package it.jacopofar.fleximatcher.italian;

import java.io.FileNotFoundException;
import java.sql.SQLException;

import org.json.JSONException;

import com.github.jacopofar.italib.ItalianModel;

import it.jacopofar.fleximatcher.rule.RuleFactory;
import it.jacopofar.fleximatcher.rules.MatchingRule;

public class ItSpecificVerbRuleFactory implements RuleFactory {

	private ItalianModel im;
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
	@Override
	public MatchingRule getRule(String parameter) {
		try {
			return new ItSpecificVerbRule(im,parameter);
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

}
