package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.TipoAzione;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I boss più astuti (Drago, Lich, Strega) non lanciano Silenzio a ogni turno: uno alla volta, e una volta su due (vedi
 * TatticheBase.fasciaStratega). Senza queste due condizioni passavano quasi tutti i turni a silenziare, senza fare danno.
 */
class SilenzioDeiBossTest {

    private static final int LIVELLO = 8;

    @BeforeEach
    @AfterEach
    void bus() {
        BusEventi.azzera();
        BusEventi.impostaConsegna(Runnable::run);
    }

    /** Le azioni che il boss sceglie in {@code round} round contro Mago e Sacerdote; se {@code unoSilenziato} il Mago lo è per tutta la prova. */
    private static List<TipoAzione> azioniDelBoss(TipoPersonaggio classeBoss, int round, boolean unoSilenziato) {
        List<TipoAzione> azioni = new ArrayList<>();
        for (int prova = 0; prova < round; prova++) {
            ModelloDati.installa();
            ModelloDati.setIstanza(new ModelloDati());
            List<Personaggio> miei = new ArrayList<>();
            for (TipoPersonaggio classe : Arrays.asList(TipoPersonaggio.GUERRIERO, TipoPersonaggio.MAGO, TipoPersonaggio.SACERDOTE)) {
                Personaggio personaggio = FabbricaPersonaggi.crea(classe, LIVELLO);
                Equipaggiamento.tipiciPer(classe).get(0).equipaggia(personaggio);
                miei.add(personaggio);
            }
            if (unoSilenziato) {
                miei.get(1).addEffettoDiStato(TipoEffettoDiStato.SILENZIATO, 50, 0);
            }
            RoundASimulato scontro = new RoundASimulato(miei, new ArrayList<>(Arrays.asList(FabbricaPersonaggi.crea(classeBoss, LIVELLO))));
            scontro.osservatoreDegliAvversari = (chi, decisione) -> {
                if (decisione != null) {
                    azioni.add(decisione.getTipoEseguito());
                }
            };
            // I miei non fanno niente: si guarda solo la prima scelta del boss
            scontro.eseguiRound((chi, r) -> {
            });
        }
        return azioni;
    }

    @Test
    void conUnoGiaSilenziatoIlBossNonLanciaUnAltroSilenzio() {
        for (TipoPersonaggio boss : new TipoPersonaggio[] {TipoPersonaggio.DRAGO, TipoPersonaggio.LICH, TipoPersonaggio.STREGA}) {
            List<TipoAzione> azioni = azioniDelBoss(boss, 60, true);
            assertFalse(azioni.isEmpty(), boss.name());
            assertFalse(azioni.contains(TipoAzione.SILENZIO), boss + " ha lanciato Silenzio con un avversario già silenziato: " + azioni);
        }
    }

    @Test
    void senzaSilenziatiIlBossLanciaSilenzioSoloUnaVoltaSuDue() {
        for (TipoPersonaggio boss : new TipoPersonaggio[] {TipoPersonaggio.DRAGO, TipoPersonaggio.LICH, TipoPersonaggio.STREGA}) {
            List<TipoAzione> azioni = azioniDelBoss(boss, 200, false);
            long silenzi = azioni.stream().filter(a -> a == TipoAzione.SILENZIO).count();
            assertTrue(silenzi > azioni.size() * 0.25 && silenzi < azioni.size() * 0.75,
                    boss + ": " + silenzi + " Silenzi su " + azioni.size() + " azioni, ne aspettavo circa la metà");
        }
    }
}
