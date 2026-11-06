package com.threeamigos.foresta.ui.schermate;

import com.threeamigos.foresta.artefatti.VistaArtefatto;
import com.threeamigos.foresta.artefatti.VistaScambio;
import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.ui.componenti.ComponenteScorrevole;
import com.threeamigos.foresta.ui.componenti.ElencoScorrevole;
import com.threeamigos.foresta.ui.componenti.ScorrimentoSuUnAsse;
import com.threeamigos.foresta.ui.componenti.ValoreDiDueTesti;
import com.threeamigos.foresta.ui.immagini.BufferedImageBuilder;
import com.threeamigos.foresta.ui.immagini.ImageCache;
import com.threeamigos.foresta.ui.LivelloDiZoom;
import com.threeamigos.foresta.ui.immagini.NomiDelleImmagini;
import com.threeamigos.foresta.ui.sprite.DescrizioneSpriteATempo;
import com.threeamigos.foresta.ui.sprite.SpriteATempo;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Base delle schermate che scambiano artefatti fra due liste affiancate (inventario, commercianti, incantatore), nella versione Swing: disegna le due liste e
 * traduce click e doppi click in comandi di scambio. Le liste (come alberi da far scorrere), l'artefatto sotto un punto, l'aiuto e i comandi sono di
 * {@link ScambioDegliArtefatti} e {@link ElencoDegliArtefatti}, nel modulo comune; qui c'è il disegno.
 *
 * @author Stefano Reksten
 */
public abstract class DisplayableCanvasScambiatoreArtefatti extends DisplayableCanvasScambiatore {

    // Lo scambio aperto, in sola lettura: gli spostamenti si chiedono con ComandoScambioArtefatto
    protected VistaScambio scambio;

    // Quota (in coordinate della finestra) a cui inizia l'elenco delle caratteristiche del
    // personaggio, aggiornata a ogni disegnaInventario e usata per l'hit-test dei click.
    protected int yAttributi = 0;

    private final ScambioDegliArtefatti logica;

    DisplayableCanvasScambiatoreArtefatti(int width, int height, VistaPartita vistaPartita) {
        super(width, height, vistaPartita);
        logica = new ScambioDegliArtefatti(this, vistaPartita, new MisureDelleImmaginiAwt(), new ScambioDegliArtefatti.Personalizzazioni() {
            @Override
            public String aiutoDoppioClickSinistra() {
                return DisplayableCanvasScambiatoreArtefatti.this.aiutoDoppioClickSinistra();
            }

            @Override
            public String aiutoDoppioClickDestra() {
                return DisplayableCanvasScambiatoreArtefatti.this.aiutoDoppioClickDestra();
            }

            @Override
            public List<String> righeAiutoColonnaPersonaggio() {
                return DisplayableCanvasScambiatoreArtefatti.this.righeAiutoColonnaPersonaggio();
            }

            @Override
            public boolean processaClickPersonaggio(int x, int y, Tasto tasto) {
                return DisplayableCanvasScambiatoreArtefatti.this.processaClickPersonaggio(x, y, tasto);
            }

            @Override
            public boolean processaDoppioClickPersonaggio(int x, int y, Tasto tasto) {
                return DisplayableCanvasScambiatoreArtefatti.this.processaDoppioClickPersonaggio(x, y, tasto);
            }

            @Override
            public String fraseDopoLaVendita() {
                return DisplayableCanvasScambiatoreArtefatti.this.fraseDopoLaVendita();
            }

            @Override
            public void aggiungiSpriteLocale(DescrizioneSpriteATempo sprite) {
                DisplayableCanvasScambiatoreArtefatti.this.aggiungiSpriteLocale(new SpriteATempo(sprite));
            }

            @Override
            public DoomdarkColorModel.Color coloreLivello(VistaArtefatto artefatto, boolean parteAttiva) {
                return DisplayableCanvasScambiatoreArtefatti.this.coloreLivello(artefatto, parteAttiva);
            }

            @Override
            public DoomdarkColorModel.Color coloreEffetto(VistaArtefatto artefatto, Object effetto, boolean parteAttiva, DoomdarkColorModel.Color predefinito) {
                return DisplayableCanvasScambiatoreArtefatti.this.coloreEffetto(artefatto, effetto, parteAttiva, predefinito);
            }
        });
    }

    /**
     * Che cosa dice chi ha appena comprato qualcosa dal gruppo.
     */
    String fraseDopoLaVendita() {
        return com.threeamigos.foresta.ui.testo.TestiDellaUI.get("ARTEFATTI_GRAZIE_AFFARI");
    }

    /**
     * Imposta lo scambio fra le due liste di artefatti.
     */
    public void impostaScambio(VistaScambio scambio) {
        this.scambio = scambio;
        logica.impostaScambio(scambio);
    }

    /**
     * Disegna le due liste affiancate.
     */
    public void disegnaInventario(Graphics2D graphics) {

        super.disegnaInventario(graphics);

        if (scambio == null) {
            return;
        }

        disegnaColonnaPersonaggio(graphics);

        disegnaElenco(graphics, logica.getArtefattiDellaParteAttiva(), xMinimaZonaSinistra, scorrimentoSinistra, true);
        disegnaElenco(graphics, logica.getArtefattiDellaParteRemota(), xMinimaZonaDestra, scorrimentoDestra, false);

        disegnaIntestazioniInventario(graphics);

        disegnaSpriteLocali(graphics);

        disegnaAiuto(graphics, logica.righeAiuto());
    }

