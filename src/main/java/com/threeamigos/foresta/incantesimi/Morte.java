package com.threeamigos.foresta.incantesimi;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.motore.CalcolatoreCombattimento;
import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.motore.Gruppo;
import com.threeamigos.foresta.motore.modellodati.TipoDanno;
import com.threeamigos.foresta.personaggi.Personaggio;

/**
 * Il colpo di grazia: se va a segno su un bersaglio sotto un quarto della salute massima gli toglie tutta la
 * salute che ha, altrimenti si ritorce contro chi lo lancia. Il danno non passa da CalcolatoreCombattimento,
 * che lo ridurrebbe con difese e resistenze.
 */
public class Morte extends IncantesimoMaleficoImpl implements Incantesimo {

	public Morte(int livello) {
		super(livello);
	}

	public ClasseIncantesimo getClasse() {
		return ClasseIncantesimo.MORTE;
	}

	public int getCostoLancio() {
		return Costanti.INCANTESIMO_MORTE_COSTO_LANCIO;
	}

	@Override
	public TipoDanno getTipoDanno() {
		return TipoDanno.NECROTICO;
	}

	public int getDanni() {
		return Costanti.INCANTESIMO_MORTE_DANNI;
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
		if (bersaglio.isImmuneAIncantesimo(getClasse())) {
			BusEventi.pubblica(new NotificaTestoFrase("L'incantesimo non ha effetto su " + nomeBersaglio + "."));
			return;
		}
		if (bersaglio.getSalute() < bersaglio.getSaluteMassima() / 4
				&& CalcolatoreCombattimento.colpisce(formulante, bersaglio, getTipoDanno().getSuperTipo())) {
			BusEventi.pubblica(new NotificaTestoFrase("L'incantesimo ha ucciso " + nomeBersaglio + "."));
			// Tutta la salute rimasta: la morte passa da subSalute come per ogni altro colpo (causa, immortali)
			bersaglio.subSalute(bersaglio.getSalute(), formulante, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
		} else {
			String s = formulante.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE);
			BusEventi.pubblica(new NotificaTestoFrase("L'incantesimo non ha avuto successo e si è ritorto contro " + s + "."));
			formulante.subSalute(formulante.getSalute() / 4, bersaglio, Personaggio.NotificaFerite.SI, Personaggio.NotificaMorte.SI);
		}
	}
}
