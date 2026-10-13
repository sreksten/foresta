package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoSpesaPuntoAbilita;
import com.threeamigos.foresta.eventi.interni.InternoNotificaViaFumettoATempo;
import com.threeamigos.foresta.eventi.notifiche.NotificaRifiutoPrelievoArtefatto;
import com.threeamigos.foresta.interfacce.*;
import com.threeamigos.foresta.modellodati.ModificatoreAttributoMD;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoModificatore;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 *
 * @author Stefano Reksten
 */
public class DisplayableCanvasInventario extends DisplayableCanvasScambiatoreArtefatti {

    // --- CLASSE DI APPOGGIO per ricordare se gli attributi vanno tenuti aperti o chiusi
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

    // Il riferimento del titolo dell'elenco dei modificatori temporanei, per distinguerlo dagli attributi
    private static final Object MODIFICATORI_TEMPORANEI = new Object();

    private boolean modificatoriTemporaneiVisibili = true;

    private final Map<TipoAttributo, StatoAttributo> statiAttributi = new HashMap<>();

    private StatoAttributo statoDi(TipoAttributo tipoAttributo) {
        return statiAttributi.computeIfAbsent(tipoAttributo, t -> new StatoAttributo());
    }
    // --- FINE classe di appoggio

    // Il personaggio di cui si mostra l'inventario: la parte attiva dello scambio
    private VistaPersonaggio personaggio;

    DisplayableCanvasInventario(int width, int height, VistaPartita vistaPartita) {
        super(width, height, vistaPartita);
        BusEventi.iscriviti(NotificaRifiutoPrelievoArtefatto.class, this::onEventoRifiutoPrelievo);
    }

    /**
     * @param scambio fra l'inventario del personaggio (parte attiva) e quello del gruppo (parte remota)
     */
    void impostaScambio(VistaScambio scambio, VistaPersonaggio personaggio) {
        impostaScambio(scambio);
        this.personaggio = personaggio;
    }

    void onEventoRifiutoPrelievo(NotificaRifiutoPrelievoArtefatto evento) {
        BusEventi.pubblica(new InternoNotificaViaFumettoATempo(evento.getMotivo(), getCoordinateFumetto()));
    }

    @Override
    protected String aiutoDoppioClickSinistra() {
        return "Doppio click: sposta nell'inventario del gruppo";
    }

    @Override
    protected String aiutoDoppioClickDestra() {
        return "Doppio click: equipaggia";
    }

    @Override
    void disegnaIntestazioniInventario(Graphics2D graphics) {
        disegnaIntestazioniInventarioImpl(graphics, "Inventario personaggio", "Inventario gruppo");
    }

    void disegnaColonnaPersonaggio(Graphics2D graphics) {

        Image doomdark;
        int y = SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI;
        DoomdarkColorModel.Color coloreTestata = DoomdarkColorModel.Color.LIGHT_GRAY;

        // Nome personaggio
        doomdark = ImageCache.get(personaggio.getNome(VistaPersonaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA), coloreTestata);
        graphics.drawImage(doomdark, (width - doomdark.getWidth(null)) / 2, y, null);
        y += fontHeight + SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI;

        // Immagine personaggio
        BufferedImage immaginePersonaggio = ClassePersonaggioImmagine.getImmagine(personaggio.getClasse());

        // Per tenere i personaggi sullo stesso livello (se si passa da un personaggio all'altro)
        // ed evitare sfarfallamenti, scegliamo il ladro come personaggio "base" per calcolare l'altezza a cui disegnare.
        y += ALTEZZA_LADRO;
        graphics.drawImage(immaginePersonaggio, (width - immaginePersonaggio.getWidth()) / 2, y - immaginePersonaggio.getHeight(), null);
        y += SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI;

        // I set leggendari completi, sotto l'immagine: quello che segue scende di quanto serve
        for (String setCompleto : personaggio.getDescrizioniSetCompleti()) {
            doomdark = ImageCache.get(setCompleto, DoomdarkColorModel.Color.YELLOW);
            graphics.drawImage(doomdark, (width - doomdark.getWidth(null)) / 2, y, null);
            y += fontHeight + SPACING;
        }

        // Livello, XP, punti disponibili
        disegnaAttributoEValore(TipoAttributo.LIVELLO, personaggio.getLivello(), graphics, y, coloreTestata);
        y += fontHeight + SPACING;
        // L'esperienza accumulata sul totale che serve per il livello dopo, e quanto ne manca
        int esperienza = personaggio.getEsperienza();
        disegnaTestoEValore(TipoAttributo.PUNTI_ESPERIENZA.getNome(), String.valueOf(esperienza), graphics, y, coloreTestata);
        y += fontHeight + SPACING;
        if (!personaggio.isAlLivelloMassimo()) {
            int perProssimoLivello = personaggio.getEsperienzaPerProssimoLivello();
            disegnaTestoEValore("Su", String.valueOf(perProssimoLivello), graphics, y, coloreTestata);
        } else {
            disegnaTestoEValore("Livello massimo", "", graphics, y, coloreTestata);
        }
        y += fontHeight + SPACING;
        // In verde se ci sono punti da spendere, perché si notino
        disegnaAttributoEValore(TipoAttributo.PUNTI_ABILITA, personaggio.getPuntiAbilitaDisponibili(), graphics, y,
                personaggio.getPuntiAbilitaDisponibili() > 0 ? DoomdarkColorModel.Color.GREEN : coloreTestata);
        y += fontHeight;

        BufferedImage separatore = ImageCache.separatore;
        graphics.drawImage(separatore, (width - separatore.getWidth()) / 2, y, null);
        y += separatore.getHeight();

        // Attributi personaggio
        yAttributi = y;
        Object riferimentoSottoIlMouse = trovaRiferimento(personaggio, mouseX, mouseY);
        TipoAttributo attributoEvidenziato = riferimentoSottoIlMouse instanceof TipoAttributo ? (TipoAttributo) riferimentoSottoIlMouse : null;
        ComponenteScorrevole<Object> componenteScorrevole = costruisciComponenteScorrevoleAttributi(personaggio, attributoEvidenziato);

        offsetYZonaCentrale = componenteScorrevole.limitaOffset(height - y, offsetYZonaCentrale);
        Image image = componenteScorrevole.produci(height - y, offsetYZonaCentrale);
        graphics.drawImage(image, corniceInventarioWidth + 2 * SPACING, y, null);

    }

