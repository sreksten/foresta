package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.intermezzi.MomentoIntermezzo;
import com.threeamigos.foresta.intermezzi.ScenaInCitta;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.motore.Foresta;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.RegistroMissioni;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.tipi.TipoLocazione;

/**
 * In città qualcuno chiede di riportare a casa una persona che sta in un posto pericoloso, in mano a dei nemici: un
 * ostaggio (vedi IlRapimento), un ferito (vedi IlSoccorso). La missione rivendica il posto, lo segna sulla mappa e ci
 * mette i nemici (vedi {@link MissioneAPassi#combatti}). Sconfitti i nemici, la persona si unisce al gruppo come
 * ospite vulnerabile (vedi
 * {@link MissioneAPassi#prendiInScorta(MomentoControllo, java.util.function.BooleanSupplier, String, boolean)}): gli
 * avversari la possono attaccare, e va riportata viva in città. Se muore per strada, la missione resta aperta finché
 * il gruppo non torna in città a dare la notizia: allora c'è la scena triste, e la missione fallisce.
 * <ol>
 * <li>COVO, in locazione, nella città: il posto compare sulla mappa;</li>
 * <li>LIBERAZIONE, a fine locazione, nel posto: i nemici sono stati sconfitti tutti;</li>
 * <li>LIBERATO, a fine locazione, nel posto: la persona si unisce al gruppo;</li>
 * <li>VIAGGIO, a inizio locazione, nella città: la persona è arrivata, viva o morta;</li>
 * <li>se è morta, LUTTO, a inizio locazione, nella città: la scena triste;</li>
 * <li>e FALLIMENTO, in locazione, nella città: la porta che si chiude, e la missione fallisce.</li>
 * </ol>
 */
public abstract class LaLiberazione extends IncaricoInCitta {

	private static final String COVO = "COVO";
	private static final String LIBERAZIONE = "LIBERAZIONE";
	private static final String LIBERATO = "LIBERATO";
	protected static final String VIAGGIO = "VIAGGIO";
	protected static final String LUTTO = "LUTTO";
	protected static final String FALLIMENTO = "FALLIMENTO";

	protected LaLiberazione(ClasseMissione classe) {
		super(classe);
	}

	/**
	 * Il nome della persona da riportare a casa: è anche il nome dell'ospite.
	 */
	protected abstract String getPersona();

	/**
	 * Dove sta la persona.
	 */
	protected abstract TipoLocazione getLuogoDelCovo();

	/**
	 * I nemici da sconfiggere nel posto.
	 */
	protected abstract IncontroDiMissione getNemiciDelCovo();

	/**
	 * Quando il posto compare sulla mappa.
	 */
	protected abstract String testoCovoSegnato();

	/**
	 * Quando la persona si unisce al gruppo.
	 */
	protected abstract String testoLiberato();

	/**
	 * Quando si arriva in città con la persona morta per strada.
	 */
	protected abstract String testoMorto();

	/**
	 * Alla fine della scena triste, quando la missione fallisce.
	 */
	protected abstract String testoPortaChiusa();

	protected abstract ScenaInCitta scenaDelLutto();

	/**
	 * Il posto dove sta la persona, o null finché la missione non l'ha trovato.
	 */
	public CoordinateMD getCovo() {
		return RegistroMissioni.getLocazioneOccupata(this);
	}

	@Override
	protected String primoPassoDelCompito() {
		return COVO;
	}

	@Override
	protected Passo costruisciPassoDelCompito(String id) {
		switch (id) {
			case COVO:
				return cercaLocazione(MomentoControllo.IN_LOCAZIONE, getLuogoDelCovo())
						.esegui(() -> {
							Foresta.setLocazioneConosciuta(getCovo());
							BusEventi.pubblica(new NotificaTestoParagrafo(testoCovoSegnato()));
						})
						.poi(LIBERAZIONE);
			case LIBERAZIONE:
				return combatti(this::getCovo, getNemiciDelCovo()).poi(LIBERATO);
			case LIBERATO:
				return prendiInScorta(MomentoControllo.POST_LOCAZIONE,
								() -> getCovo().equals(GruppoGiocatore.getIstanza().getCoordinate()), getPersona(), true)
						.esegui(() -> BusEventi.pubblica(new NotificaTestoParagrafo(testoLiberato())))
						.poi(VIAGGIO);
			case VIAGGIO:
				return scortaFinoAllaMeta(MomentoControllo.PRE_LOCAZIONE, () -> Foresta.getCoordinateLocazioneUnica(getCitta()))
						.esegui(() -> {
							if (isScortatoMorto()) {
								BusEventi.pubblica(new NotificaTestoParagrafo(testoMorto()));
							}
						})
						.poi(() -> isScortatoMorto() ? LUTTO : RITORNO);
			case LUTTO:
				// A inizio locazione la scena, in locazione il fallimento: così l'avviso arriva dopo la scena
				return Passo.quando(MomentoControllo.PRE_LOCAZIONE, this::nellaCitta)
						.conIntermezzo(MomentoIntermezzo.INIZIO_LOCAZIONE, () -> scenaDelLutto().getPagine())
						.poi(FALLIMENTO);
			case FALLIMENTO:
				return Passo.quando(MomentoControllo.IN_LOCAZIONE, this::nellaCitta)
						.esegui(() -> {
							BusEventi.pubblica(new NotificaTestoParagrafo(testoPortaChiusa()));
							fallisciMissione();
						})
						.poi(Passo.FINE);
			default:
				throw new IllegalArgumentException("Passo sconosciuto per " + getNome() + ": " + id);
		}
	}
}
