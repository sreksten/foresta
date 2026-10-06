package com.threeamigos.foresta.eventi.comandigiocatore;

import com.threeamigos.foresta.eventi.RichiestaConComandi;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.interfacce.VistaScambio;
import com.threeamigos.foresta.tipi.Comando;

import java.util.Collection;

/**
 * Il giocatore entra (o torna, dopo aver dato il nome all'artefatto) nella bottega dell'incantatore.
 */
public class ComandoAperturaIncantatore extends RichiestaConComandi {

    private final VistaScambio scambio;
    private final String messaggio;

    /**
     * @param scambio fra l'inventario del gruppo (parte attiva) e il banco di lavoro (parte remota)
     * @param messaggio il fumetto con cui l'incantatore accoglie, o null per nessuno
     */
    public ComandoAperturaIncantatore(Collection<Comando> possibilita, VistaScambio scambio, String messaggio) {
        super(TipoEvento.COMANDO_APERTURA_INCANTATORE, possibilita);
        this.scambio = scambio;
        this.messaggio = messaggio;
    }

    public VistaScambio getScambio() {
        return scambio;
    }

    public String getMessaggio() {
        return messaggio;
    }
}
