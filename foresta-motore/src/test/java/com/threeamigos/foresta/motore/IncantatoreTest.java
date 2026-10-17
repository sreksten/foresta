package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.tipi.Costanti;
import com.threeamigos.foresta.modellodati.ArtefattoMD;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.modellodati.ModificatoreAttributoMD;
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
    void unArtefattoPienoSiIncantaSoloSeGliEffettiNuoviSonoMigliori() {
        // Una spada comune di livello 3 ha 2 posti, tutti e due presi
        Artefatto spada = artefatto(TipoArtefatto.SPADA, 3);
        spada.getModelloDati().addIncantamento("Fuoco minore", TipoDanno.FUOCO, 5, 0.05);
        spada.getModelloDati().addIncantamento("Gelo minore", TipoDanno.GELO, 5, 0.05);
        // Si può mettere sul banco, ma un effetto uguale a quelli che ha non lo migliora
        Artefatto pari = artefatto(TipoArtefatto.GEMMA, 1);
        pari.getModelloDati().addIncantamento("Acido minore", TipoDanno.ACIDO, 5, 0.05);
        List<Artefatto> banco = Arrays.asList(spada, pari);
        assertEquals(Optional.empty(), RegoleIncantatura.puoMettereSulBanco(Collections.singletonList(pari), spada));
        assertEquals(0, RegoleIncantatura.effettiDaTrasferire(banco));
        assertEquals(Optional.of(TipoMotivoRifiutoIncantatura.NESSUN_MIGLIORAMENTO), RegoleIncantatura.verifica(banco, 100));
    }

    @Test
    void unEffettoMiglioreScalzaSoloUnoDeiPiuDebboli() {
        // Given: una spada con due +5 Forza uguali e una pergamena con un +10 Forza
        Artefatto spada = artefatto(TipoArtefatto.SPADA, 3);
        spada.getModelloDati().addModificatore(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_FISSA, 5, "");
        spada.getModelloDati().addModificatore(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_FISSA, 5, "");
        Artefatto pergamena = artefatto(TipoArtefatto.PERGAMENA, 1);
        pergamena.getModelloDati().addModificatore(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_FISSA, 10, "");
        BancoDiLavoro banco = new BancoDiLavoro();
        banco.addArtefatto(spada);
        banco.addArtefatto(pergamena);
        assertTrue(RegoleIncantatura.avvisoEffettiPersi(banco.getInventario()).isPresent());
        assertEquals(1, RegoleIncantatura.effettiDaTrasferire(banco.getInventario()));
        assertEquals(2, RegoleIncantatura.effettiDopoLaFusione(banco.getInventario()));
        // When
        assertEquals(Optional.empty(), gruppo.incanta(banco, ""));
        // Then: il tetto resta 2, e un solo +5 è stato sostituito
        assertEquals(2, spada.getModificatori().size());
        assertEquals(1, spada.getModificatori().stream().filter(m -> m.getQuantita() == 10).count());
        assertEquals(1, spada.getModificatori().stream().filter(m -> m.getQuantita() == 5).count());
    }

    @Test
    void ilBancoDiceQualiEffettiEntranoEQualiEscono() {
        // Given: una spada con due +5 Forza e una pergamena con un +10 Forza
        Artefatto spada = artefatto(TipoArtefatto.SPADA, 3);
        spada.getModelloDati().addModificatore(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_FISSA, 5, "");
        spada.getModelloDati().addModificatore(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_FISSA, 5, "");
        Artefatto pergamena = artefatto(TipoArtefatto.PERGAMENA, 1);
        pergamena.getModelloDati().addModificatore(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_FISSA, 10, "");
        AutomaIncantatore incantatore = new AutomaIncantatore(gruppo, new BancoDiLavoro());
        gruppo.addArtefatto(spada);
        gruppo.addArtefatto(pergamena);
        assertFalse(incantatore.isEffettoInUscita(spada.getModificatori().iterator().next()));
        incantatore.richiediSpostamentoSuParteRemota(spada);
        incantatore.richiediSpostamentoSuParteRemota(pergamena);
        // Then: esce uno solo dei due +5 (per identità), entra il +10
        long inUscita = spada.getModificatori().stream().filter(incantatore::isEffettoInUscita).count();
        assertEquals(1, inUscita);
        assertTrue(incantatore.isEffettoInEntrata(pergamena.getModificatori().iterator().next()));
        assertEquals(2, incantatore.getEffettiDopoLaFusione());
        assertEquals(1, incantatore.getEffettiDaTrasferire());
        // Tolta la pergamena dal banco il piano si ricalcola
        incantatore.richiediSpostamentoSuParteAttiva(pergamena);
        assertEquals(0, spada.getModificatori().stream().filter(incantatore::isEffettoInUscita).count());
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
        assertEquals(Optional.of(TipoMotivoRifiutoIncantatura.NESSUN_INGREDIENTE),
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
        pergamena.getModelloDati().addModificatore(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_FISSA, 2, "");
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
        assertTrue(incantata.getModificatori().contains(new ModificatoreAttributoMD(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_FISSA, 2)));
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
        pergamena.getModelloDati().addModificatore(TipoAttributo.POTERE_MAGICO, TipoModificatore.VARIAZIONE_PERCENTUALE, 10, "");
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
        sigillo.getModelloDati().addModificatore(TipoAttributo.POTERE_MAGICO, TipoModificatore.VARIAZIONE_PERCENTUALE, 20, "");
        sigillo.getModelloDati().addModificatore(TipoAttributo.PARATA, TipoModificatore.VARIAZIONE_FISSA, 3, "");
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
        pergamena.getModelloDati().addModificatore(TipoAttributo.POTERE_MAGICO, TipoModificatore.VARIAZIONE_PERCENTUALE, 10, "");
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

    // --- Gli aggiornamenti: fanno salire di un livello l'artefatto, fino al livello del mondo

    private static void livelloDelMondo(int livello) {
        ModelloDati.getIstanza().getStatisticheMD().setLivello(livello);
    }

    @Test
    void unAggiornamentoDaSoloBastaAFarSalireDiLivelloLArtefatto() {
        livelloDelMondo(6);
        Artefatto spada = artefatto(TipoArtefatto.SPADA, 3);
        Artefatto aggiornamento = artefatto(TipoArtefatto.AGGIORNAMENTO, 1);
        gruppo.addArtefatto(spada);
        gruppo.addArtefatto(aggiornamento);
        AutomaIncantatore incantatore = new AutomaIncantatore(gruppo, new BancoDiLavoro());
        incantatore.richiediSpostamentoSuParteRemota(spada);
        incantatore.richiediSpostamentoSuParteRemota(aggiornamento);
        assertEquals(2, incantatore.getBanco().getInventario().size());
        assertEquals(4, incantatore.getLivelloDopoLaFusione());
        // Una spada comune ha un posto in meno del livello: da 2 a 3
        assertEquals(3, incantatore.getEffettiMassimiDopoLaFusione());
        assertEquals(0, incantatore.getEffettiDaTrasferire());
        assertEquals(Costanti.FUSIONE_COSTO_BASE, incantatore.getCostoFusione());

        assertEquals(Optional.empty(), gruppo.incanta(incantatore.getBanco(), ""));
        assertEquals(100 - Costanti.FUSIONE_COSTO_BASE, gruppo.getMonete());
        assertEquals(1, gruppo.getInventario().size(), "l'aggiornamento si consuma");
        assertEquals(4, gruppo.getInventario().iterator().next().getLivello());
    }

    @Test
    void ilLivelloNuovoValeGiaPerGliEffettiDellaStessaFusione() {
        livelloDelMondo(6);
        // Una spada comune di livello 2 ha un solo posto, già preso: il livello 3 ne dà uno in più per la pergamena
        Artefatto spada = artefatto(TipoArtefatto.SPADA, 2);
        spada.getModelloDati().addModificatore(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_FISSA, 5, "");
        Artefatto pergamena = artefatto(TipoArtefatto.PERGAMENA, 1);
        pergamena.getModelloDati().addModificatore(TipoAttributo.DESTREZZA, TipoModificatore.VARIAZIONE_FISSA, 5, "");
        Artefatto aggiornamento = artefatto(TipoArtefatto.AGGIORNAMENTO, 1);
        BancoDiLavoro banco = new BancoDiLavoro();
        banco.addArtefatto(spada);
        banco.addArtefatto(pergamena);
        assertEquals(1, RegoleIncantatura.effettiDopoLaFusione(banco.getInventario()), "senza l'aggiornamento non c'è posto");
        banco.addArtefatto(aggiornamento);
        assertEquals(2, RegoleIncantatura.effettiDopoLaFusione(banco.getInventario()));
        assertEquals(Optional.empty(), gruppo.incanta(banco, ""));
        assertEquals(3, spada.getLivello());
        assertEquals(2, spada.getModificatori().size());
    }

    @Test
    void siSaleAlMassimoFinoAlLivelloDelMondo() {
        livelloDelMondo(4);
        Artefatto spada = artefatto(TipoArtefatto.SPADA, 3);
        // Due aggiornamenti porterebbero la spada a 5, oltre il mondo
        List<Artefatto> banco = new ArrayList<>(Collections.singletonList(spada));
        assertEquals(Optional.empty(), RegoleIncantatura.puoMettereSulBanco(banco, artefatto(TipoArtefatto.AGGIORNAMENTO, 1)));
        banco.add(artefatto(TipoArtefatto.AGGIORNAMENTO, 1));
        assertEquals(Optional.of(TipoMotivoRifiutoIncantatura.LIVELLO_DEL_MONDO_RAGGIUNTO),
                RegoleIncantatura.puoMettereSulBanco(banco, artefatto(TipoArtefatto.AGGIORNAMENTO, 1)));
        // Già al livello del mondo: neanche uno
        Artefatto allaPari = artefatto(TipoArtefatto.ELMO, 4);
        assertEquals(Optional.of(TipoMotivoRifiutoIncantatura.LIVELLO_DEL_MONDO_RAGGIUNTO),
                RegoleIncantatura.puoMettereSulBanco(Collections.singletonList(allaPari), artefatto(TipoArtefatto.AGGIORNAMENTO, 1)));
        // L'aggiornamento già sul banco ferma anche l'artefatto che arriva dopo
        assertEquals(Optional.of(TipoMotivoRifiutoIncantatura.LIVELLO_DEL_MONDO_RAGGIUNTO),
                RegoleIncantatura.puoMettereSulBanco(Collections.singletonList(artefatto(TipoArtefatto.AGGIORNAMENTO, 1)), allaPari));
        // Un artefatto sopra il livello del mondo si incanta comunque con gli ingredienti di sempre
        livelloDelMondo(1);
        assertEquals(Optional.empty(), RegoleIncantatura.puoMettereSulBanco(Collections.singletonList(pergamena(1)), artefatto(TipoArtefatto.SPADA, 5)));
    }

    @Test
    void unAggiornamentoNonSiPuoMettereSuUnAccessorioNeSiCompraONonSiVende() {
        livelloDelMondo(6);
        assertEquals(Optional.of(TipoMotivoRifiutoIncantatura.NON_INCANTABILE),
                RegoleIncantatura.puoMettereSulBanco(Collections.singletonList(artefatto(TipoArtefatto.AGGIORNAMENTO, 1)),
                        artefatto(TipoArtefatto.ANELLO, 2)));
        for (TipoNegozio negozio : TipoNegozio.values()) {
            assertFalse(negozio.tratta(TipoArtefatto.AGGIORNAMENTO), negozio.name());
        }
        // Il venditore e l'armaiolo trattano ancora il resto
        assertTrue(TipoNegozio.VENDITORE_DI_PERGAMENE.tratta(TipoArtefatto.GEMMA));
        assertTrue(TipoNegozio.ARMAIOLO.tratta(TipoArtefatto.SPADA));
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
