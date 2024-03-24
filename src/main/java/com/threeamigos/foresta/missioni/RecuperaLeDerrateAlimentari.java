package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.motore.Foresta;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.LineaTemporale;
import com.threeamigos.foresta.ui.UI;

public class RecuperaLeDerrateAlimentari extends MissioneRecuperaBersaglio implements Missione {
	
	@Override
	public String getNome() {
		return "Recupera le derrate alimentari";
	}

	@Override
	public String getDescrizione() {
		if (!isBersaglioRecuperato()) {
			return "Il Borgomastro chiede aiuto: una banda di Troll ha fatto sparire un carico di derrate alimentari. Offre 20 monete come ricompensa.";
		} else {
			return "Torna in citta' per riconsegnare le derrate alimentari recuperate e ottenere la ricompensa di 20 monete.";
		}
	}

	@Override
	public void controllaPreLocazione() {
		// Non succede nulla
	}
	
	@Override
	public void controllaInLocazione() {
		GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
		if (gruppo.isInLocazioneUnica(ClassiLocazione.CITTA_RUUNA) &&
				!LineaTemporale.isCittaDistrutta(ClassiLocazione.CITTA_RUUNA)) {
			if (!isAttiva()) {
				UI.notifica("Il Borgomastro chiede a " + gruppo.getCapo().getNome() + " aiuto per recuperare un carico di derrate alimentari che e' stato rubato da una banda di Troll, che si nascondono in alcune rovine. Offre 20 monete in cambio.");
				attivaMissione();
				Foresta.costruisciLocazioneUnica(ClassiLocazione.ROVINE_RECUPERA_LE_DERRATE_ALIMENTARI, true);
			} else if (!isCompleta() && isBersaglioRecuperato()) {
				UI.notifica(new StringBuilder("Il Borgomastro accoglie ").append(gruppo.chi()).append(", che ha recuperato le derrate alimentari. La ricompensa promessa viene saldata: 20 monete.").toString());
				gruppo.addMonete(20);
				completaMissione();
			}
		}
	}

	@Override
	public void controllaPostLocazione() {
		GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
		if (gruppo.isInLocazioneUnica(ClassiLocazione.ROVINE_RECUPERA_LE_DERRATE_ALIMENTARI) &&
				gruppo.getClasseLocazione().getIstanza().isCompleta() && !isBersaglioRecuperato()) {
			UI.notifica(new StringBuilder("Le derrate alimentari sono state recuperate. ").append(gruppo.chiMaiuscolo()).append(" puo' tornare in citta' per reclamare la ricompensa.").toString());
			setBersaglioRecuperato();
		}
	}
}
