package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.ui.UI;

/**
 *
 * @author Stefano Reksten
 */
public class SconfiggiLaStrega extends MissioneBase implements Missione {

	public SconfiggiLaStrega() {
		super(ClasseMissione.SCONFIGGI_LA_STREGA);
	}

    @Override
    public String getNome() {
        return "Sconfiggi la Strega";
    }

    @Override
    public String getDescrizione() {
        return "La Strega, Signora delle Arti Oscure ed alleata del Drago, ha preso possesso di un castello e sta facendo avvizzire il territorio circostante.";
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
        if (gruppo.getClasseLocazioneCorrente() == ClassiLocazione.CASTELLO_STREGA && gruppo.getClasseLocazioneCorrente().getIstanza().isCompleta()) {
            completaMissione();
            UI.notifica("La Strega è stata sconfitta!");
        }
    }

    @Override
    public boolean isPrimaria() {
        return true;
    }
}
