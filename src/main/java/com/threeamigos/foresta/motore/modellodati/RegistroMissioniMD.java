package com.threeamigos.foresta.motore.modellodati;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import com.threeamigos.foresta.motore.RegistroMissioni;

public class RegistroMissioniMD implements Serializzabile {

	private Map<String, MissioneMD> missioni = new HashMap<>();

	public void reimposta() {
		missioni.clear();
	}

	public void aggiungiMissione(MissioneMD missione) {
		missioni.put(missione.getId(), missione);
	}

	public MissioneMD getMissione(String id) {
		return missioni.get(id);
	}

	@Override
	public void salva(PrintWriter stream) throws IOException {
		stream.println(missioni.size());
		for (Map.Entry<String, MissioneMD> entry : missioni.entrySet()) {
			entry.getValue().salva(stream);
		}
	}

	@Override
	public void leggi(BufferedReader stream) throws IOException {
		missioni.clear();
		final int numeroMissioni = Integer.parseInt(stream.readLine());
		for (int i = 0; i < numeroMissioni; i++) {
			MissioneMD missioneMD = new MissioneMD();
			missioneMD.leggi(stream);
			missioni.put(missioneMD.getId(), missioneMD);
		}
		RegistroMissioni.aggiornaDopoRilettura();
	}
}
