package com.threeamigos.foresta.incantesimi;

import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.motore.Gruppo;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.ui.UI;

public class Resurrezione implements Incantesimo {

	private final int livello;

	public Resurrezione(int livello) {
		this.livello = livello;
	}

	public ClasseIncantesimo getClasse() {
		return ClasseIncantesimo.RESURREZIONE;
	}

	public int getLivello() {
		return livello;
	}

	public int getCostoLancio() {
		return Costanti.INCANTESIMO_RESURREZIONE_COSTO_LANCIO;
	}

	public void formula(Personaggio formulante, Personaggio personaggioBersaglio, Gruppo gruppoBersaglio) {
		String nome = personaggioBersaglio.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
				Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA);

		UI.notifica(formulante.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
				Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA) + " formula un " + getClasse().getNomeSingolare() +
				" su " + nome + ".");

		if (personaggioBersaglio.isVivo()) {
            String notifica = nome + " era già viv" + personaggioBersaglio.getLetteraFinaleAttributo() +
                    ", per cui la sua salute è stata completamente reintegrata.";
			UI.notifica(notifica);
			personaggioBersaglio.addSalute(personaggioBersaglio.getSaluteMassima());
			personaggioBersaglio.subStanchezza(Costanti.MAX_STANCHEZZA);
		} else {
            String notifica = nome + " è risort" + personaggioBersaglio.getLetteraFinaleAttributo() +
                    " dalle proprie ceneri.";
			UI.notifica(notifica);
			personaggioBersaglio.resuscita();
		}
	}
}
