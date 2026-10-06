package com.threeamigos.foresta.eventi.richieste;

import com.threeamigos.foresta.eventi.RichiestaConComandi;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.interfacce.VistaBancoDiLavoro;
import com.threeamigos.foresta.tipi.Comando;

import java.util.Collection;

/**
 * Il giocatore entra (o torna, dopo aver dato il nome all'artefatto) nella bottega dell'incantatore.
 */
public class RichiestaAperturaIncantatore extends RichiestaConComandi {

    private final VistaBancoDiLavoro banco;
    private final String messaggio;

    /**
     * @param banco lo scambio fra l'inventario del gruppo (parte attiva) e il banco di lavoro (parte remota)
     * @param messaggio il fumetto con cui l'incantatore accoglie, o null per nessuno
     */
    public RichiestaAperturaIncantatore(Collection<Comando> possibilita, VistaBancoDiLavoro banco, String messaggio) {
        super(TipoEvento.RICHIESTA_APERTURA_INCANTATORE, possibilita);
        this.banco = banco;
        this.messaggio = messaggio;
    }

    public VistaBancoDiLavoro getBanco() {
        return banco;
    }

    public String getMessaggio() {
        return messaggio;
    }
}
