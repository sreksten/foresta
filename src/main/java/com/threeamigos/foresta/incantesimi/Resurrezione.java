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
		String nome = personaggioBersaglio.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE, Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA);

		if (personaggioBersaglio.isVivo()) {
            String notifica = nome + " era gia' viv" + personaggioBersaglio.getLetteraFinaleAttributo() +
                    ", per cui la sua forza è stata completamente reintegrata.";
			UI.notifica(notifica);
			personaggioBersaglio.addSalute(personaggioBersaglio.getSaluteMassima());
			personaggioBersaglio.subStanchezza(9);
		} else {
            String notifica = nome + " è risort" + personaggioBersaglio.getLetteraFinaleAttributo() +
                    " dalle proprie ceneri.";
			UI.notifica(notifica);
			personaggioBersaglio.resuscita();
		}
	}
}
