package com.threeamigos.foresta.motore;

import java.io.IOException;
import java.util.List;

import com.threeamigos.foresta.motore.GrammarBean.InvalidGrammarException;

public class ProduttoreDiTestiCasuale {

	private static GrammarBean fiabe;
	private static GrammarBean oroscopi;

	private ProduttoreDiTestiCasuale() {
	}

	static {
		try {
			fiabe = new GrammarBean(
					ProduttoreDiTestiCasuale.class.getResourceAsStream("/com/threeamigos/foresta/motore/fiabe.txt"),
					ProduttoreDiTestiCasuale.class.getResourceAsStream("/com/threeamigos/foresta/motore/preposizioni_articolate_pp.txt"));
			oroscopi = new GrammarBean(
					ProduttoreDiTestiCasuale.class.getResourceAsStream("/com/threeamigos/foresta/motore/oroscopo.txt"),
					ProduttoreDiTestiCasuale.class.getResourceAsStream("/com/threeamigos/foresta/motore/preposizioni_articolate_pp.txt"));
		} catch (InvalidGrammarException | IOException e) {
			Logger.log(e);
			System.exit(0);
		}
	}

	public static final List<String> fiaba() {
		List<String> fiaba = fiabe.produce();
		fiabe.reset();
		return fiaba;
	}

	public static final List<String> oroscopo() {
		List<String> oroscopo = oroscopi.produce();
		oroscopi.reset();
		return oroscopo;
	}
}
