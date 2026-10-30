package com.threeamigos.foresta.missioni.incarichi;

import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.artefatti.leggendari.OggettoLeggendario;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaArtefattoTrovato;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.intermezzi.PaginaIntermezzo;
import com.threeamigos.foresta.intermezzi.ScenaFraCompagni;
import com.threeamigos.foresta.intermezzi.ScenaInCitta;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.missioni.IncontroDiMissione;
import com.threeamigos.foresta.missioni.Missione;
import com.threeamigos.foresta.missioni.Passo;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.missioni.RegistroMissioni;
import com.threeamigos.foresta.missioni.leggende.ConLeggendario;
import com.threeamigos.foresta.missioni.leggende.LaLeggenda;
import com.threeamigos.foresta.missioni.leggende.PescaLeggendaria;
import com.threeamigos.foresta.missioni.richieste.TappeRichieste;
import com.threeamigos.foresta.missioni.richieste.TappeRichieste.Posto;
import com.threeamigos.foresta.missioni.richieste.TappeRichieste.Tappa;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.statistiche.Statistiche;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.testi.Articoli;
import com.threeamigos.foresta.testi.ProduttoreDiMissioniCasuale;
import com.threeamigos.foresta.tipi.CategoriaLocazione;
import com.threeamigos.foresta.tipi.ClasseDiPosto;
import com.threeamigos.foresta.tipi.CostantiMissioni;
import com.threeamigos.foresta.tipi.MissionePredefinita;
import com.threeamigos.foresta.tipi.MomentoIntermezzo;
import com.threeamigos.foresta.tipi.TipoLocazione;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.IntPredicate;

/**
 * Una missione a tappe (vedi missione_a_tappe.md e TappeRichieste, da tappe.txt): in una città qualcuno chiede di ritrovare una persona o un
 * oggetto leggendario, e dice da dove cominciare. Il gruppo va di posto in posto: ogni tappa, una volta raggiunta, dà un'informazione che
 * dice dove andare dopo, e il posto della tappa successiva, vicino a quello, compare sulla mappa. Nell'ultimo posto sta ciò che si cerca; poi si
 * torna dal mandante a riscuotere. Una missione su tre è una farsa (vedi CostantiMissioni): una richiesta senza senso, e al più due posti.
 * <ol>
 * <li>INCARICO e ACCETTAZIONE, come un incarico in città (vedi IncaricoInCitta): la scena del mandante, che dice da dove cominciare;</li>
 * <li>TAPPA_n, per ogni tappa: il gruppo arriva nel posto (e, se c'è una guardia, a fine locazione la sconfigge): scena d'arrivo, informazione
 * e posto della tappa dopo;</li>
 * <li>ARRIVO, nell'ultimo posto: con una guardia, la persona è prigioniera; con un leggendario, i suoi custodi; poi la persona è libera o il
 * leggendario passa al gruppo;</li>
 * <li>RITORNO, CONSEGNA e RICOMPENSA, come un incarico in città.</li>
 * </ol>
 * I posti che non sono in guardia sono sicuri: niente avversari né oggetti a caso, come nei templi delle benedizioni.
 * Con la scelta fra due piste, dopo l'ultima tappa i due posti sono segnati sulla mappa (BIVIO): il primo in cui arriva il gruppo
 * decide. Se è quello giusto la catena prosegue; se è il vicolo cieco (PISTA_A o PISTA_B) si legge il testo che manda all'altro, e il
 * posto giusto resta l'unico. Con le chiavi, dopo l'ultima tappa i posti dei tre custodi (e, se uno non combatte, quello dell'oggetto
 * che vuole) sono segnati sulla mappa, e la missione affida tre figlie ({@link ChiaveDelCustode}); quando sono finite si apre l'ultimo
 * posto.
 */
public class MissioneATappe extends IncaricoInCitta implements ConLeggendario {

	private static final String RIGA = "RIGA";
	private static final String LEGGENDARIO = "LEGGENDARIO";
	private static final String POSTO = "POSTO_";
	private static final String CITTA_DEL_POSTO = "CITTA_DEL_POSTO_";
	private static final String TAPPA = "TAPPA_";
	private static final String ARRIVO = "ARRIVO";
	private static final String BIVIO = "BIVIO";
	private static final String PISTA = "PISTA_";
	private static final String PISTA_FALSA_VISTA = "PISTA_FALSA_VISTA";
	private static final String CHIAVI = "CHIAVI";
	private static final String ATTESA_DELLE_CHIAVI = "ATTESA_DELLE_CHIAVI";
	private static final String SEPARATORE = ",";
	// Le righe che non si possono ancora giocare si scartano: fra quelle di tappe.txt le altre sono la maggior parte
	private static final int TENTATIVI_PER_LA_RIGA = 500;

