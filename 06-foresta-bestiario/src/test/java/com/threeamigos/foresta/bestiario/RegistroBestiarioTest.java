package com.threeamigos.foresta.bestiario;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoAmiciziaStretta;
import com.threeamigos.foresta.eventi.interni.InternoAvversarioSconfitto;
import com.threeamigos.foresta.eventi.interni.InternoCaratteristicaOsservata;
import com.threeamigos.foresta.eventi.interni.InternoCorruzioneRiuscita;
import com.threeamigos.foresta.eventi.interni.InternoPersonaggioEntratoNelGruppo;
import com.threeamigos.foresta.eventi.interni.InternoSfidaVinta;
import com.threeamigos.foresta.eventi.interni.InternoVoceDelBestiarioSbloccata;
import com.threeamigos.foresta.interfacce.GestoreBestiario;
import com.threeamigos.foresta.modellodati.BestiarioMD;
import com.threeamigos.foresta.tipi.CaratteristicaDiClasse;
import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Il bestiario: le voci si sbloccano con gli eventi che le fanno avanzare, le caratteristiche si scoprono osservandole, e ogni novità si salva.
 */
class RegistroBestiarioTest {

    /**
     * Il "file" del bestiario, in memoria: il testo che verrebbe scritto su disco.
     */
    private static final class GestoreInMemoria implements GestoreBestiario {
        private String contenuto = "";
        private int salvataggi;

        @Override
        public boolean carica(BestiarioMD bestiarioMD) {
            try {
                bestiarioMD.leggi(new BufferedReader(new StringReader(contenuto)));
            } catch (IOException e) {
                return false;
            }
            return true;
        }

        @Override
        public boolean salva(BestiarioMD bestiarioMD) {
            StringWriter scrittura = new StringWriter();
            try (PrintWriter writer = new PrintWriter(scrittura)) {
                bestiarioMD.salva(writer);
            } catch (IOException e) {
                return false;
            }
            contenuto = scrittura.toString();
            salvataggi++;
            return true;
        }

        boolean contieneRiga(String riga) {
            return Arrays.asList(contenuto.split("\\R")).contains(riga);
        }
    }

    private GestoreInMemoria gestore;
    private final List<TipoPersonaggio> annunciate = new ArrayList<>();

    @BeforeEach
    void preparaIlRegistro() {
        BusEventi.azzera();
        BusEventi.impostaConsegna(Runnable::run);
        gestore = new GestoreInMemoria();
        RegistroBestiario.impostaGestoreBestiario(gestore);
        RegistroBestiario.registrati();
        annunciate.clear();
        BusEventi.iscriviti(InternoVoceDelBestiarioSbloccata.class, evento -> annunciate.add(evento.getVoce()));
    }

    @AfterEach
    void azzeraIlBus() {
        BusEventi.impostaConsegna(null);
        BusEventi.azzera();
    }

    @Test
    void unBestiarioNuovoNonHaVociSbloccate() {
        assertEquals(0, RegistroBestiario.getNumeroVociSbloccate());
        assertFalse(RegistroBestiario.isSbloccata(TipoPersonaggio.GOBLIN));
        assertEquals(TipoPersonaggio.getVociDelBestiario().size(), RegistroBestiario.getNumeroVoci());
        assertTrue(RegistroBestiario.getCaratteristicheScoperte(TipoPersonaggio.GOBLIN).isEmpty());
    }

    @Test
    void sconfiggereUnAvversarioSbloccaLaSuaVoceEUnaVoltaSola() {
        BusEventi.pubblica(new InternoAvversarioSconfitto(TipoPersonaggio.GOBLIN));

        assertTrue(RegistroBestiario.isSbloccata(TipoPersonaggio.GOBLIN));
        assertEquals(1, RegistroBestiario.getNumeroVociSbloccate());
        assertEquals(Collections.singletonList(TipoPersonaggio.GOBLIN), annunciate);
        assertTrue(gestore.contieneRiga("GOBLIN|true|"), "la novità si salva");

        BusEventi.pubblica(new InternoAvversarioSconfitto(TipoPersonaggio.GOBLIN));
        assertEquals(1, annunciate.size(), "la seconda volta non è una novità");
        assertEquals(1, gestore.salvataggi);
    }

