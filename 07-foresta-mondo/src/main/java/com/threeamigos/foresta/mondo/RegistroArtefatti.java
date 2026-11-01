package com.threeamigos.foresta.mondo;

import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.modellodati.ArtefattoMD;
import com.threeamigos.foresta.artefatti.CostruttoreArtefatto;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.modellodati.RegistroArtefattiMD;
import com.threeamigos.foresta.tipi.TipoModificatore;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.testi.Testi;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Gli artefatti del mondo che non sono nell'inventario del gruppo: quelli da distribuire, quelli smarriti nelle locazioni e
 * le caselle in cui il gruppo sa che c'è un artefatto smarrito. I magazzini dei negozi, che stanno negli stessi dati, li governa
 * MagazziniDeiNegozi. I dati stanno nel {@code RegistroArtefattiMD}; qui li si trasforma in
 * {@link com.threeamigos.foresta.artefatti.Artefatto} e li si distribuisce a caso.
 */
public class RegistroArtefatti {

	// Da dove prendere i dati della partita in corso: il registro non sa chi li custodisce
	private static volatile Supplier<RegistroArtefattiMD> fornitoreDiDati;

	/**
	 * Dice da dove prendere i dati della partita in corso (lo fa chi li custodisce: ogni volta si prendono da quelli in corso).
	 */
	public static void setFornitoreDiDati(Supplier<RegistroArtefattiMD> fornitore) {
		fornitoreDiDati = fornitore;
	}

	private static RegistroArtefattiMD getRegistroArtefatti() {
		Supplier<RegistroArtefattiMD> fornitore = fornitoreDiDati;
		if (fornitore == null) {
			throw new IllegalStateException("Nessuno ha detto dove sono i dati di RegistroArtefatti (vedi setFornitoreDiDati)");
		}
		return fornitore.get();
	}

	private RegistroArtefatti() {
	}

	// I nomi e le descrizioni stanno in registro_artefatti_<lingua>.properties
	private static final String FILE_DEI_TESTI = "registro_artefatti";

	private static String nome(int numero) {
		return Testi.get(FILE_DEI_TESTI, "NOME." + numero).orElse("");
	}

	private static String descrizione(String chiave) {
		return Testi.get(FILE_DEI_TESTI, "DESCRIZIONE." + chiave).orElse("");
	}

	private static final String COMBATTIMENTO = "COMBATTIMENTO";
	private static final String PROTEZIONE = "PROTEZIONE";
	private static final String PERSUASIONE = "PERSUASIONE";
	private static final String MAGIA = "MAGIA";

