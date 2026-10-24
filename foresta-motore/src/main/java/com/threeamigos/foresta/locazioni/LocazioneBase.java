package com.threeamigos.foresta.locazioni;

import com.threeamigos.foresta.abilita.AbilitaInnate;
import com.threeamigos.foresta.abilita.FabbricaAbilita;
import com.threeamigos.foresta.artefatti.Arma;
import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.artefatti.leggendari.OggettoLeggendario;
import com.threeamigos.foresta.canti.CantoDelBardo;
import com.threeamigos.foresta.canti.FabbricaCanti;
import com.threeamigos.foresta.combattimento.*;
import com.threeamigos.foresta.effettidistato.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.*;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.eventi.richieste.RichiestaSelezioneSiNo;
import com.threeamigos.foresta.incantesimi.*;
import com.threeamigos.foresta.missioni.RegistroMissioni;
import com.threeamigos.foresta.modellodati.LocazioneMD;
import com.threeamigos.foresta.modellodati.tattiche.TatticaMD;
import com.threeamigos.foresta.modificatori.tipi.UnitaDurataModificatore;
import com.threeamigos.foresta.motore.*;
import com.threeamigos.foresta.offerte.FabbricaOfferte;
import com.threeamigos.foresta.offerte.Offerta;
import com.threeamigos.foresta.oggetti.Bottino;
import com.threeamigos.foresta.oggetti.BottinoArtefatto;
import com.threeamigos.foresta.oggetti.FabbricaOggetti;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.strumenti.dado.ContestoLancio;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.strumenti.logger.Logger;
import com.threeamigos.foresta.tattiche.EsecutoreTattiche;
import com.threeamigos.foresta.tattiche.RegistroTattiche;
import com.threeamigos.foresta.testi.Numeri;
import com.threeamigos.foresta.testi.ProduttoreDiSfideCasuale;
import com.threeamigos.foresta.tipi.*;

