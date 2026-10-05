package com.threeamigos.foresta.locazioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.*;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.eventi.richieste.RichiestaSelezioneSiNo;
import com.threeamigos.foresta.incantesimi.*;
import com.threeamigos.foresta.interfacce.Arma;
import com.threeamigos.foresta.motore.*;
import com.threeamigos.foresta.motore.modellodati.LocazioneMD;
import com.threeamigos.foresta.offerte.Offerta;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.oggetti.ClassiOggetto;
import com.threeamigos.foresta.oggetti.Oggetto;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.CategoriaLocazione;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.tools.Misc;
import com.threeamigos.foresta.ui.InterfacciaUtente;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;

/**
 * La locazione è un automa a stati finiti. Un gruppo mentre si sposta per
 * la foresta si trova all'interno di una locazione. Appena entra la locazione
 * è in stato NUOVA_LOCAZIONE (vengono creati i mostri e gli oggetti). Quindi
 * il gruppo continua a trovarsi in stato IN_LOCAZIONE. A seconda delle azioni
 * che intraprende può spostarsi momentaneamente da tale stato (ad esempio
 * per richiedere il personaggio attivo o il bersaglio di un incantesimo) ma finisce
 * sempre per tornarvi.
 * Il gruppo può decidere di combattere e va in stato CHI_COMBATTE,
 * può decidere di formulare un incantesimo e va in stato CHI_FORMULA e
 * quindi in stato QUALE_FORMULA; eventualmente se l'incantesimo ha
 * bisogno di un personaggio bersaglio va in stato SU_CHI_FORMULA.
 * Può anche tentare una corruzione e va in stato CHI_CORROMPE, o
 * può tentare di fare amicizia e va in stato CHI_FA_AMICIZIA.
 * Gli stati sono riferiti al gruppo ma vengono tenuti all'interno della
 * locazione, questo perché esistono altre locazioni che fanno invece altre
 * cose - la locanda, ad esempio, permette di pernottare o prendere gente con se),
 * la città anche (andare da un alchimista, eccetera).
 */

public abstract class LocazioneBase implements Locazione {

	private static final ClassePersonaggio[] NESSUN_INCONTRO = {};
	private static final ClassiOggetto[] NESSUN_OGGETTO = {};

	private final GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
	private final GruppoAvversario gruppoAvversario = GruppoAvversario.getIstanza();

	/**
	 * Lo stato durevole della casella: nome, visitata, conosciuta, completa.
	 * Sopravvive alla visita e al salvataggio.
	 */
	private LocazioneMD md = new LocazioneMD(getClasseLocazione());

	private Oggetto oggettoCorrente;
	// Se il gruppo può (ancora) tentare di corrompere gli avversari
	private boolean opzioneCorruzioneDisponibile;
	// Se il gruppo può (ancora) cercare di fare amicizia
	private boolean opzioneAmiciziaDisponibile;
	// Se il gruppo stringe amicizia non può prendere gli oggetti
	private boolean haStrettoAmicizia;
	// Se all'arrivo del gruppo c'erano avversari: altrimenti il tesoro è incustodito
	private boolean custodita;
	// Se si riesce a fare amicizia potrebbe essere formulata un'offerta
	private Offerta offerta;

	private StatoLocazione statoLocazione;
	/**
	 * Gli avversari di cui si è già contata la sconfitta: chi si arrende resta vivo, e non va contato due volte.
	 */
	private final Set<Personaggio> avversariSconfitti = Collections.newSetFromMap(new IdentityHashMap<>());
	/**
	 * Se il gruppo non può più passare inosservato in questa locazione: ci ha già provato (si prova una volta sola),
	 * o ha già fatto qualcos'altro (vedi isAzione).
	 */
	private boolean passaggioEscluso;

	// Passare inosservati (vedi passaInosservati): la probabilità, in percentuale
	private static final int PASSAGGIO_BASE = 40;
	private static final int PASSAGGIO_PER_PUNTO = 10;
	private static final int PASSAGGIO_DI_NOTTE = 15;
	private static final int PASSAGGIO_PER_COMPAGNO = 10;
	private static final int PASSAGGIO_MINIMO = 5;
	private static final int PASSAGGIO_MASSIMO = 75;
	private static final int ORA_DELL_ALBA = 6;
	private static final int ORA_DEL_TRAMONTO = 20;

	// Chi sta combattendo
	private Personaggio combattente;

    private Incantesimo incantesimo;
	private Gruppo gruppoBersaglio;

	private enum StatoLocazione {
		NUOVA_LOCAZIONE,
		IN_LOCAZIONE,
		CHI_ESEGUE_SINGOLO_ATTACCO,
		CHI_COMBATTE,
		IN_COMBATTIMENTO,
		CHI_BEVE_POZIONE_SALUTE,
		CHI_BEVE_POZIONE_SALUTE_GRANDE,
		CHI_BEVE_POZIONE_MAGIA,
		CHI_BEVE_POZIONE_MAGIA_GRANDE,
		CHI_FORMULA,
		QUALE_FORMULA,
		SU_CHI_FORMULA,
		CHI_CORROMPE,
		CHI_FA_AMICIZIA,
		ACCETTA_OFFERTA,
		CONFERMA_FUGA,
		CHI_DUELLA
	}

	/**
	 * L'elenco dei mostri e degli oggetti che è possibile trovare all'interno di
	 * questa locazione. Ogni locazione che ne ha sovrascrive questi due getter.
	 */
	protected ClassePersonaggio[] getPossibiliIncontri() {
		return NESSUN_INCONTRO;
	}

	protected ClassiOggetto[] getPossibiliOggetti() {
		return NESSUN_OGGETTO;
	}

	@Override
	public LocazioneMD getModelloDati() {
		return md;
	}

	@Override
	public void setModelloDati(LocazioneMD modelloDati) {
		this.md = modelloDati;
	}

	@Override
	public String getNome() {
		// Per una locazione unica il nome c'è sempre, anche su un'istanza senza la sua casella
		return md.getNome() != null ? md.getNome() : getClasseLocazione().getNomeProprio();
	}

	/**
	 * Una locazione è completa se non vi sono più mostri e il gruppo non
	 * è fuggito; questo serve per sapere se si possono prendere gli oggetti
	 * o se i mostri dei castelli sono stati sconfitti.
	 */
	protected void setCompleta(boolean completa) {
		if (completa) {
			md.aggiungiProprieta(LocazioneMD.COMPLETA, LocazioneMD.AFFERMATIVO);
		} else {
			md.rimuoviProprieta(LocazioneMD.COMPLETA);
		}
	}

	protected LocazioneBase() {
		statoLocazione = StatoLocazione.NUOVA_LOCAZIONE;
		gruppo.setFormulante(null);
	}

	/**
	 * All'interno di una specifica locazione possono essere creati determinati
	 * tipi di mostri e di oggetti. Ogni locazione semplicemente modifica la
	 * getMostri() e la getOggetti() per riportare quale mostro e quale oggetto
	 * possono essere trovati in ogni locazione. La locazione base non ha mostri
	 * e oggetti associati.
	 */
	/**
	 * I mostri che non si incontrano durante l'inizio morbido (Costanti.INIZIO_MORBIDO_FINO_AL_LIVELLO): a livello 1
	 * sono più forti di un protagonista solo con la dotazione di base
	 */
	public static final Set<ClassePersonaggio> MOSTRI_ESCLUSI_A_INIZIO_PARTITA = Collections.unmodifiableSet(EnumSet.of(
			ClassePersonaggio.CENTAURO, ClassePersonaggio.CHIMERA_DRAGO, ClassePersonaggio.GIGANTE,
			ClassePersonaggio.MINOTAURO, ClassePersonaggio.TITANO, ClassePersonaggio.TROLL));

	/**
	 * I mostri che si possono incontrare in una locazione, dato il livello del capo del gruppo: durante l'inizio
	 * morbido senza quelli di MOSTRI_ESCLUSI_A_INIZIO_PARTITA (se ne resta almeno uno)
	 */
	public static ClassePersonaggio[] incontriPossibili(ClassePersonaggio[] mostri, int livelloCapo) {
		if (livelloCapo > Costanti.INIZIO_MORBIDO_FINO_AL_LIVELLO) {
			return mostri;
		}
		ClassePersonaggio[] ammessi = Arrays.stream(mostri).filter(m -> !MOSTRI_ESCLUSI_A_INIZIO_PARTITA.contains(m))
				.toArray(ClassePersonaggio[]::new);
		return ammessi.length > 0 ? ammessi : mostri;
	}

