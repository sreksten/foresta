package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.modellodati.ArtefattoMD;
import com.threeamigos.foresta.motore.modellodati.ModelloDati;
import com.threeamigos.foresta.motore.modellodati.SlotArtefatto;
import com.threeamigos.foresta.motore.modellodati.TipoArtefatto;
import com.threeamigos.foresta.motore.modellodati.TipoModificatore;
import com.threeamigos.foresta.motore.modellodati.TipoAttributo;
import com.threeamigos.foresta.motore.modellodati.ModificatoreAttributo;
import com.threeamigos.foresta.motore.ArmaNaturale;
import com.threeamigos.foresta.oggetti.Artefatto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PersonaggioEquipaggiamentoTest {

    @BeforeEach
    void nuovoModello() {
        ModelloDati.setIstanza(new ModelloDati());
    }

    // --- Motivi di rifiuto

    @Test
    void unArtefattoLeggeroEDelSuoLivelloSiPuoPrendere() {
        Personaggio guerriero = new Guerriero("Pippo", 3);
        assertEquals(Optional.empty(), guerriero.puoEquipaggiare(artefatto(TipoArtefatto.SPADA, 3)));
    }

    @Test
    void troppoCarico() {
        Personaggio guerriero = new Guerriero("Pippo", 1);
        Artefatto pesante = artefatto(TipoArtefatto.SPADA, 1);
        pesante.getModelloDati().setPeso(100_000);
        assertRifiuto(MotivoRifiutoEquipaggiamento.TROPPO_CARICO, guerriero, pesante);
    }

    @Test
    void slotOccupato() {
        Personaggio guerriero = new Guerriero("Pippo", 1);
        guerriero.addArtefatto(artefatto(TipoArtefatto.ELMO, 1));
        guerriero.addArtefatto(artefatto(TipoArtefatto.ARMATURA, 1));
        guerriero.addArtefatto(artefatto(TipoArtefatto.SCUDO, 1));
        assertRifiuto(MotivoRifiutoEquipaggiamento.SLOT_OCCUPATO, guerriero, artefatto(TipoArtefatto.ELMO, 1));
        assertRifiuto(MotivoRifiutoEquipaggiamento.SLOT_OCCUPATO, guerriero, artefatto(TipoArtefatto.VESTE, 1));
        assertRifiuto(MotivoRifiutoEquipaggiamento.SLOT_OCCUPATO, guerriero, artefatto(TipoArtefatto.SCUDO, 1));
    }

    // --- Equipaggiamento secondo la classe

    @Test
    void ogniClasseUsaLeSueArmi() {
        assertRifiuto(MotivoRifiutoEquipaggiamento.NON_ADATTO_ALLA_CLASSE, new Mago("Merlino", 1), artefatto(TipoArtefatto.SPADONE, 1));
        assertRifiuto(MotivoRifiutoEquipaggiamento.NON_ADATTO_ALLA_CLASSE, new Mago("Merlino", 1), artefatto(TipoArtefatto.SPADA, 1));
        assertRifiuto(MotivoRifiutoEquipaggiamento.NON_ADATTO_ALLA_CLASSE, new Bardo("Pippo", 1), artefatto(TipoArtefatto.SPADONE, 1));
        assertRifiuto(MotivoRifiutoEquipaggiamento.NON_ADATTO_ALLA_CLASSE, new Ladro("Pippo", 1), artefatto(TipoArtefatto.SPADONE, 1));
        assertRifiuto(MotivoRifiutoEquipaggiamento.NON_ADATTO_ALLA_CLASSE, new Ladro("Pippo", 1), artefatto(TipoArtefatto.SCUDO, 1));
        assertRifiuto(MotivoRifiutoEquipaggiamento.NON_ADATTO_ALLA_CLASSE, new Guerriero("Pippo", 1), artefatto(TipoArtefatto.LIBRO_MAGICO, 1));
        assertEquals(Optional.empty(), new Guerriero("Pippo", 1).puoEquipaggiare(artefatto(TipoArtefatto.SPADONE, 1)));
        assertEquals(Optional.empty(), new Elfa("Pippa", 1).puoEquipaggiare(artefatto(TipoArtefatto.LANCIA, 1)));
    }

    @Test
    void ilMagoUsaBastoneELibro() {
        Personaggio maga = new Maga("Morgana", 1);
        maga.addArtefatto(artefatto(TipoArtefatto.BASTONE_MAGICO, 1));
        assertEquals(Optional.empty(), maga.puoEquipaggiare(artefatto(TipoArtefatto.LIBRO_MAGICO, 1)));
        assertEquals(Optional.empty(), maga.puoEquipaggiare(artefatto(TipoArtefatto.VESTE, 1)));
    }

    @Test
    void ilBardoPortaSpadaEScudo() {
        Personaggio bardo = new Bardo("Pippo", 1);
        bardo.addArtefatto(artefatto(TipoArtefatto.SPADA, 1));
        assertEquals(Optional.empty(), bardo.puoEquipaggiare(artefatto(TipoArtefatto.SCUDO, 1)));
    }

    @Test
    void lArmaturaChiedeForza() {
        assertRifiuto(MotivoRifiutoEquipaggiamento.FORZA_INSUFFICIENTE, new Ladro("Pippo", 1), artefatto(TipoArtefatto.ARMATURA, 1));
        assertRifiuto(MotivoRifiutoEquipaggiamento.FORZA_INSUFFICIENTE, new Mago("Merlino", 1), artefatto(TipoArtefatto.ARMATURA, 1));
        assertEquals(Optional.empty(), new Guerriero("Pippo", 1).puoEquipaggiare(artefatto(TipoArtefatto.ARMATURA, 1)));
        // Con un anello della forza anche il ladro la indossa
        Ladro forzuto = new Ladro("Pippo", 1);
        forzuto.addModificatore(new ModificatoreAttributo(TipoAttributo.FORZA, TipoModificatore.AUMENTO_FISSO, 10));
        assertEquals(Optional.empty(), forzuto.puoEquipaggiare(artefatto(TipoArtefatto.ARMATURA, 1)));
    }

    @Test
    void gliAccessoriNonHannoLimite() {
        Personaggio guerriero = new Guerriero("Pippo", 1);
        guerriero.addArtefatto(artefatto(TipoArtefatto.ANELLO, 1));
        guerriero.addArtefatto(artefatto(TipoArtefatto.ANELLO, 1));
        guerriero.addArtefatto(artefatto(TipoArtefatto.TALISMANO, 1));
        assertEquals(Optional.empty(), guerriero.puoEquipaggiare(artefatto(TipoArtefatto.ANELLO, 1)));
    }

    @Test
    void lePergameneRestanoNelGruppo() {
        Personaggio guerriero = new Guerriero("Pippo", 5);
        assertRifiuto(MotivoRifiutoEquipaggiamento.PERGAMENA, guerriero, artefatto(TipoArtefatto.INCANTAMENTO, 1));
    }

    @Test
    void livelloTroppoAlto() {
        Personaggio guerriero = new Guerriero("Pippo", 2);
        assertRifiuto(MotivoRifiutoEquipaggiamento.LIVELLO_TROPPO_ALTO, guerriero, artefatto(TipoArtefatto.SPADA, 3));
        assertRifiuto(MotivoRifiutoEquipaggiamento.LIVELLO_TROPPO_ALTO, guerriero, artefatto(TipoArtefatto.ANELLO, 3));
    }

    @Test
    void secondaArmaNonConsentitaAlGuerriero() {
        Personaggio guerriero = new Guerriero("Pippo", 1);
        guerriero.addArtefatto(artefatto(TipoArtefatto.SPADA, 1));
        assertRifiuto(MotivoRifiutoEquipaggiamento.SECONDA_ARMA_NON_CONSENTITA, guerriero, artefatto(TipoArtefatto.SPADA, 1));
    }

    @Test
    void laLanciaNonVaNellaManoSecondaria() {
        // L'Elfo sa usare la lancia e combatte con due armi, ma la lancia resta nella mano principale
        Personaggio elfo = new Elfo("Pippo", 1);
        elfo.addArtefatto(artefatto(TipoArtefatto.SPADA, 1));
        assertRifiuto(MotivoRifiutoEquipaggiamento.SLOT_OCCUPATO, elfo, artefatto(TipoArtefatto.LANCIA, 1));
    }

    @Test
    void terzaArmaRifiutata() {
        Personaggio ladro = new Ladro("Pippo", 1);
        ladro.addArtefatto(artefatto(TipoArtefatto.SPADA, 1));
        ladro.addArtefatto(artefatto(TipoArtefatto.MAZZA, 1));
        assertRifiuto(MotivoRifiutoEquipaggiamento.SLOT_OCCUPATO, ladro, artefatto(TipoArtefatto.ASCIA, 1));
    }

    @Test
    void armaADueManiConUnaManoOccupata() {
        Personaggio guerriero = new Guerriero("Pippo", 1);
        guerriero.addArtefatto(artefatto(TipoArtefatto.SCUDO, 1));
        assertRifiuto(MotivoRifiutoEquipaggiamento.MANI_OCCUPATE, guerriero, artefatto(TipoArtefatto.SPADONE, 1));
        Personaggio altro = new Guerriero("Pluto", 1);
        altro.addArtefatto(artefatto(TipoArtefatto.SPADA, 1));
        assertRifiuto(MotivoRifiutoEquipaggiamento.MANI_OCCUPATE, altro, artefatto(TipoArtefatto.SPADONE, 1));
    }

    @Test
    void conUnArmaADueManiNonSiPrendonoOggettiDaMano() {
        Personaggio guerriero = new Guerriero("Pippo", 1);
        guerriero.addArtefatto(artefatto(TipoArtefatto.SPADONE, 1));
        assertRifiuto(MotivoRifiutoEquipaggiamento.ARMA_A_DUE_MANI_IMPUGNATA, guerriero, artefatto(TipoArtefatto.SCUDO, 1));
        assertRifiuto(MotivoRifiutoEquipaggiamento.ARMA_A_DUE_MANI_IMPUGNATA, guerriero, artefatto(TipoArtefatto.SPADA, 1));
        assertRifiuto(MotivoRifiutoEquipaggiamento.ARMA_A_DUE_MANI_IMPUGNATA, guerriero, artefatto(TipoArtefatto.SPADONE, 1));
        // L'elmo non è un oggetto da mano
        assertEquals(Optional.empty(), guerriero.puoEquipaggiare(artefatto(TipoArtefatto.ELMO, 1)));
    }

    // --- Slot di equipaggiamento

    @Test
    void prendereImpostaLoSlotERiporloLoAzzera() {
        Personaggio guerriero = new Guerriero("Pippo", 1);
        GruppoGiocatore gruppo = new GruppoGiocatore();
        Artefatto scudo = artefatto(TipoArtefatto.SCUDO, 1);
        guerriero.addArtefatto(scudo);
        assertEquals(SlotArtefatto.MANO_SECONDARIA, scudo.getModelloDati().getSlotEquipaggiamento());
        guerriero.removeArtefatto(scudo);
        gruppo.addArtefatto(scudo);
        assertNull(scudo.getModelloDati().getSlotEquipaggiamento());
    }

    @Test
    void laSecondaArmaDelLadroVaNellaManoSecondaria() {
        Personaggio ladro = new Ladro("Pippo", 1);
        Artefatto spada = artefatto(TipoArtefatto.SPADA, 1);
        Artefatto mazza = artefatto(TipoArtefatto.MAZZA, 1);
        ladro.addArtefatto(spada);
        assertEquals(Optional.empty(), ladro.puoEquipaggiare(mazza));
        ladro.addArtefatto(mazza);
        assertEquals(SlotArtefatto.MANO_PRINCIPALE, spada.getModelloDati().getSlotEquipaggiamento());
        assertEquals(SlotArtefatto.MANO_SECONDARIA, mazza.getModelloDati().getSlotEquipaggiamento());
        assertSame(spada.getModelloDati(), ((Artefatto) ladro.getArmaEquipaggiata()).getModelloDati());
        assertSame(mazza.getModelloDati(), ((Artefatto) ladro.getArmaSecondaria().orElseThrow(AssertionError::new)).getModelloDati());
    }

    @Test
    void anchePerLElfaLaSecondaArmaVaNellaManoSecondaria() {
        Personaggio elfa = new Elfa("Pippa", 1);
        elfa.addArtefatto(artefatto(TipoArtefatto.ASCIA, 1));
        Artefatto spada = artefatto(TipoArtefatto.SPADA, 1);
        assertEquals(Optional.empty(), elfa.puoEquipaggiare(spada));
        elfa.addArtefatto(spada);
        assertEquals(SlotArtefatto.MANO_SECONDARIA, spada.getModelloDati().getSlotEquipaggiamento());
    }

    @Test
    void riponendoLArmaPrincipaleLaSecondaRestaNellaSecondaria() {
        // Nessuno scambio automatico fra le mani: per invertirle si ripongono e si riprendono
        Personaggio ladro = new Ladro("Pippo", 1);
        Artefatto spada = artefatto(TipoArtefatto.SPADA, 1);
        Artefatto mazza = artefatto(TipoArtefatto.MAZZA, 1);
        ladro.addArtefatto(spada);
        ladro.addArtefatto(mazza);
        ladro.removeArtefatto(spada);
        assertInstanceOf(ArmaNaturale.class, ladro.getArmaEquipaggiata());
        assertTrue(ladro.getArmaSecondaria().isPresent());
        // La prossima arma presa va nella mano principale, libera
        Artefatto ascia = artefatto(TipoArtefatto.ASCIA, 1);
        ladro.addArtefatto(ascia);
        assertEquals(SlotArtefatto.MANO_PRINCIPALE, ascia.getModelloDati().getSlotEquipaggiamento());
    }

    @Test
    void lArmaImpugnataEQuellaDellaManoPrincipaleOADueMani() {
        Personaggio guerriero = new Guerriero("Pippo", 1);
        assertInstanceOf(ArmaNaturale.class, guerriero.getArmaEquipaggiata());
        assertFalse(guerriero.getArmaSecondaria().isPresent());
        Artefatto spadone = artefatto(TipoArtefatto.SPADONE, 1);
        guerriero.addArtefatto(spadone);
        assertEquals(SlotArtefatto.ENTRAMBE_LE_MANI, spadone.getModelloDati().getSlotEquipaggiamento());
        assertSame(spadone.getModelloDati(), ((Artefatto) guerriero.getArmaEquipaggiata()).getModelloDati());
        assertFalse(guerriero.getArmaSecondaria().isPresent());
    }

    @Test
    void ilMotivoSiLeggeComeFrase() {
        Personaggio guerriero = new Guerriero("Pippo", 1);
        assertEquals("Pippo non sa combattere con due armi.",
                MotivoRifiutoEquipaggiamento.SECONDA_ARMA_NON_CONSENTITA.getFrase(guerriero));
    }

    private static void assertRifiuto(MotivoRifiutoEquipaggiamento atteso, Personaggio personaggio, Artefatto artefatto) {
        assertEquals(Optional.of(atteso), personaggio.puoEquipaggiare(artefatto));
    }

    private static Artefatto artefatto(TipoArtefatto tipo, int livello) {
        ArtefattoMD md = new ArtefattoMD();
        md.setTipo(tipo);
        md.setNome("l'oggetto di prova");
        md.setDescrizione("che serve ai test");
        md.setLivello(livello);
        md.setPeso(0.1);
        return Artefatto.di(md);
    }
}
