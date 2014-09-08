package it.jacopofar.fleximatcher.italian;

import java.io.FileNotFoundException;
import java.sql.SQLException;

import com.github.jacopofar.italib.ItalianModel;

import it.jacopofar.fleximatcher.rule.RuleFactory;
import it.jacopofar.fleximatcher.rules.MatchingRule;

public class ItTokenRuleFactory implements RuleFactory {

	private ItalianModel im;
	public ItTokenRuleFactory(){
		try {
			im = new ItalianModel();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public ItTokenRuleFactory(ItalianModel im2) {
		this.im=im2;
	}
	public MatchingRule getRule(String parameter) {
		return new ItTokenRule(im);
	}

}
