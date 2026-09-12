package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.EventoParagrafo;
import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.motore.GruppoGiocatore;

/**
 * Un boccale in ogni locanda cittadina. Una tappa per città, e la missione si chiude
 * da sé quando le tappe sono tutte concluse.
 */
public class CronacheDiUnFegatoEroico extends MissioneBase {

	public CronacheDiUnFegatoEroico() {
		super(ClasseMissione.CRONACHE_DI_UN_FEGATO_EROICO);
		// Una tappa per città: se il mondo ne guadagnasse una, la missione la seguirebbe
		for (ClassiLocazione classeLocazione : ClassiLocazione.values()) {
			if (classeLocazione.getTipoLocazione() == ClassiLocazione.TipoLocazione.CITTA) {
				VisitaLocanda visitaLocanda = new VisitaLocanda();
				visitaLocanda.setCitta(classeLocazione);
				aggiungiMissione(visitaLocanda);
			}
		}
	}

	@Override
	public String getNome() {
		return "Cronache di un Fegato Eroico";
	}

	@Override
	public String getDescrizione() {
		return "Visita tutte le locande cittadine";
	}

	@Override
	public void controllaPreLocazione() {
		if (!isAttiva() && GruppoGiocatore.getIstanza().getClasseLocazioneCorrente().getTipoLocazione() == ClassiLocazione.TipoLocazione.CITTA) {
			BusEventi.pubblica(new EventoParagrafo(getDescrizione() + ": " + getNome() + " si scrive un boccale per volta."));
			attivaMissione();
		}
	}

	@Override
	public void controllaInLocazione() {
		// Non succede nulla: contano solo le tappe
	}

	@Override
	public void controllaPostLocazione() {
		if (isCompleta() || getMissioniSecondarie().isEmpty()) {
			return;
		}
		// Le tappe vengono controllate prima di qui (OrdineVisita.FIGLI_PRIMA), quindi
		// l'ultima bevuta chiude anche la missione, nello stesso giro
		if (getMissioniSecondarie().stream().allMatch(Missione::isCompleta)) {
			completaMissione();
			BusEventi.pubblica(new EventoParagrafo("Tutte le locande cittadine della Foresta sono state visitate. Le Cronache di un Fegato Eroico sono complete: un'impresa che nessun bardo oserà mai cantare."));
		}
	}
}
