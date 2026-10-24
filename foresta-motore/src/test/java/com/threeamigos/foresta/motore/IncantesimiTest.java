package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.attributi.tipi.TipoAttributo;
import com.threeamigos.foresta.combattimento.CalcolatoreCombattimento;
import com.threeamigos.foresta.combattimento.StatoDelCombattimento;
import com.threeamigos.foresta.effettidistato.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.incantesimi.Fulmine;
import com.threeamigos.foresta.incantesimi.Fuoco;
import com.threeamigos.foresta.incantesimi.Morte;
import com.threeamigos.foresta.incantesimi.Silenzio;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.modificatori.ModificatoreAttributoMD;
import com.threeamigos.foresta.modificatori.tipi.TipoModificatore;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.personaggi.Mago;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.personaggi.Scheletro;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.tipi.tattiche.TipoAzione;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Il POTERE_MAGICO (bastoni, libri) aumenta i danni degli incantesimi, la riserva di MAGIA no; Morte uccide solo
 * chi è sotto un quarto della salute massima, altrimenti si ritorce contro chi la lancia. Il critico è
 * neutralizzato come in CalcolatoreCombattimentoBerserkTest, così il danno è deterministico.
 */
class IncantesimiTest {

    private Mago mago;
    private Guerriero bersaglio;

    @BeforeEach
    void preparaCombattenti() {
        ModelloDati.setIstanza(new ModelloDati());
        mago = new Mago("Merlino", 5);
        mago.addModificatore(new ModificatoreAttributoMD(TipoAttributo.CRITICO, TipoModificatore.QUANTITA_ASSOLUTA, 0));
        bersaglio = new Guerriero("Bersaglio", 5);
        bersaglio.addModificatore(new ModificatoreAttributoMD(TipoAttributo.FORTUNA, TipoModificatore.QUANTITA_ASSOLUTA, 1000));
    }

    @Test
    void ilPotereMagicoAumentaIDanniDegliIncantesimi() {
        // Given
        int senza = CalcolatoreCombattimento.calcolaDannoRisultante(mago, bersaglio, new Fuoco(5)).getDanno();
        // When
        mago.addModificatore(new ModificatoreAttributoMD(TipoAttributo.POTERE_MAGICO, TipoModificatore.VARIAZIONE_PERCENTUALE, 50));
        int con = CalcolatoreCombattimento.calcolaDannoRisultante(mago, bersaglio, new Fuoco(5)).getDanno();
        // Then
        assertTrue(con > senza, "con POTERE_MAGICO " + con + ", senza " + senza);
    }

    @Test
    void laRiservaDiMagiaNonCambiaIDanniDegliIncantesimi() {
        // Given
        int senza = CalcolatoreCombattimento.calcolaDannoRisultante(mago, bersaglio, new Fuoco(5)).getDanno();
        // When
        mago.addMagiaMassima(20, "POZIONE_MAGIA_GRANDE");
        int con = CalcolatoreCombattimento.calcolaDannoRisultante(mago, bersaglio, new Fuoco(5)).getDanno();
        // Then
        assertEquals(senza, con);
    }

    @Test
    void morteUccideChiESottoUnQuartoDellaSalute() {
        // Given: STORDITO, così il tiro per colpire riesce sempre
        ferisciFinoA(bersaglio, bersaglio.getSaluteMassima() / 5);
        bersaglio.addEffettoDiStato(TipoEffettoDiStato.STORDITO, 1, 0);
        // When
        new Morte(5).formula(mago, bersaglio, null);
        // Then
        assertFalse(bersaglio.isVivo());
    }

    @Test
    void morteSuChiEInSaluteSiRitorceControChiLaLancia() {
        // Given
        bersaglio.addEffettoDiStato(TipoEffettoDiStato.STORDITO, 1, 0);
        int saluteMago = mago.getSalute();
        // When
        new Morte(5).formula(mago, bersaglio, null);
        // Then
        assertTrue(bersaglio.isVivo());
        assertTrue(mago.getSalute() < saluteMago);
    }

    @Test
    void morteNonHaEffettoSuGliImmuni() {
        // Given
        Scheletro scheletro = new Scheletro(5);
        ferisciFinoA(scheletro, 1);
        scheletro.addEffettoDiStato(TipoEffettoDiStato.STORDITO, 1, 0);
        int saluteMago = mago.getSalute();
        // When
        new Morte(5).formula(mago, scheletro, null);
        // Then
        assertTrue(scheletro.isVivo());
        assertEquals(saluteMago, mago.getSalute());
    }

    @Test
    void unIncantesimoMultiploColpisceFinoAlNumeroDiBersagliDiChiLoLancia() {
        // Given: quattro avversari STORDITI (colpiti di sicuro), il mago ne colpisce due
        GruppoAvversario.azzeraIstanza();
        GruppoAvversario avversari = GruppoAvversario.getIstanza();
        for (int i = 0; i < 4; i++) {
            Guerriero avversario = new Guerriero("Avversario" + i, 5);
            avversario.addEffettoDiStato(TipoEffettoDiStato.STORDITO, 1, 0);
            avversari.aggiungiPersonaggio(avversario);
        }
        mago.addModificatore(new ModificatoreAttributoMD(TipoAttributo.NUMERO_BERSAGLI, TipoModificatore.QUANTITA_ASSOLUTA, 2));
        int[] saluteIniziale = avversari.getPersonaggi().stream().mapToInt(Personaggio::getSalute).toArray();
        // When
        new Fulmine(5).formula(mago, null, avversari);
        // Then
        int colpiti = 0;
        for (int i = 0; i < 4; i++) {
            if (avversari.getPersonaggio(i).getSalute() < saluteIniziale[i]) {
                colpiti++;
            }
        }
        assertEquals(2, colpiti);
    }

