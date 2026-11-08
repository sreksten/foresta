package com.threeamigos.foresta.tipi;

import com.threeamigos.foresta.testi.Testi;

/**
 * I suggerimenti del tutorial, nell'ordine in cui si danno quando più di uno scatta nella stessa situazione (vedi {@code tutorial.md} §4). Ognuno
 * dice quando scatta (la {@link Situazione} che lo innesca), a che cosa punta (il {@link BersaglioDelSuggerimento}) e ha le sue pagine di testo
 * (un file per lingua, {@code tipo_suggerimento_it.properties}, chiavi {@code COSTANTE.PAGINA_1}, {@code COSTANTE.PAGINA_2}...; la variante per il
 * dito è {@code COSTANTE.PAGINA_1.TOCCO}, e se manca vale quella normale). Una pagina per ogni cosa che si può fare, al massimo
 * {@value #PAGINE_NORMALI} (l'elenco dei luoghi fa eccezione, vedi {@link #PAGINE_MASSIME}). A ogni pagina può puntare qualcosa di diverso.
 *
 * @author Stefano Reksten
 */
public enum TipoSuggerimento {

	SCELTA_DELLA_CLASSE(Situazione.SCELTA_DELLA_CLASSE, BersaglioDelSuggerimento.BARRA_DELLE_ICONE),
	PRIMI_PASSI(Situazione.PRIMA_SCHERMATA_DI_GIOCO, BersaglioDelSuggerimento.RIQUADRI),
	// La prima pagina parla della barra, la seconda del punto interrogativo
	BARRA_DELLE_ICONE(Situazione.BARRA_DELLE_ICONE_VISIBILE, BersaglioDelSuggerimento.BARRA_DELLE_ICONE, BersaglioDelSuggerimento.ICONA_DELL_AIUTO),
	// La prima pagina parla delle frecce, la seconda del riquadro della mappa, la terza dell'icona che apre la mappa a tutto schermo
	MOVIMENTO_E_MAPPA(Situazione.ICONE_DI_MOVIMENTO_PRESENTI, BersaglioDelSuggerimento.ICONE_DI_MOVIMENTO, BersaglioDelSuggerimento.RIQUADRO_DELLA_MAPPA,
			BersaglioDelSuggerimento.ICONA_DELLA_MAPPA),
	// Questi due si danno aprendo la mappa a tutto schermo (vedi SchermoDelGioco.isSchermataDelSuggerimento)
	JOYSTICK_DELLA_MAPPA(Situazione.JOYSTICK_SULLA_MAPPA, BersaglioDelSuggerimento.JOYSTICK_VIRTUALE),
	TRASCINARE_LA_MAPPA(Situazione.MAPPA_PIU_GRANDE_DELLO_SCHERMO, BersaglioDelSuggerimento.SCHERMATA),
	// I tipi di luogo: più pagine del solito, perché sono tanti e chi comincia non sa dove andare (vedi PAGINE_MASSIME)
	LUOGHI(Situazione.ICONA_DELLA_MAPPA_PRESENTE, BersaglioDelSuggerimento.ICONA_DELLA_MAPPA),
	COMBATTIMENTO(Situazione.COMBATTIMENTO_APERTO, BersaglioDelSuggerimento.RIQUADRO_DEL_COMBATTIMENTO),
	OGGETTO_TROVATO(Situazione.OGGETTO_TROVATO, BersaglioDelSuggerimento.ICONA_DELL_INVENTARIO),
	POZIONI(Situazione.FERITO_CON_UNA_POZIONE, BersaglioDelSuggerimento.ICONE_DELLE_POZIONI),
	SCELTE_IN_COMBATTIMENTO(Situazione.SCELTE_IN_COMBATTIMENTO, BersaglioDelSuggerimento.ICONE_DI_COMBATTIMENTO),
	MISSIONI(Situazione.MISSIONE_ATTIVA, BersaglioDelSuggerimento.RIQUADRO_DELLE_MISSIONI),
	// Le tattiche si aprono dall'inventario: il suggerimento punta alla sua icona, perché uno sia curioso di entrarci
	TATTICHE(Situazione.TATTICHE_DISPONIBILI, BersaglioDelSuggerimento.ICONA_DELL_INVENTARIO),
	// Si dà aprendo l'inventario: la prima pagina parla delle due liste, la seconda delle icone delle altre pagine
	INVENTARIO(Situazione.INVENTARIO_APERTO, BersaglioDelSuggerimento.SCHERMATA, BersaglioDelSuggerimento.ICONE_DELLE_PAGINE),
	// Nell'inventario, quando qualcuno ha punti abilità: gli attributi primari in arancione si aumentano con un doppio click
	PUNTI_ABILITA(Situazione.PUNTI_ABILITA_DA_SPENDERE, BersaglioDelSuggerimento.SCHERMATA),
	CORRUZIONE(Situazione.AVVERSARIO_CORROMPIBILE, BersaglioDelSuggerimento.ICONA_DELLA_CORRUZIONE),
	AMICIZIA(Situazione.AVVERSARIO_AMICHEVOLE, BersaglioDelSuggerimento.ICONA_DELL_AMICIZIA),
	// La prima pagina parla dell'icona che apre l'elenco (incantesimi e abilità innate), la seconda del riquadro con le scorte
	INCANTESIMI(Situazione.INCANTESIMO_LANCIABILE, BersaglioDelSuggerimento.ICONA_DEGLI_INCANTESIMI,
			BersaglioDelSuggerimento.RIQUADRO_DEGLI_INCANTESIMI_E_DELLE_POZIONI),
	BOTTEGHE(Situazione.INGRESSO_IN_BOTTEGA, BersaglioDelSuggerimento.ICONE_DELLE_BOTTEGHE),
	ACCAMPAMENTO(Situazione.NOTTE, BersaglioDelSuggerimento.ICONA_DELL_ACCAMPAMENTO),
	GESTI_SUL_TOCCO(Situazione.DITO_SULLO_SCHERMO, BersaglioDelSuggerimento.BARRA_DELLE_ICONE);

