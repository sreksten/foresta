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
	 * La nota di un modificatore ("Pelle di corteccia", "la lealtà") nella lingua in uso. La nota salvata è sempre quella italiana, che serve a
	 * riconoscere il modificatore: qui la si traduce per mostrarla (vedi note_modificatori_&lt;lingua&gt;.properties); una nota senza traduzione resta com'è.
	 */
	public static String nota(String nota) {
		return Testi.get("note_modificatori", nota.replace(" ", "_")).orElse(nota);
	}

	/**
	 * L'effetto, per esempio "Fortuna porta a 0" o "Coraggio -20%".
	 */
	public static String effetto(ModificatoreAttributoMD modificatore) {
		String variazione = variazione(modificatore);
		String testo = variazione != null ? variazione : testo("PORTA_A", (int) modificatore.getQuantita());
		return modificatore.getTipoAttributo().getNome() + " " + testo;
	}

	/**
	 * Breve, per gli sprite: "Furtività +5", "Coraggio -3", "Coraggio -20%", "Intelligenza a 0".
	 */
	public static String breve(ModificatoreAttributoMD modificatore) {
		String nome = modificatore.getTipoAttributo().getNome();
		String variazione = variazione(modificatore);
		return variazione != null ? nome + " " + variazione : testo("BREVE_A", nome, (int) modificatore.getQuantita());
	}

	/**
	 * La variazione con il suo segno, "+5" o "-20%"; null per un modificatore che porta l'attributo a un valore, che ogni
	 * metodo scrive a modo suo. {@code %+d} scrive il numero sempre con il segno, anche quando è positivo.
	 */
	private static String variazione(ModificatoreAttributoMD modificatore) {
		int valore = (int) modificatore.getQuantita();
		switch (modificatore.getTipoModificatoreAttributo()) {
			case VARIAZIONE_FISSA:
				return String.format(Locale.ROOT, "%+d", valore);
			case VARIAZIONE_PERCENTUALE:
				return String.format(Locale.ROOT, "%+d%%", valore);
			default:
				return null;
		}
	}

	/**
	 * La durata, per esempio "per 3 turni" o "per 1 locazione"; vuota se è permanente.
	 */
	public static String durata(ModificatoreAttributoMD modificatore) {
		return tempo(modificatore, "DURATA_");
	}

	/**
	 * Quanto manca alla fine, per esempio "ancora 3 turni"; vuota se il modificatore è permanente.
	 */
	public static String durataResidua(ModificatoreAttributoMD modificatore) {
		return tempo(modificatore, "RESIDUO_");
	}

	// La durata in turni o in locazioni, al singolare o al plurale: le chiavi sono il prefisso più TURNO, TURNI, LOCAZIONE o
	// LOCAZIONI ("DURATA_TURNI" dà "per 3 turni", "RESIDUO_TURNI" dà "ancora 3 turni"); vuota se il modificatore è permanente
	private static String tempo(ModificatoreAttributoMD modificatore, String prefisso) {
		int durata = modificatore.getDurata();
		switch (modificatore.getUnitaDurata()) {
			case TURNI:
				return testo(prefisso + (durata == 1 ? "TURNO" : "TURNI"), durata);
			case LOCAZIONI:
				return testo(prefisso + (durata == 1 ? "LOCAZIONE" : "LOCAZIONI"), durata);
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
