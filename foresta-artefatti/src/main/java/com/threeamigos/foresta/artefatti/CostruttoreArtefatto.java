package com.threeamigos.foresta.artefatti;

import com.threeamigos.foresta.artefatti.tipi.TipoArtefatto;
import com.threeamigos.foresta.attributi.tipi.TipoAttributo;
import com.threeamigos.foresta.danni.tipi.TipoDanno;
import com.threeamigos.foresta.modificatori.ModificatoreAttributoMD;
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
        /**
         * Il tipo dell'artefatto.
         */
        StepNome setTipo(TipoArtefatto tipo);
    }
    /**
     * Il nome.
     */
    interface StepNome {
        /**
         * Il nome, con l'articolo.
         */
        StepDescrizione setNome(String nome);
    }
    /**
     * La descrizione.
     */
    interface StepDescrizione {
        /**
         * La descrizione, "che ...".
         */
        StepLivello setDescrizione(String descrizione);
    }
    /**
     * Il livello.
     */
    interface StepLivello {
        /**
         * Il livello dell'artefatto.
         */
        StepDanniBase setLivello(int livello);
    }
    /**
     * I danni base (per le armi), o direttamente il costo.
     */
    interface StepDanniBase {
        /**
         * I danni base (solo per le armi).
         */
        StepCostoAcquisto setDanniBase(int livello);

        /**
         * Il prezzo d'acquisto.
         */
        StepPeso setCostoAcquisto(int costoAcquisto);
    }
    /**
     * Il costo di acquisto.
     */
    interface StepCostoAcquisto {
        /**
         * Il prezzo d'acquisto.
         */
        StepPeso setCostoAcquisto(int costoAcquisto);
    }
    /**
     * Il peso.
     */
    interface StepPeso {
        /**
         * Il peso.
         */
        StepModificatore setPeso(double peso);
    }
    /**
     * Modificatori e incantamenti, in numero a piacere, e infine {@code costruisci()}.
     */
    interface StepModificatore {
        /**
         * Aggiunge un modificatore già costruito.
         */
        StepModificatore setModificatore(ModificatoreAttributoMD modificatore);
        /**
         * Aggiunge un modificatore permanente con una nota.
         */
        StepModificatore setModificatore(TipoAttributo modificatore, TipoModificatore tipoModificatore, double quantita, String nota);
        /**
         * Aggiunge un modificatore permanente senza note.
         */
        StepModificatore setModificatore(TipoAttributo modificatore, TipoModificatore tipoModificatore, double quantita);
        /**
         * Aggiunge un incantamento già costruito.
         */
        StepIncantamento setIncantamento(IncantamentoMD incantamento);
        /**
         * Aggiunge un incantamento dai suoi dati.
         */
        StepIncantamento setIncantamento(String nomeIncantamento, TipoDanno tipoDannoElementale, int dannoBonusFisso, double coefficienteScala);
        /**
         * Costruisce l'artefatto.
         */
        Artefatto costruisci();
    }
    /**
     * Altri incantamenti, e infine {@code costruisci()}.
     */
    interface StepIncantamento {
        /**
         * Aggiunge un altro incantamento già costruito.
         */
        StepIncantamento setIncantamento(IncantamentoMD incantamento);
        /**
         * Aggiunge un altro incantamento dai suoi dati.
         */
        StepIncantamento setIncantamento(String nomeIncantamento, TipoDanno tipoDannoElementale, int dannoBonusFisso, double coefficienteScala);
        /**
         * Costruisce l'artefatto.
         */
        Artefatto costruisci();
    }
}
