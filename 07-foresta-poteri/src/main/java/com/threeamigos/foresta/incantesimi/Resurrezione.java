package com.threeamigos.foresta.incantesimi;

import com.threeamigos.foresta.combattimento.StatoDelCombattimento;

import com.threeamigos.foresta.tipi.CostantiPersonaggi;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.gruppi.Gruppo;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.TipoIncantesimo;
import com.threeamigos.foresta.testi.Testi;

/**
 * La resurrezione: un incantesimo benefico su un solo personaggio del gruppo. Se è caduto, risorge; se è ancora vivo, la sua salute torna al massimo e
 * la stanchezza scompare.
 */
public class Resurrezione implements Incantesimo {

	private final int livello;

	public Resurrezione(int livello) {
		this.livello = livello;
	}

	public TipoIncantesimo getClasse() {
		return TipoIncantesimo.RESURREZIONE;
	}

	public int getLivello() {
		return livello;
	}

	/**
	 * Resuscita il bersaglio, o ne reintegra la salute se è vivo, e fa pagare il lancio a chi lo formula.
	 */
	public void formula(Personaggio formulante, Personaggio personaggioBersaglio, Gruppo gruppoBersaglio) {
		String nome = personaggioBersaglio.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
				Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA);

		BusEventi.pubblica(new NotificaTestoFrase(Testi.formatta("messaggi_poteri", "Resurrezione.1", formulante.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
				Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA), getClasse().getNomeSingolare(), nome)));

		if (personaggioBersaglio.isVivo()) {
            String notifica = Testi.formatta("messaggi_poteri", personaggioBersaglio.getSesso() == Personaggio.Sesso.MASCHIO ? "Resurrezione.giaVivo.M" : "Resurrezione.giaVivo.F", nome);
			BusEventi.pubblica(new NotificaTestoFrase(notifica));
			personaggioBersaglio.addSalute(personaggioBersaglio.getSaluteMassima());
			personaggioBersaglio.subStanchezza(CostantiPersonaggi.MAX_STANCHEZZA);
		} else {
            String notifica = Testi.formatta("messaggi_poteri", personaggioBersaglio.getSesso() == Personaggio.Sesso.MASCHIO ? "Resurrezione.risorto.M" : "Resurrezione.risorto.F", nome);
			BusEventi.pubblica(new NotificaTestoFrase(notifica));
			personaggioBersaglio.resuscita();
		}
		// Come gli altri incantesimi (IncantesimoMaleficoImpl.formula), il lancio costa Magia: un bersaglio, al costo proprio della Resurrezione
		paga(formulante, 1);
		StatoDelCombattimento.getIstanza().registraIncantesimoLanciato(formulante);
		StatoDelCombattimento.getIstanza().registraAlleato(formulante, personaggioBersaglio);
	}
}