	private String rigaLetta;
	private TappeRichieste richiestaLetta;

	public MissioneATappe() {
		super(MissionePredefinita.MISSIONE_A_TAPPE);
	}

	/**
	 * La richiesta di questa missione, da una riga di tappe.txt pescata la prima volta che serve e poi sempre la stessa, anche dopo un
	 * caricamento.
	 */
	public final TappeRichieste getRichiesta() {
		String riga = parametro(RIGA, this::pescaLaRiga);
		if (!riga.equals(rigaLetta)) {
			richiestaLetta = TappeRichieste.da(riga);
			rigaLetta = riga;
		}
		return richiestaLetta;
	}

	/**
	 * Se la richiesta è già stata pescata: finché la missione non si offre non lo è, e nome e descrizione sono generici.
	 */
	private boolean isRichiestaPescata() {
		return getParametro(RIGA) != null;
	}

	private String pescaLaRiga() {
		boolean farsa = Dado.tira(100) <= CostantiMissioni.PERCENTUALE_PROBABILITA_FARSA_NELLE_TAPPE;
		for (int tentativo = 0; tentativo < TENTATIVI_PER_LA_RIGA; tentativo++) {
			TappeRichieste richiesta = TappeRichieste.da(ProduttoreDiMissioniCasuale.rigaDiTappe(farsa));
			if (richiesta.isDiUnLeggendario() && !PescaLeggendaria.restaUnLeggendario(PescaLeggendaria.giaPescati(this))) {
				continue;
			}
			return richiesta.getRiga();
		}
		throw new IllegalStateException("Nessuna riga di tappe.txt si può giocare");
	}

	/**
	 * Il leggendario in palio, o null se la missione non ne ha uno o non l'ha ancora pescato.
	 */
	@Override
	public final OggettoLeggendario getLeggendario() {
		String riga = ottieniProprieta(LEGGENDARIO);
		return riga == null ? null : OggettoLeggendario.da(riga);
	}

	// --- I posti

	/**
	 * Quanti posti ha la missione: le tappe e l'ultimo.
	 */
	private int numeroDiPosti() {
		TappeRichieste richiesta = getRichiesta();
		return richiesta.getNumeroDiPosti() + (richiesta.hasChiavi() ? TappeRichieste.CUSTODI + (indiceDelCustodeInScambio() > 0 ? 1 : 0) : 0);
	}

	/**
	 * Quale custode (da 1) vuole un oggetto invece di combattere, o 0.
	 */
	private int indiceDelCustodeInScambio() {
		List<TappeRichieste.Custode> custodi = getRichiesta().getCustodi();
		for (int i = 0; i < custodi.size(); i++) {
			if (custodi.get(i).isInScambio()) {
				return i + 1;
			}
		}
		return 0;
	}

	private Tappa tappaDi(int indice) {
		TappeRichieste richiesta = getRichiesta();
		int tappe = richiesta.getTappe().size();
		if (indice <= tappe) {
			return richiesta.getTappe().get(indice - 1);
		}
		if (richiesta.getBivio().isPresent() && indice <= tappe + 2) {
			return indice == tappe + 1 ? richiesta.getBivio().get().getPistaA() : richiesta.getBivio().get().getPistaB();
		}
		return null;
	}

	private Posto postoDi(int indice) {
		Tappa tappa = tappaDi(indice);
		return tappa != null ? tappa.getPosto() : getRichiesta().getArrivo().getPosto();
	}

	/**
	 * Il posto della pista giusta del bivio.
	 */
	private int indiceDellaPistaGiusta() {
		TappeRichieste richiesta = getRichiesta();
		int tappe = richiesta.getTappe().size();
		return richiesta.getBivio().get().getPistaGiusta() == richiesta.getBivio().get().getPistaA() ? tappe + 1 : tappe + 2;
	}

	/**
	 * Il posto da cui si sceglie quello di {@code indice}: di norma il precedente; i due del bivio partono dall'ultima tappa e l'arrivo dalla
	 * pista giusta.
	 */
	private int precedenteDi(int indice) {
		TappeRichieste richiesta = getRichiesta();
		if (richiesta.hasChiavi()) {
			int tappe = richiesta.getTappe().size();
			if (indice > tappe + TappeRichieste.CUSTODI && indice < numeroDiPosti()) {
				return tappe + indiceDelCustodeInScambio();
			}
			return indice > tappe ? tappe : indice - 1;
		}
		if (richiesta.getBivio().isPresent()) {
			int tappe = richiesta.getTappe().size();
			if (indice == tappe + 2) {
				return tappe;
			}
			if (indice == tappe + 3) {
				return indiceDellaPistaGiusta();
			}
		}
		return indice - 1;
	}

