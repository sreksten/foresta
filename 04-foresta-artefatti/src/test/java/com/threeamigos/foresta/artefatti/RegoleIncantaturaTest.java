package com.threeamigos.foresta.artefatti;

import com.threeamigos.foresta.modellodati.ArtefattoMD;
import com.threeamigos.foresta.modellodati.IncantamentoMD;
import com.threeamigos.foresta.artefatti.leggendari.LibroTecnico;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoMotivoRifiutoIncantatura;
import com.threeamigos.foresta.tipi.TipoRaritaArtefatto;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.modellodati.ModificatoreAttributoMD;
import com.threeamigos.foresta.tipi.BersaglioModificatore;
import com.threeamigos.foresta.tipi.TipoModificatore;
import com.threeamigos.foresta.tipi.UnitaDurataModificatore;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le regole della fusione dall'incantatore: che cosa si può mettere sul banco, quando la fusione si può fare, che cosa ne uscirebbe
 * e che cosa succede all'artefatto quando si fa. Il mondo (il livello massimo, lo sconto, le monete del gruppo) arriva come parametro.
 */
class RegoleIncantaturaTest {

    private static final int LIVELLO_DEL_MONDO = 10;
    private static final String K_AND_R = "THE_C_PROGRAMMING_LANGUAGE";

    private static Optional<TipoMotivoRifiutoIncantatura> puoMettereSulBanco(Collection<Artefatto> banco, Artefatto nuovo) {
        return RegoleIncantatura.puoMettereSulBanco(banco, nuovo, LIVELLO_DEL_MONDO);
    }

