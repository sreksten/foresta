package com.threeamigos.foresta.ui.schermate;

import com.threeamigos.foresta.artefatti.IncantamentoMD;
import com.threeamigos.foresta.artefatti.VistaArtefatto;
import com.threeamigos.foresta.artefatti.VistaPezzoDelSet;
import com.threeamigos.foresta.artefatti.tipi.CategoriaArtefatto;
import com.threeamigos.foresta.artefatti.tipi.StatoPezzoDelSet;
import com.threeamigos.foresta.artefatti.tipi.TipoArtefatto;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoCommutazioneElenco;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoScambioArtefatto;
import com.threeamigos.foresta.eventi.interni.InternoNotificaViaFumettoATempo;
import com.threeamigos.foresta.eventi.notifiche.NotificaApprovazioneAcquistoArtefatto;
import com.threeamigos.foresta.eventi.notifiche.NotificaApprovazioneVenditaArtefatto;
import com.threeamigos.foresta.eventi.notifiche.NotificaRifiutoAcquistoArtefatto;
import com.threeamigos.foresta.interfacce.VistaGruppoGiocatore;
import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.interfacce.VistaScambio;
import com.threeamigos.foresta.modificatori.ModificatoreAttributoMD;
import com.threeamigos.foresta.modificatori.tipi.BersaglioModificatore;
import com.threeamigos.foresta.strumenti.TestoDeiModificatori;
import com.threeamigos.foresta.testi.Articoli;
import com.threeamigos.foresta.ui.barra.Cartiglio;
import com.threeamigos.foresta.ui.componenti.ComponenteScorrevole;
import com.threeamigos.foresta.ui.immagini.BufferedImageBuilder;
import com.threeamigos.foresta.ui.immagini.ImageCache;
import com.threeamigos.foresta.ui.sprite.SpriteATempo;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.IntUnaryOperator;

/**
 * Base delle schermate che scambiano artefatti fra due liste affiancate (inventario, commercianti, incantatore): disegna le due liste e traduce click e
 * doppi click in comandi di scambio.
 *
 * @author Stefano Reksten
 */
public abstract class DisplayableCanvasScambiatoreArtefatti extends DisplayableCanvasScambiatore {


    // Lo scambio aperto, in sola lettura: gli spostamenti si chiedono con ComandoScambioArtefatto
    protected VistaScambio scambio;

    // Quota (in coordinate della finestra) a cui inizia l'elenco delle caratteristiche del
    // personaggio, aggiornata a ogni disegnaInventario e usata per l'hit-test dei click.
    protected int yAttributi = 0;

    DisplayableCanvasScambiatoreArtefatti(int width, int height, VistaPartita vistaPartita) {
        super(width, height, vistaPartita);
        BusEventi.iscriviti(NotificaApprovazioneAcquistoArtefatto.class, this::gestisciEventoApprovazioneAcquistoArtefatto);
        BusEventi.iscriviti(NotificaRifiutoAcquistoArtefatto.class, this::gestisciEventoRifiutoAcquistoArtefatto);
        BusEventi.iscriviti(NotificaApprovazioneVenditaArtefatto.class, this::gestisciEventoApprovazioneVenditaArtefatto);
    }

    /**
     * Ogni schermata di scambio (inventario, commerciante, incantatore...) è iscritta alle
     * stesse notifiche: deve reagire solo a quelle dei comandi del proprio scambio,
     * altrimenti un solo acquisto produce un fumetto e uno sprite per ciascuna schermata.
     * La notifica porta lo scambio da cui veniva la richiesta, quindi basta confrontarlo per identità.
     */
    private boolean riguardaQuestaSchermata(VistaScambio scambioDellaNotifica) {
        return scambio != null && scambioDellaNotifica == scambio;
    }

