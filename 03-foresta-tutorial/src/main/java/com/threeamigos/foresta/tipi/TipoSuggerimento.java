package com.threeamigos.foresta.tipi;

import com.threeamigos.foresta.testi.Testi;

/**
 * I suggerimenti del tutorial, nell'ordine in cui si danno quando più di uno scatta nella stessa situazione (vedi {@code tutorial.md} §4). Ognuno
 * dice quando scatta (la {@link Situazione} che lo innesca), a che cosa punta (il {@link BersaglioDelSuggerimento}) e ha le sue pagine di testo
 * (un file per lingua, {@code tipo_suggerimento_it.properties}, chiavi {@code COSTANTE.PAGINA_1}, {@code COSTANTE.PAGINA_2}...; la variante per il
 * dito è {@code COSTANTE.PAGINA_1.TOCCO}, e se manca vale quella normale). Una pagina per ogni cosa che si può fare, al massimo
 * {@value #PAGINE_MASSIME}.
 *
 * @author Stefano Reksten
 */
public enum TipoSuggerimento {

	PRIMI_PASSI(Situazione.PRIMA_SCHERMATA_DI_GIOCO, BersaglioDelSuggerimento.RIQUADRI),
	BARRA_DELLE_ICONE(Situazione.BARRA_DELLE_ICONE_VISIBILE, BersaglioDelSuggerimento.BARRA_DELLE_ICONE),
	MOVIMENTO_E_MAPPA(Situazione.ICONE_DI_MOVIMENTO_PRESENTI, BersaglioDelSuggerimento.ICONE_DI_MOVIMENTO),
	COMBATTIMENTO(Situazione.COMBATTIMENTO_APERTO, BersaglioDelSuggerimento.RIQUADRO_DEL_COMBATTIMENTO),
	OGGETTO_TROVATO(Situazione.OGGETTO_TROVATO, BersaglioDelSuggerimento.ICONA_DELL_INVENTARIO),
	POZIONI(Situazione.FERITO_CON_UNA_POZIONE, BersaglioDelSuggerimento.ICONE_DELLE_POZIONI),
	SCELTE_IN_COMBATTIMENTO(Situazione.SCELTE_IN_COMBATTIMENTO, BersaglioDelSuggerimento.ICONE_DI_COMBATTIMENTO),
	MISSIONI(Situazione.MISSIONE_ATTIVA, BersaglioDelSuggerimento.RIQUADRO_DELLE_MISSIONI),
	TATTICHE(Situazione.TATTICHE_DISPONIBILI, BersaglioDelSuggerimento.ICONA_DELLE_TATTICHE),
	CORRUZIONE(Situazione.AVVERSARIO_CORROMPIBILE, BersaglioDelSuggerimento.ICONA_DELLA_CORRUZIONE),
	AMICIZIA(Situazione.AVVERSARIO_AMICHEVOLE, BersaglioDelSuggerimento.ICONA_DELL_AMICIZIA),
	INCANTESIMI(Situazione.INCANTESIMO_LANCIABILE, BersaglioDelSuggerimento.RIQUADRO_DEGLI_INCANTESIMI_E_DELLE_POZIONI),
	BOTTEGHE(Situazione.INGRESSO_IN_BOTTEGA, BersaglioDelSuggerimento.ICONE_DELLE_BOTTEGHE),
	ACCAMPAMENTO(Situazione.NOTTE, BersaglioDelSuggerimento.ICONA_DELL_ACCAMPAMENTO),
	GESTI_SUL_TOCCO(Situazione.DITO_SULLO_SCHERMO, BersaglioDelSuggerimento.BARRA_DELLE_ICONE);

	/** Il massimo di pagine di un suggerimento: oltre, il concetto si spezza in due suggerimenti. */
	public static final int PAGINE_MASSIME = 3;

	private final Situazione innesco;
	private final BersaglioDelSuggerimento bersaglio;

	TipoSuggerimento(Situazione innesco, BersaglioDelSuggerimento bersaglio) {
		this.innesco = innesco;
		this.bersaglio = bersaglio;
	}

	/**
	 * La situazione in cui scatta.
	 */
	public Situazione getInnesco() {
		return innesco;
	}

	/**
	 * A che cosa punta il fumetto.
	 */
	public BersaglioDelSuggerimento getBersaglio() {
		return bersaglio;
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
