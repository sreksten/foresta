package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.incantesimi.Fulmine;
import com.threeamigos.foresta.incantesimi.Fuoco;
import com.threeamigos.foresta.incantesimi.Morte;
import com.threeamigos.foresta.motore.modellodati.ModelloDati;
import com.threeamigos.foresta.motore.modellodati.ModificatoreAttributo;
import com.threeamigos.foresta.motore.modellodati.TipoAttributo;
import com.threeamigos.foresta.motore.modellodati.TipoEffettoDiStato;
import com.threeamigos.foresta.motore.modellodati.TipoModificatore;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.personaggi.Mago;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.personaggi.Scheletro;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        mago.addModificatore(new ModificatoreAttributo(TipoAttributo.CRITICO, TipoModificatore.QUANTITA_ASSOLUTA, 0));
        bersaglio = new Guerriero("Bersaglio", 5);
        bersaglio.addModificatore(new ModificatoreAttributo(TipoAttributo.FORTUNA, TipoModificatore.QUANTITA_ASSOLUTA, 1000));
    }

    @Test
    void ilPotereMagicoAumentaIDanniDegliIncantesimi() {
        // Given
        int senza = CalcolatoreCombattimento.calcolaDannoRisultante(mago, bersaglio, new Fuoco(5)).getDanno();
        // When
        mago.addModificatore(new ModificatoreAttributo(TipoAttributo.POTERE_MAGICO, TipoModificatore.AUMENTO_PERCENTUALE, 50));
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
        mago.addModificatore(new ModificatoreAttributo(TipoAttributo.NUMERO_BERSAGLI, TipoModificatore.QUANTITA_ASSOLUTA, 2));
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

    private static void ferisciFinoA(Personaggio personaggio, int salute) {
        personaggio.subSalute(personaggio.getSalute() - salute, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
    }
}
