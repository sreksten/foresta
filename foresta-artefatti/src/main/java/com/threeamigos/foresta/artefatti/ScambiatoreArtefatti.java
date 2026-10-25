package com.threeamigos.foresta.artefatti;

import java.util.Collection;

/**
 * Una interfaccia per scambiare artefatti per esempio tra personaggio e gruppo, oppure gruppo e commerciante
 * @author Stefano Reksten
 */
public interface ScambiatoreArtefatti {

    /**
     * Gli artefatti che questa parte ha in questo momento.
     */
    Collection<Artefatto> getInventario();

    /**
     * Mette l'artefatto fra quelli di questa parte.
     */
    void addArtefatto(Artefatto artefatto);

    /**
     * Toglie l'artefatto da quelli di questa parte.
     */
    void removeArtefatto(Artefatto artefatto);

    /**
     * Se questa parte accetta l'artefatto in uno scambio.
     *
     * @return true se questa parte accetta l'artefatto: un negozio, per esempio, tratta solo certi tipi
     */
    default boolean tratta(Artefatto artefatto) {
        return true;
    }

}
