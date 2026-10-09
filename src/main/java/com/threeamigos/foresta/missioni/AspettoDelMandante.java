package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.intermezzi.ScenaInCitta;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.util.function.Supplier;

/**
 * Chi compare nella scena in città quando un incarico si offre. Lo sceglie la riga della grammatica dell'incarico
 * (campo ASPETTO): un personaggio, se il mandante ne ha uno, altrimenti QUALUNQUE (un uomo, con l'aspetto del
 * locandiere) o DONNA (con quello della moglie del bardo).
 */
enum AspettoDelMandante {

	QUALUNQUE(ScenaInCitta::conMandante),
	DONNA(ScenaInCitta::conMandantessa),
	CAPITANO(TipoPersonaggio.CAPITANO_DELLE_GUARDIE),
	ARMAIOLO(TipoPersonaggio.ARMAIOLO),
	ALCHIMISTA(TipoPersonaggio.ALCHIMISTA),
	LOCANDIERE(TipoPersonaggio.LOCANDIERE),
	VENDITORE_DI_PERGAMENE(TipoPersonaggio.VENDITORE_DI_PERGAMENE),
	INCANTATORE(TipoPersonaggio.INCANTATORE),
	SACERDOTE(TipoPersonaggio.SACERDOTE),
	SACERDOTESSA(TipoPersonaggio.SACERDOTESSA),
	MAGO(TipoPersonaggio.MAGO),
	MAGA(TipoPersonaggio.MAGA),
	GUERRIERO(TipoPersonaggio.GUERRIERO),
	LADRO(TipoPersonaggio.LADRO);

	private final Supplier<ScenaInCitta> scena;

	AspettoDelMandante(TipoPersonaggio classe) {
		this(() -> ScenaInCitta.con(classe));
	}

	AspettoDelMandante(Supplier<ScenaInCitta> scena) {
		this.scena = scena;
	}

	ScenaInCitta nuovaScena() {
		return scena.get();
	}
}
