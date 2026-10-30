package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaAumentoLivelloPersonaggio;
import com.threeamigos.foresta.eventi.notifiche.NotificaConsumoPuntoAbilitaPersonaggio;
import com.threeamigos.foresta.eventi.notifiche.NotificaVariazioneStatistichePersonaggio;
import com.threeamigos.foresta.modellodati.PersonaggioMD;
import com.threeamigos.foresta.tipi.CostantiPersonaggi;
import com.threeamigos.foresta.tipi.TipoAttributo;

/**
 * L'esperienza e il livello di un personaggio che cresce durante il gioco: i punti esperienza e il salto di livello, il livello di chi si
 * unisce al gruppo e la spesa dei punti abilità sui primari. Lo tiene {@link PersonaggioBase}, che ne inoltra i metodi di {@link Personaggio};
 * i dati stanno nel {@link PersonaggioMD} del personaggio, che qui si rilegge a ogni uso perché può essere sostituito.
 */
final class Progressione {

	private final PersonaggioBase p;

	Progressione(PersonaggioBase personaggio) {
		this.p = personaggio;
	}

	private PersonaggioMD md() {
		return p.getModelloDati();
	}

	void addPuntiEsperienza(int esperienza) {
		if (p.isPNG()) {
			return;
		}

		int quantitaPrecedente = md().getEsperienza();
		int nuovaQuantita = quantitaPrecedente + esperienza;
		md().setEsperienza(nuovaQuantita);
		BusEventi.pubblica(new NotificaVariazioneStatistichePersonaggio(p, TipoAttributo.PUNTI_ESPERIENZA,
				quantitaPrecedente, nuovaQuantita));

		// Verifichiamo se i nuovi XP accumulati determinano un salto di livello
		int livelloAttuale = md().getLivello();
		int nuovoLivello = GestoreProgressione.calcolaLivelloDaXp(md().getEsperienza());

		if (nuovoLivello > livelloAttuale) {
			int differenza = nuovoLivello - livelloAttuale;
			md().setLivello(nuovoLivello);
			BusEventi.pubblica(new NotificaAumentoLivelloPersonaggio(p, livelloAttuale, nuovoLivello));
			int puntiAbilitaDisponibili = md().getPuntiAbilitaDisponibili();
			int nuoviPuntiAbilitaDisponibili = puntiAbilitaDisponibili + differenza;
			md().setPuntiAbilitaDisponibili(nuoviPuntiAbilitaDisponibili);
			BusEventi.pubblica(new NotificaVariazioneStatistichePersonaggio(p, TipoAttributo.PUNTI_ABILITA, puntiAbilitaDisponibili, nuoviPuntiAbilitaDisponibili));
		}
		// QUI PUOI AGGANCIARE IL CODICE PRECEDENTE:
		// 1. Ricalcola il nuovo budget di punti primari (con la tolleranza del 5%)
		// 2. Aggiorna le statistiche nel modello md().setForza(...), ecc.
		// 3. Ricalcola i valori derivati come Carico, Critico, Velocità.
	}

	void impostaLivelloDopoReclutamento(int livello) {
		int livelloAttuale = md().getLivello();
		int nuovoLivello = Math.min(livello, CostantiPersonaggi.MASSIMO_LIVELLO);
		if (nuovoLivello <= livelloAttuale) {
			return;
		}
		double saluteMassimaPrima = p.calcolaSaluteMassima();
		double magiaMassimaPrima = p.getMagiaMassima();
		double frazioneSalute = saluteMassimaPrima > 0 ? md().get(TipoAttributo.SALUTE) / saluteMassimaPrima : 1.0d;
		double frazioneMagia = magiaMassimaPrima > 0 ? md().get(TipoAttributo.MAGIA) / magiaMassimaPrima : 1.0d;
		md().setLivello(nuovoLivello);
		md().setEsperienza(Math.max(md().getEsperienza(), GestoreProgressione.getXpNecessariPerLivello(nuovoLivello)));
		md().setPuntiAbilitaDisponibili(md().getPuntiAbilitaDisponibili() + nuovoLivello - livelloAttuale);
		// Salute e magia crescono con il livello: chi arriva ne ha la stessa frazione che aveva (di solito tutta), non i punti del livello 1
		md().set(TipoAttributo.SALUTE, (int) (p.calcolaSaluteMassima() * frazioneSalute));
		md().set(TipoAttributo.MAGIA, (int) (p.getMagiaMassima() * frazioneMagia));
	}

	void spendiPuntoAbilita(TipoAttributo tipoAttributo) {
		if (!tipoAttributo.isPrimario() || md().getPuntiAbilitaDisponibili() <= 0) {
			return;
		}
		p.add(tipoAttributo, 1);
		int puntiAbilitaDisponibili = md().getPuntiAbilitaDisponibili();
		int nuoviPuntiAbilitaDisponibili = puntiAbilitaDisponibili - 1;
		md().setPuntiAbilitaDisponibili(nuoviPuntiAbilitaDisponibili);
		// I secondari (precisione, critico, velocita'...) si ricavano dai primari
		p.ricalcolaAttributiSecondari();
		BusEventi.pubblica(new NotificaVariazioneStatistichePersonaggio(p, TipoAttributo.PUNTI_ABILITA,
				puntiAbilitaDisponibili, nuoviPuntiAbilitaDisponibili));
		BusEventi.pubblica(new NotificaConsumoPuntoAbilitaPersonaggio(p, tipoAttributo));
	}
}
