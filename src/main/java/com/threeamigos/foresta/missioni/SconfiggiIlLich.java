package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.ui.UI;

/**
 *
 * @author Stefano Reksten
 */
public class SconfiggiIlLich extends MissioneBase implements Missione {

	public SconfiggiIlLich() {
		super(ClasseMissione.SCONFIGGI_IL_LICH);
	}

    @Override
    public String getNome() {
        return "Sconfiggi il Lich";
    }

    @Override
    public String getDescrizione() {
        return "Il Lich, alleato per convenienza del Drago, infesta un castello e sta risvegliando i morti.";
    }

    @Override
    public void controllaPreLocazione() {
        if (!isAttiva()) {
            attivaMissione();
        }
    }

    @Override
    public void controllaInLocazione() {
        // Non succede niente
    }

    @Override
    public void controllaPostLocazione() {
        GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
        if (gruppo.getClasseLocazioneCorrente() == ClassiLocazione.CASTELLO_LICH && gruppo.getClasseLocazioneCorrente().getIstanza().isCompleta()) {
            completaMissione();
            UI.notifica("Il Lich è stato sconfitto!");
        }
    }

    @Override
    public boolean isPrimaria() {
        return true;
    }
}
