package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.intermezzi.MomentoIntermezzo;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.function.Supplier;

/**
 * Una missione fatta di {@link Passo}, come un piccolo automa (vedi gestione_missioni.md, §3): i passi hanno un
 * id stringa e ognuno decide quale viene dopo, così una missione può diramarsi secondo lo stato di gioco o una
 * scelta del giocatore, e una missione generata può costruire i suoi passi al volo.
 * <p>
 * Si salva solo l'id del passo corrente ({@code PASSO_CORRENTE}), più i parametri che le sottoclassi scrivono come
 * proprietà ordinarie: i passi si ricostruiscono ogni volta da {@link #costruisciPasso(String)}, che deve quindi
 * saper ricostruire ogni id che la missione abbia mai usato, anche dopo un salvataggio e un caricamento.
 * <p>
 * A ogni controllo dell'automa ({@link #controllaPreLocazione()} e seguenti), se la missione non è finita e il
 * passo corrente va valutato in quel controllo ed è concluso, se ne esegue l'azione e si passa al successivo; se
 * anche quello è dello stesso controllo ed è già concluso si prosegue subito, così un passo di solo testo non
 * costa un turno. {@link Passo#FINE} chiude la missione, completandola se l'azione non l'ha già fatto.
 * <p>
 * Un passo con un intermezzo, una volta concluso, lascia il suo id tra quelli con un intermezzo in attesa
 * ({@link #getPassiConIntermezzoInAttesa()}): chi lo mostra lo segna con {@link #segnaIntermezzoPassoMostrato}.
 * <p>
 * Le sottoclassi che hanno bisogno di altra logica (per esempio far fallire la missione se una città è stata
 * distrutta) sovrascrivono il controllo che serve e chiamano il metodo della superclasse alla fine.
 */
public abstract class MissioneAPassi extends MissioneBase {

	private static final String PASSO_CORRENTE = "PASSO_CORRENTE";
	private static final String SEQUENZA_PASSI = "SEQUENZA_PASSI";
	private static final String INTERMEZZI_IN_ATTESA = "INTERMEZZI_IN_ATTESA";
	private static final String INTERMEZZO_MOSTRATO = "INTERMEZZO_MOSTRATO_";
	/**
	 * Separatore delle liste di id salvate come una proprietà: '§' e '|' sono già riservati dal salvataggio
	 */
	private static final String SEPARATORE = ",";
	/**
	 * Quanti passi si possono concludere di fila nello stesso controllo: di più è quasi certamente un ciclo
	 */
	private static final int PASSI_DI_FILA_MASSIMI = 50;

	protected MissioneAPassi(ClasseMissione classe) {
		super(classe);
	}

	/**
	 * L'id del primo passo.
	 */
	protected abstract String passoIniziale();

	/**
	 * Il passo con quell'id, ricostruito: si chiama ogni volta che serve, non si tiene da parte.
	 */
	protected abstract Passo costruisciPasso(String id);

	@Override
	public void controllaPreLocazione() {
		avanzaSePronto(MomentoControllo.PRE_LOCAZIONE);
	}

	@Override
	public void controllaInLocazione() {
		avanzaSePronto(MomentoControllo.IN_LOCAZIONE);
	}

	@Override
	public void controllaPostLocazione() {
		avanzaSePronto(MomentoControllo.POST_LOCAZIONE);
	}

	/**
	 * L'id del passo corrente: il passo iniziale finché la missione non è avanzata, {@link Passo#FINE} quando è
	 * finita per l'ultimo passo.
	 */
	public final String getPassoCorrente() {
		String passo = ottieniProprieta(PASSO_CORRENTE);
		return passo != null ? passo : passoIniziale();
	}

	private void avanzaSePronto(MomentoControllo momento) {
		for (int passiDiFila = 0; !isCompleta() && !isFallita(); passiDiFila++) {
			String id = getPassoCorrente();
			if (Passo.FINE.equals(id)) {
				return;
			}
			Passo passo = costruisciPasso(id);
			if (passo.getMomento() != momento || !passo.isConcluso()) {
				return;
			}
			if (passiDiFila >= PASSI_DI_FILA_MASSIMI) {
				throw new IllegalStateException("La missione " + getId() + " ha concluso " + PASSI_DI_FILA_MASSIMI
						+ " passi di fila nello stesso controllo: c'è un ciclo fra i passi? Ultimo passo: " + id);
			}
			passo.eseguiAzione();
			if (passo.haIntermezzo()) {
				aggiungiIntermezzoInAttesa(id);
			}
			// L'azione può aver fatto fallire la missione: allora non si avanza più
			if (isFallita()) {
				return;
			}
			String prossimo = passo.getProssimoPasso();
			impostaPassoCorrente(prossimo);
			if (Passo.FINE.equals(prossimo) && !isCompleta()) {
				completaMissione();
			}
		}
	}

