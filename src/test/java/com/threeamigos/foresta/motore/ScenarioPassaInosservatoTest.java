package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.locazioni.LocazioneBase;
import com.threeamigos.foresta.missioni.IncontroDiMissione;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.personaggi.Ladro;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoLocazione;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Passare inosservati: la furtività del gruppo (la peggiore, o quella del ladro che lo guida) contro la percezione
 * migliore degli avversari; di notte è più facile, in tanti più difficile; mai sotto il 5% né sopra il 75%.
 */
class ScenarioPassaInosservatoTest {

    private static final int GIORNO = 12;
    private static final int NOTTE = 23;

    @Test
    void laProbabilitaDipendeDaFurtivitaPercezioneOraEGruppo() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(261)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            GruppoGiocatore gruppo = partita.gruppo();
            GruppoAvversario avversari = GruppoAvversario.getIstanza();
            avversari.rimuoviPersonaggi();
            Personaggio troll = ClassePersonaggio.TROLL.getIstanza(1);
            avversari.aggiungiPersonaggio(troll);
            Personaggio arsenio = gruppo.getCapo();

            int attesa = 40 + 10 * (arsenio.getFurtivita() - troll.getPercezione());
            assertEquals(Math.max(5, Math.min(75, attesa)), LocazioneBase.probabilitaDiPassareInosservati(gruppo, avversari, GIORNO));
            assertEquals(Math.max(5, Math.min(75, attesa + 15)), LocazioneBase.probabilitaDiPassareInosservati(gruppo, avversari, NOTTE));

            // In due si fa più rumore, e conta il più maldestro
            Guerriero compagno = new Guerriero("Compagno", 1);
            gruppo.aggiungiPersonaggio(compagno);
            int peggiore = Math.min(arsenio.getFurtivita(), compagno.getFurtivita());
            assertEquals(Math.max(5, Math.min(75, 40 + 10 * (peggiore - troll.getPercezione()) - 10)),
                    LocazioneBase.probabilitaDiPassareInosservati(gruppo, avversari, GIORNO));

            // Con un ladro, conta lui
            Ladro ladro = new Ladro("Grimaldello", 1);
            gruppo.aggiungiPersonaggio(ladro);
            assertEquals(Math.max(5, Math.min(75, 40 + 10 * (ladro.getFurtivita() - troll.getPercezione()) - 20)),
                    LocazioneBase.probabilitaDiPassareInosservati(gruppo, avversari, GIORNO));

            // Contro chi vede tutto, quasi impossibile; mai più del massimo
            avversari.aggiungiPersonaggio(ClassePersonaggio.DRAGO.getIstanza(10));
            assertEquals(5, LocazioneBase.probabilitaDiPassareInosservati(gruppo, avversari, GIORNO));
            avversari.rimuoviPersonaggi();
            avversari.aggiungiPersonaggio(ClassePersonaggio.GIGANTE.getIstanza(1));
            gruppo.rimuoviPersonaggio(arsenio);
            gruppo.rimuoviPersonaggio(compagno);
            assertTrue(LocazioneBase.probabilitaDiPassareInosservati(gruppo, avversari, NOTTE) <= 75);
        }
    }

    @Test
    void gliAvversariDiUnaMissioneVannoAffrontatiTranneLeGuardieDiUnColpo() {
        Personaggio covo = IncontroDiMissione.di(ClassePersonaggio.GOBLIN, 2).crea().get(0);
        Personaggio guardia = IncontroDiMissione.di(ClassePersonaggio.GOBLIN, 2).aggirabile().crea().get(0);
        assertTrue(covo.isDaAffrontare());
        assertFalse(guardia.isDaAffrontare());
        assertFalse(ClassePersonaggio.GOBLIN.getIstanza(1).isDaAffrontare(), "gli avversari della foresta no");
    }
}
