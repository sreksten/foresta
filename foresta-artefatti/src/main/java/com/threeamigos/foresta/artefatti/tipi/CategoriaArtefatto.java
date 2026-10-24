package com.threeamigos.foresta.artefatti.tipi;

/**
 * La categoria di un artefatto: serve a raggruppare i tipi che seguono le stesse regole (le armi colpiscono, le
 * armature proteggono, gli ingredienti magici si fondono).
 *
 * @author Stefano Reksten
 */
public enum CategoriaArtefatto {

    /**
     * Categoria per artefatti offensivi
     */
    ARMA,
    /**
     * Categoria per artefatti difensivi
     */
    SCUDO,
    /**
     * Categoria per artefatti difensivi
     */
    ARMATURA,
    /**
     * Categoria per artefatti difensivi
     */
    ELMO,
    /**
     * Categoria per artefatti difensivi
     */
    SCHINIERI,
    /**
     * Artefatti che aumentano il potere magico di chi li porta (il libro magico): si incantano, e una pergamena
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