    private static Optional<TipoMotivoRifiutoIncantatura> verifica(Collection<Artefatto> banco, int monete) {
        return RegoleIncantatura.verifica(banco, monete, RegoleIncantatura.costo(banco), LIVELLO_DEL_MONDO);
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

    private static Artefatto spadaCon(int incantamenti) {
        Artefatto spada = artefatto(TipoArtefatto.SPADA, 5);
        spada.getModelloDati().setDanni(10);
        TipoDanno[] danni = {TipoDanno.FUOCO, TipoDanno.GELO, TipoDanno.ACIDO};
        for (int i = 0; i < incantamenti; i++) {
            spada.getModelloDati().addIncantamento("Incantamento " + i, danni[i], 5 + i, 0.05);
        }
        return spada;
    }

    private static Artefatto libro() {
        return LibroTecnico.getLibro(K_AND_R).orElseThrow(AssertionError::new)
                .costruisci(new GeneratoreArtefattiTabelle(new Random(1)), 4);
    }

    private static BancoDiLavoro banco(Artefatto... artefatti) {
        BancoDiLavoro banco = new BancoDiLavoro();
        Arrays.stream(artefatti).forEach(banco::addArtefatto);
        return banco;
    }

    private static Optional<AvvisoDiFusione.Perdita> perdita(Collection<Artefatto> banco) {
        return RegoleIncantatura.avvisoEffettiPersi(banco).map(AvvisoDiFusione::getPerdita);
    }

    // --- Cosa si può mettere sul banco

    @Test
    void gliAccessoriNonSiIncantano() {
        assertEquals(Optional.of(TipoMotivoRifiutoIncantatura.NON_INCANTABILE),
                puoMettereSulBanco(Collections.emptyList(), artefatto(TipoArtefatto.ANELLO, 5)));
    }

    @Test
    void unArtefattoAllaVolta() {
        List<Artefatto> banco = Collections.singletonList(artefatto(TipoArtefatto.SPADA, 5));
        assertEquals(Optional.of(TipoMotivoRifiutoIncantatura.PIU_ARTEFATTI),
                puoMettereSulBanco(banco, artefatto(TipoArtefatto.ELMO, 5)));
    }

    @Test
    void conGliIngredientiGiaSulBancoSiControllaLArtefattoCheArriva() {
        // Prima gli ingredienti (3 effetti), poi una spada comune di livello 3 (2 posti): ne passano 2
        List<Artefatto> banco = new ArrayList<>(Arrays.asList(pergamena(2), pergamena(1)));
        assertEquals(Optional.empty(), puoMettereSulBanco(banco, artefatto(TipoArtefatto.SPADA, 3)));
        banco.add(artefatto(TipoArtefatto.SPADA, 3));
        assertEquals(2, RegoleIncantatura.effettiDaTrasferire(banco));
        assertEquals(Optional.of(AvvisoDiFusione.Perdita.LIMITE_DI_EFFETTI), perdita(banco));
        banco.set(2, artefatto(TipoArtefatto.SPADA, 4));
        assertEquals(3, RegoleIncantatura.effettiDaTrasferire(banco));
        assertFalse(RegoleIncantatura.avvisoEffettiPersi(banco).isPresent());
    }

    @Test
    void ilPotereMagicoSiFondeSoloSuBastoniELibriMagici() {
        Artefatto pergamena = artefatto(TipoArtefatto.PERGAMENA, 1);
        pergamena.getModelloDati().addModificatore(TipoAttributo.POTERE_MAGICO, TipoModificatore.VARIAZIONE_PERCENTUALE, 10, "");
        List<Artefatto> banco = Collections.singletonList(pergamena);
        assertEquals(Optional.of(TipoMotivoRifiutoIncantatura.POTERE_MAGICO_FUORI_POSTO),
                puoMettereSulBanco(banco, artefatto(TipoArtefatto.SPADA, 5)));
        assertEquals(Optional.empty(), puoMettereSulBanco(banco, artefatto(TipoArtefatto.BASTONE_MAGICO, 5)));
        assertEquals(Optional.empty(), puoMettereSulBanco(banco, artefatto(TipoArtefatto.LIBRO_MAGICO, 5)));
    }

    @Test
    void suUnLibroMagicoUnaPergamenaConSoliIncantamentiElementaliNonServe() {
        assertEquals(Optional.of(TipoMotivoRifiutoIncantatura.INCANTAMENTO_SU_LIBRO),
                puoMettereSulBanco(Collections.singletonList(pergamena(1)), artefatto(TipoArtefatto.LIBRO_MAGICO, 5)));
    }

    @Test
    void siSaleAlMassimoFinoAlLivelloDelMondo() {
        Artefatto spada = artefatto(TipoArtefatto.SPADA, LIVELLO_DEL_MONDO - 1);
        // Il primo aggiornamento porta la spada al livello del mondo, il secondo lo supererebbe
        List<Artefatto> banco = new ArrayList<>(Collections.singletonList(spada));
        assertEquals(Optional.empty(), puoMettereSulBanco(banco, artefatto(TipoArtefatto.AGGIORNAMENTO, 1)));
        banco.add(artefatto(TipoArtefatto.AGGIORNAMENTO, 1));
        assertEquals(Optional.of(TipoMotivoRifiutoIncantatura.LIVELLO_DEL_MONDO_RAGGIUNTO),
                puoMettereSulBanco(banco, artefatto(TipoArtefatto.AGGIORNAMENTO, 1)));
        // Già al livello del mondo: neanche uno
        Artefatto allaPari = artefatto(TipoArtefatto.ELMO, LIVELLO_DEL_MONDO);
        assertEquals(Optional.of(TipoMotivoRifiutoIncantatura.LIVELLO_DEL_MONDO_RAGGIUNTO),
                puoMettereSulBanco(Collections.singletonList(artefatto(TipoArtefatto.AGGIORNAMENTO, 1)), allaPari));
        assertEquals(Optional.of(TipoMotivoRifiutoIncantatura.LIVELLO_DEL_MONDO_RAGGIUNTO),
                verifica(Arrays.asList(allaPari, artefatto(TipoArtefatto.AGGIORNAMENTO, 1)), 1000));
        // Un artefatto sopra il livello del mondo si incanta comunque con gli ingredienti di sempre
        assertEquals(Optional.empty(), RegoleIncantatura.puoMettereSulBanco(Collections.singletonList(pergamena(1)),
                artefatto(TipoArtefatto.SPADA, 5), 1));
    }

    @Test
    void unAggiornamentoNonSiMetteSuUnAccessorio() {
        assertEquals(Optional.of(TipoMotivoRifiutoIncantatura.NON_INCANTABILE),
                puoMettereSulBanco(Collections.singletonList(artefatto(TipoArtefatto.AGGIORNAMENTO, 1)),
                        artefatto(TipoArtefatto.ANELLO, 2)));
    }

    @Test
    void conIlLibroChe_riprogrammaCiStaQualunqueIngrediente() {
        Artefatto pergamenaDiPotereMagico = artefatto(TipoArtefatto.PERGAMENA, 1);
        pergamenaDiPotereMagico.getModelloDati().addModificatore(TipoAttributo.POTERE_MAGICO, TipoModificatore.VARIAZIONE_FISSA, 5, "");
        Artefatto spada = spadaCon(2);
        // Senza il libro quella pergamena su una spada non avrebbe effetto e sarebbe rifiutata
        assertTrue(puoMettereSulBanco(Collections.singletonList(spada), pergamenaDiPotereMagico).isPresent());
        List<Artefatto> banco = new ArrayList<>(Arrays.asList(spada, libro()));
        assertEquals(Optional.empty(), puoMettereSulBanco(banco, pergamenaDiPotereMagico));
        // Anche il libro che arriva per ultimo trova il suo posto
        assertEquals(Optional.empty(), puoMettereSulBanco(Collections.singletonList(spada), libro()));
    }

    // --- Verifica della fusione

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
        assertEquals(Optional.empty(), puoMettereSulBanco(Collections.singletonList(pari), spada));
        assertEquals(0, RegoleIncantatura.effettiDaTrasferire(banco));
        assertEquals(Optional.of(TipoMotivoRifiutoIncantatura.NESSUN_MIGLIORAMENTO), verifica(banco, 100));
        Optional<AvvisoDiFusione> avviso = RegoleIncantatura.avvisoEffettiPersi(banco);
        assertTrue(avviso.get().isNessunMiglioramento());
        assertNull(avviso.get().getPerdita());
        assertEquals(0, avviso.get().getEffettiCheEntrano());
        assertEquals(0, avviso.get().getEffettiScalzati());
    }

