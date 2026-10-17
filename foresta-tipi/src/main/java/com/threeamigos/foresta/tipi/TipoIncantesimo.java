package com.threeamigos.foresta.tipi;

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
 * Le istanze di incantesimo assieme al costo di acquisto e di lancio li dà incantesimi.FabbricaIncantesimi.
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

	private final CategoriaIncantesimo tipo;
	private final PortataIncantesimo portata;
	private final TipoDanno tipoDanno;
	private final Comando comandoDiAttivazione;

	TipoIncantesimo(CategoriaIncantesimo tipo, PortataIncantesimo portata, TipoDanno tipoDanno, Comando comando) {
		this.tipo = tipo;
		this.portata = portata;
		this.tipoDanno = tipoDanno;
		this.comandoDiAttivazione = comando;
	}

	/**
	 * Il tipo di questo incantesimo: BENEFICO, MALEFICO
	 */
	public CategoriaIncantesimo getTipo() {
		return tipo;
	}

	/**
	 * Il tipo di danno causato da questo incantesimo: sottoclassi di FISICO, MAGICO, ELEMENTALE
	 */
	public TipoDanno getTipoDanno() {
		return tipoDanno;
	}

	/**
	 * La portata di questo incantesimo; GLOBALE, GRUPPO, SINGOLO_SOLO_VIVI, SINGOLO_QUALSIASI
	 */
	public PortataIncantesimo getPortata() {
		return portata;
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

	public Comando getComandoDiAttivazione() {
		return comandoDiAttivazione;
	}

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
}
