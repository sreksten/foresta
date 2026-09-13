package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.motore.OggettoConPeso;
import com.threeamigos.foresta.motore.ScambiatoreArtefatti;
import com.threeamigos.foresta.motore.TipoSpostamento;

/**
 * Il giocatore chiede di poter spostare un Artefatto dall'inventario generale a un Personaggio.
 * L'esito viene deciso dal peso dell'oggetto.
 *
 * @author Stefano Reksten
 */
public class EventoRichiestaPrelievoArtefatto extends EventoRichiestaSpostamentoArtefatto<OggettoConPeso> {

    /**
     * @param parteAttiva l'inventario di un Personaggio
     * @param parteRemota l'inventario generale del GruppoGiocatore
     * @param oggettoDaPrelevare l'oggetto di interesse della transazione
     */
    public EventoRichiestaPrelievoArtefatto(ScambiatoreArtefatti parteAttiva, ScambiatoreArtefatti parteRemota,
                                            OggettoConPeso oggettoDaPrelevare) {
        super(parteAttiva, parteRemota, TipoSpostamento.SPOSTAMENTO_DA_INVENTARIO_GRUPPO_A_PERSONAGGIO, oggettoDaPrelevare);
    }
}
