package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoAcquistoConsumabile;

/**
 * Il GruppoGiocatore (che "fa da banchiere" controllando le disponibilità economiche del GruppoGiocatore e il costo
 * del Consumabile) rifiuta l'acquisto di un Consumabile da un fornitore per mancanza di fondi.
 *
 * @author Stefano Reksten
 */
public class NotificaRifiutoAcquistoConsumabile extends EventoBase {

    private final ComandoAcquistoConsumabile comandoAcquistoConsumabile;

    /**
     * @param comandoAcquistoConsumabile la richiesta di acquisto di un Consumabile da un commerciante che si rifiuta
     */
    public NotificaRifiutoAcquistoConsumabile(ComandoAcquistoConsumabile comandoAcquistoConsumabile) {
        super(TipoEvento.NOTIFICA_RIFIUTO_ACQUISTO_CONSUMABILE);
        this.comandoAcquistoConsumabile = comandoAcquistoConsumabile;
    }

    /**
     * @return la richiesta di acquisto di un Consumabile da un commerciante che si rifiuta
     */
    public ComandoAcquistoConsumabile getEventoRichiestaAcquistoConsumabile() {
        return comandoAcquistoConsumabile;
    }
}
