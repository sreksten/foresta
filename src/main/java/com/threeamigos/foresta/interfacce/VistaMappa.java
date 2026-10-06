package com.threeamigos.foresta.interfacce;

import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.tipi.TipoLocazione;

import java.util.List;

/**
 * La mappa della Foresta vista dalla UI, in sola lettura (vedi VistaPartita).
 */
public interface VistaMappa {

	int getDimensioneX();

	int getDimensioneY();

	TipoLocazione getLocazione(CoordinateMD coordinate);

	/**
	 * Quale delle immagini alternative del bosco usare sulla mappa per la casella (vedi Bosco.getVarianteMappa)
	 */
	int getVarianteBosco(CoordinateMD coordinate);

	boolean isLocazioneConosciuta(CoordinateMD coordinate);

	boolean isLocazioneVisitata(CoordinateMD coordinate);

	/**
	 * Le caselle che lampeggiano sulla mappa perché c'entrano con una missione attiva
	 */
	List<CoordinateMD> getCoordinateDaSegnalare();

	/**
	 * Cresce a ogni cambiamento della mappa conosciuta: chi ne tiene un'immagine la ricostruisce quando cambia
	 */
	int getVersioneMappa();

	int getMinXConosciuta();

	int getMaxXConosciuta();

	int getMinYConosciuta();

	int getMaxYConosciuta();

	/**
	 * Il nome della casella da mostrare sulla mappa, se il gruppo la conosce e ha un nome; altrimenti null
	 */
	String getNomeDaMostrare(CoordinateMD coordinate);

	/**
	 * Il nome della missione per cui la casella lampeggia, se il gruppo la conosce; altrimenti null
	 */
	String getNomeMissioneDaMostrare(CoordinateMD coordinate);

}
