package com.threeamigos.foresta.modellodati;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Le missioni della partita, ciascuna con il proprio identificativo: le principali e le secondarie di primo livello, con
 * le loro sotto-missioni. Una missione completata resta qui, con la sua proprietà COMPLETA: è il motore a distinguerle
 * (RegistroMissioni.getMissioniCompletate).
 * <p>
 * Sono dati puri: le regole che le fanno avanzare stanno nel motore (vedi gestione_missioni.md).
 *
 * @author Stefano Reksten
 */
public class RegistroMissioniMD implements Serializzabile {

	private final Map<String, MissioneMD> missioni = new HashMap<>();

	/**
	 * Dimentica tutte le missioni.
	 */
	public void reimposta() {
		missioni.clear();
	}

	/**
	 * Aggiunge la missione con quell'identificativo (per le predefinite è il nome della costante di MissionePredefinita).
	 */
	public void aggiungiMissione(String id, MissioneMD missione) {
		missioni.put(id, missione);
	}

	/**
	 * @return la missione con quell'identificativo, o null se non c'è
	 */
	public MissioneMD getMissione(String id) {
		return missioni.get(id);
	}

	/**
	 * Le missioni, di sola lettura.
	 */
	public Collection<MissioneMD> getMissioni() {
		return Collections.unmodifiableCollection(missioni.values());
	}

	/**
	 * Scrive il numero delle missioni e poi le missioni, una dopo l'altra.
	 */
	@Override
	public void salva(PrintWriter stream) throws IOException {
		stream.println(missioni.size());
		for (MissioneMD missione : missioni.values()) {
			missione.salva(stream);
		}
	}

	/**
	 * Rilegge ciò che scrive {@link #salva(PrintWriter)}, sostituendo le missioni ricordate finora.
	 */
	@Override
	public void leggi(BufferedReader stream) throws IOException {
		missioni.clear();
		int numeroMissioni = LettoreCampi.leggiIntero(stream);
		for (int i = 0; i < numeroMissioni; i++) {
			MissioneMD missione = new MissioneMD();
			missione.leggi(stream);
			missioni.put(missione.getId(), missione);
		}
	}
}
