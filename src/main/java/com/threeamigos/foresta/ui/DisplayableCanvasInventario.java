package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.motore.AutomaInventario;
import com.threeamigos.foresta.motore.modellodati.ModificatoreAttributo;
import com.threeamigos.foresta.motore.modellodati.SupertipoArtefatto;
import com.threeamigos.foresta.motore.modellodati.TipoAttributo;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.oggetti.Incantamento;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;
import com.threeamigos.foresta.personaggi.Personaggio;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Stefano Reksten
 */
public class DisplayableCanvasInventario implements Finestra {

    private static final int DIMENSIONE_BORDO_INTERNO = 16;
    private static final int corniceInventarioWidth = ImageCache.corniceInventario.getWidth(null);
    private static final int ALTEZZA_DISPONIBILE_IN_RIQUADRO_INVENTARIO = ImageCache.corniceInventario.getHeight()
            - 2 * (DIMENSIONE_BORDO_INTERNO + ImageCache.SPACING);
    private static final int LARGHEZZA_DISPONIBILE_IN_RIQUADRO_INVENTARIO = ImageCache.corniceInventario.getWidth()
            - 2 * (DIMENSIONE_BORDO_INTERNO + ImageCache.SPACING);
    // Pixel di scorrimento per ogni scatto della rotella
    private static final int PASSO_SCORRIMENTO = 2;

    private static final int ALTEZZA_LADRO = ClassePersonaggioImmagine.getImmagine(ClassePersonaggio.LADRO).getHeight(null);

    private final int width;
    private final int height;
    private final DoomdarkFont font = DoomdarkFontMedium.getInstance();
    private final int fontHeight = font.getHeight();
    private final DoomdarkFont fontSmall = DoomdarkFontSmall.getInstance();

    private final int leftBoxX;
    private final int leftBoxLimit;
    private final int centerBoxX;
    private final int centerBoxLimit;
    private final int rightBoxX;
    private final int rightBoxLimit;
    private final int boxY;

    private AutomaInventario automa;
    int offsetYLeftBox = 0;
    int offsetYBoxPersonaggio = 0;
    int offsetYRightBox = 0;

    // Posizione del mouse, per evidenziare in bianco l'artefatto sotto il cursore.
    // -1 significa "cursore fuori dalla finestra".
    private int mouseX = -1;
    private int mouseY = -1;

    DisplayableCanvasInventario(int width, int height) {
        this.width = width;
        this.height = height;

        leftBoxX = SPACING + DIMENSIONE_BORDO_INTERNO;
        leftBoxLimit = leftBoxX + corniceInventarioWidth - DIMENSIONE_BORDO_INTERNO;

        centerBoxX = SPACING + corniceInventarioWidth + SPACING;
        centerBoxLimit = width - SPACING - corniceInventarioWidth - SPACING;

        rightBoxX = width - SPACING - corniceInventarioWidth + DIMENSIONE_BORDO_INTERNO;
        rightBoxLimit = width - SPACING - DIMENSIONE_BORDO_INTERNO;

        boxY = SPACING + DIMENSIONE_BORDO_INTERNO;
    }

    void impostaAutoma(AutomaInventario automa) {
        this.automa = automa;
    }

