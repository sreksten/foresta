package com.threeamigos.foresta.locazioni;

import java.util.List;

import com.threeamigos.foresta.incantesimi.ClassiIncantesimo;
import com.threeamigos.foresta.motore.Azioni;
import com.threeamigos.foresta.motore.Comando;
import com.threeamigos.foresta.motore.GruppoAvversario;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.ProduttoreDiTestiCasuale;
import com.threeamigos.foresta.motore.RegistroPersonaggi;
import com.threeamigos.foresta.motore.Stato;
import com.threeamigos.foresta.offerte.Informazioni;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tools.Random;
import com.threeamigos.foresta.ui.InterfacciaUtente;
import com.threeamigos.foresta.ui.UI;

public class Locanda extends LocazioneBase {

	private static Locanda istanza = new Locanda();
	
	private Locanda() {
	}
	
	public static Locanda getIstanza() {
		return istanza;
	}
	
	private static final String NO_MONETE_PERNOTTAMENTO = "Il gruppo non ha abbastanza monete per pagare il pernottamento e l'oste chiede loro di lasciare la locanda al piu' presto.";

	private enum StatoInLocanda {
		SULLA_PORTA,
		ENTRATO,
		CHI_MANGIA,
		PERSONAGGIO,
		PERNOTTA,
	}

	private static final int COSTO_PASTO = 5;
	private static final int COSTO_PERNOTTAMENTO = 5;
	
	private static final int INCONTRA_PERSONAGGIO = 0;
	private static final int RICEVE_INFORMAZIONI = 1;

	private StatoInLocanda stato;
	private int evento;

	/**
	 * In citta' e nelle locande il gruppo puo' incontrare altri personaggi.
	 */
	private Personaggio personaggioDisponibile;


	@Override
	public ClassiLocazione getClasseLocazione() {
		return ClassiLocazione.LOCANDA;
	}

	@Override
	public void reimposta() {
		super.reimposta();
		stato = StatoInLocanda.SULLA_PORTA;
		// O incontra un personaggio o riceve informazioni
		GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
		if (gruppo.getNumeroPersonaggi() < 5) {
			personaggioDisponibile = RegistroPersonaggi.getPersonaggioInLocazione(GruppoGiocatore.getIstanza().getCoordinate());
		}
		if (personaggioDisponibile != null) {
			evento = INCONTRA_PERSONAGGIO;
		} else {
			evento = RICEVE_INFORMAZIONI;
		}
	}

	@Override
	public void crea(GruppoGiocatore g, GruppoAvversario gng) {
		// Niente da fare
		if (personaggioDisponibile != null) {
			gng.aggiungiPersonaggio(personaggioDisponibile);
		}
	}

	@Override
	public void descrivi(GruppoGiocatore g, GruppoAvversario gng) {
		StringBuilder sb = new StringBuilder(g.chiMaiuscolo()).append(" e' arrivat");
		if (GruppoGiocatore.getIstanza().getCapo().getSesso() == Personaggio.Sesso.MASCHIO) {
			sb.append("o");
		} else {
			sb.append("a");
		}
		sb.append(" ad una locanda.");
		UI.notifica(sb.toString());
	}

