package com.threeamigos.foresta.interfacce;

import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.personaggi.Personaggio;
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
	List<Personaggio> getOspiti();

	boolean isOspiteVulnerabile(Personaggio ospite);

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
	 * Quanto costa all'incantatore fondere gli artefatti sul banco di lavoro
	 */
	int costoFusione(Collection<Artefatto> banco);

	/**
	 * Gli artefatti nell'inventario comune del gruppo
	 */
	Collection<Artefatto> getInventario();

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
