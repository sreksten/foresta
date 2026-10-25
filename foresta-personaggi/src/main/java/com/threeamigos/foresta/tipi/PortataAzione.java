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
}
