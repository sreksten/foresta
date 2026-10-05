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
        return "La Strega, Signora delle Arti Oscure ed alleata del Drago, ha preso possesso di un castello e" +
                " sta facendo avvizzire il territorio circostante.";
    }

    @Override
    public void controllaPreLocazione() {
        // Il castello non c'è dall'inizio: la missione se lo procura su un bosco, e si attiva solo se ci riesce
        // (altrimenti riprova al prossimo controllo)
        if (!isAttiva() && RegistroMissioni.rivendicaPerLocazioneUnica(TipoLocazione.CASTELLO_STREGA, TipoLocazione.BOSCO, this) != null) {
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
        if (gruppo.getTipoLocazioneCorrente() == TipoLocazione.CASTELLO_STREGA && gruppo.getLocazioneCorrente().isCompleta()) {
            completaMissione();
            BusEventi.pubblica(new NotificaTestoParagrafo("La Strega è stata sconfitta!"));
        }
    }

    @Override
    public boolean isPrimaria() {
        return true;
    }

    @Override
    public String getRicordoDellaLocazione() {
        return "Qui sorgeva il castello della Strega.";
    }
}
