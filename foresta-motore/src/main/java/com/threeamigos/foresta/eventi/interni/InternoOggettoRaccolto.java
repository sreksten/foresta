package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.tipi.TipoOggetto;

import java.util.Optional;

/**
 * Il gruppo ha raccolto l'oggetto di fine locazione: quale, in che quantità, l'eventuale
 * artefatto e se era custodito dagli avversari o abbandonato in una locazione senza mostri.
 */
public class InternoOggettoRaccolto extends EventoBase {

    private final TipoOggetto classe;
    private final int quantita;
    private final Artefatto artefatto;
    private final boolean custodito;

    /**
     * @param artefatto l'artefatto raccolto (un artefatto vero o, per esempio, una spada), oppure null
     */
    public InternoOggettoRaccolto(TipoOggetto classe, int quantita, Artefatto artefatto, boolean custodito) {
        super(TipoEvento.INTERNO_OGGETTO_RACCOLTO);
        this.classe = classe;
        this.quantita = quantita;
        this.artefatto = artefatto;
        this.custodito = custodito;
    }

    public TipoOggetto getClasse() {
        return classe;
    }

    public int getQuantita() {
        return quantita;
    }

    public Optional<Artefatto> getArtefatto() {
        return Optional.ofNullable(artefatto);
    }

    /**
     * @return true se nella locazione c'erano degli avversari a custodire l'oggetto
     */
    public boolean isCustodito() {
        return custodito;
    }
}
