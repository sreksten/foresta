package com.threeamigos.foresta.combattimento;

import com.threeamigos.foresta.artefatti.Arma;
import com.threeamigos.foresta.tipi.CategoriaDanno;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.poteri.Meccanica;
import com.threeamigos.foresta.poteri.MeccanicaDeiPoteri;
import com.threeamigos.foresta.tipi.TipoIncantesimo;
import com.threeamigos.foresta.tipi.EsitoAzione;
import com.threeamigos.foresta.tipi.TipoAzione;

/**
 * La meccanica dello scontro con cui si esercitano i poteri: risponde alle richieste di {@link MeccanicaDeiPoteri} con il
 * calcolatore del combattimento e lo stato dello scontro.
 */
public final class MeccanicaDelloScontro implements MeccanicaDeiPoteri {

	/**
	 * Fa esercitare i poteri con questa meccanica.
	 */
	public static void registrati() {
		Meccanica.installa(new MeccanicaDelloScontro());
	}

	@Override
	public void annuncia(Personaggio attore, Personaggio bersaglio, TipoAzione azione) {
		AnnuncioDellAzione.azione(attore, bersaglio, azione);
	}

	@Override
	public boolean colpisce(Personaggio attaccante, Personaggio difensore, CategoriaDanno categoriaDanno) {
		return CalcolatoreCombattimento.colpisce(attaccante, difensore, categoriaDanno);
	}

	@Override
	public double quotaDelDannoPerPosizione(int posizione) {
		return CalcolatoreCombattimento.quotaDelDannoPerPosizione(posizione);
	}

	@Override
	public int calcolaDurataStato(Personaggio difensore, TipoEffettoDiStato stato) {
		return CalcolatoreCombattimento.calcolaDurataStato(difensore, stato);
	}

	@Override
	public void colpisciConMagia(Personaggio formulante, Personaggio bersaglio, Arma arma, TipoAzione azione,
			TipoIncantesimo immunitaIncantesimo, double quotaDelDanno) {
		ColpoMagico.colpisci(formulante, bersaglio, arma, azione, immunitaIncantesimo, quotaDelDanno);
	}

	@Override
	public void registraEsitoAzione(Personaggio attore, Personaggio bersaglio, TipoAzione azione, EsitoAzione esito) {
		StatoDelCombattimento.getIstanza().registraEsitoAzione(attore, bersaglio, azione, esito);
	}

	@Override
	public void registraIncantesimoLanciato(Personaggio personaggio) {
		StatoDelCombattimento.getIstanza().registraIncantesimoLanciato(personaggio);
	}

	@Override
	public void registraAlleato(Personaggio personaggio, Personaggio alleato) {
		StatoDelCombattimento.getIstanza().registraAlleato(personaggio, alleato);
	}
}
