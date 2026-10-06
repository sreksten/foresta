package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.FaseDiGioco;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Evento interno - l'automa annuncia alla UI la nuova fase del gioco (vedi FaseDiGioco).
 * Può portare con se una lista di comandi possibili. Se non ci sono, valgono i comandi precedentemente impostati.
 *
 * @author Stefano Reksten
 */
public class InternoFaseDiGioco extends EventoBase {

    private final FaseDiGioco fase;
    private final Collection<Comando> comandiPossibili;

    public InternoFaseDiGioco(FaseDiGioco fase) {
        super(TipoEvento.INTERNO_FASE_DI_GIOCO);
        this.fase = fase;
        this.comandiPossibili = Collections.emptyList();
    }

    public InternoFaseDiGioco(FaseDiGioco fase, Comando ... comandiPossibili) {
        super(TipoEvento.INTERNO_FASE_DI_GIOCO);
        this.fase = fase;
        this.comandiPossibili = Arrays.asList(comandiPossibili);
    }

    public InternoFaseDiGioco(FaseDiGioco fase, Collection<Comando> comandiPossibili) {
        super(TipoEvento.INTERNO_FASE_DI_GIOCO);
        this.fase = fase;
        this.comandiPossibili = comandiPossibili;
    }

    public FaseDiGioco getFase() {
        return fase;
    }

    public Collection<Comando> getComandiPossibili() {
        return comandiPossibili;
    }
}
