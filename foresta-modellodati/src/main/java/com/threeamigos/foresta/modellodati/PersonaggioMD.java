package com.threeamigos.foresta.modellodati;

import com.threeamigos.foresta.modellodati.tattiche.TatticaMD;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoMotivoNelGruppo;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

import static com.threeamigos.foresta.modellodati.Serializzabile.*;

/**
 * I dati di un personaggio: la classe (mago, arpia...), il nome proprio facoltativo, se è vivo (o la causa della morte),
 * livello, esperienza e punti abilità non ancora assegnati, se è un PNG e perché sta nel gruppo, gli artefatti che
 * porta, i modificatori e gli effetti di stato in corso e la sua tattica di combattimento.
 * <p>
 * Gli <b>attributi</b> (salute, forza, destrezza...) stanno in una mappa indicizzata da {@link TipoAttributo}, con un
 * valore minimo e uno massimo facoltativi per ciascuno: {@link #set(TipoAttributo, double)} li rispetta, quindi un
 * valore fuori dai limiti viene portato al limite. Si leggono con {@link #get}, {@link #getOptional} e {@link #getMassimo}
 * (un valore intero, se serve, lo tronca chi lo legge); si scrivono con {@link #set} e {@link #setMassimo}. Restano
 * soltanto alcune scorciatoie di scrittura, {@code setForza}, {@code setSalute}..., perché le classi dei personaggi le
 * chiamano nel costruttore per darsi i valori di partenza, e lì si leggono meglio di un {@code set(TipoAttributo.FORZA, ...)}.
 * <p>
 * Le regole del gioco (come cambiano gli attributi, come si combatte) stanno nel motore; qui ci sono solo i dati.
 *
 * @author Stefano Reksten
 */
public class PersonaggioMD implements Serializzabile {

	/**
	 * Costante che indica che un dato attributo non ha limite.
	 * Per il tempo, indica che il personaggio è entrato definitivamente nel gruppo
	 * di un giocatore.
	 */
	public static final int SENZA_LIMITE = -1;

	/**
	 * Un identificativo unico per il personaggio
	 */
	private String uuid = UUID.randomUUID().toString();

	/**
	 * Classe del personaggio (Mago, Arpia, etc.)
	 */
	private TipoPersonaggio classe;
	/**
	 * Nome proprio del personaggio (quando presente)
	 */
	private String nome;

	// Impostati da PersonaggioBase uguali per tutti i personaggi
	/**
	 * Se il personaggio è vivo
	 */
	private boolean vivo;
	/**
	 * Livello del personaggio
	 */
	private int livello;
	/**
	 * Punti esperienza accumulati dal personaggio
	 */
	private int esperienza;
	/**
	 * Punti abilità guadagnati da passaggio a livello successivo e non ancora assegnati
	 */
	private int puntiAbilitaDisponibili;
	/**
	 * La causa della morte del personaggio
	 */
	private String causaTrapasso;
	/**
	 * Se è un PNG: non l'ha creato il giocatore, per cui non ha esperienza e può non avere un nome proprio
	 */
	private boolean png;
	/**
	 * Perché si trova nel gruppo del giocatore (se vi si trova)
	 */
	private TipoMotivoNelGruppo motivoNelGruppo = TipoMotivoNelGruppo.PERMANENTE;

	// Le tre mappe degli attributi sono EnumMap e non HashMap: l'hash di una costante di enum cambia a ogni esecuzione,
	// e con una HashMap lo stesso personaggio si scriverebbe nel salvataggio con gli attributi in un ordine diverso ogni
	// volta. Con una EnumMap l'ordine è sempre quello dell'enum.
	/**
	 * Valori minimi per un dato attributo - solitamente non presenti
	 */
	private final Map<TipoAttributo, Double> valoriMinimi = new EnumMap<>(TipoAttributo.class);
	/**
	 * Valori massimi per un dato attributo - solitamente non presenti
	 */
	private final Map<TipoAttributo, Double> valoriMassimi = new EnumMap<>(TipoAttributo.class);
	/**
	 * Valori per i vari attributi dei personaggi
	 */
	private final Map<TipoAttributo, Double> valoriAttributi = new EnumMap<>(TipoAttributo.class);
	/**
	 * Modificatori permanenti agli attributi dei personaggi
	 */
	private final List<ModificatoreAttributoMD> modificatori = new ArrayList<>();
	/**
	 * Tattica del personaggio
	 */
	private TatticaMD tattica;
	/**
	 * Effetti di stato applicati al personaggio
	 */
	private final List<EffettoDiStatoMD> effettiDiStato = new ArrayList<>();
	/**
	 * Artefatti posseduti dal personaggio
	 */
	private final List<ArtefattoMD> artefatti = new ArrayList<>();

