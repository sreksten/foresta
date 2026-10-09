package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.intermezzi.Verso;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.awt.image.BufferedImage;

/**
 *
 * @author Stefano Reksten
 */
public enum ClassePersonaggioImmagine {

    ARPIA(TipoPersonaggio.ARPIA, "personaggi/Arpia.gif"),
    BARDO(TipoPersonaggio.BARDO, "personaggi/Bardo.gif", "icone/Bardo.gif"),
    CANTASTORIE(TipoPersonaggio.CANTASTORIE, "personaggi/Cantastorie.gif", "icone/Cantastorie.gif"),
    CENTAURO(TipoPersonaggio.CENTAURO, "personaggi/Centauro.gif", "icone/Centauro.gif"),
    CHIMERA(TipoPersonaggio.CHIMERA, "personaggi/Chimera.gif"),
    CHIMERA_DRAGO(TipoPersonaggio.CHIMERA_DRAGO, "personaggi/ChimeraDrago.gif"),
    DRAGO(TipoPersonaggio.DRAGO, "personaggi/Drago.gif"),
    ELFA(TipoPersonaggio.ELFA, "personaggi/Elfa.gif", "icone/Elfa.gif"),
    ELFO(TipoPersonaggio.ELFO, "personaggi/Elfo.gif", "icone/Elfo.gif"),
    EREMITA(TipoPersonaggio.EREMITA, "personaggi/Eremita.gif", "icone/Eremita.gif"),
    FANTASMA(TipoPersonaggio.FANTASMA, "personaggi/Fantasma.gif"),
    FOLLETTO(TipoPersonaggio.FOLLETTO, "personaggi/Folletto.gif"),
    GARGOYLE(TipoPersonaggio.GARGOYLE, "personaggi/Gargoyle.gif"),
    GIGANTE(TipoPersonaggio.GIGANTE, "personaggi/Gigante.gif", "icone/Gigante.gif"),
    GOBLIN(TipoPersonaggio.GOBLIN, "personaggi/Goblin.gif", "icone/Goblin.gif"),
    GUERRIERA(TipoPersonaggio.GUERRIERA, "personaggi/Guerriera.gif", "icone/Guerriera.gif"),
    GUERRIERO(TipoPersonaggio.GUERRIERO, "personaggi/Guerriero.gif", "icone/Guerriero.gif"),
    HOBGOBLIN(TipoPersonaggio.HOBGOBLIN, "personaggi/Hobgoblin.gif", "icone/Hobgoblin.gif"),
    IDRA(TipoPersonaggio.IDRA, "personaggi/Idra.gif"),
    LADRA(TipoPersonaggio.LADRA, "personaggi/Ladra.gif", "icone/Ladra.gif"),
    LADRO(TipoPersonaggio.LADRO, "personaggi/Ladro.gif", "icone/Ladro.gif"),
    LICH(TipoPersonaggio.LICH, "personaggi/Lich.gif"),
    MAGA(TipoPersonaggio.MAGA, "personaggi/Maga.gif", "icone/Maga.gif"),
    MAGO(TipoPersonaggio.MAGO, "personaggi/Mago.gif", "icone/Mago.gif"),
    SACERDOTESSA(TipoPersonaggio.SACERDOTESSA, "personaggi/Sacerdotessa.gif", "icone/Sacerdotessa.gif"),
    SACERDOTE(TipoPersonaggio.SACERDOTE, "personaggi/Sacerdote.gif", "icone/Sacerdote.gif"),
    MINOTAURO(TipoPersonaggio.MINOTAURO, "personaggi/Minotauro.gif", "icone/Minotauro.gif"),
    MINOTAURO_GIGANTE(TipoPersonaggio.MINOTAURO_GIGANTE, "personaggi/MinotauroGigante.gif"),
    OMBRAFIAMMA(TipoPersonaggio.OMBRAFIAMMA, "personaggi/OmbraFiamma.gif", "icone/OmbraFiamma.gif"),
    OMBRA_NERA(TipoPersonaggio.OMBRA_NERA, "personaggi/OmbraNera.gif"),
    SCHELETRO(TipoPersonaggio.SCHELETRO, "personaggi/Scheletro.gif"),
    SPETTRO(TipoPersonaggio.SPETTRO, "personaggi/Spettro.gif"),
    SPIRITO(TipoPersonaggio.SPIRITO, "personaggi/Spirito.gif"),
    STREGA(TipoPersonaggio.STREGA, "personaggi/Strega.gif"),
    TITANO(TipoPersonaggio.TITANO, "personaggi/Titano.gif", "icone/Titano.gif"),
    TROLL(TipoPersonaggio.TROLL, "personaggi/Troll.gif"),
    VIVERNA(TipoPersonaggio.VIVERNA, "personaggi/Viverna.gif"),
    // I non combattenti: le icone sono provvisorie, copie di quella del Viandante
    LOCANDIERE(TipoPersonaggio.LOCANDIERE, "personaggi/Locandiere.gif", "icone/Locandiere.gif"),
    ARMAIOLO(TipoPersonaggio.ARMAIOLO, "personaggi/Armaiolo.gif", "icone/Armaiolo.gif"),
    ALCHIMISTA(TipoPersonaggio.ALCHIMISTA, "personaggi/Alchimista.gif", "icone/Alchimista.gif"),
    VENDITORE_DI_PERGAMENE(TipoPersonaggio.VENDITORE_DI_PERGAMENE, "personaggi/VenditoreDiPergamene.gif", "icone/VenditoreDiPergamene.gif"),
    INCANTATORE(TipoPersonaggio.INCANTATORE, "personaggi/Incantatore.gif", "icone/Incantatore.gif"),
    MOGLIE_DEL_BARDO(TipoPersonaggio.MOGLIE_DEL_BARDO, "personaggi/MoglieDelBardo.gif", "icone/MoglieDelBardo.gif"),
    VIANDANTE(TipoPersonaggio.VIANDANTE, "personaggi/Viandante.gif", "icone/Viandante.gif"),
    BARDO_LOCANDA(TipoPersonaggio.BARDO_LOCANDA, "personaggi/BardoLocanda.gif", "icone/BardoLocanda.gif"),
    CAPITANO_DELLE_GUARDIE(TipoPersonaggio.CAPITANO_DELLE_GUARDIE, "personaggi/CapitanoDelleGuardie.gif", "icone/CapitanoDelleGuardie.gif");

