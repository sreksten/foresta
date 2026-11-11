package com.threeamigos.foresta.incantesimi;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.gruppi.Gruppo;
import com.threeamigos.foresta.modellodati.ModificatoreAttributoMD;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.*;

import java.util.List;
import com.threeamigos.foresta.testi.Lingue;
import com.threeamigos.foresta.testi.Preposizione;
import com.threeamigos.foresta.testi.Testi;

/**
 * Spegne la magia di tutti gli avversari (incantesimo GLOBALE): a ognuno toglie punti di POTERE_MAGICO (INCANTESIMO_NEGA_MAGIA_POTERE_MAGICO)
 * per INCANTESIMO_NEGA_MAGIA_DURATA turni, quindi i suoi incantesimi fanno meno danno. Non fa danni e non ha un tiro per colpire; chi è immune
 * all'incantesimo non ne è toccato. Lanciarla di nuovo non raddoppia l'effetto: rinfresca la durata (vedi Personaggio.addModificatore).
 */
public class NegaMagia extends IncantesimoMaleficoImpl implements IncantesimoGlobale {

	/**
	 * La nota del modificatore: lo distingue dagli altri
	 */
	public static final String NOTA = "Negazione della Magia";

	public NegaMagia(int livello) {
		super(livello);
	}

	public TipoIncantesimo getClasse() {
		return TipoIncantesimo.NEGA_MAGIA;
	}

	@Override
	public TipoDanno getTipoDanno() {
		return TipoDanno.ARCANO;
	}

	public int getDanni() {
		return 0;
	}

	@Override
	protected void formulaGlobale(Personaggio formulante, List<Personaggio> bersagli) {
		BusEventi.pubblica(new NotificaTestoFrase(Testi.formatta("messaggi_poteri", "NegaMagia.1", formulante.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
				Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA))));
		for (Personaggio avversario : bersagli) {
			if (avversario.isImmuneAIncantesimo(getClasse())) {
				BusEventi.pubblica(new NotificaTestoFrase(Testi.formatta("messaggi_poteri", "NegaMagia.2", Lingue.corrente().conPreposizione(Preposizione.SU, avversario.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE)))));
				continue;
			}
			avversario.addModificatore(new ModificatoreAttributoMD(TipoAttributo.POTERE_MAGICO, TipoModificatore.VARIAZIONE_FISSA,
					CostantiIncantesimi.INCANTESIMO_NEGA_MAGIA_POTERE_MAGICO, NOTA, UnitaDurataModificatore.TURNI,
					CostantiIncantesimi.INCANTESIMO_NEGA_MAGIA_DURATA, BersaglioModificatore.PORTATORE));
		}
	}

	/**
	 * Serve se c'è un avversario in campo che sa lanciare incantesimi, non è immune e non è già senza magia
	 */
	@Override
	public boolean serve(List<Personaggio> alleati, List<Personaggio> avversari) {
		for (Personaggio avversario : avversari) {
			if (!avversario.isFuoriCombattimento() && avversario.isMagico() && !avversario.isImmuneAIncantesimo(TipoIncantesimo.NEGA_MAGIA)
					&& avversario.getModificatoriTemporanei().stream().noneMatch(m -> NOTA.equals(m.getNote()))) {
				return true;
			}
		}
		return false;
	}
}
