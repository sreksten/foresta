package com.threeamigos.foresta.ui.schermate;

import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.util.List;

/**
 * Le due colonne del bestiario (vedi DisplayableCanvasBestiario) sono descritte da {@link ContenutiDelBestiario}, nel modulo comune; qui restano i nomi che
 * gli altri file e i test di Swing usano.
 */
final class ImmagineBestiario {

	static final int ALTEZZA_RIGA_ELENCO = ContenutiDelBestiario.ALTEZZA_RIGA_ELENCO;

	private ImmagineBestiario() {
	}

	static int indiceDelSeparatore(List<TipoPersonaggio> voci) {
		return ContenutiDelBestiario.indiceDelSeparatore(voci);
	}

	static int rigaDellaVoce(List<TipoPersonaggio> voci, int voce) {
		return ContenutiDelBestiario.rigaDellaVoce(voci, voce);
	}

	static int voceDellaRiga(List<TipoPersonaggio> voci, int riga) {
		return ContenutiDelBestiario.voceDellaRiga(voci, riga);
	}
}
