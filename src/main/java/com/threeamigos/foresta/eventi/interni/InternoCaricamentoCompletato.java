package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.modellodati.MessaggioMD;

import java.util.List;

/**
 * Un salvataggio è stato riletto con successo. Porta gli ultimi messaggi mostrati
 * al giocatore prima del salvataggio (dal più recente al più vecchio), così che il
 * pannello di testo possa essere ripopolato invece di ripartire vuoto.
 *
 * @author Stefano Reksten
 */
public class InternoCaricamentoCompletato extends EventoBase {

    private final List<MessaggioMD> ultimiMessaggi;

    /**
     * @param ultimiMessaggi gli ultimi messaggi da ripristinare, dal più recente al più vecchio
     */
    public InternoCaricamentoCompletato(List<MessaggioMD> ultimiMessaggi) {
        super(TipoEvento.INTERNO_CARICAMENTO_COMPLETATO);
        this.ultimiMessaggi = ultimiMessaggi;
    }

    public List<MessaggioMD> getUltimiMessaggi() {
        return ultimiMessaggi;
    }
}