    @Test
    void morteNonSiFormulaSuUnGruppo() {
        GruppoAvversario.azzeraIstanza();
        GruppoAvversario avversari = GruppoAvversario.getIstanza();
        avversari.aggiungiPersonaggio(bersaglio);
        assertThrows(IllegalArgumentException.class, () -> new Morte(5).formula(mago, null, avversari));
    }

    @Test
    void morteUccideSottoUnQuartoSenzaArrotondamentiAncheConUnaSaluteMassimaNonDivisibilePerQuattro() {
        // Given: una salute massima che non si divide per quattro, e un bersaglio al di sotto del 25% ma non sotto la divisione intera
        if (bersaglio.getSaluteMassima() % 4 == 0) {
            bersaglio.addSaluteMassima(1, "prova");
        }
        int massima = bersaglio.getSaluteMassima();
        assertNotEquals(0, massima % 4);
        int salute = massima / 4;
        assertTrue(100 * salute < 25 * massima, "con " + salute + " su " + massima + " è sotto il 25%");
        ferisciFinoA(bersaglio, salute);
        assertTrue(Morte.puoUccidere(bersaglio));
        bersaglio.addEffettoDiStato(TipoEffettoDiStato.STORDITO, 1, 0);
        // When
        new Morte(5).formula(mago, bersaglio, null);
        // Then
        assertFalse(bersaglio.isVivo(), "Morte uccide con " + salute + " su " + massima);
    }

    @Test
    void morteNonUccideAlDiSopraDellaSoglia() {
        ferisciFinoA(bersaglio, (bersaglio.getSaluteMassima() * 25 + 99) / 100 + 1);
        assertFalse(Morte.puoUccidere(bersaglio));
    }

    @Test
    void morteRegistraGliEsitiCosiICheTentativiFallitiFermanoLaRipetizione() {
        // Given: bersaglio sano e STORDITO (colpito di sicuro): Morte si ritorce contro il mago, e il tentativo fallito si ricorda
        StatoDelCombattimento.azzeraIstanza();
        bersaglio.addEffettoDiStato(TipoEffettoDiStato.STORDITO, 1, 0);
        // When
        new Morte(5).formula(mago, bersaglio, null);
        // Then
        StatoDelCombattimento stato = StatoDelCombattimento.getIstanza();
        assertEquals(1, stato.getTentativiFalliti(mago, bersaglio, TipoAzione.MORTE));
        new Morte(5).formula(mago, bersaglio, null);
        assertEquals(2, stato.getTentativiFalliti(mago, bersaglio, TipoAzione.MORTE));
        // Una riuscita li azzera
        ferisciFinoA(bersaglio, 1);
        new Morte(5).formula(mago, bersaglio, null);
        assertEquals(0, stato.getTentativiFalliti(mago, bersaglio, TipoAzione.MORTE));
    }

    @Test
    void morteSuChiEImmuneSiRicordaComeImmunitaScoperta() {
        StatoDelCombattimento.azzeraIstanza();
        Scheletro scheletro = new Scheletro(5);
        scheletro.addEffettoDiStato(TipoEffettoDiStato.STORDITO, 1, 0);

        new Morte(5).formula(mago, scheletro, null);

        assertTrue(StatoDelCombattimento.getIstanza().isImmunitaScoperta(scheletro, TipoAzione.MORTE));
    }

    @Test
    void silenzioRegistraGliEsiti() {
        StatoDelCombattimento.azzeraIstanza();
        StatoDelCombattimento stato = StatoDelCombattimento.getIstanza();
        // Un colpo mancato (il bersaglio non è stordito, e il dado dà il massimo) conta come tentativo fallito
        Dado.trucca(100);
        try {
            new Silenzio(5).formula(mago, bersaglio, null);
        } finally {
            Dado.ripristina();
        }
        assertFalse(bersaglio.hasEffettoDiStato(TipoEffettoDiStato.SILENZIATO));
        assertEquals(1, stato.getTentativiFalliti(mago, bersaglio, TipoAzione.SILENZIO));

        // Un colpo riuscito azzera i tentativi e silenzia
        bersaglio.addEffettoDiStato(TipoEffettoDiStato.STORDITO, 1, 0);
        new Silenzio(5).formula(mago, bersaglio, null);
        assertTrue(bersaglio.hasEffettoDiStato(TipoEffettoDiStato.SILENZIATO));
        assertEquals(0, stato.getTentativiFalliti(mago, bersaglio, TipoAzione.SILENZIO));
    }

    private static void ferisciFinoA(Personaggio personaggio, int salute) {
        personaggio.subSalute(personaggio.getSalute() - salute, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
    }
}
