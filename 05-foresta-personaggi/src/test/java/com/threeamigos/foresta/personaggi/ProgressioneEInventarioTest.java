package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.artefatti.GeneratoreArtefatti;
import com.threeamigos.foresta.artefatti.GeneratoreArtefattiTabelle;
import com.threeamigos.foresta.artefatti.SpadaNormalmenteNascosta;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoSpadaNascostaImpugnata;
import com.threeamigos.foresta.eventi.notifiche.NotificaAumentoLivelloPersonaggio;
import com.threeamigos.foresta.eventi.notifiche.NotificaConsumoPuntoAbilitaPersonaggio;
import com.threeamigos.foresta.gruppi.DatiDiProva;
import com.threeamigos.foresta.modellodati.ModificatoreAttributoMD;
import com.threeamigos.foresta.tipi.CostantiPersonaggi;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoModificatore;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * L'esperienza e i livelli (la curva di GestoreProgressione e Progressione), il peso che si porta, l'equipaggiamento e la dotazione iniziale.
 */
class ProgressioneEInventarioTest {

    private final List<Object> eventi = new ArrayList<>();
    private final List<Runnable> iscrizioni = new ArrayList<>();

    @BeforeEach
    void prepara() {
        BusEventi.impostaConsegna(Runnable::run);
        DatiDiProva.nuovaPartita();
        iscrizioni.add(BusEventi.iscriviti(NotificaAumentoLivelloPersonaggio.class, eventi::add));
        iscrizioni.add(BusEventi.iscriviti(NotificaConsumoPuntoAbilitaPersonaggio.class, eventi::add));
        iscrizioni.add(BusEventi.iscriviti(InternoSpadaNascostaImpugnata.class, eventi::add));
    }

    @AfterEach
    void ripulisce() {
        iscrizioni.forEach(Runnable::run);
        BusEventi.impostaConsegna(null);
    }

    private static Artefatto spada() {
        return new GeneratoreArtefattiTabelle(new Random(7)).generaArtefatto(TipoArtefatto.SPADA, 1);
    }

    // --- La curva

    @Test
    void laCurvaDellEsperienzaCrescecolQuadratoDelLivelloMenoUno() {
        assertEquals(0, GestoreProgressione.getXpNecessariPerLivello(1));
        assertEquals(0, GestoreProgressione.getXpNecessariPerLivello(-3));
        assertEquals(100, GestoreProgressione.getXpNecessariPerLivello(2));
        assertEquals(400, GestoreProgressione.getXpNecessariPerLivello(3));
        assertEquals(300, GestoreProgressione.getXpRichiestiPerProssimoLivello(2));
        assertEquals(400, GestoreProgressione.getXpPerProssimoLivello(2));
        assertEquals(1, GestoreProgressione.calcolaLivelloDaXp(0));
        assertEquals(1, GestoreProgressione.calcolaLivelloDaXp(-5));
        assertEquals(3, GestoreProgressione.calcolaLivelloDaXp(400));
        assertEquals(CostantiPersonaggi.MASSIMO_LIVELLO, GestoreProgressione.calcolaLivelloDaXp(Integer.MAX_VALUE));
        assertNotNull(new GestoreProgressione());
    }

    @Test
    void perSalireDiUnLivelloServonoGliXpCheMancanoPiuQuelliDelProssimo() {
        int massimo = CostantiPersonaggi.MASSIMO_LIVELLO;
        assertEquals(0, GestoreProgressione.getXpPerAumentoDiLivello(massimo, 0));
        // Al penultimo basta arrivare al massimo
        int xpPenultimo = GestoreProgressione.getXpNecessariPerLivello(massimo - 1);
        assertEquals(GestoreProgressione.getXpPerProssimoLivello(massimo - 1) - xpPenultimo,
                GestoreProgressione.getXpPerAumentoDiLivello(massimo - 1, xpPenultimo));
        // In mezzo: dopo l'aumento si è al livello successivo e non oltre
        for (int livello : new int[] {1, 2, 5, 10}) {
            int xp = GestoreProgressione.getXpNecessariPerLivello(livello) + 7;
            int dopo = xp + GestoreProgressione.getXpPerAumentoDiLivello(livello, xp);
            assertEquals(livello + 1, GestoreProgressione.calcolaLivelloDaXp(dopo), "livello " + livello);
        }
    }

    // --- Progressione del personaggio

