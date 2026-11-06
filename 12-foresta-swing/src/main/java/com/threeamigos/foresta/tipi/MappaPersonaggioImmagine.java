package com.threeamigos.foresta.tipi;

import com.threeamigos.foresta.ui.immagini.ImageCache;
import com.threeamigos.foresta.ui.immagini.NomiDelleImmagini;
import com.threeamigos.foresta.ui.immagini.BufferedImageBuilder;
import com.threeamigos.foresta.ui.immagini.ImmaginiAwt;
import com.threeamigos.foresta.ui.immagini.VersiDeiPersonaggi;
import com.threeamigos.foresta.ui.LivelloDiZoom;

import java.awt.image.BufferedImage;

/**
 * Le immagini di ogni classe di personaggio: il personaggio per la schermata di gioco e, per chi si può reclutare, l'icona per il riquadro del gruppo. Si caricano alla prima volta che si usa l'enum.
 *
 * @author Stefano Reksten
 */
public enum MappaPersonaggioImmagine {

    ARPIA(TipoPersonaggio.ARPIA),
    BARDO(TipoPersonaggio.BARDO),
    CANTASTORIE(TipoPersonaggio.CANTASTORIE),
    CENTAURO(TipoPersonaggio.CENTAURO),
    CHIMERA(TipoPersonaggio.CHIMERA),
    CHIMERA_DRAGO(TipoPersonaggio.CHIMERA_DRAGO),
    DRAGO(TipoPersonaggio.DRAGO),
    ELFA(TipoPersonaggio.ELFA),
    ELFO(TipoPersonaggio.ELFO),
    EREMITA(TipoPersonaggio.EREMITA),
    FANTASMA(TipoPersonaggio.FANTASMA),
    FOLLETTO(TipoPersonaggio.FOLLETTO),
    GARGOYLE(TipoPersonaggio.GARGOYLE),
    GIGANTE(TipoPersonaggio.GIGANTE),
    GOBLIN(TipoPersonaggio.GOBLIN),
    GUERRIERA(TipoPersonaggio.GUERRIERA),
    GUERRIERO(TipoPersonaggio.GUERRIERO),
    HOBGOBLIN(TipoPersonaggio.HOBGOBLIN),
    IDRA(TipoPersonaggio.IDRA),
    LADRA(TipoPersonaggio.LADRA),
    LADRO(TipoPersonaggio.LADRO),
    LICH(TipoPersonaggio.LICH),
    MAGA(TipoPersonaggio.MAGA),
    MAGO(TipoPersonaggio.MAGO),
    SACERDOTESSA(TipoPersonaggio.SACERDOTESSA),
    SACERDOTE(TipoPersonaggio.SACERDOTE),
    CHIERICA(TipoPersonaggio.CHIERICA),
    CHIERICO(TipoPersonaggio.CHIERICO),
    DRUIDA(TipoPersonaggio.DRUIDA),
    DRUIDO(TipoPersonaggio.DRUIDO),
    MINOTAURO(TipoPersonaggio.MINOTAURO),
    MINOTAURO_GIGANTE(TipoPersonaggio.MINOTAURO_GIGANTE),
    NECROMANTE(TipoPersonaggio.NECROMANTE),
    OMBRAFIAMMA(TipoPersonaggio.OMBRAFIAMMA),
    OMBRA_NERA(TipoPersonaggio.OMBRA_NERA),
    SCHELETRO(TipoPersonaggio.SCHELETRO),
    SPETTRO(TipoPersonaggio.SPETTRO),
    SPIRITO(TipoPersonaggio.SPIRITO),
    STREGA(TipoPersonaggio.STREGA),
    TITANO(TipoPersonaggio.TITANO),
    TROLL(TipoPersonaggio.TROLL),
    VIVERNA(TipoPersonaggio.VIVERNA),
    // I non combattenti
    LOCANDIERE(TipoPersonaggio.LOCANDIERE),
    ARMAIOLO(TipoPersonaggio.ARMAIOLO),
    ALCHIMISTA(TipoPersonaggio.ALCHIMISTA),
    VENDITORE_DI_PERGAMENE(TipoPersonaggio.VENDITORE_DI_PERGAMENE),
    INCANTATORE(TipoPersonaggio.INCANTATORE),
    MOGLIE_DEL_BARDO(TipoPersonaggio.MOGLIE_DEL_BARDO),
    VIANDANTE(TipoPersonaggio.VIANDANTE),
    BARDO_LOCANDA(TipoPersonaggio.BARDO_DELLE_LOCANDE),
    VICHINGO(TipoPersonaggio.VICHINGO),
    CAPITANO_DELLE_GUARDIE(TipoPersonaggio.CAPITANO_DELLE_GUARDIE),
    BORGOMASTRO(TipoPersonaggio.BORGOMASTRO),
    UOMO(TipoPersonaggio.UOMO),
    DONNA(TipoPersonaggio.DONNA);

    private final TipoPersonaggio classePersonaggio;
    private final BufferedImage immagine;
    private final BufferedImage icona;

    MappaPersonaggioImmagine(TipoPersonaggio classePersonaggio) {
        this.classePersonaggio = classePersonaggio;
        // I nomi delle risorse sono nel modulo comune, che Swing e libGDX condividono
        this.immagine = BufferedImageBuilder.buildBufferedImage(NomiDelleImmagini.personaggio(classePersonaggio), LivelloDiZoom.valore());
        String nomeIcona = NomiDelleImmagini.iconaDelPersonaggio(classePersonaggio);
        this.icona = nomeIcona == null ? null : BufferedImageBuilder.buildBufferedImage(nomeIcona);
    }

    public static MappaPersonaggioImmagine getClassePersonaggioImmagine(TipoPersonaggio classePersonaggio) {
        for (MappaPersonaggioImmagine classePersonaggioImmagine : values()) {
            if (classePersonaggioImmagine.classePersonaggio == classePersonaggio) {
                return classePersonaggioImmagine;
            }
        }
        throw new IllegalArgumentException("MappaPersonaggioImmagine not found for TipoPersonaggio: " + classePersonaggio);
    }

    public static BufferedImage getImmagine(TipoPersonaggio classePersonaggio) {
        return getClassePersonaggioImmagine(classePersonaggio).immagine;
    }

    /**
     * L'immagine della classe rivolta verso il verso voluto: quella originale se già guarda da quella parte, altrimenti
     * la sua versione specchiata, compatibile con lo schermo e ricordata da ImageCache (finché serve).
     */
    public static BufferedImage getImmagine(TipoPersonaggio classePersonaggio, Verso versoVoluto) {
        BufferedImage originale = getImmagine(classePersonaggio);
        if (!VersiDeiPersonaggi.serveSpecchiare(classePersonaggio, versoVoluto)) {
            return originale;
        }
        return ImageCache.getImmaginePersonaggio(classePersonaggio, versoVoluto,
                () -> BufferedImageBuilder.ingrandisci(ImmaginiAwt.specchia(originale), 1.0));
    }

    public static BufferedImage getIcona(TipoPersonaggio classePersonaggio) {
        return getClassePersonaggioImmagine(classePersonaggio).icona;
    }

}