	@Override
	public Stato impostaAzioni(GruppoGiocatore gruppo, GruppoAvversario gng, Comando azione) {
		switch (stato) {
		case SULLA_PORTA:
			if (gruppo.getMonete() < COSTO_PASTO) {
				UI.notifica("L'oste pero' non e' disposto a fare credito...");
				return Stato.FINE_LOCAZIONE;
			}
			UI.notifica("Un cantastorie sta raccontando una vecchia storia locale.");
			List<String> fiaba = ProduttoreDiTestiCasuale.fiaba();
			for (String linea : fiaba) {
				UI.notifica(linea);
			}
			UI.impostaAzioni(Comando.PERGAMENA);
			stato = StatoInLocanda.ENTRATO;
			return Stato.IN_LOCAZIONE;

		case ENTRATO:
			UI.notifica("");
			if (gruppo.getMonete() < COSTO_PASTO * gruppo.getNumeroPersonaggiVivi()) {
				UI.notifica("Non avendo monete sufficienti per tutto il gruppo, una sola persona consuma un pasto in gran fretta. Chi lo fa?");
				UI.primoPiano(InterfacciaUtente.Finestra.STATO);
				UI.rinfresca();
				stato = StatoInLocanda.CHI_MANGIA;
				Personaggio p;
				int l = gruppo.getNumeroPersonaggiVivi();
				Azioni.clear();
				for (int i = 0; i < l; i++) {
					p = gruppo.getPersonaggio(i);
					if (p.isVivo()) {
						Azioni.add(Comando.ofPersonaggio(i));
					}
				}
				return Stato.IN_LOCAZIONE;
			} else {
				UI.notifica("Viene servito un pasto caldo, che fa riacquistare rapidamente le forze.");
				int personaggiCheHannoMangiato = 0;
				for (Personaggio personaggio : gruppo.getPersonaggiVivi()) {
					personaggio.addForza(100);
					personaggiCheHannoMangiato++;
				}
				gruppo.subMonete(COSTO_PASTO * personaggiCheHannoMangiato);

				if (evento == RICEVE_INFORMAZIONI) {
					Informazioni info = new Informazioni();
					UI.notifica(info.getDescrizione(gruppo, gng));
				}

				if (incontra(gruppo)) {
					stato = StatoInLocanda.PERSONAGGIO;
					Azioni.set(Comando.SI, Comando.NO);
					return Stato.IN_LOCAZIONE;
				} else {
					if (gruppo.getMonete() < COSTO_PERNOTTAMENTO * gruppo.getNumeroPersonaggi()) {
						UI.notifica(NO_MONETE_PERNOTTAMENTO);
						return Stato.FINE_LOCAZIONE;
					} else {
						UI.notifica(gruppo.chiMaiuscolo() + " desidera pernottare alla locanda?");
						UI.primoPiano(InterfacciaUtente.Finestra.STATO);
						UI.rinfresca();
						stato = StatoInLocanda.PERNOTTA;
						Azioni.set(Comando.SI, Comando.NO);
						return Stato.IN_LOCAZIONE;
					}
				}
			}

		case CHI_MANGIA:
			Personaggio p = gruppo.getPersonaggio(azione);
			p.addForza(100);
			gruppo.subMonete(COSTO_PASTO);
			StringBuilder sb = new StringBuilder(p.getNome());
			sb.append(" si e' rifocillat");
			sb.append(p.getSesso() == Personaggio.Sesso.MASCHIO ? 'o' : 'a');
			sb.append(" in gran fretta, ed il gruppo lascia la locanda dietro pressione dell'oste.");
			UI.notifica(sb.toString());
			return Stato.FINE_LOCAZIONE;
			
		case PERSONAGGIO:
			if (azione == Comando.SI) {
				accetta(gruppo, true);
				UI.primoPiano(InterfacciaUtente.Finestra.STATO);
				UI.rinfresca();
			} else {
				accetta(gruppo, false);
			}
			if (gruppo.getMonete() < COSTO_PERNOTTAMENTO * gruppo.getNumeroPersonaggiVivi()) {
				UI.notifica(NO_MONETE_PERNOTTAMENTO);
				return Stato.FINE_LOCAZIONE;
			} else {
				UI.notifica(gruppo.chiMaiuscolo() + " desidera pernottare alla locanda?");
				UI.primoPiano(InterfacciaUtente.Finestra.STATO);
				UI.rinfresca();
				stato = StatoInLocanda.PERNOTTA;
				Azioni.set(Comando.SI, Comando.NO);
				return Stato.IN_LOCAZIONE;
			}
			
		case PERNOTTA:
			if (azione == Comando.SI) {
				gruppo.subMonete(COSTO_PERNOTTAMENTO * gruppo.getNumeroPersonaggiVivi());
				gruppo.pernotta();
			} else {
				UI.notifica("L'oste chiede di lasciare la locanda al piu' presto.");
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
			StringBuilder sb = new StringBuilder("'").append(capo.getNome()).append("!', urla una voce. ")
					.append(capo.getPronome()).append(" si volta e vede ").append(personaggioDisponibile.getNome())
					.append(", ").append(personaggioDisponibile.getAIS()).append(personaggioDisponibile.getNomeSingolare())
					.append(", sua vecchia amicizia. ").append(personaggioDisponibile.getDescrizione())
					.append(' ').append(capo.getNome())
					.append(personaggioDisponibile.getSesso() == Personaggio.Sesso.MASCHIO ? " lo" : " la")
					.append(" vuole con se?");
			UI.notifica("");
			UI.notifica(sb.toString());
			UI.primoPiano(InterfacciaUtente.Finestra.STATO);
			UI.rinfresca();
			return true;
		}
		return false;
	}

	public void accetta(GruppoGiocatore g, boolean accetta) {
		if (accetta) {
			RegistroPersonaggi.rimuoviPersonaggioInLocazione(g.getCoordinate());
			GruppoAvversario.getIstanza().rimuoviPersonaggio(personaggioDisponibile);
			g.aggiungiPersonaggio(personaggioDisponibile);
			g.addMonete(Random.getInt(10) + 5);
			g.addIncantesimi(ClassiIncantesimo.ARIA, Random.getInt(3));
			g.addIncantesimi(ClassiIncantesimo.ACQUA, Random.getInt(3));
			g.addIncantesimi(ClassiIncantesimo.TERRA, Random.getInt(3));
			g.addIncantesimi(ClassiIncantesimo.FUOCO, Random.getInt(3));
			g.addPreziosi(Random.getInt(10));
			personaggioDisponibile = null;
			UI.rinfresca();
		} else {
			StringBuilder sb = new StringBuilder("'Pazienza. Sara' per un'altra volta.' dice ")
					.append(personaggioDisponibile.getNome()).append(", allontanandosi.");
			UI.notifica(sb.toString());
		}
	}
}
