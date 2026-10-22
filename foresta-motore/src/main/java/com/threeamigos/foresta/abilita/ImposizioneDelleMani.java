package com.threeamigos.foresta.abilita;

import com.threeamigos.foresta.canti.CantoCurativo;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.motore.Gruppo;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.Costanti;
import com.threeamigos.foresta.tipi.TipoAbilita;

import java.util.Optional;

/**
 * L'abilità del Chierico che cura il personaggio più ferito del gruppo (il chierico compreso): circa ABILITA_IMPOSIZIONE_DELLE_MANI_BASE × √livello,
 * aumentato dalla sua SAGGEZZA. Come il canto curativo del Bardo, ma più piccola.
 */
public class ImposizioneDelleMani implements Abilita {

	private final int livello;
	private final int saggezza;

	public ImposizioneDelleMani(Personaggio utilizzatore) {
		this.livello = utilizzatore.getLivello();
		this.saggezza = utilizzatore.getSaggezza();
	}

	@Override
	public TipoAbilita getTipo() {
		return TipoAbilita.IMPOSIZIONE_DELLE_MANI;
	}

	@Override
	public int getCostoLancio() {
		return Costanti.ABILITA_IMPOSIZIONE_DELLE_MANI_COSTO_LANCIO;
	}

	/**
	 * Quanta salute restituisce, prima del limite del massimo di chi la riceve
	 */
	public int getCura() {
		return (int) Math.round(Costanti.ABILITA_IMPOSIZIONE_DELLE_MANI_BASE * Math.sqrt(livello)
				* (1.0d + saggezza / Costanti.ABILITA_IMPOSIZIONE_DELLE_MANI_DIVISORE_SAGGEZZA));
	}

	@Override
	public boolean usa(Personaggio utilizzatore, Gruppo gruppo, Gruppo gruppoAvversario) {
		return usaSu(utilizzatore, CantoCurativo.piuFerito(gruppo).orElse(null), gruppo, gruppoAvversario);
	}

	/**
	 * Cura il bersaglio scelto (se è ferito); {@link #usa} sceglie il più ferito del gruppo
	 */
	@Override
	public boolean usaSu(Personaggio utilizzatore, Personaggio scelto, Gruppo gruppo, Gruppo gruppoAvversario) {
		Optional<Personaggio> ferito = Optional.ofNullable(scelto)
				.filter(p -> p.isVivo() && p.isCurabile() && p.getSalute() < p.getSaluteMassima());
		String nome = utilizzatore.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
				Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA);
		if (!ferito.isPresent()) {
			BusEventi.pubblica(new NotificaTestoFrase(nome + " impone le mani, ma nessuno ha bisogno di cure."));
			return false;
		}
		Personaggio bersaglio = ferito.get();
		int saluteInizio = bersaglio.getSalute();
		bersaglio.addSalute(getCura());
		utilizzatore.subMagia(getCostoLancio());
		BusEventi.pubblica(new NotificaTestoFrase(nome + " impone le mani su "
				+ bersaglio.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE)
				+ ": guarisce di " + (bersaglio.getSalute() - saluteInizio) + "."));
		return true;
	}
}
