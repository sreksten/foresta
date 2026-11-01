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
 * Sconfiggere il Lich, alleato del Drago, che infesta il Castello dell'Ombra e risveglia i morti. È una figlia di {@link SconfiggiIlDrago}.
 *
 * @author Stefano Reksten
 */
public class SconfiggiIlLich extends MissioneBase implements Missione {

	public SconfiggiIlLich() {
		super(MissionePredefinita.SCONFIGGI_IL_LICH);
	}

    @Override
    public String getNome() {
        return Testi.formatta("missioni", "SconfiggiIlLich.t1");
    }

    @Override
    public String getDescrizione() {
        return Testi.formatta("missioni", "SconfiggiIlLich.t2");
    }

    @Override
    public void controllaPreLocazione() {
        // Il castello non c'è dall'inizio: la missione se lo procura su un bosco, e si attiva solo se ci riesce
        // (altrimenti riprova al prossimo controllo)
        if (!isAttiva() && RegistroMissioni.rivendicaPerLocazioneUnica(TipoLocazione.CASTELLO_LICH, TipoLocazione.BOSCO, this) != null) {
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
        if (Foresta.getLocazioneDelGruppo() == TipoLocazione.CASTELLO_LICH && RegistroMissioni.isLocazioneCompleta()) {
            completaMissione();
            BusEventi.pubblica(new NotificaTestoParagrafo(Testi.formatta("missioni", "SconfiggiIlLich.s1")));
        }
    }

    @Override
    public boolean isPrimaria() {
        return true;
    }

    @Override
    public String getRicordoDellaLocazione() {
        return Testi.formatta("missioni", "SconfiggiIlLich.t3");
    }
}
