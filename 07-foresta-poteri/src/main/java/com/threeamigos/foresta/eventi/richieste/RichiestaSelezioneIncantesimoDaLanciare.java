package com.threeamigos.foresta.eventi.richieste;

import com.threeamigos.foresta.eventi.RichiestaConComandi;
import com.threeamigos.foresta.tipi.TipoEvento;
import com.threeamigos.foresta.tipi.Comando;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Chiede al giocatore quale incantesimo lanciare, fra quelli possibili. Oltre ai comandi possibili porta le abilità innate e i canti che il
 * personaggio conosce ma ha usato da poco: non si possono scegliere, ma la UI li mostra spenti con i turni che mancano (vedi
 * {@link #getComandiVisibili()} e {@link #getInRicarica()}).
 *
 * @author Stefano Reksten
 */
public class RichiestaSelezioneIncantesimoDaLanciare extends RichiestaConComandi {

    private final List<Comando> visibili;
    private final Map<Comando, RicaricaDelComando> inRicarica;

    public RichiestaSelezioneIncantesimoDaLanciare(Collection<Comando> possibilita) {
        this(possibilita, new ArrayList<>(possibilita), Collections.emptyMap());
    }

    /**
     * @param possibilita i comandi che si possono dare adesso
     * @param visibili tutti i comandi da mostrare, nell'ordine in cui compaiono: le possibilità e quelli in ricarica
     * @param inRicarica i comandi fra i visibili che non si possono ancora dare, con la loro ricarica
     */
    public RichiestaSelezioneIncantesimoDaLanciare(Collection<Comando> possibilita, List<Comando> visibili, Map<Comando, RicaricaDelComando> inRicarica) {
        super(TipoEvento.RICHIESTA_SELEZIONE_INCANTESIMO_DA_LANCIARE, possibilita);
        this.visibili = visibili;
        this.inRicarica = inRicarica;
    }

    /**
     * Tutti i comandi da mostrare, nell'ordine: quelli possibili e quelli in ricarica
     */
    public List<Comando> getComandiVisibili() {
        return visibili;
    }

    /**
     * I comandi fra i visibili che non si possono dare perché in ricarica, con i turni che mancano
     */
    public Map<Comando, RicaricaDelComando> getInRicarica() {
        return inRicarica;
    }

}
