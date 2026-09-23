package com.threeamigos.foresta.motore.modellodati;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author Stefano Reksten
 */
class NotizieMDTest {

    @Test
    void salvaERileggeIlTipoDeiMessaggi() throws IOException {
        // Given
        NotizieMD originale = new NotizieMD();
        originale.getUltimiMessaggi().add(new Messaggio("Una frase | con un separatore", false));
        originale.getUltimiMessaggi().add(new Messaggio("Un paragrafo", true));
        originale.getUltimeNotizie().add(new Notizia("CV1", "Titolo - Corpo"));
        StringWriter scrittura = new StringWriter();
        try (PrintWriter writer = new PrintWriter(scrittura)) {
            originale.salva(writer);
        }

        // When
        NotizieMD riletto = new NotizieMD();
        riletto.leggi(new BufferedReader(new StringReader(scrittura.toString())));

        // Then
        List<Messaggio> messaggi = riletto.getUltimiMessaggi();
        assertEquals(2, messaggi.size());
        assertEquals("Una frase | con un separatore", messaggi.get(0).getTesto());
        assertFalse(messaggi.get(0).isParagrafo());
        assertEquals("Un paragrafo", messaggi.get(1).getTesto());
        assertTrue(messaggi.get(1).isParagrafo());
        assertEquals(1, riletto.getUltimeNotizie().size());
        assertEquals("CV1", riletto.getUltimeNotizie().get(0).getId());
    }

    @Test
    void rileggeIMessaggiDeiSalvataggiSenzaTipo() throws IOException {
        // Given: il formato precedente, con il solo testo di ogni messaggio
        String salvataggio = "2\nPrimo messaggio\nF\n0\n";

        // When
        NotizieMD riletto = new NotizieMD();
        riletto.leggi(new BufferedReader(new StringReader(salvataggio)));

        // Then
        List<Messaggio> messaggi = riletto.getUltimiMessaggi();
        assertEquals("Primo messaggio", messaggi.get(0).getTesto());
        assertFalse(messaggi.get(0).isParagrafo());
        // Una riga che non ha la forma "tipo|testo" resta testo, anche se inizia per F
        assertEquals("F", messaggi.get(1).getTesto());
        assertFalse(messaggi.get(1).isParagrafo());
    }
}
