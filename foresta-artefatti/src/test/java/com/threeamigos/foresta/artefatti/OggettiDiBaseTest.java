package com.threeamigos.foresta.artefatti;

import com.threeamigos.foresta.modellodati.ArtefattoMD;
import com.threeamigos.foresta.modellodati.IncantamentoMD;
import com.threeamigos.foresta.tipi.AspettoArtefatto;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoRaritaArtefatto;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.modellodati.ModificatoreAttributoMD;
import com.threeamigos.foresta.tipi.BersaglioModificatore;
import com.threeamigos.foresta.tipi.TipoModificatore;
import com.threeamigos.foresta.tipi.UnitaDurataModificatore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Artefatto, le sue sottoclassi e le interfacce che le descrivono.
 */
class OggettiDiBaseTest {

    private static ArtefattoMD md(TipoArtefatto tipo) {
        ArtefattoMD md = new ArtefattoMD();
        md.setTipo(tipo);
        md.setNome("la prova");
        md.setDescrizione("che serve");
        md.setLivello(4);
        md.setDanni(9);
        md.setCostoAcquisto(30);
        md.setPeso(2);
        return md;
    }

    @AfterEach
    void ripristina() {
        SpadaNormalmenteNascosta.seguiIlLivello(null);
    }

    @Test
    void ilModelloDatiSiRiconoscePerLaSottoclasse() {
        assertEquals(ArmaFisica.class, Artefatto.di(md(TipoArtefatto.SPADA)).getClass());
        assertEquals(Artefatto.class, Artefatto.di(md(TipoArtefatto.SCUDO)).getClass());
        ArtefattoMD nascosta = md(TipoArtefatto.SPADA);
        nascosta.setAspetto(AspettoArtefatto.SPADA_NORMALMENTE_NASCOSTA);
        assertEquals(SpadaNormalmenteNascosta.class, Artefatto.di(nascosta).getClass());
    }

    @Test
    void unaVistaDiArtefattoEUnArtefatto() {
        Artefatto scudo = Artefatto.di(md(TipoArtefatto.SCUDO));
        assertSame(scudo, Artefatto.da(scudo));
    }

    @Test
    void unArtefattoRispondeDalSuoModelloDati() {
        ArtefattoMD dati = md(TipoArtefatto.SCUDO);
        dati.setPezzoDiSetLeggendario("ROMY_JONA", "SPADA_DI_ROMY_JONA");
        dati.setAspetto(AspettoArtefatto.LIBRO_TECNICO);
        dati.setNomeProprio("Scudetto");
        Artefatto scudo = Artefatto.di(dati);
        assertEquals(dati.getUuid(), scudo.getUuid());
        assertEquals(TipoArtefatto.SCUDO, scudo.getTipo());
        assertEquals("la prova", scudo.getNome());
        assertEquals("che serve", scudo.getDescrizione());
        assertEquals(dati.getNomeBreve(), scudo.getNomeBreve());
        assertEquals(dati.getDescrizioneBreve(), scudo.getDescrizioneBreve());
        assertTrue(scudo.getDescrizioneBreve().startsWith("la prova, "));
        assertEquals(9, scudo.getDanni());
        assertEquals("SPADA_DI_ROMY_JONA", scudo.getPezzoLeggendario());
        assertEquals(AspettoArtefatto.LIBRO_TECNICO, scudo.getAspetto());
        assertTrue(scudo.getDescrizioneSet().isPresent());
        assertTrue(scudo.getTipiDelSet().isPresent());
        assertEquals("Scudetto", scudo.getNomeProprio().orElse(null));
        assertEquals(dati.getNomeCompleto(), scudo.getNomeCompleto());
        assertEquals(TipoRaritaArtefatto.COMUNE, scudo.getRarita());
        assertEquals(4, scudo.getLivello());
        assertEquals(30, scudo.getCostoAcquisto());
        assertEquals(2, scudo.getPeso(), 0.0001);
        assertSame(dati, scudo.getModelloDati());
    }

    @Test
    void ilNomeEDescrizioneDiUnoSenzaDescrizioneEIlNome() {
        ArtefattoMD dati = md(TipoArtefatto.SCUDO);
        dati.setNomeProprio("Scudetto");
        dati.setDescrizione("");
        assertEquals("la prova", dati.getDescrizioneBreve());
        dati.setDescrizione(null);
        assertEquals("la prova", dati.getDescrizioneBreve());
    }

