package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.motore.modellodati.ArtefattoMD;
import com.threeamigos.foresta.motore.modellodati.ModificatoreAttributo;
import com.threeamigos.foresta.motore.modellodati.RaritaArtefatto;
import com.threeamigos.foresta.motore.modellodati.SupertipoArtefatto;
import com.threeamigos.foresta.motore.modellodati.SupertipoDanno;
import com.threeamigos.foresta.motore.modellodati.TipoArtefatto;
import com.threeamigos.foresta.motore.modellodati.TipoAttributo;
import com.threeamigos.foresta.motore.modellodati.TipoDanno;
import com.threeamigos.foresta.motore.modellodati.TipoModificatore;
import com.threeamigos.foresta.motore.ArmaNaturale;
import com.threeamigos.foresta.motore.modellodati.ModelloDati;
import com.threeamigos.foresta.personaggi.Guerriero;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeneratoreArtefattiTest {

    private static final int GIRI = 500;

    @Test
    void generaUnArtefattoDelTipoEDelLivelloChiesti() {
        GeneratoreArtefatti generatore = new GeneratoreArtefattiTabelle(new Random(1));
        for (TipoArtefatto tipo : TipoArtefatto.values()) {
            Artefatto artefatto = generatore.generaArtefatto(tipo, 4);
            assertEquals(tipo, artefatto.getTipo());
            // Le pergamene si fermano al livello 3
            assertEquals(tipo == TipoArtefatto.INCANTAMENTO ? 3 : 4, artefatto.getLivello());
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
        assertEquals(1, generatore.generaPergamena(-3).getLivello());
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
    void aiLivelliBassiLeArmiFannoAlmenoUndicePiuIlLivello() {
        assertEquals(12, GeneratoreArtefatti.danniMediArma(1));
        assertEquals(13, GeneratoreArtefatti.danniMediArma(2));
        assertEquals(16, GeneratoreArtefatti.danniMediArma(5));
        // Dal livello 7 vale di nuovo 4 + 2 × livello
        assertEquals(18, GeneratoreArtefatti.danniMediArma(7));
        assertEquals(24, GeneratoreArtefatti.danniMediArma(10));
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
    void lArtefattoCasualeNonEMaiUnaPergamena() {
        GeneratoreArtefatti generatore = new GeneratoreArtefattiTabelle(new Random(4));
        Set<TipoArtefatto> visti = EnumSet.noneOf(TipoArtefatto.class);
        for (int i = 0; i < GIRI; i++) {
            visti.add(generatore.generaArtefattoCasuale(3).getTipo());
        }
        assertFalse(visti.contains(TipoArtefatto.INCANTAMENTO));
        assertEquals(TipoArtefatto.values().length - 1, visti.size());
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
    void lePergameneHannoIncantamentiEModificatoriDelGradoGiusto() {
        GeneratoreArtefatti generatore = new GeneratoreArtefattiTabelle(new Random(6));
        boolean visteConIncantamenti = false;
        boolean visteConModificatori = false;
        boolean visteConEntrambi = false;
        for (int i = 0; i < GIRI; i++) {
            int livello = 1 + i % 10;
            GradoIncantamento grado = GradoIncantamento.perLivello(livello);
            Artefatto pergamena = generatore.generaPergamena(livello);
            ArtefattoMD md = pergamena.getModelloDati();
            assertEquals(TipoArtefatto.INCANTAMENTO, pergamena.getTipo());
            // Livello da 1 a 3, e tanti effetti quanto il livello
            assertEquals(Math.min(3, livello), pergamena.getLivello());
            int effetti = md.getIncantamenti().size() + md.getModificatori().size();
            assertEquals(pergamena.getLivello(), effetti);
            for (Incantamento incantamento : md.getIncantamenti()) {
                assertNotEquals(SupertipoDanno.FISICO, incantamento.getTipoDannoElementale().getSuperTipo());
                assertTrue(incantamento.getDannoBonusFisso() == 0 || incantamento.getDannoBonusFisso() == grado.getBonusFisso());
                assertTrue(incantamento.getCoefficienteScala() == 0 || incantamento.getCoefficienteScala() == grado.getCoefficiente());
                assertTrue(incantamento.getDannoBonusFisso() > 0 || incantamento.getCoefficienteScala() > 0);
            }
            assertEquals(ListinoPergamene.prezzo(md), pergamena.getCostoAcquisto());
            visteConIncantamenti |= !md.getIncantamenti().isEmpty();
            visteConModificatori |= !md.getModificatori().isEmpty();
            visteConEntrambi |= !md.getIncantamenti().isEmpty() && !md.getModificatori().isEmpty();
        }
        assertTrue(visteConIncantamenti);
        assertTrue(visteConModificatori);
        assertTrue(visteConEntrambi);
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
            assertNotEquals(RaritaArtefatto.LEGGENDARIO, artefatto.getRarita());
            if (!artefatto.isIncantabile()) {
                assertEquals(RaritaArtefatto.COMUNE, artefatto.getRarita());
                continue;
            }
            incantabili++;
            if (artefatto.getRarita() == RaritaArtefatto.RARO) {
                rari++;
            }
        }
        double quota = (double) rari / incantabili;
        assertTrue(quota > 0.06 && quota < 0.14, "Quota di rari: " + quota);
        assertEquals(RaritaArtefatto.COMUNE, generatore.generaPergamena(5).getRarita());
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
    void prezziDellaTabella() {
        // Un incantamento con entrambe le parti costa il prezzo base del grado (tipo senza effetti di stato)
        for (GradoIncantamento grado : GradoIncantamento.values()) {
            Incantamento incantamento = new Incantamento("prova", TipoDanno.ACIDO, grado.getBonusFisso(), grado.getCoefficiente());
            assertEquals(grado.getPrezzoBase(), ListinoPergamene.prezzo(incantamento), 0.0001);
        }
        // Medio con solo +10 costa 20, con solo +10% costa 10; +25% per il fuoco
        assertEquals(20, ListinoPergamene.prezzo(new Incantamento("prova", TipoDanno.ACIDO, 10, 0)), 0.0001);
        assertEquals(10, ListinoPergamene.prezzo(new Incantamento("prova", TipoDanno.ACIDO, 0, 0.1)), 0.0001);
        assertEquals(37.5, ListinoPergamene.prezzo(new Incantamento("prova", TipoDanno.FUOCO, 10, 0.1)), 0.0001);
        // Modificatori: fisso 2 × q, percentuale q, assoluto 5 × q
        assertEquals(6, ListinoPergamene.prezzo(new ModificatoreAttributo(TipoAttributo.FORZA, TipoModificatore.AUMENTO_FISSO, 3)), 0.0001);
        assertEquals(10, ListinoPergamene.prezzo(new ModificatoreAttributo(TipoAttributo.FORZA, TipoModificatore.AUMENTO_PERCENTUALE, 10)), 0.0001);
        assertEquals(20, ListinoPergamene.prezzo(new ModificatoreAttributo(TipoAttributo.FORZA, TipoModificatore.QUANTITA_ASSOLUTA, 4)), 0.0001);
    }
}