	/**
	 * L'identificativo unico del personaggio, che il salvataggio conserva.
	 */
	public String getUuid() {
		return uuid;
	}

	public TipoPersonaggio getClasse() {
		return classe;
	}

	public void setClasse(TipoPersonaggio classe) {
		this.classe = classe;
	}

	/**
	 * Il nome proprio del personaggio, o null se non ne ha: allora si chiama con la sua classe.
	 */
	public String getNome() {
		return nome;
	}

	/**
	 * Il "|" si toglie; un nome vuoto vale come nessun nome (null): il personaggio si chiama allora con la sua classe.
	 */
	public void setNome(String nome) {
		String pulito = Serializzabile.pulisciCampo(nome);
		this.nome = pulito == null || pulito.trim().isEmpty() ? null : pulito;
	}

	public boolean isPng() {
		return png;
	}

	public void setPng(boolean png) {
		this.png = png;
	}

	public TipoMotivoNelGruppo getMotivoNelGruppo() {
		return motivoNelGruppo;
	}

	public void setMotivoNelGruppo(TipoMotivoNelGruppo motivoNelGruppo) {
		this.motivoNelGruppo = motivoNelGruppo;
	}

	public boolean isVivo() {
		return vivo;
	}

	public void setVivo(boolean vivo) {
		this.vivo = vivo;
	}

	public int getLivello() {
		return livello;
	}

	public void setLivello(int livello) {
		this.livello = livello;
	}

	public int getEsperienza() {
		return esperienza;
	}

	public void setEsperienza(int esperienza) {
		this.esperienza = esperienza;
	}

	public int getPuntiAbilitaDisponibili() {
		return puntiAbilitaDisponibili;
	}

	public void setPuntiAbilitaDisponibili(int puntiAbilitaDisponibili) {
		this.puntiAbilitaDisponibili = puntiAbilitaDisponibili;
	}

	/**
	 * Imposta il valore minimo di un attributo; con null lo toglie.
	 */
	public void setMinimo(TipoAttributo tipo, Double valore) {
		if (valore == null) {
			valoriMinimi.remove(tipo);
		} else {
			this.valoriMinimi.put(tipo, valore);
		}
	}

	/**
	 * Il valore minimo dell'attributo, se ne ha uno.
	 */
	public Optional<Double> getMinimo(TipoAttributo tipo) {
		if (!valoriMinimi.containsKey(tipo)) {
			return Optional.empty();
		}
		return Optional.of(valoriMinimi.get(tipo));
	}

	/**
	 * Imposta il valore massimo di un attributo; con null lo toglie.
	 */
	public void setMassimo(TipoAttributo tipo, Double valore) {
		if (valore == null) {
			valoriMassimi.remove(tipo);
		} else {
			this.valoriMassimi.put(tipo, valore);
		}
	}

	/**
	 * Il valore massimo dell'attributo, se ne ha uno.
	 */
	public Optional<Double> getMassimo(TipoAttributo tipo) {
		if (!valoriMassimi.containsKey(tipo)) {
			return Optional.empty();
		}
		return Optional.of(valoriMassimi.get(tipo));
	}

	/**
	 * Imposta un determinato attributo con un certo valore, limitandolo però ai valori minimo e massimo,
	 * quando presenti.
	 */
	public void set(TipoAttributo tipo, double valore) {
		Optional<Double> valoreMinimoOpt = getMinimo(tipo);
		if (valoreMinimoOpt.isPresent()) {
			double valoreMinimo = valoreMinimoOpt.get();
			if (valore < valoreMinimo) {
				valore = valoreMinimo;
			}
		}
		Optional<Double> valoreMassimoOpt = getMassimo(tipo);
		if (valoreMassimoOpt.isPresent()) {
			double valoreMassimo = valoreMassimoOpt.get();
			if (valore > valoreMassimo) {
				valore = valoreMassimo;
			}
		}
		valoriAttributi.put(tipo, valore);
	}

	/**
	 * Il valore di un attributo.
	 *
	 * @throws IllegalStateException se il personaggio non ha quell'attributo
	 */
	public double get(TipoAttributo tipo) {
		if (!valoriAttributi.containsKey(tipo)) {
			throw new IllegalStateException("TipoAttributo " + tipo + " non presente");
		}
		return valoriAttributi.get(tipo);
	}

