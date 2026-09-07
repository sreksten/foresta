package com.threeamigos.foresta.locazioni;

import com.threeamigos.foresta.motore.*;
import com.threeamigos.foresta.motore.modellodati.TipoRiposo;
import com.threeamigos.foresta.ui.InterfacciaUtente;
import com.threeamigos.foresta.ui.UI;

public abstract class Citta extends LocazioneUnica {

	private enum StatoInCitta {
		IN_PIAZZA,
		IN_LOCANDA,
		DA_ALCHIMISTA
	}

	private StatoInCitta stato;
	// La Locanda è un singleton condiviso da tutte le città: la visita va segnata
	// qui, sulla città che l'ha ospitata, non sulla locanda.
	private boolean locandaVisitata;
	private final Locanda locanda = Locanda.getIstanza();
	private final Alchimista alchimista = Alchimista.getIstanza();

	@Override
	public void reimposta() {
		super.reimposta();
		locanda.reimposta();
		alchimista.reimposta();
		stato = StatoInCitta.IN_PIAZZA;
		locandaVisitata = false;
	}

	/**
	 * Vero se durante questa permanenza in città il gruppo è entrato nella locanda
	 * e ne è poi uscito.
	 */
	public boolean isLocandaVisitata() {
		return locandaVisitata;
	}

	private void registraUscitaDaLocanda() {
		locandaVisitata = locandaVisitata || locanda.isEntrato();
	}

	public abstract String getNome();
	
	@Override
	public void descrivi(GruppoGiocatore g, GruppoAvversario gng) {
        String sb = g.chiMaiuscolo() + " arriva al" + getNome() +
                ". Qui è possibile cercare una locanda o il negozio di un alchimista prima di andare via.";
		UI.notifica(sb);
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
		completa = true;
	}

	public abstract String getNomeLocanda();

	/**
	 * Il solo nome della locanda, senza la preposizione che getNomeLocanda() aggiunge
	 * per comporre una frase.
	 */
	public abstract String getNomeSempliceLocanda();

	private void impostaAzioniCitta() {
		ComandiPossibili.set(Comando.LOCANDA, Comando.ALCHIMISTA, Comando.ESCI_DA_CITTA);
	}
	
	@Override
	public Stato impostaAzioni(GruppoGiocatore g, GruppoAvversario gng, Comando azione) {
		Stato statoRitorno;
		if (stato == StatoInCitta.IN_PIAZZA) {
			if (azione == null) {
				impostaAzioniCitta();

			} else if (azione == Comando.LOCANDA) {
				locanda.reimposta();
				UI.notifica(g.chiMaiuscolo() + " è " + getNomeLocanda() + '.');
				statoRitorno = locanda.impostaAzioni(g, gng, null);
				if (statoRitorno == Stato.IN_LOCAZIONE) {
					stato = StatoInCitta.IN_LOCANDA;
				} else {
					registraUscitaDaLocanda();
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
				registraUscitaDaLocanda();
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

	public TipoRiposo getTipoRiposo() {
		throw new IllegalArgumentException("Non si può riposare all'aperto in città");
	}
}
