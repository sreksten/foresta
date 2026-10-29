package com.threeamigos.foresta.tipi;


import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.incantesimi.FabbricaIncantesimi;
import com.threeamigos.foresta.intermezzi.FabbricaIntermezzi;
import com.threeamigos.foresta.locazioni.FabbricaLocazioni;
import com.threeamigos.foresta.missioni.FabbricaMissioni;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.offerte.FabbricaOfferte;
import com.threeamigos.foresta.oggetti.FabbricaOggetti;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Ogni identificativo dei tipi ha la sua riga nella fabbrica che lo costruisce: dimenticarla non darebbe errori
 * finché il gioco non prova a costruire proprio quell'oggetto (per una missione, alla rilettura di un salvataggio).
 */
class FabbricheTest {

    @BeforeEach
    void prepara() {
        BusEventi.azzera();
        BusEventi.impostaConsegna(Runnable::run);
        ModelloDati.installa();
        ModelloDati.setIstanza(new ModelloDati());
    }

    @Test
    void ogniTipoLocazioneSiCostruisce() {
        for (TipoLocazione tipo : TipoLocazione.values()) {
            assertNotNull(FabbricaLocazioni.crea(tipo), tipo.name());
        }
    }

    @Test
    void ogniTipoPersonaggioSiCostruisce() {
        for (TipoPersonaggio tipo : TipoPersonaggio.values()) {
            assertNotNull(FabbricaPersonaggi.crea(tipo, 1), tipo.name());
        }
    }

    @Test
    void ogniClasseMissioneSiCostruisce() {
        for (MissionePredefinita classe : MissionePredefinita.values()) {
            // Le missioni di prova sono dei test, che le registrano da sé: nel gioco non esistono
            if (classe == MissionePredefinita.MISSIONE_DI_PROVA || classe == MissionePredefinita.MISSIONE_CHE_FALLISCE) {
                continue;
            }
            assertNotNull(FabbricaMissioni.crea(classe), classe.name());
        }
    }

    @Test
    void ogniClasseIncantesimoSiCostruisceEHaICosti() {
        for (TipoIncantesimo classe : TipoIncantesimo.values()) {
            assertNotNull(FabbricaIncantesimi.crea(classe, 1), classe.name());
            classe.getCostoAcquisto();
            classe.getCostoLancio();
        }
    }

    @Test
    void ogniTipoBottinoGenerabileSiCostruisce() {
        for (TipoBottino tipo : TipoBottino.values()) {
            if (tipo.isGenerabile()) {
                assertNotNull(FabbricaOggetti.crea(tipo), tipo.name());
            }
        }
    }

    @Test
    void ogniTipoIntermezzoSiCostruisce() {
        for (TipoIntermezzo tipo : TipoIntermezzo.values()) {
            assertNotNull(FabbricaIntermezzi.crea(tipo), tipo.name());
        }
    }

    @Test
    void ogniTipoOffertaSiCostruisce() {
        for (TipoOfferta tipo : TipoOfferta.values()) {
            assertNotNull(FabbricaOfferte.crea(tipo), tipo.name());
        }
    }
}
