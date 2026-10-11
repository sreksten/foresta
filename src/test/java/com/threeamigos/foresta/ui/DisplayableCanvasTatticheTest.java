package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.interfacce.VistaGruppoGiocatore;
import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.interfacce.VistaPersonaggio;
import com.threeamigos.foresta.modellodati.tattiche.AzioneMD;
import com.threeamigos.foresta.modellodati.tattiche.FiltroMD;
import com.threeamigos.foresta.modellodati.tattiche.OrdinamentoMD;
import com.threeamigos.foresta.modellodati.tattiche.PredicatoMD;
import com.threeamigos.foresta.modellodati.tattiche.SelettoreMD;
import com.threeamigos.foresta.modellodati.tattiche.TatticaMD;
import com.threeamigos.foresta.modellodati.tattiche.VoceMD;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.tipi.tattiche.Confronto;
import com.threeamigos.foresta.tipi.tattiche.CriterioIncantesimo;
import com.threeamigos.foresta.tipi.tattiche.TipoAzione;
import com.threeamigos.foresta.tipi.tattiche.TipoFiltro;
import com.threeamigos.foresta.tipi.tattiche.TipoOrdinamento;
import com.threeamigos.foresta.tipi.tattiche.TipoPredicato;
import com.threeamigos.foresta.tipi.tattiche.TipoRosa;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * La schermata delle tattiche si disegna e si usa senza partita vera: la vista della partita è un finto con un gruppo di due personaggi. Il
 * disegno si salva in target/tattiche.png per guardarlo.
 */
class DisplayableCanvasTatticheTest {

    /** Un'implementazione che risponde ai metodi di una interfaccia con i valori che servono, e con "vuoto" a tutti gli altri */
    @SuppressWarnings("unchecked")
    private static <T> T finto(Class<T> tipo, java.util.function.BiFunction<String, Object[], Object> risposte) {
        return (T) Proxy.newProxyInstance(tipo.getClassLoader(), new Class<?>[]{tipo}, (proxy, metodo, argomenti) -> {
            Object risposta = risposte.apply(metodo.getName(), argomenti);
            if (risposta != null) {
                return risposta;
            }
            Class<?> ritorno = metodo.getReturnType();
            if (ritorno == boolean.class) {
                return false;
            }
            if (ritorno == int.class) {
                return 0;
            }
            if (ritorno == double.class) {
                return 0.0d;
            }
            return null;
        });
    }

    private static VistaPersonaggio personaggio(String uuid, String nome, TipoPersonaggio classe) {
        return finto(VistaPersonaggio.class, (metodo, argomenti) -> {
            switch (metodo) {
                case "getUuid":
                    return uuid;
                case "getNome":
                    return nome;
                case "getClasse":
                    return classe;
                case "isVivo":
                    return true;
                default:
                    return null;
            }
        });
    }

    private static DisplayableCanvasTattiche schermata(VistaPersonaggio scelto, TatticaMD tattica) {
        VistaPersonaggio altro = personaggio("due", "Secondo", TipoPersonaggio.MAGO);
        VistaGruppoGiocatore gruppo = finto(VistaGruppoGiocatore.class, (metodo, argomenti) -> {
            switch (metodo) {
                case "getCapo":
                    return scelto;
                case "getPersonaggi":
                    return Arrays.asList(scelto, altro);
                default:
                    return null;
            }
        });
        VistaPartita partita = finto(VistaPartita.class, (metodo, argomenti) -> "getGruppoGiocatore".equals(metodo) ? gruppo : null);
        DisplayableCanvasTattiche schermata = new DisplayableCanvasTattiche(1024, 768, partita);
        schermata.impostaTattica(scelto, tattica, TatticaMD.VUOTA, null);
        return schermata;
    }

    private static TatticaMD tatticaDiProva() {
        return TatticaMD.di(
                new VoceMD(new SelettoreMD(TipoRosa.AVVERSARI,
                        Collections.singletonList(FiltroMD.conStato(TipoFiltro.STATO, TipoEffettoDiStato.SILENZIATO).negato()),
                        OrdinamentoMD.di(TipoOrdinamento.MENO_SALUTE)),
                        Collections.singletonList(PredicatoMD.quanti(TipoRosa.AVVERSARI,
                                Collections.singletonList(FiltroMD.conConfrontoENumero(TipoFiltro.SALUTE, Confronto.MINORE, 30)),
                                Confronto.MAGGIORE, 0)),
                        Arrays.asList(AzioneMD.incantesimoDiDanno(CriterioIncantesimo.PIU_ADATTO, true), AzioneMD.di(TipoAzione.DARDO_ARCANO))),
                new VoceMD(new SelettoreMD(TipoRosa.ALLEATI,
                        Collections.singletonList(FiltroMD.conConfrontoENumero(TipoFiltro.SALUTE_PERCENTUALE, Confronto.MINORE, 50)),
                        OrdinamentoMD.di(TipoOrdinamento.MENO_SALUTE)),
                        Collections.singletonList(PredicatoMD.probabilita(70)),
                        Arrays.asList(AzioneMD.di(TipoAzione.POZIONE_SALUTE_GRANDE), AzioneMD.di(TipoAzione.POZIONE_MAGIA_GRANDE))));
    }

    private static BufferedImage disegna(DisplayableCanvasTattiche schermata) {
        BufferedImage immagine = new BufferedImage(1024, 768, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = immagine.createGraphics();
        schermata.disegna(graphics);
        graphics.dispose();
        return immagine;
    }

    @Test
    void siDisegnaESiSalvaIlDisegno() throws Exception {
        DisplayableCanvasTattiche schermata = schermata(personaggio("uno", "Arsenio", TipoPersonaggio.GUERRIERO), tatticaDiProva());
        BufferedImage immagine = disegna(schermata);
        File file = new File("target/tattiche.png");
        file.getParentFile().mkdirs();
        ImageIO.write(immagine, "png", file);
        assertTrue(file.length() > 0);
    }

    @Test
    void siModificaLaBozzaEIlMenuSiApre() throws Exception {
        DisplayableCanvasTattiche schermata = schermata(personaggio("uno", "Arsenio", TipoPersonaggio.GUERRIERO), TatticaMD.VUOTA);
        disegna(schermata);
        assertTrue(schermata.attivaPezzo("Nuova voce"));
        assertEquals(1, schermata.getBozza().getVoci().size());
        // Il menu delle azioni si apre e si disegna
        assertTrue(schermata.attivaPezzo("(aggiungi un'azione)"));
        assertTrue(schermata.isMenuAperto());
        // Il mouse su una scelta del menu: a destra la sua spiegazione
        schermata.processaMovimento(110, 142);
        File file = new File("target/tattiche-menu.png");
        file.getParentFile().mkdirs();
        ImageIO.write(disegna(schermata), "png", file);
        // Una condizione nuova: ci vuole il menu, e il valore si cambia con più e meno
        schermata.impostaTattica(personaggio("uno", "Arsenio", TipoPersonaggio.GUERRIERO), TatticaMD.VUOTA, TatticaMD.VUOTA, null);
        assertFalse(schermata.isMenuAperto());
    }
}
