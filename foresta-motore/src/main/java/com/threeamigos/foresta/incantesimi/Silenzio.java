package com.threeamigos.foresta.incantesimi;

import com.threeamigos.foresta.combattimento.CalcolatoreCombattimento;
import com.threeamigos.foresta.combattimento.StatoDelCombattimento;
import com.threeamigos.foresta.danni.tipi.TipoDanno;
import com.threeamigos.foresta.effettidistato.EffettoDiStatoMD;
import com.threeamigos.foresta.effettidistato.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.motore.Gruppo;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.strumenti.logger.Logger;
import com.threeamigos.foresta.tipi.TipoIncantesimo;
import com.threeamigos.foresta.tipi.tattiche.EsitoAzione;
import com.threeamigos.foresta.tipi.tattiche.TipoAzione;

import java.util.List;

/**
 * Applica {@code SILENZIATO} a un avversario, senza fargli danni. Su un personaggio silenzia quello; su un gruppo
 * (per ora il giocatore non può scegliere il bersaglio) sceglie {@link #scegliBersaglio}.
 */
public class Silenzio extends IncantesimoMaleficoImpl implements Incantesimo {

	public Silenzio(int livello) {
		super(livello);
	}

	public TipoIncantesimo getClasse() {
		return TipoIncantesimo.SILENZIO;
	}

	@Override
	public TipoDanno getTipoDanno() {
		return TipoDanno.ARCANO;
	}

	public int getDanni() {
		return 0;
	}

	/**
	 * Su un gruppo il bersaglio è uno solo, scelto da {@link #scegliBersaglio}
	 */
	@Override
	public void formula(Personaggio formulante, Personaggio personaggioBersaglio, Gruppo gruppoBersaglio) {
		if (personaggioBersaglio == null && gruppoBersaglio != null) {
			personaggioBersaglio = scegliBersaglio(gruppoBersaglio.getPersonaggiVivi());
			if (personaggioBersaglio == null) {
				return;
			}
			gruppoBersaglio = null;
		}
		super.formula(formulante, personaggioBersaglio, gruppoBersaglio);
	}

	/**
	 * Il primo personaggio vivo non ancora {@code SILENZIATO}; se lo sono tutti, quello il cui silenzio dura meno.
	 *
	 * @return il bersaglio, null se non ci sono personaggi vivi
	 */
	public static Personaggio scegliBersaglio(List<Personaggio> vivi) {
		Personaggio scelto = null;
		int durataScelto = Integer.MAX_VALUE;
		for (Personaggio candidato : vivi) {
			int durata = durataSilenzio(candidato);
			if (durata == 0) {
				return candidato;
			}
			if (durata < durataScelto) {
				scelto = candidato;
				durataScelto = durata;
			}
		}
		return scelto;
	}

	private static int durataSilenzio(Personaggio personaggio) {
		for (EffettoDiStatoMD effetto : personaggio.getEffettiDiStato()) {
			if (effetto.getTipoEffettoDiStato() == TipoEffettoDiStato.SILENZIATO) {
				return effetto.getDurata();
			}
		}
		return 0;
	}

	@Override
	protected void colpisci(Personaggio formulante, Personaggio bersaglio) {
		String nomeBersaglio = bersaglio.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE);
		StatoDelCombattimento stato = StatoDelCombattimento.getIstanza();
		if (bersaglio.isImmuneAIncantesimo(getClasse()) || bersaglio.isImmuneAEffetto(TipoEffettoDiStato.SILENZIATO)) {
			Logger.log("Bersaglio immune all'incantesimo");
			BusEventi.pubblica(new NotificaTestoFrase("L'incantesimo non ha effetto su " + nomeBersaglio + "."));
			stato.registraEsitoAzione(formulante, bersaglio, TipoAzione.SILENZIO, EsitoAzione.IMMUNE);
			return;
		}
		if (!CalcolatoreCombattimento.colpisce(formulante, bersaglio, getTipoDanno().getCategoriaDanno())) {
			Logger.log("L'incantesimo non va a segno");
			BusEventi.pubblica(new NotificaTestoFrase("L'incantesimo non va a segno su " + nomeBersaglio + "."));
			stato.registraEsitoAzione(formulante, bersaglio, TipoAzione.SILENZIO, EsitoAzione.MANCATA);
			return;
		}
		stato.registraEsitoAzione(formulante, bersaglio, TipoAzione.SILENZIO, EsitoAzione.RIUSCITA);
		bersaglio.addEffettoDiStato(TipoEffettoDiStato.SILENZIATO,
				CalcolatoreCombattimento.calcolaDurataStato(bersaglio, TipoEffettoDiStato.SILENZIATO), 0);
	}
}