    @Test
    void ifigliSiMostranoENascondono() {
        Artefatto scudo = Artefatto.di(md(TipoArtefatto.SCUDO));
        scudo.mostraFigli();
        assertTrue(scudo.isFigliVisibili());
        scudo.nascondiFigli();
        assertFalse(scudo.isFigliVisibili());
    }

    @Test
    void soloAlcuniArtefattiSiIncantano() {
        assertTrue(Artefatto.di(md(TipoArtefatto.SPADA)).isIncantabile());
        assertTrue(Artefatto.di(md(TipoArtefatto.LIBRO_MAGICO)).isIncantabile());
        assertFalse(Artefatto.di(md(TipoArtefatto.ANELLO)).isIncantabile());
        assertEquals(0, Artefatto.di(md(TipoArtefatto.ANELLO)).getEffettiMassimi());
        assertTrue(Artefatto.di(md(TipoArtefatto.SPADA)).getEffettiMassimi() > 0);
    }

    @Test
    void unArmaDaITipiDiDannoDelSuoTipoEDeiSuoiIncantamenti() {
        ArtefattoMD dati = md(TipoArtefatto.SPADA);
        ArmaFisica spada = (ArmaFisica) Artefatto.di(dati);
        assertEquals(9, spada.getDanni());
        assertEquals(TipoDanno.TAGLIENTE, spada.getTipoDanno());
        assertFalse(spada.isIncantata());
        assertTrue(spada.daTipoDanno(TipoDanno.TAGLIENTE));
        assertFalse(spada.daTipoDanno(TipoDanno.NECROTICO));
        dati.addIncantamento("Incantesimo", TipoDanno.NECROTICO, 3, 0.1);
        assertTrue(spada.isIncantata());
        assertEquals(1, spada.getIncantamenti().size());
        assertTrue(spada.daTipoDanno(TipoDanno.NECROTICO));
        assertFalse(spada.daTipoDanno(TipoDanno.GELO));
    }

    @Test
    void unArmaMetteSulBersaglioSoloIModificatoriDelBersaglio() {
        ArtefattoMD dati = md(TipoArtefatto.SPADA);
        dati.addModificatore(new ModificatoreAttributoMD(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_FISSA, 2));
        ModificatoreAttributoMD sulBersaglio = new ModificatoreAttributoMD(TipoAttributo.FORTUNA, TipoModificatore.QUANTITA_ASSOLUTA, 0, "",
                UnitaDurataModificatore.TURNI, 3, BersaglioModificatore.BERSAGLIO);
        dati.addModificatore(sulBersaglio);
        ArmaFisica spada = (ArmaFisica) Artefatto.di(dati);
        assertEquals(Collections.singletonList(sulBersaglio), spada.getModificatoriSulBersaglio());
    }

    @Test
    void unArmaQualsiasiNonMetteNienteSulBersaglio() {
        Arma arma = new Arma() {
            @Override
            public int getDanni() {
                return 1;
            }

            @Override
            public int getLivello() {
                return 1;
            }

            @Override
            public TipoDanno getTipoDanno() {
                return TipoDanno.CONTUNDENTE;
            }

            @Override
            public boolean isIncantata() {
                return false;
            }

            @Override
            public Collection<IncantamentoMD> getIncantamenti() {
                return Collections.emptyList();
            }
        };
        assertTrue(arma.getModificatoriSulBersaglio().isEmpty());
        assertTrue(arma.daTipoDanno(TipoDanno.CONTUNDENTE));
    }

    @Test
    void laSpadaNormalmenteNascostaSeguiIlLivello() {
        SpadaNormalmenteNascosta spada = SpadaNormalmenteNascosta.costruisci(new GeneratoreArtefattiTabelle(new Random(1)), 5);
        assertTrue(SpadaNormalmenteNascosta.isQuesta(spada.getModelloDati()));
        assertEquals(".spada normalmente nascosta", spada.getNome().substring(3));
        assertEquals(5, spada.getLivello());
        int danniNati = spada.getDanni();
        assertEquals(danniNati, spada.getModelloDati().getDanni());
        SpadaNormalmenteNascosta.seguiIlLivello(() -> 10);
        assertEquals(10, spada.getLivello());
        assertEquals(danniNati + GeneratoreArtefatti.danniMediArma(10) - GeneratoreArtefatti.danniMediArma(5), spada.getDanni());
        SpadaNormalmenteNascosta.seguiIlLivello(() -> 1);
        assertTrue(spada.getDanni() >= 1);
        SpadaNormalmenteNascosta.seguiIlLivello(null);
        assertEquals(5, spada.getLivello());
    }