    @Test
    void ilTettoDeiLeggendariECinque() {
        Artefatto spada = artefatto(TipoArtefatto.SPADA, 20);
        spada.getModelloDati().setRarita(TipoRaritaArtefatto.LEGGENDARIO);
        List<Artefatto> banco = new ArrayList<>(Arrays.asList(spada, pergamena(3), pergamena(2)));
        assertEquals(Optional.empty(), verifica(banco, 1000));
        assertFalse(RegoleIncantatura.avvisoEffettiPersi(banco).isPresent());
        banco.add(pergamena(1));
        assertEquals(5, RegoleIncantatura.effettiDaTrasferire(banco));
        assertTrue(RegoleIncantatura.avvisoEffettiPersi(banco).isPresent());
    }

    @Test
    void rifiutiDellaFusione() {
        assertEquals(Optional.of(TipoMotivoRifiutoIncantatura.NESSUN_ARTEFATTO),
                verifica(Collections.singletonList(pergamena(1)), 100));
        assertEquals(Optional.of(TipoMotivoRifiutoIncantatura.NESSUN_INGREDIENTE),
                verifica(Collections.singletonList(artefatto(TipoArtefatto.SPADA, 4)), 100));
        assertEquals(Optional.of(TipoMotivoRifiutoIncantatura.PIU_ARTEFATTI),
                verifica(Arrays.asList(artefatto(TipoArtefatto.SPADA, 4), artefatto(TipoArtefatto.ELMO, 4), pergamena(1)), 100));
        assertEquals(Optional.of(TipoMotivoRifiutoIncantatura.NON_INCANTABILE),
                verifica(Arrays.asList(artefatto(TipoArtefatto.ANELLO, 4), pergamena(1)), 100));
        assertEquals(Optional.of(TipoMotivoRifiutoIncantatura.POTERE_MAGICO_FUORI_POSTO),
                verifica(Arrays.asList(artefatto(TipoArtefatto.SPADA, 4), potereMagico()), 100));
        // 10 + 5 × 2 = 20 monete
        List<Artefatto> banco = Arrays.asList(artefatto(TipoArtefatto.SPADA, 4), pergamena(2));
        assertEquals(20, RegoleIncantatura.costo(banco));
        assertEquals(20, RegoleIncantatura.costo(2));
        assertEquals(Optional.of(TipoMotivoRifiutoIncantatura.MONETE_INSUFFICIENTI), verifica(banco, 19));
        assertEquals(Optional.empty(), verifica(banco, 20));
        // Il costo che il gruppo deve pagare lo decide chi chiama (per esempio con uno sconto)
        assertEquals(Optional.empty(), RegoleIncantatura.verifica(banco, 16, 16, LIVELLO_DEL_MONDO));
    }

