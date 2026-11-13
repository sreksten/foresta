package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.testi.GestoreGrammatiche;

public class TestFiabe extends TestGrammatiche {

	public static void main(String[] args) throws Exception {
		GestoreGrammatiche gBean = GestoreGrammatiche.daRisorsa(TestFiabe.class,
				"/com/threeamigos/foresta/testi/fiabe.txt",
				"/com/threeamigos/foresta/testi/preposizioni_articolate_pp.txt");
		printProductions(gBean);
	}

}
