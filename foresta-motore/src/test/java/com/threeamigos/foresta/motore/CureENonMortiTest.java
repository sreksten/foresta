package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.canti.CantoCurativo;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.modellodati.tattiche.AzioneMD;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tattiche.ContestoTattico;
import com.threeamigos.foresta.tattiche.valutazione.Requisiti;
import com.threeamigos.foresta.tattiche.rose.Rose;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.tipi.tattiche.TipoAzione;
import com.threeamigos.foresta.tipi.tattiche.TipoRosa;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Un non morto entrato nel gruppo del giocatore (lo Scheletro della Mietitura) non si può curare: non è mai un
 * bersaglio possibile di pozioni di salute, canto curativo e Resurrezione. Un non morto avversario resta un
 * bersaglio come gli altri.
 */
class CureENonMortiTest {

    private Personaggio guerriero;
    private Personaggio scheletroAlleato;
    private Personaggio scheletroAvversario;
    private Personaggio goblin;

    @BeforeEach
    void prepara() {
        ModelloDati.setIstanza(new ModelloDati());
        GruppoGiocatore.azzeraIstanza();
        guerriero = FabbricaPersonaggi.crea(TipoPersonaggio.GUERRIERO, 1);
        scheletroAlleato = FabbricaPersonaggi.crea(TipoPersonaggio.SCHELETRO, 1);
        scheletroAvversario = FabbricaPersonaggi.crea(TipoPersonaggio.SCHELETRO, 1);
        goblin = FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 1);
        GruppoGiocatore.getIstanza().aggiungiPersonaggioSenzaNotificare(guerriero);
        GruppoGiocatore.getIstanza().aggiungiPersonaggioSenzaNotificare(scheletroAlleato);
    }

    @AfterEach
    void ripulisci() {
        GruppoGiocatore.azzeraIstanza();
    }

    private static void ferisci(Personaggio personaggio) {
        personaggio.getModelloDati().set(TipoAttributo.SALUTE, 1);
    }

    @Test
    void soloIlNonMortoNelGruppoDelGiocatoreNonSiPuoCurare() {
        assertTrue(guerriero.isCurabile());
        assertFalse(scheletroAlleato.isCurabile());
        assertTrue(scheletroAvversario.isCurabile());
        assertTrue(goblin.isCurabile());
    }

    @Test
    void ilCantoCurativoNonScegliIlNonMortoComePiuFerito() {
        ferisci(scheletroAlleato);
        guerriero.getModelloDati().set(TipoAttributo.SALUTE, guerriero.getSaluteMassima() - 1);

        assertEquals(guerriero, CantoCurativo.piuFerito(GruppoGiocatore.getIstanza()).orElse(null));
    }

    @Test
    void seNessunAltroEFeritoIlCantoCurativoNonHaBersaglio() {
        ferisci(scheletroAlleato);
        guerriero.getModelloDati().set(TipoAttributo.SALUTE, guerriero.getSaluteMassima());

        assertFalse(CantoCurativo.piuFerito(GruppoGiocatore.getIstanza()).isPresent());
    }

    @Test
    void leRoseDelleCureSaltanoIlNonMortoDelGruppo() {
        ContestoTattico contesto = new ContestoTattico(guerriero, true, Arrays.asList(guerriero, scheletroAlleato),
                Collections.singletonList(goblin), Collections.emptyList());
        assertEquals(Collections.singletonList(guerriero), Rose.di(TipoRosa.ALLEATI_E_OSPITI).candidati(contesto));
    }

    @Test
    void unGruppoAvversarioDiSoliNonMortiHaSempreUnPiuFerito() {
        ferisci(scheletroAvversario);
        ContestoTattico contesto = new ContestoTattico(goblin, false, Collections.singletonList(goblin),
                Arrays.asList(scheletroAvversario), Collections.emptyList());
        assertEquals(Collections.singletonList(scheletroAvversario), Rose.di(TipoRosa.AVVERSARI).candidati(contesto));
    }

    @Test
    void ICadutiDaRisuscitareNonComprendonoIlNonMortoDelGruppo() {
        scheletroAlleato.subSalute(1_000_000, goblin, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        guerriero.subSalute(1_000_000, goblin, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        ContestoTattico contesto = new ContestoTattico(guerriero, true, Arrays.asList(guerriero, scheletroAlleato),
                Collections.singletonList(goblin), Collections.emptyList());
        List<Personaggio> caduti = Rose.di(TipoRosa.ALLEATI_CADUTI).candidati(contesto);
        assertEquals(Collections.singletonList(guerriero), caduti);
    }

    @Test
    void leTatticheRifiutanoLeCureSulNonMorto() {
        ContestoTattico contesto = new ContestoTattico(guerriero, true, Arrays.asList(guerriero, scheletroAlleato),
                Collections.singletonList(goblin), Collections.emptyList());
        for (TipoAzione tipo : new TipoAzione[]{TipoAzione.CANTO_CURATIVO, TipoAzione.POZIONE_SALUTE,
                TipoAzione.POZIONE_SALUTE_GRANDE, TipoAzione.RESURREZIONE}) {
            String motivo = Requisiti.motivoDelNo(AzioneMD.di(tipo), guerriero, scheletroAlleato, contesto);
            assertNotNull(motivo, tipo.name());
            assertTrue(motivo.contains("non morto"), tipo + ": " + motivo);
            String motivoPerIlGuerriero = Requisiti.motivoDelNo(AzioneMD.di(tipo), guerriero, guerriero, contesto);
            assertTrue(motivoPerIlGuerriero == null || !motivoPerIlGuerriero.contains("non morto"), tipo + ": " + motivoPerIlGuerriero);
        }
    }
}
