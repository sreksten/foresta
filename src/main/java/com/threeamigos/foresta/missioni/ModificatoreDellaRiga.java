package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.modellodati.ModificatoreAttributo;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoModificatore;

/**
 * Un modificatore permanente scritto in una riga di missioni.txt come ATTRIBUTO TIPO QUANTITA, per esempio
 * FORZA AUMENTO_FISSO 2 o SALUTE AUMENTO_PERCENTUALE 10 (TipoAttributo e TipoModificatore, solo aumenti): la
 * benedizione (vedi BenedizioneRichiesta) e la lealtà di un compagno (vedi LealtaRichiesta).
 */
final class ModificatoreDellaRiga {

	private final TipoAttributo attributo;
	private final TipoModificatore tipoModificatore;
	private final int quantita;

	private ModificatoreDellaRiga(String valore, String riga) {
		String[] parti = valore.trim().split("\\s+");
		if (parti.length != 3) {
			throw new IllegalArgumentException("Il modificatore è ATTRIBUTO TIPO QUANTITA (FORZA AUMENTO_FISSO 2): " + riga);
		}
		attributo = TipoAttributo.valueOf(parti[0]);
		tipoModificatore = TipoModificatore.valueOf(parti[1]);
		quantita = Integer.parseInt(parti[2]);
		if (tipoModificatore == TipoModificatore.QUANTITA_ASSOLUTA || quantita < 1) {
			throw new IllegalArgumentException("Il modificatore è un aumento, fisso o in percentuale, di almeno 1: " + riga);
		}
	}

	static ModificatoreDellaRiga da(String valore, String riga) {
		return new ModificatoreDellaRiga(valore, riga);
	}

	TipoAttributo getAttributo() {
		return attributo;
	}

	TipoModificatore getTipoModificatore() {
		return tipoModificatore;
	}

	int getQuantita() {
		return quantita;
	}

	/**
	 * Un modificatore nuovo, con quella nota (il nome della benedizione, della lealtà...).
	 */
	ModificatoreAttributo nuovo(String nota) {
		return new ModificatoreAttributo(attributo, tipoModificatore, quantita, nota);
	}
}
