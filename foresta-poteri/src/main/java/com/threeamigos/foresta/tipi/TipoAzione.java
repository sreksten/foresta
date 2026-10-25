package com.threeamigos.foresta.tipi;

import com.threeamigos.foresta.testi.Testi;

import java.util.EnumMap;
import java.util.Map;

/**
 * Le azioni che una voce di tattica può compiere (tattiche_di_combattimento_automatiche.md §11.9): le sole già disponibili nella barra dei
 * comandi in combattimento. Corruzione, amicizia, fuga e passare inosservati non ci sono. Su che cosa agisce ciascuna e da quali rose può
 * pescare il bersaglio lo dicono le tattiche (PortataAzione); i suoi requisiti (la magia, la pergamena...) li verifica chi valuta la tattica.
 * <p>
 * I testi di ogni costante (DESCRIZIONE) non stanno qui ma nel file {@code tipo_azione_<lingua>.properties}, alla voce
 * {@code COSTANTE.VOCE}, in modo che il gioco si possa tradurre (vedi {@link Testi}).
 */
public enum TipoAzione {

	/** L'attacco con l'arma impugnata */
	ATTACCO_FISICO,
	ARIA(TipoIncantesimo.ARIA),
	ACQUA(TipoIncantesimo.ACQUA),
	TERRA(TipoIncantesimo.TERRA),
	FUOCO(TipoIncantesimo.FUOCO),
	FULMINE(TipoIncantesimo.FULMINE),
	GELO(TipoIncantesimo.GELO),
	VELENO(TipoIncantesimo.VELENO),
	MORTE(TipoIncantesimo.MORTE),
	SILENZIO(TipoIncantesimo.SILENZIO),
	/** L'abilità del Sacerdote: danno sacro a più avversari, il doppio ai non morti (vedi TipoAbilita) */
	LUCE_DIVINA,
	/** Uno dei sette incantesimi elementali, scelto con un criterio (non la Morte, non il Silenzio) */
	INCANTESIMO_DI_DANNO,
	/**
	 * Uno qualsiasi degli incantesimi malefici (anche la Morte e il Silenzio) che il personaggio può lanciare, scelto con un criterio: la replica di come
	 * scelgono i mostri oggi (RegistroTattiche); un giocatore usa piuttosto le azioni dei singoli incantesimi o INCANTESIMO_DI_DANNO
	 */
	INCANTESIMO_DEL_REPERTORIO,
	/** L'attacco magico di base, con danno arcano */
	DARDO_ARCANO,
	RESURREZIONE(TipoIncantesimo.RESURREZIONE),
	/** L'abilità del Sacerdote che toglie al gruppo gli stati negativi (vedi TipoAbilita) */
	ALBA_SACRA,
	/** Il canto del Bardo che cura un alleato (vedi bardo.md) */
	CANTO_CURATIVO,
	/** Il canto del Bardo che ipnotizza il gruppo avversario (vedi bardo.md) */
	CANTO_IPNOTICO,
	/** L'urlo della Banshee, che colpisce il gruppo avversario: il gruppo lo impara dal bardo della locanda */
	URLO_DELLA_BANSHEE,
	/** Il canto del Bardo che ispira il gruppo alleato (stato ISPIRATO, vedi bardo.md) */
	CANTO_DI_VALORE,
	/** L'abilità del Chierico che spaventa i non morti avversari (vedi TipoAbilita) */
	SCACCIARE_I_NON_MORTI,
	/** L'abilità del Chierico che cura un alleato */
	IMPOSIZIONE_DELLE_MANI,
	/** L'abilità del Druido: danno di Terra a più avversari */
	RADICI,
	/** L'abilità del Druido: danno di Veleno a più avversari */
	SCIAME,
	/** L'abilità del Druido che rafforza la difesa di tutto il gruppo alleato */
	PELLE_DI_CORTECCIA,
	/** L'abilità del Druido che fa guarire un alleato un poco a ogni turno */
	RIGENERAZIONE,
	/** Dà a un alleato una pozione della salute */
	POZIONE_SALUTE,
	/** Dà a un alleato una pozione della magia */
	POZIONE_MAGIA,
	/** Dà a un alleato una pozione della salute grande */
	POZIONE_SALUTE_GRANDE,
	/** Dà a un alleato una pozione della magia grande */
	POZIONE_MAGIA_GRANDE,
	/** Cambia l'arma impugnata con quella del tipo di danno indicato dall'azione */
	CAMBIA_ARMA;

	private final TipoIncantesimo tipoIncantesimo;

	TipoAzione() {
		this(null);
	}

	TipoAzione(TipoIncantesimo tipoIncantesimo) {
		this.tipoIncantesimo = tipoIncantesimo;
	}

	// L'azione di ciascun incantesimo: ogni incantesimo ne ha una (lo controlla TipiDelleTatticheTest)
	private static final Map<TipoIncantesimo, TipoAzione> PER_INCANTESIMO = new EnumMap<>(TipoIncantesimo.class);

	static {
		for (TipoAzione azione : values()) {
			if (azione.tipoIncantesimo != null) {
				PER_INCANTESIMO.putIfAbsent(azione.tipoIncantesimo, azione);
			}
		}
	}

	/**
	 * L'azione che lancia quell'incantesimo; null per null (non è un incantesimo: EnumMap.get(null) dà null).
	 */
	public static TipoAzione di(TipoIncantesimo classe) {
		return PER_INCANTESIMO.get(classe);
	}

	/**
	 * L'azione che usa quell'abilità innata: ha lo stesso nome.
	 */
	public static TipoAzione di(TipoAbilita tipo) {
		return valueOf(tipo.name());
	}

	/**
	 * L'azione che intona quel canto: ha il suo nome preceduto da CANTO_, tranne l'Urlo della Banshee.
	 */
	public static TipoAzione di(TipoCanto tipo) {
		return tipo == TipoCanto.URLO_DELLA_BANSHEE ? URLO_DELLA_BANSHEE : valueOf("CANTO_" + tipo.name());
	}

	public String getDescrizione() {
		return Testi.testoOCostante(this, "DESCRIZIONE");
	}

	/**
	 * L'incantesimo che questa azione lancia, o null se non è un incantesimo di una classe precisa
	 */
	public TipoIncantesimo getClasseIncantesimo() {
		return tipoIncantesimo;
	}

	/**
	 * Il tipo di danno dell'azione, o null se non ne ha uno fisso (l'attacco dipende dall'arma, le cure non danneggiano, l'incantesimo di
	 * danno dipende da quello scelto)
	 */
	public TipoDanno getTipoDanno() {
		if (this == DARDO_ARCANO) {
			return TipoDanno.ARCANO;
		}
		if (this == RADICI) {
			return TipoDanno.TERRA;
		}
		if (this == SCIAME) {
			return TipoDanno.VELENO;
		}
		return tipoIncantesimo == null ? null : tipoIncantesimo.getTipoDanno();
	}
}
