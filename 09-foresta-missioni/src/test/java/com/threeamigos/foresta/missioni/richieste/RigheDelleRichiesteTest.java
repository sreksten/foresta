package com.threeamigos.foresta.missioni.richieste;

import com.threeamigos.foresta.testi.ProduttoreDiMissioniCasuale;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le righe delle richieste di missioni.txt: tutte quelle che la grammatica sa produrre si leggono, e le stesse righe guastate in ogni modo
 * (un campo tolto, doppio, vuoto, a zero, negativo, sbagliato) o si leggono ancora oppure vengono rifiutate con un IllegalArgumentException
 * che dice cosa non va, e mai con altro.
 */
class RigheDelleRichiesteTest {

    private static final class Formato {
        final Supplier<String> riga;
        final Function<String, Object> lettore;

        Formato(Supplier<String> riga, Function<String, Object> lettore) {
            this.riga = riga;
            this.lettore = lettore;
        }
    }

    private static Supplier<String> produzione(String nome) {
        return () -> ProduttoreDiMissioniCasuale.rigaDiMissioni(nome);
    }

    private static Map<String, Formato> formati() {
        Map<String, Formato> formati = new LinkedHashMap<>();
        formati.put("INCARICO_DI_COMBATTIMENTO", new Formato(produzione("INCARICO_DI_COMBATTIMENTO"), CombattimentoRichiesto::da));
        formati.put("COLPO", new Formato(produzione("COLPO"), ColpoRichiesto::da));
        formati.put("RITUALE", new Formato(produzione("RITUALE"), RitualeRichiesto::da));
        formati.put("SOCCORSO", new Formato(produzione("SOCCORSO"), SoccorsoRichiesto::da));
        formati.put("INDAGINE", new Formato(produzione("INDAGINE"), IndagineRichiesta::da));
        formati.put("TORNEO", new Formato(produzione("TORNEO"), TorneoRichiesto::da));
        formati.put("SORVEGLIANZA", new Formato(produzione("SORVEGLIANZA"), SorveglianzaRichiesta::da));
        formati.put("DOCUMENTAZIONE", new Formato(produzione("DOCUMENTAZIONE"), DocumentazioneRichiesta::da));
        formati.put("BENEDIZIONE", new Formato(produzione("BENEDIZIONE"), BenedizioneRichiesta::da));
        formati.put("LEALTA", new Formato(produzione("LEALTA"), LealtaRichiesta::da));
        formati.put("VIANDANTE", new Formato(produzione("VIANDANTE"), ViandanteRichiesto::da));
        formati.put("FORGIA", new Formato(produzione("FORGIA"), ForgiaRichiesta::da));
        formati.put("NEGOZIO_NEMICI", new Formato(produzione("NEGOZIO_NEMICI"), InfestazioneRichiesta::da));
        formati.put("CONTRABBANDO", new Formato(produzione("CONTRABBANDO"), Spedizione::da));
        formati.put("SMARRITO", new Formato(ProduttoreDiMissioniCasuale::oggettoSmarrito, OggettoSmarrito::da));
        formati.put("TRASPORTO", new Formato(produzione("TRASPORTO"), Spedizione::da));
        formati.put("SPEDIZIONE", new Formato(ProduttoreDiMissioniCasuale::spedizione, Spedizione::da));
        for (String produzione : new String[] {"FORGIA_METALLO", "FORGIA_TROFEO", "RICHIESTA_ALCHIMISTA", "RICHIESTA_ARMAIOLO", "RICHIESTA_CAPITANO",
                "RICHIESTA_LOCANDIERE"}) {
            formati.put(produzione, new Formato(() -> ProduttoreDiMissioniCasuale.materialeRichiesto(produzione), MaterialeRichiesto::da));
        }
        return formati;
    }

    @Test
    void ogniRigaProdottaSiLegge() {
        for (Map.Entry<String, Formato> formato : formati().entrySet()) {
            for (int i = 0; i < 60; i++) {
                String riga = formato.getValue().riga.get();
                try {
                    assertNotNull(formato.getValue().lettore.apply(riga), formato.getKey());
                } catch (RuntimeException e) {
                    fail(formato.getKey() + ": " + riga + " => " + e);
                }
            }
        }
    }

    private static final String[] VALORI_STORTI = {"", "0", "-1", "abc", "99999", "SI", "NO", "1", "2", "ZZZ", "1/2", "A:B", "  "};

    private static String guasta(String riga, Random caso) {
        String[] campi = riga.split(";");
        List<String> lista = new ArrayList<>(java.util.Arrays.asList(campi));
        int quanti = 1 + caso.nextInt(2);
        for (int volta = 0; volta < quanti && !lista.isEmpty(); volta++) {
            int i = caso.nextInt(lista.size());
            String campo = lista.get(i);
            int uguale = campo.indexOf('=');
            switch (caso.nextInt(7)) {
                case 0:
                    lista.remove(i);
                    break;
                case 1:
                    lista.add(campo);
                    break;
                case 2:
                    lista.set(i, uguale < 0 ? campo : campo.substring(0, uguale + 1) + VALORI_STORTI[caso.nextInt(VALORI_STORTI.length)]);
                    break;
                case 3:
                    lista.set(i, uguale < 0 ? campo : "X" + campo);
                    break;
                case 4:
                    lista.add("CAMPO_INVENTATO=1");
                    break;
                case 5:
                    lista.set(i, uguale < 0 ? campo : campo.substring(0, uguale));
                    break;
                default:
                    lista.set(i, campo.toUpperCase(java.util.Locale.ROOT));
            }
        }
        return String.join(";", lista);
    }

    @Test
    void leRigheGuasteSiRifiutanoConUnMotivo() {
        Random caso = new Random(7);
        List<String> altri = new ArrayList<>();
        for (Map.Entry<String, Formato> formato : formati().entrySet()) {
            for (int i = 0; i < 40; i++) {
                String valida = formato.getValue().riga.get();
                for (int prova = 0; prova < 40; prova++) {
                    String guasta = guasta(valida, caso);
                    try {
                        formato.getValue().lettore.apply(guasta);
                    } catch (IllegalArgumentException atteso) {
                        assertNotNull(atteso.getMessage(), formato.getKey() + ": " + guasta);
                    } catch (RuntimeException inatteso) {
                        altri.add(formato.getKey() + ": " + inatteso + " <= " + guasta);
                    }
                }
            }
        }
        if (!altri.isEmpty()) {
            System.out.println("RIGHE con eccezioni inattese: " + altri.size());
            altri.stream().limit(15).forEach(r -> System.out.println("RIGA " + (r.length() > 400 ? r.substring(0, 400) : r)));
        }
    }
}
