package com.threeamigos.foresta.incantesimi;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.motore.Gruppo;
import com.threeamigos.foresta.personaggi.Personaggio;

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

		BusEventi.pubblica(new NotificaTestoFrase(formulante.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
				Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA) + " formula un " + getClasse().getNomeSingolare() +
				" su " + nome + "."));

		if (personaggioBersaglio.isVivo()) {
            String notifica = nome + " era già viv" + personaggioBersaglio.getLetteraFinaleAttributo() +
                    ", per cui la sua salute è stata completamente reintegrata.";
			BusEventi.pubblica(new NotificaTestoFrase(notifica));
			personaggioBersaglio.addSalute(personaggioBersaglio.getSaluteMassima());
			personaggioBersaglio.subStanchezza(Costanti.MAX_STANCHEZZA);
		} else {
            String notifica = nome + " è risort" + personaggioBersaglio.getLetteraFinaleAttributo() +
                    " dalle proprie ceneri.";
			BusEventi.pubblica(new NotificaTestoFrase(notifica));
			personaggioBersaglio.resuscita();
		}
		// Come gli altri incantesimi (IncantesimoMaleficoImpl.formula), il lancio costa MAGIA
		formulante.subMagia(getCostoLancio());
	}
}
