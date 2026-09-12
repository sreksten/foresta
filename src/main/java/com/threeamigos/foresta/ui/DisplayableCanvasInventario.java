package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.motore.modellodati.TipoAttributo;
import com.threeamigos.foresta.personaggi.Personaggio;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Stefano Reksten
 */
public class DisplayableCanvasInventario extends DisplayableCanvasScambiatoreArtefatti {


    /**
     * Le caratteristiche di un personaggio, a differenza degli artefatti, non hanno un
     * modello dati proprio: questa classe di appoggio associa a ogni TipoAttributo la
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

    private final Map<TipoAttributo, StatoAttributo> statiAttributi = new HashMap<>();

    private StatoAttributo statoDi(TipoAttributo tipoAttributo) {
        return statiAttributi.computeIfAbsent(tipoAttributo, t -> new StatoAttributo());
    }

    DisplayableCanvasInventario(DisplayableCanvas displayableCanvas, int width, int height) {
        super(displayableCanvas, width, height);
    }

    @Override
    void disegnaIntestazioniInventario(Graphics2D graphics) {
        disegnaIntestazioniInventarioImpl(graphics, "Inventario personaggio", "Inventario gruppo");
    }

    void disegnaColonnaPersonaggio(Graphics2D graphics) {

        Image doomdark;
        int y = SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI;
        DoomdarkColorModel.Color coloreTestata = DoomdarkColorModel.Color.LIGHT_GRAY;

        Personaggio p = (Personaggio)automa.getParteAttiva();
        // Nome personaggio
        doomdark = ImageCache.get(p.getNome(Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA), coloreTestata);
        graphics.drawImage(doomdark, (width - doomdark.getWidth(null)) / 2, y, null);
        y += fontHeight + SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI;

        // Immagine personaggio
        BufferedImage immaginePersonaggio = ClassePersonaggioImmagine.getImmagine(p.getClasse());

        // Per tenere i personaggi sullo stesso livello (se si passa da un personaggio all'altro)
        // ed evitare sfarfallamenti, scegliamo il ladro come personaggio "base" per calcolare l'altezza a cui disegnare.
        y += ALTEZZA_LADRO;
        graphics.drawImage(immaginePersonaggio, (width - immaginePersonaggio.getWidth()) / 2, y - immaginePersonaggio.getHeight(), null);
        y += SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI;

        // Livello, XP, punti disponibili
        disegna(TipoAttributo.LIVELLO, p.getLivello(), graphics, y, coloreTestata);
        y += fontHeight + SPACING;
        disegna(TipoAttributo.PUNTI_ESPERIENZA, p.getPuntiEsperienza(), graphics, y, coloreTestata);
        y += fontHeight + SPACING;
        disegna(TipoAttributo.PUNTI_ABILITA, p.getPuntiAbilitaDisponibili(), graphics, y, coloreTestata);
        y += fontHeight + SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI;

        BufferedImage separatore = ImageCache.separatore;
        graphics.drawImage(separatore, (width - separatore.getWidth()) / 2, y, null);
        y += separatore.getHeight() + SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI;

        // Attributi personaggio
        yAttributi = y;
        TipoAttributo attributoEvidenziato = trovaAttributo(p, mouseX, mouseY);
        ComponenteScorrevole<TipoAttributo> componenteScorrevole = costruisciComponenteScorrevoleAttributi(p, attributoEvidenziato);

        offsetYBoxPersonaggio = componenteScorrevole.limitaOffset(height - y, offsetYBoxPersonaggio);
        Image image = componenteScorrevole.produci(height - y, offsetYBoxPersonaggio);
        graphics.drawImage(image, corniceInventarioWidth + 2 * SPACING, y, null);

    }

    /**
     * L'albero viene ricostruito a ogni disegno e a ogni click. L'attributo passato in
     * evidenziato (se non null ed è primario) viene disegnato in bianco invece che nel suo
     * colore consueto.
     */
    private ComponenteScorrevole<TipoAttributo> costruisciComponenteScorrevoleAttributi(Personaggio p, TipoAttributo evidenziato) {

        ComponenteScorrevole<TipoAttributo> componenteScorrevole = new ComponenteScorrevole<>(
                width - 2 * corniceInventarioWidth - 4 * SPACING, 10, 2);

        DoomdarkColorModel.Color colore;

        colore = DoomdarkColorModel.Color.LIGHT_GRAY;

        creaNodo(componenteScorrevole, colore, TipoAttributo.FORZA, p.getForza(), evidenziato);
        creaNodo(componenteScorrevole, colore, TipoAttributo.DESTREZZA, p.getDestrezza(), evidenziato);
        creaNodo(componenteScorrevole, colore, TipoAttributo.COSTITUZIONE, p.getCostituzione(), evidenziato);
        creaNodo(componenteScorrevole, colore, TipoAttributo.INTELLIGENZA, p.getIntelligenza(), evidenziato);
        creaNodo(componenteScorrevole, colore, TipoAttributo.SAGGEZZA, p.getSaggezza(), evidenziato);
        creaNodo(componenteScorrevole, colore, TipoAttributo.CARISMA, p.getCarisma(), evidenziato);
        creaNodo(componenteScorrevole, colore, TipoAttributo.FORTUNA, p.getFortuna(), evidenziato);
        creaNodo(componenteScorrevole, colore, TipoAttributo.NUMERO_BERSAGLI, p.getBersagli(), evidenziato);
        creaNodo(componenteScorrevole, colore, TipoAttributo.RIGENERAZIONE_SALUTE, p.getRigenerazioneSalute(), evidenziato);
        creaNodo(componenteScorrevole, colore, TipoAttributo.RIGENERAZIONE_MAGIA, p.getRigenerazioneMagia(), evidenziato);

        colore = DoomdarkColorModel.Color.MEDIUM_GRAY;

        creaNodo(componenteScorrevole, colore, TipoAttributo.CARICO_MASSIMO, p.getCaricoMassimo(), evidenziato);
        creaNodo(componenteScorrevole, colore, TipoAttributo.CRITICO, p.getCritico(), evidenziato);
        creaNodo(componenteScorrevole, colore, TipoAttributo.PRECISIONE, p.getPrecisione(), evidenziato);
        creaNodo(componenteScorrevole, colore, TipoAttributo.VELOCITA, p.getVelocita(), evidenziato);
        creaNodo(componenteScorrevole, colore, TipoAttributo.FURTIVITA, p.getFurtivita(), evidenziato);
        creaNodo(componenteScorrevole, colore, TipoAttributo.PARATA, p.getParata(), evidenziato);
        creaNodo(componenteScorrevole, colore, TipoAttributo.RESISTENZA_MAGICA, p.getResistenzaMagica(), evidenziato);
        creaNodo(componenteScorrevole, colore, TipoAttributo.PERCEZIONE, p.getPercezione(), evidenziato);
        creaNodo(componenteScorrevole, colore, TipoAttributo.SOGGEZIONE, p.getSoggezione(), evidenziato);
        creaNodo(componenteScorrevole, colore, TipoAttributo.FURIA, p.getFuria(), evidenziato);
        creaNodo(componenteScorrevole, colore, TipoAttributo.CORAGGIO, p.getCoraggio(), evidenziato);
        creaNodo(componenteScorrevole, colore, TipoAttributo.VALORE, p.getValore(), evidenziato);

        return componenteScorrevole;
    }

