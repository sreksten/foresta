package com.threeamigos.foresta.incantesimi;

import com.threeamigos.foresta.combattimento.StatoDelCombattimento;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.gruppi.GruppoAvversario;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.modellodati.GruppoGiocatoreMD;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Goblin;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.personaggi.Mago;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
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
 * Gli incantesimi: la fabbrica, quelli che fanno danno a uno o più bersagli, il silenzio che sceglie chi mettere a tacere, la morte che si ritorce
 * contro chi la formula quando fallisce, e la resurrezione.
 */
class IncantesimiTest {

    private final List<String> frasi = new ArrayList<>();
    private Runnable iscrizione;
    private GruppoAvversario avversari;

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
    }

    @AfterEach
    void ripulisce() {
        iscrizione.run();
        BusEventi.impostaConsegna(null);
        StatoDelCombattimento.azzeraIstanza();
        GruppoAvversario.azzeraIstanza();
        Dado.ripristina();
    }

    private static Personaggio mago() {
        Personaggio p = new Mago("Merlino", 10);
        p.addMagia(100000);
        return p;
    }

    private Personaggio nemico(TipoPersonaggio tipo, int livello) {
        Personaggio p = FabbricaPersonaggi.crea(tipo, livello);
        avversari.aggiungiPersonaggio(p);
        return p;
    }

    // --- La fabbrica

    @Test
    void laFabbricaCostruisceOgniIncantesimo() {
        for (TipoIncantesimo tipo : TipoIncantesimo.values()) {
            Incantesimo incantesimo = FabbricaIncantesimi.crea(tipo, 4);
            assertEquals(tipo, incantesimo.getClasse());
            assertEquals(4, incantesimo.getLivello());
            assertEquals(tipo.getCostoLancio(), incantesimo.getCostoLancio());
            if (incantesimo instanceof IncantesimoMalefico) {
                IncantesimoMalefico malefico = (IncantesimoMalefico) incantesimo;
                assertTrue(malefico.isMagica());
                assertFalse(malefico.isIncantata());
                assertTrue(malefico.getIncantamenti().isEmpty());
                assertNotNull(malefico.getTipoDanno());
                assertTrue(malefico.getDanni() >= 0);
                assertEquals(com.threeamigos.foresta.tipi.CategoriaIncantesimo.MALEFICO, ((IncantesimoMaleficoImpl) malefico).getTipo());
            }
        }
        for (int i = 0; i < 50; i++) {
            assertNotNull(FabbricaIncantesimi.casuale());
        }
    }

    // --- Quelli che fanno danno

    @Test
    void unIncantesimoDiDannoColpisceUnBersaglioOUnGruppo() {
        Personaggio mago = mago();
        List<Personaggio> nemici = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            nemici.add(nemico(TipoPersonaggio.TITANO, 20));
        }
        for (TipoIncantesimo tipo : new TipoIncantesimo[] {TipoIncantesimo.ARIA, TipoIncantesimo.ACQUA, TipoIncantesimo.TERRA, TipoIncantesimo.FUOCO,
                TipoIncantesimo.FULMINE, TipoIncantesimo.GELO, TipoIncantesimo.VELENO}) {
            Incantesimo incantesimo = FabbricaIncantesimi.crea(tipo, 3);
            mago.addMagia(100000);
            incantesimo.formula(mago, nemici.get(0), null);
            mago.addMagia(100000);
            incantesimo.formula(mago, null, avversari);
        }
        assertTrue(frasi.stream().anyMatch(f -> f.contains(" contro il gruppo.")), frasi.toString());
        assertTrue(StatoDelCombattimento.getIstanza().haLanciatoUnIncantesimo(mago));
        // Con un solo avversario l'annuncio nomina lui
        GruppoAvversario.azzeraIstanza();
        avversari = GruppoAvversario.getIstanza();
        nemico(TipoPersonaggio.TITANO, 20);
        frasi.clear();
        mago.addMagia(100000);
        FabbricaIncantesimi.crea(TipoIncantesimo.FUOCO, 3).formula(mago, null, avversari);
        assertTrue(frasi.get(0).contains(" contro il Titano."), frasi.toString());
        // Senza bersaglio né gruppo non si sa che fare
        assertThrows(IllegalArgumentException.class, () -> FabbricaIncantesimi.crea(TipoIncantesimo.FUOCO, 3).formula(mago, null, null));
    }

    @Test
    void unIncantesimoDiGruppoSenzaMultiploNonRipartisceIlDanno() {
        Personaggio mago = mago();
        nemico(TipoPersonaggio.TITANO, 20);
        nemico(TipoPersonaggio.TITANO, 20);
        // Il silenzio è di gruppo (non multiplo): scelto un bersaglio solo; qui lo si forza sulla classe base
        IncantesimoMaleficoImpl incantesimo = new IncantesimoMaleficoImpl(3) {
            @Override
            public TipoIncantesimo getClasse() {
                return TipoIncantesimo.SILENZIO;
            }

            @Override
            public TipoDanno getTipoDanno() {
                return TipoDanno.ARCANO;
            }

            @Override
            public int getDanni() {
                return 1;
            }
        };
        incantesimo.formula(mago, null, avversari);
        assertTrue(frasi.stream().anyMatch(f -> f.contains(" contro il gruppo.")), frasi.toString());
    }

    @Test
    void unIncantesimoGlobaleSiAnnunciaSenzaBersagli() {
        Personaggio mago = mago();
        mago.addMagia(100000);
        int magiaPrima = mago.getMagia();
        IncantesimoMaleficoImpl incantesimo = new IncantesimoMaleficoImpl(3) {
            @Override
            public TipoIncantesimo getClasse() {
                return TipoIncantesimo.values()[0];
            }

            @Override
            public TipoDanno getTipoDanno() {
                return TipoDanno.ARCANO;
            }

            @Override
            public int getDanni() {
                return 1;
            }
        };
        // Nessun incantesimo del gioco è globale: si prova con la formula globale della classe base
        incantesimo.formulaGlobale(mago, null);
        assertTrue(frasi.get(frasi.size() - 1).contains(" formula un "), frasi.toString());
        assertEquals(magiaPrima, mago.getMagia());
    }

    @Test
    void unIncantesimoMultiploSiLimitaAiBersagliCheChiLoLanciaPuoColpire() {
        Personaggio debole = new Guerriero("Pippo", 1);
        debole.addModificatore(new com.threeamigos.foresta.modellodati.ModificatoreAttributoMD(com.threeamigos.foresta.tipi.TipoAttributo.NUMERO_BERSAGLI,
                com.threeamigos.foresta.tipi.TipoModificatore.QUANTITA_ASSOLUTA, 1));
        for (int i = 0; i < 4; i++) {
            nemico(TipoPersonaggio.TITANO, 20);
        }
        debole.addMagia(100000);
        FabbricaIncantesimi.crea(TipoIncantesimo.FUOCO, 1).formula(debole, null, avversari);
        assertTrue(frasi.get(0).contains(" contro il Titano 1."), frasi.toString());
    }

    // --- Silenzio

    @Test
    void ilSilenzioMetteATacereChiNonLoEGiaEOffreChiHaIlSilenzioPiuCorto() {
        Personaggio mago = mago();
        Silenzio silenzio = (Silenzio) FabbricaIncantesimi.crea(TipoIncantesimo.SILENZIO, 3);
        assertEquals(TipoIncantesimo.SILENZIO, silenzio.getClasse());
        assertEquals(TipoDanno.ARCANO, silenzio.getTipoDanno());
        assertEquals(0, silenzio.getDanni());
        silenzio.formula(mago, null, avversari);
        assertTrue(frasi.isEmpty(), "nessuno da mettere a tacere");
        assertNull(Silenzio.scegliBersaglio(Collections.emptyList()));
        Personaggio a = nemico(TipoPersonaggio.TITANO, 20);
        Personaggio b = nemico(TipoPersonaggio.TITANO, 20);
        Personaggio c = nemico(TipoPersonaggio.TITANO, 20);
        a.addEffettoDiStato(TipoEffettoDiStato.SILENZIATO, 3, 0);
        b.addEffettoDiStato(TipoEffettoDiStato.SILENZIATO, 1, 0);
        assertSame(c, Silenzio.scegliBersaglio(avversari.getPersonaggi()), "chi non è silenziato va per primo");
        c.addEffettoDiStato(TipoEffettoDiStato.SILENZIATO, 2, 0);
        assertSame(b, Silenzio.scegliBersaglio(avversari.getPersonaggi()), "poi quello che lo è da meno");
        // Su un bersaglio che si colpisce sempre (a terra) riesce
        Personaggio atterrato = nemico(TipoPersonaggio.TITANO, 20);
        atterrato.addEffettoDiStato(TipoEffettoDiStato.ATTERRATO, 2, 0);
        mago.addMagia(100000);
        silenzio.formula(mago, atterrato, null);
        assertTrue(atterrato.hasEffettoDiStato(TipoEffettoDiStato.SILENZIATO));
        // Immune
        Personaggio immune = new Goblin(5) {
            @Override
            public boolean isImmuneAEffetto(TipoEffettoDiStato effettoStato) {
                return effettoStato == TipoEffettoDiStato.SILENZIATO || super.isImmuneAEffetto(effettoStato);
            }
        };
        avversari.aggiungiPersonaggio(immune);
        mago.addMagia(100000);
        silenzio.formula(mago, immune, null);
        assertTrue(frasi.stream().anyMatch(f -> f.startsWith("L'incantesimo non ha effetto su")), frasi.toString());
        // Mancato: chi lancia è stordito
        Personaggio stordito = mago();
        stordito.addEffettoDiStato(TipoEffettoDiStato.STORDITO, 3, 0);
        Personaggio bersaglio = nemico(TipoPersonaggio.TITANO, 20);
        silenzio.formula(stordito, bersaglio, null);
        assertTrue(frasi.stream().anyMatch(f -> f.startsWith("L'incantesimo non va a segno")), frasi.toString());
        // Sul gruppo sceglie lui
        mago.addMagia(100000);
        silenzio.formula(mago, null, avversari);
        // Né un bersaglio né un gruppo: non si sa che fare
        assertThrows(IllegalArgumentException.class, () -> silenzio.formula(mago, null, null));
        // Chi è immune a quell'incantesimo in particolare
        Personaggio resistente = new Goblin(5) {
            @Override
            protected boolean isImmuneAIncantesimoPerIstanza(TipoIncantesimo tipoIncantesimo) {
                return tipoIncantesimo == TipoIncantesimo.SILENZIO;
            }
        };
        avversari.aggiungiPersonaggio(resistente);
        mago.addMagia(100000);
        silenzio.formula(mago, resistente, null);
        assertTrue(frasi.stream().anyMatch(f -> f.startsWith("L'incantesimo non ha effetto su il Goblin") || f.startsWith("L'incantesimo non ha effetto su")), frasi.toString());
    }

    // --- Morte

    @Test
    void laMorteUccideChiEQuasiMortoESiRitorceControChiFallisce() {
        Personaggio mago = mago();
        Morte morte = (Morte) FabbricaIncantesimi.crea(TipoIncantesimo.MORTE, 3);
        assertEquals(TipoDanno.NECROTICO, morte.getTipoDanno());
        assertTrue(morte.getDanni() >= 0);
        assertThrows(IllegalArgumentException.class, () -> morte.formula(mago, null, avversari));
        // Già morto: niente
        Personaggio morto = nemico(TipoPersonaggio.GOBLIN, 5);
        morto.muore("prova");
        morte.formula(mago, morto, null);
        // Immune
        Personaggio scheletro = nemico(TipoPersonaggio.SCHELETRO, 5);
        mago.addMagia(100000);
        morte.formula(mago, scheletro, null);
        assertTrue(frasi.stream().anyMatch(f -> f.startsWith("L'incantesimo non ha effetto su")), frasi.toString());
        // Quasi morto e colpito (a terra): muore
        Personaggio goblin = nemico(TipoPersonaggio.GOBLIN, 5);
        goblin.getModelloDati().setSalute(1);
        goblin.addEffettoDiStato(TipoEffettoDiStato.ATTERRATO, 2, 0);
        assertTrue(Morte.puoUccidere(goblin));
        mago.addMagia(100000);
        morte.formula(mago, goblin, null);
        assertFalse(goblin.isVivo());
        assertTrue(frasi.stream().anyMatch(f -> f.startsWith("L'incantesimo ha ucciso")), frasi.toString());
        // In piena salute non si può uccidere: si ritorce contro chi lo formula
        Personaggio sano = nemico(TipoPersonaggio.TITANO, 20);
        sano.addEffettoDiStato(TipoEffettoDiStato.ATTERRATO, 2, 0);
        assertFalse(Morte.puoUccidere(sano));
        int salute = mago.getSalute();
        mago.addMagia(100000);
        morte.formula(mago, sano, null);
        assertTrue(mago.getSalute() < salute);
        assertTrue(frasi.stream().anyMatch(f -> f.contains("si è ritorto contro")), frasi.toString());
        // Quasi morto ma mancato
        Personaggio quasi = nemico(TipoPersonaggio.GOBLIN, 5);
        quasi.getModelloDati().setSalute(1);
        Personaggio stordito = mago();
        stordito.addEffettoDiStato(TipoEffettoDiStato.STORDITO, 3, 0);
        morte.formula(stordito, quasi, null);
        assertTrue(quasi.isVivo());
    }

    // --- Resurrezione

    @Test
    void laResurrezioneRiportaInVitaOReintegra() {
        Personaggio mago = mago();
        Resurrezione resurrezione = (Resurrezione) FabbricaIncantesimi.crea(TipoIncantesimo.RESURREZIONE, 3);
        assertEquals(TipoIncantesimo.RESURREZIONE, resurrezione.getClasse());
        Personaggio caduto = new Guerriero("Caduto", 5);
        caduto.muore("prova");
        resurrezione.formula(mago, caduto, null);
        assertTrue(caduto.isVivo());
        assertTrue(frasi.stream().anyMatch(f -> f.contains("è risort") && f.endsWith("dalle proprie ceneri.")), frasi.toString());
        caduto.subSalute(caduto.getSalute() / 2, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        mago.addMagia(100000);
        resurrezione.formula(mago, caduto, null);
        assertEquals(caduto.getSaluteMassima(), caduto.getSalute());
        assertTrue(frasi.stream().anyMatch(f -> f.contains("era già viv")), frasi.toString());
        assertSame(caduto, StatoDelCombattimento.getIstanza().getUltimoAlleato(mago));
        assertNotNull(new Goblin(1));
    }
}
