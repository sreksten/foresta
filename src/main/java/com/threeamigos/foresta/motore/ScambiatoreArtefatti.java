package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.oggetti.Artefatto;

import java.util.Collection;

/**
 * Una interfaccia per scambiare artefatti per esempio tra personaggio e gruppo, oppure gruppo e commerciante
 * @author Stefano Reksten
 */
public interface ScambiatoreArtefatti {

    Collection<Artefatto> getInventario();

    void addArtefatto(Artefatto artefatto);

    void removeArtefatto(Artefatto artefatto);

    /**
     * @return true se questa parte accetta l'artefatto: un negozio, per esempio, tratta solo certi tipi
     */
    default boolean tratta(Artefatto artefatto) {
        return true;
    }

}
