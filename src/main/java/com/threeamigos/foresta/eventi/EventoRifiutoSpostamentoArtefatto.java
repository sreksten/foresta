package com.threeamigos.foresta.eventi;

/**
 * Classe base per i rifiuti delle richieste di spostamento di un artefatto.
 * (Personaggio -> GruppoGiocatore, GruppoGiocatore -> Commerciante, GruppoGiocatore -> Personaggio...)
 *
 * @author Stefano Reksten
 */
public abstract class EventoRifiutoSpostamentoArtefatto<T> extends EventoBase {

    private final EventoRichiestaSpostamentoArtefatto<T> eventoRichiestaSpostamentoArtefatto;

    /**
     * @param eventoRichiestaSpostamentoArtefatto la richiesta di spostamento di un Artefatto che si rifiuta
     */
    public EventoRifiutoSpostamentoArtefatto(EventoRichiestaSpostamentoArtefatto<T> eventoRichiestaSpostamentoArtefatto) {
        super(TipoEvento.RIFIUTO_SPOSTAMENTO_OGGETTO);
        this.eventoRichiestaSpostamentoArtefatto = eventoRichiestaSpostamentoArtefatto;
    }

    /**
     * @return la richiesta originale di spostamento di un Artefatto che si rifiuta
     */
    public EventoRichiestaSpostamentoArtefatto<T> getEventoRichiestaSpostamento() {
        return eventoRichiestaSpostamentoArtefatto;
    }
}
