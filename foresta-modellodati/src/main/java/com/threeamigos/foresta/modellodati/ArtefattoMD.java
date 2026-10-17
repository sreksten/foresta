package com.threeamigos.foresta.modellodati;

import com.threeamigos.foresta.tipi.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * I dati di un artefatto: un oggetto del gioco con un tipo (un'arma, un'armatura, una pergamena, un ingrediente...), una
 * rarità, un nome con l'articolo (la spada di fuoco) ed eventualmente un nome proprio (Diavolina) e una descrizione,
 * il livello, i danni base, il costo, il peso e lo slot che occupa mentre un personaggio lo porta.
 * <p>
 * Può portare dei {@link ModificatoreAttributoMD modificatori di attributo} e degli {@link IncantamentoMD incantamenti}.
 * Ogni artefatto ha un identificativo unico che il salvataggio conserva. Gli artefatti leggendari sono pezzi di un
 * set, riconosciuto da due chiavi (vedi {@link #setPezzoDiSetLeggendario}).
 * <p>
 * Contiene solo dati, con le poche regole di coerenza che servono a non scrivere un salvataggio rotto (per esempio
 * il carattere "|" si toglie dai nomi). Le regole del gioco stanno nel motore (vedi artefatti_e_incantamenti.md).
 *
 * @author Stefano Reksten
 */
public class ArtefattoMD implements Serializzabile {

	/**
	 * Il nome di un artefatto o di un incantamento a cui è stato dato un nome vuoto
	 */
	public static final String NESSUN_NOME = Serializzabile.NESSUN_NOME;

	/**
	 * Un identificativo unico per l'artefatto
	 */
	private String uuid = UUID.randomUUID().toString();

	private TipoArtefatto tipo;
	private TipoRaritaArtefatto rarita = TipoRaritaArtefatto.COMUNE;
	// Per un leggendario di un set (vedi leggendari.txt): la chiave del set e quella del pezzo
	private String setLeggendario;
	private String pezzoLeggendario;
	// Facoltativo (null se manca), es. "Diavolina"
	private String nomeProprio;
	// Con l'articolo, es. "la spada di fuoco"
	private String nome;
	// Es. "che brucia i nemici"
	private String descrizione;
	// Lo slot occupato davvero mentre un personaggio porta l'artefatto; null se non è equipaggiato
	private TipoSlotArtefatto slotEquipaggiamento;
	private int livello;
	private int danni;
	protected int costoAcquisto;
	private double peso;
	// Stato per la UI: se false, l'elenco modificatori/incantamenti resta chiuso.
	private boolean figliVisibili = true;
	private final List<ModificatoreAttributoMD> modificatori = new ArrayList<>();
	private final List<IncantamentoMD> incantamenti = new ArrayList<>();

	/**
	 * L'identificativo unico dell'artefatto.
	 */
	public String getUuid() {
		return uuid;
	}

	public TipoArtefatto getTipo() {
		return tipo;
	}

	public void setTipo(TipoArtefatto tipo) {
		this.tipo = tipo;
	}

	public TipoRaritaArtefatto getRarita() {
		return rarita;
	}

	public void setRarita(TipoRaritaArtefatto rarita) {
		this.rarita = rarita;
	}

	/**
	 * La chiave del set leggendario di cui l'artefatto è un pezzo, o null.
	 */
	public String getSetLeggendario() {
		return setLeggendario;
	}

	/**
	 * Il pezzo del set leggendario che è l'artefatto (la chiave del leggendario), o null.
	 */
	public String getPezzoLeggendario() {
		return pezzoLeggendario;
	}

	/**
	 * Fa dell'artefatto quel pezzo di quel set leggendario.
	 */
	public void setPezzoDiSetLeggendario(String setLeggendario, String pezzoLeggendario) {
		this.setLeggendario = setLeggendario;
		this.pezzoLeggendario = pezzoLeggendario;
	}

	public String getNomeProprio() {
		return nomeProprio;
	}

	/**
	 * Imposta il nome proprio così com'è: va bene per i nomi costruiti dal codice.
	 * Per quelli scelti dal giocatore si passa prima da {@link #normalizzaNomeProprio(String)}.
	 * Un nome vuoto o di soli spazi vale come nessun nome; il carattere "|" si toglie.
	 */
	public void setNomeProprio(String nomeProprio) {
		String pulito = Serializzabile.pulisciCampo(nomeProprio);
		this.nomeProprio = pulito == null || pulito.trim().isEmpty() ? null : pulito;
	}

	/**
	 * Mette le iniziali maiuscole a un nome proprio scelto dal giocatore
	 * (es. "lama del drago" → "Lama Del Drago"). Restituisce null se il nome è vuoto.
	 */
	public static String normalizzaNomeProprio(String nomeProprio) {
		String pulito = Serializzabile.pulisciCampo(nomeProprio);
		if (pulito == null || pulito.trim().isEmpty()) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		for (String parola : pulito.trim().split("\\s+")) {
			if (sb.length() > 0) {
				sb.append(' ');
			}
			sb.append(parola.substring(0, 1).toUpperCase(Locale.ROOT)).append(parola.substring(1));
		}
		return sb.toString();
	}

	public String getNome() {
		return nome;
	}

	/**
	 * Forma completa da usare nei testi: "Diavolina, la spada di fuoco, che brucia i nemici",
	 * oppure "la spada di fuoco, che brucia i nemici" se non c'è il nome proprio.
	 */
	public String getNomeCompleto() {
		return nomeProprio == null ? getNomeEDescrizione() : nomeProprio + ", " + getNomeEDescrizione();
	}

	/**
	 * Nome con cui l'artefatto compare in grande nell'inventario: il nome proprio, oppure il nome.
	 */
	public String getNomeBreve() {
		return nomeProprio == null ? nome : nomeProprio;
	}

	/**
	 * Testo del nodo secondario nell'inventario: con il nome proprio, "la spada di fuoco, che brucia i nemici",
	 * così il nome generico non si perde; senza, la sola descrizione.
	 */
	public String getDescrizioneBreve() {
		return nomeProprio == null ? descrizione : getNomeEDescrizione();
	}

	private String getNomeEDescrizione() {
		return descrizione == null || descrizione.isEmpty() ? nome : nome + ", " + descrizione;
	}

	/**
	 * Il "|" si toglie; un nome vuoto diventa {@link #NESSUN_NOME}.
	 */
	public void setNome(String nome) {
		String pulito = Serializzabile.pulisciCampo(nome);
		this.nome = pulito == null || pulito.trim().isEmpty() ? NESSUN_NOME : pulito;
	}

	public String getDescrizione() {
		return descrizione;
	}

	public void setDescrizione(String descrizione) {
		this.descrizione = Serializzabile.pulisciCampo(descrizione);
	}

	public TipoSlotArtefatto getSlotEquipaggiamento() {
		return slotEquipaggiamento;
	}

	public void setSlotEquipaggiamento(TipoSlotArtefatto slotEquipaggiamento) {
		this.slotEquipaggiamento = slotEquipaggiamento;
	}

	public int getLivello() {
		return livello;
	}

	public void setLivello(int livello) {
		this.livello = livello;
	}

	public int getDanni() {
		return danni;
	}

	public void setDanni(int danniBase) {
		this.danni = danniBase;
	}

	public int getCostoAcquisto() {
		return costoAcquisto;
	}

	public void setCostoAcquisto(int costoAcquisto) {
		this.costoAcquisto = costoAcquisto;
	}

	public double getPeso() {
		return peso;
	}

	public void setPeso(double peso) {
		this.peso = peso;
	}

	/**
	 * I modificatori, di sola lettura: si cambiano con {@link #addModificatore} e {@link #removeModificatore}.
	 */
	public Collection<ModificatoreAttributoMD> getModificatori() {
		return Collections.unmodifiableList(modificatori);
	}

	/**
	 * Aggiunge un modificatore.
	 *
	 * @throws IllegalArgumentException se il modificatore agisce sul bersaglio ma l'artefatto non è né un'arma né un
	 * ingrediente: solo un'arma può mettere un modificatore sul bersaglio che colpisce, e un ingrediente (la pergamena) lo
	 * porta per passarlo a un'arma
	 */
	public void addModificatore(ModificatoreAttributoMD modificatore) {
		// Lo mette sul bersaglio un'arma che colpisce; un ingrediente (la pergamena) lo porta per passarlo a un'arma
		if (modificatore.getBersaglio() == BersaglioModificatore.BERSAGLIO && tipo.getCategoria() != CategoriaArtefatto.ARMA
				&& !tipo.isIngrediente()) {
			throw new IllegalArgumentException("Un modificatore sul bersaglio sta solo su un'arma o su un ingrediente, non su " + tipo);
		}
		modificatori.add(modificatore);
	}

	/**
	 * Aggiunge un modificatore permanente, che agisce su chi porta l'artefatto.
	 */
	public void addModificatore(TipoAttributo tipoAttributo, TipoModificatore tipoModificatore,
								double quantita, String nota) {
		modificatori.add(new ModificatoreAttributoMD(tipoAttributo, tipoModificatore, quantita, nota));
	}

	/**
	 * Toglie un modificatore (uno solo, se ce ne sono di uguali): serve alla fusione, quando un effetto migliore ne
	 * prende il posto.
	 *
	 * @return se c'era
	 */
	public boolean removeModificatore(ModificatoreAttributoMD modificatore) {
		return modificatori.remove(modificatore);
	}

	/**
	 * Gli incantamenti, di sola lettura: si cambiano con {@link #addIncantamento}, {@link #removeIncantamento} e
	 * {@link #rimuoviTuttiGliIncantamenti}.
	 */
	public Collection<IncantamentoMD> getIncantamenti() {
		return Collections.unmodifiableList(incantamenti);
	}

	/**
	 * Toglie tutti gli incantamenti: serve a chi vuole un artefatto "di base", senza gli incantamenti che il generatore dà ogni
	 * tanto (la dotazione iniziale di un personaggio).
	 */
	public void rimuoviTuttiGliIncantamenti() {
		incantamenti.clear();
	}

	/**
	 * Toglie un incantamento: serve alla fusione, quando un effetto migliore ne prende il posto.
	 *
	 * @return se c'era
	 */
	public boolean removeIncantamento(IncantamentoMD incantamento) {
		return incantamenti.remove(incantamento);
	}

	/**
	 * Aggiunge un incantamento.
	 */
	public void addIncantamento(IncantamentoMD incantamento) {
		incantamenti.add(incantamento);
	}

	/**
	 * Aggiunge un incantamento, creandolo dai suoi dati.
	 */
	public void addIncantamento(String nomeIncantamento, TipoDanno tipoDanno, int dannoBonusFisso, double coefficienteScala) {
		incantamenti.add(new IncantamentoMD(nomeIncantamento, tipoDanno, dannoBonusFisso, coefficienteScala));
	}

	/**
	 * Se l'elenco dei modificatori e degli incantamenti è aperto nell'inventario; è solo uno stato della schermata.
	 */
	public boolean isFigliVisibili() {
		return figliVisibili;
	}

	public void setFigliVisibili(boolean figliVisibili) {
		this.figliVisibili = figliVisibili;
	}

	/**
	 * Scrive una riga con i dati dell'artefatto e il numero dei modificatori e degli incantamenti, poi una riga per
	 * ogni modificatore e una per ogni incantamento.
	 */
	@Override
	public void salva(PrintWriter stream) throws IOException {
		stream.print(uuid);
		stream.print(PIPE);
		stream.print(tipo.name());
		stream.print(PIPE);
		stream.print(rarita.name());
		stream.print(PIPE);
		stream.print(Serializzabile.facoltativo(setLeggendario));
		stream.print(PIPE);
		stream.print(Serializzabile.facoltativo(pezzoLeggendario));
		stream.print(PIPE);
		stream.print(Serializzabile.facoltativo(nomeProprio));
		stream.print(PIPE);
		stream.print(nome);
		stream.print(PIPE);
		stream.print(Serializzabile.facoltativo(descrizione));
		stream.print(PIPE);
		stream.print(livello);
		stream.print(PIPE);
		stream.print(danni);
		stream.print(PIPE);
		stream.print(costoAcquisto);
		stream.print(PIPE);
		stream.print(peso);
		stream.print(PIPE);
		stream.print(figliVisibili);
		stream.print(PIPE);
		stream.print(Serializzabile.facoltativo(slotEquipaggiamento));
		stream.print(PIPE);
		stream.print(modificatori.size());
		stream.print(PIPE);
		stream.println(incantamenti.size());

		for (ModificatoreAttributoMD modificatore : modificatori) {
			stream.print(modificatore.getTipoAttributo().name());
			stream.print(PIPE);
			stream.print(modificatore.getTipoModificatoreAttributo().name());
			stream.print(PIPE);
			stream.print(modificatore.getQuantita());
			stream.print(PIPE);
			stream.print(modificatore.getUnitaDurata().name());
			stream.print(PIPE);
			stream.print(modificatore.getDurata());
			stream.print(PIPE);
			stream.print(modificatore.getBersaglio().name());
			stream.print(PIPE);
			stream.println(Serializzabile.facoltativo(modificatore.getNote()));
		}

		for (IncantamentoMD incantamento : incantamenti) {
			stream.print(incantamento.getNomeIncantamento());
			stream.print(PIPE);
			stream.print(incantamento.getTipoDannoElementale().name());
			stream.print(PIPE);
			stream.print(incantamento.getDannoBonusFisso());
			stream.print(PIPE);
			stream.println(incantamento.getCoefficienteScala());
		}
	}

	/**
	 * Rilegge ciò che scrive {@link #salva(PrintWriter)}, sostituendo i dati, i modificatori e gli incantamenti attuali.
	 */
	@Override
	public void leggi(BufferedReader stream) throws IOException {
		LettoreCampi campi = new LettoreCampi(stream.readLine());
		uuid = campi.testo();
		tipo = campi.enumerato(TipoArtefatto.class);
		rarita = campi.enumerato(TipoRaritaArtefatto.class);
		setLeggendario = campi.testoFacoltativo();
		pezzoLeggendario = campi.testoFacoltativo();
		nomeProprio = campi.testoFacoltativo();
		nome = campi.testo();
		descrizione = campi.testo();
		livello = campi.intero();
		danni = campi.intero();
		costoAcquisto = campi.intero();
		peso = campi.decimale();
		figliVisibili = campi.booleano();
		slotEquipaggiamento = campi.enumeratoFacoltativo(TipoSlotArtefatto.class);
		int numeroModificatori = campi.intero();
		int numeroIncantamenti = campi.intero();
		modificatori.clear();
		for (int i = 0; i < numeroModificatori; i++) {
			campi = new LettoreCampi(stream.readLine());
			TipoAttributo tipoAttributo = campi.enumerato(TipoAttributo.class);
			TipoModificatore tipoModificatore = campi.enumerato(TipoModificatore.class);
			double quantita = campi.decimale();
			UnitaDurataModificatore unitaDurata = campi.enumerato(UnitaDurataModificatore.class);
			int durata = campi.intero();
			BersaglioModificatore bersaglio = campi.enumerato(BersaglioModificatore.class);
			// Nota vuota: si rilegge come "", come la mette il costruttore a tre argomenti di
			// ModificatoreAttributoMD, altrimenti il modificatore riletto non sarebbe più equals all'originale
			String note = campi.testo();
			modificatori.add(new ModificatoreAttributoMD(tipoAttributo, tipoModificatore, quantita, note, unitaDurata, durata,
					bersaglio));
		}
		incantamenti.clear();
		for (int i = 0; i < numeroIncantamenti; i++) {
			campi = new LettoreCampi(stream.readLine());
			String nomeIncantamento = campi.testo();
			TipoDanno tipoDannoElementale = campi.enumerato(TipoDanno.class);
			int dannoBonusFisso = campi.intero();
			double coefficienteScala = campi.decimale();
			incantamenti.add(new IncantamentoMD(nomeIncantamento, tipoDannoElementale, dannoBonusFisso, coefficienteScala));
		}
	}
}
