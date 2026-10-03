package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.intermezzi.ScenaInCitta;
import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.motore.Dado;
import com.threeamigos.foresta.motore.ProduttoreDiTestiCasuale;
import com.threeamigos.foresta.tools.Misc;

import java.util.List;

/**
 * In città l'alchimista chiede degli ingredienti (radici di mandragola, bacche di belladonna, funghi lunari...): si
 * trovano nelle locazioni che l'ingrediente dice, dove la missione li semina (vedi {@link OggettiDaRaccogliere}).
 * Raccolti tutti, si torna da lui a consegnarli e a riscuotere (vedi IncaricoInCitta). L'ingrediente e la quantità
 * si pescano quando l'incarico si offre (missioni.txt, vedi {@link IngredienteAlchemico}): la missione si ripete
 * con altri ingredienti.
 */
public class LAlchimista extends IncaricoInCitta {

	/**
	 * La chiave sotto cui la missione conta gli ingredienti raccolti.
	 */
	public static final String INGREDIENTE = "INGREDIENTE";
	private static final String QUANTITA = "QUANTITA";
	public static final int QUANTITA_MINIMA = 3;
	public static final int QUANTITA_MASSIMA = 5;
	private static final String RACCOLTA = "RACCOLTA";

	public LAlchimista() {
		super(ClasseMissione.L_ALCHIMISTA);
	}

	public IngredienteAlchemico getIngrediente() {
		return IngredienteAlchemico.da(parametro(INGREDIENTE, ProduttoreDiTestiCasuale::ingredienteAlchemico));
	}

	/**
	 * Quanti ingredienti chiede l'alchimista.
	 */
	public int getQuantita() {
		return Integer.parseInt(parametro(QUANTITA, () -> String.valueOf(Dado.tira(QUANTITA_MINIMA, QUANTITA_MASSIMA))));
	}

	/**
	 * Gli ingredienti da raccogliere: dove si trovano, quanti, quanto spesso.
	 */
	public OggettiDaRaccogliere getIngredienti() {
		IngredienteAlchemico ingrediente = getIngrediente();
		List<ClassiLocazione> luoghi = ingrediente.getLuoghi();
		return OggettiDaRaccogliere.di(INGREDIENTE, ingrediente.getNome(), getQuantita())
				.in(luoghi.get(0), luoghi.subList(1, luoghi.size()).toArray(new ClassiLocazione[0]))
				.conProbabilita(35)
				.alPiuPerLocazione(2);
	}

	@Override
	protected void allIncarico() {
		getIngrediente();
		getQuantita();
	}

	@Override
	public String getNome() {
		return "L'alchimista e " + getIngrediente().getPluraleConArticolo();
	}

	@Override
	public String getDescrizione() {
		IngredienteAlchemico ingrediente = getIngrediente();
		if (RITORNO.equals(getPassoCorrente())) {
			return "Hai " + ingrediente.getTutti() + " " + ingrediente.getPluraleConArticolo() + ": porta" + ingrediente.getPronome()
					+ " all'alchimista di " + getNomeCitta() + ".";
		}
		return "L'alchimista di " + getNomeCitta() + " ti ha chiesto " + ingrediente.quanti(getQuantita()) + ", che si trovano "
				+ ingrediente.getDoveSiTrova() + ". Finora: " + getContatore(INGREDIENTE) + ".";
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
		return raccogli(MomentoControllo.POST_LOCAZIONE, getIngredienti())
				.esegui(() -> BusEventi.pubblica(new NotificaTestoParagrafo(Misc.inizialeMaiuscola(getIngrediente().getPluraleConArticolo())
						+ " ci sono " + getIngrediente().getTutti() + ": l'alchimista di " + getNomeCitta() + " " + getIngrediente().getPronome() + " aspetta.")))
				.poi(RITORNO);
	}

	@Override
	protected OggettiDaRaccogliere getOggettiDaConsegnare() {
		return getIngredienti();
	}

	@Override
	protected String testoConsegna() {
		IngredienteAlchemico ingrediente = getIngrediente();
		return Misc.inizialeMaiuscola((ingrediente.isFemminile() ? Misc.LE : Misc.I) + ingrediente.quanti(getQuantita()))
				+ " passano all'alchimista, che " + ingrediente.getPronome() + " annusa soddisfatto.";
	}

	@Override
	protected ScenaInCitta scenaIncarico() {
		IngredienteAlchemico ingrediente = getIngrediente();
		return ScenaInCitta.conAlchimista()
				.parlaIlMandante("Buongiorno, viaggiatori. Non è che andate in giro per la foresta?")
				.parlaIlMandante("Mi servono " + ingrediente.quanti(getQuantita()) + " per un unguento. Si trovano "
						+ ingrediente.getDoveSiTrova() + ", e io lì fuori non ci metto piede.")
				.parlaIlCapo(ingrediente.getBattutaDelCapo())
				.parlaIlMandante(ingrediente.getRispostaDellAlchimista() + " Ve " + ingrediente.getPronome() + " pago "
						+ getRicompensa() + " monete.")
				.parlaIlCapo("Affare fatto.");
	}

	@Override
	protected ScenaInCitta scenaRingraziamento() {
		IngredienteAlchemico ingrediente = getIngrediente();
		return ScenaInCitta.conAlchimista()
				.parlaIlMandante(ingrediente.getPossessivo() + " " + ingrediente.getPlurale() + "! E sono pure di prima scelta.")
				.parlaIlCapo("La prossima volta, nella foresta, ci venite voi.");
	}

	@Override
	protected String testoAccettazione() {
		IngredienteAlchemico ingrediente = getIngrediente();
		return "L'alchimista pagherà " + getRicompensa() + " monete per " + ingrediente.quanti(getQuantita())
				+ ", che si trovano " + ingrediente.getDoveSiTrova() + ".";
	}

	@Override
	protected String testoRicompensa() {
		return "L'alchimista paga le " + getRicompensa() + " monete promesse.";
	}

	/**
	 * Cinque monete per ingrediente, più cinque.
	 */
	@Override
	protected int getRicompensa() {
		return 5 * getQuantita() + 5;
	}
}