    private void gestisciEventoApprovazioneAcquistoArtefatto(NotificaApprovazioneAcquistoArtefatto notificaApprovazioneAcquistoArtefatto) {
        if (!riguardaQuestaSchermata(notificaApprovazioneAcquistoArtefatto.getScambio())) {
            return;
        }
        BusEventi.pubblica(new InternoNotificaViaFumettoATempo("Grazie per il vostro acquisto!", getCoordinateFumetto()));

        int costo = vistaPartita.getGruppoGiocatore().prezzoAcquisto(
                notificaApprovazioneAcquistoArtefatto.getOggettoSpostato().getCostoAcquisto());
        aggiungiSpriteLocale(new SpriteATempo(ImageCache.spriteMoneta, -costo, font,
                xMassimaZonaCentrale, yRigaMonete(), "Monete spese"));
    }

    private void gestisciEventoApprovazioneVenditaArtefatto(NotificaApprovazioneVenditaArtefatto notificaApprovazioneVenditaArtefatto) {
        if (!riguardaQuestaSchermata(notificaApprovazioneVenditaArtefatto.getScambio())) {
            return;
        }
        BusEventi.pubblica(new InternoNotificaViaFumettoATempo(fraseDopoLaVendita(), getCoordinateFumetto()));

        int costo = vistaPartita.getGruppoGiocatore().prezzoVendita(
                notificaApprovazioneVenditaArtefatto.getOggettoSpostato().getCostoAcquisto());
        aggiungiSpriteLocale(new SpriteATempo(ImageCache.spriteMoneta, costo, font,
                xMassimaZonaCentrale, yRigaMonete(), "Monete acquisite"));
    }

    private void gestisciEventoRifiutoAcquistoArtefatto(NotificaRifiutoAcquistoArtefatto notificaRifiutoAcquistoArtefatto) {
        if (!riguardaQuestaSchermata(notificaRifiutoAcquistoArtefatto.getScambio())) {
            return;
        }
        BusEventi.pubblica(new InternoNotificaViaFumettoATempo("Non hai abbastanza denaro per comprare questo oggetto.", getCoordinateFumetto()));
    }

    /**
     * Che cosa dice chi ha appena comprato qualcosa dal gruppo.
     */
    String fraseDopoLaVendita() {
        return "Grazie di aver fatto affari con noi!";
    }

    /**
     * Imposta lo scambio fra le due liste di artefatti.
     */
    public void impostaScambio(VistaScambio scambio) {
        this.scambio = scambio;
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

        offsetYZonaSinistra = disegnaElenco(graphics, new ArrayList<>(scambio.getInventarioParteAttiva()), xMinimaZonaSinistra,
                offsetYZonaSinistra, true);
        offsetYZonaDestra = disegnaElenco(graphics, scambio.getInventarioParteRemota(), xMinimaZonaDestra, offsetYZonaDestra,
                false);

        disegnaIntestazioniInventario(graphics);

        disegnaSpriteLocali(graphics);

        disegnaAiuto(graphics, righeAiuto());
    }

    /**
     * Cosa fa il doppio click su un artefatto della parte sinistra (parte attiva) e della destra: le sottoclassi
     * lo dicono con le parole della loro schermata, per l'aiuto (vedi righeAiuto)
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
     * L'aiuto per l'elenco sotto il mouse: sul nome di un artefatto cosa fa il click (aprire o chiudere l'elenco dei
     * modificatori) e cosa fa il doppio click da quella parte; ovunque nell'elenco, se si scorre, la rotella
     */
    private List<String> righeAiuto() {
        List<String> righe = new ArrayList<>();
        Collection<? extends VistaArtefatto> elenco;
        boolean parteAttiva;
        int boxX;
        int offset;
        String doppioClick;
        if (dentroElenco(xMinimaZonaSinistra, mouseX, mouseY)) {
            elenco = new ArrayList<>(scambio.getInventarioParteAttiva());
            parteAttiva = true;
            boxX = xMinimaZonaSinistra;
            offset = offsetYZonaSinistra;
            doppioClick = aiutoDoppioClickSinistra();
        } else if (dentroElenco(xMinimaZonaDestra, mouseX, mouseY)) {
            elenco = scambio.getInventarioParteRemota();
            parteAttiva = false;
            boxX = xMinimaZonaDestra;
            offset = offsetYZonaDestra;
            doppioClick = aiutoDoppioClickDestra();
        } else {
            return righeAiutoColonnaPersonaggio();
        }
        VistaArtefatto artefatto = trovaArtefatto(elenco, boxX, offset, mouseX, mouseY, parteAttiva);
        if (artefatto != null) {
            righe.add(Cartiglio.aiutoClick(artefatto.isFigliVisibili(), "elenco modificatori"));
            righe.add(doppioClick);
        }
        if (costruisciComponenteScorrevoleArtefatti(elenco, null, parteAttiva).isScorrevole(ALTEZZA_DISPONIBILE_IN_RIQUADRO_INVENTARIO)) {
            righe.add(Cartiglio.AIUTO_ROTELLA);
        }
        return righe;
    }

