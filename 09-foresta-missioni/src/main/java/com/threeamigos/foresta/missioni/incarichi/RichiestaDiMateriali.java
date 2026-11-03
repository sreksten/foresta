package com.threeamigos.foresta.missioni.incarichi;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.intermezzi.ScenaInCitta;
import com.threeamigos.foresta.tipi.Mandante;
import com.threeamigos.foresta.missioni.OggettiDaRaccogliere;
import com.threeamigos.foresta.missioni.Passo;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.missioni.richieste.MaterialeRichiesto;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.testi.ProduttoreDiMissioniCasuale;
import com.threeamigos.foresta.tipi.MissionePredefinita;
import com.threeamigos.foresta.testi.Lingue;
import com.threeamigos.foresta.testi.Preposizione;
import com.threeamigos.foresta.testi.RegoleDellaLingua;

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

	public RichiestaDiMateriali(MissionePredefinita classe, Mandante mandante) {
		super(classe);
		this.mandante = mandante;
	}

	/**
	 * La richiesta dell'alchimista.
	 */
	public static RichiestaDiMateriali dellAlchimista() {
		return new RichiestaDiMateriali(MissionePredefinita.RICHIESTA_ALCHIMISTA, Mandante.ALCHIMISTA);
	}

	/**
	 * La richiesta dell'armaiolo.
	 */
	public static RichiestaDiMateriali dellArmaiolo() {
		return new RichiestaDiMateriali(MissionePredefinita.RICHIESTA_ARMAIOLO, Mandante.ARMAIOLO);
	}

	/**
	 * La richiesta del capitano delle guardie.
	 */
	public static RichiestaDiMateriali delCapitano() {
		return new RichiestaDiMateriali(MissionePredefinita.RICHIESTA_CAPITANO, Mandante.CAPITANO);
	}

	/**
	 * La richiesta del locandiere.
	 */
	public static RichiestaDiMateriali delLocandiere() {
		return new RichiestaDiMateriali(MissionePredefinita.RICHIESTA_LOCANDIERE, Mandante.LOCANDIERE);
	}

	public Mandante getMandante() {
		return mandante;
	}

	public MaterialeRichiesto getMateriale() {
		return MaterialeRichiesto.da(parametro(MATERIALE, () -> ProduttoreDiMissioniCasuale.materialeRichiesto(mandante.getProduzione())));
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
	protected String creaIlNome() {
		return Lingue.corrente().elenco(java.util.Arrays.asList(Lingue.corrente().inizialeMaiuscola(mandante.getNome()), getMateriale().getPluraleConArticolo()));
	}

	@Override
	public String getDescrizione() {
		return descrizioneDalModello();
	}

	@Override
	protected String produzioneDellaDescrizione(String passo) {
		return RITORNO.equals(passo) ? "RICHIESTA_DI_MATERIALI_DESCRIZIONE_RITORNO" : "RICHIESTA_DI_MATERIALI_DESCRIZIONE_RACCOLTA";
	}

	@Override
	protected Object[] valoriStabiliDellaDescrizione() {
		return valori();
	}

	@Override
	protected Object[] valoriDellaDescrizione() {
		return new Object[] {"PARAM_RACCOLTI", getContatore(MATERIALE)};
	}

	/**
	 * Tutti i segnaposto dei testi della missione: i valori del materiale e le forme del mandante (con la città o senza,
	 * con la virgola finale o senza, con la preposizione, con l'iniziale maiuscola).
	 */
	private Object[] valori() {
		MaterialeRichiesto materiale = getMateriale();
		RegoleDellaLingua lingua = Lingue.corrente();
		String nome = mandante.getNome();
		return new Object[] {
				"PARAM_MONETE", getRicompensa(),
				"PARAM_QUANTI", materiale.quanti(getQuantita()),
				"PARAM_TUTTI", materiale.getTutti(),
				"PARAM_PRONOME", materiale.getPronome(),
				"PARAM_MATERIALE", materiale.getPluraleConArticolo(),
				"PARAM_MATERIALE_MAIUSC", lingua.inizialeMaiuscola(materiale.getPluraleConArticolo()),
				"PARAM_PLURALE", materiale.getPlurale(),
				"PARAM_POSSESSIVO", materiale.getPossessivo(),
				"PARAM_DA_DOVE_VIENE", materiale.getDaDoveViene(),
				"PARAM_DA_DOVE_VIENE_FRASE_MAIUSC", lingua.inizialeMaiuscola(materiale.getDaDoveVieneComeFrase()),
				"PARAM_RISPOSTA_DEL_MANDANTE", materiale.getRispostaDelMandante(),
				"PARAM_ADP_QUANTI_MAIUSC", lingua.inizialeMaiuscola(materiale.getNome().getADP() + materiale.quanti(getQuantita())),
				"PARAM_MANDANTE_MAIUSC", lingua.inizialeMaiuscola(mandante(nome)),
				"PARAM_MANDANTE_DI_CITTA", mandanteDiCitta(nome),
				"PARAM_MANDANTE_DI_CITTA_MAIUSC", lingua.inizialeMaiuscola(mandanteDiCitta(nome)),
				"PARAM_A_MANDANTE_SENZA_VIRGOLA", lingua.conPreposizione(Preposizione.A, mandanteSenzaVirgolaFinale(nome)),
				"PARAM_A_MANDANTE_DI_CITTA_SENZA_VIRGOLA", lingua.conPreposizione(Preposizione.A, mandanteDiCittaSenzaVirgolaFinale(nome)),
				"PARAM_ALLA_CONSEGNA", mandante.allaConsegna(materiale.getPronome())};
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
				.esegui(() -> BusEventi.pubblica(new NotificaTestoParagrafo(ProduttoreDiMissioniCasuale.testoDellaMissione("RICHIESTA_DI_MATERIALI_PRONTI", valori()))))
				.poi(RITORNO);
	}

	@Override
	protected OggettiDaRaccogliere getOggettiDaConsegnare() {
		return getMateriali();
	}

	@Override
	protected String testoConsegna() {
		return ProduttoreDiMissioniCasuale.testoDellaMissione("RICHIESTA_DI_MATERIALI_CONSEGNA", valori());
	}

	@Override
	protected ScenaInCitta scenaIncarico() {
		MaterialeRichiesto materiale = getMateriale();
		Object[] valori = valori();
		return mandante.nuovaScena()
				.parlaIlMandante(mandante.getApertura(nomeDelMandante(mandante.getNome())))
				.parlaIlMandante(ProduttoreDiMissioniCasuale.testoDellaMissione("RICHIESTA_DI_MATERIALI_INCARICO_1", valori))
				.parlaIlCapo(materiale.getBattutaDelCapo())
				.parlaIlMandante(ProduttoreDiMissioniCasuale.testoDellaMissione("RICHIESTA_DI_MATERIALI_INCARICO_2", valori))
				.parlaIlCapo(ProduttoreDiMissioniCasuale.testoDellaMissione("RICHIESTA_DI_MATERIALI_INCARICO_3", valori));
	}

	@Override
	protected ScenaInCitta scenaRingraziamento() {
		Object[] valori = valori();
		return mandante.nuovaScena()
				.parlaIlMandante(ProduttoreDiMissioniCasuale.testoDellaMissione("RICHIESTA_DI_MATERIALI_RINGRAZIAMENTO_1", valori))
				.parlaIlCapo(ProduttoreDiMissioniCasuale.testoDellaMissione("RICHIESTA_DI_MATERIALI_RINGRAZIAMENTO_2", valori));
	}

	@Override
	protected String testoAccettazione() {
		return ProduttoreDiMissioniCasuale.testoDellaMissione("RICHIESTA_DI_MATERIALI_ACCETTAZIONE", valori());
	}

	@Override
	protected String testoRicompensa() {
		return ProduttoreDiMissioniCasuale.testoDellaMissione("RICHIESTA_DI_MATERIALI_RICOMPENSA", valori());
	}

	/**
	 * Il prezzo per pezzo del materiale, più cinque.
	 */
	@Override
	protected int getRicompensaBase() {
		return getMateriale().getPrezzo() * getQuantita() + 5;
	}
}
