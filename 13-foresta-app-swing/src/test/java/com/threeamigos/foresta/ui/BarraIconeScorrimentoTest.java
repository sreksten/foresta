package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoDiGioco;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoImpostazioneAiuto;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.motore.VistaPartitaMotore;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoIcona;
import com.threeamigos.foresta.ui.Finestra.Tasto;
import com.threeamigos.foresta.ui.barra.ComandiPossibili;
import com.threeamigos.foresta.ui.barra.DisplayableCanvasBarraIcone;
import com.threeamigos.foresta.ui.barra.DisplayableCanvasBarraIcone.IconaVisibile;
import com.threeamigos.foresta.ui.barra.DisplayableCanvasBarraIconeDock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le icone che non ci stanno scorrono in pixel fra le due frecce ai capi: trascinando, per inerzia e con le frecce,
 * sia nella barra classica sia nel Dock; in verticale le icone si dispongono lungo l'altezza senza sovrapporsi.
 */
class BarraIconeScorrimentoTest {

    private static final int LARGHEZZA = 300;
    private static final int ALTEZZA = 70;
    private static final int LATO = TipoIcona.getLarghezzaMassima();
    private static final long MILLISECONDO = 1_000_000L;

    private ModelloDati modelloDatiPrecedente;
    private final List<ComandoDiGioco> comandiMandati = new ArrayList<>();
    /** L'orologio delle barre di prova, in nanosecondi: lo si fa avanzare a mano */
    private long orologio;

    private class BarraDiProva extends DisplayableCanvasBarraIcone {
        BarraDiProva(int orientamento, int larghezza, int altezza) {
            super(orientamento, 0, 0, larghezza, altezza, new VistaPartitaMotore());
        }

        @Override
        protected long adesso() {
            return orologio;
        }
    }

    private class DockDiProva extends DisplayableCanvasBarraIconeDock {
        DockDiProva() {
            super(0, 0, LARGHEZZA, ALTEZZA, new VistaPartitaMotore());
        }

        @Override
        protected long adesso() {
            return orologio;
        }
    }

    @BeforeEach
    void prepara() {
        modelloDatiPrecedente = ModelloDati.getIstanza();
        ModelloDati.installa();
        ModelloDati.setIstanza(new ModelloDati());
        BusEventi.azzera();
        BusEventi.impostaConsegna(Runnable::run);
        BusEventi.iscriviti(ComandoDiGioco.class, comandiMandati::add);
        BusEventi.iscriviti(ComandoImpostazioneAiuto.class, e -> ModelloDati.getIstanza().setAiutoAbilitato(e.isAbilitato()));
        orologio = 1_000_000_000L;
    }

    @AfterEach
    void ripristina() {
        ModelloDati.setIstanza(modelloDatiPrecedente);
        ComandiPossibili.reimposta();
        BusEventi.azzera();
    }

    /** I primi comandi che hanno un'icona e non sono né frecce, né aiuto, né personaggi */
    private static List<Comando> comandiDaMostrare(int quanti) {
        List<Comando> comandi = new ArrayList<>();
        for (Comando comando : Comando.values()) {
            if (comando == Comando.SU || comando == Comando.GIU || comando == Comando.SINISTRA || comando == Comando.DESTRA
                    || comando == Comando.AIUTO || comando == Comando.NO_AIUTO || comando.isPersonaggio()) {
                continue;
            }
            try {
                TipoIcona.ofComando(comando);
            } catch (IllegalArgumentException e) {
                continue;
            }
            comandi.add(comando);
            if (comandi.size() == quanti) {
                return comandi;
            }
        }
        throw new IllegalStateException("Servono " + quanti + " comandi con un'icona, ce ne sono " + comandi.size());
    }

    private static void imposta(DisplayableCanvasBarraIcone barra, int quanti) {
        ComandiPossibili.set(comandiDaMostrare(quanti));
        barra.impostaAzioni();
    }

