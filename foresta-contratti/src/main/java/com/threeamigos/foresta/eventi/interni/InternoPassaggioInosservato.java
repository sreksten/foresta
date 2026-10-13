package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;

/**
 * Il gruppo è passato inosservato in una locazione, senza combattere (vedi LocazioneBase): le missioni che lo chiedono,
 * come il colpo, lo contano per la casella in cui è successo.
 */
public class InternoPassaggioInosservato extends EventoBase {

    public InternoPassaggioInosservato() {
        super(TipoEvento.INTERNO_PASSAGGIO_INOSSERVATO);
    }
}
