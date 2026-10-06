package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.interfacce.OggettoConPeso;
import com.threeamigos.foresta.interfacce.VistaScambio;
import com.threeamigos.foresta.motore.ScambiatoreArtefatti;

/**
 * Il giocatore chiede di poter spostare un Artefatto da un Personaggio verso l'inventario del gruppo.
 * L'esito viene deciso dal peso dell'Artefatto.
 * <p>
 * (NOTA: in questo momento l'inventario generale non ha limite di peso, quindi viene approvato automaticamente).
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
}
