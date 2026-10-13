package com.threeamigos.foresta.modellodati;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Le missioni della partita, ciascuna con il proprio identificativo: quelle attive e quelle completate.
 * <p>
 * Sono dati puri: le regole che le fanno avanzare e che le spostano da un elenco all'altro stanno nel motore
 * (vedi gestione_missioni.md).
 *
 * @author Stefano Reksten
 */
public class RegistroMissioniMD implements Serializzabile {

	private final Map<String, MissioneMD> missioniAttive = new HashMap<>();
	private final Map<String, MissioneMD> missioniCompletate = new HashMap<>();

	/**
	 * Dimentica tutte le missioni, attive e completate.
	 */
	public void reimposta() {
		missioniAttive.clear();
		missioniCompletate.clear();
	}

	/**
	 * Per le missioni predefinite che hanno un ID noto (il valore nell'enumerato)
	 */
	public void aggiungiMissione(String id, MissioneMD missione) {
		missioniAttive.put(id, missione);
	}

	/**
	 * @return la missione attiva con quell'identificativo, o null se non c'è
	 */
	public MissioneMD getMissioneAttiva(String id) {
		return missioniAttive.get(id);
	}

	/**
	 * Le missioni attive.
	 */
	public Collection<MissioneMD> getMissioniAttive() {
		return missioniAttive.values();
	}

	/**
	 * Le missioni completate.
	 */
	public Collection<MissioneMD> getMissioniCompletate() {
		return missioniCompletate.values();
	}

	/**
	 * Scrive le missioni attive e poi le completate, ciascun elenco preceduto dal numero di missioni.
	 */
	@Override
	public void salva(PrintWriter stream) throws IOException {
		salvaImpl(stream, missioniAttive);
		salvaImpl(stream, missioniCompletate);
	}

	private void salvaImpl(PrintWriter stream, Map<String, MissioneMD> missioni) throws IOException {
		stream.println(missioni.size());
		for (Map.Entry<String, MissioneMD> entry : missioni.entrySet()) {
			entry.getValue().salva(stream);
		}
	}

	/**
	 * Rilegge ciò che scrive {@link #salva(PrintWriter)}, sostituendo le missioni ricordate finora.
	 */
	@Override
	public void leggi(BufferedReader stream) throws IOException {
		leggiImpl(stream, missioniAttive);
		leggiImpl(stream, missioniCompletate);
	}

	private void leggiImpl(BufferedReader stream, Map<String, MissioneMD> missioni) throws IOException {
		missioni.clear();
		final int numeroMissioni = Integer.parseInt(stream.readLine());
		for (int i = 0; i < numeroMissioni; i++) {
			MissioneMD missioneMD = new MissioneMD();
			missioneMD.leggi(stream);
			missioni.put(missioneMD.getId(), missioneMD);
		}
	}
}
