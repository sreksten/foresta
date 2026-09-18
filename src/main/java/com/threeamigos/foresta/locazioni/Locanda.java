package com.threeamigos.foresta.locazioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.EventoMessaggio;
import com.threeamigos.foresta.eventi.EventoMostraFinestra;
import com.threeamigos.foresta.eventi.EventoParagrafo;
import com.threeamigos.foresta.incantesimi.ClasseIncantesimo;
import com.threeamigos.foresta.motore.*;
import com.threeamigos.foresta.motore.modellodati.LocazioneMD;
import com.threeamigos.foresta.motore.modellodati.TipoRiposo;
import com.threeamigos.foresta.offerte.Informazioni;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.ui.InterfacciaUtente;
import com.threeamigos.foresta.ui.UI;

import java.util.List;

public class Locanda extends LocazioneBase {

	private static final String NO_MONETE_PERNOTTAMENTO = "Il gruppo non ha abbastanza monete per pagare il pernottamento e l'oste chiede loro di lasciare la locanda al più presto.";

	private enum StatoInLocanda {
		SULLA_PORTA,
		ENTRATO,
		CHI_MANGIA,
		PERSONAGGIO,
		PERNOTTA,
	}

	private static final int INCONTRA_PERSONAGGIO = 0;
	private static final int RICEVE_INFORMAZIONI = 1;

	/**
	 * Segnata sulla casella quando il gruppo supera la porta: chi viene respinto
	 * dall'oste per mancanza di monete non è entrato, e non ha visitato la locanda.
	 */
	public static final String LOCANDA_VISITATA = "LOCANDA_VISITATA";

	/**
	 * Nome, recensione e dialogo pescati dal pool di ProduttoreDiTestiCasuale.DatiLocanda
	 * quando la locanda (o la città che la ospita) viene costruita.
	 */
	public static final String LOCANDA_NOME = "LOCANDA_NOME";
	public static final String LOCANDA_RECENSIONE = "LOCANDA_RECENSIONE";
	public static final String LOCANDA_DIALOGO = "LOCANDA_DIALOGO";

	/**
	 * Segnate quando il dialogo/la recensione sono già stati mostrati, per non ripeterli
	 * alle visite successive.
	 */
	public static final String LOCANDA_DIALOGO_LETTO = "LOCANDA_DIALOGO_LETTO";
	public static final String LOCANDA_RECENSIONE_LETTA = "LOCANDA_RECENSIONE_LETTA";

	private StatoInLocanda stato;
	private final int evento;

	/**
	 * In città e nelle locande il gruppo puo' incontrare altri personaggi.
	 */
	private Personaggio personaggioDisponibile;


	@Override
	public ClassiLocazione getClasseLocazione() {
		return ClassiLocazione.LOCANDA;
	}

	public Locanda() {
		stato = StatoInLocanda.SULLA_PORTA;
		// O incontra un personaggio o riceve informazioni
		GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
		if (gruppo.getNumeroPersonaggi() < Costanti.MAX_PERSONAGGI_GRUPPO_GIOCATORE) {
			personaggioDisponibile = RegistroPersonaggi.getPersonaggioInLocazione(gruppo.getCoordinate());
		}
		if (personaggioDisponibile != null) {
			evento = INCONTRA_PERSONAGGIO;
		} else {
			evento = RICEVE_INFORMAZIONI;
		}
	}

	@Override
	public void crea(GruppoGiocatore g, GruppoAvversario gng) {
		if (personaggioDisponibile != null) {
			gng.aggiungiPersonaggio(personaggioDisponibile);
		}
		// Va fatto qui e non nel costruttore: a costruttore ancora in corso il
		// modello dati è ancora quello provvisorio assegnato di default, non quello
		// condiviso con la casella della Foresta che setModelloDati() installa subito
		// dopo — altrimenti isCompleta() risulterebbe falso all'uscita dalla locanda,
		// facendo saltare il controllo missioni di fine locazione (FINE_LOCAZIONE) per
		// quel turno e rimandandolo di una locazione.
		setCompleta(true);
	}

	public static void impostaDatiLocanda(LocazioneMD modelloDati, ProduttoreDiTestiCasuale.DatiLocanda datiLocanda) {
		modelloDati.aggiungiProprieta(LOCANDA_NOME, datiLocanda.getNome());
		modelloDati.aggiungiProprieta(LOCANDA_RECENSIONE, datiLocanda.getRecensione());
		modelloDati.aggiungiProprieta(LOCANDA_DIALOGO, datiLocanda.getDialogo());
	}

	@Override
	public void descrivi(GruppoGiocatore g, GruppoAvversario gng) {
		BusEventi.pubblica(new EventoParagrafo(descrizioneLocanda(g)));
	}

