package com.threeamigos.foresta.modellodati;

import com.threeamigos.foresta.tipi.TipoIncantesimo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * I dati di un gruppo di personaggi: chi ne fa parte, chi è il capo e le scorte comuni (le pergamene degli incantesimi
 * e le cinque pozioni). Il gruppo del giocatore ne è una specializzazione ({@link GruppoGiocatoreMD}).
 * <p>
 * Il capo non è un personaggio a parte: è il personaggio nella posizione {@link #getIndiceCapo()} dell'elenco.
 *
 * @author Stefano Reksten
 */
public class GruppoMD implements Serializzabile {

	private final List<PersonaggioMD> personaggiMD = new ArrayList<>();
	// Le scorte del gruppo: le pergamene degli incantesimi e le pozioni
	protected final Map<TipoIncantesimo, Integer> incantesimi = new EnumMap<>(TipoIncantesimo.class);
	protected int pozioniSalute;
	protected int pozioniSaluteGrande;
	protected int pozioniMagia;
	protected int pozioniMagiaGrande;
	protected int pozioniAumentoLivello;
	// La posizione del capo fra i personaggi: il capo è il primo che è entrato nel gruppo, e non cambia se lo si sposta
	protected int indiceCapo;

	public int getIndiceCapo() {
		return indiceCapo;
	}

	public void setIndiceCapo(int indiceCapo) {
		this.indiceCapo = indiceCapo;
	}

	/**
	 * Imposta quante pergamene di quell'incantesimo ha il gruppo.
	 */
	public void setIncantesimi(TipoIncantesimo tipoIncantesimo, int quantita) {
		incantesimi.put(tipoIncantesimo, quantita);
	}

	/**
	 * Quante pergamene di quell'incantesimo ha il gruppo; zero se non ne ha.
	 */
	public int getIncantesimi(TipoIncantesimo tipoIncantesimo) {
		return incantesimi.getOrDefault(tipoIncantesimo, 0);
	}

	public int getPozioniSalute() {
		return pozioniSalute;
	}

	public void setPozioniSalute(int pozioniSalute) {
		this.pozioniSalute = pozioniSalute;
	}

	public int getPozioniSaluteGrande() {
		return pozioniSaluteGrande;
	}

	public void setPozioniSaluteGrande(int pozioniSaluteGrande) {
		this.pozioniSaluteGrande = pozioniSaluteGrande;
	}

	public int getPozioniMagia() {
		return pozioniMagia;
	}

	public void setPozioniMagia(int pozioniMagia) {
		this.pozioniMagia = pozioniMagia;
	}

	public int getPozioniMagiaGrande() {
		return pozioniMagiaGrande;
	}

	public void setPozioniMagiaGrande(int pozioniMagiaGrande) {
		this.pozioniMagiaGrande = pozioniMagiaGrande;
	}

	public int getPozioniAumentoLivello() {
		return pozioniAumentoLivello;
	}

	public void setPozioniAumentoLivello(int pozioniAumentoLivello) {
		this.pozioniAumentoLivello = pozioniAumentoLivello;
	}

	/**
	 * Aggiunge un personaggio in fondo all'elenco.
	 */
	public void addPersonaggioMD(PersonaggioMD personaggioMD) {
		personaggiMD.add(personaggioMD);
	}

	/**
	 * L'elenco dei personaggi, di sola lettura: la posizione di un personaggio è quella in cui il gruppo lo mostra. Si
	 * cambia con {@link #addPersonaggioMD}, {@link #rimuoviPersonaggioMD}, {@link #riordinaPersonaggiMD} e
	 * {@link #svuotaPersonaggiMD}.
	 */
	public List<PersonaggioMD> getPersonaggiMD() {
		return Collections.unmodifiableList(personaggiMD);
	}

	/**
	 * Toglie un personaggio dall'elenco.
	 */
	public void rimuoviPersonaggioMD(PersonaggioMD personaggioMD) {
		personaggiMD.remove(personaggioMD);
	}

	/**
	 * Mette i personaggi nell'ordine indicato, che è quello in cui il gruppo li mostra e in cui si salvano. Chi lo chiama
	 * passa gli stessi personaggi che ci sono già, in un altro ordine.
	 */
	public void riordinaPersonaggiMD(List<PersonaggioMD> inOrdine) {
		List<PersonaggioMD> copia = new ArrayList<>(inOrdine);
		personaggiMD.clear();
		personaggiMD.addAll(copia);
	}

	/**
	 * Toglie tutti i personaggi dall'elenco, senza toccare capo, pergamene e pozioni (per quello c'è {@link #reimposta()}).
	 */
	public void svuotaPersonaggiMD() {
		personaggiMD.clear();
	}

	/**
	 * Svuota il gruppo: nessun personaggio, capo in prima posizione, nessuna pergamena e nessuna pozione.
	 */
	public void reimposta() {
		personaggiMD.clear();
		indiceCapo = 0;
		incantesimi.clear();
		pozioniSalute = 0;
		pozioniSaluteGrande = 0;
		pozioniMagia = 0;
		pozioniMagiaGrande = 0;
		pozioniAumentoLivello = 0;
	}

	/**
	 * Scrive una riga con il numero dei personaggi e la posizione del capo, poi i personaggi uno dopo l'altro. Le scorte
	 * non sono qui: le scrive {@link GruppoGiocatoreMD}.
	 */
	@Override
	public void salva(PrintWriter stream) throws IOException {
		stream.println(personaggiMD.size() + PIPE + indiceCapo);
		for (PersonaggioMD personaggio : personaggiMD) {
			personaggio.salva(stream);
		}
	}

	/**
	 * Rilegge i personaggi scritti da {@link #salva(PrintWriter)} e li aggiunge a quelli già presenti.
	 */
	@Override
	public void leggi(BufferedReader stream) throws IOException {
		LettoreCampi campi = new LettoreCampi(stream.readLine());
		int numeroPersonaggi = campi.intero();
		indiceCapo = campi.intero();
		for (int i = 0; i < numeroPersonaggi; i++) {
			PersonaggioMD personaggioMD = new PersonaggioMD();
			personaggioMD.leggi(stream);
			personaggiMD.add(personaggioMD);
		}
	}
}
