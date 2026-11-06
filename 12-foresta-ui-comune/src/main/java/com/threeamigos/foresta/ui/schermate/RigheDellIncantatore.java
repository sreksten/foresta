package com.threeamigos.foresta.ui.schermate;

import com.threeamigos.foresta.artefatti.VistaArtefatto;
import com.threeamigos.foresta.artefatti.VistaBancoDiLavoro;
import com.threeamigos.foresta.ui.testo.TestiDellaUI;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Le righe che la colonna centrale della bottega dell'incantatore ha sotto le monete: il costo della fusione (se sul banco ci sono ingredienti) e, se c'è un
 * artefatto sul banco, il livello che avrebbe dopo la fusione (se cambia) e quanti effetti avrebbe sul massimo. Ogni riga è {etichetta, valore}, come le
 * vuole {@link SchermataDiScambio#colonnaDelNegoziante(com.threeamigos.foresta.tipi.TipoPersonaggio, List)}.
 */
public final class RigheDellIncantatore {

	private RigheDellIncantatore() {
	}

	public static List<String[]> righe(VistaBancoDiLavoro banco) {
		List<String[]> righe = new ArrayList<>();
		if (banco.getInventarioParteRemota().stream().anyMatch(a -> a.getTipo().isIngrediente())) {
			righe.add(new String[]{TestiDellaUI.get("COSTO_FUSIONE"), String.valueOf(banco.getCostoFusione())});
		}
		Optional<? extends VistaArtefatto> artefatto = banco.getArtefattoSulBanco();
		if (artefatto.isPresent()) {
			if (banco.getLivelloDopoLaFusione() != artefatto.get().getLivello()) {
				righe.add(new String[]{TestiDellaUI.get("NUOVO_LIVELLO"), String.valueOf(banco.getLivelloDopoLaFusione())});
			}
			righe.add(new String[]{TestiDellaUI.get("EFFETTI"), banco.getEffettiDopoLaFusione() + "/" + banco.getEffettiMassimiDopoLaFusione()});
		}
		return righe;
	}
}
