package com.threeamigos.foresta.tipi.tattiche;

import com.threeamigos.foresta.tipi.ClasseIncantesimo;
import com.threeamigos.foresta.tipi.TipoDanno;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import com.threeamigos.foresta.tipi.Testi;

/**
 * Le azioni che una voce di tattica può compiere (tattiche_di_combattimento_automatiche.md §11.9): le sole già disponibili nella barra dei
 * comandi in combattimento. Corruzione, amicizia, fuga e passare inosservati non ci sono. Ogni azione dichiara su che cosa agisce
 * ({@link PortataAzione}) e da quali rose può pescare il bersaglio; i suoi requisiti (la magia, la pergamena...) li verifica chi valuta la tattica.
 * <p>
 * I testi di ogni costante (DESCRIZIONE) non stanno qui ma nel file {@code tipo_azione_<lingua>.properties}, alla voce
 * {@code COSTANTE.VOCE}, in modo che il gioco si possa tradurre (vedi {@link com.threeamigos.foresta.tipi.Testi}).
 */
public enum TipoAzione {

	/** L'attacco con l'arma impugnata */
	ATTACCO_FISICO(PortataAzione.AVVERSARIO, null),
	ARIA(PortataAzione.AVVERSARIO, ClasseIncantesimo.ARIA),
	ACQUA(PortataAzione.AVVERSARIO, ClasseIncantesimo.ACQUA),
	TERRA(PortataAzione.AVVERSARIO, ClasseIncantesimo.TERRA),
	FUOCO(PortataAzione.AVVERSARIO, ClasseIncantesimo.FUOCO),
	FULMINE(PortataAzione.AVVERSARIO, ClasseIncantesimo.FULMINE),
	GELO(PortataAzione.AVVERSARIO, ClasseIncantesimo.GELO),
	VELENO(PortataAzione.AVVERSARIO, ClasseIncantesimo.VELENO),
	MORTE(PortataAzione.AVVERSARIO, ClasseIncantesimo.MORTE),
	SILENZIO(PortataAzione.AVVERSARIO, ClasseIncantesimo.SILENZIO),
	LUCE_DIVINA(PortataAzione.AVVERSARIO, ClasseIncantesimo.LUCE_DIVINA),
	/** Uno dei sette incantesimi elementali, scelto con un criterio (non la Morte, non il Silenzio) */
	INCANTESIMO_DI_DANNO(PortataAzione.AVVERSARIO, null),
	/**
	 * Uno qualsiasi degli incantesimi malefici (anche la Morte e il Silenzio) che il personaggio può lanciare, scelto con un criterio: la replica di come
	 * scelgono i mostri oggi (RegistroTattiche); un giocatore usa piuttosto le azioni dei singoli incantesimi o INCANTESIMO_DI_DANNO
	 */
	INCANTESIMO_DEL_REPERTORIO(PortataAzione.AVVERSARIO, null),
	/** L'attacco magico di base, con danno arcano */
	DARDO_ARCANO(PortataAzione.AVVERSARIO, null),
	RESURREZIONE(PortataAzione.ALLEATO_CADUTO, ClasseIncantesimo.RESURREZIONE),
	ALBA_SACRA(PortataAzione.GRUPPO_ALLEATI, ClasseIncantesimo.ALBA_SACRA),
	/** Il canto del Bardo che cura un alleato (vedi bardo.md) */
	CANTO_CURATIVO(PortataAzione.ALLEATO, null),
	/** Il canto del Bardo che ipnotizza il gruppo avversario (vedi bardo.md) */
	CANTO_IPNOTICO(PortataAzione.GRUPPO_AVVERSARI, null),
	/** L'urlo della Banshee, che colpisce il gruppo avversario: il gruppo lo impara dal bardo della locanda */
	URLO_DELLA_BANSHEE(PortataAzione.GRUPPO_AVVERSARI, null),
	/** Il canto del Bardo che ispira il gruppo alleato (stato ISPIRATO, vedi bardo.md) */
	CANTO_DI_VALORE(PortataAzione.GRUPPO_ALLEATI, null),
	/** Dà a un alleato una pozione della salute */
	POZIONE_SALUTE(PortataAzione.ALLEATO, null),
	/** Dà a un alleato una pozione della magia */
	POZIONE_MAGIA(PortataAzione.ALLEATO, null),
	/** Dà a un alleato una pozione della salute grande */
	POZIONE_SALUTE_GRANDE(PortataAzione.ALLEATO, null),
	/** Dà a un alleato una pozione della magia grande */
	POZIONE_MAGIA_GRANDE(PortataAzione.ALLEATO, null),
	/** Cambia l'arma impugnata con quella del tipo di danno indicato dall'azione */
	CAMBIA_ARMA(PortataAzione.ARMA, null);

	private final PortataAzione portata;
	private final ClasseIncantesimo classeIncantesimo;

	TipoAzione(PortataAzione portata, ClasseIncantesimo classeIncantesimo) {
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
		return Testi.testoOCostante(this, "DESCRIZIONE");
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
