package com.threeamigos.foresta.eventi;

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
public class EventoStatoDiGioco extends EventoBase {

    private final Stato stato;
    private final Collection<Comando> comandiPossibili;

    public EventoStatoDiGioco(Stato stato) {
        super(TipoEvento.STATO_DI_GIOCO);
        this.stato = stato;
        this.comandiPossibili = Collections.emptyList();
    }

    public EventoStatoDiGioco(Stato stato, Comando ... comandiPossibili) {
        super(TipoEvento.STATO_DI_GIOCO);
        this.stato = stato;
        this.comandiPossibili = Arrays.asList(comandiPossibili);
    }

    public EventoStatoDiGioco(Stato stato, Collection<Comando> comandiPossibili) {
        super(TipoEvento.STATO_DI_GIOCO);
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
