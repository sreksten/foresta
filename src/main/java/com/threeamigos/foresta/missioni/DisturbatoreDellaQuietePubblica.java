package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.EventoParagrafo;
import com.threeamigos.foresta.motore.GruppoAvversario;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tools.Misc;

/**
 * Dieci eremiti disturbati nel loro ritiro. Il conteggio avviene all'incontro, quando
 * il gruppo avversario è appena stato popolato e descritto: quel che succede dopo -
 * battaglia, amicizia, corruzione o fuga - non cambia il conto.
 */
public class DisturbatoreDellaQuietePubblica extends MissioneBase {

	private static final int EREMITI_DA_INCONTRARE = 10;

	private static final String DESCRIZIONE_BASE = "Angustia " + EREMITI_DA_INCONTRARE + " Eremiti";
	private static final String EREMITI_INCONTRATI = "EREMITI_INCONTRATI";

	public DisturbatoreDellaQuietePubblica() {
		super(ClasseMissione.DISTURBATORE_DELLA_QUIETE_PUBBLICA);
	}

	@Override
	public String getNome() {
		return "Disturbatore della Quiete Pubblica";
	}

	@Override
	public String getDescrizione() {
		if (isCompleta()) {
			return DESCRIZIONE_BASE + ". Dieci ritiri spirituali interrotti sul nascere.";
		}
		int mancanti = EREMITI_DA_INCONTRARE - getEremitiIncontrati();
		if (mancanti == 1) {
			return DESCRIZIONE_BASE + ". Ne manca uno, e si sta già preoccupando.";
		}
		if (mancanti == 10) {
			return DESCRIZIONE_BASE + ".";
		}
		return DESCRIZIONE_BASE + ". Ne mancano " + mancanti + ".";
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
		int eremiti = 0;
		for (Personaggio personaggio : GruppoAvversario.getIstanza().getPersonaggi()) {
			if (personaggio.getClasse() == ClassePersonaggio.EREMITA) {
				eremiti++;
			}
		}
		if (eremiti == 0) {
			return;
		}
		if (!isAttiva()) {
			BusEventi.pubblica(new EventoParagrafo("Nella Foresta vivono uomini che hanno scelto " +
					"la solitudine dopo lunga riflessione. " + DESCRIZIONE_BASE + ", e fai in modo che se ne ricordino."));
			attivaMissione();
		}
		// Si conta l'incontro, non il suo esito: qui la locazione è appena stata
		// descritta e nessuno ha ancora alzato le mani
		int incontrati = getEremitiIncontrati() + eremiti;
		aggiungiProprieta(EREMITI_INCONTRATI, String.valueOf(incontrati));
		if (incontrati >= EREMITI_DA_INCONTRARE) {
			completaMissione();
			BusEventi.pubblica(new EventoParagrafo("Dieci Eremiti su dieci confermano che la Foresta era " +
					"molto più tranquilla prima. " +
					"Il titolo di Disturbatore della Quiete Pubblica è meritato."));
		} else {
			BusEventi.pubblica(new EventoParagrafo("La quiete del " + Misc.getOrdinaleM(incontrati) +
					" Eremita è stata ufficialmente turbata."));
		}
	}

	@Override
	public void controllaPostLocazione() {
		// L'incontro si conta all'arrivo, non alla fine
	}

	private int getEremitiIncontrati() {
		String incontrati = ottieniProprieta(EREMITI_INCONTRATI);
		return incontrati == null ? 0 : Integer.parseInt(incontrati);
	}
}
