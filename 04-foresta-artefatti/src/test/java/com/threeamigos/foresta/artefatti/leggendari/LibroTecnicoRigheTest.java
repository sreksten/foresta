package com.threeamigos.foresta.artefatti.leggendari;

import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.modellodati.ArtefattoMD;
import com.threeamigos.foresta.artefatti.GeneratoreArtefattiTabelle;
import com.threeamigos.foresta.tipi.AspettoArtefatto;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoRaritaArtefatto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class LibroTecnicoRigheTest {

    private static final String LIBRO = "CHIAVE=LIBRO_A;NOME=il manuale di prova;DESCRIZIONE=che spiega tutto;DANNI_MAGICI=2;BATTUTA=Uno;BATTUTA=Due";
    private static final String RIPROGRAMMAZIONE = "CHIAVE=LIBRO_B;NOME=the manual;DESCRIZIONE=di nessuno;TIPO=RIPROGRAMMAZIONE;BATTUTA=Uno;";

    @AfterEach
    void ripristina() {
        LibroTecnico.reimposta();
    }

    private static void rifiuta(String riga, String messaggio) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> LibroTecnico.da(riga));
        assertTrue(e.getMessage().contains(messaggio), e.getMessage());
    }

    @Test
    void leggeUnLibroMagico() {
        LibroTecnico libro = LibroTecnico.da(LIBRO);
        assertEquals("LIBRO_A", libro.getChiave());
        assertEquals("il manuale di prova", libro.getNome());
        assertEquals("che spiega tutto", libro.getDescrizione());
        assertEquals(TipoArtefatto.LIBRO_MAGICO, libro.getTipo());
        assertEquals(2, libro.getMoltiplicatoreDanniMagici());
        assertEquals(Arrays.asList("Uno", "Due"), libro.getBattute());
        assertEquals("\"Il manuale di prova\", che spiega tutto", libro.getTitoloDaLeggere());
    }

    @Test
    void leggeUnaRiprogrammazione() {
        LibroTecnico libro = LibroTecnico.da(RIPROGRAMMAZIONE);
        assertEquals(TipoArtefatto.RIPROGRAMMAZIONE, libro.getTipo());
        assertEquals(1, libro.getMoltiplicatoreDanniMagici());
    }

    @Test
    void unaRigaMalFattaSiRifiuta() {
        rifiuta(LIBRO + ";;senzauguale", "Un campo è CHIAVE=valore");
        rifiuta(LIBRO + ";BOH=1", "Campo sconosciuto: BOH");
        rifiuta(LIBRO + ";NOME=altro", "Campo ripetuto: NOME");
        rifiuta(LIBRO.replace("CHIAVE=LIBRO_A;", ""), "Manca il campo CHIAVE");
        rifiuta(LIBRO.replace("NOME=il manuale di prova;", "NOME=;"), "Manca il campo NOME");
        rifiuta(LIBRO + ";TIPO=SPADA", "Un libro tecnico è un libro magico");
        rifiuta(LIBRO.replace("DANNI_MAGICI=2", "DANNI_MAGICI=0"), "DANNI_MAGICI è almeno 1");
        rifiuta("CHIAVE=A;NOME=n;DESCRIZIONE=d", "almeno una BATTUTA");
    }

    @Test
    void unaRigaNullaSiRifiuta() {
        assertThrows(NullPointerException.class, () -> LibroTecnico.da(null));
    }

    @Test
    void ILibriConLaStessaChiaveSiRifiutano() {
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> LibroTecnico.leggi(Arrays.asList(LIBRO, LIBRO)));
        assertTrue(e.getMessage().contains("Libro tecnico ripetuto: LIBRO_A"));
        assertEquals(2, LibroTecnico.leggi(Arrays.asList(LIBRO, RIPROGRAMMAZIONE)).size());
    }

    @Test
    void ilCatalogoDelGiocoSiLegge() {
        assertFalse(LibroTecnico.tutti().isEmpty());
        LibroTecnico primo = LibroTecnico.tutti().iterator().next();
        assertTrue(LibroTecnico.getLibro(primo.getChiave()).isPresent());
        assertFalse(LibroTecnico.getLibro("NON_C_E").isPresent());
    }

    @Test
    void duePezziSonoUgualiSeHannoLaStessaChiave() {
        LibroTecnico libro = LibroTecnico.da(LIBRO);
        assertEquals(libro, LibroTecnico.da(LIBRO));
        assertEquals(libro.hashCode(), LibroTecnico.da(LIBRO).hashCode());
        assertNotEquals(libro, LibroTecnico.da(RIPROGRAMMAZIONE));
        assertNotEquals(libro, "LIBRO_A");
    }

    @Test
    void siRiconoscePerAspettoETitolo() {
        LibroTecnico libro = LibroTecnico.tutti().iterator().next();
        Artefatto artefatto = libro.costruisci(new GeneratoreArtefattiTabelle(new Random(1)), 4);
        assertEquals(libro, LibroTecnico.di(artefatto).orElseThrow(AssertionError::new));
        artefatto.getModelloDati().setAspetto(null);
        assertFalse(LibroTecnico.di(artefatto).isPresent());
        artefatto.getModelloDati().setAspetto(AspettoArtefatto.LIBRO_TECNICO);
        artefatto.getModelloDati().setNome("un altro titolo");
        assertFalse(LibroTecnico.di(artefatto).isPresent());
    }

    @Test
    void costruisceUnLibroMagicoConIlDoppioDeiBonus() {
        LibroTecnico libro = LibroTecnico.da(LIBRO);
        for (int seme = 0; seme < 30; seme++) {
            Artefatto artefatto = libro.costruisci(new GeneratoreArtefattiTabelle(new Random(seme)), 8);
            ArtefattoMD md = artefatto.getModelloDati();
            assertEquals("il manuale di prova", md.getNome());
            assertEquals(TipoRaritaArtefatto.LEGGENDARIO, md.getRarita());
            assertEquals(AspettoArtefatto.LIBRO_TECNICO, md.getAspetto());
            assertNull(md.getNomeProprio());
        }
    }
}
