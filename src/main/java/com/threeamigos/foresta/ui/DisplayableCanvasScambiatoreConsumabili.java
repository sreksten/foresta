package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoAcquistoConsumabile;
import com.threeamigos.foresta.eventi.interni.InternoNotificaViaFumettoATempo;
import com.threeamigos.foresta.eventi.notifiche.NotificaApprovazioneAcquistoConsumabile;
import com.threeamigos.foresta.eventi.notifiche.NotificaRifiutoAcquistoConsumabile;
import com.threeamigos.foresta.incantesimi.ClasseIncantesimo;
import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.modellodati.TipoConsumabile;
import com.threeamigos.foresta.personaggi.Personaggio;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

/**
 *
 * @author Stefano Reksten
 */
public class DisplayableCanvasScambiatoreConsumabili extends DisplayableCanvasScambiatore {

    public DisplayableCanvasScambiatoreConsumabili(int width, int height) {
        super(width, height);
        BusEventi.iscriviti(NotificaApprovazioneAcquistoConsumabile.class, this::gestisciEventoApprovazioneAcquistoConsumabile);
        BusEventi.iscriviti(NotificaRifiutoAcquistoConsumabile.class, this::gestisciEventoRifiutoAcquistoConsumabile);
    }

    private void gestisciEventoApprovazioneAcquistoConsumabile(NotificaApprovazioneAcquistoConsumabile notificaApprovazioneAcquistoConsumabile) {
        BusEventi.pubblica(new InternoNotificaViaFumettoATempo("Grazie per l'acquisto!", getCoordinateFumetto()));

        ComandoAcquistoConsumabile comando = notificaApprovazioneAcquistoConsumabile.getEventoRichiestaAcquistoConsumabile();

        aggiungiSpriteLocale(new SpriteATempo(ImageCache.spriteMoneta, -comando.getPrezzo(), font,
                xMassimaZonaCentrale, yRigaMonete(), "Monete spese"));

        if (comando.getTipoConsumabile() == TipoConsumabile.INCANTESIMO) {
            ClasseIncantesimo classe = comando.getClasseIncantesimo();
            String descrizione = "Incantesimo acquistato: " + classe.getNomeSingolare();
            Point posizione = posizioneIncantesimoNellElencoSinistro(classe);
            if (posizione != null) {
                aggiungiSpriteLocale(new SpriteATempo(ImageCache.spriteIncantesimi[classe.ordinal()], 1, font,
                        posizione.x, posizione.y, true, descrizione));
            } else {
                aggiungiSpriteLocale(new SpriteATempo(ImageCache.spriteIncantesimi[classe.ordinal()], 1, font,
                        xMinimaZonaCentrale, yRigaMonete(), true, descrizione));
            }
        }
    }

    /**
     * Cerca la posizione a schermo, nell'elenco di sinistra (inventario del gruppo), dell'icona
     * relativa all'incantesimo indicato, se attualmente visibile nella porzione scrollata a video.
     */
    private Point posizioneIncantesimoNellElencoSinistro(ClasseIncantesimo classeIncantesimo) {
        List<Consumabile> consumabili = getElencoGruppo();
        ComponenteScorrevole<Consumabile> componenteScorrevole = costruisciComponenteScorrevoleConsumabili(consumabili, null, false);
        Point posizione = componenteScorrevole.posizioneTitolo(c -> c.classeIncantesimo == classeIncantesimo);
        if (posizione == null) {
            return null;
        }
        int relativo = posizione.y - offsetYZonaSinistra;
        if (relativo < 0 || relativo >= ALTEZZA_DISPONIBILE_IN_RIQUADRO_INVENTARIO) {
            return null;
        }
        return new Point(xMinimaZonaSinistra + SPACING + posizione.x, DIMENSIONE_BORDO_INTERNO + 2 * SPACING + relativo);
    }

    private void gestisciEventoRifiutoAcquistoConsumabile(NotificaRifiutoAcquistoConsumabile notificaRifiutoAcquistoConsumabile) {
        BusEventi.pubblica(new InternoNotificaViaFumettoATempo("Non hai abbastanza monete per comprare questo oggetto.", getCoordinateFumetto()));
    }

    @Override
    void disegnaIntestazioniInventario(Graphics2D graphics) {
        disegnaIntestazioniInventarioImpl(graphics, "Inventario gruppo", "Inventario alchimista");
    }