    void disegnaInventario(Graphics2D graphics) {

        graphics.drawImage(ImageCache.corniceInventario, SPACING, SPACING, null);
        graphics.drawImage(ImageCache.corniceInventario, width - SPACING - corniceInventarioWidth, SPACING, null);

        if (automa == null) {
            return;
        }

        Personaggio p = automa.getPersonaggio();

        final int SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI = 20;

        Image doomdark;
        int y = SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI;
        DoomdarkColorModel.Color coloreTestata = DoomdarkColorModel.Color.LIGHT_GRAY;

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
        ComponenteScorrevole<TipoAttributo> componenteScorrevole = new ComponenteScorrevole<>(
                width - 2 * corniceInventarioWidth - 4 * SPACING, 10, 2);

        DoomdarkColorModel.Color colore;

        colore = DoomdarkColorModel.Color.LIGHT_GRAY;

        creaNodo(componenteScorrevole, colore, TipoAttributo.FORZA, p.getForza());
        creaNodo(componenteScorrevole, colore, TipoAttributo.DESTREZZA, p.getDestrezza());
        creaNodo(componenteScorrevole, colore, TipoAttributo.COSTITUZIONE, p.getCostituzione());
        creaNodo(componenteScorrevole, colore, TipoAttributo.INTELLIGENZA, p.getIntelligenza());
        creaNodo(componenteScorrevole, colore, TipoAttributo.SAGGEZZA, p.getSaggezza());
        creaNodo(componenteScorrevole, colore, TipoAttributo.CARISMA, p.getCarisma());
        creaNodo(componenteScorrevole, colore, TipoAttributo.FORTUNA, p.getFortuna());
        creaNodo(componenteScorrevole, colore, TipoAttributo.NUMERO_BERSAGLI, p.getBersagli());
        creaNodo(componenteScorrevole, colore, TipoAttributo.RIGENERAZIONE_SALUTE, p.getRigenerazioneSalute());
        creaNodo(componenteScorrevole, colore, TipoAttributo.RIGENERAZIONE_MAGIA, p.getRigenerazioneMagia());

        colore = DoomdarkColorModel.Color.MEDIUM_GRAY;

        creaNodo(componenteScorrevole, colore, TipoAttributo.CARICO_MASSIMO, p.getCaricoMassimo());
        creaNodo(componenteScorrevole, colore, TipoAttributo.CRITICO, p.getCritico());
        creaNodo(componenteScorrevole, colore, TipoAttributo.PRECISIONE, p.getPrecisione());
        creaNodo(componenteScorrevole, colore, TipoAttributo.VELOCITA, p.getVelocita());
        creaNodo(componenteScorrevole, colore, TipoAttributo.FURTIVITA, p.getFurtivita());
        creaNodo(componenteScorrevole, colore, TipoAttributo.PARATA, p.getParata());
        creaNodo(componenteScorrevole, colore, TipoAttributo.RESISTENZA_MAGICA, p.getResistenzaMagica());
        creaNodo(componenteScorrevole, colore, TipoAttributo.PERCEZIONE, p.getPercezione());
        creaNodo(componenteScorrevole, colore, TipoAttributo.SOGGEZIONE, p.getSoggezione());
        creaNodo(componenteScorrevole, colore, TipoAttributo.FURIA, p.getFuria());
        creaNodo(componenteScorrevole, colore, TipoAttributo.CORAGGIO, p.getCoraggio());
        creaNodo(componenteScorrevole, colore, TipoAttributo.VALORE, p.getValore());

        offsetYBoxPersonaggio = componenteScorrevole.limitaOffset(height - y, offsetYBoxPersonaggio);
        Image image = componenteScorrevole.produci(height - y, offsetYBoxPersonaggio);
        graphics.drawImage(image, corniceInventarioWidth + 2 * SPACING, y, null);

        // Inventario personaggio
        offsetYLeftBox = disegnaElenco(graphics, new ArrayList<>(p.getInventario()), leftBoxX, offsetYLeftBox);
        // Inventario gruppo
        offsetYRightBox = disegnaElenco(graphics, automa.getArtefattiDisponibili(), rightBoxX, offsetYRightBox);
    }

    private void creaNodo(ComponenteScorrevole<TipoAttributo> componenteScorrevole, DoomdarkColorModel.Color colore,
                          TipoAttributo tipoAttributo, double valoreAttributo) {
        componenteScorrevole.creaNodo(
                tipoAttributo.getNome(), font, colore,
                String.valueOf((int)valoreAttributo), font, colore,
                tipoAttributo.getDescrizione(), fontSmall, colore,
                null, tipoAttributo);
    }

    private void disegna(TipoAttributo attributo, int valore, Graphics2D graphics, int y, DoomdarkColorModel.Color colore) {
        Image i = ImageCache.get(attributo.getNome(), colore);
        graphics.drawImage(i, centerBoxX, y, null);
        i = ImageCache.get(valore, colore);
        graphics.drawImage(i, centerBoxLimit - i.getWidth(null), y, null);
    }

    private int disegnaElenco(Graphics2D graphics, List<Artefatto> artefatti, int x, int offset) {

        Artefatto evidenziato = trovaArtefatto(artefatti, x, offset, mouseX, mouseY);
        ComponenteScorrevole<Artefatto> componenteScorrevole = costruisciComponenteScorrevoleArtefatti(artefatti, evidenziato);

        int nuovoOffset = componenteScorrevole.limitaOffset(ALTEZZA_DISPONIBILE_IN_RIQUADRO_INVENTARIO, offset);
        Image image = componenteScorrevole.produci(ALTEZZA_DISPONIBILE_IN_RIQUADRO_INVENTARIO, nuovoOffset);
        graphics.drawImage(image, x + SPACING, DIMENSIONE_BORDO_INTERNO + 2 * SPACING, null);

        return nuovoOffset;
    }

