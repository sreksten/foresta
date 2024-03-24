package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.motore.Foresta;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.LineaTemporale;
import com.threeamigos.foresta.ui.UI;

public class RecuperaIlMedaglione extends MissioneRecuperaBersaglio implements Missione {
	
	@Override
	public String getNome() {
		return "Recupera il medaglione";
	}

	@Override
	public String getDescrizione() {
		if (isBersaglioRecuperato()) {
			return "Un uomo ti ha chiesto di recuperare il suo prezioso medaglione rubato da una banda di ladri.";
		} else {
			return "Torna in citta' per riconsegnare il medaglione rubato in cambio della ricompensa.";
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
				UI.notifica(gruppo.getCapo().getNome() + " incontra un uomo che chiede aiuto per recuperare il suo prezioso medaglione rubato da una banda di ladri, che hanno il loro covo in una grotta. Offre 20 monete in cambio.");
				attivaMissione();
				Foresta.costruisciLocazioneUnica(ClassiLocazione.GROTTA_RECUPERA_IL_MEDAGLIONE, true);
			} else if (!isCompleta() && isBersaglioRecuperato()) {
				UI.notifica("L'uomo e' felicissimo di riavere il suo medaglione in cambio delle 20 monete promesse.");
				gruppo.addMonete(20);
				completaMissione();
			}
		}
	}

	@Override
	public void controllaPostLocazione() {
		GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
		if (gruppo.isInLocazioneUnica(ClassiLocazione.GROTTA_RECUPERA_IL_MEDAGLIONE) &&
				gruppo.getClasseLocazione().getIstanza().isCompleta() && !isBersaglioRecuperato()) {
			UI.notifica("Il medaglione e' stato recuperato. Puoi tornare in citta' per reclamare la ricompensa.");
			setBersaglioRecuperato();
		}
	}
}
