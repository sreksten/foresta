package com.threeamigos.foresta.locazioni;

import java.util.List;

import com.threeamigos.foresta.incantesimi.ClassiIncantesimo;
import com.threeamigos.foresta.incantesimi.Incantesimo;
import com.threeamigos.foresta.motore.Azioni;
import com.threeamigos.foresta.motore.Comando;
import com.threeamigos.foresta.motore.GruppoAvversario;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.ProduttoreDiTestiCasuale;
import com.threeamigos.foresta.motore.Stato;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.ui.InterfacciaUtente;
import com.threeamigos.foresta.ui.UI;

public class Alchimista extends LocazioneBase implements Locazione {

	private static Alchimista istanza = new Alchimista();
	
	private Alchimista() {
	}
	
	public static Alchimista getIstanza() {
		return istanza;
	}
	
	private enum StatoDaAlchimista {
		SULLA_PORTA,
		ENTRATO,
		INCANTESIMI
	}

	private static final String ARRIVEDERCI = "'Arrivederci, e buona fortuna!'";
	private static final String DICE = ", dice l'alchimista.";
	private static final String CHIEDE = ", chiede l'alchimista.";
	private StatoDaAlchimista stato;
	private GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();

	@Override
	public ClassiLocazione getClasseLocazione() {
		return ClassiLocazione.CITTA_FLEENA;
	}

	@Override
	public void reimposta() {
		super.reimposta();
		stato = StatoDaAlchimista.SULLA_PORTA;
	}

	@Override
	public void crea(GruppoGiocatore g, GruppoAvversario gng) {
		// nulla da fare
	}

	@Override
	public void descrivi(GruppoGiocatore g, GruppoAvversario gng) {
		UI.notifica("");
		UI.notifica(g.chiMaiuscolo() + " arriva alla bottega di un alchimista.");
	}

