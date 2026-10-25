package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.missioni.Missione;
import com.threeamigos.foresta.missioni.MissioneAPassi;
import com.threeamigos.foresta.missioni.RegistroMissioni;
import com.threeamigos.foresta.missioni.incarichi.IlNegozio;
import com.threeamigos.foresta.missioni.incarichi.IncaricoInCitta;
import com.threeamigos.foresta.missioni.leggende.LaLeggenda;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.MissionePredefinita;
import com.threeamigos.foresta.mappa.TipoLocazione;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Il sorteggio degli incarichi in città (SceltaDegliIncarichi): fra quelli pronti nella stessa visita ne parte uno a caso,
 * con più probabilità fra le classi non ancora affidate nel giro; esaurite quelle, il giro ricomincia.
 */
class ScenarioSorteggioDegliIncarichiTest {

    private static final String GIRO = "GIRO_DI_AFFIDAMENTO";

    /**
     * Una partita col sorteggio acceso, il gruppo a Fleena: leggende e negozi, che si offrirebbero prima delle
     * altre storie, sono rimandati.
     */
    private static PartitaDiTest aFleena(long seme) {
        PartitaDiTest partita = PartitaDiTest.nuova(seme).conSorteggioDegliIncarichi();
        partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_FLEENA));
        for (Missione missione : RegistroMissioni.getTutteLeMissioni()) {
            if (missione instanceof LaLeggenda || missione instanceof IlNegozio) {
                missione.aggiungiProprieta("DISPONIBILE_DALLE", String.valueOf(Long.MAX_VALUE));
            }
        }
        return partita;
    }

    private static List<IncaricoInCitta> incarichiInAttesa() {
        return RegistroMissioni.getTutteLeMissioni().stream()
                .filter(m -> m instanceof IncaricoInCitta && !(m instanceof IlNegozio))
                .map(IncaricoInCitta.class::cast)
                .filter(i -> "INCARICO".equals(i.getPassoCorrente()))
                .collect(Collectors.toList());
    }

    /**
     * I controlli prima della locazione, nell'ordine del registro come fa l'automa; l'incarico partito, che è uno solo.
     */
    private static IncaricoInCitta controllaLaVisita(List<IncaricoInCitta> incarichi) {
        incarichi.forEach(MissioneAPassi::controllaPreLocazione);
        List<IncaricoInCitta> partiti = incarichi.stream().filter(i -> "ACCETTAZIONE".equals(i.getPassoCorrente()))
                .collect(Collectors.toList());
        assertEquals(1, partiti.size(), String.valueOf(partiti));
        return partiti.get(0);
    }

    @Test
    void nonPartonoSempreGliStessi() {
        Set<MissionePredefinita> partiti = EnumSet.noneOf(MissionePredefinita.class);
        for (long seme = 1; seme <= 20; seme++) {
            try (PartitaDiTest partita = aFleena(seme)) {
                List<IncaricoInCitta> incarichi = incarichiInAttesa();
                assertTrue(incarichi.size() > 5, String.valueOf(incarichi));
                partiti.add(controllaLaVisita(incarichi).getClasse());
            }
        }
        assertTrue(partiti.size() >= 5, "troppo pochi incarichi diversi: " + partiti);
    }

    @Test
    void conTreQuartiEsceUnaClasseNonAncoraAffidata() {
        try (PartitaDiTest partita = aFleena(7)) {
            List<IncaricoInCitta> incarichi = incarichiInAttesa();
            // Tutte già affidate nel giro 0, tranne il cartografo
            incarichi.stream().filter(i -> i.getClasse() != MissionePredefinita.IL_CARTOGRAFO)
                    .forEach(i -> i.aggiungiProprieta(GIRO, "0"));
            Dado.trucca(0.1d);
            IncaricoInCitta partito = controllaLaVisita(incarichi);
            assertEquals(MissionePredefinita.IL_CARTOGRAFO, partito.getClasse());
            assertEquals("0", partito.ottieniProprieta(GIRO), "affidato nel giro corrente");
        }
    }

    @Test
    void conUnQuartoEsceUnaClasseGiaAffidata() {
        try (PartitaDiTest partita = aFleena(7)) {
            List<IncaricoInCitta> incarichi = incarichiInAttesa();
            incarichi.stream().filter(i -> i.getClasse() != MissionePredefinita.IL_CARTOGRAFO)
                    .forEach(i -> i.aggiungiProprieta(GIRO, "0"));
            Dado.trucca(0.9d, 1);
            assertNotEquals(MissionePredefinita.IL_CARTOGRAFO, controllaLaVisita(incarichi).getClasse());
        }
    }

    @Test
    void affidateTutteIlGiroRicomincia() {
        try (PartitaDiTest partita = aFleena(7)) {
            List<IncaricoInCitta> incarichi = incarichiInAttesa();
            incarichi.forEach(i -> i.aggiungiProprieta(GIRO, "0"));
            // I negozi, rimandati, aspettano di offrirsi come gli altri: contano anche loro per chiudere il giro
            RegistroMissioni.getTutteLeMissioni().stream().filter(m -> m instanceof IlNegozio)
                    .forEach(m -> m.aggiungiProprieta(GIRO, "0"));
            assertEquals("1", controllaLaVisita(incarichi).ottieniProprieta(GIRO), "nel giro 0 non restava niente da affidare");
        }
    }

    @Test
    void senzaSorteggioParteIlPrimoNellOrdineDelRegistro() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(7)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_FLEENA));
            for (Missione missione : RegistroMissioni.getTutteLeMissioni()) {
                if (missione instanceof LaLeggenda || missione instanceof IlNegozio) {
                    missione.aggiungiProprieta("DISPONIBILE_DALLE", String.valueOf(Long.MAX_VALUE));
                }
            }
            List<IncaricoInCitta> incarichi = incarichiInAttesa();
            assertSame(incarichi.get(0), controllaLaVisita(incarichi));
        }
    }
}
