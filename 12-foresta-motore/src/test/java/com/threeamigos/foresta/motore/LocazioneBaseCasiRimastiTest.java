package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.artefatti.GeneratoreArtefatti;
import com.threeamigos.foresta.combattimento.StatoDelCombattimento;
import com.threeamigos.foresta.gruppi.GruppoAvversario;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.locazioni.LocazioneBase;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.oggetti.Bottino;
import com.threeamigos.foresta.personaggi.Drago;
import com.threeamigos.foresta.personaggi.Goblin;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.personaggi.Necromante;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.personaggi.Scheletro;
import com.threeamigos.foresta.statistiche.Statistiche;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.tipi.ComandiAssociati;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.CostantiMotore;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoBottino;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.tipi.TipoIncantesimo;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.tipi.TipoRiposo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Casi rari della locazione di base, guidati direttamente con una locazione di prova (un bosco con incontri e oggetti a piacere): la scelta degli
 * incontri e la loro quantità, l'artefatto già in locazione, la furia che finisce, l'oggetto da rivelare, le scelte di chi cura, l'incantesimo
 * che il formulante non può pagare o che gli si ritorce contro, e gli scheletri del Necromante.
 */
class LocazioneBaseCasiRimastiTest {

    @AfterEach
    void ripulisce() {
        Dado.ripristina();
    }

    /**
     * Un bosco di prova, senza incontri né oggetti, a meno che il test non li metta.
     */
    private static class Prova extends LocazioneBase {
        TipoPersonaggio[] incontri = new TipoPersonaggio[0];
        TipoBottino[] oggetti = new TipoBottino[0];
        Artefatto artefatto;
        boolean visitata;

        @Override
        public TipoLocazione getTipoLocazione() {
            return TipoLocazione.BOSCO;
        }

        @Override
        public void descrivi(GruppoGiocatore g, GruppoAvversario gng) {
        }

        @Override
        public TipoRiposo getTipoRiposo() {
            return TipoRiposo.ALL_APERTO_SENZA_FUOCO;
        }

        @Override
        protected TipoPersonaggio[] getPossibiliIncontri() {
            return incontri;
        }

        @Override
        protected TipoBottino[] getPossibiliOggetti() {
            return oggetti;
        }

        @Override
        protected Artefatto getArtefatto(GruppoGiocatore g) {
            return artefatto;
        }

        @Override
        protected boolean isLocazioneVisitata() {
            return visitata;
        }
    }

    /**
     * Una locazione che non ridefinisce niente di quel che la base lascia alle locazioni vere.
     */
    private static final class Vuota extends LocazioneBase {
        @Override
        public TipoLocazione getTipoLocazione() {
            return TipoLocazione.BOSCO;
        }

        @Override
        public void descrivi(GruppoGiocatore g, GruppoAvversario gng) {
        }

        @Override
        public TipoRiposo getTipoRiposo() {
            return TipoRiposo.ALL_APERTO_SENZA_FUOCO;
        }
    }

