package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.locazioni.Locanda;
import com.threeamigos.foresta.motore.Foresta;
import com.threeamigos.foresta.motore.LineaTemporale;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;

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
	// Il nome della locanda, ricordato finche' la citta' esiste: distrutta la citta', la casella non lo sa piu'
	private static final String NOME_LOCANDA = "NOME_LOCANDA";

	public VisitaLocanda() {
		super(ClasseMissione.VISITA_LOCANDA);
	}

	public void setCitta(ClassiLocazione classeCitta) {
		if (classeCitta.getTipoLocazione() != ClassiLocazione.TipoLocazione.CITTA) {
			throw new IllegalArgumentException(classeCitta.name() + " non è una città");
		}
		aggiungiProprieta(CITTA, classeCitta.name());
	}

	@Override
	public String getNome() {
		return "Visita '" + getNomeLocanda() + '\'';
	}

	@Override
	public String getDescrizione() {
		return "Fatti servire almeno un boccale alla '" + getNomeLocanda() + "'.";
	}

	/**
	 * Il nome della locanda è pescato dal pool quando la città viene costruita,
	 * quindi qui va letto al momento (setCitta() viene chiamato quando la foresta
	 * non esiste ancora).
	 */
	private String getNomeLocanda() {
		String nome = ottieniProprieta(NOME_LOCANDA);
		if (nome != null) {
			return nome;
		}
		CoordinateMD coordinate = Foresta.getCoordinateLocazioneUnica(getClasseCitta());
		if (coordinate == null) {
			return "locanda perduta";
		}
		nome = Foresta.getLocazioneMD(coordinate).ottieniProprieta(Locanda.LOCANDA_NOME);
		if (nome != null) {
			aggiungiProprieta(NOME_LOCANDA, nome);
		}
		return nome;
	}

	@Override
	public void controllaPreLocazione() {
		if (!isAttiva()) {
			// Nessuna notifica: le quattro tappe si attivano tutte insieme con la
			// missione che le contiene, ed è quella ad annunciarsi
			getNomeLocanda();
			attivaMissione();
		} else if (!isCompleta() && getClasseCitta() != null && LineaTemporale.isCittaDistrutta(getClasseCitta())) {
			// Distrutta la citta', la sua locanda non c'e' piu'
			BusEventi.pubblica(new NotificaTestoParagrafo("La '" + getNomeLocanda() + "' è andata distrutta con la sua città: una tappa che non si potrà più fare."));
			fallisciMissione();
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
		// La casella della città si ricorda della bevuta, quindi non serve né
		// trovarsi lì né sapere quale istanza l'ha ospitata.
		CoordinateMD coordinate = Foresta.getCoordinateLocazioneUnica(classeCitta);
		if (coordinate != null && Foresta.getLocazioneMD(coordinate).ottieniProprieta(Locanda.LOCANDA_VISITATA) != null) {
			completaMissione();
			BusEventi.pubblica(new NotificaTestoParagrafo("Una tappa in meno: " + getNome() + " è cosa fatta."));
		}
	}

	private ClassiLocazione getClasseCitta() {
		String nomeClasseCitta = ottieniProprieta(CITTA);
		return nomeClasseCitta == null ? null : ClassiLocazione.valueOf(nomeClasseCitta);
	}
}
