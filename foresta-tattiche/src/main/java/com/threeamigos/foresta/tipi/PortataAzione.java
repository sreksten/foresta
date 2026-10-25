package com.threeamigos.foresta.tipi;

import java.util.EnumSet;
import java.util.Set;

/**
 * Su che cosa agisce un'azione, e quindi come usa il selettore della voce (tattiche_di_combattimento_automatiche.md §11.2), e da
 * quali rose il selettore può pescare, per i personaggi del giocatore e per i mostri.
 */
public enum PortataAzione {
	/** Un avversario: il selettore sceglie il bersaglio. Un attacco dei miei non colpisce gli ospiti; per un mostro sì (sono fra i suoi bersagli) */
	AVVERSARIO(
			EnumSet.of(TipoRosa.AVVERSARI),
			EnumSet.of(TipoRosa.AVVERSARI, TipoRosa.OSPITI, TipoRosa.AVVERSARI_E_OSPITI)),
	/** Un alleato (anche chi decide): il selettore sceglie il bersaglio. Le cure dei mostri non riguardano gli ospiti */
	ALLEATO(
			EnumSet.of(TipoRosa.SE_STESSO, TipoRosa.ALLEATI, TipoRosa.OSPITI, TipoRosa.ALLEATI_E_OSPITI),
			EnumSet.of(TipoRosa.SE_STESSO, TipoRosa.ALLEATI)),
	/** Un alleato caduto: il selettore sceglie il bersaglio */
	ALLEATO_CADUTO(
			EnumSet.of(TipoRosa.ALLEATI_CADUTI),
			EnumSet.of(TipoRosa.ALLEATI_CADUTI)),
	/** Tutto il gruppo alleato: il selettore è solo un test di esistenza */
	GRUPPO_ALLEATI(
			EnumSet.of(TipoRosa.ALLEATI),
			EnumSet.of(TipoRosa.ALLEATI)),
	/** Tutti gli avversari: il selettore è solo un test di esistenza */
	GRUPPO_AVVERSARI(
			EnumSet.of(TipoRosa.AVVERSARI),
			EnumSet.of(TipoRosa.AVVERSARI)),
	/** Un'arma: il selettore (sempre SE_STESSO) dice chi cambia arma */
	ARMA(
			EnumSet.of(TipoRosa.SE_STESSO),
			EnumSet.of(TipoRosa.SE_STESSO));

	private final Set<TipoRosa> roseDelGiocatore;
	private final Set<TipoRosa> roseDeiMostri;

	PortataAzione(Set<TipoRosa> roseDelGiocatore, Set<TipoRosa> roseDeiMostri) {
		this.roseDelGiocatore = roseDelGiocatore;
		this.roseDeiMostri = roseDeiMostri;
	}

	/**
	 * Le rose da cui il selettore può pescare, per i personaggi del giocatore o per i mostri: una copia, che chi la riceve può cambiare.
	 *
	 * @param attoreDelGiocatore true se chi decide è un personaggio del giocatore, false se è un mostro
	 */
	public Set<TipoRosa> roseAmmesse(boolean attoreDelGiocatore) {
		return EnumSet.copyOf(attoreDelGiocatore ? roseDelGiocatore : roseDeiMostri);
	}

	/**
	 * Su che cosa agisce quell'azione.
	 */
	public static PortataAzione di(TipoAzione azione) {
		switch (azione) {
			case ATTACCO_FISICO:
			case ARIA:
			case ACQUA:
			case TERRA:
			case FUOCO:
			case FULMINE:
			case GELO:
			case VELENO:
			case MORTE:
			case SILENZIO:
			case LUCE_DIVINA:
			case INCANTESIMO_DI_DANNO:
			case INCANTESIMO_DEL_REPERTORIO:
			case DARDO_ARCANO:
				return AVVERSARIO;
			case RESURREZIONE:
				return ALLEATO_CADUTO;
			case ALBA_SACRA:
			case CANTO_DI_VALORE:
			case PELLE_DI_CORTECCIA:
				return GRUPPO_ALLEATI;
			case CANTO_CURATIVO:
			case IMPOSIZIONE_DELLE_MANI:
			case RIGENERAZIONE:
			case POZIONE_SALUTE:
			case POZIONE_MAGIA:
			case POZIONE_SALUTE_GRANDE:
			case POZIONE_MAGIA_GRANDE:
				return ALLEATO;
			case CANTO_IPNOTICO:
			case URLO_DELLA_BANSHEE:
			case SCACCIARE_I_NON_MORTI:
			case RADICI:
			case SCIAME:
				return GRUPPO_AVVERSARI;
			case CAMBIA_ARMA:
				return ARMA;
			default:
				throw new IllegalStateException("Portata non prevista per " + azione);
		}
	}

	/**
	 * Le rose da cui il selettore di una voce con quell'azione può pescare, per i personaggi del giocatore o per i mostri: quelle della sua
	 * portata.
	 */
	public static Set<TipoRosa> roseAmmesse(TipoAzione azione, boolean attoreDelGiocatore) {
		return di(azione).roseAmmesse(attoreDelGiocatore);
	}
}
