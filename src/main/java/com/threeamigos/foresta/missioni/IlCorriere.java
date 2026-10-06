package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.intermezzi.ScenaInCitta;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.motore.Dado;
import com.threeamigos.foresta.motore.Foresta;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.LineaTemporale;
import com.threeamigos.foresta.motore.ProduttoreDiTestiCasuale;
import com.threeamigos.foresta.tipi.CategoriaLocazione;
import com.threeamigos.foresta.tipi.ClasseMissione;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.strumenti.Misc;

import java.util.ArrayList;
import java.util.List;

/**
 * In città qualcuno affida al gruppo una cosa da portare a qualcuno in un'altra città: una lettera, un pegno d'amore,
 * un antidoto, una cassa di rimedi per un lazzaretto (vedi {@link Spedizione}, da missioni.txt). La città di
 * destinazione si segna sulla mappa; lì il destinatario riceve l'oggetto e paga, tanto di più quanto è lontana
 * (vedi IncaricoInCitta, che qui riscuote nella città di destinazione). Le spedizioni urgenti hanno una scadenza: se
 * il gruppo non arriva in tempo la missione fallisce. Se la città di destinazione viene distrutta, anche.
 * <ol>
 * <li>PARTENZA, in locazione, nella città: il gruppo prende l'oggetto e la destinazione compare sulla mappa;</li>
 * <li>VIAGGIO, a inizio locazione, nella città di destinazione: poi il ringraziamento, la consegna e le monete.</li>
 * </ol>
 * La spedizione e la destinazione si pescano quando l'incarico si offre: la missione si ripete con altre.
 * <p>
 * Il contrabbandiere (vedi IlContrabbandiere) porta la sua merce di nascosto: se per strada il gruppo combatte, la
 * voce si sparge e la missione fallisce.
 */
public class IlCorriere extends IncaricoInCitta {

	/**
	 * La chiave sotto cui la missione conta l'oggetto che il gruppo porta.
	 */
	public static final String OGGETTO = "OGGETTO";
	public static final String SPEDIZIONE = "SPEDIZIONE";
	/**
	 * La proprietà con la città di destinazione.
	 */
	public static final String DESTINAZIONE = "DESTINAZIONE";
	private static final String DISTANZA = "DISTANZA";
	private static final String PARTITI_ALLE = "PARTITI_ALLE";
	private static final String PARTENZA = "PARTENZA";
	private static final String VIAGGIO = "VIAGGIO";
	/**
	 * Per una spedizione urgente, le ore che si concedono oltre a due per ogni casella di distanza: si cammina un'ora
	 * per casella, il resto è per i combattimenti e per dormire.
	 */
	private static final int ORE_DI_MARGINE = 12;

	private final String produzione;

	public IlCorriere() {
		this(ClasseMissione.IL_CORRIERE, "TRASPORTO");
	}

	/**
	 * Un corriere che pesca le spedizioni da quella produzione di missioni.txt.
	 */
	protected IlCorriere(ClasseMissione classe, String produzione) {
		super(classe);
		this.produzione = produzione;
	}

	public Spedizione getSpedizione() {
		return Spedizione.da(parametro(SPEDIZIONE, () -> ProduttoreDiTestiCasuale.rigaDiMissioni(produzione)));
	}

	/**
	 * Se la spedizione va fatta di nascosto: allora, se per strada il gruppo combatte, la missione fallisce.
	 */
	protected boolean isDiNascosto() {
		return false;
	}

	/**
	 * La città a cui portare l'oggetto, o null finché l'incarico non si offre.
	 */
	public TipoLocazione getDestinazione() {
		String destinazione = ottieniProprieta(DESTINAZIONE);
		return destinazione == null ? null : TipoLocazione.valueOf(destinazione);
	}

	/**
	 * Quante caselle ci sono fra la città dell'incarico e quella di destinazione, contando i passi a nord, sud, est
	 * e ovest; 0 finché l'incarico non si offre. Si fissa allora, perché la città di destinazione potrebbe sparire.
	 */
	public int getDistanza() {
		String distanza = ottieniProprieta(DISTANZA);
		return distanza == null ? 0 : Integer.parseInt(distanza);
	}

