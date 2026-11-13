package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.CostantiPersonaggi;
import com.threeamigos.foresta.tipi.TipoRiposo;

/**
 * Calcola quanto recupera un personaggio riposando (un'ora nelle locazioni tranquille o una notte intera: vedi LocazioneBase e Pernottamento, nel motore): salute,
 * magia e stanchezza, in base alle ore e al tipo di riposo. Non cambia niente: il recupero lo applica SaluteEMorte.riposa.
 *
 * @author Stefano Reksten
 */
public class CalcolatoreRiposo {

    // I valori di bilanciamento stanno in CostantiPersonaggi (RIPOSO_*)

    /**
     * Il recupero di un personaggio che riposa quelle ore in quel modo. Il tempo conta con la radice delle ore (quattro ore valgono il doppio
     * di una), moltiplicata per il fattore del luogo; salute e magia dipendono dai massimi del personaggio e dai moltiplicatori della sua classe,
     * e chi non recupera salute o non si stanca (i non morti) salta quella parte.
     */
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
            double saluteGrezza = CostantiPersonaggi.RIPOSO_SALUTE_PARTE_FISSA + (personaggio.getSaluteMassima() * CostantiPersonaggi.RIPOSO_SALUTE_QUOTA_SALUTE_MASSIMA);
            saluteRigenerata = (int) Math.ceil(saluteGrezza * moltiplicatoreGlobale *
                    personaggio.getMoltiplicatoreRecuperoFisico());
        }

        // --- CALCOLO RECUPERO Magia ---
        double manaGrezzo = CostantiPersonaggi.RIPOSO_MAGIA_PARTE_FISSA + (personaggio.getMagiaMassima() * CostantiPersonaggi.RIPOSO_MAGIA_QUOTA_MAGIA_MASSIMA);
        manaRigenerato = (int) Math.ceil(manaGrezzo * moltiplicatoreGlobale *
                personaggio.getMoltiplicatoreRecuperoMagico());

        // --- CALCOLO ABBASSAMENTO STANCHEZZA ---
        if (personaggio.getMoltiplicatoreStanchezza() > 0.0) { // I non-morti ignorano la stanchezza
            // Più si riposa in un luogo comodo, più la stanchezza scende drasticamente
            double stanchezzaGrezza = CostantiPersonaggi.RIPOSO_RIDUZIONE_STANCHEZZA * moltiplicatoreGlobale;

            // Che la stanchezza non scenda sotto lo zero lo garantisce subStanchezza
            stanchezzaRimossa = (int) Math.ceil(stanchezzaGrezza);
        }

        return new RisultatoRiposo(ore, tipoRiposo, saluteRigenerata, manaRigenerato, stanchezzaRimossa);
    }
}
