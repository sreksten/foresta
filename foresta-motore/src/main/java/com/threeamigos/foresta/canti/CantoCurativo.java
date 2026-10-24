package com.threeamigos.foresta.canti;

import com.threeamigos.foresta.tipi.CostantiCanti;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.motore.Gruppo;
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
		this.saggezza = cantore.getSaggezza();
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

	/**
	 * Il personaggio vivo con la quota di salute più bassa, se qualcuno è ferito (a parità, il primo del gruppo). Chi non
	 * si può curare (un non morto nel gruppo) non conta
	 */
	public static Optional<Personaggio> piuFerito(Gruppo gruppo) {
		Personaggio piuFerito = null;
		double quotaMinima = 1.0d;
		for (Personaggio personaggio : gruppo.getPersonaggiVivi()) {
			if (!personaggio.isCurabile()) {
				continue;
			}
			double quota = (double) personaggio.getSalute() / personaggio.getSaluteMassima();
			if (quota < quotaMinima) {
				quotaMinima = quota;
				piuFerito = personaggio;
			}
		}
		return Optional.ofNullable(piuFerito);
	}

	@Override
	public boolean canta(Personaggio cantore, Gruppo gruppo, Gruppo gruppoAvversario) {
		return cantaSu(cantore, piuFerito(gruppo).orElse(null), gruppo, gruppoAvversario);
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
		int saluteInizio = bersaglio.getSalute();
		bersaglio.addSalute(getCura());
		cantore.subMagia(getCostoLancio());
		BusEventi.pubblica(new NotificaTestoFrase(nomeCantore + " canta per "
				+ bersaglio.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE)
				+ ": guarisce di " + (bersaglio.getSalute() - saluteInizio) + "."));
		return true;
	}
}
