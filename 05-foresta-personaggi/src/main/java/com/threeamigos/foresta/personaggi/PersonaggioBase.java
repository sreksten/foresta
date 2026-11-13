package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.artefatti.Arma;
import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.artefatti.VistaArtefatto;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoCreazionePersonaggio;
import com.threeamigos.foresta.eventi.notifiche.NotificaVariazioneStatistichePersonaggio;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.modellodati.ArtefattoMD;
import com.threeamigos.foresta.modellodati.EffettoDiStatoMD;
import com.threeamigos.foresta.modellodati.ModificatoreAttributoMD;
import com.threeamigos.foresta.modellodati.PersonaggioMD;
import com.threeamigos.foresta.modificatori.ModificatoreAttivo;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.testi.Lingue;
import com.threeamigos.foresta.testi.Nome;
import com.threeamigos.foresta.tipi.*;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.threeamigos.foresta.personaggi.AttributiSecondari.*;

/*
 * I personaggi originali della Foresta su ZX Spectrum:
 * Kloin l'elfo
 * Korleth il nano
 * Ankus il gigante
 */

/**
 * La base di tutti i personaggi: i giocanti, i mostri e le comparse. Tiene i dati in un {@link PersonaggioMD} e implementa le
 * regole comuni: salute e magia (con i loro massimi, che crescono con il livello), stanchezza, esperienza e livelli, punti
 * abilità, modificatori e stati alterati, inventario ed equipaggiamento, danni e morte.
 * <p>
 * Una classe concreta aggiunge solo ciò che la distingue: i valori di partenza dei suoi attributi, che prende dalle costanti
 * della classe ({@code CostantiPersonaggi.GOBLIN_*} e simili), i nomi e gli articoli, il repertorio di incantesimi, le immunità e le
 * offerte che può fare.
 */
public abstract class PersonaggioBase implements Personaggio {

	protected PersonaggioMD md = new PersonaggioMD();
	final ModificatoriTemporanei modificatori = new ModificatoriTemporanei(this);
	private final EffettiDiStato effettiDiStato = new EffettiDiStato(this);
	private final SaluteEMorte saluteEMorte = new SaluteEMorte(this);
	private final NomeEDescrizione nomeEDescrizione = new NomeEDescrizione(this);
	private final Progressione progressione = new Progressione(this);
	private final CaricoEInventario caricoEInventario = new CaricoEInventario(this);

	/**
	 * Posizione all'interno del gruppo
	 */
	private int ordinale;
	/**
	 * Fuori dal combattimento di questa locazione (vedi Personaggio.isInPanchina): non si salva.
	 */
	private boolean inPanchina;
	/**
	 * Combatte fino alla resa (vedi Personaggio.isFinoAllaResa): non si salva.
	 */
	private boolean finoAllaResa;
	/**
	 * Sfida a duello (vedi Personaggio.isSfidante): non si salva.
	 */
	private boolean sfidante;
	/**
	 * Sfida a carta, forbici e sasso (vedi Personaggio.isSfidanteACartaForbiciSasso): non si salva.
	 */
	private boolean sfidanteACartaForbiciSasso;
	/**
	 * Va affrontato per forza (vedi Personaggio.isDaAffrontare): non si salva.
	 */
	private boolean daAffrontare;

	/**
	 * Crea un personaggio (non giocante: lo diventa entrando nel gruppo) di quella classe e di quel livello, con i valori di partenza: i boss con salute e magia piene,
	 * gli altri con un valore fra il 75% e il 100% dei massimi.
	 */
	public PersonaggioBase(TipoPersonaggio classe, int livello) {
		// La classe va scritta per prima: i valori di partenza vengono dalla sua riga della tabella (CostantiPersonaggi.valoriDi)
		md.setClasse(classe);
		md.setPng(true);

		// I boss partono con salute e magia piene, gli altri personaggi con un pool di valori lievemente casuale,
		// dal 75% al 100% dei valori massimi
		Function<Integer, Integer> funzionePerValoriIniziali;
		if (isParteConValoriMassimi()) {
			funzionePerValoriIniziali = val -> val;
		} else {
			// Un numero a caso fra i tre quarti del massimo e il massimo
			funzionePerValoriIniziali = (max) -> Dado.tiraAncheSenzaRange(max * 3 / 4, max);
		}

		// Questo imposta salute, magia e stanchezza
		impostaValoriDiPartenzaGenerali(funzionePerValoriIniziali, livello);

		// Gli attributi primari vengono dal budget della classe (LanciatoreDeiDadi), boss compresi
		LanciatoreDeiDadi.tiraDadiPer(classe, getLivello(), md);

		ricalcolaAttributiSecondari();
		BusEventi.pubblica(new InternoCreazionePersonaggio(this));
	}

	/**
	 * Un personaggio con un nome proprio (il giocatore, uno di quelli che si incontrano nelle locande, un residente). Come tutti
	 * nasce PNG: smette di esserlo quando entra a far parte del gruppo (vedi GruppoGiocatore.aggiungiPersonaggio).
	 */
	public PersonaggioBase(String nome, TipoPersonaggio classe, int livello) {
		this(classe, livello);
		md.setNome(nome);
	}

	/**
	 * I valori fissi della classe del personaggio: caratteristiche, valori di partenza e moltiplicatori (vedi ValoriDiClasse)
	 */
	private ValoriDiClasse valori() {
		return CostantiPersonaggi.valoriDi(getClasse());
	}

	/**
	 * Quanti personaggi della sua classe si incontrano al massimo in una locazione
	 */
	int getQuantitaMassima() {
		return valori().getQuantitaMassima();
	}

