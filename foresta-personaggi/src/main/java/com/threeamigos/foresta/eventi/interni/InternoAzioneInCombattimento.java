package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoSuPersonaggio;
import com.threeamigos.foresta.tipi.TipoEvento;
import com.threeamigos.foresta.interfacce.VistaPersonaggio;
import com.threeamigos.foresta.tipi.TipoAzione;

/**
 * Un personaggio, di uno dei due gruppi, sta per compiere la sua azione in combattimento (vedi StatoDelCombattimento): la finestra
 * del combattimento mostra chi agisce e, se c'è, il bersaglio. Il bersaglio manca quando l'azione non ne ha uno (un'Alba Sacra, una
 * pozione, un canto sul gruppo).
 */
public class InternoAzioneInCombattimento extends EventoSuPersonaggio {

    private final VistaPersonaggio bersaglio;
    private final TipoAzione azione;

    /**
     * @param attore    chi agisce
     * @param bersaglio chi subisce l'azione, o null se non ce n'è uno solo
     */
    public InternoAzioneInCombattimento(VistaPersonaggio attore, VistaPersonaggio bersaglio) {
        this(attore, bersaglio, null);
    }

    /**
     * @param attore    chi agisce
     * @param bersaglio chi subisce l'azione (o su cui si compie), o null se non ce n'è uno solo
     * @param azione    che cosa fa, per scriverlo sotto il suo nome quando non lo ha deciso una tattica, o null se è un attacco generico
     */
    public InternoAzioneInCombattimento(VistaPersonaggio attore, VistaPersonaggio bersaglio, TipoAzione azione) {
        super(TipoEvento.INTERNO_AZIONE_IN_COMBATTIMENTO, attore);
        this.bersaglio = bersaglio;
        this.azione = azione;
    }

    /**
     * Che cosa fa chi agisce, o null se non è specificato (un attacco con le armi).
     */
    public TipoAzione getAzione() {
        return azione;
    }

    public VistaPersonaggio getBersaglio() {
        return bersaglio;
    }

}
