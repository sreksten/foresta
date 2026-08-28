package com.threeamigos.foresta.motore;

public class TestFiabe extends TestGrammatiche {

	public static void main(String[] args) throws Exception {
		GrammarBean gBean = new GrammarBean(
				TestFiabe.class.getResourceAsStream("/com/threeamigos/foresta/motore/fiabe.txt"),
				TestFiabe.class.getResourceAsStream("/com/threeamigos/foresta/motore/preposizioni_articolate_pp.txt"));
		printProductions(gBean);
	}

}
