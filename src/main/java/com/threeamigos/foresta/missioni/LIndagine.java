package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.intermezzi.ScenaInCitta;
import com.threeamigos.foresta.missioni.IndagineRichiesta.Indizio;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.motore.Foresta;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.ProduttoreDiTestiCasuale;
import com.threeamigos.foresta.motore.RegistroMissioni;
import com.threeamigos.foresta.tipi.ClasseMissione;
import com.threeamigos.foresta.strumenti.Misc;

import java.util.List;

/**
 * In città qualcuno chiede di scoprire chi ha fatto qualcosa: chi ha rubato le campane, chi passa le notizie al nemico,
 * chi spaventa i viandanti (vedi {@link IndagineRichiesta}, da missioni.txt). Gli indizi stanno in due o tre posti,
 * che la missione segna sulla mappa uno alla volta: in ognuno se ne trova uno, che resta scritto nella descrizione
 * della missione. Dopo l'ultimo il giocatore sceglie il colpevole fra i sospetti (vedi {@link Passo#chiediScelta}):
 * se indovina, il nascondiglio del colpevole compare sulla mappa e lì va sconfitto, poi si torna a riscuotere (vedi
 * IncaricoInCitta); se accusa un innocente, la missione fallisce. L'indagine si pesca quando si offre: la missione si
 * ripete con altre.
 * <ol>
 * <li>TRACCIA_n, in locazione: il posto dell'indizio n compare sulla mappa (il primo nella città, gli altri nel posto
 * dell'indizio prima);</li>
 * <li>INDIZIO_n, in locazione, nel posto: l'indizio;</li>
 * <li>ACCUSA, in locazione, nel posto dell'ultimo indizio: la scelta del colpevole;</li>
 * <li>NASCONDIGLIO, in locazione: se è il colpevole giusto, il suo nascondiglio compare sulla mappa;</li>
 * <li>CATTURA, a fine locazione, nel nascondiglio: il colpevole e i suoi sono stati sconfitti;</li>
 * <li>ERRORE, in locazione: se è un innocente, la missione fallisce.</li>
 * </ol>
 */
public class LIndagine extends IncaricoInCitta {

	public static final String INDAGINE = "INDAGINE";
	public static final String ACCUSA = "ACCUSA";
	private static final String CAPO = "CAPO";
	private static final String TRACCIA = "TRACCIA_";
	private static final String INDIZIO = "INDIZIO_";
	private static final String NASCONDIGLIO = "NASCONDIGLIO";
	private static final String CATTURA = "CATTURA";
	private static final String ERRORE = "ERRORE";

	public LIndagine() {
		super(ClasseMissione.L_INDAGINE);
	}

	public IndagineRichiesta getIndagine() {
		return IndagineRichiesta.da(parametro(INDAGINE, () -> ProduttoreDiTestiCasuale.rigaDiMissioni(INDAGINE)));
	}

	/**
	 * Il nome del capo dei nemici nel nascondiglio, se l'indagine ne ha uno (da missioni.txt), altrimenti vuoto.
	 */
	public String getCapo() {
		IndagineRichiesta indagine = getIndagine();
		return indagine.isConCapo() ? parametro(CAPO, indagine::pescaNomeDelCapo) : "";
	}

	/**
	 * Chi va sconfitto nel nascondiglio, con il suo capo se c'è.
	 */
	public IncontroDiMissione getNemici() {
		IndagineRichiesta indagine = getIndagine();
		IncontroDiMissione nemici = IncontroDiMissione.di(indagine.getNemico(), indagine.getNumero());
		if (indagine.isFinoAllaResa()) {
			nemici.finoAllaResa();
		}
		if (indagine.isConCapo()) {
			nemici.conCapo(getCapo());
		}
		return indagine.getOndate().aggiungiA(nemici, this::getCapoDellOndata, this::testo);
	}

	/**
	 * Il nome del capo di quell'ondata (2, 3), pescato una volta sola.
	 */
	private String getCapoDellOndata(int ondata) {
		return parametro(CAPO + "_" + ondata, () -> getIndagine().getOndate().pescaNomeDelCapo(ondata));
	}