	/**
	 * Basati sull'uuid del PersonaggioMD, non sull'istanza Java: chi richiede lo stesso
	 * personaggio più volte (es. RegistroPersonaggi.getPersonaggioInLocazione()) può
	 * ottenere ogni volta un wrapper Personaggio diverso sullo stesso modello dati, e
	 * l'uuid resta lo stesso anche dopo un salvataggio/caricamento.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Personaggio)) {
			return false;
		}
		return md.getUuid().equals(((Personaggio) obj).getModelloDati().getUuid());
	}

	/**
	 * Coerente con {@link #equals(Object)}: dipende solo dall'identificativo del modello dati.
	 */
	@Override
	public int hashCode() {
		return md.getUuid().hashCode();
	}
	
	public int getOrdinale() {
		return ordinale;
	}

	public void setOrdinale(int ordinale) {
		this.ordinale = ordinale;
	}

	@Override
	public Personaggio.Sesso getSesso() {
		return valori().isFemmina() ? Personaggio.Sesso.FEMMINA : Personaggio.Sesso.MASCHIO;
	}

	@Override
	public boolean isCorrompibile() {
		return valori().isCorrompibile();
	}

	@Override
	public boolean isAmichevole() {
		return valori().isAmichevole();
	}

	/**
	 * Gli incantesimi malefici che il personaggio sa lanciare quando attacca (vedi RegistroTattiche e Requisiti): i boss
	 * tutti, Morte compresa; gli altri tutti tranne Morte. Le classi di mostri con un carattere preciso lo
	 * restringono (l'Arpia l'Aria, i non morti il Gelo...). Gli incantesimi GLOBALI (che agiscono su tutta la locazione, compreso il gruppo di chi
	 * li lancia) non ci sono: i mostri non sanno ancora lanciarli.
	 */
	public Set<TipoIncantesimo> getRepertorioIncantesimi() {
		Set<TipoIncantesimo> repertorio = EnumSet.noneOf(TipoIncantesimo.class);
		for (TipoIncantesimo tipoIncantesimo : TipoIncantesimo.values()) {
			if (tipoIncantesimo.getCategoriaIncantesimo() == CategoriaIncantesimo.MALEFICO
					&& tipoIncantesimo.getPortata() != PortataIncantesimo.GLOBALE
					&& (tipoIncantesimo != TipoIncantesimo.MORTE || isParteConValoriMassimi())) {
				repertorio.add(tipoIncantesimo);
			}
		}
		return repertorio;
	}

	/**
	 * Se la classe ha la magia: la sua magia cresce con il livello (getLivellamentoMagia maggiore di 0)
	 */
	public boolean isMagico() {
		return getLivellamentoMagia() > 0.0d;
	}

	protected TipoOfferta[] getOfferteAmicizia() {
		return new TipoOfferta[0];
	}

	protected TipoOfferta[] getOfferteCorruzione() {
		return new TipoOfferta[0];
	}
	
	/**
	 * Se è un non morto (categoria NON_MORTO).
	 * (Le debolezze e le immunità della classe ai tipi di danno, agli incantesimi e agli effetti di stato stanno in CategoriaPersonaggio e
	 * TipoPersonaggio, dove le legge anche il bestiario.)
	 */
	private boolean isNonMorto() {
		return getClasse().isDellaCategoria(CategoriaPersonaggio.NON_MORTO);
	}

	/**
	 * Immune a un incantesimo: lo è la sua classe (i non morti, a Morte e Veleno; vedi TipoPersonaggio.isImmuneAIncantesimo) oppure, in più, lui in
	 * particolare (vedi {@link #isImmuneAIncantesimoPerIstanza})
	 */
	public boolean isImmuneAIncantesimo(TipoIncantesimo tipoIncantesimo) {
		return getClasse().isImmuneAIncantesimo(tipoIncantesimo) || isImmuneAIncantesimoPerIstanza(tipoIncantesimo);
	}

	/**
	 * Le immunità agli incantesimi che non sono della classe ma di questo personaggio: di norma nessuna. Serve a chi vuole un personaggio particolare
	 * (un goblin di una missione) con un'immunità che gli altri della sua classe non hanno, o che dipende dal suo stato. Non entrano nel bestiario,
	 * che descrive le classi.
	 */
	protected boolean isImmuneAIncantesimoPerIstanza(TipoIncantesimo tipoIncantesimo) {
		return false;
	}

	/**
	 * Immune a un tipo di danno: non lo sente proprio (moltiplicatore 0)
	 */
	public boolean isImmuneATipoDanno(TipoDanno tipoDanno) {
		return getMoltiplicatoreDannoSubito(tipoDanno) == 0.0d;
	}

	/**
	 * Debolezze e resistenze della classe e delle sue categorie ai tipi di danno
	 */
	public double getMoltiplicatoreDannoSubito(TipoDanno tipoDanno) {
		return getClasse().getMoltiplicatoreDannoSubito(tipoDanno);
	}

	/**
	 * Immune a un effetto di stato: lo è la sua classe (i non morti non sanguinano e non si infettano; vedi TipoPersonaggio.isImmuneAEffetto)
	 * oppure, in più, lui in particolare (vedi {@link #isImmuneAEffettoPerIstanza})
	 */
	public boolean isImmuneAEffetto(TipoEffettoDiStato effettoStato) {
		return getClasse().isImmuneAEffetto(effettoStato) || isImmuneAEffettoPerIstanza(effettoStato);
	}

	/**
	 * Le immunità agli effetti di stato che non sono della classe ma di questo personaggio, in questo momento: chi ha il berserk (vedi
	 * Berserk) non si confonde e non si spaventa finché ci è dentro. Una classe può aggiungerne di sue. Non entrano nel bestiario, che
	 * descrive le classi.
	 */
	protected boolean isImmuneAEffettoPerIstanza(TipoEffettoDiStato effettoStato) {
		return Berserk.haIlBerserk(getClasse()) && hasEffettoDiStato(TipoEffettoDiStato.BERSERK)
				&& (effettoStato == TipoEffettoDiStato.CONFUSO || effettoStato == TipoEffettoDiStato.SPAVENTATO);
	}
	