    /**
     * Il prezzo da mostrare accanto agli artefatti di una parte, oppure null se lì non si mostra: sulla parte
     * attiva è quanto si ricava vendendo, sulla remota quanto si paga comprando.
     */
    private IntUnaryOperator prezzo(boolean parteAttiva) {
        VistaGruppoGiocatore gruppo = vistaPartita.getGruppoGiocatore();
        if (parteAttiva) {
            return scambio.mostraCostoSuParteAttiva() ? gruppo::prezzoVendita : null;
        }
        return scambio.mostraCostoSuParteRemota() ? gruppo::prezzoAcquisto : null;
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
     * Accanto al nome: il livello, nel suo colore, e il prezzo in giallo se la parte lo mostra.
     */
    private Image valore(VistaArtefatto artefatto, boolean parteAttiva) {
        Image livello = ImageCache.get("Lv " + artefatto.getLivello(), fontSmall, coloreLivello(artefatto, parteAttiva));
        IntUnaryOperator prezzo = prezzo(parteAttiva);
        if (prezzo == null) {
            return livello;
        }
        Image costo = ImageCache.get(String.valueOf(prezzo.applyAsInt(artefatto.getCostoAcquisto())), fontSmall,
                DoomdarkColorModel.Color.YELLOW);
        // Livello e costo vengono da ImageCache.get, quindi sempre le stesse immagini: la coppia identifica il valore composto
        return ImageCache.getComposta(Arrays.asList("valoreArtefatto", livello, costo), () -> {
            int larghezzaLivello = livello.getWidth(null) + ImageCache.SPACING;
            BufferedImage valore = BufferedImageBuilder.creaCompatibile(larghezzaLivello + costo.getWidth(null),
                    Math.max(livello.getHeight(null), costo.getHeight(null)), Transparency.TRANSLUCENT);
            Graphics2D g2d = valore.createGraphics();
            g2d.drawImage(livello, 0, 0, null);
            g2d.drawImage(costo, larghezzaLivello, 0, null);
            g2d.dispose();
            return valore;
        });
    }

    private int disegnaElenco(Graphics2D graphics, Collection<? extends VistaArtefatto> artefatti, int x, int offset,
                              boolean parteAttiva) {

        VistaArtefatto evidenziato = trovaArtefatto(artefatti, x, offset, mouseX, mouseY, parteAttiva);
        ComponenteScorrevole<VistaArtefatto> componenteScorrevole = costruisciComponenteScorrevoleArtefatti(artefatti, evidenziato, parteAttiva);

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
    private ComponenteScorrevole<VistaArtefatto> costruisciComponenteScorrevoleArtefatti(Collection<? extends VistaArtefatto> artefatti,
                                                                                    VistaArtefatto evidenziato, boolean parteAttiva) {

        ComponenteScorrevole<VistaArtefatto> componenteScorrevole = new ComponenteScorrevole<>(
                LARGHEZZA_DISPONIBILE_IN_RIQUADRO_INVENTARIO, 10, 2);

        Collection<VistaArtefatto> artefattiDaDisegnare = ordinaArtefattiDaDisegnare(artefatti);

        Image separatorePrecedente = null;

        for (VistaArtefatto artefatto : artefattiDaDisegnare) {

            Image immagineSeparatore = getImmagineSeparatore(artefatto.getTipo());
            if (immagineSeparatore != separatorePrecedente) {
                separatorePrecedente = immagineSeparatore;
                if (immagineSeparatore != null) {
                    componenteScorrevole.creaNodo(
                            null, null, null,
                            null, null, null,
                            null, null, null,
                            immagineSeparatore, null);
                }
            }

            DoomdarkColorModel.Color colore = artefatto == evidenziato
                    ? DoomdarkColorModel.Color.WHITE
                    : DoomdarkColorModel.Color.LIGHT_GRAY;
            DoomdarkColorModel.Color coloreBase = artefatto == evidenziato
                    ? DoomdarkColorModel.Color.LIGHT_GRAY
                    : DoomdarkColorModel.Color.MEDIUM_GRAY;
            DoomdarkColorModel.Color coloreSeparatori = artefatto == evidenziato
                    ? DoomdarkColorModel.Color.MEDIUM_GRAY
                    : DoomdarkColorModel.Color.DARK_GRAY;

            // In grande il nome proprio (o il nome), in piccolo la descrizione
            // TODO da riguardare con la resa grafica
            String nome = artefatto.getNomeBreve();
            nome = Articoli.inizialeMaiuscola(nome);
            ComponenteScorrevole<VistaArtefatto>.Nodo nodo = componenteScorrevole.creaNodo(
                    nome, font, colore,
                    valore(artefatto, parteAttiva),
                    artefatto.getDescrizioneBreve(), fontSmall, colore,
                    null, artefatto);
            nodo.setFigliVisibili(artefatto.isFigliVisibili());
            artefatto.getDescrizioneSet().ifPresent(set -> {
                nodo.creaNodo(
                        set, font, coloreSeparatori,
                        null, null, null,
                        null, null, null,
                        null, artefatto);
                // I pezzi: verdi quelli indossati da chi indossa questo, gialli quelli del gruppo, grigi da trovare
                // (di questi solo il tipo)
                for (VistaPezzoDelSet pezzo : vistaPartita.getGruppoGiocatore().getPezziDelSet(artefatto)) {
                    boolean noto = pezzo.getStato() != StatoPezzoDelSet.DA_TROVARE
                            || pezzo.getChiave().equals(artefatto.getPezzoLeggendario());
                    DoomdarkColorModel.Color colorePezzo = colorePezzo(pezzo.getStato());
                    nodo.creaNodo(
                            pezzo.getTipo().getDescrizione(), font, colorePezzo,
                            noto ? Articoli.inizialeMaiuscola(pezzo.getNome()) : "???",
                            fontSmall, colorePezzo,
                            null, null, null,
                            null, artefatto);
                }
            });
            if (!artefatto.getModificatori().isEmpty()) {
                nodo.creaNodo(
                        "Modificatori:", font, coloreSeparatori,
                        null, null, null,
                        null, null, null,
                        null, artefatto);
                for (ModificatoreAttributoMD modificatore : artefatto.getModificatori()) {
                    DoomdarkColorModel.Color coloreAttributi = coloreEffetto(artefatto, modificatore, parteAttiva, coloreBase);
                    String valore;
                    int valoreIntero = (int) modificatore.getQuantita();
                    switch (modificatore.getTipoModificatoreAttributo()) {
                        case VARIAZIONE_FISSA:
                            valore = segno(valoreIntero) + valoreIntero;
                            break;
                        case VARIAZIONE_PERCENTUALE:
                            valore = segno(valoreIntero) + valoreIntero + "%";
                            break;
                        case QUANTITA_ASSOLUTA:
                            valore = "Porta a " + valoreIntero;
                            break;
                        default:
                            valore = "";
                            break;
                    }
                    // Il valore resta corto; chi lo colpisce e per quanto va sotto, nella descrizione
                    StringBuilder dettaglio = new StringBuilder();
                    if (modificatore.getBersaglio() == BersaglioModificatore.BERSAGLIO) {
                        dettaglio.append("sul bersaglio");
                    }
                    if (modificatore.isTemporaneo()) {
                        dettaglio.append(dettaglio.length() > 0 ? " " : "").append(TestoDeiModificatori.durata(modificatore));
                    }
                    nodo.creaNodo(
                            modificatore.getTipoAttributo().getNome(), font, coloreAttributi,
                            valore, font, coloreAttributi,
                            dettaglio.length() > 0 ? dettaglio.toString() : null, fontSmall, coloreAttributi,
                            null, artefatto);

                }
            }
            if (!artefatto.getIncantamenti().isEmpty()) {
                nodo.creaNodo(
                        "Incantamenti:", font, coloreSeparatori,
                        null, null, null,
                        null, null, null,
                        null, artefatto);
                for (IncantamentoMD incantamento : artefatto.getIncantamenti()) {
                    DoomdarkColorModel.Color coloreAttributi = coloreEffetto(artefatto, incantamento, parteAttiva, coloreBase);
                    nodo.creaNodo(
                            incantamento.getNomeIncantamento(), font, coloreAttributi,
                            null, null, null,
                            null, artefatto);
                    int bonusFisso = incantamento.getDannoBonusFisso();
                    nodo.creaNodo(
                            incantamento.getTipoDannoElementale().getNome(), font, coloreAttributi,
                            segno(bonusFisso) + bonusFisso +
                                    " + " + (int) (incantamento.getCoefficienteScala() * 100) + "%", font, coloreAttributi,
                            null, null, null,
                            null, artefatto);
                }
            }
        }

        return componenteScorrevole;
    }

    private static String segno(int valore) {
        return valore < 0 ? "" : "+";
    }

    /**
     * Per supertipo e tipo, e dentro lo stesso tipo dal livello più alto al più basso: i migliori in cima.
     */
    public static Collection<VistaArtefatto> ordinaArtefattiDaDisegnare(Collection<? extends VistaArtefatto> artefatti) {
        java.util.List<VistaArtefatto> artefattiDaDisegnare = new ArrayList<>(artefatti);
        artefattiDaDisegnare.sort((a1, a2) -> {
            int ordinaleSupertipo1 = a1.getTipo().getCategoria().ordinal();
            int ordinaleSupertipo2 = a2.getTipo().getCategoria().ordinal();
            if (ordinaleSupertipo1 == ordinaleSupertipo2) {
                int ordinaleTipo1 = a1.getTipo().ordinal();
                int ordinaleTipo2 = a2.getTipo().ordinal();
                if (ordinaleTipo1 == ordinaleTipo2) {
                    if (a1.getLivello() != a2.getLivello()) {
                        return Integer.compare(a2.getLivello(), a1.getLivello());
                    }
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
    private VistaArtefatto trovaArtefatto(Collection<? extends VistaArtefatto> artefatti, int boxX, int offset, int x, int y, boolean parteAttiva) {
        int xInterno = x - (boxX + SPACING);
        int yInterno = y - (DIMENSIONE_BORDO_INTERNO + 2 * SPACING);
        if (xInterno < 0 || xInterno >= LARGHEZZA_DISPONIBILE_IN_RIQUADRO_INVENTARIO
                || yInterno < 0 || yInterno >= ALTEZZA_DISPONIBILE_IN_RIQUADRO_INVENTARIO) {
            return null;
        }
        return costruisciComponenteScorrevoleArtefatti(artefatti, null, parteAttiva).riferimentoTitoloAllaQuota(yInterno + offset);
    }

    /**
     * Gestisce un click nella colonna centrale; dice se l'ha consumato.
     */
    protected abstract boolean processaClickPersonaggio(int x, int y, Tasto tasto);

    @Override
    public void processaClick(int x, int y, Tasto tasto) {
        if (tasto != Tasto.SINISTRO || scambio == null) {
            return;
        }
        if (processaClickPersonaggio(x, y, tasto)) {
            return;
        }
        java.util.List<VistaArtefatto> inventarioPersonaggio = new ArrayList<>(scambio.getInventarioParteAttiva());
        VistaArtefatto artefatto = trovaArtefatto(inventarioPersonaggio, xMinimaZonaSinistra, offsetYZonaSinistra, x, y, true);
        if (artefatto == null) {
            artefatto = trovaArtefatto(scambio.getInventarioParteRemota(), xMinimaZonaDestra, offsetYZonaDestra, x, y, false);
        }
        if (artefatto == null) {
            return;
        }
        BusEventi.pubblica(ComandoCommutazioneElenco.artefatto(scambio.getId(), artefatto.getUuid()));
    }

    /**
     * Gestisce un doppio click nella colonna centrale; dice se l'ha consumato.
     */
    protected abstract boolean processaDoppioClickPersonaggio(int x, int y, Tasto tasto);

    @Override
    public void processaDoppioClick(int x, int y, Tasto tasto) {
        // Come il click singolo, solo con il tasto sinistro: un doppio click destro o centrale non compra, non vende e non spende punti
        if (tasto != Tasto.SINISTRO || scambio == null) {
            return;
        }
        if (processaDoppioClickPersonaggio(x, y, tasto)) {
            return;
        }
        List<VistaArtefatto> inventarioPersonaggio = new ArrayList<>(scambio.getInventarioParteAttiva());
        VistaArtefatto artefatto = trovaArtefatto(inventarioPersonaggio, xMinimaZonaSinistra, offsetYZonaSinistra, x, y, true);
        if (artefatto != null) {
            BusEventi.pubblica(new ComandoScambioArtefatto(scambio.getId(), ComandoScambioArtefatto.Destinazione.PARTE_REMOTA, artefatto.getUuid()));
            return;
        }
        Collection<? extends VistaArtefatto> disponibili = scambio.getInventarioParteRemota();
        artefatto = trovaArtefatto(disponibili, xMinimaZonaDestra, offsetYZonaDestra, x, y, false);
        if (artefatto != null) {
            BusEventi.pubblica(new ComandoScambioArtefatto(scambio.getId(), ComandoScambioArtefatto.Destinazione.PARTE_ATTIVA, artefatto.getUuid()));
        }
    }

    private static DoomdarkColorModel.Color colorePezzo(StatoPezzoDelSet stato) {
        switch (stato) {
            case INDOSSATO:
                return DoomdarkColorModel.Color.GREEN;
            case DEL_GRUPPO:
                return DoomdarkColorModel.Color.YELLOW;
            default:
                return DoomdarkColorModel.Color.MEDIUM_GRAY;
        }
    }

    /**
     * L'immagine che separa nell'elenco un gruppo di artefatti dal precedente: quella del supertipo, tranne per le
     * maschere, che sono armature ma hanno il loro separatore (e stanno in fondo alle armature, vedi
     * ordinaArtefattiDaDisegnare).
     */
    private static Image getImmagineSeparatore(TipoArtefatto tipo) {
        return tipo == TipoArtefatto.MASCHERA ? ImageCache.separatoreMaschere : getImmagineSupertipo(tipo.getCategoria());
    }

    /**
     * L'immagine che separa nell'elenco un supertipo dal precedente.
     */
    private static Image getImmagineSupertipo(CategoriaArtefatto supertipo) {
        switch (supertipo) {
            case ARMA:
                return ImageCache.separatoreArmi;
            case ELMO:
                return ImageCache.separatoreElmi;
            case ARMATURA:
                return ImageCache.separatoreArmature;
            case SCHINIERI:
                return ImageCache.separatoreSchinieri;
            case SCUDO:
                return ImageCache.separatoreScudi;
            case INCANTAMENTO:
                return ImageCache.separatoreIncantamenti;
            case POTENZIAMENTO_POTERE_MAGICO:
                return ImageCache.separatoreLibriMagici;
            case ALTRO:
                return ImageCache.separatoreNinnoli;
            default:
                throw new IllegalArgumentException("SupertipoArtefatto senza immagine associata");
        }
    }
}
