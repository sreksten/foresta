package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.tipi.Costanti;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.artefatti.ArtefattoMD;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.personaggi.Ladro;
import com.threeamigos.foresta.personaggi.Mago;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.artefatti.tipi.TipoArtefatto;
import com.threeamigos.foresta.danni.tipi.TipoDanno;
import com.threeamigos.foresta.tipi.TipoMotivoRifiutoEquipaggiamento;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import com.threeamigos.foresta.combattimento.StatoDelCombattimento;

/**
 * Il cambio d'arma come azione sola: controllo preventivo, esecuzione, massimo di cambi e filtro per tipo di danno.
 */
class CambioArmaTest {

    private GruppoGiocatore gruppo;
    private Personaggio guerriero;

    @BeforeEach
    void prepara() {
        ModelloDati.setIstanza(new ModelloDati());
        BusEventi.impostaConsegna(Runnable::run);
        StatoDelCombattimento.azzeraIstanza();
        gruppo = new GruppoGiocatore();
        guerriero = new Guerriero("Pippo", 5);
    }

    @AfterEach
    void ripristina() {
        BusEventi.impostaConsegna(null);
        StatoDelCombattimento.azzeraIstanza();
    }

    private static Artefatto artefatto(TipoArtefatto tipo, double peso) {
        ArtefattoMD md = new ArtefattoMD();
        md.setTipo(tipo);
        md.setNome("l'oggetto di prova");
        md.setDescrizione("che serve ai test");
        md.setLivello(1);
        md.setPeso(peso);
        return Artefatto.di(md);
    }

    private static Artefatto spada() {
        return artefatto(TipoArtefatto.SPADA, 0.1);
    }

    private static void assertMotivo(TipoMotivoRifiutoEquipaggiamento atteso, Optional<RegoleEquipaggiamento.EsitoControlloRichiestaEquipaggiamento> esito) {
        assertTrue(esito.isPresent());
        assertEquals(atteso, esito.get().getMotivo());
    }

    @Test
    void unaSpadaAlPostoDiUnaSpadaSiPuoMentreSenzaTogliereLaVecchiaNo() {
        guerriero.addArtefatto(spada());
        Artefatto nuova = spada();
        // Così com'è, la seconda spada non è consentita al Guerriero...
        assertTrue(gruppo.controllaPrelievo(guerriero, nuova).isPresent());
        // ...ma al posto della prima sì
        assertFalse(gruppo.controllaCambioArma(guerriero, nuova).isPresent());
    }

    @Test
    void unArmaTroppoPesanteEntraSeEsceLaVecchia() {
        Artefatto vecchia = artefatto(TipoArtefatto.SPADA, guerriero.getCaricoMassimo() * 0.6);
        Artefatto nuova = artefatto(TipoArtefatto.MAZZA, guerriero.getCaricoMassimo() * 0.6);
        guerriero.addArtefatto(vecchia);
        assertMotivo(TipoMotivoRifiutoEquipaggiamento.TROPPO_CARICO, gruppo.controllaPrelievo(guerriero, nuova));
        assertFalse(gruppo.controllaCambioArma(guerriero, nuova).isPresent());
        // Se è più pesante anche del carico massimo vuoto, no
        Artefatto enorme = artefatto(TipoArtefatto.MAZZA, guerriero.getCaricoMassimo() * 1.1);
        assertMotivo(TipoMotivoRifiutoEquipaggiamento.TROPPO_CARICO, gruppo.controllaCambioArma(guerriero, enorme));
    }

    @Test
    void unArmaAdueManiConLoScudoInManoNonSiPuo() {
        guerriero.addArtefatto(spada());
        guerriero.addArtefatto(artefatto(TipoArtefatto.SCUDO, 0.1));
        assertMotivo(TipoMotivoRifiutoEquipaggiamento.MANI_OCCUPATE_PER_ARMA_A_DUE_MANI,
                gruppo.controllaCambioArma(guerriero, artefatto(TipoArtefatto.SPADONE, 0.1)));
        // Lo scudo resta dov'è
        assertEquals(2, guerriero.getInventario().size());
    }

