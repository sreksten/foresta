package com.threeamigos.foresta.intermezzi;

import com.threeamigos.foresta.personaggi.ClassePersonaggio;

import java.util.ArrayList;
import java.util.List;

/**
 * Un'immagine in una pagina di intermezzo: un personaggio, un oggetto, un elemento di
 * sfondo animato. Parte da uno stato iniziale (posizione del centro in frazioni dello
 * schermo, scala, opacità) e può percorrere una sequenza di {@link Tappa}, ripetuta come
 * indicato da {@link Ripetizione}. Il tempo si misura in secondi dall'inizio della pagina.
 * <p>
 * L'identificativo serve alle {@link BattutaIntermezzo} per sapere chi parla: il fumetto
 * punta verso la bocca dell'elemento e lo segue se si muove.
 * <p>
 * Il verso dell'immagine si può fissare all'inizio ({@link #specchiato()}), cambiare tappa
 * per tappa ({@link Tappa#specchiata(boolean)}) oppure lasciar decidere al movimento
 * ({@link #orientaNelVersoDelMoto(Verso)}): in quel caso l'elemento guarda sempre dove va,
 * anche quando un'animazione avanti e indietro lo riporta al punto di partenza.
 * <pre>
 *     ElementoIntermezzo.di("drago", ImmagineIntermezzo.personaggio(ClassePersonaggio.DRAGO), 1.2, 0.2)
 *             .conScala(0.4).conOpacita(0.5)
 *             .poi(Tappa.inSecondi(10).verso(-0.2, 0.3))
 *             .ripeti(Ripetizione.CICLICA)
 * </pre>
 */
public final class ElementoIntermezzo {

	/** Dove si trova la bocca, in frazioni dell'immagine, se non indicato altrimenti. */
	private static final double BOCCA_X = 0.5;
	private static final double BOCCA_Y = 0.15;

	private final String id;
	private final ImmagineIntermezzo immagine;
	private StatoElemento statoIniziale;
	private final List<Tappa> tappe = new ArrayList<>();
	private Ripetizione ripetizione = Ripetizione.UNA_VOLTA;
	// Da che parte guarda l'immagine originale, se il verso segue il movimento; null altrimenti
	private Verso versoDellImmagine;
	private double boccaX = BOCCA_X;
	private double boccaY = BOCCA_Y;

	private ElementoIntermezzo(String id, ImmagineIntermezzo immagine, double x, double y) {
		if (id == null || id.isEmpty()) {
			throw new IllegalArgumentException("Un elemento di intermezzo deve avere un identificativo");
		}
		this.id = id;
		this.immagine = immagine;
		this.statoIniziale = new StatoElemento(x, y, 1, 1, false);
	}

	/**
	 * @param x frazione della larghezza dello schermo a cui cade il centro dell'immagine
	 * @param y frazione dell'altezza dello schermo a cui cade il centro dell'immagine
	 */
	public static ElementoIntermezzo di(String id, ImmagineIntermezzo immagine, double x, double y) {
		return new ElementoIntermezzo(id, immagine, x, y);
	}

	/** Scorciatoia per l'immagine di una classe di personaggio. */
	public static ElementoIntermezzo personaggio(String id, ClassePersonaggio classePersonaggio, double x, double y) {
		return new ElementoIntermezzo(id, ImmagineIntermezzo.personaggio(classePersonaggio), x, y);
	}

	public ElementoIntermezzo conScala(double scala) {
		statoIniziale = new StatoElemento(statoIniziale.getX(), statoIniziale.getY(), scala, statoIniziale.getOpacita(),
				statoIniziale.isSpecchiato());
		return this;
	}

	public ElementoIntermezzo conOpacita(double opacita) {
		statoIniziale = new StatoElemento(statoIniziale.getX(), statoIniziale.getY(), statoIniziale.getScala(), opacita,
				statoIniziale.isSpecchiato());
		return this;
	}

	/**
	 * Parte con l'immagine rovesciata orizzontalmente, per esempio per far guardare un
	 * personaggio dalla parte opposta; le tappe possono poi cambiarlo.
	 */
	public ElementoIntermezzo specchiato() {
		statoIniziale = statoIniziale.conSpecchiato(true);
		return this;
	}

