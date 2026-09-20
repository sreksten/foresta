package com.threeamigos.foresta.eventi.comandigiocatore;

import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.motore.OggettoConPeso;
import com.threeamigos.foresta.motore.ScambiatoreArtefatti;

/**
 * Il giocatore chiede di poter spostare un Artefatto dall'inventario generale a un Personaggio.
 * L'esito viene deciso dal peso dell'Artefatto.
 *
 * @author Stefano Reksten
 */
public class ComandoPrelievoArtefatto extends ComandoSpostamentoArtefatto<OggettoConPeso> {

    /**
     * @param parteAttiva l'inventario di un Personaggio
     * @param parteRemota l'inventario generale del GruppoGiocatore
     * @param oggettoDaPrelevare l'oggetto di interesse della transazione
     */
    public ComandoPrelievoArtefatto(ScambiatoreArtefatti parteAttiva, ScambiatoreArtefatti parteRemota,
                                    OggettoConPeso oggettoDaPrelevare) {
        super(TipoEvento.COMANDO_PRELIEVO_ARTEFATTO, parteAttiva, parteRemota, oggettoDaPrelevare);
    }
}
