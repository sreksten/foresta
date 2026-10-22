package com.threeamigos.foresta.tattiche;

import com.threeamigos.foresta.combattimento.StatoDelCombattimento;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.modellodati.tattiche.AzioneMD;
import com.threeamigos.foresta.modellodati.tattiche.TatticaMD;
import com.threeamigos.foresta.modellodati.tattiche.VoceMD;
import com.threeamigos.foresta.motore.Gruppo;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.strumenti.Dado;
import com.threeamigos.foresta.tattiche.valutazione.Astuzia;
import com.threeamigos.foresta.tipi.Costanti;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.tipi.TipoIncantesimo;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.tipi.tattiche.TipoAzione;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le tattiche base (TatticheBase): quelle dei mostri e quelle predefinite dei personaggi giocabili sono valide, si salvano e si rileggono, e
 * fanno quello che dicono: il bruto sceglie a caso, il mago nemico viene zittito dallo stratega, il quasi caduto riceve il colpo di grazia.
 */
class TatticheBaseTest {

    private Gruppo scorte;

    @BeforeEach
    void prepara() {
        ModelloDati.setIstanza(new ModelloDati());
        StatoDelCombattimento.getIstanza().reimposta();
        scorte = new Gruppo() {
            @Override
            public boolean isGruppoGiocatore() {
                return false;
            }
        };
    }

    @AfterEach
    void ripristina() {
        Dado.ripristina();
    }

    private static Personaggio crea(TipoPersonaggio classe, int livello) {
        Personaggio personaggio = FabbricaPersonaggi.crea(classe, livello);
        personaggio.addMagia(personaggio.getMagiaMassima());
        return personaggio;
    }

    private ContestoTattico perIMostri(Personaggio mostro, Personaggio... miei) {
        return new ContestoTattico(mostro, false, Collections.singletonList(mostro), Arrays.asList(miei), Collections.emptyList());
    }

    private ContestoTattico perIMiei(Personaggio attore, List<Personaggio> alleati, Personaggio... avversari) {
        return new ContestoTattico(attore, true, alleati, Arrays.asList(avversari), Collections.emptyList()).conGruppoDegliAlleati(scorte);
    }

    private static void feriscici(Personaggio p, double frazioneRimasta) {
        // Direttamente nel modello dati: non dipende da come altri test lasciano le modalità di prova
        p.getModelloDati().setSalute(p.getSaluteMassima() * frazioneRimasta);
    }

    private static TatticaMD rileggi(TatticaMD tattica) throws Exception {
        StringWriter scritto = new StringWriter();
        PrintWriter stream = new PrintWriter(scritto);
        tattica.salva(stream);
        stream.flush();
        return TatticaMD.leggi(new BufferedReader(new StringReader(scritto.toString())));
    }

    // ---- i dati ----

    @Test
    void ogniMostroHaUnaTatticaValidaCheSiSalvaEFinisceConUnAttacco() throws Exception {
        assertFalse(TatticheBase.classiDeiMostri().isEmpty());
        for (TipoPersonaggio classe : TatticheBase.classiDeiMostri()) {
            TatticaMD tattica = TatticheBase.deiMostri(classe);
            assertEquals(Collections.emptyList(), tattica.problemi(false), classe.name());
            assertEquals(tattica, rileggi(tattica), classe.name());
            VoceMD ultima = tattica.getVoci().get(tattica.getVoci().size() - 1);
            assertTrue(ultima.getCondizioni().isEmpty(), classe + ": l'ultima voce vale sempre");
            assertEquals(Collections.singletonList(AzioneMD.di(TipoAzione.ATTACCO_FISICO)), ultima.getAzioni(), classe.name());
            assertEquals(tattica, RegistroTattiche.perClasse(classe), classe.name());
        }
        assertNull(TatticheBase.deiMostri(TipoPersonaggio.UOMO));
    }

