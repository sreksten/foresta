package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.testi.GrammarBean;
import com.threeamigos.foresta.testi.GrammaticaPigra;
import com.threeamigos.foresta.testi.ProduttoreDiMissioniCasuale;
import com.threeamigos.foresta.tipi.TipoModalitaProduzioneTesti;

import java.lang.reflect.Field;

/**
 * Per i test che dipendono dalle estrazioni della grammatica delle missioni: finché è aperta, la grammatica sceglie sempre la prima
 * alternativa invece di estrarre a caso (vedi {@link GrammarBean#setProductionMode}); alla chiusura torna com'era. Il gioco non lo sa:
 * la grammatica la raggiunge il test, per riflessione.
 */
final class ProduzioneFissaDelleMissioni implements AutoCloseable {

    private final GrammarBean grammatica;
    private final TipoModalitaProduzioneTesti modalitaPrecedente;

    private ProduzioneFissaDelleMissioni(TipoModalitaProduzioneTesti modalita) {
        try {
            Field campo = ProduttoreDiMissioniCasuale.class.getDeclaredField("MISSIONI");
            campo.setAccessible(true);
            grammatica = ((GrammaticaPigra) campo.get(null)).get();
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
        modalitaPrecedente = grammatica.getProductionMode();
        grammatica.setProductionMode(modalita);
    }

    /** La prima alternativa di ogni produzione, finché non si chiude. */
    static ProduzioneFissaDelleMissioni sempreLaPrima() {
        return new ProduzioneFissaDelleMissioni(TipoModalitaProduzioneTesti.PRIMO);
    }

    /** L'ultima alternativa di ogni produzione, finché non si chiude. */
    static ProduzioneFissaDelleMissioni sempreLUltima() {
        return new ProduzioneFissaDelleMissioni(TipoModalitaProduzioneTesti.ULTIMO);
    }

    @Override
    public void close() {
        grammatica.setProductionMode(modalitaPrecedente);
    }
}
