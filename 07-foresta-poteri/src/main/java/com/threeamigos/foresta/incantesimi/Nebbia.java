package com.threeamigos.foresta.incantesimi;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.gruppi.Gruppo;
import com.threeamigos.foresta.modellodati.ModificatoreAttributoMD;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.BersaglioModificatore;
import com.threeamigos.foresta.tipi.CostantiIncantesimi;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.tipi.TipoIncantesimo;
import com.threeamigos.foresta.tipi.TipoModificatore;
import com.threeamigos.foresta.tipi.UnitaDurataModificatore;

import java.util.List;

/**
 * Una nebbia fitta su tutto il campo di battaglia (incantesimo GLOBALE): a ogni avversario vivo toglie una percentuale di PRECISIONE
 * (INCANTESIMO_NEBBIA_PRECISIONE_PERCENTUALE) per INCANTESIMO_NEBBIA_DURATA turni. Non fa danni e non ha un tiro per colpire; chi è immune
 * all'incantesimo non ne è toccato. Lanciarla di nuovo non raddoppia l'effetto: rinfresca la durata (vedi Personaggio.addModificatore).
 */
public class Nebbia extends IncantesimoMaleficoImpl implements IncantesimoGlobale {

	/**
	 * La nota del modificatore: lo distingue dagli altri
	 */
	public static final String NOTA = "Nebbia";

	public Nebbia(int livello) {
		super(livello);
	}

	public TipoIncantesimo getClasse() {
		return TipoIncantesimo.NEBBIA;
	}

	@Override
	public TipoDanno getTipoDanno() {
		return TipoDanno.ACQUA;
	}

	public int getDanni() {
		return 0;
	}

	@Override
	protected void formulaGlobale(Personaggio formulante, Gruppo gruppoAvversario) {
		BusEventi.pubblica(new NotificaTestoFrase(formulante.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
				Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA) + " evoca una nebbia fitta: gli avversari faticano a prendere la mira."));
		for (Personaggio avversario : gruppoAvversario.getPersonaggiVivi()) {
			if (avversario.isImmuneAIncantesimo(getClasse())) {
				BusEventi.pubblica(new NotificaTestoFrase("La nebbia non ha effetto su "
						+ avversario.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE) + "."));
				continue;
			}
			avversario.addModificatore(new ModificatoreAttributoMD(TipoAttributo.PRECISIONE, TipoModificatore.VARIAZIONE_PERCENTUALE,
					CostantiIncantesimi.INCANTESIMO_NEBBIA_PRECISIONE_PERCENTUALE, NOTA, UnitaDurataModificatore.TURNI,
					CostantiIncantesimi.INCANTESIMO_NEBBIA_DURATA, BersaglioModificatore.PORTATORE));
		}
	}

	/**
	 * Serve se c'è un avversario in campo che non è immune e non è già nella nebbia
	 */
	@Override
	public boolean serve(List<Personaggio> alleati, List<Personaggio> avversari) {
		for (Personaggio avversario : avversari) {
			if (!avversario.isFuoriCombattimento() && !avversario.isImmuneAIncantesimo(TipoIncantesimo.NEBBIA)
					&& avversario.getModificatoriTemporanei().stream().noneMatch(m -> NOTA.equals(m.getNote()))) {
				return true;
			}
		}
		return false;
	}
}
