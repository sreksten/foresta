package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.artefatti.GeneratoreArtefattiTabelle;
import com.threeamigos.foresta.gruppi.DatiDiProva;
import com.threeamigos.foresta.gruppi.GruppoAvversario;
import com.threeamigos.foresta.modellodati.ModificatoreAttributoMD;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoModificatore;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Come un personaggio si presenta a parole: il nome (con articolo, preposizione, iniziale maiuscola e il numero che lo distingue) e la
 * descrizione della scheda.
 */
class NomeEDescrizioneTest {

    @BeforeEach
    void prepara() {
        DatiDiProva.nuovaPartita();
        GruppoAvversario.getIstanza().reimposta();
    }

    @Test
    void ilNomeProprioNonVuoleArticolo() {
        Personaggio p = new Guerriero("Arsenio", 3);
        assertEquals("Arsenio", p.getNome());
        assertEquals("Arsenio", p.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE));
        assertEquals("di Arsenio", p.getNome(Personaggio.OpzioniGetNome.INCLUDI_PREPOSIZIONE_ARTICOLATA));
        assertEquals("Arsenio", p.getNomeProprio().get());
    }

    @Test
    void ilNomeDellaSpecieHaGliArticoliCheSiChiedono() {
        Personaggio goblin = FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 1);
        assertFalse(goblin.getNomeProprio().isPresent());
        assertEquals("Goblin", goblin.getNome());
        assertEquals("il Goblin", goblin.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE));
        assertEquals("un Goblin", goblin.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_INDETERMINATIVO_SINGOLARE));
        assertEquals("del Goblin", goblin.getNome(Personaggio.OpzioniGetNome.INCLUDI_PREPOSIZIONE_ARTICOLATA));
        assertEquals("Il Goblin", goblin.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
                Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA));
        assertEquals("o", goblin.getLetteraFinaleAttributo());
        assertEquals("a", FabbricaPersonaggi.crea(TipoPersonaggio.STREGA, 1).getLetteraFinaleAttributo());
    }

    @Test
    void unGiocanteSenzaNomeNonSiPuoNominare() {
        Personaggio senzaNome = FabbricaPersonaggi.crea(TipoPersonaggio.GUERRIERO, 1);
        senzaNome.getModelloDati().setPng(false);
        assertThrows(IllegalStateException.class, senzaNome::getNome);
    }

    @Test
    void ilNumeroDistingueGliAvversariDellaStessaSpecieSoloNelLoroGruppo() {
        GruppoAvversario gruppo = GruppoAvversario.getIstanza();
        Personaggio primo = FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 1);
        Personaggio fuori = FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 1);
        Personaggio secondo = FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 1);
        assertEquals("Goblin", primo.getNome());
        gruppo.aggiungiPersonaggio(primo);
        assertEquals("Goblin", primo.getNome(), "uno solo della specie: niente numero");
        gruppo.aggiungiPersonaggio(secondo);
        assertEquals("Goblin 1", primo.getNome());
        assertEquals("Goblin 2", secondo.getNome());
        // Chi non è nel gruppo non ha numero, e chi ha ordinale zero nemmeno
        assertEquals("Goblin", fuori.getNome());
        fuori.setOrdinale(3);
        assertEquals("Goblin", fuori.getNome(), "ha un numero ma non è nel gruppo");
        primo.setOrdinale(0);
        assertEquals("Goblin", primo.getNome());
    }

    private static Personaggio conSalute(int salute) {
        Personaggio p = new Guerriero("Arsenio", 3);
        p.getModelloDati().set(TipoAttributo.SALUTE, salute);
        return p;
    }

    @Test
    void laDescrizioneRaccontaLaSaluteACaratteri() {
        assertTrue(conSalute(10).getDescrizione().startsWith("Arsenio è molto debole, "), conSalute(10).getDescrizione());
        assertTrue(conSalute(30).getDescrizione().contains("è debole, "));
        assertTrue(conSalute(60).getDescrizione().contains("non è molto in salute, "));
        assertTrue(conSalute(90).getDescrizione().contains("è in salute, "));
        assertTrue(conSalute(150).getDescrizione().contains("è assolutamente in salute, "));
        assertTrue(conSalute(300).getDescrizione().contains("è davvero in salute, "));
        assertTrue(conSalute(500).getDescrizione().contains("è sano come un pesce, "));
        Personaggio strega = FabbricaPersonaggi.crea(TipoPersonaggio.STREGA, 1);
        strega.getModelloDati().set(TipoAttributo.SALUTE, 500);
        assertTrue(strega.getDescrizione().contains("è sana come un pesce, "), strega.getDescrizione());
    }

    private static Personaggio conFissi(TipoAttributo attributo, int valore) {
        Personaggio p = new Guerriero("Arsenio", 3);
        p.addModificatore(new ModificatoreAttributoMD(attributo, TipoModificatore.QUANTITA_ASSOLUTA, valore));
        return p;
    }

    @Test
    void laDescrizioneRaccontaCoraggioValoreECarisma() {
        assertTrue(conFissi(TipoAttributo.CORAGGIO, 10).getDescrizione().contains("non ha molto coraggio"));
        assertTrue(conFissi(TipoAttributo.CORAGGIO, 40).getDescrizione().contains("ha coraggio, "));
        assertTrue(conFissi(TipoAttributo.CORAGGIO, 90).getDescrizione().contains("ha coraggio da vendere"));
        assertTrue(conFissi(TipoAttributo.VALORE, 10).getDescrizione().contains("non e' che se la cavi egregiamente"));
        assertTrue(conFissi(TipoAttributo.VALORE, 40).getDescrizione().contains("se la cava bene"));
        assertTrue(conFissi(TipoAttributo.VALORE, 90).getDescrizione().contains("puo' arrecare gravi danni"));
        for (int[] caso : new int[][] {{1, 0}, {4, 1}, {9, 2}}) {
            Personaggio p = new Guerriero("Arsenio", 3);
            p.getModelloDati().set(TipoAttributo.CARISMA, caso[0]);
            String[] attese = {"non ha molto carisma", "ha abbastanza carisma", "ha molto carisma"};
            assertTrue(p.getDescrizione().contains(attese[caso[1]]), p.getDescrizione());
        }
    }

    @Test
    void laDescrizioneElencaGliArtefattiEIlPronome() {
        Personaggio goblin = FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 1);
        assertTrue(goblin.getDescrizione().startsWith("Il Goblin "), goblin.getDescrizione());
        assertFalse(goblin.getDescrizione().contains("Esso"));
        goblin.addArtefatto(new GeneratoreArtefattiTabelle(new Random(7)).generaArtefatto(TipoArtefatto.SPADA, 1));
        assertTrue(goblin.getDescrizione().contains("Esso") || goblin.getDescrizione().contains("esso"), goblin.getDescrizione());
    }
}