    private final TipoPersonaggio classePersonaggio;
    private final BufferedImage immagine;
    private final BufferedImage icona;

    ClassePersonaggioImmagine(TipoPersonaggio classePersonaggio, String nomeRisorsa) {
        this.classePersonaggio = classePersonaggio;
        this.immagine = BufferedImageBuilder.buildBufferedImage(nomeRisorsa, LivelloDiZoom.valore());
        this.icona = null;
    }

    ClassePersonaggioImmagine(TipoPersonaggio classePersonaggio, String nomeRisorsa, String nomeIcona) {
        this.classePersonaggio = classePersonaggio;
        this.immagine = BufferedImageBuilder.buildBufferedImage(nomeRisorsa, LivelloDiZoom.valore());
        this.icona = BufferedImageBuilder.buildBufferedImage(nomeIcona);
    }

    public static ClassePersonaggioImmagine getClassePersonaggioImmagine(TipoPersonaggio classePersonaggio) {
        for (ClassePersonaggioImmagine classePersonaggioImmagine : values()) {
            if (classePersonaggioImmagine.classePersonaggio == classePersonaggio) {
                return classePersonaggioImmagine;
            }
        }
        throw new IllegalArgumentException("ClassePersonaggioImmagine not found for TipoPersonaggio: " + classePersonaggio);
    }

    public static BufferedImage getImmagine(TipoPersonaggio classePersonaggio) {
        return getClassePersonaggioImmagine(classePersonaggio).immagine;
    }

    /**
     * L'immagine della classe rivolta verso il verso voluto: quella originale se già guarda da quella parte, altrimenti
     * la sua versione specchiata, compatibile con lo schermo e ricordata da ImageCache (finché serve).
     */
    public static BufferedImage getImmagine(TipoPersonaggio classePersonaggio, Verso versoVoluto) {
        BufferedImage originale = getImmagine(classePersonaggio);
        if (!VersiDeiPersonaggi.serveSpecchiare(classePersonaggio, versoVoluto)) {
            return originale;
        }
        return ImageCache.getImmaginePersonaggio(classePersonaggio, versoVoluto,
                () -> BufferedImageBuilder.ingrandisci(VersiDeiPersonaggi.specchia(originale), 1.0));
    }

    public static BufferedImage getIcona(TipoPersonaggio classePersonaggio) {
        return getClassePersonaggioImmagine(classePersonaggio).icona;
    }

}
