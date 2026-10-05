package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.locazioni.Rovine;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tools.Misc;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le rovine hanno un nome: dalla grammatica rovine.txt quelle che ci sono dall'inizio, da quello che c'era prima
 * quelle di un castello sconfitto o di una città distrutta.
 */
class ScenarioNomiDelleRovineTest {

    @Test
    void laGrammaticaDaNomiConLArticoloEVari() {
        Set<String> nomi = new HashSet<>();
        for (int i = 0; i < 300; i++) {
            String nome = ProduttoreDiTestiCasuale.nomeRovine();
            assertTrue(nome.matches("(il|lo|la|i|gli|le|l') ?\\S.*"), nome);
            assertFalse(nome.contains("[") || nome.contains("{") || nome.contains("  "), nome);
            assertTrue(nome.matches(".* (del|dello|della|dell'|dei|degli|delle)\\b.*") || nome.contains(" dell'"), nome);
            nomi.add(nome);
        }
        assertTrue(nomi.size() > 100, "nomi diversi: " + nomi.size());
    }

    @Test
    void leRovineDellInizioHannoUnNomeDiversoChiSiSalvaEChiEntraLoLegge() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(93)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
                    () -> partita.spostaGruppoIn(ClassiLocazione.CITTA_NYENA));
            List<CoordinateMD> rovine = new ArrayList<>();
            for (int x = 0; x < Foresta.getDimensioneX(); x++) {
                for (int y = 0; y < Foresta.getDimensioneY(); y++) {
                    if (Foresta.getLocazione(x, y) == ClassiLocazione.ROVINE) {
                        rovine.add(new CoordinateMD(x, y));
                    }
                }
            }
            assertTrue(rovine.size() > 1);
            List<String> nomi = new ArrayList<>();
            rovine.forEach(r -> nomi.add(Foresta.getLocazioneMD(r).getNome()));
            assertFalse(nomi.contains(null), "le rovine hanno il nome da quando nascono: " + nomi);
            assertEquals(nomi.size(), new HashSet<>(nomi).size(), "nomi tutti diversi: " + nomi);

            partita.salva(Comando.NUMERO_2);
            assertTrue(partita.leggi(Comando.NUMERO_2));
            for (int i = 0; i < rovine.size(); i++) {
                assertEquals(nomi.get(i), Rovine.getNome(Foresta.getLocazioneMD(rovine.get(i))), "dopo il caricamento");
            }

            CoordinateMD prima = rovine.get(0);
            partita.gruppo().setCoordinate(prima);
            Foresta.costruisciIstanza(prima).descrivi(partita.gruppo(), GruppoAvversario.getIstanza());
            String atteso = "Qui, " + Misc.conPreposizione("in", nomi.get(0)) + ", ";
            assertTrue(partita.testi().stream().anyMatch(t -> t.startsWith(atteso)), atteso + " in " + partita.testi());
        }
    }

    @Test
    void leRovineDiUnCastelloDiUnaCittaEDelCovoPrendonoIlNomeDaQuelloCheCera() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(94)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
            CoordinateMD ruuna = Foresta.getCoordinateLocazioneUnica(ClassiLocazione.CITTA_RUUNA);
            Foresta.distruggiLocazioneUnica(ClassiLocazione.CITTA_RUUNA, ClassiLocazione.ROVINE);
            assertEquals("le Rovine della città di Ruuna", Foresta.getLocazioneMD(ruuna).getNome());

            CoordinateMD covo = Foresta.costruisciLocazioneUnica(ClassiLocazione.ROVINE_RECUPERA_LE_DERRATE_ALIMENTARI, true);
            Foresta.distruggiLocazioneUnica(ClassiLocazione.ROVINE_RECUPERA_LE_DERRATE_ALIMENTARI, ClassiLocazione.ROVINE);
            assertEquals("le Rovine del covo dei Troll ladri di derrate", Foresta.getLocazioneMD(covo).getNome());

            RegistroMissioni.getMissionePrincipale().getMissioniSecondarie().forEach(m -> m.controllaPreLocazione());
            CoordinateMD lich = Foresta.getCoordinateLocazioneUnica(ClassiLocazione.CASTELLO_LICH);
            Foresta.distruggiLocazioneUnica(ClassiLocazione.CASTELLO_LICH, ClassiLocazione.ROVINE);
            assertEquals("le Rovine del Castello dell'Ombra", Foresta.getLocazioneMD(lich).getNome());
        }
    }
}
