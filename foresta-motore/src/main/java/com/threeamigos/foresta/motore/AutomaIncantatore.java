package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.artefatti.RegoleIncantatura;

import com.threeamigos.foresta.artefatti.BancoDiLavoro;

import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaAvvisoIncantatura;
import com.threeamigos.foresta.eventi.notifiche.NotificaRifiutoIncantatura;
import com.threeamigos.foresta.artefatti.VistaBancoDiLavoro;
import com.threeamigos.foresta.tipi.TipoMotivoRifiutoIncantatura;

import java.util.*;

/**
 * La bottega dell'incantatore: a sinistra l'inventario del gruppo, a destra il banco di lavoro.
 * Si sposta liberamente dal banco al gruppo; verso il banco valgono le regole di RegoleIncantatura,
 * e un rifiuto diventa una NotificaRifiutoIncantatura (un fumetto).
 */
public class AutomaIncantatore extends AutomaScambiatoreArtefatti implements VistaBancoDiLavoro {

	private final GruppoGiocatore gruppo;
	private final BancoDiLavoro banco;
	private RegoleIncantatura.Piano pianoCache;
	private List<Object> chiaveCache = Collections.emptyList();

	public AutomaIncantatore(GruppoGiocatore gruppo, BancoDiLavoro banco) {
		super(gruppo, banco);
		this.gruppo = gruppo;
		this.banco = banco;
	}

	@Override
	public Optional<Artefatto> getArtefattoSulBanco() {
		return RegoleIncantatura.artefattoSulBanco(banco.getInventario());
	}

	@Override
	public int getEffettiDaTrasferire() {
		if (!getArtefattoSulBanco().isPresent() || RegoleIncantatura.conRiprogrammazione(banco.getInventario())) {
			return RegoleIncantatura.effettiDaTrasferire(banco.getInventario());
		}
		return piano().numeroDaAggiungere();
	}

	@Override
	public int getEffettiDopoLaFusione() {
		return getArtefattoSulBanco()
				.map(a -> a.getNumeroEffetti() - piano().numeroDaTogliere() + piano().numeroDaAggiungere())
				.orElse(0);
	}

	@Override
	public int getLivelloDopoLaFusione() {
		return RegoleIncantatura.livelloDopoLaFusione(banco.getInventario());
	}

	@Override
	public int getEffettiMassimiDopoLaFusione() {
		return RegoleIncantatura.effettiMassimiDopoLaFusione(banco.getInventario());
	}

	@Override
	public int getCostoFusione() {
		if (!getArtefattoSulBanco().isPresent() || RegoleIncantatura.conRiprogrammazione(banco.getInventario())) {
			return CommercioDelGruppo.costoFusione(gruppo, banco.getInventario());
		}
		return CommercioDelGruppo.costoFusione(gruppo, piano().numeroDaAggiungere());
	}

	@Override
	public boolean isEffettoInEntrata(Object effetto) {
		return piano().entra(effetto);
	}

	@Override
	public boolean isEffettoInUscita(Object effetto) {
		return piano().esce(effetto);
	}

	/**
	 * Il piano della fusione di quel che sta sul banco. La UI lo chiede a ogni disegno (e per ogni riga di effetto),
	 * ma cambia solo quando cambia il banco: si ricalcola allora.
	 */
	private RegoleIncantatura.Piano piano() {
		Collection<Artefatto> sulBanco = banco.getInventario();
		List<Object> chiave = new ArrayList<>();
		for (Artefatto artefatto : sulBanco) {
			chiave.add(artefatto);
			chiave.add(artefatto.getNumeroEffetti());
			chiave.add(artefatto.getLivello());
		}
		if (pianoCache == null || !stessaChiave(chiave, chiaveCache)) {
			pianoCache = RegoleIncantatura.pianoDellaFusione(sulBanco);
			chiaveCache = chiave;
		}
		return pianoCache;
	}

	// Gli artefatti si confrontano per identità, i numeri di effetti per valore
	private static boolean stessaChiave(List<Object> a, List<Object> b) {
		if (a.size() != b.size()) {
			return false;
		}
		for (int i = 0; i < a.size(); i++) {
			Object x = a.get(i);
			Object y = b.get(i);
			if (x != y && !(x instanceof Integer && x.equals(y))) {
				return false;
			}
		}
		return true;
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
		Optional<TipoMotivoRifiutoIncantatura> motivo = RegoleIncantatura.puoMettereSulBanco(banco.getInventario(), artefatto, Statistiche.getLivello());
		if (motivo.isPresent()) {
			BusEventi.pubblica(new NotificaRifiutoIncantatura(motivo.get()));
		} else {
			spostaSuParteRemota(artefatto);
			RegoleIncantatura.avvisoEffettiPersi(banco.getInventario())
					.ifPresent(avviso -> BusEventi.pubblica(new NotificaAvvisoIncantatura(TestoAvvisoDiFusione.testo(avviso))));
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
