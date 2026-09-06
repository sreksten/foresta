package com.threeamigos.foresta.motore.modellodati;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class MissioneMD implements Serializzabile {

	private String id = UUID.randomUUID().toString();
	private String nome;
	private String descrizione;
	private boolean descrizioneVisibile;
	private final Map<String, String> map = new HashMap<>();
	private final List<MissioneMD> missioniSecondarie = new ArrayList<>();

	public String getId() {
		return id;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getDescrizione() {
		return descrizione;
	}

	public void setDescrizione(String descrizione) {
		this.descrizione = descrizione;
	}

	public boolean isDescrizioneVisibile() {
		return descrizioneVisibile;
	}

	public void setDescrizioneVisibile(boolean descrizioneVisibile) {
		this.descrizioneVisibile = descrizioneVisibile;
	}

	public void reimposta() {
		map.clear();
		missioniSecondarie.clear();
	}

	public void aggiungiProprieta(String nome, String valore) {
		map.put(nome, valore);
	}

	public String ottieniProprieta(String nome) {
		return map.get(nome);
	}

	public void rimuoviProprieta(String nome) {
		map.remove(nome);
	}

	public void aggiungiMissioneMD(MissioneMD missioneMD) {
		missioniSecondarie.add(missioneMD);
	}

	public void rimuoviMissioneMD(MissioneMD missioneMD) {
		missioniSecondarie.remove(missioneMD);
	}

	@Override
	public void salva(PrintWriter stream) throws IOException {
		stream.print(id);
		stream.print(PIPE);
		stream.print(nome == null ? "" : nome);
		stream.print(PIPE);
		stream.print(descrizione == null ? "" : descrizione);
		stream.print(PIPE);
		stream.print(descrizioneVisibile);
		stream.print(PIPE);
		stream.print(map.size());
		stream.print(PIPE);
		stream.println(missioniSecondarie.size());
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
		String line = stream.readLine();
		StringTokenizer st = new StringTokenizer(line, PIPE);
		id = st.nextToken();
		nome = st.nextToken();
		descrizione = st.nextToken();
		descrizioneVisibile = Boolean.parseBoolean(st.nextToken());
		int dimensioneElencoProprieta = Integer.parseInt(st.nextToken());
		int dimensioneElencoMissioniSecondarie = Integer.parseInt(st.nextToken());
		map.clear();
		for (int i = 0; i < dimensioneElencoProprieta; i++) {
			st = new StringTokenizer(stream.readLine(), PIPE);
			map.put(st.nextToken(), st.nextToken());
		}
		missioniSecondarie.clear();
		for (int i = 0; i < dimensioneElencoMissioniSecondarie; i++) {
			MissioneMD missioneSecondaria = new MissioneMD();
			missioneSecondaria.leggi(stream);
			missioniSecondarie.add(missioneSecondaria);
		}
	}
}
