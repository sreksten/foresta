package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.intermezzi.PaginaIntermezzo;

/**
 * Una pagina di un intermezzo, da mostrare a tutto schermo finché non arriva la
 * successiva o l'automa torna alla schermata di gioco.
 *
 * @author Stefano Reksten
 */
public class NotificaPaginaIntermezzo extends EventoBase {

    private final PaginaIntermezzo pagina;
    private final int numeroPagina;
    private final int totalePagine;

    /**
     * @param numeroPagina da 1 a totalePagine
     */
    public NotificaPaginaIntermezzo(PaginaIntermezzo pagina, int numeroPagina, int totalePagine) {
        super(TipoEvento.NOTIFICA_PAGINA_INTERMEZZO);
        this.pagina = pagina;
        this.numeroPagina = numeroPagina;
        this.totalePagine = totalePagine;
    }

    public PaginaIntermezzo getPagina() {
        return pagina;
    }

    public int getNumeroPagina() {
        return numeroPagina;
    }

    public int getTotalePagine() {
        return totalePagine;
    }
}
