package com.threeamigos.foresta.ui.tattiche;

import com.threeamigos.foresta.modellodati.tattiche.TatticaMD;
import com.threeamigos.foresta.motore.VistaPartitaMotore;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.tattiche.RegistroTattiche;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Il nome di una voce della tattica si chiede con un doppio click sul nome: se nel frattempo la bozza cambia (un altro personaggio, il
 * ritorno alla tattica applicata) o si lascia la schermata, la richiesta decade e l'Invio non scrive su una voce che non c'è più.
 */
class RinominaDellaVoceTest {

    private final List<Consumer<String>> richieste = new ArrayList<>();
    private final List<Consumer<String>> annullate = new ArrayList<>();
    private DisplayableCanvasTattiche schermata;
    private TatticaMD tattica;

    @BeforeEach
    void creaLaSchermata() {
        schermata = new DisplayableCanvasTattiche(1024, 768, new VistaPartitaMotore());
        schermata.setRichiestaTesto((predefinito, risposta) -> richieste.add(risposta), annullate::add);
        tattica = RegistroTattiche.predefinitaDelGiocabile(TipoPersonaggio.GUERRIERO);
        schermata.impostaTattica(new Guerriero("Arsenio", 1), tattica, tattica, null);
    }

    private Consumer<String> chiediIlNomeDellaPrimaVoce() {
        BozzaTattica.Voce voce = schermata.getBozza().getVoci().get(0);
        // Due click ravvicinati sullo stesso nome: il secondo chiede il nuovo nome
        schermata.clickSulNome(voce, 0);
        schermata.clickSulNome(voce, 0);
        return richieste.get(richieste.size() - 1);
    }

    @Test
    void laRispostaDelGiocatoreRinominaLaVoce() {
        Consumer<String> risposta = chiediIlNomeDellaPrimaVoce();

        risposta.accept("  Colpo di grazia ");

        assertEquals("Colpo di grazia", schermata.getBozza().getVoci().get(0).nome);
        assertTrue(annullate.isEmpty());
    }

    @Test
    void cambiandoPersonaggioLaRichiestaDecadeENonScriveSuUnaBozzaScartata() {
        Consumer<String> risposta = chiediIlNomeDellaPrimaVoce();
        BozzaTattica bozzaDiAllora = schermata.getBozza();

        // Un altro personaggio: la bozza riparte da quella del motore
        schermata.impostaTattica(new Guerriero("Brunilde", 1), tattica, tattica, null);

        assertEquals(java.util.Collections.singletonList(risposta), annullate, "la richiesta si chiude");
        assertNotSame(bozzaDiAllora, schermata.getBozza());
        // Se l'Invio arrivasse comunque, non cambia la bozza nuova
        String nomeNuovo = schermata.getBozza().getVoci().get(0).nome;
        risposta.accept("Da buttare");
        assertEquals(nomeNuovo, schermata.getBozza().getVoci().get(0).nome);
    }

    @Test
    void lasciandoLaSchermataLaRichiestaDecade() {
        Consumer<String> risposta = chiediIlNomeDellaPrimaVoce();

        schermata.annullaLaRichiestaDiTesto();

        assertEquals(java.util.Collections.singletonList(risposta), annullate);
        // Una seconda volta non ce n'è più nessuna da annullare
        schermata.annullaLaRichiestaDiTesto();
        assertEquals(1, annullate.size());
    }

    @Test
    void unaRichiestaGiaRispostaNonSiAnnullaPiu() {
        chiediIlNomeDellaPrimaVoce().accept("Altro nome");

        schermata.annullaLaRichiestaDiTesto();

        assertTrue(annullate.isEmpty());
    }
}
