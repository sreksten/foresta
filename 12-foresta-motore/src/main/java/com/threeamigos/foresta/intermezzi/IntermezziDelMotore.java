package com.threeamigos.foresta.intermezzi;

import com.threeamigos.foresta.missioni.IntermezziDelleMissioni;
import com.threeamigos.foresta.tipi.TipoIntermezzo;

/**
 * Gli intermezzi che il modulo degli intermezzi non conosce, perché hanno bisogno delle missioni: i loro epiloghi (l'eremita
 * rompiscatole, l'urlo della Banshee) e gli intermezzi dei loro passi.
 *
 * @author Stefano Reksten
 */
public final class IntermezziDelMotore {

	private IntermezziDelMotore() {
	}

	/**
	 * Dice alla fabbrica e al registro degli intermezzi come costruirli e dove trovarli.
	 */
	public static void registra() {
		FabbricaIntermezzi.registra(TipoIntermezzo.INTERMEZZO_URLO_DELLA_BANSHEE, tipo -> new IntermezzoUrloDellaBanshee());
		FabbricaIntermezzi.registra(TipoIntermezzo.INTERMEZZO_EREMITA_ROMPISCATOLE, tipo -> new IntermezzoEremitaRompiscatole());
		RegistroIntermezzi.aggiungiFonte(IntermezziDelleMissioni.ISTANZA);
	}
}
