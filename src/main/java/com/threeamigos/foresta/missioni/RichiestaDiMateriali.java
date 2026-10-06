package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.intermezzi.ScenaInCitta;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.motore.Dado;
import com.threeamigos.foresta.motore.ProduttoreDiTestiCasuale;
import com.threeamigos.foresta.tipi.ClasseMissione;
import com.threeamigos.foresta.strumenti.Misc;

/**
 * Un mandante in città (l'alchimista, l'armaiolo, il capitano delle guardie, il locandiere: vedi {@link Mandante})
 * chiede dei materiali: erbe, minerali e pesci da raccogliere in certi luoghi, o parti di mostri da prendere sconfiggendoli (vedi
 * {@link OggettiDaRaccogliere}). Raccolti tutti, si torna da lui a consegnarli e a riscuotere: tanto per pezzo,
 * più cinque. Il materiale e la quantità si pescano da missioni.txt quando l'incarico si offre (vedi
 * {@link MaterialeRichiesto}): la missione si ripete con altri materiali.
 */
public class RichiestaDiMateriali extends IncaricoInCitta {

	/**
	 * La chiave sotto cui la missione conta i materiali raccolti.
	 */
	public static final String MATERIALE = "MATERIALE";
	private static final String QUANTITA = "QUANTITA";
	private static final String RACCOLTA = "RACCOLTA";

	private final Mandante mandante;

	public RichiestaDiMateriali(ClasseMissione classe, Mandante mandante) {
		super(classe);
		this.mandante = mandante;
	}

	public static RichiestaDiMateriali dellAlchimista() {
		return new RichiestaDiMateriali(ClasseMissione.RICHIESTA_ALCHIMISTA, Mandante.ALCHIMISTA);
	}

	public static RichiestaDiMateriali dellArmaiolo() {
		return new RichiestaDiMateriali(ClasseMissione.RICHIESTA_ARMAIOLO, Mandante.ARMAIOLO);
	}

	public static RichiestaDiMateriali delCapitano() {
		return new RichiestaDiMateriali(ClasseMissione.RICHIESTA_CAPITANO, Mandante.CAPITANO);
	}

	public static RichiestaDiMateriali delLocandiere() {
		return new RichiestaDiMateriali(ClasseMissione.RICHIESTA_LOCANDIERE, Mandante.LOCANDIERE);
	}

	public Mandante getMandante() {
		return mandante;
	}

	public MaterialeRichiesto getMateriale() {
		return MaterialeRichiesto.da(parametro(MATERIALE, () -> ProduttoreDiTestiCasuale.materialeRichiesto(mandante.getProduzione())));
	}

	/**
	 * Quanti pezzi chiede il mandante, fra il minimo e il massimo del materiale.
	 */
	public int getQuantita() {
		MaterialeRichiesto materiale = getMateriale();
		return Integer.parseInt(parametro(QUANTITA,
				() -> String.valueOf(Dado.tira(materiale.getQuantitaMinima(), materiale.getQuantitaMassima()))));
	}

	/**
	 * I materiali da raccogliere: dove si trovano o a chi si prendono, e quanti.
	 */
	public OggettiDaRaccogliere getMateriali() {
		return getMateriale().daRaccogliere(MATERIALE, getQuantita());
	}

	@Override
	protected void allIncarico() {
		getMateriale();
		getQuantita();
	}

	@Override
	public String getNome() {
		return Misc.inizialeMaiuscola(mandante.getNome()) + " e " + getMateriale().getPluraleConArticolo();
	}

	@Override
	public String getDescrizione() {
		MaterialeRichiesto materiale = getMateriale();
		if (RITORNO.equals(getPassoCorrente())) {
			return "Hai " + materiale.getTutti() + " " + materiale.getPluraleConArticolo() + ": porta" + materiale.getPronome()
					+ " " + Misc.conPreposizione("a", mandante.getNome()) + " di " + getNomeCitta() + ".";
		}
		return Misc.inizialeMaiuscola(mandante.getNome()) + " di " + getNomeCitta() + " ti ha chiesto " + materiale.quanti(getQuantita())
				+ ", che " + materiale.getDaDoveViene() + ". Finora: " + getContatore(MATERIALE) + ".";
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
		MaterialeRichiesto materiale = getMateriale();
		return raccogli(MomentoControllo.POST_LOCAZIONE, getMateriali())
				.esegui(() -> BusEventi.pubblica(new NotificaTestoParagrafo(Misc.inizialeMaiuscola(materiale.getPluraleConArticolo())
						+ " ci sono " + materiale.getTutti() + ": " + mandante.getNome() + " di " + getNomeCitta() + " "
						+ materiale.getPronome() + " aspetta.")))
				.poi(RITORNO);
	}

	@Override
	protected OggettiDaRaccogliere getOggettiDaConsegnare() {
		return getMateriali();
	}

	@Override
	protected String testoConsegna() {
		MaterialeRichiesto materiale = getMateriale();
		return Misc.inizialeMaiuscola((materiale.isFemminile() ? Misc.LE : Misc.I) + materiale.quanti(getQuantita()))
				+ " passano " + Misc.conPreposizione("a", mandante.getNome()) + ", " + mandante.allaConsegna(materiale.getPronome()) + ".";
	}

	@Override
	protected ScenaInCitta scenaIncarico() {
		MaterialeRichiesto materiale = getMateriale();
		return mandante.nuovaScena()
				.parlaIlMandante(mandante.getApertura())
				.parlaIlMandante("Mi servono " + materiale.quanti(getQuantita()) + ". " + Misc.inizialeMaiuscola(materiale.getDaDoveViene()) + ".")
				.parlaIlCapo(materiale.getBattutaDelCapo())
				.parlaIlMandante(materiale.getRispostaDelMandante() + " Ve " + materiale.getPronome() + " pago "
						+ getRicompensa() + " monete.")
				.parlaIlCapo("Affare fatto.");
	}

	@Override
	protected ScenaInCitta scenaRingraziamento() {
		MaterialeRichiesto materiale = getMateriale();
		return mandante.nuovaScena()
				.parlaIlMandante(materiale.getPossessivo() + " " + materiale.getPlurale() + "! E sono pure di prima scelta.")
				.parlaIlCapo("La prossima volta, nella foresta, ci venite voi.");
	}

	@Override
	protected String testoAccettazione() {
		MaterialeRichiesto materiale = getMateriale();
		return Misc.inizialeMaiuscola(mandante.getNome()) + " pagherà " + getRicompensa() + " monete per "
				+ materiale.quanti(getQuantita()) + ", che " + materiale.getDaDoveViene() + ".";
	}

	@Override
	protected String testoRicompensa() {
		return Misc.inizialeMaiuscola(mandante.getNome()) + " paga le " + getRicompensa() + " monete promesse.";
	}

	/**
	 * Il prezzo per pezzo del materiale, più cinque.
	 */
	@Override
	protected int getRicompensaBase() {
		return getMateriale().getPrezzo() * getQuantita() + 5;
	}
}
