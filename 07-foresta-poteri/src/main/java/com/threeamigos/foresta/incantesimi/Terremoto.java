package com.threeamigos.foresta.incantesimi;

import com.threeamigos.foresta.combattimento.CalcolatoreCombattimento;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.gruppi.Gruppo;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.tipi.CostantiIncantesimi;
import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.tipi.TipoIncantesimo;

import java.util.ArrayList;
import java.util.List;

/**
 * Il terreno trema sotto tutti gli avversari (incantesimo GLOBALE): un danno di Terra leggero (INCANTESIMO_TERREMOTO_DANNI) a ognuno, ciascuno con
 * il suo tiro per colpire e per intero, non ripartito per posizione. Chi viene ferito rischia di cadere
 * (INCANTESIMO_TERREMOTO_PROBABILITA_ATTERRATO per cento): resta a terra per un turno e salta l'azione.
 */
public class Terremoto extends IncantesimoMaleficoImpl implements IncantesimoGlobale {

	public Terremoto(int livello) {
		super(livello);
	}

	public TipoIncantesimo getClasse() {
		return TipoIncantesimo.TERREMOTO;
	}

	@Override
	public TipoDanno getTipoDanno() {
		return TipoDanno.TERRA;
	}

	public int getDanni() {
		return CostantiIncantesimi.INCANTESIMO_TERREMOTO_DANNI;
	}

	@Override
	protected void formulaGlobale(Personaggio formulante, Gruppo gruppoAvversario) {
		BusEventi.pubblica(new NotificaTestoFrase(formulante.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
				Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA) + " scuote il terreno: la terra trema sotto gli avversari."));
		// Si guarda l'elenco com'era all'inizio: chi cade per il colpo non conta più
		List<Personaggio> colpiti = new ArrayList<>(gruppoAvversario.getPersonaggiVivi());
		for (Personaggio avversario : colpiti) {
			int salutePrima = avversario.getSalute();
			colpisci(formulante, avversario);
			if (avversario.getSalute() < salutePrima && !avversario.isFuoriCombattimento()
					&& Dado.tira(100) <= CostantiIncantesimi.INCANTESIMO_TERREMOTO_PROBABILITA_ATTERRATO
					&& !avversario.isImmuneAEffetto(TipoEffettoDiStato.ATTERRATO)) {
				avversario.addEffettoDiStato(TipoEffettoDiStato.ATTERRATO,
						CalcolatoreCombattimento.calcolaDurataStato(avversario, TipoEffettoDiStato.ATTERRATO), 0);
			}
		}
	}

	/**
	 * Serve se c'è un avversario in campo che il terremoto può ferire
	 */
	@Override
	public boolean serve(List<Personaggio> alleati, List<Personaggio> avversari) {
		for (Personaggio avversario : avversari) {
			if (!avversario.isFuoriCombattimento() && !avversario.isImmuneAIncantesimo(TipoIncantesimo.TERREMOTO)
					&& !avversario.isImmuneATipoDanno(TipoDanno.TERRA)) {
				return true;
			}
		}
		return false;
	}
}
