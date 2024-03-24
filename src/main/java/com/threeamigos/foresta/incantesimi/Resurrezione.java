package com.threeamigos.foresta.incantesimi;

import com.threeamigos.foresta.motore.Gruppo;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.ui.UI;

public class Resurrezione implements Incantesimo {

	public ClassiIncantesimo getClasse() {
		return ClassiIncantesimo.RESURREZIONE;
	}

	public String getNomeAbbreviato() {
		return "Resurr.";
	}

	public String getNomeSingolare() {
		return "incantesimo di Resurrezione";
	}

	public String getNomePlurale() {
		return "incantesimi di Resurrezione";
	}

	public PortataIncantesimo getPortata() {
		return PortataIncantesimo.SINGOLO_QUALSIASI;
	}

	public TipoIncantesimo getTipo() {
		return TipoIncantesimo.BENEFICO;
	}

	public int getCostoAcquisto() {
		return 15;
	}

	public int getCostoLancio() {
		return 15;
	}

	public void formula(Personaggio formulante, Personaggio personaggioBersaglio, Gruppo gruppoBersaglio) {
		String nome = personaggioBersaglio.getNome();
		if (nome == null) {
			StringBuilder sb = new StringBuilder();
			sb.append(Character.toUpperCase(personaggioBersaglio.getADS().charAt(0)))
			.append(personaggioBersaglio.getADS().substring(1))
			.append(personaggioBersaglio.getNomeSingolare());
			nome = sb.toString();
		}
		if (personaggioBersaglio.isVivo()) {
			StringBuilder sb = new StringBuilder(nome);
			sb.append(" era gia' viv")
			.append(personaggioBersaglio.getSesso() == Personaggio.Sesso.MASCHIO ? 'o' : 'a')
			.append(", per cui la sua forza e' stata completamente reintegrata.");
			UI.notifica(sb.toString());
			personaggioBersaglio.addForza(personaggioBersaglio.getForzaMassima());
			personaggioBersaglio.subStanchezza(9);
		} else {
			StringBuilder sb = new StringBuilder(nome);
			sb.append(" e' risort")
			.append(personaggioBersaglio.getSesso() == Personaggio.Sesso.MASCHIO ? 'o' : 'a')
			.append(" dalle proprie ceneri.");
			UI.notifica(sb.toString());
			personaggioBersaglio.resuscita();
		}
	}
}