    private static PartitaDiTest partita(long seme, Comando classe) {
        PartitaDiTest partita = PartitaDiTest.nuova(seme);
        partita.iniziaCon("Arsenio", Comando.MASCHIO, classe, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
        partita.assertStato(Stato.IN_LOCAZIONE);
        for (int x = 0; x < Foresta.getDimensioneX(); x++) {
            for (int y = 0; y < Foresta.getDimensioneY(); y++) {
                if (Foresta.getLocazione(x, y) == TipoLocazione.BOSCO) {
                    Foresta.setPosizioneDelGruppo(new CoordinateMD(x, y));
                    GruppoAvversario.getIstanza().reimposta();
                    StatoDelCombattimento.getIstanza().reimposta();
                    return partita;
                }
            }
        }
        throw new IllegalStateException("nessun bosco nella Foresta");
    }

    private static <T extends LocazioneBase> T inQuestoBosco(T locazione) {
        locazione.setModelloDati(Foresta.getLocazioneMD(Foresta.getPosizioneDelGruppo()));
        return locazione;
    }

    @Test
    void ilNumeroEIGliIncontriPossibiliDipendonoDalLivelloDelCapo() {
        TipoPersonaggio[] soloForti = {TipoPersonaggio.GIGANTE, TipoPersonaggio.TITANO};
        // All'inizio morbido, se tutti i mostri sarebbero esclusi, restano quelli che c'erano
        assertArrayEquals(soloForti, LocazioneBase.incontriPossibili(soloForti, 1));
        assertArrayEquals(new TipoPersonaggio[]{TipoPersonaggio.GOBLIN},
                LocazioneBase.incontriPossibili(new TipoPersonaggio[]{TipoPersonaggio.GOBLIN, TipoPersonaggio.TROLL}, 1));
        assertArrayEquals(soloForti, LocazioneBase.incontriPossibili(soloForti, 30));

        assertEquals(1, LocazioneBase.numeroMassimoDiMostri(1, 1));
        assertEquals(3, LocazioneBase.numeroMassimoDiMostri(5, 2));
        assertEquals(4, LocazioneBase.numeroMassimoDiMostri(10, 2));
        assertEquals(6, LocazioneBase.numeroMassimoDiMostri(20, 2));
    }

    @Test
    void unaLocazioneSenzaIncontriNeOggettiNonCreaNiente() {
        try (PartitaDiTest partita = partita(71, Comando.GUERRIERO)) {
            Vuota vuota = inQuestoBosco(new Vuota());
            GruppoAvversario avversari = GruppoAvversario.getIstanza();

            vuota.crea(partita.gruppo(), avversari);

            assertEquals(0, avversari.getNumeroPersonaggi());
            assertNull(vuota.getOggetto());
        }
    }

    @Test
    void conIncontriEOggettiLaCreazioneLiPopola() {
        try (PartitaDiTest partita = partita(72, Comando.GUERRIERO)) {
            // A partita avviata (non al primo turno) un incontro riesce con un tiro fino a 90
            Statistiche.incrementaTurniGiocati();
            Prova prova = inQuestoBosco(new Prova());
            prova.incontri = new TipoPersonaggio[]{TipoPersonaggio.GOBLIN};
            prova.oggetti = new TipoBottino[]{TipoBottino.MONETA};
            GruppoAvversario avversari = GruppoAvversario.getIstanza();
            Dado.trucca(10);

            prova.crea(partita.gruppo(), avversari);

            assertTrue(avversari.getNumeroPersonaggi() > 0);
            assertNotNull(prova.getOggetto());
        }
    }

    @Test
    void unArtefattoGiaPresenteInLocazioneDiventaIlSuoOggetto() {
        try (PartitaDiTest partita = partita(73, Comando.GUERRIERO)) {
            Prova prova = inQuestoBosco(new Prova());
            prova.artefatto = GeneratoreArtefatti.istanza().generaArtefatto(TipoArtefatto.SPADA, 1);

            prova.crea(partita.gruppo(), GruppoAvversario.getIstanza());

            assertTrue(prova.getOggetto().getArtefatto().isPresent());
        }
    }

    @Test
    void laFuriaFinisceQuandoSiAzzeraLaLocazione() {
        try (PartitaDiTest partita = partita(74, Comando.GUERRIERO)) {
            Prova prova = inQuestoBosco(new Prova());
            Personaggio capo = partita.gruppo().getCapo();
            capo.addEffettoDiStato(TipoEffettoDiStato.BERSERK, 5, 0);
            assertTrue(capo.hasEffettoDiStato(TipoEffettoDiStato.BERSERK));

            prova.azzeraLocazione(partita.gruppo());

            assertFalse(capo.hasEffettoDiStato(TipoEffettoDiStato.BERSERK));
        }
    }

    @Test
    void unOggettoDaRivelareNonSiSostituisceConQuelloDiUnaMissione() {
        try (PartitaDiTest partita = partita(75, Comando.GUERRIERO)) {
            Prova prova = inQuestoBosco(new Prova());
            Bottino nascosto = new BottinoDaRivelare();
            Bottino diMissione = new BottinoDaRivelare();

            prova.collocaOggettoMissione(nascosto);
            prova.collocaOggettoMissione(diMissione);

            assertSame(nascosto, prova.getOggetto());
        }
    }

    // Un oggetto che si rivela solo a certe condizioni
    private static final class BottinoDaRivelare implements Bottino {
        @Override
        public TipoBottino getClasse() {
            return TipoBottino.MONETA;
        }

        @Override
        public String getNomeSingolare() {
            return "tesoro";
        }

        @Override
        public String getNomePlurale() {
            return "tesori";
        }

        @Override
        public int getQuantita() {
            return 1;
        }

        @Override
        public boolean isDaRivelare() {
            return true;
        }

        @Override
        public String getAIS() {
            return "un";
        }

        @Override
        public String getAIP() {
            return "alcuni";
        }

        @Override
        public String getADS() {
            return "il";
        }

        @Override
        public String getADP() {
            return "i";
        }
    }

    // La locazione di prova già entrata (il primo passo la fa uscire da NUOVA_LOCAZIONE), con i suoi avversari
    private static Prova inCombattimentoCon(PartitaDiTest partita, Personaggio... avversari) {
        GruppoAvversario nemici = GruppoAvversario.getIstanza();
        for (Personaggio avversario : avversari) {
            nemici.aggiungiPersonaggio(avversario);
        }
        Prova prova = inQuestoBosco(new Prova());
        prova.impostaAzioni(partita.gruppo(), nemici, null);
        return prova;
    }

    private static Stato formula(Prova prova, GruppoGiocatore gruppo, TipoIncantesimo incantesimo) {
        prova.impostaAzioni(gruppo, GruppoAvversario.getIstanza(), Comando.INCANTESIMO);
        prova.impostaAzioni(gruppo, GruppoAvversario.getIstanza(), Comando.PERSONAGGIO_1);
        return prova.impostaAzioni(gruppo, GruppoAvversario.getIstanza(), ComandiAssociati.di(incantesimo));
    }

    @Test
    void chiNonHaLaMagiaPerUnIncantesimoNonLoFormula() {
        try (PartitaDiTest partita = partita(76, Comando.GUERRIERO)) {
            Prova prova = inCombattimentoCon(partita, new Goblin(1));
            partita.gruppo().getCapo().getModelloDati().set(TipoAttributo.MAGIA, 0);

            Stato stato = formula(prova, partita.gruppo(), TipoIncantesimo.FUOCO);

            assertTrue(partita.testi().stream().anyMatch(t -> t.contains("non permette di formulare questo incantesimo")), String.valueOf(partita.testi()));
            assertTrue(stato == Stato.IN_LOCAZIONE || stato == Stato.GIOCO_PERSO, String.valueOf(stato));
        }
    }

    @Test
    void chiNonHaLaMagiaPerUnIncantesimoEVieneUccisoDagliAvversariPerde() {
        try (PartitaDiTest partita = partita(77, Comando.GUERRIERO)) {
            partita.gruppo().getCapo().subSalute(partita.gruppo().getCapo().getSalute() - 1, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
            Prova prova = inCombattimentoCon(partita, new Drago(30), new Drago(30), new Drago(30));
            partita.gruppo().getCapo().getModelloDati().set(TipoAttributo.MAGIA, 0);

            Stato stato = formula(prova, partita.gruppo(), TipoIncantesimo.FUOCO);

            assertEquals(Stato.GIOCO_PERSO, stato);
        }
    }

    @Test
    void laResurrezioneChiedeSuChiEIlGiocatorePuoAnnullare() {
        try (PartitaDiTest partita = partita(79, Comando.MAGO)) {
            GruppoGiocatore gruppo = partita.gruppo();
            gruppo.addIncantesimi(TipoIncantesimo.RESURREZIONE, 1);
            Personaggio caduto = new Guerriero("Caduto", 1);
            gruppo.aggiungiPersonaggio(caduto);
            caduto.muore("prova");
            Prova prova = inCombattimentoCon(partita, new Goblin(1));
            // Con un compagno morto fra i vivi e uno tra i curabili la scelta va fatta, e di cura si tratta
            Stato scelta = formula(prova, gruppo, TipoIncantesimo.RESURREZIONE);
            assertEquals(Stato.SCELTA_PERSONAGGIO_QUALSIASI, scelta);
            assertTrue(prova.isSceltaDiUnaCura());

            // Annullare non fa passare nessun turno
            assertEquals(Stato.IN_LOCAZIONE, prova.impostaAzioni(gruppo, GruppoAvversario.getIstanza(), Comando.ANNULLA));
            assertFalse(prova.isSceltaDiUnaCura());

            // Poi la si formula davvero sul caduto
            prova.impostaAzioni(gruppo, GruppoAvversario.getIstanza(), Comando.INCANTESIMO);
            prova.impostaAzioni(gruppo, GruppoAvversario.getIstanza(), Comando.PERSONAGGIO_1);
            prova.impostaAzioni(gruppo, GruppoAvversario.getIstanza(), ComandiAssociati.di(TipoIncantesimo.RESURREZIONE));
            Stato dopo = prova.impostaAzioni(gruppo, GruppoAvversario.getIstanza(), Comando.PERSONAGGIO_2);
            assertNotNull(dopo);
            assertTrue(caduto.isVivo());
        }
    }

    @Test
    void unIncantesimoGlobaleMaleficoAgisceSulGruppoAvversarioEConsumaLaPergamena() {
        try (PartitaDiTest partita = partita(81, Comando.MAGO)) {
            GruppoGiocatore gruppo = partita.gruppo();
            gruppo.addIncantesimi(TipoIncantesimo.NEBBIA, 1);
            int pergamene = gruppo.getIncantesimi(TipoIncantesimo.NEBBIA);
            gruppo.getCapo().addMagia(100);
            Personaggio goblin = new Goblin(1);
            Prova prova = inCombattimentoCon(partita, goblin);

            Stato stato = formula(prova, gruppo, TipoIncantesimo.NEBBIA);

            assertNotNull(stato);
            assertEquals(pergamene - 1, gruppo.getIncantesimi(TipoIncantesimo.NEBBIA));
            assertTrue(goblin.getModificatoriTemporanei().stream().anyMatch(m -> "Nebbia".equals(m.getNote())));
            assertTrue(gruppo.getCapo().getModificatoriTemporanei().stream().noneMatch(m -> "Nebbia".equals(m.getNote())));
        }
    }

    @Test
    void unIncantesimoGlobaleBeneficoAgisceSulGruppoDelGiocatore() {
        try (PartitaDiTest partita = partita(82, Comando.MAGO)) {
            GruppoGiocatore gruppo = partita.gruppo();
            gruppo.addIncantesimi(TipoIncantesimo.GRIDO_DI_BATTAGLIA, 1);
            int pergamene = gruppo.getIncantesimi(TipoIncantesimo.GRIDO_DI_BATTAGLIA);
            gruppo.getCapo().addMagia(100);
            Personaggio goblin = new Goblin(1);
            Prova prova = inCombattimentoCon(partita, goblin);

            Stato stato = formula(prova, gruppo, TipoIncantesimo.GRIDO_DI_BATTAGLIA);

            assertNotNull(stato);
            assertEquals(pergamene - 1, gruppo.getIncantesimi(TipoIncantesimo.GRIDO_DI_BATTAGLIA));
            assertTrue(gruppo.getCapo().getModificatoriTemporanei().stream().anyMatch(m -> "Grido di battaglia".equals(m.getNote())));
            assertTrue(goblin.getModificatoriTemporanei().stream().noneMatch(m -> "Grido di battaglia".equals(m.getNote())));
        }
    }

    @Test
    void laPozioneGrandeDellaSaluteEUnaSceltaDiCura() {
        try (PartitaDiTest partita = partita(80, Comando.GUERRIERO)) {
            GruppoGiocatore gruppo = partita.gruppo();
            gruppo.aggiungiPersonaggio(new Guerriero("Secondo", 1));
            gruppo.addPozioniSaluteGrande(1);
            Prova prova = inCombattimentoCon(partita, new Goblin(1));

            prova.impostaAzioni(gruppo, GruppoAvversario.getIstanza(), Comando.POZIONE_SALUTE_GRANDE);

            assertTrue(prova.isSceltaDiUnaCura());
            assertTrue(prova.isSceltaDiChiAgisce());
            assertFalse(prova.isSceltaDiUnAumentoDiLivello());
        }
    }

    @Test
    void alPrimoTurnoConUnSoloIncontroPossibileNonSiTogliel_eremita() {
        try (PartitaDiTest partita = partita(85, Comando.GUERRIERO)) {
            Prova prova = inQuestoBosco(new Prova());
            prova.incontri = new TipoPersonaggio[]{TipoPersonaggio.EREMITA};
            GruppoAvversario avversari = GruppoAvversario.getIstanza();

            prova.crea(partita.gruppo(), avversari);

            // Al primo turno c'è sempre un incontro, e se è l'unico possibile non lo si può escludere
            assertTrue(avversari.getNumeroPersonaggi() > 0);
            assertEquals(TipoPersonaggio.EREMITA, avversari.getPersonaggio(0).getClasse());
        }
    }

    @Test
    void laMappaEL_inventarioSiApronoDallaLocazione() {
        try (PartitaDiTest partita = partita(86, Comando.GUERRIERO)) {
            Prova prova = inCombattimentoCon(partita, new Goblin(1));

            assertEquals(Stato.INVENTARIO, prova.impostaAzioni(partita.gruppo(), GruppoAvversario.getIstanza(), Comando.INVENTARIO));
            assertEquals(Stato.MAPPA, prova.impostaAzioni(partita.gruppo(), GruppoAvversario.getIstanza(), Comando.MAPPA));
        }
    }

    @Test
    void nellaMischiaChePassaDaSolaInterrompereNonSiPuo() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(297)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CASTELLO_IDRA));
            partita.gruppo().getCapo().addEffettoDiStato(TipoEffettoDiStato.STORDITO, 2, 0);
            partita.comando(Comando.COMBATTIMENTO);
            partita.assertStato(Stato.IN_COMBATTIMENTO);

