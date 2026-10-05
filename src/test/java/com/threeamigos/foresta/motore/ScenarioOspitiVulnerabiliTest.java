package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.missioni.MissioneAPassi;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.missioni.Passo;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.personaggi.Viandante;
import com.threeamigos.foresta.tipi.ClasseMissione;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.tipi.TipoRiposo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Un ospite vulnerabile (un ostaggio, un ferito da soccorrere) non combatte ma gli avversari lo possono attaccare,
 * e se muore la sua scorta fallisce.
 */
class ScenarioOspitiVulnerabiliTest {

    /**
     * Libera l'ostaggio: si unisce al gruppo, vulnerabile, e va riportato in città vivo.
     */
    static class LiberaLOstaggio extends MissioneAPassi {

        private final CoordinateMD casa;

        LiberaLOstaggio(CoordinateMD casa) {
            super(ClasseMissione.MISSIONE_DI_PROVA);
            this.casa = casa;
        }

        @Override
        protected String passoIniziale() {
            return "LIBERAZIONE";
        }

        @Override
        protected Passo costruisciPasso(String id) {
            if ("LIBERAZIONE".equals(id)) {
                return prendiInScorta(MomentoControllo.IN_LOCAZIONE, () -> true, "Ottone", true).poi("RITORNO");
            }
            return scorta(MomentoControllo.PRE_LOCAZIONE, () -> casa, () -> "L'ostaggio non ce l'ha fatta.").poi(Passo.FINE);
        }
    }

    private static PartitaDiTest partita(long seme) {
        PartitaDiTest partita = PartitaDiTest.nuova(seme);
        partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
        return partita;
    }

    @Test
    void unOspiteVulnerabileSiPrendeLaSuaParteDiAttacchiQuelloNormaleMai() {
        try (PartitaDiTest partita = partita(111)) {
            Viandante ostaggio = new Viandante("Ottone", 1);
            Viandante pellegrino = new Viandante("Anselmo", 1);
            partita.gruppo().aggiungiOspite(ostaggio, true);
            partita.gruppo().aggiungiOspite(pellegrino);
            assertTrue(partita.gruppo().isOspiteVulnerabile(ostaggio));
            assertFalse(partita.gruppo().isOspiteVulnerabile(pellegrino));

            // Un solo personaggio vivo e un ospite vulnerabile: circa un attacco su due va all'ospite
            int colpiAllOspite = 0;
            for (int i = 0; i < 1000; i++) {
                Personaggio bersaglio = partita.gruppo().scegliOspiteBersaglio().orElse(null);
                assertNotSame(pellegrino, bersaglio);
                if (bersaglio == ostaggio) {
                    colpiAllOspite++;
                }
            }
            assertTrue(colpiAllOspite > 400 && colpiAllOspite < 600, "colpi all'ospite: " + colpiAllOspite);

            ostaggio.muore("di prova");
            assertFalse(partita.gruppo().scegliOspiteBersaglio().isPresent(), "un ospite morto non si attacca più");
        }
    }

    @Test
    void unAvversarioAttaccaDavveroLOspiteVulnerabile() {
        try (PartitaDiTest partita = partita(112)) {
            Viandante ostaggio = new Viandante("Ottone", 1);
            partita.gruppo().aggiungiOspite(ostaggio, true);
            Personaggio goblin = FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 1);
            for (int i = 0; i < 50 && partita.testi().stream().noneMatch(t -> t.contains("attacca Ottone")); i++) {
                goblin.attacca(partita.gruppo());
                partita.gruppo().getCapo().addSalute(partita.gruppo().getCapo().getSaluteMassima());
                ostaggio.addSalute(ostaggio.getSaluteMassima());
            }
            assertTrue(partita.testi().stream().anyMatch(t -> t.contains("attacca Ottone")), String.valueOf(partita.testi()));
        }
    }

    @Test
    void dopoUnCaricamentoLOstaggioÈAncoraVulnerabileERiposandoRecupera() {
        try (PartitaDiTest partita = partita(113)) {
            partita.gruppo().aggiungiOspite(new Viandante("Ottone", 1), true);
            partita.gruppo().aggiungiOspite(new Viandante("Anselmo", 1));

            partita.salva(Comando.NUMERO_2);
            assertTrue(partita.leggi(Comando.NUMERO_2));
            Personaggio ottone = ospite(partita, "Ottone");
            assertTrue(partita.gruppo().isOspiteVulnerabile(ottone), "vulnerabile anche dopo il caricamento");
            assertFalse(partita.gruppo().isOspiteVulnerabile(ospite(partita, "Anselmo")));

            ottone.subSalute(ottone.getSalute() / 2, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
            int salute = ottone.getSalute();
            partita.gruppo().riposa(TipoRiposo.ALL_APERTO_CON_FUOCO);
            assertTrue(ottone.getSalute() > salute, "riposando l'ostaggio recupera");
        }
    }

    @Test
    void seLOstaggioMuoreLaScortaFallisceELuiSiSeparaDalGruppo() {
        try (PartitaDiTest partita = partita(114)) {
            LiberaLOstaggio missione = new LiberaLOstaggio(new CoordinateMD(0, 0));
            RegistroMissioni.getMissionePrincipale().aggiungiMissione(missione);
            missione.attivaMissione();
            missione.controllaInLocazione();
            Personaggio ostaggio = missione.getScortato().orElseThrow(AssertionError::new);
            assertTrue(partita.gruppo().isOspiteVulnerabile(ostaggio));

            ostaggio.muore("un goblin");
            missione.controllaPostLocazione();
            assertTrue(missione.isFallita());
            assertTrue(partita.gruppo().getOspiti().isEmpty(), "l'ostaggio si separa dal gruppo");
            assertTrue(partita.testi().contains("L'ostaggio non ce l'ha fatta."), String.valueOf(partita.testi()));
        }
    }

    private static Personaggio ospite(PartitaDiTest partita, String nome) {
        return partita.gruppo().getOspiti().stream().filter(p -> nome.equals(p.getNome())).findFirst()
                .orElseThrow(() -> new AssertionError(nome + " non è fra gli ospiti"));
    }
}
