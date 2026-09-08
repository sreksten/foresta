package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.personaggi.ClassePersonaggio;

import java.awt.image.BufferedImage;

/**
 *
 * @author Stefano Reksten
 */
public enum ClassePersonaggioImmagine {

    ARPIA(ClassePersonaggio.ARPIA, "personaggi/Arpia.gif"),
    BARDO(ClassePersonaggio.BARDO, "personaggi/Bardo.gif"),
    CANTASTORIE(ClassePersonaggio.CANTASTORIE, "personaggi/Cantastorie.gif"),
    CENTAURO(ClassePersonaggio.CENTAURO, "personaggi/Centauro.gif"),
    CHIMERA(ClassePersonaggio.CHIMERA, "personaggi/Chimera.gif"),
    CHIMERA_DRAGO(ClassePersonaggio.CHIMERA_DRAGO, "personaggi/ChimeraDrago.gif"),
    DRAGO(ClassePersonaggio.DRAGO, "personaggi/Drago.gif"),
    ELFA(ClassePersonaggio.ELFA, "personaggi/Elfa.gif"),
    ELFO(ClassePersonaggio.ELFO, "personaggi/Elfo.gif"),
    EREMITA(ClassePersonaggio.EREMITA, "personaggi/Eremita.gif"),
    FANTASMA(ClassePersonaggio.FANTASMA, "personaggi/Fantasma.gif"),
    FOLLETTO(ClassePersonaggio.FOLLETTO, "personaggi/Folletto.gif"),
    GARGOYLE(ClassePersonaggio.GARGOYLE, "personaggi/Gargoyle.gif"),
    GIGANTE(ClassePersonaggio.GIGANTE, "personaggi/Gigante.gif"),
    GOBLIN(ClassePersonaggio.GOBLIN, "personaggi/Goblin.gif"),
    GUERRIERA(ClassePersonaggio.GUERRIERA, "personaggi/Guerriera.gif"),
    GUERRIERO(ClassePersonaggio.GUERRIERO, "personaggi/Guerriero.gif"),
    HOBGOBLIN(ClassePersonaggio.HOBGOBLIN, "personaggi/Hobgoblin.gif"),
    IDRA(ClassePersonaggio.IDRA, "personaggi/Idra.gif"),
    LADRA(ClassePersonaggio.LADRA, "personaggi/Ladra.gif"),
    LADRO(ClassePersonaggio.LADRO, "personaggi/Ladro.gif"),
    LICH(ClassePersonaggio.LICH, "personaggi/Lich.gif"),
    MAGA(ClassePersonaggio.MAGA, "personaggi/Maga.gif"),
    MAGO(ClassePersonaggio.MAGO, "personaggi/Mago.gif"),
    MINOTAURO(ClassePersonaggio.MINOTAURO, "personaggi/Minotauro.gif"),
    MINOTAURO_GIGANTE(ClassePersonaggio.MINOTAURO_GIGANTE, "personaggi/MinotauroGigante.gif"),
    OMBRAFIAMMA(ClassePersonaggio.OMBRAFIAMMA, "personaggi/OmbraFiamma.gif"),
    OMBRA_NERA(ClassePersonaggio.OMBRA_NERA, "personaggi/OmbraNera.gif"),
    SCHELETRO(ClassePersonaggio.SCHELETRO, "personaggi/Scheletro.gif"),
    SPETTRO(ClassePersonaggio.SPETTRO, "personaggi/Spettro.gif"),
    SPIRITO(ClassePersonaggio.SPIRITO, "personaggi/Spirito.gif"),
    STREGA(ClassePersonaggio.STREGA, "personaggi/Strega.gif"),
    TITANO(ClassePersonaggio.TITANO, "personaggi/Titano.gif"),
    TROLL(ClassePersonaggio.TROLL, "personaggi/Troll.gif"),
    VIVERNA(ClassePersonaggio.VIVERNA, "personaggi/Viverna.gif");

    private final ClassePersonaggio classePersonaggio;
    private final BufferedImage immagine;

    ClassePersonaggioImmagine(ClassePersonaggio classePersonaggio, String nomeRisorsa) {
        this.classePersonaggio = classePersonaggio;
        this.immagine = BufferedImageBuilder.buildBufferedImage(nomeRisorsa);
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

}
