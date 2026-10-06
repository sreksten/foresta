package com.threeamigos.foresta.eventi.comandigiocatore;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.tipi.TipoAttributo;

/**
 * Il giocatore spende un punto abilità di un personaggio su un attributo primario (doppio click nell'inventario).
 */
public class ComandoSpesaPuntoAbilita extends EventoBase {

    private final String uuidPersonaggio;
    private final TipoAttributo attributo;

    public ComandoSpesaPuntoAbilita(String uuidPersonaggio, TipoAttributo attributo) {
        super(TipoEvento.COMANDO_SPESA_PUNTO_ABILITA);
        this.uuidPersonaggio = uuidPersonaggio;
        this.attributo = attributo;
    }

    public String getUuidPersonaggio() {
        return uuidPersonaggio;
    }

    public TipoAttributo getAttributo() {
        return attributo;
    }
}
