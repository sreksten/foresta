package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.mondo.RegistroPersonaggi;
import com.threeamigos.foresta.gruppi.GruppoAvversario;
import com.threeamigos.foresta.tipi.Quadrante;

import com.threeamigos.foresta.tipi.CategoriaLocazione;

import com.threeamigos.foresta.tipi.TipoLocazione;

import com.threeamigos.foresta.mappa.Foresta;

import com.threeamigos.foresta.tipi.TipoNegozio;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoAvversarioSconfitto;
import com.threeamigos.foresta.eventi.richieste.RichiestaAperturaIncantatore;
import com.threeamigos.foresta.eventi.richieste.RichiestaAperturaInventarioCommerciante;
import com.threeamigos.foresta.eventi.richieste.RichiestaAperturaInventarioFornitore;
import com.threeamigos.foresta.locazioni.Bottega;
import com.threeamigos.foresta.locazioni.Locazione;
import com.threeamigos.foresta.missioni.RegistroMissioni;
import com.threeamigos.foresta.missioni.incarichi.*;
import com.threeamigos.foresta.missioni.richieste.InfestazioneRichiesta;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.modellodati.LocazioneMD;
import com.threeamigos.foresta.testi.ProduttoreDiMissioniCasuale;
import com.threeamigos.foresta.tipi.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le quattro missioni delle botteghe nella Foresta (IlNegozio): il negoziante di una città chiede di disinfestare delle rovine
 * di un suo parente, e a missione completa le rovine diventano la bottega.
 */
class ScenarioNegoziNellaForestaTest {

    private static List<IlNegozio> negozi() {
        List<IlNegozio> negozi = new ArrayList<>();
        negozi.add(trova(IlNegozioDellAlchimista.class));
        negozi.add(trova(IlNegozioDellArmaiolo.class));
        negozi.add(trova(IlNegozioDelVenditoreDiPergamene.class));
        negozi.add(trova(IlNegozioDellIncantatore.class));
        return negozi;
    }

    private static <T extends IlNegozio> T trova(Class<T> tipo) {
        return RegistroMissioni.getTutteLeMissioni().stream().filter(tipo::isInstance).map(tipo::cast).findFirst()
                .orElseThrow(() -> new AssertionError("missione " + tipo.getSimpleName() + " non trovata"));
    }

    private static CoordinateMD coordinateDi(TipoLocazione citta) {
        return Foresta.getCoordinateLocazioneUnica(citta);
    }

    /**
     * Da che parte della città (verso il basso della mappa, +1, o verso l'alto, -1) c'è posto per la bottega e per un passo prima di
     * arrivarci: la città, sorteggiata, può stare vicino a un bordo.
     */
    private static int latoLibero(CoordinateMD citta) {
        return citta.getY() + 2 < Foresta.getDimensioneY() ? 1 : -1;
    }

    @Test
    void leGrammaticheSiLeggono() {
        for (int i = 0; i < 300; i++) {
            InfestazioneRichiesta infestazione = InfestazioneRichiesta.da(ProduttoreDiMissioniCasuale.rigaDiMissioni("NEGOZIO_NEMICI"));
            assertFalse(infestazione.getDescrizione().isEmpty());
            for (String produzione : new String[] {"PARENTE", "CAUSA", "EREDITA", "APPELLO", "BATTUTA", "RISPOSTA", "VITTORIA",
                    "RINGRAZIAMENTO", "APERTURA", "NOME_ALCHIMISTA", "NOME_ARMAIOLO", "NOME_VENDITORE_DI_PERGAMENE", "NOME_INCANTATORE"}) {
                String riga = ProduttoreDiMissioniCasuale.rigaDiMissioni("NEGOZIO_" + produzione);
                assertFalse(riga.isEmpty() || riga.contains("[") || riga.contains("]"), produzione + ": " + riga);
            }
        }
        assertThrows(IllegalArgumentException.class, () -> InfestazioneRichiesta.da("NEMICO=GOBLIN;NUMERO=0"));
        assertThrows(IllegalArgumentException.class, () -> InfestazioneRichiesta.da("NEMICO=GOBLIN"));
    }

    @Test
    void ogniCittaHaUnaMissioneDiBottega() {
        for (long seme = 1; seme <= 20; seme++) {
            try (PartitaDiTest partita = PartitaDiTest.nuova(seme)) {
                partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
                Set<TipoLocazione> citta = EnumSet.noneOf(TipoLocazione.class);
                for (IlNegozio negozio : negozi()) {
                    assertNotNull(negozio.getCittaAssegnata());
                    assertEquals(CategoriaLocazione.CITTA, negozio.getCittaAssegnata().getCategoriaLocazione());
                    citta.add(negozio.getCittaAssegnata());
                }
                assertEquals(4, citta.size(), "una per città: " + citta);
            }
        }
    }