    private static Artefatto potereMagico() {
        Artefatto pergamena = artefatto(TipoArtefatto.PERGAMENA, 1);
        pergamena.getModelloDati().addModificatore(TipoAttributo.POTERE_MAGICO, TipoModificatore.VARIAZIONE_PERCENTUALE, 10, "");
        return pergamena;
    }

    @Test
    void ilLibroCheRiprogrammaVaVerificatoConSuoiPropriCriteri() {
        Artefatto spada = spadaCon(2);
        List<Artefatto> banco = new ArrayList<>(Arrays.asList(spada, libro(), potereMagico()));
        assertTrue(RegoleIncantatura.conRiprogrammazione(banco));
        assertTrue(RegoleIncantatura.isRiprogrammazione(banco.get(1)));
        assertFalse(RegoleIncantatura.isRiprogrammazione(spada));
        // Si paga per ogni incantamento
        assertEquals(2, RegoleIncantatura.effettiDaTrasferire(banco));
        assertEquals(Optional.empty(), verifica(banco, 100));
        assertEquals(Optional.of(TipoMotivoRifiutoIncantatura.MONETE_INSUFFICIENTI), verifica(banco, 0));
        assertFalse(RegoleIncantatura.avvisoEffettiPersi(banco).isPresent());
        RegoleIncantatura.Piano piano = RegoleIncantatura.pianoDellaFusione(banco);
        assertEquals(0, piano.numeroDaAggiungere());
        assertEquals(2, piano.numeroDaTogliere());
        spada.getIncantamenti().forEach(incantamento -> assertTrue(piano.esce(incantamento)));
        assertFalse(piano.entra(spada.getIncantamenti().iterator().next()));
        // Un artefatto senza incantamenti non si riprogramma
        assertEquals(Optional.of(TipoMotivoRifiutoIncantatura.NIENTE_DA_RIPROGRAMMARE),
                verifica(Arrays.asList(spadaCon(0), libro()), 100));
        // E senza artefatto sul banco non c'è niente da pagare
        assertEquals(0, RegoleIncantatura.effettiDaTrasferire(Collections.singletonList(libro())));
    }

    // --- Che cosa uscirebbe dalla fusione

    @Test
    void conPiuEffettiChePostiPassanoIMiglioriEIlRestoSiPerde() {
        // Una spada comune di livello 3 (2 posti, uno preso) e un ingrediente con due incantamenti
        Artefatto spada = artefatto(TipoArtefatto.SPADA, 3);
        spada.getModelloDati().addIncantamento("Fuoco minore", TipoDanno.FUOCO, 5, 0.05);
        Artefatto gemma = artefatto(TipoArtefatto.GEMMA, 4);
        gemma.getModelloDati().addIncantamento("Acido minore", TipoDanno.ACIDO, 5, 0.05);
        gemma.getModelloDati().addIncantamento("Gelo maggiore", TipoDanno.GELO, 15, 0.2);
        BancoDiLavoro banco = banco(spada, gemma);
        AvvisoDiFusione avviso = RegoleIncantatura.avvisoEffettiPersi(banco.getInventario()).get();
        assertEquals(AvvisoDiFusione.Perdita.LIMITE_DI_EFFETTI, avviso.getPerdita());
        assertEquals(1, avviso.getEffettiCheEntrano());
        assertEquals(0, avviso.getEffettiScalzati());
        assertFalse(avviso.isNessunMiglioramento());
        // Si paga solo l'effetto che passa
        assertEquals(CostantiArtefatti.FUSIONE_COSTO_BASE + CostantiArtefatti.FUSIONE_COSTO_PER_EFFETTO,
                RegoleIncantatura.costo(banco.getInventario()));
        // Passa il più prezioso, il gelo maggiore
        RegoleIncantatura.fondi(banco, "");
        assertEquals(2, spada.getIncantamenti().size());
        assertTrue(spada.getIncantamenti().stream().anyMatch(i -> i.getTipoDannoElementale() == TipoDanno.GELO));
        assertFalse(spada.getIncantamenti().stream().anyMatch(i -> i.getTipoDannoElementale() == TipoDanno.ACIDO));
    }

