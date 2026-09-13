package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.EventoRichiestaPrelievoArtefatto;
import com.threeamigos.foresta.eventi.EventoRichiestaStoccaggioArtefatto;
import com.threeamigos.foresta.oggetti.Artefatto;

/**
 *
 * @author Stefano Reksten
 */
public class AutomaInventario extends AutomaScambiatoreArtefatti {

    public AutomaInventario(ScambiatoreArtefatti parteAttiva, ScambiatoreArtefatti parteRemota) {
        super(parteAttiva, parteRemota);
    }

    @Override
    public boolean mostraCostoSuParteAttiva() {
        return false;
    }

    @Override
    public boolean mostraCostoSuParteRemota() {
        return false;
    }

    @Override
    public void richiediSpostamentoSuParteAttiva(Artefatto artefatto) {
        BusEventi.pubblica(new EventoRichiestaPrelievoArtefatto(getParteAttiva(), getParteRemota(), artefatto));
    }

    @Override
    public void richiediSpostamentoSuParteRemota(Artefatto artefatto) {
        BusEventi.pubblica(new EventoRichiestaStoccaggioArtefatto(getParteAttiva(), getParteRemota(), artefatto));
    }

}
