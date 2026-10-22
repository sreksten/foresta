package com.threeamigos.foresta.abilita;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.motore.Gruppo;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.Costanti;
import com.threeamigos.foresta.tipi.TipoAbilita;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;

import java.util.Optional;

/**
 * L'abilità del Druido che fa rigenerare un alleato: gli dà RIGENERAZIONE per ABILITA_RIGENERAZIONE_DURATA turni, e a ogni turno guarisce di
 * ABILITA_RIGENERAZIONE_BASE più una quota della sua salute massima, aumentata dalla SAGGEZZA del druido (la quantità sta nell'intensità
 * dell'effetto: vedi PersonaggioBase.applicaDanniDaEffettiDiStato). Cura meno subito di un incantesimo, ma nel tempo.
 */
public class Rigenerazione implements Abilita {

	private final int saggezza;

	public Rigenerazione(Personaggio utilizzatore) {
		this.saggezza = utilizzatore.getSaggezza();
	}

	@Override
	public TipoAbilita getTipo() {
		return TipoAbilita.RIGENERAZIONE;
	}

	@Override
	public int getCostoLancio() {
		return Costanti.ABILITA_RIGENERAZIONE_COSTO_LANCIO;
	}

	/**
	 * Quanta salute guarisce a ogni turno chi la riceve
	 */
	public int getCuraPerTurno(Personaggio bersaglio) {
		return (int) Math.round((Costanti.ABILITA_RIGENERAZIONE_BASE
				+ Costanti.ABILITA_RIGENERAZIONE_QUOTA_SALUTE_MASSIMA * bersaglio.getSaluteMassima())
				* (1.0d + saggezza / Costanti.ABILITA_RIGENERAZIONE_DIVISORE_SAGGEZZA));
	}

	@Override
	public boolean usa(Personaggio utilizzatore, Gruppo gruppo, Gruppo gruppoAvversario) {
		return usaSu(utilizzatore, AbilitaInnate.daRigenerare(gruppo).orElse(null), gruppo, gruppoAvversario);
	}

	/**
	 * Fa rigenerare il bersaglio scelto (se è vivo, ferito e curabile); {@link #usa} sceglie il più ferito che non si rigenera già
	 */
	@Override
	public boolean usaSu(Personaggio utilizzatore, Personaggio scelto, Gruppo gruppo, Gruppo gruppoAvversario) {
		Optional<Personaggio> ferito = Optional.ofNullable(scelto)
				.filter(p -> p.isVivo() && p.isCurabile() && p.getSalute() < p.getSaluteMassima());
		String nome = utilizzatore.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
				Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA);
		if (!ferito.isPresent()) {
			BusEventi.pubblica(new NotificaTestoFrase(nome + " invoca la rigenerazione, ma nessuno ne ha bisogno."));
			return false;
		}
		Personaggio bersaglio = ferito.get();
		utilizzatore.subMagia(getCostoLancio());
		BusEventi.pubblica(new NotificaTestoFrase(nome + " fa rigenerare "
				+ bersaglio.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE) + "."));
		bersaglio.addEffettoDiStato(TipoEffettoDiStato.RIGENERAZIONE, Costanti.ABILITA_RIGENERAZIONE_DURATA, 0,
				getCuraPerTurno(bersaglio));
		return true;
	}
}
