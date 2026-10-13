package com.threeamigos.foresta.tipi;

/**
 * La famiglia generale di un artefatto: serve a raggruppare i tipi che seguono le stesse regole (le armi colpiscono, le
 * armature proteggono, gli ingredienti magici si fondono).
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

    /**
     * Gli accessori senza regole particolari (anello, talismano, ninnolo)
     */
    ALTRO

}
