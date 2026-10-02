package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.intermezzi.ScenaInCitta;
import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.oggetti.NomeOggetto;

/**
 * In città l'alchimista chiede {@link #RADICI} radici di mandragola: crescono nelle radure e nei boschi, dove la
 * missione le semina (vedi {@link OggettiDaRaccogliere}). Raccolte tutte, si torna da lui a riscuotere (vedi
 * IncaricoInCitta).
 */
public class LAlchimistaELaMandragola extends IncaricoInCitta {

	public static final int RADICI = 4;
	public static final String MANDRAGOLA = "MANDRAGOLA";
	public static final OggettiDaRaccogliere RADICI_DI_MANDRAGOLA = OggettiDaRaccogliere
			.di(MANDRAGOLA, NomeOggetto.femminile("radice di mandragola", "radici di mandragola"), RADICI)
			.in(ClassiLocazione.RADURA, ClassiLocazione.BOSCO)
			.conProbabilita(35)
			.alPiuPerLocazione(2);
	private static final int RICOMPENSA = 25;
	private static final String RACCOLTA = "RACCOLTA";

	public LAlchimistaELaMandragola() {
		super(ClasseMissione.L_ALCHIMISTA_E_LA_MANDRAGOLA);
	}

	@Override
	public String getNome() {
		return "L'alchimista e la mandragola";
	}

	@Override
	public String getDescrizione() {
		if (RITORNO.equals(getPassoCorrente())) {
			return "Hai tutte le radici di mandragola: portale all'alchimista di " + getNomeCitta() + ".";
		}
		return "L'alchimista di " + getNomeCitta() + " ti ha chiesto " + RADICI
				+ " radici di mandragola, che crescono nelle radure e nei boschi. Finora: " + getContatore(MANDRAGOLA) + ".";
	}

	@Override
	protected String primoPassoDelCompito() {
		return RACCOLTA;
	}

	@Override
	protected Passo costruisciPassoDelCompito(String id) {
		if (!RACCOLTA.equals(id)) {
			throw new IllegalArgumentException("Passo sconosciuto per " + getNome() + ": " + id);
		}
		return raccogli(MomentoControllo.POST_LOCAZIONE, RADICI_DI_MANDRAGOLA)
				.esegui(() -> BusEventi.pubblica(new NotificaTestoParagrafo("Le radici di mandragola ci sono tutte: l'alchimista di "
						+ getNomeCitta() + " le aspetta.")))
				.poi(RITORNO);
	}

	@Override
	protected ScenaInCitta scenaIncarico() {
		return ScenaInCitta.conAlchimista()
				.parlaIlMandante("Buongiorno, viaggiatori. Non è che andate per radure e per boschi?")
				.parlaIlMandante("Mi servono " + RADICI + " radici di mandragola per un unguento, e io lì fuori non ci metto piede.")
				.parlaIlCapo("E quando le tiriamo su non strillano?")
				.parlaIlMandante("Solo un po'. Ve le pago " + RICOMPENSA + " monete.")
				.parlaIlCapo("Per " + RICOMPENSA + " monete strillo anch'io.");
	}

	@Override
	protected ScenaInCitta scenaRingraziamento() {
		return ScenaInCitta.conAlchimista()
				.parlaIlMandante("Le mie radici! E sono pure di prima scelta.")
				.parlaIlCapo("Non chiedere come le abbiamo zittite.");
	}

	@Override
	protected String testoAccettazione() {
		return "L'alchimista pagherà " + RICOMPENSA + " monete per " + RADICI
				+ " radici di mandragola, che crescono nelle radure e nei boschi.";
	}

	@Override
	protected String testoRicompensa() {
		return "L'alchimista paga le " + RICOMPENSA + " monete promesse.";
	}

	@Override
	protected int getRicompensa() {
		return RICOMPENSA;
	}
}
