package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.modellodati.IncantamentoMD;
import com.threeamigos.foresta.modificatori.ModificatoreAttributoMD;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.attributi.tipi.TipoAttributo;
import com.threeamigos.foresta.danni.tipi.TipoDanno;
import com.threeamigos.foresta.modificatori.tipi.TipoModificatore;

/**
 * Il costruttore a passi di un artefatto (step builder): il compilatore impone l'ordine in cui si danno i dati. Si parte da
 * {@link #istanza()} e si danno nell'ordine il tipo, il nome, la descrizione, il livello, i danni base (solo per le armi), il costo
 * di acquisto e il peso; poi si aggiungono a piacere modificatori e incantamenti e si chiude con {@code costruisci()}.
 *
 * @author Stefano Reksten
 */
public interface CostruttoreArtefatto {

    /**
     * Comincia a costruire un artefatto.
     */
    static StepTipo istanza() {
        return new CostruttoreArtefattoImpl();
    }

    /**
     * Primo passo: il tipo.
     */
    interface StepTipo {
        StepNome setTipo(TipoArtefatto tipo);
    }
    /**
     * Il nome.
     */
    interface StepNome {
        StepDescrizione setNome(String nome);
    }
    /**
     * La descrizione.
     */
    interface StepDescrizione {
        StepLivello setDescrizione(String descrizione);
    }
    /**
     * Il livello.
     */
    interface StepLivello {
        StepDanniBase setLivello(int livello);
    }
    /**
     * I danni base (per le armi), o direttamente il costo.
     */
    interface StepDanniBase {
        StepCostoAcquisto setDanniBase(int livello);

        StepPeso setCostoAcquisto(int costoAcquisto);
    }
    /**
     * Il costo di acquisto.
     */
    interface StepCostoAcquisto {
        StepPeso setCostoAcquisto(int costoAcquisto);
    }
    /**
     * Il peso.
     */
    interface StepPeso {
        StepModificatore setPeso(double peso);
    }
    /**
     * Modificatori e incantamenti, in numero a piacere, e infine {@code costruisci()}.
     */
    interface StepModificatore {
        StepModificatore setModificatore(ModificatoreAttributoMD modificatore);
        StepModificatore setModificatore(TipoAttributo modificatore, TipoModificatore tipoModificatore, double quantita, String nota);
        StepModificatore setModificatore(TipoAttributo modificatore, TipoModificatore tipoModificatore, double quantita);
        StepIncantamento setIncantamento(IncantamentoMD incantamento);
        StepIncantamento setIncantamento(String nomeIncantamento, TipoDanno tipoDannoElementale, int dannoBonusFisso, double coefficienteScala);
        Artefatto costruisci();
    }
    /**
     * Altri incantamenti, e infine {@code costruisci()}.
     */
    interface StepIncantamento {
        StepIncantamento setIncantamento(IncantamentoMD incantamento);
        StepIncantamento setIncantamento(String nomeIncantamento, TipoDanno tipoDannoElementale, int dannoBonusFisso, double coefficienteScala);
        Artefatto costruisci();
    }
}
