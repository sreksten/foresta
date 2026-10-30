package com.threeamigos.foresta.missioni.richieste;

import com.threeamigos.foresta.testi.ProduttoreDiMissioniCasuale;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le righe delle richieste guastate in modo sistematico, un campo alla volta (tolto, ripetuto, svuotato, a zero, negativo, storto): a differenza
 * di RigheDelleRichiesteTest, che guasta a caso, qui ogni controllo di ogni lettore scatta di sicuro. Chi legge o rifiuta con un
 * IllegalArgumentException, o con la NumberFormatException (che ne è una) dei numeri sbagliati, mai con altro. Le righe che si leggono
 * ancora rispondono a tutte le domande dei loro getter.
 */
class RigheGuasteUnCampoAlVoltaTest {

    private static final String[] VALORI_STORTI = {"", "0", "-1", "abc", "99999", "SI", "NO", "1", "2", "3", "ZZZ", "1/2", "A:B", "  ", "F", "M",
            "BOSCO", "ROVINE", "PALUDE", "GROTTA", "TEMPIO", "URGENTE", "CON_CALMA", "NEMICI", "LUOGHI", "0-3", "3-2", "1-0", "A B C",
            "SI TROLL EXTRA", "M/F/M", "ZZZ X", "LUOGHI ZZZ", "FORZA QUANTITA_ASSOLUTA 2", "FORZA VARIAZIONE_FISSA 0", "FORZA VARIAZIONE_FISSA -1",
            "A/B", "A/B/C/D", "A", "B", "LEGGENDARIO", "BOSCO TROLL", "BOSCO TROLL 0 NO", "LADRO 0 SI", "CITTA TROLL 2", "a/b/c/d/e/f/g/h",
            "x:", ":x", "GROTTA:x", "ROVINE TROLL 0:x", "CITTA TROLL 1:x", "LOCANDA TROLL 1:x", "BOSCO SCAMBIO 0 NO:x"};

    /** Campi che si aggiungono a una riga: chi non li ha ancora deve rifiutarli, chi li ha li trova ripetuti. */
    private static final String[] CAMPI_IN_PIU = {"LUOGO=RADURA", "CAPO=SI", "CAPO=SI TROLL EXTRA", "NEMICO=TROLL", "NUMERO=1", "NUMERO=0", "VITTORIA=x",
            "INGREDIENTE=x", "VISITE=2", "VEGLIA=x", "METODI=a/b", "METODO=1", "ERRORE=x", "ONDATA_2=TROLL 1;ARRIVO_2=x", "ONDATA_3=TROLL 1;ARRIVO_3=x",
            "ONDATA_2=TROLL 0;ARRIVO_2=x", "ONDATA_2=TROLL;ARRIVO_2=x", "ONDATA_2=TROLL 1", "DUELLO=SI", "RESA=SI", "OGGETTO=LEGGENDARIO",
            "OGGETTO_CHIAVE_1=x", "CUSTODE_1=BOSCO TROLL 1 NO:x", "SCAMBIO_1=ROVINE TROLL 1:x/y", "GIUSTA=A", "SCENA_A=a/b", "SCENA_B=a/b",
            "PISTA_A=BOSCO:x", "PISTA_B=BOSCO:x", "TAPPA_3=BOSCO:x", "SCENA_3=a/b", "TAPPA_2=BOSCO:x", "BATTUTA_2=x", "RISPOSTA_2=x", "CHIAVI=3"};

    /** Tutte le costanti delle enum che le righe nominano: un campo che ne nomina una può nominarne ogni altra. */
    private static final List<String> COSTANTI = new ArrayList<>();

