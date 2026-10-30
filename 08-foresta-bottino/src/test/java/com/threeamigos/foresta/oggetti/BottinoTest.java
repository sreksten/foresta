package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.artefatti.GeneratoreArtefatti;
import com.threeamigos.foresta.artefatti.GeneratoreArtefattiTabelle;
import com.threeamigos.foresta.artefatti.leggendari.CatalogoLeggendari;
import com.threeamigos.foresta.artefatti.leggendari.LibroTecnico;
import com.threeamigos.foresta.eventi.interni.InternoArtefattoTrovato;
import com.threeamigos.foresta.eventi.interni.InternoOggettoRaccolto;
import com.threeamigos.foresta.eventi.notifiche.NotificaArtefattoTrovato;
import com.threeamigos.foresta.intermezzi.IntermezzoLibroTecnico;
import com.threeamigos.foresta.intermezzi.RegistroIntermezzi;
import com.threeamigos.foresta.modellodati.IntermezziMD;
import com.threeamigos.foresta.modellodati.StatisticheMD;
import com.threeamigos.foresta.statistiche.Statistiche;
import com.threeamigos.foresta.strumenti.dado.ContestoLancio;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.strumenti.dado.TruccatoreDiDadi;
import com.threeamigos.foresta.tipi.CostantiBottino;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoBottino;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class BottinoTest {

    @BeforeEach
    void prepara() {
        Statistiche.setFornitoreDiDati(() -> new StatisticheMD());
        RegistroIntermezzi.setFornitoreDiDati(() -> new IntermezziMD());
        Dado.impostaSeme(7);
        IntermezzoLibroTecnico.reimposta();
    }

    @AfterEach
    void ripulisce() {
        Dado.impostaTruccatore(null);
        Dado.ripristina();
        IntermezzoLibroTecnico.reimposta();
    }

    private static void vuole(String oggetto) {
        Dado.impostaTruccatore(new TruccatoreDiDadi() {
            @Override
            public OptionalInt lancio(ContestoLancio contesto, int min, int max) {
                return OptionalInt.empty();
            }

            @Override
            @SuppressWarnings("unchecked")
            public <T> Optional<T> scelta(ContestoLancio contesto, List<T> ammessi) {
                return contesto == ContestoLancio.COFANO_OGGETTO ? (Optional<T>) Optional.of(oggetto) : Optional.empty();
            }
        });
    }

    @Test
    void moneteCoroneEPietreHannoNomiEArticoli() {
        Bottino[] tutti = {new Moneta(), new Corona(), new PietraPreziosa(), new Cofano(), new Cofano(3)};
        for (Bottino b : tutti) {
            assertNotNull(b.getClasse());
            assertTrue(b.getQuantita() >= 1);
            assertNotNull(b.getNomeSingolare());
            assertNotNull(b.getNomePlurale());
            assertNotNull(b.getAIS());
            assertNotNull(b.getAIP());
            assertNotNull(b.getADS());
            assertNotNull(b.getADP());
            assertFalse(b.getArtefatto().isPresent());
            assertFalse(b.isDaRivelare());
        }
        assertEquals(3, new Cofano(3).getQuantita());
        assertEquals(TipoBottino.COFANO, new Cofano().getClasse());
        assertEquals("cofano", new Cofano().getNomeSingolare());
        assertEquals("cofani", new Cofano().getNomePlurale());
    }

    @Test
    void anelloMagicoENonMagico() {
        boolean visto = false, vistoNon = false;
        boolean[] tipi = new boolean[4];
        for (int seme = 0; seme < 400 && !(visto && vistoNon && tipi[1] && tipi[2] && tipi[3]); seme++) {
            Dado.impostaSeme(seme);
            Anello anello = new Anello();
            assertEquals(TipoBottino.ANELLO, anello.getClasse());
            assertEquals("anello", anello.getNomeSingolare());
            assertEquals("anelli", anello.getNomePlurale());
            assertNotNull(anello.getAIS());
            assertNotNull(anello.getAIP());
            assertNotNull(anello.getADS());
            assertNotNull(anello.getADP());
            assertFalse(anello.isMagiaAnnunciata());
            anello.segnaMagiaAnnunciata();
            assertTrue(anello.isMagiaAnnunciata());
            if (anello.isMagico()) {
                visto = true;
                tipi[anello.getTipoDiMagia()] = true;
                assertTrue(anello.getArtefatto().isPresent());
            } else {
                vistoNon = true;
                assertEquals(-1, anello.getTipoDiMagia());
                assertFalse(anello.getArtefatto().isPresent());
            }
        }
        assertTrue(visto && vistoNon && tipi[1] && tipi[2] && tipi[3]);
    }

    @Test
    void laFormaDelBottinoSiTrovaPerTipo() {
        for (FormaDelBottino forma : FormaDelBottino.values()) {
            assertEquals(forma, FormaDelBottino.di(forma.getTipoArtefatto()).get());
            assertEquals(forma, FormaDelBottino.di(forma.getTipoBottino()).get());
            assertNotNull(forma.getNomeSingolare());
            assertNotNull(forma.getNomePlurale());
            assertNotNull(forma.getAIS());
            assertNotNull(forma.getAIP());
            assertNotNull(forma.getADS());
            assertNotNull(forma.getADP());
        }
        assertFalse(FormaDelBottino.di(TipoArtefatto.ANELLO).isPresent());
        assertFalse(FormaDelBottino.di(TipoBottino.MONETA).isPresent());
    }

    @Test
    void bottinoArtefattoNuovoEGiaFatto() {
        for (FormaDelBottino forma : FormaDelBottino.values()) {
            BottinoArtefatto b = BottinoArtefatto.nuovo(forma);
            assertEquals(forma.getTipoBottino(), b.getClasse());
            assertEquals(forma.getNomeSingolare(), b.getNomeSingolare());
            assertEquals(forma.getNomePlurale(), b.getNomePlurale());
            assertEquals(forma.getAIS(), b.getAIS());
            assertEquals(forma.getAIP(), b.getAIP());
            assertEquals(forma.getADS(), b.getADS());
            assertEquals(forma.getADP(), b.getADP());
            assertEquals(1, b.getQuantita());
            assertFalse(b.isDaRivelare());
            assertTrue(b.getArtefatto().isPresent());
        }
        Artefatto ingrediente = GeneratoreArtefatti.istanza().generaIngrediente(1);
        BottinoArtefatto senzaForma = BottinoArtefatto.giaFatto(ingrediente);
        assertTrue(senzaForma.isDaRivelare());
        assertEquals(TipoBottino.ARTEFATTO, senzaForma.getClasse());
        assertEquals("artefatto", senzaForma.getNomeSingolare());
        assertEquals("artefatti", senzaForma.getNomePlurale());
        assertNotNull(senzaForma.getAIS());
        assertNotNull(senzaForma.getAIP());
        assertNotNull(senzaForma.getADS());
        assertNotNull(senzaForma.getADP());
        Artefatto spada = GeneratoreArtefatti.istanza().generaArtefatto(TipoArtefatto.SPADA, 1);
        assertEquals(TipoBottino.SPADA, BottinoArtefatto.giaFatto(spada).getClasse());
        assertThrows(NullPointerException.class, () -> BottinoArtefatto.giaFatto(null));
    }

    @Test
    void nomeOggetto() {
        NomeOggetto f = NomeOggetto.femminile("radice", "radici");
        assertEquals("radice", f.getSingolare());
        assertEquals("radici", f.getPlurale());
        assertNotNull(f.getAIS());
        assertNotNull(f.getAIP());
        assertNotNull(f.getADS());
        assertNotNull(f.getADP());
        NomeOggetto m = NomeOggetto.maschile("fungo", "funghi");
        assertEquals("fungo", m.getSingolare());
        assertNotEquals(f.getAIS(), m.getAIS());
        assertNotEquals(f.getADS(), m.getADS());
    }

    @Test
    void laFabbricaCreaTuttiIGenerabili() {
        for (TipoBottino tipo : TipoBottino.values()) {
            if (tipo.isGenerabile()) {
                assertEquals(tipo, FabbricaOggetti.crea(tipo).getClasse());
            } else {
                assertThrows(IllegalArgumentException.class, () -> FabbricaOggetti.crea(tipo));
            }
        }
    }

    @Test
    void ilCofanoDaOgniSogliaDelTiro() {
        GeneratoreArtefatti g = new GeneratoreArtefattiTabelle(new Random(3));
        double p = CostantiBottino.COFANO_PROBABILITA_PERGAMENA;
        assertNotNull(Cofano.artefattoRaro(0.0, g, 3));
        assertNotNull(Cofano.artefattoRaro(p + 0.01, g, 3));
        double a = p + CostantiBottino.COFANO_PROBABILITA_ARTEFATTO;
        assertNotNull(Cofano.artefattoRaro(a + 0.001, g, 3));
        double l = a + CostantiBottino.COFANO_PROBABILITA_AGGIORNAMENTO;
        assertNotNull(Cofano.artefattoRaro(l + 0.001, g, 3));
        assertNull(Cofano.artefattoRaro(0.99, g, 3));
        // se i libri sono stati trovati tutti, non ce ne sono altri
        for (LibroTecnico libro : LibroTecnico.tutti()) {
            IntermezzoLibroTecnico.trovato(libro);
        }
        assertNull(Cofano.artefattoRaro(l + 0.001, g, 3));
    }

    @Test
    void ilCofanoObbedisceAChiSviluppa() {
        vuole(Cofano.OGGETTO_SPECIALE_NESSUNO);
        assertNull(Cofano.artefattoRaro());
        vuole(Cofano.OGGETTO_SPECIALE_INGREDIENTE);
        assertNotNull(Cofano.artefattoRaro());
        vuole(Cofano.OGGETTO_SPECIALE_ARTEFATTO);
        assertNotNull(Cofano.artefattoRaro());
        vuole(Cofano.OGGETTO_SPECIALE_AGGIORNAMENTO);
        assertNotNull(Cofano.artefattoRaro());
        vuole(Cofano.OGGETTO_SPECIALE_CASUALE);
        Cofano.artefattoRaro();
        String chiaveLibro = LibroTecnico.tutti().iterator().next().getChiave();
        vuole(Cofano.PREFISSO_LIBRO_TECNICO + chiaveLibro);
        assertNotNull(Cofano.artefattoRaro());
        vuole(Cofano.PREFISSO_LIBRO_TECNICO + "inesistente");
        Cofano.artefattoRaro();
        String chiaveLeggendario = CatalogoLeggendari.getTuttiILeggendari().keySet().iterator().next();
        vuole(Cofano.PREFISSO_LEGGENDARIO + chiaveLeggendario);
        assertNotNull(Cofano.artefattoRaro());
        vuole(Cofano.PREFISSO_LEGGENDARIO + "inesistente");
        Cofano.artefattoRaro();
        // senza trucchi, il tiro è casuale
        Dado.impostaTruccatore(null);
        for (int i = 0; i < 300; i++) {
            Cofano.artefattoRaro();
        }
    }

    @Test
    void leOpzioniDelCofanoSonoTutteLeScelte() {
        List<String> opzioni = Cofano.opzioniOggettoSpeciale();
        assertTrue(opzioni.contains(Cofano.OGGETTO_SPECIALE_CASUALE));
        assertTrue(opzioni.contains(Cofano.OGGETTO_SPECIALE_NESSUNO));
        assertEquals(5 + CatalogoLeggendari.getTuttiILeggendari().size() + LibroTecnico.tutti().size(), opzioni.size());
    }

    @Test
    void gliEventi() {
        Artefatto spada = GeneratoreArtefatti.istanza().generaArtefatto(TipoArtefatto.SPADA, 1);
        InternoArtefattoTrovato trovato = new InternoArtefattoTrovato(spada, true);
        assertSame(spada, trovato.getArtefatto());
        assertTrue(trovato.isCustodito());
        assertThrows(NullPointerException.class, () -> new InternoArtefattoTrovato(null, false));
        InternoOggettoRaccolto raccolto = new InternoOggettoRaccolto(TipoBottino.MONETA, 4, null, false);
        assertEquals(TipoBottino.MONETA, raccolto.getClasse());
        assertEquals(4, raccolto.getQuantita());
        assertFalse(raccolto.getArtefatto().isPresent());
        assertFalse(raccolto.isCustodito());
        assertTrue(new InternoOggettoRaccolto(TipoBottino.SPADA, 1, spada, true).getArtefatto().isPresent());
        NotificaArtefattoTrovato notifica = new NotificaArtefattoTrovato(null, 5);
        assertNull(notifica.getArtefatto());
        assertEquals(5, notifica.getLivelloMondo());
    }
}