    @Test
    void ogniGiocabileHaUnaPredefinitaValidaEIlDueGeneriHannoLaStessa() throws Exception {
        for (TipoPersonaggio classe : Arrays.asList(TipoPersonaggio.GUERRIERO, TipoPersonaggio.LADRO, TipoPersonaggio.MAGO,
                TipoPersonaggio.ELFO, TipoPersonaggio.SACERDOTE, TipoPersonaggio.BARDO, TipoPersonaggio.CHIERICO, TipoPersonaggio.DRUIDO)) {
            TatticaMD tattica = TatticheBase.deiGiocabili(classe);
            assertNotNull(tattica, classe.name());
            assertFalse(tattica.getVoci().isEmpty());
            assertEquals(Collections.emptyList(), tattica.problemi(true), classe.name());
            assertEquals(tattica, rileggi(tattica), classe.name());
        }
        assertEquals(TatticheBase.deiGiocabili(TipoPersonaggio.MAGO), TatticheBase.deiGiocabili(TipoPersonaggio.MAGA));
        assertEquals(TatticheBase.deiGiocabili(TipoPersonaggio.BARDO), TatticheBase.deiGiocabili(TipoPersonaggio.CANTASTORIE));
        assertEquals(TatticheBase.deiGiocabili(TipoPersonaggio.SACERDOTE), RegistroTattiche.predefinitaDelGiocabile(TipoPersonaggio.SACERDOTESSA));
        assertEquals(TatticheBase.deiGiocabili(TipoPersonaggio.CHIERICO), RegistroTattiche.predefinitaDelGiocabile(TipoPersonaggio.CHIERICA));
        assertEquals(TatticheBase.deiGiocabili(TipoPersonaggio.DRUIDO), RegistroTattiche.predefinitaDelGiocabile(TipoPersonaggio.DRUIDA));
        assertEquals(TatticaMD.VUOTA, RegistroTattiche.predefinitaDelGiocabile(TipoPersonaggio.GOBLIN));
    }

    @Test
    void leFasceDiAstuziaSeguonoLeSoglieELeRitoccateIntelligenze() {
        assertTrue(Costanti.FOLLETTO_MAX_INTELLIGENZA > Costanti.GOBLIN_MAX_INTELLIGENZA, "il Folletto sta sopra il Goblin");
        assertTrue(Costanti.FOLLETTO_MAX_INTELLIGENZA < Costanti.HOBGOBLIN_MAX_INTELLIGENZA, "e sotto l'Hobgoblin");
        assertTrue(Costanti.OMBRANERA_MAX_INTELLIGENZA >= 31, "l'Ombra Nera entra nel quarto livello");
        // Un goblin non ha le fasce alte: due voci. Un drago le ha tutte.
        assertEquals(2, TatticheBase.deiMostri(TipoPersonaggio.GOBLIN).getVoci().size());
        assertTrue(TatticheBase.deiMostri(TipoPersonaggio.DRAGO).getVoci().size() > TatticheBase.deiMostri(TipoPersonaggio.HOBGOBLIN).getVoci().size());
    }

    // ---- i mostri ----

    @Test
    void ilGoblinAttaccaConLeArmiSenzaMagie() {
        Personaggio goblin = crea(TipoPersonaggio.GOBLIN, 1);
        Decisione decisione = Valutatore.decidi(TatticheBase.deiMostri(TipoPersonaggio.GOBLIN),
                perIMostri(goblin, crea(TipoPersonaggio.GUERRIERO, 1), crea(TipoPersonaggio.MAGO, 1)));
        assertEquals(TipoAzione.ATTACCO_FISICO, decisione.getTipoEseguito());
    }

    @Test
    void ilDragoStrategaZittisceIlMagoENonTiraIlDadoPrima() {
        Personaggio drago = crea(TipoPersonaggio.DRAGO, 1);
        Personaggio mago = crea(TipoPersonaggio.MAGO, 1);
        Personaggio guerriero = crea(TipoPersonaggio.GUERRIERO, 1);
        Decisione decisione = Valutatore.decidi(TatticheBase.deiMostri(TipoPersonaggio.DRAGO), perIMostri(drago, guerriero, mago));
        assertEquals(TipoAzione.SILENZIO, decisione.getTipoEseguito());
        assertSame(mago, decisione.getBersaglio());
    }

    @Test
    void ilDragoDaIlColpoDiGraziaAlQuasiCaduto() {
        Personaggio drago = crea(TipoPersonaggio.DRAGO, 1);
        Personaggio guerriero = crea(TipoPersonaggio.GUERRIERO, 1);
        feriscici(guerriero, 0.2);
        Decisione decisione = Valutatore.decidi(TatticheBase.deiMostri(TipoPersonaggio.DRAGO), perIMostri(drago, guerriero));
        assertEquals(TipoAzione.MORTE, decisione.getTipoEseguito());
        assertSame(guerriero, decisione.getBersaglio());
        // Se non è a un quarto, la Morte si ritorcerebbe contro il drago: non la lancia
        Personaggio sano = crea(TipoPersonaggio.GUERRIERO, 1);
        feriscici(sano, 0.6);
        assertNotEquals(TipoAzione.MORTE, Valutatore.decidi(TatticheBase.deiMostri(TipoPersonaggio.DRAGO), perIMostri(drago, sano)).getTipoEseguito());
    }

