package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoPersonaggioArreso;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.eventi.notifiche.NotificaVariazioneStatistichePersonaggio;
import com.threeamigos.foresta.eventi.notifiche.NotificaVariazioneStatoVitalePersonaggio;
import com.threeamigos.foresta.gruppi.Gruppo;
import com.threeamigos.foresta.gruppi.GruppoAvversario;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.modellodati.ModificatoreAttributoMD;
import com.threeamigos.foresta.modellodati.PersonaggioMD;
import com.threeamigos.foresta.testi.Numeri;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * La vita e la magia di un personaggio che cambiano durante il gioco: le ferite (con la resa, il berserk e il racconto della morte), la morte e
 * la resurrezione, e il consumo e il recupero della magia. Lo tiene {@link PersonaggioBase}, che ne inoltra i metodi di {@link Personaggio};
 * i dati stanno nel {@link PersonaggioMD} del personaggio, che qui si rilegge a ogni uso perché può essere sostituito. I massimi e gli altri
 * attributi restano al personaggio, perché le classi li ridefiniscono.
 */
final class SaluteEMorte {

	private final PersonaggioBase p;

	SaluteEMorte(PersonaggioBase personaggio) {
		this.p = personaggio;
	}

	private PersonaggioMD md() {
		return p.getModelloDati();
	}

	/**
	 * Se questo personaggio, invece di morire, si arrende: combatte lui fino alla resa, o è del gruppo e combatte
	 * contro chi combatte fino alla resa.
	 */
	boolean siArrende() {
		return p.isFinoAllaResa() || GruppoGiocatore.getIstanza().contiene(p) && GruppoAvversario.getIstanza().isFinoAllaResa();
	}