	/**
	 * Il posto dell'indizio da cercare, o il nascondiglio del colpevole; null finché la missione non l'ha trovato.
	 */
	public CoordinateMD getPosto() {
		return RegistroMissioni.getLocazioneOccupata(this);
	}

	/**
	 * Quanti indizi il gruppo ha già trovato: nessuno all'incarico, tutti dall'accusa in poi.
	 */
	public int getIndiziTrovati() {
		String passo = getPassoCorrente();
		if (passo.startsWith(TRACCIA)) {
			return Integer.parseInt(passo.substring(TRACCIA.length())) - 1;
		}
		if (passo.startsWith(INDIZIO)) {
			return Integer.parseInt(passo.substring(INDIZIO.length())) - 1;
		}
		return isAncoraAllIncarico() ? 0 : getIndagine().getIndizi().size();
	}

	private boolean isAncoraAllIncarico() {
		return getParametro(INDAGINE) == null || !isAttiva();
	}

	/**
	 * Il testo della grammatica, con il nome del capo al posto di %CAPO%.
	 */
	private String testo(String testo) {
		String conICapi = testo.replace(IndagineRichiesta.CAPO, getCapo());
		for (int ondata : getIndagine().getOndate().getOndateConCapo()) {
			conICapi = conICapi.replace(OndateDellaRiga.capo(ondata), getCapoDellOndata(ondata));
		}
		return conICapi;
	}

	private String getMandanteDiCitta() {
		return getIndagine().getMandante() + " di " + getNomeCitta();
	}

	private String getAttesa() {
		return Misc.inizialeMaiuscola(getIndagine().getMandante()) + " aspetta a " + getNomeCitta() + ".";
	}

	private boolean nelPosto() {
		return getPosto() != null && getPosto().equals(GruppoGiocatore.getIstanza().getCoordinate());
	}

	@Override
	protected void allIncarico() {
		getIndagine();
		getCapo();
		getIndagine().getOndate().getOndateConCapo().forEach(this::getCapoDellOndata);
	}

	@Override
	public String getNome() {
		return testo(getIndagine().getTitolo());
	}

	@Override
	public String getDescrizione() {
		IndagineRichiesta indagine = getIndagine();
		String passo = getPassoCorrente();
		if (RITORNO.equals(passo)) {
			return "Fatto: torna " + Misc.conPreposizione("da", indagine.getMandante()) + " a " + getNomeCitta() + " a riscuotere.";
		}
		if (NASCONDIGLIO.equals(passo) || CATTURA.equals(passo)) {
			return "Hai scoperto il colpevole: sconfiggilo nel suo nascondiglio, segnato sulla mappa, poi torna "
					+ Misc.conPreposizione("da", indagine.getMandante()) + " a " + getNomeCitta() + ".";
		}
		StringBuilder descrizione = new StringBuilder();
		int trovati = getIndiziTrovati();
		int indizi = indagine.getIndizi().size();
		if (trovati < indizi) {
			descrizione.append(Misc.inizialeMaiuscola(getMandanteDiCitta())).append(" ti ha chiesto di indagare: cerca ")
					.append(trovati == 0 ? "il primo indizio" : "l'indizio successivo").append(" nel posto segnato sulla mappa.");
		} else {
			descrizione.append("Hai trovato tutti gli indizi. ").append(testo(indagine.getDomanda()));
		}
		List<Indizio> elenco = indagine.getIndizi();
		for (int i = 0; i < trovati; i++) {
			descrizione.append(" Indizio ").append(i + 1).append(": ").append(testo(elenco.get(i).getTesto()));
		}
		return descrizione.toString();
	}

	@Override
	public String getRicordoDellaLocazione() {
		return isFallita() ? null : testo(getIndagine().getRicordo());
	}

	@Override
	protected String primoPassoDelCompito() {
		return TRACCIA + 1;
	}

