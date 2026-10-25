package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.mappa.Foresta;

import com.threeamigos.foresta.combattimento.ApplicazioneDelRisultato;
import com.threeamigos.foresta.bestiario.RegistroBestiario;
import com.threeamigos.foresta.combattimento.DannoRisultante;
import com.threeamigos.foresta.danni.tipi.TipoDanno;
import com.threeamigos.foresta.effettidistato.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.eventi.interni.*;
import com.threeamigos.foresta.incantesimi.FabbricaIncantesimi;
import com.threeamigos.foresta.missioni.Missione;
import com.threeamigos.foresta.missioni.RegistroMissioni;
import com.threeamigos.foresta.missioni.storie.IlCampione;
import com.threeamigos.foresta.missioni.storie.LaSfidaDeiCampioni;
import com.threeamigos.foresta.mappa.CoordinateMD;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.personaggi.*;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.tipi.*;
import com.threeamigos.foresta.trofei.RegistroTrofei;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Il bestiario (vedi bestiario.md): le voci si sbloccano sconfiggendo un avversario, facendoci amicizia, corrompendolo, battendolo a carta,
 * forbici e sasso, o avendolo nel gruppo; le caratteristiche si scoprono vedendole in uno scontro; tutto vale da una partita all'altra.
 */
class ScenarioBestiarioTest {

