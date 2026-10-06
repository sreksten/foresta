package com.threeamigos.foresta.strumenti;

import com.threeamigos.foresta.modellodati.IncantamentoMD;
import com.threeamigos.foresta.modellodati.ModificatoreAttributo;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.tipi.TipoModificatore;

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
        StepModificatore setPeso(double peso);
    }
    interface StepModificatore {
        StepModificatore setModificatore(ModificatoreAttributo modificatore);
        StepModificatore setModificatore(TipoAttributo modificatore, TipoModificatore tipoModificatore, double quantita, String nota);
        StepModificatore setModificatore(TipoAttributo modificatore, TipoModificatore tipoModificatore, double quantita);
        StepIncantamento setIncantamento(IncantamentoMD incantamento);
        StepIncantamento setIncantamento(String nomeIncantamento, TipoDanno tipoDannoElementale, int dannoBonusFisso, double coefficienteScala);
        Artefatto costruisci();
    }
    interface StepIncantamento {
        StepIncantamento setIncantamento(IncantamentoMD incantamento);
        StepIncantamento setIncantamento(String nomeIncantamento, TipoDanno tipoDannoElementale, int dannoBonusFisso, double coefficienteScala);
        Artefatto costruisci();
    }
}