	/**
	 * Quando l'incarico si offre: una città di destinazione diversa da questa e non distrutta (si tiene quella che
	 * c'è già, se va bene), e la distanza da qui.
	 */
	private void scegliLaDestinazione() {
		List<TipoLocazione> altre = altreCitta();
		if (!altre.contains(getDestinazione())) {
			aggiungiProprieta(DESTINAZIONE, altre.get(Dado.tiraAncheAUnaFaccia(altre.size()) - 1).name());
		}
		CoordinateMD da = Foresta.getCoordinateLocazioneUnica(getCitta());
		CoordinateMD a = Foresta.getCoordinateLocazioneUnica(getDestinazione());
		aggiungiProprieta(DISTANZA, String.valueOf(Math.abs(da.getX() - a.getX()) + Math.abs(da.getY() - a.getY())));
	}

	/**
	 * Per una spedizione urgente, entro quante ore dalla partenza bisogna arrivare.
	 */
	public int getOreConcesse() {
		return getDistanza() * 2 + ORE_DI_MARGINE;
	}

	/**
	 * Quante ore restano per una spedizione urgente: tutte, finché il gruppo non è partito.
	 */
	public long getOreRimaste() {
		String partitiAlle = ottieniProprieta(PARTITI_ALLE);
		if (partitiAlle == null) {
			return getOreConcesse();
		}
		return getOreConcesse() - (oreDiGioco() - Long.parseLong(partitiAlle));
	}

	private boolean isInRitardo() {
		return getSpedizione().isUrgente() && getOreRimaste() < 0;
	}

	/**
	 * Le città, diverse da quella in cui si trova il gruppo, che ci sono ancora.
	 */
	private static List<TipoLocazione> altreCitta() {
		TipoLocazione qui = GruppoGiocatore.getIstanza().getTipoLocazioneCorrente();
		List<TipoLocazione> altre = new ArrayList<>();
		for (TipoLocazione citta : TipoLocazione.values()) {
			if (citta.getCategoria() == CategoriaLocazione.CITTA && citta != qui
					&& !LineaTemporale.isCittaDistrutta(citta) && Foresta.getCoordinateLocazioneUnica(citta) != null) {
				altre.add(citta);
			}
		}
		return altre;
	}

	@Override
	protected boolean isPossibileQui() {
		return !altreCitta().isEmpty();
	}

	@Override
	protected TipoLocazione getCittaDelRitorno() {
		return getDestinazione();
	}

	private String getNomeDestinazione() {
		return nomeDellaCitta(getDestinazione());
	}

	/**
	 * Il destinatario e la sua città: "il fabbro di Ruuna".
	 */
	private String getDestinatarioDiCitta() {
		return getSpedizione().getDestinatario() + " di " + getNomeDestinazione();
	}

	@Override
	protected void allIncarico() {
		getSpedizione();
		scegliLaDestinazione();
	}

	@Override
	public String getNome() {
		Spedizione spedizione = getSpedizione();
		return Misc.inizialeMaiuscola(spedizione.getOggettoConArticolo()) + " per " + getDestinatarioDiCitta();
	}

	@Override
	public String getDescrizione() {
		Spedizione spedizione = getSpedizione();
		String descrizione = "Porta " + spedizione.getOggettoConArticolo() + " " + Misc.conPreposizione("a", getDestinatarioDiCitta())
				+ ", che ti pagherà " + getRicompensa() + " monete.";
		if (spedizione.isUrgente()) {
			descrizione += " Ore rimaste: " + Math.max(0, getOreRimaste()) + ".";
		}
		return descrizione;
	}

	@Override
	protected String primoPassoDelCompito() {
		return PARTENZA;
	}

