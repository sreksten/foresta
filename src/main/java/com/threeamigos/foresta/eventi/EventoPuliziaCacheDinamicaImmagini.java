package com.threeamigos.foresta.eventi;

/**
 * Evento interno del motore UI che segnala che lo sfoltimento della cache immagini generate dinamicamente
 * è stato effettuato.
 *
 * @author Stefano Reksten
 */
public class EventoPuliziaCacheDinamicaImmagini extends EventoBase {

    private final int elementiPrima;
    private final int elementiDopo;

    /**
     * @param elementiPrima il numero degli elementi prima dello sfoltimento
     * @param elementiDopo il numero degli elementi dopo lo sfoltimento
     */
    public EventoPuliziaCacheDinamicaImmagini(int elementiPrima, int elementiDopo) {
        super(TipoEvento.PULIZIA_CACHE_IMMAGINI);
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
