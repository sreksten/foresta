package com.threeamigos.foresta.intermezzi;

import com.threeamigos.foresta.personaggi.ClassePersonaggio;

import java.util.EnumMap;
import java.util.Map;

/**
 * Da che parte guarda, così come disegnata, l'immagine di ciascuna classe di personaggio
 * giocante: i maschi (ombrafiamma, mago, elfo, guerriero, ladro, bardo) guardano a
 * sinistra, le femmine (elfa, guerriera, cantastorie, maga, ladra) a destra. Serve agli
 * intermezzi per sapere quando applicare {@link ElementoIntermezzo#specchiato()} o
 * {@link Tappa#specchiata(boolean)} per ottenere il verso voluto, indipendentemente da
 * come è disegnata l'immagine originale.
 */
public final class VersoDiDefault {

	private static final Map<ClassePersonaggio, Verso> VERSI = new EnumMap<>(ClassePersonaggio.class);

	static {
		VERSI.put(ClassePersonaggio.OMBRAFIAMMA, Verso.SINISTRA);
		VERSI.put(ClassePersonaggio.MAGO, Verso.SINISTRA);
		VERSI.put(ClassePersonaggio.ELFO, Verso.SINISTRA);
		VERSI.put(ClassePersonaggio.GUERRIERO, Verso.SINISTRA);
		VERSI.put(ClassePersonaggio.LADRO, Verso.SINISTRA);
		VERSI.put(ClassePersonaggio.BARDO, Verso.SINISTRA);
		VERSI.put(ClassePersonaggio.ELFA, Verso.DESTRA);
		VERSI.put(ClassePersonaggio.GUERRIERA, Verso.DESTRA);
		VERSI.put(ClassePersonaggio.CANTASTORIE, Verso.DESTRA);
		VERSI.put(ClassePersonaggio.MAGA, Verso.DESTRA);
		VERSI.put(ClassePersonaggio.LADRA, Verso.DESTRA);
	}

	private VersoDiDefault() {
	}

	/**
	 * @throws IllegalArgumentException se la classe non è una delle classi giocanti note
	 */
	public static Verso di(ClassePersonaggio classe) {
		Verso verso = VERSI.get(classe);
		if (verso == null) {
			throw new IllegalArgumentException("Verso di default non noto per la classe " + classe);
		}
		return verso;
	}

	/**
	 * Se, per far guardare la classe indicata nel verso voluto, occorre specchiare
	 * l'immagine originale.
	 */
	public static boolean serveSpecchiare(ClassePersonaggio classe, Verso versoVoluto) {
		return di(classe) != versoVoluto;
	}
}
