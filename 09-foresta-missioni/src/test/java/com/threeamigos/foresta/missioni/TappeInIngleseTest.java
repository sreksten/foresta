package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.missioni.richieste.TappeRichieste;
import com.threeamigos.foresta.testi.ProduttoreDiMissioniCasuale;
import com.threeamigos.foresta.testi.Testi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * La grammatica inglese delle missioni a tappe (tappe_en.txt) dà righe che {@link TappeRichieste} legge come quelle italiane, in tutte le loro forme.
 */
class TappeInIngleseTest {

    @BeforeEach
    void inglese() {
        Testi.impostaLingua("en");
    }

    @AfterEach
    void italiano() {
        Testi.impostaLingua(null);
    }

    @Test
    void ogniRigaSiLegge() {
        Map<String, Integer> forme = new LinkedHashMap<>();
        for (int i = 0; i < 2000; i++) {
            boolean farsa = i % 3 == 0;
            String riga = ProduttoreDiMissioniCasuale.rigaDiTappe(farsa);
            TappeRichieste r = TappeRichieste.da(riga);
            assertEquals(farsa, r.isFarsa(), riga);
            assertTrue(r.getMonete() > 0);
            if (farsa) {
                assertTrue(r.getRichiesta().contains(TappeRichieste.MONETE + " coins"), riga);
            }
            assertFalse(riga.contains("{") || riga.contains("}") || riga.contains("["), riga);
            assertFalse(riga.matches("(?s).*\\b(il|la|di|che|non)\\b .*\\b(il|la|di|che|non)\\b.*"), "sembra italiano: " + riga);
            forme.merge(r.isFarsa() + "/" + r.getBivio().isPresent() + "/" + r.hasChiavi() + "/" + r.isDiUnLeggendario(), 1, Integer::sum);
        }
        assertTrue(forme.size() >= 5, forme.toString());
    }
}
