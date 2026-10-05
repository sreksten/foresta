package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.intermezzi.Intermezzo;
import com.threeamigos.foresta.intermezzi.MomentoIntermezzo;
import com.threeamigos.foresta.intermezzi.PaginaIntermezzo;
import com.threeamigos.foresta.missioni.ClasseMissione;
import com.threeamigos.foresta.missioni.IntermezzoDiPasso;
import com.threeamigos.foresta.missioni.MissioneAPassi;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.missioni.Passo;
import com.threeamigos.foresta.motore.modellodati.ModelloDati;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoLocazione;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RegistroIntermezziPassiTest {

    /**
     * Due passi in locazione: RECUPERO, con un intermezzo a fine locazione, e GRAZIE, l'ultimo, con un intermezzo a
     * inizio locazione, che completa la missione.
     */
    static class MissioneConIntermezzi extends MissioneAPassi {

        final Set<String> pronti = new HashSet<>();

        MissioneConIntermezzi() {
            super(ClasseMissione.MISSIONE_DI_PROVA);
        }

        @Override
        protected String passoIniziale() {
            return "RECUPERO";
        }

        @Override
        protected Passo costruisciPasso(String id) {
            Passo passo = Passo.quando(MomentoControllo.IN_LOCAZIONE, () -> pronti.contains(id));
            if ("RECUPERO".equals(id)) {
                return passo.esegui(this::attivaMissione).poi("GRAZIE")
                        .conIntermezzo(MomentoIntermezzo.LOCAZIONE_COMPLETATA,
                                () -> Collections.singletonList(new PaginaIntermezzo("Il medaglione è tuo.")));
            }
            return passo.poi(Passo.FINE).conIntermezzo(MomentoIntermezzo.INIZIO_LOCAZIONE,
                    () -> Collections.singletonList(new PaginaIntermezzo("Grazie!")));
        }
    }

    private PartitaDiTest partita;
    private MissioneConIntermezzi missione;

    @BeforeEach
    void nuovaPartita() {
        // Gli intermezzi fissi guardano la locazione corrente: serve un mondo vero
        partita = PartitaDiTest.nuova(21);
        partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
                () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
        RegistroIntermezzi.nuovoMomento();
        missione = new MissioneConIntermezzi();
        // Sotto la missione principale, così il registro deve scendere nell'albero per trovarla
        RegistroMissioni.getMissionePrincipale().aggiungiMissione(missione);
    }

    @AfterEach
    void fine() {
        partita.close();
    }

    /**
     * Il prossimo intermezzo di passo per quel momento, saltando (e segnando) quelli fissi che scattassero prima.
     */
    private static IntermezzoDiPasso prossimoDiPasso(MomentoIntermezzo momento) {
        for (int i = 0; i < 20; i++) {
            Intermezzo intermezzo = RegistroIntermezzi.getProssimoIntermezzo(momento);
            if (intermezzo == null || intermezzo instanceof IntermezzoDiPasso) {
                return (IntermezzoDiPasso) intermezzo;
            }
            RegistroIntermezzi.segnaScattato(intermezzo);
        }
        throw new IllegalStateException("Troppi intermezzi fissi");
    }

    @Test
    void lIntermezzoDiUnPassoConclusoScattaNelSuoMomentoUnaVoltaSola() {
        assertNull(prossimoDiPasso(MomentoIntermezzo.LOCAZIONE_COMPLETATA), "nessun passo concluso");
        missione.pronti.add("RECUPERO");
        missione.controllaInLocazione();

        assertNull(prossimoDiPasso(MomentoIntermezzo.INIZIO_LOCAZIONE), "è per la fine locazione");
        IntermezzoDiPasso intermezzo = prossimoDiPasso(MomentoIntermezzo.LOCAZIONE_COMPLETATA);
        assertNotNull(intermezzo);
        assertSame(missione, intermezzo.getMissione());
        assertEquals("RECUPERO", intermezzo.getIdPasso());
        assertTrue(intermezzo.deveScattare(MomentoIntermezzo.LOCAZIONE_COMPLETATA));

        RegistroIntermezzi.segnaScattato(intermezzo);
        assertEquals("Il medaglione è tuo.", intermezzo.getPagine().get(0).getTesto());
        assertTrue(missione.isIntermezzoPassoMostrato("RECUPERO"));
        assertFalse(ModelloDati.getIstanza().getIntermezziMD().isScattato(intermezzo.getId()),
                "il già mostrato sta nella missione, non in IntermezziMD");
        assertNull(prossimoDiPasso(MomentoIntermezzo.LOCAZIONE_COMPLETATA), "non si ripete");
    }

    @Test
    void ancheLUltimoPassoDiUnaMissioneCompletataMostraIlSuoIntermezzo() {
        missione.pronti.add("RECUPERO");
        missione.controllaInLocazione();
        missione.pronti.add("GRAZIE");
        missione.controllaInLocazione();
        assertTrue(missione.isCompleta());

        IntermezzoDiPasso intermezzo = prossimoDiPasso(MomentoIntermezzo.INIZIO_LOCAZIONE);
        assertNotNull(intermezzo);
        assertEquals("GRAZIE", intermezzo.getIdPasso());
        RegistroIntermezzi.segnaScattato(intermezzo);
        // Quello del primo passo aspetta ancora la fine di una locazione
        assertEquals("RECUPERO", prossimoDiPasso(MomentoIntermezzo.LOCAZIONE_COMPLETATA).getIdPasso());
    }
}
