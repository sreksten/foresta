package com.threeamigos.foresta.mappa;

import com.threeamigos.foresta.modellodati.CoordinateMD;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le coordinate di una casella: due coordinate con la stessa colonna e la stessa riga sono la stessa casella, anche come
 * chiave di una mappa.
 */
class CoordinateMDTest {

    @Test
    void colonnaERigaSiLeggono() {
        CoordinateMD coordinate = new CoordinateMD(3, 7);
        assertEquals(3, coordinate.getX());
        assertEquals(7, coordinate.getY());
    }

    @Test
    void sonoImmutabiliPerPotersiUsareDaChiave() {
        // Un valore che cambia mentre sta in una mappa la renderebbe irraggiungibile: niente setter e campi final
        for (String metodo : new String[]{"setX", "setY"}) {
            assertThrows(NoSuchMethodException.class, () -> CoordinateMD.class.getMethod(metodo, int.class), metodo);
        }
        for (java.lang.reflect.Field campo : CoordinateMD.class.getDeclaredFields()) {
            if (campo.isSynthetic()) {
                continue; // per esempio $jacocoData, aggiunto da JaCoCo nelle misure di copertura
            }
            assertTrue(java.lang.reflect.Modifier.isFinal(campo.getModifiers()), "il campo " + campo.getName() + " dev'essere final");
        }
    }

    @Test
    void sonoUgualiSoloConStessaColonnaEStessaRiga() {
        CoordinateMD coordinate = new CoordinateMD(2, 5);
        assertEquals(coordinate, coordinate);
        assertEquals(new CoordinateMD(2, 5), coordinate);
        assertEquals(new CoordinateMD(2, 5).hashCode(), coordinate.hashCode());
        assertNotEquals(new CoordinateMD(5, 2), coordinate, "colonna e riga scambiate sono un'altra casella");
        assertNotEquals(new CoordinateMD(2, 6), coordinate);
        assertNotEquals(new CoordinateMD(3, 5), coordinate);
        // Chiamati direttamente: assertNotEquals userebbe l'equals del primo argomento
        assertFalse(coordinate.equals(null));
        assertFalse(coordinate.equals("(2, 5)"), "un oggetto di un'altra classe non è mai uguale");
    }

    @Test
    void funzionanoDaChiaveAncheOltreLaMisuraDellaForesta() {
        // L'hash non dipende da una misura massima: anche caselle lontane restano distinte come chiavi
        Map<CoordinateMD, String> mappa = new HashMap<>();
        Set<Integer> hash = new HashSet<>();
        for (int x = 0; x < 120; x += 7) {
            for (int y = 0; y < 120; y += 11) {
                mappa.put(new CoordinateMD(x, y), x + "," + y);
                hash.add(new CoordinateMD(x, y).hashCode());
            }
        }
        for (int x = 0; x < 120; x += 7) {
            for (int y = 0; y < 120; y += 11) {
                assertEquals(x + "," + y, mappa.get(new CoordinateMD(x, y)));
            }
        }
        assertEquals(mappa.size(), hash.size(), "su queste caselle nessun hash si ripete");
    }

    @Test
    void siScrivonoComeColonnaERigaFraParentesi() {
        assertEquals("(4, 9)", new CoordinateMD(4, 9).toString());
    }
}
