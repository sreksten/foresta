package com.threeamigos.foresta.motore.modellodati;

import com.threeamigos.foresta.oggetti.Incantamento;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;

public class ArtefattoMD implements Serializzabile {

	/**
	 * Il nome di un artefatto o di un incantamento a cui è stato dato un nome vuoto
	 */
	public static final String NESSUN_NOME = Serializzabile.NESSUN_NOME;

	private TipoArtefatto tipo;
	private RaritaArtefatto rarita = RaritaArtefatto.COMUNE;
	// Facoltativo (null se manca), es. "Diavolina"
	private String nomeProprio;
	// Con l'articolo, es. "la spada di fuoco"
	private String nome;
	// Es. "che brucia i nemici"
	private String descrizione;
	// Lo slot occupato davvero mentre un personaggio porta l'artefatto; null se non è equipaggiato
	private SlotArtefatto slotEquipaggiamento;
	private int livello;
	private int danni;
	protected int costoAcquisto;
	private double peso;
	// Stato per la UI: se false, l'elenco modificatori/incantamenti resta chiuso.
	private boolean figliVisibili = true;
	private final Collection<ModificatoreAttributo> modificatori = new ArrayList<>();
	private final Collection<Incantamento> incantamenti = new ArrayList<>();

	public TipoArtefatto getTipo() {
		return tipo;
	}

	public void setTipo(TipoArtefatto tipo) {
		this.tipo = tipo;
	}

	public RaritaArtefatto getRarita() {
		return rarita;
	}

	public void setRarita(RaritaArtefatto rarita) {
		this.rarita = rarita;
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
		String pulito = Serializzabile.senzaPipe(nomeProprio);
		this.nomeProprio = pulito == null || pulito.trim().isEmpty() ? null : pulito;
	}

	/**
	 * Mette le iniziali maiuscole a un nome proprio scelto dal giocatore
	 * (es. "lama del drago" → "Lama Del Drago"). Restituisce null se il nome è vuoto.
	 */
	public static String normalizzaNomeProprio(String nomeProprio) {
		String pulito = Serializzabile.senzaPipe(nomeProprio);
		if (pulito == null || pulito.trim().isEmpty()) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		for (String parola : pulito.trim().split("\\s+")) {
			if (sb.length() > 0) {
				sb.append(' ');
			}
			sb.append(parola.substring(0, 1).toUpperCase()).append(parola.substring(1));
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
		String pulito = Serializzabile.senzaPipe(nome);
		this.nome = pulito == null || pulito.trim().isEmpty() ? NESSUN_NOME : pulito;
	}

	public String getDescrizione() {
		return descrizione;
	}

	public void setDescrizione(String descrizione) {
		this.descrizione = Serializzabile.senzaPipe(descrizione);
	}

	public SlotArtefatto getSlotEquipaggiamento() {
		return slotEquipaggiamento;
	}

	public void setSlotEquipaggiamento(SlotArtefatto slotEquipaggiamento) {
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

	public Collection<ModificatoreAttributo> getModificatori() {
		return modificatori;
	}

	public void addModificatore(ModificatoreAttributo modificatore) {
		modificatori.add(modificatore);
	}

	public void addModificatore(TipoAttributo tipoAttributo, TipoModificatore tipoModificatore,
								double quantita, String nota) {
		modificatori.add(new ModificatoreAttributo(tipoAttributo, tipoModificatore, quantita, nota));
	}

	public Collection<Incantamento> getIncantamenti() {
		return incantamenti;
	}

	public void addIncantamento(Incantamento incantamento) {
		incantamenti.add(incantamento);
	}

	public void addIncantamento(String nomeIncantamento, TipoDanno tipoDanno, int dannoBonusFisso, double coefficienteScala) {
		incantamenti.add(new Incantamento(nomeIncantamento, tipoDanno, dannoBonusFisso, coefficienteScala));
	}

	public boolean isFigliVisibili() {
		return figliVisibili;
	}

	public void setFigliVisibili(boolean figliVisibili) {
		this.figliVisibili = figliVisibili;
	}

	@Override
	public void salva(PrintWriter stream) throws IOException {
		stream.print(tipo.name());
		stream.print(PIPE);
		stream.print(rarita.name());
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

		for (ModificatoreAttributo modificatore : modificatori) {
			stream.print(modificatore.getTipoAttributo().name());
			stream.print(PIPE);
			stream.print(modificatore.getTipoModificatoreAttributo().name());
			stream.print(PIPE);
			stream.print(modificatore.getQuantita());
			stream.print(PIPE);
			stream.println(Serializzabile.facoltativo(modificatore.getNote()));
		}

		for (Incantamento incantamento : incantamenti) {
			stream.print(incantamento.getNomeIncantamento());
			stream.print(PIPE);
			stream.print(incantamento.getTipoDannoElementale().name());
			stream.print(PIPE);
			stream.print(incantamento.getDannoBonusFisso());
			stream.print(PIPE);
			stream.println(incantamento.getCoefficienteScala());
		}
	}

	@Override
	public void leggi(BufferedReader stream) throws IOException {
		LettoreCampi campi = new LettoreCampi(stream.readLine());
		tipo = campi.enumerato(TipoArtefatto.class);
		rarita = campi.enumerato(RaritaArtefatto.class);
		nomeProprio = campi.testoFacoltativo();
		nome = campi.testo();
		descrizione = campi.testo();
		livello = campi.intero();
		danni = campi.intero();
		costoAcquisto = campi.intero();
		peso = campi.decimale();
		figliVisibili = campi.booleano();
		slotEquipaggiamento = campi.enumeratoFacoltativo(SlotArtefatto.class);
		int numeroModificatori = campi.intero();
		int numeroIncantamenti = campi.intero();
		modificatori.clear();
		for (int i = 0; i < numeroModificatori; i++) {
			campi = new LettoreCampi(stream.readLine());
			TipoAttributo tipoAttributo = campi.enumerato(TipoAttributo.class);
			TipoModificatore tipoModificatore = campi.enumerato(TipoModificatore.class);
			double quantita = campi.decimale();
			// Nota vuota: si rilegge come "", come la mette il costruttore a tre argomenti di
			// ModificatoreAttributo, altrimenti il modificatore riletto non sarebbe più equals all'originale
			String note = campi.testo();
			modificatori.add(new ModificatoreAttributo(tipoAttributo, tipoModificatore, quantita, note));
		}
		incantamenti.clear();
		for (int i = 0; i < numeroIncantamenti; i++) {
			campi = new LettoreCampi(stream.readLine());
			String nomeIncantamento = campi.testo();
			TipoDanno tipoDannoElementale = campi.enumerato(TipoDanno.class);
			int dannoBonusFisso = campi.intero();
			double coefficienteScala = campi.decimale();
			incantamenti.add(new Incantamento(nomeIncantamento, tipoDannoElementale, dannoBonusFisso, coefficienteScala));
		}
	}
}