	/**
	 * Alla prima visita racconta il dialogo della locanda, alla seconda la recensione,
	 * dalla terza in poi si limita a nominarla.
	 */
	private String descrizioneLocanda(GruppoGiocatore g) {
		LocazioneMD md = getModelloDati();
		String nome = md.ottieniProprieta(LOCANDA_NOME);
		if (md.ottieniProprieta(LOCANDA_DIALOGO_LETTO) == null) {
			md.aggiungiProprieta(LOCANDA_DIALOGO_LETTO, LocazioneMD.AFFERMATIVO);
			String dialogo = md.ottieniProprieta(LOCANDA_DIALOGO);
			if (dialogo != null && !dialogo.isEmpty()) {
				return '“' + dialogo + '"';
			}
		}
		if (md.ottieniProprieta(LOCANDA_RECENSIONE_LETTA) == null) {
			md.aggiungiProprieta(LOCANDA_RECENSIONE_LETTA, LocazioneMD.AFFERMATIVO);
			String recensione = md.ottieniProprieta(LOCANDA_RECENSIONE);
			if (recensione != null && !recensione.isEmpty()) {
				return g.chiMaiuscolo() + " è a “" + nome + "\". A quanto si dice, " + recensione + ".";
			}
		}
		return g.chiMaiuscolo() + " è alla locanda “" + nome + '"';
	}

	@Override
	public Stato impostaAzioni(GruppoGiocatore gruppo, GruppoAvversario gng, Comando azione) {
		switch (stato) {
		case SULLA_PORTA:
			if (gruppo.getMonete() < Costanti.COSTO_PASTO) {
				BusEventi.pubblica(new EventoMessaggio("L'oste però non è disposto a fare credito..."));
				return Stato.FINE_LOCAZIONE;
			}
			BusEventi.pubblica(new EventoParagrafo("Un cantastorie sta raccontando una vecchia storia locale."));
			try {
				List<String> fiaba = ProduttoreDiTestiCasuale.fiaba();
				int numeroLinea = 0;
				for (String linea : fiaba) {
					if (numeroLinea == 0) {
						BusEventi.pubblica(new EventoMessaggio('“' + linea));
					} else if (numeroLinea == fiaba.size() - 1) {
						BusEventi.pubblica(new EventoMessaggio(linea + '"'));
					} else {
						BusEventi.pubblica(new EventoMessaggio(linea));
					}
					numeroLinea++;
				}
			} catch (Exception e) {
				Logger.log(e);
			}
			UI.impostaAzioni(Comando.PERGAMENA);
			stato = StatoInLocanda.ENTRATO;
			getModelloDati().aggiungiProprieta(LOCANDA_VISITATA, LocazioneMD.AFFERMATIVO);
			return Stato.IN_LOCAZIONE;

		case ENTRATO:
			if (gruppo.getMonete() < Costanti.COSTO_PASTO * gruppo.getNumeroPersonaggiVivi()) {
				BusEventi.pubblica(new EventoMessaggio("Non avendo monete sufficienti per tutto il gruppo, una sola persona consuma un pasto in gran fretta. Chi lo fa?"));
				BusEventi.pubblica(new EventoMostraFinestra(InterfacciaUtente.Finestra.STATO));
				stato = StatoInLocanda.CHI_MANGIA;
				Personaggio p;
				int l = gruppo.getNumeroPersonaggiVivi();
				ComandiPossibili.reimposta();
				for (int i = 0; i < l; i++) {
					p = gruppo.getPersonaggio(i);
					if (p.isVivo()) {
						ComandiPossibili.add(Comando.ofPersonaggio(i));
					}
				}
				return Stato.IN_LOCAZIONE;
			} else {
				BusEventi.pubblica(new EventoParagrafo("Viene servito un pasto caldo, che fa riacquistare rapidamente le forze."));
				int personaggiCheHannoMangiato = 0;
				for (Personaggio personaggio : gruppo.getPersonaggiVivi()) {
					personaggio.addSalute(Costanti.RECUPERO_SALUTE_DA_PASTO);
					personaggiCheHannoMangiato++;
				}
				gruppo.subMonete(Costanti.COSTO_PASTO * personaggiCheHannoMangiato);

				if (evento == RICEVE_INFORMAZIONI) {
					Informazioni info = new Informazioni();
					BusEventi.pubblica(new EventoParagrafo(info.getDescrizione(gruppo, gng)));
				}

				if (incontra(gruppo)) {
					stato = StatoInLocanda.PERSONAGGIO;
					ComandiPossibili.set(Comando.SI, Comando.NO);
					return Stato.IN_LOCAZIONE;
				} else {
					if (gruppo.getMonete() < Costanti.COSTO_PERNOTTAMENTO * gruppo.getNumeroPersonaggi()) {
						BusEventi.pubblica(new EventoMessaggio(NO_MONETE_PERNOTTAMENTO));
						return Stato.FINE_LOCAZIONE;
					} else {
						BusEventi.pubblica(new EventoMessaggio(gruppo.chiMaiuscolo() + " desidera pernottare alla locanda?"));
						BusEventi.pubblica(new EventoMostraFinestra(InterfacciaUtente.Finestra.STATO));
						stato = StatoInLocanda.PERNOTTA;
						ComandiPossibili.set(Comando.SI, Comando.NO);
						return Stato.IN_LOCAZIONE;
					}
				}
			}

		case CHI_MANGIA:
			Personaggio p = gruppo.getPersonaggio(azione);
			p.addSalute(Costanti.RECUPERO_SALUTE_DA_PASTO);
			gruppo.subMonete(Costanti.COSTO_PASTO);
			String nome = p.getNome(Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA, Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE);
            String sb = nome + " si è rifocillat" + p.getLetteraFinaleAttributo() +
                    " in gran fretta, ed il gruppo lascia la locanda dietro pressione dell'oste.";
			BusEventi.pubblica(new EventoMessaggio(sb));
			return Stato.FINE_LOCAZIONE;
			
		case PERSONAGGIO:
			if (azione == Comando.SI) {
				accetta(gruppo, true);
				BusEventi.pubblica(new EventoMostraFinestra(InterfacciaUtente.Finestra.STATO));
			} else {
				accetta(gruppo, false);
			}
			if (gruppo.getMonete() < Costanti.COSTO_PERNOTTAMENTO * gruppo.getNumeroPersonaggiVivi()) {
				BusEventi.pubblica(new EventoMessaggio(NO_MONETE_PERNOTTAMENTO));
				return Stato.FINE_LOCAZIONE;
			} else {
				BusEventi.pubblica(new EventoMessaggio(gruppo.chiMaiuscolo() + " desidera pernottare alla locanda?"));
				BusEventi.pubblica(new EventoMostraFinestra(InterfacciaUtente.Finestra.STATO));
				stato = StatoInLocanda.PERNOTTA;
				ComandiPossibili.set(Comando.SI, Comando.NO);
				return Stato.IN_LOCAZIONE;
			}
			
		case PERNOTTA:
			if (azione == Comando.SI) {
				gruppo.subMonete(Costanti.COSTO_PERNOTTAMENTO * gruppo.getNumeroPersonaggiVivi());
				gruppo.pernotta(TipoRiposo.AL_COPERTO);
			} else {
				BusEventi.pubblica(new EventoMessaggio("L'oste chiede di lasciare la locanda al più presto."));
			}
			return Stato.FINE_LOCAZIONE;

		default:
			throw new IllegalArgumentException("Locanda::impostaAzioni(): Qui non ci dovrei arrivare MAI");
		}
	}

