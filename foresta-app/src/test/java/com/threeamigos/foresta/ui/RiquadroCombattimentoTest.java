package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.interfacce.VistaGruppo;
import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.interfacce.VistaPersonaggio;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.tipi.tattiche.TipoAzione;
import com.threeamigos.foresta.ui.immagini.ImageCache;
import com.threeamigos.foresta.ui.riquadri.DisplayableCanvasRiquadroCombattimento;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.GraphicsEnvironment;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
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
                new DisplayableCanvasRiquadroCombattimento(LARGHEZZA, ALTEZZA, Y_PREFERITA, vista(ordine, loro));
        riquadro.setVisible(true);
        riquadro.setCombattente(ordine.get(0));
        riquadro.setAvversario(ordine.size() > 1 ? ordine.get(1) : null);
        riquadro.setAzione(ordine.get(0), DisplayableCanvasRiquadroCombattimento.testoDellAzione(TipoAzione.CANTO_DI_VALORE, ordine.get(0), null));
        BufferedImage immagine = new BufferedImage(LARGHEZZA, ALTEZZA, BufferedImage.TYPE_INT_RGB);
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
}
