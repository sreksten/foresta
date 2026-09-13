package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.motore.OggettoConPeso;
import com.threeamigos.foresta.motore.ScambiatoreArtefatti;
import com.threeamigos.foresta.motore.TipoSpostamento;

/**
 * Il giocatore chiede di poter spostare un Artefatto da un Personaggio verso l'inventario generale.
 * L'esito viene deciso dal peso dell'oggetto.
 * <p>
 * (NOTA: in questo momento l'inventario generale non ha limite di peso, quindi viene approvato automaticamente).
 *
 * @author Stefano Reksten
 */
public class EventoRichiestaStoccaggioArtefatto extends EventoRichiestaSpostamentoArtefatto<OggettoConPeso> {

    /**
     * @param parteAttiva l'inventario di un Personaggio
     * @param parteRemota l'inventario generale del GruppoGiocatore
     * @param oggettoDaStoccare l'oggetto di interesse della transazione
     */
    public EventoRichiestaStoccaggioArtefatto(ScambiatoreArtefatti parteAttiva, ScambiatoreArtefatti parteRemota,
                                              OggettoConPeso oggettoDaStoccare) {
        super(parteAttiva, parteRemota, TipoSpostamento.SPOSTAMENTO_DA_PERSONAGGIO_A_INVENTARIO_GRUPPO, oggettoDaStoccare);
    }
}
