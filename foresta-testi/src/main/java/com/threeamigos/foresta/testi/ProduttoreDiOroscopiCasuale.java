package com.threeamigos.foresta.testi;

import com.threeamigos.foresta.strumenti.logger.Logger;

/**
 * Gli oroscopi a caso dell'alchimista e il dialogo con il capo del gruppo che ne nasce (vedi {@code oroscopo.txt}).
 */
public final class ProduttoreDiOroscopiCasuale {

	private static final GrammaticaPigra OROSCOPI = GrammaticaPigra.deiTesti("oroscopo.txt", true);

	private ProduttoreDiOroscopiCasuale() {
	}

	/**
	 * Carica subito la grammatica, invece di aspettare il primo uso (vedi {@link ProduttoriDiTesti#precarica()}).
	 *
	 * @throws GrammaticaNonValidaException se la grammatica manca o non è valida
	 */
	public static void precarica() {
		OROSCOPI.get();
	}

	/**
	 * Un oroscopo a caso, la domanda del capo del gruppo, che vive nella Foresta e dell'oroscopo non capisce una parola, e
	 * come l'alchimista glissa (vedi oroscopo.txt). Il capo chiede dell'ultima cosa annotata con [TEMA=FORMA:nome] nella
	 * grammatica, con le domande DOMANDA_FORMA, che si accordano al genere e al numero; se l'oroscopo non ne cita nessuna, fa
	 * una domanda generica.
	 */
	public static DialogoSullOroscopo dialogoSullOroscopo() {
		return dialogoSullOroscopo(OROSCOPI.get());
	}

	// Il dialogo da una grammatica data: serve a provare gli oroscopi malformati, che la grammatica del gioco non ha
	static DialogoSullOroscopo dialogoSullOroscopo(GrammarBean oroscopi) {
		try {
			String prodotto = String.join(" ", oroscopi.produce("DIALOGO_SULL_OROSCOPO"));
			int separatore = prodotto.lastIndexOf('/');
			// Un oroscopo senza tema ("testo/FORMA:nome") non rompe il dialogo: il capo fa la domanda generica
			String oroscopo = (separatore < 0 ? prodotto : prodotto.substring(0, separatore)).trim();
			String tema = separatore < 0 ? "" : prodotto.substring(separatore + 1).trim();
			if (separatore < 0) {
				Logger.log(() -> "Oroscopo senza tema, manca il '/': " + prodotto);
			}
			int duePunti = tema.indexOf(':');
			if (duePunti < 0) {
				return new DialogoSullOroscopo(oroscopo, null, produciSulTema(oroscopi, "DOMANDA_GENERICA", null),
						produciSulTema(oroscopi, "RISPOSTA_SULL_OROSCOPO", null));
			}
			String forma = tema.substring(0, duePunti);
			String nome = tema.substring(duePunti + 1);
			return new DialogoSullOroscopo(oroscopo, nome, produciSulTema(oroscopi, "DOMANDA_" + forma, nome),
					produciSulTema(oroscopi, "RISPOSTA_SULL_OROSCOPO", nome));
		} finally {
			oroscopi.reset();
		}
	}

	private static String produciSulTema(GrammarBean oroscopi, String produzione, String tema) {
		if (tema != null) {
			oroscopi.addFixedProduction("TEMA", tema);
		}
		return String.join(" ", oroscopi.produce(produzione)).trim();
	}

	/**
	 * L'oroscopo letto dall'alchimista, la cosa di cui il capo chiede (con l'articolo, null se fa una domanda generica), la
	 * domanda e la risposta.
	 */
	public static class DialogoSullOroscopo {
		private final String oroscopo;
		private final String tema;
		private final String domanda;
		private final String risposta;

		DialogoSullOroscopo(String oroscopo, String tema, String domanda, String risposta) {
			this.oroscopo = oroscopo;
			this.tema = tema;
			this.domanda = domanda;
			this.risposta = risposta;
		}

		/**
		 * L'oroscopo letto dall'alchimista.
		 */
		public String getOroscopo() {
			return oroscopo;
		}

		/**
		 * La cosa di cui il capo chiede, con l'articolo; null se fa una domanda generica.
		 */
		public String getTema() {
			return tema;
		}

		/**
		 * La domanda del capo del gruppo.
		 */
		public String getDomanda() {
			return domanda;
		}

		/**
		 * La risposta dell'alchimista, che glissa.
		 */
		public String getRisposta() {
			return risposta;
		}
	}

	/**
	 * Azzera le liste esauribili della grammatica (vedi {@link ProduttoriDiTesti#resetProduzioni()}).
	 */
	static void reset() {
		OROSCOPI.get().reset();
	}
}
