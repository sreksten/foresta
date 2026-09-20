package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.eventi.EventoSuPersonaggio;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.motore.modellodati.ModificatoreAttributo;
import com.threeamigos.foresta.personaggi.Personaggio;

/**
 * Un Personaggio riceve un Modificatore di Attributo
 *
 * @author Stefano Reksten
 */
public class NotificaAggiuntaModificatorePersonaggio extends EventoSuPersonaggio {

    private final ModificatoreAttributo modificatore;

    /**
     * @param personaggio il Personaggio che riceve il Modificatore di Attributo
     * @param modificatore il Modificatore di Attributo che viene aggiunto al Personaggio
     */
    public NotificaAggiuntaModificatorePersonaggio(Personaggio personaggio, ModificatoreAttributo modificatore) {
        super(TipoEvento.NOTIFICA_AGGIUNTA_MODIFICATORE_PERSONAGGIO, personaggio);
        this.modificatore = modificatore;
    }

    public ModificatoreAttributo getModificatore() {
        return modificatore;
    }
}
