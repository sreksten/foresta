package com.threeamigos.foresta.motore;

public class TestFiabe {

	public static void main(String[] args) throws Exception {
		GrammarBean gBean = new GrammarBean(
				TestMissioni.class.getResourceAsStream("/com/threeamigos/foresta/motore/fiabe.txt"),
				TestMissioni.class.getResourceAsStream("/com/threeamigos/foresta/motore/preposizioni_articolate_pp.txt"));
		for (int i = 0; i < 5; i++) {
			for (String s : gBean.produce()) {
				System.out.println(s);
			}
			System.out.println("-----");
		}
	}
}