	@Override
	public Stato impostaAzioni(GruppoGiocatore g, GruppoAvversario gng, Comando azione) {
		switch (stato) {
		case SULLA_PORTA:
			if (gruppo.getMonete() < 10 && !isIncantesimiAcquistabili() ) {
				UI.notifica(new StringBuilder("'Buongiorno! Mi dispiace ma non posso fare credito.'").append(DICE).toString());
				return Stato.FINE_LOCAZIONE;
			}
			UI.notifica("L'alchimista e' intento a produrre l'oroscopo della giornata:");
			List<String> oroscopo = ProduttoreDiTestiCasuale.oroscopo();
			for (String linea : oroscopo) {
				UI.notifica(linea);
			}
			UI.impostaAzioni(Comando.PERGAMENA);
			stato = StatoDaAlchimista.ENTRATO;
			return Stato.IN_LOCAZIONE;

		case ENTRATO:
			StringBuilder sb = new StringBuilder();
			int numeroPersonaggiVivi = gruppo.getNumeroPersonaggiVivi();
			boolean aumentareMagia = gruppo.getMonete() > 10;
			boolean aumentareMagiaGruppo = gruppo.getMonete() >= 30 && numeroPersonaggiVivi > 1;
			boolean incantesimiAcquistabili = isIncantesimiAcquistabili();
			if (aumentareMagia) {
				if (numeroPersonaggiVivi == 1) {
					if (g.getCapo().getSesso() == Personaggio.Sesso.MASCHIO)
						sb.append("'Benvenuto");
					else
						sb.append("'Benvenuta");
					sb.append("'! Posso aumentare il tuo potere magico");
				} else {
					sb.append("'Benvenuti! Posso aumentare il potere magico di uno di voi");
				}
				sb.append(" per dieci monete");
				if (aumentareMagiaGruppo) {
					sb.append(", o di tutto il gruppo per trenta");
				}
				sb.append(".");
				if (incantesimiAcquistabili) {
					sb.append(" O ");
					if (numeroPersonaggiVivi == 1) {
						sb.append("preferisci");
					} else {
						sb.append("preferite");
					}
					sb.append(" comprare incantesimi?");
				}
				sb.append("'").append(CHIEDE);
			} else if (incantesimiAcquistabili) {
				if (numeroPersonaggiVivi == 1) {
					if (g.getCapo().getSesso() == Personaggio.Sesso.MASCHIO)
						sb.append("'Benvenuto! Vuoi");
					else
						sb.append("'Benvenuta! Vuoi");
				} else {
					sb.append("'Benvenuti! Volete");
				}
				sb.append(" comprare incantesimi?'").append(CHIEDE);
			} else {
				sb.append("'Buongiorno! Mi dispiace ma non posso fare credito.'").append(DICE);
			}
			UI.notifica(sb.toString());
			imposta();
			if (azione == Comando.PERSONAGGIO_1 ||
			azione == Comando.PERSONAGGIO_2 ||
			azione == Comando.PERSONAGGIO_3 ||
			azione == Comando.PERSONAGGIO_4 ||
			azione == Comando.PERSONAGGIO_5) {
				//TODO la gestione acquisti dall'alchimista può essere migliorata rimanendo in locazione finché si hanno monete
				if (g.getMonete() >= 10) {
					Personaggio p = g.getPersonaggio(azione);
					g.subMonete(10);
					p.addMagia(20);
					UI.primoPiano(InterfacciaUtente.Finestra.STATO);
					UI.primoPiano(InterfacciaUtente.Finestra.MAPPA);
					UI.rinfresca();
					UI.notifica(ARRIVEDERCI + DICE);
					return Stato.IN_LOCAZIONE;
				} else {
					UI.notifica("'Non hai abbastanza monete per pagare i miei servigi.'" + DICE);
					imposta();
					return Stato.FINE_LOCAZIONE;
				}

			} else if (azione == Comando.GRUPPO) {
				if (g.getMonete() >= 30) {
					g.subMonete(30);
					int l = g.getNumeroPersonaggi();
					Personaggio p;
					for (int i = 0; i < l; i++) {
						p = g.getPersonaggio(i);
						if (p.isVivo())
							p.addMagia(20);
					}
					UI.primoPiano(InterfacciaUtente.Finestra.STATO);
					UI.primoPiano(InterfacciaUtente.Finestra.MAPPA);
					UI.rinfresca();
					UI.notifica(ARRIVEDERCI + DICE);
					return Stato.FINE_LOCAZIONE;
				} else {
					UI.notifica("'Non avete abbastanza monete per pagare i miei servigi.'" + DICE);
					imposta();
					return Stato.FINE_LOCAZIONE;
				}

			} else if (azione == Comando.INCANTESIMO) {
				stato = StatoDaAlchimista.INCANTESIMI;
				UI.primoPiano(InterfacciaUtente.Finestra.INCANTESIMI);
				UI.primoPiano(InterfacciaUtente.Finestra.MAPPA);
				UI.rinfresca();
				impostaIncantesimi();
				return Stato.IN_LOCAZIONE;

			} else if (azione == Comando.NO_INCANTESIMO) {
				UI.notifica(ARRIVEDERCI + DICE);
				return Stato.FINE_LOCAZIONE;

			} else {
				imposta();
				return Stato.IN_LOCAZIONE;
			}

		case INCANTESIMI:
			if (azione == Comando.NO_INCANTESIMO) {
				UI.notifica(ARRIVEDERCI + DICE);
				return Stato.FINE_LOCAZIONE;
			} else {
				if (azione != null) {
					Incantesimo i = ClassiIncantesimo.ofComando(azione);
					int costo = i.getCostoAcquisto();
					if (g.getMonete() < costo)
						UI.notifica("'Questo incantesimo costa troppo per le tue tasche.'" + DICE);
					else {
						g.subMonete(costo);
						g.addIncantesimi(i.getClasse(), 1);
						UI.primoPiano(InterfacciaUtente.Finestra.INCANTESIMI);
						UI.primoPiano(InterfacciaUtente.Finestra.MAPPA);
						UI.rinfresca();
					}
				}
				impostaIncantesimi();
				return Stato.IN_LOCAZIONE;
			}
		}
		return Stato.FINE_LOCAZIONE;
	}
	
	private final void imposta() {
		Azioni.clear();
		if (gruppo.getMonete() >= 10) {
			int l = gruppo.getNumeroPersonaggi();
			Personaggio personaggio;
			for (int i = 0; i < l; i++) {
				personaggio = gruppo.getPersonaggio(i);
				if (personaggio.isVivo()) {
					Azioni.add(Comando.ofPersonaggio(i));
				}
			}
		}
		if (gruppo.getNumeroPersonaggiVivi() > 1 && gruppo.getMonete() > 30) {
			Azioni.add(Comando.GRUPPO);
		}
		if (isIncantesimiAcquistabili()) {
			Azioni.add(Comando.INCANTESIMO);
		}
		Azioni.add(Comando.NO_INCANTESIMO);
	}

	private final boolean isIncantesimiAcquistabili() {
		for (ClassiIncantesimo classiIncantesimo : ClassiIncantesimo.values()) {
			if (classiIncantesimo.getIstanza().getCostoAcquisto() <= gruppo.getMonete()) {
				return true;
			}
		}
		return false;
	}

	private final void impostaIncantesimi() {
		Azioni.clear();
		for (ClassiIncantesimo classiIncantesimo : ClassiIncantesimo.values()) {
			if (classiIncantesimo.getIstanza().getCostoAcquisto() <= gruppo.getMonete()) {
				Azioni.add(classiIncantesimo.getComandoDiAttivazione());
			}
		}
		Azioni.add(Comando.NO_INCANTESIMO);
	}
}
