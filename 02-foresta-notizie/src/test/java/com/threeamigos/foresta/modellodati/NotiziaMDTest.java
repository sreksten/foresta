package com.threeamigos.foresta.modellodati;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NotiziaMDTest {

    @Test
    void unaNotiziaSiScriveESiRilegge() throws IOException {
        NotiziaMD notizia = new NotiziaMD("N1", "Titolo - Corpo");
        StringWriter scrittura = new StringWriter();
        try (PrintWriter writer = new PrintWriter(scrittura)) {
            notizia.salva(writer);
        }

        NotiziaMD letta = new NotiziaMD();
        letta.leggi(new BufferedReader(new StringReader(scrittura.toString())));

        assertEquals("N1", letta.getId());
        assertEquals("Titolo - Corpo", letta.getCorpo());
    }

    @Test
    void ilSeparatoreSparisceDaIdECorpo() {
        NotiziaMD notizia = new NotiziaMD("N|1", "Titolo | Corpo");
        assertEquals("N1", notizia.getId());
        assertEquals("Titolo  Corpo", notizia.getCorpo());

        notizia.setId("M|2");
        notizia.setCorpo("Un | altro");
        assertEquals("M2", notizia.getId());
        assertEquals("Un  altro", notizia.getCorpo());
    }
}