	public static void reimposta() {
		getRegistroArtefatti().reimposta();

		aggiungiArtefatto(CostruttoreArtefatto.istanza()
				.setTipo(TipoArtefatto.SPADA)
				.setNome(nome(1))
				.setDescrizione(descrizione(COMBATTIMENTO))
				.setLivello(2)
				.setDanniBase(8)
				.setCostoAcquisto(15)
				.setPeso(1)
				.setModificatore(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_PERCENTUALE, 25)
				.setModificatore(TipoAttributo.VALORE, TipoModificatore.VARIAZIONE_PERCENTUALE, 5)
				.setModificatore(TipoAttributo.CORAGGIO, TipoModificatore.VARIAZIONE_PERCENTUALE, 5)
				.costruisci());

		aggiungiArtefatto(CostruttoreArtefatto.istanza()
				.setTipo(TipoArtefatto.ASCIA)
				.setNome(nome(2))
				.setDescrizione(descrizione(COMBATTIMENTO))
				.setLivello(3)
				.setDanniBase(12)
				.setCostoAcquisto(15)
				.setPeso(2)
				.setModificatore(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_PERCENTUALE, 50)
				.setModificatore(TipoAttributo.VALORE, TipoModificatore.VARIAZIONE_PERCENTUALE, 10)
				.setModificatore(TipoAttributo.CORAGGIO, TipoModificatore.VARIAZIONE_PERCENTUALE, 10)
				.costruisci());

		aggiungiArtefatto(CostruttoreArtefatto.istanza()
				.setTipo(TipoArtefatto.SPADA)
				.setNome(nome(3))
				.setDescrizione(descrizione(COMBATTIMENTO))
				.setLivello(4)
				.setDanniBase(10)
				.setCostoAcquisto(20)
				.setPeso(1)
				.setModificatore(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_PERCENTUALE, 75)
				.setModificatore(TipoAttributo.VALORE, TipoModificatore.VARIAZIONE_PERCENTUALE, 15)
				.setModificatore(TipoAttributo.CORAGGIO, TipoModificatore.VARIAZIONE_PERCENTUALE, 15)
				.costruisci());

		aggiungiArtefatto(CostruttoreArtefatto.istanza()
				.setTipo(TipoArtefatto.SPADA)
				.setNome(nome(4))
				.setDescrizione(descrizione(COMBATTIMENTO))
				.setLivello(5)
				.setDanniBase(12)
				.setCostoAcquisto(25)
				.setPeso(2)
				.setModificatore(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_PERCENTUALE, 100)
				.setModificatore(TipoAttributo.VALORE, TipoModificatore.VARIAZIONE_PERCENTUALE, 20)
				.setModificatore(TipoAttributo.CORAGGIO, TipoModificatore.VARIAZIONE_PERCENTUALE, 20)
				.costruisci());

		aggiungiArtefatto(CostruttoreArtefatto.istanza()
				.setTipo(TipoArtefatto.VESTE)
				.setNome(nome(5))
				.setDescrizione(descrizione(PROTEZIONE))
				.setLivello(1)
				.setCostoAcquisto(20)
				.setPeso(1)
				.setModificatore(TipoAttributo.STANCHEZZA, TipoModificatore.VARIAZIONE_PERCENTUALE, -10)
				.setModificatore(TipoAttributo.PARATA, TipoModificatore.VARIAZIONE_PERCENTUALE, 5)
				.setModificatore(TipoAttributo.RESISTENZA_MAGICA, TipoModificatore.VARIAZIONE_PERCENTUALE, 5)
				.costruisci());

		aggiungiArtefatto(CostruttoreArtefatto.istanza()
				.setTipo(TipoArtefatto.ELMO)
				.setNome(nome(6))
				.setDescrizione(descrizione(PROTEZIONE))
				.setLivello(2)
				.setCostoAcquisto(10)
				.setPeso(1)
				.setModificatore(TipoAttributo.CARISMA, TipoModificatore.VARIAZIONE_FISSA, 1)
				.setModificatore(TipoAttributo.PARATA, TipoModificatore.VARIAZIONE_PERCENTUALE, 10)
				.costruisci());

		aggiungiArtefatto(CostruttoreArtefatto.istanza()
				.setTipo(TipoArtefatto.SCUDO)
				.setNome(nome(7))
				.setDescrizione(descrizione(PROTEZIONE))
				.setLivello(3)
				.setCostoAcquisto(15)
				.setPeso(2)
				.setModificatore(TipoAttributo.CARISMA, TipoModificatore.VARIAZIONE_FISSA, 1)
				.setModificatore(TipoAttributo.PARATA, TipoModificatore.VARIAZIONE_PERCENTUALE, 15)
				.costruisci());

		aggiungiArtefatto(CostruttoreArtefatto.istanza()
				.setTipo(TipoArtefatto.ARMATURA)
				.setNome(nome(8))
				.setDescrizione(descrizione(PROTEZIONE))
				.setLivello(4)
				.setCostoAcquisto(20)
				.setPeso(3)
				.setModificatore(TipoAttributo.PARATA, TipoModificatore.VARIAZIONE_PERCENTUALE, 20)
				.costruisci());

		aggiungiArtefatto(CostruttoreArtefatto.istanza()
				.setTipo(TipoArtefatto.TALISMANO)
				.setNome(nome(9))
				.setDescrizione(descrizione(PERSUASIONE))
				.setLivello(2)
				.setCostoAcquisto(10)
				.setPeso(1)
				.setModificatore(TipoAttributo.CARISMA, TipoModificatore.VARIAZIONE_FISSA, 2)
				.costruisci());

		aggiungiArtefatto(CostruttoreArtefatto.istanza()
				.setTipo(TipoArtefatto.TALISMANO)
				.setNome(nome(10))
				.setDescrizione(descrizione(PERSUASIONE))
				.setLivello(3)
				.setCostoAcquisto(15)
				.setPeso(1)
				.setModificatore(TipoAttributo.CARISMA, TipoModificatore.VARIAZIONE_FISSA, 3)
				.costruisci());

		aggiungiArtefatto(CostruttoreArtefatto.istanza()
				.setTipo(TipoArtefatto.TALISMANO)
				.setNome(nome(11))
				.setDescrizione(descrizione(PERSUASIONE))
				.setLivello(4)
				.setCostoAcquisto(20)
				.setPeso(1)
				.setModificatore(TipoAttributo.CARISMA, TipoModificatore.VARIAZIONE_FISSA, 4)
				.costruisci());

		aggiungiArtefatto(CostruttoreArtefatto.istanza()
				.setTipo(TipoArtefatto.TALISMANO)
				.setNome(nome(12))
				.setDescrizione(descrizione(PERSUASIONE))
				.setLivello(1)
				.setCostoAcquisto(25)
				.setPeso(1)
				.setModificatore(TipoAttributo.CARISMA, TipoModificatore.VARIAZIONE_FISSA, 5)
				.costruisci());

		aggiungiArtefatto(CostruttoreArtefatto.istanza()
				.setTipo(TipoArtefatto.BASTONE_MAGICO)
				.setNome(nome(13))
				.setDescrizione(descrizione(MAGIA))
				.setLivello(1)
				.setCostoAcquisto(10)
				.setPeso(1)
				.setModificatore(TipoAttributo.POTERE_MAGICO, TipoModificatore.VARIAZIONE_FISSA, 5)
				.costruisci());

		aggiungiArtefatto(CostruttoreArtefatto.istanza()
				.setTipo(TipoArtefatto.BASTONE_MAGICO)
				.setNome(nome(14))
				.setDescrizione(descrizione(MAGIA))
				.setLivello(2)
				.setCostoAcquisto(15)
				.setPeso(2)
				.setModificatore(TipoAttributo.POTERE_MAGICO, TipoModificatore.VARIAZIONE_PERCENTUALE, 10)
				.setModificatore(TipoAttributo.NUMERO_BERSAGLI, TipoModificatore.VARIAZIONE_FISSA, 2)
				.costruisci());

		aggiungiArtefatto(CostruttoreArtefatto.istanza()
				.setTipo(TipoArtefatto.LIBRO_MAGICO)
				.setNome(nome(15))
				.setDescrizione(descrizione(MAGIA))
				.setLivello(3)
				.setCostoAcquisto(20)
				.setPeso(1)
				.setModificatore(TipoAttributo.POTERE_MAGICO, TipoModificatore.VARIAZIONE_PERCENTUALE, 15)
				.setModificatore(TipoAttributo.NUMERO_BERSAGLI, TipoModificatore.VARIAZIONE_FISSA, 3)
				.costruisci());

		aggiungiArtefatto(CostruttoreArtefatto.istanza()
				.setTipo(TipoArtefatto.BASTONE_MAGICO)
				.setNome(nome(16))
				.setDescrizione(descrizione(MAGIA))
				.setLivello(4)
				.setCostoAcquisto(25)
				.setPeso(1)
				.setModificatore(TipoAttributo.POTERE_MAGICO, TipoModificatore.VARIAZIONE_PERCENTUALE, 20)
				.setModificatore(TipoAttributo.NUMERO_BERSAGLI, TipoModificatore.VARIAZIONE_FISSA, 4)
				.costruisci());

	}

