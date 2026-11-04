package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.interni.InternoAvversarioSconfitto;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.intermezzi.BattutaProgrammata;
import com.threeamigos.foresta.intermezzi.PaginaIntermezzo;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.missioni.IntermezzoDiPasso;
import com.threeamigos.foresta.missioni.Missione;
import com.threeamigos.foresta.missioni.RegistroMissioni;
import com.threeamigos.foresta.missioni.storie.IlViaggioDiOlaf;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.statistiche.LineaTemporale;
import com.threeamigos.foresta.tipi.CategoriaLocazione;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.MomentoIntermezzo;
import com.threeamigos.foresta.tipi.CostantiPersonaggi;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoMotivoNelGruppo;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Il viaggio di Olaf: dopo sei o sette giorni, entrando in un bosco, il gruppo trova un vichingo smarrito che si unisce a lui, a tempo,
 * per uccidere una viverna o una chimera drago e poi farsi portare in una città; lì si separa e lascia un leggendario (vedi IlViaggioDiOlaf).
 */
class ScenarioIlViaggioDiOlafTest {

    @Test
    void dopoSeiOSetteGiorniInUnBoscoOlafSiUnisceAlGruppoPoiNeVaInCittaELasciaUnLeggendario() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(91)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
                    () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            partita.rimandaLeMissioniCheSiOffrono();
            IlViaggioDiOlaf olaf = trova(IlViaggioDiOlaf.class);
            assertFalse(olaf.isAttiva());

            // All'inizio si fissa il giorno
            olaf.controllaPreLocazione();
            int giorno = olaf.getGiornoDellIncontro();
            assertTrue(giorno >= IlViaggioDiOlaf.GIORNO_MINIMO && giorno <= IlViaggioDiOlaf.GIORNO_MASSIMO, "giorno " + giorno);
            assertEquals("INCONTRO", olaf.getPassoCorrente());

            // Né in un bosco troppo presto, né in una radura, né in una città
            CoordinateMD bosco = cellaSenzaLocazioniUniche(0);
            Foresta.costruisciLocazione(bosco, TipoLocazione.BOSCO);
            LineaTemporale.aggiungiOre(24 * (giorno - 2));
            Foresta.setPosizioneDelGruppo(bosco);
            olaf.controllaPreLocazione();
            assertEquals("INCONTRO", olaf.getPassoCorrente(), "il giorno prima non c'è ancora");
            LineaTemporale.aggiungiOre(24);
            CoordinateMD radura = cellaSenzaLocazioniUniche(1);
            Foresta.costruisciLocazione(radura, TipoLocazione.RADURA);
            Foresta.setPosizioneDelGruppo(radura);
            olaf.controllaPreLocazione();
            assertEquals("INCONTRO", olaf.getPassoCorrente(), "non in una radura");

            // Nel bosco, dal giorno fissato, Olaf si fa incontrare
            Foresta.setPosizioneDelGruppo(bosco);
            olaf.controllaPreLocazione();
            assertEquals("INCONTRO", olaf.getPassoConIntermezzoInAttesa(MomentoIntermezzo.INIZIO_LOCAZIONE));
            List<PaginaIntermezzo> scena = new IntermezzoDiPasso(olaf, "INCONTRO").getPagine();
            assertEquals(1, scena.size());
            List<String> chiParla = chiParla(scena.get(0));
            // Senza il bardo, e con il capo solo: capo, Olaf, capo, Olaf, il capo (Sglord), Olaf, capo
            assertEquals(7, chiParla.size(), chiParla.toString());
            assertTrue(scena.get(0).getElementi().stream().anyMatch(e -> e.getId().equals("incontrato")));
            olaf.segnaIntermezzoPassoMostrato("INCONTRO");
            olaf.controllaInLocazione();
            assertTrue(olaf.isAttiva());
            assertEquals("CACCIA", olaf.getPassoCorrente());
            assertNotNull(olaf.getLeggendario());
            assertTrue(GruppoGiocatore.getIstanza().getOspiti().isEmpty(), "Olaf combatte, non è un ospite");
            assertEquals(2, GruppoGiocatore.getIstanza().getNumeroPersonaggi());
            Personaggio vichingo = GruppoGiocatore.getIstanza().getPersonaggio(1);
            assertEquals(TipoPersonaggio.VICHINGO, vichingo.getClasse());
            assertEquals("Olaf", vichingo.getNome());
            assertEquals(TipoMotivoNelGruppo.MISSIONE, vichingo.getMotivoNelGruppo());
            assertTrue(vichingo.isATempo());
            assertTrue(vichingo.isVivo());
            assertTrue(olaf.getDescrizione().contains("Olaf"), olaf.getDescrizione());

            // Una città prima di aver ucciso il mostro non basta, e nemmeno un altro mostro
            Foresta.setPosizioneDelGruppo(Foresta.getCoordinateLocazioneUnica(TipoLocazione.CITTA_FLEENA));
            partita.pubblica(new InternoAvversarioSconfitto(TipoPersonaggio.GOBLIN));
            olaf.controllaPostLocazione();
            olaf.controllaPreLocazione();
            assertEquals("CACCIA", olaf.getPassoCorrente());

