package com.threeamigos.foresta.offerte;

import com.threeamigos.foresta.economia.Cassa;
import com.threeamigos.foresta.tipi.CostantiPersonaggi;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoMostraFinestraStato;
import com.threeamigos.foresta.gruppi.GruppoAvversario;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.tipi.TipoApproccio;
import com.threeamigos.foresta.tipi.TipoMotivoNelGruppo;

import java.util.List;
import com.threeamigos.foresta.testi.Testi;

/**
 * L'offerta di un avversario che si unisce al gruppo per un periodo limitato, in cambio di monete. Non si può se il gruppo è al completo o non ha le monete.
 */
public class AiutoMercenario implements Offerta {

	private Personaggio personaggio;
	private final int costo;
	// Perché entra nel gruppo: chi è stato corrotto non lo fa per sua scelta (vedi TipoMotivoNelGruppo.CORRUZIONE)
	private final TipoMotivoNelGruppo motivo;

	/**
	 * @param approccio come il gruppo ha ottenuto l'offerta: facendo amicizia o corrompendo
	 */
	public AiutoMercenario(TipoApproccio approccio) {
		costo = Dado.tira(6);
		motivo = approccio == TipoApproccio.CORRUZIONE ? TipoMotivoNelGruppo.CORRUZIONE : TipoMotivoNelGruppo.AIUTO_MERCENARIO;
	}

	@Override
	public boolean isFattibile(GruppoGiocatore gruppo, GruppoAvversario gruppoAvversario) {
		// A gruppo pieno nessuno si puo' unire (vedi anche PersonaggioBase.getOfferta)
		if (gruppo.getNumeroPersonaggi() >= CostantiPersonaggi.MAX_PERSONAGGI_GRUPPO_TOTALE) {
			return false;
		}
		if (Cassa.getMonete() < costo) {
			return false;
		}
		List<Personaggio> personaggiAvversari = gruppoAvversario.getPersonaggiVivi();
		if (!personaggiAvversari.isEmpty()) {
			personaggio = personaggiAvversari.get(0);
			return true;
		}
		return false;
	}

	@Override
	public boolean isGratuita(GruppoGiocatore gruppo, GruppoAvversario gruppoAvversario) {
		return false;
	}

	@Override
	public String getDescrizione(GruppoGiocatore gruppo, GruppoAvversario gruppoAvversario) {
		Personaggio capo = gruppoAvversario.getCapo();
		String sesso = capo.getSesso() == Personaggio.Sesso.MASCHIO ? "M" : "F";
		boolean inPiu = gruppoAvversario.getNumeroPersonaggiVivi() > 1;
		String chi = gruppo.getNumeroPersonaggiVivi() > 1 ? Testi.formatta("gruppo", "CHI") : gruppo.getCapo().getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE);
		String prezzo = costo == 1 ? Testi.formatta("offerte", "MONETE_UNA") : Testi.formatta("offerte", "MONETE_N", costo);
		return inPiu ? Testi.formatta("offerte", "AIUTO_MERCENARIO.GRUPPO." + sesso, prezzo, chi)
				: Testi.formatta("offerte", "AIUTO_MERCENARIO.SINGOLO." + sesso, prezzo,
						capo.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE), chi);
	}

	@Override
	public void accetta(GruppoGiocatore gruppo, GruppoAvversario gruppoAvversario) {
		// il primo turno scatta subito
		personaggio.setMotivoNelGruppo(motivo);
		personaggio.setTempo(costo + 1);
		gruppo.aggiungiPersonaggio(personaggio);
		Cassa.subMonete(costo);
		BusEventi.pubblica(new InternoMostraFinestraStato());
	}
}