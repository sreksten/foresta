package com.threeamigos.foresta.missioni.richieste;

import com.threeamigos.foresta.testi.ProduttoreDiMissioniCasuale;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoModificatore;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I casi limite delle righe delle richieste che i guasti sistematici di RigheGuasteUnCampoAlVoltaTest non raggiungono perché hanno bisogno
 * di due campi insieme o di un valore preciso: duelli a ondate, indizi nei posti sbagliati, sospetti e metodi vuoti, sfidanti di troppo.
 */
class RigheCasiLimiteTest {

    private static void rifiuta(Function<String, Object> lettore, String riga) {
        assertThrows(IllegalArgumentException.class, () -> lettore.apply(riga), riga);
    }

    /** Una riga di combattimento con il numero di nemici che si vuole (come le produce la grammatica). */
    private static String combattimentoDa(String contiene) {
        for (int i = 0; i < 2000; i++) {
            String riga = ProduttoreDiMissioniCasuale.rigaDiMissioni("INCARICO_DI_COMBATTIMENTO");
            if (riga.contains(contiene) && !riga.contains("DUELLO=") && !riga.contains("ONDATA_")) {
                return riga;
            }
        }
        throw new IllegalStateException("Nessuna riga con " + contiene);
    }

    @Test
    void unDuelloSiSfidaDaSoliENonArrivaAOndate() {
        String riga = combattimentoDa("NUMERO=1;");
        assertNotNull(CombattimentoRichiesto.da(riga + ";DUELLO=SI"));
        rifiuta(CombattimentoRichiesto::da, riga.replace("NUMERO=1;", "NUMERO=2;") + ";DUELLO=SI");
        rifiuta(CombattimentoRichiesto::da, riga.replace("NUMERO=1;", "NUMERO=LIVELLO:1;") + ";DUELLO=SI");
        rifiuta(CombattimentoRichiesto::da, riga + ";DUELLO=SI;ONDATA_2=TROLL 1;ARRIVO_2=Arriva un troll.");
        assertNotNull(CombattimentoRichiesto.da(riga + ";ONDATA_2=TROLL 1;ARRIVO_2=Arriva un troll."));
        rifiuta(CombattimentoRichiesto::da, riga + ";ONDATA_3=TROLL 1;ARRIVO_3=Arriva un troll.");
        rifiuta(CombattimentoRichiesto::da, riga + ";ONDATA_2=TROLL 0;ARRIVO_2=Arriva un troll.");
        rifiuta(CombattimentoRichiesto::da, riga + ";ONDATA_2=TROLL;ARRIVO_2=Arriva un troll.");
        rifiuta(CombattimentoRichiesto::da, riga + ";ONDATA_2=TROLL 1");
        rifiuta(CombattimentoRichiesto::da, riga + ";CAPO=SI TROLL ALTRO");
    }

    @Test
    void leOndateSenzaNemicoDellaSorveglianzaSiRifiutano() {
        String riga = ProduttoreDiMissioniCasuale.rigaDiMissioni("SORVEGLIANZA");
        while (!riga.contains("NEMICO=") || riga.contains("ONDATA_")) {
            riga = ProduttoreDiMissioniCasuale.rigaDiMissioni("SORVEGLIANZA");
        }
        String senzaNemico = riga.replaceAll(";\\s*NEMICO=[A-Z_]+", "").replaceAll(";\\s*NUMERO=\\d+", "").replaceAll(";\\s*VITTORIA=[^;]*", "")
                .replaceAll(";\\s*CAPO=[^;]*", "");
        assertFalse(senzaNemico.contains("NEMICO="), senzaNemico);
        assertNotNull(SorveglianzaRichiesta.da(senzaNemico));
        rifiuta(SorveglianzaRichiesta::da, senzaNemico + ";ONDATA_2=TROLL 1;ARRIVO_2=Arriva un troll.");
        rifiuta(SorveglianzaRichiesta::da, senzaNemico + ";CAPO=SI");
        rifiuta(SorveglianzaRichiesta::da, senzaNemico + ";VITTORIA=Vinto.");
        rifiuta(SorveglianzaRichiesta::da, senzaNemico + ";NUMERO=2");
        rifiuta(SorveglianzaRichiesta::da, riga.replaceAll(";\\s*VITTORIA=[^;]*", ""));
        rifiuta(SorveglianzaRichiesta::da, riga.replaceAll("NUMERO=\\d+", "NUMERO=0"));
    }

