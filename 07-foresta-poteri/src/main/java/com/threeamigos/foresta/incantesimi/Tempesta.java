package com.threeamigos.foresta.incantesimi;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.gruppi.Gruppo;
import com.threeamigos.foresta.gruppi.GruppoAvversario;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.CostantiIncantesimi;
import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.tipi.TipoIncantesimo;

import java.util.ArrayList;
import java.util.List;

/**
 * Una tempesta di fulmini su tutto il campo di battaglia (incantesimo GLOBALE): un danno di Fulmine (INCANTESIMO_TEMPESTA_DANNI) a tutti gli avversari
 * e anche a tutti i compagni di chi la evoca, tranne lui. Ognuno ha il suo tiro per colpire e subisce il danno per intero, non ripartito per posizione.
 * I compagni colpiti che cadono non contano come avversari sconfitti.
 */
public class Tempesta extends IncantesimoMaleficoImpl implements IncantesimoGlobale {

	public Tempesta(int livello) {
		super(livello);
	}

	public TipoIncantesimo getClasse() {
		return TipoIncantesimo.TEMPESTA;
	}

	@Override
	public TipoDanno getTipoDanno() {
		return TipoDanno.FULMINE;
	}

	public int getDanni() {
		return CostantiIncantesimi.INCANTESIMO_TEMPESTA_DANNI;
	}

	@Override
	protected void formulaGlobale(Personaggio formulante, Gruppo gruppoAvversario) {
		BusEventi.pubblica(new NotificaTestoFrase(formulante.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
				Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA) + " scatena una tempesta di fulmini: nessuno sul campo ne è al riparo."));
		// Si guardano gli elenchi com'erano all'inizio: chi cade per un fulmine non conta più
		List<Personaggio> colpiti = new ArrayList<>(gruppoAvversario.getPersonaggiVivi());
		colpiti.addAll(compagniDi(formulante));
		for (Personaggio colpito : colpiti) {
			colpisci(formulante, colpito);
		}
	}

	/**
	 * I compagni vivi di chi la evoca, lui escluso: chi la lancia ha sempre un gruppo, quello del giocatore o quello avversario
	 */
	private static List<Personaggio> compagniDi(Personaggio formulante) {
		Gruppo gruppoDelFormulante = GruppoGiocatore.getIstanza().contiene(formulante) ? GruppoGiocatore.getIstanza() : GruppoAvversario.getIstanza();
		List<Personaggio> compagni = new ArrayList<>(gruppoDelFormulante.getPersonaggiVivi());
		compagni.remove(formulante);
		return compagni;
	}

	/**
	 * Serve se gli avversari che i fulmini possono colpire sono più dei compagni che colpirebbero (chi la lancia escluso)
	 */
	@Override
	public boolean serve(List<Personaggio> alleati, List<Personaggio> avversari) {
		int daColpire = 0;
		for (Personaggio avversario : avversari) {
			if (!avversario.isFuoriCombattimento() && !avversario.isImmuneAIncantesimo(TipoIncantesimo.TEMPESTA)
					&& !avversario.isImmuneATipoDanno(TipoDanno.FULMINE)) {
				daColpire++;
			}
		}
		int compagni = 0;
		for (Personaggio alleato : alleati) {
			if (!alleato.isFuoriCombattimento()) {
				compagni++;
			}
		}
		// Chi la lancia è fra i compagni in piedi, ma non si colpisce
		return daColpire > Math.max(0, compagni - 1);
	}
}