    @Test
    void unEffettoMiglioreScalzaSoloUnoDeiPiuDebboli() {
        // Una spada con due +5 Forza uguali e una pergamena con un +10 Forza
        Artefatto spada = artefatto(TipoArtefatto.SPADA, 3);
        spada.getModelloDati().addModificatore(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_FISSA, 5, "");
        spada.getModelloDati().addModificatore(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_FISSA, 5, "");
        Artefatto pergamena = artefatto(TipoArtefatto.PERGAMENA, 1);
        pergamena.getModelloDati().addModificatore(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_FISSA, 10, "");
        BancoDiLavoro banco = banco(spada, pergamena);
        AvvisoDiFusione avviso = RegoleIncantatura.avvisoEffettiPersi(banco.getInventario()).get();
        assertNull(avviso.getPerdita());
        assertEquals(1, avviso.getEffettiCheEntrano());
        assertEquals(1, avviso.getEffettiScalzati());
        assertEquals(1, RegoleIncantatura.effettiDaTrasferire(banco.getInventario()));
        assertEquals(2, RegoleIncantatura.effettiDopoLaFusione(banco.getInventario()));
        RegoleIncantatura.Piano piano = RegoleIncantatura.pianoDellaFusione(banco.getInventario());
        assertTrue(piano.entra(pergamena.getModificatori().iterator().next()));
        assertEquals(1, spada.getModificatori().stream().filter(piano::esce).count());
        // Il tetto resta 2, e un solo +5 è stato sostituito
        RegoleIncantatura.fondi(banco, "");
        assertEquals(2, spada.getModificatori().size());
        assertEquals(1, spada.getModificatori().stream().filter(m -> m.getQuantita() == 10).count());
        assertEquals(1, spada.getModificatori().stream().filter(m -> m.getQuantita() == 5).count());
    }

    @Test
    void suUnaSpadaIlPotereMagicoSiPerdeEPassaIlResto() {
        Artefatto spada = artefatto(TipoArtefatto.SPADA, 5);
        Artefatto sigillo = artefatto(TipoArtefatto.SIGILLO, 4);
        sigillo.getModelloDati().addModificatore(TipoAttributo.POTERE_MAGICO, TipoModificatore.VARIAZIONE_PERCENTUALE, 20, "");
        sigillo.getModelloDati().addModificatore(TipoAttributo.PARATA, TipoModificatore.VARIAZIONE_FISSA, 3, "");
        BancoDiLavoro banco = banco(spada);
        assertEquals(Optional.empty(), puoMettereSulBanco(banco.getInventario(), sigillo));
        banco.addArtefatto(sigillo);
        assertEquals(Optional.of(AvvisoDiFusione.Perdita.POTERE_MAGICO_SOLO_SU_BASTONI_E_LIBRI), perdita(banco.getInventario()));
        RegoleIncantatura.fondi(banco, "");
        assertEquals(1, spada.getModificatori().size());
        assertEquals(TipoAttributo.PARATA, spada.getModificatori().iterator().next().getTipoAttributo());
    }

    @Test
    void suUnLibroMagicoPassaSoloIlModificatoreEGliIncantamentiSiPerdono() {
        Artefatto libro = artefatto(TipoArtefatto.LIBRO_MAGICO, 5);
        Artefatto pergamena = pergamena(1);
        pergamena.getModelloDati().addModificatore(TipoAttributo.POTERE_MAGICO, TipoModificatore.VARIAZIONE_PERCENTUALE, 10, "");
        BancoDiLavoro banco = banco(libro, pergamena);
        assertEquals(Optional.of(AvvisoDiFusione.Perdita.INCANTAMENTI_ELEMENTALI_NON_SUL_LIBRO), perdita(banco.getInventario()));
        // Si paga solo l'effetto che passa
        assertEquals(CostantiArtefatti.FUSIONE_COSTO_BASE + CostantiArtefatti.FUSIONE_COSTO_PER_EFFETTO,
                RegoleIncantatura.costo(banco.getInventario()));
        RegoleIncantatura.fondi(banco, "");
        assertTrue(libro.getIncantamenti().isEmpty());
        assertTrue(libro.getModificatori().stream().anyMatch(m -> m.getTipoAttributo() == TipoAttributo.POTERE_MAGICO));
    }