    /**
     * L'albero viene ricostruito a ogni disegno e a ogni click. L'attributo passato in
     * evidenziato (se non null ed è primario) viene disegnato in bianco invece che nel suo
     * colore consueto.
     */
    private ComponenteScorrevole<Object> costruisciComponenteScorrevoleAttributi(VistaPersonaggio p, TipoAttributo evidenziato) {

        ComponenteScorrevole<Object> componenteScorrevole = new ComponenteScorrevole<>(
                width - 2 * corniceInventarioWidth - 4 * SPACING, 10, 2);

        DoomdarkColorModel.Color colore;

        colore = DoomdarkColorModel.Color.LIGHT_GRAY;

        creaNodoModificatoriTemporanei(componenteScorrevole, p);

        creaNodo(componenteScorrevole, p, colore, TipoAttributo.FORZA, p.getForza(), evidenziato);
        creaNodo(componenteScorrevole, p, colore, TipoAttributo.DESTREZZA, p.getDestrezza(), evidenziato);
        creaNodo(componenteScorrevole, p, colore, TipoAttributo.COSTITUZIONE, p.getCostituzione(), evidenziato);
        creaNodo(componenteScorrevole, p, colore, TipoAttributo.INTELLIGENZA, p.getIntelligenza(), evidenziato);
        creaNodo(componenteScorrevole, p, colore, TipoAttributo.SAGGEZZA, p.getSaggezza(), evidenziato);
        creaNodo(componenteScorrevole, p, colore, TipoAttributo.CARISMA, p.getCarisma(), evidenziato);
        creaNodo(componenteScorrevole, p, colore, TipoAttributo.FORTUNA, p.getFortuna(), evidenziato);

        colore = DoomdarkColorModel.Color.MEDIUM_GRAY;

        creaNodo(componenteScorrevole, p, colore, TipoAttributo.NUMERO_BERSAGLI, p.getBersagli(), evidenziato);
        creaNodo(componenteScorrevole, p, colore, TipoAttributo.POTERE_MAGICO, p.getPotereMagico(), evidenziato);
        creaNodo(componenteScorrevole, p, colore, TipoAttributo.RIGENERAZIONE_SALUTE, p.getRigenerazioneSalute(), evidenziato);
        creaNodo(componenteScorrevole, p, colore, TipoAttributo.RIGENERAZIONE_MAGIA, p.getRigenerazioneMagia(), evidenziato);
        creaNodo(componenteScorrevole, p, colore, TipoAttributo.CARICO_MASSIMO, p.getCaricoMassimo(), evidenziato);
        creaNodo(componenteScorrevole, p, colore, TipoAttributo.CRITICO, p.getCritico(), evidenziato);
        creaNodo(componenteScorrevole, p, colore, TipoAttributo.PRECISIONE, p.getPrecisione(), evidenziato);
        creaNodo(componenteScorrevole, p, colore, TipoAttributo.VELOCITA, p.getVelocita(), evidenziato);
        creaNodo(componenteScorrevole, p, colore, TipoAttributo.FURTIVITA, p.getFurtivita(), evidenziato);
        creaNodo(componenteScorrevole, p, colore, TipoAttributo.PARATA, p.getParata(), evidenziato);
        creaNodo(componenteScorrevole, p, colore, TipoAttributo.RESISTENZA_MAGICA, p.getResistenzaMagica(), evidenziato);
        creaNodo(componenteScorrevole, p, colore, TipoAttributo.PERCEZIONE, p.getPercezione(), evidenziato);
        creaNodo(componenteScorrevole, p, colore, TipoAttributo.SOGGEZIONE, p.getSoggezione(), evidenziato);
        creaNodo(componenteScorrevole, p, colore, TipoAttributo.FURIA, p.getFuria(), evidenziato);
        creaNodo(componenteScorrevole, p, colore, TipoAttributo.CORAGGIO, p.getCoraggio(), evidenziato);
        creaNodo(componenteScorrevole, p, colore, TipoAttributo.VALORE, p.getValore(), evidenziato);
        creaNodo(componenteScorrevole, p, colore, TipoAttributo.CONTRATTAZIONE, p.getContrattazione(), evidenziato);

        return componenteScorrevole;
    }

