package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.RegistroMissioni;
import com.threeamigos.foresta.tipi.TipoLocazione;

/**
 *
 * @author Stefano Reksten
 */
public class SconfiggiIlMinotauroGigante extends MissioneBase implements Missione {

	public SconfiggiIlMinotauroGigante() {
		super(ClasseMissione.SCONFIGGI_IL_MINOTAURO_GIGANTE);
	}

    @Override
    public String getNome() {
        return "Sconfiggi il Minotauro Gigante";
    }

    @Override
    public String getDescrizione() {
        return "Il Minotauro Gigante, alleato del Drago, infesta un castello e terrorizza gli abitanti.";
    }

    @Override
    public void controllaPreLocazione() {
        // Il castello non c'è dall'inizio: la missione se lo procura su un bosco, e si attiva solo se ci riesce
        // (altrimenti riprova al prossimo controllo)
        if (!isAttiva() && RegistroMissioni.rivendicaPerLocazioneUnica(TipoLocazione.CASTELLO_MINOTAURO, TipoLocazione.BOSCO, this) != null) {
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
        if (gruppo.getClasseLocazioneCorrente() == TipoLocazione.CASTELLO_MINOTAURO && gruppo.getLocazioneCorrente().isCompleta()) {
            completaMissione();
            BusEventi.pubblica(new NotificaTestoParagrafo("Il Minotauro è stato sconfitto!"));
        }
    }

    @Override
    public boolean isPrimaria() {
        return true;
    }

    @Override
    public String getRicordoDellaLocazione() {
        return "Qui sorgeva il castello del Minotauro Gigante.";
    }
}