	/**
	 * Come {@link #get(TipoAttributo)}, ma vuoto invece di lanciare un'eccezione:
	 * serve per l'attributo del tempo, che non tutti hanno.
	 */
	public Optional<Double> getOptional(TipoAttributo tipo) {
		return Optional.ofNullable(valoriAttributi.get(tipo));
	}

	/**
	 * Il carico massimo che il personaggio può portare, 0 se non è limitato da un massimo.
	 */
	public int getCaricoMassimo() {
		return getMassimo(TipoAttributo.CARICO_MASSIMO).orElse(0.0d).intValue();
	}

	public void setSalute(double salute) {
		set(TipoAttributo.SALUTE, salute);
	}

	public void setForza(double forza) {
		set(TipoAttributo.FORZA, forza);
	}

	public void setDestrezza(double destrezza) {
		set(TipoAttributo.DESTREZZA, destrezza);
	}

	public void setCostituzione(double costituzione) {
		set(TipoAttributo.COSTITUZIONE, costituzione);
	}

	public void setIntelligenza(double intelligenza) {
		set(TipoAttributo.INTELLIGENZA, intelligenza);
	}

	public void setSaggezza(double saggezza) {
		set(TipoAttributo.SAGGEZZA, saggezza);
	}

	public void setCarisma(double carisma) {
		set(TipoAttributo.CARISMA, carisma);
	}

	public void setFortuna(double fortuna) {
		set(TipoAttributo.FORTUNA, fortuna);
	}

	public void setMagia(double magia) {
		set(TipoAttributo.MAGIA, magia);
	}

	public void setStanchezza(double stanchezza) {
		set(TipoAttributo.STANCHEZZA, stanchezza);
	}

	public String getCausaTrapasso() {
		return causaTrapasso;
	}

	public void setCausaTrapasso(String causaTrapasso) {
		this.causaTrapasso = Serializzabile.pulisciCampo(causaTrapasso);
	}

	/**
	 * Gli effetti di stato in corso, di sola lettura (ogni effetto, però, si può modificare); per aggiungerne o togliere
	 * si usano {@link #aggiungiEffettoDiStato} e {@link #togliEffettoDiStato}.
	 */
	public Collection<EffettoDiStatoMD> getEffettiDiStato() {
		return Collections.unmodifiableList(effettiDiStato);
	}

	public void aggiungiEffettoDiStato(EffettoDiStatoMD effettoDiStato) {
		effettiDiStato.add(effettoDiStato);
	}

	/**
	 * @return true se l'effetto c'era
	 */
	public boolean togliEffettoDiStato(EffettoDiStatoMD effettoDiStato) {
		return effettiDiStato.remove(effettoDiStato);
	}

	/**
	 * Gli artefatti che porta, di sola lettura; per aggiungerne o togliere si usano {@link #aggiungiArtefatto} e
	 * {@link #togliArtefatto}.
	 */
	public Collection<ArtefattoMD> getArtefatti() {
		return Collections.unmodifiableList(artefatti);
	}

	public void aggiungiArtefatto(ArtefattoMD artefatto) {
		artefatti.add(artefatto);
	}

	/**
	 * @return true se l'artefatto c'era
	 */
	public boolean togliArtefatto(ArtefattoMD artefatto) {
		return artefatti.remove(artefatto);
	}

	/**
	 * La tattica del personaggio (tattiche_di_combattimento_automatiche.md §11), o null se non ne ha una (oggi i mostri); si salva con lui
	 */
	public TatticaMD getTattica() {
		return tattica;
	}

	public void setTattica(TatticaMD tattica) {
		this.tattica = tattica;
	}

	/**
	 * Rimette gli artefatti come erano: lo stesso contenuto e lo stesso ordine. Serve a chi ne ha tolti qualcuno per una
	 * prova (vedi {@code RegoleEquipaggiamento.valutaCambioArma}) e deve lasciare tutto com'era, anche l'ordine in cui
	 * l'inventario li mostra.
	 */
	public void ripristinaArtefatti(List<ArtefattoMD> prima) {
		List<ArtefattoMD> copia = new ArrayList<>(prima);
		artefatti.clear();
		artefatti.addAll(copia);
	}

	/**
	 * I modificatori agli attributi, di sola lettura; per aggiungerne o togliere si usano {@link #aggiungiModificatore} e
	 * {@link #togliModificatore}.
	 */
	public Collection<ModificatoreAttributoMD> getModificatori() {
		return Collections.unmodifiableList(modificatori);
	}

