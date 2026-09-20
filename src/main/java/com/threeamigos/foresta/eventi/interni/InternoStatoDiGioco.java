package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.motore.Comando;
import com.threeamigos.foresta.motore.Stato;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Evento interno - l'automa annuncia il nuovo stato in cui il gioco si trova.
 * Può portare con se una lista di comandi possibili. Se non ci sono, valgono i comandi precedentemente impostati.
 *
 * @author Stefano Reksten
 */
public class InternoStatoDiGioco extends EventoBase {

    private final Stato stato;
    private final Collection<Comando> comandiPossibili;

    public InternoStatoDiGioco(Stato stato) {
        super(TipoEvento.INTERNO_STATO_DI_GIOCO);
        this.stato = stato;
        this.comandiPossibili = Collections.emptyList();
    }

    public InternoStatoDiGioco(Stato stato, Comando ... comandiPossibili) {
        super(TipoEvento.INTERNO_STATO_DI_GIOCO);
        this.stato = stato;
        this.comandiPossibili = Arrays.asList(comandiPossibili);
    }

    public InternoStatoDiGioco(Stato stato, Collection<Comando> comandiPossibili) {
        super(TipoEvento.INTERNO_STATO_DI_GIOCO);
        this.stato = stato;
        this.comandiPossibili = comandiPossibili;
    }

    public Stato getStato() {
        return stato;
    }

    public Collection<Comando> getComandiPossibili() {
        return comandiPossibili;
    }
}
