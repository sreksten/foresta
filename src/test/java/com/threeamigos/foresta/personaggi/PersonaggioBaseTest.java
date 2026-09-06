package com.threeamigos.foresta.personaggi;

import org.junit.jupiter.api.Test;

/**
 *
 * @author Stefano Reksten
 */
class PersonaggioBaseTest {

    @Test
    void costruiscePersonaggioLivelloUno() {
        for (ClassePersonaggio classe : ClassePersonaggio.values()) {
            for (int i = 0; i < 10; i++) {
                Personaggio personaggio = classe.getIstanza(1);
            }
        }
    }

}