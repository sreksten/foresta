package com.threeamigos.foresta.combattimento;

import com.threeamigos.foresta.artefatti.Arma;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.strumenti.logger.Logger;
import com.threeamigos.foresta.tipi.TipoIncantesimo;
import com.threeamigos.foresta.tipi.tattiche.EsitoAzione;
import com.threeamigos.foresta.tipi.tattiche.TipoAzione;

/**
 * Un colpo magico su un bersaglio, per un incantesimo di danno o per un'abilità di danno: se il bersaglio è immune non succede niente,
 * altrimenti si tira per colpire come per le armi e, se va a segno, il danno risultante si applica al bersaglio. L'esito viene registrato
 * nello stato del combattimento.
 */
public final class ColpoMagico {

	private ColpoMagico() {
	}

	/**
	 * @param arma                l'attacco magico, per calcolare il danno
	 * @param azione              l'azione con cui si registra l'esito per le tattiche
	 * @param immunitaIncantesimo il tipo di incantesimo di cui il bersaglio può essere immune, o null se l'attacco non ha immunità
	 * @param quotaDelDanno       la quota del danno che il bersaglio subisce (vedi CalcolatoreCombattimento.quotaDelDannoPerPosizione)
	 */
	public static void colpisci(Personaggio formulante, Personaggio bersaglio, Arma arma, TipoAzione azione,
								TipoIncantesimo immunitaIncantesimo, double quotaDelDanno) {
		StatoDelCombattimento stato = StatoDelCombattimento.getIstanza();
		if (immunitaIncantesimo != null && bersaglio.isImmuneAIncantesimo(immunitaIncantesimo)) {
			Logger.log("Bersaglio immune all'incantesimo");
			stato.registraEsitoAzione(formulante, bersaglio, azione, EsitoAzione.IMMUNE);
			return;
		}
		if (!CalcolatoreCombattimento.colpisce(formulante, bersaglio, arma.getTipoDanno().getCategoriaDanno())) {
			Logger.log("L'incantesimo non va a segno");
			stato.registraEsitoAzione(formulante, bersaglio, azione, EsitoAzione.MANCATA);
			return;
		}
		stato.registraEsitoAzione(formulante, bersaglio, azione, EsitoAzione.RIUSCITA);
		DannoRisultante risultato = CalcolatoreCombattimento.calcolaDannoRisultante(formulante, bersaglio, arma, quotaDelDanno);
		ApplicazioneDelRisultato.applica(bersaglio, risultato);
	}
}
