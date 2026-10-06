package com.threeamigos.foresta.eventi.comandigiocatore;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.interfacce.VistaArtefatto;
import com.threeamigos.foresta.interfacce.VistaMissione;

/**
 * Il giocatore apre o chiude, con un click, l'elenco dei modificatori di un artefatto o la descrizione di una
 * missione. Lo stato si salva con la partita: l'Automa lo scrive nell'artefatto o nella missione.
 */
public class ComandoCommutazioneElenco extends EventoBase {

    private final VistaArtefatto artefatto;
    private final VistaMissione missione;

    private ComandoCommutazioneElenco(VistaArtefatto artefatto, VistaMissione missione) {
        super(TipoEvento.COMANDO_COMMUTAZIONE_ELENCO);
        this.artefatto = artefatto;
        this.missione = missione;
    }

    public static ComandoCommutazioneElenco di(VistaArtefatto artefatto) {
        return new ComandoCommutazioneElenco(artefatto, null);
    }

    public static ComandoCommutazioneElenco di(VistaMissione missione) {
        return new ComandoCommutazioneElenco(null, missione);
    }

    /**
     * L'artefatto di cui aprire o chiudere l'elenco dei modificatori, o null se il comando riguarda una missione
     */
    public VistaArtefatto getArtefatto() {
        return artefatto;
    }

    /**
     * La missione di cui aprire o chiudere la descrizione, o null se il comando riguarda un artefatto
     */
    public VistaMissione getMissione() {
        return missione;
    }
}
