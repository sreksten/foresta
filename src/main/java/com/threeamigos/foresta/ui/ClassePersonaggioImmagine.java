package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.awt.image.BufferedImage;

/**
 *
 * @author Stefano Reksten
 */
public enum ClassePersonaggioImmagine {

    ARPIA(TipoPersonaggio.ARPIA, "personaggi/Arpia.gif"),
    BARDO(TipoPersonaggio.BARDO, "personaggi/Bardo.gif", "icone/Bardo-nobordo.gif"),
    CANTASTORIE(TipoPersonaggio.CANTASTORIE, "personaggi/Cantastorie.gif", "icone/Cantastorie-nobordo.gif"),
    CENTAURO(TipoPersonaggio.CENTAURO, "personaggi/Centauro.gif", "icone/Centauro-nobordo.gif"),
    CHIMERA(TipoPersonaggio.CHIMERA, "personaggi/Chimera.gif"),
    CHIMERA_DRAGO(TipoPersonaggio.CHIMERA_DRAGO, "personaggi/ChimeraDrago.gif"),
    DRAGO(TipoPersonaggio.DRAGO, "personaggi/Drago.gif"),
    ELFA(TipoPersonaggio.ELFA, "personaggi/Elfa.gif", "icone/Elfa-nobordo.gif"),
    ELFO(TipoPersonaggio.ELFO, "personaggi/Elfo.gif", "icone/Elfo-nobordo.gif"),
    EREMITA(TipoPersonaggio.EREMITA, "personaggi/Eremita.gif", "icone/Eremita-nobordo.gif"),
    FANTASMA(TipoPersonaggio.FANTASMA, "personaggi/Fantasma.gif"),
    FOLLETTO(TipoPersonaggio.FOLLETTO, "personaggi/Folletto.gif"),
    GARGOYLE(TipoPersonaggio.GARGOYLE, "personaggi/Gargoyle.gif"),
    GIGANTE(TipoPersonaggio.GIGANTE, "personaggi/Gigante.gif", "icone/Gigante-nobordo.gif"),
    GOBLIN(TipoPersonaggio.GOBLIN, "personaggi/Goblin.gif", "icone/Goblin-nobordo.gif"),
    GUERRIERA(TipoPersonaggio.GUERRIERA, "personaggi/Guerriera.gif", "icone/Guerriera-nobordo.gif"),
    GUERRIERO(TipoPersonaggio.GUERRIERO, "personaggi/Guerriero.gif", "icone/Guerriero-nobordo.gif"),
    HOBGOBLIN(TipoPersonaggio.HOBGOBLIN, "personaggi/Hobgoblin.gif", "icone/Hobgoblin-nobordo.gif"),
    IDRA(TipoPersonaggio.IDRA, "personaggi/Idra.gif"),
    LADRA(TipoPersonaggio.LADRA, "personaggi/Ladra.gif", "icone/Ladra-nobordo.gif"),
    LADRO(TipoPersonaggio.LADRO, "personaggi/Ladro.gif", "icone/Ladro-nobordo.gif"),
    LICH(TipoPersonaggio.LICH, "personaggi/Lich.gif"),
    MAGA(TipoPersonaggio.MAGA, "personaggi/Maga.gif", "icone/Maga-nobordo.gif"),
    MAGO(TipoPersonaggio.MAGO, "personaggi/Mago.gif", "icone/Mago-nobordo.gif"),
    MINOTAURO(TipoPersonaggio.MINOTAURO, "personaggi/Minotauro.gif", "icone/Minotauro-nobordo.gif"),
    MINOTAURO_GIGANTE(TipoPersonaggio.MINOTAURO_GIGANTE, "personaggi/MinotauroGigante.gif"),
    OMBRAFIAMMA(TipoPersonaggio.OMBRAFIAMMA, "personaggi/OmbraFiamma.gif", "icone/OmbraFiamma-nobordo.gif"),
    OMBRA_NERA(TipoPersonaggio.OMBRA_NERA, "personaggi/OmbraNera.gif"),
    SCHELETRO(TipoPersonaggio.SCHELETRO, "personaggi/Scheletro.gif"),
    SPETTRO(TipoPersonaggio.SPETTRO, "personaggi/Spettro.gif"),
    SPIRITO(TipoPersonaggio.SPIRITO, "personaggi/Spirito.gif"),
    STREGA(TipoPersonaggio.STREGA, "personaggi/Strega.gif"),
    TITANO(TipoPersonaggio.TITANO, "personaggi/Titano.gif", "icone/Titano-nobordo.gif"),
    TROLL(TipoPersonaggio.TROLL, "personaggi/Troll.gif"),
    VIVERNA(TipoPersonaggio.VIVERNA, "personaggi/Viverna.gif"),
    VIANDANTE(TipoPersonaggio.VIANDANTE, "personaggi/Viandante.gif", "icone/Viandante-nobordo.gif");

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

    public static BufferedImage getIcona(TipoPersonaggio classePersonaggio) {
        return getClassePersonaggioImmagine(classePersonaggio).icona;
    }

}