	private ClasseDiPosto classeDelPosto(int indice) {
		TappeRichieste richiesta = getRichiesta();
		int tappe = richiesta.getTappe().size();
		if (richiesta.hasChiavi() && indice > tappe && indice < numeroDiPosti()) {
			int custode = indice - tappe;
			return custode <= TappeRichieste.CUSTODI ? richiesta.getCustodi().get(custode - 1).getClasse()
					: richiesta.getCustodi().get(indiceDelCustodeInScambio() - 1).getScambio().get().getPosto().getClasse();
		}
		return postoDi(indice).getClasse();
	}

	/**
	 * Dove sta il posto, o null finché la missione non l'ha scelto.
	 */
	private CoordinateMD coordinateDelPosto(int indice) {
		String valore = ottieniProprieta(POSTO + indice);
		if (valore == null) {
			return null;
		}
		String[] parti = valore.split(SEPARATORE);
		return new CoordinateMD(Integer.parseInt(parti[0]), Integer.parseInt(parti[1]));
	}

	private TipoLocazione cittaDelPosto(int indice) {
		String citta = ottieniProprieta(CITTA_DEL_POSTO + indice);
		return citta == null ? null : TipoLocazione.valueOf(citta);
	}

	private boolean isNelPosto(int indice) {
		if (classeDelPosto(indice).isCitta()) {
			TipoLocazione citta = cittaDelPosto(indice);
			return citta != null && Foresta.isGruppoIn(citta);
		}
		CoordinateMD coordinate = coordinateDelPosto(indice);
		return coordinate != null && coordinate.equals(Foresta.getPosizioneDelGruppo());
	}

	private Set<CoordinateMD> postiGiaScelti(int prima) {
		Set<CoordinateMD> scelti = new HashSet<>();
		for (int i = 1; i < prima; i++) {
			CoordinateMD coordinate = coordinateDelPosto(i);
			if (coordinate != null) {
				scelti.add(coordinate);
			}
		}
		return scelti;
	}

	/**
	 * Sceglie il posto di quella tappa, vicino al precedente (la prima, vicino al gruppo), mai uno già usato dalla missione, e lo segna sulla
	 * mappa. Una città è una a caso fra le altre in piedi.
	 *
	 * @return false se non c'è un posto adatto
	 */
	private boolean rivendicaIlPosto(int indice) {
		ClasseDiPosto classe = classeDelPosto(indice);
		CoordinateMD scelto;
		if (classe.isCitta()) {
			scelto = scegliUnaCitta(indice);
		} else {
			CoordinateMD vicino = indice > 1 ? coordinateDelPosto(precedenteDi(indice)) : Foresta.getPosizioneDelGruppo();
			Optional<CoordinateMD> trovato = RegistroMissioni.cercaOCostruisciVicino(classe.getLocazione().get(), this, vicino, postiGiaScelti(indice));
			scelto = trovato.orElse(null);
		}
		if (scelto == null) {
			return false;
		}
		aggiungiProprieta(POSTO + indice, scelto.getX() + SEPARATORE + scelto.getY());
		Foresta.setLocazioneConosciuta(scelto);
		return true;
	}

	private CoordinateMD scegliUnaCitta(int indice) {
		List<TipoLocazione> possibili = altreCitta();
		for (int i = 1; i < indice; i++) {
			possibili.remove(cittaDelPosto(i));
		}
		if (possibili.isEmpty()) {
			return null;
		}
		TipoLocazione citta = possibili.get(Dado.tiraAncheAUnaFaccia(possibili.size()) - 1);
		aggiungiProprieta(CITTA_DEL_POSTO + indice, citta.name());
		return Foresta.getCoordinateLocazioneUnica(citta);
	}

	/**
	 * Le città in piedi, tranne quella del mandante.
	 */
	private List<TipoLocazione> altreCitta() {
		List<TipoLocazione> altre = new ArrayList<>();
		for (TipoLocazione citta : TipoLocazione.values()) {
			if (citta.getCategoriaLocazione() == CategoriaLocazione.CITTA && citta != getCitta() && !Foresta.isCittaDistrutta(citta)
					&& Foresta.getCoordinateLocazioneUnica(citta) != null) {
				altre.add(citta);
			}
		}
		return altre;
	}

