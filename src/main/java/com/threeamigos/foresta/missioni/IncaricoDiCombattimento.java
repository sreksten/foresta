package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.tipi.ClasseMissione;

/**
 * In città qualcuno vuole sconfitto qualcosa che si nasconde nella foresta: una riga pescata a caso da
 * INCARICO_DI_COMBATTIMENTO in missioni.txt (vedi IncaricoDiCombattimentoBase per come si svolge). Si prende in una
 * città qualsiasi e si ripete con altri; le storie delle città (il medaglione, le derrate) sono invece sottoclassi
 * di IncaricoDiCombattimentoBase.
 */
public class IncaricoDiCombattimento extends IncaricoDiCombattimentoBase {

	public IncaricoDiCombattimento() {
		super(ClasseMissione.INCARICO_DI_COMBATTIMENTO, "INCARICO_DI_COMBATTIMENTO");
	}
}