    /**
     * In cima, se il personaggio ha modificatori che scadono, l'elenco di quelli attivi con il tempo che resta
     */
    private void creaNodoModificatoriTemporanei(ComponenteScorrevole<Object> componenteScorrevole, VistaPersonaggio p) {
        Collection<ModificatoreAttributoMD> temporanei = p.getModificatoriTemporanei();
        if (temporanei.isEmpty()) {
            return;
        }
        ComponenteScorrevole<Object>.Nodo nodo = componenteScorrevole.creaNodo(
                "Modificatori temporanei", font, DoomdarkColorModel.Color.LIGHT_GRAY,
                String.valueOf(temporanei.size()), font, DoomdarkColorModel.Color.LIGHT_GRAY,
                null, null, null,
                null, MODIFICATORI_TEMPORANEI);
        nodo.setFigliVisibili(modificatoriTemporaneiVisibili);
        for (ModificatoreAttributoMD modificatore : temporanei) {
            DoomdarkColorModel.Color coloreModificatore = modificatore.getQuantita() >= 0
                    && modificatore.getTipoModificatoreAttributo() != TipoModificatore.QUANTITA_ASSOLUTA
                    ? DoomdarkColorModel.Color.GREEN : DoomdarkColorModel.Color.RED;
            nodo.creaNodo(
                    modificatore.descrizioneEffetto(), font, coloreModificatore,
                    modificatore.descrizioneDurata().replaceFirst("^per ", ""), font, coloreModificatore,
                    modificatore.getNote().isEmpty() ? null : modificatore.getNote(), fontSmall, coloreModificatore,
                    null, MODIFICATORI_TEMPORANEI);
        }
    }

    /**
     * Giallo se un modificatore porta l'attributo a un valore fisso (da qualunque fonte); altrimenti verde o rosso
     * se i modificatori temporanei lo alzano o lo abbassano. Sotto il mouse un attributo primario è bianco.
     */
    private void creaNodo(ComponenteScorrevole<Object> componenteScorrevole, VistaPersonaggio p, DoomdarkColorModel.Color colore,
                          TipoAttributo tipoAttributo, double valoreAttributo, TipoAttributo evidenziato) {
        Optional<ModificatoreAttivo> fissaggio = p.getFissaggio(tipoAttributo);
        double variazione = p.getVariazioneDaTemporanei(tipoAttributo);
        DoomdarkColorModel.Color coloreBase = fissaggio.isPresent() ? DoomdarkColorModel.Color.YELLOW
                : variazione > 0 ? DoomdarkColorModel.Color.GREEN
                : variazione < 0 ? DoomdarkColorModel.Color.RED
                : colore;
        DoomdarkColorModel.Color coloreEffettivo = tipoAttributo.isPrimario() && tipoAttributo == evidenziato
                ? DoomdarkColorModel.Color.WHITE
                : coloreBase;
        ComponenteScorrevole<Object>.Nodo nodo = componenteScorrevole.creaNodo(
                tipoAttributo.getNome(), font, coloreEffettivo,
                String.valueOf((int)valoreAttributo), font, coloreEffettivo,
                tipoAttributo.getDescrizione(), fontSmall, coloreEffettivo,
                null, tipoAttributo);
        nodo.setFigliVisibili(statoDi(tipoAttributo).isFigliVisibili());
        // La causa del valore fisso (sotto l'attributo) è gialla, ma diventa bianca insieme all'attributo sotto il mouse
        DoomdarkColorModel.Color coloreCausa = coloreEffettivo == DoomdarkColorModel.Color.WHITE ? DoomdarkColorModel.Color.WHITE : DoomdarkColorModel.Color.YELLOW;
        fissaggio.ifPresent(f -> {
            ModificatoreAttributoMD modificatore = f.getModificatore();
            String da = f.getFonte().isEmpty() ? "" : "da " + f.getFonte();
            String durata = modificatore.isTemporaneo() ? " (" + modificatore.descrizioneDurata().replaceFirst("^per ", "ancora ") + ")" : "";
            nodo.creaNodo(
                    "Fissato a " + (int) modificatore.getQuantita(), fontSmall, coloreCausa,
                    null, null, null,
                    (da + durata).trim().isEmpty() ? null : (da + durata).trim(), fontSmall, coloreCausa,
                    null, tipoAttributo);
        });
    }

