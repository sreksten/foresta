package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.oggetti.ClassiOggetto;

import java.awt.image.BufferedImage;

/**
 * Le immagini degli oggetti che si trovano nelle locazioni. Stanno qui e non in ClassiOggetto perché il motore
 * non deve caricare immagini: così si può usare senza schermo, per esempio nei test headless.
 */
public enum ClassiOggettoImmagine {

    ANELLO(ClassiOggetto.ANELLO, "oggetti/Anello.gif"),
    COFANO(ClassiOggetto.COFANO, "oggetti/Cofano.gif"),
    CORONA(ClassiOggetto.CORONA, "oggetti/Corona.gif"),
    GEMMA(ClassiOggetto.GEMMA, "oggetti/Gemma.gif"),
    MONETA(ClassiOggetto.MONETA, "oggetti/Moneta.gif"),
    SCUDO(ClassiOggetto.SCUDO, "oggetti/Scudo.gif"),
    SPADA(ClassiOggetto.SPADA, "oggetti/Spada.gif"),
    // TODO manca l'immagine "oggetti/Elmo.gif": finché non c'è, l'elmo non va tra gli oggetti delle locazioni
    ELMO(ClassiOggetto.ELMO, null),
    // TODO manca l'immagine "oggetti/Armatura.gif": finché non c'è, l'armatura non va tra gli oggetti delle locazioni
    ARMATURA(ClassiOggetto.ARMATURA, null),
    // Gli artefatti non si mostrano nelle locazioni
    ARTEFATTO(ClassiOggetto.ARTEFATTO, null);

    private final ClassiOggetto classeOggetto;
    private final BufferedImage immagine;

    ClassiOggettoImmagine(ClassiOggetto classeOggetto, String nomeRisorsa) {
        this.classeOggetto = classeOggetto;
        this.immagine = BufferedImageBuilder.buildBufferedImage(nomeRisorsa);
    }

    public static ClassiOggettoImmagine getClassiOggettoImmagine(ClassiOggetto classeOggetto) {
        for (ClassiOggettoImmagine classiOggettoImmagine : values()) {
            if (classiOggettoImmagine.classeOggetto == classeOggetto) {
                return classiOggettoImmagine;
            }
        }
        throw new IllegalArgumentException("ClassiOggettoImmagine not found for ClassiOggetto: " + classeOggetto);
    }

    public static BufferedImage getImmagine(ClassiOggetto classeOggetto) {
        return getClassiOggettoImmagine(classeOggetto).immagine;
    }

}
