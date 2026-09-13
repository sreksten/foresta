package com.threeamigos.foresta.eventi;

/**
 * Il GruppoGiocatore (che "fa da banchiere") approva l'acquisto di un Consumabile da un commerciante.
 * Il discriminante è il costo dell'oggetto.
 *
 * @author Stefano Reksten
 */
public class EventoApprovazioneAcquistoConsumabile extends EventoBase {

    private final EventoRichiestaAcquistoConsumabile eventoRichiestaAcquistoConsumabile;

    /**
     * @param eventoRichiestaAcquistoConsumabile la richiesta di acquisto di un Consumabile da un commerciante che si approva
     */
    public EventoApprovazioneAcquistoConsumabile(EventoRichiestaAcquistoConsumabile eventoRichiestaAcquistoConsumabile) {
        super(TipoEvento.APPROVAZIONE_ACQUISTO_CONSUMABILE);
        this.eventoRichiestaAcquistoConsumabile = eventoRichiestaAcquistoConsumabile;
    }

    /**
     * @return la richiesta di acquisto di un Consumabile da un commerciante che si approva
     */
    public EventoRichiestaAcquistoConsumabile getEventoRichiestaAcquistoConsumabile() {
        return eventoRichiestaAcquistoConsumabile;
    }

}
