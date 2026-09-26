package com.threeamigos.foresta.incantesimi;

import com.threeamigos.foresta.motore.Comando;
import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.motore.Dado;
import com.threeamigos.foresta.motore.modellodati.TipoDanno;
import com.threeamigos.foresta.personaggi.Personaggio;

import java.util.function.Function;

public enum ClasseIncantesimo {

	ARIA(Aria::new, TipoIncantesimo.MALEFICO, PortataIncantesimo.MULTIPLO, TipoDanno.ARIA,
			"Aria", "incantesimo dell'Aria", "incantesimi dell'Aria",
			Comando.ARIA, Costanti.INCANTESIMO_ARIA_COSTO_ACQUISTO, Costanti.INCANTESIMO_ARIA_COSTO_LANCIO,
			"Scatena una folata sgangherata che destabilizza l'avversario e lo scaraventa a terra con un sonoro tonfo."),
	ACQUA(Acqua::new, TipoIncantesimo.MALEFICO, PortataIncantesimo.MULTIPLO, TipoDanno.ACQUA,
			"Acqua", "incantesimo dell'Acqua", "incantesimi dell'Acqua",
			Comando.ACQUA, Costanti.INCANTESIMO_ACQUA_COSTO_ACQUISTO, Costanti.INCANTESIMO_ACQUA_COSTO_LANCIO,
			"Inonda il bersaglio con un getto corrosivo che spegne i suoi bollori e lo lascia inzuppato da capo a piedi."),
	TERRA(Terra::new, TipoIncantesimo.MALEFICO, PortataIncantesimo.MULTIPLO, TipoDanno.TERRA,
			"Terra", "incantesimo di Terra", "incantesimi di Terra",
			Comando.TERRA, Costanti.INCANTESIMO_TERRA_COSTO_ACQUISTO, Costanti.INCANTESIMO_TERRA_COSTO_LANCIO,
			"Scaglia una pioggia di fango e macigni ideale per bloccare le gambe del nemico e accecarlo di sabbia."),
	FUOCO(Fuoco::new, TipoIncantesimo.MALEFICO, PortataIncantesimo.MULTIPLO, TipoDanno.FUOCO,
			"Fuoco", "incantesimo del Fuoco", "incantesimi del Fuoco",
			Comando.FUOCO, Costanti.INCANTESIMO_FUOCO_COSTO_ACQUISTO, Costanti.INCANTESIMO_FUOCO_COSTO_LANCIO,
			"Avvolge l'avversario in un incendio molesto che brucia i suoi vestiti e infligge un fastidiosissimo danno nel tempo."),
	FULMINE(Fulmine::new, TipoIncantesimo.MALEFICO, PortataIncantesimo.MULTIPLO, TipoDanno.FULMINE,
			"Fulmine", "incantesimo del Fulmine", "incantesimi del Fulmine",
			Comando.FULMINE, Costanti.INCANTESIMO_FULMINE_COSTO_ACQUISTO, Costanti.INCANTESIMO_FULMINE_COSTO_LANCIO,
			"Saetta un arco energetico rapido che si propaga tra i nemici e li stordisce con una scossa ad alto voltaggio."),
	GELO(Gelo::new, TipoIncantesimo.MALEFICO, PortataIncantesimo.MULTIPLO, TipoDanno.GELO,
			"Gelo", "incantesimo del Gelo", "incantesimi del Gelo",
			Comando.GELO, Costanti.INCANTESIMO_GELO_COSTO_ACQUISTO, Costanti.INCANTESIMO_GELO_COSTO_LANCIO,
			"Congela i fluidi corporei del bersaglio riducendone i riflessi o bloccandolo in un gelido cubetto ornamentale."),
	VELENO(Veleno::new, TipoIncantesimo.MALEFICO, PortataIncantesimo.MULTIPLO, TipoDanno.VELENO,
			"Veleno", "incantesimo del Veleno", "incantesimi del Veleno",
			Comando.VELENO, Costanti.INCANTESIMO_VELENO_COSTO_ACQUISTO, Costanti.INCANTESIMO_VELENO_COSTO_LANCIO,
			"Inietta una tossina subdola che debilita le statistiche della vittima consumando la sua salute scatto dopo scatto."),
	MORTE(Morte::new, TipoIncantesimo.MALEFICO, PortataIncantesimo.SINGOLO_SOLO_VIVI, TipoDanno.NECROTICO,
			"Morte", "incantesimo di Morte", "incantesimi di Morte",
			Comando.MORTE, Costanti.INCANTESIMO_MORTE_COSTO_ACQUISTO, Costanti.INCANTESIMO_MORTE_COSTO_LANCIO,
			"Canalizza l'energia necrotica per prosciugare la forza vitale nemica, impedendo qualsiasi disperato tentativo di cura."),
	RESURREZIONE(Resurrezione::new, TipoIncantesimo.BENEFICO, PortataIncantesimo.SINGOLO_QUALSIASI, null,
			"Resurr.", "incantesimo della Resurrezione", "incantesimi della Resurrezione",
			Comando.RESURREZIONE, Costanti.INCANTESIMO_RESURREZIONE_COSTO_ACQUISTO, Costanti.INCANTESIMO_RESURREZIONE_COSTO_LANCIO,
			"Inverte il flusso del fato infliggendo un devastante rigetto rigenerativo alle creature d'ombra e ai non-morti.");