    /**
     * Cosa fa il doppio click su un artefatto della parte sinistra (parte attiva) e della destra: le sottoclassi
     * lo dicono con le parole della loro schermata, per l'aiuto
     */
    protected abstract String aiutoDoppioClickSinistra();

    /**
     * Il testo di aiuto che dice cosa fa il doppio click destro in questa schermata.
     */
    protected abstract String aiutoDoppioClickDestra();

    /**
     * L'aiuto per la colonna centrale, quando il mouse non è su un elenco di artefatti; di norma nessuno.
     */
    protected List<String> righeAiutoColonnaPersonaggio() {
        return new ArrayList<>();
    }

    /**
     * Il colore del livello di un artefatto: di suo grigio, le schermate lo ridefiniscono per dire a colpo
     * d'occhio se l'artefatto si può prendere (inventario) o incantare (incantatore).
     */
    protected DoomdarkColorModel.Color coloreLivello(VistaArtefatto artefatto, boolean parteAttiva) {
        return DoomdarkColorModel.Color.MEDIUM_GRAY;
    }

    /**
     * Il colore di una riga di effetto (un ModificatoreAttributoMD o un IncantamentoMD) di un artefatto: di suo
     * quello predefinito, le schermate lo ridefiniscono per evidenziare certi effetti (l'incantatore, quelli che
     * entrano ed escono con la fusione).
     */
    protected DoomdarkColorModel.Color coloreEffetto(VistaArtefatto artefatto, Object effetto, boolean parteAttiva,
                                                     DoomdarkColorModel.Color predefinito) {
        return predefinito;
    }

    /**
     * Le immagini degli oggetti opachi dell'elenco: il nome di una risorsa (un separatore) o un valore di due testi (livello e prezzo)
     */
    private static Image immagineDelNodo(Object oggetto) {
        if (oggetto instanceof String) {
            String risorsa = (String) oggetto;
            return ImageCache.getComposta(Arrays.asList("immagineDelNodo", risorsa),
                    () -> BufferedImageBuilder.buildBufferedImage(risorsa, NomiDelleImmagini.conZoom(risorsa) ? LivelloDiZoom.valore() : 1.0));
        }
        ValoreDiDueTesti valore = (ValoreDiDueTesti) oggetto;
        return ImageCache.getComposta(Arrays.asList("valoreDiDueTesti", valore), () -> {
            Image primo = ImageCache.get(valore.getPrimo(), valore.getFont(), valore.getColoreDelPrimo());
            Image secondo = ImageCache.get(valore.getSecondo(), valore.getFont(), valore.getColoreDelSecondo());
            BufferedImage immagine = BufferedImageBuilder.creaCompatibile(valore.getMisura().larghezza, valore.getMisura().altezza, Transparency.TRANSLUCENT);
            Graphics2D g2d = immagine.createGraphics();
            g2d.drawImage(primo, 0, 0, null);
            g2d.drawImage(secondo, valore.getXDelSecondo(), 0, null);
            g2d.dispose();
            return immagine;
        });
    }

    private void disegnaElenco(Graphics2D graphics, Collection<? extends VistaArtefatto> artefatti, int x,
                               ScorrimentoSuUnAsse scorrimento, boolean parteAttiva) {

        scorrimento.aggiorna();
        int offset = (int) Math.round(scorrimento.getScorrimento());
        VistaArtefatto evidenziato = logica.artefattoSottoIlMouse(artefatti, x, offset, parteAttiva);
        ElencoScorrevole<VistaArtefatto> elenco = logica.costruisciElenco(artefatti, evidenziato, parteAttiva);
        ComponenteScorrevole<VistaArtefatto> componenteScorrevole = new ComponenteScorrevole<>(elenco, larghezzaDisponibileInRiquadroInventario(), 10, 1,
                DisplayableCanvasScambiatoreArtefatti::immagineDelNodo);

        Image image = componenteScorrevole.produci(altezzaDisponibileInRiquadroInventario(), scorrimento);
        graphics.drawImage(image, x + SPACING, DIMENSIONE_BORDO_INTERNO + 2 * SPACING, null);
    }

    /**
     * Per supertipo e tipo, e dentro lo stesso tipo dal livello più alto al più basso: i migliori in cima.
     */
    public static Collection<VistaArtefatto> ordinaArtefattiDaDisegnare(Collection<? extends VistaArtefatto> artefatti) {
        return ElencoDegliArtefatti.ordinaArtefattiDaDisegnare(artefatti);
    }

    /**
     * Gestisce un click nella colonna centrale; dice se l'ha consumato.
     */
    protected abstract boolean processaClickPersonaggio(int x, int y, Tasto tasto);

    @Override
    public void processaClick(int x, int y, Tasto tasto) {
        logica.processaClick(x, y, tasto);
    }

    /**
     * Gestisce un doppio click nella colonna centrale; dice se l'ha consumato.
     */
    protected abstract boolean processaDoppioClickPersonaggio(int x, int y, Tasto tasto);

    @Override
    public void processaDoppioClick(int x, int y, Tasto tasto) {
        logica.processaDoppioClick(x, y, tasto);
    }
}
