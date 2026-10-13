package com.threeamigos.foresta.eventi.comandigiocatore;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;

/**
 * Il giocatore apre o chiude, con un click, l'elenco dei modificatori di un artefatto o la descrizione di una
 * missione. Lo stato si salva con la partita: l'Automa lo scrive nell'artefatto o nella missione.
 * <p>
 * Porta solo identificativi: l'artefatto sta sempre in una schermata di scambio, e il motore lo cerca nelle sue due
 * parti; se non trova niente, ignora il comando.
 */
public class ComandoCommutazioneElenco extends EventoBase {

    private final String idScambio;
    private final String uuidArtefatto;
    private final String idMissione;

    private ComandoCommutazioneElenco(String idScambio, String uuidArtefatto, String idMissione) {
        super(TipoEvento.COMANDO_COMMUTAZIONE_ELENCO);
        this.idScambio = idScambio;
        this.uuidArtefatto = uuidArtefatto;
        this.idMissione = idMissione;
    }

    /**
     * @param idScambio lo scambio aperto in cui sta l'artefatto
     * @param uuidArtefatto l'artefatto di cui aprire o chiudere l'elenco dei modificatori
     */
    public static ComandoCommutazioneElenco artefatto(String idScambio, String uuidArtefatto) {
        return new ComandoCommutazioneElenco(idScambio, uuidArtefatto, null);
    }

    /**
     * @param idMissione la missione di cui aprire o chiudere la descrizione
     */
    public static ComandoCommutazioneElenco missione(String idMissione) {
        return new ComandoCommutazioneElenco(null, null, idMissione);
    }

    /**
     * Lo scambio in cui sta l'artefatto, o null se il comando riguarda una missione
     */
    public String getIdScambio() {
        return idScambio;
    }

    /**
     * L'artefatto di cui aprire o chiudere l'elenco dei modificatori, o null se il comando riguarda una missione
     */
    public String getUuidArtefatto() {
        return uuidArtefatto;
    }

    /**
     * La missione di cui aprire o chiudere la descrizione, o null se il comando riguarda un artefatto
     */
    public String getIdMissione() {
        return idMissione;
    }
}
