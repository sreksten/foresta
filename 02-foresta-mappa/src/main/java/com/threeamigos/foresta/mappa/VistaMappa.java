package com.threeamigos.foresta.mappa;


import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.tipi.TipoLocazione;
import java.util.List;

/**
 * La mappa della Foresta vista dalla UI, in sola lettura (vedi VistaPartita).
 */
public interface VistaMappa {

	/**
	 * Quante caselle ha la mappa in orizzontale.
	 */
	int getDimensioneX();

	/**
	 * Quante caselle ha la mappa in verticale.
	 */
	int getDimensioneY();

	/**
	 * Il tipo di locazione di quella casella.
	 */
	TipoLocazione getLocazione(CoordinateMD coordinate);

	/**
	 * Quale delle immagini alternative del bosco usare sulla mappa per la casella (vedi Bosco.getVarianteMappa)
	 */
	int getVarianteBosco(CoordinateMD coordinate);

	/**
	 * Se il gruppo sa cosa c'è in quella casella.
	 */
	boolean isLocazioneConosciuta(CoordinateMD coordinate);

	/**
	 * Se il gruppo è già passato da quella casella.
	 */
	boolean isLocazioneVisitata(CoordinateMD coordinate);

	/**
	 * Le caselle che lampeggiano sulla mappa perché c'entrano con una missione attiva
	 */
	List<CoordinateMD> getCoordinateDaSegnalare();

	/**
	 * Cresce a ogni cambiamento della mappa conosciuta: chi ne tiene un'immagine la ricostruisce quando cambia
	 */
	int getVersioneMappa();

	/**
	 * La colonna più a sinistra con una casella conosciuta.
	 */
	int getMinXConosciuta();

	/**
	 * La colonna più a destra con una casella conosciuta.
	 */
	int getMaxXConosciuta();

	/**
	 * La riga più in alto con una casella conosciuta.
	 */
	int getMinYConosciuta();

	/**
	 * La riga più in basso con una casella conosciuta.
	 */
	int getMaxYConosciuta();

	/**
	 * Il nome della casella da mostrare sulla mappa, se il gruppo la conosce e ha un nome; altrimenti null
	 */
	String getNomeDaMostrare(CoordinateMD coordinate);

	/**
	 * I nomi delle missioni per cui la casella lampeggia (più di una, se più missioni ci mandano il gruppo), se il
	 * gruppo la conosce; altrimenti nessuno
	 */
	List<String> getNomiMissioniDaMostrare(CoordinateMD coordinate);

	/**
	 * La casella su cui si trova il gruppo del giocatore.
	 */
	CoordinateMD getPosizioneDelGruppo();

	/**
	 * Il tipo di locazione su cui si trova il gruppo del giocatore.
	 */
	TipoLocazione getLocazioneDelGruppo();

}
