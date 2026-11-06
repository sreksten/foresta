package com.threeamigos.foresta.ui.componenti;

import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;

import java.util.List;

/**
 * Una cosa da disegnare di un {@link ElencoScorrevole}: un'immagine (l'icona di un nodo, o il suo valore già composto) o un testo, con la posizione. Le
 * coordinate sono quelle dell'elenco intero (l'angolo in alto a sinistra è (0, 0), prima di togliere lo scorrimento): chi disegna sposta tutto in su di
 * quanto l'elenco è scorso e ritaglia all'altezza dello spazio. Un testo ha già le righe in cui va a capo e il passo fra una riga e l'altra.
 */
public final class ElementoDelElenco {

	/**
	 * Che cos'è
	 */
	public enum Tipo {
		/**
		 * L'icona a sinistra di un nodo (<code>immagine</code>)
		 */
		ICONA,
		/**
		 * Il valore di un nodo come immagine già composta (<code>immagine</code>), allineato a destra
		 */
		VALORE_IMMAGINE,
		/**
		 * Il valore di un nodo come testo, su una riga, allineato a destra
		 */
		VALORE_TESTO,
		/**
		 * Il titolo di un nodo o la sua descrizione: testo che va a capo
		 */
		TESTO
	}

	private final Tipo tipo;
	private final Object immagine;
	private final int larghezzaImmagine;
	private final String testo;
	private final List<String> righe;
	private final DoomdarkFont font;
	private final DoomdarkColorModel.Color colore;
	private final int larghezzaDiImpaginazione;
	private final int passoRiga;
	private final int x;
	private final int y;

	ElementoDelElenco(Tipo tipo, Object immagine, int larghezzaImmagine, String testo, List<String> righe, DoomdarkFont font,
					  DoomdarkColorModel.Color colore, int larghezzaDiImpaginazione, int passoRiga, int x, int y) {
		this.tipo = tipo;
		this.immagine = immagine;
		this.larghezzaImmagine = larghezzaImmagine;
		this.testo = testo;
		this.righe = righe;
		this.font = font;
		this.colore = colore;
		this.larghezzaDiImpaginazione = larghezzaDiImpaginazione;
		this.passoRiga = passoRiga;
		this.x = x;
		this.y = y;
	}

	public Tipo getTipo() {
		return tipo;
	}

	/**
	 * L'immagine, come l'ha data chi ha creato il nodo (null per i testi)
	 */
	public Object getImmagine() {
		return immagine;
	}

	/**
	 * La larghezza dell'immagine (per allineare a destra un valore)
	 */
	public int getLarghezzaImmagine() {
		return larghezzaImmagine;
	}

	/**
	 * Il testo intero (null per le immagini)
	 */
	public String getTesto() {
		return testo;
	}

	/**
	 * Le righe in cui il testo va a capo entro {@link #getLarghezzaDiImpaginazione()}; per un valore di testo una riga sola
	 */
	public List<String> getRighe() {
		return righe;
	}

	public DoomdarkFont getFont() {
		return font;
	}

	public DoomdarkColorModel.Color getColore() {
		return colore;
	}

	/**
	 * La larghezza entro cui il testo va a capo
	 */
	public int getLarghezzaDiImpaginazione() {
		return larghezzaDiImpaginazione;
	}

	/**
	 * La distanza fra le cime di due righe consecutive (l'altezza del font più l'interlinea)
	 */
	public int getPassoRiga() {
		return passoRiga;
	}

	/**
	 * Dove va l'angolo in alto a sinistra (per un valore, l'angolo in alto a sinistra dell'immagine allineata a destra)
	 */
	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
}
