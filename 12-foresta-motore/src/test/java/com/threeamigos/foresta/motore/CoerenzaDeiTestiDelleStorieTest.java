package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.missioni.incarichi.TestiDiUnIncarico;
import com.threeamigos.foresta.missioni.principali.CronacheDiUnFegatoEroico;
import com.threeamigos.foresta.missioni.principali.DisturbatoreDellaQuietePubblica;
import com.threeamigos.foresta.missioni.principali.IlGiroDelleSetteChiese;
import com.threeamigos.foresta.missioni.principali.NessunBoccaleLasciatoIndietro;
import com.threeamigos.foresta.missioni.principali.SconfiggiIlDrago;
import com.threeamigos.foresta.missioni.principali.SconfiggiIlLich;
import com.threeamigos.foresta.missioni.principali.SconfiggiIlMinotauroGigante;
import com.threeamigos.foresta.missioni.principali.SconfiggiLIdra;
import com.threeamigos.foresta.missioni.principali.SconfiggiLaStrega;
import com.threeamigos.foresta.missioni.storie.FabbricheDiProvaDelleStorie;
import com.threeamigos.foresta.missioni.storie.IlViaggioDellaMoglie;
import com.threeamigos.foresta.missioni.storie.LaBenedizione;
import com.threeamigos.foresta.missioni.storie.LaLealta;
import com.threeamigos.foresta.missioni.storie.LaSfidaDeiCampioni;
import com.threeamigos.foresta.missioni.storie.NonSparateSulPianista;
import com.threeamigos.foresta.testi.Testi;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.testi.ProduttoriDiTesti;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I testi delle storie portate alla grammatica (missioni.txt), in tutte le lingue: il titolo, la descrizione di ogni passo e le frasi
 * fisse, riempite con i valori della missione, senza segnaposto rimasti e con l'iniziale maiuscola e la punteggiatura finale.
 * Le storie non sono incarichi in città e si costruiscono dalle fabbriche del loro pacchetto, quindi il test sta qui.
 */
class CoerenzaDeiTestiDelleStorieTest {

    private static final String[] LINGUE = {"it", "en", "en-GB"};

    /** I nomi e le produzioni a esaurimento consumati qui non devono arrivare ai test che li aspettano come a inizio partita. */
    @AfterEach
    void ripristinaLaGrammatica() {
        ProduttoriDiTesti.resetProduzioni();
    }

    @Test
    void IlCampione() {
        controlla(() -> FabbricheDiProvaDelleStorie.ilCampione(), new String[] {"POSTO", "SFIDA"},
                "IL_CAMPIONE_POSTO_SEGNATO", "IL_CAMPIONE_BATTUTO");
    }

    @Test
    void LaBenedizione() {
        controlla(LaBenedizione::new, new String[] {"INCONTRO", "FAVORE", "TEMPIO", "ARRIVO"},
                "LA_BENEDIZIONE_INCONTRO_1", "LA_BENEDIZIONE_PREMIO", "LA_BENEDIZIONE_COMPAGNO");
    }

    @Test
    void LaLealta() {
        controlla(() -> {
            LaLealta lealta = new LaLealta();
            lealta.aggiungiProprieta("NOME_DEL_COMPAGNO", "Gervasio");
            return lealta;
        }, new String[] {"FAVORE", "ATTESA"}, "LA_LEALTA_CONFIDENZA", "LA_LEALTA_RINGRAZIAMENTO", "LA_LEALTA_COMPAGNO_PERSO");
    }

    @Test
    void IlFavore() {
        controlla(FabbricheDiProvaDelleStorie::ilFavore, new String[] {"RACCOLTA", "POSTO", "VEGLIA"}, "IL_FAVORE_POSTO_SEGNATO");
    }

    @Test
    void LaSfidaDeiCampioni() {
        controlla(LaSfidaDeiCampioni::new, new String[0]);
    }

    @Test
    void IlViandante() {
        controlla(FabbricheDiProvaDelleStorie::ilViandante, new String[] {"ANDATA", "CACCIA", "RITORNO"},
                "IL_VIANDANTE_ACCETTATO", "IL_VIANDANTE_CITTA_DISTRUTTA", "IL_VIANDANTE_RICOMPENSA");
    }

    @Test
    void NonSparateSulPianista() {
        controlla(NonSparateSulPianista::new, new String[] {"VIAGGIO"}, "NON_SPARATE_SUL_PIANISTA_ACCETTATO");
    }

    @Test
    void IlViaggioDellaMoglie() {
        controlla(IlViaggioDellaMoglie::new, new String[] {"INCARICO"}, "IL_VIAGGIO_DELLA_MOGLIE_BARDO_3", "IL_VIAGGIO_DELLA_MOGLIE_MOGLIE_1");
    }

