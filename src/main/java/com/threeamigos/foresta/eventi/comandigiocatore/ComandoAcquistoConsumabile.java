package com.threeamigos.foresta.eventi.comandigiocatore;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.tipi.ClasseIncantesimo;
import com.threeamigos.foresta.tipi.TipoConsumabile;

/**
 * Il giocatore chiede di acquistare un Consumabile da un alchimista.
 * Il prezzo non lo porta: lo decide il motore dall'offerta dell'alchimista (vedi OfferteAlchimista).
 *
 * @author Stefano Reksten
 */
public class ComandoAcquistoConsumabile extends EventoBase {

    private final TipoConsumabile tipoConsumabile;
    private final ClasseIncantesimo classeIncantesimo;
    private final String uuidPersonaggio;

    public ComandoAcquistoConsumabile(TipoConsumabile tipoConsumabile, ClasseIncantesimo classeIncantesimo,
                                      String uuidPersonaggio) {
        super(TipoEvento.COMANDO_ACQUISTO_CONSUMABILE);
        this.tipoConsumabile = tipoConsumabile;
        this.classeIncantesimo = classeIncantesimo;
        this.uuidPersonaggio = uuidPersonaggio;
    }

    public TipoConsumabile getTipoConsumabile() {
        return tipoConsumabile;
    }

    public ClasseIncantesimo getClasseIncantesimo() {
        return classeIncantesimo;
    }

    public String getUuidPersonaggio() {
        return uuidPersonaggio;
    }
}