    @Test
    void amiciziaCorruzioneESfidaContanoPerTuttiGliAvversari() {
        BusEventi.pubblica(new InternoAmiciziaStretta(Arrays.asList(TipoPersonaggio.GOBLIN, TipoPersonaggio.SCHELETRO)));
        BusEventi.pubblica(new InternoCorruzioneRiuscita(Collections.singletonList(TipoPersonaggio.GARGOYLE)));
        BusEventi.pubblica(new InternoSfidaVinta(TipoPersonaggio.IDRA));

        assertTrue(RegistroBestiario.isSbloccata(TipoPersonaggio.GOBLIN));
        assertTrue(RegistroBestiario.isSbloccata(TipoPersonaggio.SCHELETRO));
        assertTrue(RegistroBestiario.isSbloccata(TipoPersonaggio.GARGOYLE));
        assertTrue(RegistroBestiario.isSbloccata(TipoPersonaggio.IDRA));
        assertEquals(4, RegistroBestiario.getNumeroVociSbloccate());
    }

    @Test
    void chiEntraNelGruppoSbloccaLaVoceDelSuoRuolo() {
        BusEventi.pubblica(new InternoPersonaggioEntratoNelGruppo(TipoPersonaggio.MAGA));

        assertTrue(RegistroBestiario.isSbloccata(TipoPersonaggio.MAGO), "Mago e Maga sono la stessa voce");
        assertEquals(Collections.singletonList(TipoPersonaggio.MAGO), annunciate);
    }

    @Test
    void chiNonHaUnaVoceNonSiSblocca() {
        TipoPersonaggio senzaVoce = null;
        for (TipoPersonaggio classe : TipoPersonaggio.values()) {
            if (!classe.hasVoceNelBestiario()) {
                senzaVoce = classe;
                break;
            }
        }
        assertNotNull(senzaVoce, "serve almeno una classe senza voce");

        BusEventi.pubblica(new InternoAvversarioSconfitto(senzaVoce));

        assertEquals(0, RegistroBestiario.getNumeroVociSbloccate());
        assertTrue(annunciate.isEmpty());
        assertEquals(0, gestore.salvataggi);
    }

    @Test
    void unaCaratteristicaSiScopreSoloSeEDellaClasse() {
        BusEventi.pubblica(new InternoCaratteristicaOsservata(TipoPersonaggio.SCHELETRO, CaratteristicaDiClasse.Genere.DANNO, TipoDanno.CONTUNDENTE));
        BusEventi.pubblica(new InternoCaratteristicaOsservata(TipoPersonaggio.GOBLIN, CaratteristicaDiClasse.Genere.DANNO, TipoDanno.FUOCO));

        assertEquals(Collections.singleton("DANNO:CONTUNDENTE"), RegistroBestiario.getCaratteristicheScoperte(TipoPersonaggio.SCHELETRO));
        assertTrue(RegistroBestiario.getCaratteristicheScoperte(TipoPersonaggio.GOBLIN).isEmpty(), "il goblin subisce il danno normale");
        assertFalse(RegistroBestiario.isSbloccata(TipoPersonaggio.SCHELETRO), "scoperta, ma la voce è ancora chiusa");
        assertTrue(gestore.contieneRiga("SCHELETRO|false|DANNO:CONTUNDENTE"), "la scoperta si salva");
        assertEquals(1, gestore.salvataggi);

        BusEventi.pubblica(new InternoCaratteristicaOsservata(TipoPersonaggio.SCHELETRO, CaratteristicaDiClasse.Genere.DANNO, TipoDanno.CONTUNDENTE));
        assertEquals(1, gestore.salvataggi, "scoprirla di nuovo non è una novità");
    }

