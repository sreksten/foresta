package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.artefatti.RegoleIncantatura;

import com.threeamigos.foresta.artefatti.CostantiArtefatti;

import com.threeamigos.foresta.artefatti.BancoDiLavoro;

import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.artefatti.ArtefattoMD;
import com.threeamigos.foresta.artefatti.tipi.TipoArtefatto;
import com.threeamigos.foresta.artefatti.tipi.TipoRaritaArtefatto;
import com.threeamigos.foresta.attributi.tipi.TipoAttributo;
import com.threeamigos.foresta.danni.tipi.TipoDanno;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.modificatori.ModificatoreAttributoMD;
import com.threeamigos.foresta.modificatori.tipi.TipoModificatore;
import com.threeamigos.foresta.tipi.Costanti;
import com.threeamigos.foresta.artefatti.tipi.TipoMotivoRifiutoIncantatura;
import com.threeamigos.foresta.artefatti.tipi.TipoNegozio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class IncantatoreTest {

    // Le regole non sanno cos'è il mondo né lo sconto: i test le chiamano come le chiama il gioco, con il livello del mondo e senza sconto
    private static java.util.Optional<TipoMotivoRifiutoIncantatura> puoMettereSulBanco(java.util.Collection<Artefatto> banco, Artefatto nuovo) {
        return RegoleIncantatura.puoMettereSulBanco(banco, nuovo, Statistiche.getLivello());
    }

    private static java.util.Optional<String> avviso(java.util.Collection<Artefatto> banco) {
        return RegoleIncantatura.avvisoEffettiPersi(banco).map(TestoAvvisoDiFusione::testo);
    }

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
        assertTrue(avviso(banco.getInventario()).isPresent());
        assertEquals(1, RegoleIncantatura.effettiDaTrasferire(banco.getInventario()));
        assertEquals(2, RegoleIncantatura.effettiDopoLaFusione(banco.getInventario()));
        // When
        assertEquals(Optional.empty(), CommercioDelGruppo.incanta(gruppo, banco, ""));
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
        assertEquals(Optional.empty(), puoMettereSulBanco(banco.getInventario(), gemma));
        banco.addArtefatto(gemma);
        assertTrue(avviso(banco.getInventario()).isPresent());
        // Si paga solo l'effetto che passa
        assertEquals(1, RegoleIncantatura.effettiDaTrasferire(banco.getInventario()));
        assertEquals(CostantiArtefatti.FUSIONE_COSTO_BASE + CostantiArtefatti.FUSIONE_COSTO_PER_EFFETTO, RegoleIncantatura.costo(banco.getInventario()));
        // When
        assertEquals(Optional.empty(), CommercioDelGruppo.incanta(gruppo, banco, ""));
        // Then: passa il più prezioso, il gelo maggiore
        assertEquals(2, spada.getIncantamenti().size());
        assertTrue(spada.getIncantamenti().stream().anyMatch(i -> i.getTipoDannoElementale() == TipoDanno.GELO));
        assertFalse(spada.getIncantamenti().stream().anyMatch(i -> i.getTipoDannoElementale() == TipoDanno.ACIDO));
    }

    // --- Verifica della fusione

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
        Optional<TipoMotivoRifiutoIncantatura> esito = CommercioDelGruppo.incanta(gruppo, incantatore.getBanco(), "lama del drago");
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
        assertEquals(Optional.empty(), CommercioDelGruppo.incanta(gruppo, banco, ""));
        assertNull(gruppo.getInventario().iterator().next().getModelloDati().getNomeProprio());
    }

    @Test
    void unaFusioneRifiutataNonToccaNulla() {
        gruppo.subMonete(gruppo.getMonete());
        BancoDiLavoro banco = new BancoDiLavoro();
        banco.addArtefatto(artefatto(TipoArtefatto.SPADA, 4));
        banco.addArtefatto(pergamena(1));
        assertEquals(Optional.of(TipoMotivoRifiutoIncantatura.MONETE_INSUFFICIENTI), CommercioDelGruppo.incanta(gruppo, banco, "Diavolina"));
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
    void suUnaSpadaIlPotereMagicoSiPerdeEPassaIlResto() {
        Artefatto spada = artefatto(TipoArtefatto.SPADA, 5);
        Artefatto sigillo = artefatto(TipoArtefatto.SIGILLO, 4);
        sigillo.getModelloDati().addModificatore(TipoAttributo.POTERE_MAGICO, TipoModificatore.VARIAZIONE_PERCENTUALE, 20, "");
        sigillo.getModelloDati().addModificatore(TipoAttributo.PARATA, TipoModificatore.VARIAZIONE_FISSA, 3, "");
        BancoDiLavoro banco = new BancoDiLavoro();
        banco.addArtefatto(spada);
        assertEquals(Optional.empty(), puoMettereSulBanco(banco.getInventario(), sigillo));
        banco.addArtefatto(sigillo);
        assertEquals(Optional.of("Il potere magico si fonde solo su bastoni e libri magici: passeranno solo gli altri effetti."),
                avviso(banco.getInventario()));
        assertEquals(Optional.empty(), CommercioDelGruppo.incanta(gruppo, banco, ""));
        assertEquals(1, spada.getModificatori().size());
        assertEquals(TipoAttributo.PARATA, spada.getModificatori().iterator().next().getTipoAttributo());
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
                avviso(banco.getInventario()));
        // Si paga solo l'effetto che passa
        assertEquals(CostantiArtefatti.FUSIONE_COSTO_BASE + CostantiArtefatti.FUSIONE_COSTO_PER_EFFETTO, RegoleIncantatura.costo(banco.getInventario()));
        // When
        assertEquals(Optional.empty(), CommercioDelGruppo.incanta(gruppo, banco, ""));
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
        assertEquals(CostantiArtefatti.FUSIONE_COSTO_BASE, incantatore.getCostoFusione());

        assertEquals(Optional.empty(), CommercioDelGruppo.incanta(gruppo, incantatore.getBanco(), ""));
        assertEquals(100 - CostantiArtefatti.FUSIONE_COSTO_BASE, gruppo.getMonete());
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
        assertEquals(Optional.empty(), CommercioDelGruppo.incanta(gruppo, banco, ""));
        assertEquals(3, spada.getLivello());
        assertEquals(2, spada.getModificatori().size());
    }

    @Test
    void unAggiornamentoNonSiPuoMettereSuUnAccessorioNeSiCompraONonSiVende() {
        livelloDelMondo(6);
        assertEquals(Optional.of(TipoMotivoRifiutoIncantatura.NON_INCANTABILE),
                puoMettereSulBanco(Collections.singletonList(artefatto(TipoArtefatto.AGGIORNAMENTO, 1)),
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
