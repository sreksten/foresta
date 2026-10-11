package com.threeamigos.foresta.tattiche;

import com.threeamigos.foresta.incantesimi.FabbricaIncantesimi;
import com.threeamigos.foresta.incantesimi.IncantesimoMalefico;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.modellodati.ModificatoreAttributoMD;
import com.threeamigos.foresta.modellodati.tattiche.TatticaMD;
import com.threeamigos.foresta.motore.CalcolatoreCombattimento;
import com.threeamigos.foresta.motore.Dado;
import com.threeamigos.foresta.motore.StatoDelCombattimento;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.personaggi.Viandante;
import com.threeamigos.foresta.tipi.ClasseIncantesimo;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.tipi.TipoModificatore;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.tipi.tattiche.TipoAzione;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * La tattica base dei mostri, che replica come scegliavano prima delle tattiche (che era in PersonaggioBase: chi colpire e se e quale incantesimo lanciare).
 */
class RegistroTatticheTest {

    private Personaggio guerriero;
    private Personaggio ladro;
    private Personaggio mago1;
    private Personaggio mago3;
    private Personaggio sacerdote;
    private Personaggio elfa;
    private Personaggio bardo;
    private Personaggio goblin;
    private Personaggio strega;

    @BeforeEach
    void prepara() {
        ModelloDati.setIstanza(new ModelloDati());
        StatoDelCombattimento.getIstanza().reimposta();
        guerriero = FabbricaPersonaggi.crea(TipoPersonaggio.GUERRIERO, 1);
        ladro = FabbricaPersonaggi.crea(TipoPersonaggio.LADRO, 1);
        mago1 = FabbricaPersonaggi.crea(TipoPersonaggio.MAGO, 1);
        mago3 = FabbricaPersonaggi.crea(TipoPersonaggio.MAGA, 3);
        sacerdote = FabbricaPersonaggi.crea(TipoPersonaggio.SACERDOTE, 1);
        elfa = FabbricaPersonaggi.crea(TipoPersonaggio.ELFA, 1);
        bardo = FabbricaPersonaggi.crea(TipoPersonaggio.BARDO, 1);
        goblin = FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 1);
        strega = FabbricaPersonaggi.crea(TipoPersonaggio.STREGA, 1);
    }

    @AfterEach
    void ripristina() {
        Dado.ripristina();
        RegistroTattiche.registra(TipoPersonaggio.GOBLIN, null);
    }

    private ContestoTattico contesto(Personaggio mostro, Personaggio... miei) {
        return new ContestoTattico(mostro, false, Collections.singletonList(mostro), Arrays.asList(miei), Collections.emptyList());
    }

    private static Decisione decidi(Personaggio mostro, ContestoTattico contesto) {
        // La replica di come sceglievano i mostri prima delle tattiche; le tattiche base vere sono in TatticheBaseTest
        return Valutatore.decidi(RegistroTattiche.tatticaDiOggi(), contesto);
    }

    private static void fissa(Personaggio p, TipoAttributo attributo, double valore) {
        p.addModificatore(new ModificatoreAttributoMD(attributo, TipoModificatore.QUANTITA_ASSOLUTA, valore));
    }

    // ---- il registro ----

    @Test
    void laTatticaDiOggiHaVentiVociEValePerIMostri() {
        TatticaMD tattica = RegistroTattiche.tatticaDiOggi();
        assertEquals(20, tattica.getVoci().size());
        assertEquals(Collections.emptyList(), tattica.problemi(false), "valida per un mostro");
        // Le classi senza una tattica base scritta (non sono mostri) ripiegano su quella di oggi
        assertSame(tattica, RegistroTattiche.perClasse(TipoPersonaggio.UOMO));
        assertSame(tattica, RegistroTattiche.perClasse(TipoPersonaggio.LOCANDIERE));
    }

    @Test
    void UnaClasseSiPuoDareUnaTatticaTuaEPoiTogliere() {
        TatticaMD sua = TatticaMD.di(com.threeamigos.foresta.modellodati.tattiche.VoceMD.su(
                new com.threeamigos.foresta.modellodati.tattiche.SelettoreMD(com.threeamigos.foresta.tipi.tattiche.TipoRosa.AVVERSARI, Collections.emptyList(),
                        com.threeamigos.foresta.modellodati.tattiche.OrdinamentoMD.di(com.threeamigos.foresta.tipi.tattiche.TipoOrdinamento.MENO_SALUTE)),
                com.threeamigos.foresta.modellodati.tattiche.AzioneMD.di(TipoAzione.ATTACCO_FISICO)));
        RegistroTattiche.registra(TipoPersonaggio.GOBLIN, sua);
        assertSame(sua, RegistroTattiche.perClasse(TipoPersonaggio.GOBLIN));
        assertEquals(TatticheBase.deiMostri(TipoPersonaggio.TROLL), RegistroTattiche.perClasse(TipoPersonaggio.TROLL));
        RegistroTattiche.registra(TipoPersonaggio.GOBLIN, null);
        assertEquals(TatticheBase.deiMostri(TipoPersonaggio.GOBLIN), RegistroTattiche.perClasse(TipoPersonaggio.GOBLIN));
    }

    // ---- chi colpire: il Mago più potente, poi il Sacerdote, l'Elfo, il Bardo, poi a caso ----

    @Test
    void ilMagoPiuPotenteEIlBersagliodiTutti() {
        Decisione d = decidi(goblin, contesto(goblin, guerriero, mago1, sacerdote, mago3, ladro));
        assertSame(mago3, d.getBersaglio(), "la Maga di livello 3, anche se il Mago è prima nell'elenco");
        assertEquals(TipoAzione.ATTACCO_FISICO, d.getTipoEseguito());
    }

    @Test
    void aParitaDiLivelloIlPrimo() {
        Personaggio altroMago = FabbricaPersonaggi.crea(TipoPersonaggio.MAGO, 1);
        assertSame(mago1, decidi(goblin, contesto(goblin, guerriero, mago1, altroMago)).getBersaglio());
        assertSame(altroMago, decidi(goblin, contesto(goblin, guerriero, altroMago, mago1)).getBersaglio());
    }

    @Test
    void senzaMaghiIlSacerdoteSenzaSacerdotiL_ElfoSenzaElfiIlBardo() {
        assertSame(sacerdote, decidi(goblin, contesto(goblin, guerriero, bardo, elfa, sacerdote)).getBersaglio());
        assertSame(elfa, decidi(goblin, contesto(goblin, guerriero, bardo, elfa)).getBersaglio());
        assertSame(bardo, decidi(goblin, contesto(goblin, guerriero, bardo, ladro)).getBersaglio());
    }

    @Test
    void senzaCasterUnoACasoFraTuttiGliOspitiVulnerabiliCompresi() {
        Personaggio ostaggio = new Viandante("Ottone", 1);
        ContestoTattico contesto = new ContestoTattico(goblin, false, Collections.singletonList(goblin), Arrays.asList(guerriero, ladro),
                Collections.singletonList(ostaggio));
        Set<Personaggio> colpiti = new HashSet<>();
        for (int seme = 0; seme < 200; seme++) {
            Dado.impostaSeme(seme);
            colpiti.add(decidi(goblin, contesto).getBersaglio());
        }
        assertEquals(new HashSet<>(Arrays.asList(guerriero, ladro, ostaggio)), colpiti);
    }

    @Test
    void ICadutiNonSiColpisconoEUnMagoCadutoLasciaIlSacerdote() {
        mago3.subSalute(1_000_000, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        assertSame(sacerdote, decidi(goblin, contesto(goblin, mago3, guerriero, sacerdote)).getBersaglio());
    }

    // ---- se e quale incantesimo: secondo l'intelligenza, con una rinuncia su tre ----
    // (il bersaglio è un Mago, la prima voce dei bersagli: così il dado della rinuncia si tira una volta sola)

    private ContestoTattico dellaStrega() {
        return contesto(strega, mago1);
    }

    private ClasseIncantesimo piuPotenteChePuoPagare(Personaggio mostro) {
        ClasseIncantesimo migliore = null;
        for (ClasseIncantesimo classe : ClasseIncantesimo.values()) {
            if (mostro.getRepertorioIncantesimi().contains(classe) && FabbricaIncantesimi.costoLancio(classe) <= mostro.getMagia()
                    && !mago1.isImmuneAIncantesimo(classe)) {
                if (migliore == null || ((IncantesimoMalefico) FabbricaIncantesimi.crea(classe, mostro.getLivello())).getDanni()
                        > ((IncantesimoMalefico) FabbricaIncantesimi.crea(migliore, mostro.getLivello())).getDanni()) {
                    migliore = classe;
                }
            }
        }
        return migliore;
    }

    @Test
    void unMostroNonMagicoNonTiraIlDadoEAttaccaConLeArmi() {
        Dado.trucca(1, 1, 1);
        Decisione d = decidi(goblin, contesto(goblin, guerriero));
        assertEquals(TipoAzione.ATTACCO_FISICO, d.getTipoEseguito());
        assertEquals(3, Dado.trucchiRimasti(), "nessun dado consumato: non è magico");
    }

    @Test
    void unMostroPocoIntelligenteLanciaUnIncantesimoACasoUnaVoltaSuTreRinuncia() {
        fissa(strega, TipoAttributo.INTELLIGENZA, 3);
        assertTrue(strega.getMagia() > 0 && strega.isMagico());
        // Il dado della rinuncia dentro il 67%, poi la scelta a caso (il primo che può pagare)
        Dado.trucca(10, 1);
        Decisione lancia = decidi(strega, dellaStrega());
        assertNotNull(lancia.getIncantesimo(), lancia.getSpiegazione().toString());
        assertTrue(strega.getRepertorioIncantesimi().contains(lancia.getIncantesimo()));
        assertSame(mago1, lancia.getBersaglio());
        // Fuori dal 67%: rinuncia e attacca con le armi
        Dado.ripristina();
        Dado.trucca(90);
        Decisione rinuncia = decidi(strega, dellaStrega());
        assertEquals(TipoAzione.ATTACCO_FISICO, rinuncia.getTipoEseguito());
        assertEquals(0, Dado.trucchiRimasti(), "un solo dado: le altre fasce non lo tirano");
    }

    @Test
    void unMostroMedianamenteFurboLanciaIlPiuPotente() {
        fissa(strega, TipoAttributo.INTELLIGENZA, 6);
        Dado.trucca(10);
        Decisione d = decidi(strega, dellaStrega());
        assertEquals(piuPotenteChePuoPagare(strega), d.getIncantesimo(), d.getSpiegazione().toString());
    }

    @Test
    void unMostroFurboLanciaIlPiuPotenteSoloSeRendePiuDelleArmi() {
        fissa(strega, TipoAttributo.INTELLIGENZA, 9);
        ClasseIncantesimo piuPotente = piuPotenteChePuoPagare(strega);
        Dado.trucca(10);
        Decisione d = decidi(strega, dellaStrega());
        double incantesimo = CalcolatoreCombattimento.dannoAtteso(strega, mago1,
                (com.threeamigos.foresta.interfacce.Arma) FabbricaIncantesimi.crea(piuPotente, strega.getLivello()));
        double armi = CalcolatoreCombattimento.dannoAttesoDellaMischia(strega, mago1);
        if (incantesimo > armi) {
            assertEquals(piuPotente, d.getIncantesimo());
        } else {
            assertEquals(TipoAzione.ATTACCO_FISICO, d.getTipoEseguito(), "incantesimo " + incantesimo + " armi " + armi);
        }
    }

    @Test
    void unMostroSilenziatoOSenzaMagiaAttaccaConLeArmi() {
        fissa(strega, TipoAttributo.INTELLIGENZA, 3);
        strega.addEffettoDiStato(TipoEffettoDiStato.SILENZIATO, 3, 0);
        Dado.trucca(10, 1);
        assertEquals(TipoAzione.ATTACCO_FISICO, decidi(strega, dellaStrega()).getTipoEseguito());
        assertEquals(2, Dado.trucchiRimasti(), "silenziato: non tira nemmeno il dado");
        Dado.ripristina();
        Personaggio altra = FabbricaPersonaggi.crea(TipoPersonaggio.STREGA, 1);
        fissa(altra, TipoAttributo.INTELLIGENZA, 3);
        altra.subMagia(altra.getMagia());
        assertEquals(0, altra.getMagia());
        assertEquals(TipoAzione.ATTACCO_FISICO, decidi(altra, contesto(altra, mago1)).getTipoEseguito());
    }

    @Test
    void unMostroNonLanciaIlSilenzioSuChiELoGiaENonLancia_UnIncantesimoACuiESImmune() {
        fissa(strega, TipoAttributo.INTELLIGENZA, 3);
        mago1.addEffettoDiStato(TipoEffettoDiStato.SILENZIATO, 3, 0);
        for (int seme = 0; seme < 100; seme++) {
            Dado.impostaSeme(seme);
            Decisione d = decidi(strega, dellaStrega());
            assertNotEquals(ClasseIncantesimo.SILENZIO, d.getIncantesimo(), "già silenziato");
        }
    }
}