    private static void iniziaDaMaga(PartitaDiTest partita) {
        partita.iniziaCon("Arsenia", Comando.FEMMINA, Comando.MAGA, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
        partita.assertStato(Stato.IN_LOCAZIONE);
    }

    @Test
    void ilPersonaggioSceltoAInizioPartitaSbloccaLaSuaVoceEQuellaDiChiCondivideIlRuolo() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(901)) {
            partita.eventi().ascolta(InternoVoceDelBestiarioSbloccata.class);
            iniziaDaMaga(partita);

            assertTrue(RegistroBestiario.isSbloccata(TipoPersonaggio.MAGA));
            assertTrue(RegistroBestiario.isSbloccata(TipoPersonaggio.MAGO), "Mago e Maga sono la stessa voce");
            assertFalse(RegistroBestiario.isSbloccata(TipoPersonaggio.GUERRIERO));
            assertEquals(TipoPersonaggio.MAGO, partita.eventi().ultimo(InternoVoceDelBestiarioSbloccata.class).getVoce());
            assertTrue(partita.bestiario().contieneRiga("MAGO|true|"), "appena sbloccata, la voce va salvata");
        }
    }

    @Test
    void unCompagnoReclutatoOUnAiutoNelGruppoSbloccaLaSuaVoceMaUnOspiteNo() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(902)) {
            iniziaDaMaga(partita);

            partita.gruppo().aggiungiPersonaggio(new Druido("Alder", 1));
            partita.gruppo().aggiungiOspite(new Ladro("Viaggiatore", 1));

            assertTrue(RegistroBestiario.isSbloccata(TipoPersonaggio.DRUIDA), "il druido e la druida sono una voce sola");
            assertFalse(RegistroBestiario.isSbloccata(TipoPersonaggio.LADRO), "un ospite viaggia con il gruppo senza farne parte");
        }
    }

    @Test
    void sconfiggereUnAvversarioSbloccaLaSuaVoceUnaVoltaSola() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(903)) {
            iniziaDaMaga(partita);
            partita.eventi().ascolta(InternoVoceDelBestiarioSbloccata.class);

            partita.pubblica(new InternoAvversarioSconfitto(TipoPersonaggio.GOBLIN));
            partita.pubblica(new InternoAvversarioSconfitto(TipoPersonaggio.GOBLIN));

            assertTrue(RegistroBestiario.isSbloccata(TipoPersonaggio.GOBLIN));
            assertFalse(RegistroBestiario.isSbloccata(TipoPersonaggio.HOBGOBLIN));
            assertEquals(1, partita.eventi().tutti(InternoVoceDelBestiarioSbloccata.class).size());
            assertTrue(partita.bestiario().contieneRiga("GOBLIN|true|"));
            int salvataggi = partita.bestiario().getSalvataggi();
            partita.pubblica(new InternoAvversarioSconfitto(TipoPersonaggio.GOBLIN));
            assertEquals(salvataggi, partita.bestiario().getSalvataggi(), "una voce già sbloccata non si salva di nuovo");
        }
    }

    @Test
    void amiciziaECorruzioneContanoComeUnIncontroRiuscitoPerTuttiGliAvversariPresenti() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(904)) {
            iniziaDaMaga(partita);

            partita.pubblica(new InternoAmiciziaStretta(Arrays.asList(TipoPersonaggio.TROLL, TipoPersonaggio.GOBLIN)));
            partita.pubblica(new InternoCorruzioneRiuscita(Collections.singletonList(TipoPersonaggio.MINOTAURO)));

            assertTrue(RegistroBestiario.isSbloccata(TipoPersonaggio.TROLL));
            assertTrue(RegistroBestiario.isSbloccata(TipoPersonaggio.GOBLIN));
            assertTrue(RegistroBestiario.isSbloccata(TipoPersonaggio.MINOTAURO));
        }
    }

    @Test
    void unNonCombattenteNonHaUnaVoceDaSbloccare() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(905)) {
            iniziaDaMaga(partita);
            partita.eventi().ascolta(InternoVoceDelBestiarioSbloccata.class);

            partita.pubblica(new InternoAvversarioSconfitto(TipoPersonaggio.LOCANDIERE));
            partita.pubblica(new InternoSfidaVinta(TipoPersonaggio.VIANDANTE));

            assertFalse(RegistroBestiario.isSbloccata(TipoPersonaggio.LOCANDIERE));
            assertTrue(partita.eventi().tutti(InternoVoceDelBestiarioSbloccata.class).isEmpty(), "dopo quella della maga, che non c'è più in ascolto");
        }
    }

    @Test
    void vincereUnaSfidaACartaForbiciESassoSbloccaLaVoceDelCampione() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(7)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            LaSfidaDeiCampioni missione = (LaSfidaDeiCampioni) RegistroMissioni.avvia(MissionePredefinita.LA_SFIDA_DEI_CAMPIONI);
            missione.controllaPostLocazione();
            missione.getCampioni().forEach(Missione::controllaInLocazione);
            partita.comando(Comando.ESCI_DA_CITTA);
            IlCampione campione = (IlCampione) missione.getCampioni().get(0);
            TipoPersonaggio classeDelCampione = campione.getClasseDelCampione();
            // Il campione è di una classe che non è ancora sbloccata: quella del giocatore è il Guerriero
            if (classeDelCampione.stessoRuoloDi(TipoPersonaggio.GUERRIERO)) {
                campione = (IlCampione) missione.getCampioni().stream().filter(c -> !((IlCampione) c).getClasseDelCampione()
                        .stessoRuoloDi(TipoPersonaggio.GUERRIERO)).findFirst().orElseThrow(AssertionError::new);
                classeDelCampione = campione.getClasseDelCampione();
            }
            assertFalse(RegistroBestiario.isSbloccata(classeDelCampione));
            vaiDa(partita, campione.getPosto());

            // Tre mani vinte: la carta batte il sasso
            Dado.trucca(3, 3, 3);
            partita.comando(Comando.CARTA);
            partita.comando(Comando.CARTA);
            partita.comando(Comando.CARTA);

            assertTrue(RegistroBestiario.isSbloccata(classeDelCampione), classeDelCampione + " sfidato e battuto");
        }
    }

    private static void vaiDa(PartitaDiTest partita, CoordinateMD posto) {
        int[][] direzioni = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}};
        Comando[] comandi = {Comando.NORD, Comando.EST, Comando.SUD, Comando.OVEST};
        for (int i = 0; i < direzioni.length; i++) {
            int x = posto.getX() - direzioni[i][0];
            int y = posto.getY() - direzioni[i][1];
            if (x >= 0 && y >= 0 && x < Foresta.getDimensioneX() && y < Foresta.getDimensioneY()) {
                partita.gruppo().setCoordinate(new CoordinateMD(x, y));
                CostruttoreDellaForesta.aggiornaMappaCircostante(partita.gruppo());
                partita.comando(comandi[i]);
                if (partita.comandiDisponibili().contains(Comando.NUMERO_1)) {
                    partita.comando(Comando.NUMERO_1);
                }
                return;
            }
        }
        fail("Nessuna casella accanto a " + posto);
    }

    @Test
    void unColpoDiUnTipoDiDannoFaScoprireComeLaClasseLoSubisceSoloSeEUnaSuaCaratteristica() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(906)) {
            iniziaDaMaga(partita);
            Personaggio maga = partita.gruppo().getCapo();
            Personaggio scheletro = FabbricaPersonaggi.crea(TipoPersonaggio.SCHELETRO, 1);
            Personaggio goblin = FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 1);

            colpisci(maga, scheletro, TipoDanno.CONTUNDENTE);
            colpisci(maga, goblin, TipoDanno.FUOCO);

            assertEquals(Collections.singleton("DANNO:CONTUNDENTE"), RegistroBestiario.getCaratteristicheScoperte(TipoPersonaggio.SCHELETRO));
            assertTrue(RegistroBestiario.getCaratteristicheScoperte(TipoPersonaggio.GOBLIN).isEmpty(), "il goblin subisce il danno normale");
            assertFalse(RegistroBestiario.isSbloccata(TipoPersonaggio.SCHELETRO), "scoperta, ma la voce è ancora chiusa");
            assertTrue(partita.bestiario().contieneRiga("SCHELETRO|false|DANNO:CONTUNDENTE"), "la scoperta va salvata");
        }
    }

    private static void colpisci(Personaggio attaccante, Personaggio difensore, TipoDanno tipoDanno) {
        DannoRisultante risultato = new DannoRisultante(attaccante, difensore);
        risultato.setTipoDanno(tipoDanno);
        risultato.setDanno(1);
        ApplicazioneDelRisultato.applica(difensore, risultato);
    }

    @Test
    void unIncantesimoRespintoEUnEffettoRifiutatoPerImmunitaVengonoRicordati() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(907)) {
            iniziaDaMaga(partita);
            Personaggio maga = partita.gruppo().getCapo();
            Personaggio scheletro = FabbricaPersonaggi.crea(TipoPersonaggio.SCHELETRO, 1);

            FabbricaIncantesimi.crea(TipoIncantesimo.MORTE, 1).formula(maga, scheletro, null);
            scheletro.addEffettoDiStato(TipoEffettoDiStato.SANGUINAMENTO, 2, 1);

            assertTrue(RegistroBestiario.getCaratteristicheScoperte(TipoPersonaggio.SCHELETRO).contains("INCANTESIMO:MORTE"));
            assertTrue(RegistroBestiario.getCaratteristicheScoperte(TipoPersonaggio.SCHELETRO).contains("EFFETTO:SANGUINAMENTO"));
        }
    }

    @Test
    void chiEImmuneATuttiGliIncantesimiLoScopreConUnoQualunque() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(908)) {
            iniziaDaMaga(partita);
            Personaggio maga = partita.gruppo().getCapo();
            Personaggio gargoyle = FabbricaPersonaggi.crea(TipoPersonaggio.GARGOYLE, 1);

            FabbricaIncantesimi.crea(TipoIncantesimo.FUOCO, 1).formula(maga, gargoyle, null);

            assertTrue(RegistroBestiario.getCaratteristicheScoperte(TipoPersonaggio.GARGOYLE).contains("INCANTESIMO:TUTTI"),
                    RegistroBestiario.getCaratteristicheScoperte(TipoPersonaggio.GARGOYLE).toString());
        }
    }

    @Test
    void leImmunitaDiUnSoloPersonaggioNonSonoCaratteristicheDellaClasse() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(909)) {
            iniziaDaMaga(partita);
            Personaggio guerriero = new Guerriero("Berserker", 1);
            guerriero.addEffettoDiStato(TipoEffettoDiStato.BERSERK, 3, 0);

            assertTrue(guerriero.isImmuneAEffetto(TipoEffettoDiStato.CONFUSO), "in berserk il guerriero non si confonde");
            guerriero.addEffettoDiStato(TipoEffettoDiStato.CONFUSO, 2, 0);

            assertFalse(guerriero.hasEffettoDiStato(TipoEffettoDiStato.CONFUSO));
            assertTrue(RegistroBestiario.getCaratteristicheScoperte(TipoPersonaggio.GUERRIERO).isEmpty(),
                    "è un'immunità del suo stato, non della classe");
        }
    }

    @Test
    void ilBestiarioValeDaUnaPartitaAllAltra() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(910)) {
            iniziaDaMaga(partita);
            partita.pubblica(new InternoAvversarioSconfitto(TipoPersonaggio.TROLL));
            Personaggio scheletro = FabbricaPersonaggi.crea(TipoPersonaggio.SCHELETRO, 1);
            colpisci(partita.gruppo().getCapo(), scheletro, TipoDanno.TAGLIENTE);

            // Una nuova partita azzera il modello dati, non il bestiario
            ModelloDati.setIstanza(new ModelloDati());
            assertTrue(RegistroBestiario.isSbloccata(TipoPersonaggio.TROLL));

            // Un riavvio del gioco lo rilegge da dove era stato salvato
            RegistroBestiario.impostaGestoreBestiario(partita.bestiario());
            assertTrue(RegistroBestiario.isSbloccata(TipoPersonaggio.TROLL));
            assertTrue(RegistroBestiario.isSbloccata(TipoPersonaggio.MAGO));
            assertEquals(Collections.singleton("DANNO:TAGLIENTE"), RegistroBestiario.getCaratteristicheScoperte(TipoPersonaggio.SCHELETRO));
        }
    }

    @Test
    void dallInventarioAlBestiarioEIndietro() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(911)) {
            iniziaDaMaga(partita);
            partita.comando(Comando.ESCI_DA_CITTA);
            partita.comando(Comando.INVENTARIO);
            partita.assertStato(Stato.INVENTARIO);
            partita.assertComandoDisponibile(Comando.MOSTRA_BESTIARIO);

            partita.comando(Comando.MOSTRA_BESTIARIO);
            partita.assertStato(Stato.BESTIARIO);
            assertEquals(Collections.singletonList(Comando.ANNULLA), partita.comandiDisponibili());

            partita.comando(Comando.ANNULLA);
            partita.assertStato(Stato.INVENTARIO);
            partita.assertComandoDisponibile(Comando.MOSTRA_BESTIARIO);
        }
    }

    @Test
    void unComandoDiversoDaAnnullaNelBestiarioNonSiAccetta() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(912)) {
            iniziaDaMaga(partita);
            partita.comando(Comando.ESCI_DA_CITTA);
            partita.comando(Comando.INVENTARIO);
            partita.comando(Comando.MOSTRA_BESTIARIO);

            // Il motore segnala l'errore (lo stesso dei trofei), e si resta nel bestiario
            AssertionError errore = assertThrows(AssertionError.class, () -> partita.comando(Comando.MOSTRA_TROFEI));

            assertTrue(errore.getMessage().contains("errori"), errore.getMessage());
            partita.assertStato(Stato.BESTIARIO);
        }
    }

    @Test
    void ilTrofeoEnciclopedistaSiVinceConTutteLeVociALFineLocazione() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(913)) {
            iniziaDaMaga(partita);
            partita.eventi().ascolta(InternoTrofeoAcquisito.class);
            com.threeamigos.foresta.trofei.Trofeo trofeo = com.threeamigos.foresta.trofei.ClasseTrofeo.di(TipoTrofeo.ENCICLOPEDISTA);
            assertEquals(33, trofeo.getObiettivo());
            assertEquals(1, trofeo.getProgresso(), "la voce del Mago, del personaggio scelto");

            for (TipoPersonaggio voce : TipoPersonaggio.getVociDelBestiario()) {
                partita.pubblica(new InternoAvversarioSconfitto(voce));
            }
            assertEquals(33, trofeo.getProgresso());
            assertFalse(RegistroTrofei.isVinto(TipoTrofeo.ENCICLOPEDISTA), "i trofei si controllano solo a fine locazione");
            partita.pubblica(new InternoFineLocazione());

            assertTrue(RegistroTrofei.isVinto(TipoTrofeo.ENCICLOPEDISTA));
            assertTrue(partita.eventi().tutti(InternoTrofeoAcquisito.class).stream().anyMatch(e -> e.getTrofeo() == TipoTrofeo.ENCICLOPEDISTA));
        }
    }

    @Test
    void ilTrofeoEnciclopedistaNonSiVinceCon31Voci() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(914)) {
            iniziaDaMaga(partita);
            for (TipoPersonaggio voce : TipoPersonaggio.getVociDelBestiario()) {
                if (voce != TipoPersonaggio.DRAGO) {
                    partita.pubblica(new InternoAvversarioSconfitto(voce));
                }
            }

            partita.pubblica(new InternoFineLocazione());

            assertFalse(RegistroTrofei.isVinto(TipoTrofeo.ENCICLOPEDISTA));
        }
    }
}
