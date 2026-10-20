package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.richieste.RichiestaSelezioneSlotPerRilettura;
import com.threeamigos.foresta.interfacce.GestoreSalvataggi;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.strumenti.TestataSalvataggio;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Il salvataggio automatico in memoria: a ogni fine di locazione l'Automa ne tiene uno, solo in memoria; compare per ultimo fra i
 * salvataggi da caricare (quinta riga, "salvataggio automatico"), non si può scrivere a mano e resta anche dopo una sconfitta.
 */
class SalvataggioAutomaticoInMemoriaTest {

    private static PartitaDiTest inCitta(PartitaDiTest partita) {
        return partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
    }

    @Test
    void aFineLocazioneSiTieneLUltimoStatoInMemoria() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(310)) {
            inCitta(partita);
            assertFalse(partita.salvataggioInMemoria().isPresente(), "prima della fine di una locazione non c'è niente");

            partita.comando(Comando.ESCI_DA_CITTA);

            assertTrue(partita.salvataggioInMemoria().isPresente());
            TestataSalvataggio testata = partita.salvataggioInMemoria().getTestata();
            assertEquals(GestoreSalvataggi.SLOT_IN_MEMORIA, testata.getId());
            assertTrue(testata.getDescrizione().startsWith("salvataggio automatico"), testata.getDescrizione());
            assertEquals(1, testata.getGruppoGiocatore().getNumeroPersonaggi());
        }
    }

    @Test
    void nonSiScriveSuDiscoENonSiSalvaAMano() {
        GestoreSalvataggiInMemoria salvataggi = new GestoreSalvataggiInMemoria();
        try (PartitaDiTest partita = PartitaDiTest.nuovaConSalvataggi(311, salvataggi)) {
            inCitta(partita).comando(Comando.ESCI_DA_CITTA);

            assertTrue(salvataggi.getSalvataggiDisponibili().isEmpty(), "l'elenco dei salvataggi su disco non lo vede");
            assertFalse(salvataggi.contiene(GestoreSalvataggi.SLOT_IN_MEMORIA));

            // E la schermata del salvataggio propone solo i quattro slot del giocatore
            partita.comando(Comando.FLOPPY_SALVA);
            assertFalse(partita.comandiDisponibili().contains(Comando.NUMERO_5), String.valueOf(partita.comandiDisponibili()));
            assertTrue(partita.comandiDisponibili().containsAll(GestoreSalvataggi.SLOT_MANUALI));
            partita.comando(Comando.NUMERO_5);
            partita.assertStato(Stato.SELEZIONE_SALVATAGGIO_DA_SCRIVERE);
            assertFalse(salvataggi.contiene(Comando.NUMERO_5));
        }
    }

    @Test
    void dopoUnaSconfittaSiRiprendeDallUltimaFineDiLocazione() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(312)) {
            inCitta(partita).comando(Comando.ESCI_DA_CITTA);
            CoordinateMD doveEra = partita.gruppo().getCoordinate();
            finoAllIntroDopoLaSconfitta(partita);
            assertTrue(partita.salvataggioInMemoria().isPresente(), "dopo la sconfitta resta");
            assertTrue(partita.comandiDisponibili().contains(Comando.FLOPPY_CARICA), "e fa comparire il caricamento");

            partita.eventi().ascolta(RichiestaSelezioneSlotPerRilettura.class);
            partita.comando(Comando.FLOPPY_CARICA);
            List<TestataSalvataggio> elenco = partita.eventi().tutti(RichiestaSelezioneSlotPerRilettura.class).stream()
                    .flatMap(e -> e.getSalvataggiDisponibili().stream()).collect(Collectors.toList());
            assertEquals(1, elenco.size(), "c'è solo il salvataggio automatico");
            assertEquals(Comando.NUMERO_5, elenco.get(0).getId());

            partita.comando(Comando.NUMERO_5);

            assertTrue(partita.stato() == Stato.ATTESA_DIREZIONE || partita.stato() == Stato.SCELTA_DIREZIONE, "" + partita.stato());
            assertEquals(doveEra, partita.gruppo().getCoordinate());
            assertEquals(1, partita.gruppo().getNumeroPersonaggi(), "il gruppo com'era a fine locazione, senza il compagno arrivato dopo");
            assertEquals(TipoPersonaggio.GUERRIERO, partita.gruppo().getCapo().getClasse());
            assertTrue(partita.salvataggioInMemoria().isPresente(), "caricarlo non lo toglie");
        }
    }

    @Test
    void caricareUnAltroSalvataggioLoDimentica() {
        GestoreSalvataggiInMemoria salvataggi = new GestoreSalvataggiInMemoria();
        try (PartitaDiTest partita = PartitaDiTest.nuovaConSalvataggi(313, salvataggi)) {
            inCitta(partita).comando(Comando.ESCI_DA_CITTA);
            salvataggi.salva(Comando.NUMERO_1);
            finoAllIntroDopoLaSconfitta(partita);
            assertTrue(partita.salvataggioInMemoria().isPresente());

            partita.comando(Comando.FLOPPY_CARICA).comando(Comando.NUMERO_1);

            assertFalse(partita.salvataggioInMemoria().isPresente(), "è di un'altra linea della partita");
        }
    }

    @Test
    void unaPartitaNuovaLoDimentica() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(314)) {
            inCitta(partita).comando(Comando.ESCI_DA_CITTA);
            finoAllIntroDopoLaSconfitta(partita);
            assertTrue(partita.salvataggioInMemoria().isPresente());

            partita.comando(Comando.PERGAMENA).testo("Brunilde");

            assertFalse(partita.salvataggioInMemoria().isPresente());
        }
    }

    /**
     * Dopo una fine di locazione e l'arrivo di un compagno, il tempo finisce: la partita è persa e si torna alla schermata iniziale.
     */
    private static void finoAllIntroDopoLaSconfitta(PartitaDiTest partita) {
        partita.gruppo().aggiungiPersonaggio(new Guerriero("Compagno", 1));
        partita.gruppo().setCoordinate(unaCasellaDiBosco());
        while (LineaTemporale.getOra() <= 20) {
            LineaTemporale.aggiungiOre(1);
        }
        ModelloDati.getIstanza().getLineaTemporaleMD().setGiorno(40);
        partita.comando(Comando.ACCAMPAMENTO);
        partita.assertStato(Stato.GIOCO_PERSO);
        for (int i = 0; i < 30 && partita.stato() != Stato.INTRO; i++) {
            if (partita.stato() == Stato.ATTESA_NOME_PUNTEGGI) {
                partita.testo("Arsenio");
            } else {
                partita.comando(Comando.PERGAMENA);
            }
        }
        partita.assertStato(Stato.INTRO);
    }

    private static CoordinateMD unaCasellaDiBosco() {
        for (int x = 0; x < Foresta.getDimensioneX(); x++) {
            for (int y = 0; y < Foresta.getDimensioneY(); y++) {
                if (Foresta.getLocazione(x, y) == TipoLocazione.BOSCO) {
                    return new CoordinateMD(x, y);
                }
            }
        }
        throw new AssertionError("nessuna casella di bosco");
    }
}
