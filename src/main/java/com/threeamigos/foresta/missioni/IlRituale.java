package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.intermezzi.ScenaInCitta;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.motore.Foresta;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.ProduttoreDiTestiCasuale;
import com.threeamigos.foresta.motore.RegistroMissioni;
import com.threeamigos.foresta.tipi.ClasseMissione;
import com.threeamigos.foresta.strumenti.Misc;

import java.util.ArrayList;
import java.util.List;

/**
 * In città qualcuno chiede di celebrare un rito in un posto della foresta: sigillare un varco, benedire una fonte,
 * liberare un posseduto, parlare con uno spirito (vedi {@link RitualeRichiesto}, da missioni.txt). Il posto si segna
 * sulla mappa; prima bisogna raccogliere gli ingredienti (vedi {@link MissioneAPassi#raccogli}). Arrivati nel posto
 * con tutti gli ingredienti, il giocatore decide se cominciare il rito o, per certi riti, sceglie come celebrarlo
 * (vedi {@link Passo#chiediScelta}): con il metodo sbagliato la missione fallisce; "non ancora" rimanda il rito alla
 * prossima volta che si torna lì. A volte il rito richiama qualcuno, da sconfiggere lì; poi si torna a riscuotere
 * (vedi IncaricoInCitta). Il rito si pesca quando si offre: la missione si ripete con altri.
 * <ol>
 * <li>LUOGO, in locazione, nella città: il posto del rito compare sulla mappa;</li>
 * <li>RACCOLTA, a fine locazione: ci sono tutti gli ingredienti;</li>
 * <li>RITO, a inizio locazione, nel posto: la domanda; il posto è sicuro, niente avversari né oggetti a caso
 * all'arrivo (vedi RegistroMissioni.sopprimiContenutoLocazione) finché non arriva il guardiano, se c'è;</li>
 * <li>RINVIO, a inizio locazione, altrove: dopo un "non ancora", per tornare a chiedere;</li>
 * <li>GUARDIANO, a fine locazione, nel posto: chi è saltato fuori è stato sconfitto;</li>
 * <li>ERRORE, a inizio locazione: con il metodo sbagliato, la missione fallisce.</li>
 * </ol>
 * La domanda si pone a inizio locazione perché chi salta fuori ci sia già in questa visita.
 */
public class IlRituale extends IncaricoInCitta {

	public static final String RITUALE = "RITUALE";
	public static final String RITO = "RITO";
	/**
	 * La chiave sotto cui la missione conta gli ingredienti raccolti.
	 */
	public static final String INGREDIENTE = "INGREDIENTE";
	private static final String CAPO = "CAPO";
	private static final String LUOGO = "LUOGO";
	private static final String RACCOLTA = "RACCOLTA";
	private static final String RINVIO = "RINVIO";
	private static final String GUARDIANO = "GUARDIANO";
	private static final String ERRORE = "ERRORE";
	private static final String NON_ANCORA = "Non ancora";

	public IlRituale() {
		super(ClasseMissione.IL_RITUALE);
	}

	public RitualeRichiesto getRituale() {
		return RitualeRichiesto.da(parametro(RITUALE, () -> ProduttoreDiTestiCasuale.rigaDiMissioni(RITUALE)));
	}

	/**
	 * Il nome del capo di chi salta fuori, se il rito ne ha uno (da missioni.txt), altrimenti vuoto.
	 */
	public String getCapo() {
		RitualeRichiesto rituale = getRituale();
		return rituale.isConCapo() ? parametro(CAPO, rituale::pescaNomeDelCapo) : "";
	}

	/**
	 * Chi salta fuori durante il rito, con il suo capo se c'è; null se nessuno.
	 */
	public IncontroDiMissione getNemici() {
		RitualeRichiesto rituale = getRituale();
		if (!rituale.isConNemici()) {
			return null;
		}
		IncontroDiMissione nemici = IncontroDiMissione.di(rituale.getNemico(), rituale.getNumero());
		return rituale.isConCapo() ? nemici.conCapo(getCapo()) : nemici;
	}

	/**
	 * Gli ingredienti da raccogliere: dove si trovano o a chi si prendono, e quanti.
	 */
	public OggettiDaRaccogliere getIngredienti() {
		RitualeRichiesto rituale = getRituale();
		return rituale.getIngrediente().daRaccogliere(INGREDIENTE, rituale.getQuantita());
	}

