package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.awt.image.BufferedImage;

/**
 *
 * @author Stefano Reksten
 */
public enum ClassePersonaggioImmagine {

    ARPIA(TipoPersonaggio.ARPIA, "personaggi/Arpia.gif"),
    BARDO(TipoPersonaggio.BARDO, "personaggi/Bardo.gif", "icone/Bardo-nobordo-piccolo.gif"),
    CANTASTORIE(TipoPersonaggio.CANTASTORIE, "personaggi/Cantastorie.gif", "icone/Cantastorie-nobordo-piccolo.gif"),
    CENTAURO(TipoPersonaggio.CENTAURO, "personaggi/Centauro.gif", "icone/Centauro-nobordo-piccolo.gif"),
    CHIMERA(TipoPersonaggio.CHIMERA, "personaggi/Chimera.gif"),
    CHIMERA_DRAGO(TipoPersonaggio.CHIMERA_DRAGO, "personaggi/ChimeraDrago.gif"),
    DRAGO(TipoPersonaggio.DRAGO, "personaggi/Drago.gif"),
    ELFA(TipoPersonaggio.ELFA, "personaggi/Elfa.gif", "icone/Elfa-nobordo-piccolo.gif"),
    ELFO(TipoPersonaggio.ELFO, "personaggi/Elfo.gif", "icone/Elfo-nobordo-piccolo.gif"),
    EREMITA(TipoPersonaggio.EREMITA, "personaggi/Eremita.gif", "icone/Eremita-nobordo-piccolo.gif"),
    FANTASMA(TipoPersonaggio.FANTASMA, "personaggi/Fantasma.gif"),
    FOLLETTO(TipoPersonaggio.FOLLETTO, "personaggi/Folletto.gif"),
    GARGOYLE(TipoPersonaggio.GARGOYLE, "personaggi/Gargoyle.gif"),
    GIGANTE(TipoPersonaggio.GIGANTE, "personaggi/Gigante.gif", "icone/Gigante-nobordo-piccolo.gif"),
    GOBLIN(TipoPersonaggio.GOBLIN, "personaggi/Goblin.gif", "icone/Goblin-nobordo-piccolo.gif"),
    GUERRIERA(TipoPersonaggio.GUERRIERA, "personaggi/Guerriera.gif", "icone/Guerriera-nobordo-piccolo.gif"),
    GUERRIERO(TipoPersonaggio.GUERRIERO, "personaggi/Guerriero.gif", "icone/Guerriero-nobordo-piccolo.gif"),
    HOBGOBLIN(TipoPersonaggio.HOBGOBLIN, "personaggi/Hobgoblin.gif", "icone/Hobgoblin-nobordo-piccolo.gif"),
    IDRA(TipoPersonaggio.IDRA, "personaggi/Idra.gif"),
    LADRA(TipoPersonaggio.LADRA, "personaggi/Ladra.gif", "icone/Ladra-nobordo-piccolo.gif"),
    LADRO(TipoPersonaggio.LADRO, "personaggi/Ladro.gif", "icone/Ladro-nobordo-piccolo.gif"),
    LICH(TipoPersonaggio.LICH, "personaggi/Lich.gif"),
    MAGA(TipoPersonaggio.MAGA, "personaggi/Maga.gif", "icone/Maga-nobordo-piccolo.gif"),
    MAGO(TipoPersonaggio.MAGO, "personaggi/Mago.gif", "icone/Mago-nobordo-piccolo.gif"),
    MINOTAURO(TipoPersonaggio.MINOTAURO, "personaggi/Minotauro.gif", "icone/Minotauro-nobordo-piccolo.gif"),
    MINOTAURO_GIGANTE(TipoPersonaggio.MINOTAURO_GIGANTE, "personaggi/MinotauroGigante.gif"),
    OMBRAFIAMMA(TipoPersonaggio.OMBRAFIAMMA, "personaggi/OmbraFiamma.gif", "icone/OmbraFiamma-nobordo-piccolo.gif"),
    OMBRA_NERA(TipoPersonaggio.OMBRA_NERA, "personaggi/OmbraNera.gif"),
    SCHELETRO(TipoPersonaggio.SCHELETRO, "personaggi/Scheletro.gif"),
    SPETTRO(TipoPersonaggio.SPETTRO, "personaggi/Spettro.gif"),
    SPIRITO(TipoPersonaggio.SPIRITO, "personaggi/Spirito.gif"),
    STREGA(TipoPersonaggio.STREGA, "personaggi/Strega.gif"),
    TITANO(TipoPersonaggio.TITANO, "personaggi/Titano.gif", "icone/Titano-nobordo-piccolo.gif"),
    TROLL(TipoPersonaggio.TROLL, "personaggi/Troll.gif"),
    VIVERNA(TipoPersonaggio.VIVERNA, "personaggi/Viverna.gif"),
    // Per ora ha l'immagine del bardo (vedi todo.md)
    VIANDANTE(TipoPersonaggio.VIANDANTE, "personaggi/Bardo.gif", "icone/Bardo-nobordo-piccolo.gif");

    private final TipoPersonaggio classePersonaggio;
    private final BufferedImage immagine;
    private final BufferedImage icona;

    ClassePersonaggioImmagine(TipoPersonaggio classePersonaggio, String nomeRisorsa) {
        this.classePersonaggio = classePersonaggio;
        this.immagine = BufferedImageBuilder.buildBufferedImage(nomeRisorsa);
        this.icona = null;
    }

    ClassePersonaggioImmagine(TipoPersonaggio classePersonaggio, String nomeRisorsa, String nomeIcona) {
        this.classePersonaggio = classePersonaggio;
        this.immagine = BufferedImageBuilder.buildBufferedImage(nomeRisorsa);
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
