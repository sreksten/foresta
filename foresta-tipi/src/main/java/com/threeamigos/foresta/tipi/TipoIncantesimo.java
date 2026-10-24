package com.threeamigos.foresta.tipi;

import com.threeamigos.foresta.danni.tipi.TipoDanno;
import com.threeamigos.foresta.testi.Testi;

/**
 * Gli incantesimi: un tipo di arma che il giocatore ha a disposizione per combattere gli avversari.
 * <p>Ogni incantesimo ha un nome (coincide con il nome dell'enumerato), una {@link CategoriaIncantesimo} (se è un
 * incantesimo che fa danno al gruppo avversario o apporta beneficio al gruppo del giocatore),
 * una {@link PortataIncantesimo}, un {@link TipoDanno}. Ha anche un {@link Comando} associato che serve per passare
 * la richiesta di lancio dalla UI al motore.
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

	ARIA(CategoriaIncantesimo.MALEFICO, PortataIncantesimo.MULTIPLO, TipoDanno.ARIA, Comando.ARIA),
	ACQUA(CategoriaIncantesimo.MALEFICO, PortataIncantesimo.MULTIPLO, TipoDanno.ACQUA, Comando.ACQUA),
	TERRA(CategoriaIncantesimo.MALEFICO, PortataIncantesimo.MULTIPLO, TipoDanno.TERRA, Comando.TERRA),
	FUOCO(CategoriaIncantesimo.MALEFICO, PortataIncantesimo.MULTIPLO, TipoDanno.FUOCO, Comando.FUOCO),
	FULMINE(CategoriaIncantesimo.MALEFICO, PortataIncantesimo.MULTIPLO, TipoDanno.FULMINE, Comando.FULMINE),
	GELO(CategoriaIncantesimo.MALEFICO, PortataIncantesimo.MULTIPLO, TipoDanno.GELO, Comando.GELO),
	VELENO(CategoriaIncantesimo.MALEFICO, PortataIncantesimo.MULTIPLO, TipoDanno.VELENO, Comando.VELENO),
	MORTE(CategoriaIncantesimo.MALEFICO, PortataIncantesimo.SINGOLO_SOLO_VIVI, TipoDanno.NECROTICO, Comando.MORTE),
	RESURREZIONE(CategoriaIncantesimo.BENEFICO, PortataIncantesimo.SINGOLO_QUALSIASI, null, Comando.RESURREZIONE),
	ALBA_SACRA(CategoriaIncantesimo.BENEFICO, PortataIncantesimo.GRUPPO, null, Comando.ALBA_SACRA),
	SILENZIO(CategoriaIncantesimo.MALEFICO, PortataIncantesimo.GRUPPO, TipoDanno.ARCANO, Comando.SILENZIO),
	LUCE_DIVINA(CategoriaIncantesimo.MALEFICO, PortataIncantesimo.MULTIPLO, TipoDanno.SACRO, Comando.LUCE_DIVINA);

	/**
	 * L'incantesimo che il comando attiva.
	 *
	 * @throws IllegalArgumentException se il comando non attiva nessun incantesimo
	 */
	public static TipoIncantesimo ofComando(Comando comando) {
		for (TipoIncantesimo corrente : values()) {
			if (comando == corrente.comandoDiAttivazione) {
				return corrente;
			}
		}
		throw new IllegalArgumentException("Comando invalido per incantesimo: " + comando);
	}

	private final CategoriaIncantesimo categoriaIncantesimo;
	private final PortataIncantesimo portata;
	private final TipoDanno tipoDanno;
	private final Comando comandoDiAttivazione;

	TipoIncantesimo(CategoriaIncantesimo categoriaIncantesimo, PortataIncantesimo portata, TipoDanno tipoDanno, Comando comando) {
		this.categoriaIncantesimo = categoriaIncantesimo;
		this.portata = portata;
		this.tipoDanno = tipoDanno;
		this.comandoDiAttivazione = comando;
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
	 * Il comando di attivazione di questo incantesimo
	 */
	public Comando getComandoDiAttivazione() {
		return comandoDiAttivazione;
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
		switch (this) {
			case ARIA:
				return CostantiIncantesimi.INCANTESIMO_ARIA_COSTO_LANCIO;
			case ACQUA:
				return CostantiIncantesimi.INCANTESIMO_ACQUA_COSTO_LANCIO;
			case TERRA:
				return CostantiIncantesimi.INCANTESIMO_TERRA_COSTO_LANCIO;
			case FUOCO:
				return CostantiIncantesimi.INCANTESIMO_FUOCO_COSTO_LANCIO;
			case FULMINE:
				return CostantiIncantesimi.INCANTESIMO_FULMINE_COSTO_LANCIO;
			case GELO:
				return CostantiIncantesimi.INCANTESIMO_GELO_COSTO_LANCIO;
			case VELENO:
				return CostantiIncantesimi.INCANTESIMO_VELENO_COSTO_LANCIO;
			case MORTE:
				return CostantiIncantesimi.INCANTESIMO_MORTE_COSTO_LANCIO;
			case RESURREZIONE:
				return CostantiIncantesimi.INCANTESIMO_RESURREZIONE_COSTO_LANCIO;
			case ALBA_SACRA:
				return CostantiIncantesimi.INCANTESIMO_ALBA_SACRA_COSTO_LANCIO;
			case SILENZIO:
				return CostantiIncantesimi.INCANTESIMO_SILENZIO_COSTO_LANCIO;
			case LUCE_DIVINA:
				return CostantiIncantesimi.INCANTESIMO_LUCE_DIVINA_COSTO_LANCIO;
			default:
				throw new IllegalStateException("Costo non previsto per " + this);
		}
	}

	/**
	 * Le monete che costa una pergamena.
	 */
	public int getCostoAcquisto() {
		switch (this) {
			case ARIA:
				return CostantiIncantesimi.INCANTESIMO_ARIA_COSTO_ACQUISTO;
			case ACQUA:
				return CostantiIncantesimi.INCANTESIMO_ACQUA_COSTO_ACQUISTO;
			case TERRA:
				return CostantiIncantesimi.INCANTESIMO_TERRA_COSTO_ACQUISTO;
			case FUOCO:
				return CostantiIncantesimi.INCANTESIMO_FUOCO_COSTO_ACQUISTO;
			case FULMINE:
				return CostantiIncantesimi.INCANTESIMO_FULMINE_COSTO_ACQUISTO;
			case GELO:
				return CostantiIncantesimi.INCANTESIMO_GELO_COSTO_ACQUISTO;
			case VELENO:
				return CostantiIncantesimi.INCANTESIMO_VELENO_COSTO_ACQUISTO;
			case MORTE:
				return CostantiIncantesimi.INCANTESIMO_MORTE_COSTO_ACQUISTO;
			case RESURREZIONE:
				return CostantiIncantesimi.INCANTESIMO_RESURREZIONE_COSTO_ACQUISTO;
			case ALBA_SACRA:
				return CostantiIncantesimi.INCANTESIMO_ALBA_SACRA_COSTO_ACQUISTO;
			case SILENZIO:
				return CostantiIncantesimi.INCANTESIMO_SILENZIO_COSTO_ACQUISTO;
			case LUCE_DIVINA:
				return CostantiIncantesimi.INCANTESIMO_LUCE_DIVINA_COSTO_ACQUISTO;
			default:
				throw new IllegalStateException("Costo non previsto per " + this);
		}
	}
}