	public void aggiungiModificatore(ModificatoreAttributoMD modificatore) {
		modificatori.add(modificatore);
	}

	/**
	 * @return true se il modificatore c'era
	 */
	public boolean togliModificatore(ModificatoreAttributoMD modificatore) {
		return modificatori.remove(modificatore);
	}

	/**
	 * Scrive una riga con i dati del personaggio (se è vivo e, se è morto, la causa; il numero di artefatti), poi cinque righe con i valori minimi,
	 * i valori massimi, gli attributi, i modificatori e gli effetti di stato, poi la tattica (una riga vuota se non ne ha)
	 * e infine gli artefatti.
	 */
	@Override
	public void salva(PrintWriter stream) throws IOException {
		// La causa della morte c'e' solo per chi e' morto
		stampaRiga(stream,
				uuid,
				classe,
				facoltativo(nome),
				vivo,
				vivo ? "" : facoltativo(causaTrapasso),
				livello,
				esperienza,
				puntiAbilitaDisponibili,
				artefatti.size(),
				png,
				motivoNelGruppo);

		// Un campo composto per valore (vedi LettoreCampi.sottocampi)
		stampaRiga(stream, valoriMinimi.entrySet().stream().map(e -> composto(e.getKey(), e.getValue())).collect(Collectors.toList()));
		stampaRiga(stream, valoriMassimi.entrySet().stream().map(e -> composto(e.getKey(), e.getValue())).collect(Collectors.toList()));
		stampaRiga(stream, valoriAttributi.entrySet().stream().map(e -> composto(e.getKey(), e.getValue())).collect(Collectors.toList()));
		stampaRiga(stream, modificatori.stream().map(ModificatoreAttributoMD::comeCampo).collect(Collectors.toList()));
		stampaRiga(stream, effettiDiStato.stream().map(EffettoDiStatoMD::comeCampo).collect(Collectors.toList()));

		// La tattica: una riga vuota se non ce n'è una, altrimenti la tattica con le sue voci
		if (tattica == null) {
			stream.println();
		} else {
			tattica.salva(stream);
		}

		for (ArtefattoMD artefatto : artefatti) {
			artefatto.salva(stream);
		}
	}

	/**
	 * Rilegge ciò che scrive {@link #salva(PrintWriter)}, sostituendo i dati attuali del personaggio.
	 */
	@Override
	public void leggi(BufferedReader stream) throws IOException{
		LettoreCampi campi = new LettoreCampi(stream.readLine());

		uuid = campi.testo();
		classe = campi.enumerato(TipoPersonaggio.class);
		nome = campi.testoFacoltativo();
		vivo = campi.booleano();
		String causa = campi.testoFacoltativo();
		causaTrapasso = vivo ? null : causa;
		livello = campi.intero();
		esperienza = campi.intero();
		puntiAbilitaDisponibili = campi.intero();
		int numeroArtefatti = campi.intero();
		png = campi.booleano();
		motivoNelGruppo = campi.enumerato(TipoMotivoNelGruppo.class);

		impostaValori(valoriMinimi, stream.readLine());
		impostaValori(valoriMassimi, stream.readLine());
		impostaValori(valoriAttributi, stream.readLine());

		campi = new LettoreCampi(stream.readLine());
		modificatori.clear();
		while (campi.haAltriCampi()) {
			modificatori.add(ModificatoreAttributoMD.daCampo(campi));
		}
		campi = new LettoreCampi(stream.readLine());
		effettiDiStato.clear();
		while (campi.haAltriCampi()) {
			effettiDiStato.add(EffettoDiStatoMD.daCampo(campi));
		}

		String intestazioneTattica = stream.readLine();
		tattica = intestazioneTattica.isEmpty() ? null : TatticaMD.leggi(intestazioneTattica, stream);

		artefatti.clear();
		for (int i = 0; i < numeroArtefatti; i++) {
			ArtefattoMD artefatto = new ArtefattoMD();
			artefatto.leggi(stream);
			artefatti.add(artefatto);
		}
	}

	private void impostaValori(Map<TipoAttributo, Double> mappa, String linea) throws IOException {
		LettoreCampi campi = new LettoreCampi(linea);
		mappa.clear();
		while (campi.haAltriCampi()) {
			LettoreCampi sottocampi = campi.sottocampi(2, 2);
			mappa.put(sottocampi.enumerato(TipoAttributo.class), sottocampi.decimale());
		}
	}
}
