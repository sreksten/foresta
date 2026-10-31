package com.threeamigos.foresta.combattimento;

import com.threeamigos.foresta.artefatti.Arma;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoAzioneInCombattimento;
import com.threeamigos.foresta.eventi.notifiche.NotificaColpoMancato;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.strumenti.logger.Logger;

/**
 * L'attacco con le armi di un personaggio: sta fuori dal personaggio perché conosce il calcolo del combattimento.
 */
public final class AttaccoConLeArmi {

	private AttaccoConLeArmi() {
	}

	/**
	 * Un attacco con le sue armi (la seconda arma compresa) contro quel bersaglio, ognuna sull'asse del suo tipo di danno, finché il bersaglio resta
	 * in combattimento. Lo esegue l'EsecutoreTattiche per i mostri; chi lancia incantesimi e sceglie il bersaglio lo decide la tattica.
	 */
	public static void esegui(Personaggio attaccante, Personaggio bersaglio) {
		BusEventi.pubblica(new InternoAzioneInCombattimento(attaccante, bersaglio));
		annunciaEAttacca(attaccante, bersaglio);
	}

	/**
	 * L'attacco alle spalle: come {@link #esegui}, ma il bersaglio è distratto e non si difende, quindi ogni colpo va a segno ed è un critico sicuro. L'annuncio
	 * lo fa chi lo usa.
	 */
	public static void alleSpalle(Personaggio attaccante, Personaggio bersaglio) {
		for (FaseDiAttacco fase : CalcolatoreCombattimento.fasiDiAttacco(attaccante, bersaglio)) {
			if (bersaglio.isFuoriCombattimento()) {
				return;
			}
			DannoRisultante risultato = CalcolatoreCombattimento.calcolaDannoRisultante(attaccante, bersaglio, fase.getArma(), fase.getFattore(), true);
			Logger.log(() -> attaccante.getNome() + " colpisce alle spalle " + bersaglio.getNome() + " assegnando " + risultato.getDanno() + " danni");
			ApplicazioneDelRisultato.applica(bersaglio, risultato);
		}
	}

	private static void annunciaEAttacca(Personaggio attaccante, Personaggio bersaglio) {
		Logger.log(() -> attaccante.getNome() + " attacca " + bersaglio.getNome());
		// Gli avversari della stessa specie si distinguono per il numero ("La viverna 2"), quindi sempre l'articolo determinativo
		String messaggio = attaccante.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE, Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA) +
				" attacca " + bersaglio.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE) + '.';
		BusEventi.pubblica(new NotificaTestoFrase(messaggio));
		for (FaseDiAttacco fase : CalcolatoreCombattimento.fasiDiAttacco(attaccante, bersaglio)) {
			if (bersaglio.isFuoriCombattimento()) {
				return;
			}
			Arma arma = fase.getArma();
			if (CalcolatoreCombattimento.colpisce(attaccante, bersaglio, arma.getTipoDanno().getCategoriaDanno())) {
				DannoRisultante risultato = CalcolatoreCombattimento.calcolaDannoRisultante(attaccante, bersaglio, arma, fase.getFattore());
				Logger.log(() -> attaccante.getNome() + " colpisce " + bersaglio.getNome() + " assegnando " + risultato.getDanno() + " danni");
				ApplicazioneDelRisultato.applica(bersaglio, risultato);
			} else {
				Logger.log(() -> attaccante.getNome() + " non colpisce " + bersaglio.getNome());
				BusEventi.pubblica(new NotificaColpoMancato(bersaglio));
			}
		}
	}
}
