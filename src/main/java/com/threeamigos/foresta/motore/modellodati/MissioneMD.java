package com.threeamigos.foresta.motore.modellodati;

import com.threeamigos.foresta.missioni.ClasseMissione;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class MissioneMD implements Serializzabile {

	private String id = UUID.randomUUID().toString();
	private ClasseMissione classe = ClasseMissione.MISSIONE_SECONDARIA;
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

	public ClasseMissione getClasse() {
		return classe;
	}

	public void setClasse(ClasseMissione classe) {
		this.classe = classe;
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
		if (missioneMD == this) {
			// Un ciclo renderebbe infinite le ricorsioni di salva() e di ricostruzione
			return false;
		}
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
		stream.print(classe.name());
		stream.print(PIPE);
		stream.print(nome == null ? "" : nome);
		stream.print(PIPE);
		stream.print(descrizione == null ? "" : descrizione);
		stream.print(PIPE);
		stream.print(descrizioneVisibile);
		stream.print(PIPE);
		stream.println(missioniSecondarie.size());
		stream.println(MappaProprieta.salva(proprieta));
		for (MissioneMD missioneMD : missioniSecondarie) {
			missioneMD.salva(stream);
		}
	}

	@Override
	public void leggi(BufferedReader stream) throws IOException {
		String line = stream.readLine();
		String[] tokens = line.split("\\|", -1);
		id = tokens[0];
		classe = ClasseMissione.valueOf(tokens[1]);
		nome = tokens[2];
		descrizione = tokens[3];
		descrizioneVisibile = Boolean.parseBoolean(tokens[4]);
		int dimensioneElencoMissioniSecondarie = Integer.parseInt(tokens[5]);
		line = stream.readLine();
		tokens = line.split("\\|", -1);
		MappaProprieta.leggi(tokens, 0, proprieta);
		missioniSecondarie.clear();
		for (int i = 0; i < dimensioneElencoMissioniSecondarie; i++) {
			MissioneMD missioneSecondaria = new MissioneMD();
			missioneSecondaria.leggi(stream);
			missioniSecondarie.add(missioneSecondaria);
		}
	}
}
