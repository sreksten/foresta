package com.threeamigos.foresta.missioni.richieste;

import com.threeamigos.foresta.testi.Testi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le righe di missioni_en.txt: tutte quelle che la grammatica inglese sa produrre si leggono come quelle italiane, e nessuna lascia marcatori
 * della grammatica nei testi.
 */
class RigheDelleRichiesteInIngleseTest {

    @BeforeEach
    void inglese() {
        Testi.impostaLingua("en");
    }

    @AfterEach
    void italiano() {
        Testi.impostaLingua(null);
    }

    @Test
    void ogniRigaProdottaSiLegge() {
        for (Map.Entry<String, RigheDelleRichiesteTest.Formato> formato : RigheDelleRichiesteTest.formati().entrySet()) {
            for (int i = 0; i < 150; i++) {
                String riga = formato.getValue().riga.get();
                assertFalse(riga.contains("[") || riga.contains("{") || riga.contains("|"), formato.getKey() + ": " + riga);
                try {
                    assertNotNull(formato.getValue().lettore.apply(riga), formato.getKey());
                } catch (RuntimeException e) {
                    fail(formato.getKey() + ": " + riga + " => " + e);
                }
            }
        }
    }
}