	/**
	 * Il nome del posto come lo si scrive nei testi, con l'articolo: "Malgaard", "le Vestigia dell'Antico Impero", "la grotta segnata sulla mappa".
	 */
	private String nomeDelPosto(int indice) {
		ClasseDiPosto classe = classeDelPosto(indice);
		CoordinateMD coordinate = coordinateDelPosto(indice);
		switch (classe) {
			case CITTA:
				return nomeDellaCitta(cittaDelPosto(indice));
			case LOCANDA:
				String nomeDellaLocanda = Foresta.getLocazioneMD(coordinate).getNome();
				return nomeDellaLocanda == null ? "la locanda segnata sulla mappa" : "la locanda " + nomeDellaLocanda;
			case ROVINE:
				return Foresta.nomeDelleRovine(Foresta.getLocazioneMD(coordinate));
			case TEMPIO:
				return Foresta.nomeDelTempio(coordinate);
			case GROTTA:
				return "la grotta segnata sulla mappa";
			case BOSCO:
				return "il bosco segnato sulla mappa";
			case PALUDE:
				return "la palude segnata sulla mappa";
			default:
				return "la radura segnata sulla mappa";
		}
	}

	private static String conPreposizione(String preposizione, String nome) {
		// Le città e i nomi propri senza articolo (cominciano con la maiuscola) non si fondono con la preposizione
		return Character.isUpperCase(nome.charAt(0)) ? preposizione + " " + nome : Articoli.conPreposizione(preposizione, nome);
	}

	/**
	 * Il testo della grammatica con i segnaposto risolti: la paga vera, il leggendario, e il posto della tappa {@code prossimo} (da 1) nelle sue forme
	 * con la preposizione; con 0 il posto non conta.
	 */
	private String testo(String testo, int prossimo) {
		return testo(testo, prossimo, 0);
	}

	/**
	 * Come {@link #testo(String, int)}, e in più i nomi dei due posti del bivio e, in un vicolo cieco, {@code altra} (da 1), il posto giusto.
	 */
	private String testo(String testo, int prossimo, int altra) {
		String risolto = testo.replace(TappeRichieste.MONETE, String.valueOf(getRicompensa()));
		int tappe = getRichiesta().getTappe().size();
		if (getRichiesta().getBivio().isPresent() && coordinateDelPosto(tappe + 2) != null) {
			String a = nomeDelPosto(tappe + 1);
			String b = nomeDelPosto(tappe + 2);
			risolto = risolto.replace("%A_PISTA_A%", conPreposizione("a", a)).replace("%DI_PISTA_A%", conPreposizione("di", a))
					.replace("%A_PISTA_B%", conPreposizione("a", b)).replace("%DI_PISTA_B%", conPreposizione("di", b))
					.replace(TappeRichieste.PISTA_A, a).replace(TappeRichieste.PISTA_B, b);
		}
		if (getRichiesta().hasChiavi()) {
			for (int custode = 1; custode <= TappeRichieste.CUSTODI; custode++) {
				if (coordinateDelPosto(tappe + custode) != null) {
					risolto = risolto.replace("%POSTO_" + custode + "%", nomeDelPosto(tappe + custode));
				}
			}
		}
		if (altra >= 1 && coordinateDelPosto(altra) != null) {
			risolto = risolto.replace(TappeRichieste.A_ALTRA, conPreposizione("a", nomeDelPosto(altra)));
		}
		if (prossimo >= 1 && prossimo <= numeroDiPosti() && coordinateDelPosto(prossimo) != null) {
			String nome = nomeDelPosto(prossimo);
			risolto = risolto.replace(TappeRichieste.A_PROSSIMO, conPreposizione("a", nome))
					.replace(TappeRichieste.DI_PROSSIMO, conPreposizione("di", nome))
					.replace(TappeRichieste.DA_PROSSIMO, conPreposizione("da", nome))
					.replace(TappeRichieste.PROSSIMO, nome);
		}
		OggettoLeggendario leggendario = getLeggendario();
		if (leggendario != null) {
			risolto = risolto.replace(TappeRichieste.DI_OGGETTO, conPreposizione("di", leggendario.getNomeBreve()))
					.replace(TappeRichieste.OGGETTO, leggendario.getNomeBreve());
		}
		return risolto;
	}

	// --- La missione

	@Override
	public String getNome() {
		return isRichiestaPescata() ? getRichiesta().getTitolo() : "Una missione a tappe";
	}

	private String testoDelRitorno(TappeRichieste richiesta) {
		return "Fatto: torna " + Articoli.conPreposizione("da", mandante(richiesta.getMandante())) + " a " + getNomeCitta() + " a riscuotere.";
	}

