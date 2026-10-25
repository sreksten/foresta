package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.personaggi.GestoreProgressione;
import com.threeamigos.foresta.tipi.TipoLocazione;

import com.threeamigos.foresta.modellodati.CoordinateMD;

import com.threeamigos.foresta.mappa.Foresta;

import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.interfacce.StrumentiDiSviluppo.DovePartono;
import com.threeamigos.foresta.interfacce.StrumentiDiSviluppo.Scorta;
import com.threeamigos.foresta.interfacce.StrumentiDiSviluppo.VoceMissione;
import com.threeamigos.foresta.missioni.MissioneAPassi;
import com.threeamigos.foresta.missioni.RegistroMissioni;
import com.threeamigos.foresta.oggetti.Cofano;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.strumenti.TrucchiDiSviluppo;
import com.threeamigos.foresta.strumenti.dado.ContestoLancio;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.tipi.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

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
            strumenti.impostaIncantesimi(TipoIncantesimo.FUOCO, 9);
            assertEquals(9, partita.gruppo().getIncantesimi(TipoIncantesimo.FUOCO));
            strumenti.impostaIncantesimi(TipoIncantesimo.FUOCO, 4);
            assertEquals(4, strumenti.getIncantesimi(TipoIncantesimo.FUOCO));
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
            List<MissionePredefinita> locanda = classi(strumenti.getMissioni(DovePartono.LOCANDA));
            List<MissionePredefinita> citta = classi(strumenti.getMissioni(DovePartono.CITTA));
            assertTrue(locanda.contains(MissionePredefinita.LA_LEGGENDA_DEL_LOCANDIERE));
            assertTrue(locanda.contains(MissionePredefinita.NON_SPARATE_SUL_PIANISTA));
            assertTrue(citta.contains(MissionePredefinita.CACCIA_AI_GOBLIN));
            assertTrue(citta.contains(MissionePredefinita.IL_NEGOZIO_DELL_ARMAIOLO));
            assertTrue(locanda.stream().noneMatch(citta::contains));
            // La forgia è un incarico in città, il viandante si incontra in un bosco
            assertTrue(citta.contains(MissionePredefinita.LA_FORGIA));
            List<MissionePredefinita> boschi = classi(strumenti.getMissioni(DovePartono.BOSCO));
            assertEquals(java.util.Collections.singletonList(MissionePredefinita.IL_VIANDANTE), boschi);
            assertTrue(locanda.stream().noneMatch(boschi::contains));
            assertTrue(citta.stream().noneMatch(boschi::contains));
        }
    }

    @Test
    void laForgiaSiForzaInCittaSenzaAspettareLaVisitaTranquilla() {
        try (PartitaDiTest partita = partitaInCitta()) {
            MissioneAPassi forgia = missione(MissionePredefinita.LA_FORGIA);
            // Né la pausa né il sorteggio la farebbero partire da sola
            partita.rimandaLeMissioniCheSiOffrono();
            forgia.controllaPreLocazione();
            assertEquals("INCARICO", forgia.getPassoCorrente());
            assertEquals("da cominciare", voce(DovePartono.CITTA, MissionePredefinita.LA_FORGIA).getStato());

            strumenti.forzaPartenza(MissionePredefinita.LA_FORGIA);
            forgia.controllaPreLocazione();
            assertEquals("ACCETTAZIONE", forgia.getPassoCorrente());
            // Finché non è partita davvero, per la finestra di prova è ancora "forzata" e non si può forzare di nuovo
            assertEquals("partenza forzata", voce(DovePartono.CITTA, MissionePredefinita.LA_FORGIA).getStato());
            assertFalse(voce(DovePartono.CITTA, MissionePredefinita.LA_FORGIA).isForzabile());
            forgia.segnaIntermezzoPassoMostrato("INCARICO");
            forgia.controllaInLocazione();
            assertEquals("in corso", voce(DovePartono.CITTA, MissionePredefinita.LA_FORGIA).getStato());
            assertEquals("RACCOLTA_METALLO", forgia.getPassoCorrente());
        }
    }

    @Test
    void ilViandanteSiForzaEIncontraSubitoEntrandoInUnBosco() {
        try (PartitaDiTest partita = partitaInCitta()) {
            MissioneAPassi viandante = missione(MissionePredefinita.IL_VIANDANTE);
            partita.rimandaLeMissioniCheSiOffrono();
            VoceMissione prima = voce(DovePartono.BOSCO, MissionePredefinita.IL_VIANDANTE);
            assertEquals("da cominciare", prima.getStato());
            assertTrue(prima.isForzabile());
            strumenti.forzaPartenza(MissionePredefinita.IL_VIANDANTE);

            // In città il viandante non c'è: la missione fissa i suoi parametri e aspetta un bosco
            viandante.controllaPreLocazione();
            assertEquals("INCONTRO", viandante.getPassoCorrente());
            assertEquals("partenza forzata", voce(DovePartono.BOSCO, MissionePredefinita.IL_VIANDANTE).getStato());
            assertFalse(voce(DovePartono.BOSCO, MissionePredefinita.IL_VIANDANTE).isForzabile());

            // Al primo giorno, in un bosco, senza aspettare il giorno dell'incontro
            assertEquals(1, LineaTemporale.getGiorno());
            Foresta.setPosizioneDelGruppo(primaCasellaDi(TipoLocazione.BOSCO));
            viandante.controllaPreLocazione();
            assertEquals("ACCETTAZIONE", viandante.getPassoCorrente());
            viandante.segnaIntermezzoPassoMostrato("INCONTRO");
            viandante.controllaInLocazione();
            assertEquals("in corso", voce(DovePartono.BOSCO, MissionePredefinita.IL_VIANDANTE).getStato());
            assertEquals("ANDATA", viandante.getPassoCorrente());
        }
    }

    @Test
    void ilViandanteNonSiForzaInUnaRadura() {
        try (PartitaDiTest partita = partitaInCitta()) {
            MissioneAPassi viandante = missione(MissionePredefinita.IL_VIANDANTE);
            partita.rimandaLeMissioniCheSiOffrono();
            strumenti.forzaPartenza(MissionePredefinita.IL_VIANDANTE);
            Foresta.setPosizioneDelGruppo(primaCasellaDi(TipoLocazione.RADURA));
            viandante.controllaPreLocazione();
            assertEquals("INCONTRO", viandante.getPassoCorrente());
        }
    }

    private MissioneAPassi missione(MissionePredefinita classe) {
        return (MissioneAPassi) RegistroMissioni.getMissioniNonCompletate().stream()
                .filter(m -> m.getClasse() == classe).findFirst().get();
    }

    private static com.threeamigos.foresta.modellodati.CoordinateMD primaCasellaDi(TipoLocazione tipo) {
        for (int y = 0; y < Foresta.getDimensioneY(); y++) {
            for (int x = 0; x < Foresta.getDimensioneX(); x++) {
                com.threeamigos.foresta.modellodati.CoordinateMD coordinate = new com.threeamigos.foresta.modellodati.CoordinateMD(x, y);
                if (Foresta.getLocazione(coordinate) == tipo) {
                    return coordinate;
                }
            }
        }
        throw new AssertionError("nella mappa non c'è nessuna casella " + tipo);
    }

    @Test
    void unaMissioneSiForzaEPartePrimaDelleSueCondizioni() {
        try (PartitaDiTest partita = partitaInCitta()) {
            MissioneAPassi missione = (MissioneAPassi) RegistroMissioni.getMissioniNonCompletate().stream()
                    .filter(m -> m.getClasse() == MissionePredefinita.CACCIA_AI_GOBLIN).findFirst().get();
            VoceMissione prima = voce(DovePartono.CITTA, MissionePredefinita.CACCIA_AI_GOBLIN);
            assertTrue(prima.isForzabile());
            String passoIniziale = missione.getPassoCorrente();

            strumenti.forzaPartenza(MissionePredefinita.CACCIA_AI_GOBLIN);
            assertEquals("partenza forzata", voce(DovePartono.CITTA, MissionePredefinita.CACCIA_AI_GOBLIN).getStato());
            assertFalse(voce(DovePartono.CITTA, MissionePredefinita.CACCIA_AI_GOBLIN).isForzabile());
            assertThrows(IllegalStateException.class, () -> strumenti.forzaPartenza(MissionePredefinita.CACCIA_AI_GOBLIN));

            // Il gruppo è in una città: al prossimo controllo la missione parte
            missione.controllaPreLocazione();
            assertNotEquals(passoIniziale, missione.getPassoCorrente());
        }
    }

    @Test
    void unaMissioneForzataNonPartePerStradaFuoriDalLuogoGiusto() {
        try (PartitaDiTest partita = partitaInCitta()) {
            MissioneAPassi missione = (MissioneAPassi) RegistroMissioni.getMissioniNonCompletate().stream()
                    .filter(m -> m.getClasse() == MissionePredefinita.LA_LEGGENDA_DEL_LOCANDIERE).findFirst().get();
            String passoIniziale = missione.getPassoCorrente();
            strumenti.forzaPartenza(MissionePredefinita.LA_LEGGENDA_DEL_LOCANDIERE);
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

    private List<MissionePredefinita> classi(List<VoceMissione> voci) {
        return voci.stream().map(VoceMissione::getClasse).collect(Collectors.toList());
    }

    private VoceMissione voce(DovePartono dove, MissionePredefinita classe) {
        return strumenti.getMissioni(dove).stream().filter(v -> v.getClasse() == classe).findFirst().get();
    }
}
