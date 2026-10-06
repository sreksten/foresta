package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.tipi.ClasseMissione;
import com.threeamigos.foresta.tipi.TipoLocazione;

/**
 * La storia di Fleena: un uomo chiede di recuperare il medaglione che una banda di ladri gli ha rubato e nascosto in
 * una grotta. È un incarico di combattimento (vedi IncaricoDiCombattimentoBase) con la città fissa, e tutto il resto, dai
 * nemici ai testi, sta nella produzione RECUPERA_IL_MEDAGLIONE di missioni.txt.
 */
public class RecuperaIlMedaglione extends IncaricoDiCombattimentoBase implements Missione {

	public RecuperaIlMedaglione() {
		super(ClasseMissione.RECUPERA_IL_MEDAGLIONE, "RECUPERA_IL_MEDAGLIONE");
	}

	@Override
	protected TipoLocazione getCittaFissa() {
		return TipoLocazione.CITTA_FLEENA;
	}
}
