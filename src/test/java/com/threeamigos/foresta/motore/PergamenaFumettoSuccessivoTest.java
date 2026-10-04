package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.interni.InternoFumettoSuccessivo;
import com.threeamigos.foresta.eventi.notifiche.NotificaPaginaIntermezzo;
import com.threeamigos.foresta.intermezzi.BattutaProgrammata;
import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.missioni.LealtaRichiesta;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.motore.modellodati.ModificatoreAttributo;
import com.threeamigos.foresta.motore.tipi.TipoAttributo;
import com.threeamigos.foresta.motore.tipi.TipoModificatore;
import com.threeamigos.foresta.personaggi.Guerriero;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * In un intermezzo la pergamena salta al fumetto successivo della pagina, se ce n'è ancora uno da cominciare; quando
 * sono cominciati tutti, gira la pagina.
 */
class PergamenaFumettoSuccessivoTest {

    @Test
    void laPergamenaSaltaAlFumettoSuccessivoPoiGiraLaPagina() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(261)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.LADRO, () -> partita.spostaGruppoIn(ClassiLocazione.CITTA_NYENA));
            partita.comando(Comando.ESCI_DA_CITTA);
            partita.gruppo().setCoordinate(unaCasellaDiBosco());
            Guerriero sentinella = new Guerriero("Sentinella", 1);
            // Già "leale" (vedi LaLealta): altrimenti la sua confidenza ruberebbe la scena al primo
            // accampamento invece dell'intermezzo normale (vedi IntermezzoAccampamento)
            sentinella.addModificatore(new ModificatoreAttributo(TipoAttributo.FORTUNA, TipoModificatore.QUANTITA_ASSOLUTA, 0, LealtaRichiesta.NOTA));
            partita.gruppo().aggiungiPersonaggio(sentinella);
            while (LineaTemporale.getOra() <= 20) {
                LineaTemporale.aggiungiOre(1);
            }
            partita.orologioFermo().nonSaltareIntermezzi();
            partita.eventi().ascolta(InternoFumettoSuccessivo.class);
            int pagineDiPrima = partita.eventi().tutti(NotificaPaginaIntermezzo.class).size();

            // La scena del primo accampamento: due fumetti
            partita.comando(Comando.ACCAMPAMENTO);
            partita.assertStato(Stato.INTERMEZZO);
            List<BattutaProgrammata> fumetti = partita.eventi().ultimo(NotificaPaginaIntermezzo.class).getPagina().getBattuteProgrammate();
            assertEquals(2, fumetti.size());
            assertTrue(fumetti.get(0).getBattuta().getTesto().contains("scarafaggio"));

            // A metà del primo fumetto la pergamena salta al secondo, sulla stessa pagina
            partita.avanzaOrologio(fumetti.get(0).getFine() / 2);
            partita.comando(Comando.PERGAMENA);
            partita.assertStato(Stato.INTERMEZZO);
            assertEquals(pagineDiPrima + 1, partita.eventi().tutti(NotificaPaginaIntermezzo.class).size());
            assertEquals(fumetti.get(1).getInizio(), partita.eventi().ultimo(InternoFumettoSuccessivo.class).getSecondi());

            // Cominciato l'ultimo fumetto, la pergamena gira la pagina
            partita.avanzaOrologio(0.5);
            partita.comando(Comando.PERGAMENA);
            assertEquals(1, partita.eventi().tutti(InternoFumettoSuccessivo.class).size());
            assertTrue(partita.eventi().tutti(NotificaPaginaIntermezzo.class).size() > pagineDiPrima + 1
                    || partita.stato() != Stato.INTERMEZZO, "la pagina è girata");
        }
    }

    private static CoordinateMD unaCasellaDiBosco() {
        for (int x = 0; x < Foresta.getDimensioneX(); x++) {
            for (int y = 0; y < Foresta.getDimensioneY(); y++) {
                if (Foresta.getLocazione(x, y) == ClassiLocazione.BOSCO) {
                    return new CoordinateMD(x, y);
                }
            }
        }
        throw new AssertionError("nessuna casella di bosco");
    }
}
