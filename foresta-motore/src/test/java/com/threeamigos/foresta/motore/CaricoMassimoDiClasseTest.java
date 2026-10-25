package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.attributi.tipi.TipoAttributo;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.mappa.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Il carico massimo viene da forza e costituzione, per il moltiplicatore di classe.
 */
class CaricoMassimoDiClasseTest {

    private static int attesoPer(Personaggio p) {
        double forza = p.getModelloDati().get(TipoAttributo.FORZA);
        double costituzione = p.getModelloDati().get(TipoAttributo.COSTITUZIONE);
        return (int) ((12.0 * Math.sqrt(forza) + 6.0 * Math.sqrt(costituzione)) * p.getMoltiplicatoreCarico());
    }

    @Test
    void ilCaricoMassimoSegueIlMoltiplicatoreDiClasse() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(300)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            for (TipoPersonaggio classe : new TipoPersonaggio[]{TipoPersonaggio.GUERRIERO, TipoPersonaggio.MAGA, TipoPersonaggio.GOBLIN,
                    TipoPersonaggio.DRAGO}) {
                Personaggio p = FabbricaPersonaggi.crea(classe, 3);
                assertEquals(attesoPer(p), p.getCaricoMassimo(), classe.name());
            }
            Personaggio goblin = FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 3);
            Personaggio drago = FabbricaPersonaggi.crea(TipoPersonaggio.DRAGO, 3);
            assertTrue(drago.getCaricoMassimo() > goblin.getCaricoMassimo());
        }
    }
}
