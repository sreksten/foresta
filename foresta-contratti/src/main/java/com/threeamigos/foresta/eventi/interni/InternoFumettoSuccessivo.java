package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.eventi.notifiche.NotificaPaginaIntermezzo;

/**
 * Chiede alla UI di saltare al fumetto successivo nella pagina di un intermezzo mostrata per ultima (vedi
 * {@link NotificaPaginaIntermezzo}), perché il giocatore l'ha chiesto con la pergamena: la pagina va mostrata come
 * appare a quei secondi da quando è comparsa, cioè quando quel fumetto comincia, e da lì prosegue.
 */
public class InternoFumettoSuccessivo extends EventoBase {

    private final double secondi;

    public InternoFumettoSuccessivo(double secondi) {
        super(TipoEvento.INTERNO_FUMETTO_SUCCESSIVO);
        this.secondi = secondi;
    }

    public double getSecondi() {
        return secondi;
    }
}
