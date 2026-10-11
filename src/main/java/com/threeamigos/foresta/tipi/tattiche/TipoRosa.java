package com.threeamigos.foresta.tipi.tattiche;

/**
 * La rosa di candidati da cui pesca il selettore di una voce di tattica, prima dei filtri (vedi tattiche_di_combattimento_automatiche.md §11.3).
 * È solo il nome (e il lato per cui vale): chi calcola i candidati è la classe del pacchetto <code>tattiche</code> che gli corrisponde
 * (vedi <code>tattiche.Rose</code>). Sta in <code>tipi</code> perché serve anche alla UI, per la scheda delle tattiche.
 * <p>
 * "Alleati" e "avversari" sono quelli dell'<b>attore</b>: per un mio personaggio gli avversari sono i mostri, per un mostro sono i miei personaggi.
 */
public enum TipoRosa {

	/** Il solo attore */
	SE_STESSO("Se stesso", true, true),
	/** I compagni vivi e in campo, l'attore compreso */
	ALLEATI("Alleati", true, true),
	/** I compagni caduti (non in panchina): solo per le azioni che lo dichiarano, la Resurrezione */
	ALLEATI_CADUTI("Alleati caduti", true, true),
	/** I vivi e in campo dell'altro gruppo */
	AVVERSARI("Avversari", true, true),
	/** Gli ospiti vulnerabili vivi del gruppo del giocatore: da proteggere per i miei, bersagli per i mostri */
	OSPITI("Ospiti", true, true),
	/** Gli alleati e poi gli ospiti vulnerabili: solo per i miei personaggi (per le cure) */
	ALLEATI_E_OSPITI("Alleati e ospiti", true, false),
	/** Gli avversari e poi gli ospiti vulnerabili: solo per i mostri (la scelta a caso dei mostri, RegistroTattiche) */
	AVVERSARI_E_OSPITI("Avversari e ospiti", false, true);

	private final String descrizione;
	private final boolean validoPerIPersonaggiDelGiocatore;
	private final boolean validoPerIMostri;

	TipoRosa(String descrizione, boolean validoPerIPersonaggiDelGiocatore, boolean validoPerIMostri) {
		this.descrizione = descrizione;
		this.validoPerIPersonaggiDelGiocatore = validoPerIPersonaggiDelGiocatore;
		this.validoPerIMostri = validoPerIMostri;
	}

	public String getDescrizione() {
		return descrizione;
	}

	/**
	 * @param attoreDelGiocatore true se chi decide è un personaggio del giocatore, false se è un mostro
	 */
	public boolean valePer(boolean attoreDelGiocatore) {
		return attoreDelGiocatore ? validoPerIPersonaggiDelGiocatore : validoPerIMostri;
	}
}