    @Test
    void l_HobgoblinTieneIlFuocoSulloStessoBersaglio() {
        boolean tieneIlBersaglio = TatticheBase.deiMostri(TipoPersonaggio.HOBGOBLIN).getVoci().stream()
                .anyMatch(v -> v.selettoreEffettivo().getOrdinamento().getTipoOrdinamento() == com.threeamigos.foresta.tipi.tattiche.TipoOrdinamento.ULTIMO_SCELTO);
        assertTrue(tieneIlBersaglio);
        assertFalse(TatticheBase.deiMostri(TipoPersonaggio.GOBLIN).getVoci().stream()
                .anyMatch(v -> v.selettoreEffettivo().getOrdinamento().getTipoOrdinamento() == com.threeamigos.foresta.tipi.tattiche.TipoOrdinamento.ULTIMO_SCELTO));
    }

    @Test
    void ilTitanoEspertoFinisceIlFeritoPerPrimo() {
        Personaggio hobgoblin = crea(TipoPersonaggio.TITANO, 9);
        Personaggio sano = crea(TipoPersonaggio.GUERRIERO, 1);
        Personaggio ferito = crea(TipoPersonaggio.LADRO, 1);
        feriscici(ferito, 0.2);
        Decisione decisione = Valutatore.decidi(TatticheBase.deiMostri(TipoPersonaggio.TITANO), perIMostri(hobgoblin, sano, ferito));
        assertEquals(TipoAzione.ATTACCO_FISICO, decisione.getTipoEseguito());
        assertSame(ferito, decisione.getBersaglio(), "astuzia " + Astuzia.di(hobgoblin) + " " + decisione.getSpiegazione());
    }

    // ---- i personaggi giocabili ----

    @Test
    void ilGuerrieroSiCuraSoloSeStaMaleEAvendoUnaPozione() {
        Personaggio guerriero = crea(TipoPersonaggio.GUERRIERO, 1);
        Personaggio goblin = crea(TipoPersonaggio.GOBLIN, 1);
        TatticaMD tattica = TatticheBase.deiGiocabili(TipoPersonaggio.GUERRIERO);
        List<Personaggio> gruppo = Collections.singletonList(guerriero);
        feriscici(guerriero, 0.2);
        assertEquals(TipoAzione.ATTACCO_FISICO, Valutatore.decidi(tattica, perIMiei(guerriero, gruppo, goblin)).getTipoEseguito(), "senza pozioni attacca");
        scorte.addPozioniSalute(1);
        Decisione cura = Valutatore.decidi(tattica, perIMiei(guerriero, gruppo, goblin));
        assertEquals(TipoAzione.POZIONE_SALUTE, cura.getTipoEseguito(), cura.getSpiegazione() + " salute " + guerriero.getSalute() + "/" + guerriero.getSaluteMassima());
        assertSame(guerriero, cura.getBersaglio());
    }

