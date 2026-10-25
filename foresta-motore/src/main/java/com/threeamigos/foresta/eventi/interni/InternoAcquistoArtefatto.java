package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.artefatti.OggettoConCosto;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.eventi.notifiche.NotificaApprovazioneAcquistoArtefatto;
import com.threeamigos.foresta.eventi.notifiche.NotificaRifiutoAcquistoArtefatto;
import com.threeamigos.foresta.artefatti.VistaScambio;
import com.threeamigos.foresta.artefatti.ScambiatoreArtefatti;

/**
 * Il giocatore chiede di poter comprare un Artefatto da un commerciante e metterlo nell'inventario generale.
 * L'esito viene deciso dal costo dell'Artefatto.
 *
 * @author Stefano Reksten
 */
public class InternoAcquistoArtefatto extends InternoSpostamentoArtefatto<OggettoConCosto> {

    /**
     * @param parteAttiva l'inventario generale del GruppoGiocatore
     * @param parteRemota il commerciante
     * @param oggettoDaAcquistare l'oggetto di interesse della transazione
     * @param scambio lo scambio aperto da cui viene, o null
     */
    public InternoAcquistoArtefatto(ScambiatoreArtefatti parteAttiva, ScambiatoreArtefatti parteRemota,
                                    OggettoConCosto oggettoDaAcquistare, VistaScambio scambio) {
        super(TipoEvento.INTERNO_ACQUISTO_ARTEFATTO, parteAttiva, parteRemota, oggettoDaAcquistare, scambio);
    }

    /**
     * La notifica con cui si approva la richiesta.
     */
    public NotificaApprovazioneAcquistoArtefatto approvazione() {
        return new NotificaApprovazioneAcquistoArtefatto(getScambio(), getOggettoDaSpostare(), getUuid());
    }

    /**
     * La notifica con cui si rifiuta la richiesta.
     */
    public NotificaRifiutoAcquistoArtefatto rifiuto() {
        return new NotificaRifiutoAcquistoArtefatto(getScambio(), getOggettoDaSpostare(), getUuid());
    }
}
