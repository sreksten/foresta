package com.threeamigos.foresta.interfacce;

import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.tipi.ClasseIncantesimo;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoOggetto;

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
	 * Quanto paga il gruppo per un oggetto che costa {@code costo}, con lo sconto della contrattazione
	 */
	int prezzoAcquisto(int costo);

	/**
	 * Quanto incassa il gruppo vendendo un oggetto che costa {@code costo}
	 */
	int prezzoVendita(int costo);

	/**
	 * Gli artefatti nell'inventario comune del gruppo
	 */
	Collection<? extends VistaArtefatto> getInventario();

	/**
	 * I pezzi del set leggendario dell'artefatto e dove stanno: indossati da chi indossa l'artefatto, del gruppo o da
	 * trovare. Vuoto se non è un pezzo di un set.
	 */
	List<? extends VistaPezzoDelSet> getPezziDelSet(VistaArtefatto artefatto);

	int getIncantesimi(ClasseIncantesimo classeIncantesimo);

	int getPozioniSalute();

	int getPozioniSaluteGrande();

	int getPozioniMagia();

	int getPozioniMagiaGrande();

	CoordinateMD getCoordinate();

	TipoLocazione getTipoLocazioneCorrente();

	/**
	 * L'oggetto che si vede a terra nella locazione corrente, o null se non ce n'è
	 */
	TipoOggetto getTipoOggettoInLocazione();

}
