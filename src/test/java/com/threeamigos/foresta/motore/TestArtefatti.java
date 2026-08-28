package com.threeamigos.foresta.motore;

/**
 *
 * @author Stefano Reksten
 */
public class TestArtefatti extends TestGrammatiche {

    public static void main(String[] args) throws Exception {
        GrammarBean gBean = new GrammarBean(
                TestArtefatti.class.getResourceAsStream("/com/threeamigos/foresta/motore/artefatti.txt"),
                TestArtefatti.class.getResourceAsStream("/com/threeamigos/foresta/motore/preposizioni_articolate_pp.txt"));
        printProductions(gBean);
    }

}
