package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.intermezzi.Verso;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;

/**
 * Da che parte guarda, così com'è disegnata, l'immagine di ogni personaggio del gioco (vedi ClassePersonaggioImmagine):
 * serve a specchiarla quando deve guardare dall'altra parte, per esempio il gruppo del giocatore, che in locazione
 * guarda verso i mostri (vedi DisplayableCanvasRiquadroLocazione).
 * <p>
 * Un valore per ogni {@link TipoPersonaggio}, controllato da un test. Per ora sono tutti DESTRA, da correggere uno
 * per uno guardando le immagini.
 */
public final class VersiDeiPersonaggi {

	private static final Map<TipoPersonaggio, Verso> VERSI = new EnumMap<>(TipoPersonaggio.class);

	static {
		VERSI.put(TipoPersonaggio.ARPIA, Verso.SINISTRA);
		VERSI.put(TipoPersonaggio.CENTAURO, Verso.DESTRA);
		VERSI.put(TipoPersonaggio.CHIMERA, Verso.DESTRA);
		VERSI.put(TipoPersonaggio.CHIMERA_DRAGO, Verso.DESTRA);
		VERSI.put(TipoPersonaggio.DRAGO, Verso.SINISTRA);
		VERSI.put(TipoPersonaggio.EREMITA, Verso.SINISTRA);
		VERSI.put(TipoPersonaggio.FANTASMA, Verso.DESTRA);
		VERSI.put(TipoPersonaggio.FOLLETTO, Verso.DESTRA);
		VERSI.put(TipoPersonaggio.GARGOYLE, Verso.SINISTRA);
		VERSI.put(TipoPersonaggio.GIGANTE, Verso.DESTRA);
		VERSI.put(TipoPersonaggio.GOBLIN, Verso.DESTRA);
		VERSI.put(TipoPersonaggio.HOBGOBLIN, Verso.DESTRA);
		VERSI.put(TipoPersonaggio.IDRA, Verso.DESTRA);
		VERSI.put(TipoPersonaggio.LICH, Verso.DESTRA);
		VERSI.put(TipoPersonaggio.MINOTAURO, Verso.DESTRA);
		VERSI.put(TipoPersonaggio.MINOTAURO_GIGANTE, Verso.DESTRA);
		VERSI.put(TipoPersonaggio.OMBRA_NERA, Verso.DESTRA);
		VERSI.put(TipoPersonaggio.SCHELETRO, Verso.DESTRA);
		VERSI.put(TipoPersonaggio.SPETTRO, Verso.DESTRA);
		VERSI.put(TipoPersonaggio.SPIRITO, Verso.DESTRA);
		VERSI.put(TipoPersonaggio.STREGA, Verso.SINISTRA);
		VERSI.put(TipoPersonaggio.TITANO, Verso.DESTRA);
		VERSI.put(TipoPersonaggio.TROLL, Verso.SINISTRA);
		VERSI.put(TipoPersonaggio.VIVERNA, Verso.DESTRA);
		VERSI.put(TipoPersonaggio.BARDO, Verso.SINISTRA);
		VERSI.put(TipoPersonaggio.CANTASTORIE, Verso.DESTRA);
		VERSI.put(TipoPersonaggio.ELFA, Verso.DESTRA);
		VERSI.put(TipoPersonaggio.ELFO, Verso.SINISTRA);
		VERSI.put(TipoPersonaggio.GUERRIERA, Verso.DESTRA);
		VERSI.put(TipoPersonaggio.GUERRIERO, Verso.SINISTRA);
		VERSI.put(TipoPersonaggio.LADRA, Verso.DESTRA);
		VERSI.put(TipoPersonaggio.LADRO, Verso.SINISTRA);
		VERSI.put(TipoPersonaggio.MAGA, Verso.DESTRA);
		VERSI.put(TipoPersonaggio.MAGO, Verso.SINISTRA);
		VERSI.put(TipoPersonaggio.OMBRAFIAMMA, Verso.SINISTRA);
		VERSI.put(TipoPersonaggio.SACERDOTE, Verso.SINISTRA);
		VERSI.put(TipoPersonaggio.SACERDOTESSA, Verso.DESTRA);
		VERSI.put(TipoPersonaggio.VIANDANTE, Verso.SINISTRA);
	}

	private VersiDeiPersonaggi() {
	}

	/**
	 * Da che parte guarda l'immagine di quella classe, così com'è disegnata
	 *
	 * @throws IllegalArgumentException se la classe non ha un verso (un test impedisce che succeda)
	 */
	public static Verso di(TipoPersonaggio tipo) {
		Verso verso = VERSI.get(tipo);
		if (verso == null) {
			throw new IllegalArgumentException("Verso non noto per la classe " + tipo);
		}
		return verso;
	}

	/**
	 * Se, per far guardare la classe indicata nel verso voluto, occorre specchiare la sua immagine
	 */
	public static boolean serveSpecchiare(TipoPersonaggio tipo, Verso versoVoluto) {
		return di(tipo) != versoVoluto;
	}

	/**
	 * L'immagine rovesciata orizzontalmente, pixel per pixel. Non è ancora compatibile con lo schermo: lo diventa
	 * quando la costruisce ClassePersonaggioImmagine (vedi BufferedImageBuilder.ingrandisci).
	 */
	static BufferedImage specchia(BufferedImage immagine) {
		BufferedImage specchiata = new BufferedImage(immagine.getWidth(), immagine.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = specchiata.createGraphics();
		g.drawImage(immagine, immagine.getWidth(), 0, -immagine.getWidth(), immagine.getHeight(), null);
		g.dispose();
		return specchiata;
	}
}