	@Override
	public String getDescrizione() {
		if (!isRichiestaPescata() || !isAttiva()) {
			return "Prima o poi qualcuno, in una città, chiederà di ritrovare una persona o un oggetto, di posto in posto.";
		}
		TappeRichieste richiesta = getRichiesta();
		String passo = getPassoCorrente();
		if (RITORNO.equals(passo)) {
			return testoDelRitorno(richiesta);
		}
		// Una missione finita si legge ancora fra le completate
		if (Passo.FINE.equals(passo)) {
			return mandanteEChiesto(richiesta) + " Missione compiuta.";
		}
		int tappe = richiesta.getTappe().size();
		int corrente;
		if (ARRIVO.equals(passo)) {
			corrente = numeroDiPosti();
		} else if (BIVIO.equals(passo) || passo.startsWith(PISTA)) {
			corrente = tappe + 1;
		} else if (CHIAVI.equals(passo) || ATTESA_DELLE_CHIAVI.equals(passo)) {
			long prese = getMissioniAffidate(CHIAVI).stream().filter(Missione::isCompleta).count();
			return mandanteEChiesto(richiesta) + " Le chiavi da prendere sono " + TappeRichieste.CUSTODI + " (ne hai " + prese
					+ "), nei posti segnati sulla mappa, in qualunque ordine.";
		} else if (passo.startsWith(TAPPA)) {
			corrente = Integer.parseInt(passo.substring(TAPPA.length()));
		} else {
			// CONSEGNA e RICOMPENSA: le tappe sono finite, resta da riscuotere in città
			return testoDelRitorno(richiesta);
		}
		StringBuilder descrizione = new StringBuilder(mandanteEChiesto(richiesta));
		for (int tappa = 1; tappa < corrente && tappa <= richiesta.getTappe().size(); tappa++) {
			descrizione.append(" Informazione ").append(tappa).append(": ")
					.append(testo(richiesta.getTappe().get(tappa - 1).getInformazione(), tappa + 1));
		}
		if (richiesta.getBivio().isPresent() && corrente == tappe + 1 && coordinateDelPosto(tappe + 2) != null) {
			int giusto = indiceDellaPistaGiusta();
			descrizione.append(" Le piste sono due: ");
			if (isPistaFalsaVista()) {
				descrizione.append(nomeDelPosto(giusto));
			} else {
				descrizione.append(nomeDelPosto(tappe + 1)).append(" e ").append(nomeDelPosto(tappe + 2));
			}
			descrizione.append(", segnate sulla mappa.");
		} else if (coordinateDelPosto(corrente) != null) {
			descrizione.append(" Il prossimo posto è ").append(nomeDelPosto(corrente)).append(", segnato sulla mappa.");
		}
		return descrizione.toString();
	}

	private String mandanteEChiesto(TappeRichieste richiesta) {
		return Articoli.inizialeMaiuscola(mandanteDiCitta(richiesta.getMandante())) + " ti ha affidato una missione: «" + richiesta.getTitolo() + "».";
	}

	@Override
	public String getRicordoDellaLocazione() {
		return isFallita() || !isRichiestaPescata() ? null : testo(getRichiesta().getRicordo(), 0);
	}

	@Override
	protected int getRicompensaBase() {
		return getRichiesta().getMonete();
	}

	/**
	 * Si può offrire solo se c'è un'altra città in piedi, dove può cominciare la pista: senza pescare la riga, che altrimenti consumerebbe i dadi
	 * a ogni visita in città.
	 */
	@Override
	protected boolean isPossibileQui() {
		return !altreCitta().isEmpty();
	}

	@Override
	protected void allIncarico() {
		TappeRichieste richiesta = getRichiesta();
		if (richiesta.isDiUnLeggendario() && getLeggendario() == null) {
			aggiungiProprieta(LEGGENDARIO, PescaLeggendaria.pesca(PescaLeggendaria.giaPescati(this), 0));
		}
		if (!rivendicaIlPosto(1)) {
			// Nessun posto adatto, né da trovare né da costruire: non si può cominciare
			fallisciMissione();
		}
	}

	@Override
	protected ScenaInCitta scenaIncarico() {
		TappeRichieste richiesta = getRichiesta();
		ScenaInCitta scena = richiesta.getAspetto().nuovaScena()
				.parlaIlMandante(testo(richiesta.getRichiesta(), 1))
				.parlaIlCapo(richiesta.getBattutaDelCapo())
				.parlaIlMandante(testo(richiesta.getRisposta(), 1));
		if (richiesta.isFarsa()) {
			scena.parlaIlCapo(richiesta.getSecondaBattutaDelCapo().get()).parlaIlMandante(testo(richiesta.getSecondaRisposta().get(), 1));
		}
		return scena;
	}

