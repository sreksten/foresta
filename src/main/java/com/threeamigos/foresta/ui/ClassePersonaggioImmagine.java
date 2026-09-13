package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.personaggi.ClassePersonaggio;

import java.awt.image.BufferedImage;

/**
 *
 * @author Stefano Reksten
 */
public enum ClassePersonaggioImmagine {

    ARPIA(ClassePersonaggio.ARPIA, "personaggi/Arpia.gif"),
    BARDO(ClassePersonaggio.BARDO, "personaggi/Bardo.gif", "icone/Bardo-nobordo-piccolo.gif"),
    CANTASTORIE(ClassePersonaggio.CANTASTORIE, "personaggi/Cantastorie.gif", "icone/Cantastorie-nobordo-piccolo.gif"),
    CENTAURO(ClassePersonaggio.CENTAURO, "personaggi/Centauro.gif", "icone/Centauro-nobordo-piccolo.gif"),
    CHIMERA(ClassePersonaggio.CHIMERA, "personaggi/Chimera.gif"),
    CHIMERA_DRAGO(ClassePersonaggio.CHIMERA_DRAGO, "personaggi/ChimeraDrago.gif"),
    DRAGO(ClassePersonaggio.DRAGO, "personaggi/Drago.gif"),
    ELFA(ClassePersonaggio.ELFA, "personaggi/Elfa.gif", "icone/Elfa-nobordo-piccolo.gif"),
    ELFO(ClassePersonaggio.ELFO, "personaggi/Elfo.gif", "icone/Elfo-nobordo-piccolo.gif"),
    EREMITA(ClassePersonaggio.EREMITA, "personaggi/Eremita.gif", "icone/Eremita-nobordo-piccolo.gif"),
    FANTASMA(ClassePersonaggio.FANTASMA, "personaggi/Fantasma.gif"),
    FOLLETTO(ClassePersonaggio.FOLLETTO, "personaggi/Folletto.gif"),
    GARGOYLE(ClassePersonaggio.GARGOYLE, "personaggi/Gargoyle.gif"),
    GIGANTE(ClassePersonaggio.GIGANTE, "personaggi/Gigante.gif", "icone/Gigante-nobordo-piccolo.gif"),
    GOBLIN(ClassePersonaggio.GOBLIN, "personaggi/Goblin.gif", "icone/Goblin-nobordo-piccolo.gif"),
    GUERRIERA(ClassePersonaggio.GUERRIERA, "personaggi/Guerriera.gif", "icone/Guerriera-nobordo-piccolo.gif"),
    GUERRIERO(ClassePersonaggio.GUERRIERO, "personaggi/Guerriero.gif", "icone/Guerriero-nobordo-piccolo.gif"),
    HOBGOBLIN(ClassePersonaggio.HOBGOBLIN, "personaggi/Hobgoblin.gif", "icone/Hobgoblin-nobordo-piccolo.gif"),
    IDRA(ClassePersonaggio.IDRA, "personaggi/Idra.gif"),
    LADRA(ClassePersonaggio.LADRA, "personaggi/Ladra.gif", "icone/Ladra-nobordo-piccolo.gif"),
    LADRO(ClassePersonaggio.LADRO, "personaggi/Ladro.gif", "icone/Ladro-nobordo-piccolo.gif"),
    LICH(ClassePersonaggio.LICH, "personaggi/Lich.gif"),
    MAGA(ClassePersonaggio.MAGA, "personaggi/Maga.gif", "icone/Maga-nobordo-piccolo.gif"),
    MAGO(ClassePersonaggio.MAGO, "personaggi/Mago.gif", "icone/Mago-nobordo-piccolo.gif"),
    MINOTAURO(ClassePersonaggio.MINOTAURO, "personaggi/Minotauro.gif", "icone/Minotauro-nobordo-piccolo.gif"),
    MINOTAURO_GIGANTE(ClassePersonaggio.MINOTAURO_GIGANTE, "personaggi/MinotauroGigante.gif"),
    OMBRAFIAMMA(ClassePersonaggio.OMBRAFIAMMA, "personaggi/OmbraFiamma.gif", "icone/OmbraFiamma-nobordo-piccolo.gif"),
    OMBRA_NERA(ClassePersonaggio.OMBRA_NERA, "personaggi/OmbraNera.gif"),
    SCHELETRO(ClassePersonaggio.SCHELETRO, "personaggi/Scheletro.gif"),
    SPETTRO(ClassePersonaggio.SPETTRO, "personaggi/Spettro.gif"),
    SPIRITO(ClassePersonaggio.SPIRITO, "personaggi/Spirito.gif"),
    STREGA(ClassePersonaggio.STREGA, "personaggi/Strega.gif"),
    TITANO(ClassePersonaggio.TITANO, "personaggi/Titano.gif", "icone/Titano-nobordo-piccolo.gif"),
    TROLL(ClassePersonaggio.TROLL, "personaggi/Troll.gif"),
    VIVERNA(ClassePersonaggio.VIVERNA, "personaggi/Viverna.gif");

    private final ClassePersonaggio classePersonaggio;
    private final BufferedImage immagine;
    private final BufferedImage icona;

    ClassePersonaggioImmagine(ClassePersonaggio classePersonaggio, String nomeRisorsa) {
        this.classePersonaggio = classePersonaggio;
        this.immagine = BufferedImageBuilder.buildBufferedImage(nomeRisorsa);
        this.icona = null;
    }

    ClassePersonaggioImmagine(ClassePersonaggio classePersonaggio, String nomeRisorsa, String nomeIcona) {
        this.classePersonaggio = classePersonaggio;
        this.immagine = BufferedImageBuilder.buildBufferedImage(nomeRisorsa);
        this.icona = BufferedImageBuilder.buildBufferedImage(nomeIcona);
    }

    public static ClassePersonaggioImmagine getClassePersonaggioImmagine(ClassePersonaggio classePersonaggio) {
        for (ClassePersonaggioImmagine classePersonaggioImmagine : values()) {
            if (classePersonaggioImmagine.classePersonaggio == classePersonaggio) {
                return classePersonaggioImmagine;
            }
        }
        throw new IllegalArgumentException("ClassePersonaggioImmagine not found for ClassePersonaggio: " + classePersonaggio);
    }

    public static BufferedImage getImmagine(ClassePersonaggio classePersonaggio) {
        return getClassePersonaggioImmagine(classePersonaggio).immagine;
    }

    public static BufferedImage getIcona(ClassePersonaggio classePersonaggio) {
        return getClassePersonaggioImmagine(classePersonaggio).icona;
    }

}