    @Test
    void chiEImmuneATuttiGliIncantesimiLoScopreConUnoQualunque() {
        BusEventi.pubblica(new InternoCaratteristicaOsservata(TipoPersonaggio.GARGOYLE, CaratteristicaDiClasse.Genere.INCANTESIMO,
                com.threeamigos.foresta.tipi.TipoIncantesimo.FUOCO));

        assertTrue(RegistroBestiario.getCaratteristicheScoperte(TipoPersonaggio.GARGOYLE).contains("INCANTESIMO:TUTTI"));
    }

    @Test
    void lePartiScopertePerUnaVoceNonSiPossonoCambiareDaFuori() {
        BusEventi.pubblica(new InternoCaratteristicaOsservata(TipoPersonaggio.SCHELETRO, CaratteristicaDiClasse.Genere.DANNO, TipoDanno.CONTUNDENTE));
        assertThrows(UnsupportedOperationException.class,
                () -> RegistroBestiario.getCaratteristicheScoperte(TipoPersonaggio.SCHELETRO).add("DANNO:FUOCO"));
    }

    @Test
    void ilBestiarioValeDaUnaPartitaAllAltra() {
        BusEventi.pubblica(new InternoAvversarioSconfitto(TipoPersonaggio.GOBLIN));

        // Un'altra "esecuzione": il registro si rilegge dallo stesso file
        RegistroBestiario.impostaGestoreBestiario(gestore);

        assertTrue(RegistroBestiario.isSbloccata(TipoPersonaggio.GOBLIN));

        // E salvando di nuovo la voce già sbloccata, senza caratteristiche, resta com'era
        BusEventi.pubblica(new InternoCaratteristicaOsservata(TipoPersonaggio.SCHELETRO, CaratteristicaDiClasse.Genere.DANNO, TipoDanno.CONTUNDENTE));
        assertTrue(gestore.contieneRiga("GOBLIN|true|"));
        assertTrue(gestore.contieneRiga("SCHELETRO|false|DANNO:CONTUNDENTE"));
    }

    @Test
    void unaScopertaDiUnAltroGenereOUnAltroIncantesimoNonScopreQuellaDellaClasse() {
        BusEventi.pubblica(new InternoCaratteristicaOsservata(TipoPersonaggio.SCHELETRO, CaratteristicaDiClasse.Genere.EFFETTO,
                com.threeamigos.foresta.tipi.TipoEffettoDiStato.CONFUSO));
        BusEventi.pubblica(new InternoCaratteristicaOsservata(TipoPersonaggio.SCHELETRO, CaratteristicaDiClasse.Genere.INCANTESIMO,
                com.threeamigos.foresta.tipi.TipoIncantesimo.FUOCO));

        assertFalse(RegistroBestiario.getCaratteristicheScoperte(TipoPersonaggio.SCHELETRO).contains("INCANTESIMO:MORTE"));
        BusEventi.pubblica(new InternoCaratteristicaOsservata(TipoPersonaggio.SCHELETRO, CaratteristicaDiClasse.Genere.INCANTESIMO,
                com.threeamigos.foresta.tipi.TipoIncantesimo.MORTE));
        assertTrue(RegistroBestiario.getCaratteristicheScoperte(TipoPersonaggio.SCHELETRO).contains("INCANTESIMO:MORTE"));
    }

    @Test
    void senzaUnGestoreIlBestiarioRestaInMemoria() throws Exception {
        java.lang.reflect.Field campo = RegistroBestiario.class.getDeclaredField("gestoreBestiario");
        campo.setAccessible(true);
        campo.set(null, null);

        BusEventi.pubblica(new InternoAvversarioSconfitto(TipoPersonaggio.GOBLIN));

        assertTrue(RegistroBestiario.isSbloccata(TipoPersonaggio.GOBLIN));
        assertEquals(0, gestore.salvataggi, "non c'è dove salvare");
    }
}