	@Override
	protected ScenaInCitta scenaRingraziamento() {
		TappeRichieste richiesta = getRichiesta();
		return richiesta.getAspetto().nuovaScena()
				.parlaIlMandante(testo(richiesta.getRingraziamento(), 0))
				.parlaIlMandante("Ecco le " + getRicompensa() + " monete promesse.");
	}

	@Override
	protected String testoAccettazione() {
		return Articoli.inizialeMaiuscola(mandanteDiCitta(getRichiesta().getMandante())) + " pagherà " + getRicompensa() + " monete.";
	}

	@Override
	protected String testoRicompensa() {
		return Articoli.inizialeMaiuscola(mandante(getRichiesta().getMandante())) + " paga le " + getRicompensa() + " monete promesse.";
	}

	// --- I passi

	@Override
	protected String primoPassoDelCompito() {
		return getRichiesta().getTappe().isEmpty() ? ARRIVO : TAPPA + 1;
	}

	@Override
	protected Passo costruisciPassoDelCompito(String id) {
		if (BIVIO.equals(id)) {
			return passoDelBivio();
		}
		if (CHIAVI.equals(id)) {
			return affida(CHIAVI, MomentoControllo.POST_LOCAZIONE, () -> true, this::creaLeChiavi).poi(ATTESA_DELLE_CHIAVI);
		}
		if (ATTESA_DELLE_CHIAVI.equals(id)) {
			return attendiLeAffidate(CHIAVI, MomentoControllo.POST_LOCAZIONE).esegui(this::apriLUltimoPosto).poi(ARRIVO);
		}
		if (id.startsWith(PISTA)) {
			return passoDellaPista(getRichiesta().getTappe().size() + ("A".equals(id.substring(PISTA.length())) ? 1 : 2));
		}
		int indice = ARRIVO.equals(id) ? numeroDiPosti() : Integer.parseInt(id.substring(TAPPA.length()));
		return passoDelPosto(indice);
	}

	/**
	 * Le tre figlie, una per custode, con i posti già scelti e segnati.
	 */
	private List<Missione> creaLeChiavi() {
		TappeRichieste richiesta = getRichiesta();
		int tappe = richiesta.getTappe().size();
		List<Missione> figlie = new ArrayList<>();
		for (int k = 1; k <= TappeRichieste.CUSTODI; k++) {
			TappeRichieste.Custode custode = richiesta.getCustodi().get(k - 1);
			String capo = custode.isConCapo() ? custode.pescaNomeDelCapo() : null;
			String testoDelCustode = custode.getTesto().replace(TappeRichieste.CAPO, capo == null ? "" : capo);
			String chiave = richiesta.getOggettiChiave().get(k - 1);
			CoordinateMD posto = coordinateDelPosto(tappe + k);
			if (custode.isInScambio()) {
				TappeRichieste.Scambio scambio = custode.getScambio().get();
				int indiceDellOggetto = numeroDiPosti() - 1;
				figlie.add(ChiaveDelCustode.cheVuoleUnOggetto(posto, nomeDelPosto(tappe + k), capo, chiave, testoDelCustode, scambio.getOggetto(),
						scambio.getTesto(), coordinateDelPosto(indiceDellOggetto), nomeDelPosto(indiceDellOggetto),
						scambio.getPosto().getGuardia().get(), scambio.getPosto().getNumero()));
			} else {
				figlie.add(ChiaveDelCustode.chePerCombattere(posto, nomeDelPosto(tappe + k), custode.getNemico().get(), custode.getNumero(), capo,
						chiave, testoDelCustode));
			}
		}
		return figlie;
	}

	/**
	 * Le chiavi sono tutte prese: si segna l'ultimo posto.
	 */
	private void apriLUltimoPosto() {
		if (!sonoRiusciteLeAffidate(CHIAVI) || !rivendicaIlPosto(numeroDiPosti())) {
			BusEventi.pubblica(new NotificaTestoParagrafo("Senza le chiavi non c'è modo di andare avanti."));
			fallisciMissione();
			return;
		}
		BusEventi.pubblica(new NotificaTestoParagrafo("Con tutte le chiavi in mano si può aprire l'ultimo posto: " + nomeDelPosto(numeroDiPosti())
				+ ", segnato sulla mappa."));
	}

	private boolean isPistaFalsaVista() {
		return ottieniProprieta(PISTA_FALSA_VISTA) != null;
	}

