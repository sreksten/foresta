package com.threeamigos.foresta.intermezzi;

import com.threeamigos.foresta.missioni.IntermezziDelleMissioni;
import com.threeamigos.foresta.tipi.NegozioInScena;
import com.threeamigos.foresta.tipi.TipoIntermezzo;

/**
 * Gli intermezzi che il modulo degli intermezzi non conosce, perché hanno bisogno di chi sta sopra: gli epiloghi delle missioni
 * (l'eremita rompiscatole, l'urlo della Banshee), quelli che guardano l'ora o il livello del mondo (le notti nei negozi, la spada
 * nascosta), e gli intermezzi dei passi delle missioni.
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
		FabbricaIntermezzi.registra(TipoIntermezzo.INTERMEZZO_SPADA_NASCOSTA,
				tipo -> new IntermezzoSpadaNascosta(tipo, FabbricaIntermezzi.SALUTO_ALCHIMISTA));
		FabbricaIntermezzi.registra(TipoIntermezzo.INTERMEZZO_EREMITA_ROMPISCATOLE, tipo -> new IntermezzoEremitaRompiscatole());
		FabbricaIntermezzi.registra(TipoIntermezzo.INTERMEZZO_NOTTE_ARMAIOLO, tipo -> new IntermezzoNonChiudeteMai(tipo, NegozioInScena.ARMAIOLO));
		FabbricaIntermezzi.registra(TipoIntermezzo.INTERMEZZO_NOTTE_ALCHIMISTA, tipo -> new IntermezzoNonChiudeteMai(tipo, NegozioInScena.ALCHIMISTA));
		FabbricaIntermezzi.registra(TipoIntermezzo.INTERMEZZO_NOTTE_VENDITORE_DI_PERGAMENE,
				tipo -> new IntermezzoNonChiudeteMai(tipo, NegozioInScena.VENDITORE_DI_PERGAMENE));
		FabbricaIntermezzi.registra(TipoIntermezzo.INTERMEZZO_NOTTE_INCANTATORE, tipo -> new IntermezzoNonChiudeteMai(tipo, NegozioInScena.INCANTATORE));
		FabbricaIntermezzi.registra(TipoIntermezzo.INTERMEZZO_NOTTE_LOCANDA, tipo -> new IntermezzoNonChiudeteMai(tipo, NegozioInScena.LOCANDA));
		RegistroIntermezzi.aggiungiFonte(IntermezziDelleMissioni.ISTANZA);
	}
}
