package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.incantesimi.ClasseIncantesimo;
import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.motore.Comando;
import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.motore.Dado;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.Statistiche;
import com.threeamigos.foresta.tools.Misc;

public class Cofano extends OggettoBase implements Oggetto {

	public Cofano() {
		super();
	}
	
	public Cofano(int q) {
		quantita = q;
	}

	public String getAIS() {
		return Misc.UN;
	}

	public String getAIP() {
		return Misc.ALCUNI;
	}

	public String getADS() {
		return Misc.IL;
	}

	public String getADP() {
		return Misc.I;
	}

	public String getNomeSingolare() {
		return "cofano";
	}

	public String getNomePlurale() {
		return "cofani";
	}

	public ClassiOggetto getClasse() {
		return ClassiOggetto.COFANO;
	}

	@Override
	public boolean prendi(GruppoGiocatore gruppo, Comando azione) {
		ClassiLocazione classeLocazione = gruppo.getClasseLocazioneCorrente();
		int min;
		int max;
		int tipo;
		if (classeLocazione.getTipoLocazione() == ClassiLocazione.TipoLocazione.CASTELLO) {
			min = 1;
			max = 6;
		} else {
			min = 0;
			max = 7;
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < quantita; i++) {
			tipo = Dado.tira(min, max);
			// Di rado, al posto dell'esito solito, una pergamena o un artefatto casuale, che vanno nel gruppo
			Artefatto trovato = artefattoRaro();
			sb.append("Aprendo il");
			if (quantita > 1) {
				sb.append(' ').append(Misc.getOrdinaleM(i + 1, false));
			}
			if (Dado.tira(2) == 1) {
				sb.append(" cofano, ");
			} else {
				sb.append(" forziere, ");
			}
			if (i == 0) {
				sb.append(gruppo.chi()).append(' ');
			}
			if (trovato != null) {
				gruppo.addArtefatto(trovato);
				sb.append("trova ").append(trovato.getNomeCompleto()).append(": finisce nell'inventario del gruppo.");
			} else if (tipo == 0) {
				sb.append("non trova nulla.");
			} else if (tipo == 1) {
				sb.append("trova una pergamena con un ");
				ClasseIncantesimo classeIncantesimo = ClasseIncantesimo.casuale();
				sb.append(classeIncantesimo.getNomeSingolare()).append('.');
				gruppo.addIncantesimi(classeIncantesimo, 1);
			} else if (tipo == 2) {
				sb.append("trova una pergamena con tre ");
				ClasseIncantesimo classeIncantesimo = ClasseIncantesimo.casuale();
				sb.append(classeIncantesimo.getNomePlurale()).append('.');
				gruppo.addIncantesimi(classeIncantesimo, 3);
			} else if (tipo == 3) {
				sb.append("trova una pozione della salute.");
				gruppo.addPozioniSalute(1);
			} else if (tipo == 4) {
				sb.append("trova una pozione della salute grande.");
				gruppo.addPozioniSaluteGrande(1);
			} else if (tipo == 5) {
				sb.append("trova una pozione della magia.");
				gruppo.addPozioniMagia(1);
			} else if (tipo == 6) {
				sb.append("trova alcune monete d'oro.");
				gruppo.addMonete(Dado.tira(5, 10));
			} else if (tipo == 7) {
				sb.append("trova alcune gemme.");
				gruppo.addPreziosi(Dado.tira(5, 10));
			}
			if (i < quantita - 1) {
				sb.append(' ');
			}
		}
		BusEventi.pubblica(new NotificaTestoFrase(sb.toString()));
		return super.prendi(gruppo, azione);
	}

	/**
	 * Con probabilità 5% una pergamena, con un altro 5% un artefatto casuale, altrimenti null.
	 */
	static Artefatto artefattoRaro(double tiro, GeneratoreArtefatti generatore, int livello) {
		if (tiro < Costanti.COFANO_PROBABILITA_PERGAMENA) {
			return generatore.generaPergamena(livello);
		}
		if (tiro < Costanti.COFANO_PROBABILITA_PERGAMENA + Costanti.COFANO_PROBABILITA_ARTEFATTO) {
			return generatore.generaArtefattoCasuale(livello);
		}
		return null;
	}

	private static Artefatto artefattoRaro() {
		return artefattoRaro(Math.random(), GeneratoreArtefatti.istanza(), Statistiche.getLivello());
	}
}
