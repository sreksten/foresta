package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoPersonaggioArreso;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.eventi.notifiche.NotificaVariazioneStatistichePersonaggio;
import com.threeamigos.foresta.eventi.notifiche.NotificaVariazioneStatoVitalePersonaggio;
import com.threeamigos.foresta.gruppi.GruppoAvversario;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.modellodati.ModificatoreAttributoMD;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.tipi.BersaglioModificatore;
import com.threeamigos.foresta.tipi.CostantiPersonaggi;
import com.threeamigos.foresta.tipi.TipoModificatore;
import com.threeamigos.foresta.tipi.UnitaDurataModificatore;
import com.threeamigos.foresta.modellodati.PersonaggioMD;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.tipi.TipoRiposo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.threeamigos.foresta.testi.Testi;

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
		BusEventi.pubblica(new NotificaTestoFrase(Testi.formatta("messaggi_personaggi", p.appartieneAlGruppoDelGiocatore() ? "RESA_DEL_GIOCATORE" : "RESA", nome)));
		BusEventi.pubblica(new InternoPersonaggioArreso(p));
	}

	/**
	 * Muore (non chi è immortale): perde gli effetti di stato e i modificatori a tempo, senza raccontarlo, e lo annuncia.
	 */
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

	/**
	 * Torna in vita con un decimo della salute massima e la stanchezza al massimo.
	 */
	void resuscita() {
		int salutePrecedente = (int) md().get(TipoAttributo.SALUTE);
		int stanchezzaPrecedente = (int) md().get(TipoAttributo.STANCHEZZA);
		int salute = (int)(p.calcolaSaluteMassima() / 10.0d);
		md().set(TipoAttributo.SALUTE, salute);
		md().set(TipoAttributo.STANCHEZZA, CostantiPersonaggi.MAX_STANCHEZZA);
		md().setVivo(true);
		md().setCausaTrapasso(null);
		BusEventi.pubblica(new NotificaVariazioneStatistichePersonaggio(p, TipoAttributo.SALUTE, salutePrecedente, salute));
		BusEventi.pubblica(new NotificaVariazioneStatistichePersonaggio(p, TipoAttributo.STANCHEZZA, stanchezzaPrecedente, CostantiPersonaggi.MAX_STANCHEZZA));
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

	/**
	 * Le ferite, in quest'ordine: chi non può morire resta a un punto; chi combatte fino alla resa si arrende; chi arriva a zero muore
	 * (con il racconto e la causa della morte); gli altri si sentono dire quante ferite restano. Alla fine, se scende sotto la sua soglia,
	 * chi ha il berserk ci entra. Con un danno di zero o meno si dice solo che il colpo non ha fatto nulla (avversario non null).
	 */
	void subSalute(int quantita, Personaggio avversario, Personaggio.NotificaFerite notificaFerite, Personaggio.NotificaMorte notificaMorte) {
		// Un morto non subisce altri danni: la sua morte non va annunciata due volte (doppio danno periodico, collasso, doppia resa)
		if (!p.isVivo()) {
			return;
		}
		if (quantita <= 0) {
			if (notificaFerite == Personaggio.NotificaFerite.SI) {
				String sb = Testi.formatta("messaggi_personaggi", "NESSUN_DANNO", p.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE, Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA), avversario.getNome(Personaggio.OpzioniGetNome.INCLUDI_PREPOSIZIONE_ARTICOLATA));
				BusEventi.pubblica(new NotificaTestoFrase(sb));
			}
			return;
		}

		double saluteOriginale = md().get(TipoAttributo.SALUTE);
		double salute = saluteOriginale - quantita;
		// Chi adesso non può morire (il capo e chi si è unito durante il tutorial) resta con un punto ferita, qualunque sia il danno, e continua a combattere
		if (salute < 1 && p.isSalvoDallaMorte()) {
			salute = 1;
		}
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
						// «Il secondo scheletro» se ce n'e' piu' d'uno nel gruppo avversario, «Lo scheletro» altrimenti: lo dice getNome, come per ogni altro messaggio
						sb.append(p.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE, Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA));
					} else {
						sb.append(md().getNome());
					}
					BusEventi.pubblica(new NotificaTestoFrase(Testi.formatta("messaggi_personaggi", p.getSesso() == Personaggio.Sesso.MASCHIO ? "MORTO_PER_LE_FERITE.M" : "MORTO_PER_LE_FERITE.F", sb.toString())));
				}
				if (avversario != null) {
					md().setCausaTrapasso(Testi.formatta("messaggi_personaggi", p.getSesso() == Personaggio.Sesso.MASCHIO ? "UCCISO.M" : "UCCISO.F", avversario.getDa() + avversario.getNomeSingolare()));
				} else {
					// Senza un attaccante il danno viene dagli effetti di stato (veleno, sanguinamento...)
					md().setCausaTrapasso(Testi.formatta("messaggi_personaggi", p.getSesso() == Personaggio.Sesso.MASCHIO ? "MORTO_DANNI_NEL_TEMPO.M" : "MORTO_DANNI_NEL_TEMPO.F"));
				}
				p.muore(md().getCausaTrapasso());
			}
		} else {
			if (notificaFerite == Personaggio.NotificaFerite.SI) {
				String nome = p.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE, Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA);
				String notifica = Testi.formatta("messaggi_personaggi", salute == 1 ? "FERITE_RIMASTE_UNA" : "FERITE_RIMASTE", nome, (int) salute, (int) p.calcolaSaluteMassima());
				BusEventi.pubblica(new NotificaTestoFrase(notifica));
			}
		}
		md().setSalute(salute);
		BusEventi.pubblica(new NotificaVariazioneStatistichePersonaggio(p, TipoAttributo.SALUTE, saluteOriginale, salute));

		if (Berserk.haIlBerserk(md().getClasse()) &&
				p.get(TipoAttributo.FURIA) > 0 && !p.hasEffettoDiStato(TipoEffettoDiStato.BERSERK)) {
			double sogliaBerserk = p.calcolaSaluteMassima() * Berserk.soglia(md().getClasse());
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

	/**
	 * La salute che il personaggio recupera a fine locazione (vedi Automa, stato FINE_LOCAZIONE_2). I modificatori di
	 * RIGENERAZIONE_SALUTE (del personaggio, degli artefatti e dei set leggendari) valgono su tutta la base.
	 */
	int getRigenerazioneSalute() {
		// Chi ha un recupero fisico nullo (i non morti) non guarisce
		if (p.getMoltiplicatoreRecuperoFisico() == 0.0) {
			return 0;
		}
		// 1. Base di partenza mista
		double baseGrezza = AttributiSecondari.getQuantitaModificata(md(), p.rigenerazioneSaluteSenzaModificatori(), TipoAttributo.RIGENERAZIONE_SALUTE);
		// 2. La COSTITUZIONE aiuta, ma sempre meno quanto più è alta (la radice): 25 di costituzione danno +50%, 100 danno +100%
		double bonusCostituzione = 1.0 + (Math.sqrt(p.get(TipoAttributo.COSTITUZIONE)) / 10.0);
		// 3. Calcolo finale combinato con il moltiplicatore di archetipo
		double saluteFinale = (baseGrezza * bonusCostituzione) * p.getMoltiplicatoreRecuperoFisico();
		// Arrotondato per eccesso all'intero
		return (int)Math.ceil(saluteFinale);
	}

	/**
	 * La magia che il personaggio recupera a fine locazione (vedi Automa, stato FINE_LOCAZIONE_2). I modificatori di
	 * RIGENERAZIONE_MAGIA (del personaggio, degli artefatti e dei set leggendari) valgono su tutta la base.
	 */
	int getRigenerazioneMagia() {
		// 1. Calcolo del recupero potenziale: una parte fissa e una sulla capienza massima
		double recuperoGrezzo = AttributiSecondari.getQuantitaModificata(md(), p.rigenerazioneMagiaSenzaModificatori(), TipoAttributo.RIGENERAZIONE_MAGIA);
		// 2. Applicazione del moltiplicatore di classe/razza
		double manaRigenerato = recuperoGrezzo * p.getMoltiplicatoreRecuperoMagico();
		// Arrotondato per eccesso all'intero
		return (int)Math.ceil(manaRigenerato);
	}

	void riposa(int ore, TipoRiposo tipoRiposo) {
		RisultatoRiposo risultatoRiposo = CalcolatoreRiposo.calcolaRiposo(p, ore, tipoRiposo);
		addSalute(risultatoRiposo.getRipristinoSalute());
		addMagia(risultatoRiposo.getRipristinoMagia());
		p.subStanchezza(risultatoRiposo.getAbbassamentoStanchezza());
	}

	/**
	 * Fuggire costa una quota della salute massima (da {@link CostantiPersonaggi#FUGA_DANNO_MINIMO_PERCENTUALE} a
	 * {@link CostantiPersonaggi#FUGA_DANNO_MASSIMO_PERCENTUALE}%, quindi cresce con il livello, a differenza di un danno fisso), ma non uccide mai:
	 * chi fugge perché sta male resta in piedi con almeno un punto. Il resto del prezzo è l'ignominia, che per qualche locazione
	 * toglie coraggio, valore e carisma.
	 */
	void fugge() {
		int danno = Math.max(1, p.getSaluteMassima() * Dado.tira(CostantiPersonaggi.FUGA_DANNO_MINIMO_PERCENTUALE, CostantiPersonaggi.FUGA_DANNO_MASSIMO_PERCENTUALE) / 100);
		danno = Math.min(danno, Math.max(0, p.getSalute() - 1));
		if (danno > 0) {
			subSalute(danno, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
		}
		if (!p.isVivo()) {
			return;
		}
		// L'ignominia della fuga: coraggio, valore e carisma calano per qualche locazione. La fuga finisce in
		// FINE_LOCAZIONE, che fa già scalare di uno le locazioni: per sentirle tutte la durata ne ha una in più
		for (TipoAttributo attributo : new TipoAttributo[] {TipoAttributo.CORAGGIO, TipoAttributo.VALORE, TipoAttributo.CARISMA}) {
			p.addModificatore(new ModificatoreAttributoMD(attributo, TipoModificatore.VARIAZIONE_PERCENTUALE,
					-CostantiPersonaggi.FUGA_IGNOMINIA_PERCENTUALE, CostantiPersonaggi.FUGA_IGNOMINIA_NOTA, UnitaDurataModificatore.LOCAZIONI,
					CostantiPersonaggi.FUGA_IGNOMINIA_LOCAZIONI + 1, BersaglioModificatore.PORTATORE, false));
		}
	}
}
