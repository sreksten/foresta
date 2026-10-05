package com.threeamigos.foresta.tipi;

/**
 * Gli incantesimi: l'identificativo che salva il modello dati (le pergamene del gruppo in GruppoGiocatoreMD) con i
 * dati che li descrivono. L'incantesimo vero, il suo costo e la scelta a caso li dà incantesimi.FabbricaIncantesimi.
 * (Da non confondere con TipoIncantesimo, benefico o malefico.)
 */
public enum ClasseIncantesimo {

	ARIA(TipoIncantesimo.MALEFICO, PortataIncantesimo.MULTIPLO, TipoDanno.ARIA,
			"Aria", "incantesimo dell'Aria", "incantesimi dell'Aria",
			Comando.ARIA,
			"Scatena una folata sgangherata che destabilizza l'avversario e lo scaraventa a terra con un sonoro tonfo."),
	ACQUA(TipoIncantesimo.MALEFICO, PortataIncantesimo.MULTIPLO, TipoDanno.ACQUA,
			"Acqua", "incantesimo dell'Acqua", "incantesimi dell'Acqua",
			Comando.ACQUA,
			"Inonda il bersaglio con un getto corrosivo che spegne i suoi bollori e lo lascia inzuppato da capo a piedi."),
	TERRA(TipoIncantesimo.MALEFICO, PortataIncantesimo.MULTIPLO, TipoDanno.TERRA,
			"Terra", "incantesimo di Terra", "incantesimi di Terra",
			Comando.TERRA,
			"Scaglia una pioggia di fango e macigni ideale per bloccare le gambe del nemico e accecarlo di sabbia."),
	FUOCO(TipoIncantesimo.MALEFICO, PortataIncantesimo.MULTIPLO, TipoDanno.FUOCO,
			"Fuoco", "incantesimo del Fuoco", "incantesimi del Fuoco",
			Comando.FUOCO,
			"Avvolge l'avversario in un incendio molesto che brucia i suoi vestiti e infligge un fastidiosissimo danno nel tempo."),
	FULMINE(TipoIncantesimo.MALEFICO, PortataIncantesimo.MULTIPLO, TipoDanno.FULMINE,
			"Fulmine", "incantesimo del Fulmine", "incantesimi del Fulmine",
			Comando.FULMINE,
			"Saetta un arco energetico rapido che si propaga tra i nemici e li stordisce con una scossa ad alto voltaggio."),
	GELO(TipoIncantesimo.MALEFICO, PortataIncantesimo.MULTIPLO, TipoDanno.GELO,
			"Gelo", "incantesimo del Gelo", "incantesimi del Gelo",
			Comando.GELO,
			"Congela i fluidi corporei del bersaglio riducendone i riflessi o bloccandolo in un gelido cubetto ornamentale."),
	VELENO(TipoIncantesimo.MALEFICO, PortataIncantesimo.MULTIPLO, TipoDanno.VELENO,
			"Veleno", "incantesimo del Veleno", "incantesimi del Veleno",
			Comando.VELENO,
			"Inietta una tossina subdola che debilita le statistiche della vittima consumando la sua salute scatto dopo scatto."),
	MORTE(TipoIncantesimo.MALEFICO, PortataIncantesimo.SINGOLO_SOLO_VIVI, TipoDanno.NECROTICO,
			"Morte", "incantesimo di Morte", "incantesimi di Morte",
			Comando.MORTE,
			"Canalizza l'energia necrotica per prosciugare la forza vitale nemica, impedendo qualsiasi disperato tentativo di cura."),
	RESURREZIONE(TipoIncantesimo.BENEFICO, PortataIncantesimo.SINGOLO_QUALSIASI, null,
			"Resurr.", "incantesimo della Resurrezione", "incantesimi della Resurrezione",
			Comando.RESURREZIONE,
			"Inverte il flusso del fato infliggendo un devastante rigetto rigenerativo alle creature d'ombra e ai non-morti."),
	ALBA_SACRA(TipoIncantesimo.BENEFICO, PortataIncantesimo.GRUPPO, null,
			"Alba", "incantesimo dell'Alba Sacra", "incantesimi dell'Alba Sacra",
			Comando.ALBA_SACRA,
			"Rimuove tutti gli effetti di stato al gruppo.");

	private final TipoIncantesimo tipo;
	private final PortataIncantesimo portata;
	private final TipoDanno tipoDanno;
	private final String nomeAbbreviato;
	private final String nomeSingolare;
	private final String nomePlurale;
	private final Comando comandoDiAttivazione;
	private final String effetto;

	ClasseIncantesimo(TipoIncantesimo tipo, PortataIncantesimo portata, TipoDanno tipoDanno,
					  String nomeAbbreviato, String nomeSingolare, String nomePlurale,
					  Comando comando, String effetto) {
		this.tipo = tipo;
		this.portata = portata;
		this.tipoDanno = tipoDanno;
		this.nomeAbbreviato = nomeAbbreviato;
		this.nomeSingolare = nomeSingolare;
		this.nomePlurale = nomePlurale;
		this.comandoDiAttivazione = comando;
		this.effetto = effetto;
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
	 * Il nome abbreviato di un incantesimo (es. Aria)
	 */
	public String getNomeAbbreviato() {
		return nomeAbbreviato;
	}

	/**
	 * Il nome completo di un incantesimo al singolare (es. Incantesimo dell'Aria)
	 */
	public String getNomeSingolare() {
		return nomeSingolare;
	}

	/**
	 * Il nome completo di un incantesimo al plurale (es. Incantesimi dell'Aria)
	 */
	public String getNomePlurale() {
		return nomePlurale;
	}

	/**
	 * Quale effetto produce questo incantesimo
	 */
	public String getEffetto() {
		return effetto;
	}

	public Comando getComandoDiAttivazione() {
		return comandoDiAttivazione;
	}

	public static ClasseIncantesimo ofComando(Comando comando) {
		for (ClasseIncantesimo corrente : values()) {
			if (comando == corrente.comandoDiAttivazione) {
				return corrente;
			}
		}
		throw new IllegalArgumentException("Comando invalido per incantesimo: " + comando);
	}
}
