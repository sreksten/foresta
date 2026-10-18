package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoSuPersonaggio;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.interfacce.VistaPersonaggio;
import com.threeamigos.foresta.tipi.tattiche.TipoAzione;

import java.util.Collections;
import java.util.List;

/**
 * Un personaggio ha deciso che cosa fare scorrendo la sua tattica (tattiche_di_combattimento_automatiche.md §7.7). Porta l'azione scelta, il bersaglio (se
 * c'è), la voce e il percorso: quali voci ha saltato e perché, i candidati e le misure con cui ha scelto, i danni attesi confrontati. Serve a mettere
 * a punto le tattiche (un <code>grep INTERNO_DECISIONE_TATTICA</code> sul log), ai test e alla UI, che può spiegare al giocatore perché un personaggio
 * ha agito così. Non cambia niente nel gioco: viene pubblicato prima che l'azione si esegua.
 */
public class InternoDecisioneTattica extends EventoSuPersonaggio {

    private final VistaPersonaggio bersaglio;
    private final TipoAzione azione;
    private final int indiceVoce;
    private final List<String> spiegazione;

    /**
     * @param personaggio il personaggio che decide
     * @param bersaglio   il bersaglio, o null se l'azione agisce su un gruppo o non ne ha
     * @param azione      l'azione che esegue (per l'incantesimo di danno, quello scelto)
     * @param indiceVoce  la posizione della voce scelta nella tattica, da 0; -1 se nessuna era eseguibile e si è ripiegato sull'attacco fisico
     * @param spiegazione il percorso, una riga per passo
     */
    public InternoDecisioneTattica(VistaPersonaggio personaggio, VistaPersonaggio bersaglio, TipoAzione azione, int indiceVoce,
                                   List<String> spiegazione) {
        super(TipoEvento.INTERNO_DECISIONE_TATTICA, personaggio);
        this.bersaglio = bersaglio;
        this.azione = azione;
        this.indiceVoce = indiceVoce;
        this.spiegazione = Collections.unmodifiableList(spiegazione);
    }

    public VistaPersonaggio getBersaglio() {
        return bersaglio;
    }

    public TipoAzione getAzione() {
        return azione;
    }

    /**
     * La posizione della voce scelta, da 0; -1 per il ripiego sull'attacco fisico
     */
    public int getIndiceVoce() {
        return indiceVoce;
    }

    public List<String> getSpiegazione() {
        return spiegazione;
    }

    public boolean isRipiego() {
        return indiceVoce < 0;
    }

}