	/**
	 * Il posto del rito, o null finché la missione non l'ha trovato.
	 */
	public CoordinateMD getPosto() {
		return RegistroMissioni.getLocazioneOccupata(this);
	}

	private boolean nelPosto() {
		return getPosto() != null && getPosto().equals(GruppoGiocatore.getIstanza().getCoordinate());
	}

	/**
	 * Il testo della grammatica, con il nome del capo al posto di %CAPO%.
	 */
	private String testo(String testo) {
		return testo.replace(RitualeRichiesto.CAPO, getCapo());
	}

	private String getMandanteDiCitta() {
		return getRituale().getMandante() + " di " + getNomeCitta();
	}

	private String getAttesa() {
		return Misc.inizialeMaiuscola(getRituale().getMandante()) + " aspetta a " + getNomeCitta() + ".";
	}

	/**
	 * Le risposte possibili alla domanda del rito: i metodi, e per ultimo "non ancora".
	 */
	private List<String> getOpzioni() {
		List<String> opzioni = new ArrayList<>(getRituale().getMetodi());
		opzioni.add(NON_ANCORA);
		return opzioni;
	}

	private boolean isRinviato() {
		String risposta = getRisposta(RITO);
		RitualeRichiesto rituale = getRituale();
		return rituale.isConMetodi() ? String.valueOf(rituale.getMetodi().size() + 1).equals(risposta) : Passo.NO.equals(risposta);
	}

	private boolean isRiuscito() {
		String risposta = getRisposta(RITO);
		RitualeRichiesto rituale = getRituale();
		return rituale.isConMetodi() ? String.valueOf(rituale.getMetodo()).equals(risposta) : Passo.SI.equals(risposta);
	}

	@Override
	protected void allIncarico() {
		getRituale();
		getCapo();
	}

	@Override
	public String getNome() {
		return testo(getRituale().getTitolo());
	}

	@Override
	public String getDescrizione() {
		RitualeRichiesto rituale = getRituale();
		MaterialeRichiesto ingrediente = rituale.getIngrediente();
		String passo = getPassoCorrente();
		if (RITORNO.equals(passo)) {
			return "Fatto: torna " + Misc.conPreposizione("da", rituale.getMandante()) + " a " + getNomeCitta() + " a riscuotere.";
		}
		if (GUARDIANO.equals(passo)) {
			return "Il rito è cominciato: sconfiggi chi è venuto a disturbarlo, nel posto segnato sulla mappa.";
		}
		if (RITO.equals(passo) || RINVIO.equals(passo)) {
			return "Hai " + ingrediente.getTutti() + " " + ingrediente.getPluraleConArticolo()
					+ ": vai nel posto segnato sulla mappa per celebrare il rito.";
		}
		return Misc.inizialeMaiuscola(getMandanteDiCitta()) + " ti ha chiesto di celebrare un rito nel posto segnato sulla mappa. "
				+ "Prima servono " + ingrediente.quanti(rituale.getQuantita()) + ", che " + ingrediente.getDaDoveViene()
				+ ". Finora: " + getContatore(INGREDIENTE) + ".";
	}

	@Override
	public String getRicordoDellaLocazione() {
		return isFallita() ? null : testo(getRituale().getRicordo());
	}

	@Override
	protected String primoPassoDelCompito() {
		return LUOGO;
	}

