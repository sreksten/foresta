package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.combattimento.CalcolatoreCombattimento;
import com.threeamigos.foresta.combattimento.DannoRisultante;
import com.threeamigos.foresta.interfacce.Arma;
import com.threeamigos.foresta.effettidistato.EffettoDiStatoMD;
import com.threeamigos.foresta.modellodati.IncantamentoMD;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.danni.tipi.TipoDanno;
import com.threeamigos.foresta.effettidistato.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.danni.tipi.TipoInterazioneConEffettiDiStato;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Gli stati che ogni interazione dichiara di togliere e di mettere (TipoInterazioneConEffettiDiStato.getStatiRimossi e
 * getStatiApplicati) sono quelli che il calcolo del danno toglie e mette davvero quando l'interazione scatta.
 */
class ConseguenzeDelleInterazioniTest {

    @BeforeEach
    void prepara() {
        ModelloDati.setIstanza(new ModelloDati());
        Dado.impostaSeme(11);
    }

    @AfterEach
    void ripristina() {
        Dado.ripristina();
        Dado.impostaSeme(System.nanoTime());
    }

    private static Arma arma(TipoDanno tipo) {
        return new Arma() {
            @Override public int getDanni() { return 20; }
            @Override public int getLivello() { return 5; }
            @Override public TipoDanno getTipoDanno() { return tipo; }
            @Override public boolean isIncantata() { return false; }
            @Override public Collection<IncantamentoMD> getIncantamenti() { return new ArrayList<>(); }
        };
    }

    @Test
    void ogniInterazioneToglieEMetteGliStatiCheDichiara() {
        int provate = 0;
        for (TipoInterazioneConEffettiDiStato interazione : TipoInterazioneConEffettiDiStato.values()) {
            TipoEffettoDiStato precondizione = interazione.getPrecondizioni().iterator().next();
            TipoDanno innescante = interazione.getInnescanti().iterator().next();
            Personaggio attaccante = FabbricaPersonaggi.crea(TipoPersonaggio.LADRO, 5);
            Personaggio difensore = FabbricaPersonaggi.crea(TipoPersonaggio.GUERRIERO, 5);
            difensore.addEffettoDiStato(precondizione, 3, 0);

            DannoRisultante danno = CalcolatoreCombattimento.calcolaDannoRisultante(attaccante, difensore, arma(innescante));
            if (!danno.getInterazioni().contains(interazione)) {
                // Alcune reazioni scattano con una probabilità (vedi fattoreEffetti): qui conta solo che, quando scattano, facciano quel che dicono
                continue;
            }
            Set<TipoEffettoDiStato> messi = danno.getEffettiDiStatoDaAggiungere().stream()
                    .map(EffettoDiStatoMD::getTipoEffettoDiStato).collect(Collectors.toSet());
            assertTrue(messi.containsAll(interazione.getStatiApplicati()), interazione + " mette " + interazione.getStatiApplicati() + ": " + messi);
            assertTrue(danno.getEffettiDiStatoDaRimuovere().containsAll(interazione.getStatiRimossi()),
                    interazione + " toglie " + interazione.getStatiRimossi() + ": " + danno.getEffettiDiStatoDaRimuovere());
            provate++;
        }
        assertEquals(TipoInterazioneConEffettiDiStato.values().length, provate, "qualche interazione non è scattata");
    }

    @Test
    void lInciampoAtterraIlRallentatoColpitoDaUnaMazza() {
        Personaggio attaccante = FabbricaPersonaggi.crea(TipoPersonaggio.LADRO, 5);
        Personaggio difensore = FabbricaPersonaggi.crea(TipoPersonaggio.GUERRIERO, 5);
        difensore.addEffettoDiStato(TipoEffettoDiStato.RALLENTATO, 3, 0);
        DannoRisultante danno = CalcolatoreCombattimento.calcolaDannoRisultante(attaccante, difensore, arma(TipoDanno.CONTUNDENTE));
        assertTrue(danno.getInterazioni().contains(TipoInterazioneConEffettiDiStato.INCIAMPO));
        assertTrue(danno.getEffettiDiStatoDaAggiungere().stream().anyMatch(e -> e.getTipoEffettoDiStato() == TipoEffettoDiStato.ATTERRATO));
    }

    @Test
    void leReazioniCheNonToccanoGliStatiNonNeDichiarano() {
        for (TipoInterazioneConEffettiDiStato interazione : new TipoInterazioneConEffettiDiStato[] {
                TipoInterazioneConEffettiDiStato.ELETTROCUZIONE, TipoInterazioneConEffettiDiStato.MIETITURA,
                TipoInterazioneConEffettiDiStato.COLPO_DI_GRAZIA, TipoInterazioneConEffettiDiStato.RISONANZA_SIGILLATA}) {
            assertTrue(interazione.getStatiApplicati().isEmpty() && interazione.getStatiRimossi().isEmpty(), interazione.name());
        }
        assertThrows(UnsupportedOperationException.class,
                () -> TipoInterazioneConEffettiDiStato.INCIAMPO.getStatiApplicati().add(TipoEffettoDiStato.BAGNATO));
    }
}