	/**
	 * I boss: partono con salute e magia piene e non possono essere mietuti (vedi ApplicazioneDelRisultato). Lo dice la tabella della classe.
	 */
	@Override
	public boolean isParteConValoriMassimi() {
		return valori().isBoss();
	}

	public boolean isPNG() {
		return md.isPng();
	}

	/**
	 * Se le cure lo guariscono: non un non morto del gruppo del giocatore (vedi Personaggio.isCurabile)
	 */
	public boolean isCurabile() {
		return !isNonMorto() || !GruppoGiocatore.getIstanza().contiene(this);
	}

	public boolean isImmortale() {
		return false;
	}

	// Chi il motore protegge dalla morte, oltre a chi è salvo fino alla scadenza: i personaggi non sanno del tutorial
	private static volatile Predicate<Personaggio> protezioneDallaMorte = p -> false;

	/**
	 * Installa chi il motore protegge dalla morte (vedi {@link #isSalvoDallaMorte()}): il capo, mentre il tutorial pilota.
	 */
	public static void impostaProtezioneDallaMorte(Predicate<Personaggio> protezione) {
		protezioneDallaMorte = protezione;
	}

	@Override
	public boolean isSalvoDallaMorte() {
		return md.isSalvoFinoAllaScadenza() && isATempo() || protezioneDallaMorte.test(this);
	}

	public boolean isVivo() {
		return md.isVivo();
	}

	@Override
	public boolean isInPanchina() {
		return inPanchina;
	}

	@Override
	public void setInPanchina(boolean inPanchina) {
		this.inPanchina = inPanchina;
	}

	@Override
	public boolean isFinoAllaResa() {
		return finoAllaResa;
	}

	@Override
	public void setFinoAllaResa(boolean finoAllaResa) {
		this.finoAllaResa = finoAllaResa;
	}

	@Override
	public boolean isDaAffrontare() {
		return daAffrontare;
	}

	@Override
	public void setDaAffrontare(boolean daAffrontare) {
		this.daAffrontare = daAffrontare;
	}

	@Override
	public boolean isSfidante() {
		return sfidante;
	}

	@Override
	public void setSfidante(boolean sfidante) {
		this.sfidante = sfidante;
	}

	@Override
	public boolean isSfidanteACartaForbiciSasso() {
		return sfidanteACartaForbiciSasso;
	}

	@Override
	public void setSfidanteACartaForbiciSasso(boolean sfidante) {
		this.sfidanteACartaForbiciSasso = sfidante;
	}

	/**
	 * Se sta dalla parte del giocatore: nel suo gruppo o fra i suoi ospiti (un ospite vulnerabile non è nella lista dei personaggi, ma non è
	 * un avversario: per esempio la Purificazione che lo colpisce cura il gruppo del giocatore, non i mostri).
	 */
	@Override
	public boolean appartieneAlGruppoDelGiocatore() {
		GruppoGiocatore giocatore = GruppoGiocatore.getIstanza();
		return giocatore.contiene(this) || giocatore.getOspiti().contains(this);
	}

	public String getCausaTrapasso() {
		return md.getCausaTrapasso();
	}

	/**
	 * Quanti bersagli colpisce un incantesimo MULTIPLO: il NUMERO_BERSAGLI calcolato dalle caratteristiche (con il
	 * moltiplicatore di classe, vedi calcolaNumeroBersagli) più i modificatori di bastoni e libri.
	 */
	public int getBersagli() {
		double numeroBase = md.getOptional(TipoAttributo.NUMERO_BERSAGLI).orElse(1.0d);
		double numeroBersagli = getQuantitaModificata(md, numeroBase, TipoAttributo.NUMERO_BERSAGLI);
		// Almeno un bersaglio: con un moltiplicatore sotto 1 il troncamento darebbe 0
		return (int)(Math.max(1, numeroBersagli));
	}

	/**
	 * Il POTERE_MAGICO non ha un valore proprio: si parte da 100 e si applicano i modificatori, quelli fissi come
	 * punti percentuali (+5 porta a 105) e quelli percentuali in proporzione (+20% porta a 120).
	 */
	@Override
	public double getPotereMagico() {
		return getQuantitaModificata(md, 100.0d, TipoAttributo.POTERE_MAGICO);
	}

	/**
	 * Applica ai danni di un incantesimo il POTERE_MAGICO (non la Magia, che è solo la riserva)
 	 */
	@Override
	public double getModificaDanniMagia(double danni) {
		return danni * getPotereMagico() / 100.0d;
	}

	/**
	 * L'arma impugnata nella mano principale (o a due mani); senza, l'arma naturale.
	 */
	@Override
	public Arma getArmaEquipaggiata() {
		return armaInSlot(TipoSlotArtefatto.MANO_PRINCIPALE, TipoSlotArtefatto.ENTRAMBE_LE_MANI)
				.orElseGet(() -> new ArmaNaturale(this));
	}

	/**
	 * L'arma nella mano secondaria, per chi combatte con due armi (Ladro/Ladra, Elfo/Elfa).
	 */
	@Override
	public Optional<Arma> getArmaSecondaria() {
		return armaInSlot(TipoSlotArtefatto.MANO_SECONDARIA);
	}

	private Optional<Arma> armaInSlot(TipoSlotArtefatto... slot) {
		List<TipoSlotArtefatto> slotAmmessi = Arrays.asList(slot);
		return md.getArtefatti().stream()
				.filter(a -> a.getTipo().getCategoria() == CategoriaArtefatto.ARMA)
				.filter(a -> slotAmmessi.contains(RegoleEquipaggiamento.slotOccupato(a)))
				.findFirst()
				.map(a -> (Arma) Artefatto.di(a));
	}