	/**
	 * Quanti mostri al massimo in un incontro (e comunque non più del massimo per locazione della classe): tanti
	 * quanti i personaggi in campo del gruppo (gli ospiti non contano), più uno fino al livello 5 del capo, due fino
	 * al 15, quattro oltre; durante l'inizio morbido nessuno in più. Per un protagonista solo: 1, 2, 3 e 5.
	 */
	public static int numeroMassimoDiMostri(int livelloCapo, int personaggiDelGruppo) {
		int inPiu;
		if (livelloCapo <= Costanti.INIZIO_MORBIDO_FINO_AL_LIVELLO) {
			inPiu = 0;
		} else if (livelloCapo <= 5) {
			inPiu = 1;
		} else if (livelloCapo <= 15) {
			inPiu = 2;
		} else {
			inPiu = 4;
		}
		return Math.max(1, personaggiDelGruppo) + inPiu;
	}

	public void crea(GruppoGiocatore g, GruppoAvversario avversario) {
		ClassePersonaggio[] m = incontriPossibili(getPossibiliIncontri(), g.getCapo().getLivello());
		if (m.length > 0) {
			// Non sempre si trovano mostri. Al primo turno però vogliamo sempre trovarne uno,
			// un po' per non dare l'impressione che la foresta sia vuota, un po' per non far
			// scattare immediatamente le missioni secondarie che scattano a fine locazione.
			boolean possibilitaIncontro = Statistiche.getTurniGiocati() == 0 || Dado.tira(100) <= 90;
			if (possibilitaIncontro) {
				ClassePersonaggio classePersonaggio = null;

				int ordinale = Dado.tiraAncheAUnaFaccia(m.length) - 1;
				classePersonaggio = m[ordinale];

				// FIXME occorrerebbe gestire la cosa un po' più elegantemente...
				// PEr non far apparire subito un Eremita che fa scattare la missione secondaria a inizio locazione
				while (Statistiche.getTurniGiocati() == 0 && classePersonaggio == ClassePersonaggio.EREMITA) {
					ordinale = Dado.tiraAncheAUnaFaccia(m.length) - 1;
					classePersonaggio = m[ordinale];
				}

				int capLivello = numeroMassimoDiMostri(g.getCapo().getLivello(), g.getPersonaggiVivi().size());

				int limiteMassimoIncontro = Math.min(capLivello, classePersonaggio.getQuantitaMassima());

				int numero = Dado.tiraAncheAUnaFaccia(limiteMassimoIncontro);
				Logger.log("Scelta da " + m.length + " personaggi la classe " + classePersonaggio + ", numero " + numero + " con cap " + capLivello);
				Personaggio p;
				for (int i = 0; i < numero; i++) {
					p = classePersonaggio.getIstanza(Statistiche.getLivello());
					p.setOrdinale(i + 1);
					avversario.aggiungiPersonaggio(p);
				}
			} else {
				Logger.log("Per stavolta niente mostri");
			}
		}
		// Vediamo quali artefatti sono presenti in questa locazione.
		Artefatto a = getArtefatto(g);
		if (a != null) {
			setOggetto(a);
		} else if (!isLocazioneVisitata()) {
			// Non ci sono artefatti, creiamo un oggetto.
			ClassiOggetto[] o = getPossibiliOggetti();
			Logger.log("Scelta da " + o.length + " oggetti");
			if (o.length > 0) {
				int indice = Dado.tiraAncheAUnaFaccia(o.length) - 1;
				ClassiOggetto classeOggetto = o[indice];
				Logger.log("Classe oggetto " + classeOggetto);
				Oggetto probabileOggetto = classeOggetto.getIstanza();
				if (probabileOggetto.getQuantita() > 0) {
					setOggetto(probabileOggetto);
				}
			}
		}
	}
	
	protected boolean isLocazioneVisitata() {
		return Foresta.isLocazioneVisitata(gruppo.getCoordinate());
	}

	/**
	 * Restituisce il primo artefatto presente in questa locazione
	 */
	protected Artefatto getArtefatto(GruppoGiocatore g) {
		return RegistroArtefatti.getArtefattoInLocazione(g.getCoordinate());
	}

	/**
	 * All'interno di una specifica locazione possono essere eseguiti determinati tipi di azioni.
	 * La funzione torna lo stato (dell'Automa) in cui si trova il gruppo.
	 *
	 * @param azione l'azione che il gruppo ha deciso di intraprendere; la prima
	 *        volta la funzione viene chiamata con Azione.INVALIDA; dalla successiva
	 *        viene chiamata passandole l'azione scelta dal giocatore.
	 */
	@Override
	public Stato impostaAzioni(GruppoGiocatore gruppo, GruppoAvversario gruppoAvversario, Comando azione) {
		Stato stato = impostaAzioniImpl(gruppo, gruppoAvversario, azione);
		if (stato != Stato.GIOCO_PERSO && stato != Stato.FINE_LOCAZIONE && isDuelloPerso()) {
			return perdiIlDuello();
		}
		// Non si guarda se la locazione è completa: quando l'ultimo avversario muore per un veleno non lo è ancora
		if (stato == Stato.FINE_LOCAZIONE && gruppoAvversario.getPersonaggiVivi().isEmpty() && gruppoAvversario.hasOndateSuccessive()) {
			return arrivaLaProssimaOndata();
		}
		return stato;
	}

	/**
	 * Sconfitti tutti gli avversari in campo, ne arriva un'altra ondata (vedi GruppoAvversario.prossimaOndata): la
	 * locazione non è più completa, la finestra di combattimento si chiude e si torna a scegliere che cosa fare. Tutti
	 * i punti in cui un combattimento si vince (la mischia, gli incantesimi, il dardo, i veleni) passano di qui.
	 */
	private Stato arrivaLaProssimaOndata() {
		BusEventi.pubblica(new InternoRichiestaChiusuraFinestraCombattimento());
		Ondata ondata = gruppoAvversario.prossimaOndata();
		combattente = null;
		statoLocazione = StatoLocazione.IN_LOCAZIONE;
		opzioneCorruzioneDisponibile = false;
		opzioneAmiciziaDisponibile = false;
		setCompleta(false);
		BusEventi.pubblica(new InternoAssegnaCoordinateAPersonaggi());
		BusEventi.pubblica(new NotificaTestoParagrafo(ondata.getArrivo()));
		impostaComandiPossibili();
		return Stato.IN_LOCAZIONE;
	}

	/**
	 * Se quel comando, nello stato corrente della locazione, è un'azione vera: un attacco, una mischia, un incantesimo
	 * lanciato, una corruzione, un tentativo d'amicizia, una pozione bevuta. Dopo, passare inosservati non si può
	 * più. Guardare la mappa o l'inventario non conta, e nemmeno scegliere un'azione e poi annullarla: l'azione conta
	 * quando si sceglie chi la compie (o, per un incantesimo, quale e su chi).
	 */
	private boolean isAzione(Comando azione) {
		if (azione == null || azione == Comando.ANNULLA) {
			return false;
		}
		switch (statoLocazione) {
			case IN_COMBATTIMENTO:
			case CHI_ESEGUE_SINGOLO_ATTACCO:
			case CHI_COMBATTE:
			case CHI_BEVE_POZIONE_SALUTE:
			case CHI_BEVE_POZIONE_SALUTE_GRANDE:
			case CHI_BEVE_POZIONE_MAGIA:
			case CHI_BEVE_POZIONE_MAGIA_GRANDE:
			case QUALE_FORMULA:
			case SU_CHI_FORMULA:
			case CHI_CORROMPE:
			case CHI_FA_AMICIZIA:
				return true;
			default:
				return false;
		}
	}

	/**
	 * In un combattimento fino alla resa, i personaggi del gruppo si sono arresi tutti: sono vivi, ma in panchina; o,
	 * in un duello, chi lo combatteva si è arreso o è morto, e gli altri sono in panchina.
	 */
	private boolean isDuelloPerso() {
		return (gruppoAvversario.isFinoAllaResa() || gruppoAvversario.isDuello()) && gruppo.getCapo().isVivo()
				&& gruppo.getPersonaggiVivi().isEmpty() && !gruppoAvversario.getPersonaggiVivi().isEmpty();
	}