	/**
	 * Il gruppo arriva in uno dei due posti: se è la pista giusta o la falsa non ancora provata, si passa al passo di quella pista. Se il
	 * posto non ha guardia è sicuro, come ogni posto senza guardia.
	 */
	private Passo passoDelBivio() {
		int a = getRichiesta().getTappe().size() + 1;
		int b = a + 1;
		int giusto = indiceDellaPistaGiusta();
		IntPredicate valido = i -> i == giusto || !isPistaFalsaVista();
		Passo passo = Passo.quando(MomentoControllo.PRE_LOCAZIONE, () -> valido.test(a) && isNelPosto(a) || valido.test(b) && isNelPosto(b));
		passo = passo.aOgniControllo(() -> {
			for (int i : new int[] {a, b}) {
				if (valido.test(i) && isNelPosto(i) && !classeDelPosto(i).isCitta() && !postoDi(i).hasGuardia()) {
					RegistroMissioni.sopprimiContenutoLocazione(coordinateDelPosto(i));
				}
			}
		});
		return passo.poi(PISTA + (isNelPosto(b) ? "B" : "A"));
	}

	/**
	 * Una delle due piste, a fine locazione (e dopo la guardia, se c'è): la giusta dà l'informazione per l'ultimo posto, la falsa manda
	 * all'altra e non si prova più.
	 */
	private Passo passoDellaPista(int indice) {
		boolean giusta = indice == indiceDellaPistaGiusta();
		Passo passo = hasGuardia(indice) ? combatti(() -> coordinateDelPosto(indice), incontroDelPosto(indice))
				: Passo.quando(MomentoControllo.POST_LOCAZIONE, () -> isNelPosto(indice));
		return passo.esegui(() -> concludiLaPista(indice, giusta))
				.conIntermezzo(MomentoIntermezzo.LOCAZIONE_COMPLETATA, () -> pagineDellaTappa(indice))
				.poi(giusta ? ARRIVO : BIVIO);
	}

	private void concludiLaPista(int indice, boolean giusta) {
		Tappa pista = tappaDi(indice);
		if (!giusta) {
			aggiungiProprieta(PISTA_FALSA_VISTA, "SI");
			BusEventi.pubblica(new NotificaTestoParagrafo(testo(pista.getInformazione(), 0, indiceDellaPistaGiusta())));
			return;
		}
		int arrivo = numeroDiPosti();
		if (!rivendicaIlPosto(arrivo)) {
			BusEventi.pubblica(new NotificaTestoParagrafo("La pista si interrompe qui: non c'è più nessun posto dove cercare."));
			fallisciMissione();
			return;
		}
		BusEventi.pubblica(new NotificaTestoParagrafo(testo(pista.getInformazione(), arrivo)));
	}

	/**
	 * Il passo di un posto: il gruppo ci arriva e, se c'è una guardia, a fine locazione la sconfigge; poi si ricevono l'informazione sul posto
	 * dopo (o, all'ultimo, ciò che si cercava) e, se non è l'ultimo, la scena d'arrivo.
	 */
	private Passo passoDelPosto(int indice) {
		boolean ultimo = indice == numeroDiPosti();
		ClasseDiPosto classe = classeDelPosto(indice);
		boolean conGuardia = hasGuardia(indice);
		Passo passo;
		if (conGuardia) {
			passo = combatti(() -> coordinateDelPosto(indice), incontroDelPosto(indice));
		} else {
			passo = Passo.quando(MomentoControllo.PRE_LOCAZIONE, () -> isNelPosto(indice));
			if (classe.isCitta()) {
				passo = passo.segnala(() -> Foresta.getCoordinateLocazioneUnica(cittaDelPosto(indice)));
			} else {
				// Un posto senza guardia è sicuro: niente avversari né oggetti a caso
				passo = passo.aOgniControllo(() -> {
					if (isNelPosto(indice)) {
						RegistroMissioni.sopprimiContenutoLocazione(coordinateDelPosto(indice));
					}
				});
			}
		}
		passo = passo.esegui(() -> concludiIlPosto(indice));
		if (!ultimo) {
			passo = passo.conIntermezzo(conGuardia ? MomentoIntermezzo.LOCAZIONE_COMPLETATA : MomentoIntermezzo.INIZIO_LOCAZIONE,
					() -> pagineDellaTappa(indice));
		}
		if (classe.isCitta()) {
			passo = passo.falliscoSe(() -> Foresta.isCittaDistrutta(cittaDelPosto(indice)),
					() -> nomeDellaCitta(cittaDelPosto(indice)) + " è stata distrutta: la pista si perde.");
		}
		if (!ultimo && indice == getRichiesta().getTappe().size() && (getRichiesta().getBivio().isPresent() || getRichiesta().hasChiavi())) {
			return passo.poi(getRichiesta().hasChiavi() ? CHIAVI : BIVIO);
		}
		return passo.poi(ultimo ? RITORNO : indice + 1 == numeroDiPosti() ? ARRIVO : TAPPA + (indice + 1));
	}