    void disegnaColonnaPersonaggio(Graphics2D graphics) {

        Image doomdark;
        int y = SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI;
        DoomdarkColorModel.Color coloreTestata = DoomdarkColorModel.Color.LIGHT_GRAY;

        // Nome personaggio
        doomdark = ImageCache.get("Alchimista", coloreTestata);
        graphics.drawImage(doomdark, (width - doomdark.getWidth(null)) / 2, y, null);
        y += fontHeight + SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI;

        // Immagine personaggio
        BufferedImage immaginePersonaggio = ImageCache.alchimista;

        // Per tenere i personaggi sullo stesso livello (se si passa da un personaggio all'altro)
        // ed evitare sfarfallamenti, scegliamo il ladro come personaggio "base" per calcolare l'altezza a cui disegnare.
        y += ALTEZZA_LADRO;
        graphics.drawImage(immaginePersonaggio, (width - immaginePersonaggio.getWidth()) / 2, y - immaginePersonaggio.getHeight(), null);
        y += SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI;

        Image i = ImageCache.get("Monete", coloreTestata);
        graphics.drawImage(i, xMinimaZonaCentrale, y, null);
        i = DoomdarkTextProducer.getImage(GruppoGiocatore.getIstanza().getMonete(), font, coloreTestata);
        graphics.drawImage(i, xMassimaZonaCentrale - i.getWidth(null), y, null);

        y += fontHeight + SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI;

        BufferedImage separatore = ImageCache.separatore;
        graphics.drawImage(separatore, (width - separatore.getWidth()) / 2, y, null);
    }

    protected void disegnaInventario(Graphics2D graphics) {

        super.disegnaInventario(graphics);

        disegnaColonnaPersonaggio(graphics);

        // Inventario personaggio
        offsetYZonaSinistra = disegnaElencoSinistro(graphics, xMinimaZonaSinistra, offsetYZonaSinistra);
        // Inventario gruppo
        offsetYZonaDestra = disegnaElencoDestro(graphics, xMinimaZonaDestra, offsetYZonaDestra);

        disegnaIntestazioniInventario(graphics);

        disegnaSpriteLocali(graphics);
    }

    private int disegnaElencoSinistro(Graphics2D graphics, int x, int offset) {

        List<Consumabile> consumabili = getElencoGruppo();
        Consumabile evidenziato = trovaConsumabile(consumabili, x, offset, mouseX, mouseY, false);
        ComponenteScorrevole<Consumabile> componenteScorrevole = costruisciComponenteScorrevoleConsumabili(consumabili, evidenziato, false);

        int nuovoOffset = componenteScorrevole.limitaOffset(ALTEZZA_DISPONIBILE_IN_RIQUADRO_INVENTARIO, offset);
        Image image = componenteScorrevole.produci(ALTEZZA_DISPONIBILE_IN_RIQUADRO_INVENTARIO, nuovoOffset);
        graphics.drawImage(image, x + SPACING, DIMENSIONE_BORDO_INTERNO + 2 * SPACING, null);

        return nuovoOffset;
    }

    private int disegnaElencoDestro(Graphics2D graphics, int x, int offset) {

        List<Consumabile> consumabili = getElencoVenditore();
        Consumabile evidenziato = trovaConsumabile(consumabili, x, offset, mouseX, mouseY, true);
        ComponenteScorrevole<Consumabile> componenteScorrevole = costruisciComponenteScorrevoleConsumabili(consumabili, evidenziato, true);

        int nuovoOffset = componenteScorrevole.limitaOffset(ALTEZZA_DISPONIBILE_IN_RIQUADRO_INVENTARIO, offset);
        Image image = componenteScorrevole.produci(ALTEZZA_DISPONIBILE_IN_RIQUADRO_INVENTARIO, nuovoOffset);
        graphics.drawImage(image, x + SPACING, DIMENSIONE_BORDO_INTERNO + 2 * SPACING, null);

        return nuovoOffset;
    }