    @Test
    void ilModificatoreSulBersaglioSiFondeSoloSuUnArma() {
        Artefatto pergamena = artefatto(TipoArtefatto.PERGAMENA, 1);
        pergamena.getModelloDati().addModificatore(new ModificatoreAttributoMD(TipoAttributo.FORTUNA, TipoModificatore.QUANTITA_ASSOLUTA, 0, "",
                UnitaDurataModificatore.TURNI, 5, BersaglioModificatore.BERSAGLIO));
        pergamena.getModelloDati().addModificatore(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_FISSA, 1, "");

        List<Artefatto> suUnaSpada = Arrays.asList(artefatto(TipoArtefatto.SPADA, 5), pergamena);
        assertEquals(2, RegoleIncantatura.effettiDaTrasferire(suUnaSpada));
        assertFalse(RegoleIncantatura.avvisoEffettiPersi(suUnaSpada).isPresent());

        List<Artefatto> suUnElmo = Arrays.asList(artefatto(TipoArtefatto.ELMO, 5), pergamena);
        assertEquals(1, RegoleIncantatura.effettiDaTrasferire(suUnElmo), "passa solo la forza");
        assertEquals(Optional.of(AvvisoDiFusione.Perdita.MODIFICATORI_SUL_BERSAGLIO_SOLO_SULLE_ARMI), perdita(suUnElmo));
    }

    // --- Fusione

    @Test
    void laFusioneDaIlNomeProprioNormalizzatoEVuotaIlBanco() {
        Artefatto spada = artefatto(TipoArtefatto.SPADA, 4);
        Artefatto pergamena = pergamena(0);
        pergamena.getModelloDati().addIncantamento("Gelo medio", TipoDanno.GELO, 10, 0.1);
        pergamena.getModelloDati().addModificatore(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_FISSA, 2, "");
        BancoDiLavoro banco = banco(spada, pergamena);
        Artefatto incantata = RegoleIncantatura.fondi(banco, "lama del drago");
        assertSame(spada, incantata);
        assertTrue(banco.getInventario().isEmpty());
        assertEquals("Lama Del Drago", incantata.getNomeProprio().orElse(null));
        assertEquals(1, incantata.getIncantamenti().size());
        assertTrue(incantata.getModificatori().contains(new ModificatoreAttributoMD(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_FISSA, 2)));
    }

    @Test
    void conIlNomeVuotoLArtefattoNonHaNomeProprio() {
        Artefatto spada = artefatto(TipoArtefatto.SPADA, 4);
        spada.getModelloDati().setNomeProprio("Diavolina");
        RegoleIncantatura.fondi(banco(spada, pergamena(1)), "");
        assertNull(spada.getModelloDati().getNomeProprio());
    }

    @Test
    void unAggiornamentoDaSoloBastaAFarSalireDiLivelloLArtefatto() {
        Artefatto spada = artefatto(TipoArtefatto.SPADA, 3);
        BancoDiLavoro banco = banco(spada, artefatto(TipoArtefatto.AGGIORNAMENTO, 1));
        Collection<Artefatto> sulBanco = banco.getInventario();
        assertEquals(4, RegoleIncantatura.livelloDopoLaFusione(sulBanco));
        assertEquals(1, RegoleIncantatura.aggiornamenti(sulBanco));
        assertTrue(RegoleIncantatura.isAggiornamento(sulBanco.stream().skip(1).findFirst().get()));
        assertTrue(RegoleIncantatura.isIngrediente(sulBanco.stream().skip(1).findFirst().get()));
        assertFalse(RegoleIncantatura.isIngrediente(spada));
        // Una spada comune ha un posto in meno del livello: da 2 a 3
        assertEquals(3, RegoleIncantatura.effettiMassimiDopoLaFusione(sulBanco));
        assertEquals(0, RegoleIncantatura.effettiDaTrasferire(sulBanco));
        assertEquals(CostantiArtefatti.FUSIONE_COSTO_BASE, RegoleIncantatura.costo(sulBanco));
        assertEquals(Optional.empty(), verifica(sulBanco, 100));
        assertEquals(spada, RegoleIncantatura.artefattoSulBanco(sulBanco).get());
        RegoleIncantatura.fondi(banco, "");
        assertEquals(4, spada.getLivello());
    }

    @Test
    void ilLivelloNuovoValeGiaPerGliEffettiDellaStessaFusione() {
        // Una spada comune di livello 2 ha un solo posto, già preso: il livello 3 ne dà uno in più per la pergamena
        Artefatto spada = artefatto(TipoArtefatto.SPADA, 2);
        spada.getModelloDati().addModificatore(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_FISSA, 5, "");
        Artefatto pergamena = artefatto(TipoArtefatto.PERGAMENA, 1);
        pergamena.getModelloDati().addModificatore(TipoAttributo.DESTREZZA, TipoModificatore.VARIAZIONE_FISSA, 5, "");
        BancoDiLavoro banco = banco(spada, pergamena);
        assertEquals(1, RegoleIncantatura.effettiDopoLaFusione(banco.getInventario()), "senza l'aggiornamento non c'è posto");
        banco.addArtefatto(artefatto(TipoArtefatto.AGGIORNAMENTO, 1));
        assertEquals(2, RegoleIncantatura.effettiDopoLaFusione(banco.getInventario()));
        RegoleIncantatura.fondi(banco, "");
        assertEquals(3, spada.getLivello());
        assertEquals(2, spada.getModificatori().size());
    }

