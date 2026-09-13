package com.threeamigos.foresta.eventi;

/**
 * Il GruppoGiocatore (che "fa da banchiere") rifiuta l'acquisto di un Consumabile da un commerciante
 * (per mancanza di fondi da parte del giocatore). Il discriminante è il costo dell'oggetto.
 *
 * @author Stefano Reksten
 */
public class EventoRifiutoAcquistoConsumabile extends EventoBase {

    private final EventoRichiestaAcquistoConsumabile eventoRichiestaAcquistoConsumabile;

    /**
     * @param eventoRichiestaAcquistoConsumabile la richiesta di acquisto di un Consumabile da un commerciante che si rifiuta
     */
    public EventoRifiutoAcquistoConsumabile(EventoRichiestaAcquistoConsumabile eventoRichiestaAcquistoConsumabile) {
        super(TipoEvento.RIFIUTO_ACQUISTO_CONSUMABILE);
        this.eventoRichiestaAcquistoConsumabile = eventoRichiestaAcquistoConsumabile;
    }

    /**
     * @return la richiesta di acquisto di un Consumabile da un commerciante che si rifiuta
     */
    public EventoRichiestaAcquistoConsumabile getEventoRichiestaAcquistoConsumabile() {
        return eventoRichiestaAcquistoConsumabile;
    }
}
