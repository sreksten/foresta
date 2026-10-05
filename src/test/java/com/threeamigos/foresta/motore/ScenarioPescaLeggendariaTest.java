package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.missioni.LaLeggenda;
import com.threeamigos.foresta.missioni.LaLeggendaDellArmaiolo;
import com.threeamigos.foresta.missioni.OggettoLeggendario;
import com.threeamigos.foresta.missioni.PescaLeggendaria;
import com.threeamigos.foresta.missioni.SetLeggendario;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoLocazione;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.IntUnaryOperator;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * La pesca delle leggende favorisce i set cominciati: con un set cominciato, una leggenda su due ne racconta un pezzo
 * mancante, del set più vicino a essere completo, e dopo tre leggende di fila senza pezzi mancanti la successiva lo
 * racconta per forza. I test prendono set e pezzi dal catalogo, senza nominarli.
 */
class ScenarioPescaLeggendariaTest {

    /**
     * Un dado che fa sempre 1: la leggenda tocca al set cominciato, e del set il primo pezzo mancante.
     */
    private static final IntUnaryOperator FA_UNO = facce -> 1;
    /**
     * Un dado che fa sempre il massimo: con il 50% la leggenda non tocca al set cominciato.
     */
    private static final IntUnaryOperator FA_IL_MASSIMO = facce -> facce;

    @Test
    void senzaSetCominciatiSiPescaACaso() {
        assertEquals(Optional.empty(), PescaLeggendaria.pezzoDaRaccontare(Collections.emptySet(), 0, FA_UNO));
        assertEquals(Optional.empty(), PescaLeggendaria.pezzoDaRaccontare(Collections.emptySet(), 5, FA_UNO));
        // Un set già completo non è cominciato: non ha pezzi mancanti
        Set<String> completo = new HashSet<>(pezziDi(unSet()));
        assertTrue(PescaLeggendaria.setCominciati(completo).isEmpty());
        assertEquals(Optional.empty(), PescaLeggendaria.pezzoDaRaccontare(completo, 5, FA_UNO));
    }

    @Test
    void conUnSetCominciatoUnaLeggendaSuDueNeRaccontaUnPezzoMancante() {
        SetLeggendario set = unSet();
        Set<String> giaPescati = Collections.singleton(pezziDi(set).get(0));
        assertEquals(Collections.singletonList(set.getChiave()), chiavi(PescaLeggendaria.setCominciati(giaPescati)));

        String pezzo = PescaLeggendaria.pezzoDaRaccontare(giaPescati, 0, FA_UNO).orElseThrow(AssertionError::new);
        assertTrue(pezziDi(set).contains(pezzo) && !giaPescati.contains(pezzo), pezzo);
        assertTrue(PescaLeggendaria.isPezzoMancante(pezzo, giaPescati));
        assertFalse(PescaLeggendaria.isPezzoMancante(pezziDi(set).get(0), giaPescati));

        // Con il dado sopra il 50% si pesca a caso
        assertEquals(Optional.empty(), PescaLeggendaria.pezzoDaRaccontare(giaPescati, 0, FA_IL_MASSIMO));
        assertEquals(Optional.empty(), PescaLeggendaria.pezzoDaRaccontare(giaPescati,
                PescaLeggendaria.LEGGENDE_SENZA_PEZZI_MASSIME - 1, FA_IL_MASSIMO));
    }

    @Test
    void dopoTroppeLeggendeSenzaPezziMancantiLaSuccessivaNeRaccontaUnoPerForza() {
        SetLeggendario set = unSet();
        Set<String> giaPescati = Collections.singleton(pezziDi(set).get(0));
        String pezzo = PescaLeggendaria.pezzoDaRaccontare(giaPescati, PescaLeggendaria.LEGGENDE_SENZA_PEZZI_MASSIME, FA_IL_MASSIMO)
                .orElseThrow(AssertionError::new);
        assertTrue(pezziDi(set).contains(pezzo) && !giaPescati.contains(pezzo), pezzo);
    }

    @Test
    void fraPiuSetCominciatiSiPreferisceQuelloPiuVicinoAEssereCompleto() {
        // Il set con più pezzi, quasi completo, e un altro con un pezzo solo uscito
        List<SetLeggendario> perPezzi = CatalogoLeggendari.getTuttiISet().values().stream()
                .sorted(Comparator.comparingInt((SetLeggendario set) -> -pezziDi(set).size()).thenComparing(SetLeggendario::getChiave))
                .collect(Collectors.toList());
        SetLeggendario quasiCompleto = perPezzi.get(0);
        assertTrue(pezziDi(quasiCompleto).size() >= 3, "serve un set con almeno tre pezzi");
        SetLeggendario appenaCominciato = perPezzi.get(1);
        List<String> pezzi = pezziDi(quasiCompleto);
        Set<String> giaPescati = new HashSet<>(pezzi.subList(0, pezzi.size() - 1));
        giaPescati.add(pezziDi(appenaCominciato).get(0));

        assertEquals(quasiCompleto.getChiave(), PescaLeggendaria.setCominciati(giaPescati).get(0).getChiave());
        assertEquals(Optional.of(pezzi.get(pezzi.size() - 1)), PescaLeggendaria.pezzoDaRaccontare(giaPescati, 0, FA_UNO));
    }

