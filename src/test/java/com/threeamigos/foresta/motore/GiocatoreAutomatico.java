package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.comandigiocatore.ComandoAcquistoConsumabile;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoAperturaInventarioCommerciante;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoVenditaArtefatto;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.motore.modellodati.TipoNegozio;
import com.threeamigos.foresta.incantesimi.ClasseIncantesimo;
import com.threeamigos.foresta.incantesimi.DardoArcano;
import com.threeamigos.foresta.incantesimi.Incantesimo;
import com.threeamigos.foresta.incantesimi.PortataIncantesimo;
import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.locazioni.ClassiLocazione.TipoLocazione;
import com.threeamigos.foresta.locazioni.Locanda;
import com.threeamigos.foresta.locazioni.Locazione;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.motore.modellodati.TipoAttributo;
import com.threeamigos.foresta.motore.modellodati.TipoConsumabile;
import com.threeamigos.foresta.personaggi.Personaggio;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;

/**
 * Il giocatore automatico di {@link SimulazionePartiteTest}: gioca una partita vera (senza la modalità di prova)
 * mandando all'Automa, tramite {@link PartitaDiTest}, gli stessi comandi della UI. La priorità è sopravvivere.
 * <p>
 * <b>Combattimento.</b> Prima di agire in una locazione con avversari stima lo scontro a valore atteso
 * ({@link Valutatore}): per ogni personaggio vivo calcola quanti danni fa per round (probabilità di colpire di
 * CalcolatoreCombattimento per la media di qualche tiro di calcolaDannoRisultante, in mischia, col dardo arcano o
 * con le pergamene) e quanti ne riceve, e ne ricava il <i>carico del capo</i>: la frazione della salute del capo
 * (più le pozioni) che lo scontro dovrebbe costargli, dopo che i compagni hanno fatto la loro parte (i compagni
 * combattono per primi: se muore il capo la partita è persa). Poi:
 * <ul>
 * <li>carico basso: combatte, col personaggio che rischia meno, preferendo i compagni al capo;</li>
 * <li>altrimenti prova a corrompere (se gli avversari lo permettono e ci sono le monete) o a fare amicizia (se
 * c'è qualcuno con abbastanza carisma);</li>
 * <li>se lo scontro resta sfavorevole fugge, ma solo se la fuga (da 50 a 100 danni a ogni vivo, vedi
 * GruppoGiocatore.fugge) lascia più speranze del combattimento; altrimenti combatte comunque.</li>
 * </ul>
 * Chi combatte usa mischia, dardo arcano o pergamene di danno, secondo cosa fa più danni attesi. Durante la
 * mischia il capo beve una pozione sotto un terzo della salute, un compagno ferito lascia il posto a un altro, e
 * se la stima rifatta a ogni round diventa disperata e la fuga è sopportabile si fugge.
 * <p>
 * <b>Fuori dal combattimento</b> (scelta della direzione): spende i punti abilità (Forza, Destrezza e
 * Costituzione a turno, Intelligenza per i maghi), risuscita i compagni con la Resurrezione, beve pozioni
 * (il capo sotto il 55% della salute, i compagni sotto il 40% se ne restano), si accampa di notte quando il
 * gioco lo permette (almeno due vivi).
 * <p>
 * <b>Spostamenti.</b> Sulla mappa conosciuta cerca il percorso più sicuro con Dijkstra: ogni spostamento (da 1
 * a 5 passi, fermandosi sulla prima casella che non è bosco o radura, come GruppoGiocatore.getMaxPassi*) costa
 * il rischio della casella d'arrivo (bosco e radura hanno quasi sempre mostri, la palude mai, locande e città
 * sono sicure, i templi sono pieni di viverne, i castelli si evitano). La meta, in ordine:
 * <ol>
 * <li>una locanda o una città conosciuta, se il capo è ferito (sotto il 60% senza pozioni) o stanco, e ci sono
 * le monete per il pasto;</li>
 * <li>una città conosciuta, se ci sono monete da spendere e servono pozioni o la mappa;</li>
 * <li>una locanda conosciuta mai visitata, per reclutare, se il gruppo non è pieno;</li>
 * <li>altrimenti esplora: lo spostamento che svela più caselle sconosciute per unità di rischio.</li>
 * </ol>
 * <b>In città</b> va dall'alchimista (la mappa della Foresta una volta, pozioni della salute, una Resurrezione
 * se c'è chi può lanciarla, pergamene di danno se nel gruppo c'è un mago, tenendo da parte le monete per
 * mangiare e dormire), poi alla locanda, poi esce. <b>In locanda</b> mangia, recluta chi si offre, pernotta se
 * è stanco, ferito o è sera. Accetta le offerte di chi ha corrotto o con cui ha fatto amicizia, e raccoglie
 * gli oggetti dandoli al capo quando il gioco chiede a chi.
 */
final class GiocatoreAutomatico {

	/**
	 * Gli incantesimi di danno che il giocatore usa e compra (niente Morte, che può ritorcersi contro)
	 */
	private static final ClasseIncantesimo[] INCANTESIMI_DI_DANNO = {ClasseIncantesimo.FULMINE, ClasseIncantesimo.FUOCO,
			ClasseIncantesimo.TERRA, ClasseIncantesimo.GELO, ClasseIncantesimo.VELENO, ClasseIncantesimo.ACQUA,
			ClasseIncantesimo.ARIA};
	private static final Comando[] DIREZIONI = {Comando.NORD, Comando.EST, Comando.SUD, Comando.OVEST};
	private static final int[] DX = {0, 1, 0, -1};
	private static final int[] DY = {-1, 0, 1, 0};
	/**
	 * Quante pozioni della salute si contano come salute aggiuntiva nella stima di uno scontro
	 */
	private static final int POZIONI_NELLA_STIMA = 3;

	/**
	 * Cosa fare in una locazione con avversari
	 */
	enum Decisione {
		COMBATTI, CORROMPI, AMICIZIA, FUGGI
	}

	/**
	 * A cosa serve la prossima scelta di un personaggio (SCELTA_MANUALE_PERSONAGGIO)
	 */
	private enum Attesa {
		NESSUNA, COMBATTENTE, FORMULANTE, BEVITORE, CORRUTTORE, AMICO
	}

	private final PartitaDiTest partita;
	private final Random random;

	// Contatori letti dal riepilogo
	int pozioniComprate;
	int pergameneComprate;
	int moneteNeiNegozi;
	int mappeComprate;
	int fughe;
	int corruzioni;
	int amicizie;
	int resurrezioni;
	int moneteDaVendite;

	// Stato per la locazione corrente (si azzera quando cambia l'istanza della locazione)
	private Locazione locazione;
	private Valutatore valutatore;
	private Personaggio combattente;
	private Personaggio scelto;
	private Attesa attesa = Attesa.NESSUNA;
	private Comando incantesimoScelto;
	private boolean fugaChiesta;
	private boolean negoziFatti;
	private boolean armaioloFatto;
	private boolean venditoreFatto;
	private Comando negozioAperto;
	private boolean locandaFatta;
	private int incantesimiFalliti;

	// Stato per la partita
	private boolean mappaComprata;
	private Comando passiPianificati;

	GiocatoreAutomatico(PartitaDiTest partita, Random random) {
		this.partita = partita;
		this.random = random;
	}