	public boolean isATempo() {
		return md.getMotivoNelGruppo().isATempo();
	}

	public void setTempo(int tempo) {
		double quantitaPrecedente = md.getOptional(TipoAttributo.TEMPO).orElse(0.0d);
		md.set(TipoAttributo.TEMPO, tempo);
		BusEventi.pubblica(new NotificaVariazioneStatistichePersonaggio(this, TipoAttributo.TEMPO, quantitaPrecedente, tempo));
	}

	public int decrementaTempo() {
		double tempo = md.getOptional(TipoAttributo.TEMPO).orElse(0.0d);
		// Chi è nel gruppo per una missione ci resta finché è la missione a congedarlo
		if (!md.isVivo() || !isATempo() || md.getMotivoNelGruppo() == TipoMotivoNelGruppo.MISSIONE) {
			return (int)tempo;
		}
		double quantitaPrecedente = tempo;
		tempo--;
		md.set(TipoAttributo.TEMPO, tempo);
		BusEventi.pubblica(new NotificaVariazioneStatistichePersonaggio(this, TipoAttributo.TEMPO, quantitaPrecedente, tempo));
		return (int)tempo;
	}

	@Override
	public void setMotivoNelGruppo(TipoMotivoNelGruppo motivo) {
		md.setMotivoNelGruppo(motivo);
	}

	@Override
	public TipoMotivoNelGruppo getMotivoNelGruppo() {
		return md.getMotivoNelGruppo();
	}

	public TipoOfferta getOfferta(TipoApproccio approccio) {
		TipoOfferta[] offerte = null;
		if (isAmichevole() && approccio == TipoApproccio.AMICIZIA) {
			offerte = getOfferteAmicizia();
		} else {
			offerte = getOfferteCorruzione();
		}
		// A gruppo pieno nessuno si puo' unire: le offerte di aiuto non si propongono
		if (GruppoGiocatore.getIstanza().getNumeroPersonaggi() >= CostantiPersonaggi.MAX_PERSONAGGI_GRUPPO_TOTALE) {
			offerte = Arrays.stream(offerte)
					.filter(o -> o != TipoOfferta.AIUTO_GRATUITO && o != TipoOfferta.AIUTO_MERCENARIO)
					.toArray(TipoOfferta[]::new);
		}
		if (offerte.length > 0) {
			int indice = Dado.tiraAncheAUnaFaccia(offerte.length) - 1;
			return offerte[indice];
		}
		return null;
	}
	
	public PersonaggioMD getModelloDati() {
		return md;
	}
	
	public void setModelloDati(PersonaggioMD personaggioMD) {
		this.md = personaggioMD;
		ricalcolaAttributiSecondari();
	}

	// CLASSE
	// Il nome della classe con quel che serve per articolarlo: vedi NomeDellaClasse
	private NomeDellaClasse nomeDellaClasse() {
		return NomeDellaClasse.di(getClasse());
	}

	@Override
	public String getNomeSingolare() {
		return nomeDellaClasse().getNomeSingolare();
	}

	@Override
	public String getNomePlurale() {
		return nomeDellaClasse().getNomePlurale();
	}

	@Override
	public String getADS() {
		return nomeDellaClasse().getADS();
	}

	@Override
	public String getADP() {
		return nomeDellaClasse().getADP();
	}

	@Override
	public String getAIS() {
		return nomeDellaClasse().getAIS();
	}

	@Override
	public String getAIP() {
		return nomeDellaClasse().getAIP();
	}

	@Override
	public String getDeS() {
		return nomeDellaClasse().getDeS();
	}

	@Override
	public String getDeP() {
		return nomeDellaClasse().getDeP();
	}

	@Override
	public String getDa() {
		return nomeDellaClasse().getDa();
	}

	@Override
	public String getPronome() {
		return nomeDellaClasse().getPronome();
	}

	@Override
	public TipoPersonaggio getClasse() {
		return md.getClasse();
	}

	// NOME

	@Override
	public String getDescrizione() {
		return nomeEDescrizione.getDescrizione();
	}

	@Override
	public String getNome(Personaggio.OpzioniGetNome... opzioni) {
		return nomeEDescrizione.getNome(opzioni);
	}

	@Override
	public Optional<String> getNomeProprio() {
		return Optional.ofNullable(md.getNome());
	}

	@Override
	public String getLetteraFinaleAttributo() {
		return getSesso() == Personaggio.Sesso.MASCHIO ? "o" : "a";
	}

	/**
	 * Da chiamare durante l'inizializzazione di un personaggio: lo fa vivo, al livello dato e senza esperienza, e gli dà salute,
	 * magia e stanchezza di partenza. Gli attributi primari e secondari li impostano altri (LanciatoreDeiDadi e
	 * ricalcolaAttributiSecondari, nel costruttore).
	 * @param funzione dal massimo di un valore, quello di partenza: il massimo stesso (i boss) o un valore a caso fra i tre quarti e il
	 *                 massimo. Per la stanchezza vale al contrario (si parte da MAX_STANCHEZZA meno il valore: da 0 a 2 di stanchezza).
	 */
	private void impostaValoriDiPartenzaGenerali(Function<Integer, Integer> funzione, int livello) {
		md.setVivo(true);
		md.setLivello(livello);
		md.setEsperienza(0);
		md.set(TipoAttributo.SALUTE, funzione.apply(getSaluteMassima()));
		md.set(TipoAttributo.MAGIA,funzione.apply(getMagiaMassima()));
		md.set(TipoAttributo.STANCHEZZA,CostantiPersonaggi.MAX_STANCHEZZA - funzione.apply(CostantiPersonaggi.MAX_STANCHEZZA));
	}


