package com.threeamigos.foresta.missioni.incarichi;

import com.threeamigos.foresta.testi.Articoli;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.intermezzi.ScenaInCitta;
import com.threeamigos.foresta.locazioni.Bottega;
import com.threeamigos.foresta.missioni.*;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.missioni.richieste.InfestazioneRichiesta;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.modellodati.LocazioneMD;
import com.threeamigos.foresta.motore.*;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.testi.ProduttoreDiMissioniCasuale;
import com.threeamigos.foresta.tipi.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Il negoziante di una città (l'armaiolo, l'alchimista, il venditore di pergamene, l'incantatore: vedi le sottoclassi)
 * chiede di disinfestare delle rovine che un suo parente ha ereditato: il parente vuole andare a vivere fuori città e aprirci
 * una bottega del suo stesso mestiere. La missione sceglie le rovine (le cause, i mostri e i testi vengono da missioni.txt: da
 * NEGOZIO_PARENTE in poi) e le segna sulla mappa. Disinfestate, si torna in città a riscuotere, e solo allora la missione si
 * completa e le rovine diventano la bottega, con un negoziante e un nome suoi, un magazzino generato da zero e le stesse cose
 * del negozio di una città (vedi locazioni.Bottega).
 * <p>
 * Ogni città ha una delle quattro missioni, assegnata a caso a inizio partita (vedi {@link #assegnaLeCitta}): si offre solo
 * lì, nella sua visita tranquilla, insieme agli altri incarichi in città (vedi IncaricoInCitta), e non si ripete. La
 * bottega non nasce se la missione fallisce (la città del mandante distrutta): le rovine restano rovine.
 * <ol>
 * <li>COVO, in locazione, nella città: le rovine, scelte quando la missione si offre, compaiono sulla mappa;</li>
 * <li>DISINFESTAZIONE, a fine locazione, nelle rovine: i mostri sono stati sconfitti tutti;</li>
 * <li>poi il ritorno in città e la ricompensa, come ogni incarico.</li>
 * </ol>
 * Le rovine sono dello stesso quadrante della città; se lì non ce ne sono di libere (nessun'altra missione le ha rivendicate)
 * si cercano in un altro, e se in tutta la mappa non ce n'è nessuna se ne costruisce una al posto di un bosco o di una palude
 * (vedi RegistroMissioni.cercaOCostruisci).
 */
public abstract class IlNegozio extends IncaricoInCitta {

	/**
	 * La città da cui parte la missione, assegnata a inizio partita (il nome di un TipoLocazione).
	 */
	public static final String CITTA_ASSEGNATA = "CITTA_ASSEGNATA";

	private static final String COVO = "COVO";
	private static final String DISINFESTAZIONE = "DISINFESTAZIONE";

	// I parametri della missione, pescati da missioni.txt quando si offre: i testi con i loro segnaposto, per il resto
	private static final String PARENTE = "PARENTE";
	private static final String CAUSA = "CAUSA";
	private static final String EREDITA = "EREDITA";
	private static final String APPELLO = "APPELLO";
	private static final String BATTUTA = "BATTUTA";
	private static final String RISPOSTA = "RISPOSTA";
	private static final String VITTORIA = "VITTORIA";
	private static final String RINGRAZIAMENTO = "RINGRAZIAMENTO";
	private static final String APERTURA = "APERTURA";
	private static final String NEMICI = "NEMICI";
	private static final String NOME_ROVINA = "NOME_ROVINA";
	private static final String NOME_BOTTEGA = "NOME_BOTTEGA";

	private static final String SEGNAPOSTO_PARENTE = "%PARENTE%";
	private static final String SEGNAPOSTO_NOME = "%NOME%";
	private static final String SEGNAPOSTO_ROVINA = "%ROVINA%";
	private static final String SEGNAPOSTO_NEMICI = "%NEMICI%";
	private static final String SEGNAPOSTO_BOTTEGA = "%BOTTEGA%";

	private static final int RICOMPENSA_BASE = 50;

	private final String nome;
	private final TipoLocazione bottega;
	private final PersonaggioNoto negoziante;
	private final AspettoDelMandante aspetto;
	// Il mandante come lo scrivono le missioni ("l'armaiolo"), per il suo nome nel registro
	private final String mandante;
	// La produzione di missioni.txt dei nomi di questa bottega
	private final String produzioneDelNome;

	protected IlNegozio(MissionePredefinita classe, String nome, TipoLocazione bottega, PersonaggioNoto negoziante,
						AspettoDelMandante aspetto, String mandante) {
		super(classe);
		this.nome = nome;
		this.bottega = bottega;
		this.negoziante = negoziante;
		this.aspetto = aspetto;
		this.mandante = mandante;
		this.produzioneDelNome = "NEGOZIO_NOME_" + bottega.name();
	}

	/**
	 * Assegna a ogni missione di bottega una città a caso, diversa da quelle delle altre finché ce ne sono: a inizio partita,
	 * quando le missioni predefinite sono appena nate.
	 */
	public static void assegnaLeCitta(Iterable<? extends Missione> missioni) {
		List<TipoLocazione> libere = new ArrayList<>();
		for (Missione missione : missioni) {
			if (!(missione instanceof IlNegozio)) {
				continue;
			}
			if (libere.isEmpty()) {
				for (TipoLocazione tipo : TipoLocazione.values()) {
					if (tipo.getCategoriaLocazione() == CategoriaLocazione.CITTA) {
						libere.add(tipo);
					}
				}
			}
			TipoLocazione citta = libere.remove(Dado.tiraAncheAUnaFaccia(libere.size()) - 1);
			missione.aggiungiProprieta(CITTA_ASSEGNATA, citta.name());
		}
	}

	/**
	 * La città da cui parte la missione, o null se non è stata assegnata.
	 */
	public final TipoLocazione getCittaAssegnata() {
		String citta = ottieniProprieta(CITTA_ASSEGNATA);
		return citta == null ? null : TipoLocazione.valueOf(citta);
	}

	/**
	 * Le rovine da disinfestare, o null finché la missione non le ha scelte.
	 */
	public final CoordinateMD getCovo() {
		return RegistroMissioni.getLocazioneOccupata(this);
	}

	/**
	 * Il nome del negoziante, "Mastro Ferruccio": lo pesca la prima volta che serve (vedi RegistroPersonaggi).
	 */
	private String getNomeDelNegoziante() {
		return RegistroPersonaggi.getResidente(negoziante, getCitta()).getNome();
	}

	@Override
	public String getNome() {
		return nome;
	}

	@Override
	protected boolean isRipetibile() {
		return false;
	}

	/**
	 * Se la città del negozio è distrutta non si offrirà mai più.
	 */
	@Override
	protected boolean puoAncoraOffrirsi() {
		TipoLocazione citta = getCittaAssegnata();
		return citta != null && !LineaTemporale.isCittaDistrutta(citta);
	}

	@Override
	protected boolean isPossibileQui() {
		return getCittaAssegnata() == GruppoGiocatore.getIstanza().getTipoLocazioneCorrente()
				&& (getCovo() != null || RegistroMissioni.esisteUnPostoPer(TipoLocazione.ROVINE, this));
	}

	/**
	 * Quando la missione si offre: pesca il negoziante, i testi e i nemici, e sceglie le rovine, così l'intermezzo del mandante
	 * può nominarle.
	 */
	@Override
	protected void allIncarico() {
		getNomeDelNegoziante();
		for (String produzione : new String[] {PARENTE, CAUSA, EREDITA, APPELLO, BATTUTA, RISPOSTA, VITTORIA, RINGRAZIAMENTO, APERTURA}) {
			parametro(produzione, () -> ProduttoreDiMissioniCasuale.rigaDiMissioni("NEGOZIO_" + produzione));
		}
		parametro(NEMICI, () -> ProduttoreDiMissioniCasuale.rigaDiMissioni("NEGOZIO_NEMICI"));
		trovaLeRovine();
		parametro(NOME_ROVINA, () -> TestiDeiLuoghi.nome(getCovo()));
		parametro(NOME_BOTTEGA, () -> ProduttoreDiMissioniCasuale.rigaDiMissioni(produzioneDelNome)
				.replace(SEGNAPOSTO_NOME, getNomeDelNegoziante()));
	}

	/**
	 * Le rovine dello stesso quadrante della città, se ce ne sono di libere; altrimenti quelle di un altro quadrante; se
	 * in nessun quadrante ce ne sono, si costruiscono nel proprio e, in mancanza, dovunque sulla mappa (l'incarico non si
	 * offre se non c'è posto da nessuna parte: vedi {@link #isPossibileQui()}).
	 */
	private void trovaLeRovine() {
		if (getCovo() != null) {
			return;
		}
		Quadrante proprio = Quadrante.di(Foresta.getCoordinateLocazioneUnica(getCitta()));
		List<Quadrante> quadranti = new ArrayList<>();
		quadranti.add(proprio);
		for (Quadrante quadrante : Quadrante.inOrdineCasuale()) {
			if (quadrante != proprio) {
				quadranti.add(quadrante);
			}
		}
		for (Quadrante quadrante : quadranti) {
			if (RegistroMissioni.cerca(TipoLocazione.ROVINE, this, quadrante).isPresent()) {
				return;
			}
		}
		if (!RegistroMissioni.cercaOCostruisci(TipoLocazione.ROVINE, this, proprio).isPresent()
				&& !RegistroMissioni.cercaOCostruisci(TipoLocazione.ROVINE, this).isPresent()) {
			throw new IllegalStateException("Nessun posto per le rovine di " + getNome());
		}
	}

	private InfestazioneRichiesta getInfestazione() {
		return InfestazioneRichiesta.da(parametro(NEMICI, () -> ProduttoreDiMissioniCasuale.rigaDiMissioni("NEGOZIO_NEMICI")));
	}

	/**
	 * I mostri che infestano le rovine.
	 */
	public IncontroDiMissione getNemici() {
		return IncontroDiMissione.di(getInfestazione().getNemico(), getInfestazione().getNumero());
	}

	/**
	 * Un testo della missione, con i segnaposto di missioni.txt sostituiti: il parente con il suo nome, le rovine, i mostri.
	 */
	private String testo(String parametro) {
		String nomeDelNegoziante = getNomeDelNegoziante();
		return Articoli.inizialeMaiuscola(getParametro(parametro)
				.replace(SEGNAPOSTO_PARENTE, getParametro(PARENTE) + " " + nomeDelNegoziante)
				.replace(SEGNAPOSTO_NOME, nomeDelNegoziante)
				.replace(SEGNAPOSTO_ROVINA, getParametro(NOME_ROVINA))
				.replace(SEGNAPOSTO_NEMICI, getInfestazione().getDescrizione())
				.replace(SEGNAPOSTO_BOTTEGA, getParametro(NOME_BOTTEGA)));
	}

	private String getMandanteDiCitta() {
		return mandanteDiCitta(mandante);
	}

	@Override
	public String getDescrizione() {
		if (getParametro(NOME_ROVINA) == null) {
			return Articoli.inizialeMaiuscola(mandante) + " ha un parente che vuole aprire una bottega nella Foresta.";
		}
		if (RITORNO.equals(getPassoCorrente())) {
			return "Fatto: torna " + Articoli.conPreposizione("da", mandante(mandante)) + " a " + getNomeCitta() + " a riscuotere.";
		}
		return Articoli.inizialeMaiuscola(getMandanteDiCitta()) + " ti ha chiesto di disinfestare " + getParametro(NOME_ROVINA)
				+ ", segnate sulla mappa, per un parente che ci aprirà " + getParametro(NOME_BOTTEGA) + ".";
	}

	@Override
	public String getRicordoDellaLocazione() {
		return "Qui, dove c'erano delle rovine, " + getNomeDelNegoziante() + " ha aperto " + getParametro(NOME_BOTTEGA) + ".";
	}

	@Override
	protected String primoPassoDelCompito() {
		return COVO;
	}

	@Override
	protected Passo costruisciPassoDelCompito(String id) {
		switch (id) {
			case COVO:
				return Passo.quando(MomentoControllo.IN_LOCAZIONE, this::nellaCitta)
						.esegui(() -> {
							Foresta.setLocazioneConosciuta(getCovo());
							BusEventi.pubblica(new NotificaTestoParagrafo("Il posto da disinfestare è segnato sulla mappa: "
									+ getParametro(NOME_ROVINA) + "."));
						})
						.poi(DISINFESTAZIONE);
			case DISINFESTAZIONE:
				return combatti(this::getCovo, getNemici())
						.esegui(() -> BusEventi.pubblica(new NotificaTestoParagrafo(testo(VITTORIA) + " "
								+ Articoli.inizialeMaiuscola(mandante(mandante)) + " aspetta a " + getNomeCitta() + ".")))
						.poi(RITORNO);
			default:
				throw new IllegalArgumentException("Passo sconosciuto per " + getNome() + ": " + id);
		}
	}

	@Override
	protected ScenaInCitta scenaIncarico() {
		return aspetto.nuovaScena()
				.parlaIlMandante(testo(CAUSA))
				.parlaIlMandante(testo(EREDITA) + " " + testo(APPELLO))
				.parlaIlCapo(testo(BATTUTA))
				.parlaIlMandante(testo(RISPOSTA) + " Vi segno le rovine sulla mappa. " + getRicompensa() + " monete a lavoro fatto.");
	}

	@Override
	protected ScenaInCitta scenaRingraziamento() {
		return aspetto.nuovaScena()
				.parlaIlMandante(testo(RINGRAZIAMENTO))
				.parlaIlMandante("Ecco le " + getRicompensa() + " monete promesse.");
	}

	@Override
	protected String testoAccettazione() {
		return Articoli.inizialeMaiuscola(getMandanteDiCitta()) + " pagherà " + getRicompensa() + " monete per disinfestare "
				+ getParametro(NOME_ROVINA) + ".";
	}

	@Override
	protected String testoRicompensa() {
		return Articoli.inizialeMaiuscola(mandante(mandante)) + " paga le " + getRicompensa() + " monete promesse.";
	}

	@Override
	protected int getRicompensaBase() {
		return RICOMPENSA_BASE;
	}

	/**
	 * Completa la missione, e le rovine diventano la bottega.
	 */
	@Override
	public void completaMissione() {
		super.completaMissione();
		apriLaBottega();
	}

	/**
	 * Le rovine diventano la bottega: con il nome della bottega, il negoziante e un magazzino generato da zero, e conosciuta
	 * sulla mappa.
	 */
	private void apriLaBottega() {
		CoordinateMD posto = getCovo();
		Foresta.costruisciLocazione(posto, bottega);
		LocazioneMD modelloDati = Foresta.getLocazioneMD(posto);
		modelloDati.setNome(getParametro(NOME_BOTTEGA));
		modelloDati.aggiungiProprieta(Bottega.NEGOZIANTE, RegistroPersonaggi.getNomeCompleto(RegistroPersonaggi.getResidente(negoziante, getCitta())));
		if (bottega == TipoLocazione.ARMAIOLO) {
			RegistroArtefatti.riempiMagazzino(posto, TipoNegozio.ARMAIOLO);
		} else if (bottega == TipoLocazione.VENDITORE_DI_PERGAMENE) {
			RegistroArtefatti.riempiMagazzino(posto, TipoNegozio.VENDITORE_DI_PERGAMENE);
		}
		Foresta.setLocazioneConosciuta(posto);
		BusEventi.pubblica(new NotificaTestoParagrafo(testo(APERTURA)));
	}
}
