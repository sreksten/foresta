package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.mondo.RegistroArtefatti;
import com.threeamigos.foresta.mondo.RegistroPersonaggi;
import com.threeamigos.foresta.statistiche.Statistiche;
import com.threeamigos.foresta.statistiche.LineaTemporale;
import com.threeamigos.foresta.gruppi.GruppoAvversario;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.artefatti.GeneratoreArtefatti;
import com.threeamigos.foresta.intermezzi.IntermezzoLibroTecnico;
import com.threeamigos.foresta.locazioni.FabbricaLocazioni;
import com.threeamigos.foresta.locazioni.Locanda;
import com.threeamigos.foresta.locazioni.Locazione;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.mappa.GeneratoreDellaForesta;
import com.threeamigos.foresta.modellodati.LocazioneMD;
import com.threeamigos.foresta.mappa.PopolatoreDellaForesta;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.missioni.RegistroMissioni;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.testi.ProduttoreDiLocandeCasuale;
import com.threeamigos.foresta.testi.ProduttoriDiTesti;
import com.threeamigos.foresta.trofei.RegistroTrofei;

import java.util.*;

/**
 * Chi fa nascere il mondo di una partita nuova sulla mappa della Foresta (vedi {@link Foresta} e {@link GeneratoreDellaForesta}, che
 * costruiscono le caselle e i loro nomi): azzera i registri, e poi popola la Foresta mentre nasce (i residenti delle città, i magazzini
 * dei negozi, i personaggi nelle locande, gli artefatti nei templi). Dà anche l'istanza di una casella per la visita, e fa da ponte fra la
 * mappa e le missioni e gli artefatti (le caselle da segnalare, i nomi delle missioni).
 */
public class CostruttoreDellaForesta {

	private static final int DIMENSIONE_X = 20;
	private static final int DIMENSIONE_Y = 20;

	private CostruttoreDellaForesta() {
	}

	/**
	 * Fa della casella in cui si trova il gruppo una locazione di quel tipo.
	 */
	public static void impostaLocazioneCorrente(TipoLocazione tipoLocazione) {
		Foresta.impostaLocazione(Foresta.getPosizioneDelGruppo(), tipoLocazione);
	}

	/**
	 * Il punto unico dove nasce l'istanza di una casella: la classe la costruisce,
	 * e il modello dati della casella le dice di quale casella si tratta.
	 * L'istanza vive quanto la visita e poi si butta.
	 */
	public static Locazione costruisciIstanza(CoordinateMD coordinate) {
		LocazioneMD locazioneMD = Foresta.getLocazioneMD(coordinate);
		Locazione locazione = FabbricaLocazioni.crea(locazioneMD.getTipo());
		locazione.setModelloDati(locazioneMD);
		return locazione;
	}
	
	/**
	 * Reimposta completamente il mondo
	 */
	static void reimposta() {
		// Reset produzioni one-shot (nomi locande, fiabe, oroscopi) per la nuova partita
		ProduttoriDiTesti.resetProduzioni();

		RegistroPersonaggi.reimposta();
		LineaTemporale.reimposta();
		RegistroMissioni.reimposta();
		RegistroArtefatti.reimposta();
		Statistiche.reimposta();
		IntermezzoLibroTecnico.reimposta();
		// Quello che la partita precedente aveva guadagnato per i trofei e non aveva ancora consolidato non vale per questa
		RegistroTrofei.scartaIlProgressoInSospeso();
		// Gli intermezzi già visti e le notizie sono di una partita: la nuova ricomincia da capo (l'intro, le prime visite)
		ModelloDati.getIstanza().getIntermezziMD().reimposta();
		ModelloDati.getIstanza().getNotizieMD().reimposta();

		// I castelli degli alleati del Drago non si costruiscono qui: li rivendica ciascuno la propria missione
		// all'inizio della partita (vedi RegistroMissioni.rivendicaPerLocazioneUnica)
		GeneratoreDellaForesta.genera(DIMENSIONE_X, DIMENSIONE_Y, RegistroPersonaggi.getNumeroPersonaggiDisponibili(), new PopolatoreDelMondo());

		GruppoGiocatore.getIstanza().reimposta();
		// Il gruppo parte da una casella libera a caso: dove sta lo sa la mappa
		Foresta.posizionaIlGruppoSuUnaCasellaLibera();
		DotazioneDiProva.applica(GruppoGiocatore.getIstanza());
		GruppoAvversario.getIstanza().reimposta();
	}

	/**
	 * Quel che il mondo mette nella Foresta quando nasce.
	 */
	private static final class PopolatoreDelMondo implements PopolatoreDellaForesta {

		private Personaggio personaggioDaPiazzare;
		private Artefatto artefattoDaPiazzare;

		@Override
		public void impostaDatiLocanda(LocazioneMD locazione, ProduttoreDiLocandeCasuale.DatiLocanda datiLocanda) {
			Locanda.impostaDatiLocanda(locazione, datiLocanda);
		}

		@Override
		public void cittaCostruita(TipoLocazione citta, CoordinateMD coordinate) {
			RegistroPersonaggi.creaIResidentiDellaCitta(citta);
			MagazziniDeiNegozi.riempiMagazzini(coordinate, GeneratoreArtefatti.istanza());
			Personaggio personaggioDisponibile = RegistroPersonaggi.getPersonaggioDisponibile();
			if (personaggioDisponibile != null) {
				RegistroPersonaggi.addPersonaggioInLocazione(personaggioDisponibile, coordinate);
			}
		}

		@Override
		public void dopoLeCitta() {
			RegistroPersonaggi.creaIlBardoESuaMoglie();
		}

		@Override
		public boolean prelevaPersonaggio() {
			personaggioDaPiazzare = RegistroPersonaggi.getPersonaggioDisponibile();
			return personaggioDaPiazzare != null;
		}

		@Override
		public void locandaCostruita(CoordinateMD coordinate) {
			RegistroPersonaggi.addPersonaggioInLocazione(personaggioDaPiazzare, coordinate);
		}

		@Override
		public boolean prelevaArtefatto() {
			artefattoDaPiazzare = RegistroArtefatti.getArtefattoDisponibile();
			return artefattoDaPiazzare != null;
		}

		@Override
		public void tempioCostruito(CoordinateMD coordinate) {
			RegistroArtefatti.addArtefattoInLocazione(artefattoDaPiazzare, coordinate);
		}
	}

	/**
	 * I nomi delle missioni da mostrare sulla mappa per quella casella (vedi RegistroMissioni.getNomiMissioniDaSegnalare),
	 * se il gruppo la conosce; altrimenti nessuno.
	 */
	public static List<String> getNomiMissioniDaMostrare(CoordinateMD coordinate) {
		if (!Foresta.isDentroLaMappa(coordinate) || !Foresta.isLocazioneConosciuta(coordinate)) {
			return Collections.emptyList();
		}
		return RegistroMissioni.getNomiMissioniDaSegnalare(coordinate);
	}

	/**
	 * Le locazioni da segnalare sulla mappa con Indicatore.gif: quelle di un artefatto di cui
	 * si è saputo tramite Informazioni, più le caselle rivendicate dalle missioni a passi attive che il gruppo conosce.
	 */
	public static List<CoordinateMD> getCoordinateDaSegnalare() {
		Set<CoordinateMD> coordinateDaSegnalare = new LinkedHashSet<>(RegistroArtefatti.getLocalizzazioniConosciute());
		coordinateDaSegnalare.addAll(RegistroMissioni.getLocazioniDaSegnalare());
		return new ArrayList<>(coordinateDaSegnalare);
	}
}
