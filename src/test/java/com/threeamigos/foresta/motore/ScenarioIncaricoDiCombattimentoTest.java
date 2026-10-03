package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.interni.InternoAvversarioSconfitto;
import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.missioni.CombattimentoRichiesto;
import com.threeamigos.foresta.missioni.IncaricoDiCombattimento;
import com.threeamigos.foresta.missioni.TipoMissione;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;
import com.threeamigos.foresta.personaggi.Personaggio;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        for (int i = 0; i < 300; i++) {
            CombattimentoRichiesto incarico = CombattimentoRichiesto.da(ProduttoreDiTestiCasuale.rigaDiMissioni("INCARICO_DI_COMBATTIMENTO"));
            assertTrue(incarico.getNumero() > 0 && incarico.getMonete() > 0, incarico.getRiga());
            assertFalse(incarico.getVittoria().isEmpty() || incarico.getRingraziamento().isEmpty(), incarico.getRiga());
            if (!incarico.isConCapo()) {
                assertFalse(incarico.getRiga().contains("%CAPO%"), incarico.getRiga());
            }
            chiavi.add(incarico.getChiave());
            tipi.add(incarico.getTipo());
        }
        assertTrue(chiavi.size() >= 9, String.valueOf(chiavi));
        assertEquals(EnumSet.of(TipoMissione.VENDETTA, TipoMissione.COMBATTIMENTO_BESTIA, TipoMissione.PULIZIA_DEI_DUNGEON,
                TipoMissione.SCHERMAGLIA, TipoMissione.IMBOSCATA, TipoMissione.PROTEZIONE_TEMPORALE), tipi);

        assertThrows(IllegalArgumentException.class, () -> CombattimentoRichiesto.da(TROLL + ";COLORE=VERDE"));
        assertThrows(IllegalArgumentException.class, () -> CombattimentoRichiesto.da(TROLL.replace("LUOGO=GROTTA", "LUOGO=CITTA_NYENA")));
        assertThrows(IllegalArgumentException.class, () -> CombattimentoRichiesto.da(TROLL.replace("MONETE=35;", "")));
    }

    @Test
    void iTrollStannoNellaGrottaSegnataEPoiSiRiscuote() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(171)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(ClassiLocazione.CITTA_NYENA));
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
            assertEquals(ClassiLocazione.GROTTA, Foresta.getLocazione(covo));
            assertTrue(Foresta.isLocazioneConosciuta(covo));
            assertEquals("Qui viveva " + capo + ".", incarico.getRicordoDellaLocazione());

            List<Personaggio> troll = RegistroMissioni.getIncontroMissione(covo).orElseThrow(AssertionError::new);
            assertEquals(2, troll.size());
            assertEquals(ClassePersonaggio.TROLL, troll.get(0).getClasse());
            assertEquals(capo, troll.get(0).getNome());

            partita.gruppo().setCoordinate(covo);
            partita.pubblica(new InternoAvversarioSconfitto(ClassePersonaggio.TROLL));
            incarico.controllaPostLocazione();
            assertEquals("CACCIA", incarico.getPassoCorrente());
            partita.pubblica(new InternoAvversarioSconfitto(ClassePersonaggio.TROLL));
            incarico.controllaPostLocazione();
            assertEquals("RITORNO", incarico.getPassoCorrente());
            assertTrue(partita.testi().contains(capo + " non sfascerà più niente. Il mugnaio aspetta a Nyena."), String.valueOf(partita.testi()));
            assertEquals("Fatto: torna dal mugnaio a Nyena a riscuotere.", incarico.getDescrizione());

            partita.gruppo().setCoordinate(Foresta.getCoordinateLocazioneUnica(ClassiLocazione.CITTA_NYENA));
            int monete = partita.gruppo().getMonete();
            incarico.controllaPreLocazione();
            incarico.segnaIntermezzoPassoMostrato("RITORNO");
            incarico.controllaInLocazione();
            assertEquals(monete + 35, partita.gruppo().getMonete());
            assertTrue(incarico.isCompleta());
            assertTrue(RegistroMissioni.getTutteLeMissioni().stream().anyMatch(m -> m instanceof IncaricoDiCombattimento && m != incarico),
                    "c'è già un altro incarico");
        }
    }
}