	/**
	 * Funzione che limita il valore di un attributo al suo massimo (valore base più modifica da artefatti). Non restituisce mai meno di 0
	 * per un'aggiunta: se il valore è già sopra il massimo (per esempio è scaduto un bonus), non lo si abbassa aggiungendo.
	 */
	int limitaEntroMassimi(int valoreAttuale, int valoreMassimo, int quantitaDaAggiungere) {
		if (valoreMassimo == PersonaggioMD.SENZA_LIMITE) {
			return quantitaDaAggiungere;
		}
		if (valoreAttuale + quantitaDaAggiungere <= valoreMassimo) {
			return quantitaDaAggiungere;
		}
		return Math.max(0, valoreMassimo - valoreAttuale);
	}

	// LIVELLO - non è un attributo con un massimo

	public int getLivello() {
		return md.getLivello();
	}

	// ESPERIENZA - non è un attributo con un massimo

	public int getEsperienza() {
		return md.getEsperienza();
	}

	@Override
	public boolean isAlLivelloMassimo() {
		return md.getLivello() >= CostantiPersonaggi.MASSIMO_LIVELLO;
	}

	@Override
	public int getEsperienzaPerProssimoLivello() {
		return GestoreProgressione.getXpPerProssimoLivello(md.getLivello());
	}

	@Override
	public int getPuntiAbilitaDisponibili() {
		return md.getPuntiAbilitaDisponibili();
	}

	@Override
	public boolean puoFormulare(TipoIncantesimo tipoIncantesimo) {
		return getMagia() >= tipoIncantesimo.getCostoPerBersaglio() && !hasEffettoDiStato(TipoEffettoDiStato.SILENZIATO);
	}

	// ESPERIENZA E PUNTI ABILITA'

	@Override
	public void addPuntiEsperienza(int esperienza) {
		// Chi è nel gruppo per una missione non cresce: com'è arrivato, se ne va
		if (md.getMotivoNelGruppo() == TipoMotivoNelGruppo.MISSIONE) {
			return;
		}
		progressione.addPuntiEsperienza(esperienza);
	}

	@Override
	public void impostaLivelloDopoReclutamento(int livello) {
		progressione.impostaLivelloDopoReclutamento(livello);
	}

	@Override
	public void spendiPuntoAbilita(TipoAttributo tipoAttributo) {
		progressione.spendiPuntoAbilita(tipoAttributo);
	}

	// CARICO E INVENTARIO

	@Override
	public double getCarico() {
		return caricoEInventario.getCarico();
	}

	@Override
	public Optional<RegoleEquipaggiamento.EsitoControlloRichiestaEquipaggiamento> puoEquipaggiare(Artefatto artefatto) {
		return caricoEInventario.puoEquipaggiare(artefatto);
	}

	@Override
	public boolean isEquipaggiabile(VistaArtefatto artefatto) {
		return caricoEInventario.isEquipaggiabile(artefatto);
	}

	@Override
	public List<String> getDescrizioniSetCompleti() {
		return caricoEInventario.getDescrizioniSetCompleti();
	}

	public boolean puoPrendere(double quantita) {
		return caricoEInventario.puoPrendere(quantita);
	}

	@Override
	public int getCaricoMassimo() {
		return caricoEInventario.getCaricoMassimo();
	}

	@Override
	public List<Artefatto> getInventario() {
		return caricoEInventario.getInventario();
	}

	public void addArtefatto(Artefatto a) {
		caricoEInventario.addArtefatto(a);
	}

	public void removeArtefatto(Artefatto a) {
		caricoEInventario.removeArtefatto(a);
	}

	// SALUTE

	@Override
	public int getRigenerazioneSalute() {
		return saluteEMorte.getRigenerazioneSalute();
	}

	@Override
	public int getRigenerazioneMagia() {
		return saluteEMorte.getRigenerazioneMagia();
	}

	@Override
	public void riposa(int ore, TipoRiposo tipoRiposo) {
		saluteEMorte.riposa(ore, tipoRiposo);
	}

	@Override
	public void fugge() {
		saluteEMorte.fugge();
	}

	@Override
	public void muore(String causaTrapasso) {
		saluteEMorte.muore(causaTrapasso);
	}

	@Override
	public void resuscita() {
		saluteEMorte.resuscita();
	}

	@Override
	public void addSalute(int quantita) {
		saluteEMorte.addSalute(quantita);
	}

	@Override
	public void subSalute(int quantita, Personaggio avversario, Personaggio.NotificaFerite notificaFerite, Personaggio.NotificaMorte notificaMorte) {
		saluteEMorte.subSalute(quantita, avversario, notificaFerite, notificaMorte);
	}

	@Override
	public void addMagia(int quantita) {
		saluteEMorte.addMagia(quantita);
	}

	@Override
	public void subMagia(int quantita) {
		saluteEMorte.subMagia(quantita);
	}

	@Override
	public int getSalute() {
		return (int) md.get(TipoAttributo.SALUTE);
	}

	// SALUTE MASSIMA

	/**
	 * La quantità di base accresciuta da bonus da artefatti e altri modificatori
	 */
	@Override
	public int getSaluteMassima() {
		return (int)calcolaSaluteMassima();
	}

	/**
	 * La salute massima: quella di base della classe per la radice quadrata del livello, più i modificatori.
	 */
	protected double calcolaSaluteMassima() {
		return getQuantitaModificata(md, saluteMassimaSenzaModificatori(), TipoAttributo.SALUTE);
	}

	/**
	 * La salute massima prima dei modificatori: la base a cui si applicano.
	 */
	private double saluteMassimaSenzaModificatori() {
		// Cresce con la radice quadrata del livello (100, 141, 173, 200...), per tutti: come il danno
		// (vedi CalcolatoreCombattimento.livelloDiCombattimento), così uno scontro alla pari dura uguale a ogni livello
		double saluteMassima = getSaluteBase() * Math.sqrt(getLivello());
		if (getClasse().isMostroComune()) {
			// I mostri comuni crescono un po' più dei giocanti, che hanno le armi e le armature di livello via via più alto
			saluteMassima *= 1.0d + CostantiPersonaggi.MOSTRI_COMUNI_CRESCITA_SALUTE_PER_LIVELLO * (getLivello() - 1);
		}
		return saluteMassima;
	}

