package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;

/**
 * Una pagina di un intermezzo, da mostrare a tutto schermo finché non arriva la
 * successiva o l'automa torna alla schermata di gioco.
 *
 * @author Stefano Reksten
 */
public class NotificaPaginaIntermezzo extends EventoBase {

    private final String testo;
    private final int numeroPagina;
    private final int totalePagine;

    /**
     * @param numeroPagina da 1 a totalePagine
     */
    public NotificaPaginaIntermezzo(String testo, int numeroPagina, int totalePagine) {
        super(TipoEvento.NOTIFICA_PAGINA_INTERMEZZO);
        this.testo = testo;
        this.numeroPagina = numeroPagina;
        this.totalePagine = totalePagine;
    }

    public String getTesto() {
        return testo;
    }

    public int getNumeroPagina() {
        return numeroPagina;
    }

    public int getTotalePagine() {
        return totalePagine;
    }
}