	/**
	 * La prossima mossa: un comando, oppure null per lasciar passare il tempo (un impulso del temporizzatore,
	 * come i round della mischia). Può fare acquisti prima di rispondere (vedi {@link #acquistaDallAlchimista}).
	 */
	Comando scegli() {
		Stato stato = partita.stato();
		Collection<Comando> comandi = partita.comandiDisponibili();
		GruppoGiocatore gruppo = partita.gruppo();
		aggiornaLocazione(gruppo);

		switch (stato) {
			case SCELTA_DIREZIONE:
				return scegliFuoriDalleLocazioni(gruppo, comandi);
			case SCELTA_PASSI:
				return scegliPassi(comandi);
			case IN_LOCAZIONE:
				return scegliInLocazione(gruppo, comandi);
			case IN_COMBATTIMENTO:
				return scegliInCombattimento(gruppo, comandi);
			case SCELTA_MANUALE_PERSONAGGIO:
				return scegliPersonaggio(gruppo, comandi);
			case INCANTESIMO_SCELTO:
			case SCELTA_INCANTESIMO_DA_LANCIARE:
				return scegliIncantesimo(comandi);
			case ATTESA_SI_NO:
				// Solo la conferma della fuga passa di qui
				if (fugaChiesta && comandi.contains(Comando.SI)) {
					fugaChiesta = false;
					fughe++;
					return Comando.SI;
				}
				return comandi.contains(Comando.NO) ? Comando.NO : primoDi(comandi);
			case SCELTA_DESTINATARIO_OGGETTO:
				// L'oggetto al capo se può prenderlo, altrimenti al primo che può
				if (comandi.contains(Comando.PERSONAGGIO_1)) {
					return Comando.PERSONAGGIO_1;
				}
				return primoPersonaggio(comandi, Comando.GRUPPO);
			case SCELTA_FORMULANTE_RESURREZIONE:
			case SCELTA_BERSAGLIO_RESURREZIONE:
				return primoPersonaggio(comandi, Comando.ANNULLA);
			default:
				return ripiego(comandi);
		}
	}

	// ---------------------------------------------------------------------------------------------------------
	// Fuori dalle locazioni: cure, riposo, spostamenti

	private Comando scegliFuoriDalleLocazioni(GruppoGiocatore gruppo, Collection<Comando> comandi) {
		spendiPuntiAbilita(gruppo);
		Personaggio capo = gruppo.getCapo();
		if (comandi.contains(Comando.RESURREZIONE)) {
			resurrezioni++;
			return Comando.RESURREZIONE;
		}
		// Pozioni: prima il capo, poi i compagni se ne restano per il capo
		Personaggio daCurare = null;
		if (frazioneSalute(capo) < 0.55) {
			daCurare = capo;
		} else if (gruppo.getPozioniSalute() + gruppo.getPozioniSaluteGrande() > 2) {
			for (Personaggio p : gruppo.getPersonaggiVivi()) {
				if (p != capo && frazioneSalute(p) < 0.4) {
					daCurare = p;
					break;
				}
			}
		}
		if (daCurare != null) {
			Comando pozione = pozioneSalutePer(daCurare, comandi);
			if (pozione != null) {
				preparaScelta(Attesa.BEVITORE, daCurare);
				return pozione;
			}
		}
		for (Personaggio p : gruppo.getPersonaggiVivi()) {
			if (usaIncantesimi(p) && p.getMagia() < p.getMagiaMassima() / 4) {
				Comando pozione = comandi.contains(Comando.POZIONE_MAGIA) ? Comando.POZIONE_MAGIA
						: comandi.contains(Comando.POZIONE_MAGIA_GRANDE) ? Comando.POZIONE_MAGIA_GRANDE : null;
				if (pozione != null) {
					preparaScelta(Attesa.BEVITORE, p);
					return pozione;
				}
			}
		}
		if (comandi.contains(Comando.ACCAMPAMENTO)) {
			return Comando.ACCAMPAMENTO;
		}
		return spostati(gruppo, comandi);
	}

	private Comando pozioneSalutePer(Personaggio p, Collection<Comando> comandi) {
		int mancante = p.getSaluteMassima() - p.getSalute();
		// Si guardano anche le pozioni rimaste: in mischia i comandi non si aggiornano dopo una pozione, e il
		// gioco la lascerebbe bere anche senza (vedi il resoconto)
		GruppoGiocatore gruppo = partita.gruppo();
		boolean grande = comandi.contains(Comando.POZIONE_SALUTE_GRANDE) && gruppo.getPozioniSaluteGrande() > 0;
		boolean piccola = comandi.contains(Comando.POZIONE_SALUTE) && gruppo.getPozioniSalute() > 0;
		if (grande && (mancante >= Costanti.RECUPERO_DA_POZIONE_SALUTE_GRANDE || !piccola)) {
			return Comando.POZIONE_SALUTE_GRANDE;
		}
		return piccola ? Comando.POZIONE_SALUTE : null;
	}

	/**
	 * Come fa la UI dalla schermata dell'inventario (DisplayableCanvasInventario): un punto alla volta.
	 */
	private static void spendiPuntiAbilita(GruppoGiocatore gruppo) {
		for (Personaggio p : gruppo.getPersonaggiVivi()) {
			int giro = 0;
			while (p.getPuntiAbilitaDisponibili() > 0 && giro++ < 20) {
				TipoAttributo attributo;
				if (p.getMoltiplicatoreDanniMagici() >= 1.4d) {
					attributo = TipoAttributo.INTELLIGENZA;
				} else {
					TipoAttributo[] fisici = {TipoAttributo.FORZA, TipoAttributo.DESTREZZA, TipoAttributo.COSTITUZIONE};
					attributo = fisici[(p.getLivello() + p.getPuntiAbilitaDisponibili()) % fisici.length];
				}
				p.spendiPuntoAbilita(attributo);
			}
		}
	}

	private Comando scegliPassi(Collection<Comando> comandi) {
		Comando passi = passiPianificati;
		passiPianificati = null;
		if (passi != null && comandi.contains(passi)) {
			return passi;
		}
		for (int i = Comando.MAX_MOVIMENTO - 1; i >= 0; i--) {
			Comando numero = Comando.values()[Comando.NUMERO_1.ordinal() + i];
			if (comandi.contains(numero)) {
				return numero;
			}
		}
		return comandi.contains(Comando.ANNULLA) ? Comando.ANNULLA : primoDi(comandi);
	}

	/**
	 * Sceglie la meta e fa il primo spostamento del percorso più sicuro per arrivarci.
	 */
	private Comando spostati(GruppoGiocatore gruppo, Collection<Comando> comandi) {
		int x = gruppo.getX();
		int y = gruppo.getY();
		int[] maxQui = {gruppo.getMaxPassiNord(), gruppo.getMaxPassiEst(), gruppo.getMaxPassiSud(), gruppo.getMaxPassiOvest()};
		List<CoordinateMD> mete = scegliMete(gruppo);
		Mossa mossa = null;
		if (!mete.isEmpty()) {
			mossa = Mappa.primaMossaVerso(x, y, maxQui, mete);
		}
		if (mossa == null) {
			mossa = esplora(x, y, maxQui);
		}
		if (mossa == null || !comandi.contains(DIREZIONI[mossa.direzione])) {
			List<Comando> possibili = new ArrayList<>();
			for (Comando direzione : DIREZIONI) {
				if (comandi.contains(direzione)) {
					possibili.add(direzione);
				}
			}
			if (possibili.isEmpty()) {
				return ripiego(comandi);
			}
			passiPianificati = Comando.NUMERO_1;
			return possibili.get(random.nextInt(possibili.size()));
		}
		passiPianificati = Comando.values()[Comando.NUMERO_1.ordinal() + mossa.passi - 1];
		return DIREZIONI[mossa.direzione];
	}