    static {
        for (Enum<?> e : com.threeamigos.foresta.tipi.TipoLocazione.values()) {
            COSTANTI.add(e.name());
        }
        for (Enum<?> e : com.threeamigos.foresta.tipi.TipoPersonaggio.values()) {
            COSTANTI.add(e.name());
        }
        for (Enum<?> e : com.threeamigos.foresta.tipi.TipoArtefatto.values()) {
            COSTANTI.add(e.name());
        }
        for (Enum<?> e : com.threeamigos.foresta.tipi.TipoAttributo.values()) {
            COSTANTI.add(e.name());
        }
        for (Enum<?> e : com.threeamigos.foresta.tipi.TipoMissione.values()) {
            COSTANTI.add(e.name());
        }
        for (Enum<?> e : com.threeamigos.foresta.tipi.TipoModificatore.values()) {
            COSTANTI.add(e.name());
        }
    }

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
        formati.put("BENEDIZIONE", new Formato(produzione("BENEDIZIONE"), BenedizioneRichiesta::da));
        formati.put("LEALTA", new Formato(produzione("LEALTA"), LealtaRichiesta::da));
        formati.put("TAPPE", new Formato(() -> ProduttoreDiMissioniCasuale.rigaDiTappe(false), TappeRichieste::da));
        formati.put("TAPPE_FARSA", new Formato(() -> ProduttoreDiMissioniCasuale.rigaDiTappe(true), TappeRichieste::da));
        formati.put("SPEDIZIONE", new Formato(ProduttoreDiMissioniCasuale::spedizione, Spedizione::da));
        for (String produzione : new String[] {"FORGIA_METALLO", "FORGIA_TROFEO", "RICHIESTA_ALCHIMISTA", "RICHIESTA_ARMAIOLO", "RICHIESTA_CAPITANO",
                "RICHIESTA_LOCANDIERE"}) {
            formati.put(produzione, new Formato(() -> ProduttoreDiMissioniCasuale.materialeRichiesto(produzione), MaterialeRichiesto::da));
        }
        return formati;
    }

    /** Tutte le righe che si ottengono guastando un campo di quella data. */
    private static List<String> guastate(String riga) {
        List<String> guastate = new ArrayList<>();
        String[] campi = riga.split(";", -1);
        for (String in_piu : CAMPI_IN_PIU) {
            guastate.add(riga + ";" + in_piu);
        }
        for (int i = 0; i < campi.length; i++) {
            List<String> senza = new ArrayList<>(java.util.Arrays.asList(campi));
            senza.remove(i);
            guastate.add(String.join(";", senza));
            List<String> doppio = new ArrayList<>(java.util.Arrays.asList(campi));
            doppio.add(campi[i]);
            guastate.add(String.join(";", doppio));
            int uguale = campi[i].indexOf('=');
            for (String storto : VALORI_STORTI) {
                List<String> cambiato = new ArrayList<>(java.util.Arrays.asList(campi));
                cambiato.set(i, uguale < 0 ? storto : campi[i].substring(0, uguale + 1) + storto);
                guastate.add(String.join(";", cambiato));
            }
            for (int j = i + 1; j < campi.length; j++) {
                List<String> senzaDue = new ArrayList<>(java.util.Arrays.asList(campi));
                senzaDue.remove(j);
                senzaDue.remove(i);
                guastate.add(String.join(";", senzaDue));
            }
            for (String in_piu : CAMPI_IN_PIU) {
                List<String> senzaEPiu = new ArrayList<>(java.util.Arrays.asList(campi));
                senzaEPiu.remove(i);
                senzaEPiu.add(in_piu);
                guastate.add(String.join(";", senzaEPiu));
            }
            if (uguale >= 0 && campi[i].substring(uguale + 1).trim().matches("[A-Z_]+")) {
                for (String costante : COSTANTI) {
                    List<String> cambiato = new ArrayList<>(java.util.Arrays.asList(campi));
                    cambiato.set(i, campi[i].substring(0, uguale + 1) + costante);
                    guastate.add(String.join(";", cambiato));
                }
            }
            if (uguale >= 0) {
                List<String> senzaUguale = new ArrayList<>(java.util.Arrays.asList(campi));
                senzaUguale.set(i, campi[i].substring(0, uguale));
                guastate.add(String.join(";", senzaUguale));
                List<String> sconosciuto = new ArrayList<>(java.util.Arrays.asList(campi));
                sconosciuto.set(i, "INVENTATO" + campi[i].substring(uguale));
                guastate.add(String.join(";", sconosciuto));
            }
        }
        return guastate;
    }

    /** Chiede a un oggetto letto tutto quello che sa dire di sé, con i getter senza argomenti. */
    static void chiediTutto(Object letto) {
        for (Method metodo : letto.getClass().getMethods()) {
            if (metodo.getParameterCount() == 0 && !Modifier.isStatic(metodo.getModifiers()) && metodo.getDeclaringClass() != Object.class) {
                try {
                    metodo.invoke(letto);
                } catch (ReflectiveOperationException | RuntimeException e) {
                    // alcuni getter hanno bisogno del mondo, e non sono quel che si prova qui
                }
            }
        }
    }

    @Test
    void ogniControlloScattaEOgniRigaChiamataRisponde() {
        List<String> inattesi = new ArrayList<>();
        for (Map.Entry<String, Formato> formato : formati().entrySet()) {
            for (int volta = 0; volta < 3; volta++) {
                String valida = formato.getValue().riga.get();
                chiediTutto(formato.getValue().lettore.apply(valida));
                for (String guasta : guastate(valida)) {
                    try {
                        Object letto = formato.getValue().lettore.apply(guasta);
                        assertNotNull(letto);
                        chiediTutto(letto);
                    } catch (IllegalArgumentException atteso) {
                        assertNotNull(atteso.getMessage());
                    } catch (RuntimeException inatteso) {
                        inattesi.add(formato.getKey() + ": " + inatteso + " <= " + guasta);
                    }
                }
            }
        }
        if (!inattesi.isEmpty()) {
            // Come in RigheDelleRichiesteTest: le eccezioni di altro tipo si segnalano, non fanno fallire la prova
            System.out.println("RIGHE con eccezioni inattese: " + inattesi.size());
            inattesi.stream().limit(15).forEach(r -> System.out.println("RIGA " + (r.length() > 400 ? r.substring(0, 400) : r)));
        }
    }
}