	@Override
	protected Passo costruisciPassoDelCompito(String id) {
		RitualeRichiesto rituale = getRituale();
		MaterialeRichiesto ingrediente = rituale.getIngrediente();
		switch (id) {
			case LUOGO:
				return cercaLocazione(MomentoControllo.IN_LOCAZIONE, rituale.getLuogo())
						.esegui(() -> {
							Foresta.setLocazioneConosciuta(getPosto());
							BusEventi.pubblica(new NotificaTestoParagrafo("Il posto del rito è segnato sulla mappa."));
						})
						.poi(RACCOLTA);
			case RACCOLTA:
				return raccogli(MomentoControllo.POST_LOCAZIONE, getIngredienti())
						.esegui(() -> BusEventi.pubblica(new NotificaTestoParagrafo(Misc.inizialeMaiuscola(ingrediente.getPluraleConArticolo())
								+ " ci sono " + ingrediente.getTutti() + ": adesso il rito si può celebrare, nel posto segnato sulla mappa.")))
						.poi(RITO);
			case RITO:
				// Non la soppressione se ci sono nemici: altrimenti, risolvendosi il rito e incatenandosi al passo
				// GUARDIANO nello stesso controllo in cui arriva la risposta, soppresse anche i nemici appena arrivati
				Passo rito = Passo.quando(MomentoControllo.PRE_LOCAZIONE, this::nelPosto)
						.aOgniControllo(() -> {
							if (nelPosto() && !rituale.isConNemici()) {
								RegistroMissioni.sopprimiContenutoLocazione(getPosto());
							}
						});
				rito = rituale.isConMetodi() ? rito.chiediScelta(testo(rituale.getDomanda()), getOpzioni())
						: rito.chiediConferma(testo(rituale.getDomanda()));
				return rito
						.esegui(() -> {
							if (isRinviato()) {
								BusEventi.pubblica(new NotificaTestoParagrafo("Il rito può aspettare: si farà un'altra volta, tornando qui."));
							} else if (isRiuscito()) {
								BusEventi.pubblica(new NotificaTestoParagrafo(testo(rituale.getRito())
										+ (rituale.isConNemici() ? "" : " " + getAttesa())));
							}
						})
						.poi(() -> isRinviato() ? RINVIO : !isRiuscito() ? ERRORE : rituale.isConNemici() ? GUARDIANO : RITORNO);
			case RINVIO:
				return Passo.quando(MomentoControllo.PRE_LOCAZIONE, () -> !nelPosto())
						.esegui(() -> dimenticaRisposta(RITO))
						.poi(RITO);
			case GUARDIANO:
				return combatti(this::getPosto, getNemici())
						.esegui(() -> BusEventi.pubblica(new NotificaTestoParagrafo(testo(rituale.getVittoria()) + " " + getAttesa())))
						.poi(RITORNO);
			case ERRORE:
				return Passo.quando(MomentoControllo.PRE_LOCAZIONE, () -> true)
						.esegui(() -> {
							BusEventi.pubblica(new NotificaTestoParagrafo(testo(rituale.getErrore())));
							fallisciMissione();
						})
						.poi(Passo.FINE);
			default:
				throw new IllegalArgumentException("Passo sconosciuto per " + getNome() + ": " + id);
		}
	}

	@Override
	protected ScenaInCitta scenaIncarico() {
		RitualeRichiesto rituale = getRituale();
		MaterialeRichiesto ingrediente = rituale.getIngrediente();
		return rituale.getAspetto().nuovaScena()
				.parlaIlMandante(testo(rituale.getRichiesta()))
				.parlaIlMandante("Il rito si celebra " + TestiDeiLuoghi.dentro(rituale.getLuogo()) + " qui vicino: ve lo segno sulla mappa. "
						+ "Servono " + ingrediente.quanti(rituale.getQuantita()) + ", che " + ingrediente.getDaDoveViene() + ". "
						+ getRicompensa() + " monete a rito compiuto.")
				.parlaIlCapo(testo(rituale.getBattutaDelCapo()))
				.parlaIlMandante(testo(rituale.getRisposta()));
	}

	@Override
	protected ScenaInCitta scenaRingraziamento() {
		return getRituale().getAspetto().nuovaScena()
				.parlaIlMandante(testo(getRituale().getRingraziamento()))
				.parlaIlMandante("Ecco le " + getRicompensa() + " monete promesse.");
	}

	@Override
	protected String testoAccettazione() {
		return Misc.inizialeMaiuscola(getMandanteDiCitta()) + " pagherà " + getRicompensa() + " monete per un rito "
				+ TestiDeiLuoghi.dentro(getRituale().getLuogo()) + ".";
	}

	@Override
	protected String testoRicompensa() {
		return Misc.inizialeMaiuscola(getRituale().getMandante()) + " paga le " + getRicompensa() + " monete promesse.";
	}

	@Override
	protected int getRicompensaBase() {
		return getRituale().getMonete();
	}
}
