package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.comandigiocatore.ComandoCommutazioneElenco;
import com.threeamigos.foresta.gruppi.GruppoAvversario;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.interfacce.GestoreSalvataggi;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.missioni.Missione;
import com.threeamigos.foresta.missioni.RegistroMissioni;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.oggetti.Moneta;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.personaggi.Maga;
import com.threeamigos.foresta.personaggi.Mago;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.statistiche.LineaTemporale;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.CostantiPersonaggi;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.tipi.TipoIncantesimo;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.threeamigos.foresta.motore.AutomaCasiRimastiTest.automaDi;
import static com.threeamigos.foresta.motore.AutomaCasiRimastiTest.camminaUnPasso;
import static com.threeamigos.foresta.motore.AutomaCasiRimastiTest.impostaCampo;
import static com.threeamigos.foresta.motore.AutomaCasiRimastiTest.partitaFuoriDallaCitta;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Gli stati rari dell'automa: il tempo che scade, i testi e i caricamenti fuori posto, la resurrezione fuori dalle locazioni, le scelte di chi
 * beve o riceve un oggetto, la descrizione delle missioni e il capo che muore nel sonno. Dove nessuna partita ci arriva da sola si porta
 * l'automa nello stato con la reflection sui suoi campi privati.
 */
class AutomaStatiRariTest {

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

    private static PartitaDiTest partitaInCitta(long seme) {
        PartitaDiTest partita = PartitaDiTest.nuova(seme);
        partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
        return partita;
    }

    @SuppressWarnings("unchecked")
    private static Map<Stato, Supplier<Object>> gestoriIngresso(Automa automa) throws Exception {
        Field campo = Automa.class.getDeclaredField("gestoriIngresso");
        campo.setAccessible(true);
        return (Map<Stato, Supplier<Object>>) campo.get(automa);
    }

    @SuppressWarnings("unchecked")
    private static Map<Stato, Function<Comando, Object>> gestoriComando(Automa automa) throws Exception {
        Field campo = Automa.class.getDeclaredField("gestoriComando");
        campo.setAccessible(true);
        return (Map<Stato, Function<Comando, Object>>) campo.get(automa);
    }

    private static Object esito(String nome) throws Exception {
        Field campo = Class.forName("com.threeamigos.foresta.motore.Automa$Esito").getDeclaredField(nome);
        campo.setAccessible(true);
        return campo.get(null);
    }

