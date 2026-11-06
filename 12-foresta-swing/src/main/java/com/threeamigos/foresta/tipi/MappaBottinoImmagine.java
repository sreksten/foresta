package com.threeamigos.foresta.tipi;

import com.threeamigos.foresta.ui.immagini.BufferedImageBuilder;
import com.threeamigos.foresta.ui.LivelloDiZoom;

import java.awt.image.BufferedImage;

/**
 * Le immagini degli oggetti che si trovano nelle locazioni. Stanno qui e non in TipoBottino perché il motore
 * non deve caricare immagini: così si può usare senza schermo, per esempio nei test headless.
 */
public enum MappaBottinoImmagine {

    ANELLO(TipoBottino.ANELLO, "oggetti/Anello.png"),
    COFANO(TipoBottino.COFANO, "oggetti/Cofano.png"),
    CORONA(TipoBottino.CORONA, "oggetti/Corona.png"),
    PIETRA_PREZIOSA(TipoBottino.PIETRA_PREZIOSA, "oggetti/PietraPreziosa.png"),
    MONETA(TipoBottino.MONETA, "oggetti/Moneta.png"),
    SCUDO(TipoBottino.SCUDO, "oggetti/Scudo.png"),
    SPADA(TipoBottino.SPADA, "oggetti/Spada.png"),
    SPADONE(TipoBottino.SPADONE, "oggetti/Spadone.png"),
    FALCE(TipoBottino.FALCE, "oggetti/FalceDelTristoMietitore.png"),
    MAZZA(TipoBottino.MAZZA, "oggetti/Mazza.png"),
    ASCIA(TipoBottino.ASCIA, "oggetti/Ascia.png"),
    LANCIA(TipoBottino.LANCIA, "oggetti/Lancia.png"),
    BASTONE_MAGICO(TipoBottino.BASTONE_MAGICO, "oggetti/BastoneMagico.png"),
    LIBRO_MAGICO(TipoBottino.LIBRO_MAGICO, "oggetti/LibroMagico.png"),
    ELMO(TipoBottino.ELMO, "oggetti/Elmo.png"),
    // Per ora una copia dell'elmo, da ridisegnare
    MASCHERA(TipoBottino.MASCHERA, "oggetti/Maschera.png"),
    ARMATURA(TipoBottino.ARMATURA, "oggetti/Armatura.png"),
    SCHINIERI(TipoBottino.SCHINIERI, "oggetti/Schinieri.png"),
    // Non si trova per terra: è l'aspetto dei libri tecnici (vedi ArtefattoMD.getAspetto)
    LIBRO_TECNICO(null, "oggetti/LibroTecnico.png"),
    // Non si trova per terra: è l'aspetto della spada normalmente nascosta (vedi ArtefattoMD.getAspetto)
    SPADA_NORMALMENTE_NASCOSTA(null, "oggetti/LaSpadaNormalmenteNascosta.png"),
    // Per ora un sacchetto generico per tutti gli oggetti delle missioni
    OGGETTO_MISSIONE(TipoBottino.OGGETTO_MISSIONE, "oggetti/OggettoMissione.png"),
    // Gli artefatti non si mostrano nelle locazioni
    ARTEFATTO(TipoBottino.ARTEFATTO, null);

    private final TipoBottino tipoOggetto;
    private final BufferedImage immagine;

    MappaBottinoImmagine(TipoBottino tipoOggetto, String nomeRisorsa) {
        this.tipoOggetto = tipoOggetto;
        this.immagine = BufferedImageBuilder.buildBufferedImage(nomeRisorsa, LivelloDiZoom.valore());
    }

    public static MappaBottinoImmagine getClassiOggettoImmagine(TipoBottino tipoOggetto) {
        for (MappaBottinoImmagine classiOggettoImmagine : values()) {
            if (classiOggettoImmagine.tipoOggetto == tipoOggetto) {
                return classiOggettoImmagine;
            }
        }
        throw new IllegalArgumentException("MappaBottinoImmagine not found for TipoBottino: " + tipoOggetto);
    }

    public static BufferedImage getImmagine(TipoBottino tipoOggetto) {
        return getClassiOggettoImmagine(tipoOggetto).immagine;
    }

    /**
     * L'immagine di un aspetto speciale di artefatto (i libri tecnici, la spada normalmente nascosta).
     */
    public static BufferedImage getImmagine(AspettoArtefatto aspetto) {
        return valueOf(aspetto.name()).immagine;
    }

}