	// --- Artefatti leggendari (vedi LaLeggenda)

	/**
	 * Fa sorgere in quelle coordinate un tempio che custodisce l'artefatto, segnato sulla mappa: come gli altri
	 * templi con un artefatto, è guardato da un nido di viverne (vedi Tempio), a meno che la missione non ci metta
	 * i suoi guardiani.
	 */
	public static void custodisciInUnTempioNuovo(Artefatto artefatto, CoordinateMD coordinate) {
		Foresta.costruisciLocazione(coordinate, TipoLocazione.TEMPIO);
		addArtefattoInLocazione(artefatto, coordinate);
		segnaLocalizzazioneConosciuta(coordinate);
		Foresta.setLocazioneConosciuta(coordinate);
	}

	/**
	 * Uno a caso fra quelli ancora da sistemare nella foresta, tolto dall'elenco; null se sono finiti.
	 */
	public static Artefatto getArtefattoDisponibile() {
		int disponibili = getRegistroArtefatti().getNumeroDisponibili();
		if (disponibili == 0) {
			return null;
		}
		return costruisciArtefatto(getRegistroArtefatti().rimuoviDisponibile(Dado.tiraAncheAUnaFaccia(disponibili) - 1));
	}

	/**
	 * Uno a caso fra gli artefatti smarriti nelle locazioni, con la sua ubicazione; null se non ce ne sono.
	 */
	public static RegistroArtefattiMD.ArtefattoESuaUbicazione getArtefattoCasuale() {
		List<CoordinateMD> ubicazioni = new ArrayList<>(getRegistroArtefatti().getUbicazioniArtefattiSmarriti());
		if (ubicazioni.isEmpty()) {
			return null;
		}
		CoordinateMD coordinate = ubicazioni.get(Dado.tiraAncheAUnaFaccia(ubicazioni.size()) - 1);
		return new RegistroArtefattiMD.ArtefattoESuaUbicazione(getRegistroArtefatti().getArtefattoInLocazione(coordinate), coordinate);
	}