            // Una chimera drago (o una viverna) basta, dovunque
            partita.pubblica(new InternoAvversarioSconfitto(TipoPersonaggio.CHIMERA_DRAGO));
            olaf.controllaPostLocazione();
            assertEquals("CITTA", olaf.getPassoCorrente());
            assertTrue(olaf.getDescrizione().contains("Olaf"), olaf.getDescrizione());
            assertEquals(2, GruppoGiocatore.getIstanza().getNumeroPersonaggi(), "Olaf è ancora con il gruppo");
            // I turni che passano non fanno scadere il suo tempo: lo fa la missione
            for (int i = 0; i < 20; i++) {
                assertTrue(vichingo.decrementaTempo() > 0);
            }

            // In un bosco no, in una città sì: Olaf si separa e lascia il leggendario
            Foresta.setPosizioneDelGruppo(bosco);
            olaf.controllaPreLocazione();
            assertEquals("CITTA", olaf.getPassoCorrente());
            Foresta.setPosizioneDelGruppo(Foresta.getCoordinateLocazioneUnica(TipoLocazione.CITTA_FLEENA));
            olaf.controllaPreLocazione();
            assertEquals("CITTA", olaf.getPassoConIntermezzoInAttesa(MomentoIntermezzo.INIZIO_LOCAZIONE));
            assertEquals(1, GruppoGiocatore.getIstanza().getNumeroPersonaggi(), "Olaf si è separato dal gruppo");
            assertEquals(1, new IntermezzoDiPasso(olaf, "CITTA").getPagine().size());
            assertEquals(1, chiParla(new IntermezzoDiPasso(olaf, "CITTA").getPagine().get(0)).size());
            int artefatti = GruppoGiocatore.getIstanza().getInventario().size();
            olaf.segnaIntermezzoPassoMostrato("CITTA");
            olaf.controllaInLocazione();
            assertTrue(olaf.isCompleta());
            assertEquals(artefatti + 1, GruppoGiocatore.getIstanza().getInventario().size(), "il leggendario");
        }
    }

    @Test
    void ConIlBardoNelGruppoOlafLoRiconoscePoiParlaUnCompagnoDelCapo() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(92)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
                    () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            partita.rimandaLeMissioniCheSiOffrono();
            IlViaggioDiOlaf olaf = trova(IlViaggioDiOlaf.class);
            olaf.controllaPreLocazione();

            // Il capo solo e senza bardo: sette battute, tutte del capo o di Olaf
            List<String> senza = chiParla(new IntermezzoDiPasso(olaf, "INCONTRO").getPagine().get(0));
            assertEquals(7, senza.size(), senza.toString());
            assertEquals("personaggio0", senza.get(4), "la battuta su Sglord è del capo, che è solo");

            // Con un bardo da riportare a casa Olaf lo riconosce
            GruppoGiocatore.getIstanza().aggiungiOspite(FabbricaPersonaggi.crea(TipoPersonaggio.BARDO_DELLE_LOCANDE, 1));
            List<String> conBardo = chiParla(new IntermezzoDiPasso(olaf, "INCONTRO").getPagine().get(0));
            assertEquals(8, conBardo.size(), conBardo.toString());
            assertEquals("incontrato", conBardo.get(4), "la battuta in più è di Olaf");

            // Con un secondo personaggio, la battuta su Sglord è sua
            Personaggio compagno = FabbricaPersonaggi.crea(TipoPersonaggio.GUERRIERO, 1);
            GruppoGiocatore.getIstanza().aggiungiPersonaggio(compagno);
            List<String> conCompagno = chiParla(new IntermezzoDiPasso(olaf, "INCONTRO").getPagine().get(0));
            assertEquals(8, conCompagno.size(), conCompagno.toString());
            String chiDiceSglord = conCompagno.get(5);
            assertNotEquals("personaggio0", chiDiceSglord);
            assertNotEquals("incontrato", chiDiceSglord);
        }
    }

    @Test
    void OlafEntraAncheAGruppoPienoEFaPostoAlPersonaggioATempoPiuVicinoAllaScadenza() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(93)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
                    () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            partita.rimandaLeMissioniCheSiOffrono();
            GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
            // Cinque personaggi permanenti e tre a tempo: otto, il massimo
            while (gruppo.getNumeroPersonaggiPermanenti() < 5) {
                gruppo.aggiungiPersonaggio(FabbricaPersonaggi.crea(TipoPersonaggio.GUERRIERO, 1));
            }
            Personaggio[] aTempo = new Personaggio[3];
            int[] tempi = {5, 2, 9};
            for (int i = 0; i < 3; i++) {
                aTempo[i] = FabbricaPersonaggi.crea(TipoPersonaggio.GUERRIERO, 1);
                aTempo[i].setMotivoNelGruppo(i == 1 ? TipoMotivoNelGruppo.MIETITURA : TipoMotivoNelGruppo.AIUTO_GRATUITO);
                aTempo[i].setTempo(tempi[i]);
                gruppo.aggiungiPersonaggio(aTempo[i]);
            }
            assertEquals(CostantiPersonaggi.MAX_PERSONAGGI_GRUPPO_TOTALE, gruppo.getNumeroPersonaggi());

            IlViaggioDiOlaf olaf = trova(IlViaggioDiOlaf.class);
            olaf.controllaPreLocazione();
            CoordinateMD bosco = cellaSenzaLocazioniUniche(0);
            Foresta.costruisciLocazione(bosco, TipoLocazione.BOSCO);
            LineaTemporale.aggiungiOre(24 * olaf.getGiornoDellIncontro());
            Foresta.setPosizioneDelGruppo(bosco);
            olaf.controllaPreLocazione();
            assertEquals("INCONTRO", olaf.getPassoConIntermezzoInAttesa(MomentoIntermezzo.INIZIO_LOCAZIONE), "entra anche a gruppo pieno");
            olaf.segnaIntermezzoPassoMostrato("INCONTRO");
            olaf.controllaInLocazione();

            assertTrue(olaf.isAttiva());
            assertEquals(CostantiPersonaggi.MAX_PERSONAGGI_GRUPPO_TOTALE, gruppo.getNumeroPersonaggi(), "non si superano gli otto");
            assertTrue(gruppo.getPersonaggi().contains(aTempo[0]));
            assertFalse(gruppo.getPersonaggi().contains(aTempo[1]), "se ne va chi ha il tempo più vicino alla scadenza, anche uno scheletro");
            assertTrue(gruppo.getPersonaggi().contains(aTempo[2]));
            assertEquals(5, gruppo.getNumeroPersonaggiPermanenti());
        }
    }

    @Test
    void OlafNonAccumulaEsperienzaNonSaleDiLivelloEPuoEssereFeritoOMorire() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(94)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
                    () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            partita.rimandaLeMissioniCheSiOffrono();
            IlViaggioDiOlaf olaf = trova(IlViaggioDiOlaf.class);
            olaf.controllaPreLocazione();
            CoordinateMD bosco = cellaSenzaLocazioniUniche(0);
            Foresta.costruisciLocazione(bosco, TipoLocazione.BOSCO);
            LineaTemporale.aggiungiOre(24 * olaf.getGiornoDellIncontro());
            Foresta.setPosizioneDelGruppo(bosco);
            olaf.controllaPreLocazione();
            olaf.segnaIntermezzoPassoMostrato("INCONTRO");
            olaf.controllaInLocazione();
            GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
            Personaggio vichingo = gruppo.getPersonaggio(1);
            Personaggio capo = gruppo.getCapo();
            int livello = vichingo.getLivello();
            double esperienza = vichingo.getModelloDati().getOptional(TipoAttributo.PUNTI_ESPERIENZA).orElse(0.0d);
            int livelloDelCapo = capo.getLivello();

            gruppo.addPuntiEsperienza(100000);
            assertEquals(livello, vichingo.getLivello(), "Olaf non sale di livello");
            assertEquals(esperienza, vichingo.getModelloDati().getOptional(TipoAttributo.PUNTI_ESPERIENZA).orElse(0.0d));
            assertTrue(capo.getLivello() > livelloDelCapo, "gli altri sì");

            // Combatte come gli altri: è fra i personaggi vivi, e può essere ferito e morire
            assertTrue(gruppo.getPersonaggiVivi().contains(vichingo));
            double salute = vichingo.getSalute();
            vichingo.subSalute(1, null, null, null);
            assertTrue(vichingo.getSalute() < salute);
            vichingo.subSalute(1_000_000, null, null, null);
            assertFalse(vichingo.isVivo());
        }
    }

    private static List<String> chiParla(PaginaIntermezzo pagina) {
        return pagina.getBattuteProgrammate().stream().map(BattutaProgrammata::getBattuta).map(b -> b.getIdElemento()).collect(Collectors.toList());
    }

    private static CoordinateMD cellaSenzaLocazioniUniche(int saltate) {
        int daSaltare = saltate;
        for (int y = 0; y < Foresta.getDimensioneY(); y++) {
            for (int x = 0; x < Foresta.getDimensioneX(); x++) {
                CoordinateMD coordinate = new CoordinateMD(x, y);
                TipoLocazione tipo = Foresta.getLocazione(coordinate);
                if (tipo.getCategoriaLocazione() != CategoriaLocazione.CITTA && tipo.getCategoriaLocazione() != CategoriaLocazione.CASTELLO
                        && daSaltare-- == 0) {
                    return coordinate;
                }
            }
        }
        throw new AssertionError("nessuna casella libera");
    }

    @SuppressWarnings("unchecked")
    private static <T extends Missione> T trova(Class<T> tipo) {
        return (T) RegistroMissioni.getTutteLeMissioni().stream().filter(tipo::isInstance).findFirst()
                .orElseThrow(() -> new AssertionError("missione " + tipo.getSimpleName() + " non trovata"));
    }
}
