package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.motore.Foresta;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.RegistroMissioni;
import com.threeamigos.foresta.tipi.TipoLocazione;

/**
 * Un incarico di recupero, la storia di una città (vedi IncaricoInCitta, con la città fissa): il mandante chiede di
 * recuperare qualcosa da un covo, il gruppo lo recupera e lo riporta in città per la ricompensa. Il compito ha due
 * passi:
 * <ol>
 * <li>COVO, in locazione, nella città, subito dopo l'accettazione: compare il covo, rivendicato dalla missione, così
 * a missione finita chi ci passa ne legge il ricordo ({@link #getRicordoDellaLocazione()});</li>
 * <li>RECUPERO, a fine locazione, nel covo completato: il segnalino lascia il covo e passa sulla città, dove va
 * riportato il bersaglio (il covo resta comunque rivendicato ai fini del ricordo); poi si torna in città.</li>
 * </ol>
 */
public abstract class MissioneRecuperaBersaglio extends IncaricoInCitta {

	private static final String COVO = "COVO";
	private static final String RECUPERO = "RECUPERO";

	protected static final int AMMONTARE_RICOMPENSA = 20;

	protected MissioneRecuperaBersaglio(ClasseMissione classe) {
		super(classe);
	}

	/**
	 * La locazione unica in cui si trova ciò che va recuperato.
	 */
	protected abstract TipoLocazione getCovo();

	protected abstract String testoRecupero();

	/**
	 * Se il bersaglio è già stato recuperato, cioè la missione è al ritorno in città o oltre.
	 */
	protected final boolean isBersaglioRecuperato() {
		String passo = getPassoCorrente();
		return !COVO.equals(passo) && !RECUPERO.equals(passo) && isAttiva();
	}

	@Override
	protected int getRicompensa() {
		return AMMONTARE_RICOMPENSA;
	}

	@Override
	protected String primoPassoDelCompito() {
		return COVO;
	}

	@Override
	protected Passo costruisciPassoDelCompito(String id) {
		GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
		switch (id) {
			case COVO:
				return Passo.quando(MomentoControllo.IN_LOCAZIONE, () -> true)
						.esegui(() -> RegistroMissioni.occupaLocazione(Foresta.costruisciLocazioneUnica(getCovo(), true), this))
						.poi(RECUPERO);
			case RECUPERO:
				return Passo.quando(MomentoControllo.POST_LOCAZIONE,
								() -> gruppo.isInLocazioneUnica(getCovo()) && gruppo.getLocazioneCorrente().isCompleta())
						.esegui(() -> {
							BusEventi.pubblica(new NotificaTestoParagrafo(testoRecupero()));
							RegistroMissioni.occupaLocazione(Foresta.getCoordinateLocazioneUnica(getCittaDelRitorno()), this);
						})
						.poi(RITORNO);
			default:
				throw new IllegalArgumentException("Passo sconosciuto per " + getNome() + ": " + id);
		}
	}
}