    @Test
    void ilRitualeControllaNemiciEMetodi() {
        String riga = ProduttoreDiMissioniCasuale.rigaDiMissioni("RITUALE");
        String senzaNemico = riga.replaceAll(";\\s*NEMICO=[A-Z_]+", "").replaceAll(";\\s*NUMERO=\\d+", "").replaceAll(";\\s*VITTORIA=[^;]*", "")
                .replaceAll(";\\s*CAPO=[^;]*", "").replaceAll(";\\s*METODI=[^;]*", "").replaceAll(";\\s*METODO=[^;]*", "")
                .replaceAll(";\\s*ERRORE=[^;]*", "");
        assertNotNull(RitualeRichiesto.da(senzaNemico));
        rifiuta(RitualeRichiesto::da, senzaNemico + ";CAPO=SI");
        rifiuta(RitualeRichiesto::da, senzaNemico + ";VITTORIA=Vinto.");
        rifiuta(RitualeRichiesto::da, senzaNemico + ";NUMERO=2");
        String conMetodi = senzaNemico + ";METODI=uno/due/tre;METODO=2;ERRORE=Sbagliato.";
        assertNotNull(RitualeRichiesto.da(conMetodi));
        rifiuta(RitualeRichiesto::da, senzaNemico + ";METODI=uno/due/tre;METODO=2");
        rifiuta(RitualeRichiesto::da, senzaNemico + ";METODI=uno/due/tre;METODO=4;ERRORE=Sbagliato.");
        rifiuta(RitualeRichiesto::da, senzaNemico + ";METODI=uno/due/tre;METODO=0;ERRORE=Sbagliato.");
        rifiuta(RitualeRichiesto::da, senzaNemico + ";METODI=uno//tre;METODO=1;ERRORE=Sbagliato.");
        rifiuta(RitualeRichiesto::da, senzaNemico + ";METODI=uno;METODO=1;ERRORE=Sbagliato.");
        rifiuta(RitualeRichiesto::da, senzaNemico + ";METODI=a/b/c/d/e/f/g/h/i/l;METODO=1;ERRORE=Sbagliato.");
        rifiuta(RitualeRichiesto::da, senzaNemico + ";METODO=1");
        rifiuta(RitualeRichiesto::da, senzaNemico + ";ERRORE=Sbagliato.");
        rifiuta(RitualeRichiesto::da, riga.replaceAll("MONETE=\\d+", "MONETE=0"));
    }

    @Test
    void gliIndiziStannoSoloNeiPostiAmmessi() {
        String riga = ProduttoreDiMissioniCasuale.rigaDiMissioni("INDAGINE");
        int accettati = 0;
        for (TipoLocazione posto : TipoLocazione.values()) {
            String cambiata = riga.replaceFirst("INDIZIO_1=[A-Z_]+:", "INDIZIO_1=" + posto.name() + ":");
            try {
                IndagineRichiesta.da(cambiata);
                accettati++;
            } catch (IllegalArgumentException rifiutata) {
                assertNotNull(rifiutata.getMessage());
            }
        }
        assertTrue(accettati > 0 && accettati < TipoLocazione.values().length, "alcuni posti sì e altri no: " + accettati);
        String sospetti = riga.replaceFirst("SOSPETTI=[^;]*", "SOSPETTI=@");
        assertNotNull(IndagineRichiesta.da(sospetti.replace("@", "uno/due/tre")));
        rifiuta(IndagineRichiesta::da, sospetti.replace("@", "uno"));
        rifiuta(IndagineRichiesta::da, sospetti.replace("@", "uno//tre"));
        rifiuta(IndagineRichiesta::da, sospetti.replace("@", "a/b/c/d/e/f/g/h/i/l/m/n"));
        rifiuta(IndagineRichiesta::da, riga.replaceFirst("COLPEVOLE=\\d+", "COLPEVOLE=9"));
        rifiuta(IndagineRichiesta::da, riga.replaceFirst("COLPEVOLE=\\d+", "COLPEVOLE=0"));
    }

