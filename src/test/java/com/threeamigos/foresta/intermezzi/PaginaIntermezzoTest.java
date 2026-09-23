package com.threeamigos.foresta.intermezzi;

import com.threeamigos.foresta.personaggi.ClassePersonaggio;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author Stefano Reksten
 */
class PaginaIntermezzoTest {

    private static final double DELTA = 1e-9;

    private static PaginaIntermezzo paginaConDuePersonaggi() {
        return new PaginaIntermezzo()
                .conElemento(ElementoIntermezzo.personaggio("a", ClassePersonaggio.MAGO, 0.3, 0.7))
                .conElemento(ElementoIntermezzo.personaggio("b", ClassePersonaggio.EREMITA, 0.7, 0.7));
    }

    @Test
    void leBattuteSenzaInizioSeguonoLaPrecedente() {
        PaginaIntermezzo pagina = paginaConDuePersonaggi()
                .conBattuta(BattutaIntermezzo.di("a", "Uno").daSecondo(1).perSecondi(2))
                .conBattuta(BattutaIntermezzo.di("b", "Due").perSecondi(3));

        List<BattutaProgrammata> programma = pagina.getBattuteProgrammate();

        assertEquals(1, programma.get(0).getInizio(), DELTA);
        assertEquals(3, programma.get(0).getFine(), DELTA);
        assertEquals(3 + PaginaIntermezzo.PAUSA_FRA_BATTUTE, programma.get(1).getInizio(), DELTA);
        assertTrue(programma.get(1).isVisibileAl(4));
        assertFalse(programma.get(0).isVisibileAl(3));
    }

    @Test
    void laDurataDiUnaBattutaDipendeDalTesto() {
        BattutaIntermezzo breve = BattutaIntermezzo.di("a", "Sì.");
        BattutaIntermezzo lunga = BattutaIntermezzo.di("a", "Questa è una battuta decisamente più lunga delle altre, per davvero.");

        assertEquals(BattutaIntermezzo.DURATA_MINIMA, breve.getDurata(), DELTA);
        assertTrue(lunga.getDurata() > BattutaIntermezzo.DURATA_MINIMA);
    }

    @Test
    void laDurataDelContenutoIgnoraLeAnimazioniCheSiRipetono() {
        PaginaIntermezzo pagina = paginaConDuePersonaggi()
                .conElemento(ElementoIntermezzo.personaggio("sfondo", ClassePersonaggio.DRAGO, 0, 0)
                        .poi(Tappa.inSecondi(100).verso(1, 0)).ripeti(Ripetizione.CICLICA))
                .conElemento(ElementoIntermezzo.personaggio("entrata", ClassePersonaggio.GOBLIN, 0, 0)
                        .poi(Tappa.inSecondi(5).verso(1, 0)))
                .conBattuta(BattutaIntermezzo.di("a", "Ciao").perSecondi(2));

        assertEquals(5, pagina.getDurataContenuto(), DELTA);
    }

    @Test
    void unaBattutaDeveEssereDiUnElementoGiaPresente() {
        PaginaIntermezzo pagina = paginaConDuePersonaggi();

        assertThrows(IllegalArgumentException.class, () -> pagina.conBattuta(BattutaIntermezzo.di("nessuno", "Ehi")));
        assertThrows(IllegalArgumentException.class,
                () -> pagina.conElemento(ElementoIntermezzo.personaggio("a", ClassePersonaggio.ELFO, 0, 0)));
    }
}
