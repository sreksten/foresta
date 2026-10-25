package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.motore.EsperienzaDelGruppo;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoMissioneCompletata;
import com.threeamigos.foresta.eventi.notifiche.NotificaAggiornamentoStatoMissione;
import com.threeamigos.foresta.modellodati.MissioneMD;
import com.threeamigos.foresta.motore.GestoreProgressione;
import com.threeamigos.foresta.tipi.MissionePredefinita;

import java.util.ArrayList;
import java.util.List;

/**
 * La base delle missioni scritte a mano: tiene i dati nel {@code MissioneMD} (nome, descrizione, proprietà, missioni figlie) e implementa lo stato di una missione
 * (attiva, completa, fallita) e l'albero delle missioni secondarie. Una missione concreta dice cosa succede prima, durante e dopo una locazione
 * ({@code controllaPreLocazione}, {@code controllaInLocazione}, {@code controllaPostLocazione}). Le missioni fatte di passi estendono invece
 * {@link MissioneAPassi}.
 */
public abstract class MissioneBase implements Missione {

	protected static final String NOME = "NOME";
	protected static final String DESCRIZIONE = "DESCRIZIONE";

	private static final String ATTIVA = "ATTIVA";
	private static final String COMPLETA = "COMPLETA";
	private static final String FALLITA = "FALLITA";

	protected static final String AFFERMATIVO = "S";

	protected MissioneMD md;
	protected List<Missione> missioniSecondarie;

	protected MissioneBase(MissionePredefinita classe) {
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
		if (isDiClassePredefinita()) {
			BusEventi.pubblica(new NotificaAggiornamentoStatoMissione(this, "NUOVA MISSIONE", getNome()));
		}
	}

	@Override
	public boolean isCompleta() {
		return md.ottieniProprieta(COMPLETA) != null;
	}

	/**
	 * Se la missione è di una classe predefinita, anche se è una copia con un altro id (le missioni ripetibili): è quella
	 * di cui si annunciano la partenza e la fine.
	 */
	protected final boolean isDiClassePredefinita() {
		return md.getClasse().isPredefinita();
	}

	/**
	 * Completa la missione una volta sola: una seconda chiamata non ripaga né riannuncia niente.
	 */
	@Override
	public void completaMissione() {
		if (isCompleta()) {
			return;
		}
		md.aggiungiProprieta(COMPLETA, "S");
		RegistroMissioni.completaMissione(this);
		BusEventi.pubblica(new NotificaAggiornamentoStatoMissione(this, "MISSIONE COMPLETATA", getNome()));
		BusEventi.pubblica(new InternoMissioneCompletata(this));
		if (isPrimaria()) {
			EsperienzaDelGruppo.completaMissionePrincipale();
		} else {
			EsperienzaDelGruppo.completaMissioneSecondaria();
		}
		md.setDescrizioneVisibile(false);
	}

	@Override
	public boolean isFallita() {
		return md.ottieniProprieta(FALLITA) != null;
	}

	@Override
	public void fallisciMissione() {
		if (isCompleta() || isFallita()) {
			return;
		}
		md.aggiungiProprieta(FALLITA, AFFERMATIVO);
		RegistroMissioni.fallisciMissione(this);
		if (isDiClassePredefinita()) {
			BusEventi.pubblica(new NotificaAggiornamentoStatoMissione(this, "MISSIONE FALLITA", getNome()));
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
