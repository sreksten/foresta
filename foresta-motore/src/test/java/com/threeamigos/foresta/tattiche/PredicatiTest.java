package com.threeamigos.foresta.tattiche;

import com.threeamigos.foresta.modellodati.tattiche.FiltroMD;
import com.threeamigos.foresta.modellodati.tattiche.PredicatoMD;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.motore.Dado;
import com.threeamigos.foresta.combattimento.StatoDelCombattimento;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.CategoriaPersonaggio;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.tipi.tattiche.Confronto;
import com.threeamigos.foresta.tipi.tattiche.TipoFiltro;
import com.threeamigos.foresta.tipi.tattiche.TipoPredicato;
import com.threeamigos.foresta.tipi.tattiche.TipoRosa;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le condizioni di una voce che non sono un controllo su un personaggio (tattiche_di_combattimento_automatiche.md §11.8).
 */
class PredicatiTest {

    private Personaggio guerriero;
    private Personaggio ladro;
    private Personaggio goblin;
    private Personaggio fantasma;
    private ContestoTattico contesto;

    @BeforeEach
    void prepara() {
        ModelloDati.setIstanza(new ModelloDati());
        StatoDelCombattimento.getIstanza().reimposta();
        guerriero = FabbricaPersonaggi.crea(TipoPersonaggio.GUERRIERO, 1);
        ladro = FabbricaPersonaggi.crea(TipoPersonaggio.LADRO, 1);
        goblin = FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 1);
        fantasma = FabbricaPersonaggi.crea(TipoPersonaggio.FANTASMA, 1);
        contesto = new ContestoTattico(guerriero, true, Arrays.asList(guerriero, ladro), Arrays.asList(goblin, fantasma), Collections.emptyList());
    }

    @AfterEach
    void ripristina() {
        Dado.ripristina();
    }

    private boolean vale(PredicatoMD predicato) {
        return Predicati.vale(predicato, contesto);
    }

    @Test
    void ogniTipoDiPredicatoHaLaSuaRisposta() {
        for (TipoPredicato tipo : TipoPredicato.values()) {
            assertTrue(Predicati.haLaSuaRisposta(tipo), tipo.name());
        }
    }

    @Test
    void ilParametroDeveEssereQuelloDelTipoDiPredicato() {
        assertThrows(IllegalArgumentException.class, () -> PredicatoMD.conConfrontoENumero(TipoPredicato.PROBABILITA, Confronto.MINORE, 3));
        assertThrows(IllegalArgumentException.class, () -> PredicatoMD.conConfrontoENumero(TipoPredicato.QUANTI, Confronto.MINORE, 3));
        assertDoesNotThrow(() -> PredicatoMD.conConfrontoENumero(TipoPredicato.ROUND, Confronto.UGUALE, 1));
    }

    @Test
    void quantiContaICandidatiDopoIFiltri() {
        FiltroMD nonMorto = FiltroMD.conCategoria(TipoFiltro.CATEGORIA, CategoriaPersonaggio.NON_MORTO);
        // "C'è un non morto fra gli avversari"
        PredicatoMD esisteNonMorto = PredicatoMD.quanti(TipoRosa.AVVERSARI, Collections.singletonList(nonMorto), Confronto.MAGGIORE, 0);
        assertTrue(vale(esisteNonMorto));
        assertFalse(vale(esisteNonMorto.negato()));
        // "Non ci sono non morti fra gli alleati"
        assertTrue(vale(PredicatoMD.quanti(TipoRosa.ALLEATI, Collections.singletonList(nonMorto), Confronto.UGUALE, 0)));
        // "Gli avversari sono almeno 2" è NON MINORE 2, "almeno 3" no
        assertTrue(vale(PredicatoMD.quanti(TipoRosa.AVVERSARI, Collections.emptyList(), Confronto.MINORE, 2).negato()));
        assertFalse(vale(PredicatoMD.quanti(TipoRosa.AVVERSARI, Collections.emptyList(), Confronto.MINORE, 3).negato()));
        // "Sono l'unico vivo"
        assertFalse(vale(PredicatoMD.quanti(TipoRosa.ALLEATI, Collections.emptyList(), Confronto.UGUALE, 1)));
        guerriero.subSalute(1_000_000, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        assertFalse(guerriero.isVivo());
        ContestoTattico delLadro = new ContestoTattico(ladro, true, Arrays.asList(guerriero, ladro), Arrays.asList(goblin, fantasma), Collections.emptyList());
        assertTrue(Predicati.vale(PredicatoMD.quanti(TipoRosa.ALLEATI, Collections.emptyList(), Confronto.UGUALE, 1), delLadro));
    }

    @Test
    void quantiConLaRosaSeStessoControllaIPropriStati() {
        PredicatoMD spaventato = PredicatoMD.quanti(TipoRosa.SE_STESSO,
                Collections.singletonList(FiltroMD.conStato(TipoFiltro.STATO, TipoEffettoDiStato.SPAVENTATO)), Confronto.UGUALE, 1);
        assertFalse(vale(spaventato));
        guerriero.addEffettoDiStato(TipoEffettoDiStato.SPAVENTATO, 3, 0);
        assertTrue(vale(spaventato));
    }

    @Test
    void ilRoundSiLeggeDalloStatoDelCombattimento() {
        assertEquals(1, StatoDelCombattimento.getIstanza().getRound());
        assertTrue(vale(PredicatoMD.conConfrontoENumero(TipoPredicato.ROUND, Confronto.UGUALE, 1)));
        assertFalse(vale(PredicatoMD.conConfrontoENumero(TipoPredicato.ROUND, Confronto.MAGGIORE, 1)));
        assertTrue(vale(PredicatoMD.conConfrontoENumero(TipoPredicato.ROUND, Confronto.UGUALE, 1).negato().negato()));
    }

    @Test
    void l_AstuziaEILMassimoDiClassePerLaRampaSulLivello() {
        double massimo = guerriero.getMaxStatistica(com.threeamigos.foresta.tipi.TipoAttributo.INTELLIGENZA);
        assertEquals(massimo * Costanti.ASTUZIA_PARTENZA, Astuzia.di(guerriero), 1e-9, "al livello 1 il 40%");
        Personaggio stregaAlta = FabbricaPersonaggi.crea(TipoPersonaggio.STREGA, 9);
        assertEquals(stregaAlta.getMaxStatistica(com.threeamigos.foresta.tipi.TipoAttributo.INTELLIGENZA), Astuzia.di(stregaAlta), 1e-9, "al livello 9 il pieno");
        Personaggio stregaBassa = FabbricaPersonaggi.crea(TipoPersonaggio.STREGA, 1);
        ContestoTattico dellaStrega = new ContestoTattico(stregaBassa, false, Collections.singletonList(stregaBassa), Collections.singletonList(guerriero), Collections.emptyList());
        // La Strega ha 65 di massimo: 26 al livello 1, quindi è della terza fascia (20-30) e non della quarta
        assertTrue(Predicati.vale(PredicatoMD.conConfrontoENumero(TipoPredicato.ASTUZIA, Confronto.MINORE, 20).negato(), dellaStrega));
        assertFalse(Predicati.vale(PredicatoMD.conConfrontoENumero(TipoPredicato.ASTUZIA, Confronto.MINORE, 31).negato(), dellaStrega));
        assertTrue(vale(PredicatoMD.conConfrontoENumero(TipoPredicato.ASTUZIA, Confronto.MINORE, 12)), "il Guerriero è tonto al livello 1");
    }

    @Test
    void laProbabilitaTiraIlDado() {
        Dado.trucca(30, 70);
        PredicatoMD unaVoltaSuTre = PredicatoMD.probabilita(33);
        assertTrue(vale(unaVoltaSuTre), "30 è dentro il 33%");
        assertFalse(vale(unaVoltaSuTre), "70 no");
        for (int i = 0; i < 20; i++) {
            assertTrue(vale(PredicatoMD.probabilita(100)));
            assertFalse(vale(PredicatoMD.probabilita(0)));
        }
    }

    @Test
    void gliUsiDellaVoceSiContanoPerPersonaggioEPerVoce() {
        StatoDelCombattimento stato = StatoDelCombattimento.getIstanza();
        PredicatoMD mai = PredicatoMD.conConfrontoENumero(TipoPredicato.VOCE_USATA, Confronto.UGUALE, 0);
        ContestoTattico perLaVoce = contesto.conVoce("v1");
        assertTrue(Predicati.vale(mai, perLaVoce), "una volta per scontro: ancora mai usata");
        stato.registraUsoVoce(guerriero, "v1");
        stato.registraUsoVoce(guerriero, "v1");
        assertFalse(Predicati.vale(mai, perLaVoce));
        assertTrue(Predicati.vale(PredicatoMD.conConfrontoENumero(TipoPredicato.VOCE_USATA, Confronto.UGUALE, 2), perLaVoce));
        assertTrue(Predicati.vale(mai, contesto.conVoce("v2")), "un'altra voce");
        assertEquals(0, stato.getUsiDellaVoce(ladro, "v1"), "un altro personaggio");
        assertThrows(IllegalStateException.class, () -> vale(mai), "senza dire quale voce");
    }

    @Test
    void ilCooldownContaIRoundDallUltimoUso() {
        StatoDelCombattimento stato = StatoDelCombattimento.getIstanza();
        PredicatoMD dopoDueRound = PredicatoMD.conConfrontoENumero(TipoPredicato.ROUND_DALL_ULTIMO_USO, Confronto.MINORE, 2).negato();
        ContestoTattico perLaVoce = contesto.conVoce("fiato");
        assertTrue(Predicati.vale(dopoDueRound, perLaVoce), "mai usata: il periodo di attesa è finito");
        stato.registraUsoVoce(guerriero, "fiato");
        assertFalse(Predicati.vale(dopoDueRound, perLaVoce), "appena usata");
        assertTrue(Predicati.vale(PredicatoMD.conConfrontoENumero(TipoPredicato.ROUND_DALL_ULTIMO_USO, Confronto.UGUALE, 0), perLaVoce));
    }

    @Test
    void leCondizioniDiUnaVoceSonoInAnd() {
        PredicatoMD vero = PredicatoMD.conConfrontoENumero(TipoPredicato.ROUND, Confronto.UGUALE, 1);
        PredicatoMD falso = vero.negato();
        assertTrue(Predicati.valgonoTutti(Collections.emptyList(), contesto), "nessuna condizione: sempre");
        assertTrue(Predicati.valgonoTutti(Arrays.asList(vero, vero), contesto));
        assertFalse(Predicati.valgonoTutti(Arrays.asList(vero, falso), contesto));
    }
}