    /**
     * Porta l'automa in quello stato come se ci fosse arrivato da un comando, e ne esegue l'ingresso: CONFERMA_USCITA fa da trampolino.
     */
    private static void entraIn(Automa automa, Stato stato) throws Exception {
        Object continua = esito("CONTINUA_CON_INGRESSO");
        gestoriComando(automa).put(Stato.CONFERMA_USCITA, comando -> {
            try {
                impostaCampo(automa, "stato", stato);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            return continua;
        });
        impostaCampo(automa, "stato", Stato.CONFERMA_USCITA);
        automa.processaComando(Comando.SI);
    }

    @Test
    void unaTransizioneAdUnoStatoSenzaIngressoSiSegnala() throws Exception {
        try (PartitaDiTest partita = PartitaDiTest.nuova(111)) {
            Automa automa = automaDi(partita);
            Object continua = esito("CONTINUA_CON_INGRESSO");
            gestoriIngresso(automa).remove(Stato.CONFERMA_USCITA);
            gestoriComando(automa).put(Stato.CONFERMA_USCITA, comando -> continua);
            impostaCampo(automa, "stato", Stato.CONFERMA_USCITA);

            assertThrows(IllegalStateException.class, () -> automa.processaComando(Comando.SI));
        }
    }

    @Test
    void unTestoInCittaArrivaAllaLocazioneCheLoIgnora() {
        try (PartitaDiTest partita = partitaInCitta(112)) {
            partita.testo("un nome che nessuno ha chiesto");

            partita.assertStato(Stato.IN_LOCAZIONE);
        }
    }

    @Test
    void caricareDallaMemoriaSenzaUnSalvataggioRiportaAllIntro() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(113)) {
            partita.comando(Comando.FLOPPY_CARICA);

            partita.comando(GestoreSalvataggi.SLOT_IN_MEMORIA);

            partita.assertStato(Stato.INTRO);
        }
    }

    @Test
    void scadutoIlTempoCamminandoLaPartitaEPersaAllIngressoDellaLocazione() {
        try (PartitaDiTest partita = partitaFuoriDallaCitta(114)) {
            LineaTemporale.aggiungiOre(24 * 41);

            camminaUnPasso(partita);

            partita.assertStato(Stato.GIOCO_PERSO);
        }
    }

    @Test
    void scadutoIlTempoUscendoDallaCittaLaSconfittaSiRaccontaAFineLocazione() {
        try (PartitaDiTest partita = partitaInCitta(115)) {
            LineaTemporale.aggiungiOre(24 * 41);
            LineaTemporale.eventi(partita.gruppo());

            partita.comando(Comando.ESCI_DA_CITTA);

            partita.assertStato(Stato.GIOCO_PERSO);
            assertTrue(partita.testi().stream().anyMatch(t -> t.contains("tutto e' perduto")), String.valueOf(partita.testi()));
        }
    }

    @Test
    void unaMissioneCheSopprimeIlContenutoSvuotaLaLocazioneDoveSiArriva() {
        try (PartitaDiTest partita = partitaFuoriDallaCitta(116)) {
            CoordinateMD partenza = Foresta.getPosizioneDelGruppo();
            Comando direzione = null;
            for (Comando candidata : new Comando[]{Comando.NORD, Comando.SUD, Comando.EST, Comando.OVEST}) {
                if (partita.comandiDisponibili().contains(candidata)) {
                    direzione = candidata;
                    break;
                }
            }
            assertNotNull(direzione);
            // Dove si arriva con un passo in quella direzione
            switch (direzione) {
                case NORD:
                    Foresta.muoveNord(1);
                    break;
                case SUD:
                    Foresta.muoveSud(1);
                    break;
                case EST:
                    Foresta.muoveEst(1);
                    break;
                default:
                    Foresta.muoveOvest(1);
                    break;
            }
            CoordinateMD arrivo = Foresta.getPosizioneDelGruppo();
            Foresta.setPosizioneDelGruppo(partenza);
            RegistroMissioni.sopprimiContenutoLocazione(arrivo);

            partita.comando(direzione);
            partita.comando(Comando.NUMERO_1);

            assertEquals(arrivo, Foresta.getPosizioneDelGruppo());
            assertEquals(0, GruppoAvversario.getIstanza().getNumeroPersonaggi());
        }
    }

    @Test
    void seIlCapoMuoreNelSonnoDelCampoLaPartitaEPersa() {
        try (PartitaDiTest partita = partitaFuoriDallaCitta(117)) {
            GruppoGiocatore gruppo = partita.gruppo();
            gruppo.aggiungiPersonaggio(new Guerriero("Secondo", 1));
            Foresta.setPosizioneDelGruppo(primaCasellaDi(TipoLocazione.BOSCO));
            LineaTemporale.aggiungiOre((22 - LineaTemporale.getOra() + 24) % 24);
            gruppo.getCapo().addEffettoDiStato(TipoEffettoDiStato.AVVELENATO, 100, 100000);

            partita.comando(Comando.ACCAMPAMENTO);

            partita.assertStato(Stato.GIOCO_PERSO);
        }
    }

    @Test
    void unOspiteVulnerabileVedeScadereIModificatoriAFineLocazione() {
        try (PartitaDiTest partita = partitaInCitta(118)) {
            partita.gruppo().aggiungiOspite(new Guerriero("Ospite", 1), true);

            partita.comando(Comando.ESCI_DA_CITTA);

            partita.assertStato(Stato.SCELTA_DIREZIONE);
        }
    }

    @Test
    void conPiuFormulantiLaResurrezioneFuoriDallaLocazioneChiedeChiLaFormulaEChiRisuscitare() {
        try (PartitaDiTest partita = partitaFuoriDallaCitta(119)) {
            GruppoGiocatore gruppo = partita.gruppo();
            Personaggio mago = new Mago("Merlino", 5);
            Personaggio maga = new Maga("Merlina", 5);
            Personaggio primoCaduto = new Guerriero("Caduto", 1);
            Personaggio secondoCaduto = new Guerriero("Caduta", 1);
            gruppo.aggiungiPersonaggio(mago);
            gruppo.aggiungiPersonaggio(maga);
            gruppo.aggiungiPersonaggio(primoCaduto);
            gruppo.aggiungiPersonaggio(secondoCaduto);
            primoCaduto.muore("prova");
            secondoCaduto.muore("prova");
            gruppo.addIncantesimi(TipoIncantesimo.RESURREZIONE, 3);
            mago.getModelloDati().set(TipoAttributo.MAGIA, mago.getMagiaMassima());
            maga.getModelloDati().set(TipoAttributo.MAGIA, maga.getMagiaMassima());

            // Due possono formulare: si chiede chi, e si può rinunciare
            partita.comando(Comando.RESURREZIONE);
            partita.assertStato(Stato.SCELTA_FORMULANTE_RESURREZIONE);
            partita.comando(Comando.ANNULLA);
            partita.assertStato(Stato.SCELTA_DIREZIONE);

            // Poi si sceglie chi formula e, fra due caduti, chi risuscitare
            partita.comando(Comando.RESURREZIONE);
            partita.comando(Comando.ofPersonaggio(gruppo.getPersonaggi().indexOf(mago)));
            partita.assertStato(Stato.SCELTA_BERSAGLIO_RESURREZIONE);
            partita.comando(Comando.ofPersonaggio(gruppo.getPersonaggi().indexOf(primoCaduto)));

            partita.assertStato(Stato.SCELTA_DIREZIONE);
            assertTrue(primoCaduto.isVivo());
            assertFalse(secondoCaduto.isVivo());
        }
    }

    @Test
    void chiEGiaAlLivelloMassimoOMortoNonSiProponePerLaPozioneDiLivello() {
        try (PartitaDiTest partita = partitaFuoriDallaCitta(120)) {
            GruppoGiocatore gruppo = partita.gruppo();
            Personaggio alMassimo = new Guerriero("Massimo", 1);
            Personaggio caduto = new Guerriero("Caduto", 1);
            gruppo.aggiungiPersonaggio(alMassimo);
            gruppo.aggiungiPersonaggio(caduto);
            alMassimo.getModelloDati().setLivello(CostantiPersonaggi.MASSIMO_LIVELLO);
            caduto.muore("prova");
            gruppo.addPozioniAumentoLivello(1);
            int livelloDelCapo = gruppo.getCapo().getLivello();

            partita.comando(Comando.POZIONE_AUMENTO_LIVELLO);

            // Resta solo il capo: nessuna scelta da fare, e la pozione la beve lui
            partita.assertStato(Stato.SCELTA_DIREZIONE);
            assertEquals(livelloDelCapo + 1, gruppo.getCapo().getLivello());
        }
    }

    @Test
    void ilCapoSenzaNomeProprioDaIlNomeAllaClassifica() throws Exception {
        try (PartitaDiTest partita = partitaInCitta(122)) {
            GruppoGiocatore gruppo = partita.gruppo();
            Personaggio senzaNome = FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 1);
            gruppo.aggiungiPersonaggio(senzaNome);
            gruppo.rimuoviPersonaggio(gruppo.getPersonaggi().get(0));
            impostaCampo(automaDi(partita), "stato", Stato.ATTESA_NOME_PUNTEGGI);

            partita.testo("   ");

            partita.assertStato(Stato.INTRO);
        }
    }

    @Test
    void laDescrizioneDiUnaMissioneSiApreESiChiude() {
        try (PartitaDiTest partita = partitaInCitta(123)) {
            Missione missione = RegistroMissioni.getMissionePrincipale();

            boolean prima = missione.isDescrizioneVisibile();
            partita.pubblica(ComandoCommutazioneElenco.missione(missione.getId()));
            assertEquals(!prima, missione.isDescrizioneVisibile());
            partita.pubblica(ComandoCommutazioneElenco.missione(missione.getId()));
            assertEquals(prima, missione.isDescrizioneVisibile());
        }
    }

    @Test
    void l_attesaDellaRispostaIgnoraIlBattitoESegnalaIComandiSbagliati() throws Exception {
        try (PartitaDiTest partita = PartitaDiTest.nuova(124)) {
            Automa automa = automaDi(partita);
            impostaCampo(automa, "stato", Stato.ATTESA_RISPOSTA_MISSIONE);
            impostaCampo(automa, "comandiRispostaMissione", Arrays.asList(Comando.SI, Comando.NO));

            automa.processaComando(Comando.TIMER);
            assertEquals(Stato.ATTESA_RISPOSTA_MISSIONE, automa.getStato());
            automa.processaComando(Comando.NORD);
            assertEquals(Stato.ATTESA_RISPOSTA_MISSIONE, automa.getStato());
        }
    }

    @Test
    void conUnSoloDestinatarioPossibileLOggettoVaDirettamenteALui() throws Exception {
        try (PartitaDiTest partita = partitaInCitta(125)) {
            Automa automa = automaDi(partita);
            LocazioneCorrente.get().collocaOggettoMissione(new Moneta());
            impostaCampo(automa, "statoPrecedente", Stato.SCELTA_DIREZIONE);

            entraIn(automa, Stato.SCELTA_DESTINATARIO_OGGETTO);

            assertEquals(Stato.SCELTA_DIREZIONE, automa.getStato());
        }
    }

    @Test
    void conPiuDestinatariSiChiedeEUnComandoSbagliatoSiRifiuta() throws Exception {
        try (PartitaDiTest partita = partitaInCitta(126)) {
            partita.gruppo().aggiungiPersonaggio(new Guerriero("Secondo", 1));
            Automa automa = automaDi(partita);
            LocazioneCorrente.get().collocaOggettoMissione(new Moneta());

            entraIn(automa, Stato.SCELTA_DESTINATARIO_OGGETTO);
            assertEquals(Stato.SCELTA_DESTINATARIO_OGGETTO, automa.getStato());
            automa.processaComando(Comando.NORD);

            assertEquals(Stato.SCELTA_DESTINATARIO_OGGETTO, automa.getStato());
        }
    }

    @Test
    void lAttesaDiUnIncantesimoQualsiasiOffreTuttiGliIncantesimi() throws Exception {
        try (PartitaDiTest partita = partitaInCitta(127)) {
            Automa automa = automaDi(partita);

            entraIn(automa, Stato.ATTESA_INCANTESIMO_QUALSIASI);

            assertEquals(Stato.INCANTESIMO_SCELTO, automa.getStato());
        }
    }

    @Test
    void conUnaDirezioneImpossibileIPassiSiRifiutano() throws Exception {
        try (PartitaDiTest partita = partitaFuoriDallaCitta(128)) {
            Automa automa = automaDi(partita);
            impostaCampo(automa, "stato", Stato.SCELTA_PASSI);
            impostaCampo(automa, "direzione", Comando.PERGAMENA);
            impostaCampo(automa, "comandiPossibiliPerNumeroPassi", Collections.singletonList(Comando.NUMERO_1));

            assertThrows(IllegalArgumentException.class, () -> automa.processaComando(Comando.NUMERO_1));
        }
    }

    @Test
    void conUnSoloPersonaggioDaCurareLaSceltaQualsiasiSiRisolveDaSola() throws Exception {
        try (PartitaDiTest partita = partitaInCitta(132)) {
            Automa automa = automaDi(partita);
            impostaCampo(automa, "statoPrecedente", Stato.ATTESA_POZIONE_SALUTE);

            entraIn(automa, Stato.SCELTA_PERSONAGGIO_QUALSIASI);

            // Beve la pozione l'unico personaggio e si torna a scegliere la direzione
            assertEquals(Stato.SCELTA_DIREZIONE, automa.getStato());
        }
    }

    @Test
    void senzaNessunoInCampoOSenzaOggettoIlDestinatarioDiRipiegoEIlGruppo() throws Exception {
        try (PartitaDiTest partita = partitaInCitta(133)) {
            Automa automa = automaDi(partita);
            LocazioneCorrente.get().rimuoviOggetto();
            partita.gruppo().getCapo().muore("prova");
            impostaCampo(automa, "statoPrecedente", Stato.SCELTA_DIREZIONE);

            entraIn(automa, Stato.SCELTA_DESTINATARIO_OGGETTO);

            assertEquals(Stato.SCELTA_DIREZIONE, automa.getStato());
        }
    }

    @Test
    void chiELaPersonaggioATempoNonHaUnInventarioNeUnaTattica() throws Exception {
        try (PartitaDiTest partita = partitaFuoriDallaCitta(134)) {
            Personaggio gigante = FabbricaPersonaggi.crea(TipoPersonaggio.GIGANTE, 1);
            gigante.setTempo(3);
            partita.gruppo().aggiungiPersonaggio(gigante);
            Automa automa = automaDi(partita);
            // Prima un indice negativo, poi quello del personaggio a tempo: l'inventario e le tattiche ripartono dal primo
            impostaCampo(automa, "indicePersonaggioInventario", -1);
            impostaCampo(automa, "indicePersonaggioTattiche", -1);
            partita.comando(Comando.INVENTARIO);
            partita.comando(Comando.MOSTRA_TATTICHE);
            partita.comando(Comando.ANNULLA);
            partita.comando(Comando.ANNULLA);
            int indiceDelGigante = partita.gruppo().getPersonaggi().indexOf(gigante);
            impostaCampo(automa, "indicePersonaggioInventario", indiceDelGigante);
            impostaCampo(automa, "indicePersonaggioTattiche", indiceDelGigante);

            partita.comando(Comando.INVENTARIO);
            partita.comando(Comando.MOSTRA_TATTICHE);
            partita.comando(Comando.ANNULLA);
            partita.comando(Comando.ANNULLA);

            partita.assertStato(Stato.SCELTA_DIREZIONE);
        }
    }

    @Test
    void laMappaSiApreESiChiude() {
        try (PartitaDiTest partita = partitaFuoriDallaCitta(129)) {
            partita.comando(Comando.MAPPA);
            partita.assertStato(Stato.MAPPA);
            partita.comando(Comando.SI);
            partita.assertStato(Stato.SCELTA_DIREZIONE);
        }
    }

    @Test
    void ilSalvataggioSiPuoAnnullareELaPartitaSiPuoLasciare() {
        try (PartitaDiTest partita = partitaFuoriDallaCitta(130)) {
            partita.comando(Comando.FLOPPY_SALVA);

            partita.comando(Comando.ANNULLA);

            partita.assertStato(Stato.CONFERMA_USCITA);
        }
    }

    @Test
    void conIndiciDiPersonaggioFuoriPostoLInventarioELeTattichePartonoDalPrimo() throws Exception {
        try (PartitaDiTest partita = partitaFuoriDallaCitta(131)) {
            Automa automa = automaDi(partita);
            impostaCampo(automa, "indicePersonaggioInventario", 17);
            impostaCampo(automa, "indicePersonaggioTattiche", 17);

            partita.comando(Comando.INVENTARIO);
            partita.assertStato(Stato.INVENTARIO);
            partita.comando(Comando.MOSTRA_TATTICHE);
            partita.assertStato(Stato.TATTICHE);
            partita.comando(Comando.ANNULLA);
            partita.comando(Comando.ANNULLA);
            partita.assertStato(Stato.SCELTA_DIREZIONE);
        }
    }
}
