package com.threeamigos.foresta.salvataggi;

import com.threeamigos.foresta.interfacce.GestorePunteggi;
import com.threeamigos.foresta.punteggi.Classifica;
import com.threeamigos.foresta.strumenti.Punteggio;

import java.io.File;

/**
 * La classifica dei migliori dieci punteggi su un supporto: la classifica vera (i posti e le regole per entrarci) è
 * {@link Classifica}, in {@code 01-foresta-punteggi}; qui si decide quando leggerla e quando scriverla. Chi la estende dice come si
 * legge e si scrive ({@code carica} e {@code salva}).
 */
abstract class GestorePunteggiBase extends GestoreSuFile implements GestorePunteggi {

	private final Classifica classifica = new Classifica();

	/**
	 * Se la classifica non si riesce a leggere (file assente, ma anche rovinato o troncato) si riparte da
	 * quella predefinita e la si salva subito. Un file rovinato non si sovrascrive: chi lo legge lo mette da
	 * parte con l'estensione {@code .rovinato}, così quello che conteneva di buono si può ancora recuperare.
	 */
	public GestorePunteggiBase() {
		this(null);
	}

	/**
	 * Come {@link #GestorePunteggiBase()}, ma con la cartella indicata (vedi {@link GestoreSuFile}).
	 */
	public GestorePunteggiBase(File directory) {
		super(directory);
		if (!carica()) {
			classifica.ripristinaLaPredefinita();
			salva();
		}
	}

	public int getConteggio() {
		return classifica.getConteggio();
	}

	public Punteggio getPunteggio(int posizione) {
		return classifica.getPunteggio(posizione);
	}

	public boolean isPunteggioInClassifica(int punteggio, String idPartita) {
		return classifica.isPunteggioInClassifica(punteggio, idPartita);
	}

	/**
	 * Inserisce il punteggio al suo posto in classifica, se ce n'è uno, e poi salva la classifica.
	 */
	public void addPunteggio(String nome, int punteggio, String idPartita) {
		if (classifica.inserisci(nome, punteggio, idPartita)) {
			salva();
		}
	}

	protected void setPunteggio(int posizione, String nome, int punteggio, String idPartita) {
		classifica.setPunteggio(posizione, nome, punteggio, idPartita);
	}
}