    @Test
    void ipngNonGuadagnanoEsperienza() {
        Personaggio goblin = FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 1);
        goblin.addPuntiEsperienza(5000);
        assertEquals(0, goblin.getEsperienza());
        assertTrue(eventi.isEmpty());
    }

    @Test
    void saleDiLivelloEDaUnPuntoAbilitaPerOgniLivello() {
        Personaggio p = new Guerriero("Arsenio", 1);
        p.getModelloDati().setPng(false);
        p.addPuntiEsperienza(50);
        assertEquals(1, p.getLivello());
        assertTrue(eventi.isEmpty());
        p.addPuntiEsperienza(GestoreProgressione.getXpNecessariPerLivello(4));
        assertEquals(4, p.getLivello());
        assertEquals(3, p.getPuntiAbilitaDisponibili());
        assertEquals(1, eventi.size());
        assertFalse(p.isAlLivelloMassimo());
        assertEquals(GestoreProgressione.getXpPerProssimoLivello(4), p.getEsperienzaPerProssimoLivello());
    }

    @Test
    void ipuntiAbilitaSiSpendonoSoloSuiPrimariECheCiSono() {
        Personaggio p = new Guerriero("Arsenio", 1);
        int forza = p.get(TipoAttributo.FORZA);
        p.spendiPuntoAbilita(TipoAttributo.FORZA);
        assertEquals(forza, p.get(TipoAttributo.FORZA), "senza punti non si spende");
        p.getModelloDati().setPuntiAbilitaDisponibili(2);
        p.spendiPuntoAbilita(TipoAttributo.FORZA);
        assertEquals(forza + 1, p.get(TipoAttributo.FORZA));
        assertEquals(1, p.getPuntiAbilitaDisponibili());
        assertEquals(1, eventi.size());
        p.spendiPuntoAbilita(TipoAttributo.CRITICO);
        assertEquals(1, p.getPuntiAbilitaDisponibili());
    }

    @Test
    void chiSiUnisceAlGruppoPortaIlLivelloDelMondoConLaStessaFrazioneDiSalute() {
        Personaggio p = new Guerriero("Arsenio", 1);
        p.subSalute(p.getSaluteMassima() / 2, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        double frazione = (double) p.getSalute() / p.getSaluteMassima();
        p.impostaLivelloDopoReclutamento(8);
        assertEquals(8, p.getLivello());
        assertTrue(p.getEsperienza() >= GestoreProgressione.getXpNecessariPerLivello(8));
        assertEquals(7, p.getPuntiAbilitaDisponibili());
        assertEquals(frazione, (double) p.getSalute() / p.getSaluteMassima(), 0.02);
        // Livello uguale o più basso: niente
        p.impostaLivelloDopoReclutamento(3);
        p.impostaLivelloDopoReclutamento(8);
        assertEquals(8, p.getLivello());
        // Mai oltre il massimo
        p.impostaLivelloDopoReclutamento(CostantiPersonaggi.MASSIMO_LIVELLO + 50);
        assertEquals(CostantiPersonaggi.MASSIMO_LIVELLO, p.getLivello());
        assertTrue(p.isAlLivelloMassimo());
    }

    @Test
    void chiNonHaMagiaLaConservaIntera() {
        Personaggio goblin = FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 1);
        goblin.impostaLivelloDopoReclutamento(6);
        assertEquals(6, goblin.getLivello());
        Personaggio mago = new Mago("Merlino", 1);
        double frazione = (double) mago.getMagia() / mago.getMagiaMassima();
        mago.impostaLivelloDopoReclutamento(6);
        assertEquals(frazione, (double) mago.getMagia() / mago.getMagiaMassima(), 0.02);
    }

    // --- Carico ed equipaggiamento

    @Test
    void ilCaricoESommaDeiPesiESiPuoPrendereFinoAlMassimo() {
        PersonaggioBase p = new Guerriero("Arsenio", 5);
        double vuoto = p.getCarico();
        Artefatto spada = spada();
        p.addArtefatto(spada);
        assertEquals(vuoto + spada.getPeso(), p.getCarico(), 1e-9);
        assertTrue(p.getCaricoMassimo() > 0);
        assertTrue(p.puoPrendere(spada()));
        assertTrue(p.puoPrendere(1.0d));
        assertFalse(p.puoPrendere(1000000.0d));
        assertEquals(1, p.getInventario().size());
        assertTrue(p.getDescrizioniSetCompleti().isEmpty());
    }

    @Test
    void sapereSeUnArtefattoSiPuoEquipaggiare() {
        Personaggio mago = new Mago("Merlino", 5);
        Artefatto spada = spada();
        // Il Mago non impugna una spada: la regola dice il perché
        assertTrue(mago.puoEquipaggiare(spada).isPresent());
        assertFalse(mago.isEquipaggiabile(spada));
        Personaggio guerriero = new Guerriero("Arsenio", 5);
        assertFalse(guerriero.puoEquipaggiare(spada).isPresent());
        assertTrue(guerriero.isEquipaggiabile(spada));
    }

    @Test
    void impugnareLaSpadaNormalmenteNascostaSiSa() {
        Personaggio guerriero = new Guerriero("Arsenio", 5);
        guerriero.addArtefatto(SpadaNormalmenteNascosta.costruisci(new GeneratoreArtefattiTabelle(new Random(3)), 1));
        assertEquals(1, eventi.size());
        assertTrue(eventi.get(0) instanceof InternoSpadaNascostaImpugnata);
    }

    @Test
    void togliendoUnArtefattoCheDavaSaluteESaluteMassimaOMagiaLaSaluteTornaEntroIlMassimo() {
        Personaggio mago = new Mago("Merlino", 5);
        Artefatto bastone = new GeneratoreArtefattiTabelle(new Random(7)).generaArtefatto(TipoArtefatto.BASTONE_MAGICO, 1);
        bastone.getModelloDati().addModificatore(new ModificatoreAttributoMD(TipoAttributo.SALUTE, TipoModificatore.VARIAZIONE_FISSA, 40, "bonus"));
        bastone.getModelloDati().addModificatore(new ModificatoreAttributoMD(TipoAttributo.MAGIA, TipoModificatore.VARIAZIONE_FISSA, 40, "bonus"));
        int saluteMassima = mago.getSaluteMassima();
        int magiaMassima = mago.getMagiaMassima();
        mago.addArtefatto(bastone);
        mago.addSalute(100000);
        mago.addMagia(100000);
        assertEquals(saluteMassima + 40, mago.getSalute());
        assertEquals(magiaMassima + 40, mago.getMagia());
        mago.removeArtefatto(bastone);
        assertEquals(saluteMassima, mago.getSalute());
        assertEquals(magiaMassima, mago.getMagia());
        assertNull(bastone.getModelloDati().getSlotEquipaggiamento());
        assertTrue(mago.getInventario().isEmpty());
        // Se non superano il massimo non si toccano
        mago.removeArtefatto(bastone);
        assertEquals(saluteMassima, mago.getSalute());
    }

    // --- Dotazione iniziale

    @Test
    void ognunaDelleClassiGiocantiHaLaSuaDotazioneEGliAltriNiente() {
        for (TipoPersonaggio classe : TipoPersonaggio.values()) {
            boolean giocante = !EquipaggiamentoIniziale.perClasse(classe).isEmpty();
            if (giocante) {
                assertEquals(2, EquipaggiamentoIniziale.perClasse(classe).size(), classe.name());
            }
        }
        assertTrue(EquipaggiamentoIniziale.perClasse(TipoPersonaggio.GOBLIN).isEmpty());
        for (int i = 0; i < 50; i++) {
            int livello = EquipaggiamentoIniziale.livelloCasuale(5);
            assertTrue(livello >= 3 && livello <= 5, "livello " + livello);
        }
        assertEquals(1, EquipaggiamentoIniziale.livelloCasuale(1));
    }

    @Test
    void ladotazioneSiDaSoloAChiPuoUsarla() {
        for (TipoPersonaggio classe : new TipoPersonaggio[] {TipoPersonaggio.GUERRIERO, TipoPersonaggio.MAGO, TipoPersonaggio.CHIERICO,
                TipoPersonaggio.DRUIDO, TipoPersonaggio.LADRO}) {
            Personaggio p = FabbricaPersonaggi.crea(classe, 3);
            EquipaggiamentoIniziale.equipaggia(p, 4);
            assertFalse(p.getInventario().isEmpty(), classe.name());
        }
        Personaggio goblin = FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 3);
        EquipaggiamentoIniziale.equipaggia(goblin, 4);
        assertTrue(goblin.getInventario().isEmpty());
        assertNotNull(GeneratoreArtefatti.istanza());
    }
}