import java.util.*;
import java.util.stream.Collectors;

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

	private static final TipoPersonaggio[] NESSUN_INCONTRO = {};
	private static final TipoBottino[] NESSUN_OGGETTO = {};

	private final GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
	private final GruppoAvversario gruppoAvversario = GruppoAvversario.getIstanza();
	private final StatoDelCombattimento statoDelCombattimento = StatoDelCombattimento.getIstanza();

	/**
	 * Lo stato durevole della casella: nome, visitata, conosciuta, completa.
	 * Sopravvive alla visita e al salvataggio.
	 */
	private LocazioneMD md = new LocazioneMD(getTipoLocazione());

	private Bottino oggettoCorrente;
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
	// L'evocazione del necromante (vedi contaLoScheletroCaduto): gli scheletri caduti da quando ne ha evocato uno, le evocazioni che
	// aspettano il momento buono (a metà di un ciclo sugli avversari non si può aggiungerne) e quelle già fatte
	private int scheletriCaduti;
	private int evocazioniDaFare;
	private int evocazioniFatte;
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

	// Lo spregio per un tentativo di amicizia o di corruzione fallito (vedi subisciUnoSpregio): monete o preziosi persi al
	// massimo, e le ferite al massimo, in percentuale della salute che il personaggio ha in quel momento (e lievi o gravi
	// secondo quanto sono rispetto alla salute di prima)
	private static final int SPREGIO_MASSIMO = 3;
	private static final int SPREGIO_FERITE_MASSIME_PERCENTUALE = 20;
	private static final int SPREGIO_FERITE_LIEVI_PERCENTUALE = 8;
	private static final int SPREGIO_FERITE_GRAVI_PERCENTUALE = 15;

	// La sfida a carta, forbici e sasso in corso (stato SFIDA_CARTA_FORBICI_SASSO), che non si salva
	private PartitaCartaForbiciSasso sfida;

	// Chi sta combattendo
	private Personaggio combattente;

    private Incantesimo incantesimo;
	private Gruppo gruppoBersaglio;

	// Il turno di un'azione del giocatore è già stato chiuso da dopoAzioneDelGiocatore (che a fine round fa trascorrere gli effetti di
	// stato): il passaggio generico di impostaAzioniImpl non deve farli trascorrere una seconda volta. Vale anche quando non c'è stata
	// nessuna azione (si è solo scelto chi combatte o chi duella, o un canto o un dardo non sono partiti): senza che gli avversari agiscano
	// non passa nessun turno, altrimenti ripetendo la scelta si avvelenerebbero i mostri "gratis"
	private boolean turnoGestitoDalRound;

	// Nessuno dei miei può agire (tutti storditi, congelati o atterrati) e gli avversari sono ancora in campo: il combattimento procede da solo, a
	// ogni battito, saltando i miei (vedi avviaLaMischiaForzata), finché qualcuno non si riprende. La fuga resta sempre possibile
	private boolean mischiaForzata;

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
		CHI_BEVE_POZIONE_AUMENTO_LIVELLO,
		CHI_FORMULA,
		QUALE_FORMULA,
		SU_CHI_FORMULA,
		CHI_CORROMPE,
		CHI_FA_AMICIZIA,
		ACCETTA_OFFERTA,
		CONFERMA_FUGA,
		CHI_DUELLA,
		SFIDA_CARTA_FORBICI_SASSO
	}

	/**
	 * L'elenco dei mostri e degli oggetti che è possibile trovare all'interno di
	 * questa locazione. Ogni locazione che ne ha sovrascrive questi due getter.
	 */
	protected TipoPersonaggio[] getPossibiliIncontri() {
		return NESSUN_INCONTRO;
	}

	protected TipoBottino[] getPossibiliOggetti() {
		return NESSUN_OGGETTO;
	}

	@Override
	public boolean isSceltaDiUnaCura() {
		return statoLocazione == StatoLocazione.CHI_BEVE_POZIONE_SALUTE
				|| statoLocazione == StatoLocazione.CHI_BEVE_POZIONE_SALUTE_GRANDE
				|| (statoLocazione == StatoLocazione.SU_CHI_FORMULA && incantesimo != null
				&& incantesimo.getClasse() == TipoIncantesimo.RESURREZIONE);
	}

	@Override
	public boolean isSceltaDiUnAumentoDiLivello() {
		return statoLocazione == StatoLocazione.CHI_BEVE_POZIONE_AUMENTO_LIVELLO;
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
		return md.getNome() != null ? md.getNome() : getTipoLocazione().getNomeProprio();
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
	public static final Set<TipoPersonaggio> MOSTRI_ESCLUSI_A_INIZIO_PARTITA = Collections.unmodifiableSet(EnumSet.of(
			TipoPersonaggio.CENTAURO, TipoPersonaggio.CHIMERA_DRAGO, TipoPersonaggio.GIGANTE,
			TipoPersonaggio.MINOTAURO, TipoPersonaggio.TITANO, TipoPersonaggio.TROLL));

	/**
	 * I mostri che si possono incontrare in una locazione, dato il livello del capo del gruppo: durante l'inizio
	 * morbido senza quelli di MOSTRI_ESCLUSI_A_INIZIO_PARTITA (se ne resta almeno uno)
	 */
	public static TipoPersonaggio[] incontriPossibili(TipoPersonaggio[] mostri, int livelloCapo) {
		if (livelloCapo > Costanti.INIZIO_MORBIDO_FINO_AL_LIVELLO) {
			return mostri;
		}
		TipoPersonaggio[] ammessi = Arrays.stream(mostri).filter(m -> !MOSTRI_ESCLUSI_A_INIZIO_PARTITA.contains(m))
				.toArray(TipoPersonaggio[]::new);
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

	/**
	 * Crea gli avversari della locazione scegliendo a caso fra gli incontri possibili adatti al livello del capo. Non sempre si trova qualcuno, ma al
	 * primo turno sì, perché la Foresta non sembri vuota.
	 */
	public void crea(GruppoGiocatore g, GruppoAvversario avversario) {
		TipoPersonaggio[] m = incontriPossibili(getPossibiliIncontri(), g.getCapo().getLivello());
		if (m.length > 0) {
			// Non sempre si trovano mostri. Al primo turno però vogliamo sempre trovarne uno,
			// un po' per non dare l'impressione che la foresta sia vuota, un po' per non far
			// scattare immediatamente le missioni secondarie che scattano a fine locazione.
			boolean possibilitaIncontro = Statistiche.getTurniGiocati() == 0 || Dado.tira(100) <= 90;
			if (possibilitaIncontro) {
				// FIXME occorrerebbe gestire la cosa un po' più elegantemente...
				// PEr non far apparire subito un Eremita che fa scattare la missione secondaria a inizio locazione
				List<TipoPersonaggio> ammessi = new ArrayList<>(Arrays.asList(m));
				if (Statistiche.getTurniGiocati() == 0 && ammessi.size() > 1) {
					ammessi.remove(TipoPersonaggio.EREMITA);
				}
				TipoPersonaggio classePersonaggio = Dado.scegli(ContestoLancio.TIPO_MOSTRO, ammessi);

				int capLivello = numeroMassimoDiMostri(g.getCapo().getLivello(), g.getPersonaggiVivi().size());

				int limiteMassimoIncontro = Math.min(capLivello, FabbricaPersonaggi.quantitaMassima(classePersonaggio));

				int numero = Dado.tiraAncheAUnaFaccia(ContestoLancio.NUMERO_MOSTRI, limiteMassimoIncontro);
				Logger.log(() -> "Scelta da " + m.length + " personaggi la classe " + classePersonaggio + ", numero " + numero + " con cap " + capLivello);
				Personaggio p;
				for (int i = 0; i < numero; i++) {
					p = FabbricaPersonaggi.crea(classePersonaggio, Statistiche.getLivello());
					avversario.aggiungiPersonaggio(p);
				}
			} else {
				Logger.log("Per stavolta niente mostri");
			}
		}
		// Vediamo quali artefatti sono presenti in questa locazione.
		Artefatto a = getArtefatto(g);
		if (a != null) {
			setOggetto(BottinoArtefatto.giaFatto(a));
		} else if (!isLocazioneVisitata()) {
			// Non ci sono artefatti, creiamo un oggetto.
			TipoBottino[] o = getPossibiliOggetti();
			Logger.log(() -> "Scelta da " + o.length + " oggetti");
			if (o.length > 0) {
				int indice = Dado.tiraAncheAUnaFaccia(o.length) - 1;
				TipoBottino tipoOggetto = o[indice];
				Logger.log(() -> "Classe oggetto " + tipoOggetto);
				Bottino probabileOggetto = FabbricaOggetti.crea(tipoOggetto);
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
		// Chiunque sia caduto in questa azione, e comunque (attacco, incantesimo, abilità, canto, effetto di stato, reazione...), conta come
		// sconfitto per esperienza, bestiario e trofei: qui, prima che lo scontro finisca o arrivi un'altra ondata, perché nessun ramo
		// di impostaAzioniImpl che termina in anticipo se lo dimentichi. Idempotente: chi è già stato contato non si conta due volte
		registraMorteDegliAvversari();
		if (stato != Stato.GIOCO_PERSO) {
			evocaGliScheletri();
		}
		if (stato != Stato.GIOCO_PERSO && stato != Stato.FINE_LOCAZIONE && isDuelloPerso()) {
			return perdiIlDuello();
		}
		// Non si guarda se la locazione è completa: quando l'ultimo avversario muore per un veleno non lo è ancora
		if (stato == Stato.FINE_LOCAZIONE && gruppoAvversario.getPersonaggiVivi().isEmpty()) {
			if (gruppoAvversario.hasOndateSuccessive()) {
				return arrivaLaProssimaOndata();
			}
			// Vinto lo scontro: le scorte che gli avversari non hanno usato sono bottino
			BottinoDegliAvversari.raccogli(gruppo, gruppoAvversario);
		}
		if (stato == Stato.FINE_LOCAZIONE || stato == Stato.GIOCO_PERSO) {
			mischiaForzata = false;
		} else if ((stato == Stato.IN_LOCAZIONE || (stato == Stato.IN_COMBATTIMENTO && !mischiaForzata)) && nessunoPuoAgire()) {
			return avviaLaMischiaForzata();
		}
		return stato;
	}

	/**
	 * I miei personaggi che possono agire adesso: quelli in campo e, se ci sono avversari, non incapaci di agire (storditi, congelati, atterrati).
	 */
	private List<Personaggio> chiPuoAgire() {
		List<Personaggio> inCampo = gruppo.getPersonaggiVivi();
		if (gruppoAvversario.getPersonaggiVivi().isEmpty()) {
			return inCampo;
		}
		return inCampo.stream().filter(p -> !p.isIncapaceDiAgire()).collect(Collectors.toList());
	}

	/**
	 * Se ci sono avversari e dei miei in campo, ma nessuno è in grado di agire.
	 */
	private boolean nessunoPuoAgire() {
		return !gruppo.getPersonaggiVivi().isEmpty() && chiPuoAgire().isEmpty();
	}

	/**
	 * Nessuno dei miei può agire: non c'è niente da scegliere, e il turno passa da solo. Si passa alla mischia automatica, che salta chi non
	 * può agire, fa agire gli avversari e fa scendere gli effetti di stato a fine round; si torna al giocatore appena qualcuno si riprende.
	 * La fuga resta fra i comandi.
	 */
	private Stato avviaLaMischiaForzata() {
		if (!mischiaForzata) {
			mischiaForzata = true;
			BusEventi.pubblica(new NotificaTestoFrase(gruppo.chiMaiuscolo() + " non è in grado di agire."));
		}
		statoLocazione = StatoLocazione.IN_COMBATTIMENTO;
		impostaComandiPossibili();
		return Stato.IN_COMBATTIMENTO;
	}

	@Override
	public boolean isSceltaDiChiAgisce() {
		if (gruppoAvversario.getPersonaggiVivi().isEmpty()) {
			return false;
		}
		switch (statoLocazione) {
			case CHI_ESEGUE_SINGOLO_ATTACCO:
			case CHI_COMBATTE:
			case CHI_FORMULA:
			case CHI_CORROMPE:
			case CHI_FA_AMICIZIA:
			case CHI_BEVE_POZIONE_SALUTE:
			case CHI_BEVE_POZIONE_SALUTE_GRANDE:
			case CHI_BEVE_POZIONE_MAGIA:
			case CHI_BEVE_POZIONE_MAGIA_GRANDE:
			case CHI_BEVE_POZIONE_AUMENTO_LIVELLO:
				return true;
			default:
				return false;
		}
	}

	/**
	 * I comandi che chiedono a un personaggio di agire: senza nessuno in grado di farlo non si possono dare.
	 */
	private static boolean isComandoDiChiAgisce(Comando azione) {
		switch (azione) {
			case SINGOLO_ATTACCO:
			case COMBATTIMENTO:
			case INCANTESIMO:
			case CORRUZIONE:
			case AMICIZIA:
			case POZIONE_SALUTE:
			case POZIONE_SALUTE_GRANDE:
			case POZIONE_MAGIA:
			case POZIONE_MAGIA_GRANDE:
			case POZIONE_AUMENTO_LIVELLO:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Sconfitti tutti gli avversari in campo, ne arriva un'altra ondata (vedi GruppoAvversario.prossimaOndata): la
	 * locazione non è più completa, la finestra di combattimento si chiude e si torna a scegliere che cosa fare. Tutti
	 * i punti in cui un combattimento si vince (la mischia, gli incantesimi, il dardo, i veleni) passano di qui.
	 */
	private Stato arrivaLaProssimaOndata() {
		BusEventi.pubblica(new InternoRichiestaChiusuraFinestraCombattimento());
		Ondata ondata = gruppoAvversario.prossimaOndata();
		BottinoDegliAvversari.fornisciLeScorte(gruppoAvversario, ondata.getAvversari());
		statoDelCombattimento.nuovaOndata();
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
			case CHI_BEVE_POZIONE_AUMENTO_LIVELLO:
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
		turnoGestitoDalRound = false;
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
				turnoGestitoDalRound = true;
				break;

			case CHI_DUELLA:
				if (azione == Comando.ANNULLA) {
					return rifiutaIlDuello();
				}
				gestisciChiDuella(azione);
				turnoGestitoDalRound = true;
				break;

			case SFIDA_CARTA_FORBICI_SASSO:
				return giocaUnaMano(azione);

			case CHI_BEVE_POZIONE_SALUTE:
				Logger.log("LocazioneBase.CHI_BEVE_POZIONE_SALUTE");
				statoLocazione = StatoLocazione.IN_LOCAZIONE;
				if (azione == Comando.ANNULLA) {
					return annullaScelta();
				}
				gruppo.consumaPozioneSalute(azione);
				// Bere la pozione è l'azione del personaggio: consuma il suo turno nel round
				return dopoUnaPozione(gruppo.getPersonaggio(azione));

			case CHI_BEVE_POZIONE_SALUTE_GRANDE:
				Logger.log("LocazioneBase.CHI_BEVE_POZIONE_SALUTE_GRANDE");
				statoLocazione = StatoLocazione.IN_LOCAZIONE;
				if (azione == Comando.ANNULLA) {
					return annullaScelta();
				}
				gruppo.consumaPozioneSaluteGrande(azione);
				// Bere la pozione è l'azione del personaggio: consuma il suo turno nel round
				return dopoUnaPozione(gruppo.getPersonaggio(azione));

			case CHI_BEVE_POZIONE_MAGIA:
				Logger.log("LocazioneBase.CHI_BEVE_POZIONE_MAGIA");
				statoLocazione = StatoLocazione.IN_LOCAZIONE;
				if (azione == Comando.ANNULLA) {
					return annullaScelta();
				}
				gruppo.consumaPozioneMagia(azione);
				// Bere la pozione è l'azione del personaggio: consuma il suo turno nel round
				return dopoUnaPozione(gruppo.getPersonaggio(azione));

			case CHI_BEVE_POZIONE_MAGIA_GRANDE:
				Logger.log("LocazioneBase.CHI_BEVE_POZIONE_MAGIA_GRANDE");
				statoLocazione = StatoLocazione.IN_LOCAZIONE;
				if (azione == Comando.ANNULLA) {
					return annullaScelta();
				}
				gruppo.consumaPozioneMagiaGrande(azione);
				// Bere la pozione è l'azione del personaggio: consuma il suo turno nel round
				return dopoUnaPozione(gruppo.getPersonaggio(azione));

			case CHI_BEVE_POZIONE_AUMENTO_LIVELLO:
				Logger.log("LocazioneBase.CHI_BEVE_POZIONE_AUMENTO_LIVELLO");
				statoLocazione = StatoLocazione.IN_LOCAZIONE;
				if (azione == Comando.ANNULLA) {
					return annullaScelta();
				}
				gruppo.consumaPozioneAumentoLivello(azione);
				// Bere la pozione è l'azione del personaggio: consuma il suo turno nel round
				return dopoUnaPozione(gruppo.getPersonaggio(azione));

			case CHI_FORMULA:
				Logger.log("LocazioneBase.CHI_FORMULA");
				if (azione == Comando.ANNULLA) {
					return annullaScelta();
				}
				gruppo.setFormulante(gruppo.getPersonaggio(azione));
				statoLocazione = StatoLocazione.QUALE_FORMULA;
				return Stato.SCELTA_INCANTESIMO_DA_LANCIARE;

			case QUALE_FORMULA:
				Logger.log(() -> "LocazioneBase.QUALE_FORMULA: " + azione);
				if (azione == Comando.ANNULLA) {
					return annullaScelta();
				} else if (TipoCanto.ofComando(azione) != null) {
					Stato statoDopoIlCanto = cantaUnCanto(gruppo.getFormulante(), TipoCanto.ofComando(azione), gruppo, gruppoAvversario);
					if (statoDopoIlCanto != null) {
						return statoDopoIlCanto;
					}
					statoLocazione = StatoLocazione.IN_LOCAZIONE;
					break;
				} else if (TipoAbilita.ofComando(azione) != null) {
					Stato statoDopoLAbilita = usaUnaAbilita(gruppo.getFormulante(), TipoAbilita.ofComando(azione), gruppo, gruppoAvversario);
					if (statoDopoLAbilita != null) {
						return statoDopoLAbilita;
					}
					statoLocazione = StatoLocazione.IN_LOCAZIONE;
					break;
				} else {
					opzioneCorruzioneDisponibile = false;
					opzioneAmiciziaDisponibile = false;
					Personaggio formulante = gruppo.getFormulante();
					TipoIncantesimo tipoIncantesimo = TipoIncantesimo.ofComando(azione);
					incantesimo = FabbricaIncantesimi.crea(tipoIncantesimo, formulante.getLivello());
					if (formulante.getMagia() < incantesimo.getCostoLancio()) {

						// Non si dovrebbe più riuscire a entrare in questo ramo perché la scelta degli incantesimi è già stata filtrata
						String sb = "Il livello di magia " + formulante.getNome(Personaggio.OpzioniGetNome.INCLUDI_PREPOSIZIONE_ARTICOLATA) +
								" non permette di formulare questo incantesimo.";
						BusEventi.pubblica(new NotificaTestoFrase(sb));

						{
							Stato dopoLAzione = dopoAzioneDelGiocatore(formulante);
							if (dopoLAzione != null) {
								return dopoLAzione;
							}
						}

						if (!gruppo.getCapo().isVivo()) {
							return Stato.GIOCO_PERSO;
						}

						statoLocazione = StatoLocazione.IN_LOCAZIONE;
						break;
					}

					PortataIncantesimo tipo = tipoIncantesimo.getPortata();
					boolean suUnSoloBersaglio = tipo == PortataIncantesimo.SINGOLO_SOLO_VIVI || tipo == PortataIncantesimo.SINGOLO_QUALSIASI;
					annunciaAzione(formulante, suUnSoloBersaglio && tipoIncantesimo.getCategoriaIncantesimo() != CategoriaIncantesimo.BENEFICO
							? gruppoAvversario.getPersonaggioVivo() : null);
					// Un incantesimo benefico su un solo bersaglio (Resurrezione) si formula su un personaggio del gruppo,
					// e occorre chiedere quale sia
					if (suUnSoloBersaglio && tipoIncantesimo.getCategoriaIncantesimo() == CategoriaIncantesimo.BENEFICO) {
						Logger.log(() -> "Incantesimo di tipo " + (tipo == PortataIncantesimo.SINGOLO_SOLO_VIVI ? "SINGOLO_SOLO_VIVI" : "SINGOLO_QUALSIASI"));
						int l = gruppo.getNumeroPersonaggi();
						Personaggio personaggio;
						List<Comando> comandiPossibiliBersaglio = new ArrayList<>();
						for (int i = 0; i < l; i++) {
							personaggio = gruppo.getPersonaggio(i);
							if (Logger.isAttivo()) {
								Logger.log("tipo == Incantesimo.SINGOLO_QUALSIASI || p.isVivo() ? " + ((tipo == PortataIncantesimo.SINGOLO_QUALSIASI || personaggio.isVivo())));
							}
							if ((tipo == PortataIncantesimo.SINGOLO_QUALSIASI || personaggio.isVivo()) && personaggio.isCurabile()) {
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
					if (!suUnSoloBersaglio && tipoIncantesimo.getCategoriaIncantesimo() == CategoriaIncantesimo.BENEFICO) {
						incantesimo.formula(formulante, null, gruppo);

						if (!gruppo.getCapo().isVivo()) {
							return Stato.GIOCO_PERSO;
						}

						// Chi la conosce per natura (Sacerdote, Sacerdotessa) non consuma pergamene
						if (!(incantesimo.getClasse() == TipoIncantesimo.ALBA_SACRA && AlbaSacra.conosciutaDa(formulante.getClasse()))) {
							gruppo.subIncantesimi(incantesimo.getClasse(), 1);
						}

						{
							Stato dopoLAzione = dopoAzioneDelGiocatore(formulante);
							if (dopoLAzione != null) {
								return dopoLAzione;
							}
						}

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

					{
						Stato dopoLAzione = dopoAzioneDelGiocatore(formulante);
						if (dopoLAzione != null) {
							return dopoLAzione;
						}
					}

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
				annunciaAzione(formulanteScelto, personaggioBersaglio);
				incantesimo.formula(formulanteScelto, personaggioBersaglio, null);
				registraUccisione(formulanteScelto, personaggioBersaglio);
				gruppo.subIncantesimi(incantesimo.getClasse(), 1);
				{
					Stato dopoLAzione = dopoAzioneDelGiocatore(formulanteScelto);
					if (dopoLAzione != null) {
						return dopoLAzione;
					}
				}
				statoLocazione = StatoLocazione.IN_LOCAZIONE;
				break;

			case CHI_CORROMPE:
				Logger.log("LocazioneBase.CHI_CORROMPE");
				if (azione == Comando.ANNULLA) {
					// Annullare non e' un'azione: nessun turno trascorre, si ripresentano solo i comandi
					return annullaScelta();
				}
				if (gruppo.getMonete() >= gruppo.getNumeroPersonaggi() * 2 && Dado.tira(Costanti.FACCE_DEL_DADO_DELLA_CORRUZIONE) > Costanti.SOGLIA_DEL_DADO_DELLA_CORRUZIONE) {
					gruppo.subMonete(gruppo.getNumeroPersonaggi() * 2);
					BusEventi.pubblica(new NotificaTestoFrase(gruppo.chiMaiuscolo() + " ha ottenuto un passaggio sicuro."));
					BusEventi.pubblica(new InternoCorruzioneRiuscita(classiAvversariVivi()));
					setOggetto(null);

					offerta = creaOfferta(gruppoAvversario.getCapo().getOfferta(Comando.CORRUZIONE));
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
					// Se fallisce, per qualunque motivo (anche se mancano le monete per pagare), c'è sempre uno spregio
					String descrizione = subisciUnoSpregio(p);
					String tentativo = "Il tentativo di corruzione " + p.getNome(Personaggio.OpzioniGetNome.INCLUDI_PREPOSIZIONE_ARTICOLATA)
							+ " non ha avuto successo";
					if (descrizione != null) {
						BusEventi.pubblica(new NotificaTestoFrase(tentativo + ", e in una breve colluttazione " + descrizione));
						// Non sapendo cosa andiamo a perdere rinfreschiamo tutto
						BusEventi.pubblica(new InternoRichiestaRefreshUI());
					} else {
						BusEventi.pubblica(new NotificaTestoFrase(tentativo + "."));
					}
					opzioneCorruzioneDisponibile = false;
					opzioneAmiciziaDisponibile = false;
					statoLocazione = StatoLocazione.IN_LOCAZIONE;
					break;
				}

			case CHI_FA_AMICIZIA:
				Logger.log(() -> "LocazioneBase.CHI_FA_AMICIZIA (azione " + azione + ")");
				if (azione == Comando.ANNULLA) {
					// Annullare non e' un'azione: nessun turno trascorre, si ripresentano solo i comandi
					return annullaScelta();
				}
				Personaggio personaggio = gruppo.getPersonaggio(azione);
				int tiroDelDado = Dado.tira(Costanti.FACCE_DEL_DADO_DELL_AMICIZIA);
				Logger.log(() -> "Carisma personaggio: " + personaggio.getCarisma() + "; tiro del dado: " + tiroDelDado);
				if (personaggio.getCarisma() > tiroDelDado) {
					personaggio.addCarisma(1);
					haStrettoAmicizia = true;
					// Come con la corruzione: con l'amicizia l'oggetto non si prende, e sparisce subito
					setOggetto(null);
					BusEventi.pubblica(new InternoAmiciziaStretta(classiAvversariVivi()));
					BusEventi.pubblica(new NotificaTestoFrase(personaggio.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
							Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA) + " riesce a stringere amicizia."));

					// Finché la missione La sfida dei campioni non è partita, a volte l'avversario sfida invece di offrire
					if (!RegistroMissioni.isPartita(MissionePredefinita.LA_SFIDA_DEI_CAMPIONI) && MossaCartaForbiciSasso.puoGiocare(gruppoAvversario.getCapo().getClasse())
							&& Dado.tira(100) <= Costanti.PERCENTUALE_PROBABILITA_CARTA_FORBICI_SASSO) {
						return iniziaLaSfida();
					}
					offerta = creaOfferta(gruppoAvversario.getCapo().getOfferta(Comando.AMICIZIA));
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
					String descrizione = subisciUnoSpregio(personaggio);

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

		if (!turnoGestitoDalRound) {
			Stato dopoGliEffetti = trascorriTurnoEffettiDiStato();
			if (dopoGliEffetti != null) {
				return dopoGliEffetti;
			}
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
	 * Un canto del Bardo o della Cantastorie: costa MAGIA, non consuma pergamene, occupa il turno e l'avversario
	 * risponde (vedi {@link CantoDelBardo}).
	 *
	 * @return lo stato in cui passare se lo scontro finisce (avversari tutti morti o gioco perso), altrimenti null
	 */
	private Stato cantaUnCanto(Personaggio cantore, TipoCanto tipo, GruppoGiocatore gruppo, GruppoAvversario gruppoAvversario) {
		opzioneCorruzioneDisponibile = false;
		opzioneAmiciziaDisponibile = false;
		// Il comando poteva essere stato dato quando il canto serviva e non serve più (e viceversa)
		if (!CantoDelBardo.cantiUtili(cantore, gruppo, gruppoAvversario).contains(tipo)
				|| !FabbricaCanti.crea(tipo, cantore).canta(cantore, gruppo, gruppoAvversario)) {
			// Il canto non è partito: non è stata fatta nessuna azione, e quindi non passa nessun turno
			turnoGestitoDalRound = true;
			return null;
		}
		statoDelCombattimento.registraIncantesimoLanciato(cantore);
		if (gruppoAvversario.getNumeroPersonaggiVivi() == 0) {
			setCompleta(true);
			return Stato.FINE_LOCAZIONE;
		}
		return dopoAzioneDelGiocatore(cantore);
	}

	/**
	 * Un'abilità innata di Chierico o Druido: costa MAGIA, non consuma pergamene, occupa il turno e l'avversario risponde (vedi
	 * {@link AbilitaInnate}).
	 *
	 * @return lo stato in cui passare se lo scontro finisce (avversari tutti morti o gioco perso), altrimenti null
	 */
	private Stato usaUnaAbilita(Personaggio utilizzatore, TipoAbilita tipo, GruppoGiocatore gruppo, GruppoAvversario gruppoAvversario) {
		opzioneCorruzioneDisponibile = false;
		opzioneAmiciziaDisponibile = false;
		// Il comando poteva essere stato dato quando l'abilità serviva e non serve più (e viceversa)
		if (!AbilitaInnate.abilitaUtili(utilizzatore, gruppo, gruppoAvversario).contains(tipo)
				|| !FabbricaAbilita.crea(tipo, utilizzatore).usa(utilizzatore, gruppo, gruppoAvversario)) {
			// L'abilità non è partita: non è stata fatta nessuna azione, e quindi non passa nessun turno
			turnoGestitoDalRound = true;
			return null;
		}
		statoDelCombattimento.registraIncantesimoLanciato(utilizzatore);
		// Chi è caduto per l'abilità conta subito come sconfitto
		for (Personaggio avversario : gruppoAvversario.getPersonaggi()) {
			if (avversario.isFuoriCombattimento()) {
				registraUccisione(utilizzatore, avversario);
			}
		}
		if (gruppoAvversario.getNumeroPersonaggiVivi() == 0) {
			setCompleta(true);
			return Stato.FINE_LOCAZIONE;
		}
		return dopoAzioneDelGiocatore(utilizzatore);
	}
	// La tattica di un personaggio (tattiche_di_combattimento_automatiche.md §11): in combattimento automatico agiscono con quella i miei personaggi e i
	// mostri (la sua, o quella base della loro classe, RegistroTattiche). Le azioni manuali del giocatore (la barra dei comandi) non passano di qui.

	/**
	 * Chi esegue le decisioni delle tattiche, con i gruppi e lo stato di questa locazione; registra gli uccisi come le azioni manuali
	 */
	private EsecutoreTattiche esecutore() {
		return new EsecutoreTattiche(gruppo, gruppoAvversario, statoDelCombattimento, this::registraUccisione);
	}

	/**
	 * La tattica con cui agisce un personaggio: la sua; se non ne ha una, per i miei quella predefinita del suo ruolo e per un mostro quella
	 * base della sua classe (RegistroTattiche, che le prende da TatticheBase).
	 */
	private TatticaMD tatticaDi(Personaggio personaggio) {
		TatticaMD tattica = personaggio.getTattica();
		// Una tattica disabilitata (con errori, letta da un salvataggio) è come se non ci fosse, finché il giocatore non la corregge
		if (tattica != null && !tattica.isDisabilitata()) {
			return tattica;
		}
		return gruppo.contiene(personaggio) ? RegistroTattiche.predefinitaDelGiocabile(personaggio.getClasse())
				: RegistroTattiche.perClasse(personaggio.getClasse());
	}

	/**
	 * L'azione di un personaggio decisa dalla sua tattica (vedi EsecutoreTattiche): la esegue e controlla se lo scontro o la partita sono finiti. Non
	 * gestisce il round (lo fa chi la chiama).
	 *
	 * @return lo stato in cui passare se lo scontro o la partita sono finiti, altrimenti vuoto
	 */
	private Optional<Stato> agisciConLaTattica(Personaggio attore, TatticaMD tattica) {
		boolean deiMiei = gruppo.contiene(attore);
		if (esecutore().agisci(attore, tattica) == null) {
			// Nessun bersaglio: per i miei vuol dire che gli avversari sono finiti
			if (deiMiei && gruppoAvversario.getPersonaggiVivi().isEmpty()) {
				setCompleta(true);
				return Optional.of(Stato.FINE_LOCAZIONE);
			}
			return Optional.empty();
		}
		if (deiMiei) {
			opzioneCorruzioneDisponibile = false;
			opzioneAmiciziaDisponibile = false;
		}
		if (gruppoAvversario.getPersonaggiVivi().isEmpty()) {
			setCompleta(true);
			return Optional.of(Stato.FINE_LOCAZIONE);
		}
		if (!gruppo.getCapo().isVivo()) {
			return Optional.of(Stato.GIOCO_PERSO);
		}
		return Optional.empty();
	}

	private void impostaComandiPossibili() {
		List<Comando> comandiPossibili = new ArrayList<>();
		if (statoLocazione == StatoLocazione.SFIDA_CARTA_FORBICI_SASSO) {
			// Durante la sfida non si può fare altro
			for (MossaCartaForbiciSasso mossa : MossaCartaForbiciSasso.values()) {
				comandiPossibili.add(mossa.getComando());
			}
			BusEventi.pubblica(new InternoAggiornamentoComandiDisponibili(comandiPossibili));
			return;
		}
		// Se nessuno è in grado di agire restano solo la fuga e le azioni che non sono di un personaggio
		boolean qualcunoPuoAgire = !nessunoPuoAgire();
		List<Personaggio> chiAgisce = chiPuoAgire();
		// Possiamo combattere? Oppure, vogliamo cambiare chi combatte?
		if (qualcunoPuoAgire && (statoLocazione != StatoLocazione.IN_COMBATTIMENTO || chiAgisce.size() > 1)) {
			// Nella mischia COMBATTIMENTO cambia chi combatte; l'attacco singolo c'è solo fuori, dove è un'azione a sé
			if (statoLocazione != StatoLocazione.IN_COMBATTIMENTO) {
				comandiPossibili.add(Comando.SINGOLO_ATTACCO);
			}
			comandiPossibili.add(Comando.COMBATTIMENTO);
		}
		// Se stiamo combattendo possiamo interrompere la schermaglia (non quella che prosegue da sola perché nessuno può agire)
		if (statoLocazione == StatoLocazione.IN_COMBATTIMENTO && !mischiaForzata) {
			comandiPossibili.add(Comando.INTERRUZIONE_COMBATTIMENTO);
		}
		// Possiamo formulare incantesimi? Si se ne abbiamo almeno uno e se uno dei personaggi vivi può lanciarlo,
		// oppure se un Sacerdote ha la MAGIA per l'alba sacra, o se qualcuno può usare un'abilità innata (Chierico, Druido, Sacerdote, Mago, Elfo) o un canto
		boolean incantesimoPossibile = chiAgisce.stream()
				.anyMatch(p -> AlbaSacra.puoLanciarlaInnata(p)
						|| !CantoDelBardo.cantiUtili(p, gruppo, gruppoAvversario).isEmpty()
						|| !AbilitaInnate.abilitaUtili(p, gruppo, gruppoAvversario).isEmpty());
		for (TipoIncantesimo tipoIncantesimo : TipoIncantesimo.values()) {
			if (gruppo.getIncantesimi(tipoIncantesimo) > 0 &&
					chiAgisce.stream().anyMatch(p -> p.puoFormulare(tipoIncantesimo))) {
				incantesimoPossibile = true;
				break;
			}
		}
		if (incantesimoPossibile) {
			comandiPossibili.add(Comando.INCANTESIMO);
		}
		// Possiamo corrompere gli avversari?
		if (qualcunoPuoAgire && opzioneCorruzioneDisponibile) {
			comandiPossibili.add(Comando.CORRUZIONE);
		}
		// Possiamo fare amicizia?
		if (qualcunoPuoAgire && opzioneAmiciziaDisponibile) {
			comandiPossibili.add(Comando.AMICIZIA);
		}
		if (statoLocazione != StatoLocazione.IN_COMBATTIMENTO) {
			comandiPossibili.add(Comando.MAPPA);
		}
		if (qualcunoPuoAgire && gruppo.getPozioniSalute() > 0) {
			comandiPossibili.add(Comando.POZIONE_SALUTE);
		}
		if (qualcunoPuoAgire && gruppo.getPozioniSaluteGrande() > 0) {
			comandiPossibili.add(Comando.POZIONE_SALUTE_GRANDE);
		}
		if (qualcunoPuoAgire && gruppo.getPozioniMagia() > 0) {
			comandiPossibili.add(Comando.POZIONE_MAGIA);
		}
		if (qualcunoPuoAgire && gruppo.getPozioniMagiaGrande() > 0) {
			comandiPossibili.add(Comando.POZIONE_MAGIA_GRANDE);
		}
		if (qualcunoPuoAgire && gruppo.isPozioneAumentoLivelloUtilizzabile()) {
			comandiPossibili.add(Comando.POZIONE_AUMENTO_LIVELLO);
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
	 * Gli avversari per classe, nell'ordine in cui compare ognuna: "cinque Scheletri", "tre Goblin e un Hobgoblin", "quattro Scheletri e un
	 * Necromante".
	 */
	private String descrizioneDegliAvversari(GruppoAvversario gruppoAvversario) {
		Map<TipoPersonaggio, List<Personaggio>> perClasse = new LinkedHashMap<>();
		for (Personaggio avversario : gruppoAvversario.getPersonaggi()) {
			perClasse.computeIfAbsent(avversario.getClasse(), c -> new ArrayList<>()).add(avversario);
		}
		List<String> parti = new ArrayList<>();
		for (List<Personaggio> classe : perClasse.values()) {
			Personaggio p = classe.get(0);
			parti.add(classe.size() == 1 ? p.getAIS() + p.getNomeSingolare() : Numeri.getCardinaleM(classe.size()) + " " + p.getNomePlurale());
		}
		if (parti.size() == 1) {
			return parti.get(0);
		}
		return String.join(", ", parti.subList(0, parti.size() - 1)) + " e " + parti.get(parti.size() - 1);
	}

	/**
	 * La descrizione dei presenti
	 */
	protected String descrizioneMostriEOggetti(GruppoGiocatore gruppoGiocatore, GruppoAvversario gruppoAvversario) {
		int numeroAvversari = gruppoAvversario.getNumeroPersonaggi();
		Bottino o = getOggetto();
		int numeroOggetti = (o == null ? 0 : o.getQuantita());
		StringBuilder sb = new StringBuilder(gruppoGiocatore.getCapo().getNome(Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA, Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE));
		if (numeroAvversari == 0 && numeroOggetti == 0) {
			sb.append(" non trova nulla");
		} else {
			sb.append(" vede ");
			if (numeroAvversari > 0) {
				sb.append(descrizioneDegliAvversari(gruppoAvversario));
				if (numeroOggetti > 0) {
					switch (Dado.tira(3)) {
						case 1:
							sb.append(numeroAvversari == 1 ? " che protegge " : " che proteggono ");
							break;
						case 2:
							sb.append(numeroAvversari == 1 ? " che custodisce " : " che custodiscono ");
							break;
						default:
							sb.append(numeroAvversari == 1 ? " che sorveglia " : " che sorvegliano ");
							break;
					}
				}
			}
			if (numeroOggetti > 0) {
				if (numeroOggetti == 1) {
					if (o.isDaRivelare()) {
						sb.append(o.getArtefatto().orElseThrow(IllegalStateException::new).getNomeCompleto());
					} else {
						sb.append(o.getAIS()).append(o.getNomeSingolare());
					}
				} else {
					sb.append(Numeri.getCardinaleM(numeroOggetti)).append(' ').append(o.getNomePlurale());
				}
			}
		}
		sb.append('.');
		return sb.toString();
	}

	// Il combattimento procede per round (vedi StatoDelCombattimento): un piano di coppie, il mio personaggio e poi un avversario, e
	// i personaggi in più di un gruppo agiscono da soli. Non c'è una "risposta" agli attacchi del giocatore: gli avversari agiscono
	// quando il piano tocca a loro, in round-robin, e chi non è in grado di farlo (atterrato, congelato, stordito) salta il turno.
	// Il bersaglio lo sceglie l'avversario con la sua tattica (la base della sua classe, RegistroTattiche).

	/**
	 * Un personaggio del giocatore ha compiuto la sua azione (un attacco, un incantesimo, un canto, una pozione): consuma il suo posto nel
	 * piano del round, e poi gli avversari agiscono finché il piano non torna a un mio personaggio o il round non finisce; alla fine del
	 * round passa un turno di effetti di stato. Senza avversari vivi non c'è piano: passa solo il turno degli effetti, come fuori dal
	 * combattimento.
	 *
	 * @return lo stato in cui passare se la partita o lo scontro sono finiti, altrimenti null
	 */
	private Stato dopoAzioneDelGiocatore(Personaggio chi) {
		gruppo.congedaChiNonRisorge();
		registraMorteDegliAvversari();
		if (gruppoAvversario.getPersonaggiVivi().isEmpty()) {
			return null;
		}
		turnoGestitoDalRound = true;
		boolean fineRound = statoDelCombattimento.registraAzione(chi);
		while (!fineRound && statoDelCombattimento.getProssimoLato() == StatoDelCombattimento.Lato.AVVERSARI) {
			Personaggio avversario = statoDelCombattimento.getProssimoAttaccante(gruppoAvversario);
			if (avversario == null) {
				fineRound = statoDelCombattimento.registraAzioneSaltata(StatoDelCombattimento.Lato.AVVERSARI);
				continue;
			}
			agisceUnAvversario(avversario);
			gruppo.congedaChiNonRisorge();
			registraMorteDegliAvversari();
			if (!gruppo.getCapo().isVivo()) {
				return Stato.GIOCO_PERSO;
			}
			fineRound = statoDelCombattimento.registraAzione(avversario);
		}
		if (!gruppo.getCapo().isVivo()) {
			return Stato.GIOCO_PERSO;
		}
		return fineRound ? trascorriTurnoEffettiDiStato() : null;
	}

	/**
	 * Bere una pozione è l'azione del personaggio: consuma il suo turno nel round (e interrompe la mischia, come l'incantesimo).
	 */
	private Stato dopoUnaPozione(Personaggio chiHaBevuto) {
		annunciaAzione(chiHaBevuto, null);
		Stato dopoLAzione = dopoAzioneDelGiocatore(chiHaBevuto);
		if (dopoLAzione != null) {
			return dopoLAzione;
		}
		if (!turnoGestitoDalRound) {
			// Senza avversari non c'è il piano del round: passa solo il turno degli effetti di stato
			dopoLAzione = trascorriTurnoEffettiDiStato();
			if (dopoLAzione != null) {
				return dopoLAzione;
			}
		}
		if (isCompleta()) {
			return Stato.FINE_LOCAZIONE;
		}
		impostaComandiPossibili();
		return Stato.IN_LOCAZIONE;
	}

	/**
	 * Come dopoUnaPozione, ma la mischia continua (se non è finita): bere l'ultima pozione di un personaggio solo non la interrompe.
	 */
	private Stato dopoUnaPozioneNellaMischia(Personaggio chiHaBevuto) {
		annunciaAzione(chiHaBevuto, null);
		Stato dopoLAzione = dopoAzioneDelGiocatore(chiHaBevuto);
		if (dopoLAzione != null) {
			return dopoLAzione;
		}
		if (!turnoGestitoDalRound) {
			dopoLAzione = trascorriTurnoEffettiDiStato();
			if (dopoLAzione != null) {
				return dopoLAzione;
			}
		}
		return Stato.IN_COMBATTIMENTO;
	}

	/**
	 * L'azione di un avversario: attacca il gruppo del giocatore, scegliendo da sé il bersaglio. Chi non è in grado di agire salta il
	 * turno.
	 */
	private void agisceUnAvversario(Personaggio avversario) {
		if (isIncapaceDiAgire(avversario) || gruppo.getNumeroPersonaggiVivi() == 0) {
			return;
		}
		agisciConLaTattica(avversario, tatticaDi(avversario));
	}

	/**
	 * Gli avversari si sono accorti del gruppo e attaccano per primi (un passaggio inosservato fallito): uno solo attacca, fuori dal piano
	 * del round.
	 */
	private void gliAvversariAttaccanoPerPrimi() {
		Personaggio avversario = statoDelCombattimento.getAvversarioCheAttaccaPerPrimo();
		if (avversario != null) {
			agisceUnAvversario(avversario);
		}
	}

	/**
	 * Dice alla finestra del combattimento chi sta per agire e, se c'è, contro chi
	 */
	private void annunciaAzione(Personaggio attore, Personaggio bersaglio) {
		BusEventi.pubblica(new InternoAzioneInCombattimento(attore, bersaglio));
	}

	/**
	 * Se per un effetto di stato il personaggio salta il turno
	 */
	private static boolean isIncapaceDiAgire(Personaggio personaggio) {
		return personaggio.hasEffettoDiStato(TipoEffettoDiStato.ATTERRATO) ||
				personaggio.hasEffettoDiStato(TipoEffettoDiStato.CONGELATO) ||
				personaggio.hasEffettoDiStato(TipoEffettoDiStato.STORDITO);
	}

	protected void setOggetto(Bottino oggetto) {
		if (oggetto != null && oggetto.getQuantita() > 0) {
			this.oggettoCorrente = oggetto;
		} else {
			this.oggettoCorrente = null;
		}
	}

	public Bottino getOggetto() {
		return oggettoCorrente;
	}

	/**
	 * Toglie l'oggetto della locazione, dopo che il gruppo l'ha raccolto. Se è un artefatto lo toglie anche dal registro, altrimenti a ogni visita se ne
	 * avrebbe una copia in più.
	 */
	public void rimuoviOggetto() {
		if (oggettoCorrente != null && oggettoCorrente.isDaRivelare()) {
			// L'unico artefatto che una locazione può offrire è quello del registro (vedi crea):
			// una volta raccolto va tolto, altrimenti a ogni nuova visita si ripresenta e se
			// ne ottengono infinite copie. Corruzione e fuga usano setOggetto(null), che invece
			// lo lascia al suo posto.
			RegistroArtefatti.rimuoviArtefattoInLocazione(gruppo.getCoordinate());
			// Un leggendario custodito in un tempio è nato molto prima: lo si ottiene al livello del mondo di adesso
			OggettoLeggendario.alLivello(oggettoCorrente.getArtefatto().orElseThrow(IllegalStateException::new), Statistiche.getLivello());
		}
		oggettoCorrente = null;
	}

	@Override
	public void collocaOggettoMissione(Bottino oggetto) {
		if (oggettoCorrente == null || !oggettoCorrente.isDaRivelare()) {
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
		int numeroAvversari = gruppoAvversario.getNumeroPersonaggi();
		custodita = numeroAvversari > 0;
		if (numeroAvversari == 0) {
			if (oggettoCorrente != null) {
				BusEventi.pubblica(new NotificaTestoFrase("Essendo il tesoro incustodito, " +
						gruppo.chi() + " se ne impossessa."));
			}
			gruppo.riposa(getTipoRiposo());
			setCompleta(true);
			return Stato.FINE_LOCAZIONE;
		} else {
			setCompleta(false);
			// Né con degli avversari che una missione vuole sconfitti (vedi Personaggio.isDaAffrontare): un capobanda con
			// una taglia non si corrompe, e una banda non diventa amica. E nemmeno con delle ondate in arrivo: corrotta
			// la prima, non arriverebbero le altre
			if (!gruppoAvversario.hasOndateSuccessive() && !gruppoAvversario.isDaAffrontare()) {
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
			if (gruppoAvversario.isSfidaACartaForbiciSasso()) {
				// Un campione de La sfida dei campioni: si comincia subito, e le uniche azioni sono carta, forbici e sasso
				opzioneAmiciziaDisponibile = false;
				opzioneCorruzioneDisponibile = false;
				return iniziaLaSfida();
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

	private List<TipoPersonaggio> classiAvversariVivi() {
		List<TipoPersonaggio> classi = new ArrayList<>();
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
			contaLoScheletroCaduto(vittima);
			facciCrollareGliScheletri(vittima);
		}
	}

	/**
	 * Caduto un necromante, gli scheletri che era lui a tenere in piedi crollano in un mucchio d'ossa, e contano come sconfitti.
	 */
	private void facciCrollareGliScheletri(Personaggio vittima) {
		if (vittima.getClasse() != TipoPersonaggio.NECROMANTE) {
			return;
		}
		List<Personaggio> scheletri = gruppoAvversario.getPersonaggiVivi().stream()
				.filter(p -> p.getClasse() == TipoPersonaggio.SCHELETRO).collect(Collectors.toList());
		if (scheletri.isEmpty()) {
			return;
		}
		BusEventi.pubblica(new NotificaTestoParagrafo(vittima.getNome() + " è caduto: " + (scheletri.size() == 1
				? "lo scheletro che teneva in piedi crolla in un mucchio d'ossa." : "gli scheletri che teneva in piedi crollano in un mucchio d'ossa.")));
		for (Personaggio scheletro : scheletri) {
			scheletro.muore("è crollato in un mucchio d'ossa");
			registraMorteAvversario(scheletro);
		}
	}

	/**
	 * Un necromante vivo evoca uno scheletro ogni volta che ne cadono due (Costanti.NECROMANTE_SCHELETRI_PER_EVOCAZIONE), finché è vivo e
	 * al più Costanti.NECROMANTE_EVOCAZIONI_MASSIME volte per scontro: qui si conta e si segna che serve un'evocazione, che si fa subito dopo
	 * l'azione (vedi evocaGliScheletri).
	 */
	private void contaLoScheletroCaduto(Personaggio vittima) {
		if (vittima.getClasse() != TipoPersonaggio.SCHELETRO || !haUnNecromanteVivo()) {
			return;
		}
		scheletriCaduti++;
		if (scheletriCaduti >= Costanti.NECROMANTE_SCHELETRI_PER_EVOCAZIONE) {
			scheletriCaduti -= Costanti.NECROMANTE_SCHELETRI_PER_EVOCAZIONE;
			evocazioniDaFare++;
		}
	}

	private Optional<Personaggio> getNecromanteVivo() {
		return gruppoAvversario.getPersonaggiVivi().stream().filter(p -> p.getClasse() == TipoPersonaggio.NECROMANTE).findFirst();
	}

	private boolean haUnNecromanteVivo() {
		return getNecromanteVivo().isPresent();
	}

	/**
	 * Fa le evocazioni che aspettavano: il necromante, se è ancora vivo, richiama uno scheletro, che entra nel gruppo avversario.
	 */
	private void evocaGliScheletri() {
		while (evocazioniDaFare > 0) {
			evocazioniDaFare--;
			Optional<Personaggio> necromante = getNecromanteVivo();
			if (!necromante.isPresent() || evocazioniFatte >= Costanti.NECROMANTE_EVOCAZIONI_MASSIME) {
				continue;
			}
			evocazioniFatte++;
			Personaggio scheletro = FabbricaPersonaggi.crea(TipoPersonaggio.SCHELETRO, Statistiche.getLivello());
			scheletro.setDaAffrontare(true);
			gruppoAvversario.aggiungiPersonaggio(scheletro);
			BusEventi.pubblica(new InternoAssegnaCoordinateAPersonaggi());
			BusEventi.pubblica(new NotificaTestoParagrafo(necromante.get().getNome() + " evoca un altro scheletro, che esce dalla terra e si unisce alla battaglia."));
		}
	}

	/**
	 * Conta tutti gli avversari caduti, anche quelli che nessun colpo diretto ha ucciso: i vicini colpiti da una reazione ad area
	 * (elettrocuzione, esplosione di gas, rigetto) non passano da registraUccisione. Idempotente.
	 */
	private void registraMorteDegliAvversari() {
		for (Personaggio avversario : gruppoAvversario.getPersonaggi()) {
			registraMorteAvversario(avversario);
		}
		// Anche chi è caduto ed è già stato tolto dal gruppo (un minion mietuto): ci vuole una copia, perché contarlo può far crollare altri
		for (Personaggio caduto : new ArrayList<>(gruppoAvversario.getCadutiRimossi())) {
			registraMorteAvversario(caduto);
		}
	}

	private Stato trascorriTurnoEffettiDiStato() {
		registraMorteDegliAvversari();
		for (Personaggio personaggio : gruppo.getPersonaggiVivi()) {
			personaggio.applicaDanniDaEffettiDiStato();
			personaggio.riduciEffettiDiStato();
			personaggio.riduciModificatori(UnitaDurataModificatore.TURNI);
		}
		// Anche gli ospiti vulnerabili, che possono essere colpiti e avere degli stati, non sono fra i personaggi del gruppo
		for (Personaggio ospite : gruppo.getOspitiBersaglio()) {
			ospite.applicaDanniDaEffettiDiStato();
			ospite.riduciEffettiDiStato();
			ospite.riduciModificatori(UnitaDurataModificatore.TURNI);
		}
		gruppo.congedaChiNonRisorge();
		if (!gruppo.getCapo().isVivo()) {
			return Stato.GIOCO_PERSO;
		}

		for (Personaggio personaggio : gruppoAvversario.getPersonaggiVivi()) {
			personaggio.applicaDanniDaEffettiDiStato();
			personaggio.riduciEffettiDiStato();
			personaggio.riduciModificatori(UnitaDurataModificatore.TURNI);
			registraMorteAvversario(personaggio);
		}
		if (gruppoAvversario.getPersonaggiVivi().isEmpty()) {
			// Anche se l'ultimo avversario muore di un effetto di stato (dopo un incantesimo, una pozione...) e non
			// per un colpo: senza questo l'oggetto non verrebbe offerto
			setCompleta(true);
			return Stato.FINE_LOCAZIONE;
		}
		return null;
	}

	/**
	 * L'attacco con le armi di un personaggio del giocatore contro il primo avversario vivo (con la seconda arma, se ce l'ha, sullo stesso
	 * bersaglio o sul prossimo vivo se la prima l'ha ucciso).
	 *
	 * @return FINE_LOCAZIONE se non resta nessun avversario, altrimenti vuoto
	 */
	/**
	 * L'offerta di quel tipo, o null se il personaggio non ne faceva.
	 */
	private static Offerta creaOfferta(TipoOfferta tipo) {
		return tipo == null ? null : FabbricaOfferte.crea(tipo);
	}

	private Optional<Stato> attaccaConLeArmi(Personaggio attore) {
		return attaccaConLeArmi(attore, gruppoAvversario.getPersonaggioVivo());
	}

	/**
	 * Come {@link #attaccaConLeArmi(Personaggio)}, ma contro un bersaglio scelto (dalla tattica del personaggio): è lui il primo colpito
	 */
	private Optional<Stato> attaccaConLeArmi(Personaggio attore, Personaggio bersaglio) {

		if (bersaglio == null) {
			setCompleta(true);
			return Optional.of(Stato.FINE_LOCAZIONE);
		}
		esecutore().attaccaConLeArmi(attore, bersaglio);
		if (gruppoAvversario.getPersonaggioVivo() == null) {
			setCompleta(true);
			return Optional.of(Stato.FINE_LOCAZIONE);
		}
		return Optional.empty();
	}

	/**
	 * Un attacco scelto dal giocatore per un suo personaggio (SINGOLO_ATTACCO): è la sua azione nel round, e gli avversari agiscono
	 * finché il piano non torna a un mio personaggio.
	 */
	private Optional<Stato> eseguiSingoloAttacco() {
		Optional<Stato> fine = attaccaConLeArmi(combattente);
		if (fine.isPresent()) {
			return fine;
		}
		Stato dopoLAzione = dopoAzioneDelGiocatore(combattente);
		if (dopoLAzione != null) {
			return Optional.of(dopoLAzione);
		}
		if (!turnoGestitoDalRound) {
			dopoLAzione = trascorriTurnoEffettiDiStato();
			if (dopoLAzione != null) {
				return Optional.of(dopoLAzione);
			}
		}
		return Optional.empty();
	}

	/**
	 * Un passo della mischia automatica (a ogni scatto del TIMER): agisce una sola volta il prossimo del piano del round, un mio personaggio
	 * o un avversario, in round-robin. I personaggi in più di un gruppo agiscono da soli di seguito. A fine round passa un turno di effetti
	 * di stato. Chi dei miei agisce attacca con le armi il primo avversario vivo.
	 *
	 * @return lo stato in cui passare se lo scontro o la partita sono finiti, altrimenti vuoto
	 */
	private Optional<Stato> eseguiPassoDellaMischia() {
		if (gruppoAvversario.getPersonaggioVivo() == null) {
			setCompleta(true);
			return Optional.of(Stato.FINE_LOCAZIONE);
		}
		StatoDelCombattimento.Lato lato = statoDelCombattimento.getProssimoLato();
		boolean fineRound;
		if (lato == StatoDelCombattimento.Lato.AVVERSARI) {
			Personaggio avversario = statoDelCombattimento.getProssimoAttaccante(gruppoAvversario);
			if (avversario == null) {
				fineRound = statoDelCombattimento.registraAzioneSaltata(StatoDelCombattimento.Lato.AVVERSARI);
			} else {
				agisceUnAvversario(avversario);
				if (!gruppo.getCapo().isVivo()) {
					return Optional.of(Stato.GIOCO_PERSO);
				}
				fineRound = statoDelCombattimento.registraAzione(avversario);
			}
		} else {
			Personaggio attore = statoDelCombattimento.getProssimoAttaccante(gruppo);
			if (attore == null) {
				fineRound = statoDelCombattimento.registraAzioneSaltata(StatoDelCombattimento.Lato.GIOCATORE);
			} else {
				combattente = attore;
				if (!isIncapaceDiAgire(attore)) {
					Optional<Stato> fine = agisciConLaTattica(attore, tatticaDi(attore));
					if (fine.isPresent()) {
						return fine;
					}
				}
				fineRound = statoDelCombattimento.registraAzione(attore);
			}
		}
		Stato dopoGliEffetti = fineRound ? trascorriTurnoEffettiDiStato() : null;
		if (dopoGliEffetti == null && mischiaForzata && !nessunoPuoAgire()) {
			// Qualcuno si è ripreso: si torna a scegliere che cosa fare
			mischiaForzata = false;
			BusEventi.pubblica(new InternoRichiestaChiusuraFinestraCombattimento());
			statoLocazione = StatoLocazione.IN_LOCAZIONE;
			combattente = null;
			impostaComandiPossibili();
			return Optional.of(Stato.IN_LOCAZIONE);
		}
		return Optional.ofNullable(dopoGliEffetti);
	}

	private Stato gestisciCombattimento(Comando azione) {
		Logger.log("LocazioneBase.IN_COMBATTIMENTO");
		if (azione == Comando.INTERRUZIONE_COMBATTIMENTO && mischiaForzata) {
			// Il combattimento prosegue da solo finché nessuno può agire: non c'è niente da interrompere
			return Stato.IN_COMBATTIMENTO;
		}
		if (azione != Comando.TIMER && isComandoDiChiAgisce(azione) && nessunoPuoAgire()) {
			return Stato.IN_COMBATTIMENTO;
		}
		if (azione == Comando.INTERRUZIONE_COMBATTIMENTO) {
			BusEventi.pubblica(new InternoRichiestaChiusuraFinestraCombattimento());
			statoLocazione = StatoLocazione.IN_LOCAZIONE;
			combattente = null;
			return Stato.IN_LOCAZIONE;
		}
		if (azione.isPersonaggio()) {
			// Il giocatore sceglie chi agisce per primo fra i suoi: poi la rotazione prosegue
			combattente = gruppo.getPersonaggio(azione);
			statoDelCombattimento.faAgirePerPrimo(gruppo, combattente);
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
			Optional<Stato> stato = eseguiPassoDellaMischia();
			if (stato.isPresent()) {
				return stato.get();
			}
		}
		if (azione == Comando.POZIONE_SALUTE) {
			if (chiPuoAgire().size() > 1) {
				// Bere interrompe la mischia (come l'incantesimo): la sua finestra si chiude
				BusEventi.pubblica(new InternoRichiestaChiusuraFinestraCombattimento());
				statoLocazione = StatoLocazione.CHI_BEVE_POZIONE_SALUTE;
				return Stato.SCELTA_AUTOMATICA_PERSONAGGIO;
			} else {
				Personaggio unico = chiPuoAgire().get(0);
				gruppo.consumaPozioneSalute(unico);
				return dopoUnaPozioneNellaMischia(unico);
			}
		}
		if (azione == Comando.POZIONE_SALUTE_GRANDE) {
			if (chiPuoAgire().size() > 1) {
				// Bere interrompe la mischia (come l'incantesimo): la sua finestra si chiude
				BusEventi.pubblica(new InternoRichiestaChiusuraFinestraCombattimento());
				statoLocazione = StatoLocazione.CHI_BEVE_POZIONE_SALUTE_GRANDE;
				return Stato.SCELTA_AUTOMATICA_PERSONAGGIO;
			} else {
				Personaggio unico = chiPuoAgire().get(0);
				gruppo.consumaPozioneSaluteGrande(unico);
				return dopoUnaPozioneNellaMischia(unico);
			}
		}
		if (azione == Comando.POZIONE_MAGIA) {
			if (chiPuoAgire().size() > 1) {
				// Bere interrompe la mischia (come l'incantesimo): la sua finestra si chiude
				BusEventi.pubblica(new InternoRichiestaChiusuraFinestraCombattimento());
				statoLocazione = StatoLocazione.CHI_BEVE_POZIONE_MAGIA;
				return Stato.SCELTA_AUTOMATICA_PERSONAGGIO;
			} else {
				Personaggio unico = chiPuoAgire().get(0);
				gruppo.consumaPozioneMagia(unico);
				return dopoUnaPozioneNellaMischia(unico);
			}
		}
		if (azione == Comando.POZIONE_MAGIA_GRANDE) {
			if (chiPuoAgire().size() > 1) {
				// Bere interrompe la mischia (come l'incantesimo): la sua finestra si chiude
				BusEventi.pubblica(new InternoRichiestaChiusuraFinestraCombattimento());
				statoLocazione = StatoLocazione.CHI_BEVE_POZIONE_MAGIA_GRANDE;
				return Stato.SCELTA_AUTOMATICA_PERSONAGGIO;
			} else {
				Personaggio unico = chiPuoAgire().get(0);
				gruppo.consumaPozioneMagiaGrande(unico);
				return dopoUnaPozioneNellaMischia(unico);
			}
		}
		if (azione == Comando.POZIONE_AUMENTO_LIVELLO) {
			if (chiPuoAgire().size() > 1) {
				// Bere interrompe la mischia (come l'incantesimo): la sua finestra si chiude
				BusEventi.pubblica(new InternoRichiestaChiusuraFinestraCombattimento());
				statoLocazione = StatoLocazione.CHI_BEVE_POZIONE_AUMENTO_LIVELLO;
				return Stato.SCELTA_AUTOMATICA_PERSONAGGIO;
			} else {
				Personaggio unico = chiPuoAgire().get(0);
				gruppo.consumaPozioneAumentoLivello(unico);
				return dopoUnaPozioneNellaMischia(unico);
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
		if (azione != null && isComandoDiChiAgisce(azione) && nessunoPuoAgire()) {
			// Nessuno può agire: un comando di questo genere, arrivato comunque, non fa niente e non fa passare nessun turno
			turnoGestitoDalRound = true;
			return null;
		}
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

				case POZIONE_AUMENTO_LIVELLO:
					statoLocazione = StatoLocazione.CHI_BEVE_POZIONE_AUMENTO_LIVELLO;
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
				&& gruppo.getTipoLocazioneCorrente().getCategoriaLocazione() == CategoriaLocazione.STANDARD
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
				.filter(p -> p.getClasse() == TipoPersonaggio.LADRO || p.getClasse() == TipoPersonaggio.LADRA)
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
		gliAvversariAttaccanoPerPrimi();
		if (!gruppo.getCapo().isVivo()) {
			return Stato.GIOCO_PERSO;
		}
		impostaComandiPossibili();
		return Stato.IN_LOCAZIONE;
	}

	/**
	 * L'avversario sfida il capo del gruppo a carta, forbici e sasso (vedi carta_forbici_sasso.md): una sfida a tre mani
	 * vinte, con i soli comandi carta, forbici e sasso.
	 */
	private Stato iniziaLaSfida() {
		sfida = new PartitaCartaForbiciSasso();
		statoLocazione = StatoLocazione.SFIDA_CARTA_FORBICI_SASSO;
		opzioneAmiciziaDisponibile = false;
		opzioneCorruzioneDisponibile = false;
		BusEventi.pubblica(InternoSfidaCartaForbiciSasso.apre());
		BusEventi.pubblica(new NotificaTestoFrase(nomeDelloSfidante() + " dice: \"" + ProduttoreDiSfideCasuale.fraseDiSfida() + "\""));
		impostaComandiPossibili();
		return Stato.IN_LOCAZIONE;
	}

	/**
	 * Una mano: il giocatore ha scelto carta, forbici o sasso, e l'avversario sceglie a caso. Un altro comando non
	 * conta (la sfida non si può lasciare).
	 */
	private Stato giocaUnaMano(Comando azione) {
		MossaCartaForbiciSasso mossa = MossaCartaForbiciSasso.da(azione);
		if (mossa == null) {
			impostaComandiPossibili();
			return Stato.IN_LOCAZIONE;
		}
		PartitaCartaForbiciSasso.Mano mano = sfida.gioca(mossa);
		BusEventi.pubblica(InternoSfidaCartaForbiciSasso.mano(mano.getGiocatore(), mano.getAvversario(),
				sfida.getVittorieDelGiocatore(), sfida.getVittorieDellAvversario(), sfida.isFinita()));
		if (!sfida.isFinita()) {
			impostaComandiPossibili();
			return Stato.IN_LOCAZIONE;
		}
		return concludiLaSfida();
	}

	/**
	 * Finita la sfida l'avversario commenta, e la locazione finisce. Se il gruppo ha vinto la terza sfida della partita
	 * l'avversario lo manda al torneo e parte La sfida dei campioni. Nella sfida di un campione, invece, la locazione è
	 * completa solo se il giocatore ha vinto: se ha perso resta com'era, e si può tornare a riprovare. In ogni caso è
	 * come aver stretto amicizia: l'oggetto non si prende.
	 */
	private Stato concludiLaSfida() {
		boolean vinta = sfida.haVintoIlGiocatore();
		boolean diUnCampione = gruppoAvversario.isSfidaACartaForbiciSasso();
		sfida = null;
		if (vinta) {
			// Lo sfidante non si uccide, ma per il bestiario è un incontro riuscito
			BusEventi.pubblica(new InternoSfidaVinta(gruppoAvversario.getCapo().getClasse()));
		}
		statoLocazione = StatoLocazione.IN_LOCAZIONE;
		haStrettoAmicizia = true;
		BusEventi.pubblica(new NotificaTestoFrase(nomeDelloSfidante() + " dice: \""
				+ (vinta ? ProduttoreDiSfideCasuale.fraseSeIlGiocatoreVince() : ProduttoreDiSfideCasuale.fraseSeIlGiocatorePerde()) + "\""));
		if (diUnCampione) {
			setCompleta(vinta);
			setOggetto(null);
			return Stato.FINE_LOCAZIONE;
		}
		if (vinta) {
			gruppo.addSfidaVinta();
			if (gruppo.getSfideVinte() >= Costanti.SFIDE_VINTE_PER_IL_TORNEO && !RegistroMissioni.isPartita(MissionePredefinita.LA_SFIDA_DEI_CAMPIONI)) {
				BusEventi.pubblica(new NotificaTestoFrase(nomeDelloSfidante() + " dice: \"" + ProduttoreDiSfideCasuale.fraseDelTorneo() + "\""));
				RegistroMissioni.avvia(MissionePredefinita.LA_SFIDA_DEI_CAMPIONI);
			}
		}
		setCompleta(true);
		return Stato.FINE_LOCAZIONE;
	}

	/**
	 * Lo spregio per un tentativo di amicizia o di corruzione fallito: un tiro a cinque facce. Un incantesimo a caso
	 * perso (se il gruppo ne ha di quella classe), un attacco dell'avversario, da 1 a {@value #SPREGIO_MASSIMO} monete o
	 * preziosi persi (se ce ne sono), oppure niente.
	 *
	 * @return che cosa è successo a chi ha tentato, da scrivere dopo "in una breve colluttazione"; null se non è
	 * successo niente
	 */
	private String subisciUnoSpregio(Personaggio personaggio) {
		switch (Dado.tira(5)) {
			case 1:
				TipoIncantesimo quale = FabbricaIncantesimi.casuale();
				if (gruppo.getIncantesimi(quale) > 0) {
					gruppo.subIncantesimi(quale, 1);
					return "perde un " + quale.getNomeSingolare() + '.';
				}
				return null;
			case 2:
				return subisciUnAttaccoDellAvversario(personaggio);
			case 3:
				if (gruppo.getMonete() > 0) {
					gruppo.subMonete(Math.min(Dado.tira(SPREGIO_MASSIMO), gruppo.getMonete()));
					return "perde alcune monete.";
				}
				return null;
			case 4:
				if (gruppo.getPreziosi() > 0) {
					gruppo.subPreziosi(Math.min(Dado.tira(SPREGIO_MASSIMO), gruppo.getPreziosi()));
					return "perde alcuni preziosi.";
				}
				return null;
			default:
				return null;
		}
	}

	/**
	 * Un solo attacco del capo degli avversari, con l'arma principale: se colpisce, si calcola il danno come in ogni
	 * combattimento (difese comprese) ma il danno diretto si limita al {@value #SPREGIO_FERITE_MASSIME_PERCENTUALE}% della
	 * salute che il personaggio ha adesso (almeno un punto) e comunque in modo che gliene resti almeno uno. Il colpo non
	 * investe i vicini (niente reazioni ad area: è una colluttazione fra due soli). Gli effetti di stato del colpo si
	 * applicano interi, e il turno degli effetti che segue può farli scattare: chi è avvelenato o bruciato può quindi
	 * morire anche per un tentativo di amicizia o di corruzione fallito.
	 *
	 * @return la descrizione delle ferite, null se il colpo non arriva o non fa niente
	 */
	private String subisciUnAttaccoDellAvversario(Personaggio personaggio) {
		Personaggio avversario = gruppoAvversario.getCapo();
		FaseDiAttacco fase = CalcolatoreCombattimento.fasiDiAttacco(avversario).get(0);
		Arma arma = fase.getArma();
		if (!CalcolatoreCombattimento.colpisce(avversario, personaggio, arma.getTipoDanno().getCategoriaDanno())) {
			return null;
		}
		DannoRisultante risultato = CalcolatoreCombattimento.calcolaDannoRisultante(avversario, personaggio, arma, fase.getFattore());
		int salute = personaggio.getSalute();
		int tetto = Math.min(Math.max(1, salute * SPREGIO_FERITE_MASSIME_PERCENTUALE / 100), Math.max(0, salute - 1));
		int danno = Math.min(risultato.getDanno(), tetto);
		risultato.setDanno(danno);
		risultato.togliDanniAdArea();
		ApplicazioneDelRisultato.applica(personaggio, risultato);
		if (danno <= 0 && risultato.getEffettiDiStatoDaAggiungere().isEmpty()) {
			return null;
		}
		if (danno * 100 > salute * SPREGIO_FERITE_GRAVI_PERCENTUALE) {
			return "riceve gravi ferite.";
		}
		return danno * 100 < salute * SPREGIO_FERITE_LIEVI_PERCENTUALE ? "riceve alcune lievi ferite." : "riceve alcune ferite.";
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
		annunciaAzione(combattente, gruppoAvversario.getPersonaggioVivo());
		// Il personaggio scelto agisce per primo, poi si alternano tutti
		statoDelCombattimento.faAgirePerPrimo(gruppo, combattente);
		opzioneAmiciziaDisponibile = false;
		opzioneCorruzioneDisponibile = false;
		statoLocazione = StatoLocazione.IN_COMBATTIMENTO;
	}
}
