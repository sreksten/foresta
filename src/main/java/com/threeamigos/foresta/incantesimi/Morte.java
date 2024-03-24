package com.threeamigos.foresta.incantesimi;

import com.threeamigos.foresta.motore.Gruppo;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.ui.UI;

public class Morte extends IncantesimoMaleficoImpl implements Incantesimo {

	public ClassiIncantesimo getClasse() {
		return ClassiIncantesimo.MORTE;
	}

	public String getNomeAbbreviato() {
		return "Morte";
	}

	public String getNomeSingolare() {
		return "incantesimo di Morte";
	}

	public String getNomePlurale() {
		return "incantesimi di Morte";
	}

	public PortataIncantesimo getPortata() {
		return PortataIncantesimo.GRUPPO;
	}

	public int getCostoAcquisto() {
		return 5;
	}

	public int getCostoLancio() {
		return 5;
	}

	public int getDanni() {
		return 0;
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
		if (bersaglio.getForza() < bersaglio.getForzaMassima() / 4) {
			String s = bersaglio.getNome();
			if (s == null)
				s = bersaglio.getADS() + bersaglio.getNomeSingolare();
			UI.notifica("L'incantesimo ha ucciso " + s + ".");
			bersaglio.muore((bersaglio.getSesso() == Personaggio.Sesso.MASCHIO ? "Ucciso " : "Uccisa ") + " da un incantesimo di Morte");
			uccisi++;
		} else {
			String s = formulante.getNome();
			if (s == null) {
				s = formulante.getADS() + formulante.getNomeSingolare();
			}
			UI.notifica("L'incantesimo non ha avuto successo e si e' ritorto contro " + s + ".");
			formulante.subForza(formulante.getForza() / 4, bersaglio, Personaggio.NotificaFerite.SI, Personaggio.NotificaMorte.SI);
		}
	}
}
