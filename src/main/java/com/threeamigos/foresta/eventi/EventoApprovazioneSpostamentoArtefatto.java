package com.threeamigos.foresta.eventi;

/**
 * Classe base per le approvazioni delle richieste di spostamento di un artefatto.
 * (Personaggio -> GruppoGiocatore, GruppoGiocatore -> Commerciante, GruppoGiocatore -> Personaggio...)
 *
 * @author Stefano Reksten
 */
public abstract class EventoApprovazioneSpostamentoArtefatto<T> extends EventoBase {

    private final EventoRichiestaSpostamentoArtefatto<T> eventoRichiestaSpostamentoArtefatto;

    /**
     * @param eventoRichiestaSpostamentoArtefatto la richiesta di spostamento di un Artefatto che si approva
     */
    public EventoApprovazioneSpostamentoArtefatto(EventoRichiestaSpostamentoArtefatto<T> eventoRichiestaSpostamentoArtefatto) {
        super(TipoEvento.APPROVAZIONE_SPOSTAMENTO_OGGETTO);
        this.eventoRichiestaSpostamentoArtefatto = eventoRichiestaSpostamentoArtefatto;
    }

    /**
     * @return la richiesta originale di spostamento di un Artefatto che si approva
     */
    public EventoRichiestaSpostamentoArtefatto<T> getEventoRichiestaSpostamentoArtefatto() {
        return eventoRichiestaSpostamentoArtefatto;
    }
}
