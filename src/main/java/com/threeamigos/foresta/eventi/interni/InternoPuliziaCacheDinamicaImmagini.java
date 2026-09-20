package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;

/**
 * Evento interno del motore UI che segnala che lo sfoltimento della cache immagini generate dinamicamente
 * è stato effettuato.
 *
 * @author Stefano Reksten
 */
public class InternoPuliziaCacheDinamicaImmagini extends EventoBase {

    private final int elementiPrima;
    private final int elementiDopo;

    /**
     * @param elementiPrima il numero degli elementi prima dello sfoltimento
     * @param elementiDopo il numero degli elementi dopo lo sfoltimento
     */
    public InternoPuliziaCacheDinamicaImmagini(int elementiPrima, int elementiDopo) {
        super(TipoEvento.INTERNO_PULIZIA_CACHE_DINAMICA_IMMAGINI);
        this.elementiPrima = elementiPrima;
        this.elementiDopo = elementiDopo;
    }

    /**
     * @return il numero degli elementi prima dello sfoltimento
     */
    public int getElementiPrima() {
        return elementiPrima;
    }

    /**
     * @return il numero degli elementi dopo lo sfoltimento
     */
    public int getElementiDopo() {
        return elementiDopo;
    }
}
