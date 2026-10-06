package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoFineLocazione;
import com.threeamigos.foresta.eventi.interni.InternoPreparazioneLocazione;
import com.threeamigos.foresta.eventi.notifiche.NotificaApprovazioneVenditaArtefatto;
import com.threeamigos.foresta.eventi.notifiche.NotificaAumentoLivelloMondo;
import com.threeamigos.foresta.modellodati.ArtefattoMD;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.modellodati.RegistroArtefattiMD;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.oggetti.GeneratoreArtefatti;
import com.threeamigos.foresta.tipi.CategoriaLocazione;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoModificatore;
import com.threeamigos.foresta.tipi.TipoNegozio;
import com.threeamigos.foresta.strumenti.CostruttoreArtefatto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RegistroArtefatti {

	private static RegistroArtefattiMD getRegistroArtefatti() {
		return ModelloDati.getIstanza().getRegistroArtefattiMD();
	}

	private RegistroArtefatti() {
	}

	private static final String COMBATTIMENTO = "il cui potere è nel combattimento";
	private static final String PROTEZIONE = "che protegge dagli attacchi avversari";
	private static final String PERSUASIONE = "il cui potere è nella persuasione";
	private static final String MAGIA = "che aumenta il potere magico";

	static void reimposta() {
		getRegistroArtefatti().reimposta();

		aggiungiArtefatto(CostruttoreArtefatto.istanza()
				.setTipo(TipoArtefatto.SPADA)
				.setNome("il pugnale di Worr")
				.setDescrizione(COMBATTIMENTO)
				.setLivello(2)
				.setDanniBase(8)
				.setCostoAcquisto(15)
				.setPeso(1)
				.setModificatore(TipoAttributo.FORZA, TipoModificatore.AUMENTO_PERCENTUALE, 25)
				.setModificatore(TipoAttributo.VALORE, TipoModificatore.AUMENTO_PERCENTUALE, 5)
				.setModificatore(TipoAttributo.CORAGGIO, TipoModificatore.AUMENTO_PERCENTUALE, 5)
				.costruisci());

		aggiungiArtefatto(CostruttoreArtefatto.istanza()
				.setTipo(TipoArtefatto.ASCIA)
				.setNome("l'Ascia di Thrann")
				.setDescrizione(COMBATTIMENTO)
				.setLivello(3)
				.setDanniBase(12)
				.setCostoAcquisto(15)
				.setPeso(2)
				.setModificatore(TipoAttributo.FORZA, TipoModificatore.AUMENTO_PERCENTUALE, 50)
				.setModificatore(TipoAttributo.VALORE, TipoModificatore.AUMENTO_PERCENTUALE, 10)
				.setModificatore(TipoAttributo.CORAGGIO, TipoModificatore.AUMENTO_PERCENTUALE, 10)
				.costruisci());

		aggiungiArtefatto(CostruttoreArtefatto.istanza()
				.setTipo(TipoArtefatto.SPADA)
				.setNome("la Daga di Yltrim")
				.setDescrizione(COMBATTIMENTO)
				.setLivello(4)
				.setDanniBase(10)
				.setCostoAcquisto(20)
				.setPeso(1)
				.setModificatore(TipoAttributo.FORZA, TipoModificatore.AUMENTO_PERCENTUALE, 75)
				.setModificatore(TipoAttributo.VALORE, TipoModificatore.AUMENTO_PERCENTUALE, 15)
				.setModificatore(TipoAttributo.CORAGGIO, TipoModificatore.AUMENTO_PERCENTUALE, 15)
				.costruisci());

		aggiungiArtefatto(CostruttoreArtefatto.istanza()
				.setTipo(TipoArtefatto.SPADA)
				.setNome("la Spada di Kartham")
				.setDescrizione(COMBATTIMENTO)
				.setLivello(5)
				.setDanniBase(12)
				.setCostoAcquisto(25)
				.setPeso(2)
				.setModificatore(TipoAttributo.FORZA, TipoModificatore.AUMENTO_PERCENTUALE, 100)
				.setModificatore(TipoAttributo.VALORE, TipoModificatore.AUMENTO_PERCENTUALE, 20)
				.setModificatore(TipoAttributo.CORAGGIO, TipoModificatore.AUMENTO_PERCENTUALE, 20)
				.costruisci());

		aggiungiArtefatto(CostruttoreArtefatto.istanza()
				.setTipo(TipoArtefatto.VESTE)
				.setNome("il Manto di Grakk")
				.setDescrizione(PROTEZIONE)
				.setLivello(1)
				.setCostoAcquisto(20)
				.setPeso(1)
				.setModificatore(TipoAttributo.STANCHEZZA, TipoModificatore.AUMENTO_PERCENTUALE, -10)
				.setModificatore(TipoAttributo.PARATA, TipoModificatore.AUMENTO_PERCENTUALE, 5)
				.setModificatore(TipoAttributo.RESISTENZA_MAGICA, TipoModificatore.AUMENTO_PERCENTUALE, 5)
				.costruisci());

		aggiungiArtefatto(CostruttoreArtefatto.istanza()
				.setTipo(TipoArtefatto.ELMO)
				.setNome("l'Elmo di Mithrr")
				.setDescrizione(PROTEZIONE)
				.setLivello(2)
				.setCostoAcquisto(10)
				.setPeso(1)
				.setModificatore(TipoAttributo.CARISMA, TipoModificatore.AUMENTO_FISSO, 1)
				.setModificatore(TipoAttributo.PARATA, TipoModificatore.AUMENTO_PERCENTUALE, 10)
				.costruisci());

		aggiungiArtefatto(CostruttoreArtefatto.istanza()
				.setTipo(TipoArtefatto.SCUDO)
				.setNome("lo Scudo di Kalar")
				.setDescrizione(PROTEZIONE)
				.setLivello(3)
				.setCostoAcquisto(15)
				.setPeso(2)
				.setModificatore(TipoAttributo.CARISMA, TipoModificatore.AUMENTO_FISSO, 1)
				.setModificatore(TipoAttributo.PARATA, TipoModificatore.AUMENTO_PERCENTUALE, 15)
				.costruisci());

		aggiungiArtefatto(CostruttoreArtefatto.istanza()
				.setTipo(TipoArtefatto.ARMATURA)
				.setNome("la Corazza di Yar")
				.setDescrizione(PROTEZIONE)
				.setLivello(4)
				.setCostoAcquisto(20)
				.setPeso(3)
				.setModificatore(TipoAttributo.PARATA, TipoModificatore.AUMENTO_PERCENTUALE, 20)
				.costruisci());

		aggiungiArtefatto(CostruttoreArtefatto.istanza()
				.setTipo(TipoArtefatto.TALISMANO)
				.setNome("il Talismano di Beltram")
				.setDescrizione(PERSUASIONE)
				.setLivello(2)
				.setCostoAcquisto(10)
				.setPeso(1)
				.setModificatore(TipoAttributo.CARISMA, TipoModificatore.AUMENTO_FISSO, 2)
				.costruisci());

		aggiungiArtefatto(CostruttoreArtefatto.istanza()
				.setTipo(TipoArtefatto.TALISMANO)
				.setNome("il Sigillo di Yshtalar")
				.setDescrizione(PERSUASIONE)
				.setLivello(3)
				.setCostoAcquisto(15)
				.setPeso(1)
				.setModificatore(TipoAttributo.CARISMA, TipoModificatore.AUMENTO_FISSO, 3)
				.costruisci());

		aggiungiArtefatto(CostruttoreArtefatto.istanza()
				.setTipo(TipoArtefatto.TALISMANO)
				.setNome("la Serpe di Yalar")
				.setDescrizione(PERSUASIONE)
				.setLivello(4)
				.setCostoAcquisto(20)
				.setPeso(1)
				.setModificatore(TipoAttributo.CARISMA, TipoModificatore.AUMENTO_FISSO, 4)
				.costruisci());

		aggiungiArtefatto(CostruttoreArtefatto.istanza()
				.setTipo(TipoArtefatto.TALISMANO)
				.setNome("il Flagello di Mutr")
				.setDescrizione(PERSUASIONE)
				.setLivello(1)
				.setCostoAcquisto(25)
				.setPeso(1)
				.setModificatore(TipoAttributo.CARISMA, TipoModificatore.AUMENTO_FISSO, 5)
				.costruisci());

		aggiungiArtefatto(CostruttoreArtefatto.istanza()
				.setTipo(TipoArtefatto.BASTONE_MAGICO)
				.setNome("la Bacchetta di Yuw")
				.setDescrizione(MAGIA)
				.setLivello(1)
				.setCostoAcquisto(10)
				.setPeso(1)
				.setModificatore(TipoAttributo.POTERE_MAGICO, TipoModificatore.AUMENTO_FISSO, 5)
				.costruisci());

		aggiungiArtefatto(CostruttoreArtefatto.istanza()
				.setTipo(TipoArtefatto.BASTONE_MAGICO)
				.setNome("la Verga di Pannk")
				.setDescrizione(MAGIA)
				.setLivello(2)
				.setCostoAcquisto(15)
				.setPeso(2)
				.setModificatore(TipoAttributo.POTERE_MAGICO, TipoModificatore.AUMENTO_PERCENTUALE, 10)
				.setModificatore(TipoAttributo.NUMERO_BERSAGLI, TipoModificatore.AUMENTO_FISSO, 2)
				.costruisci());

		aggiungiArtefatto(CostruttoreArtefatto.istanza()
				.setTipo(TipoArtefatto.LIBRO_MAGICO)
				.setNome("il Libro di Menk")
				.setDescrizione(MAGIA)
				.setLivello(3)
				.setCostoAcquisto(20)
				.setPeso(1)
				.setModificatore(TipoAttributo.POTERE_MAGICO, TipoModificatore.AUMENTO_PERCENTUALE, 15)
				.setModificatore(TipoAttributo.NUMERO_BERSAGLI, TipoModificatore.AUMENTO_FISSO, 3)
				.costruisci());

		aggiungiArtefatto(CostruttoreArtefatto.istanza()
				.setTipo(TipoArtefatto.BASTONE_MAGICO)
				.setNome("il Bastone di Plarr")
				.setDescrizione(MAGIA)
				.setLivello(4)
				.setCostoAcquisto(25)
				.setPeso(1)
				.setModificatore(TipoAttributo.POTERE_MAGICO, TipoModificatore.AUMENTO_PERCENTUALE, 20)
				.setModificatore(TipoAttributo.NUMERO_BERSAGLI, TipoModificatore.AUMENTO_FISSO, 4)
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
	static Artefatto getArtefattoDisponibile() {
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

	public static void addArtefattoInLocazione(Artefatto artefatto, CoordinateMD coordinate) {
		getRegistroArtefatti().addArtefattoInLocazione(artefatto.getModelloDati(), coordinate);
	}

	public static Artefatto getArtefattoInLocazione(CoordinateMD coordinate) {
		return costruisciArtefatto(getRegistroArtefatti().getArtefattoInLocazione(coordinate));
	}

	public static void rimuoviArtefattoInLocazione(CoordinateMD coordinate) {
		getRegistroArtefatti().rimuoviArtefattoInLocazione(coordinate);
	}

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

	public static ScambiatoreArtefatti getScambiatorePerNegozio(CoordinateMD coordinate, TipoNegozio negozio) {
		return new ScambiatoreArtefatti() {
			@Override
			public Collection<Artefatto> getInventario() {
				return getMagazzino().stream().map(Artefatto::di).collect(Collectors.toList());
			}

			@Override
			public void addArtefatto(Artefatto artefatto) {
				getMagazzino().add(artefatto.getModelloDati());
			}

			@Override
			public void removeArtefatto(Artefatto artefatto) {
				getMagazzino().remove(artefatto.getModelloDati());
			}

			@Override
			public boolean tratta(Artefatto artefatto) {
				return negozio.tratta(artefatto.getTipo());
			}

			private Collection<ArtefattoMD> getMagazzino() {
				return getRegistroArtefatti().getMagazzino(coordinate, negozio);
			}
		};
	}

	/**
	 * Si iscrive agli eventi che cambiano i magazzini dei negozi: le vendite del gruppo, la fine
	 * della locazione e l'aumento del livello del mondo.
	 */
	public static void registrati() {
		BusEventi.iscriviti(NotificaApprovazioneVenditaArtefatto.class, RegistroArtefatti::ricordaVendita);
		BusEventi.iscriviti(InternoPreparazioneLocazione.class, evento -> vendutiNellaVisita.clear());
		BusEventi.iscriviti(InternoFineLocazione.class, evento -> smaltisciVenduti());
		BusEventi.iscriviti(NotificaAumentoLivelloMondo.class, evento -> aggiornaMagazzini(evento.getLivello(), GeneratoreArtefatti.istanza()));
	}

	/**
	 * Riempie i magazzini dei negozi di una città alla creazione del mondo: armi ed equipaggiamento per
	 * l'armaiolo, ingredienti magici per il venditore di pergamene, attorno al livello del mondo.
	 */
	static void riempiMagazzini(CoordinateMD coordinate, GeneratoreArtefatti generatore) {
		rifornisci(coordinate, Statistiche.getLivello(), generatore);
	}

	/**
	 * Il livello del mondo è aumentato: in ogni città ancora in piedi l'armaiolo scarta gli artefatti
	 * ormai troppo deboli, il venditore rinnova tutti i suoi ingredienti magici, e arriva merce nuova.
	 */
	static void aggiornaMagazzini(int livelloMondo, GeneratoreArtefatti generatore) {
		int livelloMinimo = livelloMondo - Costanti.MAGAZZINO_SCARTO_SOTTO_LIVELLO;
		RegistroArtefattiMD registro = getRegistroArtefatti();
		for (CoordinateMD coordinate : registro.getCoordinateMagazzini(TipoNegozio.ARMAIOLO)) {
			if (Foresta.getLocazione(coordinate).getCategoria() == CategoriaLocazione.CITTA) {
				registro.tieniInMagazzino(coordinate, TipoNegozio.ARMAIOLO, artefatto -> artefatto.getLivello() >= livelloMinimo);
				registro.tieniInMagazzino(coordinate, TipoNegozio.VENDITORE_DI_PERGAMENE, artefatto -> false);
				rifornisci(coordinate, livelloMondo, generatore);
			}
		}
	}

	private static void rifornisci(CoordinateMD coordinate, int livelloMondo, GeneratoreArtefatti generatore) {
		ScambiatoreArtefatti armaiolo = getScambiatorePerNegozio(coordinate, TipoNegozio.ARMAIOLO);
		for (int i = 0; i < Costanti.MAGAZZINO_ARTEFATTI_ARMAIOLO; i++) {
			armaiolo.addArtefatto(generatore.generaArtefattoCasuale(livelloInMagazzino(i, livelloMondo)));
		}
		ScambiatoreArtefatti venditoreDiPergamene = getScambiatorePerNegozio(coordinate, TipoNegozio.VENDITORE_DI_PERGAMENE);
		for (int i = 0; i < Costanti.MAGAZZINO_PERGAMENE; i++) {
			venditoreDiPergamene.addArtefatto(generatore.generaIngrediente(livelloInMagazzino(i, livelloMondo)));
		}
	}

	/**
	 * Livelli a rotazione fra livello del mondo - divario e livello del mondo + divario, mai sotto 1.
	 */
	private static int livelloInMagazzino(int indice, int livelloMondo) {
		int minimo = Math.max(1, livelloMondo - Costanti.MAGAZZINO_DIVARIO_LIVELLO);
		int massimo = livelloMondo + Costanti.MAGAZZINO_DIVARIO_LIVELLO;
		return minimo + indice % (massimo - minimo + 1);
	}

	/**
	 * Quanto il gruppo ha venduto ai negozi durante la visita alla città, e dove.
	 */
	private static final List<Vendita> vendutiNellaVisita = new ArrayList<>();

	private static final class Vendita {
		private final CoordinateMD coordinate;
		private final TipoNegozio negozio;
		private final ArtefattoMD artefatto;

		Vendita(CoordinateMD coordinate, TipoNegozio negozio, ArtefattoMD artefatto) {
			this.coordinate = coordinate;
			this.negozio = negozio;
			this.artefatto = artefatto;
		}
	}

	/**
	 * Il negozio si riconosce dalla merce: il venditore tratta solo le pergamene, l'armaiolo tutto il resto.
	 */
	private static void ricordaVendita(NotificaApprovazioneVenditaArtefatto evento) {
		Artefatto artefatto = (Artefatto) evento.getOggettoSpostato();
		TipoNegozio negozio = TipoNegozio.VENDITORE_DI_PERGAMENE.tratta(artefatto.getTipo())
				? TipoNegozio.VENDITORE_DI_PERGAMENE : TipoNegozio.ARMAIOLO;
		vendutiNellaVisita.add(new Vendita(GruppoGiocatore.getIstanza().getCoordinate(), negozio, artefatto.getModelloDati()));
	}

	/**
	 * All'uscita dalla città i negozi distruggono quanto il gruppo ha venduto loro e non ha ricomprato,
	 * così i magazzini non crescono all'infinito. Che cosa ne faranno lo dicono al momento della vendita, a bottega
	 * aperta (vedi DisplayableCanvasCommerciante.fraseDopoLaVendita).
	 */
	private static void smaltisciVenduti() {
		for (Vendita vendita : vendutiNellaVisita) {
			getRegistroArtefatti().rimuoviDaMagazzino(vendita.coordinate, vendita.negozio, vendita.artefatto);
		}
		vendutiNellaVisita.clear();
	}
}
