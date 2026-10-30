package com.threeamigos.foresta.intermezzi;

import com.threeamigos.foresta.tipi.TipoIntermezzo;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class FabbricaIntermezziTest {

    @Test
    @SuppressWarnings("unchecked")
    void unTipoSenzaCostruttoreNonSiCrea() throws Exception {
        Field campo = FabbricaIntermezzi.class.getDeclaredField("COSTRUTTORI");
        campo.setAccessible(true);
        Map<TipoIntermezzo, Function<TipoIntermezzo, Intermezzo>> costruttori =
                (Map<TipoIntermezzo, Function<TipoIntermezzo, Intermezzo>>) campo.get(null);
        TipoIntermezzo tipo = TipoIntermezzo.INTERMEZZO_ACCAMPAMENTO;
        Function<TipoIntermezzo, Intermezzo> costruttore = costruttori.remove(tipo);
        try {
            assertFalse(FabbricaIntermezzi.isRegistrato(tipo));
            IllegalStateException errore = assertThrows(IllegalStateException.class, () -> FabbricaIntermezzi.crea(tipo));
            assertTrue(errore.getMessage().contains(tipo.name()), errore.getMessage());
        } finally {
            costruttori.put(tipo, costruttore);
        }
        assertTrue(FabbricaIntermezzi.isRegistrato(tipo));
    }
}
