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

	// Quante caselle si fanno in un giorno di cammino
	private static final int CASELLE_AL_GIORNO = 6;
	// Oltre questa distanza (sei giorni) non si dice quanto manca, solo che è lontano
	private static final int CASELLE_PER_LONTANO = 36;
	// Oltre questa distanza è molto lontano
	private static final int CASELLE_PER_MOLTO_LONTANO = 50;
	// Da quante caselle oltre i giorni interi si dice "quasi" il giorno dopo invece di "poco più di"
	private static final int CASELLE_PER_QUASI_UN_GIORNO = 4;

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
		// Quante caselle in orizzontale e in verticale (sempre positive), e la distanza a piedi: si cammina solo in
		// orizzontale e in verticale, quindi le due si sommano
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
		// Un asse conta se il suo spostamento è almeno la metà dell'altro: 4 a nord e 1 a est è "nord", 4 a nord e 3 a est
		// è "nordest"
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
		if (distanza > CASELLE_PER_LONTANO) {
			return testo(distanza > CASELLE_PER_MOLTO_LONTANO ? "MOLTO_LONTANO" : "LONTANO", punto);
		}
		// I giorni interi di cammino e le caselle che avanzano (sono le ore: un giorno di cammino ha sei ore)
		int giorni = distanza / CASELLE_AL_GIORNO;
		int resto = distanza % CASELLE_AL_GIORNO;
		String quanto;
		if (giorni == 0) {
			quanto = resto == 1 ? testo("UN_ORA") : testo("ORE", Lingue.corrente().cardinale(resto, Genere.MASCHILE));
		} else if (resto < CASELLE_PER_QUASI_UN_GIORNO) {
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
