package com.threeamigos.foresta.tipi.tattiche;

import com.threeamigos.foresta.tipi.ClasseIncantesimo;
import com.threeamigos.foresta.tipi.TipoDanno;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Le azioni che una voce di tattica può compiere (tattiche_di_combattimento_automatiche.md §11.9): le sole già disponibili nella barra dei
 * comandi in combattimento. Corruzione, amicizia, fuga e passare inosservati non ci sono. Ogni azione dichiara su che cosa agisce
 * ({@link PortataAzione}) e da quali rose può pescare il bersaglio; i suoi requisiti (la magia, la pergamena...) li verifica chi valuta la tattica.
 */
public enum TipoAzione {

	ATTACCO_FISICO("Attacco fisico", PortataAzione.AVVERSARIO, null),
	ARIA("Aria", PortataAzione.AVVERSARIO, ClasseIncantesimo.ARIA),
	ACQUA("Acqua", PortataAzione.AVVERSARIO, ClasseIncantesimo.ACQUA),
	TERRA("Terra", PortataAzione.AVVERSARIO, ClasseIncantesimo.TERRA),
	FUOCO("Fuoco", PortataAzione.AVVERSARIO, ClasseIncantesimo.FUOCO),
	FULMINE("Fulmine", PortataAzione.AVVERSARIO, ClasseIncantesimo.FULMINE),
	GELO("Gelo", PortataAzione.AVVERSARIO, ClasseIncantesimo.GELO),
	VELENO("Veleno", PortataAzione.AVVERSARIO, ClasseIncantesimo.VELENO),
	MORTE("Morte", PortataAzione.AVVERSARIO, ClasseIncantesimo.MORTE),
	SILENZIO("Silenzio", PortataAzione.AVVERSARIO, ClasseIncantesimo.SILENZIO),
	LUCE_DIVINA("Luce Divina", PortataAzione.AVVERSARIO, ClasseIncantesimo.LUCE_DIVINA),
	/** Uno dei sette incantesimi elementali, scelto con un criterio (non la Morte, non il Silenzio) */
	INCANTESIMO_DI_DANNO("Incantesimo di danno", PortataAzione.AVVERSARIO, null),
	/**
	 * Uno qualsiasi degli incantesimi malefici (anche la Morte e il Silenzio) che il personaggio può lanciare, scelto con un criterio: la replica di come
	 * scelgono i mostri oggi (RegistroTattiche); un giocatore usa piuttosto le azioni dei singoli incantesimi o INCANTESIMO_DI_DANNO
	 */
	INCANTESIMO_DEL_REPERTORIO("Incantesimo del repertorio", PortataAzione.AVVERSARIO, null),
	DARDO_ARCANO("Dardo Arcano", PortataAzione.AVVERSARIO, null),
	RESURREZIONE("Resurrezione", PortataAzione.ALLEATO_CADUTO, ClasseIncantesimo.RESURREZIONE),
	ALBA_SACRA("Alba Sacra", PortataAzione.GRUPPO_ALLEATI, ClasseIncantesimo.ALBA_SACRA),
	CANTO_CURATIVO("Canto Curativo", PortataAzione.ALLEATO, null),
	CANTO_IPNOTICO("Canto Ipnotico", PortataAzione.GRUPPO_AVVERSARI, null),
	URLO_DELLA_BANSHEE("Urlo della Banshee", PortataAzione.GRUPPO_AVVERSARI, null),
	CANTO_DI_VALORE("Canto di Valore", PortataAzione.GRUPPO_ALLEATI, null),
	POZIONE_SALUTE("Pozione della Salute", PortataAzione.ALLEATO, null),
	POZIONE_MAGIA("Pozione della Magia", PortataAzione.ALLEATO, null),
	POZIONE_SALUTE_GRANDE("Pozione della Salute grande", PortataAzione.ALLEATO, null),
	POZIONE_MAGIA_GRANDE("Pozione della Magia grande", PortataAzione.ALLEATO, null),
	CAMBIA_ARMA("Cambia Arma", PortataAzione.ARMA, null);

	private final String descrizione;
	private final PortataAzione portata;
	private final ClasseIncantesimo classeIncantesimo;

	TipoAzione(String descrizione, PortataAzione portata, ClasseIncantesimo classeIncantesimo) {
		this.descrizione = descrizione;
		this.portata = portata;
		this.classeIncantesimo = classeIncantesimo;
	}

	/**
	 * L'azione che lancia quell'incantesimo, o null se l'incantesimo non ne ha una
	 */
	public static TipoAzione di(ClasseIncantesimo classe) {
		for (TipoAzione azione : values()) {
			if (azione.classeIncantesimo == classe) {
				return azione;
			}
		}
		return null;
	}

	public String getDescrizione() {
		return descrizione;
	}

	public PortataAzione getPortata() {
		return portata;
	}

	/**
	 * L'incantesimo che questa azione lancia, o null se non è un incantesimo di una classe precisa
	 */
	public ClasseIncantesimo getClasseIncantesimo() {
		return classeIncantesimo;
	}

	/**
	 * Il tipo di danno dell'azione, o null se non ne ha uno fisso (l'attacco dipende dall'arma, le cure non danneggiano, l'incantesimo di
	 * danno dipende da quello scelto)
	 */
	public TipoDanno getTipoDanno() {
		if (this == DARDO_ARCANO) {
			return TipoDanno.ARCANO;
		}
		return classeIncantesimo == null ? null : classeIncantesimo.getTipoDanno();
	}

	/**
	 * Le rose da cui il selettore di una voce con questa azione può pescare, per i personaggi del giocatore o per i mostri. Un attacco dei miei non
	 * colpisce gli ospiti; per un mostro sì (sono fra i suoi bersagli). Le cure dei mostri non riguardano gli ospiti.
	 */
	public Set<TipoRosa> roseAmmesse(boolean attoreDelGiocatore) {
		switch (portata) {
			case AVVERSARIO:
				return attoreDelGiocatore ? EnumSet.of(TipoRosa.AVVERSARI)
						: EnumSet.of(TipoRosa.AVVERSARI, TipoRosa.OSPITI, TipoRosa.AVVERSARI_E_OSPITI);
			case ALLEATO:
				return attoreDelGiocatore ? EnumSet.of(TipoRosa.SE_STESSO, TipoRosa.ALLEATI, TipoRosa.OSPITI, TipoRosa.ALLEATI_E_OSPITI)
						: EnumSet.of(TipoRosa.SE_STESSO, TipoRosa.ALLEATI);
			case ALLEATO_CADUTO:
				return EnumSet.of(TipoRosa.ALLEATI_CADUTI);
			case GRUPPO_ALLEATI:
				return EnumSet.of(TipoRosa.ALLEATI);
			case GRUPPO_AVVERSARI:
				return EnumSet.of(TipoRosa.AVVERSARI);
			case ARMA:
				return EnumSet.of(TipoRosa.SE_STESSO);
			default:
				return Collections.emptySet();
		}
	}
}
