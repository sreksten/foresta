package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.RegistroMissioni;
import com.threeamigos.foresta.tipi.ClasseMissione;
import com.threeamigos.foresta.tipi.TipoLocazione;

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
        // Il castello non c'è dall'inizio: la missione se lo procura su un bosco, e si attiva solo se ci riesce
        // (altrimenti riprova al prossimo controllo)
        if (!isAttiva() && RegistroMissioni.rivendicaPerLocazioneUnica(TipoLocazione.CASTELLO_IDRA, TipoLocazione.BOSCO, this) != null) {
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
        if (gruppo.getTipoLocazioneCorrente() == TipoLocazione.CASTELLO_IDRA && gruppo.getLocazioneCorrente().isCompleta()) {
            completaMissione();
            BusEventi.pubblica(new NotificaTestoParagrafo("L'Idra è stato sconfitta!"));
        }
    }

    @Override
    public boolean isPrimaria() {
        return true;
    }

    @Override
    public String getRicordoDellaLocazione() {
        return "Qui sorgeva il castello dell'Idra.";
    }
}
