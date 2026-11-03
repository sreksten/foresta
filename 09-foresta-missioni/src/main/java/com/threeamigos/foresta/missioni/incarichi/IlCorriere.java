package com.threeamigos.foresta.missioni.incarichi;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.intermezzi.ScenaInCitta;
import com.threeamigos.foresta.missioni.OggettiDaRaccogliere;
import com.threeamigos.foresta.missioni.Passo;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.missioni.richieste.Spedizione;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.testi.ProduttoreDiMissioniCasuale;
import com.threeamigos.foresta.tipi.CategoriaLocazione;
import com.threeamigos.foresta.tipi.MissionePredefinita;
import com.threeamigos.foresta.tipi.TipoLocazione;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.threeamigos.foresta.testi.Lingue;
import com.threeamigos.foresta.testi.Preposizione;
import com.threeamigos.foresta.testi.RegoleDellaLingua;

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
		this(MissionePredefinita.IL_CORRIERE, "TRASPORTO");
	}

	/**
	 * Un corriere che pesca le spedizioni da quella produzione di missioni.txt.
	 */
	protected IlCorriere(MissionePredefinita classe, String produzione) {
		super(classe);
		this.produzione = produzione;
	}

	public Spedizione getSpedizione() {
		return Spedizione.da(parametro(SPEDIZIONE, () -> ProduttoreDiMissioniCasuale.rigaDiMissioni(produzione)));
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
		TipoLocazione qui = Foresta.getLocazioneDelGruppo();
		List<TipoLocazione> altre = new ArrayList<>();
		for (TipoLocazione citta : TipoLocazione.values()) {
			if (citta.getCategoriaLocazione() == CategoriaLocazione.CITTA && citta != qui
					&& !Foresta.isCittaDistrutta(citta) && Foresta.getCoordinateLocazioneUnica(citta) != null) {
				altre.add(citta);
			}
		}
		return altre;
	}

	@Override
	protected boolean isPossibileQui() {
		return !altreCitta().isEmpty();
	}

	/**
	 * Con una sola città in piedi non c'è più niente da portare altrove.
	 */
	@Override
	protected boolean puoAncoraOffrirsi() {
		long inPiedi = Arrays.stream(TipoLocazione.values())
				.filter(citta -> citta.getCategoriaLocazione() == CategoriaLocazione.CITTA && !Foresta.isCittaDistrutta(citta)
						&& Foresta.getCoordinateLocazioneUnica(citta) != null)
				.count();
		return inPiedi >= 2;
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
		return getSpedizione().getDestinatario() + " " + Lingue.corrente().conPreposizione(Preposizione.DI, getNomeDestinazione());
	}

	@Override
	protected void allIncarico() {
		getSpedizione();
		scegliLaDestinazione();
	}

	/**
	 * Il titolo si conserva nei dati solo quando la destinazione è scelta (vedi {@link #allIncarico()}): prima non è ancora quello vero.
	 */
	@Override
	public String getNome() {
		return getDestinazione() == null ? creaIlNome() : super.getNome();
	}

	@Override
	protected String creaIlNome() {
		return ProduttoreDiMissioniCasuale.testoDellaMissione("IL_CORRIERE_TITOLO", valori());
	}

	@Override
	public String getDescrizione() {
		return descrizioneDalModello();
	}

	@Override
	protected String produzioneDellaDescrizione(String passo) {
		return getSpedizione().isUrgente() ? "IL_CORRIERE_DESCRIZIONE_URGENTE" : "IL_CORRIERE_DESCRIZIONE";
	}

	@Override
	protected Object[] valoriStabiliDellaDescrizione() {
		return valori();
	}

	@Override
	protected Object[] valoriDellaDescrizione() {
		return new Object[] {"PARAM_ORE_RIMASTE", Math.max(0, getOreRimaste())};
	}

	/**
	 * Tutti i segnaposto dei testi della missione: l'oggetto, la città di destinazione e il destinatario e il mittente nelle loro forme
	 * (con la città o senza, con la virgola finale o senza, con la preposizione, con l'iniziale maiuscola).
	 */
	private Object[] valori() {
		Spedizione spedizione = getSpedizione();
		RegoleDellaLingua lingua = Lingue.corrente();
		TipoLocazione destinazione = getDestinazione();
		String destinatario = spedizione.getDestinatario();
		String destinatarioIn = mandanteIn(destinatario, destinazione);
		return new Object[] {
				"PARAM_OGGETTO", spedizione.getOggettoConArticolo(),
				"PARAM_OGGETTO_MAIUSC", lingua.inizialeMaiuscola(spedizione.getOggettoConArticolo()),
				"PARAM_QUESTO_OGGETTO", spedizione.getQuestoOggetto(),
				"PARAM_PRONOME", spedizione.getPronome(),
				"PARAM_CITTA", getNomeDestinazione(),
				"PARAM_MONETE", getRicompensa(),
				"PARAM_ORE", getOreConcesse(),
				"PARAM_A_DESTINATARIO_DI_CITTA", lingua.conPreposizione(Preposizione.A, getDestinatarioDiCitta()),
				"PARAM_DESTINATARIO_DI_CITTA", getDestinatarioDiCitta(),
				"PARAM_DESTINATARIO_DI_CITTA_MAIUSC", lingua.inizialeMaiuscola(getDestinatarioDiCitta()),
				"PARAM_DESTINATARIO_IN", destinatarioIn,
				"PARAM_DESTINATARIO_IN_MAIUSC", lingua.inizialeMaiuscola(destinatarioIn),
				"PARAM_A_DESTINATARIO_SENZA_VIRGOLA", lingua.conPreposizione(Preposizione.A, mandanteInSenzaVirgolaFinale(destinatario, destinazione)),
				"PARAM_MITTENTE", mandante(spedizione.getMittente()),
				"PARAM_MITTENTE_SENZA_VIRGOLA", mandanteSenzaVirgolaFinale(spedizione.getMittente())};
	}

	private static Object[] con(Object[] valori, Object... altri) {
		Object[] tutti = Arrays.copyOf(valori, valori.length + altri.length);
		System.arraycopy(altri, 0, tutti, valori.length, altri.length);
		return tutti;
	}

	@Override
	protected String primoPassoDelCompito() {
		return PARTENZA;
	}

	@Override
	protected Passo costruisciPassoDelCompito(String id) {
		switch (id) {
			case PARTENZA:
				return Passo.quando(MomentoControllo.IN_LOCAZIONE, this::nellaCitta)
						.esegui(() -> {
							incrementaContatore(OGGETTO, 1);
							aggiungiProprieta(PARTITI_ALLE, String.valueOf(oreDiGioco()));
							Foresta.setLocazioneConosciuta(Foresta.getCoordinateLocazioneUnica(getDestinazione()));
							BusEventi.pubblica(new NotificaTestoParagrafo(ProduttoreDiMissioniCasuale.testoDellaMissione("IL_CORRIERE_DESTINAZIONE_SEGNATA", valori())));
						})
						.poi(VIAGGIO);
			case VIAGGIO:
				return Passo.quando(MomentoControllo.PRE_LOCAZIONE, this::nellaCittaDelRitorno)
						.segnala(this::coordinateDellaCittaDelRitorno)
						.falliscoSe(this::isInRitardo, () -> ProduttoreDiMissioniCasuale.testoDellaMissione("IL_CORRIERE_IN_RITARDO", valori()))
						.falliscoSe(() -> isDiNascosto() && haCombattutoNelPassoCorrente(), () -> ProduttoreDiMissioniCasuale.testoDellaMissione("IL_CORRIERE_VOCE_SPARSA", valori()))
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
		return ProduttoreDiMissioniCasuale.testoDellaMissione("IL_CORRIERE_CONSEGNA", valori());
	}

	@Override
	protected ScenaInCitta scenaIncarico() {
		Spedizione spedizione = getSpedizione();
		Object[] valori = valori();
		String quando = " " + ProduttoreDiMissioniCasuale.testoDellaMissione(spedizione.isUrgente() ? "IL_CORRIERE_QUANDO_URGENTE" : "IL_CORRIERE_QUANDO_SENZA_FRETTA", valori);
		if (isDiNascosto()) {
			quando += " " + ProduttoreDiMissioniCasuale.testoDellaMissione("IL_CORRIERE_QUANDO_DI_NASCOSTO");
		}
		return scenaDel(spedizione.getMittente())
				.parlaIlMandante(spedizione.getRichiesta())
				.parlaIlMandante(ProduttoreDiMissioniCasuale.testoDellaMissione("IL_CORRIERE_INCARICO_1", con(valori, "PARAM_QUANDO", quando)))
				.parlaIlCapo(spedizione.getBattutaDelCapo())
				.parlaIlMandante(spedizione.getRispostaDelMittente());
	}

	@Override
	protected ScenaInCitta scenaRingraziamento() {
		return scenaDel(getSpedizione().getDestinatario())
				.parlaIlMandante(getSpedizione().getRingraziamento())
				.parlaIlMandante(ProduttoreDiMissioniCasuale.testoDellaMissione("IL_CORRIERE_RINGRAZIAMENTO_2", valori()));
	}

	@Override
	protected String testoAccettazione() {
		return ProduttoreDiMissioniCasuale.testoDellaMissione(getSpedizione().isUrgente() ? "IL_CORRIERE_ACCETTAZIONE_URGENTE" : "IL_CORRIERE_ACCETTAZIONE", valori());
	}

	@Override
	protected String testoRicompensa() {
		return ProduttoreDiMissioniCasuale.testoDellaMissione("IL_CORRIERE_RICOMPENSA", valori());
	}

	@Override
	protected String testoCittaDistrutta() {
		return ProduttoreDiMissioniCasuale.testoDellaMissione("IL_CORRIERE_CITTA_DISTRUTTA", valori());
	}

	/**
	 * Le monete della spedizione, più una ogni due caselle di strada.
	 */
	@Override
	protected int getRicompensaBase() {
		return getSpedizione().getMonete() + getDistanza() / 2;
	}
}
