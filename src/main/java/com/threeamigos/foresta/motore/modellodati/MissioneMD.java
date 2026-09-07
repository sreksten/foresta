package com.threeamigos.foresta.motore.modellodati;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

public class MissioneMD implements Serializzabile {

	private String id = UUID.randomUUID().toString();
	private String nome;
	private String descrizione;
	private boolean descrizioneVisibile = true;
	private final Map<String, String> proprieta = new HashMap<>();
	private final List<MissioneMD> missioniSecondarie = new ArrayList<>();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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
		proprieta.clear();
		missioniSecondarie.clear();
	}

	public void aggiungiProprieta(String nome, String valore) {
		proprieta.put(nome, valore);
	}

	public String ottieniProprieta(String nome) {
		return proprieta.get(nome);
	}

	public void rimuoviProprieta(String nome) {
		proprieta.remove(nome);
	}

	public boolean aggiungiMissioneMD(MissioneMD missioneMD) {
		if (missioniSecondarie.stream().noneMatch(m -> m.getId().equals(missioneMD.getId()))) {
			missioniSecondarie.add(missioneMD);
			return true;
		}
		return false;
	}

	public void rimuoviMissioneMD(MissioneMD missioneMD) {
		missioniSecondarie.removeIf(m -> m.getId().equals(missioneMD.getId()));
	}

	public Collection<MissioneMD> getMissioniMD() {
		return missioniSecondarie;
	}

	@Override
	public void salva(PrintWriter stream) throws IOException {
		stream.print(id);
		stream.print(PIPE);
		stream.print((nome == null || nome.isEmpty()) ? "-" : nome);
		stream.print(PIPE);
		stream.print((descrizione == null || descrizione.isEmpty()) ? "-" : descrizione);
		stream.print(PIPE);
		stream.print(descrizioneVisibile);
		stream.print(PIPE);
		stream.println(missioniSecondarie.size());
		stream.println(proprieta.entrySet().stream().map(e -> e.getKey() + ":" + e.getValue()).collect(Collectors.joining(PIPE)));
		for (MissioneMD missioneMD : missioniSecondarie) {
			missioneMD.salva(stream);
		}
	}

	@Override
	public void leggi(BufferedReader stream) throws IOException {
		String line = stream.readLine();
		String[] tokens = line.split("\\|", -1);
		id = tokens[0];
		nome = tokens[1];
		descrizione = tokens[2];
		descrizioneVisibile = Boolean.parseBoolean(tokens[3]);
		int dimensioneElencoMissioniSecondarie = Integer.parseInt(tokens[4]);
		proprieta.clear();
		line = stream.readLine();
		StringTokenizer st = new StringTokenizer(line, PIPE);
		while (st.hasMoreTokens()) {
			tokens = st.nextToken().split(":");
			proprieta.put(tokens[0], tokens[1]);
		}
		missioniSecondarie.clear();
		for (int i = 0; i < dimensioneElencoMissioniSecondarie; i++) {
			MissioneMD missioneSecondaria = new MissioneMD();
			missioneSecondaria.leggi(stream);
			missioniSecondarie.add(missioneSecondaria);
		}
	}
}