	@Override
	protected Passo costruisciPassoDelCompito(String id) {
		IndagineRichiesta indagine = getIndagine();
		List<Indizio> indizi = indagine.getIndizi();
		if (id.startsWith(TRACCIA)) {
			int numero = Integer.parseInt(id.substring(TRACCIA.length()));
			Indizio indizio = indizi.get(numero - 1);
			return cercaLocazione(MomentoControllo.IN_LOCAZIONE, indizio.getLuogo())
					.esegui(() -> {
						Foresta.setLocazioneConosciuta(getPosto());
						BusEventi.pubblica(new NotificaTestoParagrafo((numero == 1 ? "Il primo indizio va cercato " : "Il prossimo indizio va cercato ")
								+ TestiDeiLuoghi.dentro(indizio.getLuogo()) + ": il posto è segnato sulla mappa."));
					})
					.poi(INDIZIO + numero);
		}
		if (id.startsWith(INDIZIO)) {
			int numero = Integer.parseInt(id.substring(INDIZIO.length()));
			return Passo.quando(MomentoControllo.IN_LOCAZIONE, this::nelPosto)
					.esegui(() -> BusEventi.pubblica(new NotificaTestoParagrafo(testo(indizi.get(numero - 1).getTesto()))))
					.poi(numero < indizi.size() ? TRACCIA + (numero + 1) : ACCUSA);
		}
		switch (id) {
			case ACCUSA:
				return Passo.quando(MomentoControllo.IN_LOCAZIONE, () -> true)
						.chiediScelta(testo(indagine.getDomanda()), indagine.getSospetti())
						.poi(() -> String.valueOf(indagine.getColpevole()).equals(getRisposta(ACCUSA)) ? NASCONDIGLIO : ERRORE);
			case NASCONDIGLIO:
				return cercaLocazione(MomentoControllo.IN_LOCAZIONE, indagine.getLuogo())
						.esegui(() -> {
							Foresta.setLocazioneConosciuta(getPosto());
							BusEventi.pubblica(new NotificaTestoParagrafo(testo(indagine.getSmascheramento())
									+ " Il nascondiglio è segnato sulla mappa."));
						})
						.poi(CATTURA);
			case CATTURA:
				return combatti(this::getPosto, getNemici())
						.esegui(() -> BusEventi.pubblica(new NotificaTestoParagrafo(testo(indagine.getVittoria()) + " " + getAttesa())))
						.poi(RITORNO);
			case ERRORE:
				return Passo.quando(MomentoControllo.IN_LOCAZIONE, () -> true)
						.esegui(() -> {
							BusEventi.pubblica(new NotificaTestoParagrafo(testo(indagine.getErrore())));
							fallisciMissione();
						})
						.poi(Passo.FINE);
			default:
				throw new IllegalArgumentException("Passo sconosciuto per " + getNome() + ": " + id);
		}
	}

	@Override
	protected ScenaInCitta scenaIncarico() {
		IndagineRichiesta indagine = getIndagine();
		return indagine.getAspetto().nuovaScena()
				.parlaIlMandante(testo(indagine.getRichiesta()))
				.parlaIlMandante("Vi segno sulla mappa dove cominciare a cercare. Trovate il colpevole e avrete " + getRicompensa()
						+ " monete. Ma attenzione a non accusare un innocente.")
				.parlaIlCapo(testo(indagine.getBattutaDelCapo()))
				.parlaIlMandante(testo(indagine.getRisposta()));
	}

	@Override
	protected ScenaInCitta scenaRingraziamento() {
		return getIndagine().getAspetto().nuovaScena()
				.parlaIlMandante(testo(getIndagine().getRingraziamento()))
				.parlaIlMandante("Ecco le " + getRicompensa() + " monete promesse.");
	}

	@Override
	protected String testoAccettazione() {
		return Misc.inizialeMaiuscola(getMandanteDiCitta()) + " pagherà " + getRicompensa() + " monete per scoprire il colpevole.";
	}

	@Override
	protected String testoRicompensa() {
		return Misc.inizialeMaiuscola(getIndagine().getMandante()) + " paga le " + getRicompensa() + " monete promesse.";
	}

	@Override
	protected int getRicompensa() {
		return getIndagine().getMonete();
	}
}
