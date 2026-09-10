package com.threeamigos.foresta.tools;

import com.threeamigos.foresta.motore.modellodati.*;
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
        CostruttoreArtefatto.StepDanniBase,
        CostruttoreArtefatto.StepCostoAcquisto,
        CostruttoreArtefatto.StepPeso,
        CostruttoreArtefatto.StepModificatore {

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
    public CostruttoreArtefatto.StepDanniBase setLivello(int livello) {
        artefattoMD.setLivello(livello);
        return this;
    }

    @Override
    public CostruttoreArtefatto.StepCostoAcquisto setDanniBase(int livello) {
        artefattoMD.setDanni(livello);
        return this;
    }

    @Override
    public CostruttoreArtefatto.StepPeso setCostoAcquisto(int costoAcquisto) {
        artefattoMD.setCostoAcquisto(costoAcquisto);
        return this;
    }

    @Override
    public CostruttoreArtefatto.StepModificatore setPeso(double peso) {
        artefattoMD.setPeso(peso);
        return this;
    }

    @Override
    public CostruttoreArtefatto.StepModificatore setModificatore(ModificatoreAttributo modificatore) {
        artefattoMD.addModificatore(modificatore);
        return this;
    }

    @Override
    public CostruttoreArtefatto.StepModificatore setModificatore(TipoAttributo modificatore, TipoModificatore tipoModificatore, double quantita, String nota) {
        artefattoMD.addModificatore(modificatore, tipoModificatore, quantita, nota);
        return this;
    }

    @Override
    public CostruttoreArtefatto.StepModificatore setModificatore(TipoAttributo modificatore, TipoModificatore tipoModificatore, double quantita) {
        artefattoMD.addModificatore(modificatore, tipoModificatore, quantita, "");
        return this;
    }

    @Override
    public Artefatto costruisci() {
        return Artefatto.di(artefattoMD);
    }
}