	public void addSaluteMassima(int quantita, String note) {
		ModificatoreAttributoMD modificatore = new ModificatoreAttributoMD(TipoAttributo.SALUTE, TipoModificatore.VARIAZIONE_FISSA, quantita, note);
		addModificatore(modificatore);
	}

	// Magia

	@Override
	public int getMagia() {
		return (int) md.get(TipoAttributo.MAGIA);
	}

	@Override
	public int getMagiaMassima() {
		return (int)calcolaMagiaMassima();
	}

	@Override
	public void addMagiaMassima(int quantita, String note) {
		ModificatoreAttributoMD modificatore = new ModificatoreAttributoMD(TipoAttributo.MAGIA, TipoModificatore.VARIAZIONE_FISSA, quantita, note);
		addModificatore(modificatore);
	}

	/**
	 * La magia massima: quella di base più il livellamento per la radice del livello meno uno, più i modificatori; zero per chi non ha magia.
	 */
	protected double calcolaMagiaMassima() {
		if (getLivellamentoMagia() == 0.0d) {
			return 0.0d;
		}
		return getQuantitaModificata(md, magiaMassimaSenzaModificatori(), TipoAttributo.MAGIA);
	}

	/**
	 * La magia massima prima dei modificatori: zero per chi non ha magia.
	 */
	private double magiaMassimaSenzaModificatori() {
		return getLivellamentoMagia() == 0.0d ? 0.0d : getMagiaBase() + getLivellamentoMagia() * Math.sqrt(getLivello() - 1);
	}

	/**
	 * La salute recuperata a fine locazione prima dei modificatori di RIGENERAZIONE_SALUTE: 5 punti più una quota della salute massima
	 * (la usa SaluteEMorte.getRigenerazioneSalute)
	 */
	double rigenerazioneSaluteSenzaModificatori() {
		return CostantiPersonaggi.RIGENERAZIONE_PARTE_FISSA + getSaluteMassima() * CostantiPersonaggi.RIGENERAZIONE_SALUTE_QUOTA_SALUTE_MASSIMA;
	}

	/**
	 * La magia recuperata a fine locazione prima dei modificatori di RIGENERAZIONE_MAGIA: 5 punti più una quota della magia massima
	 * (la usa SaluteEMorte.getRigenerazioneMagia)
	 */
	double rigenerazioneMagiaSenzaModificatori() {
		return CostantiPersonaggi.RIGENERAZIONE_PARTE_FISSA + getMagiaMassima() * CostantiPersonaggi.RIGENERAZIONE_MAGIA_QUOTA_MAGIA_MASSIMA;
	}

	// ATTRIBUTI PRIMARI E SECONDARI

	/**
	 * Il valore di un attributo primario o secondario con i modificatori. La parata e la resistenza magica aggiungono anche quello che dà
	 * l'equipaggiamento di suo (vedi {@link #parataConEquipaggiamento} e {@link #resistenzaMagicaConEquipaggiamento}).
	 */
	@Override
	public int get(TipoAttributo tipoAttributo) {
		switch (tipoAttributo) {
			case PARATA:
				return parataConEquipaggiamento();
			case RESISTENZA_MAGICA:
				return resistenzaMagicaConEquipaggiamento();
			default:
				return valoreDi(md, tipoAttributo);
		}
	}

	@Override
	public void add(TipoAttributo tipoAttributo, int quantita) {
		double valorePrecedente = md.get(tipoAttributo);
		double valoreAttuale = valorePrecedente + quantita;
		md.set(tipoAttributo, valoreAttuale);
		BusEventi.pubblica(new NotificaVariazioneStatistichePersonaggio(this, tipoAttributo, valorePrecedente, valoreAttuale));
	}

	// PARATA

	/**
	 * La PARATA con l'equipaggiamento: scudo, elmo, maschera, armatura e schinieri ne aggiungono una parte fissa più una
	 * per ogni loro livello, mentre chi impugna due armi o un'arma a due mani ha la guardia aperta e ne perde
	 * un quarto.
	 */
	private int parataConEquipaggiamento() {
		double parata = valoreDi(md, TipoAttributo.PARATA);
		for (ArtefattoMD artefatto : md.getArtefatti()) {
			parata += parataIntrinseca(artefatto);
		}
		if (haGuardiaAperta()) {
			parata *= CostantiPersonaggi.GUARDIA_APERTA_FATTORE_PARATA;
		}
		return (int) parata;
	}

	/**
	 * La PARATA che un pezzo difensivo dà di suo, oltre ai modificatori scritti sull'artefatto.
	 * La veste non para: dà lo stesso minimo in RESISTENZA_MAGICA (vedi resistenzaMagicaConEquipaggiamento).
	 */
	private static int parataIntrinseca(ArtefattoMD artefatto) {
		int livello = artefatto.getLivello();
		switch (artefatto.getTipo()) {
			case SCUDO:
				return CostantiPersonaggi.SCUDO_PARATA_MINIMA + CostantiPersonaggi.SCUDO_PARATA_PER_LIVELLO * livello;
			case ELMO:
				return CostantiPersonaggi.ELMO_PARATA_MINIMA + CostantiPersonaggi.ELMO_PARATA_PER_LIVELLO * livello;
			case MASCHERA:
				return CostantiPersonaggi.MASCHERA_PARATA_MINIMA + CostantiPersonaggi.MASCHERA_PARATA_PER_LIVELLO * livello;
			case ARMATURA:
				return CostantiPersonaggi.ARMATURA_PARATA_MINIMA + CostantiPersonaggi.ARMATURA_PARATA_PER_LIVELLO * livello;
			case SCHINIERI:
				return CostantiPersonaggi.SCHINIERI_PARATA_MINIMA + CostantiPersonaggi.SCHINIERI_PARATA_PER_LIVELLO * livello;
			default:
				return 0;
		}
	}