	/**
	 * Il duello perso: il gruppo se ne va, come da una fuga ma senza danni, e la locazione non è completa; chi l'ha
	 * vinto resta lì ad aspettare la rivincita (la missione che l'ha messo lì è ancora a quel passo).
	 */
	private Stato perdiIlDuello() {
		BusEventi.pubblica(new InternoRichiestaChiusuraFinestraCombattimento());
		Personaggio vincitore = gruppoAvversario.getPersonaggioVivo();
		BusEventi.pubblica(new NotificaTestoParagrafo(vincitore.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
				Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA) + " ha vinto, e vi lascia andare. Vi aspetta qui per la rivincita, quando sarete in forze."));
		combattente = null;
		statoLocazione = StatoLocazione.IN_LOCAZIONE;
		setCompleta(false);
		setOggetto(null);
		return Stato.FINE_LOCAZIONE;
	}

	private Stato impostaAzioniImpl(GruppoGiocatore gruppo, GruppoAvversario gruppoAvversario, Comando azione) {
		if (isAzione(azione)) {
			passaggioEscluso = true;
		}
        Stato possibileStato;
        switch (statoLocazione) {
			case NUOVA_LOCAZIONE:
				possibileStato = gestisciNuovaLocazione();
				if (possibileStato != null) {
					return possibileStato;
				}
				break;

			case IN_COMBATTIMENTO:
				possibileStato = gestisciCombattimento(azione);
				if (possibileStato != null) {
					return possibileStato;
				}
				possibileStato = gestisciInLocazione(azione);
				if (possibileStato != null) {
					return possibileStato;
				}
				break;

			case IN_LOCAZIONE:
				possibileStato = gestisciInLocazione(azione);
				if (possibileStato != null) {
					return possibileStato;
				}
				break;

			case CHI_ESEGUE_SINGOLO_ATTACCO:
				if (azione == Comando.ANNULLA) {
					return annullaScelta();
				}
				return gestisciChiEsegueSingoloAttacco(azione);

			case CHI_COMBATTE:
				if (azione == Comando.ANNULLA) {
					return annullaScelta();
				}
				gestisciChiCombatte(azione);
				break;

			case CHI_DUELLA:
				if (azione == Comando.ANNULLA) {
					return rifiutaIlDuello();
				}
				gestisciChiDuella(azione);
				break;

			case CHI_BEVE_POZIONE_SALUTE:
				Logger.log("LocazioneBase.CHI_BEVE_POZIONE_SALUTE");
				statoLocazione = StatoLocazione.IN_LOCAZIONE;
				if (azione == Comando.ANNULLA) {
					return annullaScelta();
				}
				gruppo.consumaPozioneSalute(azione);
				// Bere la pozione fa trascorrere un turno: il suo esito (per esempio la morte del
				// capo o dell'ultimo avversario per un effetto di stato) va restituito all'automa.
				return impostaAzioni(gruppo, gruppoAvversario, null);

			case CHI_BEVE_POZIONE_SALUTE_GRANDE:
				Logger.log("LocazioneBase.CHI_BEVE_POZIONE_SALUTE_GRANDE");
				statoLocazione = StatoLocazione.IN_LOCAZIONE;
				if (azione == Comando.ANNULLA) {
					return annullaScelta();
				}
				gruppo.consumaPozioneSaluteGrande(azione);
				return impostaAzioni(gruppo, gruppoAvversario, null);

			case CHI_BEVE_POZIONE_MAGIA:
				Logger.log("LocazioneBase.CHI_BEVE_POZIONE_MAGIA");
				statoLocazione = StatoLocazione.IN_LOCAZIONE;
				if (azione == Comando.ANNULLA) {
					return annullaScelta();
				}
				gruppo.consumaPozioneMagia(azione);
				return impostaAzioni(gruppo, gruppoAvversario, null);

			case CHI_BEVE_POZIONE_MAGIA_GRANDE:
				Logger.log("LocazioneBase.CHI_BEVE_POZIONE_MAGIA_GRANDE");
				statoLocazione = StatoLocazione.IN_LOCAZIONE;
				if (azione == Comando.ANNULLA) {
					return annullaScelta();
				}
				gruppo.consumaPozioneMagiaGrande(azione);
				return impostaAzioni(gruppo, gruppoAvversario, null);

			case CHI_FORMULA:
				Logger.log("LocazioneBase.CHI_FORMULA");
				if (azione == Comando.ANNULLA) {
					return annullaScelta();
				}
				gruppo.setFormulante(gruppo.getPersonaggio(azione));
				statoLocazione = StatoLocazione.QUALE_FORMULA;
				return Stato.SCELTA_INCANTESIMO_DA_LANCIARE;

			case QUALE_FORMULA:
				Logger.log("LocazioneBase.QUALE_FORMULA: " + azione);
				if (azione == Comando.NO_INCANTESIMO) {
					return annullaScelta();
				} else if (azione == Comando.DARDO_ARCANO) {
					Stato statoDopoIlDardo = lanciaDardoArcano(gruppo.getFormulante(), gruppo, gruppoAvversario);
					if (statoDopoIlDardo != null) {
						return statoDopoIlDardo;
					}
					statoLocazione = StatoLocazione.IN_LOCAZIONE;
					break;
				} else {
					opzioneCorruzioneDisponibile = false;
					opzioneAmiciziaDisponibile = false;
					Personaggio formulante = gruppo.getFormulante();
					ClasseIncantesimo classeIncantesimo = ClasseIncantesimo.ofComando(azione);
					incantesimo = classeIncantesimo.getIstanza(formulante.getLivello());
					if (formulante.getMagia() < incantesimo.getCostoLancio()) {

						// Non si dovrebbe più riuscire a entrare in questo ramo perché la scelta degli incantesimi è già stata filtrata
						String sb = "Il livello di magia " + formulante.getNome(Personaggio.OpzioniGetNome.INCLUDI_PREPOSIZIONE_ARTICOLATA) +
								" non permette di formulare questo incantesimo.";
						BusEventi.pubblica(new NotificaTestoFrase(sb));

						rispostaAvversaria(null, gruppo, gruppoAvversario);

						if (!gruppo.getCapo().isVivo()) {
							return Stato.GIOCO_PERSO;
						}

						statoLocazione = StatoLocazione.IN_LOCAZIONE;
						break;
					}

					PortataIncantesimo tipo = classeIncantesimo.getPortata();
					boolean suUnSoloBersaglio = tipo == PortataIncantesimo.SINGOLO_SOLO_VIVI || tipo == PortataIncantesimo.SINGOLO_QUALSIASI;
					// Un incantesimo benefico su un solo bersaglio (Resurrezione) si formula su un personaggio del gruppo,
					// e occorre chiedere quale sia
					if (suUnSoloBersaglio && classeIncantesimo.getTipo() == TipoIncantesimo.BENEFICO) {
						Logger.log("Incantesimo di tipo " + (tipo == PortataIncantesimo.SINGOLO_SOLO_VIVI ? "SINGOLO_SOLO_VIVI" : "SINGOLO_QUALSIASI"));
						int l = gruppo.getNumeroPersonaggi();
						Personaggio personaggio;
						List<Comando> comandiPossibiliBersaglio = new ArrayList<>();
						for (int i = 0; i < l; i++) {
							personaggio = gruppo.getPersonaggio(i);
							Logger.log("tipo == Incantesimo.SINGOLO_QUALSIASI || p.isVivo() ? " + ((tipo == PortataIncantesimo.SINGOLO_QUALSIASI || personaggio.isVivo())));
							if (tipo == PortataIncantesimo.SINGOLO_QUALSIASI || personaggio.isVivo()) {
								comandiPossibiliBersaglio.add(Comando.ofPersonaggio(i));
							}
						}
						BusEventi.pubblica(new InternoAggiornamentoComandiDisponibili(comandiPossibiliBersaglio));
						statoLocazione = StatoLocazione.SU_CHI_FORMULA;
						gruppoBersaglio = gruppo;
						return Stato.SCELTA_PERSONAGGIO_QUALSIASI;
					}

					// Un incantesimo benefico su tutto il gruppo (Alba Sacra) si formula sul proprio gruppo,
					// non su quello avversario
					if (!suUnSoloBersaglio && classeIncantesimo.getTipo() == TipoIncantesimo.BENEFICO) {
						incantesimo.formula(formulante, null, gruppo);

						if (!gruppo.getCapo().isVivo()) {
							return Stato.GIOCO_PERSO;
						}

						gruppo.subIncantesimi(incantesimo.getClasse(), 1);

						rispostaAvversaria(formulante, gruppo, gruppoAvversario);

						if (!gruppo.getCapo().isVivo()) {
							return Stato.GIOCO_PERSO;
						}

						statoLocazione = StatoLocazione.IN_LOCAZIONE;
						break;
					}

					// Altrimenti l'incantesimo agisce sul gruppo avversario (tutto o fino al numero di bersagli del
					// formulante, secondo la portata), su un solo avversario (Morte) o su tutta la locazione: il lancio,
					// costo in MAGIA compreso, lo fa l'incantesimo. Gli avversari che uccide contano nelle statistiche e
					// danno esperienza.
					List<Personaggio> avversariVivi = gruppoAvversario.getPersonaggiVivi();
					if (suUnSoloBersaglio) {
						// TODO far scegliere al giocatore su quale avversario formularlo, quando ci saranno le icone dei
						// mostri: per ora il primo ancora vivo, come per l'inizio del combattimento
						incantesimo.formula(formulante, gruppoAvversario.getPersonaggioVivo(), null);
					} else {
						incantesimo.formula(formulante, null, gruppoAvversario);
					}
					for (Personaggio avversario : avversariVivi) {
						registraUccisione(formulante, avversario);
					}

					if (!gruppo.getCapo().isVivo()) {
						return Stato.GIOCO_PERSO;
					}

					gruppo.subIncantesimi(incantesimo.getClasse(), 1);

					if (gruppoAvversario.getNumeroPersonaggiVivi() == 0) {
						setCompleta(true);
						return Stato.FINE_LOCAZIONE;
					}

					rispostaAvversaria(formulante, gruppo, gruppoAvversario);

					if (!gruppo.getCapo().isVivo()) {
						return Stato.GIOCO_PERSO;
					}

					statoLocazione = StatoLocazione.IN_LOCAZIONE;
					break;
				}

			case SU_CHI_FORMULA:
				Logger.log("LocazioneBase.SU_CHI_FORMULA");
				if (azione == Comando.ANNULLA) {
					// Annullare non e' un'azione: nessun turno trascorre, si ripresentano solo i comandi
					return annullaScelta();
				}
				Personaggio personaggioBersaglio = gruppoBersaglio.getPersonaggio(azione);
				Personaggio formulanteScelto = gruppo.getFormulante();
				incantesimo.formula(formulanteScelto, personaggioBersaglio, null);
				registraUccisione(formulanteScelto, personaggioBersaglio);
				gruppo.subIncantesimi(incantesimo.getClasse(), 1);
				rispostaAvversaria(formulanteScelto, gruppo, gruppoAvversario);
				if (!gruppo.getCapo().isVivo()) {
					return Stato.GIOCO_PERSO;
				}
				statoLocazione = StatoLocazione.IN_LOCAZIONE;
				break;

			case CHI_CORROMPE:
				Logger.log("LocazioneBase.CHI_CORROMPE");
				if (azione == Comando.ANNULLA) {
					// Annullare non e' un'azione: nessun turno trascorre, si ripresentano solo i comandi
					return annullaScelta();
				}
				if (gruppo.getMonete() >= gruppo.getNumeroPersonaggi() * 2 && Dado.tira(10) > 3) {
					gruppo.subMonete(gruppo.getNumeroPersonaggi() * 2);
					BusEventi.pubblica(new NotificaTestoFrase(gruppo.chiMaiuscolo() + " ha ottenuto un passaggio sicuro."));
					BusEventi.pubblica(new InternoCorruzioneRiuscita(classiAvversariVivi()));
					setOggetto(null);

					offerta = gruppoAvversario.getCapo().getOfferta(Comando.CORRUZIONE);
					if (offerta != null && offerta.isFattibile(gruppo, gruppoAvversario)) {
						BusEventi.pubblica(new NotificaTestoFrase(offerta.getDescrizione(gruppo, gruppoAvversario)));
						if (offerta.isGratuita(gruppo, gruppoAvversario)) {
							offerta.accetta(gruppo, gruppoAvversario);
						} else {
							BusEventi.pubblica(new NotificaTestoFrase("Accetta?"));
							BusEventi.pubblica(new RichiestaSelezioneSiNo());
							statoLocazione = StatoLocazione.ACCETTA_OFFERTA;
							return Stato.IN_LOCAZIONE;
						}
					} else {
						Logger.log("Mancano i prerequisiti per l'offerta");
					}
					setCompleta(true);
					return Stato.FINE_LOCAZIONE;
				} else {
					Personaggio p = gruppo.getPersonaggio(azione);
					BusEventi.pubblica(new NotificaTestoFrase("Il tentativo di corruzione " + p.getNome(Personaggio.OpzioniGetNome.INCLUDI_PREPOSIZIONE_ARTICOLATA) +
							" non ha avuto successo."));
					opzioneCorruzioneDisponibile = false;
					opzioneAmiciziaDisponibile = false;
					statoLocazione = StatoLocazione.IN_LOCAZIONE;
					break;
				}

			case CHI_FA_AMICIZIA:
				Logger.log("LocazioneBase.CHI_FA_AMICIZIA (azione " + azione + ")");
				if (azione == Comando.ANNULLA) {
					// Annullare non e' un'azione: nessun turno trascorre, si ripresentano solo i comandi
					return annullaScelta();
				}
				Personaggio personaggio = gruppo.getPersonaggio(azione);
				int tiroDelDado = Dado.tira(12);
				Logger.log("Carisma personaggio: " + personaggio.getCarisma() + "; tiro del dado: " + tiroDelDado);
				if (personaggio.getCarisma() > tiroDelDado) {
					personaggio.addCarisma(1);
					haStrettoAmicizia = true;
					BusEventi.pubblica(new InternoAmiciziaStretta(classiAvversariVivi()));
					BusEventi.pubblica(new NotificaTestoFrase(personaggio.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
							Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA) + " riesce a stringere amicizia."));

					offerta = gruppoAvversario.getCapo().getOfferta(Comando.AMICIZIA);
					if (offerta != null && offerta.isFattibile(gruppo, gruppoAvversario)) {
						BusEventi.pubblica(new NotificaTestoFrase(offerta.getDescrizione(gruppo, gruppoAvversario)));
						if (offerta.isGratuita(gruppo, gruppoAvversario)) {
							offerta.accetta(gruppo, gruppoAvversario);
						} else {
							BusEventi.pubblica(new NotificaTestoFrase("Accetta?"));
							BusEventi.pubblica(new RichiestaSelezioneSiNo());
							statoLocazione = StatoLocazione.ACCETTA_OFFERTA;
							return Stato.IN_LOCAZIONE;
						}
					} else {
						Logger.log("Mancano i prerequisiti per l'offerta");
					}
					setCompleta(true);
					return Stato.FINE_LOCAZIONE;
				} else {
					int spregio = Dado.tira(5);
					String descrizione = null;
					switch (spregio) {
						case 1:
							ClasseIncantesimo quale = ClasseIncantesimo.casuale();
							if (gruppo.getIncantesimi(quale) > 0) {
								descrizione = "perde un " + quale.getNomeSingolare() + '.';
								gruppo.subIncantesimi(quale, 1);
							}
							break;
						case 2:
							Personaggio avversario = gruppoAvversario.getCapo();
							int ferite = Dado.tiraAncheAUnaFaccia(avversario.getSalute());
							if (ferite < 20) {
								descrizione = "riceve alcune lievi ferite.";
							} else if (ferite > 40) {
								descrizione = "riceve gravi ferite.";
							} else {
								descrizione = "riceve alcune ferite.";
							}
							personaggio.subSalute(ferite, avversario, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.SI);
							break;
						case 3:
							if (gruppo.getMonete() > 0) {
								descrizione = "perde alcune monete.";
								int quanteMonetePerde = Dado.tira(5);
								if (quanteMonetePerde > gruppo.getMonete()) {
									quanteMonetePerde = gruppo.getMonete();
								}
								gruppo.subMonete(quanteMonetePerde);
							}
							break;
						case 4:
							if (gruppo.getPreziosi() > 0) {
								descrizione = "perde alcuni preziosi.";
								int quantiPreziosiPerde = Dado.tira(5);
								if (quantiPreziosiPerde > gruppo.getPreziosi()) {
									quantiPreziosiPerde = gruppo.getPreziosi();
								}
								gruppo.subPreziosi(quantiPreziosiPerde);
							}
							break;
						default:
							break;
					}

					String s = personaggio.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE);
					if (descrizione != null) {
						BusEventi.pubblica(new NotificaTestoFrase("Non solo " + s + " non riesce a stringere amicizia, ma in una breve colluttazione " + descrizione));
						// Non sapendo cosa andiamo a perdere rinfreschiamo tutto
						BusEventi.pubblica(new InternoRichiestaRefreshUI());
					} else {
						BusEventi.pubblica(new NotificaTestoFrase(s + " non riesce a stringere amicizia."));
					}
					opzioneAmiciziaDisponibile = false;
					statoLocazione = StatoLocazione.IN_LOCAZIONE;
					break;
				}

			case ACCETTA_OFFERTA:
				if (azione == Comando.SI) {
					offerta.accetta(gruppo, gruppoAvversario);
				}
				setCompleta(true);
				return Stato.FINE_LOCAZIONE;

			case CONFERMA_FUGA:
				if (azione == Comando.SI) {
					setCompleta(false);
					setOggetto(null);
					gruppo.fugge();
					if (!gruppo.getCapo().isVivo()) {
						return Stato.GIOCO_PERSO;
					} else {
						return Stato.FINE_LOCAZIONE;
					}
				} else if (azione == Comando.NO) {
					// Rinunciare alla fuga non fa trascorrere il turno
					return annullaScelta();
				} else {
					// Qualunque altro comando: si attende ancora il si' o il no
					return Stato.ATTESA_SI_NO;
				}
		}

		Stato dopoGliEffetti = trascorriTurnoEffettiDiStato();
		if (dopoGliEffetti != null) {
			return dopoGliEffetti;
		}

		Logger.log("LocazioneBase.impostaAzioni() continua...");
		if (isCompleta()) {
			return Stato.FINE_LOCAZIONE;
		}

		impostaComandiPossibili();

		return statoLocazione == StatoLocazione.IN_COMBATTIMENTO ? Stato.IN_COMBATTIMENTO : Stato.IN_LOCAZIONE;
	}

	@Override
	public void ripresentaComandi() {
		impostaComandiPossibili();
	}

	/**
	 * Una scelta annullata (chi combatte, chi formula, quale incantesimo, su chi, chi corrompe, chi fa amicizia, la
	 * conferma della fuga): si torna in locazione senza far trascorrere un turno di effetti di stato.
	 */
	private Stato annullaScelta() {
		statoLocazione = StatoLocazione.IN_LOCAZIONE;
		ripresentaComandi();
		return Stato.IN_LOCAZIONE;
	}

	/**
	 * Il dardo arcano di Mago ed Elfo contro il primo avversario vivo: costa MAGIA, non consuma pergamene.
	 *
	 * @return lo stato in cui passare se lo scontro finisce (avversari tutti morti o gioco perso), altrimenti null
	 */
	private Stato lanciaDardoArcano(Personaggio formulante, GruppoGiocatore gruppo, GruppoAvversario gruppoAvversario) {
		opzioneCorruzioneDisponibile = false;
		opzioneAmiciziaDisponibile = false;
		Personaggio bersaglio = gruppoAvversario.getPersonaggioVivo();
		if (bersaglio == null || !DardoArcano.puoLanciarlo(formulante)) {
			return null;
		}
		DardoArcano dardo = new DardoArcano(formulante);
		BusEventi.pubblica(new NotificaTestoFrase(formulante.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
				Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA) + " scaglia un dardo arcano contro "
				+ bersaglio.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE) + "."));
		if (CalcolatoreCombattimento.colpisce(formulante, bersaglio, dardo.getTipoDanno().getSuperTipo())) {
			bersaglio.applicaRisultatoCombattimento(CalcolatoreCombattimento.calcolaDannoRisultante(formulante, bersaglio, dardo));
			registraUccisione(formulante, bersaglio);
		}
		formulante.subMagia(dardo.getCostoLancio());
		if (gruppoAvversario.getNumeroPersonaggiVivi() == 0) {
			setCompleta(true);
			return Stato.FINE_LOCAZIONE;
		}
		rispostaAvversaria(formulante, gruppo, gruppoAvversario);
		if (!gruppo.getCapo().isVivo()) {
			return Stato.GIOCO_PERSO;
		}
		return null;
	}

	private void impostaComandiPossibili() {
		List<Comando> comandiPossibili = new ArrayList<>();
		// Possiamo combattere? Oppure, vogliamo cambiare chi combatte?
		if (statoLocazione != StatoLocazione.IN_COMBATTIMENTO || gruppo.getNumeroPersonaggiVivi() > 1) {
			comandiPossibili.add(Comando.SINGOLO_ATTACCO);
			comandiPossibili.add(Comando.COMBATTIMENTO);
		}
		// Se stiamo combattendo possiamo interrompere la schermaglia
		if (statoLocazione == StatoLocazione.IN_COMBATTIMENTO) {
			comandiPossibili.add(Comando.INTERRUZIONE_COMBATTIMENTO);
		}
		// Possiamo formulare incantesimi? Si se ne abbiamo almeno uno e se uno dei personaggi vivi può lanciarlo,
		// oppure se un Mago o un Elfo vivo ha la MAGIA per il dardo arcano
		boolean incantesimoPossibile = gruppo.getPersonaggiVivi().stream().anyMatch(DardoArcano::puoLanciarlo);
		for (ClasseIncantesimo classeIncantesimo : ClasseIncantesimo.values()) {
			if (gruppo.getIncantesimi(classeIncantesimo) > 0 &&
					gruppo.getPersonaggiVivi().stream().anyMatch(p -> p.puoFormulare(classeIncantesimo))) {
				incantesimoPossibile = true;
				break;
			}
		}
		if (incantesimoPossibile) {
			comandiPossibili.add(Comando.INCANTESIMO);
		}
		// Possiamo corrompere gli avversari?
		if (opzioneCorruzioneDisponibile) {
			comandiPossibili.add(Comando.CORRUZIONE);
		}
		// Possiamo fare amicizia?
		if (opzioneAmiciziaDisponibile) {
			comandiPossibili.add(Comando.AMICIZIA);
		}
		if (statoLocazione != StatoLocazione.IN_COMBATTIMENTO) {
			comandiPossibili.add(Comando.MAPPA);
		}
		if (gruppo.getPozioniSalute() > 0) {
			comandiPossibili.add(Comando.POZIONE_SALUTE);
		}
		if (gruppo.getPozioniSaluteGrande() > 0) {
			comandiPossibili.add(Comando.POZIONE_SALUTE_GRANDE);
		}
		if (gruppo.getPozioniMagia() > 0) {
			comandiPossibili.add(Comando.POZIONE_MAGIA);
		}
		if (gruppo.getPozioniMagiaGrande() > 0) {
			comandiPossibili.add(Comando.POZIONE_MAGIA_GRANDE);
		}
		// Possiamo sempre controllare l'inventario
		if (statoLocazione != StatoLocazione.IN_COMBATTIMENTO) {
			comandiPossibili.add(Comando.INVENTARIO);
		}
		// Si può sempre ricorrere a una bella...
		comandiPossibili.add(Comando.FUGA);
		// ...o provare, una volta, a passare inosservati
		if (isPassaggioPossibile()) {
			comandiPossibili.add(Comando.PASSA_INOSSERVATO);
		}
		BusEventi.pubblica(new InternoAggiornamentoComandiDisponibili(comandiPossibili));
	}

	/**
	 * Il giocatore ha portato in fondo la locazione o è fuggito?
	 */
	public boolean isCompleta() {
		return md.ottieniProprieta(LocazioneMD.COMPLETA) != null;
	}

	/**
	 * Alla fine di un turno un gruppo rade al suolo una locazione e si può verificare
	 * qualcosa; per adesso al momento in cui il giocatore rade al suolo quattro castelli
	 * appare quello del drago
	 */
	public void azzeraLocazione(GruppoGiocatore g) {
		// Per evitare che dopo un ricaricamento successivo a una partita persa rimanga in locazione il mostro
		// che ha sconfitto il giocatore nella partita precedente.
		gruppoAvversario.rimuoviPersonaggi();
		if (isCompleta()) {
			g.setLocazioneCorrenteVisitata();
		}
		for (Personaggio p : g.getPersonaggi()) {
			if (p.hasEffettoDiStato(TipoEffettoDiStato.BERSERK)) {
				p.rimuoviEffettoDiStato(TipoEffettoDiStato.BERSERK);
			}
		}
	}

	/**
	 * La descrizione dei presenti
	 */
	protected String descrizioneMostriEOggetti(GruppoGiocatore gruppoGiocatore, GruppoAvversario gruppoAvversario) {
		int numeroAvversari = gruppoAvversario.getNumeroPersonaggi();
		Personaggio p = gruppoAvversario.getCapo();
		Oggetto o = getOggetto();
		int numeroOggetti = (o == null ? 0 : o.getQuantita());
		StringBuilder sb = new StringBuilder(gruppoGiocatore.getCapo().getNome(Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA, Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE));
		if (numeroAvversari == 0 && numeroOggetti == 0) {
			sb.append(" non trova nulla");
		} else {
			sb.append(" vede ");
			if (numeroAvversari > 0) {
				if (numeroAvversari == 1) {
					sb.append(p.getAIS()).append(p.getNomeSingolare());
					if (numeroOggetti > 0) {
						switch (Dado.tira(3)) {
							case 1:
								sb.append(" che protegge ");
								break;
							case 2:
								sb.append(" che custodisce ");
								break;
							default:
								sb.append(" che sorveglia ");
								break;
						}
					}
				} else {
					sb.append(Misc.getCardinaleM(numeroAvversari)).append(' ').append(p.getNomePlurale());
					if (numeroOggetti > 0) {
						switch (Dado.tira(3)) {
							case 1:
								sb.append(" che proteggono ");
								break;
							case 2:
								sb.append(" che custodiscono ");
								break;
							default:
								sb.append(" che sorvegliano ");
								break;
						}
					}
				}
			}
			if (numeroOggetti > 0) {
				if (numeroOggetti == 1) {
					if (o.getClasse() == ClassiOggetto.ARTEFATTO) {
						Artefatto artefatto = (Artefatto)o;
						sb.append(artefatto.getNomeCompleto());
					} else {
						sb.append(o.getAIS()).append(o.getNomeSingolare());
					}
				} else {
					sb.append(Misc.getCardinaleM(numeroOggetti)).append(' ').append(o.getNomePlurale());
				}
			}
		}
		sb.append('.');
		return sb.toString();
	}

	// La metodologia usata ora è un round-robin. Questo fa si che se il personaggio che dovrebbe attaccare non è in
	// grado di farlo, salta il turno - e questo da un senso agli effetti di stato.
	private void rispostaAvversaria(Personaggio personaggioBersaglio, GruppoGiocatore gruppo, GruppoAvversario gruppoAvversario) {

		Personaggio avversarioAttaccante = gruppoAvversario.getProssimoAttaccante();
		if (avversarioAttaccante == null ||
				avversarioAttaccante.hasEffettoDiStato(TipoEffettoDiStato.ATTERRATO) ||
				avversarioAttaccante.hasEffettoDiStato(TipoEffettoDiStato.CONGELATO) ||
				avversarioAttaccante.hasEffettoDiStato(TipoEffettoDiStato.STORDITO)) {
			return;
		}
		if (gruppo.getNumeroPersonaggiVivi() == 0) {
			return;
		}
		// Chi ha appena agito può essere morto (es. un Morte che gli si è ritorto contro): allora il mostro
		// sceglie fra i vivi del gruppo
		if (personaggioBersaglio == null || personaggioBersaglio.isFuoriCombattimento()) {
			avversarioAttaccante.attacca(gruppo);
		} else {
			avversarioAttaccante.attacca(personaggioBersaglio);
		}
	}

	protected void setOggetto(Oggetto oggetto) {
		if (oggetto != null && oggetto.getQuantita() > 0) {
			this.oggettoCorrente = oggetto;
		} else {
			this.oggettoCorrente = null;
		}
	}

	public Oggetto getOggetto() {
		return oggettoCorrente;
	}

	public void rimuoviOggetto() {
		if (oggettoCorrente instanceof Artefatto) {
			// L'unico artefatto che una locazione può offrire è quello del registro (vedi crea):
			// una volta raccolto va tolto, altrimenti a ogni nuova visita si ripresenta e se
			// ne ottengono infinite copie. Corruzione e fuga usano setOggetto(null), che invece
			// lo lascia al suo posto.
			RegistroArtefatti.rimuoviArtefattoInLocazione(gruppo.getCoordinate());
		}
		oggettoCorrente = null;
	}

	@Override
	public void collocaOggettoMissione(Oggetto oggetto) {
		if (!(oggettoCorrente instanceof Artefatto)) {
			setOggetto(oggetto);
		}
	}

	@Override
	public boolean isCustodita() {
		return custodita;
	}

	public boolean isHaStrettoAmicizia() {
		return haStrettoAmicizia;
	}
	
	private Stato gestisciNuovaLocazione() {
		Logger.log("LocazioneBase.NUOVA_LOCAZIONE");
		CategoriaLocazione tipoLocazione = gruppo.getClasseLocazioneCorrente().getCategoria();
		int numeroAvversari = gruppoAvversario.getNumeroPersonaggi();
		custodita = numeroAvversari > 0;
		if (numeroAvversari == 0) {
			if (oggettoCorrente != null) {
				BusEventi.pubblica(new NotificaTestoFrase("Essendo il tesoro incustodito, " +
						gruppo.chi() + " se ne impossessa."));
			}
			if (tipoLocazione != CategoriaLocazione.MISSIONE_SECONDARIA) {
				gruppo.riposa(getTipoRiposo());
			}
			setCompleta(true);
			return Stato.FINE_LOCAZIONE;
		} else {
			setCompleta(false);
			// Non possiamo fare amicizia o corrompere per completare le missioni secondarie! E nemmeno con delle
			// ondate in arrivo: corrotta la prima, non arriverebbero le altre
			//TODO il meccanismo delle missioni andrebbe gestito meglio
			if (tipoLocazione != CategoriaLocazione.MISSIONE_SECONDARIA && !gruppoAvversario.hasOndateSuccessive()) {
				Personaggio p;
				for (int i = 0; i < numeroAvversari; i++) {
					p = gruppoAvversario.getPersonaggio(i);
					if (p.isCorrompibile() && gruppo.getMonete() > 0) {
						opzioneCorruzioneDisponibile = true;
						break;
					}
				}
				// Possiamo fare amicizia?
				for (int i = 0; i < numeroAvversari; i++) {
					p = gruppoAvversario.getPersonaggio(i);
					if (p.isAmichevole()) {
						opzioneAmiciziaDisponibile = true;
						break;
					}
				}
			}
			if (gruppoAvversario.isDuello()) {
				// Una sfida a duello: prima si sceglie chi la accetta, o la si rifiuta
				opzioneAmiciziaDisponibile = false;
				opzioneCorruzioneDisponibile = false;
				BusEventi.pubblica(new NotificaTestoFrase(nomeDelloSfidante() + (gruppo.getNumeroPersonaggiVivi() > 1
						? " sfida a duello uno di voi: chi accetta la sfida?" : " vi sfida a duello.")));
				statoLocazione = StatoLocazione.CHI_DUELLA;
				return Stato.SCELTA_AUTOMATICA_PERSONAGGIO;
			}
		}
		statoLocazione = StatoLocazione.IN_LOCAZIONE;
		return null;
	}
	
	/**
	 * Un turno di effetti di stato: i danni nel tempo vengono inflitti e le durate scendono, prima per il gruppo e
	 * poi per gli avversari. Fuori dal combattimento a ogni passo della locazione, in combattimento a ogni round.
	 *
	 * @return GIOCO_PERSO se muore il capo, FINE_LOCAZIONE se muoiono tutti gli avversari, altrimenti null
	 */
	/**
	 * Se il colpo appena subito ha ucciso un avversario per mano di un personaggio del gruppo, lo conta nelle
	 * statistiche e ne da' i punti esperienza al gruppo: in mischia, con gli incantesimi di gruppo e con il dardo.
	 * I compagni colpiti da un incantesimo globale non contano.
	 */
	private void registraUccisione(Personaggio uccisore, Personaggio vittima) {
		if (gruppo.contiene(uccisore)) {
			registraMorteAvversario(vittima);
		}
	}

	private List<ClassePersonaggio> classiAvversariVivi() {
		List<ClassePersonaggio> classi = new ArrayList<>();
		for (Personaggio avversario : gruppoAvversario.getPersonaggiVivi()) {
			classi.add(avversario.getClasse());
		}
		return classi;
	}

	/**
	 * Un avversario morto o arreso conta nelle statistiche e da' i punti esperienza al gruppo, una volta sola: per
	 * mano di un personaggio del gruppo (vedi registraUccisione) o per veleno, sanguinamento e gli altri effetti di
	 * stato.
	 */
	private void registraMorteAvversario(Personaggio vittima) {
		if (vittima.isFuoriCombattimento() && !gruppo.contiene(vittima) && avversariSconfitti.add(vittima)) {
			BusEventi.pubblica(new InternoAvversarioSconfitto(vittima.getClasse()));
			Statistiche.addPunti(vittima.getSaluteMassima());
			gruppo.addPuntiEsperienza(vittima.getPuntiEsperienza());
		}
	}

	private Stato trascorriTurnoEffettiDiStato() {
		for (Personaggio personaggio : gruppo.getPersonaggiVivi()) {
			personaggio.applicaDanniDaEffettiDiStato();
			personaggio.riduciEffettiDiStato();
		}
		if (!gruppo.getCapo().isVivo()) {
			return Stato.GIOCO_PERSO;
		}

		for (Personaggio personaggio : gruppoAvversario.getPersonaggiVivi()) {
			personaggio.applicaDanniDaEffettiDiStato();
			personaggio.riduciEffettiDiStato();
			registraMorteAvversario(personaggio);
		}
		if (gruppoAvversario.getPersonaggiVivi().isEmpty()) {
			return Stato.FINE_LOCAZIONE;
		}
		return null;
	}

	/**
	 * Alla fine di un round di mischia passa anche un turno di effetti di stato (altrimenti, dato che il
	 * combattimento non esce mai da gestisciCombattimento, veleni e stordimenti non avanzerebbero mai). Se ne muore
	 * il combattente la mischia si interrompe, come quando lo uccide un colpo.
	 *
	 * @return lo stato con cui chiudere il round, oppure null se il combattimento continua
	 */
	private Stato dopoIlRound() {
		Stato dopoGliEffetti = trascorriTurnoEffettiDiStato();
		if (dopoGliEffetti == Stato.FINE_LOCAZIONE) {
			setCompleta(true);
		}
		if (dopoGliEffetti != null) {
			return dopoGliEffetti;
		}
		if (combattente.isFuoriCombattimento()) {
			statoLocazione = StatoLocazione.IN_LOCAZIONE;
			return Stato.IN_LOCAZIONE;
		}
		if (!gruppo.getCapo().isVivo()) {
			return Stato.GIOCO_PERSO;
		}
		return null;
	}

	private Optional<Stato> eseguiSingoloAttacco() {

		Personaggio bersaglio = gruppoAvversario.getPersonaggioVivo();
		if (bersaglio == null) {
			setCompleta(true);
			return Optional.of(Stato.FINE_LOCAZIONE);
		}

		BusEventi.pubblica(new InternoRichiestaAperturaFinestraCombattimento(combattente, bersaglio));

		Logger.log(combattente.getNome() + " attacca " + bersaglio.getNome());

		Logger.log("Valutazione combattente -> bersaglio");
		// Chi combatte con due armi ha una seconda fase, con l'arma secondaria, sullo stesso bersaglio
		// (o sul prossimo vivo, se la prima l'ha ucciso)
		for (FaseDiAttacco fase : CalcolatoreCombattimento.fasiDiAttacco(combattente)) {
			Arma arma = fase.getArma();
			boolean colpisce = CalcolatoreCombattimento.colpisce(combattente, bersaglio, arma.getTipoDanno().getSuperTipo());
			if (colpisce) {
				DannoRisultante risultato = CalcolatoreCombattimento.calcolaDannoRisultante(combattente, bersaglio, arma, fase.getFattore());
				bersaglio.applicaRisultatoCombattimento(risultato);
				if (bersaglio.isFuoriCombattimento()) {
					registraUccisione(combattente, bersaglio);
					Personaggio nuovoBersaglio = gruppoAvversario.getPersonaggioVivo();
					if (nuovoBersaglio != null) {
						bersaglio = nuovoBersaglio;
					} else {
						setCompleta(true);
						return Optional.of(Stato.FINE_LOCAZIONE);
					}
				}
			}
		}
		Logger.log("Valutazione bersaglio -> combattente");

		// L'avversario risponde sul combattente: con le armi o, se sa la magia, con un incantesimo
		bersaglio.rispondiInMischia(combattente);
		if (combattente.isFuoriCombattimento()) {
			if (gruppo.getCapo().isVivo()) {
				statoLocazione = StatoLocazione.IN_LOCAZIONE;
				return Optional.of(Stato.IN_LOCAZIONE);
			} else {
				return Optional.of(Stato.GIOCO_PERSO);
			}
		}

		if (gruppoAvversario.getNumeroPersonaggiVivi() > gruppo.getNumeroPersonaggiVivi()) {
			bersaglio = gruppoAvversario.getPersonaggioVivo();
			String sb = bersaglio.getNome(Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA, Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE) +
					" si disimpegna e attacca!";
			BusEventi.pubblica(new NotificaTestoFrase(sb));
			bersaglio.attacca(gruppo);
		}
		if (!gruppo.getCapo().isVivo()) {
			return Optional.of(Stato.GIOCO_PERSO);
		}
		Stato dopoIlRound = dopoIlRound();
		if (dopoIlRound != null) {
			return Optional.of(dopoIlRound);
		}

		return Optional.empty();
	}

	private Stato gestisciCombattimento(Comando azione) {
		Logger.log("LocazioneBase.IN_COMBATTIMENTO");
		if (azione == Comando.INTERRUZIONE_COMBATTIMENTO) {
			BusEventi.pubblica(new InternoRichiestaChiusuraFinestraCombattimento());
			statoLocazione = StatoLocazione.IN_LOCAZIONE;
			combattente = null;
			return Stato.IN_LOCAZIONE;
		}
		if (azione.isPersonaggio()) {
			combattente = gruppo.getPersonaggio(azione);
			impostaComandiPossibili();
			return Stato.IN_COMBATTIMENTO;
		}
		if (azione == Comando.COMBATTIMENTO && combattente != null) {
			// Il combattente corrente non va azzerato: se la scelta viene annullata
			// deve poter continuare a combattere lui.
			return Stato.SCELTA_AUTOMATICA_PERSONAGGIO;
		}
		if (azione == Comando.ANNULLA) {
			// Scelta del nuovo combattente annullata: si continua con quello corrente
			impostaComandiPossibili();
			return Stato.IN_COMBATTIMENTO;
		}
		if (azione == Comando.COMBATTIMENTO || azione == Comando.TIMER) {
			Optional<Stato> stato = eseguiSingoloAttacco();
			if (stato.isPresent()) {
				return stato.get();
			}
		}
		if (azione == Comando.MAPPA) {
			return Stato.MAPPA;
		}
		if (azione == Comando.INVENTARIO) {
			return Stato.INVENTARIO;
		}
		if (azione == Comando.POZIONE_SALUTE) {
			if (gruppo.getNumeroPersonaggiVivi() > 1) {
				// Bere interrompe la mischia (come l'incantesimo): la sua finestra si chiude
				BusEventi.pubblica(new InternoRichiestaChiusuraFinestraCombattimento());
				statoLocazione = StatoLocazione.CHI_BEVE_POZIONE_SALUTE;
				return Stato.SCELTA_AUTOMATICA_PERSONAGGIO;
			} else {
				gruppo.consumaPozioneSalute(gruppo.getPersonaggiVivi().get(0));
				return Stato.IN_COMBATTIMENTO;
			}
		}
		if (azione == Comando.POZIONE_SALUTE_GRANDE) {
			if (gruppo.getNumeroPersonaggiVivi() > 1) {
				// Bere interrompe la mischia (come l'incantesimo): la sua finestra si chiude
				BusEventi.pubblica(new InternoRichiestaChiusuraFinestraCombattimento());
				statoLocazione = StatoLocazione.CHI_BEVE_POZIONE_SALUTE_GRANDE;
				return Stato.SCELTA_AUTOMATICA_PERSONAGGIO;
			} else {
				gruppo.consumaPozioneSaluteGrande(gruppo.getPersonaggiVivi().get(0));
				return Stato.IN_COMBATTIMENTO;
			}
		}
		if (azione == Comando.POZIONE_MAGIA) {
			if (gruppo.getNumeroPersonaggiVivi() > 1) {
				// Bere interrompe la mischia (come l'incantesimo): la sua finestra si chiude
				BusEventi.pubblica(new InternoRichiestaChiusuraFinestraCombattimento());
				statoLocazione = StatoLocazione.CHI_BEVE_POZIONE_MAGIA;
				return Stato.SCELTA_AUTOMATICA_PERSONAGGIO;
			} else {
				gruppo.consumaPozioneMagia(gruppo.getPersonaggiVivi().get(0));
				return Stato.IN_COMBATTIMENTO;
			}
		}
		if (azione == Comando.POZIONE_MAGIA_GRANDE) {
			if (gruppo.getNumeroPersonaggiVivi() > 1) {
				// Bere interrompe la mischia (come l'incantesimo): la sua finestra si chiude
				BusEventi.pubblica(new InternoRichiestaChiusuraFinestraCombattimento());
				statoLocazione = StatoLocazione.CHI_BEVE_POZIONE_MAGIA_GRANDE;
				return Stato.SCELTA_AUTOMATICA_PERSONAGGIO;
			} else {
				gruppo.consumaPozioneMagiaGrande(gruppo.getPersonaggiVivi().get(0));
				return Stato.IN_COMBATTIMENTO;
			}
		}
		if (azione == Comando.INCANTESIMO) {
			BusEventi.pubblica(new InternoRichiestaChiusuraFinestraCombattimento());
			statoLocazione = StatoLocazione.CHI_FORMULA;
			return Stato.SCELTA_AUTOMATICA_PERSONAGGIO;
		}
		if (azione == Comando.FUGA) {
			BusEventi.pubblica(new InternoRichiestaChiusuraFinestraCombattimento());
			chiediConfermaPerLaFuga();
			statoLocazione = StatoLocazione.CONFERMA_FUGA;
			BusEventi.pubblica(new RichiestaSelezioneSiNo());
			return Stato.ATTESA_SI_NO;
		}
		return Stato.IN_COMBATTIMENTO;
	}
	
	private Stato gestisciInLocazione(Comando azione) {
		BusEventi.pubblica(new InternoMessaggio("LocazioneBase.IN_LOCAZIONE, Comando: " + azione));
		if (azione != null) {
			switch (azione) {

				case SINGOLO_ATTACCO:
					statoLocazione = StatoLocazione.CHI_ESEGUE_SINGOLO_ATTACCO;
					return Stato.SCELTA_AUTOMATICA_PERSONAGGIO;

				case COMBATTIMENTO:
					statoLocazione = StatoLocazione.CHI_COMBATTE;
					return Stato.SCELTA_AUTOMATICA_PERSONAGGIO;

				case PASSA_INOSSERVATO:
					return isPassaggioPossibile() ? passaInosservati() : null;

				case INCANTESIMO:
					statoLocazione = StatoLocazione.CHI_FORMULA;
					return Stato.SCELTA_AUTOMATICA_PERSONAGGIO;

				case CORRUZIONE:
					statoLocazione = StatoLocazione.CHI_CORROMPE;
					return Stato.SCELTA_AUTOMATICA_PERSONAGGIO;

				case AMICIZIA:
					statoLocazione = StatoLocazione.CHI_FA_AMICIZIA;
					return Stato.SCELTA_AUTOMATICA_PERSONAGGIO;

				case MAPPA:
					return Stato.MAPPA;

				case INVENTARIO:
					return Stato.INVENTARIO;

				case POZIONE_SALUTE:
					statoLocazione = StatoLocazione.CHI_BEVE_POZIONE_SALUTE;
					return Stato.SCELTA_AUTOMATICA_PERSONAGGIO;

				case POZIONE_SALUTE_GRANDE:
					statoLocazione = StatoLocazione.CHI_BEVE_POZIONE_SALUTE_GRANDE;
					return Stato.SCELTA_AUTOMATICA_PERSONAGGIO;

				case POZIONE_MAGIA:
					statoLocazione = StatoLocazione.CHI_BEVE_POZIONE_MAGIA;
					return Stato.SCELTA_AUTOMATICA_PERSONAGGIO;

				case POZIONE_MAGIA_GRANDE:
					statoLocazione = StatoLocazione.CHI_BEVE_POZIONE_MAGIA_GRANDE;
					return Stato.SCELTA_AUTOMATICA_PERSONAGGIO;

				case FUGA:
					chiediConfermaPerLaFuga();
					statoLocazione = StatoLocazione.CONFERMA_FUGA;
					BusEventi.pubblica(new RichiestaSelezioneSiNo());
					return Stato.ATTESA_SI_NO;

				default:
					break;
			}
		}
		return null;
	}

	private void chiediConfermaPerLaFuga() {
		if (gruppo.getNumeroPersonaggiVivi() > 1) {
			BusEventi.pubblica(new NotificaTestoFrase("Il gruppo è sicuro di voler fuggire?"));
		} else {
			Personaggio capo = gruppo.getCapo();
			String sb = capo.getNome(Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA, Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE) + " è sicur" +
					capo.getLetteraFinaleAttributo() +
					" di voler fuggire?";
			BusEventi.pubblica(new NotificaTestoFrase(sb));
		}
	}

	/**
	 * A differenza di CHI_COMBATTE/IN_COMBATTIMENTO (che avvia una mischia a round continui
	 * scandita da Comando.TIMER), qui si esegue subito un solo round con eseguiSingoloAttacco()
	 * e si torna direttamente IN_LOCAZIONE.
	 */
	private Stato gestisciChiEsegueSingoloAttacco(Comando azione) {
		Logger.log("LocazioneBase.CHI_ESEGUE_SINGOLO_ATTACCO");
		combattente = gruppo.getPersonaggio(azione);
		String nome = combattente.getNome(Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA, Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE);
		BusEventi.pubblica(new NotificaTestoFrase(nome + " lancia un attacco."));
		opzioneAmiciziaDisponibile = false;
		opzioneCorruzioneDisponibile = false;
		Stato esito = eseguiSingoloAttacco().orElse(Stato.IN_LOCAZIONE);
		combattente = null;
		statoLocazione = StatoLocazione.IN_LOCAZIONE;
		if (esito == Stato.IN_LOCAZIONE) {
			impostaComandiPossibili();
		}
		return esito;
	}

	/**
	 * Se il gruppo può provare a passare inosservato: una volta sola, prima di fare qualunque altra cosa (vedi
	 * isAzione), fuori dal combattimento, in una locazione della
	 * foresta (non in una città, in un castello o nella locazione di una missione secondaria), con avversari che non
	 * vanno affrontati per forza (vedi Personaggio.isDaAffrontare), che non sfidano a duello e senza ondate in arrivo.
	 */
	private boolean isPassaggioPossibile() {
		return !passaggioEscluso && statoLocazione == StatoLocazione.IN_LOCAZIONE
				&& gruppo.getClasseLocazioneCorrente().getCategoria() == CategoriaLocazione.STANDARD
				&& !gruppoAvversario.getPersonaggiVivi().isEmpty() && !gruppoAvversario.isDaAffrontare() && !gruppoAvversario.isDuello()
				&& !gruppoAvversario.hasOndateSuccessive();
	}

	/**
	 * La probabilità, in percentuale, che il gruppo passi inosservato: la furtività del gruppo contro la percezione
	 * migliore fra gli avversari, dieci punti ogni punto di differenza a partire da quaranta. Il gruppo è furtivo
	 * quanto il suo membro più maldestro, a meno che un ladro non lo guidi: allora conta il ladro. Di notte è più
	 * facile, e ogni compagno oltre il primo fa rumore. Mai meno di cinque, mai più di settantacinque.
	 */
	public static int probabilitaDiPassareInosservati(Gruppo gruppo, Gruppo avversari, int ora) {
		List<Personaggio> vivi = gruppo.getPersonaggiVivi();
		OptionalInt ladro = vivi.stream()
				.filter(p -> p.getClasse() == ClassePersonaggio.LADRO || p.getClasse() == ClassePersonaggio.LADRA)
				.mapToInt(Personaggio::getFurtivita).max();
		int furtivita = ladro.isPresent() ? ladro.getAsInt() : vivi.stream().mapToInt(Personaggio::getFurtivita).min().orElse(0);
		int percezione = avversari.getPersonaggiVivi().stream().mapToInt(Personaggio::getPercezione).max().orElse(0);
		int probabilita = PASSAGGIO_BASE + PASSAGGIO_PER_PUNTO * (furtivita - percezione)
				- PASSAGGIO_PER_COMPAGNO * Math.max(0, vivi.size() - 1);
		if (ora < ORA_DELL_ALBA || ora > ORA_DEL_TRAMONTO) {
			probabilita += PASSAGGIO_DI_NOTTE;
		}
		return Math.max(PASSAGGIO_MINIMO, Math.min(PASSAGGIO_MASSIMO, probabilita));
	}

	/**
	 * Il gruppo prova a passare inosservato, e ci mette un'ora. Se ci riesce se ne va senza combattere, senza
	 * esperienza né bottino, e la locazione non è completa; se no, gli avversari se ne accorgono e attaccano per primi.
	 */
	private Stato passaInosservati() {
		passaggioEscluso = true;
		int probabilita = probabilitaDiPassareInosservati(gruppo, gruppoAvversario, LineaTemporale.getOra());
		LineaTemporale.aggiungiOre(1);
		if (Dado.tira(100) <= probabilita) {
			BusEventi.pubblica(new NotificaTestoParagrafo(gruppo.chiMaiuscolo() + " passa in silenzio, senza farsi notare, e si allontana."));
			BusEventi.pubblica(new InternoPassaggioInosservato());
			setCompleta(false);
			setOggetto(null);
			return Stato.FINE_LOCAZIONE;
		}
		BusEventi.pubblica(new NotificaTestoParagrafo("Un rametto si spezza: gli avversari vi hanno visti, e attaccano per primi!"));
		rispostaAvversaria(null, gruppo, gruppoAvversario);
		if (!gruppo.getCapo().isVivo()) {
			return Stato.GIOCO_PERSO;
		}
		impostaComandiPossibili();
		return Stato.IN_LOCAZIONE;
	}

	private String nomeDelloSfidante() {
		return gruppoAvversario.getCapo().getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
				Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA);
	}

	/**
	 * Chi accetta la sfida a duello resta in campo, gli altri vanno in panchina.
	 */
	private void gestisciChiDuella(Comando azione) {
		Personaggio duellante = gruppo.getPersonaggio(azione);
		boolean altri = false;
		for (Personaggio personaggio : gruppo.getPersonaggiVivi()) {
			if (personaggio != duellante) {
				personaggio.setInPanchina(true);
				altri = true;
			}
		}
		BusEventi.pubblica(new NotificaTestoFrase(duellante.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
				Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA) + " accetta la sfida" + (altri ? ": gli altri si fanno da parte." : ".")));
		statoLocazione = StatoLocazione.IN_LOCAZIONE;
	}

	/**
	 * La sfida rifiutata: il gruppo se ne va senza combattere, e lo sfidante resta lì ad aspettarlo.
	 */
	private Stato rifiutaIlDuello() {
		BusEventi.pubblica(new NotificaTestoFrase("Rifiutate la sfida. " + nomeDelloSfidante() + " vi aspetta qui, se cambiate idea."));
		statoLocazione = StatoLocazione.IN_LOCAZIONE;
		setCompleta(false);
		setOggetto(null);
		return Stato.FINE_LOCAZIONE;
	}

	private void gestisciChiCombatte(Comando azione) {
		Logger.log("LocazioneBase.CHI_COMBATTE");
		combattente = gruppo.getPersonaggio(azione);
		String nome = combattente.getNome(Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA, Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE);
		BusEventi.pubblica(new NotificaTestoFrase(nome + " inizia il combattimento."));
		BusEventi.pubblica(new InternoRichiestaAperturaFinestraCombattimento(combattente, gruppoAvversario.getPersonaggioVivo()));
		opzioneAmiciziaDisponibile = false;
		opzioneCorruzioneDisponibile = false;
		statoLocazione = StatoLocazione.IN_COMBATTIMENTO;
	}
}
