package com.threeamigos.foresta.economia;

import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.artefatti.GeneratoreArtefatti;
import com.threeamigos.foresta.artefatti.GeneratoreArtefattiTabelle;
import com.threeamigos.foresta.artefatti.ScambiatoreArtefatti;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoFineLocazione;
import com.threeamigos.foresta.eventi.interni.InternoPreparazioneLocazione;
import com.threeamigos.foresta.eventi.notifiche.NotificaApprovazioneVenditaArtefatto;
import com.threeamigos.foresta.eventi.notifiche.NotificaAumentoLivelloMondo;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.interfacce.VistaOffertaConsumabile;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.personaggi.Mago;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.CostantiEconomia;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoConsumabile;
import com.threeamigos.foresta.tipi.TipoIncantesimo;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoNegozio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class MagazziniEListinoTest {

    private final CoordinateMD citta = new CoordinateMD(2, 2);
    private final CoordinateMD bottega = new CoordinateMD(7, 6);
    private GruppoGiocatore gruppo;

    @BeforeEach
    void prepara() {
        MondoDiProva.nuovo(20, 20);
        gruppo = GruppoGiocatore.getIstanza();
    }

    @AfterEach
    void ripulisce() {
        MondoDiProva.finito();
    }

    private void aggiungi(Personaggio personaggio) {
        personaggio.getModelloDati().setPng(false);
        gruppo.aggiungiPersonaggio(personaggio);
    }

    private static int quanti(ScambiatoreArtefatti scambiatore) {
        return scambiatore.getInventario().size();
    }

    private static ScambiatoreArtefatti negozio(CoordinateMD coordinate, TipoNegozio tipo) {
        return MagazziniDeiNegozi.getScambiatorePerNegozio(coordinate, tipo);
    }

    @Test
    void lePrimeDueBottegheDiUnaCittaSiRiempiono() {
        MagazziniDeiNegozi.riempiMagazzini(citta, new GeneratoreArtefattiTabelle(new Random(1)));
        assertEquals(CostantiEconomia.MAGAZZINO_ARTEFATTI_ARMAIOLO, quanti(negozio(citta, TipoNegozio.ARMAIOLO)));
        assertEquals(CostantiEconomia.MAGAZZINO_PERGAMENE, quanti(negozio(citta, TipoNegozio.VENDITORE_DI_PERGAMENE)));
    }

    @Test
    void unaBottegaNataNellaForestaSiRiempieAlLivelloDelMondo() {
        MondoDiProva.statistiche.setLivello(6);
        MagazziniDeiNegozi.riempiMagazzino(bottega, TipoNegozio.ARMAIOLO);
        assertEquals(CostantiEconomia.MAGAZZINO_ARTEFATTI_ARMAIOLO, quanti(negozio(bottega, TipoNegozio.ARMAIOLO)));
        MagazziniDeiNegozi.riempiMagazzino(bottega, TipoNegozio.VENDITORE_DI_PERGAMENE);
        assertEquals(CostantiEconomia.MAGAZZINO_PERGAMENE, quanti(negozio(bottega, TipoNegozio.VENDITORE_DI_PERGAMENE)));
    }

    @Test
    void ilNegozioTrattaSoloLaSuaMerce() {
        ScambiatoreArtefatti armaiolo = negozio(citta, TipoNegozio.ARMAIOLO);
        ScambiatoreArtefatti venditore = negozio(citta, TipoNegozio.VENDITORE_DI_PERGAMENE);
        Artefatto spada = MondoDiProva.artefatto(TipoArtefatto.SPADA, 2);
        Artefatto pergamena = MondoDiProva.artefatto(TipoArtefatto.PERGAMENA, 2);
        assertTrue(armaiolo.tratta(spada));
        assertFalse(armaiolo.tratta(pergamena));
        assertTrue(venditore.tratta(pergamena));
        assertFalse(venditore.tratta(spada));
        armaiolo.addArtefatto(spada);
        assertEquals(1, quanti(armaiolo));
        armaiolo.removeArtefatto(spada);
        assertEquals(0, quanti(armaiolo));
    }

    @Test
    void conIlLivelloDelMondoPiuAltoSiScartaIlVecchioESiRifornisce() {
        MondoDiProva.statistiche.setLivello(1);
        Foresta.impostaLocazione(citta, TipoLocazione.values()[0]);
        TipoLocazione unaCitta = null;
        for (TipoLocazione tipo : TipoLocazione.values()) {
            if (tipo.getCategoriaLocazione() == com.threeamigos.foresta.tipi.CategoriaLocazione.CITTA) {
                unaCitta = tipo;
                break;
            }
        }
        assertNotNull(unaCitta);
        Foresta.impostaLocazione(citta, unaCitta);
        Foresta.impostaLocazione(bottega, TipoLocazione.ARMAIOLO);
        CoordinateMD secondaBottega = new CoordinateMD(8, 8);
        Foresta.impostaLocazione(secondaBottega, TipoLocazione.VENDITORE_DI_PERGAMENE);
        CoordinateMD distrutta = new CoordinateMD(9, 9);
        MagazziniDeiNegozi.riempiMagazzini(citta, GeneratoreArtefatti.istanza());
        MagazziniDeiNegozi.riempiMagazzino(bottega, TipoNegozio.ARMAIOLO);
        MagazziniDeiNegozi.riempiMagazzino(secondaBottega, TipoNegozio.VENDITORE_DI_PERGAMENE);
        // Un negozio la cui casella non è più quel negozio (città distrutta) non si rifornisce
        MagazziniDeiNegozi.riempiMagazzino(distrutta, TipoNegozio.ARMAIOLO);
        MagazziniDeiNegozi.riempiMagazzino(distrutta, TipoNegozio.VENDITORE_DI_PERGAMENE);
        int armaioloDistrutto = quanti(negozio(distrutta, TipoNegozio.ARMAIOLO));
        int pergameneDistrutte = quanti(negozio(distrutta, TipoNegozio.VENDITORE_DI_PERGAMENE));
        MondoDiProva.statistiche.setLivello(40);
        MagazziniDeiNegozi.aggiornaMagazzini(40, GeneratoreArtefatti.istanza());
        assertTrue(negozio(bottega, TipoNegozio.ARMAIOLO).getInventario().size() <= CostantiEconomia.MAGAZZINO_ARTEFATTI_ARMAIOLO * 2);
        MagazziniDeiNegozi.registrati();
        BusEventi.pubblica(new NotificaAumentoLivelloMondo(41));
        assertEquals(armaioloDistrutto, quanti(negozio(distrutta, TipoNegozio.ARMAIOLO)));
        assertEquals(pergameneDistrutte, quanti(negozio(distrutta, TipoNegozio.VENDITORE_DI_PERGAMENE)));
        assertTrue(quanti(negozio(citta, TipoNegozio.ARMAIOLO)) > 0);
        assertEquals(CostantiEconomia.MAGAZZINO_PERGAMENE, quanti(negozio(citta, TipoNegozio.VENDITORE_DI_PERGAMENE)));
        assertEquals(CostantiEconomia.MAGAZZINO_PERGAMENE, quanti(negozio(secondaBottega, TipoNegozio.VENDITORE_DI_PERGAMENE)));
        // gli artefatti troppo deboli dell'armaiolo sono stati scartati
        Collection<Artefatto> armi = negozio(bottega, TipoNegozio.ARMAIOLO).getInventario();
        assertTrue(armi.stream().allMatch(a -> a.getLivello() >= 40 - CostantiEconomia.MAGAZZINO_SCARTO_SOTTO_LIVELLO));
    }

    @Test
    void quelloCheIlGruppoVendeSiSmaltisceAllUscita() {
        MagazziniDeiNegozi.registrati();
        ScambiatoreArtefatti armaiolo = negozio(MondoDiProva.QUI, TipoNegozio.ARMAIOLO);
        ScambiatoreArtefatti venditore = negozio(MondoDiProva.QUI, TipoNegozio.VENDITORE_DI_PERGAMENE);
        BusEventi.pubblica(new InternoPreparazioneLocazione());
        Artefatto spada = MondoDiProva.artefatto(TipoArtefatto.SPADA, 2);
        Artefatto pergamena = MondoDiProva.artefatto(TipoArtefatto.PERGAMENA, 2);
        armaiolo.addArtefatto(spada);
        venditore.addArtefatto(pergamena);
        BusEventi.pubblica(new NotificaApprovazioneVenditaArtefatto(null, spada, "a"));
        BusEventi.pubblica(new NotificaApprovazioneVenditaArtefatto(null, pergamena, "b"));
        assertEquals(1, quanti(armaiolo));
        assertEquals(1, quanti(venditore));
        BusEventi.pubblica(new InternoFineLocazione());
        assertEquals(0, quanti(armaiolo));
        assertEquals(0, quanti(venditore));
        // il registro dei venduti è vuoto: una seconda uscita non toglie altro
        armaiolo.addArtefatto(spada);
        BusEventi.pubblica(new InternoFineLocazione());
        assertEquals(1, quanti(armaiolo));
    }

    @Test
    void daUnMagazzinoCheNonEsisteNonSiTogliNiente() {
        Artefatto spada = MondoDiProva.artefatto(TipoArtefatto.SPADA, 2);
        assertFalse(MondoDiProva.magazzini.rimuoviDaMagazzino(citta, TipoNegozio.ARMAIOLO, spada.getModelloDati()));
        negozio(citta, TipoNegozio.ARMAIOLO).addArtefatto(spada);
        assertTrue(MondoDiProva.magazzini.rimuoviDaMagazzino(citta, TipoNegozio.ARMAIOLO, spada.getModelloDati()));
        assertFalse(MondoDiProva.magazzini.rimuoviDaMagazzino(citta, TipoNegozio.ARMAIOLO, spada.getModelloDati()));
    }

    @Test
    void senzaIDatiIMagazziniNonVanno() {
        MagazziniDeiNegozi.setFornitoreDiDati(null);
        assertThrows(IllegalStateException.class, () -> negozio(citta, TipoNegozio.ARMAIOLO).getInventario());
    }

    // ---- Il listino dell'alchimista

    @Test
    void ilListinoHaGliIncantesimiLePozioniELeMappe() {
        aggiungi(new Guerriero("Arsenio", 3));
        List<? extends VistaOffertaConsumabile> elenco = OfferteAlchimista.elenco(gruppo);
        assertEquals(TipoIncantesimo.values().length + 5 + 1 + 2, elenco.size());
        for (VistaOffertaConsumabile offerta : elenco) {
            assertNotNull(offerta.getNome());
            assertNotNull(offerta.getDescrizione());
            assertTrue(offerta.getCosto() > 0);
            assertNotEquals(TipoConsumabile.AUMENTO_MAGIA_GRUPPO, offerta.getTipo());
            if (offerta.getTipo() == TipoConsumabile.INCANTESIMO) {
                assertNotNull(offerta.getClasseIncantesimo());
            } else {
                assertNull(offerta.getClasseIncantesimo());
            }
            if (offerta.getTipo() == TipoConsumabile.AUMENTO_MAGIA_SINGOLO) {
                assertNotNull(offerta.getPersonaggio());
            } else {
                assertNull(offerta.getPersonaggio());
            }
        }
    }

    @Test
    void conPiuGiocantiArrivaAncheLAumentoPerTuttoIlGruppoScontato() {
        aggiungi(new Guerriero("Arsenio", 3));
        aggiungi(new Mago("Merlino", 3));
        Personaggio png = new Guerriero("Comparsa", 3);
        gruppo.aggiungiPersonaggio(png);
        png.getModelloDati().setPng(true);
        List<? extends VistaOffertaConsumabile> elenco = OfferteAlchimista.elenco(gruppo);
        long singoli = elenco.stream().filter(o -> o.getTipo() == TipoConsumabile.AUMENTO_MAGIA_SINGOLO).count();
        assertEquals(2, singoli);
        VistaOffertaConsumabile tutti = elenco.stream().filter(o -> o.getTipo() == TipoConsumabile.AUMENTO_MAGIA_GRUPPO).findFirst().get();
        assertTrue(tutti.getCosto() < 2 * CostantiEconomia.COSTO_AUMENTO_MAGIA_GIOCATORE_SINGOLO);
        assertTrue(tutti.getCosto() > CostantiEconomia.COSTO_AUMENTO_MAGIA_GIOCATORE_SINGOLO);
    }

    @Test
    void siTrovaLOffertaCheCorrispondeAUnaRichiesta() {
        aggiungi(new Guerriero("Arsenio", 3));
        aggiungi(new Mago("Merlino", 3));
        Personaggio mago = gruppo.getPersonaggi().get(1);
        Optional<OffertaConsumabile> pozione = OfferteAlchimista.trova(gruppo, TipoConsumabile.POZIONE_SALUTE, null, null);
        assertTrue(pozione.isPresent());
        assertEquals(TipoConsumabile.POZIONE_SALUTE, pozione.get().getTipo());
        assertEquals(TipoIncantesimo.FUOCO, OfferteAlchimista.trova(gruppo, TipoConsumabile.INCANTESIMO, TipoIncantesimo.FUOCO, null).get().getClasseIncantesimo());
        Optional<OffertaConsumabile> aumento = OfferteAlchimista.trova(gruppo, TipoConsumabile.AUMENTO_MAGIA_SINGOLO, null, mago.getUuid());
        assertSame(mago, aumento.get().getPersonaggio());
        assertFalse(OfferteAlchimista.trova(gruppo, TipoConsumabile.AUMENTO_MAGIA_SINGOLO, null, "nessuno").isPresent());
        assertFalse(OfferteAlchimista.trova(gruppo, TipoConsumabile.MONETE, null, null).isPresent());
    }
}
