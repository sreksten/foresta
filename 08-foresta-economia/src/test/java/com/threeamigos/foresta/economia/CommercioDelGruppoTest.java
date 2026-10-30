package com.threeamigos.foresta.economia;

import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.artefatti.BancoDiLavoro;
import com.threeamigos.foresta.artefatti.ScambiatoreArtefatti;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoAcquistoConsumabile;
import com.threeamigos.foresta.eventi.interni.InternoAcquistoArtefatto;
import com.threeamigos.foresta.eventi.interni.InternoIncantatura;
import com.threeamigos.foresta.eventi.interni.InternoVenditaArtefatto;
import com.threeamigos.foresta.eventi.notifiche.NotificaApprovazioneAcquistoArtefatto;
import com.threeamigos.foresta.eventi.notifiche.NotificaApprovazioneAcquistoConsumabile;
import com.threeamigos.foresta.eventi.notifiche.NotificaApprovazioneIncantatura;
import com.threeamigos.foresta.eventi.notifiche.NotificaApprovazioneVenditaArtefatto;
import com.threeamigos.foresta.eventi.notifiche.NotificaAvvisoIncantatura;
import com.threeamigos.foresta.eventi.notifiche.NotificaRifiutoAcquistoArtefatto;
import com.threeamigos.foresta.eventi.notifiche.NotificaRifiutoAcquistoConsumabile;
import com.threeamigos.foresta.eventi.notifiche.NotificaRifiutoIncantatura;
import com.threeamigos.foresta.eventi.notifiche.NotificaRifiutoVenditaArtefatto;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.modellodati.ArtefattoMD;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.personaggi.Ladro;
import com.threeamigos.foresta.personaggi.Mago;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoConsumabile;
import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.tipi.TipoIncantesimo;
import com.threeamigos.foresta.tipi.TipoMotivoRifiutoIncantatura;
import com.threeamigos.foresta.tipi.TipoNegozio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CommercioDelGruppoTest {

    private GruppoGiocatore gruppo;
    private final List<Object> eventi = new ArrayList<>();

    @BeforeEach
    void prepara() {
        MondoDiProva.nuovo(20, 20);
        CommercioDelGruppo.registrati();
        MagazziniDeiNegozi.registrati();
        gruppo = GruppoGiocatore.getIstanza();
        aggiungi(new Guerriero("Arsenio", 3));
        for (Class<?> classe : new Class<?>[] {NotificaApprovazioneAcquistoArtefatto.class, NotificaRifiutoAcquistoArtefatto.class,
                NotificaApprovazioneVenditaArtefatto.class, NotificaRifiutoVenditaArtefatto.class, NotificaApprovazioneIncantatura.class,
                NotificaRifiutoIncantatura.class, NotificaAvvisoIncantatura.class, NotificaApprovazioneAcquistoConsumabile.class,
                NotificaRifiutoAcquistoConsumabile.class, NotificaTestoFrase.class}) {
            ascolta(classe);
        }
    }

    private void aggiungi(Personaggio personaggio) {
        personaggio.getModelloDati().setPng(false);
        gruppo.aggiungiPersonaggio(personaggio);
    }

    @SuppressWarnings("unchecked")
    private <T> void ascolta(Class<?> classe) {
        BusEventi.iscriviti((Class<T>) classe, e -> eventi.add(e));
    }

    @AfterEach
    void ripulisce() {
        MondoDiProva.finito();
    }

    private <T> T ultimo(Class<T> classe) {
        for (int i = eventi.size() - 1; i >= 0; i--) {
            if (classe.isInstance(eventi.get(i))) {
                return classe.cast(eventi.get(i));
            }
        }
        return null;
    }

    private static Artefatto con(TipoArtefatto tipo, int livello, int effetti) {
        ArtefattoMD md = new ArtefattoMD();
        md.setTipo(tipo);
        md.setNome("l'oggetto di prova");
        md.setDescrizione("che serve ai test");
        md.setLivello(livello);
        md.setPeso(1);
        md.setCostoAcquisto(50);
        md.setDanni(tipo == TipoArtefatto.SPADA ? 10 : 0);
        TipoDanno[] danni = {TipoDanno.FUOCO, TipoDanno.GELO, TipoDanno.ACIDO};
        for (int i = 0; i < effetti; i++) {
            md.addIncantamento("Incantamento " + i, danni[i % danni.length], 5 + i, 0.05);
        }
        return Artefatto.di(md);
    }

    private ScambiatoreArtefatti negozio(TipoNegozio tipo) {
        return MagazziniDeiNegozi.getScambiatorePerNegozio(MondoDiProva.QUI, tipo);
    }

    // ---- I prezzi

    @Test
    void iPrezziSeguonoLaContrattazione() {
        assertEquals(RegoleContrattazione.prezzoAcquisto(100, gruppo.getContrattazione()), CommercioDelGruppo.prezzoAcquisto(gruppo, 100));
        assertEquals(RegoleContrattazione.prezzoVendita(100, gruppo.getContrattazione()), CommercioDelGruppo.prezzoVendita(gruppo, 100));
        Artefatto pergamena = con(TipoArtefatto.PERGAMENA, 1, 2);
        List<Artefatto> banco = new ArrayList<>();
        banco.add(pergamena);
        assertTrue(CommercioDelGruppo.costoFusione(gruppo, banco) > 0);
        assertTrue(CommercioDelGruppo.costoFusione(gruppo, 3) >= CommercioDelGruppo.costoFusione(gruppo, 1));
    }

    // ---- I preziosi

    @Test
    void senzaPreziosiNonSiVendeNiente() {
        CommercioDelGruppo.vendePreziosi(gruppo);
        assertNull(ultimo(NotificaTestoFrase.class));
        assertEquals(0, Cassa.getMonete());
    }

    @Test
    void iPreziosiSiVendonoAlLivelloDelMondo() {
        Cassa.addPreziosi(3);
        MondoDiProva.statistiche.setLivello(4);
        CommercioDelGruppo.vendePreziosi(gruppo);
        assertEquals(12, Cassa.getMonete());
        assertEquals(0, Cassa.getPreziosi());
        assertNotNull(ultimo(NotificaTestoFrase.class));
    }

    @Test
    void ConUnaMonetaSolaSiDiceUnaMoneta() {
        Cassa.addPreziosi(1);
        CommercioDelGruppo.vendePreziosi(gruppo);
        assertEquals(1, Cassa.getMonete());
        assertTrue(ultimo(NotificaTestoFrase.class).getMessaggio().contains("una moneta"));
    }

    @Test
    void ancheUnaLadraVendeMeglio() {
        aggiungi(new com.threeamigos.foresta.personaggi.Ladra("Zorra", 3));
        Cassa.addPreziosi(10);
        CommercioDelGruppo.vendePreziosi(gruppo);
        assertTrue(Cassa.getMonete() >= 10);
    }

    @Test
    void unLadroVendeMeglio() {
        aggiungi(new Ladro("Zorro", 3));
        Cassa.addPreziosi(10);
        CommercioDelGruppo.vendePreziosi(gruppo);
        assertTrue(Cassa.getMonete() >= 10);
    }

    // ---- Gli artefatti

    @Test
    void sCompraDalNegozioSeCiSonoLeMonete() {
        Artefatto spada = con(TipoArtefatto.SPADA, 3, 0);
        ScambiatoreArtefatti armaiolo = negozio(TipoNegozio.ARMAIOLO);
        armaiolo.addArtefatto(spada);
        BusEventi.pubblica(new InternoAcquistoArtefatto(gruppo, armaiolo, spada, null));
        assertNotNull(ultimo(NotificaRifiutoAcquistoArtefatto.class));
        assertEquals(1, armaiolo.getInventario().size());
        Cassa.addMonete(100000);
        BusEventi.pubblica(new InternoAcquistoArtefatto(gruppo, armaiolo, spada, null));
        assertNotNull(ultimo(NotificaApprovazioneAcquistoArtefatto.class));
        assertTrue(armaiolo.getInventario().isEmpty());
        assertEquals(1, gruppo.getInventario().size());
    }

    @Test
    void siVendeSoloQuelloCheIlNegozioTratta() {
        Artefatto spada = con(TipoArtefatto.SPADA, 3, 0);
        gruppo.addArtefatto(spada);
        ScambiatoreArtefatti venditore = negozio(TipoNegozio.VENDITORE_DI_PERGAMENE);
        BusEventi.pubblica(new InternoVenditaArtefatto(gruppo, venditore, spada, null));
        assertNotNull(ultimo(NotificaRifiutoVenditaArtefatto.class));
        assertEquals(0, Cassa.getMonete());
        ScambiatoreArtefatti armaiolo = negozio(TipoNegozio.ARMAIOLO);
        InternoVenditaArtefatto vendita = new InternoVenditaArtefatto(gruppo, armaiolo, spada, null);
        assertTrue(CommercioDelGruppo.vende(gruppo, vendita));
        assertTrue(Cassa.getMonete() > 0);
        assertEquals(1, armaiolo.getInventario().size());
        assertNotNull(ultimo(NotificaApprovazioneVenditaArtefatto.class));
        assertFalse(CommercioDelGruppo.vende(gruppo, new InternoVenditaArtefatto(gruppo, venditore, spada, null)));
    }

    // ---- La fusione

    private void fusione(BancoDiLavoro banco, String nome) {
        BusEventi.pubblica(new InternoIncantatura(banco, nome));
    }

    @Test
    void laFusioneSiRifiutaSeManca()  {
        BancoDiLavoro banco = new BancoDiLavoro();
        fusione(banco, "");
        assertEquals(TipoMotivoRifiutoIncantatura.NESSUN_ARTEFATTO, ultimo(NotificaRifiutoIncantatura.class).getMotivo());
        Optional<TipoMotivoRifiutoIncantatura> motivo = CommercioDelGruppo.incanta(gruppo, banco, "");
        assertTrue(motivo.isPresent());
    }

    @Test
    void laFusioneRiuscitaFaPagareERestituisceLArtefattoIncantato() {
        BancoDiLavoro banco = new BancoDiLavoro();
        banco.addArtefatto(con(TipoArtefatto.SPADA, 5, 0));
        banco.addArtefatto(con(TipoArtefatto.PERGAMENA, 1, 1));
        Cassa.addMonete(100000);
        int prima = Cassa.getMonete();
        Optional<TipoMotivoRifiutoIncantatura> m = CommercioDelGruppo.incanta(gruppo, banco, "Diavolina");
        assertFalse(m.isPresent(), String.valueOf(m));
        NotificaApprovazioneIncantatura approvazione = ultimo(NotificaApprovazioneIncantatura.class);
        assertNotNull(approvazione);
        assertEquals(prima - approvazione.getCosto(), Cassa.getMonete());
        assertEquals(1, gruppo.getInventario().size());
        assertTrue(banco.getInventario().isEmpty());
    }

    @Test
    void conIlLibroCheRiprogrammaLaFusioneVaSempreMale() {
        BancoDiLavoro banco = new BancoDiLavoro();
        banco.addArtefatto(con(TipoArtefatto.SPADA, 1, 2));
        banco.addArtefatto(con(TipoArtefatto.RIPROGRAMMAZIONE, 1, 0));
        Cassa.addMonete(100000);
        assertFalse(CommercioDelGruppo.incanta(gruppo, banco, "").isPresent());
        assertNotNull(ultimo(NotificaAvvisoIncantatura.class));
        assertNull(ultimo(NotificaApprovazioneIncantatura.class));
        // la spada, il libro e i due ingredienti del Core Dump
        assertEquals(4, gruppo.getInventario().size());
    }

    // ---- L'alchimista

    private void compra(TipoConsumabile tipo, TipoIncantesimo incantesimo, String uuid) {
        BusEventi.pubblica(new ComandoAcquistoConsumabile(tipo, incantesimo, uuid));
    }

    @Test
    void senzaMoneteNonSiCompraNiente() {
        compra(TipoConsumabile.POZIONE_SALUTE, null, null);
        assertNotNull(ultimo(NotificaRifiutoAcquistoConsumabile.class));
    }

    @Test
    void unAcquistoChePerLAlchimistaNonEsisteSiIgnora() {
        Cassa.addMonete(100000);
        compra(TipoConsumabile.MONETE, null, null);
        compra(TipoConsumabile.AUMENTO_MAGIA_SINGOLO, null, "nessuno");
        compra(TipoConsumabile.AUMENTO_MAGIA_GRUPPO, null, null);
        assertNull(ultimo(NotificaApprovazioneAcquistoConsumabile.class));
        assertNull(ultimo(NotificaRifiutoAcquistoConsumabile.class));
        assertEquals(100000, Cassa.getMonete());
    }

    @Test
    void ogniConsumabileDaEffettoESiPaga() {
        aggiungi(new Mago("Merlino", 3));
        Cassa.addMonete(1000000);
        Personaggio mago = gruppo.getPersonaggi().stream().filter(p -> p instanceof Mago).findFirst().get();
        int salute = gruppo.getPozioniSalute();
        int grandi = gruppo.getPozioniSaluteGrande();
        int magia = gruppo.getPozioniMagia();
        int magiaGrandi = gruppo.getPozioniMagiaGrande();
        int incantesimi = gruppo.getIncantesimi(TipoIncantesimo.ARIA);
        int magiaMassima = mago.getMagiaMassima();
        compra(TipoConsumabile.POZIONE_SALUTE, null, null);
        compra(TipoConsumabile.POZIONE_SALUTE_GRANDE, null, null);
        compra(TipoConsumabile.POZIONE_MAGIA, null, null);
        compra(TipoConsumabile.POZIONE_MAGIA_GRANDE, null, null);
        compra(TipoConsumabile.INCANTESIMO, TipoIncantesimo.ARIA, null);
        compra(TipoConsumabile.AUMENTO_MAGIA_SINGOLO, null, mago.getUuid());
        Personaggio comparsa = new Guerriero("Comparsa", 3);
        gruppo.aggiungiPersonaggio(comparsa);
        comparsa.getModelloDati().setPng(true);
        compra(TipoConsumabile.AUMENTO_MAGIA_GRUPPO, null, null);
        compra(TipoConsumabile.MAPPA_PARZIALE_FORESTA, null, null);
        compra(TipoConsumabile.MAPPA_COMPLETA_FORESTA, null, null);
        assertEquals(salute + 1, gruppo.getPozioniSalute());
        assertEquals(grandi + 1, gruppo.getPozioniSaluteGrande());
        assertEquals(magia + 1, gruppo.getPozioniMagia());
        assertEquals(magiaGrandi + 1, gruppo.getPozioniMagiaGrande());
        assertEquals(incantesimi + 1, gruppo.getIncantesimi(TipoIncantesimo.ARIA));
        assertTrue(mago.getMagiaMassima() > magiaMassima);
        assertTrue(Cassa.getMonete() < 1000000);
        assertNotNull(ultimo(NotificaApprovazioneAcquistoConsumabile.class));
        assertTrue(ultimo(NotificaApprovazioneAcquistoConsumabile.class).getPrezzoPagato() > 0);
        assertNotNull(ultimo(NotificaApprovazioneAcquistoConsumabile.class).getEventoRichiestaAcquistoConsumabile());
    }
}
