package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.artefatti.ScambiatoreArtefatti;

import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoAcquistoArtefatto;
import com.threeamigos.foresta.eventi.interni.InternoVenditaArtefatto;

/**
 * Lo scambio di artefatti fra il gruppo e un commerciante: ogni spostamento è un acquisto o una vendita, e i costi si mostrano.
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
        BusEventi.pubblica(new InternoAcquistoArtefatto(getParteAttiva(), getParteRemota(), artefatto, this));
    }

    @Override
    public void richiediSpostamentoSuParteRemota(Artefatto artefatto) {
        BusEventi.pubblica(new InternoVenditaArtefatto(getParteAttiva(), getParteRemota(), artefatto, this));
    }
}
