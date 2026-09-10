package com.threeamigos.foresta.motore.modellodati;

import com.threeamigos.foresta.motore.RegistroMissioni;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class RegistroMissioniMD implements Serializzabile {

	private final Map<String, MissioneMD> missioniAttive = new HashMap<>();
	private final Map<String, MissioneMD> missioniCompletate = new HashMap<>();

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

	public MissioneMD getMissioneAttiva(String id) {
		return missioniAttive.get(id);
	}

	public Collection<MissioneMD> getMissioniAttive() {
		return missioniAttive.values();
	}

	public Collection<MissioneMD> getMissioniCompletate() {
		return missioniCompletate.values();
	}

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

	@Override
	public void leggi(BufferedReader stream) throws IOException {
		leggiImpl(stream, missioniAttive);
		leggiImpl(stream, missioniCompletate);
		RegistroMissioni.aggiornaDopoRilettura();
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
