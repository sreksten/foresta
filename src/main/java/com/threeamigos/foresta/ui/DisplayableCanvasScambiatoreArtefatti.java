package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoSpostamentoArtefatto;
import com.threeamigos.foresta.eventi.interni.InternoNotificaViaFumettoATempo;
import com.threeamigos.foresta.eventi.notifiche.NotificaApprovazioneAcquistoArtefatto;
import com.threeamigos.foresta.eventi.notifiche.NotificaApprovazioneVenditaArtefatto;
import com.threeamigos.foresta.eventi.notifiche.NotificaRifiutoAcquistoArtefatto;
import com.threeamigos.foresta.motore.AutomaScambiatoreArtefatti;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.modellodati.ModificatoreAttributo;
import com.threeamigos.foresta.motore.modellodati.SupertipoArtefatto;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.oggetti.Incantamento;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.IntUnaryOperator;

/**
 *
 * @author Stefano Reksten
 */
abstract class DisplayableCanvasScambiatoreArtefatti extends DisplayableCanvasScambiatore {

    protected AutomaScambiatoreArtefatti automa;

    // Quota (in coordinate della finestra) a cui inizia l'elenco delle caratteristiche del
    // personaggio, aggiornata a ogni disegnaInventario e usata per l'hit-test dei click.
    protected int yAttributi = 0;

    DisplayableCanvasScambiatoreArtefatti(int width, int height) {
        super(width, height);
        BusEventi.iscriviti(NotificaApprovazioneAcquistoArtefatto.class, this::gestisciEventoApprovazioneAcquistoArtefatto);
        BusEventi.iscriviti(NotificaRifiutoAcquistoArtefatto.class, this::gestisciEventoRifiutoAcquistoArtefatto);
        BusEventi.iscriviti(NotificaApprovazioneVenditaArtefatto.class, this::gestisciEventoApprovazioneVenditaArtefatto);
    }

    /**
     * Ogni schermata di scambio (inventario, commerciante, incantatore...) è iscritta alle
     * stesse notifiche: deve reagire solo a quelle dei comandi pubblicati dal proprio automa,
     * altrimenti un solo acquisto produce un fumetto e uno sprite per ciascuna schermata.
     * Il comando porta le stesse parti dell'automa che lo ha pubblicato, quindi basta
     * confrontarle per identità.
     */
    private boolean riguardaQuestaSchermata(ComandoSpostamentoArtefatto<?> comando) {
        return automa != null
                && comando.getParteAttiva() == automa.getParteAttiva()
                && comando.getParteRemota() == automa.getParteRemota();
    }

    private void gestisciEventoApprovazioneAcquistoArtefatto(NotificaApprovazioneAcquistoArtefatto notificaApprovazioneAcquistoArtefatto) {
        if (!riguardaQuestaSchermata(notificaApprovazioneAcquistoArtefatto.getEventoRichiestaSpostamentoArtefatto())) {
            return;
        }
        BusEventi.pubblica(new InternoNotificaViaFumettoATempo("Grazie per il vostro acquisto!", getCoordinateFumetto()));

        int costo = GruppoGiocatore.getIstanza().prezzoAcquisto(
                notificaApprovazioneAcquistoArtefatto.getEventoRichiestaSpostamentoArtefatto().getOggettoDaSpostare().getCostoAcquisto());
        aggiungiSpriteLocale(new SpriteATempo(ImageCache.spriteMoneta, -costo, font,
                xMassimaZonaCentrale, yRigaMonete(), "Monete spese"));
    }

    private void gestisciEventoApprovazioneVenditaArtefatto(NotificaApprovazioneVenditaArtefatto notificaApprovazioneVenditaArtefatto) {
        if (!riguardaQuestaSchermata(notificaApprovazioneVenditaArtefatto.getEventoRichiestaSpostamentoArtefatto())) {
            return;
        }
        BusEventi.pubblica(new InternoNotificaViaFumettoATempo("Grazie di aver fatto affari con noi!", getCoordinateFumetto()));

        int costo = GruppoGiocatore.getIstanza().prezzoVendita(
                notificaApprovazioneVenditaArtefatto.getEventoRichiestaSpostamentoArtefatto().getOggettoDaSpostare().getCostoAcquisto());
        aggiungiSpriteLocale(new SpriteATempo(ImageCache.spriteMoneta, costo, font,
                xMassimaZonaCentrale, yRigaMonete(), "Monete acquisite"));
    }

