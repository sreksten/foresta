package com.threeamigos.foresta.testi;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.personaggi.Bardo;
import com.threeamigos.foresta.personaggi.Elfo;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.personaggi.Ladro;
import com.threeamigos.foresta.personaggi.Mago;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Ogni alternativa di ogni produzione NOTIZIE_* (le notizie delle locande e quelle delle città, in locande.txt e nelle sue versioni
 * inglesi) ha la forma {@code ID-Titolo - Corpo}, ha un identificativo che nessun'altra notizia ha, e con un gruppo che ha tutte le classi
 * di cui parlano le notizie si applica e non lascia segnaposto: le notizie non si possono produrre tutte a caso in una prova, e un
 * errore in una sola farebbe lo sbaglio in partita.
 */
class TutteLeNotizieSonoValideTest {

    private static final String[] FILE = {"locande.txt", "locande_en.txt", "locande_en_GB.txt"};
    private static final Pattern PRODUZIONE = Pattern.compile("^(NOTIZIE_[A-Z0-9_]+)\\s*$");
    private static final Pattern RIFERIMENTO = Pattern.compile("^\\s*\\[(NOTIZIE_[A-Z0-9_]+)]\\s*$");
    // Un segnaposto rimasto: parole fatte solo di maiuscole e underscore, di almeno tre lettere
    private static final Pattern SEGNAPOSTO = Pattern.compile("\\b[A-Z][A-Z_]{2,}\\b");

    @BeforeEach
    @AfterEach
    void gruppoPulito() {
        BusEventi.azzera();
        ModelloDati.installa();
        ModelloDati.setIstanza(new ModelloDati());
        GruppoGiocatore.azzeraIstanza();
    }

    private static void gruppoCompleto() {
        GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
        gruppo.aggiungiPersonaggio(new Guerriero("Arsenio", 1));
        gruppo.aggiungiPersonaggio(new Bardo("Dante", 1));
        gruppo.aggiungiPersonaggio(new Ladro("Gualtiero", 1));
        gruppo.aggiungiPersonaggio(new Mago("Merlino", 1));
        gruppo.aggiungiPersonaggio(new Elfo("Legolas", 1));
    }

    /** I nomi delle produzioni NOTIZIE_* del file. */
    private static List<String> produzioni(String file) throws IOException {
        List<String> nomi = new ArrayList<>();
        try (BufferedReader lettore = new BufferedReader(new InputStreamReader(
                TutteLeNotizieSonoValideTest.class.getResourceAsStream("/com/threeamigos/foresta/testi/" + file), StandardCharsets.UTF_8))) {
            String riga;
            while ((riga = lettore.readLine()) != null) {
                Matcher produzione = PRODUZIONE.matcher(riga);
                if (produzione.matches()) {
                    nomi.add(produzione.group(1));
                }
            }
        }
        return nomi;
    }

    @Test
    void ognunaHaLaFormaGiustaUnIdentificativoSuoEInUnGruppoCompletoSiApplicaSenzaSegnaposto() throws IOException {
        gruppoCompleto();
        String[] lingue = {"it", "en", "en-GB"};
        for (int l = 0; l < FILE.length; l++) {
            Testi.impostaLingua(lingue[l]);
            ProduttoriDiTesti.resetProduzioni();
            try {
                List<String> nomi = produzioni(FILE[l]);
                assertTrue(nomi.contains("NOTIZIE_CITTA"), FILE[l] + ": mancano le notizie delle città");
                // Quale produzione ha dato quale identificativo
                Map<String, String> produzioneDi = new LinkedHashMap<>();
                for (String nome : nomi) {
                    String identificativo = nome.substring("NOTIZIE_".length());
                    // Le alternative hanno parti a caso (i sotto-testi fra parentesi quadre): se ne pescano tante
                    for (int giro = 0; giro < CAMPIONI; giro++) {
                        String alternativa = ProduttoreDiLocandeCasuale.notiziaGrezza(identificativo);
                        String dove = FILE[l] + " " + nome + ": " + alternativa;
                        int separatore = alternativa.indexOf('-');
                        if (separatore <= 0) { difetti.add(dove + ": manca «ID-»"); continue; }
                        String id = alternativa.substring(0, separatore);
                        if (!id.matches("[A-Z]+[0-9]+")) { difetti.add(dove + ": identificativo strano"); }
                        // Lo stesso identificativo in due produzioni farebbe passare per "già pubblicata" una notizia che non lo è; le notizie
                        // generiche (LG, riprese in ogni locanda con [NOTIZIE_LOCANDA]) stanno in più produzioni di proposito
                        if (!id.startsWith("LG")) {
                            String altra = produzioneDi.putIfAbsent(id, nome);
                            if (altra != null && !altra.equals(nome)) { difetti.add(dove + ": l'identificativo " + id + " è anche di " + altra); }
                        }
                        String corpo = alternativa.substring(separatore + 1);
                        if (!corpo.contains(" - ")) { difetti.add(dove + ": manca « - » fra titolo e corpo"); }
                        String testo = RegistroNotizie.validaNotizia(corpo);
                        if (testo == null) { difetti.add(dove + ": con un gruppo completo non si applica"); continue; }
                        testo = RegistroNotizie.conIlNomeDellaLocanda(testo, "La Locanda").replace("NOME_LOCANDIERE", "Oste");
                        Matcher rimasto = SEGNAPOSTO.matcher(testo);
                        while (rimasto.find()) {
                            if (!CONSENTITE.contains(rimasto.group())) { difetti.add(FILE[l] + " " + nome + " " + id + ": segnaposto o maiuscolo rimasto: " + rimasto.group()); }
                        }
                        if (testo.contains("  ")) { difetti.add(dove + ": spazi doppi"); }
                        if (testo.contains("null") || testo.contains("[") || testo.contains("]")) { difetti.add(dove + ": resti di grammatica"); }
                    }
                }
            } finally {
                Testi.impostaLingua("it");
                ProduttoriDiTesti.resetProduzioni();
            }
        }
        assertTrue(difetti.isEmpty(), "Difetti nelle notizie:\n" + String.join("\n", difetti));
    }

    private static final int CAMPIONI = 60;
    private final Set<String> difetti = new java.util.TreeSet<>();

    // Sigle e nomi di stati che le notizie scrivono volutamente in maiuscolo ("applicandogli lo stato INFETTATO")
    private static final Set<String> CONSENTITE = new HashSet<>(java.util.Arrays.asList("DM", "NPC", "BAGNATO", "INFETTATO", "IOU"));
}
