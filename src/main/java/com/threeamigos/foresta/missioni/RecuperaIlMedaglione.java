package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.EventoParagrafo;
import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.motore.Foresta;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.LineaTemporale;
import com.threeamigos.foresta.personaggi.Personaggio;

public class RecuperaIlMedaglione extends MissioneRecuperaBersaglio implements Missione {

	public RecuperaIlMedaglione() {
		super(ClasseMissione.RECUPERA_IL_MEDAGLIONE);
	}

	@Override
	public String getNome() {
		return "Recupera il medaglione";
	}

	@Override
	public String getDescrizione() {
		if (isBersaglioRecuperato()) {
			return "Un uomo ti ha chiesto di recuperare il suo prezioso medaglione rubato da una banda di ladri.";
		} else {
			return "Torna in città per riconsegnare il medaglione rubato in cambio della ricompensa.";
		}
	}

	@Override
	public void controllaPreLocazione() {
		// Non succede nulla
	}
	
	@Override
	public void controllaInLocazione() {
		GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
		if (gruppo.isInLocazioneUnica(ClassiLocazione.CITTA_FLEENA) &&
				!LineaTemporale.isCittaDistrutta(ClassiLocazione.CITTA_FLEENA)) {
			if (!isAttiva()) {
				BusEventi.pubblica(new EventoParagrafo(gruppo.getCapo().getNome(Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA, Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE) +
						" incontra un uomo che chiede aiuto per recuperare il suo prezioso medaglione rubato da " +
						"una banda di ladri, che hanno il loro covo in una grotta. Offre 20 monete in cambio."));
				attivaMissione();
				Foresta.costruisciLocazioneUnica(ClassiLocazione.GROTTA_RECUPERA_IL_MEDAGLIONE, true);
			} else if (!isCompleta() && isBersaglioRecuperato()) {
				completaMissione();
				BusEventi.pubblica(new EventoParagrafo("L'uomo è felicissimo di riavere il suo medaglione in cambio delle 20 monete promesse."));
				gruppo.addMonete(20);
			}
		}
	}

	@Override
	public void controllaPostLocazione() {
		GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
		if (gruppo.isInLocazioneUnica(ClassiLocazione.GROTTA_RECUPERA_IL_MEDAGLIONE) &&
				gruppo.getLocazioneCorrente().isCompleta() && !isBersaglioRecuperato()) {
			BusEventi.pubblica(new EventoParagrafo("Il medaglione è stato recuperato. Puoi tornare in città per reclamare la ricompensa."));
			setBersaglioRecuperato();
		}
	}
}
