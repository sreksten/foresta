package com.threeamigos.foresta.incantesimi;

import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.motore.Gruppo;
import com.threeamigos.foresta.personaggi.Personaggio;

/**
 * Il canto che rafforza il coraggio e la precisione di tutto il gruppo con un effetto di stato proprio, a cui
 * manca ancora l'agganciamento ai calcoli (vedi bardo.md, fasi 7 e 8): finché non c'è, non si può cantare.
 */
public class CantoDiValore implements Canto {

	private final int livello;

	public CantoDiValore(Personaggio cantore) {
		this.livello = cantore.getLivello();
	}

	@Override
	public TipoCanto getTipo() {
		return TipoCanto.DI_VALORE;
	}

	@Override
	public int getCostoLancio() {
		return Costanti.CANTO_DI_VALORE_COSTO_LANCIO;
	}

	public int getLivello() {
		return livello;
	}

	@Override
	public boolean canta(Personaggio cantore, Gruppo gruppo, Gruppo gruppoAvversario) {
		throw new UnsupportedOperationException("Il canto di valore aspetta l'effetto di stato ISPIRATO (bardo.md, fasi 7 e 8)");
	}
}
