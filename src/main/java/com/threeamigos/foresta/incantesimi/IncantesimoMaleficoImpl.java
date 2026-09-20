package com.threeamigos.foresta.incantesimi;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.motore.Gruppo;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.Logger;
import com.threeamigos.foresta.motore.Statistiche;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tools.Misc;

import java.util.List;

/*
 * Incantesimi originali su ZX Spectrum:
 * Sonno
 * Lievitazione
 * Invisibilità
 */

public abstract class IncantesimoMaleficoImpl implements IncantesimoMalefico {

	protected final int livello;
	protected int totale;
	protected int bersagli;
	protected int feriti;
	protected int uccisi;

	protected IncantesimoMaleficoImpl(int livello) {
		this.livello = livello;
	}

	public int getLivello() {
		return livello;
	}

	public TipoIncantesimo getTipo() {
		return TipoIncantesimo.MALEFICO;
	}

	public void formula(Personaggio formulante, Personaggio personaggioBersaglio, Gruppo gruppoBersaglio) {
		if (getClasse().getPortata() == PortataIncantesimo.GLOBALE) {
			formula(formulante);
		} else if (personaggioBersaglio != null) {
			formula(formulante, personaggioBersaglio);
		} else if (gruppoBersaglio != null) {
			formula(formulante, gruppoBersaglio);
		} else {
			throw new IllegalArgumentException("Non so come formulare questo incantesimo!");
		}
		if (!formulante.isPNG()) {
			BusEventi.pubblica(new NotificaTestoFrase(risultato(formulante)));
		}
	}

	private void formula(Personaggio formulante, Gruppo gruppoBersaglio) {

		List<Personaggio> personaggiVivi = gruppoBersaglio.getPersonaggiVivi();

		String nomeBersaglio;
		if (personaggiVivi.size() > 1) {
			nomeBersaglio = "il gruppo";
		} else {
			nomeBersaglio = gruppoBersaglio.getPersonaggiVivi().get(0)
					.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
							Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA);
		}

		String nomeFormulante = formulante.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
				Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA);

		BusEventi.pubblica(new NotificaTestoFrase(nomeFormulante + " formula un " + getClasse().getNomeSingolare() + " contro " + nomeBersaglio + "."));

		totale = gruppoBersaglio.getNumeroPersonaggi();
		bersagli = formulante.getBersagli();
		if (bersagli > personaggiVivi.size()) {
			bersagli = personaggiVivi.size();
		}
		int danni = formulante.getModificaDanniMagia(getDanni());
		if (getClasse().getPortata() != PortataIncantesimo.GRUPPO && bersagli > 1) {
			Logger.log("I danni vengono suddivisi tra i personaggi bersaglio");
			danni /= bersagli;
		}
		Logger.log("Bersagli vivi: " + personaggiVivi.size() + " -> bersagli: " + bersagli + ", danni=" + danni);

		for (int i = 0; i < bersagli; i++) {
			Personaggio personaggioBersaglio = personaggiVivi.get(i);
			formulaImpl(formulante, personaggioBersaglio, danni, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
		}
		formulante.subMagia(getCostoLancio());
	}

	public void formula(Personaggio formulante, Personaggio personaggioBersaglio) {
		String nomeBersaglio = personaggioBersaglio.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
				Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA);
		BusEventi.pubblica(new NotificaTestoFrase(formulante.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
				Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA) + " formula un " + getClasse().getNomeSingolare() + " contro " +
				nomeBersaglio + "."));

		int danni = formulante.getModificaDanniMagia(getDanni());
		formulaImpl(formulante, personaggioBersaglio, danni, Personaggio.NotificaFerite.SI, Personaggio.NotificaMorte.SI);
		formulante.subMagia(getCostoLancio());
	}
	
	private void formulaImpl(Personaggio formulante, Personaggio personaggioBersaglio, int danni, Personaggio.NotificaFerite notificaFerite, Personaggio.NotificaMorte notificaMorte) {
		if (!personaggioBersaglio.isImmuneAIncantesimo(getClasse())) {
			personaggioBersaglio.subSalute(danni, formulante, notificaFerite, notificaMorte);
			if (personaggioBersaglio.isVivo()) {
				feriti++;
			} else {
				uccisi++;
				if (GruppoGiocatore.getIstanza().contiene(formulante)) {
					Statistiche.addMostroUcciso(personaggioBersaglio.getClasse());
					Statistiche.addPunti(personaggioBersaglio.getSaluteMassima());
					GruppoGiocatore.getIstanza().addPuntiEsperienza(personaggioBersaglio.getPuntiEsperienza());
				}
			}
		} else {
			Logger.log("Mostro immune a incantesimo");
		}
		Logger.log("Feriti: " + feriti + ", uccisi: " + uccisi);
	}

	protected String risultato(Personaggio formulante) {
		Logger.log("Totale: " + totale + ", bersagli: " + bersagli + ", feriti: " + feriti + ", uccisi: " + uccisi);

		String s = formulante.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE, Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA);

		StringBuilder sb = new StringBuilder(s);
		if (uccisi == totale && totale > 1) {
			sb.append(" ha formulato l'incantesimo alla perfezione, eliminando tutti i suoi avversari.");
		} else {
			if (uccisi > 0) {
				sb.append(" ha eliminato ");
				if (uccisi == 1) {
					if (totale == 1) {
						sb.append("il suo avversario");
					} else {
						sb.append("un suo avversario");
					}
				} else {
					sb.append(Misc.getCardinaleM(uccisi)).append(" dei suoi avversari");
				}
				if (feriti > 0) {
					sb.append(" e ne ha ");
					if (feriti == 1) {
						sb.append("ferito uno");
					} else {
						sb.append("feriti ").append(Misc.getCardinaleM(feriti));
					}
				}
				sb.append('.');
			} else {
				sb.append(" ha ferito ");
				if (totale == 1) {
					sb.append("il suo avversario.");
				} else {
					if (feriti == totale) {
						sb.append(" tutti i suoi avversari.");
					} else {
						sb.append(Misc.getCardinaleM(feriti)).append(" dei suoi avversari.");
					}
				}
			}
		}
		return sb.toString();
	}

	public void formula(Personaggio formulante) {
		BusEventi.pubblica(new NotificaTestoFrase(formulante.getNome(
				Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
				Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA) +
				" formula un " + getClasse().getNomeSingolare() + "."));
	}
}