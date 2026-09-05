package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.motore.modellodati.TipoRiposo;
import com.threeamigos.foresta.personaggi.Personaggio;

/**
 *
 * @author Stefano Reksten
 */
public class CalcolatoreRiposo {

    private static final double BASE_RECUPERO_SALUTE = 5.0;
    private static final double PERCENTUALE_SALUTE_MAX = 0.05;

    private static final double BASE_RECUPERO_MANA = 4.0;
    private static final double PERCENTUALE_MANA_MAX = 0.05;

    private static final double BASE_RIDUZIONE_STANCHEZZA = 2.0;

    public static RisultatoRiposo calcolaRiposo(Personaggio personaggio, int ore, TipoRiposo tipoRiposo) {

        // 1. Calcolo del coefficiente non lineare del tempo (es. 1 ora = 1.0, 4 ore = 2.0)
        double fattoreTempo = Math.sqrt(ore);
        double fattoreAmbiente = tipoRiposo.getMoltiplicatore();
        double moltiplicatoreGlobale = fattoreTempo * fattoreAmbiente;

        int saluteRigenerata = 0;
        int manaRigenerato;
        int stanchezzaRimossa = 0;

        // --- CALCOLO RECUPERO SALUTE ---
        if (personaggio.getMoltiplicatoreRecuperoFisico() > 0.0) { // Salta se è un non-morto/spettro
            double saluteGrezza = BASE_RECUPERO_SALUTE + (personaggio.getSaluteMassima() * PERCENTUALE_SALUTE_MAX);
            saluteRigenerata = (int) Math.ceil(saluteGrezza * moltiplicatoreGlobale *
                    personaggio.getMoltiplicatoreRecuperoFisico());
        }

        // --- CALCOLO RECUPERO MAGIA ---
        double manaGrezzo = BASE_RECUPERO_MANA + (personaggio.getMagiaMassima() * PERCENTUALE_MANA_MAX);
        manaRigenerato = (int) Math.ceil(manaGrezzo * moltiplicatoreGlobale *
                personaggio.getMoltiplicatoreRecuperoMagico());

        // --- CALCOLO ABBASSAMENTO STANCHEZZA ---
        if (personaggio.getMoltiplicatoreStanchezza() > 0.0) { // I non-morti ignorano la stanchezza
            // Più si riposa in un luogo comodo, più la stanchezza scende drasticamente
            double stanchezzaGrezza = BASE_RIDUZIONE_STANCHEZZA * moltiplicatoreGlobale;

            // La stanchezza non può scendere sotto lo zero
            stanchezzaRimossa = (int) Math.ceil(stanchezzaGrezza);
        }

        return new RisultatoRiposo(ore, tipoRiposo, saluteRigenerata, manaRigenerato, stanchezzaRimossa);
    }
}
