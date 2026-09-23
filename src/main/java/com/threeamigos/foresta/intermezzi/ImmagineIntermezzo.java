package com.threeamigos.foresta.intermezzi;

import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;

import java.util.Arrays;
import java.util.Objects;

/**
 * Un riferimento a un'immagine da usare in un intermezzo, senza dipendere dalla UI:
 * il motore dice quale immagine, la UI la carica. Può essere:
 * <ul>
 *     <li>l'immagine di una classe di personaggio;</li>
 *     <li>l'illustrazione di una locazione;</li>
 *     <li>una risorsa qualsiasi sotto /com/threeamigos/foresta/img/ (per esempio "intermezzi/Tramonto.png");</li>
 *     <li>uno sprite sheet: una risorsa divisa in una griglia di fotogrammi uguali, mostrati
 *     in sequenza a un certo numero di fotogrammi al secondo;</li>
 *     <li>un'{@link Animazione} fornita da una classe della UI.</li>
 * </ul>
 * Le ultime due sono animate: il fotogramma dipende dai secondi trascorsi dall'inizio della pagina.
 */
public final class ImmagineIntermezzo {

	public enum Tipo {
		PERSONAGGIO,
		LOCAZIONE,
		RISORSA,
		SPRITE_SHEET,
		ANIMAZIONE
	}

	private final Tipo tipo;
	private ClassePersonaggio classePersonaggio;
	private ClassiLocazione classeLocazione;
	private String risorsa;
	private Animazione animazione;
	// Solo per gli sprite sheet
	private int colonne;
	private int righe;
	private double fotogrammiAlSecondo;
	private int[] sequenza;

	private ImmagineIntermezzo(Tipo tipo) {
		this.tipo = tipo;
	}

	public static ImmagineIntermezzo personaggio(ClassePersonaggio classePersonaggio) {
		ImmagineIntermezzo immagine = new ImmagineIntermezzo(Tipo.PERSONAGGIO);
		immagine.classePersonaggio = Objects.requireNonNull(classePersonaggio);
		return immagine;
	}

	public static ImmagineIntermezzo locazione(ClassiLocazione classeLocazione) {
		ImmagineIntermezzo immagine = new ImmagineIntermezzo(Tipo.LOCAZIONE);
		immagine.classeLocazione = Objects.requireNonNull(classeLocazione);
		return immagine;
	}

	/**
	 * @param percorso relativo a /com/threeamigos/foresta/img/, per esempio "intermezzi/Tramonto.png"
	 */
	public static ImmagineIntermezzo risorsa(String percorso) {
		ImmagineIntermezzo immagine = new ImmagineIntermezzo(Tipo.RISORSA);
		immagine.risorsa = verificaPercorso(percorso);
		return immagine;
	}

	/**
	 * Uno sprite sheet: la risorsa è divisa in colonne × righe fotogrammi della stessa
	 * dimensione, numerati da 0 riga per riga (da sinistra a destra, dall'alto in basso).
	 *
	 * @param sequenza i fotogrammi da mostrare, nell'ordine, ripetuti all'infinito; se
	 *                 non indicata, tutti i fotogrammi dal primo all'ultimo
	 */
	public static ImmagineIntermezzo spriteSheet(String percorso, int colonne, int righe, double fotogrammiAlSecondo,
			int... sequenza) {
		if (colonne <= 0 || righe <= 0) {
			throw new IllegalArgumentException("Uno sprite sheet deve avere almeno una riga e una colonna");
		}
		if (fotogrammiAlSecondo <= 0) {
			throw new IllegalArgumentException("I fotogrammi al secondo devono essere positivi");
		}
		for (int fotogramma : sequenza) {
			if (fotogramma < 0 || fotogramma >= colonne * righe) {
				throw new IllegalArgumentException("Fotogramma " + fotogramma + " fuori dallo sprite sheet " + percorso);
			}
		}
		ImmagineIntermezzo immagine = new ImmagineIntermezzo(Tipo.SPRITE_SHEET);
		immagine.risorsa = verificaPercorso(percorso);
		immagine.colonne = colonne;
		immagine.righe = righe;
		immagine.fotogrammiAlSecondo = fotogrammiAlSecondo;
		immagine.sequenza = sequenza.length > 0 ? sequenza.clone() : null;
		return immagine;
	}

	public static ImmagineIntermezzo animazione(Animazione animazione) {
		ImmagineIntermezzo immagine = new ImmagineIntermezzo(Tipo.ANIMAZIONE);
		immagine.animazione = Objects.requireNonNull(animazione);
		return immagine;
	}

	private static String verificaPercorso(String percorso) {
		if (percorso == null || percorso.isEmpty()) {
			throw new IllegalArgumentException("Percorso della risorsa mancante");
		}
		return percorso;
	}

	public Tipo getTipo() {
		return tipo;
	}

	public ClassePersonaggio getClassePersonaggio() {
		return classePersonaggio;
	}

	public ClassiLocazione getClasseLocazione() {
		return classeLocazione;
	}

	/** Il percorso della risorsa, per RISORSA e SPRITE_SHEET. */
	public String getRisorsa() {
		return risorsa;
	}

	public Animazione getAnimazione() {
		return animazione;
	}

	public int getColonne() {
		return colonne;
	}

	public int getRighe() {
		return righe;
	}

	/**
	 * Per uno sprite sheet, il fotogramma da mostrare a un certo numero di secondi
	 * dall'inizio della pagina (numerato da 0 riga per riga).
	 */
	public int getFotogrammaAl(double secondi) {
		int passo = (int) Math.floor(Math.max(0, secondi) * fotogrammiAlSecondo);
		if (sequenza != null) {
			return sequenza[passo % sequenza.length];
		}
		return passo % (colonne * righe);
	}

	@Override
	public String toString() {
		switch (tipo) {
			case PERSONAGGIO:
				return "personaggio " + classePersonaggio;
			case LOCAZIONE:
				return "locazione " + classeLocazione;
			case SPRITE_SHEET:
				return "sprite sheet " + risorsa + " " + colonne + "x" + righe
						+ (sequenza == null ? "" : " " + Arrays.toString(sequenza));
			case ANIMAZIONE:
				return "animazione " + animazione;
			default:
				return "risorsa " + risorsa;
		}
	}
}