	private final Function<Integer, Incantesimo> supplier;
	private final TipoIncantesimo tipo;
	private final PortataIncantesimo portata;
	private final TipoDanno tipoDanno;
	private final String nomeAbbreviato;
	private final String nomeSingolare;
	private final String nomePlurale;
	private final int costoAcquisto;
	private final int costoLancio;
	private final Comando comandoDiAttivazione;
	private final String effetto;

	ClasseIncantesimo(Function<Integer, Incantesimo> supplier, TipoIncantesimo tipo, PortataIncantesimo portata,
					  TipoDanno tipoDanno,
					  String nomeAbbreviato, String nomeSingolare, String nomePlurale,
					  Comando comando, int costoAcquisto, int costoLancio, String effetto) {
		this.supplier = supplier;
		this.tipo = tipo;
		this.portata = portata;
		this.tipoDanno = tipoDanno;
		this.nomeAbbreviato = nomeAbbreviato;
		this.nomeSingolare = nomeSingolare;
		this.nomePlurale = nomePlurale;
		this.costoAcquisto = costoAcquisto;
		this.costoLancio = costoLancio;
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
	 * Quante monete costa acquistarlo
	 */
	public int getCostoAcquisto() {
		return costoAcquisto;
	}

	/**
	 * Quale effetto produce questo incantesimo
	 */
	public String getEffetto() {
		return effetto;
	}

	/**
	 * Quanto costa lanciarlo in termini di magia
	 */
	// FIXME o qui o in istanza...
	public int getCostoLancio() {
		return costoLancio;
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

	public Incantesimo getIstanza(int livello) {
		return supplier.apply(livello);
	}

	public static Incantesimo ofComando(Comando comando, Personaggio formulante) {
		for (ClasseIncantesimo corrente : values()) {
			if (comando == corrente.comandoDiAttivazione) {
				return corrente.supplier.apply(formulante.getLivello());
			}
		}
		throw new IllegalArgumentException("Comando invalido per incantesimo: " + comando);
	}

	public static ClasseIncantesimo casuale() {
		int ordinale = Dado.tira(ClasseIncantesimo.values().length) - 1;
		for (ClasseIncantesimo corrente : ClasseIncantesimo.values()) {
			if (corrente.ordinal() == ordinale) {
				return corrente;
			}
		}
		throw new IllegalStateException("Errore nella generazione casuale di un incantesimo");
	}
}