	private boolean incontra(GruppoGiocatore g) {
		if (evento != INCONTRA_PERSONAGGIO) {
			return false;
		}

		Personaggio capo = g.getCapo();
		personaggioDisponibile = RegistroPersonaggi.getPersonaggioInLocazione(g.getCoordinate());
		if (personaggioDisponibile != null) {
            String sb = "“Ehi, " + capo.getNome() + "!\", urla una voce. " +
                    Character.toUpperCase(capo.getPronome().charAt(0)) + capo.getPronome().substring(1) +
					" si volta e vede " + personaggioDisponibile.getNomeProprio().orElseThrow(Personaggio.PERSONAGGIO_SENZA_NOME) +
                    ", " + personaggioDisponibile.getAIS() + personaggioDisponibile.getNomeSingolare() +
                    ", sua vecchia amicizia. " + personaggioDisponibile.getDescrizione() +
                    ' ' + capo.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE, Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA) +
                    (personaggioDisponibile.getSesso() == Personaggio.Sesso.MASCHIO ? " lo" : " la") +
                    " vuole con se?";
			BusEventi.pubblica(new EventoParagrafo(sb));
			BusEventi.pubblica(new EventoMostraFinestra(InterfacciaUtente.Finestra.STATO));
			return true;
		}
		return false;
	}

	public void accetta(GruppoGiocatore g, boolean accetta) {
		if (accetta) {
			RegistroPersonaggi.rimuoviPersonaggioInLocazione(g.getCoordinate());
			GruppoAvversario.getIstanza().rimuoviPersonaggio(personaggioDisponibile);
			g.aggiungiPersonaggio(personaggioDisponibile);
			g.addMonete(Dado.tira(5, 15));
			g.addIncantesimi(ClasseIncantesimo.ARIA, Dado.tira(0, 3));
			g.addIncantesimi(ClasseIncantesimo.ACQUA, Dado.tira(0, 3));
			g.addIncantesimi(ClasseIncantesimo.TERRA, Dado.tira(0, 3));
			g.addIncantesimi(ClasseIncantesimo.FUOCO, Dado.tira(0, 3));
			g.addPreziosi(Dado.tira(0, 10));
			personaggioDisponibile = null;
			BusEventi.pubblica(new EventoMostraFinestra(InterfacciaUtente.Finestra.STATO));
		} else if (personaggioDisponibile != null) {
            String notifica = "“Pazienza. Sarà per un'altra volta.\" dice " +
                    personaggioDisponibile.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE) +
                    ", allontanandosi.";
			BusEventi.pubblica(new EventoMessaggio(notifica));
		}
	}

	public TipoRiposo getTipoRiposo() {
		return TipoRiposo.AL_COPERTO;
	}
}
