package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.artefatti.GeneratoreArtefattiTabelle;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.statistiche.Statistiche;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.testi.Articoli;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoBottino;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Il bottino che porta un artefatto: la forma (una spada, uno scudo...) decide classe, nome e articoli; l'artefatto già fatto
 * (quello di un tempio) si rivela; prenderlo dà i punti del suo valore e fa salire un poco di livello, tranne gli ingredienti
 * magici.
 */
class BottinoArtefattoTest {

    private GruppoGiocatore gruppo;
    private final GeneratoreArtefattiTabelle generatore = new GeneratoreArtefattiTabelle(new Random(1));

    @BeforeEach
    void nuovoGruppo() {
        // Il livello del mondo sale ascoltando i punti esperienza del gruppo: le statistiche devono essere iscritte
        BusEventi.azzera();
        Statistiche.registrati();
        ModelloDati.installa();
        ModelloDati.setIstanza(new ModelloDati());
        gruppo = new GruppoGiocatore();
        gruppo.aggiungiPersonaggioSenzaNotificare(new Guerriero("Pippo", 1));
    }

    @Test
    void ognunaDelleFormeHaIlSuoArtefattoELaSuaClasse() {
        for (FormaDelBottino forma : FormaDelBottino.values()) {
            BottinoArtefatto bottino = BottinoArtefatto.nuovo(forma);

            assertEquals(forma.getTipoBottino(), bottino.getClasse(), forma.name());
            assertEquals(forma.getTipoArtefatto(), bottino.getArtefatto().orElseThrow(AssertionError::new).getTipo(), forma.name());
            assertEquals(forma.name(), forma.getTipoBottino().name(), "la forma ha lo stesso nome del tipo di bottino");
            assertFalse(bottino.isDaRivelare(), "un artefatto per terra non si rivela");
            assertEquals(1, bottino.getQuantita());
            assertEquals(forma.getAIS(), bottino.getAIS());
            assertEquals(forma.getAIP(), bottino.getAIP());
            assertEquals(forma.getADS(), bottino.getADS());
            assertEquals(forma.getADP(), bottino.getADP());
            assertEquals(forma.getNomeSingolare(), bottino.getNomeSingolare());
            assertEquals(forma.getNomePlurale(), bottino.getNomePlurale());
        }
    }

    @Test
    void unaSpadaPerTerraSiChiamaSpada() {
        BottinoArtefatto spada = BottinoArtefatto.nuovo(FormaDelBottino.SPADA);
        assertEquals(Articoli.UNA, spada.getAIS());
        assertEquals("spada", spada.getNomeSingolare());
        assertEquals("spade", spada.getNomePlurale());
        assertEquals(TipoBottino.SPADA, spada.getClasse());
    }

    @Test
    void unArtefattoGiaFattoDiUnaFormaHaLaClasseDellaFormaEVaRivelato() {
        Artefatto elmo = generatore.generaArtefatto(TipoArtefatto.ELMO, 1);

        BottinoArtefatto bottino = BottinoArtefatto.giaFatto(elmo);

        assertEquals(TipoBottino.ELMO, bottino.getClasse(), "ha lo sprite dell'elmo");
        assertTrue(bottino.isDaRivelare());
        assertSame(elmo, bottino.getArtefatto().orElseThrow(AssertionError::new));
    }

    @Test
    void unArtefattoGiaFattoSenzaFormaEUnGenericoArtefatto() {
        Artefatto ingrediente = generatore.generaIngrediente(1);

        BottinoArtefatto bottino = BottinoArtefatto.giaFatto(ingrediente);

        assertEquals(TipoBottino.ARTEFATTO, bottino.getClasse());
        assertEquals("artefatto", bottino.getNomeSingolare());
        assertEquals("artefatti", bottino.getNomePlurale());
        assertEquals(Articoli.UN, bottino.getAIS());
        assertEquals(Articoli.ALCUNI, bottino.getAIP());
        assertEquals(Articoli.L_APOSTROFO, bottino.getADS());
        assertEquals(Articoli.GLI, bottino.getADP());
        assertTrue(bottino.isDaRivelare());
    }

    @Test
    void ilRestoDelBottinoNonSiRivela() {
        assertFalse(new Moneta().isDaRivelare());
        assertFalse(new Cofano(1).isDaRivelare());
    }

    @Test
    void laFormaSiTrovaDalTipoDiArtefattoEDalTipoDiBottino() {
        assertEquals(FormaDelBottino.SCUDO, FormaDelBottino.di(TipoArtefatto.SCUDO).orElseThrow(AssertionError::new));
        assertEquals(FormaDelBottino.SCUDO, FormaDelBottino.di(TipoBottino.SCUDO).orElseThrow(AssertionError::new));
        assertFalse(FormaDelBottino.di(TipoArtefatto.PERGAMENA).isPresent(), "un ingrediente non ha una forma");
        assertFalse(FormaDelBottino.di(TipoBottino.MONETA).isPresent(), "le monete non hanno una forma");
        assertFalse(FormaDelBottino.di(TipoBottino.ARTEFATTO).isPresent());
    }

    @Test
    void prendereUnArtefattoDaiPuntiDelSuoValoreEFaSalireDiLivello() {
        int puntiPrima = Statistiche.getPunti();
        int esperienzaPrima = Statistiche.getPuntiEsperienza();

        assertTrue(RaccoltaBottino.raccogli(BottinoArtefatto.giaFatto(generatore.generaArtefatto(TipoArtefatto.SPADA, 1)), gruppo, Comando.GRUPPO));

        assertEquals(puntiPrima + TipoBottino.SPADA.getValore(), Statistiche.getPunti(), "anche quello del tempio dà i punti");
        assertTrue(Statistiche.getPuntiEsperienza() > esperienzaPrima, "un artefatto fa salire un poco di livello");
    }

    @Test
    void ancheUnaSpadaPerTerraDaPuntiEEsperienza() {
        int puntiPrima = Statistiche.getPunti();
        int esperienzaPrima = Statistiche.getPuntiEsperienza();

        assertTrue(RaccoltaBottino.raccogli(BottinoArtefatto.nuovo(FormaDelBottino.SPADA), gruppo, Comando.GRUPPO));

        assertEquals(puntiPrima + TipoBottino.SPADA.getValore(), Statistiche.getPunti());
        assertTrue(Statistiche.getPuntiEsperienza() > esperienzaPrima);
    }

    @Test
    void unIngredienteMagicoDaPuntiMaNonFaSalireDiLivello() {
        int puntiPrima = Statistiche.getPunti();
        int esperienzaPrima = Statistiche.getPuntiEsperienza();

        assertTrue(RaccoltaBottino.raccogli(BottinoArtefatto.giaFatto(generatore.generaIngrediente(1)), gruppo, Comando.GRUPPO));

        assertEquals(puntiPrima + TipoBottino.ARTEFATTO.getValore(), Statistiche.getPunti());
        assertEquals(esperienzaPrima, Statistiche.getPuntiEsperienza(), "gli ingredienti non contano come artefatti");
    }
}
