package com.threeamigos.foresta.tipi;

import com.threeamigos.foresta.testi.Testi;

/**
 * Gli incantesimi: un tipo di arma che il giocatore ha a disposizione per combattere gli avversari.
 * <p>Ogni incantesimo ha un nome (coincide con il nome dell'enumerato), una {@link CategoriaIncantesimo} (se è un
 * incantesimo che fa danno al gruppo avversario o apporta beneficio al gruppo del giocatore),
 * una {@link PortataIncantesimo}, un {@link TipoDanno}.
 * <p>
 * L'identificativo viene usato per salvare e rileggere il modello dati del gruppo del giocatore assieme alle quantità
 * disponibili per quel tipo di incantesimo.
 * <p>
 * Il costo di acquisto e di lancio lo dà questa enum (i valori sono in {@link CostantiIncantesimi}); le istanze di incantesimo le dà incantesimi.FabbricaIncantesimi.
 * <p>
 * I testi di ogni costante (NOME, NOME_SINGOLARE, NOME_PLURALE, EFFETTO) non stanno qui ma nel file
 * {@code classe_incantesimo_<lingua>.properties}, alla voce {@code COSTANTE.VOCE}, in modo che il gioco si possa
 * tradurre (vedi {@link Testi}).
 *
 * @author Stefano Reksten
 */
public enum TipoIncantesimo {

	ARIA(CategoriaIncantesimo.MALEFICO, PortataIncantesimo.MULTIPLO, TipoDanno.ARIA,
			CostantiIncantesimi.INCANTESIMO_ARIA_COSTO_LANCIO, CostantiIncantesimi.INCANTESIMO_ARIA_COSTO_ACQUISTO),
	ACQUA(CategoriaIncantesimo.MALEFICO, PortataIncantesimo.MULTIPLO, TipoDanno.ACQUA,
			CostantiIncantesimi.INCANTESIMO_ACQUA_COSTO_LANCIO, CostantiIncantesimi.INCANTESIMO_ACQUA_COSTO_ACQUISTO),
	TERRA(CategoriaIncantesimo.MALEFICO, PortataIncantesimo.MULTIPLO, TipoDanno.TERRA,
			CostantiIncantesimi.INCANTESIMO_TERRA_COSTO_LANCIO, CostantiIncantesimi.INCANTESIMO_TERRA_COSTO_ACQUISTO),
	FUOCO(CategoriaIncantesimo.MALEFICO, PortataIncantesimo.MULTIPLO, TipoDanno.FUOCO,
			CostantiIncantesimi.INCANTESIMO_FUOCO_COSTO_LANCIO, CostantiIncantesimi.INCANTESIMO_FUOCO_COSTO_ACQUISTO),
	FULMINE(CategoriaIncantesimo.MALEFICO, PortataIncantesimo.MULTIPLO, TipoDanno.FULMINE,
			CostantiIncantesimi.INCANTESIMO_FULMINE_COSTO_LANCIO, CostantiIncantesimi.INCANTESIMO_FULMINE_COSTO_ACQUISTO),
	GELO(CategoriaIncantesimo.MALEFICO, PortataIncantesimo.MULTIPLO, TipoDanno.GELO,
			CostantiIncantesimi.INCANTESIMO_GELO_COSTO_LANCIO, CostantiIncantesimi.INCANTESIMO_GELO_COSTO_ACQUISTO),
	VELENO(CategoriaIncantesimo.MALEFICO, PortataIncantesimo.MULTIPLO, TipoDanno.VELENO,
			CostantiIncantesimi.INCANTESIMO_VELENO_COSTO_LANCIO, CostantiIncantesimi.INCANTESIMO_VELENO_COSTO_ACQUISTO),
	MORTE(CategoriaIncantesimo.MALEFICO, PortataIncantesimo.SINGOLO_SOLO_VIVI, TipoDanno.NECROTICO,
			CostantiIncantesimi.INCANTESIMO_MORTE_COSTO_LANCIO, CostantiIncantesimi.INCANTESIMO_MORTE_COSTO_ACQUISTO),
	RESURREZIONE(CategoriaIncantesimo.BENEFICO, PortataIncantesimo.SINGOLO_QUALSIASI, null,
			CostantiIncantesimi.INCANTESIMO_RESURREZIONE_COSTO_LANCIO, CostantiIncantesimi.INCANTESIMO_RESURREZIONE_COSTO_ACQUISTO),
	SILENZIO(CategoriaIncantesimo.MALEFICO, PortataIncantesimo.GRUPPO, TipoDanno.ARCANO,
			CostantiIncantesimi.INCANTESIMO_SILENZIO_COSTO_LANCIO, CostantiIncantesimi.INCANTESIMO_SILENZIO_COSTO_ACQUISTO);

	private final CategoriaIncantesimo categoriaIncantesimo;
	private final PortataIncantesimo portata;
	private final TipoDanno tipoDanno;
	private final int costoLancio;
	private final int costoAcquisto;

	TipoIncantesimo(CategoriaIncantesimo categoriaIncantesimo, PortataIncantesimo portata, TipoDanno tipoDanno, int costoLancio, int costoAcquisto) {
		this.categoriaIncantesimo = categoriaIncantesimo;
		this.portata = portata;
		this.tipoDanno = tipoDanno;
		this.costoLancio = costoLancio;
		this.costoAcquisto = costoAcquisto;
	}

	/**
	 * Il tipo di questo incantesimo: BENEFICO, MALEFICO
	 */
	public CategoriaIncantesimo getCategoriaIncantesimo() {
		return categoriaIncantesimo;
	}

	/**
	 * La portata di questo incantesimo; GLOBALE, GRUPPO, SINGOLO_SOLO_VIVI, SINGOLO_QUALSIASI
	 */
	public PortataIncantesimo getPortata() {
		return portata;
	}

	/**
	 * Il tipo di danno causato da questo incantesimo: sottoclassi di FISICO, MAGICO, ELEMENTALE
	 */
	public TipoDanno getTipoDanno() {
		return tipoDanno;
	}

	/**
	 * Il nome breve di un incantesimo, senza "incantesimo di" (es. Aria, Alba Sacra)
	 */
	public String getNome() {
		return Testi.testoOCostante(this, "NOME");
	}

	/**
	 * Il nome completo di un incantesimo al singolare (es. "Incantesimo dell'Aria")
	 */
	public String getNomeSingolare() {
		return Testi.testoOCostante(this, "NOME_SINGOLARE");
	}

	/**
	 * Il nome completo di un incantesimo al plurale (es. "Incantesimi dell'Aria")
	 */
	public String getNomePlurale() {
		return Testi.testoOCostante(this, "NOME_PLURALE");
	}

	/**
	 * Quale effetto produce questo incantesimo
	 */
	public String getEffetto() {
		return Testi.testoOCostante(this, "EFFETTO");
	}

	/**
	 * I punti di MAGIA che costa lanciarlo.
	 */
	public int getCostoLancio() {
		return costoLancio;
	}

	/**
	 * Le monete che costa una pergamena.
	 */
	public int getCostoAcquisto() {
		return costoAcquisto;
	}
}
