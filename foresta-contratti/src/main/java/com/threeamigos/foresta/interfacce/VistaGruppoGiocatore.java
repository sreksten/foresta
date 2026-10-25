package com.threeamigos.foresta.interfacce;

import com.threeamigos.foresta.artefatti.VistaArtefatto;
import com.threeamigos.foresta.artefatti.VistaPezzoDelSet;
import com.threeamigos.foresta.mappa.CoordinateMD;
import com.threeamigos.foresta.tipi.TipoBottino;
import com.threeamigos.foresta.tipi.TipoIncantesimo;
import com.threeamigos.foresta.mappa.TipoLocazione;

import java.util.Collection;
import java.util.List;

/**
 * Il gruppo del giocatore visto dalla UI, in sola lettura (vedi VistaPartita). Lo implementa GruppoGiocatore.
 */
public interface VistaGruppoGiocatore extends VistaGruppo {

	/**
	 * I personaggi temporanei che il gruppo scorta
	 */
	List<? extends VistaPersonaggio> getOspiti();

	boolean isOspiteVulnerabile(VistaPersonaggio ospite);

	int getMonete();

	int getPreziosi();

	/**
	 * Gli artefatti nell'inventario comune del gruppo
	 */
	Collection<? extends VistaArtefatto> getInventario();

	/**
	 * I pezzi del set leggendario dell'artefatto e dove stanno: indossati da chi indossa l'artefatto, del gruppo o da
	 * trovare. Vuoto se non è un pezzo di un set.
	 */
	List<? extends VistaPezzoDelSet> getPezziDelSet(VistaArtefatto artefatto);

	int getIncantesimi(TipoIncantesimo tipoIncantesimo);

	int getPozioniSalute();

	int getPozioniSaluteGrande();

	int getPozioniMagia();

	int getPozioniMagiaGrande();

	int getPozioniAumentoLivello();

	CoordinateMD getCoordinate();

	TipoLocazione getTipoLocazioneCorrente();

}