    @Test
    void unArmaAdueManiAlPostoDellaSpadaSiPuo() {
        guerriero.addArtefatto(spada());
        assertFalse(gruppo.controllaCambioArma(guerriero, artefatto(TipoArtefatto.SPADONE, 0.1)).isPresent());
    }

    @Test
    void unaSpadaAlPostoDiUnArmaAdueManiSiPuo() {
        guerriero.addArtefatto(artefatto(TipoArtefatto.SPADONE, 0.1));
        assertFalse(gruppo.controllaCambioArma(guerriero, spada()).isPresent());
    }

    @Test
    void laClasseNonAdattaNonPuoCambiare() {
        Personaggio mago = new Mago("Merlino", 5);
        assertMotivo(TipoMotivoRifiutoEquipaggiamento.NON_ADATTO_ALLA_CLASSE, gruppo.controllaCambioArma(mago, spada()));
    }

    @Test
    void ilControlloNonCambiaNienteNeNotifica() {
        Artefatto vecchia = spada();
        Artefatto elmo = artefatto(TipoArtefatto.ELMO, 0.1);
        guerriero.addArtefatto(elmo);
        guerriero.addArtefatto(vecchia);
        List<ArtefattoMD> prima = new ArrayList<>(guerriero.getModelloDati().getArtefatti());
        List<Object> eventi = new ArrayList<>();
        BusEventi.iscriviti(com.threeamigos.foresta.eventi.notifiche.NotificaVariazioneStatistichePersonaggio.class, eventi::add);
        double carico = guerriero.getCarico();
        gruppo.controllaCambioArma(guerriero, spada());
        assertEquals(prima, new ArrayList<>(guerriero.getModelloDati().getArtefatti()));
        assertEquals(carico, guerriero.getCarico(), 1e-9);
        assertTrue(eventi.isEmpty());
        assertNotNull(vecchia.getModelloDati().getSlotEquipaggiamento());
    }

    @Test
    void cambiaArmaScambiaLeArmiEContaIlCambio() {
        Artefatto vecchia = spada();
        Artefatto nuova = artefatto(TipoArtefatto.MAZZA, 0.1);
        guerriero.addArtefatto(vecchia);
        gruppo.addArtefatto(nuova);

        assertTrue(gruppo.cambiaArma(guerriero, nuova));

        assertTrue(guerriero.getInventario().stream().anyMatch(a -> a.getModelloDati() == nuova.getModelloDati()));
        assertTrue(guerriero.getInventario().stream().noneMatch(a -> a.getModelloDati() == vecchia.getModelloDati()));
        assertTrue(gruppo.getInventario().stream().anyMatch(a -> a.getModelloDati() == vecchia.getModelloDati()));
        assertTrue(gruppo.getInventario().stream().noneMatch(a -> a.getModelloDati() == nuova.getModelloDati()));
        assertEquals(1, StatoDelCombattimento.getIstanza().getCambiDArma(guerriero));
    }

    @Test
    void dopoIlMassimoDeiCambiNonSiCambiaPiu() {
        Artefatto a = spada();
        Artefatto b = artefatto(TipoArtefatto.MAZZA, 0.1);
        Artefatto c = artefatto(TipoArtefatto.ASCIA, 0.1);
        guerriero.addArtefatto(a);
        gruppo.addArtefatto(b);
        gruppo.addArtefatto(c);
        assertEquals(2, Costanti.MAX_CAMBI_ARMA_PER_COMBATTIMENTO);
        assertTrue(gruppo.cambiaArma(guerriero, b));   // a -> b
        assertTrue(gruppo.cambiaArma(guerriero, a));   // b -> a
        assertFalse(gruppo.cambiaArma(guerriero, c));  // il terzo no
        assertFalse(gruppo.puoCambiareArma(guerriero, c));
        assertEquals(2, StatoDelCombattimento.getIstanza().getCambiDArma(guerriero));
        assertTrue(gruppo.getInventario().stream().anyMatch(x -> x.getModelloDati() == c.getModelloDati()));
    }

