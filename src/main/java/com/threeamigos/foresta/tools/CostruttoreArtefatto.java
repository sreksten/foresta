package com.threeamigos.foresta.tools;

import com.threeamigos.foresta.motore.modellodati.ModificatoreAttributo;
import com.threeamigos.foresta.motore.modellodati.TipoArtefatto;
import com.threeamigos.foresta.motore.modellodati.TipoAttributo;
import com.threeamigos.foresta.oggetti.Artefatto;

/**
 *
 * @author Stefano Reksten
 */
public interface CostruttoreArtefatto {

    static StepTipo istanza() {
        return new CostruttoreArtefattoImpl();
    }

    public interface StepTipo {
        StepNome setTipo(TipoArtefatto tipo);
    }
    public interface StepNome {
        StepDescrizione setNome(String nome);
    }
    public interface StepDescrizione {
        StepLivello setDescrizione(String descrizione);
    }
    public interface StepLivello {
        StepCostoAcquisto setLivello(int livello);
    }
    public interface StepCostoAcquisto {
        StepPeso setCostoAcquisto(int costoAcquisto);
    }
    public interface StepPeso {
        StepModificatore setPeso(int peso);
    }
    public interface StepModificatore {
        StepModificatore2 setModificatore(TipoAttributo modificatore, int quantita);
    }
    public interface StepModificatore2 {
        StepModificatore2 setModificatore(TipoAttributo modificatore, int quantita);
        Artefatto costruisci();
    }
}
