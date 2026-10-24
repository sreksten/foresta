package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.comandigiocatore.ComandoImpostaTattica;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoSpostaPersonaggio;
import com.threeamigos.foresta.eventi.richieste.RichiestaAperturaTattiche;
import com.threeamigos.foresta.modellodati.GruppoGiocatoreMD;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.modellodati.tattiche.TatticaMD;
import com.threeamigos.foresta.personaggi.Gigante;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tattiche.RegistroTattiche;
import com.threeamigos.foresta.tattiche.TatticheBase;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoMotivoNelGruppo;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * La schermata delle tattiche: si apre dall'inventario con MOSTRA_TATTICHE, offre i personaggi permanenti (anche i caduti) e ANNULLA;
 * la tattica e l'ordine del gruppo si cambiano con i comandi, solo con la schermata aperta.
 */
class ScenarioSchermataTatticheTest {

    private static void apriLeTattiche(PartitaDiTest partita) {
        partita.comando(Comando.ESCI_DA_CITTA);
        partita.comando(Comando.INVENTARIO);
        partita.eventi().ascolta(RichiestaAperturaTattiche.class);
        partita.comando(Comando.MOSTRA_TATTICHE);
    }

    @Test
    void siApreDallInventarioOffreIPermanentiETornaIndietro() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(231)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            Personaggio caduto = new Guerriero("Caduto", 1);
            partita.gruppo().aggiungiPersonaggio(caduto);
            Gigante gigante = new Gigante(1);
            gigante.setMotivoNelGruppo(TipoMotivoNelGruppo.AIUTO_GRATUITO);
            gigante.setTempo(3);
            partita.gruppo().aggiungiPersonaggio(gigante);
            caduto.getModelloDati().setSalute(0);

            apriLeTattiche(partita);
            partita.assertStato(Stato.TATTICHE);
            assertEquals(Arrays.asList(Comando.PERSONAGGIO_1, Comando.PERSONAGGIO_2, Comando.ANNULLA), partita.comandiDisponibili());
            RichiestaAperturaTattiche apertura = partita.eventi().ultimo(RichiestaAperturaTattiche.class);
            assertSame(partita.gruppo().getCapo(), apertura.getPersonaggio());
            // Chi non ha una tattica sua agisce con quella predefinita del ruolo, ed è quella che la schermata mostra
            assertEquals(RegistroTattiche.predefinitaDelGiocabile(TipoPersonaggio.GUERRIERO), apertura.getTattica());
            assertEquals(apertura.getTattica(), apertura.getTatticaPredefinita());

            partita.comando(Comando.PERSONAGGIO_2);
            assertSame(caduto, partita.eventi().ultimo(RichiestaAperturaTattiche.class).getPersonaggio());

