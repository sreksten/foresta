package com.threeamigos.foresta.testi;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le regole di postproduzione che il gioco pubblica (nel motore) devono continuare a essere accettate da {@link GrammarBean}.
 */
class GrammatichePubblicateTest {

    @Test
    void shippedPostProductionFileIsStillAccepted() throws Exception {
        // The rules actually used by the game must keep loading: preposizioni_articolate_pp.txt
        // is treated as final, so this test fails loudly if the new check ever rejects it.
        try (InputStream grammar = new ByteArrayInputStream("ROOT\n\tvai a il castello\n".getBytes(StandardCharsets.UTF_8));
             InputStream postProduction = GrammatichePubblicateTest.class.getResourceAsStream(
                     "/com/threeamigos/foresta/motore/preposizioni_articolate_pp.txt")) {
            assertNotNull(postProduction, "preposizioni_articolate_pp.txt not found on the test classpath");
            GrammarBean bean = new GrammarBean(grammar, postProduction);
            assertEquals("vai al castello", bean.produce().get(0));
        }
    }
}
