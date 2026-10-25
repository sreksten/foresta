package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.economia.Cassa;
import com.threeamigos.foresta.mappa.Foresta;

import com.threeamigos.foresta.eventi.interni.InternoAvversarioSconfitto;
import com.threeamigos.foresta.missioni.RegistroMissioni;
import com.threeamigos.foresta.missioni.incarichi.IncaricoDiCombattimento;
import com.threeamigos.foresta.missioni.richieste.CombattimentoRichiesto;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.testi.ProduttoreDiMissioniCasuale;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoMissione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Gli incarichi di combattimento: in città qualcuno vuole sconfitto qualcosa che sta in un posto segnato sulla mappa;
 * sconfitto, si torna a riscuotere.
 */
class ScenarioIncaricoDiCombattimentoTest {

    private static final String TROLL = "CHIAVE=TROLL_DI_PROVA;TIPO=VENDETTA;ASPETTO=QUALUNQUE;MANDANTE=il mugnaio;NEMICO=TROLL;"
            + "NUMERO=2;CAPO=SI;LUOGO=GROTTA;MONETE=35;TITOLO=La vendetta su %CAPO%;RICHIESTA=%CAPO% ha sfasciato il mulino.;"
            + "BATTUTA=E se chiede scusa?;RISPOSTA=No.;VITTORIA=%CAPO% non sfascerà più niente.;RINGRAZIAMENTO=Grazie.;"
            + "RICORDO=Qui viveva %CAPO%.";

    @Test
    void ogniIncaricoSiLeggeEOgniTipoCompare() {
        Set<String> chiavi = new HashSet<>();
        Set<TipoMissione> tipi = EnumSet.noneOf(TipoMissione.class);
        for (int i = 0; i < 1000; i++) {
            CombattimentoRichiesto incarico = CombattimentoRichiesto.da(ProduttoreDiMissioniCasuale.rigaDiMissioni("INCARICO_DI_COMBATTIMENTO"));
            assertTrue(incarico.getNumero(1) > 0 && incarico.getMonete() > 0, incarico.getRiga());
            assertFalse(incarico.getVittoria().isEmpty() || incarico.getRingraziamento().isEmpty(), incarico.getRiga());
            if (!incarico.isConCapo()) {
                assertFalse(incarico.getRiga().contains("%CAPO%"), incarico.getRiga());
            }
            chiavi.add(incarico.getChiave());
            tipi.add(incarico.getTipo());
        }
        assertTrue(chiavi.size() >= 31, String.valueOf(chiavi));
        assertTrue(chiavi.containsAll(Arrays.asList("MEDAGLIONE_RUBATO", "DERRATE_SPARITE")), "due incarichi di recupero: " + chiavi);
        assertEquals(EnumSet.of(TipoMissione.VENDETTA, TipoMissione.COMBATTIMENTO_BESTIA, TipoMissione.PULIZIA_DEI_DUNGEON,
                TipoMissione.SCHERMAGLIA, TipoMissione.IMBOSCATA, TipoMissione.PROTEZIONE_TEMPORALE, TipoMissione.DUELLO,
                TipoMissione.DUELLO_ANTICO, TipoMissione.DUELLO_MAGICO, TipoMissione.COMBATTIMENTO_RITUALE, TipoMissione.BLOCCO,
                TipoMissione.PONTE_TATTICO, TipoMissione.SORTITA, TipoMissione.CARICA, TipoMissione.COMPETIZIONE,
                TipoMissione.CONFINAMENTO, TipoMissione.BRIGANTAGGIO, TipoMissione.ASSEDIO_OFFENSIVO, TipoMissione.CIRCONDAMENTO,
                TipoMissione.SICARIO, TipoMissione.TITOLO_NOBILIARE, TipoMissione.BATTAGLIA, TipoMissione.RECUPERO,
                TipoMissione.RITIRATA_TATTICA, TipoMissione.RIFUGIO, TipoMissione.BLINDATURA, TipoMissione.RAPIMENTO), tipi);

        assertThrows(IllegalArgumentException.class, () -> CombattimentoRichiesto.da(TROLL + ";COLORE=VERDE"));
        assertEquals("Teodolinda", CombattimentoRichiesto.da(TROLL.replace("CAPO=SI", "CAPO=Teodolinda")).pescaNomeDelCapo());
        assertTrue(CombattimentoRichiesto.da(TROLL.replace("CAPO=SI", "CAPO=NOME_CAMPIONESSA")).pescaNomeDelCapo()
                .matches("(Bradamante|Marfisa|Ermengarda|Matilde|Clorinda|Gualdrada|Isotta|Brunilde) .+"));
        assertThrows(IllegalArgumentException.class, () -> CombattimentoRichiesto.da(TROLL.replace("LUOGO=GROTTA", "LUOGO=CITTA_NYENA")));
        assertThrows(IllegalArgumentException.class, () -> CombattimentoRichiesto.da(TROLL.replace("MONETE=35;", "")));

        // NUMERO=LIVELLO:4: tanti quanto il livello del mondo, da uno a quattro
        CombattimentoRichiesto perLivello = CombattimentoRichiesto.da(TROLL.replace("NUMERO=2", "NUMERO=LIVELLO:4"));
        assertEquals(2, CombattimentoRichiesto.da(TROLL).getNumero(9));
        assertEquals(1, perLivello.getNumero(1));
        assertEquals(3, perLivello.getNumero(3));
        assertEquals(4, perLivello.getNumero(4));
        assertEquals(4, perLivello.getNumero(10));
        assertThrows(IllegalArgumentException.class, () -> CombattimentoRichiesto.da(TROLL.replace("NUMERO=2", "NUMERO=LIVELLO:x")));
        assertThrows(IllegalArgumentException.class, () -> CombattimentoRichiesto.da(TROLL.replace("NUMERO=2", "NUMERO=LIVELLO:1").replace("TIPO=VENDETTA", "TIPO=DUELLO").replace("NEMICO=TROLL", "NEMICO=GUERRIERO")
                .replace(";CAPO=SI", ";DUELLO=SI")));
    }

