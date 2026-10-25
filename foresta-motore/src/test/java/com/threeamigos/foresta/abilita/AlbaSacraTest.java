package com.threeamigos.foresta.abilita;


import com.threeamigos.foresta.combattimento.StatoDelCombattimento;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.motore.Gruppo;
import com.threeamigos.foresta.motore.GruppoAvversario;
import com.threeamigos.foresta.motore.StatoDelMotore;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.TipoAbilita;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * L'Alba sacra toglie gli effetti di stato negativi al gruppo e lascia quelli benefici (ispirazione, berserk).
 */
class AlbaSacraTest {

    @Test
    void togliGliStatiNegativiELasciaQuelliBenefici() {
        StatoDelMotore.azzera();
        StatoDelCombattimento.getIstanza().reimposta();
        Personaggio sacerdote = FabbricaPersonaggi.crea(TipoPersonaggio.SACERDOTE, 3);
        Personaggio guerriero = FabbricaPersonaggi.crea(TipoPersonaggio.GUERRIERO, 3);
        sacerdote.addMagia(sacerdote.getMagiaMassima());
        sacerdote.addEffettoDiStato(TipoEffettoDiStato.ISPIRATO, 3, 0);
        sacerdote.addEffettoDiStato(TipoEffettoDiStato.AVVELENATO, 3, 0);
        guerriero.addEffettoDiStato(TipoEffettoDiStato.BERSERK, 3, 0);
        guerriero.addEffettoDiStato(TipoEffettoDiStato.STORDITO, 3, 0);
        guerriero.addEffettoDiStato(TipoEffettoDiStato.SANGUINAMENTO, 3, 0);
        Gruppo gruppo = new Gruppo() {
            @Override
            public boolean isGruppoGiocatore() {
                return false;
            }
        };
        gruppo.aggiungiPersonaggio(sacerdote);
        gruppo.aggiungiPersonaggio(guerriero);

        FabbricaAbilita.crea(TipoAbilita.ALBA_SACRA, sacerdote).usa(sacerdote, gruppo, GruppoAvversario.getIstanza());

        assertTrue(sacerdote.hasEffettoDiStato(TipoEffettoDiStato.ISPIRATO));
        assertFalse(sacerdote.hasEffettoDiStato(TipoEffettoDiStato.AVVELENATO));
        assertTrue(guerriero.hasEffettoDiStato(TipoEffettoDiStato.BERSERK));
        assertFalse(guerriero.hasEffettoDiStato(TipoEffettoDiStato.STORDITO));
        assertFalse(guerriero.hasEffettoDiStato(TipoEffettoDiStato.SANGUINAMENTO));
    }
}