	/**
	 * Le caselle verso cui andare, secondo i bisogni del gruppo (vuota: si esplora).
	 */
	private List<CoordinateMD> scegliMete(GruppoGiocatore gruppo) {
		Personaggio capo = gruppo.getCapo();
		int vivi = gruppo.getNumeroPersonaggiVivi();
		int monete = gruppo.getMonete();
		int pozioni = gruppo.getPozioniSalute() + gruppo.getPozioniSaluteGrande();
		CoordinateMD qui = gruppo.getCoordinate();
		List<CoordinateMD> citta = new ArrayList<>();
		List<CoordinateMD> locande = new ArrayList<>();
		List<CoordinateMD> locandeNuove = new ArrayList<>();
		for (int x = 0; x < Foresta.getDimensioneX(); x++) {
			for (int y = 0; y < Foresta.getDimensioneY(); y++) {
				ClassiLocazione classe = Mappa.nota(x, y);
				CoordinateMD c = new CoordinateMD(x, y);
				if (classe == null || c.equals(qui)) {
					continue;
				}
				if (classe.getTipoLocazione() == TipoLocazione.CITTA) {
					citta.add(c);
				} else if (classe == ClassiLocazione.LOCANDA) {
					locande.add(c);
				}
				if ((classe == ClassiLocazione.LOCANDA || classe.getTipoLocazione() == TipoLocazione.CITTA)
						&& Foresta.getLocazioneMD(c).ottieniProprieta(Locanda.LOCANDA_VISITATA) == null) {
					locandeNuove.add(c);
				}
			}
		}
		boolean ferito = frazioneSalute(capo) < (pozioni > 0 ? 0.35 : 0.6)
				|| gruppo.getPersonaggiVivi().stream().anyMatch(p -> frazioneSalute(p) < 0.3);
		// Stanchezza: -2 all'attacco e alla difesa per punto (CalcolatoreCombattimento); in due o più ci si
		// accampa la notte, da soli si riposa solo in locanda
		boolean stanco = capo.getStanchezza() >= (vivi > 1 ? 9 : 5);
		if ((ferito || stanco) && monete >= Costanti.COSTO_PASTO) {
			List<CoordinateMD> mete = new ArrayList<>(locande);
			mete.addAll(citta);
			if (!mete.isEmpty()) {
				return mete;
			}
		}
		boolean servonoAcquisti = pozioni < 2 + vivi || (!mappaComprata && monete >= 70);
		boolean daVendere = haDaVendere(gruppo, TipoNegozio.ARMAIOLO) || haDaVendere(gruppo, TipoNegozio.VENDITORE_DI_PERGAMENE);
		if (((servonoAcquisti && monete >= 25) || daVendere) && !citta.isEmpty()) {
			return citta;
		}
		if (gruppo.getNumeroPersonaggi() < Costanti.MAX_PERSONAGGI_GRUPPO_GIOCATORE
				&& monete >= Costanti.COSTO_PASTO * (vivi + 1) && !locandeNuove.isEmpty()) {
			return locandeNuove;
		}
		return Collections.emptyList();
	}

	/**
	 * Lo spostamento che svela più caselle sconosciute per unità di rischio; se da qui non se ne svelano, il
	 * primo passo verso la più vicina casella da cui se ne svelerebbero; se la mappa è tutta nota, uno
	 * spostamento a caso fra i meno rischiosi.
	 */
	private Mossa esplora(int x, int y, int[] maxQui) {
		Mossa migliore = null;
		double valoreMigliore = 0;
		List<Mossa> sicure = new ArrayList<>();
		for (int d = 0; d < 4; d++) {
			for (int k = 1; k <= maxQui[d]; k++) {
				int nx = x + DX[d] * k;
				int ny = y + DY[d] * k;
				double rischio = Mappa.rischio(Mappa.nota(nx, ny), false);
				if (Double.isInfinite(rischio)) {
					continue;
				}
				int svelate = Mappa.sconosciuteIntorno(nx, ny);
				double valore = svelate / (rischio + 0.2d) + random.nextDouble() * 0.01d;
				if (svelate > 0 && valore > valoreMigliore) {
					valoreMigliore = valore;
					migliore = new Mossa(d, k);
				}
				if (rischio <= 1.0d) {
					sicure.add(new Mossa(d, k));
				}
			}
		}
		if (migliore != null) {
			return migliore;
		}
		List<CoordinateMD> frontiera = new ArrayList<>();
		for (int fx = 0; fx < Foresta.getDimensioneX(); fx++) {
			for (int fy = 0; fy < Foresta.getDimensioneY(); fy++) {
				if (Foresta.isLocazioneConosciuta(new CoordinateMD(fx, fy)) && Mappa.sconosciuteIntorno(fx, fy) > 0) {
					frontiera.add(new CoordinateMD(fx, fy));
				}
			}
		}
		if (!frontiera.isEmpty()) {
			Mossa verso = Mappa.primaMossaVerso(x, y, maxQui, frontiera);
			if (verso != null) {
				return verso;
			}
		}
		if (sicure.isEmpty()) {
			return null;
		}
		// Mappa tutta nota: si gira a caso preferendo le caselle senza mostri
		sicure.sort((a, b) -> Double.compare(
				Mappa.rischio(Mappa.nota(x + DX[a.direzione] * a.passi, y + DY[a.direzione] * a.passi), false),
				Mappa.rischio(Mappa.nota(x + DX[b.direzione] * b.passi, y + DY[b.direzione] * b.passi), false)));
		return sicure.get(random.nextInt(Math.min(3, sicure.size())));
	}

	// ---------------------------------------------------------------------------------------------------------
	// Nelle locazioni

	private Comando scegliInLocazione(GruppoGiocatore gruppo, Collection<Comando> comandi) {
		if (comandi.contains(Comando.ESCI_DA_CITTA)) {
			return scegliInPiazza(gruppo, comandi);
		}
		if (comandi.size() == 1 && comandi.contains(Comando.ANNULLA)) {
			// In una bottega: dall'alchimista si compra, dall'armaiolo e dal venditore di pergamene si vende
			if (negozioAperto == Comando.ALCHIMISTA) {
				acquistaDallAlchimista(gruppo);
			} else if (negozioAperto == Comando.ARMAIOLO) {
				vendi(gruppo, TipoNegozio.ARMAIOLO);
			} else if (negozioAperto == Comando.VENDITORE_DI_PERGAMENE) {
				vendi(gruppo, TipoNegozio.VENDITORE_DI_PERGAMENE);
			}
			negozioAperto = null;
			return Comando.ANNULLA;
		}
		if (comandi.contains(Comando.PERGAMENA) && comandi.size() == 1) {
			// La fiaba del cantastorie in locanda
			return Comando.PERGAMENA;
		}
		if (comandi.contains(Comando.SI) && comandi.contains(Comando.NO)) {
			return rispostaSiNo(gruppo);
		}
		if (!comandi.isEmpty() && comandi.stream().allMatch(Comando::isPersonaggio)) {
			// In locanda senza monete per tutti: mangia il più malconcio, di preferenza il capo
			return comandoPer(gruppo, piuBisognoso(gruppo));
		}
		if (comandi.contains(Comando.COMBATTIMENTO) || comandi.contains(Comando.FUGA)) {
			return scegliControAvversari(gruppo, comandi, false);
		}
		return ripiego(comandi);
	}

	private Comando scegliInPiazza(GruppoGiocatore gruppo, Collection<Comando> comandi) {
		// Prima si vende quel che nessuno usa, poi si compra
		if (!armaioloFatto && comandi.contains(Comando.ARMAIOLO) && haDaVendere(gruppo, TipoNegozio.ARMAIOLO)) {
			armaioloFatto = true;
			negozioAperto = Comando.ARMAIOLO;
			return Comando.ARMAIOLO;
		}
		if (!venditoreFatto && comandi.contains(Comando.VENDITORE_DI_PERGAMENE)
				&& haDaVendere(gruppo, TipoNegozio.VENDITORE_DI_PERGAMENE)) {
			venditoreFatto = true;
			negozioAperto = Comando.VENDITORE_DI_PERGAMENE;
			return Comando.VENDITORE_DI_PERGAMENE;
		}
		if (!negoziFatti && comandi.contains(Comando.ALCHIMISTA)) {
			negoziFatti = true;
			negozioAperto = Comando.ALCHIMISTA;
			return Comando.ALCHIMISTA;
		}
		if (!locandaFatta && comandi.contains(Comando.LOCANDA) && gruppo.getMonete() >= Costanti.COSTO_PASTO) {
			locandaFatta = true;
			return Comando.LOCANDA;
		}
		return Comando.ESCI_DA_CITTA;
	}

