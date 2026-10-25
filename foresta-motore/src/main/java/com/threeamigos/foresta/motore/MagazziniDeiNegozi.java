package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.statistiche.Statistiche;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.artefatti.ScambiatoreArtefatti;
import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.modellodati.ArtefattoMD;
import com.threeamigos.foresta.artefatti.GeneratoreArtefatti;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoFineLocazione;
import com.threeamigos.foresta.eventi.interni.InternoPreparazioneLocazione;
import com.threeamigos.foresta.eventi.notifiche.NotificaApprovazioneVenditaArtefatto;
import com.threeamigos.foresta.eventi.notifiche.NotificaAumentoLivelloMondo;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.modellodati.RegistroArtefattiMD;
import com.threeamigos.foresta.tipi.CategoriaLocazione;
import com.threeamigos.foresta.tipi.Costanti;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoNegozio;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * I magazzini dei negozi delle città e delle botteghe della Foresta: come si riempiono, come si riforniscono quando il mondo sale di
 * livello, e che cosa fanno di quanto il gruppo vende loro. I dati stanno nel {@code RegistroArtefattiMD}, insieme agli artefatti sparsi
 * per la Foresta (vedi RegistroArtefatti, nel modulo del mondo); qui c'è il commercio, in attesa di un modulo suo.
 */
public final class MagazziniDeiNegozi {

	private MagazziniDeiNegozi() {
	}

	private static RegistroArtefattiMD getRegistroArtefatti() {
		return ModelloDati.getIstanza().getRegistroArtefattiMD();
	}

	public static ScambiatoreArtefatti getScambiatorePerNegozio(CoordinateMD coordinate, TipoNegozio negozio) {
		return new ScambiatoreArtefatti() {
			@Override
			public Collection<Artefatto> getInventario() {
				return getMagazzino().stream().map(Artefatto::di).collect(Collectors.toList());
			}

			@Override
			public void addArtefatto(Artefatto artefatto) {
				getRegistroArtefatti().aggiungiAMagazzino(coordinate, negozio, artefatto.getModelloDati());
			}

			@Override
			public void removeArtefatto(Artefatto artefatto) {
				getRegistroArtefatti().rimuoviDaMagazzino(coordinate, negozio, artefatto.getModelloDati());
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
		BusEventi.iscriviti(NotificaApprovazioneVenditaArtefatto.class, MagazziniDeiNegozi::ricordaVendita);
		BusEventi.iscriviti(InternoPreparazioneLocazione.class, evento -> vendutiNellaVisita.clear());
		BusEventi.iscriviti(InternoFineLocazione.class, evento -> smaltisciVenduti());
		BusEventi.iscriviti(NotificaAumentoLivelloMondo.class, evento -> aggiornaMagazzini(evento.getLivello(), GeneratoreArtefatti.istanza()));
	}

	/**
	 * Riempie i magazzini dei negozi di una città alla creazione del mondo: armi ed equipaggiamento per
	 * l'armaiolo, ingredienti magici per il venditore di pergamene, attorno al livello del mondo.
	 */
	public static void riempiMagazzini(CoordinateMD coordinate, GeneratoreArtefatti generatore) {
		riempiMagazzino(coordinate, TipoNegozio.ARMAIOLO, generatore);
		riempiMagazzino(coordinate, TipoNegozio.VENDITORE_DI_PERGAMENE, generatore);
	}

	/**
	 * Riempie il magazzino di una bottega nata nella Foresta (vedi locazioni.Bottega), attorno al livello del mondo di adesso.
	 */
	public static void riempiMagazzino(CoordinateMD coordinate, TipoNegozio negozio) {
		riempiMagazzino(coordinate, negozio, GeneratoreArtefatti.istanza());
	}

	private static void riempiMagazzino(CoordinateMD coordinate, TipoNegozio negozio, GeneratoreArtefatti generatore) {
		rifornisci(coordinate, Statistiche.getLivello(), negozio, generatore);
	}

	/**
	 * Il livello del mondo è aumentato: in ogni città ancora in piedi, e in ogni bottega della Foresta, l'armaiolo scarta gli
	 * artefatti ormai troppo deboli, il venditore rinnova tutti i suoi ingredienti magici, e arriva merce nuova.
	 */
	static void aggiornaMagazzini(int livelloMondo, GeneratoreArtefatti generatore) {
		int livelloMinimo = livelloMondo - Costanti.MAGAZZINO_SCARTO_SOTTO_LIVELLO;
		RegistroArtefattiMD registro = getRegistroArtefatti();
		for (CoordinateMD coordinate : registro.getCoordinateMagazzini(TipoNegozio.ARMAIOLO)) {
			if (haUnNegozio(coordinate, TipoNegozio.ARMAIOLO)) {
				registro.tieniInMagazzino(coordinate, TipoNegozio.ARMAIOLO, artefatto -> artefatto.getLivello() >= livelloMinimo);
				rifornisci(coordinate, livelloMondo, TipoNegozio.ARMAIOLO, generatore);
			}
		}
		for (CoordinateMD coordinate : registro.getCoordinateMagazzini(TipoNegozio.VENDITORE_DI_PERGAMENE)) {
			if (haUnNegozio(coordinate, TipoNegozio.VENDITORE_DI_PERGAMENE)) {
				registro.tieniInMagazzino(coordinate, TipoNegozio.VENDITORE_DI_PERGAMENE, artefatto -> false);
				rifornisci(coordinate, livelloMondo, TipoNegozio.VENDITORE_DI_PERGAMENE, generatore);
			}
		}
	}

	/**
	 * Se quella casella ha ancora quel negozio: una città in piedi, o la bottega di quel mestiere nella Foresta.
	 */
	private static boolean haUnNegozio(CoordinateMD coordinate, TipoNegozio negozio) {
		TipoLocazione tipo = Foresta.getLocazione(coordinate);
		return tipo.getCategoriaLocazione() == CategoriaLocazione.CITTA
				|| tipo == (negozio == TipoNegozio.ARMAIOLO ? TipoLocazione.ARMAIOLO : TipoLocazione.VENDITORE_DI_PERGAMENE);
	}

	private static void rifornisci(CoordinateMD coordinate, int livelloMondo, TipoNegozio negozio, GeneratoreArtefatti generatore) {
		ScambiatoreArtefatti scambiatore = getScambiatorePerNegozio(coordinate, negozio);
		if (negozio == TipoNegozio.ARMAIOLO) {
			for (int i = 0; i < Costanti.MAGAZZINO_ARTEFATTI_ARMAIOLO; i++) {
				scambiatore.addArtefatto(generatore.generaArtefattoCasuale(livelloInMagazzino(i, livelloMondo)));
			}
		} else {
			for (int i = 0; i < Costanti.MAGAZZINO_PERGAMENE; i++) {
				scambiatore.addArtefatto(generatore.generaIngrediente(livelloInMagazzino(i, livelloMondo)));
			}
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
		vendutiNellaVisita.add(new Vendita(Foresta.getPosizioneDelGruppo(), negozio, artefatto.getModelloDati()));
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
