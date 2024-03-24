package com.threeamigos.foresta.motore;

import java.util.List;

public class TestMissioni {

	public static void main(String[] args) throws Exception {
		GrammarBean gBean = new GrammarBean(
				TestMissioni.class.getResourceAsStream("/com/threeamigos/foresta/motore/missioni.txt"),
				TestMissioni.class.getResourceAsStream("/com/threeamigos/foresta/motore/preposizioni_articolate_pp.txt"));
		List<String> production = gBean.produce();
		int tabulazioni = 0;
		int posGraffaAperta;
		int posGraffaChiusa;
		int posGraffa;
		for (String line : production) {
			posGraffaAperta = line.indexOf('{');
			posGraffaChiusa = line.indexOf('}');
			if (posGraffaAperta >= 0) {
				posGraffa = Math.min(posGraffaAperta, posGraffaChiusa);
			} else {
				posGraffa = posGraffaChiusa;
			}
			while (posGraffa >= 0) {
				if (posGraffa == posGraffaAperta) {
					tabs(tabulazioni);
					tabulazioni++;
					println(line.substring(0, posGraffa + 1).trim());
				}
				if (posGraffa == posGraffaChiusa) {
					tabulazioni--;
					tabs(tabulazioni);
					if (line.substring(0, posGraffa).trim().length() > 0) {
						println(line.substring(0, posGraffa).trim());
						tabs(tabulazioni);
					}
					System.out.println("}");
				}
				line = line.substring(posGraffa + 1).trim();
				posGraffaAperta = line.indexOf('{');
				posGraffaChiusa = line.indexOf('}');
				if (posGraffaAperta >= 0) {
					posGraffa = Math.min(posGraffaAperta, posGraffaChiusa);
				} else {
					posGraffa = posGraffaChiusa;
				}
			}
			println(line);
		}
	}
	
	private static final void println(String s) {
		System.out.println(s);
	}

	private static final void tabs(int tabulazioni) {
		for (int i = 0; i < tabulazioni; i++) {
			System.out.print("\t");
		}
	}
}
