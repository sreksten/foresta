package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaAvvisoIncantatura;
import com.threeamigos.foresta.eventi.notifiche.NotificaRifiutoIncantatura;
import com.threeamigos.foresta.oggetti.Artefatto;

import java.util.Optional;

/**
 * La bottega dell'incantatore: a sinistra l'inventario del gruppo, a destra il banco di lavoro.
 * Si sposta liberamente dal banco al gruppo; verso il banco valgono le regole di RegoleIncantatura,
 * e un rifiuto diventa una NotificaRifiutoIncantatura (un fumetto).
 */
public class AutomaIncantatore extends AutomaScambiatoreArtefatti {

	private final BancoDiLavoro banco;

	public AutomaIncantatore(GruppoGiocatore gruppo, BancoDiLavoro banco) {
		super(gruppo, banco);
		this.banco = banco;
	}

	public BancoDiLavoro getBanco() {
		return banco;
	}

	@Override
	public boolean mostraCostoSuParteAttiva() {
		return false;
	}

	@Override
	public boolean mostraCostoSuParteRemota() {
		return false;
	}

	@Override
	public void richiediSpostamentoSuParteAttiva(Artefatto artefatto) {
		spostaSuParteAttiva(artefatto);
	}

	@Override
	public void richiediSpostamentoSuParteRemota(Artefatto artefatto) {
		Optional<MotivoRifiutoIncantatura> motivo = RegoleIncantatura.puoMettereSulBanco(banco.getInventario(), artefatto);
		if (motivo.isPresent()) {
			BusEventi.pubblica(new NotificaRifiutoIncantatura(motivo.get()));
		} else {
			spostaSuParteRemota(artefatto);
			if (RegoleIncantatura.incantamentiPersi(banco.getInventario())) {
				BusEventi.pubblica(new NotificaAvvisoIncantatura(
						"Sul libro gli incantamenti elementali non hanno effetto: passeranno solo gli altri effetti."));
			}
		}
	}

	/**
	 * Rimette nel gruppo quel che è rimasto sul banco (uscendo dalla bottega).
	 */
	public void svuotaBanco() {
		for (Artefatto artefatto : banco.getInventario()) {
			spostaSuParteAttiva(artefatto);
		}
	}
}