    @Test
    void siOffreSoloNellaCittaAssegnata() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(71)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            IlNegozio armaiolo = trova(IlNegozioDellArmaiolo.class);
            for (TipoLocazione citta : TipoLocazione.values()) {
                if (citta.getCategoriaLocazione() == CategoriaLocazione.CITTA && citta != armaiolo.getCittaAssegnata()) {
                    Foresta.setPosizioneDelGruppo(coordinateDi(citta));
                    armaiolo.controllaPreLocazione();
                    assertEquals("INCARICO", armaiolo.getPassoCorrente(), citta.name());
                }
            }
        }
    }

    @Test
    void leRovineDisinfestateDiventanoLaBottegaSoloAlRitornoInCitta() {
        for (int indice = 0; indice < 4; indice++) {
            try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(80 + indice)) {
                partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
                IlNegozio negozio = negozi().get(indice);
                TipoLocazione citta = negozio.getCittaAssegnata();
                CoordinateMD cittaCoordinate = coordinateDi(citta);
                Foresta.setPosizioneDelGruppo(cittaCoordinate);

                negozio.controllaPreLocazione();
                assertEquals("ACCETTAZIONE", negozio.getPassoCorrente(), negozio.getNome());
                negozio.segnaIntermezzoPassoMostrato("INCARICO");
                negozio.controllaInLocazione();
                assertTrue(negozio.isAttiva());
                assertEquals(citta, negozio.getCitta());

                CoordinateMD covo = negozio.getCovo();
                assertEquals(TipoLocazione.ROVINE, Foresta.getLocazione(covo));
                assertTrue(Foresta.isLocazioneConosciuta(covo));
                assertEquals(Quadrante.di(cittaCoordinate), Quadrante.di(covo), negozio.getNome());
                assertTrue(negozio.getDescrizione().contains("disinfestare"), negozio.getDescrizione());
                assertFalse(negozio.getDescrizione().contains("%"), negozio.getDescrizione());

                // Le rovine sono dei mostri, finché il gruppo non li sconfigge
                assertTrue(RegistroMissioni.getIncontroMissione(covo).isPresent());
                Foresta.setPosizioneDelGruppo(covo);
                TipoPersonaggio classe = negozio.getNemici().getSconfittiRichiesti().keySet().iterator().next();
                int numero = negozio.getNemici().getSconfittiRichiesti().get(classe);
                for (int i = 0; i < numero; i++) {
                    partita.pubblica(new InternoAvversarioSconfitto(classe));
                }
                negozio.controllaPostLocazione();
                assertEquals("RITORNO", negozio.getPassoCorrente());
                // Ancora rovine: la bottega nasce solo quando si è riscosso
                assertEquals(TipoLocazione.ROVINE, Foresta.getLocazione(covo));

                Foresta.setPosizioneDelGruppo(cittaCoordinate);
                int monete = partita.gruppo().getMonete();
                negozio.controllaPreLocazione();
                negozio.segnaIntermezzoPassoMostrato("RITORNO");
                negozio.controllaInLocazione();
                assertTrue(negozio.isCompleta());
                assertTrue(partita.gruppo().getMonete() > monete);

                TipoLocazione attesa = EnumSet.of(TipoLocazione.ALCHIMISTA, TipoLocazione.ARMAIOLO,
                        TipoLocazione.VENDITORE_DI_PERGAMENE, TipoLocazione.INCANTATORE).stream()
                        .filter(tipo -> negozio.getClass().getSimpleName().toUpperCase().contains(tipo.name().replace("_", "")))
                        .findFirst().orElseThrow(AssertionError::new);
                assertEquals(attesa, Foresta.getLocazione(covo));
                assertTrue(Foresta.isLocazioneConosciuta(covo));
                LocazioneMD md = Foresta.getLocazioneMD(covo);
                assertNotNull(md.getNome());
                String negoziante = RegistroPersonaggi.getNegoziante(md);
                assertTrue(negoziante.endsWith(" da " + citta.getNomeDellaCitta()), negoziante);
                assertTrue(md.getNome().contains(negoziante.substring(0, negoziante.indexOf(" da "))), md.getNome());
                assertTrue(negozio.getRicordoDellaLocazione().contains(md.getNome()), negozio.getRicordoDellaLocazione());
                if (attesa == TipoLocazione.ARMAIOLO) {
                    assertEquals(Costanti.MAGAZZINO_ARTEFATTI_ARMAIOLO,
                            MagazziniDeiNegozi.getScambiatorePerNegozio(covo, TipoNegozio.ARMAIOLO).getInventario().size());
                } else if (attesa == TipoLocazione.VENDITORE_DI_PERGAMENE) {
                    assertEquals(Costanti.MAGAZZINO_PERGAMENE,
                            MagazziniDeiNegozi.getScambiatorePerNegozio(covo, TipoNegozio.VENDITORE_DI_PERGAMENE).getInventario().size());
                }
            }
        }
    }

    @Test
    void laBottegaSiComportaComeIlNegozioDiCitta() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(90)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            CoordinateMD posto = coordinateDi(TipoLocazione.CITTA_NYENA);
            int lato = latoLibero(posto);
            Foresta.costruisciLocazione(new CoordinateMD(posto.getX(), posto.getY() + lato), TipoLocazione.ROVINE);
            CoordinateMD bottega = new CoordinateMD(posto.getX(), posto.getY() + lato);
            Object[][] botteghe = {
                    {TipoLocazione.ARMAIOLO, Comando.NEGOZIO_ARMAIOLO, RichiestaAperturaInventarioCommerciante.class},
                    {TipoLocazione.VENDITORE_DI_PERGAMENE, Comando.NEGOZIO_VENDITORE_DI_PERGAMENE, RichiestaAperturaInventarioCommerciante.class},
                    {TipoLocazione.ALCHIMISTA, Comando.NEGOZIO_ALCHIMISTA, RichiestaAperturaInventarioFornitore.class},
                    {TipoLocazione.INCANTATORE, Comando.NEGOZIO_INCANTATORE, RichiestaAperturaIncantatore.class}};
            for (Object[] voce : botteghe) {
                TipoLocazione tipo = (TipoLocazione) voce[0];
                Foresta.costruisciLocazione(bottega, tipo);
                Foresta.getLocazioneMD(bottega).aggiungiProprieta(RegistroPersonaggi.NEGOZIANTE, "Mastro Prova da Nyena");
                Foresta.setPosizioneDelGruppo(bottega);
                assertTrue(tipo.isBottega());

                List<Object> aperture = new ArrayList<>();
                BusEventi.iscriviti((Class<Object>) voce[2], aperture::add);
                Locazione locazione = CostruttoreDellaForesta.costruisciIstanza(bottega);
                assertEquals(tipo, locazione.getTipoLocazione());
                locazione.crea(partita.gruppo(), GruppoAvversario.getIstanza());
                assertTrue(locazione.isCompleta());
                assertEquals(voce[1], ((Bottega) locazione).getComandoDiIngresso());

                assertEquals(Stato.IN_LOCAZIONE, locazione.impostaAzioni(partita.gruppo(), GruppoAvversario.getIstanza(), (Comando) voce[1]));
                assertEquals(1, aperture.size(), tipo.name());
                assertEquals("Mastro Prova da Nyena",
                        RegistroPersonaggi.getNomeDelPersonaggioDiCitta(tipo == TipoLocazione.ARMAIOLO ? TipoPersonaggio.ARMAIOLO
                                : tipo == TipoLocazione.ALCHIMISTA ? TipoPersonaggio.ALCHIMISTA
                                : tipo == TipoLocazione.INCANTATORE ? TipoPersonaggio.INCANTATORE : TipoPersonaggio.VENDITORE_DI_PERGAMENE));
                assertEquals(Stato.FINE_LOCAZIONE, locazione.impostaAzioni(partita.gruppo(), GruppoAvversario.getIstanza(), Comando.ANNULLA));
            }
        }
    }

    @Test
    void arrivandoNellaBottegaSiEntraSenzaComandiEAnnullaFaRipartire() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(91)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            CoordinateMD citta = Foresta.getPosizioneDelGruppo();
            // La bottega sta sotto la città o, se lì non c'è posto, sopra; si parte da due caselle di distanza e si fa un passo verso la città
            int lato = latoLibero(citta);
            Comando versoLaCitta = lato > 0 ? Comando.NORD : Comando.SUD;
            CoordinateMD bottega = new CoordinateMD(citta.getX(), citta.getY() + lato);
            Foresta.costruisciLocazione(bottega, TipoLocazione.ARMAIOLO);
            Foresta.getLocazioneMD(bottega).setNome("la Fucina di Mastro Prova");
            Foresta.getLocazioneMD(bottega).aggiungiProprieta(RegistroPersonaggi.NEGOZIANTE, "Mastro Prova da Nyena");
            List<Object> aperture = new ArrayList<>();
            BusEventi.iscriviti(RichiestaAperturaInventarioCommerciante.class, aperture::add);

            partita.comando(Comando.ESCI_DA_CITTA);
            Foresta.setPosizioneDelGruppo(new CoordinateMD(citta.getX(), citta.getY() + 2 * lato));
            partita.assertStato(Stato.SCELTA_DIREZIONE);
            partita.comando(versoLaCitta).comando(Comando.NUMERO_1);
            assertEquals(bottega, Foresta.getPosizioneDelGruppo());
            // Il primo ingresso mostra l'intermezzo del negozio: lo si lascia andare avanti
            for (int i = 0; i < 100 && aperture.isEmpty(); i++) {
                partita.scatta();
                if (aperture.isEmpty() && partita.comandiDisponibili().contains(Comando.PERGAMENA)) {
                    partita.comando(Comando.PERGAMENA);
                }
            }
            assertEquals(1, aperture.size());
            partita.comando(Comando.ANNULLA);
            partita.assertStato(Stato.SCELTA_DIREZIONE);
        }
    }
}
