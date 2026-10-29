package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.gruppi.GruppoAvversario;
import com.threeamigos.foresta.mappa.Foresta;

import com.threeamigos.foresta.tipi.CostantiCanti;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.eventi.interni.InternoAggiornamentoComandiDisponibili;
import com.threeamigos.foresta.locazioni.Locazione;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.modellodati.ModificatoreAttributoMD;
import com.threeamigos.foresta.tipi.TipoModificatore;
import com.threeamigos.foresta.personaggi.Goblin;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.personaggi.PersonaggioBase;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoLocazione;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I canti del Bardo nello scontro (vedi personaggi_giocabili.md): quando compare il comando INCANTESIMO, quali canti offre il
 * menu e che cosa succede quando se ne sceglie uno.
 */
class ScenarioCantiDelBardoTest {

    private Locazione locazione;
    private GruppoAvversario avversari;
    private PersonaggioBase bardo;

    private void inUnBoscoConUnBardo(PartitaDiTest partita, int goblin) {
        partita.iniziaCon("Orfeo", Comando.MASCHIO, Comando.BARDO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
        CoordinateMD bosco = unaCasellaDi(TipoLocazione.BOSCO);
        Foresta.setPosizioneDelGruppo(bosco);
        locazione = CostruttoreDellaForesta.costruisciIstanza(bosco);
        LocazioneCorrente.set(locazione);
        avversari = GruppoAvversario.getIstanza();
        avversari.reimposta();
        for (int i = 0; i < goblin; i++) {
            Goblin g = new Goblin(1);
            // Con poco coraggio e molta soggezione tutti cedono
            g.addModificatore(new ModificatoreAttributoMD(TipoAttributo.CORAGGIO, TipoModificatore.QUANTITA_ASSOLUTA, 0));
            avversari.aggiungiPersonaggio(g);
        }
        bardo = (PersonaggioBase) partita.gruppo().getCapo();
        bardo.addMagia(1000);
        bardo.addSalute(10000);
        bardo.addModificatore(new ModificatoreAttributoMD(TipoAttributo.SOGGEZIONE, TipoModificatore.QUANTITA_ASSOLUTA, 50));
        locazione.impostaAzioni(partita.gruppo(), avversari, null);
    }

    private static Collection<Comando> comandi(PartitaDiTest partita) {
        return partita.eventi().ultimo(InternoAggiornamentoComandiDisponibili.class).getPossibilita();
    }

    @Test
    void ilComandoIncantesimoCompareAlBardoConGliAvversariVivi() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(301)) {
            partita.eventi().ascolta(InternoAggiornamentoComandiDisponibili.class);
            inUnBoscoConUnBardo(partita, 2);
            assertTrue(comandi(partita).contains(Comando.INCANTESIMO), String.valueOf(comandi(partita)));
        }
    }

    @Test
    void senzaAvversariVivoNonSiCanta() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(302)) {
            partita.eventi().ascolta(InternoAggiornamentoComandiDisponibili.class);
            inUnBoscoConUnBardo(partita, 0);
            assertFalse(comandi(partita).contains(Comando.INCANTESIMO), String.valueOf(comandi(partita)));
        }
    }

    @Test
    void senzaMagiaNonSiCantaEIlComandoSparisce() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(303)) {
            partita.eventi().ascolta(InternoAggiornamentoComandiDisponibili.class);
            inUnBoscoConUnBardo(partita, 2);
            bardo.subMagia(bardo.getMagia());
            locazione.impostaAzioni(partita.gruppo(), avversari, null);
            assertFalse(comandi(partita).contains(Comando.INCANTESIMO), String.valueOf(comandi(partita)));
        }
    }

    @Test
    void sceltoIlCantoIpnoticoGliAvversariCedonoEIlTurnoSiConsuma() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(304)) {
            partita.eventi().ascolta(InternoAggiornamentoComandiDisponibili.class);
            inUnBoscoConUnBardo(partita, 2);
            int magia = bardo.getMagia();

            assertEquals(Stato.SCELTA_AUTOMATICA_PERSONAGGIO, locazione.impostaAzioni(partita.gruppo(), avversari, Comando.INCANTESIMO));
            assertEquals(Stato.SCELTA_INCANTESIMO_DA_LANCIARE, locazione.impostaAzioni(partita.gruppo(), avversari, Comando.PERSONAGGIO_1));
            locazione.impostaAzioni(partita.gruppo(), avversari, Comando.CANTO_IPNOTICO);

            long colpiti = avversari.getPersonaggiVivi().stream()
                    .filter(a -> a.hasEffettoDiStato(TipoEffettoDiStato.SPAVENTATO) || a.hasEffettoDiStato(TipoEffettoDiStato.CONFUSO)).count();
            assertTrue(colpiti >= 1, "almeno un avversario ha ceduto");
            assertEquals(magia - CostantiCanti.CANTO_IPNOTICO_COSTO_LANCIO, bardo.getMagia());
        }
    }

    @Test
    void ilCantoCurativoRichiedeUnFerito() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(305)) {
            partita.eventi().ascolta(InternoAggiornamentoComandiDisponibili.class);
            inUnBoscoConUnBardo(partita, 1);
            // Senza feriti il canto curativo non si canta: se si sceglie lo stesso non succede niente e non si paga
            int magia = bardo.getMagia();
            locazione.impostaAzioni(partita.gruppo(), avversari, Comando.INCANTESIMO);
            locazione.impostaAzioni(partita.gruppo(), avversari, Comando.PERSONAGGIO_1);
            locazione.impostaAzioni(partita.gruppo(), avversari, Comando.CANTO_CURATIVO);
            assertEquals(magia, bardo.getMagia());

            // Con un ferito sì
            bardo.subSalute(bardo.getSaluteMassima() / 2, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
            int salute = bardo.getSalute();
            locazione.impostaAzioni(partita.gruppo(), avversari, Comando.INCANTESIMO);
            locazione.impostaAzioni(partita.gruppo(), avversari, Comando.PERSONAGGIO_1);
            locazione.impostaAzioni(partita.gruppo(), avversari, Comando.CANTO_CURATIVO);
            assertEquals(magia - CostantiCanti.CANTO_CURATIVO_COSTO_LANCIO, bardo.getMagia());
            assertTrue(bardo.getSalute() > salute - 1, "è stato curato (al netto dei colpi della risposta)");
        }
    }

    @Test
    void ilCantoDiValoreRendeIspiratoIlGruppoEIlTurnoSiConsuma() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(306)) {
            partita.eventi().ascolta(InternoAggiornamentoComandiDisponibili.class);
            inUnBoscoConUnBardo(partita, 1);
            int magia = bardo.getMagia();
            locazione.impostaAzioni(partita.gruppo(), avversari, Comando.INCANTESIMO);
            locazione.impostaAzioni(partita.gruppo(), avversari, Comando.PERSONAGGIO_1);
            locazione.impostaAzioni(partita.gruppo(), avversari, Comando.CANTO_DI_VALORE);
            assertTrue(bardo.hasEffettoDiStato(TipoEffettoDiStato.ISPIRATO));
            assertEquals(magia - CostantiCanti.CANTO_DI_VALORE_COSTO_LANCIO, bardo.getMagia());
        }
    }

    /**
     * Il menu dell'Automa, con la partita vera: il Bardo che entra in un bosco con degli avversari trova nel menu degli
     * incantesimi i canti (quello curativo no: nessuno è ferito).
     */
    @Test
    void ilMenuDegliIncantesimiOffreICantiAlBardo() {
        boolean provato = false;
        for (long seme = 310; seme < 340 && !provato; seme++) {
            try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(seme)) {
                partita.iniziaCon("Orfeo", Comando.MASCHIO, Comando.BARDO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
                CoordinateMD bosco = unaCasellaDi(TipoLocazione.BOSCO);
                partita.gruppo().getCapo().addMagia(1000);
                partita.gruppo().getCapo().addSalute(10000);
                partita.comando(Comando.ESCI_DA_CITTA);
                Foresta.setPosizioneDelGruppo(new CoordinateMD(bosco.getX(), bosco.getY() + 1));
                partita.comando(Comando.NORD).comando(Comando.NUMERO_1);
                if (partita.stato() != Stato.IN_LOCAZIONE || GruppoAvversario.getIstanza().getNumeroPersonaggiVivi() == 0) {
                    continue;
                }
                provato = true;
                assertTrue(partita.comandiDisponibili().contains(Comando.INCANTESIMO), String.valueOf(partita.comandiDisponibili()));
                partita.comando(Comando.INCANTESIMO);
                partita.assertStato(Stato.INCANTESIMO_SCELTO);
                Collection<Comando> menu = partita.comandiDisponibili();
                assertTrue(menu.contains(Comando.CANTO_IPNOTICO), String.valueOf(menu));
                assertTrue(menu.contains(Comando.CANTO_DI_VALORE), String.valueOf(menu));
                assertFalse(menu.contains(Comando.CANTO_CURATIVO), String.valueOf(menu));
                assertTrue(menu.contains(Comando.ANNULLA), String.valueOf(menu));
            }
        }
        assertTrue(provato, "nessun seme ha dato un bosco con degli avversari");
    }

    private static CoordinateMD unaCasellaDi(TipoLocazione tipo) {
        for (int x = 0; x < Foresta.getDimensioneX(); x++) {
            for (int y = 0; y < Foresta.getDimensioneY(); y++) {
                if (Foresta.getLocazione(x, y) == tipo) {
                    return new CoordinateMD(x, y);
                }
            }
        }
        throw new AssertionError("nessuna casella di " + tipo);
    }
}
