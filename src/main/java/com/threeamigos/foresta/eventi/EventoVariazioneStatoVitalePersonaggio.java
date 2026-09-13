package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.personaggi.Personaggio;

/**
 * Lo stato vitale di un Personaggio cambia (muore o risorge).
 *
 * @author Stefano Reksten
 */
public class EventoVariazioneStatoVitalePersonaggio extends EventoPersonaggio {

    private final boolean vivo;

    /**
     * @param personaggio il Personaggio che subisce la variazione
     * @param vivo se il personaggio dopo la variazione è vivo o morto
     */
    public EventoVariazioneStatoVitalePersonaggio(Personaggio personaggio, boolean vivo) {
        super(TipoEvento.PERSONAGGIO_VARIAZIONE_STATO_VITALE, personaggio);
        this.vivo = vivo;
    }

    /**
     * @return true se il personaggio dopo la variazione è vivo, false altrimenti
     */
    public boolean isVivo() {
        return vivo;
    }

}