    @Test
    void iTrollStannoNellaGrottaSegnataEPoiSiRiscuote() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(171)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            IncaricoDiCombattimento incarico = RegistroMissioni.getTutteLeMissioni().stream()
                    .filter(IncaricoDiCombattimento.class::isInstance).map(IncaricoDiCombattimento.class::cast)
                    .findFirst().orElseThrow(AssertionError::new);
            incarico.aggiungiProprieta("PARAMETRO_" + IncaricoDiCombattimento.INCARICO, TROLL);
            incarico.controllaPreLocazione();
            incarico.segnaIntermezzoPassoMostrato("INCARICO");
            incarico.controllaInLocazione();
            assertTrue(incarico.isAttiva());

            String capo = incarico.getCapo();
            assertFalse(capo.isEmpty());
            assertEquals("La vendetta su " + capo, incarico.getNome());
            assertEquals("Il mugnaio di Nyena ti ha chiesto di sconfiggere due Troll guidati da " + capo
                    + ", che si trovano nel posto segnato sulla mappa.",
                    incarico.getDescrizione());

            CoordinateMD covo = incarico.getCovo();
            assertEquals(TipoLocazione.GROTTA, Foresta.getLocazione(covo));
            assertTrue(Foresta.isLocazioneConosciuta(covo));
            assertEquals("Qui viveva " + capo + ".", incarico.getRicordoDellaLocazione());

            List<Personaggio> troll = RegistroMissioni.getIncontroMissione(covo).orElseThrow(AssertionError::new);
            assertEquals(2, troll.size());
            assertEquals(TipoPersonaggio.TROLL, troll.get(0).getClasse());
            assertEquals(capo, troll.get(0).getNome());

            Foresta.setPosizioneDelGruppo(covo);
            partita.pubblica(new InternoAvversarioSconfitto(TipoPersonaggio.TROLL));
            incarico.controllaPostLocazione();
            assertEquals("CACCIA", incarico.getPassoCorrente());
            partita.pubblica(new InternoAvversarioSconfitto(TipoPersonaggio.TROLL));
            incarico.controllaPostLocazione();
            assertEquals("RITORNO", incarico.getPassoCorrente());
            assertTrue(partita.testi().contains(capo + " non sfascerà più niente. Il mugnaio aspetta a Nyena."), String.valueOf(partita.testi()));
            assertEquals("Fatto: torna dal mugnaio a Nyena a riscuotere.", incarico.getDescrizione());

            Foresta.setPosizioneDelGruppo(Foresta.getCoordinateLocazioneUnica(TipoLocazione.CITTA_NYENA));
            int monete = Cassa.getMonete();
            incarico.controllaPreLocazione();
            incarico.segnaIntermezzoPassoMostrato("RITORNO");
            incarico.controllaInLocazione();
            assertEquals(monete + 35, Cassa.getMonete());
            assertTrue(incarico.isCompleta());
            assertTrue(RegistroMissioni.getTutteLeMissioni().stream().anyMatch(m -> m instanceof IncaricoDiCombattimento && m != incarico),
                    "c'è già un altro incarico");
        }
    }

    @Test
    void laCampionessaDelDuelloEUnaGuerrieraConUnNomeDaCampionessa() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(172)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            IncaricoDiCombattimento incarico = RegistroMissioni.getTutteLeMissioni().stream()
                    .filter(IncaricoDiCombattimento.class::isInstance).map(IncaricoDiCombattimento.class::cast)
                    .findFirst().orElseThrow(AssertionError::new);
            incarico.aggiungiProprieta("PARAMETRO_" + IncaricoDiCombattimento.INCARICO, TROLL.replace("TIPO=VENDETTA", "TIPO=DUELLO")
                    .replace("NEMICO=TROLL", "NEMICO=GUERRIERA").replace("NUMERO=2", "NUMERO=1").replace("CAPO=SI", "CAPO=NOME_CAMPIONESSA"));
            incarico.controllaPreLocazione();
            incarico.segnaIntermezzoPassoMostrato("INCARICO");
            incarico.controllaInLocazione();

            String campionessa = incarico.getCapo();
            assertTrue(campionessa.matches("(Bradamante|Marfisa|Ermengarda|Matilde|Clorinda|Gualdrada|Isotta|Brunilde) .+"), campionessa);
            assertEquals("Il mugnaio di Nyena ti ha chiesto di sconfiggere " + campionessa + ", la Guerriera, che si trova nel posto "
                    + "segnato sulla mappa.", incarico.getDescrizione());
            List<Personaggio> sfidante = RegistroMissioni.getIncontroMissione(incarico.getCovo()).orElseThrow(AssertionError::new);
            assertEquals(1, sfidante.size());
            assertEquals(TipoPersonaggio.GUERRIERA, sfidante.get(0).getClasse());
            assertEquals(campionessa, sfidante.get(0).getNome());
        }
    }
}
