package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.interni.InternoAvversarioSconfitto;
import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.missioni.CacciatoreDiTaglie;
import com.threeamigos.foresta.missioni.IlRapimento;
import com.threeamigos.foresta.missioni.IncaricoInCitta;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tools.GestoreSalvataggi;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Il cacciatore di taglie: la banda di goblin si nasconde fra rovine non segnate sulla mappa, basta abbattere il capo,
 * un hobgoblin, e poi si torna a riscuotere. E i nomi presi da missioni.txt restano gli stessi dopo un caricamento.
 */
class ScenarioCacciatoreDiTaglieTest {

    private static <T extends IncaricoInCitta> T prendiLIncarico(PartitaDiTest partita, Class<T> tipo) {
        T incarico = RegistroMissioni.getTutteLeMissioni().stream().filter(tipo::isInstance).map(tipo::cast)
                .findFirst().orElseThrow(AssertionError::new);
        // Come a una visita tranquilla della città
        incarico.controllaPreLocazione();
        incarico.segnaIntermezzoPassoMostrato("INCARICO");
        incarico.controllaInLocazione();
        assertTrue(incarico.isAttiva());
        return incarico;
    }

    @Test
    void laBandaVaCercataBastaAbbattereIlCapoEPoiSiRiscuote() {
        // Senza trucchi: in modalità di prova la mappa è già tutta svelata
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(161)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(ClassiLocazione.CITTA_NYENA));
            CacciatoreDiTaglie taglia = prendiLIncarico(partita, CacciatoreDiTaglie.class);
            String ricercato = taglia.getRicercato();
            assertEquals("Ricercato: " + ricercato, taglia.getNome());

            // Le rovine non sono sulla mappa: si sa solo da che parte cercarle
            CoordinateMD covo = taglia.getCovo();
            assertEquals(ClassiLocazione.ROVINE, Foresta.getLocazione(covo));
            assertFalse(Foresta.isLocazioneConosciuta(covo));
            assertFalse(Foresta.getCoordinateDaSegnalare().contains(covo));
            assertTrue(partita.testi().stream().anyMatch(t -> t.startsWith("Dicono che la banda di " + ricercato)
                    && t.contains("di cammino verso")), String.valueOf(partita.testi()));
            assertTrue(taglia.getDescrizione().contains("da Nyena"), taglia.getDescrizione());

            // La banda: tre goblin e il ricercato, un hobgoblin
            List<Personaggio> banda = RegistroMissioni.getIncontroMissione(covo).orElseThrow(AssertionError::new);
            assertEquals(CacciatoreDiTaglie.GOBLIN + 1, banda.size());
            assertEquals(ClassePersonaggio.GOBLIN, banda.get(0).getClasse());
            Personaggio capo = banda.get(banda.size() - 1);
            assertEquals(ClassePersonaggio.HOBGOBLIN, capo.getClasse());
            assertEquals(ricercato, capo.getNome());

            // Un hobgoblin abbattuto altrove non conta; i goblin nemmeno; il capo sì
            partita.gruppo().setCoordinate(new CoordinateMD(covo.getX() == 0 ? 1 : covo.getX() - 1, covo.getY()));
            partita.pubblica(new InternoAvversarioSconfitto(ClassePersonaggio.HOBGOBLIN));
            partita.gruppo().setCoordinate(covo);
            for (int i = 0; i < CacciatoreDiTaglie.GOBLIN; i++) {
                partita.pubblica(new InternoAvversarioSconfitto(ClassePersonaggio.GOBLIN));
            }
            taglia.controllaPostLocazione();
            assertEquals("CACCIA", taglia.getPassoCorrente());
            partita.pubblica(new InternoAvversarioSconfitto(ClassePersonaggio.HOBGOBLIN));
            taglia.controllaPostLocazione();
            assertEquals("RITORNO", taglia.getPassoCorrente());

            partita.gruppo().setCoordinate(Foresta.getCoordinateLocazioneUnica(ClassiLocazione.CITTA_NYENA));
            int monete = partita.gruppo().getMonete();
            taglia.controllaPreLocazione();
            taglia.segnaIntermezzoPassoMostrato("RITORNO");
            taglia.controllaInLocazione();
            assertEquals(monete + 30, partita.gruppo().getMonete());
            assertTrue(taglia.isCompleta());
            assertEquals(2, RegistroMissioni.getTutteLeMissioni().stream().filter(CacciatoreDiTaglie.class::isInstance).count(),
                    "c'è già un altro ricercato");
        }
    }

    @Test
    void iNomiPescatiRestanoGliStessiDopoUnCaricamento() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(162)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(ClassiLocazione.CITTA_NYENA));
            IlRapimento rapimento = prendiLIncarico(partita, IlRapimento.class);
            String ostaggio = rapimento.getOstaggio();
            String capobanda = rapimento.getCapobanda();

            GestoreSalvataggi.salva(Comando.NUMERO_2);
            assertTrue(GestoreSalvataggi.leggi(Comando.NUMERO_2));
            IlRapimento riletto = RegistroMissioni.getTutteLeMissioni().stream().filter(IlRapimento.class::isInstance)
                    .map(IlRapimento.class::cast).findFirst().orElseThrow(AssertionError::new);
            assertEquals(ostaggio, riletto.getOstaggio());
            assertEquals(capobanda, riletto.getCapobanda());
            assertEquals("Il rapimento di " + ostaggio, riletto.getNome());
        }
    }
}
