package com.threeamigos.foresta.missioni.incarichi;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.missioni.IncontroDiMissione;
import com.threeamigos.foresta.missioni.MissioneAPassi;
import com.threeamigos.foresta.missioni.Passo;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.missioni.RegistroMissioni;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.testi.Articoli;
import com.threeamigos.foresta.tipi.MissionePredefinita;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * Una delle chiavi di una {@link MissioneATappe} a chiavi, che la madre affida quando il gruppo ne scopre i custodi (le tre ricerche si
 * fanno in qualunque ordine): il custode sta in un posto, scelto e segnato dalla madre, e ha la chiave.
 * <ol>
 * <li>CUSTODE, a fine locazione, nel posto: il custode e i suoi sono sconfitti e la chiave è del gruppo;</li>
 * <li>oppure, se il custode non vuole combattere ma uno scambio: CHIEDE (nel suo posto dice che cosa vuole e dove sta), OGGETTO (a fine
 * locazione, nel posto dell'oggetto, la sua guardia è sconfitta) e CONSEGNA (di nuovo dal custode, che dà la chiave).</li>
 * </ol>
 * La chiave non è un oggetto dell'inventario: conta averla presa, e la madre aspetta che le tre figlie siano finite.
 */
public class ChiaveDelCustode extends MissioneAPassi {

	private static final String POSTO = "POSTO";
	private static final String NOME_DEL_POSTO = "NOME_DEL_POSTO";
	private static final String NEMICO = "NEMICO";
	private static final String NUMERO = "NUMERO";
	private static final String CAPO = "CAPO";
	private static final String CHIAVE = "CHIAVE";
	private static final String TESTO = "TESTO";
	private static final String OGGETTO = "OGGETTO";
	private static final String TESTO_DELL_OGGETTO = "TESTO_DELL_OGGETTO";
	private static final String POSTO_DELL_OGGETTO = "POSTO_DELL_OGGETTO";
	private static final String NOME_DEL_POSTO_DELL_OGGETTO = "NOME_DEL_POSTO_DELL_OGGETTO";
	private static final String NEMICO_DELL_OGGETTO = "NEMICO_DELL_OGGETTO";
	private static final String NUMERO_DELL_OGGETTO = "NUMERO_DELL_OGGETTO";

	private static final String CUSTODE = "CUSTODE";
	private static final String CHIEDE = "CHIEDE";
	private static final String CONSEGNA = "CONSEGNA";
	private static final String SEPARATORE = ",";

	public ChiaveDelCustode() {
		super(MissionePredefinita.CHIAVE_DEL_CUSTODE);
	}

	/**
	 * Un custode che combatte: {@code numero} avversari della classe, con il capo di quel nome (null se non ha un capo).
	 */
	static ChiaveDelCustode chePerCombattere(CoordinateMD posto, String nomeDelPosto, TipoPersonaggio nemico, int numero, String capo,
			String chiave, String testo) {
		ChiaveDelCustode figlia = conParametri(posto, nomeDelPosto, capo, chiave, testo);
		figlia.aggiungiProprieta(PARAMETRO + NEMICO, nemico.name());
		figlia.aggiungiProprieta(PARAMETRO + NUMERO, String.valueOf(numero));
		return figlia;
	}

	/**
	 * Un custode che vuole un oggetto invece del combattimento: l'oggetto sta in un altro posto, con la sua guardia.
	 */
	static ChiaveDelCustode cheVuoleUnOggetto(CoordinateMD posto, String nomeDelPosto, String capo, String chiave, String testo,
			String oggetto, String testoDellOggetto, CoordinateMD postoDellOggetto, String nomeDelPostoDellOggetto, TipoPersonaggio guardia,
			int numeroDiGuardie) {
		ChiaveDelCustode figlia = conParametri(posto, nomeDelPosto, capo, chiave, testo);
		figlia.aggiungiProprieta(PARAMETRO + OGGETTO, oggetto);
		figlia.aggiungiProprieta(PARAMETRO + TESTO_DELL_OGGETTO, testoDellOggetto);
		figlia.aggiungiProprieta(PARAMETRO + POSTO_DELL_OGGETTO, coordinate(postoDellOggetto));
		figlia.aggiungiProprieta(PARAMETRO + NOME_DEL_POSTO_DELL_OGGETTO, nomeDelPostoDellOggetto);
		figlia.aggiungiProprieta(PARAMETRO + NEMICO_DELL_OGGETTO, guardia.name());
		figlia.aggiungiProprieta(PARAMETRO + NUMERO_DELL_OGGETTO, String.valueOf(numeroDiGuardie));
		return figlia;
	}

	private static ChiaveDelCustode conParametri(CoordinateMD posto, String nomeDelPosto, String capo, String chiave, String testo) {
		ChiaveDelCustode figlia = new ChiaveDelCustode();
		figlia.aggiungiProprieta(PARAMETRO + POSTO, coordinate(posto));
		figlia.aggiungiProprieta(PARAMETRO + NOME_DEL_POSTO, nomeDelPosto);
		figlia.aggiungiProprieta(PARAMETRO + CAPO, capo == null ? "" : capo);
		figlia.aggiungiProprieta(PARAMETRO + CHIAVE, chiave);
		figlia.aggiungiProprieta(PARAMETRO + TESTO, testo);
		return figlia;
	}

	private static String coordinate(CoordinateMD coordinate) {
		return coordinate.getX() + SEPARATORE + coordinate.getY();
	}

	private static CoordinateMD leggi(String valore) {
		String[] parti = valore.split(SEPARATORE);
		return new CoordinateMD(Integer.parseInt(parti[0]), Integer.parseInt(parti[1]));
	}

	private boolean isInScambio() {
		return getParametro(OGGETTO) != null;
	}

	private CoordinateMD getPosto() {
		return leggi(getParametro(POSTO));
	}

	private CoordinateMD getPostoDellOggetto() {
		return leggi(getParametro(POSTO_DELL_OGGETTO));
	}

	private String getCapo() {
		return getParametro(CAPO);
	}

	private boolean isNelPostoDelCustode() {
		return getPosto().equals(Foresta.getPosizioneDelGruppo());
	}

	private IncontroDiMissione incontroDelCustode() {
		IncontroDiMissione incontro = IncontroDiMissione.di(TipoPersonaggio.valueOf(getParametro(NEMICO)), Integer.parseInt(getParametro(NUMERO)));
		return getCapo().isEmpty() ? incontro : incontro.conCapo(getCapo());
	}

	@Override
	public String getNome() {
		return Articoli.inizialeMaiuscola(getParametro(CHIAVE));
	}

	@Override
	public String getDescrizione() {
		StringBuilder descrizione = new StringBuilder(getParametro(TESTO)).append(" Il posto è ").append(getParametro(NOME_DEL_POSTO))
				.append(", segnato sulla mappa.");
		if (isInScambio() && !CUSTODE.equals(getPassoCorrente()) && !CHIEDE.equals(getPassoCorrente())) {
			descrizione.append(" ").append(getParametro(TESTO_DELL_OGGETTO)).append(" Il posto è ").append(getParametro(NOME_DEL_POSTO_DELL_OGGETTO))
					.append(", segnato sulla mappa.");
		}
		return descrizione.toString();
	}

	@Override
	public String getRicordoDellaLocazione() {
		return null;
	}

	@Override
	protected String passoIniziale() {
		return isInScambio() ? CHIEDE : CUSTODE;
	}

	@Override
	protected Passo costruisciPasso(String id) {
		switch (id) {
			case CUSTODE:
				return combatti(this::getPosto, incontroDelCustode())
						.esegui(() -> BusEventi.pubblica(new NotificaTestoParagrafo("Il custode è vinto: " + getParametro(CHIAVE) + " è vostra.")))
						.poi(Passo.FINE);
			case CHIEDE:
				return Passo.quando(MomentoControllo.PRE_LOCAZIONE, this::isNelPostoDelCustode)
						.aOgniControllo(() -> {
							if (isNelPostoDelCustode()) {
								RegistroMissioni.sopprimiContenutoLocazione(getPosto());
							}
						})
						.esegui(() -> {
							BusEventi.pubblica(new NotificaTestoParagrafo(getParametro(TESTO)));
							BusEventi.pubblica(new NotificaTestoParagrafo(getParametro(TESTO_DELL_OGGETTO) + " Il posto è "
									+ getParametro(NOME_DEL_POSTO_DELL_OGGETTO) + ": è segnato sulla mappa."));
							Foresta.setLocazioneConosciuta(getPostoDellOggetto());
						})
						.poi(OGGETTO);
			case OGGETTO:
				return combatti(this::getPostoDellOggetto, IncontroDiMissione.di(TipoPersonaggio.valueOf(getParametro(NEMICO_DELL_OGGETTO)),
						Integer.parseInt(getParametro(NUMERO_DELL_OGGETTO))))
						.esegui(() -> {
							BusEventi.pubblica(new NotificaTestoParagrafo("Avete recuperato " + getParametro(OGGETTO) + ": ora tocca riportarlo."));
							Foresta.setLocazioneConosciuta(getPosto());
						})
						.poi(CONSEGNA);
			case CONSEGNA:
				return Passo.quando(MomentoControllo.PRE_LOCAZIONE, this::isNelPostoDelCustode)
						.aOgniControllo(() -> {
							if (isNelPostoDelCustode()) {
								RegistroMissioni.sopprimiContenutoLocazione(getPosto());
							}
						})
						.esegui(() -> BusEventi.pubblica(new NotificaTestoParagrafo("In cambio di " + getParametro(OGGETTO) + " il custode vi dà "
								+ getParametro(CHIAVE) + ".")))
						.poi(Passo.FINE);
			default:
				throw new IllegalArgumentException("Passo sconosciuto per " + getNome() + ": " + id);
		}
	}
}
