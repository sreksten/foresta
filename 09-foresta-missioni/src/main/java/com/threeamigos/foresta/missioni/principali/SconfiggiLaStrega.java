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
import com.threeamigos.foresta.testi.ProduttoreDiMissioniCasuale;
import com.threeamigos.foresta.testi.Testi;

/**
 * Sconfiggere la Strega, alleata del Drago, nel Maniero del Malefizio. È una figlia di {@link SconfiggiIlDrago}.
 *
 * @author Stefano Reksten
 */
public class SconfiggiLaStrega extends MissioneBase implements Missione {

	public SconfiggiLaStrega() {
		super(MissionePredefinita.SCONFIGGI_LA_STREGA);
	}

    @Override
    protected String creaIlNome() {
        return ProduttoreDiMissioniCasuale.testoDellaMissione("SCONFIGGI_LA_STREGA_TITOLO");
    }

    @Override
    public String getDescrizione() {
        return ProduttoreDiMissioniCasuale.testoDellaMissione("SCONFIGGI_LA_STREGA_DESCRIZIONE");
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
        if (Foresta.getLocazioneDelGruppo() == TipoLocazione.CASTELLO_STREGA && RegistroMissioni.isLocazioneCompleta()) {
            completaMissione();
            BusEventi.pubblica(new NotificaTestoParagrafo(ProduttoreDiMissioniCasuale.testoDellaMissione("SCONFIGGI_LA_STREGA_VITTORIA")));
        }
    }

    @Override
    public boolean isPrimaria() {
        return true;
    }

    @Override
    public String getRicordoDellaLocazione() {
        return ProduttoreDiMissioniCasuale.testoDellaMissione("SCONFIGGI_LA_STREGA_RICORDO");
    }
}
