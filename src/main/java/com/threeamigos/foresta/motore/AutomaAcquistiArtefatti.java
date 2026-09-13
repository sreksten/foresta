package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.EventoRichiestaAcquistoArtefatto;
import com.threeamigos.foresta.eventi.EventoRichiestaVenditaArtefatto;
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
        BusEventi.pubblica(new EventoRichiestaAcquistoArtefatto(getParteAttiva(), getParteRemota(), artefatto));
    }

    @Override
    public void richiediSpostamentoSuParteRemota(Artefatto artefatto) {
        BusEventi.pubblica(new EventoRichiestaVenditaArtefatto(getParteAttiva(), getParteRemota(), artefatto));
    }
}
