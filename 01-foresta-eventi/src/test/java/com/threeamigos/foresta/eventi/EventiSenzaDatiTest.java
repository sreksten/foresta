package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.eventi.interni.InternoFineLocazione;
import com.threeamigos.foresta.eventi.interni.InternoMostraFinestraIncantesimiEPozioni;
import com.threeamigos.foresta.eventi.interni.InternoMostraFinestraStato;
import com.threeamigos.foresta.eventi.interni.InternoPassaggioInosservato;
import com.threeamigos.foresta.eventi.interni.InternoPreparazioneLocazione;
import com.threeamigos.foresta.eventi.interni.InternoRichiestaRefreshUI;
import com.threeamigos.foresta.eventi.notifiche.NotificaGlobale;
import com.threeamigos.foresta.tipi.TipoEvento;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Gli eventi che non portano dati del dominio: ognuno dichiara il suo tipo, che è ciò su cui il bus li distingue nei log.
 */
class EventiSenzaDatiTest {

    @Test
    void ognunoDichiaraIlSuoTipo() {
        assertEquals(TipoEvento.INTERNO_FINE_LOCAZIONE, new InternoFineLocazione().getTipoEvento());
        assertEquals(TipoEvento.INTERNO_MOSTRA_FINESTRA_INCANTESIMI_E_POZIONI, new InternoMostraFinestraIncantesimiEPozioni().getTipoEvento());
        assertEquals(TipoEvento.INTERNO_MOSTRA_FINESTRA_STATO, new InternoMostraFinestraStato().getTipoEvento());
        assertEquals(TipoEvento.INTERNO_PASSAGGIO_INOSSERVATO, new InternoPassaggioInosservato().getTipoEvento());
        assertEquals(TipoEvento.INTERNO_PREPARAZIONE_LOCAZIONE, new InternoPreparazioneLocazione().getTipoEvento());
        assertEquals(TipoEvento.INTERNO_RICHIESTA_REFRESH_UI, new InternoRichiestaRefreshUI().getTipoEvento());
    }

    @Test
    void laNotificaGlobalePortaEtichettaEMessaggio() {
        NotificaGlobale notifica = new NotificaGlobale("Nuova missione!", "Fai questa cosa");
        assertEquals(TipoEvento.NOTIFICA_GLOBALE, notifica.getTipoEvento());
        assertEquals("Nuova missione!", notifica.getEtichetta());
        assertEquals("Fai questa cosa", notifica.getMessaggio());
    }
}