    /**
     * Riporta il consumabile disegnato alla posizione (x, y) espressa in coordinate della
     * finestra, oppure null se il punto non cade sull'elenco o non corrisponde al titolo
     * di un consumabile.
     */
    private Consumabile trovaConsumabile(Collection<Consumabile> consumabili, int boxX, int offset, int x, int y, boolean mostraCosto) {
        int xInterno = x - (boxX + SPACING);
        int yInterno = y - (DIMENSIONE_BORDO_INTERNO + 2 * SPACING);
        if (xInterno < 0 || xInterno >= LARGHEZZA_DISPONIBILE_IN_RIQUADRO_INVENTARIO
                || yInterno < 0 || yInterno >= ALTEZZA_DISPONIBILE_IN_RIQUADRO_INVENTARIO) {
            return null;
        }
        return costruisciComponenteScorrevoleConsumabili(consumabili, null, mostraCosto).riferimentoTitoloAllaQuota(yInterno + offset);
    }

    /**
     * L'albero viene ricostruito a ogni disegno e a ogni click. L'artefatto passato in
     * evidenziato (se non null) viene disegnato in bianco invece che in grigio chiaro.
     */
    private ComponenteScorrevole<Consumabile> costruisciComponenteScorrevoleConsumabili(Collection<Consumabile> consumabili,
                                                                                    Consumabile evidenziato, boolean mostraCosto) {

        ComponenteScorrevole<Consumabile> componenteScorrevole = new ComponenteScorrevole<>(
                LARGHEZZA_DISPONIBILE_IN_RIQUADRO_INVENTARIO, 10, 2);

        TipoConsumabile tipoPrecedente = null;

        for (Consumabile consumabile : consumabili) {

            if (tipoPrecedente != consumabile.tipo) {
                tipoPrecedente = consumabile.tipo;
                Image separatore = getSeparatoreConsumabile(tipoPrecedente);
                if (separatore != null) {
                    componenteScorrevole.creaNodo(
                            null, null, null,
                            null, null, null,
                            null, null, null,
                            getSeparatoreConsumabile(tipoPrecedente), null);
                }
            }

            DoomdarkColorModel.Color colore = consumabile == evidenziato
                    ? DoomdarkColorModel.Color.WHITE
                    : DoomdarkColorModel.Color.LIGHT_GRAY;

            String nome = consumabile.nome;
            ComponenteScorrevole<Consumabile>.Nodo nodo = componenteScorrevole.creaNodo(
                    nome, font, colore,
                    String.valueOf(mostraCosto ? consumabile.costo : consumabile.quantita), fontSmall,
                    mostraCosto ? DoomdarkColorModel.Color.YELLOW : DoomdarkColorModel.Color.LIGHT_GRAY,
                    consumabile.descrizione, fontSmall, colore,
                    consumabile.icona, consumabile);
            nodo.setFigliVisibili(statoDi(consumabile).isFigliVisibili());
        }

        return componenteScorrevole;
    }

    @Override
    public void processaClick(int x, int y, Tasto tasto) {
        if (tasto != Tasto.SINISTRO) {
            return;
        }
        Consumabile consumabile = trovaConsumabile(getElencoGruppo(), xMinimaZonaSinistra, offsetYZonaSinistra, x, y, false);
        if (consumabile == null) {
            consumabile = trovaConsumabile(getElencoVenditore(), xMinimaZonaDestra, offsetYZonaDestra, x, y, true);
        }
        if (consumabile == null) {
            return;
        }
        StatoAttributo statoAttributo = statoDi(consumabile);
        if (statoAttributo.isFigliVisibili()) {
            statoAttributo.nascondiFigli();
        } else {
            statoAttributo.mostraFigli();
        }
    }

    @Override
    public void processaDoppioClick(int x, int y, Tasto tasto) {
        Collection<Consumabile> disponibili = getElencoVenditore();
        Consumabile consumabile = trovaConsumabile(disponibili, xMinimaZonaDestra, offsetYZonaDestra, x, y, true);
        if (consumabile != null) {
            BusEventi.pubblica(new ComandoAcquistoConsumabile(consumabile.tipo, consumabile.classeIncantesimo,
                    consumabile.personaggio, consumabile.costo));
        }
    }

