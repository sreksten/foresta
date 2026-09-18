package com.threeamigos.foresta.eventi;

/**
 * Acta est fabula. La partita, nel bene o nel male, è finita.
 *
 * @author Stefano Reksten
 */
public class EventoFineGioco extends EventoBase {

    private final boolean completatoConSuccesso;

    public EventoFineGioco(boolean completatoConSuccesso) {
        super(TipoEvento.FINE_GIOCO);
        this.completatoConSuccesso = completatoConSuccesso;
    }

    public boolean isCompletatoConSuccesso() {
        return completatoConSuccesso;
    }
}