    private void gestisciEventoRifiutoAcquistoArtefatto(NotificaRifiutoAcquistoArtefatto notificaRifiutoAcquistoArtefatto) {
        if (!riguardaQuestaSchermata(notificaRifiutoAcquistoArtefatto.getEventoRichiestaSpostamento())) {
            return;
        }
        BusEventi.pubblica(new InternoNotificaViaFumettoATempo("Non hai abbastanza denaro per comprare questo oggetto.", getCoordinateFumetto()));
    }

    void impostaAutoma(AutomaScambiatoreArtefatti automa) {
        this.automa = automa;
    }

    protected void disegnaInventario(Graphics2D graphics) {

        super.disegnaInventario(graphics);

        if (automa == null) {
            return;
        }

        disegnaColonnaPersonaggio(graphics);

        offsetYZonaSinistra = disegnaElenco(graphics, new ArrayList<>(automa.getParteAttiva().getInventario()), xMinimaZonaSinistra,
                offsetYZonaSinistra, true);
        offsetYZonaDestra = disegnaElenco(graphics, automa.getParteRemota().getInventario(), xMinimaZonaDestra, offsetYZonaDestra,
                false);

        disegnaIntestazioniInventario(graphics);

        disegnaSpriteLocali(graphics);
    }

    /**
     * Il prezzo da mostrare accanto agli artefatti di una parte, oppure null se lì non si mostra: sulla parte
     * attiva è quanto si ricava vendendo, sulla remota quanto si paga comprando.
     */
    private IntUnaryOperator prezzo(boolean parteAttiva) {
        GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
        if (parteAttiva) {
            return automa.mostraCostoSuParteAttiva() ? gruppo::prezzoVendita : null;
        }
        return automa.mostraCostoSuParteRemota() ? gruppo::prezzoAcquisto : null;
    }

    /**
     * Il colore del livello di un artefatto: di suo grigio, le schermate lo ridefiniscono per dire a colpo
     * d'occhio se l'artefatto si può prendere (inventario) o incantare (incantatore).
     */
    protected DoomdarkColorModel.Color coloreLivello(Artefatto artefatto, boolean parteAttiva) {
        return DoomdarkColorModel.Color.MEDIUM_GRAY;
    }

