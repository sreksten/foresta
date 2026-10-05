package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.motore.ArmaNaturale;
import com.threeamigos.foresta.motore.modellodati.ArtefattoMD;
import com.threeamigos.foresta.motore.modellodati.ModelloDati;
import com.threeamigos.foresta.motore.modellodati.ModificatoreAttributo;
import com.threeamigos.foresta.motore.tipi.*;
import com.threeamigos.foresta.personaggi.Guerriero;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GeneratoreArtefattiTest {

    private static final int GIRI = 500;

    @Test
    void generaUnArtefattoDelTipoEDelLivelloChiesti() {
        GeneratoreArtefatti generatore = new GeneratoreArtefattiTabelle(new Random(1));
        for (TipoArtefatto tipo : TipoArtefatto.values()) {
            Artefatto artefatto = generatore.generaArtefatto(tipo, 4);
            assertEquals(tipo, artefatto.getTipo());
            assertEquals(4, artefatto.getLivello());
            assertNotNull(artefatto.getNome());
            assertNotNull(artefatto.getDescrizione());
            assertTrue(artefatto.getPeso() > 0);
            assertTrue(artefatto.getCostoAcquisto() > 0);
            if (tipo.getSupertipo() == SupertipoArtefatto.ARMA) {
                assertInstanceOf(ArmaFisica.class, artefatto);
                assertTrue(artefatto.getModelloDati().getDanni() > 0);
            }
        }
    }

    @Test
    void ilLivelloMinimoEUno() {
        GeneratoreArtefatti generatore = new GeneratoreArtefattiTabelle(new Random(2));
        assertEquals(1, generatore.generaArtefatto(TipoArtefatto.SPADA, 0).getLivello());
        assertEquals(1, generatore.generaIngrediente(-3).getLivello());
    }

    @Test
    void loSpadoneFaIlCinquantaPerCentoInPiu() {
        // Con lo stesso seme i due tiri danno lo stesso scarto sul danno base
        ArtefattoMD spada = new GeneratoreArtefattiTabelle(new Random(3)).generaArtefatto(TipoArtefatto.SPADA, 5).getModelloDati();
        ArtefattoMD spadone = new GeneratoreArtefattiTabelle(new Random(3)).generaArtefatto(TipoArtefatto.SPADONE, 5).getModelloDati();
        assertEquals(Math.round(spada.getDanni() * 1.5), spadone.getDanni());
        assertTrue(spadone.getCostoAcquisto() > spada.getCostoAcquisto());
    }

    @Test
    void aiLivelliBassiLeArmiFannoAlmenoSediciPiuIlLivello() {
        assertEquals(17, GeneratoreArtefatti.danniMediArma(1));
        assertEquals(18, GeneratoreArtefatti.danniMediArma(2));
        assertEquals(21, GeneratoreArtefatti.danniMediArma(5));
        // Dal livello 12 vale di nuovo 4 + 2 × livello
        assertEquals(28, GeneratoreArtefatti.danniMediArma(12));
        assertEquals(34, GeneratoreArtefatti.danniMediArma(15));
    }

    @Test
    void unaSpadaDiLivelloUnoFaPiuDelleManiNudeDiUnGuerriero() {
        ModelloDati.setIstanza(new ModelloDati());
        int maniNude = new ArmaNaturale(new Guerriero("Pippo", 1)).getDanni();
        GeneratoreArtefatti generatore = new GeneratoreArtefattiTabelle(new Random(5));
        for (int i = 0; i < GIRI; i++) {
            int danni = generatore.generaArtefatto(TipoArtefatto.SPADA, 1).getModelloDati().getDanni();
            assertTrue(danni > maniNude, "spada " + danni + ", mani nude " + maniNude);
        }
    }

    @Test
    void lArtefattoCasualeNonEMaiUnIngrediente() {
        GeneratoreArtefatti generatore = new GeneratoreArtefattiTabelle(new Random(4));
        Set<TipoArtefatto> visti = EnumSet.noneOf(TipoArtefatto.class);
        for (int i = 0; i < GIRI; i++) {
            visti.add(generatore.generaArtefattoCasuale(3).getTipo());
        }
        assertTrue(visti.stream().noneMatch(TipoArtefatto::isIngrediente));
        assertEquals(Arrays.stream(TipoArtefatto.values()).filter(t -> !t.isIngrediente()).count(), visti.size());
    }

    @Test
    void ilNomeProprioCompareDiRado() {
        GeneratoreArtefatti generatore = new GeneratoreArtefattiTabelle(new Random(5));
        int conNomeProprio = 0;
        for (int i = 0; i < GIRI; i++) {
            if (generatore.generaArtefatto(TipoArtefatto.SPADA, 3).getNomeProprio().isPresent()) {
                conNomeProprio++;
            }
        }
        assertTrue(conNomeProprio > 0);
        assertTrue(conNomeProprio < GIRI / 5);
    }

    @Test
    void gliIngredientiHannoEffettiDellaLoroSpecialitaEDelGradoGiusto() {
        GeneratoreArtefatti generatore = new GeneratoreArtefattiTabelle(new Random(6));
        Set<TipoArtefatto> tipi = EnumSet.noneOf(TipoArtefatto.class);
        for (int i = 0; i < GIRI; i++) {
            int livello = 1 + i % 10;
            GradoIncantamento grado = GradoIncantamento.perIngrediente(livello);
            Artefatto pergamena = generatore.generaIngrediente(livello);
            ArtefattoMD md = pergamena.getModelloDati();
            TipoArtefatto tipo = pergamena.getTipo();
            assertTrue(tipo.isIngrediente());
            tipi.add(tipo);
            // Il livello di riferimento, e tanti effetti quanto il livello fino a tre, tutti diversi
            assertEquals(livello, pergamena.getLivello());
            int effetti = md.getIncantamenti().size() + md.getModificatori().size();
            assertEquals(Math.min(3, livello), effetti);
            assertEquals(md.getIncantamenti().size(), md.getIncantamenti().stream().map(Incantamento::getTipoDannoElementale).distinct().count());
            assertEquals(md.getModificatori().size(), md.getModificatori().stream().map(m -> m.getTipoAttributo()).distinct().count());
            for (ModificatoreAttributo modificatore : md.getModificatori()) {
                assertTrue(GeneratoreArtefattiTabelle.attributiDi(tipo).contains(modificatore.getTipoAttributo()), md.getNome());
            }
            // Il grado nel nome: niente per il medio
            assertEquals(grado != GradoIncantamento.MEDIO, md.getNome().contains(" " + grado.getNome()), md.getNome());
            for (Incantamento incantamento : md.getIncantamenti()) {
                assertTrue(GeneratoreArtefattiTabelle.danniDi(tipo).contains(incantamento.getTipoDannoElementale()), md.getNome());
                assertNotEquals(SupertipoDanno.FISICO, incantamento.getTipoDannoElementale().getSuperTipo());
                assertTrue(incantamento.getDannoBonusFisso() == 0 || incantamento.getDannoBonusFisso() == grado.getBonusFisso());
                assertTrue(incantamento.getCoefficienteScala() == 0 || incantamento.getCoefficienteScala() == grado.getCoefficiente());
                assertTrue(incantamento.getDannoBonusFisso() > 0 || incantamento.getCoefficienteScala() > 0);
            }
            assertEquals(ListinoPergamene.prezzo(md), pergamena.getCostoAcquisto());
        }
        assertEquals(EnumSet.of(TipoArtefatto.PERGAMENA, TipoArtefatto.GEMMA, TipoArtefatto.MONILE, TipoArtefatto.GINGILLO,
                TipoArtefatto.SIGILLO), tipi);
    }

    @Test
    void gliArtefattiNasconoIncantatiPiuSpessoAiLivelliAlti() {
        GeneratoreArtefatti generatore = new GeneratoreArtefattiTabelle(new Random(7));
        int incantatiLivello5 = contaIncantati(generatore, 5);
        int incantatiLivello10 = contaIncantati(generatore, 10);
        assertTrue(incantatiLivello5 > 0);
        assertTrue(incantatiLivello10 > incantatiLivello5);
        assertEquals(0, contaIncantati(generatore, 1));
    }

    @Test
    void gliIncantamentiInNascitaRispettanoIlLimiteEIlGrado() {
        GeneratoreArtefatti generatore = new GeneratoreArtefattiTabelle(new Random(8));
        for (int i = 0; i < GIRI; i++) {
            int livello = 1 + i % 10;
            Artefatto artefatto = generatore.generaArtefattoCasuale(livello);
            ArtefattoMD md = artefatto.getModelloDati();
            if (!artefatto.isIncantabile()) {
                assertTrue(md.getIncantamenti().isEmpty(), "Un " + artefatto.getTipo() + " non si incanta");
                continue;
            }
            // Al massimo 3 incantamenti, e almeno un posto libero nel limite (che conta anche i modificatori)
            if (!md.getIncantamenti().isEmpty()) {
                assertTrue(md.getIncantamenti().size() <= 3);
                assertTrue(md.getIncantamenti().size() + md.getModificatori().size() < artefatto.getEffettiMassimi());
            }
            GradoIncantamento grado = GradoIncantamento.perLivello(livello);
            for (Incantamento incantamento : md.getIncantamenti()) {
                assertTrue(incantamento.getDannoBonusFisso() == 0 || incantamento.getDannoBonusFisso() == grado.getBonusFisso());
            }
        }
    }

    @Test
    void unArtefattoIncantatoCostaDiPiu() {
        GeneratoreArtefatti generatore = new GeneratoreArtefattiTabelle(new Random(9));
        for (int i = 0; i < GIRI; i++) {
            Artefatto artefatto = generatore.generaArtefatto(TipoArtefatto.SPADA, 8);
            int costoBase = 5 + 5 * 8;
            if (artefatto.getIncantamenti().isEmpty()) {
                assertEquals(costoBase, artefatto.getCostoAcquisto());
            } else {
                assertTrue(artefatto.getCostoAcquisto() > costoBase);
            }
        }
    }

    @Test
    void unArtefattoIncantabileSuDieciERaroEMaiLeggendario() {
        GeneratoreArtefatti generatore = new GeneratoreArtefattiTabelle(new Random(10));
        int rari = 0;
        int incantabili = 0;
        for (int i = 0; i < 2000; i++) {
            Artefatto artefatto = generatore.generaArtefattoCasuale(5);
            assertNotEquals(TipoRaritaArtefatto.LEGGENDARIO, artefatto.getRarita());
            if (!artefatto.isIncantabile()) {
                assertEquals(TipoRaritaArtefatto.COMUNE, artefatto.getRarita());
                continue;
            }
            incantabili++;
            if (artefatto.getRarita() == TipoRaritaArtefatto.RARO) {
                rari++;
            }
        }
        double quota = (double) rari / incantabili;
        assertTrue(quota > 0.06 && quota < 0.14, "Quota di rari: " + quota);
        assertEquals(TipoRaritaArtefatto.COMUNE, generatore.generaIngrediente(5).getRarita());
    }

    private static int contaIncantati(GeneratoreArtefatti generatore, int livello) {
        int incantati = 0;
        for (int i = 0; i < GIRI; i++) {
            if (!generatore.generaArtefatto(TipoArtefatto.SPADA, livello).getIncantamenti().isEmpty()) {
                incantati++;
            }
        }
        return incantati;
    }

    @Test
    void sogliaDeiGradi() {
        assertEquals(GradoIncantamento.MINORE, GradoIncantamento.perLivello(1));
        assertEquals(GradoIncantamento.MINORE, GradoIncantamento.perLivello(3));
        assertEquals(GradoIncantamento.MEDIO, GradoIncantamento.perLivello(4));
        assertEquals(GradoIncantamento.MEDIO, GradoIncantamento.perLivello(7));
        assertEquals(GradoIncantamento.MAGGIORE, GradoIncantamento.perLivello(8));
        assertEquals(GradoIncantamento.MAGGIORE, GradoIncantamento.perLivello(10));
        assertEquals(GradoIncantamento.MAGGIORE, GradoIncantamento.perLivello(25));
    }

    @Test
    void prezziDelListino() {
        // Incantamento: 0,5 per punto di danno, fisso × livello + coefficiente × Intelligenza tipica (9,7)
        assertEquals(0.5 * (10 + 0.97), ListinoPergamene.prezzo(new Incantamento("prova", TipoDanno.ACIDO, 10, 0.1), 1), 0.0001);
        assertEquals(0.5 * (40 + 0.97), ListinoPergamene.prezzo(new Incantamento("prova", TipoDanno.ACIDO, 10, 0.1), 4), 0.0001);
        assertEquals(0.5 * 40, ListinoPergamene.prezzo(new Incantamento("prova", TipoDanno.ACIDO, 10, 0), 4), 0.0001);
        // +25% per un tipo di danno con effetti di stato
        assertEquals(0.5 * 40 * 1.25, ListinoPergamene.prezzo(new Incantamento("prova", TipoDanno.FUOCO, 10, 0), 4), 0.0001);
        // Modificatore fisso: 0,5 per ogni 1% del valore tipico, quindi +3 di Precisione (3,2) costa più di +3 di Forza (11,5)
        assertEquals(0.5 * 300 / 11.5, ListinoPergamene.prezzo(new ModificatoreAttributo(TipoAttributo.FORZA, TipoModificatore.AUMENTO_FISSO, 3)), 0.0001);
        assertEquals(0.5 * 300 / 3.2, ListinoPergamene.prezzo(new ModificatoreAttributo(TipoAttributo.PRECISIONE, TipoModificatore.AUMENTO_FISSO, 3)), 0.0001);
        // Percentuale: 0,5 per punto, qualunque sia l'attributo; assoluto 5 × q
        assertEquals(5, ListinoPergamene.prezzo(new ModificatoreAttributo(TipoAttributo.FORZA, TipoModificatore.AUMENTO_PERCENTUALE, 10)), 0.0001);
        assertEquals(5, ListinoPergamene.prezzo(new ModificatoreAttributo(TipoAttributo.PRECISIONE, TipoModificatore.AUMENTO_PERCENTUALE, 10)), 0.0001);
        assertEquals(20, ListinoPergamene.prezzo(new ModificatoreAttributo(TipoAttributo.FORZA, TipoModificatore.QUANTITA_ASSOLUTA, 4)), 0.0001);
    }
}
