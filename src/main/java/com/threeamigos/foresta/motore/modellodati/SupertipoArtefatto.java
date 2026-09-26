package com.threeamigos.foresta.motore.modellodati;

/**
 *
 * @author Stefano Reksten
 */
public enum SupertipoArtefatto {

    /**
     * Supertipo generico per artefatti offensivi
     */
    ARMA,
    /**
     * Supertipo generico per artefatti difensivi
     */
    SCUDO,
    /**
     * Supertipo generico per artefatti difensivi
     */
    ARMATURA,
    /**
     * Supertipo generico per artefatti difensivi
     */
    ELMO,

    /**
     * Artefatti che aumentano il POTERE_MAGICO di chi li porta (il libro magico): si incantano, e una pergamena
     * fusa diventa come una pagina in più del libro
     */
    POTENZIAMENTO_POTERE_MAGICO,

    /**
     * Supertipo generico per artefatti destinati a potenziarne altri
     */
    INCANTAMENTO,

    ALTRO

}
