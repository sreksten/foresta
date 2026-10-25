package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.artefatti.OggettoConPeso;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.eventi.notifiche.NotificaApprovazionePrelievoArtefatto;
import com.threeamigos.foresta.eventi.notifiche.NotificaRifiutoPrelievoArtefatto;
import com.threeamigos.foresta.interfacce.VistaScambio;
import com.threeamigos.foresta.artefatti.ScambiatoreArtefatti;

/**
 * Il giocatore chiede di poter spostare un Artefatto dall'inventario generale a un Personaggio.
 * L'esito viene deciso dal peso dell'Artefatto.
 *
 * @author Stefano Reksten
 */
public class InternoPrelievoArtefatto extends InternoSpostamentoArtefatto<OggettoConPeso> {

    /**
     * @param parteAttiva l'inventario di un Personaggio
     * @param parteRemota l'inventario generale del GruppoGiocatore
     * @param oggettoDaPrelevare l'oggetto di interesse della transazione
     * @param scambio lo scambio aperto da cui viene, o null
     */
    public InternoPrelievoArtefatto(ScambiatoreArtefatti parteAttiva, ScambiatoreArtefatti parteRemota,
                                    OggettoConPeso oggettoDaPrelevare, VistaScambio scambio) {
        super(TipoEvento.INTERNO_PRELIEVO_ARTEFATTO, parteAttiva, parteRemota, oggettoDaPrelevare, scambio);
    }

    /**
     * La notifica con cui si approva la richiesta.
     */
    public NotificaApprovazionePrelievoArtefatto approvazione() {
        return new NotificaApprovazionePrelievoArtefatto(getScambio(), getOggettoDaSpostare(), getUuid());
    }

    /**
     * La notifica con cui si rifiuta la richiesta.
     */
    public NotificaRifiutoPrelievoArtefatto rifiuto(String motivo) {
        return new NotificaRifiutoPrelievoArtefatto(getScambio(), getOggettoDaSpostare(), getUuid(), motivo);
    }
}