    /** Fa passare il tempo disegnando un fotogramma ogni 16 ms */
    private void lasciaPassare(DisplayableCanvasBarraIcone barra, long millisecondi) {
        BufferedImage immagine = new BufferedImage(LARGHEZZA + 100, ALTEZZA + 400, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = immagine.createGraphics();
        try {
            for (long trascorsi = 0; trascorsi < millisecondi; trascorsi += 16) {
                orologio += 16 * MILLISECONDO;
                barra.disegna(graphics);
            }
        } finally {
            graphics.dispose();
        }
    }

    private static Comando comandoNellaPosizione(DisplayableCanvasBarraIcone barra, int x, int y) {
        for (IconaVisibile icona : barra.iconeVisibili) {
            if (icona.rettangolo.contains(x, y)) {
                return icona.comando;
            }
        }
        return null;
    }

    @Test
    void seLeIconeCiStannoNonCeScorrimentoNeFrecce() {
        // Given: tre comandi più l'aiuto fanno 4 icone, quante ne stanno in 300 pixel
        BarraDiProva barra = new BarraDiProva(DisplayableCanvas.ORIENTAMENTO_ORIZZONTALE, LARGHEZZA, ALTEZZA);
        // When
        imposta(barra, 3);
        // Then
        assertFalse(barra.isScorrevole());
        assertEquals(4, barra.iconeVisibili.size());
        for (IconaVisibile icona : barra.iconeVisibili) {
            assertNotEquals(Comando.SINISTRA, icona.comando);
            assertNotEquals(Comando.DESTRA, icona.comando);
        }
    }

    @Test
    void seNonCiStannoLeFrecceRestanoAiDueCapi() {
        // Given
        BarraDiProva barra = new BarraDiProva(DisplayableCanvas.ORIENTAMENTO_ORIZZONTALE, LARGHEZZA, ALTEZZA);
        // When
        imposta(barra, 8);
        // Then
        assertTrue(barra.isScorrevole());
        IconaVisibile prima = barra.iconeVisibili.get(0);
        IconaVisibile ultima = barra.iconeVisibili.get(barra.iconeVisibili.size() - 1);
        assertEquals(Comando.SINISTRA, prima.comando);
        assertEquals(0, prima.rettangolo.x);
        assertEquals(Comando.DESTRA, ultima.comando);
        assertEquals(LARGHEZZA - LATO, ultima.rettangolo.x);
        // 9 icone con l'aiuto; la fascia di mezzo è larga 300 - 2 * 64 = 172
        assertEquals(9 * LATO - (LARGHEZZA - 2 * LATO), barra.getScorrimentoMassimo(), 0.001d);
    }

    @Test
    void trascinandoLeIconeSeguonoIlPuntatore() {
        // Given
        BarraDiProva barra = new BarraDiProva(DisplayableCanvas.ORIENTAMENTO_ORIZZONTALE, LARGHEZZA, ALTEZZA);
        imposta(barra, 8);
        int xPrimaIcona = barra.iconeVisibili.get(1).rettangolo.x;
        // When: si preme a metà e si trascina a sinistra; i primi 10 pixel superano la soglia e non contano
        barra.processaPressione(150, 35, Tasto.SINISTRO);
        barra.processaTrascinamento(140, 35);
        barra.processaTrascinamento(110, 35);
        // Then
        assertEquals(30.0d, barra.getScorrimento(), 0.001d);
        assertEquals(xPrimaIcona - 30, barra.iconeVisibili.get(1).rettangolo.x);
        // When: si lascia subito, senza velocità
        barra.processaRilascio(110, 35, Tasto.SINISTRO);
        // Then: nessun comando e nessuna inerzia
        assertTrue(comandiMandati.isEmpty(), "trascinare non è toccare: " + comandiMandati);
        assertFalse(barra.inTransizione());
        // When: si trascina oltre il capo
        barra.processaPressione(150, 35, Tasto.SINISTRO);
        barra.processaTrascinamento(300, 35);
        barra.processaTrascinamento(1000, 35);
        // Then: lo scorrimento non esce dai limiti
        assertEquals(0.0d, barra.getScorrimento(), 0.001d);
    }

    @Test
    void unPiccoloMovimentoSottoLaSogliaEUnTocco() {
        // Given
        BarraDiProva barra = new BarraDiProva(DisplayableCanvas.ORIENTAMENTO_ORIZZONTALE, LARGHEZZA, ALTEZZA);
        imposta(barra, 8);
        int x = 100;
        Comando atteso = comandoNellaPosizione(barra, x, 35);
        assertNotNull(atteso);
        // When
        barra.processaPressione(x, 35, Tasto.SINISTRO);
        barra.processaTrascinamento(x + 3, 35);
        barra.processaRilascio(x + 3, 35, Tasto.SINISTRO);
        // Then
        assertEquals(0.0d, barra.getScorrimento(), 0.001d);
        assertEquals(1, comandiMandati.size());
        assertEquals(atteso, comandiMandati.get(0).getComando());
    }

    @Test
    void ilRilascioSuUnAltraIconaNonEUnTocco() {
        BarraDiProva barra = new BarraDiProva(DisplayableCanvas.ORIENTAMENTO_ORIZZONTALE, LARGHEZZA, ALTEZZA);
        imposta(barra, 3);
        barra.processaPressione(barra.iconeVisibili.get(0).rettangolo.x + 5, 35, Tasto.SINISTRO);
        barra.processaRilascio(barra.iconeVisibili.get(1).rettangolo.x + 5, 35, Tasto.SINISTRO);
        assertTrue(comandiMandati.isEmpty(), "" + comandiMandati);
    }

    @Test
    void ilClickDiSwingDopoIlRilascioNonVieneContatoDueVolte() {
        BarraDiProva barra = new BarraDiProva(DisplayableCanvas.ORIENTAMENTO_ORIZZONTALE, LARGHEZZA, ALTEZZA);
        imposta(barra, 3);
        int x = barra.iconeVisibili.get(0).rettangolo.x + 5;
        barra.processaPressione(x, 35, Tasto.SINISTRO);
        barra.processaRilascio(x, 35, Tasto.SINISTRO);
        barra.processaClick(x, 35, Tasto.SINISTRO);
        assertEquals(1, comandiMandati.size());
    }

    /** Un trascinamento a sinistra a 1000 pixel al secondo, lasciato dopo 5 ms dall'ultimo movimento */
    private void trascinaVeloce(DisplayableCanvasBarraIcone barra) {
        barra.processaPressione(200, 35, Tasto.SINISTRO);
        orologio += 10 * MILLISECONDO;
        barra.processaTrascinamento(190, 35);
        orologio += 10 * MILLISECONDO;
        barra.processaTrascinamento(180, 35);
        orologio += 10 * MILLISECONDO;
        barra.processaTrascinamento(170, 35);
        orologio += 10 * MILLISECONDO;
        barra.processaTrascinamento(160, 35);
    }

    @Test
    void lasciandoVeloceLeIconeProseguonoPerInerziaEPoiSiFermano() {
        // Given
        BarraDiProva barra = new BarraDiProva(DisplayableCanvas.ORIENTAMENTO_ORIZZONTALE, LARGHEZZA, ALTEZZA);
        imposta(barra, 8);
        trascinaVeloce(barra);
        double allaFineDelTrascinamento = barra.getScorrimento();
        assertEquals(30.0d, allaFineDelTrascinamento, 0.001d);
        // When
        orologio += 5 * MILLISECONDO;
        barra.processaRilascio(160, 35, Tasto.SINISTRO);
        // Then: la barra chiede di essere ridisegnata da sola
        assertTrue(barra.inTransizione());
        lasciaPassare(barra, 32);
        assertTrue(barra.getScorrimento() > allaFineDelTrascinamento, "prosegue: " + barra.getScorrimento());
        // When: passa il tempo
        lasciaPassare(barra, 4000);
        // Then: si è fermata, avanti rispetto a dove era, dentro i limiti
        assertFalse(barra.inTransizione());
        assertTrue(barra.getScorrimento() > allaFineDelTrascinamento + 100, "" + barra.getScorrimento());
        assertTrue(barra.getScorrimento() <= barra.getScorrimentoMassimo());
        assertTrue(comandiMandati.isEmpty());
    }

    @Test
    void sePuntatoreEraFermoAlRilascioNonCeInerzia() {
        BarraDiProva barra = new BarraDiProva(DisplayableCanvas.ORIENTAMENTO_ORIZZONTALE, LARGHEZZA, ALTEZZA);
        imposta(barra, 8);
        trascinaVeloce(barra);
        orologio += 300 * MILLISECONDO;
        barra.processaRilascio(160, 35, Tasto.SINISTRO);
        assertFalse(barra.inTransizione());
    }

    @Test
    void inerziaSiFermaAlCapoSenzaRimbalzo() {
        BarraDiProva barra = new BarraDiProva(DisplayableCanvas.ORIENTAMENTO_ORIZZONTALE, LARGHEZZA, ALTEZZA);
        imposta(barra, 8);
        // Trascinando verso destra le icone tornano verso l'inizio: già a 0 non c'è dove andare
        barra.processaPressione(100, 35, Tasto.SINISTRO);
        orologio += 10 * MILLISECONDO;
        barra.processaTrascinamento(120, 35);
        orologio += 10 * MILLISECONDO;
        barra.processaTrascinamento(160, 35);
        orologio += 5 * MILLISECONDO;
        barra.processaRilascio(160, 35, Tasto.SINISTRO);
        lasciaPassare(barra, 2000);
        assertEquals(0.0d, barra.getScorrimento(), 0.001d);
        assertFalse(barra.inTransizione());
    }

    @Test
    void toccareLeIconeMentreScorronoLeFermaSenzaEseguireIlComando() {
        BarraDiProva barra = new BarraDiProva(DisplayableCanvas.ORIENTAMENTO_ORIZZONTALE, LARGHEZZA, ALTEZZA);
        imposta(barra, 8);
        trascinaVeloce(barra);
        orologio += 5 * MILLISECONDO;
        barra.processaRilascio(160, 35, Tasto.SINISTRO);
        assertTrue(barra.inTransizione());
        // When: si tocca a metà della corsa
        lasciaPassare(barra, 64);
        barra.processaPressione(150, 35, Tasto.SINISTRO);
        barra.processaRilascio(150, 35, Tasto.SINISTRO);
        // Then
        assertFalse(barra.inTransizione());
        assertTrue(comandiMandati.isEmpty(), "il tocco che ferma non esegue: " + comandiMandati);
    }

    @Test
    void leFrecceScorronoDiUnaPaginaConUnaBreveAnimazione() {
        // Given: la fascia di mezzo tiene due icone, quindi la pagina è di una sola icona
        BarraDiProva barra = new BarraDiProva(DisplayableCanvas.ORIENTAMENTO_ORIZZONTALE, LARGHEZZA, ALTEZZA);
        imposta(barra, 8);
        // When: a inizio corsa la freccia indietro è spenta e non fa niente
        barra.esegui(Comando.SINISTRA);
        assertFalse(barra.inTransizione());
        // When
        barra.esegui(Comando.DESTRA);
        // Then
        assertTrue(barra.inTransizione());
        assertEquals(0.0d, barra.getScorrimento(), 0.001d);
        lasciaPassare(barra, 1000);
        assertEquals(LATO, barra.getScorrimento(), 0.001d);
        assertFalse(barra.inTransizione());
        // When
        barra.esegui(Comando.SINISTRA);
        lasciaPassare(barra, 1000);
        // Then
        assertEquals(0.0d, barra.getScorrimento(), 0.001d);
        assertTrue(comandiMandati.isEmpty(), "le frecce non vanno al gioco: " + comandiMandati);
    }

    @Test
    void leFrecceArrivanoFinoAlCapoEsattamente() {
        BarraDiProva barra = new BarraDiProva(DisplayableCanvas.ORIENTAMENTO_ORIZZONTALE, LARGHEZZA, ALTEZZA);
        imposta(barra, 8);
        for (int i = 0; i < 12; i++) {
            barra.esegui(Comando.DESTRA);
            lasciaPassare(barra, 1000);
        }
        assertEquals(barra.getScorrimentoMassimo(), barra.getScorrimento(), 0.001d);
    }

    @Test
    void ilToccoSuUnaFrecciaLaFaScorrere() {
        BarraDiProva barra = new BarraDiProva(DisplayableCanvas.ORIENTAMENTO_ORIZZONTALE, LARGHEZZA, ALTEZZA);
        imposta(barra, 8);
        barra.processaPressione(LARGHEZZA - 10, 35, Tasto.SINISTRO);
        barra.processaRilascio(LARGHEZZA - 10, 35, Tasto.SINISTRO);
        lasciaPassare(barra, 1000);
        assertEquals(LATO, barra.getScorrimento(), 0.001d);
        assertTrue(comandiMandati.isEmpty());
    }

    @Test
    void ilTrascinamentoNonParteDaUnaFreccia() {
        BarraDiProva barra = new BarraDiProva(DisplayableCanvas.ORIENTAMENTO_ORIZZONTALE, LARGHEZZA, ALTEZZA);
        imposta(barra, 8);
        barra.processaPressione(LARGHEZZA - 10, 35, Tasto.SINISTRO);
        barra.processaTrascinamento(LARGHEZZA - 60, 35);
        barra.processaTrascinamento(LARGHEZZA - 100, 35);
        assertEquals(0.0d, barra.getScorrimento(), 0.001d);
    }

    @Test
    void lePartiDelleIconeFuoriDallaFasciaDiMezzoNonSiToccano() {
        BarraDiProva barra = new BarraDiProva(DisplayableCanvas.ORIENTAMENTO_ORIZZONTALE, LARGHEZZA, ALTEZZA);
        imposta(barra, 8);
        // Si scorre di un'icona: una icona esce a destra, in parte sotto la freccia
        barra.esegui(Comando.DESTRA);
        lasciaPassare(barra, 1000);
        int x = LARGHEZZA - LATO + 14;
        Comando sotto = comandoNellaPosizione(barra, x, 35);
        assertNotNull(sotto);
        // When: si tocca dove l'icona che esce e la freccia si sovrappongono
        barra.processaPressione(x, 35, Tasto.SINISTRO);
        barra.processaRilascio(x, 35, Tasto.SINISTRO);
        // Then: vince la freccia, che ha fatto scorrere; l'icona non ha ricevuto il tocco
        assertTrue(comandiMandati.isEmpty(), "" + comandiMandati);
        lasciaPassare(barra, 1000);
        assertEquals(2.0d * LATO, barra.getScorrimento(), 0.001d);
    }

    @Test
    void inVerticaleLeIconeSiDispongonoLungoLAltezzaSenzaSovrapporsi() {
        int altezzaBarra = 600;
        int larghezzaBarra = 72;
        int latoVerticale = TipoIcona.getAltezzaMassima();
        // Quante ci stanno senza frecce
        BarraDiProva barra = new BarraDiProva(DisplayableCanvas.ORIENTAMENTO_VERTICALE, larghezzaBarra, altezzaBarra);
        imposta(barra, 4);
        assertFalse(barra.isScorrevole());
        for (int i = 0; i + 1 < barra.iconeVisibili.size(); i++) {
            Rectangle a = barra.iconeVisibili.get(i).rettangolo;
            Rectangle b = barra.iconeVisibili.get(i + 1).rettangolo;
            assertEquals(latoVerticale, b.y - a.y, "icone adiacenti, senza sovrapposizioni");
            assertEquals((larghezzaBarra - LATO) / 2, a.x, "centrate nella colonna");
        }
        // Troppe: le frecce SU e GIU stanno ai capi
        imposta(barra, 14);
        assertTrue(barra.isScorrevole());
        IconaVisibile prima = barra.iconeVisibili.get(0);
        IconaVisibile ultima = barra.iconeVisibili.get(barra.iconeVisibili.size() - 1);
        assertEquals(Comando.SU, prima.comando);
        assertEquals(0, prima.rettangolo.y);
        assertEquals(Comando.GIU, ultima.comando);
        assertEquals(altezzaBarra - latoVerticale, ultima.rettangolo.y);
    }

    @Test
    void inVerticaleSiTrascinaSuYENonSuX() {
        BarraDiProva barra = new BarraDiProva(DisplayableCanvas.ORIENTAMENTO_VERTICALE, 72, 400);
        imposta(barra, 10);
        assertTrue(barra.isScorrevole());
        // Un movimento orizzontale non scorre
        barra.processaPressione(36, 200, Tasto.SINISTRO);
        barra.processaTrascinamento(10, 200);
        barra.processaTrascinamento(0, 200);
        assertEquals(0.0d, barra.getScorrimento(), 0.001d);
        barra.processaRilascio(0, 200, Tasto.SINISTRO);
        // Uno verticale sì: anche uscendo dalla colonna, con coordinate fuori dalla barra
        barra.processaPressione(36, 200, Tasto.SINISTRO);
        barra.processaTrascinamento(36, 180);
        barra.processaTrascinamento(-50, 150);
        assertEquals(30.0d, barra.getScorrimento(), 0.001d);
    }

    @Test
    void laBarraSiFaCatturareIlTrascinamento() {
        assertTrue(new BarraDiProva(DisplayableCanvas.ORIENTAMENTO_ORIZZONTALE, LARGHEZZA, ALTEZZA).catturaIlTrascinamento());
    }

    // ---- Il Dock fa le stesse cose ----

    @Test
    void ilDockScorreComeLaBarraClassica() {
        DockDiProva dock = new DockDiProva();
        imposta(dock, 8);
        assertTrue(dock.isScorrevole());
        dock.processaMovimento(150, 35);
        // Si trascina
        dock.processaPressione(150, 35, Tasto.SINISTRO);
        dock.processaTrascinamento(140, 35);
        dock.processaTrascinamento(110, 35);
        assertEquals(30.0d, dock.getScorrimento(), 0.001d);
        dock.processaRilascio(110, 35, Tasto.SINISTRO);
        assertTrue(comandiMandati.isEmpty());
        // Una freccia
        dock.processaPressione(LARGHEZZA - 10, 35, Tasto.SINISTRO);
        dock.processaRilascio(LARGHEZZA - 10, 35, Tasto.SINISTRO);
        lasciaPassare(dock, 1000);
        assertEquals(30.0d + LATO, dock.getScorrimento(), 0.001d);
    }

    @Test
    void ilDockEseguiIlComandoDellIconaSottoIlCursoreAncheIngrandita() {
        // Given: tre comandi e l'aiuto, centrati nella barra
        DockDiProva dock = new DockDiProva();
        imposta(dock, 3);
        assertFalse(dock.isScorrevole());
        Rectangle seconda = dock.iconeVisibili.get(1).rettangolo;
        int x = seconda.x + seconda.width / 2;
        // When: il cursore entra e tocca l'icona
        dock.processaMovimento(x, 35);
        dock.processaPressione(x, 35, Tasto.SINISTRO);
        dock.processaRilascio(x, 35, Tasto.SINISTRO);
        // Then
        assertEquals(1, comandiMandati.size());
        assertEquals(dock.iconeVisibili.get(1).comando, comandiMandati.get(0).getComando());
    }

    @Test
    void ilDockDisegnaSenzaErroriConESenzaScorrimento() {
        for (int quante : new int[]{3, 8}) {
            DockDiProva dock = new DockDiProva();
            imposta(dock, quante);
            dock.processaMovimento(150, 35);
            lasciaPassare(dock, 200);
            dock.processaUscita(150, 35);
            lasciaPassare(dock, 200);
        }
    }

    @Test
    void ilDockReclamaIPuntiDelleIconeIngrandite() {
        DockDiProva dock = new DockDiProva();
        imposta(dock, 3);
        Rectangle seconda = dock.iconeVisibili.get(1).rettangolo;
        int x = seconda.x + seconda.width / 2;
        dock.processaMovimento(x, 35);
        // L'effetto del Dock cresce in tempo reale (120 ms)
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        // L'icona ingrandita esce sopra la barra: un punto appena sopra, sopra di lei, è della barra
        assertTrue(dock.contiene(x, -5), "l'icona ingrandita esce sopra la barra");
        assertFalse(dock.contiene(x, -80));
    }
}