	private Comando rispostaSiNo(GruppoGiocatore gruppo) {
		String testo = partita.ultimoTesto();
		if (testo.contains("pernottare")) {
			Personaggio capo = gruppo.getCapo();
			int ora = LineaTemporale.getOra();
			int costo = Costanti.COSTO_PERNOTTAMENTO * gruppo.getNumeroPersonaggiVivi();
			boolean serve = capo.getStanchezza() >= 2 || ora >= 18 || ora < 6
					|| gruppo.getPersonaggiVivi().stream().anyMatch(p -> frazioneSalute(p) < 0.9);
			return serve && gruppo.getMonete() - costo >= Costanti.COSTO_PASTO ? Comando.SI : Comando.NO;
		}
		if (partita.ultimiTesti().contains("ad accompagnare")) {
			// Aiuto gratuito o mercenario: accettarli fa andare in errore il gioco (PersonaggioBase.setTempo su un
			// personaggio senza TEMPO), quindi si rifiutano
			return Comando.NO;
		}
		// Reclutamento in locanda e le altre offerte dopo corruzione o amicizia: si accetta
		return Comando.SI;
	}

	/**
	 * Cosa fare contro gli avversari, fuori dalla mischia (appena arrivati, o fra un'azione e l'altra).
	 */
	private Comando scegliControAvversari(GruppoGiocatore gruppo, Collection<Comando> comandi, boolean inMischia) {
		List<Personaggio> avversari = GruppoAvversario.getIstanza().getPersonaggiVivi();
		if (avversari.isEmpty()) {
			return ripiego(comandi);
		}
		Personaggio capo = gruppo.getCapo();
		// Prima di tutto il capo non deve morire
		if (frazioneSalute(capo) < 0.35) {
			Comando pozione = pozioneSalutePer(capo, comandi);
			if (pozione != null) {
				preparaScelta(Attesa.BEVITORE, capo);
				return pozione;
			}
		}
		Stima stima = valutatore.stima(gruppo, avversari);
		traccia(stima);
		Decisione decisione = decidi(gruppo, comandi, stima);
		switch (decisione) {
			case CORROMPI:
				corruzioni++;
				preparaScelta(Attesa.CORRUTTORE, capo);
				return Comando.CORRUZIONE;
			case AMICIZIA:
				amicizie++;
				preparaScelta(Attesa.AMICO, piuCarismatico(gruppo));
				return Comando.AMICIZIA;
			case FUGGI:
				fugaChiesta = true;
				return Comando.FUGA;
			default:
				break;
		}
		Personaggio attore = stima.attore;
		if (stima.incantesimo != null && comandi.contains(Comando.INCANTESIMO) && incantesimiFalliti < 3) {
			incantesimoScelto = stima.incantesimo;
			preparaScelta(Attesa.FORMULANTE, attore);
			return Comando.INCANTESIMO;
		}
		if (comandi.contains(Comando.COMBATTIMENTO)) {
			preparaScelta(Attesa.COMBATTENTE, attore);
			combattente = attore;
			return Comando.COMBATTIMENTO;
		}
		return inMischia ? null : ripiego(comandi);
	}

	/**
	 * Combattere, corrompere, fare amicizia o fuggire, secondo la stima dello scontro.
	 */
	Decisione decidi(GruppoGiocatore gruppo, Collection<Comando> comandi, Stima stima) {
		double carico = stima.caricoCapo;
		if (carico < 0.5 && stima.compagniPersi == 0) {
			return Decisione.COMBATTI;
		}
		if (comandi.contains(Comando.CORRUZIONE) && gruppo.getMonete() >= gruppo.getNumeroPersonaggi() * 2) {
			return Decisione.CORROMPI;
		}
		if (comandi.contains(Comando.AMICIZIA) && carico >= 0.7 && probabilitaAmicizia(gruppo) >= 0.5) {
			return Decisione.AMICIZIA;
		}
		if (carico < 0.9 || !comandi.contains(Comando.FUGA)) {
			return Decisione.COMBATTI;
		}
		return convieneFuggire(gruppo.getCapo(), carico) ? Decisione.FUGGI : Decisione.COMBATTI;
	}

	/**
	 * La fuga costa da 50 a 100 punti di salute a ogni vivo e fino a metà delle risorse: conviene solo se lo
	 * scontro è davvero perso e il capo la regge.
	 */
	static boolean convieneFuggire(Personaggio capo, double carico) {
		double vittoria = probabilitaVittoria(carico);
		double fuga = sopravvivenzaAllaFuga(capo);
		return fuga >= 0.9d ? vittoria < 0.3d : fuga > vittoria + 0.3d;
	}

	/**
	 * Dal carico atteso del capo (1 = lo scontro gli costa tutta la salute e le pozioni) a una probabilità di
	 * vincerlo, a occhio: 0.7 → 77%, 1 → 50%, 1.4 → 17%.
	 */
	static double probabilitaVittoria(double carico) {
		return 1.0d / (1.0d + Math.exp(4.0d * (carico - 1.0d)));
	}

	/**
	 * La fuga toglie da 50 a 100 punti di salute (PersonaggioBase.fugge)
	 */
	static double sopravvivenzaAllaFuga(Personaggio capo) {
		int salute = capo.getSalute();
		if (salute > 100) {
			return 1;
		}
		if (salute <= 50) {
			return 0;
		}
		return (salute - 50) / 51.0d;
	}

	private static double probabilitaAmicizia(GruppoGiocatore gruppo) {
		// Riesce se il carisma supera un tiro da 1 a 12 (LocazioneBase, CHI_FA_AMICIZIA)
		return Math.min(12, Math.max(0, piuCarismatico(gruppo).getCarisma() - 1)) / 12.0d;
	}

	private static Personaggio piuCarismatico(GruppoGiocatore gruppo) {
		Personaggio migliore = gruppo.getCapo();
		for (Personaggio p : gruppo.getPersonaggiVivi()) {
			if (p.getCarisma() > migliore.getCarisma()) {
				migliore = p;
			}
		}
		return migliore;
	}

	private Comando scegliInCombattimento(GruppoGiocatore gruppo, Collection<Comando> comandi) {
		List<Personaggio> avversari = GruppoAvversario.getIstanza().getPersonaggiVivi();
		Personaggio capo = gruppo.getCapo();
		if (avversari.isEmpty()) {
			return null;
		}
		if (combattente == null || !combattente.isVivo()) {
			combattente = capo;
		}
		// Il capo sotto un terzo della salute beve (da solo la pozione non costa il round)
		if (frazioneSalute(capo) < 0.34) {
			Comando pozione = pozioneSalutePer(capo, comandi);
			if (pozione != null) {
				preparaScelta(Attesa.BEVITORE, capo);
				return pozione;
			}
		}
		Stima stima = valutatore.stima(gruppo, avversari);
		traccia(stima);
		// Stima disperata: si fugge se la fuga lascia più speranze
		if (comandi.contains(Comando.FUGA) && stima.caricoCapo >= 1.2 && convieneFuggire(capo, stima.caricoCapo)) {
			fugaChiesta = true;
			return Comando.FUGA;
		}
		// Chi combatte è malconcio e c'è di meglio: cambio
		Double caricoCombattente = stima.carichi.get(combattente);
		boolean malconcio = frazioneSalute(combattente) < 0.35 || caricoCombattente == null
				|| Valutatore.rischioPesato(combattente, capo, stima.carichi)
				> 1.5d * Valutatore.rischioPesato(stima.attore, capo, stima.carichi);
		if (comandi.contains(Comando.COMBATTIMENTO) && stima.attore != combattente && malconcio) {
			preparaScelta(Attesa.COMBATTENTE, stima.attore);
			combattente = stima.attore;
			return Comando.COMBATTIMENTO;
		}
		// Un mago non resta in mischia se ha di meglio da fare
		if (stima.incantesimo != null && comandi.contains(Comando.INCANTESIMO) && incantesimiFalliti < 3) {
			incantesimoScelto = stima.incantesimo;
			preparaScelta(Attesa.FORMULANTE, stima.attore);
			return Comando.INCANTESIMO;
		}
		return null;
	}

	private Comando scegliPersonaggio(GruppoGiocatore gruppo, Collection<Comando> comandi) {
		Attesa perCosa = attesa;
		Personaggio chi = scelto;
		attesa = Attesa.NESSUNA;
		scelto = null;
		if (chi != null) {
			Comando comando = comandoPer(gruppo, chi);
			if (comando != null && comandi.contains(comando)) {
				return comando;
			}
		}
		if (perCosa == Attesa.NESSUNA && comandi.contains(Comando.ANNULLA)) {
			return Comando.ANNULLA;
		}
		return primoPersonaggio(comandi, Comando.ANNULLA);
	}

