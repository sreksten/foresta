package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.interni.InternoAvversarioSconfitto;
import com.threeamigos.foresta.missioni.LaSorveglianza;
import com.threeamigos.foresta.missioni.SorveglianzaRichiesta;
import com.threeamigos.foresta.missioni.TipoMissione;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le sorveglianze: si passa più volte da un posto segnato sulla mappa, lasciando passare abbastanza ore fra una
 * visita e l'altra; all'ultima a volte salta fuori qualcuno; poi si torna a riscuotere.
 */
class ScenarioSorveglianzaTest {

    private static final String TROLL = "CHIAVE=TROLL_DI_PROVA;TIPO=SORVEGLIANZA;ASPETTO=LOCANDIERE;MANDANTE=il locandiere;"
            + "LUOGO=GROTTA;VISITE=2;ORE=12;NEMICO=TROLL;NUMERO=1;MONETE=35;TITOLO=I rumori;RICHIESTA=Rumori.;BATTUTA=Quali?;"
            + "RISPOSTA=Russa.;VEGLIA=Qualcuno russa.;SCOPERTA=Esce un troll.;VITTORIA=Il troll non russerà più.;"
            + "RINGRAZIAMENTO=Grazie.;RICORDO=Qui russava un troll.";
    private static final String TOMBA = "CHIAVE=TOMBA_DI_PROVA;TIPO=VIGILIA;ASPETTO=QUALUNQUE;MANDANTE=la sorella;"
            + "LUOGO=BOSCO;VISITE=2;ORE=24;MONETE=25;TITOLO=La tomba;RICHIESTA=Vegliate.;BATTUTA=Perché?;RISPOSTA=Tradizione.;"
            + "VEGLIA=Tutto tranquillo.;SCOPERTA=La notte passa.;RINGRAZIAMENTO=Grazie.;RICORDO=Qui c'è una tomba.";

    @Test
    void ogniSorveglianzaSiLeggeEOgniTipoCompare() {
        Set<String> chiavi = new HashSet<>();
        Set<TipoMissione> tipi = EnumSet.noneOf(TipoMissione.class);
        for (int i = 0; i < 300; i++) {
            SorveglianzaRichiesta sorveglianza = SorveglianzaRichiesta.da(ProduttoreDiTestiCasuale.rigaDiMissioni("SORVEGLIANZA"));
            if (!sorveglianza.isConCapo()) {
                assertFalse(sorveglianza.getRiga().contains("%CAPO%"), sorveglianza.getRiga());
            }
            chiavi.add(sorveglianza.getChiave());
            tipi.add(sorveglianza.getTipo());
        }
        assertTrue(chiavi.size() >= 13, String.valueOf(chiavi));
        assertEquals(EnumSet.of(TipoMissione.VIGILIA, TipoMissione.SORVEGLIANZA, TipoMissione.SPIONAGGIO, TipoMissione.DIFESA,
                TipoMissione.ASSEDIO_DIFESA, TipoMissione.TRINCEA, TipoMissione.CONTENIMENTO, TipoMissione.QUARANTENA,
                TipoMissione.AGRICOLTURA, TipoMissione.ALLEVAMENTO), tipi);

        assertFalse(SorveglianzaRichiesta.da(TOMBA).isConNemici());
        assertThrows(IllegalArgumentException.class, () -> SorveglianzaRichiesta.da(TROLL.replace("VITTORIA=Il troll non russerà più.;", "")));
        assertThrows(IllegalArgumentException.class, () -> SorveglianzaRichiesta.da(TOMBA + ";NUMERO=3"));
        assertThrows(IllegalArgumentException.class, () -> SorveglianzaRichiesta.da(TOMBA.replace("VISITE=2", "VISITE=1")));
    }

    @Test
    void dopoDueVisiteDistanzialeEsceIlTrollEPoiSiRiscuote() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(181)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            LaSorveglianza sorveglianza = prendiLIncarico(TROLL);
            assertEquals("Il locandiere di Nyena ti ha chiesto di sorvegliare il posto segnato sulla mappa, passandoci due volte "
                    + "e lasciando passare almeno 12 ore fra una visita e l'altra.", sorveglianza.getDescrizione());
            CoordinateMD posto = sorveglianza.getPosto();
            assertEquals(TipoLocazione.GROTTA, Foresta.getLocazione(posto));
            assertTrue(Foresta.isLocazioneConosciuta(posto));

