package com.threeamigos.foresta.tipi;

import com.threeamigos.foresta.ui.immagini.BufferedImageBuilder;
import com.threeamigos.foresta.ui.immagini.NomiDelleImmagini;
import com.threeamigos.foresta.ui.LivelloDiZoom;

import java.awt.image.BufferedImage;

/**
 * Le immagini degli oggetti che si trovano nelle locazioni. Stanno qui e non in TipoBottino perché il motore
 * non deve caricare immagini: così si può usare senza schermo, per esempio nei test headless.
 */
public enum MappaBottinoImmagine {

    ANELLO(TipoBottino.ANELLO),
    COFANO(TipoBottino.COFANO),
    CORONA(TipoBottino.CORONA),
    PIETRA_PREZIOSA(TipoBottino.PIETRA_PREZIOSA),
    MONETA(TipoBottino.MONETA),
    SCUDO(TipoBottino.SCUDO),
    SPADA(TipoBottino.SPADA),
    SPADONE(TipoBottino.SPADONE),
    FALCE(TipoBottino.FALCE),
    MAZZA(TipoBottino.MAZZA),
    ASCIA(TipoBottino.ASCIA),
    LANCIA(TipoBottino.LANCIA),
    BASTONE_MAGICO(TipoBottino.BASTONE_MAGICO),
    LIBRO_MAGICO(TipoBottino.LIBRO_MAGICO),
    ELMO(TipoBottino.ELMO),
    // Per ora una copia dell'elmo, da ridisegnare
    MASCHERA(TipoBottino.MASCHERA),
    ARMATURA(TipoBottino.ARMATURA),
    SCHINIERI(TipoBottino.SCHINIERI),
    // Non si trova per terra: è l'aspetto dei libri tecnici (vedi ArtefattoMD.getAspetto)
    LIBRO_TECNICO(null),
    // Non si trova per terra: è l'aspetto della spada normalmente nascosta (vedi ArtefattoMD.getAspetto)
    SPADA_NORMALMENTE_NASCOSTA(null),
    // Per ora un sacchetto generico per tutti gli oggetti delle missioni
    OGGETTO_MISSIONE(TipoBottino.OGGETTO_MISSIONE),
    // Gli artefatti non si mostrano nelle locazioni
    ARTEFATTO(TipoBottino.ARTEFATTO);

    private final TipoBottino tipoOggetto;
    private final BufferedImage immagine;

    MappaBottinoImmagine(TipoBottino tipoOggetto) {
        this.tipoOggetto = tipoOggetto;
        // I nomi delle risorse sono nel modulo comune, che Swing e libGDX condividono: quelli senza tipo di bottino (i libri tecnici, la spada
        // normalmente nascosta) sono aspetti di artefatto, con lo stesso nome della costante
        String nomeRisorsa = tipoOggetto != null ? NomiDelleImmagini.oggetto(tipoOggetto) : NomiDelleImmagini.oggetto(AspettoArtefatto.valueOf(name()));
        this.immagine = nomeRisorsa == null ? null : BufferedImageBuilder.buildBufferedImage(nomeRisorsa, LivelloDiZoom.valore());
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
