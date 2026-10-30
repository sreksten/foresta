package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.gruppi.DatiDiProva;
import com.threeamigos.foresta.modellodati.ArtefattoMD;
import com.threeamigos.foresta.modellodati.PersonaggioMD;
import com.threeamigos.foresta.tipi.CaratteristicaDiClasse;
import com.threeamigos.foresta.tipi.CategoriaPersonaggio;
import com.threeamigos.foresta.tipi.MoltiplicatoreDiClasse;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.tipi.TipoIncantesimo;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.tipi.TipoRiposo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I piccoli pezzi che le altre prove non toccano: i costruttori con nome di ogni classe, i costi degli incantesimi, l'arma naturale, i testi delle
 * caratteristiche di classe, il riposo di chi non guarisce e le sorgenti di caso della creazione.
 */
class CodaDiCoperturaTest {

    @BeforeEach
    void prepara() {
        DatiDiProva.nuovaPartita();
    }

    @Test
    void ogniClasseConcretaSiCostruisceAncheConUnNome() throws Exception {
        List<Class<?>> classi = new ArrayList<>();
        try (Stream<Path> files = Files.list(Paths.get("target/classes/com/threeamigos/foresta/personaggi"))) {
            for (Path file : files.filter(f -> f.toString().endsWith(".class") && !f.toString().contains("$")).collect(Collectors.toList())) {
                String nome = "com.threeamigos.foresta.personaggi." + file.getFileName().toString().replace(".class", "");
                classi.add(Class.forName(nome));
            }
        }
        int conNome = 0;
        for (Class<?> classe : classi) {
            if (!PersonaggioBase.class.isAssignableFrom(classe) || java.lang.reflect.Modifier.isAbstract(classe.getModifiers())) {
                continue;
            }
            for (Constructor<?> costruttore : classe.getConstructors()) {
                if (costruttore.getParameterCount() == 2 && costruttore.getParameterTypes()[0] == String.class) {
                    Personaggio p = (Personaggio) costruttore.newInstance("Prova", 2);
                    assertEquals("Prova", p.getNomeProprio().get(), classe.getSimpleName());
                    conNome++;
                }
            }
        }
        assertTrue(conNome >= 30, "costruttori con nome: " + conNome);
    }

    @Test
    void iCostiDegliIncantesimi() {
        int conCostoDiLancio = 0;
        for (TipoIncantesimo tipo : TipoIncantesimo.values()) {
            assertTrue(tipo.getCostoAcquisto() > 0, tipo.name());
            assertTrue(tipo.getCostoLancio() > 0, tipo.name());
            conCostoDiLancio++;
        }
        assertEquals(TipoIncantesimo.values().length, conCostoDiLancio);
    }

