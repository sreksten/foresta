package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.motore.modellodati.ArtefattoMD;
import com.threeamigos.foresta.motore.modellodati.ModificatoreAttributo;
import com.threeamigos.foresta.motore.modellodati.TipoArtefatto;

/**
 * Stampa qualche spada come la genera il gioco, metà dalle tabelle e metà da artefatti2.txt,
 * per vedere a colpo d'occhio che cosa esce.
 */
public class TestArtefatti2 {

    public static void main(String[] args) {
        GeneratoreArtefatti generatore = GeneratoreArtefatti.istanza();
        for (int livello = 1; livello <= 10; livello++) {
            for (int i = 0; i < 4; i++) {
                stampa(generatore.generaArtefatto(TipoArtefatto.SPADA, livello));
            }
        }
    }

    private static void stampa(Artefatto artefatto) {
        ArtefattoMD md = artefatto.getModelloDati();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("liv %2d %-5s %3d mo, danni %2d: ", md.getLivello(), md.getRarita().getNome(),
                md.getCostoAcquisto(), md.getDanni()));
        sb.append(artefatto.getNomeCompleto());
        for (ModificatoreAttributo modificatore : md.getModificatori()) {
            sb.append(String.format(" [%s %+.0f]", modificatore.getTipoAttributo().getNome(), modificatore.getQuantita()));
        }
        for (Incantamento incantamento : md.getIncantamenti()) {
            sb.append(" [").append(incantamento.getNomeIncantamento()).append(']');
        }
        System.out.println(sb);
    }
}