	private Comando scegliIncantesimo(Collection<Comando> comandi) {
		Comando voluto = incantesimoScelto;
		incantesimoScelto = null;
		if (voluto != null && comandi.contains(voluto)) {
			return voluto;
		}
		incantesimiFalliti++;
		return comandi.contains(Comando.NO_INCANTESIMO) ? Comando.NO_INCANTESIMO : primoDi(comandi);
	}

	// ---------------------------------------------------------------------------------------------------------
	// Acquisti

	/**
	 * Gli acquisti dall'alchimista, come il doppio click della UI (DisplayableCanvasScambiatoreConsumabili):
	 * ComandoAcquistoConsumabile col costo di listino, che il gruppo sconta con la contrattazione.
	 */
	private void acquistaDallAlchimista(GruppoGiocatore gruppo) {
		int vivi = gruppo.getNumeroPersonaggiVivi();
		// Le monete per mangiare e dormire alla locanda della città
		int riserva = (Costanti.COSTO_PASTO + Costanti.COSTO_PERNOTTAMENTO) * vivi;
		if (!mappaComprata && puoSpendere(gruppo, Costanti.COSTO_MAPPA_DELLA_FORESTA, riserva + 40)) {
			if (compra(gruppo, TipoConsumabile.MAPPA_COMPLETA_FORESTA, null, null, Costanti.COSTO_MAPPA_DELLA_FORESTA)) {
				mappaComprata = true;
				mappeComprate++;
			}
		}
		int obiettivoPozioni = 3 + 2 * vivi;
		while (gruppo.getPozioniSalute() < obiettivoPozioni
				&& puoSpendere(gruppo, Costanti.COSTO_POZIONE_SALUTE, riserva)
				&& compra(gruppo, TipoConsumabile.POZIONE_SALUTE, null, null, Costanti.COSTO_POZIONE_SALUTE)) {
			pozioniComprate++;
		}
		boolean qualcunoPuoRisuscitare = gruppo.getPersonaggiVivi().stream()
				.anyMatch(p -> p.getMagiaMassima() >= ClasseIncantesimo.RESURREZIONE.getCostoLancio());
		if (gruppo.getNumeroPersonaggi() > 1 && qualcunoPuoRisuscitare
				&& gruppo.getIncantesimi(ClasseIncantesimo.RESURREZIONE) == 0
				&& puoSpendere(gruppo, ClasseIncantesimo.RESURREZIONE.getCostoAcquisto(), riserva)) {
			if (compra(gruppo, TipoConsumabile.INCANTESIMO, ClasseIncantesimo.RESURREZIONE, null,
					ClasseIncantesimo.RESURREZIONE.getCostoAcquisto())) {
				pergameneComprate++;
			}
		}
		// Pergamene di danno per chi sa usarle: le più convenienti per danno e costo
		boolean mago = gruppo.getPersonaggiVivi().stream().anyMatch(GiocatoreAutomatico::usaIncantesimi);
		if (mago) {
			ClasseIncantesimo[] daComprare = {ClasseIncantesimo.FUOCO, ClasseIncantesimo.TERRA, ClasseIncantesimo.GELO,
					ClasseIncantesimo.VELENO};
			int giro = 0;
			while (pergameneDiDanno(gruppo) < 8 && giro < 8) {
				ClasseIncantesimo classe = daComprare[giro++ % daComprare.length];
				if (!puoSpendere(gruppo, classe.getCostoAcquisto(), riserva + 10)
						|| !compra(gruppo, TipoConsumabile.INCANTESIMO, classe, null, classe.getCostoAcquisto())) {
					break;
				}
				pergameneComprate++;
			}
			if (gruppo.getPozioniMagia() < 2 && puoSpendere(gruppo, Costanti.COSTO_POZIONE_MAGIA, riserva + 20)
					&& compra(gruppo, TipoConsumabile.POZIONE_MAGIA, null, null, Costanti.COSTO_POZIONE_MAGIA)) {
				pozioniComprate++;
			}
		}
		// Con le monete che avanzano, qualche pozione in più
		while (gruppo.getPozioniSalute() < obiettivoPozioni + 4
				&& puoSpendere(gruppo, Costanti.COSTO_POZIONE_SALUTE, riserva + 30)
				&& compra(gruppo, TipoConsumabile.POZIONE_SALUTE, null, null, Costanti.COSTO_POZIONE_SALUTE)) {
			pozioniComprate++;
		}
	}

	private static boolean haDaVendere(GruppoGiocatore gruppo, TipoNegozio negozio) {
		return gruppo.getInventario().stream().anyMatch(a -> negozio.tratta(a.getTipo()));
	}

	/**
	 * Vende al negozio aperto tutti gli artefatti dell'inventario del gruppo che tratta (quelli che nessuno ha
	 * potuto equipaggiare), come il trascinamento della UI (AutomaAcquistiArtefatti).
	 */
	private void vendi(GruppoGiocatore gruppo, TipoNegozio negozio) {
		AutomaAcquistiArtefatti bottega = partita.eventi().ultimo(ComandoAperturaInventarioCommerciante.class)
				.getAutomaAcquistiArtefatti();
		for (Artefatto artefatto : new ArrayList<>(gruppo.getInventario())) {
			if (negozio.tratta(artefatto.getTipo())) {
				int prima = gruppo.getMonete();
				partita.pubblica(new ComandoVenditaArtefatto(bottega.getParteAttiva(), bottega.getParteRemota(), artefatto));
				moneteDaVendite += Math.max(0, gruppo.getMonete() - prima);
			}
		}
	}

	private static boolean puoSpendere(GruppoGiocatore gruppo, int costo, int riserva) {
		return gruppo.getMonete() - gruppo.prezzoAcquisto(costo) >= riserva;
	}

	private boolean compra(GruppoGiocatore gruppo, TipoConsumabile tipo, ClasseIncantesimo classe, Personaggio personaggio, int costo) {
		int prima = gruppo.getMonete();
		partita.pubblica(new ComandoAcquistoConsumabile(tipo, classe, personaggio, costo));
		int spese = prima - gruppo.getMonete();
		moneteNeiNegozi += Math.max(0, spese);
		return spese > 0 || (spese == 0 && gruppo.prezzoAcquisto(costo) == 0);
	}

	static int pergameneDiDanno(GruppoGiocatore gruppo) {
		return Arrays.stream(INCANTESIMI_DI_DANNO).mapToInt(gruppo::getIncantesimi).sum();
	}

	/**
	 * Chi lancia volentieri incantesimi: i personaggi con il moltiplicatore dei danni magici alto (Mago, Elfo)
	 */
	static boolean usaIncantesimi(Personaggio p) {
		return p.getMoltiplicatoreDanniMagici() >= 1.4d;
	}

	// ---------------------------------------------------------------------------------------------------------
	// Utilità

	private void aggiornaLocazione(GruppoGiocatore gruppo) {
		Locazione corrente = gruppo.getLocazioneCorrente();
		if (corrente != locazione) {
			locazione = corrente;
			valutatore = new Valutatore(gruppo);
			combattente = null;
			fugaChiesta = false;
			negoziFatti = false;
			armaioloFatto = false;
			venditoreFatto = false;
			negozioAperto = null;
			locandaFatta = false;
			incantesimiFalliti = 0;
		}
		if (valutatore == null) {
			valutatore = new Valutatore(gruppo);
		}
	}

	/**
	 * Dove scrivere le stime degli scontri mentre si mette a punto la strategia (null: da nessuna parte)
	 */
	static java.io.PrintStream traccia;

	private void traccia(Stima stima) {
		if (traccia != null) {
			traccia.printf("      stima: attore %s azione %s caricoCapo %.2f compagniPersi %d dettaglio %s%n",
					stima.attore.getClasse(), stima.incantesimo, stima.caricoCapo, stima.compagniPersi, stima.dettaglio);
		}
	}

