package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoAcquistoArtefatto;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoVenditaArtefatto;
import com.threeamigos.foresta.oggetti.Artefatto;

/**
 *
 * @author Stefano Reksten
 */
public class AutomaAcquistiArtefatti extends AutomaScambiatoreArtefatti {

    public AutomaAcquistiArtefatti(ScambiatoreArtefatti parteAttiva, ScambiatoreArtefatti parteRemota) {
        super(parteAttiva, parteRemota);
    }

    @Override
    public boolean mostraCostoSuParteAttiva() {
        return true;
    }

    @Override
    public boolean mostraCostoSuParteRemota() {
        return true;
    }

    @Override
    public void richiediSpostamentoSuParteAttiva(Artefatto artefatto) {
        BusEventi.pubblica(new ComandoAcquistoArtefatto(getParteAttiva(), getParteRemota(), artefatto));
    }

    @Override
    public void richiediSpostamentoSuParteRemota(Artefatto artefatto) {
        BusEventi.pubblica(new ComandoVenditaArtefatto(getParteAttiva(), getParteRemota(), artefatto));
    }
}
