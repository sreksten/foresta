package com.threeamigos.foresta.ui.tutorial;

import com.threeamigos.foresta.ui.sprite.DescrizioneFumetto;

/**
 * L'intestazione del fumetto del tutorial, senza toolkit: in cima, le icone (la tessera che quel luogo ha sulla mappa) in fila e il titolo accanto;
 * il testo scende sotto di lei. Ogni toolkit misura le sue immagini e il suo titolo, e qui si fanno i conti: quanto è grande e dove va ogni pezzo,
 * rispetto all'angolo in alto a sinistra del testo del fumetto.
 *
 * @author Stefano Reksten
 */
public final class IntestazioneDelFumetto {

	/** Fra un'icona e l'altra, e fra l'ultima icona e il titolo */
	public static final int SPAZIO_FRA_ICONA_E_TITOLO = 8;
	/** Fra la riga dell'intestazione e il testo */
	public static final int SPAZIO_SOTTO_L_INTESTAZIONE = 4;

	private final int[] larghezzeDelleIcone;
	private final int[] altezzeDelleIcone;
	private final boolean conTitolo;
	private final int altezzaDelTitolo;
	private final int larghezza;
	private final int altezzaDellaRiga;

	/**
	 * @param larghezzeDelleIcone le larghezze delle icone, in ordine (anche nessuna)
	 * @param altezzeDelleIcone   le loro altezze, nello stesso ordine
	 * @param conTitolo           se c'è il titolo
	 * @param larghezzaDelTitolo  la larghezza del titolo scritto (se c'è)
	 * @param altezzaDelTitolo    l'altezza del font del titolo (se c'è)
	 */
	public IntestazioneDelFumetto(int[] larghezzeDelleIcone, int[] altezzeDelleIcone, boolean conTitolo, int larghezzaDelTitolo, int altezzaDelTitolo) {
		if (larghezzeDelleIcone.length != altezzeDelleIcone.length) {
			throw new IllegalArgumentException("Larghezze e altezze delle icone non sono tante quante");
		}
		this.larghezzeDelleIcone = larghezzeDelleIcone.clone();
		this.altezzeDelleIcone = altezzeDelleIcone.clone();
		this.conTitolo = conTitolo;
		this.altezzaDelTitolo = conTitolo ? altezzaDelTitolo : 0;
		int larghezzaIcone = 0;
		int altezzaIcone = 0;
		for (int i = 0; i < larghezzeDelleIcone.length; i++) {
			larghezzaIcone += larghezzeDelleIcone[i] + (i > 0 ? SPAZIO_FRA_ICONA_E_TITOLO : 0);
			altezzaIcone = Math.max(altezzaIcone, altezzeDelleIcone[i]);
		}
		boolean conIcone = larghezzeDelleIcone.length > 0;
		this.larghezza = larghezzaIcone + (conIcone && conTitolo ? SPAZIO_FRA_ICONA_E_TITOLO : 0) + (conTitolo ? Math.max(1, larghezzaDelTitolo) : 0);
		this.altezzaDellaRiga = Math.max(altezzaIcone, this.altezzaDelTitolo);
	}

	/**
	 * Se c'è (almeno un'icona o il titolo).
	 */
	public boolean isPresente() {
		return larghezzeDelleIcone.length > 0 || conTitolo;
	}

	/**
	 * La larghezza della riga (zero se non c'è).
	 */
	public int getLarghezza() {
		return larghezza;
	}

	/**
	 * Quanto spazio prende in alto, compreso quello che la separa dal testo (zero se non c'è).
	 */
	public int getAltezza() {
		return isPresente() ? altezzaDellaRiga + SPAZIO_SOTTO_L_INTESTAZIONE : 0;
	}

	/**
	 * La x dell'icona di quella posizione, dall'angolo del testo del fumetto.
	 */
	public int getXDellIcona(int indice) {
		int x = DescrizioneFumetto.SMUSSAMENTO_BORDO;
		for (int i = 0; i < indice; i++) {
			x += larghezzeDelleIcone[i] + SPAZIO_FRA_ICONA_E_TITOLO;
		}
		return x;
	}

	/**
	 * La y dell'icona di quella posizione (centrata sulla riga), dall'angolo del testo del fumetto.
	 */
	public int getYDellIcona(int indice) {
		return DescrizioneFumetto.SMUSSAMENTO_BORDO + (altezzaDellaRiga - altezzeDelleIcone[indice]) / 2;
	}

	/**
	 * La x del titolo, dopo tutte le icone.
	 */
	public int getXDelTitolo() {
		return getXDellIcona(larghezzeDelleIcone.length);
	}

	/**
	 * La y del titolo (centrato sulla riga).
	 */
	public int getYDelTitolo() {
		return DescrizioneFumetto.SMUSSAMENTO_BORDO + (altezzaDellaRiga - altezzaDelTitolo) / 2;
	}
}