	private void preparaScelta(Attesa perCosa, Personaggio chi) {
		attesa = perCosa;
		scelto = chi;
	}

	static double frazioneSalute(Personaggio p) {
		return p.getSaluteMassima() <= 0 ? 0 : (double) p.getSalute() / p.getSaluteMassima();
	}

	private static Personaggio piuBisognoso(GruppoGiocatore gruppo) {
		Personaggio capo = gruppo.getCapo();
		if (frazioneSalute(capo) < 0.8) {
			return capo;
		}
		Personaggio migliore = capo;
		for (Personaggio p : gruppo.getPersonaggiVivi()) {
			if (frazioneSalute(p) < frazioneSalute(migliore)) {
				migliore = p;
			}
		}
		return migliore;
	}

	private static Comando comandoPer(GruppoGiocatore gruppo, Personaggio p) {
		int indice = gruppo.getPersonaggi().indexOf(p);
		return indice < 0 ? null : Comando.ofPersonaggio(indice);
	}

	private static Comando primoPersonaggio(Collection<Comando> comandi, Comando altrimenti) {
		for (Comando comando : comandi) {
			if (comando.isPersonaggio()) {
				return comando;
			}
		}
		return comandi.contains(altrimenti) ? altrimenti : primoDi(comandi);
	}

	private static Comando primoDi(Collection<Comando> comandi) {
		return comandi.isEmpty() ? null : comandi.iterator().next();
	}

	/**
	 * Per gli stati che il giocatore non gestisce in modo particolare
	 */
	private static Comando ripiego(Collection<Comando> comandi) {
		for (Comando preferito : new Comando[]{Comando.PERSONAGGIO_1, Comando.ESCI_DA_CITTA, Comando.NO,
				Comando.ANNULLA, Comando.PERGAMENA}) {
			if (comandi.contains(preferito)) {
				return preferito;
			}
		}
		for (Comando direzione : DIREZIONI) {
			if (comandi.contains(direzione)) {
				return direzione;
			}
		}
		return null;
	}

	// ---------------------------------------------------------------------------------------------------------
	// Stima degli scontri

	/**
	 * Il risultato di una stima: chi deve agire e come, e quanto dovrebbe costare lo scontro al capo.
	 */
	static final class Stima {
		/**
		 * Chi agisce per primo (in mischia o con un incantesimo)
		 */
		Personaggio attore;
		/**
		 * L'incantesimo che l'attore deve lanciare (anche DARDO_ARCANO), o null per la mischia
		 */
		Comando incantesimo;
		/**
		 * I danni attesi sul capo in rapporto alla sua salute più le pozioni: sopra 1 il capo non dovrebbe farcela
		 */
		double caricoCapo;
		/**
		 * Quanti compagni si prevede di perdere
		 */
		int compagniPersi;
		/**
		 * Il carico di ciascuno se combattesse da solo, per la traccia
		 */
		String dettaglio = "";
		/**
		 * Il carico di ciascun vivo se combattesse da solo
		 */
		Map<Personaggio, Double> carichi = Collections.emptyMap();
	}

	/**
	 * Stima a valore atteso degli scontri di una locazione. I danni medi di ogni coppia attaccante-difensore-arma
	 * si campionano una volta sola per locazione (calcolaDannoRisultante tira i dadi del gioco: prelevare qualche
	 * numero in più dalla sorgente non ne altera la distribuzione); la probabilità di colpire, che dipende da
	 * stanchezza ed effetti di stato, si ricalcola ogni volta.
	 */
	static final class Valutatore {

		private static final int CAMPIONI = 5;
		private final Map<Personaggio, Map<Personaggio, Map<String, Double>>> danniMedi = new IdentityHashMap<>();
		private final GruppoGiocatore gruppo;

		Valutatore(GruppoGiocatore gruppo) {
			this.gruppo = gruppo;
		}

		Stima stima(GruppoGiocatore gruppo, List<Personaggio> avversari) {
			Personaggio capo = gruppo.getCapo();
			List<Personaggio> vivi = gruppo.getPersonaggiVivi();
			double[] saluteAvversari = avversari.stream().mapToDouble(Personaggio::getSalute).toArray();

			// Il carico di ogni personaggio se combattesse da solo fino alla fine
			Map<Personaggio, Double> carico = new IdentityHashMap<>();
			Map<Personaggio, Comando> azione = new IdentityHashMap<>();
			for (Personaggio p : vivi) {
				Scontro scontro = scontro(p, avversari, saluteAvversari, vivi.size(), p == capo);
				carico.put(p, scontro.danniSubiti / Math.max(1, saluteEffettiva(p, p == capo)));
				azione.put(p, scontro.primaAzione);
			}
			StringBuilder dettaglio = new StringBuilder();
			for (Personaggio p : vivi) {
				dettaglio.append(p.getClasse()).append('=').append(String.format("%.2f", carico.get(p))).append(' ');
			}
			// I compagni combattono per primi, a cominciare da chi rischia meno; il capo per ultimo
			List<Personaggio> ordine = new ArrayList<>(vivi);
			ordine.remove(capo);
			ordine.sort((a, b) -> Double.compare(carico.get(a), carico.get(b)));
			ordine.add(capo);

			Stima stima = new Stima();
			stima.dettaglio = dettaglio.toString();
			stima.carichi = carico;
			// Apre lo scontro chi rischia meno, contando il capo una volta e mezza (se muore lui è finita)
			for (Personaggio p : vivi) {
				if (stima.attore == null || rischioPesato(p, capo, carico) < rischioPesato(stima.attore, capo, carico)) {
					stima.attore = p;
					stima.incantesimo = azione.get(p);
				}
			}
			double restante = 1.0d; // la frazione dello scontro ancora da combattere
			for (Personaggio p : ordine) {
				double c = carico.get(p);
				if (p == capo) {
					stima.caricoCapo = restante * c;
					break;
				}
				if (c <= 0.8) {
					restante = 0;
				} else {
					restante = Math.max(0, restante - 0.8d / c);
					stima.compagniPersi++;
				}
				if (restante <= 0) {
					stima.caricoCapo = 0;
					break;
				}
			}
			return stima;
		}

		static double rischioPesato(Personaggio p, Personaggio capo, Map<Personaggio, Double> carico) {
			return carico.get(p) * (p == capo ? 1.5d : 1.0d);
		}

		private double saluteEffettiva(Personaggio p, boolean capo) {
			double salute = p.getSalute();
			// Le pozioni si contano solo per il capo, che le beve per primo
			if (capo) {
				int pozioni = Math.min(POZIONI_NELLA_STIMA, gruppo.getPozioniSalute() + gruppo.getPozioniSaluteGrande());
				salute += pozioni * Math.min(Costanti.RECUPERO_DA_POZIONE_SALUTE, p.getSaluteMassima() * 0.66d);
			}
			return salute;
		}

