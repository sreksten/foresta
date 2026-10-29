package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.economia.Cassa;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.tipi.CostantiPersonaggi;

import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Fuggire costa una percentuale della salute massima (non un danno fisso, che al primo livello uccide) e non uccide mai.
 */
class FugaTest {

    @AfterEach
    void ripristina() {
        Dado.ripristina();
    }

    private static Personaggio personaggioInForze(PartitaDiTest partita, TipoPersonaggio classe) {
        Personaggio p = FabbricaPersonaggi.crea(classe, 1);
        p.addSalute(p.getSaluteMassima());
        return p;
    }

    private static PartitaDiTest partita(long seme) {
        PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(seme);
        partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
        return partita;
    }

    @Test
    void ilCostoELaPercentualeTirataDellaSaluteMassima() {
        try (PartitaDiTest partita = partita(318)) {
            Personaggio mago = personaggioInForze(partita, TipoPersonaggio.MAGO);
            int massima = mago.getSaluteMassima();

            Dado.trucca(CostantiPersonaggi.FUGA_DANNO_MINIMO_PERCENTUALE);
            mago.fugge();
            assertEquals(massima - massima * CostantiPersonaggi.FUGA_DANNO_MINIMO_PERCENTUALE / 100, mago.getSalute(), "con il tiro più basso");

            Personaggio altro = personaggioInForze(partita, TipoPersonaggio.MAGO);
            Dado.trucca(CostantiPersonaggi.FUGA_DANNO_MASSIMO_PERCENTUALE);
            altro.fugge();
            assertEquals(massima - massima * CostantiPersonaggi.FUGA_DANNO_MASSIMO_PERCENTUALE / 100, altro.getSalute(), "con il tiro più alto");
            assertTrue(altro.isVivo());
        }
    }

    @Test
    void laFugaNonUccideMaiAncheSeIlTiroELAltoELaSaluteBassa() {
        try (PartitaDiTest partita = partita(319)) {
            for (int salute : new int[]{1, 2, 10}) {
                Personaggio ferito = personaggioInForze(partita, TipoPersonaggio.MAGO);
                ferito.subSalute(ferito.getSalute() - salute, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
                Dado.trucca(CostantiPersonaggi.FUGA_DANNO_MASSIMO_PERCENTUALE);

                ferito.fugge();

                assertTrue(ferito.isVivo(), "chi fugge con " + salute + " punti non muore");
                int danno = Math.max(1, ferito.getSaluteMassima() * CostantiPersonaggi.FUGA_DANNO_MASSIMO_PERCENTUALE / 100);
                assertEquals(Math.max(1, salute - danno), ferito.getSalute(), "ne perde fino al tiro, ma gliene resta almeno uno");
            }
        }
    }

    @Test
    void alPrimoLivelloNessunoMuoreFuggendoNeanchePiuVolte() {
        try (PartitaDiTest partita = partita(320)) {
            for (int i = 0; i < 200; i++) {
                Personaggio mago = personaggioInForze(partita, TipoPersonaggio.MAGO);
                mago.fugge();
                assertTrue(mago.isVivo());
                assertTrue(mago.getSalute() >= mago.getSaluteMassima() * (100 - CostantiPersonaggi.FUGA_DANNO_MASSIMO_PERCENTUALE) / 100 - 1,
                        "perde al massimo il " + CostantiPersonaggi.FUGA_DANNO_MASSIMO_PERCENTUALE + "%: " + mago.getSalute());
            }
        }
    }

    @Test
    void ilGruppoCheFuggePerdeUnaPercentualeDelleScorteENonGliArtefatti() {
        try (PartitaDiTest partita = partita(321)) {
            GruppoGiocatore gruppo = partita.gruppo();
            Cassa.addMonete(100 - Cassa.getMonete());
            gruppo.addPozioniSalute(10 - gruppo.getPozioniSalute());
            gruppo.addPozioniMagia(3 - gruppo.getPozioniMagia());
            gruppo.addIncantesimi(com.threeamigos.foresta.tipi.TipoIncantesimo.FUOCO, 5 - gruppo.getIncantesimi(com.threeamigos.foresta.tipi.TipoIncantesimo.FUOCO));
            int artefatti = gruppo.getInventario().size();

            // Come in LocazioneBase: il gruppo perde le sue scorte, la cassa monete e preziosi
            gruppo.fugge();
            Cassa.perdeNellaFuga();

            int percentuale = CostantiPersonaggi.FUGA_PERDITA_CONSUMABILI_PERCENTUALE;
            assertEquals(100 - 100 * percentuale / 100, Cassa.getMonete());
            assertEquals(10 - 10 * percentuale / 100, gruppo.getPozioniSalute());
            assertEquals(5 - 5 * percentuale / 100, gruppo.getIncantesimi(com.threeamigos.foresta.tipi.TipoIncantesimo.FUOCO));
            assertEquals(3, gruppo.getPozioniMagia(), "con poche pozioni, arrotondando per difetto, non se ne perde nessuna");
            assertEquals(artefatti, gruppo.getInventario().size());
        }
    }
}
