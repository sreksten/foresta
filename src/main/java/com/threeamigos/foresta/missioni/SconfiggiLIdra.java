package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.ui.UI;

/**
 *
 * @author Stefano Reksten
 */
public class SconfiggiLIdra extends MissioneBase implements Missione {

	public SconfiggiLIdra() {
		super(ClasseMissione.SCONFIGGI_L_IDRA);
	}

    @Override
    public String getNome() {
        return "Sconfiggi l'Idra";
    }

    @Override
    public String getDescrizione() {
        return "Una gigantesca Idra, alleata del Drago, infesta un castello e le lande adiacenti.";
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
        if (gruppo.getClasseLocazioneCorrente() == ClassiLocazione.CASTELLO_IDRA && gruppo.getClasseLocazioneCorrente().getIstanza().isCompleta()) {
            completaMissione();
            UI.notifica("L'Idra è stato sconfitta!");
        }
    }

    @Override
    public boolean isPrimaria() {
        return true;
    }
}