	private void impostaPassoCorrente(String id) {
		aggiungiProprieta(PASSO_CORRENTE, validaId(id));
	}

	// --- Sequenze di passi decise alla generazione

	/**
	 * Fissa una sequenza di passi decisa alla generazione della missione (per esempio "visita N locazioni scelte a
	 * caso"), da percorrere con {@link #prossimoNellaSequenza()}. Si decide una volta sola e resta stabile per tutta
	 * la missione, anche attraverso salvataggio e caricamento. Gli id devono essere tutti diversi.
	 */
	protected final void impostaSequenzaPassi(List<String> idPassi) {
		if (idPassi.isEmpty()) {
			throw new IllegalArgumentException("La sequenza di passi è vuota");
		}
		if (new HashSet<>(idPassi).size() != idPassi.size()) {
			throw new IllegalArgumentException("Un passo compare più volte nella sequenza: " + idPassi);
		}
		idPassi.forEach(MissioneAPassi::validaId);
		aggiungiProprieta(SEQUENZA_PASSI, String.join(SEPARATORE, idPassi));
	}

	protected final List<String> leggiSequenzaPassi() {
		return leggiLista(SEQUENZA_PASSI);
	}

	/**
	 * Per un passo di una sequenza fissata con {@link #impostaSequenzaPassi}: il passo che lo segue nella sequenza,
	 * o {@link Passo#FINE} se era l'ultimo.
	 */
	protected final Supplier<String> prossimoNellaSequenza() {
		return () -> {
			List<String> sequenza = leggiSequenzaPassi();
			int indice = sequenza.indexOf(getPassoCorrente());
			if (indice < 0) {
				throw new IllegalStateException("Il passo " + getPassoCorrente() + " non fa parte della sequenza " + sequenza);
			}
			return indice + 1 < sequenza.size() ? sequenza.get(indice + 1) : Passo.FINE;
		};
	}

	// --- Intermezzi dei passi

	/**
	 * Gli id dei passi conclusi il cui intermezzo non è ancora stato mostrato, nell'ordine in cui si sono conclusi.
	 */
	public final List<String> getPassiConIntermezzoInAttesa() {
		return Collections.unmodifiableList(leggiLista(INTERMEZZI_IN_ATTESA));
	}

	/**
	 * Il primo passo con un intermezzo in attesa per quel momento, o null.
	 */
	public final String getPassoConIntermezzoInAttesa(MomentoIntermezzo momento) {
		for (String id : leggiLista(INTERMEZZI_IN_ATTESA)) {
			if (costruisciPasso(id).getMomentoIntermezzo() == momento) {
				return id;
			}
		}
		return null;
	}

	public final boolean isIntermezzoPassoMostrato(String idPasso) {
		return ottieniProprieta(INTERMEZZO_MOSTRATO + idPasso) != null;
	}

	public final void segnaIntermezzoPassoMostrato(String idPasso) {
		aggiungiProprieta(INTERMEZZO_MOSTRATO + validaId(idPasso), AFFERMATIVO);
		List<String> inAttesa = leggiLista(INTERMEZZI_IN_ATTESA);
		inAttesa.remove(idPasso);
		scriviLista(INTERMEZZI_IN_ATTESA, inAttesa);
	}

	private void aggiungiIntermezzoInAttesa(String idPasso) {
		if (isIntermezzoPassoMostrato(idPasso)) {
			return;
		}
		List<String> inAttesa = leggiLista(INTERMEZZI_IN_ATTESA);
		if (!inAttesa.contains(idPasso)) {
			inAttesa.add(idPasso);
			scriviLista(INTERMEZZI_IN_ATTESA, inAttesa);
		}
	}

	// --- Liste di id salvate come una proprietà

	private List<String> leggiLista(String chiave) {
		String valore = ottieniProprieta(chiave);
		if (valore == null || valore.isEmpty()) {
			return new ArrayList<>();
		}
		return new ArrayList<>(Arrays.asList(valore.split(SEPARATORE)));
	}

	private void scriviLista(String chiave, List<String> lista) {
		if (lista.isEmpty()) {
			rimuoviProprieta(chiave);
		} else {
			aggiungiProprieta(chiave, String.join(SEPARATORE, lista));
		}
	}

	/**
	 * Un id di passo finisce nelle proprietà salvate e nelle liste separate da virgole: niente caratteri riservati.
	 */
	private static String validaId(String id) {
		if (id == null || id.isEmpty() || id.contains(SEPARATORE) || id.contains("§") || id.contains("|")) {
			throw new IllegalArgumentException("Id di passo non valido: " + id);
		}
		return id;
	}
}
