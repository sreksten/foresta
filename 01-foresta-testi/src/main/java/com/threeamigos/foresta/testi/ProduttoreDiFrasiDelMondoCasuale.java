package com.threeamigos.foresta.testi;

/**
 * Le frasi a caso dei personaggi del mondo nei fumetti e nei dialoghi ({@code mondo.txt}): come si presenta il
 * protagonista all'inizio della partita come salutano i negozianti quando il gruppo entra nella loro bottega e la scenetta di notte ("non chiudete mai?"). La formula con cui ci si presenta ({@code FORMULA_NOME}) sta in un punto solo e la
 * includono anche le grammatiche che ne hanno bisogno (le locande inglesi).
 */
public final class ProduttoreDiFrasiDelMondoCasuale {

	private static final GrammaticaPigra MONDO = GrammaticaPigra.deiTesti("mondo.txt", false);

	private ProduttoreDiFrasiDelMondoCasuale() {
	}

	/**
	 * Carica subito la grammatica, invece di aspettare il primo uso (vedi {@link ProduttoriDiTesti#precarica()}).
	 *
	 * @throws GrammaticaNonValidaException se la grammatica manca o non è valida
	 */
	public static void precarica() {
		MONDO.get();
	}

	/**
	 * Quello che dice il protagonista quando si presenta all'eremita: il suo nome e che cosa cerca (vedi mondo.txt).
	 *
	 * @param nome il nome con cui si presenta l'eroe
	 */
	public static String presentazioneDelProtagonista(String nome) {
		MONDO.get().addFixedProduction("NOME_EROE", nome);
		return MONDO.get().produce("PRESENTAZIONE_EROE").get(0).trim();
	}

	/**
	 * Il saluto dell'armaiolo quando il gruppo entra nella sua bottega (vedi mondo.txt).
	 */
	public static String salutoDellArmaiolo() {
		return MONDO.get().produce("SALUTO_ARMAIOLO").get(0).trim();
	}

	/**
	 * Il saluto dell'alchimista quando il gruppo entra nella sua bottega (vedi mondo.txt).
	 */
	public static String salutoDellAlchimista() {
		return MONDO.get().produce("SALUTO_ALCHIMISTA").get(0).trim();
	}

	/**
	 * Il saluto dell'incantatore quando il gruppo entra nel suo laboratorio (vedi mondo.txt).
	 */
	public static String salutoDellIncantatore() {
		return MONDO.get().produce("SALUTO_INCANTATORE").get(0).trim();
	}

	/**
	 * Il saluto del venditore di pergamene quando il gruppo entra nel suo negozio (vedi mondo.txt).
	 */
	public static String salutoDelVenditoreDiPergamene() {
		return MONDO.get().produce("SALUTO_VENDITORE").get(0).trim();
	}

	/**
	 * Quello che chiede il capo a un negoziante che di notte è ancora aperto: se quelli come lui non chiudono mai (vedi mondo.txt).
	 *
	 * @param negozianti il plurale del mestiere, in minuscolo ("armaioli")
	 */
	public static String domandaAlNegozianteSempreAperto(String negozianti) {
		MONDO.get().addFixedProduction("NEGOZIANTI", negozianti);
		return MONDO.get().produce("NON_CHIUDETE_MAI_DOMANDA").get(0).trim();
	}

	/**
	 * La risposta del negoziante a chi gli chiede perché non chiude mai (vedi mondo.txt).
	 */
	public static String rispostaDelNegozianteSempreAperto() {
		return MONDO.get().produce("NON_CHIUDETE_MAI_RISPOSTA").get(0).trim();
	}
}
