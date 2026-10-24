package com.threeamigos.foresta.incantesimi;

import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.motore.Gruppo;
import com.threeamigos.foresta.combattimento.StatoDelCombattimento;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.effettidistato.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * L'Alba sacra toglie gli effetti di stato negativi al gruppo e lascia quelli benefici (ispirazione, berserk).
 */
class AlbaSacraTest {

    @Test
    void togliGliStatiNegativiELasciaQuelliBenefici() {
        ModelloDati.setIstanza(new ModelloDati());
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

        new AlbaSacra(3).formula(sacerdote, null, gruppo);

        assertTrue(sacerdote.hasEffettoDiStato(TipoEffettoDiStato.ISPIRATO));
        assertFalse(sacerdote.hasEffettoDiStato(TipoEffettoDiStato.AVVELENATO));
        assertTrue(guerriero.hasEffettoDiStato(TipoEffettoDiStato.BERSERK));
        assertFalse(guerriero.hasEffettoDiStato(TipoEffettoDiStato.STORDITO));
        assertFalse(guerriero.hasEffettoDiStato(TipoEffettoDiStato.SANGUINAMENTO));
    }
}
