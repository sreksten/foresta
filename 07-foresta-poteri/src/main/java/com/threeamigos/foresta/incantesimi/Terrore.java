package com.threeamigos.foresta.incantesimi;

import com.threeamigos.foresta.combattimento.CalcolatoreCombattimento;
import com.threeamigos.foresta.combattimento.StatoDelCombattimento;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.gruppi.Gruppo;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.EsitoAzione;
import com.threeamigos.foresta.tipi.TipoAzione;
import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.tipi.TipoIncantesimo;

import java.util.List;
import com.threeamigos.foresta.testi.Lingue;
import com.threeamigos.foresta.testi.Preposizione;
import com.threeamigos.foresta.testi.Testi;

/**
 * Un'ondata di paura su tutto il campo (incantesimo GLOBALE): ogni avversario vivo subisce una prova di spavento, con lo stesso tiro per colpire degli
 * altri incantesimi, e chi la fallisce diventa {@code SPAVENTATO}. Non fa danni; chi è immune all'incantesimo o allo spavento non ne è toccato.
 */
public class Terrore extends IncantesimoMaleficoImpl implements IncantesimoGlobale {

	public Terrore(int livello) {
		super(livello);
	}

	public TipoIncantesimo getClasse() {
		return TipoIncantesimo.TERRORE;
	}

	@Override
	public TipoDanno getTipoDanno() {
		return TipoDanno.PSICHICO;
	}

	public int getDanni() {
		return 0;
	}

	@Override
	protected void formulaGlobale(Personaggio formulante, List<Personaggio> bersagli) {
		BusEventi.pubblica(new NotificaTestoFrase(Testi.formatta("messaggi_poteri", "Terrore.1", formulante.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
				Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA))));
		StatoDelCombattimento stato = StatoDelCombattimento.getIstanza();
		for (Personaggio avversario : bersagli) {
			String nome = avversario.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE);
			if (avversario.isImmuneAIncantesimo(getClasse()) || avversario.isImmuneAEffetto(TipoEffettoDiStato.SPAVENTATO)) {
				BusEventi.pubblica(new NotificaTestoFrase(Testi.formatta("messaggi_poteri", "Terrore.2", Lingue.corrente().conPreposizione(Preposizione.SU, nome))));
				stato.registraEsitoAzione(formulante, avversario, TipoAzione.TERRORE, EsitoAzione.IMMUNE);
			} else if (!CalcolatoreCombattimento.colpisce(formulante, avversario, getTipoDanno().getCategoriaDanno())) {
				BusEventi.pubblica(new NotificaTestoFrase(Testi.formatta("messaggi_poteri", "Terrore.3", nome)));
				stato.registraEsitoAzione(formulante, avversario, TipoAzione.TERRORE, EsitoAzione.MANCATA);
			} else {
				stato.registraEsitoAzione(formulante, avversario, TipoAzione.TERRORE, EsitoAzione.RIUSCITA);
				avversario.addEffettoDiStato(TipoEffettoDiStato.SPAVENTATO,
						CalcolatoreCombattimento.calcolaDurataStato(avversario, TipoEffettoDiStato.SPAVENTATO), 0);
			}
		}
	}

	/**
	 * Serve se c'è un avversario in campo che non è già spaventato e che non è immune al terrore
	 */
	@Override
	public boolean serve(List<Personaggio> alleati, List<Personaggio> avversari) {
		for (Personaggio avversario : avversari) {
			if (!avversario.isFuoriCombattimento() && !avversario.hasEffettoDiStato(TipoEffettoDiStato.SPAVENTATO)
					&& !avversario.isImmuneAIncantesimo(TipoIncantesimo.TERRORE) && !avversario.isImmuneAEffetto(TipoEffettoDiStato.SPAVENTATO)) {
				return true;
			}
		}
		return false;
	}
}
