package com.threeamigos.foresta.canti;

import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.combattimento.AnnuncioDellAzione;

import com.threeamigos.foresta.tipi.TipoAzione;
import com.threeamigos.foresta.tipi.CostantiCanti;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.gruppi.Gruppo;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.TipoCanto;

import java.util.Optional;

/**
 * Il canto che cura il personaggio più ferito del gruppo (la quota di salute più bassa, il cantore compreso): circa
 * CANTO_CURATIVO_BASE × √livello del cantore, aumentato dalla sua SAGGEZZA.
 */
public class CantoCurativo implements Canto {

	private final int livello;
	private final int saggezza;

	public CantoCurativo(Personaggio cantore) {
		this.livello = cantore.getLivello();
		this.saggezza = cantore.get(TipoAttributo.SAGGEZZA);
	}

	@Override
	public TipoCanto getTipo() {
		return TipoCanto.CURATIVO;
	}

	/**
	 * Quanta salute restituisce, prima del limite del massimo di chi la riceve
	 */
	public int getCura() {
		return (int) Math.round(CostantiCanti.CANTO_CURATIVO_BASE * Math.sqrt(livello)
				* (1.0d + saggezza / CostantiCanti.CANTO_CURATIVO_DIVISORE_SAGGEZZA));
	}

	@Override
	public boolean canta(Personaggio cantore, Gruppo gruppo, Gruppo gruppoAvversario) {
		return cantaSu(cantore, gruppo.piuFerito().orElse(null), gruppo, gruppoAvversario);
	}

	/**
	 * Cura il bersaglio scelto (se è ferito); {@link #canta} sceglie il più ferito del gruppo
	 */
	@Override
	public boolean cantaSu(Personaggio cantore, Personaggio scelto, Gruppo gruppo, Gruppo gruppoAvversario) {
		Optional<Personaggio> ferito = Optional.ofNullable(scelto)
				.filter(p -> p.isVivo() && p.isCurabile() && p.getSalute() < p.getSaluteMassima());
		String nomeCantore = cantore.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
				Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA);
		if (!ferito.isPresent()) {
			BusEventi.pubblica(new NotificaTestoFrase(nomeCantore + " intona un canto curativo, ma nessuno ha bisogno di cure."));
			return false;
		}
		Personaggio bersaglio = ferito.get();
		AnnuncioDellAzione.azione(cantore, bersaglio, TipoAzione.CANTO_CURATIVO);
		int saluteInizio = bersaglio.getSalute();
		bersaglio.addSalute(getCura());
		cantore.subMagia(getCostoLancio());
		BusEventi.pubblica(new NotificaTestoFrase(nomeCantore + " canta per "
				+ bersaglio.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE)
				+ ": guarisce di " + (bersaglio.getSalute() - saluteInizio) + "."));
		return true;
	}
}
