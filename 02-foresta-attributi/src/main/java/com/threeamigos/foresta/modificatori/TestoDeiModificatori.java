package com.threeamigos.foresta.modificatori;

import com.threeamigos.foresta.modellodati.ModificatoreAttributoMD;
import com.threeamigos.foresta.testi.Testi;
import com.threeamigos.foresta.tipi.BersaglioModificatore;

import java.util.Locale;

/**
 * I testi con cui si racconta un modificatore di attributo al giocatore (i messaggi del motore, le schermate e gli
 * sprite della UI). Stanno qui e non nel modello dati, che tiene solo i dati: il {@link ModificatoreAttributoMD} non
 * sa come lo si scrive.
 * <p>
 * Le cifre sono sempre quelle latine, qualunque sia la lingua del dispositivo: il font bitmap del gioco non sa disegnare
 * le altre.
 */
public final class TestoDeiModificatori {

	/**
	 * L'effetto, per esempio "Fortuna porta a 0" o "Coraggio -20%".
	 */
	public static String effetto(ModificatoreAttributoMD modificatore) {
		int valore = (int) modificatore.getQuantita();
		String testo;
		switch (modificatore.getTipoModificatoreAttributo()) {
			case VARIAZIONE_FISSA:
				testo = String.format(Locale.ROOT, "%+d", valore);
				break;
			case VARIAZIONE_PERCENTUALE:
				testo = String.format(Locale.ROOT, "%+d%%", valore);
				break;
			default:
				testo = testo("PORTA_A", valore);
				break;
		}
		return modificatore.getTipoAttributo().getNome() + " " + testo;
	}

	/**
	 * Breve, per gli sprite: "Furtività +5", "Coraggio -3", "Coraggio -20%", "Intelligenza a 0".
	 */
	public static String breve(ModificatoreAttributoMD modificatore) {
		int valore = (int) modificatore.getQuantita();
		String nome = modificatore.getTipoAttributo().getNome();
		switch (modificatore.getTipoModificatoreAttributo()) {
			case VARIAZIONE_FISSA:
				return String.format(Locale.ROOT, "%s %+d", nome, valore);
			case VARIAZIONE_PERCENTUALE:
				return String.format(Locale.ROOT, "%s %+d%%", nome, valore);
			default:
				return testo("BREVE_A", nome, valore);
		}
	}

	/**
	 * La durata, per esempio "per 3 turni" o "per 1 locazione"; vuota se è permanente.
	 */
	public static String durata(ModificatoreAttributoMD modificatore) {
		int durata = modificatore.getDurata();
		switch (modificatore.getUnitaDurata()) {
			case TURNI:
				return testo(durata == 1 ? "DURATA_TURNO" : "DURATA_TURNI", durata);
			case LOCAZIONI:
				return testo(durata == 1 ? "DURATA_LOCAZIONE" : "DURATA_LOCAZIONI", durata);
			default:
				return "";
		}
	}

	/**
	 * Quanto manca alla fine, per esempio "ancora 3 turni"; vuota se il modificatore è permanente.
	 */
	public static String durataResidua(ModificatoreAttributoMD modificatore) {
		int durata = modificatore.getDurata();
		switch (modificatore.getUnitaDurata()) {
			case TURNI:
				return testo(durata == 1 ? "RESIDUO_TURNO" : "RESIDUO_TURNI", durata);
			case LOCAZIONI:
				return testo(durata == 1 ? "RESIDUO_LOCAZIONE" : "RESIDUO_LOCAZIONI", durata);
			default:
				return "";
		}
	}

	// Le parole stanno in testo_modificatori_<lingua>.properties
	private static String testo(String chiave, Object... valori) {
		return Testi.formatta("testo_modificatori", chiave, valori);
	}

	/**
	 * Tutto, per esempio "Fortuna porta a 0 sul bersaglio per 5 turni".
	 */
	public static String completo(ModificatoreAttributoMD modificatore) {
		StringBuilder sb = new StringBuilder(effetto(modificatore));
		if (modificatore.getBersaglio() == BersaglioModificatore.BERSAGLIO) {
			sb.append(' ').append(testo("SUL_BERSAGLIO"));
		}
		if (modificatore.isTemporaneo()) {
			sb.append(' ').append(durata(modificatore));
		}
		return sb.toString();
	}

	private TestoDeiModificatori() {
	}

}
