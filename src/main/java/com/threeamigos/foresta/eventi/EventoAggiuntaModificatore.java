package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.motore.modellodati.ModificatoreAttributo;
import com.threeamigos.foresta.personaggi.Personaggio;

/**
 * Un Personaggio riceve un Modificatore di Attributo
 *
 * @author Stefano Reksten
 */
public class EventoAggiuntaModificatore extends EventoPersonaggio {

    private final ModificatoreAttributo modificatore;

    /**
     * @param personaggio il Personaggio che riceve il Modificatore di Attributo
     * @param modificatore il Modificatore di Attributo che viene aggiunto al Personaggio
     */
    public EventoAggiuntaModificatore(Personaggio personaggio, ModificatoreAttributo modificatore) {
        super(TipoEvento.PERSONAGGIO_AGGIUNTA_MODIFICATORE, personaggio);
        this.modificatore = modificatore;
    }

    public ModificatoreAttributo getModificatore() {
        return modificatore;
    }
}
