package it.jacopofar.fleximatcher.italian;

import java.io.FileNotFoundException;
import java.sql.SQLException;

import org.json.JSONException;

import com.github.jacopofar.italib.ItalianModel;

import it.jacopofar.fleximatcher.rule.RuleFactory;
import it.jacopofar.fleximatcher.rules.MatchingRule;

public class ItVerbFormRuleFactory implements RuleFactory {

	private ItalianModel im;
	public ItVerbFormRuleFactory(){
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
	public ItVerbFormRuleFactory(ItalianModel im2) {
		this.im=im2;
	}
	@Override
	public MatchingRule getRule(String parameter) {
		try {
			return new ItVerbFormRule(im,parameter);
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}
}
