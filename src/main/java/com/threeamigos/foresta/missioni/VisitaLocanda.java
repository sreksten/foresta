package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.locazioni.Citta;
import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.ui.UI;

/**
 * Sotto-missione di {@link CronacheDiUnFegatoEroico}: si conclude quando il gruppo
 * esce dalla locanda di una città precisa.
 * <p>
 * La città non è cablata nella classe ma vive fra le proprieta' del modello dati,
 * come fa {@link MuoviALocazione} con le coordinate: così una sola classe copre
 * tutte le locande, e dopo un caricamento la ricostruzione polimorfica ritrova la
 * città senza bisogno di un costruttore con argomenti.
 */
public class VisitaLocanda extends MissioneBase {

	private static final String CITTA = "CITTA";

	public VisitaLocanda() {
		super(ClasseMissione.VISITA_LOCANDA);
	}

	public void setCitta(ClassiLocazione classeCitta) {
		if (classeCitta.getTipoLocazione() != ClassiLocazione.TipoLocazione.CITTA) {
			throw new IllegalArgumentException(classeCitta.name() + " non è una città");
		}
		Citta citta = (Citta) classeCitta.getIstanza();
		aggiungiProprieta(CITTA, classeCitta.name());
		aggiungiProprieta(NOME, "Visita '" + citta.getNomeSempliceLocanda() + '\'');
		aggiungiProprieta(DESCRIZIONE, "Fatti servire almeno un boccale " + citta.getNomeLocanda() + '.');
	}

	@Override
	public String getNome() {
		return ottieniProprieta(NOME);
	}

	@Override
	public String getDescrizione() {
		return ottieniProprieta(DESCRIZIONE);
	}

	@Override
	public void controllaPreLocazione() {
		if (!isAttiva()) {
			// Nessuna notifica: le quattro tappe si attivano tutte insieme con la
			// missione che le contiene, ed è quella ad annunciarsi
			attivaMissione();
		}
	}

	@Override
	public void controllaInLocazione() {
		// La visita si verifica solo quando la locazione è conclusa
	}

	@Override
	public void controllaPostLocazione() {
		ClassiLocazione classeCitta = getClasseCitta();
		if (isCompleta() || classeCitta == null) {
			return;
		}
		GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
		// La condizione sulla città corrente non è ridondante: la locanda è un
		// singleton condiviso, e senza di essa la tappa di una città si chiuderebbe
		// per una bevuta fatta in un'altra.
		if (gruppo.isInLocazioneUnica(classeCitta) && ((Citta) classeCitta.getIstanza()).isLocandaVisitata()) {
			completaMissione();
		}
	}

	@Override
	public void completaMissione() {
		super.completaMissione();
		UI.notifica("Una tappa in meno: " + getNome() + " è cosa fatta.");
	}

	private ClassiLocazione getClasseCitta() {
		String nomeClasseCitta = ottieniProprieta(CITTA);
		return nomeClasseCitta == null ? null : ClassiLocazione.valueOf(nomeClasseCitta);
	}
}
