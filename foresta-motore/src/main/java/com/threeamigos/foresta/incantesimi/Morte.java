package com.threeamigos.foresta.incantesimi;

import com.threeamigos.foresta.tipi.CostantiIncantesimi;
import com.threeamigos.foresta.combattimento.CalcolatoreCombattimento;
import com.threeamigos.foresta.combattimento.StatoDelCombattimento;
import com.threeamigos.foresta.danni.tipi.TipoDanno;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.motore.Gruppo;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.Costanti;
import com.threeamigos.foresta.tipi.TipoIncantesimo;
import com.threeamigos.foresta.tipi.tattiche.EsitoAzione;
import com.threeamigos.foresta.tipi.tattiche.TipoAzione;

/**
 * Il colpo di grazia: se va a segno su un bersaglio sotto un quarto della salute massima ({@link #puoUccidere}) gli toglie tutta la
 * salute che ha, altrimenti si ritorce contro chi lo lancia. Su un bersaglio sano lo pagano anche i mostri che scelgono le magie a caso:
 * non sanno come funzionano, e se si ritorcono contro è un rischio loro. Il danno non passa da CalcolatoreCombattimento,
 * che lo ridurrebbe con difese e resistenze.
 */
public class Morte extends IncantesimoMaleficoImpl implements Incantesimo {

	public Morte(int livello) {
		super(livello);
	}

	public TipoIncantesimo getClasse() {
		return TipoIncantesimo.MORTE;
	}

	@Override
	public TipoDanno getTipoDanno() {
		return TipoDanno.NECROTICO;
	}

	public int getDanni() {
		return CostantiIncantesimi.INCANTESIMO_MORTE_DANNI;
	}

	/**
	 * Se il bersaglio è abbastanza ferito perché Morte lo uccida: ha meno di {@link Costanti#INCANTESIMO_MORTE_SOGLIA_SALUTE_PERCENTUALE}% della
	 * salute massima, senza arrotondamenti (con 10 di salute massima uccide con 2, che è il 20%). È la stessa soglia della tattica che lo sceglie.
	 */
	public static boolean puoUccidere(Personaggio bersaglio) {
		return 100 * bersaglio.getSalute() < CostantiIncantesimi.INCANTESIMO_MORTE_SOGLIA_SALUTE_PERCENTUALE * bersaglio.getSaluteMassima();
	}

	/**
	 * Solo su un bersaglio: su un gruppo, o con un effetto globale, sarebbe troppo pericoloso.
	 */
	@Override
	public void formula(Personaggio formulante, Personaggio bersaglio, Gruppo gruppoBersaglio) {
		if (bersaglio == null) {
			throw new IllegalArgumentException("Morte si formula solo su un bersaglio");
		}
		super.formula(formulante, bersaglio, null);
	}

	@Override
	protected void colpisci(Personaggio formulante, Personaggio bersaglio) {
		if (!bersaglio.isVivo()) {
			return;
		}
		String nomeBersaglio = bersaglio.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE);
		StatoDelCombattimento stato = StatoDelCombattimento.getIstanza();
		if (bersaglio.isImmuneAIncantesimo(getClasse())) {
			BusEventi.pubblica(new NotificaTestoFrase("L'incantesimo non ha effetto su " + nomeBersaglio + "."));
			stato.registraEsitoAzione(formulante, bersaglio, TipoAzione.MORTE, EsitoAzione.IMMUNE);
			return;
		}
		if (puoUccidere(bersaglio) && CalcolatoreCombattimento.colpisce(formulante, bersaglio, getTipoDanno().getCategoriaDanno())) {
			stato.registraEsitoAzione(formulante, bersaglio, TipoAzione.MORTE, EsitoAzione.RIUSCITA);
			BusEventi.pubblica(new NotificaTestoFrase("L'incantesimo ha ucciso " + nomeBersaglio + "."));
			// Tutta la salute rimasta: la morte passa da subSalute come per ogni altro colpo (causa, immortali)
			bersaglio.subSalute(bersaglio.getSalute(), formulante, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
		} else {
			// Fallito (o lanciato su chi è troppo in salute): i tentativi falliti fermano chi lo ripete contro lo stesso bersaglio
			stato.registraEsitoAzione(formulante, bersaglio, TipoAzione.MORTE, EsitoAzione.MANCATA);
			String s = formulante.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE);
			BusEventi.pubblica(new NotificaTestoFrase("L'incantesimo non ha avuto successo e si è ritorto contro " + s + "."));
			formulante.subSalute(formulante.getSalute() / 4, bersaglio, Personaggio.NotificaFerite.SI, Personaggio.NotificaMorte.SI);
		}
	}
}
