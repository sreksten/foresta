package com.threeamigos.foresta.eventi;

/**
 *
 * @author Stefano Reksten
 */
public class EventoPuliziaCacheDinamicaImmagini extends EventoBase {

    private final int elementiPrima;
    private final int elementiDopo;

    public EventoPuliziaCacheDinamicaImmagini(int elementiPrima, int elementiDopo) {
        super(TipoEvento.PULIZIA_CACHE_IMMAGINI);
        this.elementiPrima = elementiPrima;
        this.elementiDopo = elementiDopo;
    }

    public int getElementiPrima() {
        return elementiPrima;
    }

    public int getElementiDopo() {
        return elementiDopo;
    }
}