    /**
     * Riporta l'attributo il cui titolo occupa la posizione (x, y) espressa in coordinate
     * della finestra (oppure MODIFICATORI_TEMPORANEI, se è il titolo di quell'elenco), oppure null se il punto non
     * cade sull'elenco degli attributi o non corrisponde a un titolo.
     */
    private Object trovaRiferimento(VistaPersonaggio p, int x, int y) {
        int larghezzaAttributi = width - 2 * corniceInventarioWidth - 4 * SPACING;
        int xInterno = x - (corniceInventarioWidth + 2 * SPACING);
        if (xInterno < 0 || xInterno >= larghezzaAttributi || y < yAttributi || y >= height) {
            return null;
        }
        int yInterno = y - yAttributi + offsetYZonaCentrale;
        return costruisciComponenteScorrevoleAttributi(p, null).riferimentoTitoloAllaQuota(yInterno);
    }

    private void disegnaTestoEValore(String etichetta, String valore, Graphics2D graphics, int y, DoomdarkColorModel.Color colore) {
        Image i = ImageCache.get(etichetta, colore);
        graphics.drawImage(i, xMinimaZonaCentrale, y, null);
        i = ImageCache.get(valore, colore);
        graphics.drawImage(i, xMassimaZonaCentrale - i.getWidth(null), y, null);
    }

    private void disegnaAttributoEValore(TipoAttributo attributo, int valore, Graphics2D graphics, int y, DoomdarkColorModel.Color colore) {
        Image i = ImageCache.get(attributo.getNome(), colore);
        graphics.drawImage(i, xMinimaZonaCentrale, y, null);
        i = ImageCache.get(valore, colore);
        graphics.drawImage(i, xMassimaZonaCentrale - i.getWidth(null), y, null);
    }

    protected boolean processaClickPersonaggio(int x, int y, Tasto tasto) {
        Object riferimento = trovaRiferimento(personaggio, x, y);
        if (riferimento == MODIFICATORI_TEMPORANEI) {
            modificatoriTemporaneiVisibili = !modificatoriTemporaneiVisibili;
            return true;
        }
        TipoAttributo attributo = riferimento instanceof TipoAttributo ? (TipoAttributo) riferimento : null;
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

    /**
     * Negli artefatti del gruppo il livello dice se il personaggio può prenderli adesso (verde) o no (rosso), per
     * qualunque motivo: livello, classe, peso, forza, posto occupato. Quelli che ha già restano grigi.
     */
    @Override
    protected DoomdarkColorModel.Color coloreLivello(VistaArtefatto artefatto, boolean parteAttiva) {
        if (parteAttiva) {
            return super.coloreLivello(artefatto, true);
        }
                return personaggio.isEquipaggiabile(artefatto) ? DoomdarkColorModel.Color.GREEN : DoomdarkColorModel.Color.RED;
    }

    protected boolean processaDoppioClickPersonaggio(int x, int y, Tasto tasto) {
        Object riferimento = trovaRiferimento(personaggio, x, y);
        TipoAttributo attributo = riferimento instanceof TipoAttributo ? (TipoAttributo) riferimento : null;
        if (attributo != null) {
            BusEventi.pubblica(new ComandoSpesaPuntoAbilita(personaggio.getUuid(), attributo));
            return true;
        }
        return false;
    }

}
