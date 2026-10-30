package com.threeamigos.foresta.incantesimi;

import com.threeamigos.foresta.combattimento.StatoDelCombattimento;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.gruppi.Gruppo;
import com.threeamigos.foresta.gruppi.GruppoAvversario;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.modellodati.GruppoGiocatoreMD;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Goblin;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.personaggi.Mago;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.personaggi.Titano;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.tipi.CostantiIncantesimi;
import com.threeamigos.foresta.tipi.PortataIncantesimo;
import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoIncantesimo;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Gli incantesimi GLOBALI: agiscono su tutta la locazione, senza un bersaglio scelto.
 */
class IncantesimiGlobaliTest {

    private final List<String> frasi = new ArrayList<>();
    private Runnable iscrizione;
    private GruppoAvversario avversari;
    private Personaggio mago;

    @BeforeEach
    void prepara() {
        BusEventi.impostaConsegna(Runnable::run);
        GruppoGiocatoreMD dati = new GruppoGiocatoreMD();
        GruppoGiocatore.setFornitoreDiDati(() -> dati);
        GruppoGiocatore.azzeraIstanza();
        GruppoAvversario.azzeraIstanza();
        StatoDelCombattimento.azzeraIstanza();
        Dado.impostaSeme(5L);
        iscrizione = BusEventi.iscriviti(NotificaTestoFrase.class, e -> frasi.add(e.getMessaggio()));
        avversari = GruppoAvversario.getIstanza();
        mago = new Mago("Merlino", 10);
        mago.addMagia(100000);
    }

    @AfterEach
    void ripulisce() {
        iscrizione.run();
        BusEventi.impostaConsegna(null);
        StatoDelCombattimento.azzeraIstanza();
        GruppoAvversario.azzeraIstanza();
        GruppoGiocatore.azzeraIstanza();
        Dado.ripristina();
    }

    private Personaggio nemico(TipoPersonaggio tipo, int livello) {
        Personaggio p = FabbricaPersonaggi.crea(tipo, livello);
        avversari.aggiungiPersonaggio(p);
        return p;
    }

    private static Personaggio immuneAlDanno(TipoDanno tipoDanno) {
        return new Titano(20) {
            @Override
            public boolean isImmuneATipoDanno(TipoDanno tipo) {
                return tipo == tipoDanno || super.isImmuneATipoDanno(tipo);
            }
        };
    }

    private static boolean haIlModificatore(Personaggio personaggio, String nota) {
        return personaggio.getModificatoriTemporanei().stream().anyMatch(m -> nota.equals(m.getNote()));
    }

    @Test
    void ognunoHaUnaClasseGlobaleCheSaDireSeServe() {
        for (TipoIncantesimo tipo : TipoIncantesimo.values()) {
            if (tipo.getPortata() == PortataIncantesimo.GLOBALE) {
                assertTrue(FabbricaIncantesimi.crea(tipo, 3) instanceof IncantesimoGlobale, tipo.name());
            }
        }
    }

    // --- Nebbia

    @Test
    void laNebbiaTogliePrecisioneATuttiGliAvversariPerQualcheTurno() {
        Personaggio primo = nemico(TipoPersonaggio.TITANO, 20);
        Personaggio secondo = nemico(TipoPersonaggio.GOBLIN, 1);
        int precisioneDelPrimo = primo.get(TipoAttributo.PRECISIONE);
        int magiaPrima = mago.getMagia();

        FabbricaIncantesimi.crea(TipoIncantesimo.NEBBIA, 3).formula(mago, null, avversari);

        assertTrue(haIlModificatore(primo, Nebbia.NOTA));
        assertTrue(haIlModificatore(secondo, Nebbia.NOTA));
        assertTrue(primo.get(TipoAttributo.PRECISIONE) < precisioneDelPrimo);
        assertEquals(magiaPrima - TipoIncantesimo.NEBBIA.getCostoLancio(), mago.getMagia());
        assertTrue(frasi.get(0).contains("nebbia fitta"), frasi.toString());
        assertTrue(StatoDelCombattimento.getIstanza().haLanciatoUnIncantesimo(mago));
        assertEquals(0, new Nebbia(3).getDanni());
    }

