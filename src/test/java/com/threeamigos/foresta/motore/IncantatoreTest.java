package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.motore.modellodati.ArtefattoMD;
import com.threeamigos.foresta.motore.modellodati.ModelloDati;
import com.threeamigos.foresta.motore.modellodati.ModificatoreAttributo;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.tipi.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class IncantatoreTest {

    private GruppoGiocatore gruppo;

    @BeforeEach
    void nuovoGruppo() {
        ModelloDati.setIstanza(new ModelloDati());
        gruppo = new GruppoGiocatore();
        gruppo.subMonete(gruppo.getMonete());
        gruppo.addMonete(100);
    }

    // --- Cosa si può mettere sul banco

    @Test
    void gliAccessoriNonSiIncantano() {
        assertEquals(Optional.of(TipoMotivoRifiutoIncantatura.NON_INCANTABILE),
                RegoleIncantatura.puoMettereSulBanco(Collections.emptyList(), artefatto(TipoArtefatto.ANELLO, 5)));
    }

    @Test
    void unArtefattoAllaVolta() {
        List<Artefatto> banco = Collections.singletonList(artefatto(TipoArtefatto.SPADA, 5));
        assertEquals(Optional.of(TipoMotivoRifiutoIncantatura.PIU_ARTEFATTI),
                RegoleIncantatura.puoMettereSulBanco(banco, artefatto(TipoArtefatto.ELMO, 5)));
    }

    @Test
    void unArtefattoSenzaPostiLiberiNonRiceveNiente() {
        // Una spada comune di livello 3 ha 2 posti, tutti e due presi
        Artefatto spada = artefatto(TipoArtefatto.SPADA, 3);
        spada.getModelloDati().addIncantamento("Fuoco minore", TipoDanno.FUOCO, 5, 0.05);
        spada.getModelloDati().addIncantamento("Gelo minore", TipoDanno.GELO, 5, 0.05);
        assertEquals(Optional.of(TipoMotivoRifiutoIncantatura.LIMITE_SUPERATO),
                RegoleIncantatura.puoMettereSulBanco(Collections.singletonList(spada), pergamena(1)));
        assertEquals(Optional.of(TipoMotivoRifiutoIncantatura.LIMITE_SUPERATO),
                RegoleIncantatura.puoMettereSulBanco(Collections.singletonList(pergamena(1)), spada));
    }

    @Test
    void conPiuEffettiChePostiPassanoIMiglioriEIlRestoSiPerde() {
        // Given: una spada comune di livello 3 (2 posti, uno preso) e un ingrediente con due incantamenti
        Artefatto spada = artefatto(TipoArtefatto.SPADA, 3);
        spada.getModelloDati().addIncantamento("Fuoco minore", TipoDanno.FUOCO, 5, 0.05);
        Artefatto gemma = artefatto(TipoArtefatto.GEMMA, 4);
        gemma.getModelloDati().addIncantamento("Acido minore", TipoDanno.ACIDO, 5, 0.05);
        gemma.getModelloDati().addIncantamento("Gelo maggiore", TipoDanno.GELO, 15, 0.2);
        BancoDiLavoro banco = new BancoDiLavoro();
        banco.addArtefatto(spada);
        assertEquals(Optional.empty(), RegoleIncantatura.puoMettereSulBanco(banco.getInventario(), gemma));
        banco.addArtefatto(gemma);
        assertTrue(RegoleIncantatura.avvisoEffettiPersi(banco.getInventario()).isPresent());
        // Si paga solo l'effetto che passa
        assertEquals(1, RegoleIncantatura.effettiDaTrasferire(banco.getInventario()));
        assertEquals(Costanti.FUSIONE_COSTO_BASE + Costanti.FUSIONE_COSTO_PER_EFFETTO, RegoleIncantatura.costo(banco.getInventario()));
        // When
        assertEquals(Optional.empty(), gruppo.incanta(banco, ""));
        // Then: passa il più prezioso, il gelo maggiore
        assertEquals(2, spada.getIncantamenti().size());
        assertTrue(spada.getIncantamenti().stream().anyMatch(i -> i.getTipoDannoElementale() == TipoDanno.GELO));
        assertFalse(spada.getIncantamenti().stream().anyMatch(i -> i.getTipoDannoElementale() == TipoDanno.ACIDO));
    }

    @Test
    void conGliIngredientiGiaSulBancoSiControllaLArtefattoCheArriva() {
        // Prima gli ingredienti (3 effetti), poi una spada comune di livello 3 (2 posti): ne passano 2
        List<Artefatto> banco = new ArrayList<>(Arrays.asList(pergamena(2), pergamena(1)));
        assertEquals(Optional.empty(), RegoleIncantatura.puoMettereSulBanco(banco, artefatto(TipoArtefatto.SPADA, 3)));
        banco.add(artefatto(TipoArtefatto.SPADA, 3));
        assertEquals(2, RegoleIncantatura.effettiDaTrasferire(banco));
        assertTrue(RegoleIncantatura.avvisoEffettiPersi(banco).isPresent());
        banco.set(2, artefatto(TipoArtefatto.SPADA, 4));
        assertEquals(3, RegoleIncantatura.effettiDaTrasferire(banco));
        assertFalse(RegoleIncantatura.avvisoEffettiPersi(banco).isPresent());
    }

    @Test
    void ilTettoDeiLeggendariECinque() {
        Artefatto spada = artefatto(TipoArtefatto.SPADA, 20);
        spada.getModelloDati().setRarita(TipoRaritaArtefatto.LEGGENDARIO);
        List<Artefatto> banco = new ArrayList<>(Arrays.asList(spada, pergamena(3), pergamena(2)));
        assertEquals(Optional.empty(), RegoleIncantatura.verifica(banco, 1000));
        assertFalse(RegoleIncantatura.avvisoEffettiPersi(banco).isPresent());
        banco.add(pergamena(1));
        assertEquals(5, RegoleIncantatura.effettiDaTrasferire(banco));
        assertTrue(RegoleIncantatura.avvisoEffettiPersi(banco).isPresent());
    }

    // --- Verifica della fusione

    @Test
    void rifiutiDellaFusione() {
        assertEquals(Optional.of(TipoMotivoRifiutoIncantatura.NESSUN_ARTEFATTO),
                RegoleIncantatura.verifica(Collections.singletonList(pergamena(1)), 100));
        assertEquals(Optional.of(TipoMotivoRifiutoIncantatura.NESSUNA_PERGAMENA),
                RegoleIncantatura.verifica(Collections.singletonList(artefatto(TipoArtefatto.SPADA, 4)), 100));
        assertEquals(Optional.of(TipoMotivoRifiutoIncantatura.PIU_ARTEFATTI),
                RegoleIncantatura.verifica(Arrays.asList(artefatto(TipoArtefatto.SPADA, 4), artefatto(TipoArtefatto.ELMO, 4), pergamena(1)), 100));
        // 10 + 5 × 2 = 20 monete
        List<Artefatto> banco = Arrays.asList(artefatto(TipoArtefatto.SPADA, 4), pergamena(2));
        assertEquals(20, RegoleIncantatura.costo(banco));
        assertEquals(Optional.of(TipoMotivoRifiutoIncantatura.MONETE_INSUFFICIENTI), RegoleIncantatura.verifica(banco, 19));
        assertEquals(Optional.empty(), RegoleIncantatura.verifica(banco, 20));
    }

    // --- Fusione

    @Test
    void fusioneRiuscita() {
        // Given: una spada di livello 4 (3 posti) e una pergamena con un incantamento e un modificatore
        Artefatto spada = artefatto(TipoArtefatto.SPADA, 4);
        Artefatto pergamena = pergamena(0);
        pergamena.getModelloDati().addIncantamento("Gelo medio", TipoDanno.GELO, 10, 0.1);
        pergamena.getModelloDati().addModificatore(TipoAttributo.FORZA, TipoModificatore.AUMENTO_FISSO, 2, "");
        gruppo.addArtefatto(spada);
        gruppo.addArtefatto(pergamena);
        AutomaIncantatore incantatore = new AutomaIncantatore(gruppo, new BancoDiLavoro());
        incantatore.richiediSpostamentoSuParteRemota(spada);
        incantatore.richiediSpostamentoSuParteRemota(pergamena);
        assertTrue(gruppo.getInventario().isEmpty());
        // When
        Optional<TipoMotivoRifiutoIncantatura> esito = gruppo.incanta(incantatore.getBanco(), "lama del drago");
        // Then
        assertEquals(Optional.empty(), esito);
        assertEquals(100 - 20, gruppo.getMonete());
        assertTrue(incantatore.getBanco().getInventario().isEmpty());
        assertEquals(1, gruppo.getInventario().size());
        Artefatto incantata = gruppo.getInventario().iterator().next();
        assertEquals(TipoArtefatto.SPADA, incantata.getTipo());
        assertEquals("Lama Del Drago", incantata.getNomeProprio().orElse(null));
        assertEquals(1, incantata.getIncantamenti().size());
        assertTrue(incantata.getModificatori().contains(new ModificatoreAttributo(TipoAttributo.FORZA, TipoModificatore.AUMENTO_FISSO, 2)));
    }

    @Test
    void conIlNomeVuotoLArtefattoNonHaNomeProprio() {
        Artefatto spada = artefatto(TipoArtefatto.SPADA, 4);
        spada.getModelloDati().setNomeProprio("Diavolina");
        BancoDiLavoro banco = new BancoDiLavoro();
        banco.addArtefatto(spada);
        banco.addArtefatto(pergamena(1));
        assertEquals(Optional.empty(), gruppo.incanta(banco, ""));
        assertNull(gruppo.getInventario().iterator().next().getModelloDati().getNomeProprio());
    }

    @Test
    void unaFusioneRifiutataNonToccaNulla() {
        gruppo.subMonete(gruppo.getMonete());
        BancoDiLavoro banco = new BancoDiLavoro();
        banco.addArtefatto(artefatto(TipoArtefatto.SPADA, 4));
        banco.addArtefatto(pergamena(1));
        assertEquals(Optional.of(TipoMotivoRifiutoIncantatura.MONETE_INSUFFICIENTI), gruppo.incanta(banco, "Diavolina"));
        assertEquals(2, banco.getInventario().size());
        assertTrue(gruppo.getInventario().isEmpty());
    }

    // --- Banco

    @Test
    void ilBancoRifiutaEUscendoTornaTuttoNelGruppo() {
        Artefatto anello = artefatto(TipoArtefatto.ANELLO, 3);
        Artefatto spada = artefatto(TipoArtefatto.SPADA, 4);
        Artefatto pergamena = pergamena(1);
        gruppo.addArtefatto(anello);
        gruppo.addArtefatto(spada);
        gruppo.addArtefatto(pergamena);
        AutomaIncantatore incantatore = new AutomaIncantatore(gruppo, new BancoDiLavoro());
        // L'anello non va sul banco
        incantatore.richiediSpostamentoSuParteRemota(anello);
        assertEquals(3, gruppo.getInventario().size());
        incantatore.richiediSpostamentoSuParteRemota(spada);
        incantatore.richiediSpostamentoSuParteRemota(pergamena);
        assertEquals(2, incantatore.getBanco().getInventario().size());
        // Dal banco si torna al gruppo liberamente
        incantatore.richiediSpostamentoSuParteAttiva(pergamena);
        assertEquals(1, incantatore.getBanco().getInventario().size());
        // Uscendo, il banco si svuota nel gruppo
        incantatore.svuotaBanco();
        assertTrue(incantatore.getBanco().getInventario().isEmpty());
        assertEquals(3, gruppo.getInventario().size());
    }

    @Test
    void ilPotereMagicoSiFondeSoloSuBastoniELibriMagici() {
        Artefatto pergamena = artefatto(TipoArtefatto.PERGAMENA, 1);
        pergamena.getModelloDati().addModificatore(TipoAttributo.POTERE_MAGICO, TipoModificatore.AUMENTO_PERCENTUALE, 10, "");
        List<Artefatto> banco = Collections.singletonList(pergamena);
        assertEquals(Optional.of(TipoMotivoRifiutoIncantatura.POTERE_MAGICO_FUORI_POSTO),
                RegoleIncantatura.puoMettereSulBanco(banco, artefatto(TipoArtefatto.SPADA, 5)));
        assertEquals(Optional.empty(), RegoleIncantatura.puoMettereSulBanco(banco, artefatto(TipoArtefatto.BASTONE_MAGICO, 5)));
        assertEquals(Optional.empty(), RegoleIncantatura.puoMettereSulBanco(banco, artefatto(TipoArtefatto.LIBRO_MAGICO, 5)));
    }

    @Test
    void suUnaSpadaIlPotereMagicoSiPerdeEPassaIlResto() {
        Artefatto spada = artefatto(TipoArtefatto.SPADA, 5);
        Artefatto sigillo = artefatto(TipoArtefatto.SIGILLO, 4);
        sigillo.getModelloDati().addModificatore(TipoAttributo.POTERE_MAGICO, TipoModificatore.AUMENTO_PERCENTUALE, 20, "");
        sigillo.getModelloDati().addModificatore(TipoAttributo.PARATA, TipoModificatore.AUMENTO_FISSO, 3, "");
        BancoDiLavoro banco = new BancoDiLavoro();
        banco.addArtefatto(spada);
        assertEquals(Optional.empty(), RegoleIncantatura.puoMettereSulBanco(banco.getInventario(), sigillo));
        banco.addArtefatto(sigillo);
        assertEquals(Optional.of("Il potere magico si fonde solo su bastoni e libri magici: passeranno solo gli altri effetti."),
                RegoleIncantatura.avvisoEffettiPersi(banco.getInventario()));
        assertEquals(Optional.empty(), gruppo.incanta(banco, ""));
        assertEquals(1, spada.getModificatori().size());
        assertEquals(TipoAttributo.PARATA, spada.getModificatori().iterator().next().getTipoAttributo());
    }

    @Test
    void suUnLibroMagicoUnaPergamenaConSoliIncantamentiElementaliNonServe() {
        assertEquals(Optional.of(TipoMotivoRifiutoIncantatura.INCANTAMENTO_SU_LIBRO),
                RegoleIncantatura.puoMettereSulBanco(Collections.singletonList(pergamena(1)), artefatto(TipoArtefatto.LIBRO_MAGICO, 5)));
    }

    @Test
    void suUnLibroMagicoPassaSoloIlModificatoreEGliIncantamentiSiPerdono() {
        // Given: una pergamena con un incantamento elementale e un modificatore di POTERE_MAGICO
        Artefatto libro = artefatto(TipoArtefatto.LIBRO_MAGICO, 5);
        Artefatto pergamena = pergamena(1);
        pergamena.getModelloDati().addModificatore(TipoAttributo.POTERE_MAGICO, TipoModificatore.AUMENTO_PERCENTUALE, 10, "");
        BancoDiLavoro banco = new BancoDiLavoro();
        banco.addArtefatto(libro);
        banco.addArtefatto(pergamena);
        assertEquals(Optional.of("Sul libro gli incantamenti elementali non hanno effetto: passeranno solo gli altri effetti."),
                RegoleIncantatura.avvisoEffettiPersi(banco.getInventario()));
        // Si paga solo l'effetto che passa
        assertEquals(Costanti.FUSIONE_COSTO_BASE + Costanti.FUSIONE_COSTO_PER_EFFETTO, RegoleIncantatura.costo(banco.getInventario()));
        // When
        assertEquals(Optional.empty(), gruppo.incanta(banco, ""));
        // Then
        assertTrue(libro.getIncantamenti().isEmpty());
        assertTrue(libro.getModificatori().stream().anyMatch(m -> m.getTipoAttributo() == TipoAttributo.POTERE_MAGICO));
    }

    private static Artefatto artefatto(TipoArtefatto tipo, int livello) {
        ArtefattoMD md = new ArtefattoMD();
        md.setTipo(tipo);
        md.setNome("l'oggetto di prova");
        md.setDescrizione("che serve ai test");
        md.setLivello(livello);
        md.setPeso(1);
        return Artefatto.di(md);
    }

    private static Artefatto pergamena(int effetti) {
        Artefatto pergamena = artefatto(TipoArtefatto.PERGAMENA, 1);
        for (int i = 0; i < effetti; i++) {
            pergamena.getModelloDati().addIncantamento("Fuoco minore", TipoDanno.FUOCO, 5, 0.05);
        }
        return pergamena;
    }
}
