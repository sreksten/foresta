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

	private static final String COMBATTIMENTO = "il cui potere è nel combattimento";
	private static final String PROTEZIONE = "che protegge dagli attacchi avversari";
	private static final String PERSUASIONE = "il cui potere è nella persuasione";
	private static final String MAGIA = "che aumenta il potere magico";

	public static void reimposta() {
		getRegistroArtefatti().reimposta();

		aggiungiArtefatto(CostruttoreArtefatto.istanza()
				.setTipo(TipoArtefatto.SPADA)
				.setNome("il pugnale di Worr")
				.setDescrizione(COMBATTIMENTO)
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
				.setNome("l'Ascia di Thrann")
				.setDescrizione(COMBATTIMENTO)
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
				.setNome("la Daga di Yltrim")
				.setDescrizione(COMBATTIMENTO)
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
				.setNome("la Spada di Kartham")
				.setDescrizione(COMBATTIMENTO)
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
				.setNome("il Manto di Grakk")
				.setDescrizione(PROTEZIONE)
				.setLivello(1)
				.setCostoAcquisto(20)
				.setPeso(1)
				.setModificatore(TipoAttributo.STANCHEZZA, TipoModificatore.VARIAZIONE_PERCENTUALE, -10)
				.setModificatore(TipoAttributo.PARATA, TipoModificatore.VARIAZIONE_PERCENTUALE, 5)
				.setModificatore(TipoAttributo.RESISTENZA_MAGICA, TipoModificatore.VARIAZIONE_PERCENTUALE, 5)
				.costruisci());

		aggiungiArtefatto(CostruttoreArtefatto.istanza()
				.setTipo(TipoArtefatto.ELMO)
				.setNome("l'Elmo di Mithrr")
				.setDescrizione(PROTEZIONE)
				.setLivello(2)
				.setCostoAcquisto(10)
				.setPeso(1)
				.setModificatore(TipoAttributo.CARISMA, TipoModificatore.VARIAZIONE_FISSA, 1)
				.setModificatore(TipoAttributo.PARATA, TipoModificatore.VARIAZIONE_PERCENTUALE, 10)
				.costruisci());

		aggiungiArtefatto(CostruttoreArtefatto.istanza()
				.setTipo(TipoArtefatto.SCUDO)
				.setNome("lo Scudo di Kalar")
				.setDescrizione(PROTEZIONE)
				.setLivello(3)
				.setCostoAcquisto(15)
				.setPeso(2)
				.setModificatore(TipoAttributo.CARISMA, TipoModificatore.VARIAZIONE_FISSA, 1)
				.setModificatore(TipoAttributo.PARATA, TipoModificatore.VARIAZIONE_PERCENTUALE, 15)
				.costruisci());

		aggiungiArtefatto(CostruttoreArtefatto.istanza()
				.setTipo(TipoArtefatto.ARMATURA)
				.setNome("la Corazza di Yar")
				.setDescrizione(PROTEZIONE)
				.setLivello(4)
				.setCostoAcquisto(20)
				.setPeso(3)
				.setModificatore(TipoAttributo.PARATA, TipoModificatore.VARIAZIONE_PERCENTUALE, 20)
				.costruisci());

		aggiungiArtefatto(CostruttoreArtefatto.istanza()
				.setTipo(TipoArtefatto.TALISMANO)
				.setNome("il Talismano di Beltram")
				.setDescrizione(PERSUASIONE)
				.setLivello(2)
				.setCostoAcquisto(10)
				.setPeso(1)
				.setModificatore(TipoAttributo.CARISMA, TipoModificatore.VARIAZIONE_FISSA, 2)
				.costruisci());

		aggiungiArtefatto(CostruttoreArtefatto.istanza()
				.setTipo(TipoArtefatto.TALISMANO)
				.setNome("il Sigillo di Yshtalar")
				.setDescrizione(PERSUASIONE)
				.setLivello(3)
				.setCostoAcquisto(15)
				.setPeso(1)
				.setModificatore(TipoAttributo.CARISMA, TipoModificatore.VARIAZIONE_FISSA, 3)
				.costruisci());

		aggiungiArtefatto(CostruttoreArtefatto.istanza()
				.setTipo(TipoArtefatto.TALISMANO)
				.setNome("la Serpe di Yalar")
				.setDescrizione(PERSUASIONE)
				.setLivello(4)
				.setCostoAcquisto(20)
				.setPeso(1)
				.setModificatore(TipoAttributo.CARISMA, TipoModificatore.VARIAZIONE_FISSA, 4)
				.costruisci());

		aggiungiArtefatto(CostruttoreArtefatto.istanza()
				.setTipo(TipoArtefatto.TALISMANO)
				.setNome("il Flagello di Mutr")
				.setDescrizione(PERSUASIONE)
				.setLivello(1)
				.setCostoAcquisto(25)
				.setPeso(1)
				.setModificatore(TipoAttributo.CARISMA, TipoModificatore.VARIAZIONE_FISSA, 5)
				.costruisci());

		aggiungiArtefatto(CostruttoreArtefatto.istanza()
				.setTipo(TipoArtefatto.BASTONE_MAGICO)
				.setNome("la Bacchetta di Yuw")
				.setDescrizione(MAGIA)
				.setLivello(1)
				.setCostoAcquisto(10)
				.setPeso(1)
				.setModificatore(TipoAttributo.POTERE_MAGICO, TipoModificatore.VARIAZIONE_FISSA, 5)
				.costruisci());

		aggiungiArtefatto(CostruttoreArtefatto.istanza()
				.setTipo(TipoArtefatto.BASTONE_MAGICO)
				.setNome("la Verga di Pannk")
				.setDescrizione(MAGIA)
				.setLivello(2)
				.setCostoAcquisto(15)
				.setPeso(2)
				.setModificatore(TipoAttributo.POTERE_MAGICO, TipoModificatore.VARIAZIONE_PERCENTUALE, 10)
				.setModificatore(TipoAttributo.NUMERO_BERSAGLI, TipoModificatore.VARIAZIONE_FISSA, 2)
				.costruisci());

		aggiungiArtefatto(CostruttoreArtefatto.istanza()
				.setTipo(TipoArtefatto.LIBRO_MAGICO)
				.setNome("il Libro di Menk")
				.setDescrizione(MAGIA)
				.setLivello(3)
				.setCostoAcquisto(20)
				.setPeso(1)
				.setModificatore(TipoAttributo.POTERE_MAGICO, TipoModificatore.VARIAZIONE_PERCENTUALE, 15)
				.setModificatore(TipoAttributo.NUMERO_BERSAGLI, TipoModificatore.VARIAZIONE_FISSA, 3)
				.costruisci());

		aggiungiArtefatto(CostruttoreArtefatto.istanza()
				.setTipo(TipoArtefatto.BASTONE_MAGICO)
				.setNome("il Bastone di Plarr")
				.setDescrizione(MAGIA)
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
