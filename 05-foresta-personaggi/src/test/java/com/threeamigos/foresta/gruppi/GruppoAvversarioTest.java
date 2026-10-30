package com.threeamigos.foresta.gruppi;

import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Il gruppo degli avversari: ogni specie conta per sé, e chi cade ed è tolto dal gruppo non sfugge al conteggio degli sconfitti.
 */
class GruppoAvversarioTest {

    @BeforeEach
    void prepara() {
        DatiDiProva.nuovaPartita();
        GruppoAvversario.azzeraIstanza();
    }

    private static Personaggio crea(TipoPersonaggio tipo) {
        return FabbricaPersonaggi.crea(tipo, 1);
    }

    @Test
    void ognuniPrendeIlNumeroDellaSuaSpecieENonFaIlGiocatore() {
        GruppoAvversario gruppo = GruppoAvversario.getIstanza();
        assertSame(gruppo, GruppoAvversario.getIstanza());
        assertFalse(gruppo.isGruppoGiocatore());
        Personaggio g1 = crea(TipoPersonaggio.GOBLIN);
        Personaggio t1 = crea(TipoPersonaggio.TROLL);
        Personaggio g2 = crea(TipoPersonaggio.GOBLIN);
        gruppo.aggiungiPersonaggio(g1);
        gruppo.aggiungiPersonaggio(t1);
        gruppo.aggiungiPersonaggio(g2);
        assertEquals(1, g1.getOrdinale());
        assertEquals(1, t1.getOrdinale());
        assertEquals(2, g2.getOrdinale());
        assertEquals(2, gruppo.contaDellaStessaSpecie(g1));
        assertEquals(1, gruppo.contaDellaStessaSpecie(t1));
    }

    @Test
    void ilPrimoVivoSaltaIcaduti() {
        GruppoAvversario gruppo = GruppoAvversario.getIstanza();
        assertNull(gruppo.getPersonaggioVivo());
        Personaggio a = crea(TipoPersonaggio.GOBLIN);
        Personaggio b = crea(TipoPersonaggio.TROLL);
        gruppo.aggiungiPersonaggio(a);
        gruppo.aggiungiPersonaggio(b);
        a.muore("prova");
        assertSame(b, gruppo.getPersonaggioVivo());
        b.muore("prova");
        assertNull(gruppo.getPersonaggioVivo());
    }

    @Test
    void chiCadeEVieneToltoRestaFraICadutiRimossiUnaVoltaSola() {
        GruppoAvversario gruppo = GruppoAvversario.getIstanza();
        Personaggio caduto = crea(TipoPersonaggio.GOBLIN);
        Personaggio vivo = crea(TipoPersonaggio.TROLL);
        Personaggio estraneo = crea(TipoPersonaggio.TROLL);
        gruppo.aggiungiPersonaggio(caduto);
        gruppo.aggiungiPersonaggio(vivo);
        caduto.muore("prova");
        estraneo.muore("prova");
        gruppo.rimuoviPersonaggio(estraneo);
        gruppo.rimuoviPersonaggio(vivo);
        assertTrue(gruppo.getCadutiRimossi().isEmpty());
        gruppo.rimuoviPersonaggio(caduto);
        assertEquals(1, gruppo.getCadutiRimossi().size());
        // Toglierlo di nuovo non lo conta due volte
        gruppo.aggiungiPersonaggio(caduto);
        gruppo.rimuoviPersonaggio(caduto);
        assertEquals(1, gruppo.getCadutiRimossi().size());
        assertThrows(UnsupportedOperationException.class, () -> gruppo.getCadutiRimossi().clear());
        gruppo.rimuoviPersonaggi();
        assertTrue(gruppo.getCadutiRimossi().isEmpty());
        // Anche reimpostando il gruppo
        gruppo.aggiungiPersonaggio(caduto);
        gruppo.rimuoviPersonaggio(caduto);
        gruppo.addPozioniSalute(2);
        gruppo.reimposta();
        assertTrue(gruppo.getCadutiRimossi().isEmpty());
        assertEquals(0, gruppo.getPozioniSalute());
    }
}
