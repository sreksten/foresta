package com.threeamigos.foresta.mappa;

import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.testi.Lingue;
import com.threeamigos.foresta.testi.Genere;
import com.threeamigos.foresta.testi.Testi;

/**
 * La direzione e la distanza fra due punti della foresta, a parole ("a poco più di un giorno di cammino verso nordest."): per
 * le informazioni su città, castelli e artefatti, o per gli eventi.
 *
 * @author Stefano Reksten
 */
public final class Direzioni {

	private static final String FILE_DEI_TESTI = "direzioni";

	private Direzioni() {
	}

	/**
	 * Riporta la direzione di un punto della foresta rispetto a un altro, di solito la posizione del gruppo
	 * (per le informazioni su città, castelli, artefatti, o per gli eventi)
	 */
	public static String getDirezione(CoordinateMD da, CoordinateMD coordinate) {
		return getDirezione(da.getX(), da.getY(), coordinate.getX(), coordinate.getY());
	}

	public static String getDirezione(int daX, int daY, int aX, int aY) {
		int dx = (daX > aX ? daX - aX : aX - daX);
		int dy = (daY > aY ? daY - aY : aY - daY);
		int distanza = dx + dy;
		if (distanza == 0) {
			// Nello stesso punto non c'è una direzione: "a zero ore di cammino verso ovest" non vuol dire niente
			return testo("PROPRIO_QUI");
		}
		// Il punto cardinale: nord o sud, est od ovest, o tutti e due (nordest) se nessuno dei due assi domina
		String nordSud = null;
		String estOvest = null;
		if (daY != aY) {
			if (dy >= (dx + 1) / 2) {
				nordSud = daY < aY ? "SUD" : "NORD";
			}
			if (dx >= (dy + 1) / 2) {
				estOvest = aX > daX ? "EST" : "OVEST";
			}
		} else {
			estOvest = aX > daX ? "EST" : "OVEST";
		}
		String punto = testo("PUNTO." + (nordSud != null ? nordSud : "") + (nordSud != null && estOvest != null ? "_" : "") + (estOvest != null ? estOvest : ""));
		if (distanza > 36) {
			return testo(distanza > 50 ? "MOLTO_LONTANO" : "LONTANO", punto);
		}
		int giorni = distanza / 6;
		int resto = distanza % 6;
		String quanto;
		if (giorni == 0) {
			quanto = resto == 1 ? testo("UN_ORA") : testo("ORE", Lingue.corrente().cardinale(resto, Genere.MASCHILE));
		} else if (resto < 4) {
			String giorniInLettere = giorni == 1 ? testo("UN_GIORNO") : testo("GIORNI", Lingue.corrente().cardinale(giorni, Genere.MASCHILE));
			quanto = resto > 0 ? testo("POCO_PIU_DI", giorniInLettere) : giorniInLettere;
		} else {
			quanto = testo("QUASI", testo("GIORNI", Lingue.corrente().cardinale(giorni + 1, Genere.MASCHILE)));
		}
		return testo("DI_CAMMINO", quanto, punto);
	}

	// Le frasi stanno in direzioni_<lingua>.properties
	private static String testo(String chiave, Object... valori) {
		return Testi.formatta(FILE_DEI_TESTI, chiave, valori);
	}
}