    @Test
    void l_armaNaturaleDipendeDalMostroEsiRiparaControChiEImmuneAlFuoco() {
        for (TipoPersonaggio tipo : TipoPersonaggio.values()) {
            Personaggio p = FabbricaPersonaggi.crea(tipo, 3);
            ArmaNaturale arma;
            try {
                arma = new ArmaNaturale(p);
            } catch (IllegalArgumentException | IllegalStateException e) {
                continue;
            }
            assertEquals(3, arma.getLivello());
            assertTrue(arma.getDanni() >= 0);
            assertNotNull(arma.getTipoDanno());
            assertFalse(arma.isIncantata());
            assertTrue(arma.getIncantamenti().isEmpty());
        }
        Personaggio ombra = new OmbraFiamma(3);
        ArmaNaturale fiamma = new ArmaNaturale(ombra);
        assertEquals(TipoDanno.FUOCO, fiamma.getTipoDanno());
        Personaggio drago = FabbricaPersonaggi.crea(TipoPersonaggio.DRAGO, 3);
        assertTrue(drago.isImmuneATipoDanno(TipoDanno.FUOCO));
        assertNotEquals(TipoDanno.FUOCO, fiamma.controDifensore(drago).getTipoDanno());
        assertSame(fiamma, fiamma.controDifensore(FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 1)));
        ArmaNaturale morso = new ArmaNaturale(FabbricaPersonaggi.crea(TipoPersonaggio.TROLL, 1));
        assertSame(morso, morso.controDifensore(drago));
    }

    @Test
    void leCaratteristicheDiClasseSiRaccontanoESiConfrontano() {
        CaratteristicaDiClasse fuoco = CaratteristicaDiClasse.danno(TipoDanno.FUOCO, 1.5d);
        CaratteristicaDiClasse fuoco2 = CaratteristicaDiClasse.danno(TipoDanno.FUOCO, 1.5d);
        CaratteristicaDiClasse fuocoMeno = CaratteristicaDiClasse.danno(TipoDanno.FUOCO, 0.5d);
        CaratteristicaDiClasse immune = CaratteristicaDiClasse.immuneAIncantesimo(TipoIncantesimo.MORTE);
        CaratteristicaDiClasse tutti = CaratteristicaDiClasse.immuneATuttiGliIncantesimi();
        CaratteristicaDiClasse effetto = CaratteristicaDiClasse.immuneAEffetto(TipoEffettoDiStato.INFETTATO);
        assertEquals(fuoco, fuoco2);
        assertEquals(fuoco.hashCode(), fuoco2.hashCode());
        assertNotEquals(fuoco, fuocoMeno);
        assertNotEquals(fuoco, immune);
        assertNotEquals(fuoco, "fuoco");
        assertEquals(TipoDanno.FUOCO, fuoco.getValore());
        assertNull(tutti.getValore());
        assertTrue(immune.isImmunita() && tutti.isImmunita() && effetto.isImmunita());
        assertFalse(fuoco.isImmunita());
        assertTrue(fuoco.toString().endsWith(" x1.5"), fuoco.toString());
        assertFalse(immune.toString().contains(" x"), immune.toString());
        assertNotNull(fuoco.getDescrizione());
        assertNotNull(tutti.getDescrizione());
        assertNotNull(effetto.getDescrizione());
        assertNotNull(immune.getDescrizione());
    }

    @Test
    void ilNomeDellaCategoriaEIlTestoDelMoltiplicatore() {
        for (CategoriaPersonaggio categoria : CategoriaPersonaggio.values()) {
            assertNotNull(categoria.getNome(), categoria.name());
        }
        MoltiplicatoreDiClasse moltiplicatore = new MoltiplicatoreDiClasse(TipoAttributo.FORZA.name(), TipoPersonaggio.GUERRIERO, 1.25d);
        assertEquals(TipoAttributo.FORZA.getNome() + " x1,25", moltiplicatore.toString());
    }

    @Test
    void chiNonGuariscePerRiposoNonRecuperaSaluteOStanchezza() {
        Personaggio scheletro = FabbricaPersonaggi.crea(TipoPersonaggio.SCHELETRO, 3);
        RisultatoRiposo risultato = CalcolatoreRiposo.calcolaRiposo(scheletro, 8, TipoRiposo.AL_COPERTO);
        assertEquals(0, risultato.getRipristinoSalute());
        assertEquals(0, risultato.getAbbassamentoStanchezza());
        assertEquals(TipoRiposo.AL_COPERTO, risultato.getTipoRiposo());
        assertEquals(8, risultato.getTempoRiposo());
        Personaggio guerriero = new Guerriero("Arsenio", 3);
        RisultatoRiposo vivo = CalcolatoreRiposo.calcolaRiposo(guerriero, 8, TipoRiposo.AL_COPERTO);
        assertTrue(vivo.getRipristinoSalute() > 0 && vivo.getAbbassamentoStanchezza() > 0);
        assertNotNull(new CalcolatoreRiposo());
    }

    @Test
    void laFabbricaDiceNomiEQuantitaDiOgniClasse() {
        assertEquals("Goblin", FabbricaPersonaggi.nomeSingolare(TipoPersonaggio.GOBLIN));
        assertEquals("Goblin", FabbricaPersonaggi.nomePlurale(TipoPersonaggio.GOBLIN));
        assertTrue(FabbricaPersonaggi.quantitaMassima(TipoPersonaggio.GOBLIN) > 0);
    }

    @Test
    void ladistribuzioneDeiPuntiDaTuttiIPesiPerOgniClasse() {
        for (TipoPersonaggio tipo : TipoPersonaggio.values()) {
            int[] percentuali = LanciatoreDeiDadi.getPercentualiPer(tipo);
            assertEquals(7, percentuali.length, tipo.name());
            assertEquals(100, java.util.Arrays.stream(percentuali).sum(), tipo.name());
        }
        for (TipoPersonaggio tipo : TipoPersonaggio.values()) {
            for (int livello : new int[] {0, 1, 3, 10, 25}) {
                for (int giro = 0; giro < 30; giro++) {
                    LanciatoreDeiDadi.tiraDadiPer(tipo, livello, new PersonaggioMD());
                }
            }
        }
        assertNotNull(new LanciatoreDeiDadi());
    }

    @Test
    void ilSlotOccupatoSenzaRegoleEQuelloDelTipo() {
        ArtefattoMD md = new com.threeamigos.foresta.artefatti.GeneratoreArtefattiTabelle(new java.util.Random(3))
                .generaArtefatto(com.threeamigos.foresta.tipi.TipoArtefatto.SPADA, 1).getModelloDati();
        md.setSlotEquipaggiamento(null);
        assertEquals(md.getTipo().getSlotArtefatto(), RegoleEquipaggiamento.slotOccupato(md));
    }
}