    /**
     * Accanto al nome: il livello, nel suo colore, e il prezzo in giallo se la parte lo mostra.
     */
    private Image valore(Artefatto artefatto, boolean parteAttiva) {
        Image livello = ImageCache.get("Lv " + artefatto.getLivello(), fontSmall, coloreLivello(artefatto, parteAttiva));
        IntUnaryOperator prezzo = prezzo(parteAttiva);
        if (prezzo == null) {
            return livello;
        }
        Image costo = ImageCache.get(String.valueOf(prezzo.applyAsInt(artefatto.getCostoAcquisto())), fontSmall,
                DoomdarkColorModel.Color.YELLOW);
        int larghezzaLivello = livello.getWidth(null) + ImageCache.SPACING;
        BufferedImage valore = new BufferedImage(larghezzaLivello + costo.getWidth(null),
                Math.max(livello.getHeight(null), costo.getHeight(null)), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = valore.createGraphics();
        g2d.drawImage(livello, 0, 0, null);
        g2d.drawImage(costo, larghezzaLivello, 0, null);
        g2d.dispose();
        return valore;
    }

    private int disegnaElenco(Graphics2D graphics, Collection<Artefatto> artefatti, int x, int offset,
                              boolean parteAttiva) {

        Artefatto evidenziato = trovaArtefatto(artefatti, x, offset, mouseX, mouseY, parteAttiva);
        ComponenteScorrevole<Artefatto> componenteScorrevole = costruisciComponenteScorrevoleArtefatti(artefatti, evidenziato, parteAttiva);

        int nuovoOffset = componenteScorrevole.limitaOffset(ALTEZZA_DISPONIBILE_IN_RIQUADRO_INVENTARIO, offset);
        Image image = componenteScorrevole.produci(ALTEZZA_DISPONIBILE_IN_RIQUADRO_INVENTARIO, nuovoOffset);
        graphics.drawImage(image, x + SPACING, DIMENSIONE_BORDO_INTERNO + 2 * SPACING, null);

        return nuovoOffset;
    }

    /**
     * L'albero viene ricostruito a ogni disegno e a ogni click, sempre con lo stesso valore accanto ai nomi
     * (livello ed eventuale prezzo), così le righe cadono alla stessa quota. L'artefatto passato in
     * evidenziato (se non null) viene disegnato in bianco invece che in grigio chiaro.
     */
    private ComponenteScorrevole<Artefatto> costruisciComponenteScorrevoleArtefatti(Collection<Artefatto> artefatti,
                                                                                    Artefatto evidenziato, boolean parteAttiva) {

        ComponenteScorrevole<Artefatto> componenteScorrevole = new ComponenteScorrevole<>(
                LARGHEZZA_DISPONIBILE_IN_RIQUADRO_INVENTARIO, 10, 2);

        Collection<Artefatto> artefattiDaDisegnare = ordinaArtefattiDaDisegnare(artefatti);

        SupertipoArtefatto supertipoPrecedente = null;

        for (Artefatto artefatto : artefattiDaDisegnare) {

            if (supertipoPrecedente != artefatto.getTipo().getSupertipo()) {
                supertipoPrecedente = artefatto.getTipo().getSupertipo();
                Image immagineSupertipo = getImmagineSupertipo(supertipoPrecedente);
                if (immagineSupertipo != null) {
                    componenteScorrevole.creaNodo(
                            null, null, null,
                            null, null, null,
                            null, null, null,
                            immagineSupertipo, null);
                }
            }

            DoomdarkColorModel.Color colore = artefatto == evidenziato
                    ? DoomdarkColorModel.Color.WHITE
                    : DoomdarkColorModel.Color.LIGHT_GRAY;
            DoomdarkColorModel.Color coloreAttributi = artefatto == evidenziato
                    ? DoomdarkColorModel.Color.LIGHT_GRAY
                    : DoomdarkColorModel.Color.MEDIUM_GRAY;
            DoomdarkColorModel.Color coloreSeparatori = artefatto == evidenziato
                    ? DoomdarkColorModel.Color.MEDIUM_GRAY
                    : DoomdarkColorModel.Color.DARK_GRAY;

            // In grande il nome proprio (o il nome), in piccolo la descrizione
            // TODO da riguardare con la resa grafica
            String nome = artefatto.getModelloDati().getNomeBreve();
            nome = nome.substring(0, 1).toUpperCase() + nome.substring(1);
            ComponenteScorrevole<Artefatto>.Nodo nodo = componenteScorrevole.creaNodo(
                    nome, font, colore,
                    valore(artefatto, parteAttiva),
                    artefatto.getModelloDati().getDescrizioneBreve(), fontSmall, colore,
                    null, artefatto);
            nodo.setFigliVisibili(artefatto.isFigliVisibili());
            if (!artefatto.getModificatori().isEmpty()) {
                nodo.creaNodo(
                        "Modificatori:", font, coloreSeparatori,
                        null, null, null,
                        null, null, null,
                        null, artefatto);
                for (ModificatoreAttributo modificatore : artefatto.getModificatori()) {
                    String valore;
                    switch (modificatore.getTipoModificatoreAttributo()) {
                        case AUMENTO_FISSO:
                            valore = (modificatore.getQuantita() < 0 ? "-" : "+") + (int) modificatore.getQuantita();
                            break;
                        case AUMENTO_PERCENTUALE:
                            valore = (modificatore.getQuantita() < 0 ? "-" : "+") + (int) modificatore.getQuantita() + "%";
                            break;
                        case QUANTITA_ASSOLUTA:
                            valore = "Porta a " + (int) modificatore.getQuantita();
                            break;
                        default:
                            valore = "";
                            break;
                    }
                    nodo.creaNodo(
                            modificatore.getTipoAttributo().getNome(), font, coloreAttributi,
                            valore, font, coloreAttributi,
                            null, null, null,
                            null, artefatto);

                }
            }
            if (!artefatto.getIncantamenti().isEmpty()) {
                nodo.creaNodo(
                        "Incantamenti:", font, coloreSeparatori,
                        null, null, null,
                        null, null, null,
                        null, artefatto);
                for (Incantamento incantamento : artefatto.getIncantamenti()) {
                    nodo.creaNodo(
                            incantamento.getNomeIncantamento(), font, coloreAttributi,
                            null, null, null,
                            null, artefatto);
                    int bonusFisso = incantamento.getDannoBonusFisso();
                    nodo.creaNodo(
                            incantamento.getTipoDannoElementale().getNome(), font, coloreAttributi,
                            (bonusFisso < 0 ? "-" : "+") + bonusFisso +
                                    " + " + (int) (incantamento.getCoefficienteScala() * 100) + "%", font, coloreAttributi,
                            null, null, null,
                            null, artefatto);
                }
            }
        }

        return componenteScorrevole;
    }

    private static Collection<Artefatto> ordinaArtefattiDaDisegnare(Collection<Artefatto> artefatti) {
        java.util.List<Artefatto> artefattiDaDisegnare = new ArrayList<>(artefatti);
        artefattiDaDisegnare.sort((a1, a2) -> {
            int ordinaleSupertipo1 = a1.getTipo().getSupertipo().ordinal();
            int ordinaleSupertipo2 = a2.getTipo().getSupertipo().ordinal();
            if (ordinaleSupertipo1 == ordinaleSupertipo2) {
                int ordinaleTipo1 = a1.getTipo().ordinal();
                int ordinaleTipo2 = a2.getTipo().ordinal();
                if (ordinaleTipo1 == ordinaleTipo2) {
                    return a1.getNome().compareTo(a2.getNome());
                }
                return Integer.compare(ordinaleTipo1, ordinaleTipo2);
            }
            return Integer.compare(ordinaleSupertipo1, ordinaleSupertipo2);
        });
        return artefattiDaDisegnare;
    }

    /**
     * Riporta l'artefatto disegnato alla posizione (x, y) espressa in coordinate della
     * finestra, oppure null se il punto non cade sull'elenco o non corrisponde al titolo
     * di un artefatto (es. una riga di modificatore/incantamento, o spazio vuoto).
     */
    private Artefatto trovaArtefatto(Collection<Artefatto> artefatti, int boxX, int offset, int x, int y, boolean parteAttiva) {
        int xInterno = x - (boxX + SPACING);
        int yInterno = y - (DIMENSIONE_BORDO_INTERNO + 2 * SPACING);
        if (xInterno < 0 || xInterno >= LARGHEZZA_DISPONIBILE_IN_RIQUADRO_INVENTARIO
                || yInterno < 0 || yInterno >= ALTEZZA_DISPONIBILE_IN_RIQUADRO_INVENTARIO) {
            return null;
        }
        return costruisciComponenteScorrevoleArtefatti(artefatti, null, parteAttiva).riferimentoTitoloAllaQuota(yInterno + offset);
    }

    protected abstract boolean processaClickPersonaggio(int x, int y, Tasto tasto);

    @Override
    public void processaClick(int x, int y, Tasto tasto) {
        if (tasto != Tasto.SINISTRO || automa == null) {
            return;
        }
        if (processaClickPersonaggio(x, y, tasto)) {
            return;
        }
        java.util.List<Artefatto> inventarioPersonaggio = new ArrayList<>(automa.getParteAttiva().getInventario());
        Artefatto artefatto = trovaArtefatto(inventarioPersonaggio, xMinimaZonaSinistra, offsetYZonaSinistra, x, y, true);
        if (artefatto == null) {
            artefatto = trovaArtefatto(automa.getArtefattiDisponibili(), xMinimaZonaDestra, offsetYZonaDestra, x, y, false);
        }
        if (artefatto == null) {
            return;
        }
        if (artefatto.isFigliVisibili()) {
            artefatto.nascondiFigli();
        } else {
            artefatto.mostraFigli();
        }
    }

    protected abstract boolean processaDoppioClickPersonaggio(int x, int y, Tasto tasto);

    @Override
    public void processaDoppioClick(int x, int y, Tasto tasto) {
        if (automa == null) {
            return;
        }
        if (processaDoppioClickPersonaggio(x, y, tasto)) {
            return;
        }
        List<Artefatto> inventarioPersonaggio = new ArrayList<>(automa.getParteAttiva().getInventario());
        Artefatto artefatto = trovaArtefatto(inventarioPersonaggio, xMinimaZonaSinistra, offsetYZonaSinistra, x, y, true);
        if (artefatto != null) {
            automa.richiediSpostamentoSuParteRemota(artefatto);
            return;
        }
        Collection<Artefatto> disponibili = automa.getArtefattiDisponibili();
        artefatto = trovaArtefatto(disponibili, xMinimaZonaDestra, offsetYZonaDestra, x, y, false);
        if (artefatto != null) {
            automa.richiediSpostamentoSuParteAttiva(artefatto);
        }
    }

    /**
     * L'immagine che separa nell'elenco un supertipo dal precedente, oppure null se il
     * supertipo non ne ha una (es. ELMO, INCANTAMENTO): in quel caso il separatore si omette.
     */
    protected Image getImmagineSupertipo(SupertipoArtefatto supertipo) {
        switch (supertipo) {
            case ARMA:
                return ImageCache.separatoreArmi;
            case ELMO:
                return ImageCache.separatoreElmi;
            case ARMATURA:
                return ImageCache.separatoreArmature;
            case SCUDO:
                return ImageCache.separatoreScudi;
            case INCANTAMENTO:
            case POTENZIAMENTO_POTERE_MAGICO:
                return ImageCache.separatoreIncantamenti;
            case ALTRO:
                return ImageCache.separatoreNinnoli;
            default:
                throw new IllegalArgumentException("SupertipoArtefatto senza immagine associata");
        }
    }
}
