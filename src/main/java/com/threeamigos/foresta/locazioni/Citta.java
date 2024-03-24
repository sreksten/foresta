package com.threeamigos.foresta.locazioni;

import com.threeamigos.foresta.motore.Azioni;
import com.threeamigos.foresta.motore.Comando;
import com.threeamigos.foresta.motore.GruppoAvversario;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.Stato;
import com.threeamigos.foresta.ui.UI;
import com.threeamigos.foresta.ui.InterfacciaUtente;

public abstract class Citta extends LocazioneBase {

	private enum StatoInCitta {
		IN_PIAZZA,
		IN_LOCANDA,
		DA_ALCHIMISTA
	}

	protected String descizioneAzioni = " puo' cercare una Locanda o il negozio di un Alchimista, o lasciare il posto.";

	private StatoInCitta stato;
	private Locanda locanda = Locanda.getIstanza();
	private Alchimista alchimista = Alchimista.getIstanza();

	@Override
	public void reimposta() {
		super.reimposta();
		locanda.reimposta();
		alchimista.reimposta();
		stato = StatoInCitta.IN_PIAZZA;
	}

	abstract String getNome();
	
	@Override
	public void descrivi(GruppoGiocatore g, GruppoAvversario gng) {
		StringBuilder sb = new StringBuilder(g.chiMaiuscolo()).append(" arriva al").append(getNome())
				.append(". Qui e' possibile cercare una locanda o il negozio di un alchimista prima di andare via.");
		UI.notifica(sb.toString());
		if (g.getPreziosi() > 0) {
			g.vendePreziosi();
			UI.primoPiano(InterfacciaUtente.Finestra.MAPPA);
			UI.rinfresca();
		}
	}

	@Override
	public void crea(GruppoGiocatore g, GruppoAvversario gng) {
		locanda.crea(g, gng);
		alchimista.crea(g, gng);
	}

	public abstract String getNomeLocanda();

	private final void impostaAzioniCitta() {
		Azioni.set(Comando.LOCANDA, Comando.ALCHIMISTA, Comando.ESCI_DA_CITTA);
	}
	
	@Override
	public Stato impostaAzioni(GruppoGiocatore g, GruppoAvversario gng, Comando azione) {
		Stato statoRitorno;
		if (stato == StatoInCitta.IN_PIAZZA) {
			if (azione == null) {
				impostaAzioniCitta();

			} else if (azione == Comando.LOCANDA) {
				locanda.reimposta();
				UI.notifica(new StringBuilder(g.chiMaiuscolo()).append(" e' ").append(getNomeLocanda()).append('.').toString());
				statoRitorno = locanda.impostaAzioni(g, gng, null);
				if (statoRitorno == Stato.IN_LOCAZIONE) {
					stato = StatoInCitta.IN_LOCANDA;
				} else {
					stato = StatoInCitta.IN_PIAZZA;
					impostaAzioniCitta();
				}

			} else if (azione == Comando.ALCHIMISTA) {
				alchimista.reimposta();
				alchimista.descrivi(g, gng);
				statoRitorno = alchimista.impostaAzioni(g, gng, null);
				if (statoRitorno == Stato.IN_LOCAZIONE) {
					stato = StatoInCitta.DA_ALCHIMISTA;
				} else {
					stato = StatoInCitta.IN_PIAZZA;
					impostaAzioniCitta();
				}

			} else if (azione == Comando.ESCI_DA_CITTA) {
				return Stato.FINE_LOCAZIONE;
			}

		} else if (stato == StatoInCitta.IN_LOCANDA) {
			statoRitorno = locanda.impostaAzioni(g, gng, azione);
			if (statoRitorno == Stato.FINE_LOCAZIONE) {
				impostaAzioniCitta();
				stato = StatoInCitta.IN_PIAZZA;
			}

		} else if (stato == StatoInCitta.DA_ALCHIMISTA) {
			statoRitorno = alchimista.impostaAzioni(g, gng, azione);
			if (statoRitorno == Stato.FINE_LOCAZIONE) {
				impostaAzioniCitta();
				stato = StatoInCitta.IN_PIAZZA;
			}
		}
		return Stato.IN_LOCAZIONE;
	}
}
