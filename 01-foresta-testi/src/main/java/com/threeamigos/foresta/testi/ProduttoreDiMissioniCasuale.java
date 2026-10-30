package com.threeamigos.foresta.testi;

/**
 * I testi a caso delle missioni e dei personaggi del mondo: gli incarichi, i materiali richiesti, le spedizioni, gli oggetti
 * smarriti e i nomi di persona di {@code nomi.txt} (ostaggi, pellegrini, capibanda, sacerdoti, personaggi noti), più le righe delle missioni
 * a tappe di {@code tappe.txt}. Vedi missioni.txt, tappe.txt, nomi.md, passi_missioni.md e missione_a_tappe.md.
 */
public final class ProduttoreDiMissioniCasuale {

	private static final GrammaticaPigra MISSIONI = GrammaticaPigra.deiTesti("missioni.txt", true);
	// Le missioni a tappe stanno a parte: non usano nomi di persona (i capi li dà la missione) e hanno produzioni a esaurimento che si
	// ripristinano dopo ogni riga, cosa che non si può fare con la grammatica dei nomi condivisi. Niente preposizioni articolate da
	// sistemare: i nomi dei posti, che nascono con la mappa, li scrive la missione con i segnaposto
	private static final GrammaticaPigra TAPPE = GrammaticaPigra.deiTesti("tappe.txt", false);
	private static final Object SERRATURA_DELLE_TAPPE = new Object();

	private ProduttoreDiMissioniCasuale() {
	}

	/**
	 * Carica subito la grammatica, invece di aspettare il primo uso (vedi {@link ProduttoriDiTesti#precarica()}).
	 *
	 * @throws GrammaticaNonValidaException se la grammatica manca o non è valida
	 */
	public static void precarica() {
		MISSIONI.get();
		TAPPE.get();
	}

	/**
	 * Una riga di missione a tappe (vedi tappe.txt e TappeRichieste): una farsa o una missione seria (una persona o un leggendario da
	 * cercare, le tre chiavi, la scelta fra due piste). Quale chiedere lo decide chi chiama, perché la grammatica non può garantire
	 * una proporzione (vedi GrammarBean.md, §5.2). Le righe sono tutte diverse: le produzioni a esaurimento si ripristinano a ogni
	 * riga.
	 */
	public static String rigaDiTappe(boolean farsa) {
		synchronized (SERRATURA_DELLE_TAPPE) {
			GrammarBean grammatica = TAPPE.get();
			try {
				return String.join(" ", grammatica.produce(farsa ? "TAPPA_MISSIONE_FARSA" : "TAPPA_MISSIONE")).replace('\t', ' ').trim();
			} finally {
				grammatica.reset();
			}
		}
	}

	/**
	 * Il nome di un ostaggio da liberare (vedi missioni.txt).
	 */
	public static String nomeOstaggio() {
		return produciDaMissioni("NOME_OSTAGGIO");
	}

	/**
	 * Un nome di nomi.txt per un personaggio del mondo (vedi tipi.PersonaggioNoto): "Mastro Ferruccio", "Gualtiero"...
	 * I nomi one-shot sono quelli della partita: non si ripetono finché non si azzerano le sezioni statiche.
	 */
	public static String nomeDiPersonaggioNoto(String produzione) {
		return produciDaMissioni(produzione);
	}

	/**
	 * Il nome di un sacerdote (vedi NOME_SACERDOTE in nomi.txt): one-shot, non si ripete finché non si azzerano le sezioni
	 * statiche.
	 */
	public static String nomeSacerdote() {
		return produciDaMissioni("NOME_SACERDOTE");
	}

	/**
	 * Il nome di una sacerdotessa (vedi NOME_SACERDOTESSA in nomi.txt): one-shot, come quello del sacerdote.
	 */
	public static String nomeSacerdotessa() {
		return produciDaMissioni("NOME_SACERDOTESSA");
	}

	/**
	 * Il nome di un pellegrino (vedi missioni.txt).
	 */
	public static String nomePellegrino() {
		return produciDaMissioni("NOME_PELLEGRINO");
	}

	/**
	 * Una riga di una produzione di missioni.txt (gli incarichi di combattimento, le sorveglianze...).
	 */
	public static String rigaDiMissioni(String produzione) {
		return produciDaMissioni(produzione);
	}

	/**
	 * Una riga di una produzione di missioni.txt. I nomi di persona (nomi.txt) sono one-shot e condivisi: se si sono
	 * esauriti (e con loro le produzioni che li usano) si azzerano le sezioni statiche e si riparte.
	 */
	private static String produciDaMissioni(String produzione) {
		if (!MISSIONI.get().canProduce(produzione)) {
			GrammarBean.resetStaticProductions();
		}
		return MISSIONI.get().produce(produzione).get(0).trim();
	}

	/**
	 * Un materiale che un mandante chiede, da una produzione di missioni.txt (RICHIESTA_ALCHIMISTA...), come riga di
	 * otto campi separati da ";" (vedi MaterialeRichiesto).
	 */
	public static String materialeRichiesto(String produzione) {
		return MISSIONI.get().produce(produzione).get(0).trim();
	}

	/**
	 * Una cosa da portare da una città all'altra, come riga di dieci campi separati da ";" (vedi missioni.txt e
	 * Spedizione).
	 */
	public static String spedizione() {
		return MISSIONI.get().produce("TRASPORTO").get(0).trim();
	}

	/**
	 * Un oggetto smarrito nella foresta, come riga di nove campi separati da ";" (vedi missioni.txt e
	 * OggettoSmarrito).
	 */
	public static String oggettoSmarrito() {
		return MISSIONI.get().produce("OGGETTO_SMARRITO").get(0).trim();
	}

	/**
	 * Il nome di un capobanda di goblin o hobgoblin, a volte con un soprannome: "Grumolo il Guercio" (vedi
	 * missioni.txt).
	 */
	public static String nomeCapobanda() {
		return MISSIONI.get().produce("NOME_CAPOBANDA").get(0).trim();
	}
}