    @Test
    void senzaArtefattoSulBancoNonCiSonoEffettiNeLivelli() {
        List<Artefatto> soloIngredienti = Collections.singletonList(pergamena(2));
        assertFalse(RegoleIncantatura.artefattoSulBanco(soloIngredienti).isPresent());
        assertEquals(0, RegoleIncantatura.effettiDopoLaFusione(soloIngredienti));
        assertEquals(0, RegoleIncantatura.livelloDopoLaFusione(soloIngredienti));
        assertEquals(0, RegoleIncantatura.effettiMassimiDopoLaFusione(soloIngredienti));
        assertEquals(2, RegoleIncantatura.effettiDaTrasferire(soloIngredienti));
        assertEquals(0, RegoleIncantatura.pianoDellaFusione(soloIngredienti).numeroDaAggiungere());
        assertFalse(RegoleIncantatura.avvisoEffettiPersi(soloIngredienti).isPresent());
        assertEquals(1, RegoleIncantatura.effetti(pergamena(1)));
    }

    @Test
    void laRiprogrammazioneSpezzaLArtefattoNeiSuoiIncantamenti() {
        Artefatto spada = spadaCon(3);
        Collection<IncantamentoMD> originali = new ArrayList<>(spada.getIncantamenti());
        Artefatto libro = libro();
        Artefatto gemma = artefatto(TipoArtefatto.GEMMA, 1);
        gemma.getModelloDati().addIncantamento("Fulmine minore", TipoDanno.FULMINE, 5, 0.05);
        BancoDiLavoro banco = banco(spada, libro, gemma);
        assertEquals(CostantiArtefatti.FUSIONE_COSTO_BASE + 3 * CostantiArtefatti.FUSIONE_COSTO_PER_EFFETTO,
                RegoleIncantatura.costo(banco.getInventario()));
        assertEquals(spada.getLivello(), RegoleIncantatura.livelloDopoLaFusione(banco.getInventario()));

        List<Artefatto> restituiti = RegoleIncantatura.riprogramma(banco);

        assertTrue(banco.getInventario().isEmpty(), "il banco si svuota");
        assertTrue(spada.getIncantamenti().isEmpty());
        assertEquals(10, spada.getModelloDati().getDanni());
        assertTrue(restituiti.stream().anyMatch(a -> a.getModelloDati() == spada.getModelloDati()));
        assertTrue(restituiti.stream().anyMatch(a -> a.getModelloDati() == libro.getModelloDati()), "il libro non si perde");
        assertTrue(restituiti.stream().anyMatch(a -> a.getModelloDati() == gemma.getModelloDati()), "l'ingrediente non è stato usato");
        assertEquals(1, gemma.getIncantamenti().size());
        List<Artefatto> delCoreDump = restituiti.stream().filter(a -> a.getNome().endsWith("del Core Dump")).collect(Collectors.toList());
        assertEquals(3, delCoreDump.size());
        for (Artefatto ingrediente : delCoreDump) {
            assertTrue(ingrediente.getTipo().isIngrediente());
            assertFalse(ingrediente.getTipo().isRiprogrammazione());
            assertEquals(1, ingrediente.getIncantamenti().size());
        }
        assertTrue(delCoreDump.stream().flatMap(a -> a.getIncantamenti().stream()).collect(Collectors.toList()).containsAll(originali));
    }

    @Test
    void ilTestoDelleCostantiDelMotivoDiRifiutoEDisponibile() {
        for (TipoMotivoRifiutoIncantatura motivo : TipoMotivoRifiutoIncantatura.values()) {
            assertFalse(motivo.getFrase().trim().isEmpty(), motivo.name());
        }
    }

