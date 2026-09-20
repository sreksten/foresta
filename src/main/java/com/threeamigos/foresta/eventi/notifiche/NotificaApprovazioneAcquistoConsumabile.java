package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoAcquistoConsumabile;

/**
 * Il GruppoGiocatore (che "fa da banchiere" controllando le disponibilità economiche del GruppoGiocatore e il costo
 * del Consumabile) approva l'acquisto di un Consumabile da un fornitore. Il Consumabile passa nell'inventario del gruppo.
 *
 * @author Stefano Reksten
 */
public class NotificaApprovazioneAcquistoConsumabile extends EventoBase {

    private final ComandoAcquistoConsumabile comandoAcquistoConsumabile;

    /**
     * @param comandoAcquistoConsumabile la richiesta di acquisto di un Consumabile da un commerciante che si approva
     */
    public NotificaApprovazioneAcquistoConsumabile(ComandoAcquistoConsumabile comandoAcquistoConsumabile) {
        super(TipoEvento.NOTIFICA_APPROVAZIONE_ACQUISTO_CONSUMABILE);
        this.comandoAcquistoConsumabile = comandoAcquistoConsumabile;
    }

    /**
     * @return la richiesta di acquisto di un Consumabile da un commerciante che si approva
     */
    public ComandoAcquistoConsumabile getEventoRichiestaAcquistoConsumabile() {
        return comandoAcquistoConsumabile;
    }

}
