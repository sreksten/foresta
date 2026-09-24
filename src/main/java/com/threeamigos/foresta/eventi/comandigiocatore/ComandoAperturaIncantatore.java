package com.threeamigos.foresta.eventi.comandigiocatore;

import com.threeamigos.foresta.eventi.RichiestaConComandi;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.motore.AutomaIncantatore;
import com.threeamigos.foresta.motore.Comando;

import java.util.Collection;

/**
 * Il giocatore entra (o torna, dopo aver dato il nome all'artefatto) nella bottega dell'incantatore.
 */
public class ComandoAperturaIncantatore extends RichiestaConComandi {

    private final AutomaIncantatore automaIncantatore;
    private final String messaggio;

    /**
     * @param messaggio il fumetto con cui l'incantatore accoglie, o null per nessuno
     */
    public ComandoAperturaIncantatore(Collection<Comando> possibilita, AutomaIncantatore automaIncantatore, String messaggio) {
        super(TipoEvento.COMANDO_APERTURA_INCANTATORE, possibilita);
        this.automaIncantatore = automaIncantatore;
        this.messaggio = messaggio;
    }

    public AutomaIncantatore getAutomaIncantatore() {
        return automaIncantatore;
    }

    public String getMessaggio() {
        return messaggio;
    }
}
