package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.motore.GrammarBean.InvalidGrammarException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProduttoreDiTestiCasuale {

	private static GrammarBean fiabe;
	private static GrammarBean oroscopi;
	private static GrammarBean locande;

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
			locande = new GrammarBean(
					ProduttoreDiTestiCasuale.class.getResourceAsStream("/com/threeamigos/foresta/motore/locande.txt"),
					ProduttoreDiTestiCasuale.class.getResourceAsStream("/com/threeamigos/foresta/motore/preposizioni_articolate_pp.txt"));
		} catch (InvalidGrammarException | IOException e) {
			Logger.log(e);
			System.exit(0);
		}
	}

	public static List<String> fiaba() {
		List<String> fiaba = fiabe.produce();
		fiabe.reset();
		return fiaba;
	}

	public static List<String> oroscopo() {
		List<String> oroscopo = oroscopi.produce();
		oroscopi.reset();
		return oroscopo;
	}

	public static void resetProduzioni() {
		fiabe.reset();
		oroscopi.reset();
		locande.reset();
	}

	public static List<DatiLocanda> getDatiLocanda(int quantita) {
		List<DatiLocanda> datiLocanda = new ArrayList<>();
		for (int i = 0; i < quantita; i++) {
			List<String> produzioni = locande.produce();
			if (produzioni.size() > 1) {
				throw new IllegalArgumentException("La grammatica produce più di una riga di produzione per " + locande.getRootNode());
			}
			String[] token = produzioni.get(0).split("/");
			String nomeLocanda = token[0];
			String recensioneLocanda = token[1];
			String dialogoLocanda = token[2];
			final String chiaveRecensione = "RECENSIONE=";
			if (recensioneLocanda != null && recensioneLocanda.startsWith(chiaveRecensione)) {
				recensioneLocanda = recensioneLocanda.substring(chiaveRecensione.length());
			} else {
				recensioneLocanda = "";
			}
			final String chiaveDialogo = "DIALOGO=";
			if (dialogoLocanda != null && dialogoLocanda.startsWith(chiaveDialogo)) {
				dialogoLocanda = dialogoLocanda.substring(chiaveDialogo.length());
			} else {
				dialogoLocanda = "";
			}
			datiLocanda.add(new DatiLocanda(nomeLocanda, dialogoLocanda, recensioneLocanda));
		}
		return datiLocanda;
	}

	public static class DatiLocanda {
		private final String nome;
		private final String dialogo;
		private final String recensione;
		DatiLocanda(String nome, String dialogo, String recensione) {
			this.nome = nome;
			this.dialogo = dialogo;
			this.recensione = recensione;
		}

		public String getNome() {
			return nome;
		}

		public String getDialogo() {
			return dialogo;
		}

		public String getRecensione() {
			return recensione;
		}
	}
}