    @Test
    void uncambioRifiutatoNonTocca() {
        guerriero.addArtefatto(spada());
        guerriero.addArtefatto(artefatto(TipoArtefatto.SCUDO, 0.1));
        Artefatto spadone = artefatto(TipoArtefatto.SPADONE, 0.1);
        gruppo.addArtefatto(spadone);
        assertFalse(gruppo.cambiaArma(guerriero, spadone));
        assertEquals(2, guerriero.getInventario().size());
        assertEquals(1, gruppo.getInventario().size());
        assertEquals(0, StatoDelCombattimento.getIstanza().getCambiDArma(guerriero));
    }

    @Test
    void leValutazioniSiRicordanoESiScartanoConUnCambio() {
        StatoDelCombattimento stato = StatoDelCombattimento.getIstanza();
        guerriero.addArtefatto(spada());
        Artefatto nuova = artefatto(TipoArtefatto.MAZZA, 0.1);
        Artefatto altra = artefatto(TipoArtefatto.ASCIA, 0.1);
        gruppo.addArtefatto(nuova);
        gruppo.addArtefatto(altra);
        assertNull(stato.getValutazioneArma(guerriero, nuova));
        assertTrue(gruppo.puoCambiareArma(guerriero, nuova));
        assertEquals(Boolean.TRUE, stato.getValutazioneArma(guerriero, nuova));
        assertTrue(gruppo.puoCambiareArma(guerriero, altra));
        gruppo.cambiaArma(guerriero, nuova);
        assertNull(stato.getValutazioneArma(guerriero, altra));
    }

    @Test
    void ilFiltroTrovaSoloLeArmiDelTipoDiDannoCheSiPossonoPrendere() {
        guerriero.addArtefatto(spada());
        Artefatto sacra = spada();
        sacra.getModelloDati().addIncantamento("sacro", TipoDanno.SACRO, 10, 0.1);
        Artefatto normale = spada();
        Artefatto sacraAdueManiConScudo = artefatto(TipoArtefatto.SPADONE, 0.1);
        sacraAdueManiConScudo.getModelloDati().addIncantamento("sacro", TipoDanno.SACRO, 10, 0.1);
        guerriero.addArtefatto(artefatto(TipoArtefatto.SCUDO, 0.1));
        gruppo.addArtefatto(sacra);
        gruppo.addArtefatto(normale);
        gruppo.addArtefatto(sacraAdueManiConScudo);

        List<Artefatto> trovate = gruppo.armiPerCambio(guerriero, TipoDanno.SACRO);
        assertEquals(1, trovate.size());
        assertSame(sacra.getModelloDati(), trovate.get(0).getModelloDati());
    }

    @Test
    void equipaggiaEStoccaSpostanoSenzaControlli() {
        Artefatto spada = spada();
        gruppo.addArtefatto(spada);
        gruppo.equipaggia(guerriero, spada);
        assertEquals(0, gruppo.getInventario().size());
        assertEquals(1, guerriero.getInventario().size());
        gruppo.stocca(guerriero, spada);
        assertEquals(1, gruppo.getInventario().size());
        assertEquals(0, guerriero.getInventario().size());
    }

    // --- Doppia arma: esce la più debole

    private static Artefatto armaDa(TipoArtefatto tipo, int danni) {
        Artefatto arma = artefatto(tipo, 0.1);
        arma.getModelloDati().setDanni(danni);
        return arma;
    }

    private static boolean impugna(Personaggio personaggio, Artefatto arma) {
        return personaggio.getInventario().stream().anyMatch(a -> a.getModelloDati() == arma.getModelloDati());
    }

