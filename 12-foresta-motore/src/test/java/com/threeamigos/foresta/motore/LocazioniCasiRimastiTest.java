package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.artefatti.GeneratoreArtefatti;
import com.threeamigos.foresta.economia.Cassa;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.eventi.richieste.RichiestaAperturaIncantatore;
import com.threeamigos.foresta.eventi.richieste.RichiestaTesto;
import com.threeamigos.foresta.gruppi.GruppoAvversario;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.locazioni.Bottega;
import com.threeamigos.foresta.locazioni.Citta;
import com.threeamigos.foresta.locazioni.FabbricaLocazioni;
import com.threeamigos.foresta.locazioni.Locazione;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.missioni.principali.IlGiroDelleSetteChiese;
import com.threeamigos.foresta.modellodati.ArtefattoMD;
import com.threeamigos.foresta.mondo.RegistroArtefatti;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.modellodati.LocazioneMD;
import com.threeamigos.foresta.oggetti.Moneta;
import com.threeamigos.foresta.statistiche.LineaTemporale;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoModificatore;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I casi delle locazioni che il gioco vero non raggiunge o raggiunge di rado: le implementazioni di base dell'interfaccia, i castelli in cui
 * non si può riposare, la bottega dell'incantatore con la fusione e il nome dell'artefatto, la città a cui si cambia il modello dati dopo
 * la creazione, il tempio ripulito e la palude con un oggetto.
 */
class LocazioniCasiRimastiTest {

    private static PartitaDiTest partitaInCitta(long seme) {
        PartitaDiTest partita = PartitaDiTest.nuova(seme);
        partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
        return partita;
    }

    private static CoordinateMD primaCasellaDi(TipoLocazione tipo) {
        for (int x = 0; x < Foresta.getDimensioneX(); x++) {
            for (int y = 0; y < Foresta.getDimensioneY(); y++) {
                if (Foresta.getLocazione(x, y) == tipo) {
                    return new CoordinateMD(x, y);
                }
            }
        }
        throw new IllegalStateException("nessuna casella " + tipo);
    }

    // Una casella accanto alla città, trasformata in una bottega di quel tipo
    private static CoordinateMD costruisciBottega(TipoLocazione tipo, String negoziante) {
        CoordinateMD citta = Foresta.getPosizioneDelGruppo();
        int lato = citta.getY() + 1 < Foresta.getDimensioneY() ? 1 : -1;
        CoordinateMD posto = new CoordinateMD(citta.getX(), citta.getY() + lato);
        Foresta.costruisciLocazione(posto, tipo);
        Foresta.getLocazioneMD(posto).setNome("la Bottega di prova");
        if (negoziante != null) {
            Foresta.getLocazioneMD(posto).aggiungiProprieta(com.threeamigos.foresta.mondo.RegistroPersonaggi.NEGOZIANTE, negoziante);
        }
        Foresta.setPosizioneDelGruppo(posto);
        return posto;
    }

    @Test
    void leImplementazioniDiBaseDellInterfacciaLocazione() {
        Locazione base = new Locazione() {
            @Override
            public TipoLocazione getTipoLocazione() {
                return TipoLocazione.BOSCO;
            }

            @Override
            public LocazioneMD getModelloDati() {
                return null;
            }

            @Override
            public void setModelloDati(LocazioneMD modelloDati) {
            }

            @Override
            public String getNome() {
                return "";
            }

            @Override
            public void crea(GruppoGiocatore g, GruppoAvversario gng) {
            }

            @Override
            public void descrivi(GruppoGiocatore g, GruppoAvversario gng) {
            }

            @Override
            public com.threeamigos.foresta.motore.Stato impostaAzioni(GruppoGiocatore g, GruppoAvversario gng, Comando azione) {
                return null;
            }

            @Override
            public void ripresentaComandi() {
            }

            @Override
            public boolean isCompleta() {
                return false;
            }

            @Override
            public boolean isHaStrettoAmicizia() {
                return false;
            }

            @Override
            public boolean isCustodita() {
                return false;
            }

            @Override
            public void azzeraLocazione(GruppoGiocatore g) {
            }

            @Override
            public com.threeamigos.foresta.oggetti.Bottino getOggetto() {
                return null;
            }

            @Override
            public void rimuoviOggetto() {
            }

            @Override
            public void collocaOggettoMissione(com.threeamigos.foresta.oggetti.Bottino oggetto) {
            }

            @Override
            public com.threeamigos.foresta.tipi.TipoRiposo getTipoRiposo() {
                return null;
            }
        };

        base.setOndateSuccessive(new ArrayList<>());
        assertFalse(base.hasOndateSuccessive());
        assertFalse(base.isSceltaDiUnaCura());
        assertFalse(base.isSceltaDiChiAgisce());
        assertFalse(base.isSceltaDiUnAumentoDiLivello());
        assertEquals(Stato.IN_LOCAZIONE, base.riceviTesto(null, "niente"));
    }

