package com.threeamigos.foresta.tools;

import com.threeamigos.foresta.motore.modellodati.ArtefattoMD;
import com.threeamigos.foresta.motore.modellodati.TipoArtefatto;
import com.threeamigos.foresta.motore.modellodati.TipoAttributo;
import com.threeamigos.foresta.oggetti.Artefatto;

/**
 *
 * @author Stefano Reksten
 */
public class CostruttoreArtefattoImpl implements
        CostruttoreArtefatto.StepTipo,
        CostruttoreArtefatto.StepNome,
        CostruttoreArtefatto.StepDescrizione,
        CostruttoreArtefatto.StepLivello,
        CostruttoreArtefatto.StepCostoAcquisto,
        CostruttoreArtefatto.StepPeso,
        CostruttoreArtefatto.StepModificatore,
        CostruttoreArtefatto.StepModificatore2 {

    private final ArtefattoMD artefattoMD;

    CostruttoreArtefattoImpl() {
        artefattoMD = new ArtefattoMD();
    }

    @Override
    public CostruttoreArtefatto.StepNome setTipo(TipoArtefatto tipo) {
        artefattoMD.setTipo(tipo);
        return this;
    }

    @Override
    public CostruttoreArtefatto.StepDescrizione setNome(String nome) {
        artefattoMD.setNome(nome);
        return this;
    }

    @Override
    public CostruttoreArtefatto.StepLivello setDescrizione(String descrizione) {
        artefattoMD.setDescrizione(descrizione);
        return this;
    }

    @Override
    public CostruttoreArtefatto.StepCostoAcquisto setLivello(int livello) {
        artefattoMD.setLivello(livello);
        return this;
    }

    @Override
    public CostruttoreArtefatto.StepPeso setCostoAcquisto(int costoAcquisto) {
        artefattoMD.setCostoAcquisto(costoAcquisto);
        return this;
    }

    @Override
    public CostruttoreArtefatto.StepModificatore setPeso(int peso) {
        artefattoMD.setPeso(peso);
        return this;
    }

    @Override
    public CostruttoreArtefatto.StepModificatore2 setModificatore(TipoAttributo modificatore, int quantita) {
        artefattoMD.addModificatoreAttributo(modificatore, quantita);
        return this;
    }

    @Override
    public Artefatto costruisci() {
        return new Artefatto(artefattoMD);
    }
}