	/**
	 * Durante gli spostamenti orizzontali l'elemento guarda nel verso in cui si muove.
	 *
	 * @param versoDellImmagine da che parte guarda l'immagine così com'è disegnata
	 */
	public ElementoIntermezzo orientaNelVersoDelMoto(Verso versoDellImmagine) {
		this.versoDellImmagine = versoDellImmagine;
		return this;
	}

	/**
	 * Il punto verso cui puntano i fumetti di questo elemento, in frazioni dell'immagine
	 * (0,0 = angolo in alto a sinistra); per default in alto al centro.
	 */
	public ElementoIntermezzo conBocca(double x, double y) {
		boccaX = x;
		boccaY = y;
		return this;
	}

	public ElementoIntermezzo poi(Tappa tappa) {
		tappe.add(tappa);
		return this;
	}

	/** Una tappa in cui l'elemento resta fermo. */
	public ElementoIntermezzo attendi(double secondi) {
		return poi(Tappa.inSecondi(secondi));
	}

	public ElementoIntermezzo ripeti(Ripetizione ripetizione) {
		this.ripetizione = ripetizione;
		return this;
	}

	public String getId() {
		return id;
	}

	public ImmagineIntermezzo getImmagine() {
		return immagine;
	}

	public double getBoccaX() {
		return boccaX;
	}

	public double getBoccaY() {
		return boccaY;
	}

	/**
	 * Quanto dura l'animazione: la somma delle tappe se si ferma sull'ultima, infinito
	 * se si ripete (in quel caso non determina la durata della pagina).
	 */
	public double getDurata() {
		double durataCiclo = getDurataCiclo();
		if (ripetizione == Ripetizione.UNA_VOLTA || durataCiclo == 0) {
			return durataCiclo;
		}
		return Double.POSITIVE_INFINITY;
	}

	private double getDurataCiclo() {
		double durata = 0;
		for (Tappa tappa : tappe) {
			durata += tappa.getSecondi();
		}
		return durata;
	}

	/**
	 * Come appare l'elemento a un certo numero di secondi dall'inizio della pagina.
	 */
	public StatoElemento getStatoAl(double secondi) {
		double durataCiclo = getDurataCiclo();
		if (tappe.isEmpty()) {
			return statoIniziale;
		}
		// Anche all'istante zero si passa dalla prima tappa, che ne stabilisce il verso
		secondi = Math.max(0, secondi);
		if (durataCiclo == 0) {
			return statoFinale();
		}
		double tempo;
		// Nella seconda metà di un avanti e indietro le tappe si percorrono al contrario
		boolean alRitorno = false;
		switch (ripetizione) {
			case CICLICA:
				tempo = secondi % durataCiclo;
				break;
			case AVANTI_E_INDIETRO:
				double nelDoppioCiclo = secondi % (2 * durataCiclo);
				alRitorno = nelDoppioCiclo > durataCiclo;
				tempo = alRitorno ? 2 * durataCiclo - nelDoppioCiclo : nelDoppioCiclo;
				break;
			default:
				tempo = Math.min(secondi, durataCiclo);
		}
		StatoElemento partenza = statoIniziale;
		for (Tappa tappa : tappe) {
			StatoElemento arrivo = tappa.arrivoDa(partenza);
			if (tempo <= tappa.getSecondi()) {
				double frazione = tappa.getSecondi() == 0 ? 1 : tempo / tappa.getSecondi();
				return orienta(partenza.interpola(arrivo, frazione), partenza, arrivo, alRitorno);
			}
			tempo -= tappa.getSecondi();
			partenza = arrivo;
		}
		return partenza;
	}

	/**
	 * Se il verso segue il movimento e nel tratto corrente l'elemento si sposta in
	 * orizzontale, lo fa guardare dalla parte in cui va; altrimenti vale il verso delle tappe.
	 */
	private StatoElemento orienta(StatoElemento stato, StatoElemento partenza, StatoElemento arrivo, boolean alRitorno) {
		if (versoDellImmagine == null || arrivo.getX() == partenza.getX()) {
			return stato;
		}
		boolean versoDestra = (arrivo.getX() > partenza.getX()) != alRitorno;
		Verso versoDelMoto = versoDestra ? Verso.DESTRA : Verso.SINISTRA;
		return stato.conSpecchiato(versoDelMoto != versoDellImmagine);
	}

	private StatoElemento statoFinale() {
		StatoElemento stato = statoIniziale;
		for (Tappa tappa : tappe) {
			stato = tappa.arrivoDa(stato);
		}
		return stato;
	}
}
