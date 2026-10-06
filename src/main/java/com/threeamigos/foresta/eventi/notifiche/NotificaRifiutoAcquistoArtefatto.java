package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.eventi.interni.InternoAcquistoArtefatto;
import com.threeamigos.foresta.interfacce.OggettoConCosto;

/**
 * Il GruppoGiocatore (che "fa da banchiere" controllando le disponibilità economiche del GruppoGiocatore e il costo
 * dell'Artefatto) rifiuta l'acquisto di un Artefatto da un commerciante per mancanza di fondi.
 *
 * @author Stefano Reksten
 */
public class NotificaRifiutoAcquistoArtefatto extends NotificaRifiutoSpostamentoArtefatto<OggettoConCosto> {

    /**
     * @param comandoAcquistoArtefatto la richiesta di acquisto di un Artefatto che si rifiuta
     */
    public NotificaRifiutoAcquistoArtefatto(InternoAcquistoArtefatto comandoAcquistoArtefatto) {
        super(TipoEvento.NOTIFICA_RIFIUTO_ACQUISTO_ARTEFATTO, comandoAcquistoArtefatto);
    }
}
