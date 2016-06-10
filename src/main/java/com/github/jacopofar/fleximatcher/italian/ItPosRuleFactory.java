package com.github.jacopofar.fleximatcher.italian;

import java.io.FileNotFoundException;
import java.sql.SQLException;

import com.github.jacopofar.fleximatcher.rule.RuleFactory;
import com.github.jacopofar.fleximatcher.rules.MatchingRule;
import com.github.jacopofar.italib.ItalianModel;

public class ItPosRuleFactory implements RuleFactory {
	private ItalianModel im;
	public ItPosRuleFactory(){
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
	public ItPosRuleFactory(ItalianModel im2) {
		this.im=im2;
	}
	public MatchingRule getRule(String parameter) {
		return new ItPosRule(im,parameter);
	}

}
