package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.eventi.EventoSuPersonaggio;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.motore.modellodati.TipoAttributo;
import com.threeamigos.foresta.personaggi.PersonaggioBase;

/**
 * Un giocatore consuma un Punto Abilità di un Personaggio per aumentargli un certo Attributo.
 *
 * @author Stefano Reksten
 */
public class NotificaConsumoPuntoAbilitaPersonaggio extends EventoSuPersonaggio {

    private final TipoAttributo tipoAttributo;

    /**
     * @param personaggio il Personaggio che consuma il Punto Abilità
     * @param tipoAttributo l'Attributo del Personaggio che viene aumentato
     */
	public NotificaConsumoPuntoAbilitaPersonaggio(PersonaggioBase personaggio, TipoAttributo tipoAttributo) {
		super(TipoEvento.NOTIFICA_CONSUMO_PUNTO_ABILITA_PERSONAGGIO, personaggio);
        this.tipoAttributo = tipoAttributo;
	}

    /**
     * @return l'Attributo del Personaggio che viene aumentato
     */
    public TipoAttributo getTipoAttributo() {
        return tipoAttributo;
    }
}