    @Test
    void laNebbiaNonToccaChiEImmune() {
        Personaggio gargoyle = nemico(TipoPersonaggio.GARGOYLE, 5);
        Personaggio goblin = nemico(TipoPersonaggio.GOBLIN, 1);

        new Nebbia(3).formula(mago, null, avversari);

        assertFalse(haIlModificatore(gargoyle, Nebbia.NOTA));
        assertTrue(haIlModificatore(goblin, Nebbia.NOTA));
        assertTrue(frasi.stream().anyMatch(f -> f.startsWith("La nebbia non ha effetto su ")), frasi.toString());
        assertFalse(new Nebbia(3).serve(Collections.emptyList(), Collections.singletonList(gargoyle)));
    }

    @Test
    void laNebbiaServeSoloSeQualcunoPuoPrenderla() {
        Personaggio goblin = nemico(TipoPersonaggio.GOBLIN, 1);
        Nebbia nebbia = new Nebbia(3);
        assertTrue(nebbia.serve(Collections.emptyList(), avversari.getPersonaggi()));
        nebbia.formula(mago, null, avversari);
        assertFalse(nebbia.serve(Collections.emptyList(), avversari.getPersonaggi()));
        assertFalse(nebbia.serve(Collections.emptyList(), Collections.emptyList()));
        Personaggio altro = nemico(TipoPersonaggio.GOBLIN, 2);
        assertTrue(nebbia.serve(Collections.emptyList(), avversari.getPersonaggi()));
        altro.subSalute(altro.getSalute(), null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        assertFalse(nebbia.serve(Collections.emptyList(), avversari.getPersonaggi()));
        assertTrue(haIlModificatore(goblin, Nebbia.NOTA));
    }

    // --- Nega magia

    @Test
    void laNegaMagiaAbbassaIlPotereMagicoDegliAvversari() {
        Personaggio strega = nemico(TipoPersonaggio.STREGA, 5);
        Personaggio goblin = nemico(TipoPersonaggio.GOBLIN, 1);
        double potereDellaStrega = strega.getPotereMagico();
        int magiaPrima = mago.getMagia();

        FabbricaIncantesimi.crea(TipoIncantesimo.NEGA_MAGIA, 3).formula(mago, null, avversari);

        assertTrue(haIlModificatore(strega, NegaMagia.NOTA));
        assertTrue(haIlModificatore(goblin, NegaMagia.NOTA));
        assertTrue(strega.getPotereMagico() < potereDellaStrega);
        assertEquals(magiaPrima - TipoIncantesimo.NEGA_MAGIA.getCostoLancio(), mago.getMagia());
        assertTrue(frasi.get(0).contains("spegne la magia"), frasi.toString());
        assertEquals(0, new NegaMagia(3).getDanni());
    }

    @Test
    void laNegaMagiaNonToccaChiEImmune() {
        Personaggio gargoyle = nemico(TipoPersonaggio.GARGOYLE, 5);

        new NegaMagia(3).formula(mago, null, avversari);

        assertFalse(haIlModificatore(gargoyle, NegaMagia.NOTA));
        assertTrue(frasi.stream().anyMatch(f -> f.startsWith("La nega magia non ha effetto su ")), frasi.toString());
    }

    @Test
    void laNegaMagiaServeSoloControChiSaLanciareIncantesimi() {
        Personaggio goblin = nemico(TipoPersonaggio.GOBLIN, 1);
        Personaggio strega = nemico(TipoPersonaggio.STREGA, 5);
        NegaMagia negaMagia = new NegaMagia(3);
        assertFalse(negaMagia.serve(Collections.emptyList(), Collections.singletonList(goblin)), "un goblin non ha magia da spegnere");
        assertTrue(negaMagia.serve(Collections.emptyList(), Collections.singletonList(strega)));
        negaMagia.formula(mago, null, avversari);
        assertFalse(negaMagia.serve(Collections.emptyList(), Collections.singletonList(strega)));
        assertFalse(negaMagia.serve(Collections.emptyList(), Collections.emptyList()));
        Personaggio altra = nemico(TipoPersonaggio.STREGA, 6);
        altra.subSalute(altra.getSalute(), null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        assertFalse(negaMagia.serve(Collections.emptyList(), Collections.singletonList(altra)));
        assertFalse(negaMagia.serve(Collections.emptyList(), Collections.singletonList(nemico(TipoPersonaggio.GARGOYLE, 5))));
    }

    // --- Grido di battaglia

    @Test
    void ilGridoDiBattagliaDaCoraggioEValoreATuttoIlGruppoDiChiLaLancia() {
        Personaggio compagno = new Mago("Gandalf", 4);
        GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
        gruppo.aggiungiPersonaggio(mago);
        gruppo.aggiungiPersonaggio(compagno);
        Personaggio nemico = nemico(TipoPersonaggio.GOBLIN, 1);
        int coraggioPrima = compagno.get(TipoAttributo.CORAGGIO);
        int valorePrima = compagno.get(TipoAttributo.VALORE);
        int magiaPrima = mago.getMagia();

        GridoDiBattaglia grido = (GridoDiBattaglia) FabbricaIncantesimi.crea(TipoIncantesimo.GRIDO_DI_BATTAGLIA, 9);
        grido.formula(mago, null, gruppo);

        assertEquals(9, grido.getLivello());
        assertEquals(6, grido.getPuntiCoraggio());
        assertEquals(6, grido.getPuntiValore());
        assertTrue(haIlModificatore(mago, GridoDiBattaglia.NOTA));
        assertTrue(haIlModificatore(compagno, GridoDiBattaglia.NOTA));
        assertFalse(haIlModificatore(nemico, GridoDiBattaglia.NOTA));
        assertEquals(coraggioPrima + 6, compagno.get(TipoAttributo.CORAGGIO));
        assertEquals(valorePrima + 6, compagno.get(TipoAttributo.VALORE));
        assertEquals(magiaPrima - TipoIncantesimo.GRIDO_DI_BATTAGLIA.getCostoLancio(), mago.getMagia());
        assertTrue(frasi.stream().anyMatch(f -> f.contains("grido di battaglia")), frasi.toString());
        assertTrue(StatoDelCombattimento.getIstanza().haLanciatoUnIncantesimo(mago));
    }

    @Test
    void ilGridoDiBattagliaServeSoloSeQualcunoDelGruppoNonLaGiaSubisce() {
        GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
        gruppo.aggiungiPersonaggio(mago);
        GridoDiBattaglia grido = new GridoDiBattaglia(3);
        assertTrue(grido.serve(gruppo.getPersonaggi(), Collections.emptyList()));
        grido.formula(mago, null, gruppo);
        assertFalse(grido.serve(gruppo.getPersonaggi(), Collections.emptyList()));
        assertFalse(grido.serve(Collections.emptyList(), Collections.emptyList()));
        Personaggio caduto = new Mago("Caduto", 4);
        caduto.subSalute(caduto.getSalute(), null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        assertFalse(grido.serve(Collections.singletonList(caduto), Collections.emptyList()));
    }

    // --- Terremoto

    @Test
    void ilTerremotoFeriscePiuAvversariECapitaCheNeFacciaCadere() {
        List<Personaggio> titani = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            titani.add(nemico(TipoPersonaggio.TITANO, 20));
        }
        Personaggio debole = nemico(TipoPersonaggio.GOBLIN, 1);
        int magiaPrima = mago.getMagia();

        FabbricaIncantesimi.crea(TipoIncantesimo.TERREMOTO, 10).formula(mago, null, avversari);

        long feriti = titani.stream().filter(t -> t.getSalute() < t.getSaluteMassima()).count();
        long caduti = titani.stream().filter(t -> t.hasEffettoDiStato(TipoEffettoDiStato.ATTERRATO)).count();
        assertTrue(feriti > 1, "colpisce più di un avversario: " + feriti);
        assertTrue(caduti > 0 && caduti < feriti, "qualcuno cade, non tutti: " + caduti + " su " + feriti);
        assertTrue(debole.isFuoriCombattimento(), "il più debole non regge il colpo");
        assertFalse(debole.hasEffettoDiStato(TipoEffettoDiStato.ATTERRATO));
        assertEquals(magiaPrima - TipoIncantesimo.TERREMOTO.getCostoLancio(), mago.getMagia());
        assertTrue(frasi.get(0).contains("la terra trema"), frasi.toString());
    }

    @Test
    void ilTerremotoNonFaCadereChiNonPuoCadere() {
        Personaggio saldo = new Titano(20) {
            @Override
            public boolean isImmuneAEffetto(TipoEffettoDiStato effettoStato) {
                return effettoStato == TipoEffettoDiStato.ATTERRATO || super.isImmuneAEffetto(effettoStato);
            }
        };
        avversari.aggiungiPersonaggio(saldo);
        Terremoto terremoto = new Terremoto(10);
        for (int i = 0; i < 40; i++) {
            mago.addMagia(100000);
            terremoto.formula(mago, null, avversari);
            saldo.addSalute(saldo.getSaluteMassima());
        }
        assertTrue(frasi.size() >= 40);
        assertFalse(saldo.hasEffettoDiStato(TipoEffettoDiStato.ATTERRATO));
    }

    @Test
    void ilTerremotoServeSoloSeQualcunoPuoEssereFerito() {
        Terremoto terremoto = new Terremoto(10);
        assertEquals(CostantiIncantesimi.INCANTESIMO_TERREMOTO_DANNI, terremoto.getDanni());
        assertEquals(TipoDanno.TERRA, terremoto.getTipoDanno());
        assertFalse(terremoto.serve(Collections.emptyList(), Collections.emptyList()));
        assertTrue(terremoto.serve(Collections.emptyList(), Collections.singletonList(nemico(TipoPersonaggio.GOBLIN, 1))));
        assertFalse(terremoto.serve(Collections.emptyList(), Collections.singletonList(nemico(TipoPersonaggio.GARGOYLE, 5))));
        Personaggio caduto = nemico(TipoPersonaggio.GOBLIN, 1);
        caduto.subSalute(caduto.getSalute(), null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        assertFalse(terremoto.serve(Collections.emptyList(), Collections.singletonList(caduto)));
        assertFalse(terremoto.serve(Collections.emptyList(), Collections.singletonList(immuneAlDanno(TipoDanno.TERRA))));
    }

    // --- Tempesta

    @Test
    void laTempestaColpisceGliAvversariEICompagniMaNonChiLaEvoca() {
        Personaggio compagno = new Guerriero("Arsenio", 30);
        GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
        gruppo.aggiungiPersonaggio(mago);
        gruppo.aggiungiPersonaggio(compagno);
        List<Personaggio> titani = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            titani.add(nemico(TipoPersonaggio.TITANO, 20));
        }
        int saluteDelMago = mago.getSalute();
        List<Integer> saluteDegliAltri = new ArrayList<>();

        for (int i = 0; i < 30; i++) {
            mago.addMagia(100000);
            new Tempesta(10).formula(mago, null, avversari);
            saluteDegliAltri.add(compagno.getSalute());
            compagno.addSalute(compagno.getSaluteMassima());
            titani.forEach(t -> t.addSalute(t.getSaluteMassima()));
        }

        assertEquals(saluteDelMago, mago.getSalute(), "chi la evoca non si colpisce");
        assertTrue(saluteDegliAltri.stream().anyMatch(salute -> salute < compagno.getSaluteMassima()), "i compagni vengono colpiti");
        assertTrue(frasi.stream().anyMatch(f -> f.contains("tempesta di fulmini")), frasi.toString());
    }

    @Test
    void laTempestaDeiMostriColpisceIlLoroGruppo() {
        // Chi non è del gruppo del giocatore ha per compagni gli avversari: l'altro lato del campo
        Personaggio strega = nemico(TipoPersonaggio.STREGA, 5);
        Personaggio compagnoDellaStrega = nemico(TipoPersonaggio.TITANO, 20);
        GruppoGiocatore.getIstanza().aggiungiPersonaggio(new Titano(20));
        Gruppo bersagli = GruppoGiocatore.getIstanza();
        for (int i = 0; i < 30; i++) {
            strega.addMagia(100000);
            new Tempesta(10).formula(strega, null, bersagli);
            compagnoDellaStrega.addSalute(compagnoDellaStrega.getSaluteMassima());
        }
        assertEquals(strega.getSaluteMassima(), strega.getSalute());
    }

    @Test
    void laTempestaServeSeColpisceChiPiuDiChiLaLancia() {
        Tempesta tempesta = new Tempesta(10);
        assertEquals(CostantiIncantesimi.INCANTESIMO_TEMPESTA_DANNI, tempesta.getDanni());
        assertEquals(TipoDanno.FULMINE, tempesta.getTipoDanno());
        Personaggio goblin = nemico(TipoPersonaggio.GOBLIN, 1);
        Personaggio altro = nemico(TipoPersonaggio.GOBLIN, 1);
        Personaggio compagno = new Mago("Gandalf", 4);
        Personaggio caduto = new Mago("Caduto", 4);
        caduto.subSalute(caduto.getSalute(), null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        // Da solo contro uno: nessun compagno da colpire
        assertTrue(tempesta.serve(Collections.singletonList(mago), Collections.singletonList(goblin)));
        assertTrue(tempesta.serve(Collections.emptyList(), Collections.singletonList(goblin)));
        // Con un compagno vivo: uno contro uno non basta, due contro uno sì; i caduti non contano
        assertFalse(tempesta.serve(java.util.Arrays.asList(mago, compagno), Collections.singletonList(goblin)));
        assertTrue(tempesta.serve(java.util.Arrays.asList(mago, compagno, caduto), java.util.Arrays.asList(goblin, altro)));
        // Chi è immune al fulmine, o è già a terra, non è da colpire
        assertFalse(tempesta.serve(Collections.singletonList(mago), Collections.singletonList(nemico(TipoPersonaggio.GARGOYLE, 5))));
        assertFalse(tempesta.serve(Collections.singletonList(mago), Collections.singletonList(immuneAlDanno(TipoDanno.FULMINE))));
        altro.subSalute(altro.getSalute(), null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        assertFalse(tempesta.serve(java.util.Arrays.asList(mago, compagno), java.util.Arrays.asList(goblin, altro)));
    }

    // --- Terrore

    @Test
    void ilTerroreSpaventaChiNonResisteELasciaStareChiResiste() {
        List<Personaggio> titani = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            titani.add(nemico(TipoPersonaggio.TITANO, 40));
        }
        Personaggio immune = new Titano(20) {
            @Override
            public boolean isImmuneAEffetto(TipoEffettoDiStato effettoStato) {
                return effettoStato == TipoEffettoDiStato.SPAVENTATO || super.isImmuneAEffetto(effettoStato);
            }
        };
        avversari.aggiungiPersonaggio(immune);
        Personaggio gargoyle = nemico(TipoPersonaggio.GARGOYLE, 5);
        int magiaPrima = mago.getMagia();

        FabbricaIncantesimi.crea(TipoIncantesimo.TERRORE, 10).formula(mago, null, avversari);

        long spaventati = titani.stream().filter(t -> t.hasEffettoDiStato(TipoEffettoDiStato.SPAVENTATO)).count();
        assertTrue(spaventati > 0 && spaventati < titani.size(), "qualcuno resiste, qualcuno no: " + spaventati);
        assertFalse(immune.hasEffettoDiStato(TipoEffettoDiStato.SPAVENTATO));
        assertFalse(gargoyle.hasEffettoDiStato(TipoEffettoDiStato.SPAVENTATO));
        assertTrue(frasi.stream().anyMatch(f -> f.endsWith(" resiste al terrore.")), frasi.toString());
        assertTrue(frasi.stream().anyMatch(f -> f.startsWith("Il terrore non ha effetto su ")), frasi.toString());
        assertEquals(magiaPrima - TipoIncantesimo.TERRORE.getCostoLancio(), mago.getMagia());
        assertEquals(0, new Terrore(3).getDanni());
    }

    @Test
    void ilTerroreServeSoloControChiPuoEssereSpaventato() {
        Terrore terrore = new Terrore(10);
        assertEquals(TipoDanno.PSICHICO, terrore.getTipoDanno());
        Personaggio goblin = nemico(TipoPersonaggio.GOBLIN, 1);
        assertFalse(terrore.serve(Collections.emptyList(), Collections.emptyList()));
        assertTrue(terrore.serve(Collections.emptyList(), Collections.singletonList(goblin)));
        assertFalse(terrore.serve(Collections.emptyList(), Collections.singletonList(nemico(TipoPersonaggio.GARGOYLE, 5))));
        Personaggio caduto = nemico(TipoPersonaggio.GOBLIN, 1);
        caduto.subSalute(caduto.getSalute(), null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        assertFalse(terrore.serve(Collections.emptyList(), Collections.singletonList(caduto)));
        Personaggio spaventato = nemico(TipoPersonaggio.GOBLIN, 1);
        spaventato.addEffettoDiStato(TipoEffettoDiStato.SPAVENTATO, 3, 0);
        assertFalse(terrore.serve(Collections.emptyList(), Collections.singletonList(spaventato)));
        Personaggio coraggioso = new Goblin(1) {
            @Override
            public boolean isImmuneAEffetto(TipoEffettoDiStato effettoStato) {
                return effettoStato == TipoEffettoDiStato.SPAVENTATO || super.isImmuneAEffetto(effettoStato);
            }
        };
        assertFalse(terrore.serve(Collections.emptyList(), Collections.singletonList(coraggioso)));
    }
}