            partita.comando(Comando.INTERRUZIONE_COMBATTIMENTO);

            partita.assertStato(Stato.IN_COMBATTIMENTO);
        }
    }

    @Test
    void sePerStradaGliAvversariSonoStatiTuttiSconfittiBereUnaPozioneChiudeLaLocazione() {
        try (PartitaDiTest partita = partita(87, Comando.GUERRIERO)) {
            GruppoGiocatore gruppo = partita.gruppo();
            Goblin goblin = new Goblin(1);
            Prova prova = inCombattimentoCon(partita, goblin);
            gruppo.addPozioniSalute(1);
            goblin.muore("prova");

            prova.impostaAzioni(gruppo, GruppoAvversario.getIstanza(), Comando.POZIONE_SALUTE);
            Stato stato = prova.impostaAzioni(gruppo, GruppoAvversario.getIstanza(), Comando.PERSONAGGIO_1);

            assertEquals(Stato.FINE_LOCAZIONE, stato);
        }
    }

    @Test
    void ancheNellaMischiaSeGliAvversariSonoFinitiBereUnaPozioneChiudeLaLocazione() {
        try (PartitaDiTest partita = partita(88, Comando.GUERRIERO)) {
            GruppoGiocatore gruppo = partita.gruppo();
            Goblin goblin = new Goblin(1);
            Prova prova = inCombattimentoCon(partita, goblin);
            gruppo.addPozioniSalute(1);
            prova.impostaAzioni(gruppo, GruppoAvversario.getIstanza(), Comando.COMBATTIMENTO);
            Stato inMischia = prova.impostaAzioni(gruppo, GruppoAvversario.getIstanza(), Comando.PERSONAGGIO_1);
            assertEquals(Stato.IN_COMBATTIMENTO, inMischia);
            goblin.muore("prova");

            Stato stato = prova.impostaAzioni(gruppo, GruppoAvversario.getIstanza(), Comando.POZIONE_SALUTE);

            assertEquals(Stato.FINE_LOCAZIONE, stato);
        }
    }

    @Test
    void unaLocazioneGiaCompletaFinisceAnchePerUnComandoChenonFaNiente() {
        try (PartitaDiTest partita = partita(89, Comando.GUERRIERO)) {
            Prova prova = inCombattimentoCon(partita, new Goblin(1));
            prova.getModelloDati().aggiungiProprieta(com.threeamigos.foresta.modellodati.LocazioneMD.COMPLETA,
                    com.threeamigos.foresta.modellodati.LocazioneMD.AFFERMATIVO);

            Stato stato = prova.impostaAzioni(partita.gruppo(), GruppoAvversario.getIstanza(), Comando.ANNULLA);

            assertEquals(Stato.FINE_LOCAZIONE, stato);
        }
    }

    @Test
    void sePassareInosservatiFallisceEGliAvversariNonPossonoAgireNessunoAttaccaPerPrimo() {
        try (PartitaDiTest partita = partita(90, Comando.GUERRIERO)) {
            Goblin stordito = new Goblin(1);
            stordito.addEffettoDiStato(TipoEffettoDiStato.STORDITO, 5, 0);
            Prova prova = inCombattimentoCon(partita, stordito);
            // Il tiro per passare inosservati va male
            Dado.trucca(100);

            Stato stato = prova.impostaAzioni(partita.gruppo(), GruppoAvversario.getIstanza(), Comando.PASSA_INOSSERVATO);

            assertEquals(Stato.IN_LOCAZIONE, stato);
            assertTrue(partita.testi().stream().anyMatch(t -> t.contains("Un rametto si spezza")), String.valueOf(partita.testi()));
        }
    }

    private static void privato(Object oggetto, String campo, Object valore) throws Exception {
        Field f = LocazioneBase.class.getDeclaredField(campo);
        f.setAccessible(true);
        f.set(oggetto, valore);
    }

    @Test
    void cadutoIlNecromanteGliScheletriCrollano() {
        try (PartitaDiTest partita = partita(81, Comando.GUERRIERO)) {
            Necromante necromante = new Necromante(1);
            Scheletro uno = new Scheletro(1);
            Prova prova = inCombattimentoCon(partita, necromante, uno);

            necromante.muore("prova");
            prova.impostaAzioni(partita.gruppo(), GruppoAvversario.getIstanza(), null);

            assertFalse(uno.isVivo());
            assertTrue(partita.testi().stream().anyMatch(t -> t.contains("lo scheletro che teneva in piedi crolla")), String.valueOf(partita.testi()));
        }
        try (PartitaDiTest partita = partita(82, Comando.GUERRIERO)) {
            Necromante necromante = new Necromante(1);
            Prova prova = inCombattimentoCon(partita, necromante, new Scheletro(1), new Scheletro(1), new Goblin(1));

            necromante.muore("prova");
            prova.impostaAzioni(partita.gruppo(), GruppoAvversario.getIstanza(), null);

            assertTrue(partita.testi().stream().anyMatch(t -> t.contains("gli scheletri che teneva in piedi crollano")), String.valueOf(partita.testi()));
        }
    }

    @Test
    void ilNecromanteNonCadutoSenzaScheletriNonFaCrollareNiente() {
        try (PartitaDiTest partita = partita(83, Comando.GUERRIERO)) {
            Necromante necromante = new Necromante(1);
            Prova prova = inCombattimentoCon(partita, necromante, new Goblin(1));

            necromante.muore("prova");
            prova.impostaAzioni(partita.gruppo(), GruppoAvversario.getIstanza(), null);

            assertTrue(partita.testi().stream().noneMatch(t -> t.contains("scheletri che teneva in piedi")));
        }
    }

    @Test
    void ilNecromanteEvocaUnNumeroLimitatoDiScheletriESenzaDiLuiNiente() throws Exception {
        try (PartitaDiTest partita = partita(84, Comando.GUERRIERO)) {
            Necromante necromante = new Necromante(1);
            Scheletro primo = new Scheletro(1);
            Prova prova = inCombattimentoCon(partita, necromante, primo);
            GruppoAvversario avversari = GruppoAvversario.getIstanza();
            int evocati = 0;

            // Ogni due scheletri caduti ne evoca un altro, ma non più di quattro volte
            for (int giro = 0; giro < CostantiMotore.NECROMANTE_EVOCAZIONI_MASSIME + 2; giro++) {
                int prima = avversari.getNumeroPersonaggi();
                for (Personaggio p : avversari.getPersonaggiVivi()) {
                    if (p.getClasse() == TipoPersonaggio.SCHELETRO) {
                        p.muore("prova");
                    }
                }
                avversari.aggiungiPersonaggio(new Scheletro(1));
                avversari.getPersonaggi().get(avversari.getNumeroPersonaggi() - 1).muore("prova");
                prova.impostaAzioni(partita.gruppo(), avversari, null);
                if (avversari.getNumeroPersonaggi() > prima + 1) {
                    evocati++;
                }
            }
            assertTrue(evocati <= CostantiMotore.NECROMANTE_EVOCAZIONI_MASSIME);

            // Una evocazione che aspettava, ma il necromante non c'è più: niente scheletri nuovi
            necromante.muore("prova");
            int scheletri = avversari.getNumeroPersonaggi();
            privato(prova, "evocazioniDaFare", 1);
            prova.impostaAzioni(partita.gruppo(), avversari, null);
            assertEquals(scheletri, avversari.getNumeroPersonaggi());
        }
    }
}
