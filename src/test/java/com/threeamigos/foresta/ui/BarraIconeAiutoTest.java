package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoDiGioco;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoImpostazioneAiuto;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.motore.VistaPartitaMotore;
import com.threeamigos.foresta.tipi.Comando;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * L'interruttore dell'aiuto in fondo alla barra delle icone, e il cartiglio dell'aiuto che resta dentro lo schermo.
 */
class BarraIconeAiutoTest {

    private static final int LARGHEZZA_SCHERMO = 800;
    private static final int ALTEZZA_SCHERMO = 600;

    private ModelloDati modelloDatiPrecedente;
    private final List<ComandoDiGioco> comandiMandati = new ArrayList<>();
    private final List<ComandoImpostazioneAiuto> aiutiMandati = new ArrayList<>();

    @BeforeEach
    void prepara() {
        modelloDatiPrecedente = ModelloDati.getIstanza();
        ModelloDati.setIstanza(new ModelloDati());
        BusEventi.azzera();
        BusEventi.impostaConsegna(Runnable::run);
        BusEventi.iscriviti(ComandoDiGioco.class, comandiMandati::add);
        // Come fa l'Automa, che scrive l'aiuto nel modello dati
        BusEventi.iscriviti(ComandoImpostazioneAiuto.class, e -> {
            aiutiMandati.add(e);
            ModelloDati.getIstanza().setAiutoAbilitato(e.isAbilitato());
        });
    }

    @AfterEach
    void ripristina() {
        ModelloDati.setIstanza(modelloDatiPrecedente);
        ComandiPossibili.reimposta();
        BusEventi.azzera();
    }

    @Test
    void lInterruttoreMostraCosaFaIlClickELoStatoNonArrivaAlGioco() {
        // Given
        DisplayableCanvasBarraIcone barra = new DisplayableCanvasBarraIcone(DisplayableCanvas.ORIENTAMENTO_ORIZZONTALE,
                0, ALTEZZA_SCHERMO - 70, LARGHEZZA_SCHERMO, 70, new VistaPartitaMotore());
        ComandiPossibili.set(Comando.NORD, Comando.SUD);
        // When
        barra.impostaAzioni();
        // Then: l'aiuto parte acceso, quindi l'icona è quella per spegnerlo
        assertTrue(ModelloDati.getIstanza().isAiutoAbilitato());
        assertEquals(Comando.NO_AIUTO, ultimo(barra));
        // When
        barra.esegui(Comando.NO_AIUTO);
        // Then
        assertFalse(aiutiMandati.get(0).isAbilitato());
        assertFalse(ModelloDati.getIstanza().isAiutoAbilitato());
        assertEquals(Comando.AIUTO, ultimo(barra));
        // When
        barra.esegui(Comando.AIUTO);
        // Then
        assertTrue(ModelloDati.getIstanza().isAiutoAbilitato());
        assertEquals(Comando.NO_AIUTO, ultimo(barra));
        assertEquals(2, aiutiMandati.size());
        assertTrue(comandiMandati.isEmpty(), "l'interruttore non manda comandi al gioco: " + comandiMandati);
    }

    @Test
    void senzaComandiNonCeNemmenoLInterruttore() {
        DisplayableCanvasBarraIcone barra = new DisplayableCanvasBarraIcone(DisplayableCanvas.ORIENTAMENTO_ORIZZONTALE,
                0, ALTEZZA_SCHERMO - 70, LARGHEZZA_SCHERMO, 70, new VistaPartitaMotore());
        ComandiPossibili.reimposta();
        barra.impostaAzioni();
        assertTrue(barra.iconeVisibili.isEmpty());
    }

    @Test
    void conLaBarraInBassoIlCartiglioStaSopraLIconaECentrato() {
        Point p = DisplayableCanvasBarraIcone.posizioneCartiglio(DisplayableCanvas.ORIENTAMENTO_ORIZZONTALE,
                new Rectangle(300, 530, 62, 64), 100, 25, LARGHEZZA_SCHERMO, ALTEZZA_SCHERMO);
        assertEquals(300 + (62 - 100) / 2, p.x);
        assertTrue(p.y + 25 <= 530, "sopra l'icona: " + p);
    }

    @Test
    void ilCartiglioDiUnIconaAiBordiRestaDentroLoSchermo() {
        // Barra in basso: icona all'estrema sinistra e all'estrema destra
        Point sinistra = DisplayableCanvasBarraIcone.posizioneCartiglio(DisplayableCanvas.ORIENTAMENTO_ORIZZONTALE,
                new Rectangle(0, 530, 62, 64), 300, 25, LARGHEZZA_SCHERMO, ALTEZZA_SCHERMO);
        assertEquals(0, sinistra.x);
        Point destra = DisplayableCanvasBarraIcone.posizioneCartiglio(DisplayableCanvas.ORIENTAMENTO_ORIZZONTALE,
                new Rectangle(LARGHEZZA_SCHERMO - 62, 530, 62, 64), 300, 25, LARGHEZZA_SCHERMO, ALTEZZA_SCHERMO);
        assertEquals(LARGHEZZA_SCHERMO - 300, destra.x);
        // Un'icona ingrandita del Dock che arriva in cima allo schermo: il cartiglio non esce sopra
        Point inCima = DisplayableCanvasBarraIcone.posizioneCartiglio(DisplayableCanvas.ORIENTAMENTO_ORIZZONTALE,
                new Rectangle(300, 10, 93, 96), 100, 25, LARGHEZZA_SCHERMO, ALTEZZA_SCHERMO);
        assertEquals(0, inCima.y);
        // Barra a destra: il cartiglio sta a sinistra dell'icona, e dentro lo schermo in alto e in basso
        Point aSinistra = DisplayableCanvasBarraIcone.posizioneCartiglio(DisplayableCanvas.ORIENTAMENTO_VERTICALE,
                new Rectangle(LARGHEZZA_SCHERMO - 64, 200, 62, 64), 100, 25, LARGHEZZA_SCHERMO, ALTEZZA_SCHERMO);
        assertTrue(aSinistra.x + 100 <= LARGHEZZA_SCHERMO - 64, "a sinistra dell'icona: " + aSinistra);
        Point inBasso = DisplayableCanvasBarraIcone.posizioneCartiglio(DisplayableCanvas.ORIENTAMENTO_VERTICALE,
                new Rectangle(LARGHEZZA_SCHERMO - 64, ALTEZZA_SCHERMO - 10, 62, 64), 100, 25, LARGHEZZA_SCHERMO, ALTEZZA_SCHERMO);
        assertEquals(ALTEZZA_SCHERMO - 25, inBasso.y);
        // Un cartiglio più largo dello schermo resta allineato al bordo sinistro
        Point largo = DisplayableCanvasBarraIcone.posizioneCartiglio(DisplayableCanvas.ORIENTAMENTO_ORIZZONTALE,
                new Rectangle(300, 530, 62, 64), 1000, 25, LARGHEZZA_SCHERMO, ALTEZZA_SCHERMO);
        assertEquals(0, largo.x);
    }

    private static Comando ultimo(DisplayableCanvasBarraIcone barra) {
        return barra.iconeVisibili.get(barra.iconeVisibili.size() - 1).comando;
    }
}
