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

	private static final String DESCRIZIONE_BASE = "Angustia " + Lingue.corrente().cardinale(EREMITI_DA_INCONTRARE, Genere.MASCHILE) + " Eremiti";
	// Gli identificativi degli eremiti già contati, separati da virgole
	private static final String EREMITI_CONTATI = "EREMITI_CONTATI";

	public DisturbatoreDellaQuietePubblica() {
		super(MissionePredefinita.DISTURBATORE_DELLA_QUIETE_PUBBLICA);
	}

	@Override
	public String getNome() {
		return Testi.formatta("missioni", "DisturbatoreDellaQuietePubblica.t1");
	}

	@Override
	public String getDescrizione() {
		if (isCompleta()) {
			return Testi.formatta("missioni", "DisturbatoreDellaQuietePubblica.t2", DESCRIZIONE_BASE);
		}
		int mancanti = EREMITI_DA_INCONTRARE - getEremitiIncontrati();
		if (mancanti == 1) {
			return Testi.formatta("missioni", "DisturbatoreDellaQuietePubblica.t3", DESCRIZIONE_BASE);
		}
		if (mancanti == 10) {
			return DESCRIZIONE_BASE + ".";
		}
		return Testi.formatta("missioni", "DisturbatoreDellaQuietePubblica.t4", DESCRIZIONE_BASE, Lingue.corrente().cardinale(mancanti, Genere.MASCHILE));
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
			BusEventi.pubblica(new NotificaTestoParagrafo(Testi.formatta("missioni", "DisturbatoreDellaQuietePubblica.s1", DESCRIZIONE_BASE)));
			attivaMissione();
		}
		// Si conta l'incontro, non il suo esito: qui la locazione è appena stata
		// descritta e nessuno ha ancora alzato le mani
		int incontrati = contati.size();
		aggiungiProprieta(EREMITI_CONTATI, String.join(",", contati));
		if (incontrati >= EREMITI_DA_INCONTRARE) {
			completaMissione();
			BusEventi.pubblica(new NotificaTestoParagrafo(Testi.formatta("missioni", "DisturbatoreDellaQuietePubblica.s2")));
		} else {
			BusEventi.pubblica(new NotificaTestoParagrafo(Testi.formatta("missioni", "DisturbatoreDellaQuietePubblica.s3", Lingue.corrente().ordinale(incontrati, Genere.MASCHILE, false))));
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