    private void creaNodo(ComponenteScorrevole<TipoAttributo> componenteScorrevole, DoomdarkColorModel.Color colore,
                          TipoAttributo tipoAttributo, double valoreAttributo, TipoAttributo evidenziato) {
        DoomdarkColorModel.Color coloreEffettivo = tipoAttributo.isPrimario() && tipoAttributo == evidenziato
                ? DoomdarkColorModel.Color.WHITE
                : colore;
        ComponenteScorrevole<TipoAttributo>.Nodo nodo = componenteScorrevole.creaNodo(
                tipoAttributo.getNome(), font, coloreEffettivo,
                String.valueOf((int)valoreAttributo), font, coloreEffettivo,
                tipoAttributo.getDescrizione(), fontSmall, coloreEffettivo,
                null, tipoAttributo);
        nodo.setFigliVisibili(statoDi(tipoAttributo).isFigliVisibili());
    }

    /**
     * Riporta l'attributo il cui titolo occupa la posizione (x, y) espressa in coordinate
     * della finestra, oppure null se il punto non cade sull'elenco degli attributi o non
     * corrisponde al titolo di un attributo.
     */
    private TipoAttributo trovaAttributo(Personaggio p, int x, int y) {
        int larghezzaAttributi = width - 2 * corniceInventarioWidth - 4 * SPACING;
        int xInterno = x - (corniceInventarioWidth + 2 * SPACING);
        if (xInterno < 0 || xInterno >= larghezzaAttributi || y < yAttributi || y >= height) {
            return null;
        }
        int yInterno = y - yAttributi + offsetYBoxPersonaggio;
        return costruisciComponenteScorrevoleAttributi(p, null).riferimentoTitoloAllaQuota(yInterno);
    }

    private void disegna(TipoAttributo attributo, int valore, Graphics2D graphics, int y, DoomdarkColorModel.Color colore) {
        Image i = ImageCache.get(attributo.getNome(), colore);
        graphics.drawImage(i, centerBoxX, y, null);
        i = ImageCache.get(valore, colore);
        graphics.drawImage(i, centerBoxLimit - i.getWidth(null), y, null);
    }

    protected boolean processaClickPersonaggio(int x, int y, Tasto tasto) {
        Personaggio personaggio = (Personaggio)automa.getParteAttiva();
        TipoAttributo attributo = trovaAttributo(personaggio, x, y);
        if (attributo != null) {
            StatoAttributo stato = statoDi(attributo);
            if (stato.isFigliVisibili()) {
                stato.nascondiFigli();
            } else {
                stato.mostraFigli();
            }
            return true;
        }
        return false;
    }

    protected boolean processaDoppioClickPersonaggio(int x, int y, Tasto tasto) {
        Personaggio personaggio = (Personaggio)automa.getParteAttiva();
        TipoAttributo attributo = trovaAttributo(personaggio, x, y);
        if (attributo != null) {
            personaggio.spendiPuntoAbilita(attributo);
            return true;
        }
        return false;
    }

}
