package com.threeamigos.foresta.mondo;

import com.threeamigos.foresta.gruppi.DatiDiProva;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.modellodati.ForestaMD;
import com.threeamigos.foresta.modellodati.LocazioneMD;
import com.threeamigos.foresta.modellodati.RegistroPersonaggiMD;
import com.threeamigos.foresta.modellodati.ResidenteMD;
import com.threeamigos.foresta.modellodati.StatisticheMD;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.statistiche.Statistiche;
import com.threeamigos.foresta.tipi.PersonaggioNoto;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Chi abita il mondo: i compagni da reclutare, i personaggi sparsi per le caselle, i residenti con il loro nome proprio, i campioni di carta,
 * forbici e sasso e il negoziante di una bottega.
 */
class RegistroPersonaggiTest {

    private final CoordinateMD qui = new CoordinateMD(3, 4);

    @BeforeEach
    void preparaDati() {
        DatiDiProva.nuovaPartita();
        RegistroPersonaggiMD dati = new RegistroPersonaggiMD();
        RegistroPersonaggi.setFornitoreDiDati(() -> dati);
        ForestaMD foresta = new ForestaMD();
        Foresta.setFornitoreDiDati(() -> foresta);
        Foresta.azzera(10, 8);
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 8; y++) {
                Foresta.impostaLocazione(new CoordinateMD(x, y), TipoLocazione.BOSCO);
            }
        }
        StatisticheMD statistiche = new StatisticheMD();
        Statistiche.setFornitoreDiDati(() -> statistiche);
    }

    @AfterEach
    void ripulisce() {
        RegistroPersonaggi.setFornitoreDiDati(() -> new RegistroPersonaggiMD());
    }

    @Test
    void senzaIDatiIlRegistroNonVa() {
        RegistroPersonaggi.setFornitoreDiDati(null);
        assertThrows(IllegalStateException.class, RegistroPersonaggi::getNumeroPersonaggiDisponibili);
    }

    @Test
    void iCompagniDaReclutareSiPescanoUnaVoltaSola() {
        assertEquals(0, RegistroPersonaggi.getNumeroPersonaggiDisponibili());
        assertNull(RegistroPersonaggi.getPersonaggioDisponibile());
        RegistroPersonaggi.reimposta();
        int disponibili = RegistroPersonaggi.getNumeroPersonaggiDisponibili();
        assertEquals(20, disponibili);
        List<String> nomi = new ArrayList<>();
        for (int i = 0; i < disponibili; i++) {
            Personaggio p = RegistroPersonaggi.getPersonaggioDisponibile();
            assertNotNull(p);
            assertFalse(nomi.contains(p.getNome()), p.getNome());
            nomi.add(p.getNome());
        }
        assertNull(RegistroPersonaggi.getPersonaggioDisponibile());
    }

    @Test
    void iPersonaggiSparsiPerLeCaselle() {
        assertNull(RegistroPersonaggi.getPersonaggioCasuale());
        assertNull(RegistroPersonaggi.getPersonaggioInLocazione(qui));
        Personaggio p = FabbricaPersonaggi.crea(TipoPersonaggio.MAGO, 3);
        RegistroPersonaggi.addPersonaggioInLocazione(p, qui);
        assertEquals(p, RegistroPersonaggi.getPersonaggioInLocazione(qui));
        RegistroPersonaggi.rimuoviPersonaggioInLocazione(qui);
        assertNull(RegistroPersonaggi.getPersonaggioInLocazione(qui));
        RegistroPersonaggi.addPersonaggioInLocazione(p, qui);
        Personaggio casuale = RegistroPersonaggi.getPersonaggioCasuale();
        assertEquals(p, casuale);
        assertNull(RegistroPersonaggi.getPersonaggioInLocazione(qui), "chi si pesca a caso esce dalla casella");
    }

    @Test
    void ilCompagnoReclutatoPortaIlLivelloDelMondoELaDotazione() {
        Personaggio guerriero = FabbricaPersonaggi.crea(TipoPersonaggio.GUERRIERO, 1);
        Personaggio pronto = RegistroPersonaggi.preparaCompagno(guerriero);
        assertSame(guerriero, pronto);
        assertFalse(pronto.getInventario().isEmpty());
    }

    @Test
    void iResidentiHannoUnNomeProprioEUnaProvenienza() {
        RegistroPersonaggi.creaIResidentiDellaCitta(TipoLocazione.CITTA_NYENA);
        ResidenteMD borgomastro = RegistroPersonaggi.getResidente(PersonaggioNoto.BORGOMASTRO, TipoLocazione.CITTA_NYENA);
        assertNotNull(borgomastro.getNome());
        assertEquals(TipoLocazione.CITTA_NYENA.getNomeDellaCitta(), borgomastro.getProvenienza());
        assertSame(borgomastro, RegistroPersonaggi.getResidente(PersonaggioNoto.BORGOMASTRO, TipoLocazione.CITTA_NYENA));
        assertTrue(RegistroPersonaggi.getNomeCompleto(borgomastro).startsWith(borgomastro.getNome() + " da"));
        RegistroPersonaggi.creaIlBardoESuaMoglie();
        ResidenteMD bardo = RegistroPersonaggi.getResidente(PersonaggioNoto.BARDO_DELLE_LOCANDE);
        assertNull(bardo.getProvenienza());
        assertEquals(bardo.getNome(), RegistroPersonaggi.getNomeCompleto(bardo));
        assertNotNull(RegistroPersonaggi.getResidente(PersonaggioNoto.LEVATRICE));
        // Negozio della Foresta: uno per città di partenza
        assertNotNull(RegistroPersonaggi.getResidente(PersonaggioNoto.ARMAIOLO_DELLA_FORESTA, TipoLocazione.CITTA_NYENA));
    }

    @Test
    void unResidenteSiRichiedeConLArgomentoGiusto() {
        assertThrows(IllegalArgumentException.class, () -> RegistroPersonaggi.getResidente(PersonaggioNoto.BORGOMASTRO));
        assertThrows(IllegalArgumentException.class, () -> RegistroPersonaggi.getResidente(PersonaggioNoto.LOCANDIERE));
        assertThrows(IllegalArgumentException.class, () -> RegistroPersonaggi.getResidente(PersonaggioNoto.BARDO_DELLE_LOCANDE, TipoLocazione.CITTA_NYENA));
        assertThrows(IllegalArgumentException.class, () -> RegistroPersonaggi.getResidente(PersonaggioNoto.LOCANDIERE, TipoLocazione.CITTA_NYENA));
    }

    @Test
    void unResidenteSiCostruisceSoloSeHaUnaClasse() {
        ResidenteMD senzaClasse = new ResidenteMD("Ignoto", null, null);
        assertThrows(IllegalStateException.class, () -> RegistroPersonaggi.costruisciPersonaggio(senzaClasse));
        Personaggio p = RegistroPersonaggi.costruisciPersonaggio(new ResidenteMD("Ugo", "Nyena", TipoPersonaggio.ARMAIOLO), 4);
        assertEquals("Ugo", p.getNome());
        assertEquals(4, p.getLivello());
        assertEquals(1, RegistroPersonaggi.costruisciPersonaggio(new ResidenteMD("Ugo", null, TipoPersonaggio.ARMAIOLO)).getLivello());
    }

    @Test
    void iCampioniDiCartaForbiciESasso() {
        assertTrue(RegistroPersonaggi.getCampioni().isEmpty());
        RegistroPersonaggi.registraCampione("Bradamante", TipoPersonaggio.GUERRIERA);
        RegistroPersonaggi.registraCampione("Orlando", TipoPersonaggio.GUERRIERO);
        RegistroPersonaggi.creaIlBardoESuaMoglie();
        assertEquals(2, RegistroPersonaggi.getCampioni().size());
        RegistroPersonaggi.segnaCampioneSconfitto("Bradamante");
        RegistroPersonaggi.segnaCampioneSconfitto("Nessuno");
        long sconfitti = RegistroPersonaggi.getCampioni().stream().filter(ResidenteMD::isSconfitto).count();
        assertEquals(1, sconfitti);
    }

    private void mettiIlGruppoIn(TipoLocazione tipo) {
        Foresta.impostaLocazione(qui, tipo);
        Foresta.setPosizioneDelGruppo(qui);
    }

    @Test
    void ilNomeDelPersonaggioDiCittaDipendeDaDoveSiTrovaIlGruppo() {
        // Nessuna locazione sotto il gruppo (la mappa è vuota)
        Foresta.azzera(10, 8);
        assertNull(RegistroPersonaggi.getNomeDelPersonaggioDiCitta(TipoPersonaggio.ARMAIOLO));
        // In una locazione che non è una città
        for (int xx = 0; xx < 10; xx++) {
            for (int yy = 0; yy < 8; yy++) {
                Foresta.impostaLocazione(new CoordinateMD(xx, yy), TipoLocazione.BOSCO);
            }
        }
        mettiIlGruppoIn(TipoLocazione.BOSCO);
        assertNull(RegistroPersonaggi.getNomeDelPersonaggioDiCitta(TipoPersonaggio.ARMAIOLO));
        // In una città: il nome di chi fa quel mestiere, o niente se in città quel mestiere non c'è
        mettiIlGruppoIn(TipoLocazione.CITTA_NYENA);
        String armaiolo = RegistroPersonaggi.getNomeDelPersonaggioDiCitta(TipoPersonaggio.ARMAIOLO);
        assertNotNull(armaiolo);
        assertEquals(armaiolo, RegistroPersonaggi.getNomeDelPersonaggioDiCitta(TipoPersonaggio.ARMAIOLO));
        assertNull(RegistroPersonaggi.getNomeDelPersonaggioDiCitta(TipoPersonaggio.GOBLIN));
        // In una bottega della Foresta: il negoziante scritto nella locazione
        mettiIlGruppoIn(TipoLocazione.ARMAIOLO);
        LocazioneMD locazione = Foresta.getLocazioneMD(qui);
        locazione.aggiungiProprieta(RegistroPersonaggi.NEGOZIANTE, "Mastro Gigi");
        assertEquals("Mastro Gigi", RegistroPersonaggi.getNomeDelPersonaggioDiCitta(TipoPersonaggio.ARMAIOLO));
        assertEquals("Mastro Gigi", RegistroPersonaggi.getNegoziante(locazione));
    }
}
