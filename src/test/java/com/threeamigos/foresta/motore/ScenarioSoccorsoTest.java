package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.interni.InternoAvversarioSconfitto;
import com.threeamigos.foresta.intermezzi.MomentoIntermezzo;
import com.threeamigos.foresta.missioni.IlSoccorso;
import com.threeamigos.foresta.missioni.SoccorsoRichiesto;
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
 * Il soccorso: qualcuno è rimasto nella foresta in mezzo a dei nemici; sconfitti i nemici, viaggia con il gruppo come
 * ospite vulnerabile e va riportato vivo in città.
 */
class ScenarioSoccorsoTest {

    private static final String TAGLIALEGNA = "CHIAVE=TAGLIALEGNA_DI_PROVA;TIPO=SOCCORSO;ASPETTO=QUALUNQUE;"
            + "MANDANTE=la moglie del taglialegna;PERSONA=il taglialegna;NEMICO=ARPIA;NUMERO=3;LUOGO=BOSCO;MONETE=35;"
            + "TITOLO=Il taglialegna %NOME%;RICHIESTA=Mio marito %NOME% non è tornato.;BATTUTA=Su un albero?;RISPOSTA=Sì.;"
            + "SOCCORSO=%NOME% scende dall'albero.;RINGRAZIAMENTO=%NOME%!;LUTTO=Andate via.;RICORDO=Qui c'era %NOME%.";

    @Test
    void ogniSoccorsoSiLegge() {
        Set<String> chiavi = new HashSet<>();
        Set<TipoMissione> tipi = EnumSet.noneOf(TipoMissione.class);
        for (int i = 0; i < 300; i++) {
            SoccorsoRichiesto soccorso = SoccorsoRichiesto.da(ProduttoreDiTestiCasuale.rigaDiMissioni("SOCCORSO"));
            if (!soccorso.isConCapo()) {
                assertFalse(soccorso.getRiga().contains("%CAPO%"), soccorso.getRiga());
            }
            chiavi.add(soccorso.getChiave());
            tipi.add(soccorso.getTipo());
        }
        assertTrue(chiavi.size() >= 7, String.valueOf(chiavi));
        assertEquals(EnumSet.of(TipoMissione.SOCCORSO, TipoMissione.SALVATAGGIO, TipoMissione.PRIMO_SOCCORSO,
                TipoMissione.EVACUAZIONE, TipoMissione.ASILO), tipi);
        assertThrows(IllegalArgumentException.class, () -> SoccorsoRichiesto.da(TAGLIALEGNA.replace("LUTTO=Andate via.;", "")));
    }

    @Test
    void ilTaglialegnaSalvatoTornaACasa() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(191)) {
            IlSoccorso soccorso = prendiLIncarico(partita);
            String nome = soccorso.getNomeDellaPersona();
            assertEquals("Il taglialegna " + nome, soccorso.getNome());
            assertEquals("La moglie del taglialegna di Nyena ti ha chiesto di salvare " + nome + ", il taglialegna, dal posto "
                    + "segnato sulla mappa, dove lo minacciano tre Arpie.", soccorso.getDescrizione());
            CoordinateMD bosco = soccorso.getCovo();
            assertEquals(TipoLocazione.BOSCO, Foresta.getLocazione(bosco));
            assertTrue(Foresta.isLocazioneConosciuta(bosco));
            List<Personaggio> arpie = RegistroMissioni.getIncontroMissione(bosco).orElseThrow(AssertionError::new);
            assertEquals(3, arpie.size());
            assertEquals(TipoPersonaggio.ARPIA, arpie.get(0).getClasse());

            Personaggio taglialegna = salva(partita, soccorso);
            assertTrue(partita.gruppo().isOspiteVulnerabile(taglialegna));
            assertTrue(partita.testi().contains(nome + " scende dall'albero. Riportatelo vivo a Nyena."), String.valueOf(partita.testi()));

            partita.gruppo().setCoordinate(Foresta.getCoordinateLocazioneUnica(TipoLocazione.CITTA_NYENA));
            int monete = partita.gruppo().getMonete();
            soccorso.controllaPreLocazione();
            assertTrue(partita.gruppo().getOspiti().isEmpty());
            soccorso.segnaIntermezzoPassoMostrato("RITORNO");
            soccorso.controllaInLocazione();
            assertEquals(monete + 35, partita.gruppo().getMonete());
            assertTrue(soccorso.isCompleta());
            assertTrue(RegistroMissioni.getTutteLeMissioni().stream().anyMatch(m -> m instanceof IlSoccorso && m != soccorso));
        }
    }

    @Test
    void seIlTaglialegnaMuorePerStradaLaMissioneFallisce() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(192)) {
            IlSoccorso soccorso = prendiLIncarico(partita);
            salva(partita, soccorso).muore("un'arpia rimasta indietro");
            soccorso.controllaPreLocazione();
            assertEquals("LUTTO", soccorso.getPassoCorrente());
            assertEquals(soccorso.getNomeDellaPersona() + " è morto: devi dare la notizia alla moglie del taglialegna, a Nyena.",
                    soccorso.getDescrizione());

            partita.gruppo().setCoordinate(Foresta.getCoordinateLocazioneUnica(TipoLocazione.CITTA_NYENA));
            soccorso.controllaPreLocazione();
            assertEquals("LUTTO", soccorso.getPassoConIntermezzoInAttesa(MomentoIntermezzo.INIZIO_LOCAZIONE));
            soccorso.segnaIntermezzoPassoMostrato("LUTTO");
            soccorso.controllaInLocazione();
            assertTrue(soccorso.isFallita());
            assertTrue(partita.testi().contains("La moglie del taglialegna chiude la porta senza dire una parola."), String.valueOf(partita.testi()));
        }
    }

    private static IlSoccorso prendiLIncarico(PartitaDiTest partita) {
        partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
        IlSoccorso soccorso = RegistroMissioni.getTutteLeMissioni().stream().filter(IlSoccorso.class::isInstance)
                .map(IlSoccorso.class::cast).findFirst().orElseThrow(AssertionError::new);
        soccorso.aggiungiProprieta("PARAMETRO_" + IlSoccorso.SOCCORSO, TAGLIALEGNA);
        soccorso.controllaPreLocazione();
        soccorso.segnaIntermezzoPassoMostrato("INCARICO");
        soccorso.controllaInLocazione();
        assertTrue(soccorso.isAttiva());
        return soccorso;
    }

    private static Personaggio salva(PartitaDiTest partita, IlSoccorso soccorso) {
        partita.gruppo().setCoordinate(soccorso.getCovo());
        for (int i = 0; i < 3; i++) {
            partita.pubblica(new InternoAvversarioSconfitto(TipoPersonaggio.ARPIA));
        }
        soccorso.controllaPostLocazione();
        assertEquals("VIAGGIO", soccorso.getPassoCorrente());
        return soccorso.getScortato().orElseThrow(AssertionError::new);
    }
}
