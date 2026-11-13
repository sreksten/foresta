package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.artefatti.OggettoConPeso;
import com.threeamigos.foresta.tipi.TipoEvento;
import com.threeamigos.foresta.eventi.notifiche.NotificaApprovazioneStoccaggioArtefatto;
import com.threeamigos.foresta.artefatti.VistaScambio;
import com.threeamigos.foresta.artefatti.ScambiatoreArtefatti;

/**
 * Il giocatore chiede di poter spostare un Artefatto da un Personaggio verso l'inventario del gruppo.
 * <p>
 * L'inventario generale non ha limite di peso, quindi GruppoGiocatore approva sempre ({@link #approvazione()}).
 *
 * @author Stefano Reksten
 */
public class InternoStoccaggioArtefatto extends InternoSpostamentoArtefatto<OggettoConPeso> {

    /**
     * @param parteAttiva l'inventario di un Personaggio
     * @param parteRemota l'inventario generale del GruppoGiocatore
     * @param oggettoDaStoccare l'oggetto di interesse della transazione
     * @param scambio lo scambio aperto da cui viene, o null
     */
    public InternoStoccaggioArtefatto(ScambiatoreArtefatti parteAttiva, ScambiatoreArtefatti parteRemota,
                                      OggettoConPeso oggettoDaStoccare, VistaScambio scambio) {
        super(TipoEvento.INTERNO_STOCCAGGIO_ARTEFATTO, parteAttiva, parteRemota, oggettoDaStoccare, scambio);
    }

    /**
     * La notifica con cui si approva la richiesta.
     */
    public NotificaApprovazioneStoccaggioArtefatto approvazione() {
        return new NotificaApprovazioneStoccaggioArtefatto(getScambio(), getOggettoDaSpostare(), getUuid());
    }
}
