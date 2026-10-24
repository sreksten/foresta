package com.threeamigos.foresta.strumenti;

import com.threeamigos.foresta.modificatori.tipi.BersaglioModificatore;
import com.threeamigos.foresta.modificatori.ModificatoreAttributoMD;

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
				testo = "porta a " + valore;
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
				return nome + " a " + valore;
		}
	}

	/**
	 * La durata, per esempio "per 3 turni" o "per 1 locazione"; vuota se è permanente.
	 */
	public static String durata(ModificatoreAttributoMD modificatore) {
		int durata = modificatore.getDurata();
		switch (modificatore.getUnitaDurata()) {
			case TURNI:
				return "per " + durata + (durata == 1 ? " turno" : " turni");
			case LOCAZIONI:
				return "per " + durata + (durata == 1 ? " locazione" : " locazioni");
			default:
				return "";
		}
	}

	/**
	 * Tutto, per esempio "Fortuna porta a 0 sul bersaglio per 5 turni".
	 */
	public static String completo(ModificatoreAttributoMD modificatore) {
		StringBuilder sb = new StringBuilder(effetto(modificatore));
		if (modificatore.getBersaglio() == BersaglioModificatore.BERSAGLIO) {
			sb.append(" sul bersaglio");
		}
		if (modificatore.isTemporaneo()) {
			sb.append(' ').append(durata(modificatore));
		}
		return sb.toString();
	}

	private TestoDeiModificatori() {
	}

}
