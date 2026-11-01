package com.threeamigos.foresta.offerte;

import com.threeamigos.foresta.tipi.CostantiPersonaggi;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoMostraFinestraStato;
import com.threeamigos.foresta.gruppi.GruppoAvversario;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.tipi.TipoMotivoNelGruppo;

import java.util.List;
import com.threeamigos.foresta.testi.Testi;

/**
 * L'offerta di un avversario che si unisce al gruppo per un periodo limitato, senza chiedere niente. Non si può se il gruppo è già al completo.
 */
public class AiutoGratuito implements Offerta {

	private Personaggio personaggio;
	private final int durata;

	public AiutoGratuito() {
		durata = Dado.tira(6);
	}

	@Override
	public boolean isFattibile(GruppoGiocatore gruppo, GruppoAvversario gng) {
		// A gruppo pieno nessuno si puo' unire (vedi anche PersonaggioBase.getOfferta)
		if (gruppo.getNumeroPersonaggi() >= CostantiPersonaggi.MAX_PERSONAGGI_GRUPPO_TOTALE) {
			return false;
		}
		List<Personaggio> personaggiAvversari = gng.getPersonaggiVivi();
		if (!personaggiAvversari.isEmpty()) {
			personaggio = personaggiAvversari.get(0);
			return true;
		}
		return false;
	}

	@Override
	// Qualcuno con se ha comunque un costo...
	public boolean isGratuita(GruppoGiocatore g, GruppoAvversario gng) {
		return false;
	}

	@Override
	public String getDescrizione(GruppoGiocatore gruppo, GruppoAvversario gruppoAvversario) {
		Personaggio capo = gruppoAvversario.getCapo();
		String sesso = capo.getSesso() == Personaggio.Sesso.MASCHIO ? "M" : "F";
		boolean inPiu = gruppoAvversario.getNumeroPersonaggiVivi() > 1;
		String chi = gruppo.getNumeroPersonaggiVivi() > 1 ? Testi.formatta("gruppo", "CHI") : gruppo.getCapo().getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE);
		return inPiu ? Testi.formatta("offerte", "AIUTO_GRATUITO.GRUPPO." + sesso, chi)
				: Testi.formatta("offerte", "AIUTO_GRATUITO.SINGOLO." + sesso,
						capo.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE, Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA), chi);
	}

	@Override
	public void accetta(GruppoGiocatore gruppo, GruppoAvversario gruppoAvversario) {
		// il primo turno scatta subito
		personaggio.setMotivoNelGruppo(TipoMotivoNelGruppo.AIUTO_GRATUITO);
		personaggio.setTempo(durata + 1);
		gruppo.aggiungiPersonaggio(personaggio);
		BusEventi.pubblica(new InternoMostraFinestraStato());
	}
}