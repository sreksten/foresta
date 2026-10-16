package com.threeamigos.foresta.tipi;

/**
 * Gli incantesimi: l'identificativo che salva il modello dati (le pergamene del gruppo in GruppoGiocatoreMD) con i
 * dati che li descrivono. L'incantesimo vero, il suo costo e la scelta a caso li dà incantesimi.FabbricaIncantesimi.
 * (Da non confondere con TipoIncantesimo, benefico o malefico.)
 * <p>
 * I testi di ogni costante (NOME, NOME_SINGOLARE, NOME_PLURALE, EFFETTO) non stanno qui ma nel file {@code classe_incantesimo_<lingua>.properties}, alla voce
 * {@code COSTANTE.VOCE}, in modo che il gioco si possa tradurre (vedi {@link Testi}).
 */
public enum ClasseIncantesimo {

	ARIA(TipoIncantesimo.MALEFICO, PortataIncantesimo.MULTIPLO, TipoDanno.ARIA, Comando.ARIA),
	ACQUA(TipoIncantesimo.MALEFICO, PortataIncantesimo.MULTIPLO, TipoDanno.ACQUA, Comando.ACQUA),
	TERRA(TipoIncantesimo.MALEFICO, PortataIncantesimo.MULTIPLO, TipoDanno.TERRA, Comando.TERRA),
	FUOCO(TipoIncantesimo.MALEFICO, PortataIncantesimo.MULTIPLO, TipoDanno.FUOCO, Comando.FUOCO),
	FULMINE(TipoIncantesimo.MALEFICO, PortataIncantesimo.MULTIPLO, TipoDanno.FULMINE, Comando.FULMINE),
	GELO(TipoIncantesimo.MALEFICO, PortataIncantesimo.MULTIPLO, TipoDanno.GELO, Comando.GELO),
	VELENO(TipoIncantesimo.MALEFICO, PortataIncantesimo.MULTIPLO, TipoDanno.VELENO, Comando.VELENO),
	MORTE(TipoIncantesimo.MALEFICO, PortataIncantesimo.SINGOLO_SOLO_VIVI, TipoDanno.NECROTICO, Comando.MORTE),
	RESURREZIONE(TipoIncantesimo.BENEFICO, PortataIncantesimo.SINGOLO_QUALSIASI, null, Comando.RESURREZIONE),
	ALBA_SACRA(TipoIncantesimo.BENEFICO, PortataIncantesimo.GRUPPO, null, Comando.ALBA_SACRA),
	SILENZIO(TipoIncantesimo.MALEFICO, PortataIncantesimo.GRUPPO, TipoDanno.ARCANO, Comando.SILENZIO),
	LUCE_DIVINA(TipoIncantesimo.MALEFICO, PortataIncantesimo.MULTIPLO, TipoDanno.SACRO, Comando.LUCE_DIVINA);

	private final TipoIncantesimo tipo;
	private final PortataIncantesimo portata;
	private final TipoDanno tipoDanno;
	private final Comando comandoDiAttivazione;

	ClasseIncantesimo(TipoIncantesimo tipo, PortataIncantesimo portata, TipoDanno tipoDanno, Comando comando) {
		this.tipo = tipo;
		this.portata = portata;
		this.tipoDanno = tipoDanno;
		this.comandoDiAttivazione = comando;
	}

	/**
	 * Il tipo di questo incantesimo: BENEFICO, MALEFICO
	 */
	public TipoIncantesimo getTipo() {
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
	 * Il nome completo di un incantesimo al singolare (es. Incantesimo dell'Aria)
	 */
	public String getNomeSingolare() {
		return Testi.testoOCostante(this, "NOME_SINGOLARE");
	}

	/**
	 * Il nome completo di un incantesimo al plurale (es. Incantesimi dell'Aria)
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
	public static ClasseIncantesimo ofComando(Comando comando) {
		for (ClasseIncantesimo corrente : values()) {
			if (comando == corrente.comandoDiAttivazione) {
				return corrente;
			}
		}
		throw new IllegalArgumentException("Comando invalido per incantesimo: " + comando);
	}
}
