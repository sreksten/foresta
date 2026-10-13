package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.eventi.EventoSuPersonaggio;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.interfacce.VistaPersonaggio;
import com.threeamigos.foresta.modellodati.ModificatoreAttributoMD;

/**
 * Un Personaggio subisce una variazione di un Modificatore di Attributo: lo riceve, gli cambia la durata
 * (rinfrescata da un doppione o scesa di un turno o di una locazione) oppure lo perde.
 *
 * @author Stefano Reksten
 */
public class NotificaVariazioneModificatorePersonaggio extends EventoSuPersonaggio {

    public enum TipoVariazione {
        AGGIUNTA,
        MODIFICA,
        RIMOZIONE
    }

    private final TipoVariazione tipoVariazione;
    private final ModificatoreAttributoMD modificatore;
    private final int durataPrecedente;
    private final int nuovaDurata;

    /**
     * @param personaggio il Personaggio che subisce la variazione del Modificatore di Attributo
     * @param tipoVariazione il tipo di variazione (aggiunta, modifica, rimozione)
     * @param modificatore il Modificatore di Attributo interessato, com'è dopo la variazione (alla rimozione,
     *                     com'era prima)
     * @param durataPrecedente la durata precedente alla variazione (0 se è permanente o se è appena arrivato)
     * @param nuovaDurata la durata successiva alla variazione (0 se è permanente o se è stato tolto)
     */
    public NotificaVariazioneModificatorePersonaggio(VistaPersonaggio personaggio, TipoVariazione tipoVariazione,
                                                     ModificatoreAttributoMD modificatore, int durataPrecedente,
                                                     int nuovaDurata) {
        super(TipoEvento.NOTIFICA_VARIAZIONE_MODIFICATORE_PERSONAGGIO, personaggio);
        this.tipoVariazione = tipoVariazione;
        this.modificatore = modificatore;
        this.durataPrecedente = durataPrecedente;
        this.nuovaDurata = nuovaDurata;
    }

    public TipoVariazione getTipo() {
        return tipoVariazione;
    }

    public ModificatoreAttributoMD getModificatore() {
        return modificatore;
    }

    public int getDurataPrecedente() {
        return durataPrecedente;
    }

    public int getNuovaDurata() {
        return nuovaDurata;
    }
}
