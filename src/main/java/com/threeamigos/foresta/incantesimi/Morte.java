package com.threeamigos.foresta.incantesimi;

import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.motore.Gruppo;
import com.threeamigos.foresta.motore.modellodati.TipoDanno;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.ui.UI;

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

	@Override
	public void formula(Personaggio formulante, Personaggio bersaglio, Gruppo gruppoBersaglio) {
		if (bersaglio != null) {
			formulaImpl(formulante, bersaglio);
		} else {
			int l = gruppoBersaglio.getNumeroPersonaggi();
			for (int i = 0; i < l; i++) {
				formulaImpl(formulante, gruppoBersaglio.getPersonaggio(i));
				if (!formulante.isVivo()) {
					break;
				}
			}
		}
		if (!formulante.isPNG()) {
			UI.notifica(risultato(formulante));
		}
	}

	private void formulaImpl(Personaggio formulante, Personaggio bersaglio) {
		if (bersaglio == null)
			return;
		if (!bersaglio.isVivo())
			return;
		if (bersaglio.getSalute() < bersaglio.getSaluteMassima() / 4) {
			String s = bersaglio.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE);
			UI.notifica("L'incantesimo ha ucciso " + s + ".");
			bersaglio.muore((bersaglio.getSesso() == Personaggio.Sesso.MASCHIO ? "Ucciso " : "Uccisa ") + " da un incantesimo di Morte");
			uccisi++;
		} else {
			String s = formulante.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE);
			UI.notifica("L'incantesimo non ha avuto successo e si è ritorto contro " + s + ".");
			formulante.subSalute(formulante.getSalute() / 4, bersaglio, Personaggio.NotificaFerite.SI, Personaggio.NotificaMorte.SI);
		}
	}
}
