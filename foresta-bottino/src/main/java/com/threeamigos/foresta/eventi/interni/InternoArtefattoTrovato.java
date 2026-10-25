package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.tipi.TipoEvento;

import java.util.Objects;

/**
 * Il gruppo ha ottenuto un artefatto trovandolo: preso per terra o in un tempio, aperto in un cofano o ricevuto con un anello magico.
 * Gli ingredienti magici non contano come artefatti e non lo pubblicano. Serve ai trofei dei tesori degli avversari.
 */
public class InternoArtefattoTrovato extends EventoBase {

    private final Artefatto artefatto;
    private final boolean custodito;

    /**
     * @param custodito vero se nella locazione c'erano degli avversari a custodire l'artefatto
     */
    public InternoArtefattoTrovato(Artefatto artefatto, boolean custodito) {
        super(TipoEvento.INTERNO_ARTEFATTO_TROVATO);
        this.artefatto = Objects.requireNonNull(artefatto);
        this.custodito = custodito;
    }

    public Artefatto getArtefatto() {
        return artefatto;
    }

    /**
     * @return true se nella locazione c'erano degli avversari a custodire l'artefatto
     */
    public boolean isCustodito() {
        return custodito;
    }
}
