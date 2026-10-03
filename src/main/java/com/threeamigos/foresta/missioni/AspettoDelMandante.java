package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.intermezzi.ScenaInCitta;

import java.util.function.Supplier;

/**
 * Chi compare nella scena in città quando un incarico si offre: un mandante qualsiasi (con l'aspetto del
 * locandiere), il capitano delle guardie, l'armaiolo, l'alchimista o il locandiere. Lo sceglie la riga della
 * grammatica dell'incarico (campo ASPETTO).
 */
enum AspettoDelMandante {

	QUALUNQUE(ScenaInCitta::conMandante),
	CAPITANO(ScenaInCitta::conCapitano),
	ARMAIOLO(ScenaInCitta::conArmaiolo),
	ALCHIMISTA(ScenaInCitta::conAlchimista),
	LOCANDIERE(ScenaInCitta::conLocandiere);

	private final Supplier<ScenaInCitta> scena;

	AspettoDelMandante(Supplier<ScenaInCitta> scena) {
		this.scena = scena;
	}

	ScenaInCitta nuovaScena() {
		return scena.get();
	}
}
