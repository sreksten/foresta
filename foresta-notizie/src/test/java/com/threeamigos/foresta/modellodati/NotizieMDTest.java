package com.threeamigos.foresta.modellodati;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Stefano Reksten
 */
class NotizieMDTest {

    @Test
    void salvaERileggeIlTipoDeiMessaggi() throws IOException {
        // Given
        NotizieMD originale = new NotizieMD();
        // Si aggiunge in testa: il più recente è il primo
        originale.aggiungiMessaggio(new MessaggioMD("Un paragrafo", true));
        originale.aggiungiMessaggio(new MessaggioMD("Una frase | con un separatore", false));
        originale.aggiungiNotizia(new NotiziaMD("CV1", "Titolo - Corpo"));
        StringWriter scrittura = new StringWriter();
        try (PrintWriter writer = new PrintWriter(scrittura)) {
            originale.salva(writer);
        }

        // When
        NotizieMD riletto = new NotizieMD();
        riletto.leggi(new BufferedReader(new StringReader(scrittura.toString())));

        // Then
        List<MessaggioMD> messaggi = riletto.getUltimiMessaggi();
        assertEquals(2, messaggi.size());
        assertEquals(originale.getUltimiMessaggi().get(0).getUuid(), messaggi.get(0).getUuid());
        assertEquals("Una frase | con un separatore", messaggi.get(0).getTesto());
        assertFalse(messaggi.get(0).isParagrafo());
        assertEquals("Un paragrafo", messaggi.get(1).getTesto());
        assertTrue(messaggi.get(1).isParagrafo());
        assertEquals(1, riletto.getUltimeNotizie().size());
        assertEquals("CV1", riletto.getUltimeNotizie().get(0).getId());
    }

    @Test
    void rileggeUnSalvataggioScrittoAMano() throws IOException {
        // Given: ogni messaggio è "uuid|tipo|testo"
        String salvataggio = "2\n"
                + "uuid-1|P|Primo messaggio\n"
                + "uuid-2|F|F|P\n"
                + "0\n";

        // When
        NotizieMD riletto = new NotizieMD();
        riletto.leggi(new BufferedReader(new StringReader(salvataggio)));

        // Then
        List<MessaggioMD> messaggi = riletto.getUltimiMessaggi();
        assertEquals(2, messaggi.size());
        assertEquals("uuid-1", messaggi.get(0).getUuid());
        assertTrue(messaggi.get(0).isParagrafo());
        assertEquals("Primo messaggio", messaggi.get(0).getTesto());
        assertEquals("uuid-2", messaggi.get(1).getUuid());
        assertFalse(messaggi.get(1).isParagrafo());
        // Il testo è tutto ciò che segue il secondo separatore, anche se somiglia a un tipo
        assertEquals("F|P", messaggi.get(1).getTesto());
        assertTrue(riletto.getUltimeNotizie().isEmpty());
    }

    @Test
    void ilPiuRecenteEInTestaEOltreIlLimiteSiScartanoIPiuVecchi() {
        NotizieMD notizie = new NotizieMD();

        for (int i = 1; i <= NotizieMD.MASSIMO_MESSAGGI_RICORDATI + 5; i++) {
            notizie.aggiungiMessaggio(new MessaggioMD("messaggio " + i, false));
        }
        for (int i = 1; i <= NotizieMD.MASSIMO_NOTIZIE_RICORDATE + 3; i++) {
            notizie.aggiungiNotizia(new NotiziaMD("N" + i, "notizia " + i));
        }

        assertEquals(NotizieMD.MASSIMO_MESSAGGI_RICORDATI, notizie.getUltimiMessaggi().size());
        assertEquals("messaggio " + (NotizieMD.MASSIMO_MESSAGGI_RICORDATI + 5), notizie.getUltimiMessaggi().get(0).getTesto());
        assertEquals("messaggio 6", notizie.getUltimiMessaggi().get(NotizieMD.MASSIMO_MESSAGGI_RICORDATI - 1).getTesto());
        assertEquals(NotizieMD.MASSIMO_NOTIZIE_RICORDATE, notizie.getUltimeNotizie().size());
        assertEquals("N" + (NotizieMD.MASSIMO_NOTIZIE_RICORDATE + 3), notizie.getUltimeNotizie().get(0).getId());
        assertEquals("N4", notizie.getUltimeNotizie().get(NotizieMD.MASSIMO_NOTIZIE_RICORDATE - 1).getId());
    }

    @Test
    void reimpostareDimenticaMessaggiENotizie() {
        NotizieMD notizie = new NotizieMD();
        notizie.aggiungiMessaggio(new MessaggioMD("Una frase.", false));
        notizie.aggiungiNotizia(new NotiziaMD("N1", "Titolo - Corpo"));

        notizie.reimposta();

        assertTrue(notizie.getUltimiMessaggi().isEmpty());
        assertTrue(notizie.getUltimeNotizie().isEmpty());
    }
}
