package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.eventi.EventoSuPersonaggio;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.interfacce.VistaPersonaggio;
import com.threeamigos.foresta.tipi.TipoDanno;

/**
 * Un Personaggio, per la sua classe, è debole, resiste o è immune a un tipo di danno che ha appena subito (vedi
 * Personaggio.getMoltiplicatoreDannoSubito).
 */
public class NotificaReazioneDiClassePersonaggio extends EventoSuPersonaggio {

    private final TipoDanno tipoDanno;
    private final double moltiplicatore;

    /**
     * @param personaggio il Personaggio che subisce il danno
     * @param tipoDanno il tipo di danno
     * @param moltiplicatore per quanto si moltiplica quel danno contro di lui: 0 se è immune, tra 0 e 1 se resiste, più di 1 se è
     *                       debole
     */
    public NotificaReazioneDiClassePersonaggio(VistaPersonaggio personaggio, TipoDanno tipoDanno, double moltiplicatore) {
        super(TipoEvento.NOTIFICA_REAZIONE_DI_CLASSE_PERSONAGGIO, personaggio);
        this.tipoDanno = tipoDanno;
        this.moltiplicatore = moltiplicatore;
    }

    public TipoDanno getTipoDanno() {
        return tipoDanno;
    }

    public double getMoltiplicatore() {
        return moltiplicatore;
    }

}