    private List<Consumabile> getElencoGruppo() {
        List<Consumabile> elencoGruppo = new ArrayList<>();
        GruppoGiocatore gruppoGiocatore = GruppoGiocatore.getIstanza();
        for (ClasseIncantesimo classeIncantesimo : ClasseIncantesimo.values()) {
            int quantita = gruppoGiocatore.getIncantesimi(classeIncantesimo);
            if (quantita > 0) {
                elencoGruppo.add(costruisciIncantesimo(classeIncantesimo, quantita));
            }
        }
        if (gruppoGiocatore.getPozioniSalute() > 0) {
            elencoGruppo.add(costruisciPozioneSalute(gruppoGiocatore.getPozioniSalute()));
        }
        if (gruppoGiocatore.getPozioniSaluteGrande() > 0) {
            elencoGruppo.add(costruisciPozioneSaluteGrande(gruppoGiocatore.getPozioniSaluteGrande()));
        }
        if (gruppoGiocatore.getPozioniMagia() > 0) {
            elencoGruppo.add(costruisciPozioneMagia(gruppoGiocatore.getPozioniMagia()));
        }
        if (gruppoGiocatore.getPozioniMagia() > 0) {
            elencoGruppo.add(costruisciPozioneMagiaGrande(gruppoGiocatore.getPozioniMagiaGrande()));
        }
        return elencoGruppo;
    }

    private List<Consumabile> getElencoVenditore() {
        List<Consumabile> elencoVenditore = new ArrayList<>();
        for (ClasseIncantesimo classeIncantesimo : ClasseIncantesimo.values()) {
            elencoVenditore.add(costruisciIncantesimo(classeIncantesimo, 1));
        }
        elencoVenditore.add(costruisciPozioneSalute(1));
        elencoVenditore.add(costruisciPozioneSaluteGrande(1));
        elencoVenditore.add(costruisciPozioneMagia(1));
        elencoVenditore.add(costruisciPozioneMagiaGrande(1));
        GruppoGiocatore gruppoGiocatore = GruppoGiocatore.getIstanza();
        for (Personaggio personaggio : gruppoGiocatore.getPersonaggiVivi()) {
            if (!personaggio.isPNG()) {
                Consumabile consumabile = new Consumabile(
                        TipoConsumabile.AUMENTO_MAGIA_SINGOLO,
                        "Aumento Magia massima",
                        "Aumenta la magia massima di " + personaggio.getNome(),
                        1,
                        null,
                        personaggio,
                        Costanti.COSTO_AUMENTO_MAGIA_GIOCATORE_SINGOLO,
                        ClassePersonaggioImmagine.getIcona(personaggio.getClasse())
                );
                elencoVenditore.add(consumabile);
            }
        }
        long conteggioPersonaggi = gruppoGiocatore.getPersonaggiVivi()
                .stream()
                .filter(p -> !p.isPNG())
                .count();
        if (conteggioPersonaggi > 1) {
            long costo = Costanti.COSTO_AUMENTO_MAGIA_GIOCATORE_SINGOLO +
                    (conteggioPersonaggi - 1) * Costanti.COSTO_AUMENTO_MAGIA_GIOCATORE_SINGOLO * 75 / 100;
            Consumabile consumabile = new Consumabile(
                    TipoConsumabile.AUMENTO_MAGIA_GRUPPO,
                    "Aumento Magia massima",
                    "Aumenta la magia massima di tutto il gruppo",
                    1,
                    null,
                    null,
                    (int)costo,
                    ImageCache.spriteGruppo
            );
            elencoVenditore.add(consumabile);
        }
        elencoVenditore.add(new Consumabile(TipoConsumabile.MAPPA_PARZIALE_FORESTA,
                "Mappa della zona",
                "Una mappa della zona circostante",
                1,
                null,
                null,
                Costanti.COSTO_MAPPA_DELLA_ZONA,
                ImageCache.spriteMappa
        ));
        elencoVenditore.add(new Consumabile(TipoConsumabile.MAPPA_COMPLETA_FORESTA,
                "Mappa della Foresta",
                "Una mappa completa della Foresta",
                1,
                null,
                null,
                Costanti.COSTO_MAPPA_DELLA_FORESTA,
                ImageCache.spriteMappa
        ));
        return elencoVenditore;
    }

    private Consumabile costruisciIncantesimo(ClasseIncantesimo classeIncantesimo, int quantita) {
        String nomeSingolare = classeIncantesimo.getNomeSingolare();
        nomeSingolare = nomeSingolare.substring(0, 1).toUpperCase() + nomeSingolare.substring(1);
        return new Consumabile(TipoConsumabile.INCANTESIMO,
                nomeSingolare,
                classeIncantesimo.getEffetto(),
                quantita,
                classeIncantesimo,
                null,
                classeIncantesimo.getCostoAcquisto(),
                ImageCache.spriteIncantesimi[classeIncantesimo.ordinal()]);
    }