    @Test
    void alTorneoGliSfidantiSonoQuelliGiusti() {
        String riga = ProduttoreDiMissioniCasuale.rigaDiMissioni("TORNEO");
        String sfidanti = riga.replaceFirst("SFIDANTI=[A-Z_ ]*", "SFIDANTI=@");
        assertNotNull(TorneoRichiesto.da(sfidanti.replace("@", "GUERRIERO ELFA")));
        rifiuta(TorneoRichiesto::da, sfidanti.replace("@", "GUERRIERO"));
        rifiuta(TorneoRichiesto::da, sfidanti.replace("@", "GUERRIERO ELFA MAGO"));
    }

    @Test
    void ilMaterialeDiceDaDoveViene() {
        String base = ProduttoreDiMissioniCasuale.materialeRichiesto("RICHIESTA_ALCHIMISTA");
        String[] campi = base.split(";", -1);
        for (String provenienza : new String[] {"LUOGHI RADURA", "LUOGHI BOSCO", "LUOGHI GROTTA", "LUOGHI ROVINE", "LUOGHI PALUDE", "LUOGHI TEMPIO",
                "LUOGHI RADURA BOSCO TEMPIO", "NEMICI TROLL", "NEMICI TROLL GOBLIN"}) {
            campi[3] = provenienza;
            MaterialeRichiesto materiale = MaterialeRichiesto.da(String.join(";", campi));
            assertFalse(materiale.getDaDoveViene().isEmpty(), provenienza);
        }
        campi[3] = "LUOGHI RADURA";
        campi[0] = "M/F/M";
        rifiuta(MaterialeRichiesto::da, String.join(";", campi));
        campi[0] = "M";
        campi[3] = "ALTROVE TEMPIO";
        rifiuta(MaterialeRichiesto::da, String.join(";", campi));
        campi[3] = "LUOGHI RADURA";
        campi[4] = "0-3";
        rifiuta(MaterialeRichiesto::da, String.join(";", campi));
        campi[4] = "4-3";
        rifiuta(MaterialeRichiesto::da, String.join(";", campi));
    }

    @Test
    void ilModificatoreDellaRigaDiceCheCosaFa() {
        ModificatoreDellaRiga m = ModificatoreDellaRiga.da("FORZA VARIAZIONE_FISSA 2", "riga");
        assertSame(TipoAttributo.FORZA, m.getAttributo());
        assertSame(TipoModificatore.VARIAZIONE_FISSA, m.getTipoModificatore());
        assertEquals(2, m.getQuantita());
        rifiuta(r -> ModificatoreDellaRiga.da(r, "riga"), "FORZA QUANTITA_ASSOLUTA 2");
        rifiuta(r -> ModificatoreDellaRiga.da(r, "riga"), "FORZA VARIAZIONE_FISSA 0");
        rifiuta(r -> ModificatoreDellaRiga.da(r, "riga"), "FORZA VARIAZIONE_FISSA");
    }

    @Test
    void ilViandanteHaUnNumeroValido() {
        String riga = ProduttoreDiMissioniCasuale.rigaDiMissioni("VIANDANTE");
        String numero = riga.replaceFirst("NUMERO=[0-9-]+", "NUMERO=@");
        assertNotNull(ViandanteRichiesto.da(numero.replace("@", "2-3")));
        rifiuta(ViandanteRichiesto::da, numero.replace("@", "0-3"));
        rifiuta(ViandanteRichiesto::da, numero.replace("@", "3-2"));
        rifiuta(ViandanteRichiesto::da, numero.replace("@", "3"));
    }
}