            partita.comando(Comando.ANNULLA);
            partita.assertStato(Stato.INVENTARIO);
        }
    }

    @Test
    void laTatticaSiImpostaSoloConLaSchermataAperta() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(232)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            Personaggio capo = partita.gruppo().getCapo();
            TatticaMD tattica = TatticheBase.deiGiocabili(com.threeamigos.foresta.tipi.TipoPersonaggio.LADRO);

            // Schermata chiusa: ignorato
            partita.pubblica(new ComandoImpostaTattica(capo.getUuid(), tattica));
            assertNotEquals(tattica, capo.getTattica());

            apriLeTattiche(partita);
            partita.pubblica(new ComandoImpostaTattica("non-esiste", tattica));
            partita.pubblica(new ComandoImpostaTattica(null, tattica));
            partita.pubblica(new ComandoImpostaTattica(capo.getUuid(), null));
            assertNotEquals(tattica, capo.getTattica());

            // Una tattica con errori (un attacco sugli alleati) il motore non l'accetta
            TatticaMD incoerente = TatticaMD.di(com.threeamigos.foresta.modellodati.tattiche.VoceMD.su(
                    new com.threeamigos.foresta.modellodati.tattiche.SelettoreMD(com.threeamigos.foresta.tipi.tattiche.TipoRosa.ALLEATI,
                            java.util.Collections.emptyList(),
                            com.threeamigos.foresta.modellodati.tattiche.OrdinamentoMD.di(com.threeamigos.foresta.tipi.tattiche.TipoOrdinamento.PRIMO)),
                    com.threeamigos.foresta.modellodati.tattiche.AzioneMD.di(com.threeamigos.foresta.tipi.tattiche.TipoAzione.ATTACCO_FISICO)));
            partita.pubblica(new ComandoImpostaTattica(capo.getUuid(), incoerente));
            assertNotEquals(incoerente, capo.getTattica());

            partita.pubblica(new ComandoImpostaTattica(capo.getUuid(), tattica));
            assertEquals(tattica, capo.getTattica());
            assertEquals(tattica, partita.eventi().ultimo(RichiestaAperturaTattiche.class).getTattica());
        }
    }

    @Test
    void ilPersonaggioSiSpostaNelGruppoSenzaCambiareIlCapoENonOltreIMargini() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(233)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            Personaggio capo = partita.gruppo().getCapo();
            Personaggio secondo = new Guerriero("Secondo", 1);
            partita.gruppo().aggiungiPersonaggio(secondo);
            Gigante gigante = new Gigante(1);
            gigante.setMotivoNelGruppo(TipoMotivoNelGruppo.AIUTO_GRATUITO);
            gigante.setTempo(3);
            partita.gruppo().aggiungiPersonaggio(gigante);
            Personaggio terzo = new Guerriero("Terzo", 1);
            partita.gruppo().aggiungiPersonaggio(terzo);

            apriLeTattiche(partita);
            // Al margine non si muove
            partita.pubblica(new ComandoSpostaPersonaggio(capo.getUuid(), false));
            partita.pubblica(new ComandoSpostaPersonaggio(terzo.getUuid(), true));
            assertSame(capo, partita.gruppo().getPersonaggio(0));

            // Il secondo va a destra scavalcando chi è a tempo: scambia con il terzo
            partita.pubblica(new ComandoSpostaPersonaggio(secondo.getUuid(), true));
            assertSame(terzo, partita.gruppo().getPersonaggio(1));
            assertSame(gigante, partita.gruppo().getPersonaggio(2));
            assertSame(secondo, partita.gruppo().getPersonaggio(3));
            assertSame(capo, partita.gruppo().getCapo());
            // La schermata segue il personaggio spostato, con i comandi nel nuovo ordine
            assertSame(secondo, partita.eventi().ultimo(RichiestaAperturaTattiche.class).getPersonaggio());
            assertEquals(Arrays.asList(Comando.PERSONAGGIO_1, Comando.PERSONAGGIO_2, Comando.PERSONAGGIO_4, Comando.ANNULLA),
                    partita.comandiDisponibili());

            // Chi è a tempo non si sposta
            partita.pubblica(new ComandoSpostaPersonaggio(gigante.getUuid(), true));
            assertSame(gigante, partita.gruppo().getPersonaggio(2));
        }
    }

    @Test
    void ancheIlCapoSiSpostaEIlGruppoSiRicordaChiE() throws Exception {
        try (PartitaDiTest partita = PartitaDiTest.nuova(234)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            Personaggio capo = partita.gruppo().getCapo();
            Personaggio secondo = new Guerriero("Secondo", 1);
            partita.gruppo().aggiungiPersonaggio(secondo);

            apriLeTattiche(partita);
            partita.pubblica(new ComandoSpostaPersonaggio(capo.getUuid(), true));
            assertSame(secondo, partita.gruppo().getPersonaggio(0));
            assertSame(capo, partita.gruppo().getPersonaggio(1));
            assertSame(capo, partita.gruppo().getCapo());

            // Salvataggio e rilettura: l'ordine e il capo sono quelli di prima
            GruppoGiocatoreMD md = ModelloDati.getIstanza().getGruppoGiocatoreMD();
            assertEquals(1, md.getIndiceCapo());
            StringWriter scritto = new StringWriter();
            PrintWriter stream = new PrintWriter(scritto);
            md.salva(stream);
            stream.flush();
            GruppoGiocatoreMD riletto = new GruppoGiocatoreMD();
            riletto.leggi(new BufferedReader(new StringReader(scritto.toString())));
            GruppoGiocatore gruppo = GruppoGiocatore.of(riletto);
            assertEquals(secondo.getUuid(), gruppo.getPersonaggio(0).getUuid());
            assertEquals(capo.getUuid(), gruppo.getCapo().getUuid());
            assertEquals(1, riletto.getIndiceCapo());
        }
    }
}