	/**
	 * Se il posto ha chi lo custodisce: la guardia della riga o, all'ultimo posto di un leggendario, i suoi custodi.
	 */
	private boolean hasGuardia(int indice) {
		return postoDi(indice).hasGuardia() || indice == numeroDiPosti() && getRichiesta().isDiUnLeggendario();
	}

	private IncontroDiMissione incontroDelPosto(int indice) {
		Posto posto = postoDi(indice);
		if (posto.hasGuardia()) {
			return IncontroDiMissione.di(posto.getGuardia().get(), posto.getNumero());
		}
		return LaLeggenda.guardianiDi(getLeggendario().getChiave(), getLivelloDellIncarico());
	}

	private void concludiIlPosto(int indice) {
		TappeRichieste richiesta = getRichiesta();
		if (indice == numeroDiPosti()) {
			BusEventi.pubblica(new NotificaTestoParagrafo(testo(richiesta.getArrivo().getTesto(), 0)));
			if (richiesta.isDiUnLeggendario()) {
				consegnaIlLeggendario();
			}
			return;
		}
		int prossimo = indice + 1;
		if (richiesta.hasChiavi() && indice == richiesta.getTappe().size()) {
			// I posti dei custodi, e quello dell'oggetto che uno di loro vuole
			for (int posto = prossimo; posto < numeroDiPosti(); posto++) {
				if (!rivendicaIlPosto(posto)) {
					BusEventi.pubblica(new NotificaTestoParagrafo("La pista si interrompe qui: non c'è più nessun posto dove cercare."));
					fallisciMissione();
					return;
				}
			}
			BusEventi.pubblica(new NotificaTestoParagrafo(testo(richiesta.getTappe().get(indice - 1).getInformazione(), 0)));
			return;
		}
		if (getRichiesta().getBivio().isPresent() && indice == richiesta.getTappe().size()) {
			if (!rivendicaIlPosto(prossimo) || !rivendicaIlPosto(prossimo + 1)) {
				BusEventi.pubblica(new NotificaTestoParagrafo("La pista si interrompe qui: non c'è più nessun posto dove cercare."));
				fallisciMissione();
				return;
			}
			BusEventi.pubblica(new NotificaTestoParagrafo(testo(richiesta.getTappe().get(indice - 1).getInformazione(), 0)));
			return;
		}
		if (!rivendicaIlPosto(prossimo)) {
			BusEventi.pubblica(new NotificaTestoParagrafo("La pista si interrompe qui: non c'è più nessun posto dove cercare."));
			fallisciMissione();
			return;
		}
		BusEventi.pubblica(new NotificaTestoParagrafo(testo(richiesta.getTappe().get(indice - 1).getInformazione(), prossimo)));
	}

	private void consegnaIlLeggendario() {
		OggettoLeggendario leggendario = getLeggendario();
		Artefatto artefatto = leggendario.costruisci(Statistiche.getLivello());
		GruppoGiocatore.getIstanza().addArtefatto(artefatto);
		BusEventi.pubblica(new NotificaTestoParagrafo("I custodi sono sconfitti: " + leggendario.getNomeBreve() + " è nelle vostre mani."));
		BusEventi.pubblica(new NotificaArtefattoTrovato(artefatto, Statistiche.getLivello()));
	}

	/**
	 * La scena d'arrivo di una tappa: la narrazione, la battuta del capo e, se il gruppo ha un compagno, la sua.
	 */
	private List<PaginaIntermezzo> pagineDellaTappa(int indice) {
		TappeRichieste.Scena scena = tappaDi(indice).getScena();
		GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
		List<Personaggio> inScena = new ArrayList<>();
		inScena.add(gruppo.getCapo());
		gruppo.getPersonaggiVivi().stream().filter(personaggio -> personaggio != gruppo.getCapo()).forEach(inScena::add);
		TipoLocazione sfondo = classeDelPosto(indice).isCitta() ? cittaDelPosto(indice) : classeDelPosto(indice).getLocazione().get();
		ScenaFraCompagni pagina = ScenaFraCompagni.in(sfondo, scena.getNarrazione(), inScena).parlaIlPrimo(scena.getBattutaDelCapo());
		if (inScena.size() > 1 && scena.getBattutaDiUnCompagno().isPresent()) {
			pagina.parlaIlSecondo(scena.getBattutaDiUnCompagno().get());
		}
		return pagina.getPagine();
	}
}