	/**
	 * Si arrende: resta con un punto ferita, va in panchina e lo dice.
	 */
	void arrenditi() {
		md().set(TipoAttributo.SALUTE, 1);
		p.setInPanchina(true);
		String nome = p.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE, Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA);
		BusEventi.pubblica(new NotificaTestoFrase(nome + (p.appartieneAlGruppoDelGiocatore()
				? " ammette la sconfitta e si fa da parte." : " abbassa le armi e si arrende.")));
		BusEventi.pubblica(new InternoPersonaggioArreso(p));
	}

	void muore(String causaTrapasso) {
		if (p.isImmortale()) {
			return;
		}
		md().setVivo(false);
		md().setCausaTrapasso(causaTrapasso);
		// Con la morte finiscono anche gli effetti di stato: non devono tornare, e magari riuccidere, con la resurrezione. Senza notifiche:
		// un morto non ha niente da mostrare (la UI disegnerebbe lo sprite grigio della rimozione)
		PersonaggioMD dati = md();
		new ArrayList<>(dati.getEffettiDiStato()).forEach(dati::togliEffettoDiStato);
		// Lo stesso per i modificatori a tempo (anche i negativi): restano solo quelli permanenti, come le benedizioni e l'equipaggiamento
		List<ModificatoreAttributoMD> aTempo = md().getModificatori().stream().filter(ModificatoreAttributoMD::isTemporaneo).collect(Collectors.toList());
		if (!aTempo.isEmpty()) {
			p.modificatori.modificaModificatori(() -> aTempo.forEach(dati::togliModificatore));
		}
		BusEventi.pubblica(new NotificaVariazioneStatoVitalePersonaggio(p, false));
	}

	void resuscita() {
		int salutePrecedente = (int) md().get(TipoAttributo.SALUTE);
		int stanchezzaPrecedente = (int) md().get(TipoAttributo.STANCHEZZA);
		int salute = (int)(p.calcolaSaluteMassima() / 10.0d);
		md().set(TipoAttributo.SALUTE, salute);
		md().set(TipoAttributo.STANCHEZZA, 9);
		md().setVivo(true);
		BusEventi.pubblica(new NotificaVariazioneStatistichePersonaggio(p, TipoAttributo.SALUTE, salutePrecedente, salute));
		BusEventi.pubblica(new NotificaVariazioneStatistichePersonaggio(p, TipoAttributo.STANCHEZZA, stanchezzaPrecedente, 9));
		BusEventi.pubblica(new NotificaVariazioneStatoVitalePersonaggio(p, true));
	}

	/**
	 * La quantità che si può aggiungere è limitata dal valore massimo di base accresciuto da bonus da artefatti
	 */
	void addSalute(int quantita) {
		int salutePrecedente = (int)md().get(TipoAttributo.SALUTE);
		quantita = p.limitaEntroMassimi(salutePrecedente, p.getSaluteMassima(), quantita);
		int saluteCorrente = salutePrecedente + quantita;
		md().set(TipoAttributo.SALUTE, saluteCorrente);
		BusEventi.pubblica(new NotificaVariazioneStatistichePersonaggio(p, TipoAttributo.SALUTE, salutePrecedente, saluteCorrente));
	}

	void subSalute(int quantita, Personaggio avversario, Personaggio.NotificaFerite notificaFerite, Personaggio.NotificaMorte notificaMorte) {
		// Un morto non subisce altri danni: la sua morte non va annunciata due volte (doppio danno periodico, collasso, doppia resa)
		if (!p.isVivo()) {
			return;
		}
		if (quantita <= 0) {
			if (notificaFerite == Personaggio.NotificaFerite.SI) {
				String sb = p.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE, Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA) +
						" non ha riportato danni dall'attacco " + avversario.getNome(Personaggio.OpzioniGetNome.INCLUDI_PREPOSIZIONE_ARTICOLATA) + '.';
				BusEventi.pubblica(new NotificaTestoFrase(sb));
			}
			return;
		}

		double saluteOriginale = md().get(TipoAttributo.SALUTE);
		double salute = saluteOriginale - quantita;
		if (salute <= 0 && !p.isImmortale() && siArrende()) {
			arrenditi();
			BusEventi.pubblica(new NotificaVariazioneStatistichePersonaggio(p, TipoAttributo.SALUTE, saluteOriginale, 1));
			return;
		}
		if (salute <= 0) {
			salute = 0;
			if (!p.isImmortale()) {
				// Prima la salute, poi la morte: chi ascolta la notifica di morte trova già 0
				md().setSalute(0);
				md().setVivo(false);
				StringBuilder sb;
				// Chi lascia il gruppo appena muore (lo Scheletro della Mietitura) non ha ferite da raccontare: se ne occupa il gruppo
				if (notificaMorte == Personaggio.NotificaMorte.SI && !p.getMotivoNelGruppo().isLasciaSeMuore()) {
					sb = new StringBuilder();
					if (md().getNome() == null) {
						boolean isGruppoAvversario;
						Gruppo gruppo;
						if (p.appartieneAlGruppoDelGiocatore()) {
							isGruppoAvversario = false;
							gruppo = GruppoGiocatore.getIstanza();
						} else {
							isGruppoAvversario = true;
							gruppo = GruppoAvversario.getIstanza();
						}
						if (isGruppoAvversario && GruppoAvversario.getIstanza().contaDellaStessaSpecie(p) > 1) {
							if (p.getSesso() == Personaggio.Sesso.MASCHIO) {
								sb.append(Numeri.getOrdinaleM(p.getOrdinale(), true));
							} else {
								sb.append(Numeri.getOrdinaleF(p.getOrdinale(), true));
							}
							sb.append(" ");
							sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
						} else {
							String ads = p.getADS();
							sb.append(Character.toUpperCase(ads.charAt(0)));
							sb.append(ads.substring(1));
						}
						sb.append(p.getNomeSingolare());
					} else {
						sb.append(md().getNome());
					}
					sb.append(" è mort");
					sb.append(p.getLetteraFinaleAttributo());
					sb.append(" per le ferite riportate.");
					BusEventi.pubblica(new NotificaTestoFrase(sb.toString()));
				}
				if (avversario != null) {
					md().setCausaTrapasso("Uccis" + p.getLetteraFinaleAttributo() + " " + avversario.getDa() + avversario.getNomeSingolare() + ".");
				} else {
					// Senza un attaccante il danno viene dagli effetti di stato (veleno, sanguinamento...)
					md().setCausaTrapasso("Mort" + p.getLetteraFinaleAttributo() + " per i danni nel tempo.");
				}
				p.muore(md().getCausaTrapasso());
			}
		} else {
			if (notificaFerite == Personaggio.NotificaFerite.SI) {
				String nome = p.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE, Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA);
				String notifica = nome + " ha ancora " + (int)salute + " punt" + (salute == 1 ? 'o' : 'i') +
						" ferita su " + (int)p.calcolaSaluteMassima() + '.';
				BusEventi.pubblica(new NotificaTestoFrase(notifica));
			}
		}
		md().setSalute(salute);
		BusEventi.pubblica(new NotificaVariazioneStatistichePersonaggio(p, TipoAttributo.SALUTE, saluteOriginale, salute));

		if ((md().getClasse() == TipoPersonaggio.GUERRIERO || md().getClasse() == TipoPersonaggio.GUERRIERA
				|| md().getClasse() == TipoPersonaggio.CAPITANO_DELLE_GUARDIE) &&
				p.getFuria() > 0 && !p.hasEffettoDiStato(TipoEffettoDiStato.BERSERK)) {
			double sogliaBerserk = p.calcolaSaluteMassima() / 3.0d;
			if (salute > 0 && salute <= sogliaBerserk) {
				p.addEffettoDiStato(TipoEffettoDiStato.BERSERK, 1, 0);
			}
		}
	}

	/**
	 * La quantità che si può aggiungere è limitata dal valore massimo di base accresciuto da bonus da artefatti
	 */
	void addMagia(int quantita) {
        int quantitaPrecedente = (int) md().get(TipoAttributo.MAGIA);
		quantita = p.limitaEntroMassimi(quantitaPrecedente, p.getMagiaMassima(), quantita);
		int quantitaAttuale = quantitaPrecedente + quantita;
		md().setMagia(quantitaAttuale);
		BusEventi.pubblica(new NotificaVariazioneStatistichePersonaggio(p, TipoAttributo.MAGIA, quantitaPrecedente, quantitaAttuale));
	}

	void subMagia(int quantita) {
		int quantitaPrecedente = (int) md().get(TipoAttributo.MAGIA);
		if (quantita > quantitaPrecedente) {
			throw new IllegalStateException("Tentativo di utilizzo di più magia rispetto a quella disponibile");
		}
		int quantitaAttuale = quantitaPrecedente - quantita;
		md().setMagia(quantitaAttuale);
		BusEventi.pubblica(new NotificaVariazioneStatistichePersonaggio(p, TipoAttributo.MAGIA, quantitaPrecedente, quantitaAttuale));
	}
}