    @Test
    void laLeggendaContaLeLeggendeSenzaPezziGiaRaccontateEDopoTreRaccontaUnPezzoMancante() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(211)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
            LaLeggenda leggenda = trova(LaLeggendaDellArmaiolo.class);
            assertNull(leggenda.getLeggendario(), "la leggenda non deve essere già stata raccontata");
            long adesso = LineaTemporale.getGiorno() * 24L + LineaTemporale.getOra();

            // Tanto tempo fa una leggenda ha cominciato un set; poi tre leggende senza pezzi mancanti
            SetLeggendario set = unSet();
            raccontata(pezziDi(set).get(0), adesso - 400, false);
            List<String> senzaSet = CatalogoLeggendari.getTuttiILeggendari().values().stream()
                    .filter(leggendario -> leggendario.getSet() == null)
                    .map(OggettoLeggendario::getChiave).sorted().collect(Collectors.toList());
            for (int i = 0; i < PescaLeggendaria.LEGGENDE_SENZA_PEZZI_MASSIME; i++) {
                raccontata(senzaSet.get(i), adesso - 300 + i * 50L, true);
            }

            // A una visita tranquilla in città l'armaiolo racconta per forza un pezzo mancante di quel set
            partita.gruppo().setCoordinate(Foresta.getCoordinateLocazioneUnica(TipoLocazione.CITTA_NYENA));
            leggenda.controllaPreLocazione();
            OggettoLeggendario raccontato = leggenda.getLeggendario();
            assertNotNull(raccontato, "l'armaiolo racconta la leggenda");
            assertEquals(set.getChiave(), raccontato.getSet());
            assertNotEquals(pezziDi(set).get(0), raccontato.getChiave());
            assertNull(leggenda.ottieniProprieta("SENZA_PEZZI"), "ha raccontato un pezzo mancante");
        }
    }

    @Test
    void ilNarratoreDiceSeLaLeggendaRaccontaUnAltroPezzoDiUnSetCominciato() {
        SetLeggendario set = unSet();
        List<String> pezzi = pezziDi(set);
        OggettoLeggendario secondo = Leggendari.con(pezzi.get(1));
        // Il primo pezzo che esce non ha niente da dire
        assertEquals(Optional.empty(), PescaLeggendaria.battutaDelSet(secondo, Collections.emptySet()));
        // Un altro pezzo, quando ne è già uscito uno
        String battuta = PescaLeggendaria.battutaDelSet(secondo, Collections.singleton(pezzi.get(0))).orElseThrow(AssertionError::new);
        assertTrue(battuta.startsWith("Vi interessa " + set.getNome() + "? Allora ascoltate: questo è un altro dei suoi pezzi, "), battuta);
        // L'ultimo pezzo che manca
        OggettoLeggendario ultimo = Leggendari.con(pezzi.get(pezzi.size() - 1));
        String battutaUltimo = PescaLeggendaria.battutaDelSet(ultimo, new HashSet<>(pezzi.subList(0, pezzi.size() - 1)))
                .orElseThrow(AssertionError::new);
        assertTrue(battutaUltimo.endsWith("ed è l'ultimo che manca."), battutaUltimo);
        // Un leggendario fuori dai set
        CatalogoLeggendari.getTuttiILeggendari().values().stream().filter(leggendario -> leggendario.getSet() == null).findFirst()
                .ifPresent(leggendario -> assertEquals(Optional.empty(), PescaLeggendaria.battutaDelSet(leggendario, new HashSet<>(pezzi))));
    }

    // --- Aiuti

    private static SetLeggendario unSet() {
        return CatalogoLeggendari.getTuttiISet().values().stream().min(Comparator.comparing(SetLeggendario::getChiave))
                .orElseThrow(AssertionError::new);
    }

    private static List<String> pezziDi(SetLeggendario set) {
        return new ArrayList<>(CatalogoLeggendari.getPezzi(set.getChiave()));
    }

    private static List<String> chiavi(List<SetLeggendario> set) {
        return set.stream().map(SetLeggendario::getChiave).collect(Collectors.toList());
    }

    /**
     * Una leggenda già raccontata in un'altra città, di quel leggendario, a quell'ora.
     */
    private static void raccontata(String leggendario, long alle, boolean senzaPezzi) {
        LaLeggenda leggenda = new LaLeggendaDellArmaiolo();
        leggenda.aggiungiProprieta(LaLeggenda.LEGGENDARIO, Leggendari.con(leggendario).getRiga());
        leggenda.aggiungiProprieta("RACCONTATA_ALLE", String.valueOf(alle));
        if (senzaPezzi) {
            leggenda.aggiungiProprieta("SENZA_PEZZI", "S");
        }
        RegistroMissioni.getMissionePrincipale().aggiungiMissione(leggenda);
    }

    private static LaLeggenda trova(Class<? extends LaLeggenda> tipo) {
        return RegistroMissioni.getTutteLeMissioni().stream().filter(tipo::isInstance).map(LaLeggenda.class::cast).findFirst()
                .orElseThrow(() -> new AssertionError("missione " + tipo.getSimpleName() + " non trovata"));
    }
}
