package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.interfacce.StrumentiDiSviluppo.DovePartono;
import com.threeamigos.foresta.interfacce.StrumentiDiSviluppo.Scorta;
import com.threeamigos.foresta.interfacce.StrumentiDiSviluppo.VoceMissione;
import com.threeamigos.foresta.missioni.MissioneAPassi;
import com.threeamigos.foresta.missioni.RegistroMissioni;
import com.threeamigos.foresta.oggetti.Cofano;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.strumenti.ContestoLancio;
import com.threeamigos.foresta.strumenti.Dado;
import com.threeamigos.foresta.strumenti.TrucchiDiSviluppo;
import com.threeamigos.foresta.tipi.ClasseIncantesimo;
import com.threeamigos.foresta.tipi.ClasseMissione;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StrumentiDiSviluppoMotoreTest {

    private final StrumentiDiSviluppoMotore strumenti = new StrumentiDiSviluppoMotore();

    @AfterEach
    void spegni() {
        TrucchiDiSviluppo.istanza().spegniTutto();
        Dado.impostaTruccatore(null);
    }

    private PartitaDiTest partitaInCitta() {
        PartitaDiTest partita = PartitaDiTest.nuova(7);
        partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
                () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
        return partita;
    }

    @Test
    void gliAttributiDelPersonaggioSiImpostano() {
        try (PartitaDiTest partita = partitaInCitta()) {
            Personaggio capo = partita.gruppo().getPersonaggi().get(0);
            String uuid = capo.getModelloDati().getUuid();
            strumenti.impostaAttributo(uuid, TipoAttributo.FORZA, 18);
            assertEquals(18, strumenti.getValoreBase(uuid, TipoAttributo.FORZA));
            // La salute non supera il massimo
            strumenti.impostaAttributo(uuid, TipoAttributo.SALUTE, 100000);
            assertEquals(capo.getSaluteMassima(), strumenti.getValoreBase(uuid, TipoAttributo.SALUTE));
            assertThrows(IllegalArgumentException.class,
                    () -> strumenti.impostaAttributo(uuid, TipoAttributo.RIGENERAZIONE_MAGIA, 3));
            assertThrows(IllegalArgumentException.class,
                    () -> strumenti.impostaAttributo("non-esiste", TipoAttributo.FORZA, 3));
        }
    }

    @Test
    void ilPersonaggioMuoreEResuscita() {
        try (PartitaDiTest partita = partitaInCitta()) {
            Personaggio capo = partita.gruppo().getPersonaggi().get(0);
            String uuid = capo.getModelloDati().getUuid();
            strumenti.impostaVivo(uuid, false);
            assertFalse(capo.isVivo());
            strumenti.impostaVivo(uuid, true);
            assertTrue(capo.isVivo());
        }
    }

    @Test
    void ilLivelloSiImposta() {
        try (PartitaDiTest partita = partitaInCitta()) {
            Personaggio capo = partita.gruppo().getPersonaggi().get(0);
            String uuid = capo.getModelloDati().getUuid();
            strumenti.impostaLivello(uuid, 7);
            assertEquals(7, capo.getLivello());
            int puntiPrima = capo.getPuntiAbilitaDisponibili();
            strumenti.aggiungiPuntiAbilita(uuid, 5);
            assertEquals(puntiPrima + 5, capo.getPuntiAbilitaDisponibili());
        }
    }

    @Test
    void leScorteDelGruppoSiImpostanoAUnValore() {
        try (PartitaDiTest partita = partitaInCitta()) {
            for (Scorta scorta : Scorta.values()) {
                strumenti.impostaScorta(scorta, 37);
                assertEquals(37, strumenti.getScorta(scorta), scorta.name());
                strumenti.impostaScorta(scorta, 2);
                assertEquals(2, strumenti.getScorta(scorta), scorta.name());
                strumenti.impostaScorta(scorta, -5);
                assertEquals(0, strumenti.getScorta(scorta), scorta.name());
            }
            assertEquals(0, partita.gruppo().getMonete());
            strumenti.impostaIncantesimi(ClasseIncantesimo.FUOCO, 9);
            assertEquals(9, partita.gruppo().getIncantesimi(ClasseIncantesimo.FUOCO));
            strumenti.impostaIncantesimi(ClasseIncantesimo.FUOCO, 4);
            assertEquals(4, strumenti.getIncantesimi(ClasseIncantesimo.FUOCO));
        }
    }

    @Test
    void ilLivelloDelMondoSiImpostaSuEGiuEFaRestareCoerentiGliXp() {
        try (PartitaDiTest partita = partitaInCitta()) {
            strumenti.impostaLivelloDelMondo(9);
            assertEquals(9, strumenti.getLivelloDelMondo());
            assertEquals(GestoreProgressione.getXpNecessariPerLivello(9), strumenti.getEsperienzaDelMondo());
            strumenti.impostaLivelloDelMondo(3);
            assertEquals(3, strumenti.getLivelloDelMondo());
            assertEquals(GestoreProgressione.getXpNecessariPerLivello(3), strumenti.getEsperienzaDelMondo());
            // Guadagnare esperienza non riporta il livello al valore di prima
            strumenti.aggiungiEsperienzaAlGruppo(1);
            assertEquals(3, strumenti.getLivelloDelMondo());
        }
    }

    @Test
    void leMissioniSonoDivisePerLuogoDiPartenza() {
        try (PartitaDiTest partita = partitaInCitta()) {
            List<ClasseMissione> locanda = classi(strumenti.getMissioni(DovePartono.LOCANDA));
            List<ClasseMissione> citta = classi(strumenti.getMissioni(DovePartono.CITTA));
            assertTrue(locanda.contains(ClasseMissione.LA_LEGGENDA_DEL_LOCANDIERE));
            assertTrue(locanda.contains(ClasseMissione.NON_SPARATE_SUL_PIANISTA));
            assertTrue(citta.contains(ClasseMissione.CACCIA_AI_GOBLIN));
            assertTrue(citta.contains(ClasseMissione.IL_NEGOZIO_DELL_ARMAIOLO));
            assertTrue(locanda.stream().noneMatch(citta::contains));
        }
    }

    @Test
    void unaMissioneSiForzaEPartePrimaDelleSueCondizioni() {
        try (PartitaDiTest partita = partitaInCitta()) {
            MissioneAPassi missione = (MissioneAPassi) RegistroMissioni.getMissioniNonCompletate().stream()
                    .filter(m -> m.getClasse() == ClasseMissione.CACCIA_AI_GOBLIN).findFirst().get();
            VoceMissione prima = voce(DovePartono.CITTA, ClasseMissione.CACCIA_AI_GOBLIN);
            assertTrue(prima.isForzabile());
            String passoIniziale = missione.getPassoCorrente();

            strumenti.forzaPartenza(ClasseMissione.CACCIA_AI_GOBLIN);
            assertEquals("partenza forzata", voce(DovePartono.CITTA, ClasseMissione.CACCIA_AI_GOBLIN).getStato());
            assertFalse(voce(DovePartono.CITTA, ClasseMissione.CACCIA_AI_GOBLIN).isForzabile());
            assertThrows(IllegalStateException.class, () -> strumenti.forzaPartenza(ClasseMissione.CACCIA_AI_GOBLIN));

            // Il gruppo è in una città: al prossimo controllo la missione parte
            missione.controllaPreLocazione();
            assertNotEquals(passoIniziale, missione.getPassoCorrente());
        }
    }

    @Test
    void unaMissioneForzataNonPartePerStradaFuoriDalLuogoGiusto() {
        try (PartitaDiTest partita = partitaInCitta()) {
            MissioneAPassi missione = (MissioneAPassi) RegistroMissioni.getMissioniNonCompletate().stream()
                    .filter(m -> m.getClasse() == ClasseMissione.LA_LEGGENDA_DEL_LOCANDIERE).findFirst().get();
            String passoIniziale = missione.getPassoCorrente();
            strumenti.forzaPartenza(ClasseMissione.LA_LEGGENDA_DEL_LOCANDIERE);
            // Il gruppo è in città, non in una locanda
            missione.controllaPreLocazione();
            assertEquals(passoIniziale, missione.getPassoCorrente());
        }
    }

    @Test
    void ilCofanoPuoEssereCostrettoADareUnLeggendario() {
        List<String> codici = strumenti.getOggettiSpeciali().stream().map(o -> o.getCodice()).collect(Collectors.toList());
        assertTrue(codici.contains(Cofano.OGGETTO_SPECIALE_CASUALE));
        assertTrue(codici.contains(Cofano.OGGETTO_SPECIALE_AGGIORNAMENTO));
        assertTrue(codici.stream().anyMatch(c -> c.startsWith(Cofano.PREFISSO_LEGGENDARIO)));
    }

    @Test
    void ilTrucchiDelTipoDiMostroSiApplicaSoloSeIlMostroEFraGliAmmessi() {
        TrucchiDiSviluppo.istanza().installa();
        TrucchiDiSviluppo.istanza().accendiElemento(ContestoLancio.TIPO_MOSTRO, TipoPersonaggio.DRAGO);
        List<TipoPersonaggio> ammessi = java.util.Arrays.asList(TipoPersonaggio.GOBLIN, TipoPersonaggio.DRAGO);
        assertEquals(TipoPersonaggio.DRAGO, Dado.scegli(ContestoLancio.TIPO_MOSTRO, ammessi));
    }

    private List<ClasseMissione> classi(List<VoceMissione> voci) {
        return voci.stream().map(VoceMissione::getClasse).collect(Collectors.toList());
    }

    private VoceMissione voce(DovePartono dove, ClasseMissione classe) {
        return strumenti.getMissioni(dove).stream().filter(v -> v.getClasse() == classe).findFirst().get();
    }
}