	/**
	 * Il massimo di pagine che un suggerimento può avere. Di norma sono al massimo {@value #PAGINE_NORMALI} (oltre, il concetto si spezza in due
	 * suggerimenti); l'elenco dei luoghi ne ha di più, perché i tipi di luogo sono tanti e fanno capire dove andare.
	 */
	public static final int PAGINE_MASSIME = 6;
	/** Il massimo di pagine di un suggerimento che non sia l'elenco dei luoghi. */
	public static final int PAGINE_NORMALI = 3;

	private final Situazione innesco;
	private final BersaglioDelSuggerimento[] bersagli;

	/**
	 * @param bersagli a che cosa punta la coda, pagina per pagina; l'ultimo vale anche per le pagine che seguono
	 */
	TipoSuggerimento(Situazione innesco, BersaglioDelSuggerimento... bersagli) {
		this.innesco = innesco;
		this.bersagli = bersagli;
	}

	/**
	 * La situazione in cui scatta.
	 */
	public Situazione getInnesco() {
		return innesco;
	}

	/**
	 * A che cosa punta il fumetto nella prima pagina: è quello che deve essere a schermo perché il suggerimento si apra.
	 */
	public BersaglioDelSuggerimento getBersaglio() {
		return bersagli[0];
	}

	/**
	 * A che cosa punta il fumetto in quella pagina (da 1): la pagina che parla dell'icona della mappa punta alla mappa, non alle frecce.
	 */
	public BersaglioDelSuggerimento getBersaglio(int pagina) {
		return bersagli[Math.max(0, Math.min(pagina, bersagli.length) - 1)];
	}

	/**
	 * Quante pagine ha nella lingua in uso (da 1 a {@value #PAGINE_MASSIME}).
	 */
	public int getNumeroPagine() {
		int pagine = 0;
		while (pagine < PAGINE_MASSIME && Testi.testo(this, "PAGINA_" + (pagine + 1)).isPresent()) {
			pagine++;
		}
		return pagine;
	}

	/**
	 * Il testo di una pagina (da 1), nella variante per il dito se {@code dito} è vero e la pagina ne ha una.
	 */
	public String getPagina(int pagina, boolean dito) {
		String voce = "PAGINA_" + pagina;
		if (dito) {
			java.util.Optional<String> perIlDito = Testi.testo(this, voce + ".TOCCO");
			if (perIlDito.isPresent()) {
				return perIlDito.get();
			}
		}
		return Testi.testoOCostante(this, voce);
	}
}
