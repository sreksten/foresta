package com.threeamigos.foresta.ui.schermate;

import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Lo svolazzo dell'elenco del bestiario sta fra i mostri (l'ultimo è la viverna) e i personaggi giocanti, e ha una riga sua.
 */
class ImmagineBestiarioSeparatoreTest {

    private final List<TipoPersonaggio> voci = TipoPersonaggio.getVociDelBestiario();

    @Test
    void ilSeparatoreStaDopoLaViverna() {
        int separatore = ImmagineBestiario.indiceDelSeparatore(voci);
        assertTrue(separatore > 0);
        assertEquals(TipoPersonaggio.VIVERNA, voci.get(separatore - 1));
        assertNotEquals(TipoPersonaggio.VIVERNA, voci.get(separatore));
        assertTrue(voci.get(separatore).isCompagno(), voci.get(separatore).name());
    }

    @Test
    void leRigheSaltanoLaRigaDelSeparatoreEPortanoAllaVoceGiusta() {
        int separatore = ImmagineBestiario.indiceDelSeparatore(voci);
        for (int voce = 0; voce < voci.size(); voce++) {
            int riga = ImmagineBestiario.rigaDellaVoce(voci, voce);
            assertNotEquals(separatore, riga, "nessuna voce sta sulla riga del separatore");
            assertEquals(voce, ImmagineBestiario.voceDellaRiga(voci, riga));
        }
        assertEquals(-1, ImmagineBestiario.voceDellaRiga(voci, separatore), "un clic sul separatore non sceglie niente");
        assertEquals(-1, ImmagineBestiario.voceDellaRiga(voci, voci.size() + 1), "e nemmeno sotto l'ultima voce");
        assertEquals(-1, ImmagineBestiario.voceDellaRiga(voci, -1));
    }
}
