package com.threeamigos.foresta.canti;

import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.TipoCanto;

/**
 * Costruisce il canto di quel tipo, per il personaggio che lo canta
 */
public final class FabbricaCanti {

	private FabbricaCanti() {
	}

	/**
	 * Il canto di quel tipo, per il personaggio che lo canta.
	 */
	public static Canto crea(TipoCanto tipo, Personaggio cantore) {
		switch (tipo) {
			case CURATIVO:
				return new CantoCurativo(cantore);
			case IPNOTICO:
				return new CantoIpnotico(cantore);
			case DI_VALORE:
				return new CantoDiValore(cantore);
			case URLO_DELLA_BANSHEE:
				return new UrloDellaBanshee(cantore);
			default:
				throw new IllegalArgumentException("TipoCanto non gestito: " + tipo);
		}
	}
}
