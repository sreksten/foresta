package com.threeamigos.foresta.missioni.richieste;

import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * Che cosa chiede il viandante incontrato nel bosco (vedi IlViandante), letto da una riga di VIANDANTE in missioni.txt, che ne
 * descrive i campi: i mostri da sconfiggere (la classe, quanti al minimo e al massimo, come si dicono al plurale, quante monete
 * per ciascuno) e i testi. Nei testi {@link #CITTA} è la città in cui il gruppo deve andare.
 */
public final class ViandanteRichiesto {

	public static final String CITTA = "%CITTA%";

	private final String riga;
	private final String chiave;
	private final TipoPersonaggio nemico;
	private final int minimo;
	private final int massimo;
	private final String plurale;
	private final int monete;
	private final String richiesta;
	private final String battutaDelCapo;
	private final String risposta;
	private final String arrivo;
	private final String ritorno;

	private ViandanteRichiesto(String riga) {
		this.riga = riga;
		CampiDiGrammatica campi = CampiDiGrammatica.da(riga, "CHIAVE", "NEMICO", "NUMERO", "PLURALE", "MONETE", "RICHIESTA", "BATTUTA",
				"RISPOSTA", "ARRIVO", "RITORNO");
		chiave = campi.obbligatorio("CHIAVE");
		nemico = campi.enumerato("NEMICO", TipoPersonaggio.class);
		String[] numero = campi.obbligatorio("NUMERO").split("-");
		if (numero.length != 2) {
			throw new IllegalArgumentException("Il numero è minimo-massimo, \"2-4\": " + riga);
		}
		minimo = Integer.parseInt(numero[0].trim());
		massimo = Integer.parseInt(numero[1].trim());
		if (minimo < 1 || massimo < minimo) {
			throw new IllegalArgumentException("Il numero va da almeno uno, e il massimo non è sotto il minimo: " + riga);
		}
		plurale = campi.obbligatorio("PLURALE");
		monete = campi.intero("MONETE");
		richiesta = campi.obbligatorio("RICHIESTA");
		battutaDelCapo = campi.obbligatorio("BATTUTA");
		risposta = campi.obbligatorio("RISPOSTA");
		arrivo = campi.obbligatorio("ARRIVO");
		ritorno = campi.obbligatorio("RITORNO");
	}

	/**
	 * Legge la riga della grammatica che descrive la richiesta.
	 *
	 * @throws IllegalArgumentException se la riga è sbagliata o manca un campo obbligatorio
	 */
	public static ViandanteRichiesto da(String riga) {
		return new ViandanteRichiesto(riga);
	}

	public String getRiga() {
		return riga;
	}

	public String getChiave() {
		return chiave;
	}

	/**
	 * La classe dei mostri da sconfiggere.
	 */
	public TipoPersonaggio getNemico() {
		return nemico;
	}

	public int getQuantitaMinima() {
		return minimo;
	}

	public int getQuantitaMassima() {
		return massimo;
	}

	/**
	 * I mostri al plurale, senza articolo: "troll", "arpie".
	 */
	public String getPlurale() {
		return plurale;
	}

	/**
	 * Quante monete paga per ogni mostro sconfitto, per il primo livello del mondo.
	 */
	public int getMonete() {
		return monete;
	}

	/**
	 * Quello che dice il viandante quando chiede aiuto.
	 */
	public String getRichiesta(String citta) {
		return richiesta.replace(CITTA, citta);
	}

	public String getBattutaDelCapo() {
		return battutaDelCapo;
	}

	public String getRisposta() {
		return risposta;
	}

	/**
	 * Quello che si legge arrivando nella città.
	 */
	public String getArrivo(String citta) {
		return arrivo.replace(CITTA, citta);
	}

	/**
	 * Quello che dice il viandante quando il gruppo torna a riferire.
	 */
	public String getRitorno() {
		return ritorno;
	}
}
