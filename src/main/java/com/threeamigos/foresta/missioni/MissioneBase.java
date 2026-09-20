package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.EventoAggiornamentoStatoMissione;
import com.threeamigos.foresta.motore.GestoreProgressione;
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

	protected MissioneMD md;
	protected List<Missione> missioniSecondarie;

	protected MissioneBase(ClasseMissione classe) {
		md = new MissioneMD();
		md.setClasse(classe);
		missioniSecondarie = new ArrayList<>();
	}

	@Override
	public String getId() {
		return md.getId();
	}

	@Override
	public MissioneMD getModelloDati() {
		return md;
	}

	@Override
	public void setModelloDati(MissioneMD modelloDati) {
		this.md = modelloDati;
		// La struttura dell'albero appartiene al modello dati: i figli creati dal
		// costruttore avrebbero identificativi diversi da quelli persistiti, e
		// tenerli in lista farebbe sparire senza errori i progressi salvati.
		missioniSecondarie.clear();
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
		BusEventi.pubblica(new EventoAggiornamentoStatoMissione(this, "NUOVA MISSIONE", getNome()));
	}

	@Override
	public boolean isCompleta() {
		return md.ottieniProprieta(COMPLETA) != null;
	}

	@Override
	public void completaMissione() {
		md.aggiungiProprieta(COMPLETA, "S");
		RegistroMissioni.completaMissione(this);
		BusEventi.pubblica(new EventoAggiornamentoStatoMissione(this, "MISSIONE COMPLETATA", getNome()));
		if (isPrimaria()) {
			GestoreProgressione.completaMissionePrincipale();
		} else {
			GestoreProgressione.completaMissioneSecondaria();
		}
		md.setDescrizioneVisibile(false);
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
	public void sostituisciMissioniSecondarie(List<Missione> missioni) {
		// Copia difensiva: la lista in ingresso può essere quella viva di questa
		// stessa missione, e lo svuotamento la azzererebbe.
		List<Missione> nuoveMissioni = new ArrayList<>(missioni);
		missioniSecondarie.clear();
		missioniSecondarie.addAll(nuoveMissioni);
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
