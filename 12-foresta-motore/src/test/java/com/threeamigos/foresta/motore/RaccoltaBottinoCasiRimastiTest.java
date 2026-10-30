package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.artefatti.GeneratoreArtefatti;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.oggetti.Anello;
import com.threeamigos.foresta.oggetti.Cofano;
import com.threeamigos.foresta.oggetti.Bottino;
import com.threeamigos.foresta.oggetti.RaccoltaBottino;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoBottino;
import com.threeamigos.foresta.tipi.TipoLocazione;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I casi della raccolta del bottino che gli altri test non toccano: gli anelli magici (tutti e tre i tipi, annunciati una volta sola, con
 * più candidati fra cui scegliere), un oggetto di un tipo che la raccolta non conosce, i cofani dei castelli e un artefatto il cui nome ha
 * un inciso.
 */
class RaccoltaBottinoCasiRimastiTest {

    @AfterEach
    void ripulisce() {
        Dado.ripristina();
    }

    private static Anello anelloMagico(int tipo) {
        // Un anello è magico con un 6 sul d6; poi il tipo (da 1 a 3) e l'appartenenza (una delle sette)
        Dado.trucca(6, tipo, 1);
        Anello anello = new Anello();
        assertTrue(anello.isMagico());
        assertEquals(tipo, anello.getTipoDiMagia());
        return anello;
    }

    private static PartitaDiTest partitaConDueGuerrieri(long seme) {
        PartitaDiTest partita = PartitaDiTest.nuova(seme);
        partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
        partita.gruppo().aggiungiPersonaggio(new Guerriero("Bruno", 1));
        return partita;
    }

    @Test
    void unAnelloDelCoraggioSiAnnunciaESiMetteNelGruppoSeNessunoLoPuoUsare() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(3)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            Anello anello = anelloMagico(2);

            assertTrue(RaccoltaBottino.raccogli(anello, partita.gruppo(), Comando.GRUPPO));

            assertTrue(partita.eventi().tutti(NotificaTestoFrase.class).stream().anyMatch(e -> e.getMessaggio().contains("il coraggio")));
        }
    }

    @Test
    void unAnelloDelCarismaEDelValoreHannoIlLoroAnnuncio() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(4)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));

            assertTrue(RaccoltaBottino.raccogli(anelloMagico(3), partita.gruppo(), Comando.GRUPPO));
            assertTrue(RaccoltaBottino.raccogli(anelloMagico(1), partita.gruppo(), Comando.GRUPPO));

            assertTrue(partita.eventi().tutti(NotificaTestoFrase.class).stream().anyMatch(e -> e.getMessaggio().contains("il carisma")));
            assertTrue(partita.eventi().tutti(NotificaTestoFrase.class).stream().anyMatch(e -> e.getMessaggio().contains("il valore in combattimento")));
        }
    }

    @Test
    void conPiuCandidatiSiChiedeAChiDarloELAnnuncioNonSiRipete() {
        try (PartitaDiTest partita = partitaConDueGuerrieri(5)) {
            Anello anello = anelloMagico(1);
            GruppoGiocatore gruppo = partita.gruppo();

            assertFalse(RaccoltaBottino.raccogli(anello, gruppo, null), "due candidati: deve scegliere il giocatore");
            assertTrue(anello.isMagiaAnnunciata());
            long annunci = partita.eventi().tutti(NotificaTestoFrase.class).stream().filter(e -> e.getMessaggio().contains("Questo è un anello magico")).count();
            assertEquals(1, annunci);

            assertTrue(RaccoltaBottino.raccogli(anello, gruppo, Comando.GRUPPO));
            long annunciDopo = partita.eventi().tutti(NotificaTestoFrase.class).stream().filter(e -> e.getMessaggio().contains("Questo è un anello magico")).count();
            assertEquals(1, annunciDopo, "un anello rifiutato e ripreso non si annuncia due volte");
        }
    }

    @Test
    void ancheConIlComandoTimerSiLasciaScegliereAlGioco() {
        try (PartitaDiTest partita = partitaConDueGuerrieri(6)) {
            Artefatto spada = GeneratoreArtefatti.istanza().generaArtefatto(TipoArtefatto.SPADA, 1);
            assertFalse(RaccoltaBottino.raccogliArtefatto(partita.gruppo(), Comando.TIMER, spada));
        }
    }

    @Test
    void unOggettoDiUnTipoSconosciutoSiContaSoloPerIPunti() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(7)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            Bottino sasso = new Bottino() {
                @Override
                public int getQuantita() {
                    return 2;
                }

                @Override
                public String getAIS() {
                    return "un";
                }

                @Override
                public String getAIP() {
                    return "alcuni";
                }

                @Override
                public String getADS() {
                    return "il";
                }

                @Override
                public String getADP() {
                    return "i";
                }

                @Override
                public String getNomeSingolare() {
                    return "sasso";
                }

                @Override
                public String getNomePlurale() {
                    return "sassi";
                }

                @Override
                public TipoBottino getClasse() {
                    return TipoBottino.MONETA;
                }
            };
            assertTrue(RaccoltaBottino.raccogli(sasso, partita.gruppo(), Comando.GRUPPO));
        }
    }

    @Test
    void unArtefattoConUnIncisoNelNomeChiudeLInciso() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(8)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            // Una pergamena non la può equipaggiare nessuno: va nell'inventario del gruppo, e il testo lo dice con l'inciso chiuso
            Artefatto pergamena = GeneratoreArtefatti.istanza().generaArtefatto(TipoArtefatto.PERGAMENA, 1);
            pergamena.getModelloDati().setNome("Foglio, il rotolo");
            pergamena.getModelloDati().setDescrizione("che non serve a niente");
            assertTrue(RaccoltaBottino.raccogliArtefatto(partita.gruppo(), null, pergamena));
            assertTrue(partita.testi().stream().anyMatch(t -> t.contains(", ")), String.valueOf(partita.testi()));
        }
    }

    @Test
    void neiCastelliIlCofanoDaDaUnoAContro() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(9)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CASTELLO_IDRA));
            // Quattro cofani, uno per ogni esito: con gli esiti della locazione nei castelli (da 1 a 6) e fuori (da 0 a 7)
            assertTrue(RaccoltaBottino.raccogli(new Cofano(1), partita.gruppo(), null));
            assertFalse(partita.eventi().tutti(NotificaTestoFrase.class).isEmpty());
        }
    }
}
