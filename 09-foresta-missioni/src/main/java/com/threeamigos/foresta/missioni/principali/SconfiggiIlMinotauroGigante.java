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
 * Sconfiggere il Minotauro Gigante, alleato del Drago, nella Torre della Paura. È una figlia di {@link SconfiggiIlDrago}.
 *
 * @author Stefano Reksten
 */
public class SconfiggiIlMinotauroGigante extends MissioneBase implements Missione {

	public SconfiggiIlMinotauroGigante() {
		super(MissionePredefinita.SCONFIGGI_IL_MINOTAURO_GIGANTE);
	}

    @Override
    public String getNome() {
        return Testi.formatta("missioni", "SconfiggiIlMinotauroGigante.t1");
    }

    @Override
    public String getDescrizione() {
        return Testi.formatta("missioni", "SconfiggiIlMinotauroGigante.t2");
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
        if (Foresta.getLocazioneDelGruppo() == TipoLocazione.CASTELLO_MINOTAURO && RegistroMissioni.isLocazioneCompleta()) {
            completaMissione();
            BusEventi.pubblica(new NotificaTestoParagrafo(Testi.formatta("missioni", "SconfiggiIlMinotauroGigante.s1")));
        }
    }

    @Override
    public boolean isPrimaria() {
        return true;
    }

    @Override
    public String getRicordoDellaLocazione() {
        return Testi.formatta("missioni", "SconfiggiIlMinotauroGigante.t3");
    }
}
