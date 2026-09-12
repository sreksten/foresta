package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.EventoRichiestaAcquisto;
import com.threeamigos.foresta.eventi.EventoRichiestaVendita;
import com.threeamigos.foresta.oggetti.Artefatto;

/**
 *
 * @author Stefano Reksten
 */
public class AutomaAcquisti extends AutomaScambiatoreArtefatti {

    public AutomaAcquisti(ScambiatoreArtefatti parteAttiva, ScambiatoreArtefatti parteRemota) {
        super(parteAttiva, parteRemota);
    }

    @Override
    public boolean mostraCostoSuParteAttiva() {
        return false;
    }

    @Override
    public boolean mostraCostoSuParteRemota() {
        return true;
    }

    @Override
    public void richiediSpostamentoSuParteAttiva(Artefatto artefatto) {
        BusEventi.pubblica(new EventoRichiestaAcquisto(getParteAttiva(), getParteRemota(), artefatto));
    }

    @Override
    public void richiediSpostamentoSuParteRemota(Artefatto artefatto) {
        BusEventi.pubblica(new EventoRichiestaVendita(getParteAttiva(), getParteRemota(), artefatto));
    }
}
