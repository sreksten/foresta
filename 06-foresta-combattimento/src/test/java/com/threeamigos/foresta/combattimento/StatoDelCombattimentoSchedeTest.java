package com.threeamigos.foresta.combattimento;

import com.threeamigos.foresta.gruppi.DatiDiProva;

import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le schede dei personaggi e la conoscenza pubblica sulle reazioni dello StatoDelCombattimento.
 */
class StatoDelCombattimentoSchedeTest {

    private StatoDelCombattimento stato;
    private Personaggio guerriero;
    private Personaggio goblin;
    private Personaggio troll;

    @BeforeEach
    void prepara() {
        DatiDiProva.nuovaPartita();
        BusEventi.impostaConsegna(Runnable::run);
        StatoDelCombattimento.azzeraIstanza();
        stato = StatoDelCombattimento.getIstanza();
        guerriero = FabbricaPersonaggi.crea(TipoPersonaggio.GUERRIERO, 1);
        goblin = FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 1);
        troll = FabbricaPersonaggi.crea(TipoPersonaggio.TROLL, 1);
    }

    @AfterEach
    void ripristina() {
        BusEventi.impostaConsegna(null);
        StatoDelCombattimento.azzeraIstanza();
    }

    @Test
    void unaSchedaVuotaNonRicordaNiente() {
        assertNull(stato.getUltimoAvversario(guerriero));
        assertNull(stato.getUltimoAlleato(guerriero));
        assertNull(stato.getUltimoAttaccante(guerriero));
        assertFalse(stato.haLanciatoUnIncantesimo(guerriero));
        assertEquals(0, stato.getCambiDArma(guerriero));
        assertFalse(stato.haUsatoLaVoce(guerriero, "v1"));
        assertEquals(0, stato.getRoundUltimoUsoVoce(guerriero, "v1"));
        assertEquals(1.0d, stato.getFattoreEquipaggiamentoNoto(goblin, TipoDanno.FUOCO), 1e-9);
        assertFalse(stato.isReazioneNota(goblin, TipoDanno.FUOCO));
    }

    @Test
    void unColpoRicordaChiHaColpitoChi() {
        stato.registraColpo(new DannoRisultante(guerriero, goblin));
        assertSame(goblin, stato.getUltimoAvversario(guerriero));
        assertSame(guerriero, stato.getUltimoAttaccante(goblin));
        assertNull(stato.getUltimoAttaccante(guerriero));

        stato.registraColpo(new DannoRisultante(guerriero, troll));
        assertSame(troll, stato.getUltimoAvversario(guerriero));
        assertSame(guerriero, stato.getUltimoAttaccante(goblin));
    }

    @Test
    void ilNumeroDeiCambiDArmaEIlLancioSiContano() {
        stato.registraCambioArma(guerriero);
        stato.registraCambioArma(guerriero);
        stato.registraIncantesimoLanciato(goblin);
        stato.registraAlleato(guerriero, troll);
        assertEquals(2, stato.getCambiDArma(guerriero));
        assertEquals(0, stato.getCambiDArma(goblin));
        assertTrue(stato.haLanciatoUnIncantesimo(goblin));
        assertFalse(stato.haLanciatoUnIncantesimo(guerriero));
        assertSame(troll, stato.getUltimoAlleato(guerriero));
    }

    @Test
    void leVociUsateRicordanoIlRound() {
        stato.registraUsoVoce(goblin, "silenzio");
        assertEquals(1, stato.getRoundUltimoUsoVoce(goblin, "silenzio"));
        assertTrue(stato.haUsatoLaVoce(goblin, "silenzio"));
        assertFalse(stato.haUsatoLaVoce(goblin, "altra"));
        assertFalse(stato.haUsatoLaVoce(troll, "silenzio"));
    }

    @Test
    void laConoscenzaSulleReazioniEPubblicaEChiunqueLaLegge() {
        // Registrata da un colpo di chiunque, vale per tutti: non c'è una conoscenza per osservatore
        DannoRisultante risultato = new DannoRisultante(guerriero, goblin);
        risultato.setTipoDanno(TipoDanno.FUOCO);
        risultato.setFattoreEquipaggiamento(0.6d);
        stato.registraColpo(risultato);
        assertTrue(stato.isReazioneNota(goblin, TipoDanno.FUOCO));
        assertEquals(0.6d, stato.getFattoreEquipaggiamentoNoto(goblin, TipoDanno.FUOCO), 1e-9);
        assertFalse(stato.isReazioneNota(goblin, TipoDanno.GELO));
        assertFalse(stato.isReazioneNota(troll, TipoDanno.FUOCO));
    }

    @Test
    void unaNuovaOsservazioneSostituisceLaPrecedente() {
        stato.registraReazione(goblin, TipoDanno.FUOCO, 0.6d);
        stato.registraReazione(goblin, TipoDanno.FUOCO, 1.0d);
        assertEquals(1.0d, stato.getFattoreEquipaggiamentoNoto(goblin, TipoDanno.FUOCO), 1e-9);
    }

    @Test
    void ilFattoreDiUnColpoSenzaEquipaggiamentoVale1() {
        DannoRisultante risultato = CalcolatoreCombattimento.calcolaDannoRisultante(guerriero, goblin, guerriero.getArmaEquipaggiata());
        assertEquals(1.0d, risultato.getFattoreEquipaggiamento(), 1e-9);
    }

    @Test
    void laSchedaSiAzzeraConLaLocazione() {
        stato.registraColpo(new DannoRisultante(guerriero, goblin));
        stato.registraReazione(goblin, TipoDanno.FUOCO, 0.6d);
        stato.registraCambioArma(guerriero);
        stato.reimposta();
        assertNull(stato.getUltimoAvversario(guerriero));
        assertEquals(0, stato.getCambiDArma(guerriero));
        assertFalse(stato.isReazioneNota(goblin, TipoDanno.FUOCO));
    }
}