	private boolean haGuardiaAperta() {
		return md.getArtefatti().stream()
				.filter(a -> a.getTipo().getCategoria() == CategoriaArtefatto.ARMA)
				.map(RegoleEquipaggiamento::slotOccupato)
				.anyMatch(slot -> slot == TipoSlotArtefatto.ENTRAMBE_LE_MANI || slot == TipoSlotArtefatto.MANO_SECONDARIA);
	}

	// RESISTENZA MAGICA

	/**
	 * La RESISTENZA_MAGICA con l'equipaggiamento: lo scudo ne aggiunge per ogni suo livello, di più se è raro;
	 * la veste, l'armatura di chi usa la magia, ne dà quanto un'armatura dà di PARATA.
	 */
	private int resistenzaMagicaConEquipaggiamento() {
		int resistenza = valoreDi(md, TipoAttributo.RESISTENZA_MAGICA);
		for (ArtefattoMD artefatto : md.getArtefatti()) {
			if (artefatto.getTipo() == TipoArtefatto.SCUDO) {
				int perLivello = artefatto.getRarita() == TipoRaritaArtefatto.COMUNE
						? CostantiPersonaggi.SCUDO_RESISTENZA_MAGICA_PER_LIVELLO
						: CostantiPersonaggi.SCUDO_RARO_RESISTENZA_MAGICA_PER_LIVELLO;
				resistenza += perLivello * artefatto.getLivello();
			} else if (artefatto.getTipo() == TipoArtefatto.VESTE) {
				resistenza += CostantiPersonaggi.ARMATURA_PARATA_MINIMA + CostantiPersonaggi.ARMATURA_PARATA_PER_LIVELLO * artefatto.getLivello();
			}
		}
		return resistenza;
	}

	public void addStanchezza(int quantita) {
		int quantitaPrecedente = (int) md.get(TipoAttributo.STANCHEZZA);
		quantita = limitaEntroMassimi(quantitaPrecedente, CostantiPersonaggi.MAX_STANCHEZZA, quantita);
		int quantitaAttuale = quantitaPrecedente + quantita;
		md.setStanchezza(quantitaAttuale);
		BusEventi.pubblica(new NotificaVariazioneStatistichePersonaggio(this, TipoAttributo.STANCHEZZA, quantitaPrecedente, quantitaAttuale));
	}

	public void subStanchezza(int quantita) {
		int quantitaPrecedente = (int) md.get(TipoAttributo.STANCHEZZA);
		int quantitaAttuale = Math.max(quantitaPrecedente - quantita, 0);
		md.setStanchezza(quantitaAttuale);
		BusEventi.pubblica(new NotificaVariazioneStatistichePersonaggio(this, TipoAttributo.STANCHEZZA, quantitaPrecedente, quantitaAttuale));
	}

	@Override
	public void addModificatore(ModificatoreAttributoMD modificatore) {
		modificatori.addModificatore(modificatore);
	}

	@Override
	public Collection<ModificatoreAttributoMD> getModificatoriTemporanei() {
		return modificatori.getModificatoriTemporanei();
	}

	@Override
	public void riduciModificatori(UnitaDurataModificatore unitaDurata) {
		modificatori.riduciModificatori(unitaDurata);
	}

	@Override
	public void rimuoviIModificatoriTemporaneiRimovibili() {
		modificatori.rimuoviIModificatoriTemporaneiRimovibili();
	}

	@Override
	public double getVariazioneDaTemporanei(TipoAttributo tipoAttributo) {
		return modificatori.getVariazioneDaTemporanei(tipoAttributo);
	}

	@Override
	public Optional<ModificatoreAttivo> getFissaggio(TipoAttributo tipoAttributo) {
		return modificatori.getFissaggio(tipoAttributo);
	}

	/**
	 * Il valore a cui si applicano i modificatori di un attributo, lo stesso dei suoi getter (salute e magia massime e le
	 * rigenerazioni passano dagli stessi metodi). Serve a ModificatoriTemporanei per dire di quanto i soli temporanei spostano
	 * l'attributo.
	 */
	double baseDelModificabile(TipoAttributo tipoAttributo) {
		switch (tipoAttributo) {
			case POTERE_MAGICO:
				return 100.0d;
			case SALUTE:
				return saluteMassimaSenzaModificatori();
			case MAGIA:
				return magiaMassimaSenzaModificatori();
			case RIGENERAZIONE_SALUTE:
				return rigenerazioneSaluteSenzaModificatori();
			case RIGENERAZIONE_MAGIA:
				return rigenerazioneMagiaSenzaModificatori();
			case CARICO_MASSIMO:
				// Quello salvato ha già i modificatori: applicandoli di nuovo la variazione dei temporanei verrebbe contata due volte
				return potenzialeDiCarico(md, valori());
			default:
				return md.getOptional(tipoAttributo).orElse(0.0d);
		}
	}

	// Funzioni di calcolo per gli attributi derivati

	/**
	 * Ricalcola gli attributi secondari del personaggio: carico massimo, critico, precisione, velocità, furtività,
	 * parata, resistenza magica, percezione, soggezione, furia, coraggio, valore, contrattazione, numero bersagli
	 */
	protected void ricalcolaAttributiSecondari() {
		AttributiSecondari.ricalcola(md, valori(), this);
	}

