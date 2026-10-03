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
    PIETRA_PREZIOSA(ClassiOggetto.PIETRA_PREZIOSA, "oggetti/PietraPreziosa.gif"),
    MONETA(ClassiOggetto.MONETA, "oggetti/Moneta.gif"),
    SCUDO(ClassiOggetto.SCUDO, "oggetti/Scudo.gif"),
    SPADA(ClassiOggetto.SPADA, "oggetti/Spada.gif"),
    SPADONE(ClassiOggetto.SPADONE, "oggetti/Spadone.gif"),
    ELMO(ClassiOggetto.ELMO, "oggetti/Elmo.gif"),
    // Per ora una copia dell'elmo, da ridisegnare
    MASCHERA(ClassiOggetto.MASCHERA, "oggetti/Maschera.gif"),
    ARMATURA(ClassiOggetto.ARMATURA, "oggetti/Armatura.gif"),
    SCHINIERI(ClassiOggetto.SCHINIERI, "oggetti/Schinieri.gif"),
    // Per ora un sacchetto generico per tutti gli oggetti delle missioni
    OGGETTO_MISSIONE(ClassiOggetto.OGGETTO_MISSIONE, "oggetti/OggettoMissione.gif"),
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
