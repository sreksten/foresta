package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.tipi.ClasseMissione;
import com.threeamigos.foresta.tipi.TipoLocazione;

/**
 * La storia di Ruuna: il Borgomastro chiede di recuperare un carico di derrate alimentari che una banda di troll ha
 * nascosto in alcune rovine. È un incarico di combattimento (vedi IncaricoDiCombattimentoBase) con la città fissa, e
 * tutto il resto, dai nemici ai testi, sta nella produzione RECUPERA_LE_DERRATE_ALIMENTARI di missioni.txt.
 */
public class RecuperaLeDerrateAlimentari extends IncaricoDiCombattimentoBase implements Missione {

	public RecuperaLeDerrateAlimentari() {
		super(ClasseMissione.RECUPERA_LE_DERRATE_ALIMENTARI, "RECUPERA_LE_DERRATE_ALIMENTARI");
	}

	@Override
	protected TipoLocazione getCittaFissa() {
		return TipoLocazione.CITTA_RUUNA;
	}
}