	/**
	 * Ricalcola gli attributi secondari (carico massimo, critico, precisione, velocità, furtività, parata,
	 * resistenza magica, percezione, soggezione, furia, coraggio, valore, contrattazione, numero bersagli)
	 * direttamente su {@code md}, con i moltiplicatori della sua classe (CostantiPersonaggi.valoriDi). Non pubblica eventi: da usare
	 * in fase di caricamento, quando non esiste ancora un Personaggio vivo a cui riferirli.
	 */
	public static void ricalcolaAttributiSecondari(PersonaggioMD md) {
		AttributiSecondari.ricalcola(md, CostantiPersonaggi.valoriDi(md.getClasse()), null);
	}

	// I valori della classe, dalla sua riga della tabella (CostantiPersonaggi.valoriDi): uguali per tutti i personaggi della classe

	@Override
	public int getPuntiEsperienza() {
		return valori().getPuntiEsperienza();
	}

	@Override
	public double getSaluteBase() {
		return valori().getSaluteBase();
	}

	@Override
	public double getMagiaBase() {
		return valori().getMagiaBase();
	}

	@Override
	public double getLivellamentoMagia() {
		return valori().getLivellamentoMagia();
	}

	@Override
	public double getMaxIntelligenza() {
		return valori().getMaxIntelligenza();
	}

	@Override
	public double getMoltiplicatoreCarico() {
		return valori().getMoltiplicatoreCarico();
	}

	@Override
	public double getMoltiplicatoreCritico() {
		return valori().getMoltiplicatoreCritico();
	}

	@Override
	public double getMoltiplicatorePrecisione() {
		return valori().getMoltiplicatorePrecisione();
	}

	@Override
	public double getMoltiplicatoreVelocita() {
		return valori().getMoltiplicatoreVelocita();
	}

	@Override
	public double getMoltiplicatoreFurtivita() {
		return valori().getMoltiplicatoreFurtivita();
	}

	@Override
	public double getMoltiplicatoreParata() {
		return valori().getMoltiplicatoreParata();
	}

	@Override
	public double getMoltiplicatoreResistenzaMagica() {
		return valori().getMoltiplicatoreResistenzaMagica();
	}

	@Override
	public double getMoltiplicatorePercezione() {
		return valori().getMoltiplicatorePercezione();
	}

	@Override
	public double getMoltiplicatoreSoggezione() {
		return valori().getMoltiplicatoreSoggezione();
	}

	@Override
	public double getMoltiplicatoreFuria() {
		return valori().getMoltiplicatoreFuria();
	}

	@Override
	public double getMoltiplicatoreCoraggio() {
		return valori().getMoltiplicatoreCoraggio();
	}

	@Override
	public double getMoltiplicatoreValore() {
		return valori().getMoltiplicatoreValore();
	}

	@Override
	public double getMoltiplicatoreContrattazione() {
		return valori().getMoltiplicatoreContrattazione();
	}

	@Override
	public double getMoltiplicatoreNumeroBersagli() {
		return valori().getMoltiplicatoreNumeroBersagli();
	}

	@Override
	public double getMoltiplicatoreStanchezza() {
		return valori().getMoltiplicatoreStanchezza();
	}

	@Override
	public double getMoltiplicatoreDanniMagici() {
		return valori().getMoltiplicatoreDanniMagici();
	}

	@Override
	public double getMoltiplicatoreDanniFisici() {
		return valori().getMoltiplicatoreDanniFisici();
	}

	@Override
	public double getMoltiplicatoreRecuperoFisico() {
		return valori().getMoltiplicatoreRecuperoFisico();
	}

	@Override
	public double getMoltiplicatoreRecuperoMagico() {
		return valori().getMoltiplicatoreRecuperoMagico();
	}


	// EFFETTI DI STATO

	public Collection<EffettoDiStatoMD> getEffettiDiStato() {
		return effettiDiStato.getEffettiDiStato();
	}

	public void addEffettoDiStato(TipoEffettoDiStato tipoEffettoDiStato, int durata, int danniNelTempo) {
		effettiDiStato.addEffettoDiStato(tipoEffettoDiStato, durata, danniNelTempo);
	}

	@Override
	public void addEffettoDiStato(TipoEffettoDiStato tipoEffettoDiStato, int durata, int danniNelTempo, double intensita) {
		effettiDiStato.addEffettoDiStato(tipoEffettoDiStato, durata, danniNelTempo, intensita);
	}

	@Override
	public void applicaDanniDaEffettiDiStato() {
		effettiDiStato.applicaDanniDaEffettiDiStato();
	}

	@Override
	public int getRicarica(String nome) {
		return effettiDiStato.getRicarica(nome);
	}

	@Override
	public Map<String, Integer> getRicariche() {
		return effettiDiStato.getRicariche();
	}

	@Override
	public void setRicarica(String nome, int turni) {
		effettiDiStato.setRicarica(nome, turni);
	}

	@Override
	public void riduciRicariche() {
		effettiDiStato.riduciRicariche();
	}

	@Override
	public void riduciEffettiDiStato() {
		effettiDiStato.riduciEffettiDiStato();
	}

	@Override
	public void rimuoviTuttiGliEffettiDiStato() {
		effettiDiStato.rimuoviTuttiGliEffettiDiStato();
	}

	@Override
	public void rimuoviGliEffettiDiStatoNegativi() {
		effettiDiStato.rimuoviGliEffettiDiStatoNegativi();
	}

	@Override
	public void rimuoviEffettoDiStato(TipoEffettoDiStato tipoEffettoDiStato) {
		effettiDiStato.rimuoviEffettoDiStato(tipoEffettoDiStato);
	}

	@Override
	public boolean hasEffettoDiStato(TipoEffettoDiStato tipoEffettoDiStato) {
		return effettiDiStato.hasEffettoDiStato(tipoEffettoDiStato);
	}

	@Override
	public int getQuantitaEffettoDiStato(TipoEffettoDiStato tipoEffettoDiStato) {
		return effettiDiStato.getQuantitaEffettoDiStato(tipoEffettoDiStato);
	}
}
