package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.oggetti.ClassiOggetto;

/**
 * Il gruppo ha raccolto l'oggetto di fine locazione: quale, in che quantità e se era
 * custodito dagli avversari o abbandonato in una locazione senza mostri.
 */
public class InternoOggettoRaccolto extends EventoBase {

    private final ClassiOggetto classe;
    private final int quantita;
    private final boolean custodito;

    public InternoOggettoRaccolto(ClassiOggetto classe, int quantita, boolean custodito) {
        super(TipoEvento.INTERNO_OGGETTO_RACCOLTO);
        this.classe = classe;
        this.quantita = quantita;
        this.custodito = custodito;
    }

    public ClassiOggetto getClasse() {
        return classe;
    }

    public int getQuantita() {
        return quantita;
    }

    /**
     * @return true se nella locazione c'erano degli avversari a custodire l'oggetto
     */
    public boolean isCustodito() {
        return custodito;
    }
}