            // Prima visita: conta, e nella grotta non c'è ancora il troll
            partita.gruppo().setCoordinate(posto);
            sorveglianza.controllaPreLocazione();
            assertEquals("SORVEGLIA", sorveglianza.getPassoCorrente());
            assertTrue(partita.testi().contains("Qualcuno russa. Bisogna tornare qui ancora una volta, lasciando passare almeno "
                    + "12 ore fra una visita e l'altra."), String.valueOf(partita.testi()));
            assertFalse(RegistroMissioni.getIncontroMissione(posto).isPresent());
            assertTrue(sorveglianza.getDescrizione().endsWith(": passaci ancora una volta, lasciando passare almeno 12 ore fra una visita e l'altra."),
                    sorveglianza.getDescrizione());

            // Troppo presto: non conta
            LineaTemporale.aggiungiOre(6);
            sorveglianza.controllaPreLocazione();
            assertEquals("SORVEGLIA", sorveglianza.getPassoCorrente());

            LineaTemporale.aggiungiOre(6);
            sorveglianza.controllaPreLocazione();
            assertEquals("AGGUATO", sorveglianza.getPassoCorrente());
            assertTrue(partita.testi().contains("Esce un troll."), String.valueOf(partita.testi()));
            List<Personaggio> troll = RegistroMissioni.getIncontroMissione(posto).orElseThrow(AssertionError::new);
            assertEquals(1, troll.size());
            assertEquals(TipoPersonaggio.TROLL, troll.get(0).getClasse());

            partita.pubblica(new InternoAvversarioSconfitto(TipoPersonaggio.TROLL));
            sorveglianza.controllaPostLocazione();
            assertEquals("RITORNO", sorveglianza.getPassoCorrente());
            assertTrue(partita.testi().contains("Il troll non russerà più. Il locandiere aspetta a Nyena."), String.valueOf(partita.testi()));
            assertEquals("Qui russava un troll.", sorveglianza.getRicordoDellaLocazione());

            riscuoti(partita, sorveglianza, 35);
        }
    }

    @Test
    void laVegliaSenzaNemiciFinisceAllUltimaVisita() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(182)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            LaSorveglianza sorveglianza = prendiLIncarico(TOMBA);
            CoordinateMD posto = sorveglianza.getPosto();
            assertEquals(TipoLocazione.BOSCO, Foresta.getLocazione(posto));

            partita.gruppo().setCoordinate(posto);
            sorveglianza.controllaPreLocazione();
            LineaTemporale.aggiungiOre(24);
            sorveglianza.controllaPreLocazione();
            assertEquals("RITORNO", sorveglianza.getPassoCorrente());
            assertTrue(partita.testi().contains("La notte passa. La sorella aspetta a Nyena."), String.valueOf(partita.testi()));
            assertFalse(RegistroMissioni.getIncontroMissione(posto).isPresent());

            riscuoti(partita, sorveglianza, 25);
        }
    }

    private static LaSorveglianza prendiLIncarico(String riga) {
        LaSorveglianza sorveglianza = RegistroMissioni.getTutteLeMissioni().stream().filter(LaSorveglianza.class::isInstance)
                .map(LaSorveglianza.class::cast).findFirst().orElseThrow(AssertionError::new);
        sorveglianza.aggiungiProprieta("PARAMETRO_" + LaSorveglianza.SORVEGLIANZA, riga);
        sorveglianza.controllaPreLocazione();
        sorveglianza.segnaIntermezzoPassoMostrato("INCARICO");
        sorveglianza.controllaInLocazione();
        assertTrue(sorveglianza.isAttiva());
        return sorveglianza;
    }

    private static void riscuoti(PartitaDiTest partita, LaSorveglianza sorveglianza, int ricompensa) {
        partita.gruppo().setCoordinate(Foresta.getCoordinateLocazioneUnica(TipoLocazione.CITTA_NYENA));
        int monete = partita.gruppo().getMonete();
        sorveglianza.controllaPreLocazione();
        sorveglianza.segnaIntermezzoPassoMostrato("RITORNO");
        sorveglianza.controllaInLocazione();
        assertEquals(monete + ricompensa, partita.gruppo().getMonete());
        assertTrue(sorveglianza.isCompleta());
        assertTrue(RegistroMissioni.getTutteLeMissioni().stream().anyMatch(m -> m instanceof LaSorveglianza && m != sorveglianza),
                "c'è già un'altra sorveglianza");
    }
}
