package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoSuPersonaggio;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.interfacce.VistaPersonaggio;

/**
 * Un personaggio, di uno dei due gruppi, sta per compiere la sua azione in combattimento (vedi StatoDelCombattimento): la finestra
 * del combattimento mostra chi agisce e, se c'è, il bersaglio. Il bersaglio manca quando l'azione non ne ha uno (un'Alba Sacra, una
 * pozione, un canto sul gruppo).
 */
public class InternoAzioneInCombattimento extends EventoSuPersonaggio {

    private final VistaPersonaggio bersaglio;

    /**
     * @param attore    chi agisce
     * @param bersaglio chi subisce l'azione, o null se non ce n'è uno solo
     */
    public InternoAzioneInCombattimento(VistaPersonaggio attore, VistaPersonaggio bersaglio) {
        super(TipoEvento.INTERNO_AZIONE_IN_COMBATTIMENTO, attore);
        this.bersaglio = bersaglio;
    }

    public VistaPersonaggio getBersaglio() {
        return bersaglio;
    }

}
