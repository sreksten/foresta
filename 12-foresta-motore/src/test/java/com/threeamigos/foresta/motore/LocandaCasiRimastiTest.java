package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.economia.Cassa;
import com.threeamigos.foresta.gruppi.GruppoAvversario;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.intermezzi.Locande;
import com.threeamigos.foresta.locazioni.Locanda;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.modellodati.LocazioneMD;
import com.threeamigos.foresta.mondo.RegistroPersonaggi;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.CostantiEconomia;
import com.threeamigos.foresta.tipi.TipoLocazione;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I casi della locanda che il gioco incontra di rado: il racconto della prima e della seconda visita senza dialogo o recensione, il pasto
 * che si può pagare per uno solo (con un compagno morto), l'amico che se n'è andato prima che il gruppo lo incontrasse, e le cose che alla
 * locanda non si chiedono.
 */
class LocandaCasiRimastiTest {

    private static PartitaDiTest partitaInUnaLocanda(long seme) {
        PartitaDiTest partita = PartitaDiTest.nuova(seme);
        partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
        // Una casella accanto alla città diventa una locanda del bosco, con i dati che servono
        CoordinateMD citta = Foresta.getPosizioneDelGruppo();
        CoordinateMD posto = new CoordinateMD(citta.getX(), citta.getY() + (citta.getY() + 1 < Foresta.getDimensioneY() ? 1 : -1));
        Foresta.costruisciLocazione(posto, TipoLocazione.LOCANDA);
        LocazioneMD md = Foresta.getLocazioneMD(posto);
        md.setNome("la Locanda di prova");
        md.aggiungiProprieta(Locande.LOCANDA_IDENTIFICATIVO, "CINGHIALE_VEGANO");
        md.aggiungiProprieta(Locanda.LOCANDA_NOME_LOCANDIERE, "Gino");
        Foresta.setPosizioneDelGruppo(posto);
        return partita;
    }

    private static Locanda locanda() {
        Locanda locanda = new Locanda();
        locanda.setModelloDati(Foresta.getLocazioneMD(Foresta.getPosizioneDelGruppo()));
        return locanda;
    }

    @Test
    void senzaDialogoLaPrimaVisitaRaccontaLaRecensioneELaSecondaNomina() {
        try (PartitaDiTest partita = partitaInUnaLocanda(141)) {
            LocazioneMD md = Foresta.getLocazioneMD(Foresta.getPosizioneDelGruppo());
            md.aggiungiProprieta(Locande.LOCANDA_DIALOGO, "");
            md.aggiungiProprieta(Locande.LOCANDA_RECENSIONE, "il vino è ottimo");
            Locanda locanda = locanda();

            locanda.descrivi(partita.gruppo(), GruppoAvversario.getIstanza());
            assertTrue(partita.ultimoTesto().contains("A quanto si dice, il vino è ottimo"), partita.ultimoTesto());

            locanda.descrivi(partita.gruppo(), GruppoAvversario.getIstanza());
            assertTrue(partita.ultimoTesto().contains("è alla locanda"), partita.ultimoTesto());
        }
    }

    @Test
    void senzaDialogoNeRecensioneSiNominaSoloLaLocanda() {
        try (PartitaDiTest partita = partitaInUnaLocanda(142)) {
            Locanda locanda = locanda();

            locanda.descrivi(partita.gruppo(), GruppoAvversario.getIstanza());

            assertTrue(partita.ultimoTesto().contains("è alla locanda"), partita.ultimoTesto());
        }
    }

    @Test
    void laLocandaNonOffreMappaNeInventarioNeUnPosticinoAll_aperto() {
        try (PartitaDiTest partita = partitaInUnaLocanda(143)) {
            Locanda locanda = locanda();

            assertThrows(IllegalStateException.class, locanda::ripresentaComandi);
            assertEquals(com.threeamigos.foresta.tipi.TipoRiposo.AL_COPERTO, locanda.getTipoRiposo());
        }
    }

    @Test
    void conMoneteSoloPerUnPastoChiMangiaEUnoFraIVivi() {
        try (PartitaDiTest partita = partitaInUnaLocanda(144)) {
            GruppoGiocatore gruppo = partita.gruppo();
            Personaggio caduto = new Guerriero("Caduto", 1);
            gruppo.aggiungiPersonaggio(caduto);
            gruppo.aggiungiPersonaggio(new Guerriero("Vivo", 1));
            caduto.muore("prova");
            Cassa.subMonete(Cassa.getMonete());
            Cassa.addMonete(CostantiEconomia.COSTO_PASTO);
            Locanda locanda = locanda();
            GruppoAvversario avversari = GruppoAvversario.getIstanza();

            assertEquals(Stato.IN_LOCAZIONE, locanda.impostaAzioni(gruppo, avversari, null));
            assertTrue(partita.testi().stream().anyMatch(t -> t.contains("Chi lo fa?")), String.valueOf(partita.testi()));
            Stato fine = locanda.impostaAzioni(gruppo, avversari, Comando.PERSONAGGIO_1);

            assertEquals(Stato.FINE_LOCAZIONE, fine);
            assertTrue(partita.testi().stream().anyMatch(t -> t.contains("si è rifocillat")), String.valueOf(partita.testi()));
        }
    }

    @Test
    void seL_amicoSeNEGiaAndatoLaLocandaNonLoPropone() {
        try (PartitaDiTest partita = partitaInUnaLocanda(145)) {
            GruppoGiocatore gruppo = partita.gruppo();
            CoordinateMD posto = Foresta.getPosizioneDelGruppo();
            RegistroPersonaggi.addPersonaggioInLocazione(new Guerriero("Amico", 1), posto);
            Locanda locanda = locanda();
            GruppoAvversario avversari = GruppoAvversario.getIstanza();
            avversari.reimposta();
            locanda.crea(gruppo, avversari);
            RegistroPersonaggi.rimuoviPersonaggioInLocazione(posto);
            Cassa.addMonete(1000);

            Stato stato = locanda.impostaAzioni(gruppo, avversari, null);

            assertEquals(Stato.IN_LOCAZIONE, stato);
            assertTrue(partita.testi().stream().noneMatch(t -> t.contains("vecchia amicizia")));
        }
    }

    @Test
    void rifiutareUnAmicoCheNonCEPiuNonDiceNiente() {
        try (PartitaDiTest partita = partitaInUnaLocanda(146)) {
            Locanda locanda = locanda();
            int testiPrima = partita.testi().size();

            locanda.accetta(partita.gruppo(), false);

            assertEquals(testiPrima, partita.testi().size());
        }
    }
}
