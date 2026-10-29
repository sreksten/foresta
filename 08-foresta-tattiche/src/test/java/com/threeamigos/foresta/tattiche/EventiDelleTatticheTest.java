package com.threeamigos.foresta.tattiche;

import com.threeamigos.foresta.eventi.comandigiocatore.ComandoImpostaTattica;
import com.threeamigos.foresta.eventi.interni.InternoDecisioneTattica;
import com.threeamigos.foresta.eventi.interni.InternoRichiestaAperturaFinestraCombattimento;
import com.threeamigos.foresta.eventi.richieste.RichiestaAperturaTattiche;
import com.threeamigos.foresta.modellodati.TatticaMD;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoAzione;
import com.threeamigos.foresta.tipi.TipoEvento;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Gli eventi che portano le tattiche fra il motore e la UI.
 */
class EventiDelleTatticheTest {

    private final Personaggio guerriero = FabbricaPersonaggi.crea(TipoPersonaggio.GUERRIERO, 1);
    private final Personaggio goblin = FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 1);

    @Test
    void laDecisioneRaccontaChiHaFattoCheCosaESuChi() {
        List<String> spiegazione = Arrays.asList("voce 1: bersaglio Goblin", "voce 1: ATTACCO_FISICO su Goblin");
        InternoDecisioneTattica decisione = new InternoDecisioneTattica(guerriero, goblin, TipoAzione.ATTACCO_FISICO, 0, spiegazione);

        assertEquals(TipoEvento.INTERNO_DECISIONE_TATTICA, decisione.getTipoEvento());
        assertSame(guerriero, decisione.getPersonaggio());
        assertSame(goblin, decisione.getBersaglio());
        assertEquals(TipoAzione.ATTACCO_FISICO, decisione.getAzione());
        assertEquals(0, decisione.getIndiceVoce());
        assertEquals(spiegazione, decisione.getSpiegazione());
        assertFalse(decisione.isRipiego());
        assertThrows(UnsupportedOperationException.class, () -> decisione.getSpiegazione().add("altro"));
    }

    @Test
    void unaDecisioneSenzaVoceEIlRipiego() {
        InternoDecisioneTattica ripiego = new InternoDecisioneTattica(guerriero, goblin, TipoAzione.ATTACCO_FISICO, -1, Collections.emptyList());
        assertTrue(ripiego.isRipiego());
    }

    @Test
    void laRichiestaDiAprireIlRiquadroDelCombattimentoNominaChiCombatteEContro() {
        InternoRichiestaAperturaFinestraCombattimento richiesta = new InternoRichiestaAperturaFinestraCombattimento(guerriero, goblin);
        assertEquals(TipoEvento.INTERNO_RICHIESTA_APERTURA_FINESTRA_COMBATTIMENTO, richiesta.getTipoEvento());
        assertSame(guerriero, richiesta.getPersonaggio());
        assertSame(goblin, richiesta.getAvversario());
    }

    @Test
    void ilComandoDiImpostareUnaTatticaPortaIlPersonaggioELaTattica() {
        TatticaMD tattica = TatticaMD.di();
        ComandoImpostaTattica comando = new ComandoImpostaTattica("uuid-1", tattica);
        assertEquals(TipoEvento.COMANDO_IMPOSTA_TATTICA, comando.getTipoEvento());
        assertEquals("uuid-1", comando.getUuidPersonaggio());
        assertSame(tattica, comando.getTattica());
    }

    @Test
    void laRichiestaDiAprireLaSchermataDelleTatticheOffreLeSueScelte() {
        TatticaMD attuale = TatticaMD.di();
        TatticaMD predefinita = TatticaMD.di();
        Set<TipoAzione> possibili = EnumSet.of(TipoAzione.ATTACCO_FISICO);
        RichiestaAperturaTattiche richiesta = new RichiestaAperturaTattiche(Collections.singletonList(Comando.ANNULLA), guerriero, attuale,
                predefinita, possibili);

        assertEquals(TipoEvento.RICHIESTA_APERTURA_TATTICHE, richiesta.getTipoEvento());
        assertEquals(Collections.singletonList(Comando.ANNULLA), richiesta.getPossibilita());
        assertSame(guerriero, richiesta.getPersonaggio());
        assertSame(attuale, richiesta.getTattica());
        assertSame(predefinita, richiesta.getTatticaPredefinita());
        assertSame(possibili, richiesta.getAzioniPossibili());
    }
}
