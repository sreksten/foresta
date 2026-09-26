package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.motore.modellodati.ModelloDati;
import com.threeamigos.foresta.motore.modellodati.TipoAttributo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 * @author Stefano Reksten
 */
class PersonaggioBaseTest {

    @Test
    void costruiscePersonaggioLivelloUno() {
        for (ClassePersonaggio classe : ClassePersonaggio.values()) {
            for (int i = 0; i < 10; i++) {
                Personaggio personaggio = classe.getIstanza(1);
            }
        }
    }

    @Test
    void iPuntiAbilitaSiSpendonoSoloSulleCaratteristiche() {
        // Given
        ModelloDati.setIstanza(new ModelloDati());
        Guerriero guerriero = new Guerriero("Arsenio", 5);
        guerriero.getModelloDati().setPuntiAbilitaDisponibili(1);
        int forza = guerriero.getForza();
        // When: sugli attributi ricavati da altro non si spende niente (e non si rompe niente)
        guerriero.spendiPuntoAbilita(TipoAttributo.RIGENERAZIONE_SALUTE);
        guerriero.spendiPuntoAbilita(TipoAttributo.POTERE_MAGICO);
        guerriero.spendiPuntoAbilita(TipoAttributo.NUMERO_BERSAGLI);
        // Then
        assertEquals(1, guerriero.getPuntiAbilitaDisponibili());
        guerriero.spendiPuntoAbilita(TipoAttributo.FORZA);
        assertEquals(0, guerriero.getPuntiAbilitaDisponibili());
        assertEquals(forza + 1, guerriero.getForza());
    }
}
