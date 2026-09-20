package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.eventi.EventoSuPersonaggio;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.personaggi.Personaggio;

/**
 * Lo stato vitale di un Personaggio cambia (muore o risorge).
 *
 * @author Stefano Reksten
 */
public class NotificaVariazioneStatoVitalePersonaggio extends EventoSuPersonaggio {

    private final boolean vivo;

    /**
     * @param personaggio il Personaggio che subisce la variazione
     * @param vivo se il personaggio dopo la variazione è vivo o morto
     */
    public NotificaVariazioneStatoVitalePersonaggio(Personaggio personaggio, boolean vivo) {
        super(TipoEvento.NOTIFICA_VARIAZIONE_STATO_VITALE_PERSONAGGIO, personaggio);
        this.vivo = vivo;
    }

    /**
     * @return true se il personaggio dopo la variazione è vivo, false altrimenti
     */
    public boolean isVivo() {
        return vivo;
    }

}