    @Test
    void neiCastelliENelleBottegheENellaCittaNonSiPuoRiposare() {
        for (TipoLocazione castello : new TipoLocazione[]{TipoLocazione.CASTELLO_MINOTAURO, TipoLocazione.CASTELLO_STREGA,
                TipoLocazione.CASTELLO_LICH, TipoLocazione.CASTELLO_IDRA, TipoLocazione.CASTELLO_DRAGO, TipoLocazione.ALCHIMISTA,
                TipoLocazione.CITTA_NYENA}) {
            Locazione locazione = FabbricaLocazioni.crea(castello);
            assertThrows(IllegalArgumentException.class, locazione::getTipoRiposo, castello.name());
        }
    }

    @Test
    void ognunDeiCastelliSiAzzeraELasciaRovineSoloSeCompleto() {
        TipoLocazione[] castelli = {TipoLocazione.CASTELLO_MINOTAURO, TipoLocazione.CASTELLO_STREGA, TipoLocazione.CASTELLO_LICH,
                TipoLocazione.CASTELLO_IDRA, TipoLocazione.CASTELLO_DRAGO};
        for (int i = 0; i < castelli.length; i++) {
            TipoLocazione castello = castelli[i];
            try (PartitaDiTest partita = PartitaDiTest.nuova(60 + i)) {
                partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> {
                    if (castello == TipoLocazione.CASTELLO_DRAGO) {
                        // Il castello del Drago nasce solo quando gli altri sono caduti: qui lo si costruisce
                        Foresta.setPosizioneDelGruppo(Foresta.costruisciLocazioneUnica(castello, true));
                    } else {
                        partita.spostaGruppoIn(castello);
                    }
                });
                CoordinateMD posto = Foresta.getPosizioneDelGruppo();
                Locazione locazione = CostruttoreDellaForesta.costruisciIstanza(posto);
                GruppoAvversario avversari = GruppoAvversario.getIstanza();
                avversari.reimposta();
                locazione.crea(partita.gruppo(), avversari);
                if (castello == TipoLocazione.CASTELLO_DRAGO) {
                    assertEquals(1, avversari.getNumeroPersonaggi());
                    locazione.descrivi(partita.gruppo(), avversari);
                    assertTrue(partita.ultimoTesto().contains("Castello della Morte Alata"), partita.ultimoTesto());
                }
                avversari.rimuoviPersonaggi();

                // Incompleto, il castello resta com'e'; completo, diventa rovine
                locazione.azzeraLocazione(partita.gruppo());
                assertEquals(castello, Foresta.getLocazione(posto));
                Foresta.getLocazioneMD(posto).aggiungiProprieta(LocazioneMD.COMPLETA, LocazioneMD.AFFERMATIVO);
                assertTrue(locazione.isCompleta());
                locazione.azzeraLocazione(partita.gruppo());
                assertEquals(TipoLocazione.ROVINE, Foresta.getLocazione(posto));
            }
        }
    }

    @Test
    void laPaludeConUnOggettoDiMissioneNonEVuota() {
        try (PartitaDiTest partita = partitaInCitta(52)) {
            CoordinateMD posto = Foresta.getPosizioneDelGruppo();
            Foresta.costruisciLocazione(posto, TipoLocazione.PALUDE);
            Locazione palude = CostruttoreDellaForesta.costruisciIstanza(posto);
            GruppoAvversario avversari = GruppoAvversario.getIstanza();
            avversari.reimposta();
            palude.crea(partita.gruppo(), avversari);

            palude.descrivi(partita.gruppo(), avversari);
            assertTrue(partita.testi().get(partita.testi().size() - 1).contains("non trova nulla"));

            palude.collocaOggettoMissione(new Moneta());
            palude.descrivi(partita.gruppo(), avversari);
            assertFalse(partita.ultimoTesto().contains("non trova nulla"), partita.ultimoTesto());
        }
    }

    @Test
    void unTempioRipulitoSenzaOggettoDiceCheRegnaLaPace() {
        try (PartitaDiTest partita = partitaInCitta(53)) {
            CoordinateMD posto = primaCasellaDi(TipoLocazione.TEMPIO);
            Foresta.setPosizioneDelGruppo(posto);
            Locazione tempio = CostruttoreDellaForesta.costruisciIstanza(posto);
            GruppoAvversario avversari = GruppoAvversario.getIstanza();
            avversari.reimposta();
            IlGiroDelleSetteChiese.ripulisci(Foresta.getLocazioneMD(posto));

            tempio.crea(partita.gruppo(), avversari);
            tempio.rimuoviOggetto();
            tempio.descrivi(partita.gruppo(), avversari);

            assertTrue(partita.ultimoTesto().contains("regna la pace"), partita.ultimoTesto());
            assertEquals(com.threeamigos.foresta.tipi.TipoRiposo.ALL_APERTO_CON_FUOCO, tempio.getTipoRiposo());
        }
    }

    @Test
    void unTempioGiaVisitatoNonRimettePiuIlCofano() {
        try (PartitaDiTest partita = partitaInCitta(54)) {
            CoordinateMD posto = primaCasellaDi(TipoLocazione.TEMPIO);
            Foresta.setPosizioneDelGruppo(posto);
            Foresta.setLocazioneVisitata(posto);
            // Senza l'artefatto che custodiva, e già visitato, il tempio non ha più il cofano
            RegistroArtefatti.rimuoviArtefattoInLocazione(posto);
            Locazione tempio = CostruttoreDellaForesta.costruisciIstanza(posto);
            GruppoAvversario avversari = GruppoAvversario.getIstanza();
            avversari.reimposta();

            tempio.crea(partita.gruppo(), avversari);

            assertTrue(avversari.getNumeroPersonaggi() > 0);
            assertNull(tempio.getOggetto());
        }
    }

    @Test
    void unTempioRipulitoCheCustodisceUnArtefattoNonDiceCheRegnaLaPace() {
        try (PartitaDiTest partita = partitaInCitta(59)) {
            CoordinateMD posto = primaCasellaDi(TipoLocazione.TEMPIO);
            Foresta.setPosizioneDelGruppo(posto);
            RegistroArtefatti.addArtefattoInLocazione(GeneratoreArtefatti.istanza().generaArtefatto(TipoArtefatto.SPADA, 1), posto);
            IlGiroDelleSetteChiese.ripulisci(Foresta.getLocazioneMD(posto));
            Locazione tempio = CostruttoreDellaForesta.costruisciIstanza(posto);
            GruppoAvversario avversari = GruppoAvversario.getIstanza();
            avversari.reimposta();

            tempio.crea(partita.gruppo(), avversari);
            tempio.descrivi(partita.gruppo(), avversari);

            assertNotNull(tempio.getOggetto());
            assertFalse(partita.ultimoTesto().contains("regna la pace"), partita.ultimoTesto());
        }
    }

    @Test
    void laCittaCheCambiaModelloDatiDopoLaCreazioneLoPassaAncheAllaLocanda() {
        try (PartitaDiTest partita = partitaInCitta(55)) {
            Citta citta = (Citta) CostruttoreDellaForesta.costruisciIstanza(Foresta.getPosizioneDelGruppo());
            citta.crea(partita.gruppo(), GruppoAvversario.getIstanza());

            citta.setModelloDati(Foresta.getLocazioneMD(Foresta.getPosizioneDelGruppo()));

            // In piazza il testo scritto non serve a nessuna bottega, e non si può tornare da dove non si è
            assertEquals(Stato.IN_LOCAZIONE, citta.riceviTesto(partita.gruppo(), "niente"));
            citta.ripresentaComandi();
            citta.impostaAzioni(partita.gruppo(), GruppoAvversario.getIstanza(), Comando.NEGOZIO_ALCHIMISTA);
            assertThrows(IllegalStateException.class, citta::ripresentaComandi);
        }
    }

    @Test
    void laBottegaDellIncantatoreConLaFusioneEIlNomeDellArtefatto() {
        try (PartitaDiTest partita = partitaInCitta(56)) {
            costruisciBottega(TipoLocazione.INCANTATORE, null);
            Bottega bottega = (Bottega) CostruttoreDellaForesta.costruisciIstanza(Foresta.getPosizioneDelGruppo());
            GruppoGiocatore gruppo = partita.gruppo();
            GruppoAvversario avversari = GruppoAvversario.getIstanza();
            bottega.crea(gruppo, avversari);
            List<RichiestaAperturaIncantatore> aperture = new ArrayList<>();
            List<RichiestaTesto> richieste = new ArrayList<>();
            BusEventi.iscriviti(RichiestaAperturaIncantatore.class, aperture::add);
            BusEventi.iscriviti(RichiestaTesto.class, richieste::add);

            // Senza il negoziante la descrizione è quella breve; un comando che non è l'ingresso non apre niente
            bottega.descrivi(gruppo, avversari);
            assertEquals(Stato.IN_LOCAZIONE, bottega.impostaAzioni(gruppo, avversari, Comando.NORD));
            assertTrue(aperture.isEmpty());
            assertEquals(Stato.IN_LOCAZIONE, bottega.impostaAzioni(gruppo, avversari, Comando.NEGOZIO_INCANTATORE));
            assertEquals(1, aperture.size());
            BancoIncantatore banco = new BancoIncantatore((AutomaIncantatore) aperture.get(0).getBanco());

            // Il banco è vuoto: la fusione è rifiutata e si resta in bottega, e un altro comando non fa niente
            assertEquals(Stato.IN_LOCAZIONE, bottega.impostaAzioni(gruppo, avversari, Comando.FUSIONE));
            assertEquals(Stato.IN_LOCAZIONE, bottega.impostaAzioni(gruppo, avversari, Comando.NORD));
            assertTrue(richieste.isEmpty());

            // Con una spada e una pergamena la fusione chiede il nome dell'artefatto, e il testo arrivato la fa
            Cassa.addMonete(1000);
            banco.metti(artefatto(TipoArtefatto.SPADA, 3, 5, 5));
            banco.metti(artefatto(TipoArtefatto.PERGAMENA, 1, 10));
            assertEquals(Stato.IN_LOCAZIONE, bottega.impostaAzioni(gruppo, avversari, Comando.FUSIONE));
            assertEquals(1, richieste.size());
            // Mentre si aspetta il nome, ANNULLA non chiude la bottega
            assertEquals(Stato.IN_LOCAZIONE, bottega.impostaAzioni(gruppo, avversari, Comando.ANNULLA));
            assertEquals(Stato.IN_LOCAZIONE, bottega.riceviTesto(gruppo, "Excalibur"));

            // Il testo di chi non l'ha chiesto si ignora; la bottega si lascia con ANNULLA
            assertEquals(Stato.IN_LOCAZIONE, bottega.riceviTesto(gruppo, "ancora"));
            assertEquals(Stato.FINE_LOCAZIONE, bottega.impostaAzioni(gruppo, avversari, Comando.ANNULLA));
            assertThrows(IllegalStateException.class, bottega::ripresentaComandi);
            assertThrows(IllegalArgumentException.class, bottega::getTipoRiposo);
        }
    }

    @Test
    void laRiprogrammazioneSulBancoSiFaSubitoSenzaChiedereIlNome() {
        try (PartitaDiTest partita = partitaInCitta(57)) {
            costruisciBottega(TipoLocazione.INCANTATORE, "Mastro Prova da Nyena");
            Bottega bottega = (Bottega) CostruttoreDellaForesta.costruisciIstanza(Foresta.getPosizioneDelGruppo());
            GruppoGiocatore gruppo = partita.gruppo();
            GruppoAvversario avversari = GruppoAvversario.getIstanza();
            bottega.crea(gruppo, avversari);
            List<RichiestaAperturaIncantatore> aperture = new ArrayList<>();
            List<RichiestaTesto> richieste = new ArrayList<>();
            BusEventi.iscriviti(RichiestaAperturaIncantatore.class, aperture::add);
            BusEventi.iscriviti(RichiestaTesto.class, richieste::add);
            bottega.impostaAzioni(gruppo, avversari, Comando.NEGOZIO_INCANTATORE);
            BancoIncantatore banco = new BancoIncantatore((AutomaIncantatore) aperture.get(0).getBanco());
            Cassa.addMonete(1000);
            Artefatto spada = artefatto(TipoArtefatto.SPADA, 5, 5);
            spada.getModelloDati().addIncantamento("Fuoco minore", com.threeamigos.foresta.tipi.TipoDanno.FUOCO, 5, 0.05);
            banco.metti(spada);
            banco.metti(artefatto(TipoArtefatto.RIPROGRAMMAZIONE, 1));

            bottega.impostaAzioni(gruppo, avversari, Comando.FUSIONE);

            assertTrue(richieste.isEmpty(), "la riprogrammazione non chiede il nome");
        }
    }

    private static Artefatto artefatto(TipoArtefatto tipo, int livello, int... forza) {
        ArtefattoMD md = new ArtefattoMD();
        md.setTipo(tipo);
        md.setNome("l'oggetto di prova");
        md.setDescrizione("che serve ai test");
        md.setLivello(livello);
        md.setPeso(1);
        for (int f : forza) {
            md.addModificatore(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_FISSA, f, "");
        }
        return Artefatto.di(md);
    }

    // Il banco dell'incantatore, dove il test mette gli artefatti come farebbe la UI
    private static final class BancoIncantatore {
        private final AutomaIncantatore incantatore;

        BancoIncantatore(AutomaIncantatore incantatore) {
            this.incantatore = incantatore;
        }

        void metti(Artefatto artefatto) {
            incantatore.getBanco().addArtefatto(artefatto);
        }
    }

}