    @Test
    void ognunoHaSoloLeAzioniCheSaFare() {
        java.util.Set<TipoAzione> elfo = RegistroTattiche.azioniPossibili(TipoPersonaggio.ELFO);
        assertFalse(elfo.contains(TipoAzione.CANTO_CURATIVO), "un elfo non canta");
        assertFalse(elfo.contains(TipoAzione.ALBA_SACRA));
        assertTrue(elfo.contains(TipoAzione.DARDO_ARCANO));
        assertTrue(elfo.contains(TipoAzione.POZIONE_SALUTE_GRANDE));
        assertTrue(elfo.contains(TipoAzione.FULMINE), "le pergamene le legge chiunque");
        assertFalse(elfo.contains(TipoAzione.INCANTESIMO_DEL_REPERTORIO), "è dei mostri");
        assertTrue(RegistroTattiche.azioniPossibili(TipoPersonaggio.BARDO).contains(TipoAzione.CANTO_CURATIVO));
        assertTrue(RegistroTattiche.azioniPossibili(TipoPersonaggio.CANTASTORIE).contains(TipoAzione.CANTO_DI_VALORE));
        assertTrue(RegistroTattiche.azioniPossibili(TipoPersonaggio.SACERDOTESSA).contains(TipoAzione.ALBA_SACRA));
        assertFalse(RegistroTattiche.azioniPossibili(TipoPersonaggio.GUERRIERO).contains(TipoAzione.DARDO_ARCANO));
        // Le abilità di chierico e druido sono solo di chi le conosce
        assertTrue(RegistroTattiche.azioniPossibili(TipoPersonaggio.CHIERICA).contains(TipoAzione.SCACCIARE_I_NON_MORTI));
        assertFalse(RegistroTattiche.azioniPossibili(TipoPersonaggio.CHIERICO).contains(TipoAzione.ALBA_SACRA), "è del sacerdote");
        assertFalse(RegistroTattiche.azioniPossibili(TipoPersonaggio.CHIERICO).contains(TipoAzione.RADICI));
        assertTrue(RegistroTattiche.azioniPossibili(TipoPersonaggio.DRUIDA).contains(TipoAzione.RADICI));
        assertTrue(RegistroTattiche.azioniPossibili(TipoPersonaggio.DRUIDO).contains(TipoAzione.PELLE_DI_CORTECCIA));
        assertFalse(RegistroTattiche.azioniPossibili(TipoPersonaggio.DRUIDO).contains(TipoAzione.IMPOSIZIONE_DELLE_MANI));
        assertFalse(RegistroTattiche.azioniPossibili(TipoPersonaggio.DRUIDO).contains(TipoAzione.ALBA_SACRA));
        assertFalse(RegistroTattiche.azioniPossibili(TipoPersonaggio.SACERDOTE).contains(TipoAzione.RIGENERAZIONE));
        assertFalse(RegistroTattiche.azioniPossibili(TipoPersonaggio.MAGO).contains(TipoAzione.SCIAME));
        // Le predefinite usano solo azioni della loro classe
        for (TipoPersonaggio classe : Arrays.asList(TipoPersonaggio.GUERRIERO, TipoPersonaggio.LADRO, TipoPersonaggio.MAGO, TipoPersonaggio.ELFO,
                TipoPersonaggio.SACERDOTE, TipoPersonaggio.BARDO, TipoPersonaggio.CHIERICO, TipoPersonaggio.DRUIDO)) {
            for (VoceMD voce : TatticheBase.deiGiocabili(classe).getVoci()) {
                for (AzioneMD azione : voce.getAzioni()) {
                    assertTrue(RegistroTattiche.azioniPossibili(classe).contains(azione.getTipo()), classe + ": " + azione.getTipo());
                }
            }
        }
    }

    @Test
    void laPozioneGrandeSiBeveQuandoMancaMoltoOSeNonCEQuellaNormale() {
        Personaggio guerriero = crea(TipoPersonaggio.GUERRIERO, 1);
        Personaggio goblin = crea(TipoPersonaggio.GOBLIN, 1);
        TatticaMD tattica = TatticheBase.deiGiocabili(TipoPersonaggio.GUERRIERO);
        List<Personaggio> gruppo = Collections.singletonList(guerriero);
        scorte.addPozioniSalute(1);
        scorte.addPozioniSaluteGrande(1);
        feriscici(guerriero, 0.2);
        assertEquals(TipoAzione.POZIONE_SALUTE, Valutatore.decidi(tattica, perIMiei(guerriero, gruppo, goblin)).getTipoEseguito(), "poco ferito: la normale");
        feriscici(guerriero, 0.1);
        assertEquals(TipoAzione.POZIONE_SALUTE_GRANDE, Valutatore.decidi(tattica, perIMiei(guerriero, gruppo, goblin)).getTipoEseguito(), "quasi morto: la grande");
        // Senza pozioni normali ripiega sulla grande anche se non è così grave
        Gruppo soloGrande = new Gruppo() {
            @Override
            public boolean isGruppoGiocatore() {
                return false;
            }
        };
        soloGrande.addPozioniSaluteGrande(1);
        feriscici(guerriero, 0.2);
        assertEquals(TipoAzione.POZIONE_SALUTE_GRANDE, Valutatore.decidi(tattica,
                new ContestoTattico(guerriero, true, gruppo, Collections.singletonList(goblin), Collections.emptyList()).conGruppoDegliAlleati(soloGrande))
                .getTipoEseguito());
    }