    @Test
    void ilPianoVuotoNonFaEntrareNeUscireNiente() {
        RegoleIncantatura.Piano piano = RegoleIncantatura.pianoDellaFusione(Collections.emptyList());
        assertFalse(piano.entra("niente"));
        assertFalse(piano.esce("niente"));
        assertEquals(0, piano.numeroDaAggiungere());
        assertEquals(0, piano.numeroDaTogliere());
    }

    @Test
    void unIngredienteSenzaArtefattoSulBancoNonHaNienteDaControllare() {
        assertEquals(Optional.empty(), puoMettereSulBanco(Collections.emptyList(), pergamena(1)));
    }

    @Test
    void unAggiornamentoSulBancoNonFaDireCheLaFusioneNonCambiaNiente() {
        Artefatto spada = artefatto(TipoArtefatto.SPADA, 3);
        spada.getModelloDati().addIncantamento("Fuoco minore", TipoDanno.FUOCO, 5, 0.05);
        spada.getModelloDati().addIncantamento("Gelo minore", TipoDanno.GELO, 5, 0.05);
        Artefatto pari = artefatto(TipoArtefatto.GEMMA, 1);
        pari.getModelloDati().addIncantamento("Acido minore", TipoDanno.ACIDO, 5, 0.05);
        // Con il livello in più c'è un posto libero: l'effetto uguale passa
        List<Artefatto> banco = Arrays.asList(spada, pari, artefatto(TipoArtefatto.AGGIORNAMENTO, 1));
        assertEquals(Optional.empty(), RegoleIncantatura.avvisoEffettiPersi(banco));
    }

    @Test
    void unIncantamentoMiglioreScalzaUnIncantamentoDellArtefatto() {
        Artefatto spada = artefatto(TipoArtefatto.SPADA, 3);
        spada.getModelloDati().addIncantamento("Fuoco minore", TipoDanno.FUOCO, 5, 0.05);
        spada.getModelloDati().addIncantamento("Gelo minore", TipoDanno.GELO, 5, 0.05);
        Artefatto gemma = artefatto(TipoArtefatto.GEMMA, 4);
        gemma.getModelloDati().addIncantamento("Acido maggiore", TipoDanno.ACIDO, 15, 0.2);
        BancoDiLavoro banco = banco(spada, gemma);
        assertEquals(1, RegoleIncantatura.avvisoEffettiPersi(banco.getInventario()).get().getEffettiScalzati());
        RegoleIncantatura.fondi(banco, "");
        assertEquals(2, spada.getIncantamenti().size());
        assertTrue(spada.getIncantamenti().stream().anyMatch(i -> i.getTipoDannoElementale() == TipoDanno.ACIDO));
    }

    @Test
    void conUnSoloIngredienteCheNonHaSensoNonSiDiceChePassaMenoDelSolito() {
        // Il potere magico su una spada non ha senso: nessun effetto "sensato", e l'avviso dice perché
        List<Artefatto> banco = Arrays.asList(artefatto(TipoArtefatto.SPADA, 5), potereMagico());
        assertEquals(Optional.of(AvvisoDiFusione.Perdita.POTERE_MAGICO_SOLO_SU_BASTONI_E_LIBRI), perdita(banco));
        assertFalse(RegoleIncantatura.avvisoEffettiPersi(banco).get().isNessunMiglioramento());
    }

    @Test
    void conUnAggiornamentoSulBancoLaFusioneCambiaComunqueQualcosa() {
        // Più effetti dei posti (come certi leggendari): l'effetto debole non passa neanche col livello in più
        Artefatto spada = artefatto(TipoArtefatto.SPADA, 3);
        TipoDanno[] danni = {TipoDanno.FUOCO, TipoDanno.GELO, TipoDanno.FULMINE, TipoDanno.TAGLIENTE};
        for (TipoDanno danno : danni) {
            spada.getModelloDati().addIncantamento("Forte " + danno, danno, 15, 0.2);
        }
        Artefatto debole = artefatto(TipoArtefatto.GEMMA, 1);
        debole.getModelloDati().addIncantamento("Acido minore", TipoDanno.ACIDO, 5, 0.05);
        List<Artefatto> banco = Arrays.asList(spada, debole, artefatto(TipoArtefatto.AGGIORNAMENTO, 1));
        assertFalse(RegoleIncantatura.avvisoEffettiPersi(banco).get().isNessunMiglioramento());
        assertEquals(Optional.of(AvvisoDiFusione.Perdita.LIMITE_DI_EFFETTI), perdita(banco));
    }
}
