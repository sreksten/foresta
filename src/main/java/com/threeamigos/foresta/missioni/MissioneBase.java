package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.motore.RegistroMissioni;
import com.threeamigos.foresta.motore.modellodati.MissioneMD;

import java.util.ArrayList;
import java.util.List;

public abstract class MissioneBase implements Missione {

	protected static final String NOME = "NOME";
	protected static final String DESCRIZIONE = "DESCRIZIONE";

	private static final String ATTIVA = "ATTIVA";
	private static final String COMPLETA = "COMPLETA";

	protected static final String AFFERMATIVO = "S";

	protected MissioneMD md = new MissioneMD();
	protected List<Missione> missioniSecondarie = new ArrayList<>();

	protected MissioneBase() {
		md = new MissioneMD();
		missioniSecondarie = new ArrayList<>();
	}

	@Override
	public String getId() {
		return md.getId();
	}

	protected void setId(RegistroMissioni.TipoMissionePredefinita tipoMissione) {
		md.setId(tipoMissione.name());
	}

	@Override
	public MissioneMD getModelloDati() {
		return md;
	}

	@Override
	public void setModelloDati(MissioneMD modelloDati) {
		this.md = modelloDati;
	}

	@Override
	public String getNome() {
		return md.getNome();
	}

	@Override
	public String getDescrizione() {
		return md.getDescrizione();
	}

	@Override
	public boolean isDescrizioneVisibile() {
		return md.isDescrizioneVisibile();
	}

	@Override
	public void mostraDescrizione() {
		md.setDescrizioneVisibile(true);
	}

	@Override
	public void nascondiDescrizione() {
		md.setDescrizioneVisibile(false);
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

	@Override
	public String ottieniProprieta(String nome) {
		return md.ottieniProprieta(nome);
	}

	@Override
	public void aggiungiProprieta(String nome, String valore) {
		md.aggiungiProprieta(nome, valore);
	}

	@Override
	public void rimuoviProprieta(String nome) {
		md.rimuoviProprieta(nome);
	}

	@Override
	public void aggiungiMissione(Missione missione) {
		if (md.aggiungiMissioneMD(missione.getModelloDati())) {
			missioniSecondarie.add(missione);
		}
	}

	@Override
	public void rimuoviMissione(Missione missione) {
		missioniSecondarie.remove(missione);
		md.rimuoviMissioneMD(missione.getModelloDati());
	}

	@Override
	public List<Missione> getMissioniSecondarie() {
		return missioniSecondarie;
	}

	@Override
	public String toString() {
		return getNome();
	}
}
