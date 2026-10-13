package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.tipi.TipoOggetto;

import java.awt.image.BufferedImage;

/**
 * Le immagini degli oggetti che si trovano nelle locazioni. Stanno qui e non in TipoOggetto perché il motore
 * non deve caricare immagini: così si può usare senza schermo, per esempio nei test headless.
 */
public enum ClassiOggettoImmagine {

    ANELLO(TipoOggetto.ANELLO, "oggetti/Anello.gif"),
    COFANO(TipoOggetto.COFANO, "oggetti/Cofano.gif"),
    CORONA(TipoOggetto.CORONA, "oggetti/Corona.gif"),
    PIETRA_PREZIOSA(TipoOggetto.PIETRA_PREZIOSA, "oggetti/PietraPreziosa.gif"),
    MONETA(TipoOggetto.MONETA, "oggetti/Moneta.gif"),
    SCUDO(TipoOggetto.SCUDO, "oggetti/Scudo.gif"),
    SPADA(TipoOggetto.SPADA, "oggetti/Spada.gif"),
    SPADONE(TipoOggetto.SPADONE, "oggetti/Spadone.gif"),
    ELMO(TipoOggetto.ELMO, "oggetti/Elmo.gif"),
    // Per ora una copia dell'elmo, da ridisegnare
    MASCHERA(TipoOggetto.MASCHERA, "oggetti/Maschera.gif"),
    ARMATURA(TipoOggetto.ARMATURA, "oggetti/Armatura.gif"),
    SCHINIERI(TipoOggetto.SCHINIERI, "oggetti/Schinieri.gif"),
    // Per ora un sacchetto generico per tutti gli oggetti delle missioni
    OGGETTO_MISSIONE(TipoOggetto.OGGETTO_MISSIONE, "oggetti/OggettoMissione.gif"),
    // Gli artefatti non si mostrano nelle locazioni
    ARTEFATTO(TipoOggetto.ARTEFATTO, null);

    private final TipoOggetto tipoOggetto;
    private final BufferedImage immagine;

    ClassiOggettoImmagine(TipoOggetto tipoOggetto, String nomeRisorsa) {
        this.tipoOggetto = tipoOggetto;
        this.immagine = BufferedImageBuilder.buildBufferedImage(nomeRisorsa, LivelloDiZoom.valore());
    }

    public static ClassiOggettoImmagine getClassiOggettoImmagine(TipoOggetto tipoOggetto) {
        for (ClassiOggettoImmagine classiOggettoImmagine : values()) {
            if (classiOggettoImmagine.tipoOggetto == tipoOggetto) {
                return classiOggettoImmagine;
            }
        }
        throw new IllegalArgumentException("ClassiOggettoImmagine not found for TipoOggetto: " + tipoOggetto);
    }

    public static BufferedImage getImmagine(TipoOggetto tipoOggetto) {
        return getClassiOggettoImmagine(tipoOggetto).immagine;
    }

}
