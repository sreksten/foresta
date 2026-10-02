package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.RegistroMissioni;

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
        // Il castello non c'è dall'inizio: la missione se lo procura su un bosco, e si attiva solo se ci riesce
        // (altrimenti riprova al prossimo controllo)
        if (!isAttiva() && RegistroMissioni.rivendicaPerLocazioneUnica(ClassiLocazione.CASTELLO_LICH, ClassiLocazione.BOSCO, this) != null) {
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
        if (gruppo.getClasseLocazioneCorrente() == ClassiLocazione.CASTELLO_LICH && gruppo.getLocazioneCorrente().isCompleta()) {
            completaMissione();
            BusEventi.pubblica(new NotificaTestoParagrafo("Il Lich è stato sconfitto!"));
        }
    }

    @Override
    public boolean isPrimaria() {
        return true;
    }

    @Override
    public String getRicordoDellaLocazione() {
        return "il castello del Lich";
    }
}