	@Override
	protected Passo costruisciPassoDelCompito(String id) {
		Spedizione spedizione = getSpedizione();
		switch (id) {
			case PARTENZA:
				return Passo.quando(MomentoControllo.IN_LOCAZIONE, this::nellaCitta)
						.esegui(() -> {
							incrementaContatore(OGGETTO, 1);
							aggiungiProprieta(PARTITI_ALLE, String.valueOf(oreDiGioco()));
							Foresta.setLocazioneConosciuta(Foresta.getCoordinateLocazioneUnica(getDestinazione()));
							BusEventi.pubblica(new NotificaTestoParagrafo(getNomeDestinazione() + " è segnata sulla mappa."));
						})
						.poi(VIAGGIO);
			case VIAGGIO:
				return Passo.quando(MomentoControllo.PRE_LOCAZIONE, this::nellaCittaDelRitorno)
						.falliscoSe(this::isInRitardo, () -> "Troppo tardi: " + spedizione.getOggettoConArticolo() + " non arriverà più in tempo "
								+ Misc.conPreposizione("a", getDestinatarioDiCitta()) + ".")
						.falliscoSe(() -> isDiNascosto() && haCombattutoNelPassoCorrente(), () -> "La voce del combattimento si è sparsa: "
								+ spedizione.getDestinatario() + " non vorrà più saperne " + Misc.conPreposizione("di", spedizione.getOggettoConArticolo())
								+ ", e " + spedizione.getMittente() + " nemmeno di voi.")
						.poi(RITORNO);
			default:
				throw new IllegalArgumentException("Passo sconosciuto per " + getNome() + ": " + id);
		}
	}

	@Override
	protected OggettiDaRaccogliere getOggettiDaConsegnare() {
		return OggettiDaRaccogliere.di(OGGETTO, getSpedizione().getNome(), 1);
	}

	@Override
	protected String testoConsegna() {
		Spedizione spedizione = getSpedizione();
		return Misc.inizialeMaiuscola(spedizione.getOggettoConArticolo()) + " passa " + Misc.conPreposizione("a", spedizione.getDestinatario()) + ".";
	}

	@Override
	protected ScenaInCitta scenaIncarico() {
		Spedizione spedizione = getSpedizione();
		String quando = spedizione.isUrgente()
				? " E in fretta: avete " + getOreConcesse() + " ore, non di più."
				: " Non c'è fretta, ma non " + spedizione.getPronome() + " perdete per strada.";
		if (isDiNascosto()) {
			quando += " E che nessuno vi noti: niente combattimenti per strada, o la voce arriva alle guardie.";
		}
		return ScenaInCitta.conMandante()
				.parlaIlMandante(spedizione.getRichiesta())
				.parlaIlMandante("Portate " + spedizione.getQuestoOggetto() + " " + Misc.conPreposizione("a", getDestinatarioDiCitta()) + "."
						+ quando + " Alla consegna avrete " + getRicompensa() + " monete.")
				.parlaIlCapo(spedizione.getBattutaDelCapo())
				.parlaIlMandante(spedizione.getRispostaDelMittente());
	}

	@Override
	protected ScenaInCitta scenaRingraziamento() {
		return ScenaInCitta.conMandante()
				.parlaIlMandante(getSpedizione().getRingraziamento())
				.parlaIlMandante("Ecco le " + getRicompensa() + " monete, ve le siete guadagnate.");
	}

	@Override
	protected String testoAccettazione() {
		Spedizione spedizione = getSpedizione();
		String testo = Misc.inizialeMaiuscola(getDestinatarioDiCitta()) + " pagherà " + getRicompensa() + " monete per "
				+ spedizione.getOggettoConArticolo() + " che manda " + spedizione.getMittente();
		if (spedizione.isUrgente()) {
			return testo + ", se arriva entro " + getOreConcesse() + " ore.";
		}
		return testo + ".";
	}

	@Override
	protected String testoRicompensa() {
		return Misc.inizialeMaiuscola(getSpedizione().getDestinatario()) + " paga le " + getRicompensa() + " monete promesse.";
	}

	@Override
	protected String testoCittaDistrutta() {
		return getNomeDestinazione() + " è stata distrutta: " + getSpedizione().getOggettoConArticolo() + " non arriverà più "
				+ Misc.conPreposizione("a", getSpedizione().getDestinatario()) + ".";
	}

	/**
	 * Le monete della spedizione, più una ogni due caselle di strada.
	 */
	@Override
	protected int getRicompensa() {
		return getSpedizione().getMonete() + getDistanza() / 2;
	}
}
