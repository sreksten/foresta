package com.threeamigos.foresta.tools;

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

    interface StepTipo {
        StepNome setTipo(TipoArtefatto tipo);
    }
    interface StepNome {
        StepDescrizione setNome(String nome);
    }
    interface StepDescrizione {
        StepLivello setDescrizione(String descrizione);
    }
    interface StepLivello {
        StepDanniBase setLivello(int livello);
    }
    interface StepDanniBase {
        StepCostoAcquisto setDanniBase(int livello);

        StepPeso setCostoAcquisto(int costoAcquisto);
    }
    interface StepCostoAcquisto {
        StepPeso setCostoAcquisto(int costoAcquisto);
    }
    interface StepPeso {
        StepModificatore setPeso(int peso);
    }
    interface StepModificatore {
        StepModificatore2 setModificatore(TipoAttributo modificatore, int quantita);
    }
    interface StepModificatore2 {
        StepModificatore2 setModificatore(TipoAttributo modificatore, int quantita);
        Artefatto costruisci();
    }
}