    @Test
    void unaSpadaComuneNonESpadaNascosta() {
        assertFalse(SpadaNormalmenteNascosta.isQuesta(md(TipoArtefatto.SPADA)));
    }

    @Test
    void ilGeneratoreDelGiocoSiOttieneEPrecarica() {
        GeneratoreArtefatti.precarica();
        assertNotNull(GeneratoreArtefatti.istanza());
        assertSame(GeneratoreArtefatti.istanza(), GeneratoreArtefatti.istanza());
    }

    @Test
    void ilCostruttoreAcceptaModificatoriEIncantamentiInTuttiIModi() {
        Artefatto artefatto = CostruttoreArtefatto.istanza()
                .setTipo(TipoArtefatto.SPADA).setNome("la spada").setDescrizione("che serve").setLivello(2).setDanniBase(5)
                .setCostoAcquisto(10).setPeso(1)
                .setModificatore(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_FISSA, 2, "nota")
                .setModificatore(TipoAttributo.CORAGGIO, TipoModificatore.VARIAZIONE_FISSA, 1)
                .setIncantamento(new IncantamentoMD("Primo", TipoDanno.GELO, 1, 0.1))
                .setIncantamento("Secondo", TipoDanno.NECROTICO, 2, 0.2)
                .costruisci();
        assertEquals(2, artefatto.getModificatori().size());
        assertEquals(2, artefatto.getIncantamenti().size());
    }

    @Test
    void ilModificatoreScrittoInTesto() {
        ModificatoreAttributoMD percentuale = ModificatoreDelTesto.da("FORZA +100%");
        assertEquals(TipoModificatore.VARIAZIONE_PERCENTUALE, percentuale.getTipoModificatoreAttributo());
        assertEquals(100, percentuale.getQuantita(), 0.0001);
        assertEquals(BersaglioModificatore.PORTATORE, percentuale.getBersaglio());
        ModificatoreAttributoMD fisso = ModificatoreDelTesto.da("  VALORE +2 ");
        assertEquals(TipoModificatore.VARIAZIONE_FISSA, fisso.getTipoModificatoreAttributo());
        ModificatoreAttributoMD assoluto = ModificatoreDelTesto.da("FORTUNA =0 @BERSAGLIO 3TURNI");
        assertEquals(TipoModificatore.QUANTITA_ASSOLUTA, assoluto.getTipoModificatoreAttributo());
        assertEquals(BersaglioModificatore.BERSAGLIO, assoluto.getBersaglio());
        assertEquals(UnitaDurataModificatore.TURNI, assoluto.getUnitaDurata());
        assertEquals(3, assoluto.getDurata());
        ModificatoreAttributoMD locazioni = ModificatoreDelTesto.da("CORAGGIO -20% 3 locazioni");
        assertEquals(UnitaDurataModificatore.LOCAZIONI, locazioni.getUnitaDurata());
        assertEquals(UnitaDurataModificatore.TURNI, ModificatoreDelTesto.da("CORAGGIO +1 1 TURNO").getUnitaDurata());
    }

    @Test
    void unModificatoreScrittoMaleSiRifiuta() {
        assertThrows(IllegalArgumentException.class, () -> ModificatoreDelTesto.da("FORZA"));
        assertThrows(IllegalArgumentException.class, () -> ModificatoreDelTesto.da("FORZA +1 per sempre"));
        assertThrows(IllegalArgumentException.class, () -> ModificatoreDelTesto.da("BOH +1"));
    }

    @Test
    void lIngredienteDelCoreDumpPortaSoloQuelIncantamento() {
        IncantamentoMD incantamento = new IncantamentoMD("Fiamma", TipoDanno.FUOCO, 3, 0.1);
        for (int i = 0; i < 20; i++) {
            Artefatto ingrediente = IngredienteDelCoreDump.da(incantamento, i - 2);
            assertTrue(ingrediente.getNome().endsWith("del Core Dump"));
            assertTrue(ingrediente.getTipo().isIngrediente());
            assertEquals(Collections.singletonList(incantamento), ingrediente.getIncantamenti().stream().collect(java.util.stream.Collectors.toList()));
            assertTrue(ingrediente.getLivello() >= 1);
        }
    }

    @Test
    void unaSpadaNonEUnAttaccoMagico() {
        assertFalse(((Arma) Artefatto.di(md(TipoArtefatto.SPADA))).isMagica());
    }
}
