package com.threeamigos.foresta.tipi;

import com.threeamigos.foresta.ui.testo.TestiDellaUI;

/**
 * Le schermate di scambio a due colonne e quel che scrivono, uguale per Swing e libGDX: le intestazioni delle due colonne, chi sta al
 * centro (il negoziante, con il suo mestiere; nessuno nell'inventario, dove c'è il personaggio) e che cosa fa il doppio click su un
 * oggetto a sinistra e a destra.
 */
public enum TipoBottega {

	/** L'inventario: il personaggio a sinistra, il gruppo a destra */
	INVENTARIO(null, "INVENTARIO_PERSONAGGIO", "INVENTARIO_GRUPPO", null, null, "AIUTO_DOPPIO_CLICK_SPOSTA_NEL_GRUPPO",
			"AIUTO_DOPPIO_CLICK_EQUIPAGGIA"),
	ARMAIOLO(TipoNegozio.ARMAIOLO, "INVENTARIO_GRUPPO", "INVENTARIO_ARMAIOLO", TipoPersonaggio.ARMAIOLO, null, "AIUTO_DOPPIO_CLICK_VENDI",
			"AIUTO_DOPPIO_CLICK_COMPRA"),
	VENDITORE_DI_PERGAMENE(TipoNegozio.VENDITORE_DI_PERGAMENE, "INVENTARIO_GRUPPO", "INVENTARIO_VENDITORE", TipoPersonaggio.VENDITORE_DI_PERGAMENE,
			null, "AIUTO_DOPPIO_CLICK_VENDI", "AIUTO_DOPPIO_CLICK_COMPRA"),
	INCANTATORE(null, "INVENTARIO_GRUPPO", "BANCO_DI_LAVORO", TipoPersonaggio.INCANTATORE, "MESTIERE_INCANTATORE", "AIUTO_DOPPIO_CLICK_SUL_BANCO",
			"AIUTO_DOPPIO_CLICK_DAL_BANCO"),
	/** L'alchimista: i consumabili non si spostano col doppio click di una lista di artefatti, quindi niente testi per il doppio click */
	ALCHIMISTA(null, "INVENTARIO_GRUPPO", "INVENTARIO_ALCHIMISTA", TipoPersonaggio.ALCHIMISTA, "MESTIERE_ALCHIMISTA", null, null);

	private final TipoNegozio negozio;
	private final String intestazioneSinistra;
	private final String intestazioneDestra;
	private final TipoPersonaggio mestiere;
	private final String etichettaDelMestiere;
	private final String aiutoDoppioClickSinistra;
	private final String aiutoDoppioClickDestra;

	TipoBottega(TipoNegozio negozio, String intestazioneSinistra, String intestazioneDestra, TipoPersonaggio mestiere, String etichettaDelMestiere,
				String aiutoDoppioClickSinistra, String aiutoDoppioClickDestra) {
		this.negozio = negozio;
		this.intestazioneSinistra = intestazioneSinistra;
		this.intestazioneDestra = intestazioneDestra;
		this.mestiere = mestiere;
		this.etichettaDelMestiere = etichettaDelMestiere;
		this.aiutoDoppioClickSinistra = aiutoDoppioClickSinistra;
		this.aiutoDoppioClickDestra = aiutoDoppioClickDestra;
	}

	/**
	 * La bottega del commerciante per quel negozio: l'armaiolo o il venditore di pergamene
	 *
	 * @throws IllegalArgumentException se il negozio non ha una schermata del commerciante
	 */
	public static TipoBottega del(TipoNegozio negozio) {
		for (TipoBottega bottega : values()) {
			if (bottega.negozio == negozio) {
				return bottega;
			}
		}
		throw new IllegalArgumentException("Nessuna schermata del commerciante per " + negozio);
	}

	/** La chiave del titolo della colonna di sinistra (vedi SchermataDiScambio.titoloDellaCornice, che sceglie la forma corta se serve) */
	public String getChiaveIntestazioneSinistra() {
		return intestazioneSinistra;
	}

	public String getChiaveIntestazioneDestra() {
		return intestazioneDestra;
	}

	public String getIntestazioneSinistra() {
		return TestiDellaUI.get(intestazioneSinistra);
	}

	public String getIntestazioneDestra() {
		return TestiDellaUI.get(intestazioneDestra);
	}

	/**
	 * Chi sta al centro, o null nell'inventario (dove al centro c'è il personaggio)
	 */
	public TipoPersonaggio getMestiere() {
		return mestiere;
	}

	/**
	 * Il mestiere scritto sotto il nome del negoziante (per i commercianti è il nome del negozio), o null nell'inventario
	 */
	public String getEtichettaDelMestiere() {
		if (negozio != null) {
			return negozio.getNome();
		}
		return etichettaDelMestiere == null ? null : TestiDellaUI.get(etichettaDelMestiere);
	}

	/**
	 * Che cosa fa il doppio click su un oggetto della colonna di sinistra (null se non ha un significato)
	 */
	public String getAiutoDoppioClickSinistra() {
		return aiutoDoppioClickSinistra == null ? null : TestiDellaUI.get(aiutoDoppioClickSinistra);
	}

	/**
	 * Che cosa fa il doppio click su un oggetto della colonna di destra (null se non ha un significato)
	 */
	public String getAiutoDoppioClickDestra() {
		return aiutoDoppioClickDestra == null ? null : TestiDellaUI.get(aiutoDoppioClickDestra);
	}
}
