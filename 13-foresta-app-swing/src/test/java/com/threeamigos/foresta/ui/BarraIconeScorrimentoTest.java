package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoDiGioco;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoImpostazioneAiuto;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.motore.VistaPartitaMotore;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoIcona;
import com.threeamigos.foresta.ui.Finestra;
import com.threeamigos.foresta.ui.Finestra.Tasto;
import com.threeamigos.foresta.ui.barra.ComandiPossibili;
import com.threeamigos.foresta.ui.componenti.ScorrimentoSuUnAsse;
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
    void seNonCiStannoLeIconeScorronoSuTuttaLaLunghezzaSenzaBottoniAiCapi() {
        // Given
        BarraDiProva barra = new BarraDiProva(DisplayableCanvas.ORIENTAMENTO_ORIZZONTALE, LARGHEZZA, ALTEZZA);
        // When
        imposta(barra, 8);
        // Then
        assertTrue(barra.isScorrevole());
        for (IconaVisibile icona : barra.iconeVisibili) {
            assertNotEquals(Comando.SINISTRA, icona.comando);
            assertNotEquals(Comando.DESTRA, icona.comando);
        }
        // La prima icona sta a inizio barra e l'ultima visibile esce dalla fine (la ritaglia il disegno)
        assertEquals(0, barra.iconeVisibili.get(0).rettangolo.x);
        IconaVisibile ultima = barra.iconeVisibili.get(barra.iconeVisibili.size() - 1);
        assertTrue(ultima.rettangolo.x < LARGHEZZA);
        assertTrue(ultima.rettangolo.x + ultima.rettangolo.width > LARGHEZZA);
        // 9 icone con l'aiuto, su tutta la lunghezza
        assertEquals(9 * LATO - LARGHEZZA, barra.getScorrimentoMassimo(), 0.001d);
    }

    @Test
    void laRiservaAFineAsseAccorciaLaBarraDelleIcone() {
        // Given: 4 icone ci stanno in 300 pixel
        BarraDiProva barra = new BarraDiProva(DisplayableCanvas.ORIENTAMENTO_ORIZZONTALE, LARGHEZZA, ALTEZZA);
        imposta(barra, 3);
        assertFalse(barra.isScorrevole());
        // When: il joystick prende 100 pixel a destra
        barra.setRiservaAFineAsse(100);
        // Then: non ci stanno più, e scorrono nei 200 rimasti
        assertTrue(barra.isScorrevole());
        assertEquals(4 * LATO - 200, barra.getScorrimentoMassimo(), 0.001d);
        for (IconaVisibile icona : barra.iconeVisibili) {
            assertTrue(icona.rettangolo.x < 200, "" + icona.rettangolo);
        }
        // When: la riserva sparisce
        barra.setRiservaAFineAsse(0);
        assertFalse(barra.isScorrevole());
    }

    @Test
    void nellaRiservaAFineAsseNonSiTocca() {
        BarraDiProva barra = new BarraDiProva(DisplayableCanvas.ORIENTAMENTO_ORIZZONTALE, LARGHEZZA, ALTEZZA);
        imposta(barra, 8);
        barra.setRiservaAFineAsse(100);
        // Dentro la lunghezza utile si tocca
        barra.processaPressione(150, 35, Tasto.SINISTRO);
        barra.processaRilascio(150, 35, Tasto.SINISTRO);
        assertEquals(1, comandiMandati.size());
        // Nella riserva no: lì c'è il joystick
        barra.processaPressione(250, 35, Tasto.SINISTRO);
        barra.processaRilascio(250, 35, Tasto.SINISTRO);
        assertEquals(1, comandiMandati.size(), "" + comandiMandati);
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
        // Then: le icone si tirano oltre il capo, ma poco (elastico)
        assertTrue(barra.getScorrimento() < 0.0d, "" + barra.getScorrimento());
        assertTrue(barra.getScorrimento() >= -ScorrimentoSuUnAsse.MASSIMO_ELASTICO, "" + barra.getScorrimento());
        // When: si lascia: tornano al capo con un'animazione
        barra.processaRilascio(1000, 35, Tasto.SINISTRO);
        assertTrue(barra.inTransizione());
        lasciaPassare(barra, 2000);
        assertEquals(0.0d, barra.getScorrimento(), 0.001d);
        assertFalse(barra.inTransizione());
        assertTrue(comandiMandati.isEmpty(), "trascinare non è toccare: " + comandiMandati);
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
    void laRotellaScorreDiUnIconaPerScattoVersoLaFineEVersoLInizio() {
        // Given
        BarraDiProva barra = new BarraDiProva(DisplayableCanvas.ORIENTAMENTO_ORIZZONTALE, LARGHEZZA, ALTEZZA);
        imposta(barra, 8);
        // When: giù, un scatto
        barra.processaRotella(150, 35, 1, Finestra.MovimentoRotella.GIU);
        // Then
        assertTrue(barra.inTransizione());
        lasciaPassare(barra, 1000);
        assertEquals(LATO, barra.getScorrimento(), 0.001d);
        // When: due scatti insieme
        barra.processaRotella(150, 35, 2, Finestra.MovimentoRotella.GIU);
        lasciaPassare(barra, 1000);
        assertEquals(3 * LATO, barra.getScorrimento(), 0.001d);
        // When: su
        barra.processaRotella(150, 35, 3, Finestra.MovimentoRotella.SU);
        lasciaPassare(barra, 1000);
        assertEquals(0.0d, barra.getScorrimento(), 0.001d);
        assertTrue(comandiMandati.isEmpty(), "la rotella non va al gioco: " + comandiMandati);
    }

    @Test
    void laRotellaNonFaNienteSeLeIconeCiStannoTutte() {
        BarraDiProva barra = new BarraDiProva(DisplayableCanvas.ORIENTAMENTO_ORIZZONTALE, LARGHEZZA, ALTEZZA);
        imposta(barra, 2);
        barra.processaRotella(150, 35, 1, Finestra.MovimentoRotella.GIU);
        assertFalse(barra.inTransizione());
        assertEquals(0.0d, barra.getScorrimento(), 0.001d);
    }

    @Test
    void laRotellaArrivaFinoAlCapoEsattamente() {
        BarraDiProva barra = new BarraDiProva(DisplayableCanvas.ORIENTAMENTO_ORIZZONTALE, LARGHEZZA, ALTEZZA);
        imposta(barra, 8);
        for (int i = 0; i < 12; i++) {
            barra.processaRotella(150, 35, 1, Finestra.MovimentoRotella.GIU);
            lasciaPassare(barra, 1000);
        }
        assertEquals(barra.getScorrimentoMassimo(), barra.getScorrimento(), 0.001d);
    }

    @Test
    void ilToccoVicinoAllaFineEseguiLIconaCheCiStaSotto() {
        BarraDiProva barra = new BarraDiProva(DisplayableCanvas.ORIENTAMENTO_ORIZZONTALE, LARGHEZZA, ALTEZZA);
        imposta(barra, 8);
        Comando atteso = comandoNellaPosizione(barra, LARGHEZZA - 10, 35);
        assertNotNull(atteso);
        barra.processaPressione(LARGHEZZA - 10, 35, Tasto.SINISTRO);
        barra.processaRilascio(LARGHEZZA - 10, 35, Tasto.SINISTRO);
        assertEquals(0.0d, barra.getScorrimento(), 0.001d);
        assertEquals(1, comandiMandati.size());
        assertEquals(atteso, comandiMandati.get(0).getComando());
    }

    @Test
    void siTrascinaDaQualunquePuntoDellaBarraAncheDallaFine() {
        BarraDiProva barra = new BarraDiProva(DisplayableCanvas.ORIENTAMENTO_ORIZZONTALE, LARGHEZZA, ALTEZZA);
        imposta(barra, 8);
        barra.processaPressione(LARGHEZZA - 10, 35, Tasto.SINISTRO);
        barra.processaTrascinamento(LARGHEZZA - 60, 35);
        barra.processaTrascinamento(LARGHEZZA - 100, 35);
        assertEquals(40.0d, barra.getScorrimento(), 0.001d);
    }

    @Test
    void lePartiDelleIconeFuoriDallaBarraNonSiToccano() {
        BarraDiProva barra = new BarraDiProva(DisplayableCanvas.ORIENTAMENTO_ORIZZONTALE, LARGHEZZA, ALTEZZA);
        imposta(barra, 8);
        barra.setRiservaAFineAsse(100);
        // Un'icona sta a cavallo della fine della lunghezza utile (200): vi esce a destra
        int x = 250;
        assertNotNull(comandoNellaPosizione(barra, x, 35));
        barra.processaPressione(x, 35, Tasto.SINISTRO);
        barra.processaRilascio(x, 35, Tasto.SINISTRO);
        assertTrue(comandiMandati.isEmpty(), "" + comandiMandati);
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
        // Troppe: scorrono, senza bottoni SU e GIU ai capi
        imposta(barra, 14);
        assertTrue(barra.isScorrevole());
        for (IconaVisibile icona : barra.iconeVisibili) {
            assertNotEquals(Comando.SU, icona.comando);
            assertNotEquals(Comando.GIU, icona.comando);
        }
        assertEquals(0, barra.iconeVisibili.get(0).rettangolo.y);
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
        // La rotella
        dock.processaRotella(150, 35, 1, Finestra.MovimentoRotella.GIU);
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