    private Consumabile costruisciPozioneSalute(int quantita) {
        return new Consumabile(TipoConsumabile.POZIONE_SALUTE,
                "Pozione della Salute",
                "Fa riacquistare punti di Salute",
                quantita,
                null,
                null,
                Costanti.COSTO_POZIONE_SALUTE,
                ImageCache.spritePozioneSalute);
    }

    private Consumabile costruisciPozioneSaluteGrande(int quantita) {
        return new Consumabile(TipoConsumabile.POZIONE_SALUTE_GRANDE,
                "Pozione della Salute (grande)",
                "Fa riacquistare punti di Salute e ne aumenta il livello massimo",
                quantita,
                null,
                null,
                Costanti.COSTO_POZIONE_SALUTE_GRANDE,
                ImageCache.spritePozioneSaluteGrande);
    }

    private Consumabile costruisciPozioneMagia(int quantita) {
        return new Consumabile(TipoConsumabile.POZIONE_MAGIA,
                "Pozione della Magia",
                "Fa riacquistare punti di Magia",
                quantita,
                null,
                null,
                Costanti.COSTO_POZIONE_MAGIA,
                ImageCache.spritePozioneMagia);
    }

    private Consumabile costruisciPozioneMagiaGrande(int quantita) {
        return new Consumabile(TipoConsumabile.POZIONE_MAGIA_GRANDE,
                "Pozione della Magia (grande)",
                "Fa riacquistare punti di Magia e ne aumenta il livello massimo",
                quantita,
                null,
                null,
                Costanti.COSTO_POZIONE_MAGIA_GRANDE,
                ImageCache.spritePozioneMagiaGrande);
    }

    private static class Consumabile {

        private final TipoConsumabile tipo;
        private final String nome;
        private final String descrizione;
        private final int quantita;
        private final ClasseIncantesimo classeIncantesimo;
        private final Personaggio personaggio;
        private final int costo;
        private final Image icona;

        public Consumabile(TipoConsumabile tipo, String nome, String descrizione, int quantita,
                           ClasseIncantesimo classeIncantesimo, Personaggio personaggio,
                           int costo, Image icona) {
            this.tipo = tipo;
            this.nome = nome;
            this.descrizione = descrizione;
            this.quantita = quantita;
            this.classeIncantesimo = classeIncantesimo;
            this.personaggio = personaggio;
            this.costo = costo;
            this.icona = icona;
        }
    }

    private Image getSeparatoreConsumabile(TipoConsumabile tipoConsumabile) {
        switch (tipoConsumabile) {
            case INCANTESIMO:
                return ImageCache.separatoreIncantesimi;
            case POZIONE_SALUTE:
                return ImageCache.separatorePozioni;
            default:
                return null;
        }
    }
    // --- CLASSE DI APPOGGIO per ricordare se gli attributi vanno tenuti aperti o chiusi
    /**
     * Le caratteristiche di un Consumabile, a differenza degli artefatti, non hanno un
     * modello dati proprio: questa classe di appoggio associa a ogni voce la
     * visibilità della sua descrizione, replicando l'API isFigliVisibili/mostraFigli/
     * nascondiFigli già usata da Artefatto.
     */
    private static final class StatoAttributo {

        private boolean figliVisibili = true;

        private boolean isFigliVisibili() {
            return figliVisibili;
        }

        private void mostraFigli() {
            figliVisibili = true;
        }

        private void nascondiFigli() {
            figliVisibili = false;
        }
    }

    private final Map<Object, StatoAttributo> statiAttributi = new HashMap<>();

    /**
     * TipoConsumabile da solo non basta come chiave: INCANTESIMO è lo stesso valore per
     * tutte le ClasseIncantesimo, e AUMENTO_MAGIA_SINGOLO è lo stesso per ogni personaggio.
     * Usiamo quindi, quando presente, il tratto che distingue le singole voci dello stesso tipo.
     */
    private Object chiaveVisibilita(Consumabile consumabile) {
        if (consumabile.classeIncantesimo != null) {
            return consumabile.classeIncantesimo;
        }
        if (consumabile.personaggio != null) {
            return consumabile.personaggio;
        }
        return consumabile.tipo;
    }

    private StatoAttributo statoDi(Consumabile consumabile) {
        return statiAttributi.computeIfAbsent(chiaveVisibilita(consumabile), k -> new StatoAttributo());
    }
    // --- FINE classe di appoggio

}
