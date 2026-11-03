package com.threeamigos.foresta.missioni.principali;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.missioni.MissioneBase;
import com.threeamigos.foresta.gruppi.GruppoAvversario;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.MissionePredefinita;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import com.threeamigos.foresta.testi.ProduttoreDiMissioniCasuale;
import com.threeamigos.foresta.testi.Lingue;
import com.threeamigos.foresta.testi.Genere;
import com.threeamigos.foresta.testi.Testi;

/**
 * Dieci eremiti disturbati nel loro ritiro. Il conteggio avviene all'incontro, quando
 * il gruppo avversario è appena stato popolato e descritto: quel che succede dopo -
 * battaglia, amicizia, corruzione o fuga - non cambia il conto. Si contano gli eremiti, uno per uno, non i controlli: l'automa rifà
 * i controlli anche dopo la risposta a una domanda, e lo stesso eremita non deve contare due volte.
 */
public class DisturbatoreDellaQuietePubblica extends MissioneBase {

	private static final int EREMITI_DA_INCONTRARE = 10;

	// Si legge a ogni uso, non una volta sola: la lingua può cambiare fra una partita e l'altra
	private static String descrizioneBase() {
		return ProduttoreDiMissioniCasuale.testoDellaMissione("DISTURBATORE_DELLA_QUIETE_PUBBLICA_DESCRIZIONE_BASE", "PARAM_NUMERO", Lingue.corrente().cardinale(EREMITI_DA_INCONTRARE, Genere.MASCHILE));
	}

	// Gli identificativi degli eremiti già contati, separati da virgole
	private static final String EREMITI_CONTATI = "EREMITI_CONTATI";

	public DisturbatoreDellaQuietePubblica() {
		super(MissionePredefinita.DISTURBATORE_DELLA_QUIETE_PUBBLICA);
	}

	@Override
	protected String creaIlNome() {
		return ProduttoreDiMissioniCasuale.testoDellaMissione("DISTURBATORE_DELLA_QUIETE_PUBBLICA_TITOLO");
	}

	@Override
	public String getDescrizione() {
		if (isCompleta()) {
			return ProduttoreDiMissioniCasuale.testoDellaMissione("DISTURBATORE_DELLA_QUIETE_PUBBLICA_DESCRIZIONE_INIZIO", "PARAM_DESCRIZIONE", descrizioneBase());
		}
		int mancanti = EREMITI_DA_INCONTRARE - getEremitiIncontrati();
		if (mancanti == 1) {
			return ProduttoreDiMissioniCasuale.testoDellaMissione("DISTURBATORE_DELLA_QUIETE_PUBBLICA_DESCRIZIONE_ULTIMO", "PARAM_DESCRIZIONE", descrizioneBase());
		}
		if (mancanti == 10) {
			return descrizioneBase() + ".";
		}
		return ProduttoreDiMissioniCasuale.testoDellaMissione("DISTURBATORE_DELLA_QUIETE_PUBBLICA_DESCRIZIONE_MANCANO", "PARAM_DESCRIZIONE", descrizioneBase(), "PARAM_MANCANTI", Lingue.corrente().cardinale(mancanti, Genere.MASCHILE));
	}

	@Override
	public void controllaPreLocazione() {
		// L'attivazione avviene al primo incontro con un Eremita, non prima:
		// vedi controllaInLocazione()
	}

	@Override
	public void controllaInLocazione() {
		if (isCompleta()) {
			return;
		}
		Set<String> contati = getEremitiContati();
		boolean nuovi = false;
		for (Personaggio personaggio : GruppoAvversario.getIstanza().getPersonaggi()) {
			if (personaggio.getClasse() == TipoPersonaggio.EREMITA && contati.add(personaggio.getModelloDati().getUuid())) {
				nuovi = true;
			}
		}
		if (!nuovi) {
			// Nessun eremita, o già contati da un controllo precedente di questo stesso incontro
			return;
		}
		if (!isAttiva()) {
			BusEventi.pubblica(new NotificaTestoParagrafo(ProduttoreDiMissioniCasuale.testoDellaMissione("DISTURBATORE_DELLA_QUIETE_PUBBLICA_INIZIO", "PARAM_DESCRIZIONE", descrizioneBase())));
			attivaMissione();
		}
		// Si conta l'incontro, non il suo esito: qui la locazione è appena stata
		// descritta e nessuno ha ancora alzato le mani
		int incontrati = contati.size();
		aggiungiProprieta(EREMITI_CONTATI, String.join(",", contati));
		if (incontrati >= EREMITI_DA_INCONTRARE) {
			completaMissione();
			BusEventi.pubblica(new NotificaTestoParagrafo(ProduttoreDiMissioniCasuale.testoDellaMissione("DISTURBATORE_DELLA_QUIETE_PUBBLICA_FINE")));
		} else {
			BusEventi.pubblica(new NotificaTestoParagrafo(ProduttoreDiMissioniCasuale.testoDellaMissione("DISTURBATORE_DELLA_QUIETE_PUBBLICA_TURBATA", "PARAM_ORDINALE", Lingue.corrente().ordinale(incontrati, Genere.MASCHILE, false))));
		}
	}

	@Override
	public void controllaPostLocazione() {
		// L'incontro si conta all'arrivo, non alla fine
	}

	private int getEremitiIncontrati() {
		return getEremitiContati().size();
	}

	private Set<String> getEremitiContati() {
		String contati = ottieniProprieta(EREMITI_CONTATI);
		Set<String> eremiti = new LinkedHashSet<>();
		if (contati != null && !contati.isEmpty()) {
			eremiti.addAll(Arrays.asList(contati.split(",")));
		}
		return eremiti;
	}
}
