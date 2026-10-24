package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.eventi.notifiche.NotificaApprovazioneVenditaArtefatto;
import com.threeamigos.foresta.eventi.notifiche.NotificaRifiutoVenditaArtefatto;
import com.threeamigos.foresta.artefatti.OggettoConCosto;
import com.threeamigos.foresta.interfacce.VistaScambio;
import com.threeamigos.foresta.motore.ScambiatoreArtefatti;

/**
 * Il giocatore chiede di poter vendere un Artefatto presente nell'inventario del gruppo a un commerciante.
 * L'esito viene deciso dal costo dell'Artefatto.
 * <p>
 * (NOTA: in questo momento i commercianti non hanno limite di budget, quindi la vendita viene approvata automaticamente.)
 *
 * @author Stefano Reksten
 */
public class InternoVenditaArtefatto extends InternoSpostamentoArtefatto<OggettoConCosto> {

    /**
     * @param parteAttiva l'inventario generale del GruppoGiocatore
     * @param parteRemota il commerciante
     * @param oggettoDaVendere l'oggetto di interesse della transazione
     * @param scambio lo scambio aperto da cui viene, o null
     */
    public InternoVenditaArtefatto(ScambiatoreArtefatti parteAttiva, ScambiatoreArtefatti parteRemota,
                                   OggettoConCosto oggettoDaVendere, VistaScambio scambio) {
        super(TipoEvento.INTERNO_VENDITA_ARTEFATTO, parteAttiva, parteRemota, oggettoDaVendere, scambio);
    }

    /**
     * La notifica con cui si approva la richiesta.
     */
    public NotificaApprovazioneVenditaArtefatto approvazione() {
        return new NotificaApprovazioneVenditaArtefatto(getScambio(), getOggettoDaSpostare(), getUuid());
    }

    /**
     * La notifica con cui si rifiuta la richiesta.
     */
    public NotificaRifiutoVenditaArtefatto rifiuto() {
        return new NotificaRifiutoVenditaArtefatto(getScambio(), getOggettoDaSpostare(), getUuid());
    }
}
