package com.threeamigos.foresta.missioni;

import java.util.ArrayList;
import java.util.List;

import com.threeamigos.foresta.motore.modellodati.MissioneMD;

public abstract class MissioneBase implements Missione {

	protected static final String NOME = "NOME";
	protected static final String DESCRIZIONE = "DESCRIZIONE";

	private static final String ATTIVA = "ATTIVA";
	private static final String COMPLETA = "COMPLETA";

	protected static final String AFFERMATIVO = "S";

	protected MissioneMD md = new MissioneMD();
	protected List<Missione> missioniSecondarie = new ArrayList<>();

	@Override
	public MissioneMD getModelloDati() {
		return md;
	}

	@Override
	public void setModelloDati(MissioneMD modelloDati) {
		this.md = modelloDati;
	}

	@Override
	public boolean isAttiva() {
		return md.ottieniProprieta(ATTIVA) != null;
	}

	@Override
	public void attivaMissione() {
		md.aggiungiProprieta(ATTIVA, "S");
	}

	@Override
	public boolean isCompleta() {
		return md.ottieniProprieta(COMPLETA) != null;
	}

	@Override
	public void completaMissione() {
		md.aggiungiProprieta(COMPLETA, "S");
	}

	@Override
	public boolean isPrimaria() {
		return false;
	}

	public String ottieniProprieta(String nome) {
		return md.ottieniProprieta(nome);
	}

	public void aggiungiProprieta(String nome, String valore) {
		md.aggiungiProprieta(nome, valore);
	}

	public void rimuoviProprieta(String nome) {
		md.rimuoviProprieta(nome);
	}

	public void aggiungiMissione(Missione missione) {
		missioniSecondarie.add(missione);
		md.aggiungiMissioneMD(missione.getModelloDati());
	}

	public void rimuoviMissione(Missione missione) {
		missioniSecondarie.remove(missione);
		md.rimuoviMissioneMD(missione.getModelloDati());
	}
}
