package com.threeamigos.foresta.eventi.comandigiocatore;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.tipi.TipoConsumabile;
import com.threeamigos.foresta.tipi.TipoIncantesimo;

/**
 * Il giocatore chiede di acquistare un Consumabile da un alchimista.
 * Il prezzo non lo porta: lo decide il motore dall'offerta dell'alchimista (vedi OfferteAlchimista).
 *
 * @author Stefano Reksten
 */
public class ComandoAcquistoConsumabile extends EventoBase {

    private final TipoConsumabile tipoConsumabile;
    private final TipoIncantesimo tipoIncantesimo;
    private final String uuidPersonaggio;

    public ComandoAcquistoConsumabile(TipoConsumabile tipoConsumabile, TipoIncantesimo tipoIncantesimo,
                                      String uuidPersonaggio) {
        super(TipoEvento.COMANDO_ACQUISTO_CONSUMABILE);
        this.tipoConsumabile = tipoConsumabile;
        this.tipoIncantesimo = tipoIncantesimo;
        this.uuidPersonaggio = uuidPersonaggio;
    }

    public TipoConsumabile getTipoConsumabile() {
        return tipoConsumabile;
    }

    public TipoIncantesimo getClasseIncantesimo() {
        return tipoIncantesimo;
    }

    public String getUuidPersonaggio() {
        return uuidPersonaggio;
    }
}
