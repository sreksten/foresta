package com.threeamigos.foresta.missioni.principali;

import com.threeamigos.foresta.mappa.Foresta;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.missioni.Missione;
import com.threeamigos.foresta.missioni.MissioneBase;
import com.threeamigos.foresta.missioni.RegistroMissioni;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.tipi.MissionePredefinita;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.testi.Testi;

/**
 * Sconfiggere l'Idra, alleata del Drago, nella Rocca del Sangue. È una figlia di {@link SconfiggiIlDrago}.
 *
 * @author Stefano Reksten
 */
public class SconfiggiLIdra extends MissioneBase implements Missione {

	public SconfiggiLIdra() {
		super(MissionePredefinita.SCONFIGGI_L_IDRA);
	}

    @Override
    public String getNome() {
        return Testi.formatta("missioni", "SconfiggiLIdra.t1");
    }

    @Override
    public String getDescrizione() {
        return Testi.formatta("missioni", "SconfiggiLIdra.t2");
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
        if (Foresta.getLocazioneDelGruppo() == TipoLocazione.CASTELLO_IDRA && RegistroMissioni.isLocazioneCompleta()) {
            completaMissione();
            BusEventi.pubblica(new NotificaTestoParagrafo(Testi.formatta("missioni", "SconfiggiLIdra.s1")));
        }
    }

    @Override
    public boolean isPrimaria() {
        return true;
    }

    @Override
    public String getRicordoDellaLocazione() {
        return Testi.formatta("missioni", "SconfiggiLIdra.t3");
    }
}