    @Test
    void conLaDoppiaArmaEsceLaSecondariaSeELaPiuDebole() {
        Personaggio ladro = new Ladro("Pippo", 5);
        Artefatto forte = armaDa(TipoArtefatto.SPADA, 20);
        Artefatto debole = armaDa(TipoArtefatto.MAZZA, 5);
        Artefatto nuova = armaDa(TipoArtefatto.ASCIA, 10);
        ladro.addArtefatto(forte);
        ladro.addArtefatto(debole);
        gruppo.addArtefatto(nuova);
        assertTrue(gruppo.cambiaArma(ladro, nuova));
        assertTrue(impugna(ladro, forte));
        assertTrue(impugna(ladro, nuova));
        assertFalse(impugna(ladro, debole));
    }

    @Test
    void conLaDoppiaArmaEsceLaPrincipaleSeELaPiuDebole() {
        Personaggio ladro = new Ladro("Pippo", 5);
        Artefatto debole = armaDa(TipoArtefatto.SPADA, 5);
        Artefatto forte = armaDa(TipoArtefatto.MAZZA, 20);
        Artefatto nuova = armaDa(TipoArtefatto.ASCIA, 10);
        ladro.addArtefatto(debole);
        ladro.addArtefatto(forte);
        gruppo.addArtefatto(nuova);
        assertTrue(gruppo.cambiaArma(ladro, nuova));
        assertTrue(impugna(ladro, forte));
        assertTrue(impugna(ladro, nuova));
        assertFalse(impugna(ladro, debole));
        // La nuova ha preso la mano principale
        assertEquals(nuova.getModelloDati().getTipo().getSlotArtefatto(), nuova.getModelloDati().getSlotEquipaggiamento());
    }

    @Test
    void aParitaEsceLaSecondaria() {
        Personaggio ladro = new Ladro("Pippo", 5);
        Artefatto principale = armaDa(TipoArtefatto.SPADA, 10);
        Artefatto secondaria = armaDa(TipoArtefatto.MAZZA, 10);
        Artefatto nuova = armaDa(TipoArtefatto.ASCIA, 10);
        ladro.addArtefatto(principale);
        ladro.addArtefatto(secondaria);
        gruppo.addArtefatto(nuova);
        assertTrue(gruppo.cambiaArma(ladro, nuova));
        assertTrue(impugna(ladro, principale));
        assertFalse(impugna(ladro, secondaria));
    }

    @Test
    void unaLanciaNonVaNellaSecondariaQuindiEsceLaPrincipaleAnchePiuForte() {
        Personaggio elfo = new com.threeamigos.foresta.personaggi.Elfo("Legolas", 5);
        Artefatto forte = armaDa(TipoArtefatto.SPADA, 20);
        Artefatto debole = armaDa(TipoArtefatto.MAZZA, 5);
        Artefatto lancia = armaDa(TipoArtefatto.LANCIA, 10);
        elfo.addArtefatto(forte);
        elfo.addArtefatto(debole);
        gruppo.addArtefatto(lancia);
        assertTrue(gruppo.cambiaArma(elfo, lancia));
        assertFalse(impugna(elfo, forte));
        assertTrue(impugna(elfo, debole));
        assertTrue(impugna(elfo, lancia));
    }

    @Test
    void ilGuerrieroNonHaLaDoppiaArmaEIlSuoCambioNonCambia() {
        Artefatto principale = armaDa(TipoArtefatto.SPADA, 20);
        Artefatto nuova = armaDa(TipoArtefatto.MAZZA, 1);
        guerriero.addArtefatto(principale);
        gruppo.addArtefatto(nuova);
        assertTrue(gruppo.cambiaArma(guerriero, nuova));
        assertFalse(impugna(guerriero, principale));
        assertTrue(impugna(guerriero, nuova));
    }
}
