package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.SnifferBusEventi;
import com.threeamigos.foresta.SorgentiDelProgetto;
import com.threeamigos.foresta.eventi.interni.InternoDecisioneTattica;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.mappa.TipoLocazione;
import com.threeamigos.foresta.tipi.tattiche.TipoAzione;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SnifferBusEventiTest {

    @Test
    void loSnifferScriveLaDecisioneNelLogConIlSuoTipoPerIlGrep() {
        java.io.ByteArrayOutputStream catturato = new java.io.ByteArrayOutputStream();
        java.io.PrintStream originale = System.out;
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(609)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            new SnifferBusEventi();
            System.setOut(new java.io.PrintStream(catturato, true));
            Personaggio capo = partita.gruppo().getCapo();
            com.threeamigos.foresta.eventi.BusEventi.pubblica(new InternoDecisioneTattica(capo, null, TipoAzione.CANTO_DI_VALORE, 2,
                    Arrays.asList("voce 1: disattivata", "voce 3: CANTO_DI_VALORE su tutto il gruppo")));
        } finally {
            System.setOut(originale);
        }
        String riga = new String(catturato.toByteArray(), java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(riga.contains("INTERNO_DECISIONE_TATTICA"), riga);
        assertTrue(riga.contains("decide CANTO_DI_VALORE (voce 3)"), riga);
        assertTrue(riga.contains("voce 1: disattivata | voce 3: CANTO_DI_VALORE su tutto il gruppo"), riga);
    }

    /**
     * Lo sniffer ascolta qualsiasi evento. Il bus consegna per classe esatta, quindi ogni classe di evento che si
     * pubblica (quelle concrete e senza sottoclassi, che derivano da EventoBase) deve avere la sua iscrizione.
     */
    @Test
    void loSnifferSiIscriveAOgniEvento() throws IOException {
        Pattern dichiarazione = Pattern.compile("^public\\s+(abstract\\s+)?(?:final\\s+)?class\\s+(\\w+)(?:<[^>]*>)?\\s+extends\\s+(\\w+)", Pattern.MULTILINE);
        Map<String, String> genitori = new HashMap<>();
        Set<String> astratte = new HashSet<>();
        for (Path sorgente : SorgentiDelProgetto.sorgenti(false)) {
            String testo = new String(Files.readAllBytes(sorgente), StandardCharsets.UTF_8);
            Matcher m = dichiarazione.matcher(testo);
            while (m.find()) {
                genitori.put(m.group(2), m.group(3));
                if (m.group(1) != null) {
                    astratte.add(m.group(2));
                }
            }
        }
        Set<String> conSottoclassi = new HashSet<>(genitori.values());
        Set<String> eventi = new TreeSet<>();
        for (String classe : genitori.keySet()) {
            String corrente = classe;
            while (genitori.containsKey(corrente) && !corrente.equals("EventoBase")) {
                corrente = genitori.get(corrente);
            }
            if (corrente.equals("EventoBase") && !astratte.contains(classe) && !conSottoclassi.contains(classe)) {
                eventi.add(classe);
            }
        }
        assertTrue(eventi.size() > 100, "Gli eventi trovati sono troppo pochi, la ricerca non funziona: " + eventi);

        Path sniffer = SorgentiDelProgetto.radice().resolve("foresta-app/src/main/java/com/threeamigos/foresta/SnifferBusEventi.java");
        String testoSniffer = new String(Files.readAllBytes(sniffer), StandardCharsets.UTF_8);
        Set<String> senzaIscrizione = new TreeSet<>();
        for (String evento : eventi) {
            if (!testoSniffer.contains("iscriviti(" + evento + ".class")) {
                senzaIscrizione.add(evento);
            }
        }
        assertTrue(senzaIscrizione.isEmpty(), "SnifferBusEventi non ascolta questi eventi: " + senzaIscrizione);
    }
}