    @Test
    void ilSacerdoteRisuscitaIlCadutoSeHaLaPergamena() {
        Personaggio sacerdote = crea(TipoPersonaggio.SACERDOTE, 1);
        Personaggio guerriero = crea(TipoPersonaggio.GUERRIERO, 1);
        Personaggio goblin = crea(TipoPersonaggio.GOBLIN, 1);
        guerriero.subSalute(1_000_000, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        scorte.addIncantesimi(TipoIncantesimo.RESURREZIONE, 1);
        Decisione decisione = Valutatore.decidi(TatticheBase.deiGiocabili(TipoPersonaggio.SACERDOTE),
                perIMiei(sacerdote, Arrays.asList(sacerdote, guerriero), goblin));
        assertEquals(TipoAzione.RESURREZIONE, decisione.getTipoEseguito());
        assertSame(guerriero, decisione.getBersaglio());
    }

    @Test
    void ilSacerdoteUsaLAlbaSacraSoloSeAlmenoDueAlleatiHannoUnoStatoNegativo() {
        Personaggio sacerdote = crea(TipoPersonaggio.SACERDOTE, 1);
        Personaggio guerriero = crea(TipoPersonaggio.GUERRIERO, 1);
        Personaggio goblin = crea(TipoPersonaggio.GOBLIN, 1);
        guerriero.addEffettoDiStato(TipoEffettoDiStato.SANGUINAMENTO, 3, 0);
        List<Personaggio> alleati = Arrays.asList(sacerdote, guerriero);
        TatticaMD tattica = TatticheBase.deiGiocabili(TipoPersonaggio.SACERDOTE);

        assertNotEquals(TipoAzione.ALBA_SACRA, Valutatore.decidi(tattica, perIMiei(sacerdote, alleati, goblin)).getTipoEseguito(),
                "uno solo con uno stato negativo non basta");

        sacerdote.addEffettoDiStato(TipoEffettoDiStato.AVVELENATO, 3, 0);
        Decisione decisione = Valutatore.decidi(tattica, perIMiei(sacerdote, alleati, goblin));
        assertEquals(TipoAzione.ALBA_SACRA, decisione.getTipoEseguito(), decisione.getSpiegazione().toString());

        StatoDelCombattimento.getIstanza().registraUsoVoce(sacerdote, decisione.getIdentificativoVoce());
        assertNotEquals(TipoAzione.ALBA_SACRA, Valutatore.decidi(tattica, perIMiei(sacerdote, alleati, goblin)).getTipoEseguito(),
                "appena lanciata, per qualche round non la ripete");
    }

    @Test
    void ilBerserkELIspirazioneNonSonoStatiDaTogliere() {
        Personaggio sacerdote = crea(TipoPersonaggio.SACERDOTE, 1);
        Personaggio guerriero = crea(TipoPersonaggio.GUERRIERO, 1);
        Personaggio goblin = crea(TipoPersonaggio.GOBLIN, 1);
        sacerdote.addEffettoDiStato(TipoEffettoDiStato.ISPIRATO, 3, 0);
        guerriero.addEffettoDiStato(TipoEffettoDiStato.BERSERK, 3, 0);
        Decisione decisione = Valutatore.decidi(TatticheBase.deiGiocabili(TipoPersonaggio.SACERDOTE),
                perIMiei(sacerdote, Arrays.asList(sacerdote, guerriero), goblin));
        assertNotEquals(TipoAzione.ALBA_SACRA, decisione.getTipoEseguito(), decisione.getSpiegazione().toString());
        assertTrue(TipoEffettoDiStato.BERSERK.isBenefico() && TipoEffettoDiStato.ISPIRATO.isBenefico());
        assertFalse(TipoEffettoDiStato.SILENZIATO.isBenefico());
    }

    @Test
    void ilBardoCantaIlCantoDiValoreAUnGruppoCheNonEIspirato() {
        Personaggio bardo = crea(TipoPersonaggio.BARDO, 1);
        Personaggio guerriero = crea(TipoPersonaggio.GUERRIERO, 1);
        Personaggio goblin = crea(TipoPersonaggio.GOBLIN, 1);
        Decisione decisione = Valutatore.decidi(TatticheBase.deiGiocabili(TipoPersonaggio.BARDO),
                perIMiei(bardo, Arrays.asList(bardo, guerriero), goblin));
        assertEquals(TipoAzione.CANTO_DI_VALORE, decisione.getTipoEseguito());
    }
}