		/**
		 * Uno scontro simulato a valori attesi: l'attore contro tutti gli avversari, azione per azione
		 * (la più dannosa fra un round di mischia, il dardo arcano e le pergamene), finché muoiono gli avversari
		 * o il conto dei danni subiti diventa insostenibile.
		 * <p>
		 * Come risponde il gioco: in un round di mischia il bersaglio risponde con le armi (LocazioneBase,
		 * gestisciCombattimento) e, se gli avversari sono più dei vivi del gruppo, uno di loro "si disimpegna e
		 * attacca" con Personaggio.attacca; a un incantesimo o a un dardo risponde un avversario con attacca
		 * (rispostaAvversaria). E attacca, per chi ha magia, vuol dire spesso un incantesimo, senza pergamene.
		 */
		private Scontro scontro(Personaggio attore, List<Personaggio> avversari, double[] saluteIniziale, int vivi, boolean capo) {
			double[] salute = saluteIniziale.clone();
			double[] magiaAvversari = avversari.stream().mapToDouble(Personaggio::getMagia).toArray();
			double magia = attore.getMagia();
			Map<ClasseIncantesimo, Integer> pergamene = new HashMap<>();
			for (ClasseIncantesimo classe : INCANTESIMI_DI_DANNO) {
				pergamene.put(classe, gruppo.getIncantesimi(classe));
			}
			boolean dardo = DardoArcano.conosciutoDa(attore.getClasse());
			double costoDardo = attore.getClasse().name().startsWith("MAG")
					? Costanti.DARDO_ARCANO_COSTO_LANCIO_MAGO : Costanti.DARDO_ARCANO_COSTO_LANCIO_ELFO;
			Scontro scontro = new Scontro();
			double limite = saluteEffettiva(attore, capo) * 4;
			int prossimoAttaccante = 0;
			for (int azioni = 0; azioni < 100 && scontro.danniSubiti < limite; azioni++) {
				int primo = primoVivo(salute);
				if (primo < 0) {
					break;
				}
				int avversariVivi = contaVivi(salute);
				Personaggio bersaglio = avversari.get(primo);
				double mischia = mischia(attore, bersaglio);
				double migliore = mischia;
				Comando scelta = null;
				ClasseIncantesimo classeScelta = null;
				for (ClasseIncantesimo classe : INCANTESIMI_DI_DANNO) {
					Incantesimo incantesimo = classe.getIstanza(attore.getLivello());
					if (pergamene.get(classe) <= 0 || magia < incantesimo.getCostoLancio()
							|| !(incantesimo instanceof Arma)) {
						continue;
					}
					double danno = dannoIncantesimo(attore, avversari, salute, classe, false);
					if (danno > migliore) {
						migliore = danno;
						classeScelta = classe;
						scelta = classe.getComandoDiAttivazione();
					}
				}
				if (dardo && magia >= costoDardo) {
					double danno = Math.min(salute[primo], atteso(attore, bersaglio, new DardoArcano(attore), 1.0d, "DARDO"));
					if (danno > migliore) {
						migliore = danno;
						classeScelta = null;
						scelta = Comando.DARDO_ARCANO;
					}
				}
				// La risposta: alla mischia il bersaglio con le armi (più chi si disimpegna), agli incantesimi
				// il prossimo avversario con attacca
				double rispostaMischia = mischia(bersaglio, attore);
				double[] disimpegno = null;
				if (avversariVivi > vivi) {
					disimpegno = attacco(bersaglio, attore, magiaAvversari[primo]);
					rispostaMischia += disimpegno[0];
				}
				int chiRisponde = prossimoVivo(salute, prossimoAttaccante);
				double[] rispostaIncantesimo = attacco(avversari.get(chiRisponde), attore, magiaAvversari[chiRisponde]);
				// La mischia costa meno risposte magiche: un incantesimo deve valerne la pena
				if (scelta != null && migliore / Math.max(1, rispostaIncantesimo[0]) < 1.1d * mischia / Math.max(1, rispostaMischia)
						&& migliore < primoSalute(salute)) {
					scelta = null;
					classeScelta = null;
					migliore = mischia;
				}
				if (azioni == 0) {
					scontro.primaAzione = scelta;
				}
				if (migliore <= 0.5d) {
					// Non si scalfisce nessuno: lo scontro non si vince
					scontro.danniSubiti = Math.max(scontro.danniSubiti, limite);
					break;
				}
				if (scelta == null) {
					salute[primo] -= mischia;
					scontro.danniSubiti += rispostaMischia;
					if (disimpegno != null) {
						magiaAvversari[primo] -= disimpegno[1];
					}
				} else {
					if (classeScelta != null) {
						pergamene.put(classeScelta, pergamene.get(classeScelta) - 1);
						magia -= classeScelta.getIstanza(attore.getLivello()).getCostoLancio();
						dannoIncantesimo(attore, avversari, salute, classeScelta, true);
					} else {
						magia -= costoDardo;
						salute[primo] -= atteso(attore, bersaglio, new DardoArcano(attore), 1.0d, "DARDO");
					}
					scontro.danniSubiti += rispostaIncantesimo[0];
					magiaAvversari[chiRisponde] -= rispostaIncantesimo[1];
					prossimoAttaccante = chiRisponde + 1;
				}
			}
			if (primoVivo(salute) >= 0) {
				scontro.danniSubiti = Math.max(scontro.danniSubiti, limite);
			}
			return scontro;
		}

		private static double primoSalute(double[] salute) {
			int primo = primoVivo(salute);
			return primo < 0 ? 0 : salute[primo];
		}

		/**
		 * I danni attesi di un incantesimo sugli avversari che raggiunge; con applica li toglie dalla salute
		 */
		private double dannoIncantesimo(Personaggio attore, List<Personaggio> avversari, double[] salute,
										ClasseIncantesimo classe, boolean applica) {
			Arma incantesimo = (Arma) classe.getIstanza(attore.getLivello());
			double danno = 0;
			int bersagli = 0;
			for (int i = 0; i < salute.length; i++) {
				if (salute[i] <= 0 || avversari.get(i).isImmuneAIncantesimo(classe)) {
					continue;
				}
				if (classe.getPortata() == PortataIncantesimo.MULTIPLO && bersagli >= attore.getBersagli()) {
					break;
				}
				bersagli++;
				double colpo = atteso(attore, avversari.get(i), incantesimo, 1.0d, classe.name());
				danno += Math.min(salute[i], colpo);
				if (applica) {
					salute[i] -= colpo;
				}
			}
			return danno;
		}

		private static int prossimoVivo(double[] salute, int da) {
			for (int i = 0; i < salute.length; i++) {
				int indice = (da + i) % salute.length;
				if (salute[indice] > 0) {
					return indice;
				}
			}
			return 0;
		}

		/**
		 * I danni attesi di Personaggio.attacca (PersonaggioBase.scegliIncantesimoContro): chi ha magia lancia
		 * due volte su tre un incantesimo malefico che può permettersi (il più potente, o a caso se è poco
		 * intelligente, o solo se rende più dell'arma se è molto intelligente), altrimenti colpisce con l'arma.
		 *
		 * @return i danni attesi e la magia che in media consuma
		 */
		double[] attacco(Personaggio attaccante, Personaggio difensore, double magia) {
			Arma arma = attaccante.getArmaEquipaggiata();
			double fisico = atteso(attaccante, difensore, arma, 1.0d, "ATTACCA/" + arma.getClass().getName() + '/' + arma.getDanni());
			if (!attaccante.isMagico() || magia <= 0) {
				return new double[]{fisico, 0};
			}
			List<ClasseIncantesimo> possibili = new ArrayList<>();
			for (ClasseIncantesimo classe : ClasseIncantesimo.values()) {
				if (classe.getTipo() == com.threeamigos.foresta.incantesimi.TipoIncantesimo.MALEFICO
						&& classe.getCostoLancio() <= magia && classe != ClasseIncantesimo.MORTE) {
					possibili.add(classe);
				}
			}
			if (possibili.isEmpty()) {
				return new double[]{fisico, 0};
			}
			ClasseIncantesimo scelto = possibili.get(0);
			for (ClasseIncantesimo classe : possibili) {
				if (((Arma) classe.getIstanza(attaccante.getLivello())).getDanni()
						> ((Arma) scelto.getIstanza(attaccante.getLivello())).getDanni()) {
					scelto = classe;
				}
			}
			double magico;
			if (attaccante.getIntelligenza() < 5) {
				double somma = 0;
				for (ClasseIncantesimo classe : possibili) {
					somma += atteso(attaccante, difensore, (Arma) classe.getIstanza(attaccante.getLivello()), 1.0d, classe.name());
				}
				magico = somma / possibili.size();
			} else {
				magico = atteso(attaccante, difensore, (Arma) scelto.getIstanza(attaccante.getLivello()), 1.0d, scelto.name());
				if (attaccante.getIntelligenza() >= 7) {
					magico = Math.max(magico, fisico);
				}
			}
			return new double[]{(2 * magico + fisico) / 3, 2.0d * scelto.getCostoLancio() / 3};
		}

		private static int primoVivo(double[] salute) {
			for (int i = 0; i < salute.length; i++) {
				if (salute[i] > 0) {
					return i;
				}
			}
			return -1;
		}

