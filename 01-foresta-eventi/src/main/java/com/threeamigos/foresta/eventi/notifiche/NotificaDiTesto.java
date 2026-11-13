package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.tipi.TipoEvento;

/**
 * Un testo che il motore manda al giocatore, da mostrare nel riquadro di testo. Ce ne sono due specie,
 * {@link NotificaTestoFrase} (continua il paragrafo in corso) e {@link NotificaTestoParagrafo} (ne comincia uno nuovo):
 * sono due classi e non una con un'opzione perché il bus smista gli eventi per classe, e la UI si iscrive a ognuna.
 */
public abstract class NotificaDiTesto extends EventoBase {

    private final String messaggio;

    /**
     * Un testo per il giocatore; se comincia con una lettera minuscola (un nome con l'articolo, come "the city of Fleena",
     * messo a inizio frase) la maiuscola gliela dà la notifica.
     */
    protected NotificaDiTesto(TipoEvento tipoEvento, String messaggio) {
        super(tipoEvento);
        this.messaggio = conInizialeMaiuscola(messaggio);
    }

    /**
     * Il messaggio con il primo carattere maiuscolo (null e il testo vuoto restano come sono).
     */
    static String conInizialeMaiuscola(String messaggio) {
        if (messaggio == null || messaggio.isEmpty() || Character.isUpperCase(messaggio.charAt(0))) {
            return messaggio;
        }
        return Character.toUpperCase(messaggio.charAt(0)) + messaggio.substring(1);
    }

    /**
     * Il messaggio da mostrare al giocatore.
     */
    public String getMessaggio() {
        return messaggio;
    }

}
