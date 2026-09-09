package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.motore.modellodati.ModificatoreAttributo;
import com.threeamigos.foresta.personaggi.Personaggio;

/**
 *
 * @author Stefano Reksten
 */
public class EventoAggiuntaModificatore extends EventoPersonaggio {

    private final ModificatoreAttributo modificatore;

    public EventoAggiuntaModificatore(Personaggio personaggio, ModificatoreAttributo modificatore) {
        super(TipoEvento.PERSONAGGIO_AGGIUNTA_MODIFICATORE, personaggio);
        this.modificatore = modificatore;
    }

    public ModificatoreAttributo getModificatore() {
        return modificatore;
    }
}