		private static int contaVivi(double[] salute) {
			int vivi = 0;
			for (double s : salute) {
				if (s > 0) {
					vivi++;
				}
			}
			return vivi;
		}

		/**
		 * I danni attesi di un round di mischia (arma principale ed eventuale seconda arma)
		 */
		double mischia(Personaggio attaccante, Personaggio difensore) {
			double danni = 0;
			for (FaseDiAttacco fase : CalcolatoreCombattimento.fasiDiAttacco(attaccante)) {
				Arma arma = fase.getArma();
				danni += atteso(attaccante, difensore, arma, fase.getFattore(),
						arma.getClass().getName() + '/' + arma.getDanni() + '/' + arma.getLivello());
			}
			return danni;
		}

		private double atteso(Personaggio attaccante, Personaggio difensore, Arma arma, double fattore, String chiave) {
			double probabilita = CalcolatoreCombattimento.calcolaProbabilitaDiColpire(attaccante, difensore,
					arma.getTipoDanno().getSuperTipo()) / 100.0d;
			Map<String, Double> perArma = danniMedi.computeIfAbsent(attaccante, k -> new IdentityHashMap<>())
					.computeIfAbsent(difensore, k -> new HashMap<>());
			String chiaveCompleta = chiave + '@' + fattore;
			Double medio = perArma.get(chiaveCompleta);
			if (medio == null) {
				double somma = 0;
				for (int i = 0; i < CAMPIONI; i++) {
					somma += CalcolatoreCombattimento.calcolaDannoRisultante(attaccante, difensore, arma, fattore).getDanno();
				}
				medio = somma / CAMPIONI;
				perArma.put(chiaveCompleta, medio);
			}
			return probabilita * medio;
		}
	}

	private static final class Scontro {
		double danniSubiti;
		Comando primaAzione;
	}

	// ---------------------------------------------------------------------------------------------------------
	// Mappa

	/**
	 * Uno spostamento: direzione (indice in DIREZIONI) e numero di passi
	 */
	static final class Mossa {
		final int direzione;
		final int passi;

		Mossa(int direzione, int passi) {
			this.direzione = direzione;
			this.passi = passi;
		}
	}

	/**
	 * La mappa come la conosce il giocatore: le caselle conosciute (ForestaMD) e le regole di movimento di
	 * GruppoGiocatore.getMaxPassi*, con le caselle sconosciute trattate come bosco.
	 */
	static final class Mappa {

		private Mappa() {
		}

		/**
		 * La classe della casella se è conosciuta, altrimenti null
		 */
		static ClassiLocazione nota(int x, int y) {
			if (x < 0 || y < 0 || x >= Foresta.getDimensioneX() || y >= Foresta.getDimensioneY()) {
				return null;
			}
			CoordinateMD c = new CoordinateMD(x, y);
			return Foresta.isLocazioneConosciuta(c) ? Foresta.getLocazione(c) : null;
		}

		/**
		 * Come GruppoGiocatore.getMaxPassi*: ci si ferma sul bordo o sulla prima casella che non è bosco o radura
		 */
		static int maxPassi(int x, int y, int d) {
			int dimX = Foresta.getDimensioneX();
			int dimY = Foresta.getDimensioneY();
			if ((d == 0 && y == 0) || (d == 1 && x == dimX - 1) || (d == 2 && y == dimY - 1) || (d == 3 && x == 0)) {
				return 0;
			}
			for (int i = 1; i <= Comando.MAX_MOVIMENTO; i++) {
				int nx = x + DX[d] * i;
				int ny = y + DY[d] * i;
				if ((d == 0 && ny == 0) || (d == 1 && nx == dimX - 1) || (d == 2 && ny == dimY - 1) || (d == 3 && nx == 0)) {
					return i;
				}
				ClassiLocazione classe = nota(nx, ny);
				if (classe != null && classe != ClassiLocazione.BOSCO && classe != ClassiLocazione.RADURA) {
					return i;
				}
			}
			return Comando.MAX_MOVIMENTO;
		}

		/**
		 * Il rischio di fermarsi su una casella: circa la probabilità di trovarci mostri, pesata per la loro forza
		 */
		static double rischio(ClassiLocazione classe, boolean meta) {
			if (classe == null || classe == ClassiLocazione.BOSCO || classe == ClassiLocazione.RADURA) {
				return 1.0d;
			}
			switch (classe.getTipoLocazione()) {
				case CITTA:
					return 0.2d;
				case CASTELLO:
					return meta ? 5.0d : Double.POSITIVE_INFINITY;
				case MISSIONE_SECONDARIA:
					return 1.5d;
				default:
					break;
			}
			switch (classe) {
				case PALUDE:
					return 0.1d;
				case LOCANDA:
					return 0.3d;
				case GROTTA:
					return 0.6d;
				case TEMPIO:
					return 4.0d;
				default:
					return 1.0d;
			}
		}

		/**
		 * Quante caselle sconosciute si vedrebbero arrivando lì (Foresta.aggiornaMappaCircostante: 7x7)
		 */
		static int sconosciuteIntorno(int x, int y) {
			int dimX = Foresta.getDimensioneX();
			int dimY = Foresta.getDimensioneY();
			int daX = Math.max(0, Math.min(x - 3, dimX - 7));
			int daY = Math.max(0, Math.min(y - 3, dimY - 7));
			int sconosciute = 0;
			for (int i = daX; i < daX + 7; i++) {
				for (int j = daY; j < daY + 7; j++) {
					if (!Foresta.isLocazioneConosciuta(new CoordinateMD(i, j))) {
						sconosciute++;
					}
				}
			}
			return sconosciute;
		}

		/**
		 * Il primo spostamento del percorso meno rischioso verso una delle mete (Dijkstra: ogni spostamento costa
		 * il rischio della casella d'arrivo più un poco per il tempo), o null se non ce n'è.
		 *
		 * @param maxQui i passi possibili dalla casella di partenza, come li dà il gioco
		 */
		static Mossa primaMossaVerso(int x0, int y0, int[] maxQui, List<CoordinateMD> mete) {
			int dimX = Foresta.getDimensioneX();
			int dimY = Foresta.getDimensioneY();
			boolean[] meta = new boolean[dimX * dimY];
			for (CoordinateMD c : mete) {
				meta[c.getX() * dimY + c.getY()] = true;
			}
			double[] costo = new double[dimX * dimY];
			Arrays.fill(costo, Double.POSITIVE_INFINITY);
			Mossa[] primaMossa = new Mossa[dimX * dimY];
			PriorityQueue<double[]> coda = new PriorityQueue<>((a, b) -> Double.compare(a[0], b[0]));
			int partenza = x0 * dimY + y0;
			costo[partenza] = 0;
			coda.add(new double[]{0, partenza});
			while (!coda.isEmpty()) {
				double[] elemento = coda.poll();
				int nodo = (int) elemento[1];
				if (elemento[0] > costo[nodo]) {
					continue;
				}
				if (meta[nodo]) {
					return primaMossa[nodo];
				}
				int x = nodo / dimY;
				int y = nodo % dimY;
				ClassiLocazione qui = nota(x, y);
				if (nodo != partenza && qui != null && qui.getTipoLocazione() == TipoLocazione.CASTELLO) {
					continue;
				}
				for (int d = 0; d < 4; d++) {
					int max = nodo == partenza ? maxQui[d] : maxPassi(x, y, d);
					for (int k = 1; k <= max; k++) {
						int nx = x + DX[d] * k;
						int ny = y + DY[d] * k;
						int vicino = nx * dimY + ny;
						double nuovo = costo[nodo] + rischio(nota(nx, ny), meta[vicino]) + 0.05d + 0.01d * k;
						if (nuovo < costo[vicino]) {
							costo[vicino] = nuovo;
							primaMossa[vicino] = nodo == partenza ? new Mossa(d, k) : primaMossa[nodo];
							coda.add(new double[]{nuovo, vicino});
						}
					}
				}
			}
			return null;
		}
	}
}
