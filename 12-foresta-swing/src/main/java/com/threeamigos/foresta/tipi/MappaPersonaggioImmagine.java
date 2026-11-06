package com.threeamigos.foresta.tipi;

import com.threeamigos.foresta.ui.immagini.ImageCache;
import com.threeamigos.foresta.ui.immagini.BufferedImageBuilder;
import com.threeamigos.foresta.ui.immagini.VersiDeiPersonaggi;
import com.threeamigos.foresta.ui.LivelloDiZoom;

import java.awt.image.BufferedImage;

/**
 * Le immagini di ogni classe di personaggio: il personaggio per la schermata di gioco e, per chi si può reclutare, l'icona per il riquadro del gruppo. Si caricano alla prima volta che si usa l'enum.
 *
 * @author Stefano Reksten
 */
public enum MappaPersonaggioImmagine {

    ARPIA(TipoPersonaggio.ARPIA, "personaggi/Arpia.png"),
    BARDO(TipoPersonaggio.BARDO, "personaggi/Bardo.png", "icone/Bardo.png"),
    CANTASTORIE(TipoPersonaggio.CANTASTORIE, "personaggi/Cantastorie.png", "icone/Cantastorie.png"),
    CENTAURO(TipoPersonaggio.CENTAURO, "personaggi/Centauro.png", "icone/Centauro.png"),
    CHIMERA(TipoPersonaggio.CHIMERA, "personaggi/Chimera.png"),
    CHIMERA_DRAGO(TipoPersonaggio.CHIMERA_DRAGO, "personaggi/ChimeraDrago.png"),
    DRAGO(TipoPersonaggio.DRAGO, "personaggi/Drago.png"),
    ELFA(TipoPersonaggio.ELFA, "personaggi/Elfa.png", "icone/Elfa.png"),
    ELFO(TipoPersonaggio.ELFO, "personaggi/Elfo.png", "icone/Elfo.png"),
    EREMITA(TipoPersonaggio.EREMITA, "personaggi/Eremita.png", "icone/Eremita.png"),
    FANTASMA(TipoPersonaggio.FANTASMA, "personaggi/Fantasma.png"),
    FOLLETTO(TipoPersonaggio.FOLLETTO, "personaggi/Folletto.png"),
    GARGOYLE(TipoPersonaggio.GARGOYLE, "personaggi/Gargoyle.png"),
    GIGANTE(TipoPersonaggio.GIGANTE, "personaggi/Gigante.png", "icone/Gigante.png"),
    GOBLIN(TipoPersonaggio.GOBLIN, "personaggi/Goblin.png", "icone/Goblin.png"),
    GUERRIERA(TipoPersonaggio.GUERRIERA, "personaggi/Guerriera.png", "icone/Guerriera.png"),
    GUERRIERO(TipoPersonaggio.GUERRIERO, "personaggi/Guerriero.png", "icone/Guerriero.png"),
    HOBGOBLIN(TipoPersonaggio.HOBGOBLIN, "personaggi/Hobgoblin.png", "icone/Hobgoblin.png"),
    IDRA(TipoPersonaggio.IDRA, "personaggi/Idra.png"),
    LADRA(TipoPersonaggio.LADRA, "personaggi/Ladra.png", "icone/Ladra.png"),
    LADRO(TipoPersonaggio.LADRO, "personaggi/Ladro.png", "icone/Ladro.png"),
    LICH(TipoPersonaggio.LICH, "personaggi/Lich.png"),
    MAGA(TipoPersonaggio.MAGA, "personaggi/Maga.png", "icone/Maga.png"),
    MAGO(TipoPersonaggio.MAGO, "personaggi/Mago.png", "icone/Mago.png"),
    SACERDOTESSA(TipoPersonaggio.SACERDOTESSA, "personaggi/Sacerdotessa.png", "icone/Sacerdotessa.png"),
    SACERDOTE(TipoPersonaggio.SACERDOTE, "personaggi/Sacerdote.png", "icone/Sacerdote.png"),
    CHIERICA(TipoPersonaggio.CHIERICA, "personaggi/Chierica.png", "icone/Chierica.png"),
    CHIERICO(TipoPersonaggio.CHIERICO, "personaggi/Chierico.png", "icone/Chierico.png"),
    DRUIDA(TipoPersonaggio.DRUIDA, "personaggi/Druida.png", "icone/Druida.png"),
    DRUIDO(TipoPersonaggio.DRUIDO, "personaggi/Druido.png", "icone/Druido.png"),
    MINOTAURO(TipoPersonaggio.MINOTAURO, "personaggi/Minotauro.png", "icone/Minotauro.png"),
    MINOTAURO_GIGANTE(TipoPersonaggio.MINOTAURO_GIGANTE, "personaggi/MinotauroGigante.png"),
    NECROMANTE(TipoPersonaggio.NECROMANTE, "personaggi/Necromante.png"),
    OMBRAFIAMMA(TipoPersonaggio.OMBRAFIAMMA, "personaggi/OmbraFiamma.png", "icone/OmbraFiamma.png"),
    OMBRA_NERA(TipoPersonaggio.OMBRA_NERA, "personaggi/OmbraNera.png"),
    SCHELETRO(TipoPersonaggio.SCHELETRO, "personaggi/Scheletro.png"),
    SPETTRO(TipoPersonaggio.SPETTRO, "personaggi/Spettro.png"),
    SPIRITO(TipoPersonaggio.SPIRITO, "personaggi/Spirito.png"),
    STREGA(TipoPersonaggio.STREGA, "personaggi/Strega.png"),
    TITANO(TipoPersonaggio.TITANO, "personaggi/Titano.png", "icone/Titano.png"),
    TROLL(TipoPersonaggio.TROLL, "personaggi/Troll.png"),
    VIVERNA(TipoPersonaggio.VIVERNA, "personaggi/Viverna.png"),
    // I non combattenti
    LOCANDIERE(TipoPersonaggio.LOCANDIERE, "personaggi/Locandiere.png", "icone/Locandiere.png"),
    ARMAIOLO(TipoPersonaggio.ARMAIOLO, "personaggi/Armaiolo.png", "icone/Armaiolo.png"),
    ALCHIMISTA(TipoPersonaggio.ALCHIMISTA, "personaggi/Alchimista.png", "icone/Alchimista.png"),
    VENDITORE_DI_PERGAMENE(TipoPersonaggio.VENDITORE_DI_PERGAMENE, "personaggi/VenditoreDiPergamene.png", "icone/VenditoreDiPergamene.png"),
    INCANTATORE(TipoPersonaggio.INCANTATORE, "personaggi/Incantatore.png", "icone/Incantatore.png"),
    MOGLIE_DEL_BARDO(TipoPersonaggio.MOGLIE_DEL_BARDO, "personaggi/MoglieDelBardo.png", "icone/MoglieDelBardo.png"),
    VIANDANTE(TipoPersonaggio.VIANDANTE, "personaggi/Viandante.png", "icone/Viandante.png"),
    BARDO_LOCANDA(TipoPersonaggio.BARDO_DELLE_LOCANDE, "personaggi/BardoLocanda.png", "icone/BardoLocanda.png"),
    VICHINGO(TipoPersonaggio.VICHINGO, "personaggi/Vichingo.png", "icone/Vichingo.png"),
    CAPITANO_DELLE_GUARDIE(TipoPersonaggio.CAPITANO_DELLE_GUARDIE, "personaggi/CapitanoDelleGuardie.png", "icone/CapitanoDelleGuardie.png"),
    BORGOMASTRO(TipoPersonaggio.BORGOMASTRO, "personaggi/Borgomastro.png"),
    UOMO(TipoPersonaggio.UOMO, "personaggi/Uomo.png", "icone/Uomo.png"),
    DONNA(TipoPersonaggio.DONNA, "personaggi/Donna.png", "icone/Donna.png");

    private final TipoPersonaggio classePersonaggio;
    private final BufferedImage immagine;
    private final BufferedImage icona;

    MappaPersonaggioImmagine(TipoPersonaggio classePersonaggio, String nomeRisorsa) {
        this.classePersonaggio = classePersonaggio;
        this.immagine = BufferedImageBuilder.buildBufferedImage(nomeRisorsa, LivelloDiZoom.valore());
        this.icona = null;
    }

    MappaPersonaggioImmagine(TipoPersonaggio classePersonaggio, String nomeRisorsa, String nomeIcona) {
        this.classePersonaggio = classePersonaggio;
        this.immagine = BufferedImageBuilder.buildBufferedImage(nomeRisorsa, LivelloDiZoom.valore());
        this.icona = BufferedImageBuilder.buildBufferedImage(nomeIcona);
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
                () -> BufferedImageBuilder.ingrandisci(VersiDeiPersonaggi.specchia(originale), 1.0));
    }

    public static BufferedImage getIcona(TipoPersonaggio classePersonaggio) {
        return getClassePersonaggioImmagine(classePersonaggio).icona;
    }

}