    @Test
    void IlGiroDelleSetteChiese() {
        controlla(IlGiroDelleSetteChiese::new, new String[0], "IL_GIRO_DELLE_SETTE_CHIESE_DESCRIZIONE_COMPLETA", "IL_GIRO_DELLE_SETTE_CHIESE_INCONTRO_TITOLO",
                "IL_GIRO_DELLE_SETTE_CHIESE_INCONTRO_1", "IL_GIRO_DELLE_SETTE_CHIESE_INCONTRO_3", "IL_GIRO_DELLE_SETTE_CHIESE_INCONTRO_5",
                "IL_GIRO_DELLE_SETTE_CHIESE_RINGRAZIAMENTO_TITOLO", "IL_GIRO_DELLE_SETTE_CHIESE_RINGRAZIAMENTO_1", "IL_GIRO_DELLE_SETTE_CHIESE_UNISCITI_M",
                "IL_GIRO_DELLE_SETTE_CHIESE_UNISCITI_F");
    }

    @Test
    void ILeBossDelDrago() {
        controlla(SconfiggiIlLich::new, new String[0], "SCONFIGGI_IL_LICH_VITTORIA", "SCONFIGGI_IL_LICH_RICORDO");
        controlla(SconfiggiLIdra::new, new String[0], "SCONFIGGI_L_IDRA_VITTORIA", "SCONFIGGI_L_IDRA_RICORDO");
        controlla(SconfiggiLaStrega::new, new String[0], "SCONFIGGI_LA_STREGA_VITTORIA", "SCONFIGGI_LA_STREGA_RICORDO");
        controlla(SconfiggiIlMinotauroGigante::new, new String[0], "SCONFIGGI_IL_MINOTAURO_GIGANTE_VITTORIA", "SCONFIGGI_IL_MINOTAURO_GIGANTE_RICORDO");
        controlla(SconfiggiIlDrago::new, new String[0], "SCONFIGGI_IL_DRAGO_DESCRIZIONE_APERTO", "SCONFIGGI_IL_DRAGO_CASTELLO_RIVELATO", "SCONFIGGI_IL_DRAGO_RICORDO");
    }

    @Test
    void LeAltrePrincipali() {
        controlla(DisturbatoreDellaQuietePubblica::new, new String[0], "DISTURBATORE_DELLA_QUIETE_PUBBLICA_FINE");
        controlla(NessunBoccaleLasciatoIndietro::new, new String[0], "NESSUN_BOCCALE_LASCIATO_INDIETRO_FINE");
        controlla(CronacheDiUnFegatoEroico::new, new String[0], "CRONACHE_DI_UN_FEGATO_EROICO_INCOMPIUTE", "CRONACHE_DI_UN_FEGATO_EROICO_COMPLETE");
    }

    /** Controlla una storia in tutte le lingue: nome, descrizione, ricordo, descrizione di ogni passo e le produzioni date. */
    private static void controlla(Supplier<? extends com.threeamigos.foresta.missioni.Missione> fabbrica, String[] passi, String... produzioni) {
        for (String lingua : LINGUE) {
            Testi.impostaLingua(lingua);
            try (PartitaDiTest partita = PartitaDiTest.nuova(90)) {
                partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_FLEENA));
                com.threeamigos.foresta.missioni.Missione storia = fabbrica.get();
                String dove = storia.getClass().getSimpleName() + " " + lingua + ": ";
                List<String> frasi = new ArrayList<>();
                String titolo = storia.getNome(); // un titolo non finisce con la punteggiatura
                assertFalse(titolo.isEmpty() || titolo.matches("(?s).*%[A-Z_0-9]+%.*") || titolo.contains("null"), dove + titolo);
                assertEquals(storia.getNome(), storia.getNome(), dove + "il titolo non cambia");
                String descrizione = storia.getDescrizione(); // una descrizione può essere un'etichetta senza punto finale
                controllaFrase(dove, descrizione, false);
                String ricordo = storia.getRicordoDellaLocazione();
                if (ricordo != null) {
                    frasi.add(ricordo);
                }
                frasi.addAll(TestiDiUnIncarico.testiDuranteLaMissione(storia, passi, produzioni));
                frasi.forEach(f -> controllaFrase(dove, f, true));
            } finally {
                Testi.impostaLingua("it");
            }
        }
    }

    private static void controllaFrase(String dove, String frase, boolean conPunteggiatura) {
        String descrizione = dove + "«" + frase + "»";
        assertNotNull(frase, descrizione);
        assertFalse(frase.trim().isEmpty(), descrizione);
        assertFalse(frase.matches("(?s).*%[A-Z_0-9]+%.*"), descrizione + ": segnaposto rimasto");
        assertFalse(frase.contains("{") || frase.contains("}"), descrizione + ": segnaposto rimasto");
        assertFalse(frase.contains("null"), descrizione + ": valore mancante");
        assertFalse(frase.contains("  "), descrizione + ": spazi doppi");
        assertEquals(frase, frase.trim(), descrizione + ": spazi ai bordi");
        int iniziale = frase.codePointAt(0);
        assertTrue(Character.isUpperCase(iniziale) || "“\"«¡¿".indexOf(iniziale) >= 0, descrizione + ": non comincia con la maiuscola");
        assertTrue(!conPunteggiatura || ".!?:”\"…".indexOf(frase.charAt(frase.length() - 1)) >= 0, descrizione + ": non finisce con la punteggiatura");
    }
}
