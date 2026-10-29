package com.threeamigos.foresta.testi;

/**
 * Le frasi a caso della sfida a carta, forbici e sasso (vedi {@code sfide.txt} e carta_forbici_sasso.md).
 */
public final class ProduttoreDiSfideCasuale {

	private static final GrammaticaPigra SFIDE = GrammaticaPigra.deiTesti("sfide.txt", false);

	private ProduttoreDiSfideCasuale() {
	}

	/**
	 * Carica subito la grammatica, invece di aspettare il primo uso (vedi {@link ProduttoriDiTesti#precarica()}).
	 *
	 * @throws GrammaticaNonValidaException se la grammatica manca o non è valida
	 */
	public static void precarica() {
		SFIDE.get();
	}

	/**
	 * La frase con cui chi ha stretto amicizia sfida il capo del gruppo a carta, forbici e sasso (vedi sfide.txt).
	 */
	public static String fraseDiSfida() {
		return SFIDE.get().produce("SFIDA").get(0).trim();
	}

	/**
	 * Quello che dice l'avversario quando il giocatore vince la sfida (vedi sfide.txt).
	 */
	public static String fraseSeIlGiocatoreVince() {
		return SFIDE.get().produce("GIOCATORE_VINCE").get(0).trim();
	}

	/**
	 * Quello che dice l'avversario quando il giocatore perde la sfida (vedi sfide.txt).
	 */
	public static String fraseSeIlGiocatorePerde() {
		return SFIDE.get().produce("GIOCATORE_PERDE").get(0).trim();
	}

	/**
	 * Quello che dice l'avversario alla terza sfida vinta dal gruppo, per mandarlo al torneo (vedi sfide.txt).
	 */
	public static String fraseDelTorneo() {
		return SFIDE.get().produce("TORNEO").get(0).trim();
	}

	/**
	 * Il nome, con l'articolo, di un leggendario costruito al volo per la ricompensa de La sfida dei campioni (vedi
	 * sfide.txt).
	 */
	public static String nomeLeggendarioDeiCampioni() {
		return SFIDE.get().produce("NOME_LEGGENDARIO_DEI_CAMPIONI").get(0).trim();
	}
}