    /**
     * L'albero viene ricostruito a ogni disegno e a ogni click. L'artefatto passato in
     * evidenziato (se non null) viene disegnato in bianco invece che in grigio chiaro.
     */
    private ComponenteScorrevole<Artefatto> costruisciComponenteScorrevoleArtefatti(List<Artefatto> artefatti, Artefatto evidenziato) {

        ComponenteScorrevole<Artefatto> componenteScorrevole = new ComponenteScorrevole<>(
                LARGHEZZA_DISPONIBILE_IN_RIQUADRO_INVENTARIO, 10, 2);

        List<Artefatto> artefattiDaDisegnare = ordinaArtifattiDaDisegnare(artefatti);

        SupertipoArtefatto supertipoPrecedente = null;
        for (Artefatto artefatto : artefattiDaDisegnare) {

            if (supertipoPrecedente != artefatto.getTipo().getSupertipo()) {
                supertipoPrecedente = artefatto.getTipo().getSupertipo();
                componenteScorrevole.creaNodo(
                        null, null, null,
                        null, null, null,
                        null, null, null,
                        getImmagineSupertipo(supertipoPrecedente), null);
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

            String nome = artefatto.getNome();
            nome = nome.substring(0, 1).toUpperCase() + nome.substring(1);
            ComponenteScorrevole<Artefatto>.Nodo nodo = componenteScorrevole.creaNodo(
                    nome, font, colore,
                    artefatto.getTipo().getDescrizione(), fontSmall, colore,
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

    private static List<Artefatto> ordinaArtifattiDaDisegnare(List<Artefatto> artefatti) {
        List<Artefatto> artefattiDaDisegnare = new ArrayList<>(artefatti);
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
    private Artefatto trovaArtefatto(List<Artefatto> artefatti, int boxX, int offset, int x, int y) {
        int xInterno = x - (boxX + SPACING);
        int yInterno = y - (DIMENSIONE_BORDO_INTERNO + 2 * SPACING);
        if (xInterno < 0 || xInterno >= LARGHEZZA_DISPONIBILE_IN_RIQUADRO_INVENTARIO
                || yInterno < 0 || yInterno >= ALTEZZA_DISPONIBILE_IN_RIQUADRO_INVENTARIO) {
            return null;
        }
        return costruisciComponenteScorrevoleArtefatti(artefatti, null).riferimentoTitoloAllaQuota(yInterno + offset);
    }

    @Override
    public void processaMovimento(int x, int y) {
        mouseX = x;
        mouseY = y;
    }

    @Override
    public void processaUscita(int x, int y) {
        mouseX = -1;
        mouseY = -1;
    }

    @Override
    public void processaClick(int x, int y, Tasto tasto) {
        if (tasto != Tasto.SINISTRO || automa == null) {
            return;
        }
        List<Artefatto> inventarioPersonaggio = new ArrayList<>(automa.getPersonaggio().getInventario());
        Artefatto artefatto = trovaArtefatto(inventarioPersonaggio, leftBoxX, offsetYLeftBox, x, y);
        if (artefatto == null) {
            artefatto = trovaArtefatto(automa.getArtefattiDisponibili(), rightBoxX, offsetYRightBox, x, y);
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

    @Override
    public void processaDoppioClick(int x, int y, Tasto tasto) {
        if (automa == null) {
            return;
        }
        List<Artefatto> inventarioPersonaggio = new ArrayList<>(automa.getPersonaggio().getInventario());
        Artefatto artefatto = trovaArtefatto(inventarioPersonaggio, leftBoxX, offsetYLeftBox, x, y);
        if (artefatto != null) {
            automa.spostaNelPool(artefatto);
            return;
        }
        List<Artefatto> disponibili = automa.getArtefattiDisponibili();
        artefatto = trovaArtefatto(disponibili, rightBoxX, offsetYRightBox, x, y);
        if (artefatto != null) {
            automa.spostaNelPersonaggio(artefatto);
        }
    }

    @Override
    public void processaRotella(int x, int y, int numeroRotazioni, MovimentoRotella movimentoRotella) {
        if (movimentoRotella == MovimentoRotella.SU) {
            if (x >= leftBoxX && x < leftBoxLimit) {
                offsetYLeftBox = Math.max(0, offsetYLeftBox - numeroRotazioni * PASSO_SCORRIMENTO);
            } else if (x >= centerBoxX && x < centerBoxLimit) {
                offsetYBoxPersonaggio = Math.max(0, offsetYBoxPersonaggio - numeroRotazioni * PASSO_SCORRIMENTO);
            } else if (x >= rightBoxX && x < rightBoxLimit) {
                offsetYRightBox = Math.max(0, offsetYRightBox - numeroRotazioni * PASSO_SCORRIMENTO);
            }
        } else if (movimentoRotella == MovimentoRotella.GIU) {
            if (x >= leftBoxX && x < leftBoxLimit) {
                offsetYLeftBox += numeroRotazioni * PASSO_SCORRIMENTO;
            } else if (x >= centerBoxX && x < centerBoxLimit) {
                offsetYBoxPersonaggio += numeroRotazioni * PASSO_SCORRIMENTO;
            } else if (x >= rightBoxX && x < rightBoxLimit) {
                offsetYRightBox += numeroRotazioni * PASSO_SCORRIMENTO;
            }
        }
    }

    private Image getImmagineSupertipo(SupertipoArtefatto supertipo) {
        switch (supertipo) {
            case ARMA:
                return ImageCache.separatoreArmi;
            case ARMATURA:
                return ImageCache.separatoreArmature;
            case SCUDO:
                return ImageCache.separatoreScudi;
            case ALTRO:
                return ImageCache.separatoreNinnoli;
            default:
                throw new IllegalArgumentException("SupertipoArtefatto senza immagine associata");
        }
    }
}
