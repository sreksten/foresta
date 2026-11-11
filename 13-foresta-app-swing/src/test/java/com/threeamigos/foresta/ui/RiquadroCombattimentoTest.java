package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.interfacce.VistaGruppo;
import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.tipi.TipoAzione;
import com.threeamigos.foresta.ui.immagini.ImageCache;
import com.threeamigos.foresta.ui.riquadri.DisplayableCanvasRiquadroCombattimento;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/**
 * Il riquadro del combattimento mostra tutti i personaggi e cresce con il loro numero: sempre dentro l'area, sotto l'immagine della
 * locazione se ci sta, altrimenti più in alto.
 */
class RiquadroCombattimentoTest {

    private static final int LARGHEZZA = 1000;
    private static final int ALTEZZA = 700;
    private static final int Y_PREFERITA = 300;

    @BeforeAll
    static void caricaLeImmagini() {
        // Le immagini si caricano solo con uno schermo
        assumeFalse(GraphicsEnvironment.isHeadless());
        ImageCache.init();
        // I danni chiedono al gruppo se si arrende: il gruppo deve sapere dove sono i suoi dati, anche se questo test gira da solo
        ModelloDati.installa();
        ModelloDati.setIstanza(new ModelloDati());
    }

    private static VistaPartita vista(List<Personaggio> ordine, List<Personaggio> avversari) {
        VistaGruppo gruppoAvversario = (VistaGruppo) Proxy.newProxyInstance(VistaGruppo.class.getClassLoader(), new Class<?>[]{VistaGruppo.class},
                (proxy, metodo, argomenti) -> metodo.getName().equals("contiene") ? avversari.contains(argomenti[0]) : null);
        return (VistaPartita) Proxy.newProxyInstance(VistaPartita.class.getClassLoader(), new Class<?>[]{VistaPartita.class},
                (proxy, metodo, argomenti) -> {
                    switch (metodo.getName()) {
                        case "getOrdineDiCombattimento":
                            return ordine;
                        case "getGruppoAvversario":
                            return gruppoAvversario;
                        default:
                            return null;
                    }
                });
    }

    private static List<Personaggio> personaggi(TipoPersonaggio tipo, int quanti) {
        List<Personaggio> elenco = new ArrayList<>();
        for (int i = 0; i < quanti; i++) {
            elenco.add(FabbricaPersonaggi.crea(tipo, 1));
        }
        return elenco;
    }

    private static DisplayableCanvasRiquadroCombattimento disegna(int amici, int nemici, String png) throws IOException {
        return disegna(LARGHEZZA, ALTEZZA, Y_PREFERITA, amici, nemici, png);
    }

    private static DisplayableCanvasRiquadroCombattimento disegna(int larghezza, int altezza, int yPreferita, int amici, int nemici, String png)
            throws IOException {
        List<Personaggio> miei = personaggi(TipoPersonaggio.BARDO, amici);
        List<Personaggio> loro = personaggi(TipoPersonaggio.VIVERNA, nemici);
        List<Personaggio> ordine = new ArrayList<>();
        for (int i = 0; i < Math.max(amici, nemici); i++) {
            if (i < amici) ordine.add(miei.get(i));
            if (i < nemici) ordine.add(loro.get(i));
        }
        if (nemici > 1) {
            loro.get(1).subSalute(loro.get(1).getSalute() * 2 / 3, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        }
        if (amici > 1) {
            miei.get(1).setInPanchina(true);
        }
        if (nemici > 2) {
            loro.get(2).subSalute(loro.get(2).getSalute() + 10, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        }
        DisplayableCanvasRiquadroCombattimento riquadro =
                new DisplayableCanvasRiquadroCombattimento(larghezza, altezza, yPreferita, vista(ordine, loro));
        riquadro.setVisible(true);
        riquadro.setCombattente(ordine.get(0));
        riquadro.setAvversario(ordine.size() > 1 ? ordine.get(1) : null);
        riquadro.setAzione(ordine.get(0), DisplayableCanvasRiquadroCombattimento.testoDellAzione(TipoAzione.CANTO_DI_VALORE, ordine.get(0), null));
        BufferedImage immagine = new BufferedImage(larghezza, altezza, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = immagine.createGraphics();
        riquadro.disegnaInfoCombattimento(graphics);
        graphics.dispose();
        if (png != null && System.getProperty("foresta.png") != null) {
            ImageIO.write(immagine, "png", new File(System.getProperty("foresta.png"), png));
        }
        return riquadro;
    }

    @Test
    void ilRiquadroCresceConIPersonaggiESiAlzaSeNonCiStaSottoLaLocazione() throws IOException {
        DisplayableCanvasRiquadroCombattimento piccolo = disegna(1, 1, "combattimento-2.png");
        int altezzaPiccolo = piccolo.getRettangolo().height;
        assertEquals(Y_PREFERITA, piccolo.getYOffset(), "con pochi personaggi sta sotto la locazione");

        DisplayableCanvasRiquadroCombattimento medio = disegna(3, 4, "combattimento-7.png");
        assertTrue(medio.getRettangolo().height > altezzaPiccolo);
        assertTrue(medio.getRettangolo().y + medio.getRettangolo().height <= ALTEZZA);

        DisplayableCanvasRiquadroCombattimento grande = disegna(8, 8, "combattimento-16.png");
        assertTrue(grande.getRettangolo().y >= 0);
        assertTrue(grande.getRettangolo().y + grande.getRettangolo().height <= ALTEZZA, String.valueOf(grande.getRettangolo()));
        assertEquals((LARGHEZZA - grande.getRettangolo().width) / 2, grande.getRettangolo().x, "centrato");
    }

    @Test
    void sulTelefonoMinimoStaNellaColonnaASinistraEDentroLAltezza() throws IOException {
        // Il layout del telefono più stretto (layout_telefono.md §4.4): la colonna a sinistra comincia a 0 e la pila a 398, l'area è alta 505 e la
        // locazione finisce a 328
        int colonnaDestraX = 398;
        int altezza = 505;
        for (int[] scontro : new int[][]{{1, 1}, {5, 5}, {5, 8}}) {
            DisplayableCanvasRiquadroCombattimento riquadro = disegna(colonnaDestraX, altezza, 328, scontro[0], scontro[1], null);
            Rectangle r = riquadro.getRettangolo();
            assertTrue(r.x >= 0 && r.x + r.width <= colonnaDestraX, "non tocca la pila: " + r);
            assertTrue(r.y >= 0 && r.y + r.height <= altezza, "dentro l'altezza: " + r);
        }
    }

    @Test
    void finitoLoScontroIlRiquadroAncoraApertoMostraChiCombattevaInveceDiRestareVuoto() {
        // Il motore svuota l'ordine di combattimento quando lo scontro finisce, ma il riquadro resta aperto finché il fumetto del tutorial lo indica
        List<Personaggio> ordine = new ArrayList<>(personaggi(TipoPersonaggio.BARDO, 2));
        DisplayableCanvasRiquadroCombattimento riquadro = new DisplayableCanvasRiquadroCombattimento(LARGHEZZA, ALTEZZA, Y_PREFERITA,
                vista(ordine, new ArrayList<>()));
        riquadro.setVisible(true);
        assertEquals(2, riquadro.disponi().getRighe().size());
        ordine.clear();
        assertEquals(2, riquadro.disponi().getRighe().size(), "le righe dell'ultimo disegno restano");
        // Chiuso, il prossimo scontro non si porta dietro quello di prima
        riquadro.setVisible(false);
        riquadro.setVisible(true);
        assertNull(riquadro.disponi());
    }
}
