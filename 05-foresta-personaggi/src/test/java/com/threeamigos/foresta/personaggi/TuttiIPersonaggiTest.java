package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.gruppi.DatiDiProva;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le classi concrete sono quasi tutte dati (articoli, moltiplicatori, massimi, offerte): le si legge tutte, per ogni classe e per qualche livello,
 * chiamando ogni lettura senza argomenti e quelle che ne vogliono uno di un'enumerazione con ogni suo valore. Una classe che dimentica un
 * valore, o che lancia dove non deve, si vede qui.
 */
class TuttiIPersonaggiTest {

    /** Metodi che cambiano il personaggio, e non si leggono soltanto. */
    private static final Set<String> DA_NON_CHIAMARE = new HashSet<>(Arrays.asList(
            "fugge", "muore", "resuscita", "riduciModificatori", "rimuoviTuttiGliEffettiDiStato", "rimuoviGliEffettiDiStatoNegativi",
            "rimuoviIModificatoriTemporaneiRimovibili", "applicaDanniDaEffettiDiStato", "riduciEffettiDiStato", "riduciRicariche",
            "decrementaTempo", "wait", "notify", "notifyAll"));

    @BeforeEach
    void preparaModello() {
        DatiDiProva.nuovaPartita();
    }

    private static Object chiama(Object oggetto, Method metodo, Object... argomenti) throws IllegalAccessException {
        try {
            return metodo.invoke(oggetto, argomenti);
        } catch (InvocationTargetException e) {
            // Chi non ha senso per una classe (un attributo non primario, un personaggio giocante senza nome) lo dice con queste eccezioni
            assertTrue(e.getCause() instanceof IllegalArgumentException || e.getCause() instanceof IllegalStateException
                            || e.getCause() instanceof UnsupportedOperationException,
                    oggetto.getClass().getSimpleName() + "." + metodo.getName() + " ha lanciato " + e.getCause());
            return null;
        }
    }

    @Test
    void ogniClasseSiLeggeTutta() throws IllegalAccessException {
        int letture = 0;
        for (TipoPersonaggio tipo : TipoPersonaggio.values()) {
            for (int livello : new int[] {1, 5, 20}) {
                Personaggio personaggio;
                try {
                    personaggio = FabbricaPersonaggi.crea(tipo, livello);
                } catch (IllegalArgumentException e) {
                    continue;
                }
                for (Method metodo : personaggio.getClass().getMethods()) {
                    if (Modifier.isStatic(metodo.getModifiers()) || DA_NON_CHIAMARE.contains(metodo.getName())
                            || metodo.getName().startsWith("set") || metodo.getName().startsWith("add") || metodo.getName().startsWith("sub")) {
                        continue;
                    }
                    Class<?>[] parametri = metodo.getParameterTypes();
                    if (parametri.length == 0) {
                        chiama(personaggio, metodo);
                        letture++;
                    } else if (parametri.length == 1 && parametri[0].isEnum()) {
                        for (Object valore : parametri[0].getEnumConstants()) {
                            chiama(personaggio, metodo, valore);
                            letture++;
                        }
                    }
                }
            }
        }
        assertTrue(letture > 10000, "letture: " + letture);
    }
}
