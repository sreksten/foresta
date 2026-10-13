package com.threeamigos.foresta.tipi;

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
     * Supertipo generico per artefatti difensivi
     */
    SCHINIERI,

    /**
     * Artefatti che aumentano il POTERE_MAGICO di chi li porta (il libro magico): si incantano, e una pergamena
     * fusa diventa come una pagina in più del libro
     */
    POTENZIAMENTO_POTERE_MAGICO,

    /**
     * Gli ingredienti magici (pergamena, gemma, monile, gingillo, sigillo), che si fondono sugli artefatti
     * per potenziarli
     */
    INCANTAMENTO,

    ALTRO

}
