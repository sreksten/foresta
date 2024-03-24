package com.threeamigos.foresta.motore.modellodati;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class MissioneMD implements Serializzabile {

	private String id;
	private Map<String, String> map = new HashMap<>();
	private List<MissioneMD> missioniSecondarie = new ArrayList<>();

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void reimposta() {
		map.clear();
	}

	public void aggiungiProprieta(String nome, String valore) {
		map.put(nome, valore);
	}

	public String ottieniProprieta(String nome) {
		return map.get(nome);
	}

	public String rimuoviProprieta(String nome) {
		return map.remove(nome);
	}

	public void aggiungiMissioneMD(MissioneMD missioneMD) {
		missioniSecondarie.add(missioneMD);
	}

	public void rimuoviMissioneMD(MissioneMD missioneMD) {
		missioniSecondarie.remove(missioneMD);
	}

	@Override
	public void salva(PrintWriter stream) throws IOException {
		stream.println(id);
		stream.println(map.size());
		for (Map.Entry<String, String> property : map.entrySet()) {
			stream.print(property.getKey());
			stream.print(PIPE);
			stream.println(property.getValue());
		}
		stream.println(missioniSecondarie.size());
		for (MissioneMD missioneMD : missioniSecondarie) {
			missioneMD.salva(stream);
		}
	}

	@Override
	public void leggi(BufferedReader stream) throws IOException {
		id = stream.readLine();
		int dimensioneElencoProprieta = Integer.parseInt(stream.readLine());
		map.clear();
		for (int i = 0; i < dimensioneElencoProprieta; i++) {
			StringTokenizer st = new StringTokenizer(stream.readLine(), PIPE);
			map.put(st.nextToken(), st.nextToken());
		}
		int totaleMissioniSecondarie = Integer.parseInt(stream.readLine());
		for (int i = 0; i < totaleMissioniSecondarie; i++) {
			MissioneMD missioneSecondaria = new MissioneMD();
			missioneSecondaria.leggi(stream);
			missioniSecondarie.add(missioneSecondaria);
		}
	}
}
