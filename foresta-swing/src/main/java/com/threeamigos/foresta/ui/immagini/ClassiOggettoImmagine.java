package com.threeamigos.foresta.ui.immagini;

import com.threeamigos.foresta.artefatti.leggendari.LibroTecnico;
import com.threeamigos.foresta.artefatti.ArtefattoMD;
import com.threeamigos.foresta.artefatti.tipi.AspettoArtefatto;
import com.threeamigos.foresta.tipi.TipoBottino;

import java.awt.image.BufferedImage;
import com.threeamigos.foresta.ui.LivelloDiZoom;

/**
 * Le immagini degli oggetti che si trovano nelle locazioni. Stanno qui e non in TipoBottino perché il motore
 * non deve caricare immagini: così si può usare senza schermo, per esempio nei test headless.
 */
public enum ClassiOggettoImmagine {

    ANELLO(TipoBottino.ANELLO, "oggetti/Anello.gif"),
    COFANO(TipoBottino.COFANO, "oggetti/Cofano.gif"),
    CORONA(TipoBottino.CORONA, "oggetti/Corona.gif"),
    PIETRA_PREZIOSA(TipoBottino.PIETRA_PREZIOSA, "oggetti/PietraPreziosa.gif"),
    MONETA(TipoBottino.MONETA, "oggetti/Moneta.gif"),
    SCUDO(TipoBottino.SCUDO, "oggetti/Scudo.gif"),
    SPADA(TipoBottino.SPADA, "oggetti/Spada.gif"),
    SPADONE(TipoBottino.SPADONE, "oggetti/Spadone.gif"),
    FALCE(TipoBottino.FALCE, "oggetti/FalceDelTristoMietitore.gif"),
    MAZZA(TipoBottino.MAZZA, "oggetti/Mazza.gif"),
    ASCIA(TipoBottino.ASCIA, "oggetti/Ascia.gif"),
    LANCIA(TipoBottino.LANCIA, "oggetti/Lancia.gif"),
    BASTONE_MAGICO(TipoBottino.BASTONE_MAGICO, "oggetti/BastoneMagico.gif"),
    LIBRO_MAGICO(TipoBottino.LIBRO_MAGICO, "oggetti/LibroMagico.gif"),
    ELMO(TipoBottino.ELMO, "oggetti/Elmo.gif"),
    // Per ora una copia dell'elmo, da ridisegnare
    MASCHERA(TipoBottino.MASCHERA, "oggetti/Maschera.gif"),
    ARMATURA(TipoBottino.ARMATURA, "oggetti/Armatura.gif"),
    SCHINIERI(TipoBottino.SCHINIERI, "oggetti/Schinieri.gif"),
    // Non si trova per terra: è l'aspetto dei libri tecnici (vedi ArtefattoMD.getAspetto)
    LIBRO_TECNICO(null, "oggetti/LibroTecnico.gif"),
    // Non si trova per terra: è l'aspetto della spada normalmente nascosta (vedi ArtefattoMD.getAspetto)
    SPADA_NORMALMENTE_NASCOSTA(null, "oggetti/LaSpadaNormalmenteNascosta.gif"),
    // Per ora un sacchetto generico per tutti gli oggetti delle missioni
    OGGETTO_MISSIONE(TipoBottino.OGGETTO_MISSIONE, "oggetti/OggettoMissione.gif"),
    // Gli artefatti non si mostrano nelle locazioni
    ARTEFATTO(TipoBottino.ARTEFATTO, null);

    private final TipoBottino tipoOggetto;
    private final BufferedImage immagine;

    ClassiOggettoImmagine(TipoBottino tipoOggetto, String nomeRisorsa) {
        this.tipoOggetto = tipoOggetto;
        this.immagine = BufferedImageBuilder.buildBufferedImage(nomeRisorsa, LivelloDiZoom.valore());
    }

    public static ClassiOggettoImmagine getClassiOggettoImmagine(TipoBottino tipoOggetto) {
        for (ClassiOggettoImmagine classiOggettoImmagine : values()) {
            if (classiOggettoImmagine.tipoOggetto == tipoOggetto) {
                return classiOggettoImmagine;
            }
        }
        throw new IllegalArgumentException("ClassiOggettoImmagine not found for TipoBottino: " + tipoOggetto);
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
