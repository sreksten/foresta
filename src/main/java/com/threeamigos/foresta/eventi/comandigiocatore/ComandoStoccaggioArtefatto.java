package com.threeamigos.foresta.eventi.comandigiocatore;

import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.motore.OggettoConPeso;
import com.threeamigos.foresta.motore.ScambiatoreArtefatti;

/**
 * Il giocatore chiede di poter spostare un Artefatto da un Personaggio verso l'inventario del gruppo.
 * L'esito viene deciso dal peso dell'Artefatto.
 * <p>
 * (NOTA: in questo momento l'inventario generale non ha limite di peso, quindi viene approvato automaticamente).
 *
 * @author Stefano Reksten
 */
public class ComandoStoccaggioArtefatto extends ComandoSpostamentoArtefatto<OggettoConPeso> {

    /**
     * @param parteAttiva l'inventario di un Personaggio
     * @param parteRemota l'inventario generale del GruppoGiocatore
     * @param oggettoDaStoccare l'oggetto di interesse della transazione
     */
    public ComandoStoccaggioArtefatto(ScambiatoreArtefatti parteAttiva, ScambiatoreArtefatti parteRemota,
                                      OggettoConPeso oggettoDaStoccare) {
        super(TipoEvento.COMANDO_STOCCAGGIO_ARTEFATTO, parteAttiva, parteRemota, oggettoDaStoccare);
    }
}
