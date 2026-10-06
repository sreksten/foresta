package com.threeamigos.foresta.eventi.comandigiocatore;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.TipoAttributo;

/**
 * Il giocatore spende un punto abilità di un personaggio su un attributo primario (doppio click nell'inventario).
 */
public class ComandoSpesaPuntoAbilita extends EventoBase {

    private final Personaggio personaggio;
    private final TipoAttributo attributo;

    public ComandoSpesaPuntoAbilita(Personaggio personaggio, TipoAttributo attributo) {
        super(TipoEvento.COMANDO_SPESA_PUNTO_ABILITA);
        this.personaggio = personaggio;
        this.attributo = attributo;
    }

    public Personaggio getPersonaggio() {
        return personaggio;
    }

    public TipoAttributo getAttributo() {
        return attributo;
    }
}
