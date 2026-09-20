package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoPrelievoArtefatto;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoStoccaggioArtefatto;
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
        BusEventi.pubblica(new ComandoPrelievoArtefatto(getParteAttiva(), getParteRemota(), artefatto));
    }

    @Override
    public void richiediSpostamentoSuParteRemota(Artefatto artefatto) {
        BusEventi.pubblica(new ComandoStoccaggioArtefatto(getParteAttiva(), getParteRemota(), artefatto));
    }

}
