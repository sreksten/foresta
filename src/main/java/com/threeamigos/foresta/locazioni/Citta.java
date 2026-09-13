package com.threeamigos.foresta.locazioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.EventoParagrafo;
import com.threeamigos.foresta.motore.*;
import com.threeamigos.foresta.motore.modellodati.LocazioneMD;
import com.threeamigos.foresta.motore.modellodati.TipoRiposo;
import com.threeamigos.foresta.ui.InterfacciaUtente;
import com.threeamigos.foresta.ui.UI;

public abstract class Citta extends LocazioneUnica {

	private enum StatoInCitta {
		IN_PIAZZA,
		IN_LOCANDA,
		DA_ALCHIMISTA,
		DA_ARMAIOLO
	}

	private StatoInCitta stato;
	// La locanda e la bottega dell'alchimista stanno sulla casella della città:
	// ne condividono il modello dati, così quel che vi segnano resta lì.
	private Locanda locanda;
	private Alchimista alchimista;

	protected Citta() {
		stato = StatoInCitta.IN_PIAZZA;
	}

	@Override
	public void setModelloDati(LocazioneMD modelloDati) {
		super.setModelloDati(modelloDati);
		if (locanda != null) {
			locanda.setModelloDati(modelloDati);
		}
		if (alchimista != null) {
			alchimista.setModelloDati(modelloDati);
		}
	}

	private Locanda nuovaLocanda() {
		locanda = new Locanda();
		locanda.setModelloDati(getModelloDati());
		return locanda;
	}

	private Alchimista nuovoAlchimista() {
		alchimista = new Alchimista();
		alchimista.setModelloDati(getModelloDati());
		return alchimista;
	}

	public abstract String getNome();
	
	@Override
	public void descrivi(GruppoGiocatore g, GruppoAvversario gng) {
        BusEventi.pubblica(new EventoParagrafo(g.chiMaiuscolo() + " arriva al" + getNome() +
                ". Qui è possibile cercare una locanda, il negozio di un alchimista o fare un salto dall'armaiolo prima di andare via."));
		if (g.getPreziosi() > 0) {
			g.vendePreziosi();
			UI.primoPiano(InterfacciaUtente.Finestra.MAPPA);
			UI.rinfresca();
		}
	}

	@Override
	public void crea(GruppoGiocatore g, GruppoAvversario gng) {
		nuovaLocanda().crea(g, gng);
		nuovoAlchimista().crea(g, gng);
		setCompleta(true);
	}

	private void impostaAzioniCitta() {
		ComandiPossibili.set(Comando.LOCANDA, Comando.ALCHIMISTA, Comando.ARMAIOLO,
				Comando.INVENTARIO, Comando.ESCI_DA_CITTA);
	}
	
	@Override
	public Stato impostaAzioni(GruppoGiocatore g, GruppoAvversario gng, Comando azione) {
		Stato statoRitorno;
		if (stato == StatoInCitta.IN_PIAZZA) {
			if (azione == null) {
				impostaAzioniCitta();

			} else if (azione == Comando.LOCANDA) {
				nuovaLocanda();
				locanda.descrivi(g, gng);
				statoRitorno = locanda.impostaAzioni(g, gng, null);
				if (statoRitorno == Stato.IN_LOCAZIONE) {
					stato = StatoInCitta.IN_LOCANDA;
				} else {
					stato = StatoInCitta.IN_PIAZZA;
					impostaAzioniCitta();
				}

			} else if (azione == Comando.ALCHIMISTA) {
				nuovoAlchimista();
				alchimista.descrivi(g, gng);
				statoRitorno = alchimista.impostaAzioni(g, gng, null);
				if (statoRitorno == Stato.IN_LOCAZIONE) {
					stato = StatoInCitta.DA_ALCHIMISTA;
				} else {
					stato = StatoInCitta.IN_PIAZZA;
					impostaAzioniCitta();
				}

			} else if (azione == Comando.ARMAIOLO) {
				ScambiatoreArtefatti scambiatoreArtefatti = RegistroArtefatti.getScambiatorePerLocazione(g.getCoordinate());
				UI.impostaAutomaArmaiolo(new AutomaAcquisti(g, scambiatoreArtefatti));
				ComandiPossibili.set(Comando.ANNULLA);
				UI.armaiolo();
				stato = StatoInCitta.DA_ARMAIOLO;

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

		} else if (stato == StatoInCitta.DA_ARMAIOLO) {
			if (azione == Comando.ANNULLA) {
				UI.mostraSchermataGioco();
				UI.primoPiano(InterfacciaUtente.Finestra.GRAFICA);
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