	/**
	 * Mette l'artefatto smarrito nella casella indicata.
	 */
	public static void addArtefattoInLocazione(Artefatto artefatto, CoordinateMD coordinate) {
		getRegistroArtefatti().addArtefattoInLocazione(artefatto.getModelloDati(), coordinate);
	}

	public static Artefatto getArtefattoInLocazione(CoordinateMD coordinate) {
		return costruisciArtefatto(getRegistroArtefatti().getArtefattoInLocazione(coordinate));
	}

	/**
	 * Toglie l'artefatto smarrito dalla casella, perché il gruppo l'ha preso.
	 */
	public static void rimuoviArtefattoInLocazione(CoordinateMD coordinate) {
		getRegistroArtefatti().rimuoviArtefattoInLocazione(coordinate);
	}

	/**
	 * Ricorda che il gruppo sa che nella casella c'è un artefatto smarrito, così la mappa la segnala.
	 */
	public static void segnaLocalizzazioneConosciuta(CoordinateMD coordinate) {
		getRegistroArtefatti().segnaLocalizzazioneConosciuta(coordinate);
	}

	public static boolean isLocalizzazioneConosciuta(CoordinateMD coordinate) {
		return getRegistroArtefatti().isLocalizzazioneConosciuta(coordinate);
	}

	public static Set<CoordinateMD> getLocalizzazioniConosciute() {
		return getRegistroArtefatti().getLocalizzazioniConosciute();
	}

	private static void aggiungiArtefatto(Artefatto artefatto) {
		getRegistroArtefatti().aggiungiArtefatto(artefatto.getModelloDati());
	}

	private static Artefatto costruisciArtefatto(ArtefattoMD modelloDati) {
		if (modelloDati == null) {
			return null;
		}
		// Artefatto.di e non new Artefatto: un'arma deve tornare un ArmaFisica, altrimenti
		// getArmaEquipaggiata() (che fa il cast ad Arma dell'arma impugnata)
		// fallisce con ClassCastException per le armi raccolte nei templi.
		return Artefatto.di(modelloDati);
	}
}
