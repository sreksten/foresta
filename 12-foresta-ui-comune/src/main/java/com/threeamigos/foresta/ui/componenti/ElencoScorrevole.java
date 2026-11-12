package com.threeamigos.foresta.ui.componenti;

import com.threeamigos.foresta.ui.Finestra;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.Punto;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;
import com.threeamigos.foresta.ui.testo.FontTool;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Il modello di un elenco ad albero scorrevole, senza disegno: i nodi, le loro misure, quanto si può scorrere, che nodo sta a una certa quota (per sapere dove
 * si è cliccato), dove sta un nodo e quali cose vanno disegnate nella parte visibile ({@link #elementiVisibili}). Ogni nodo può portarsi dietro un
 * riferimento all'oggetto che rappresenta, per poter risalire dalla posizione di un click a quell'oggetto, e un'icona messa a sinistra.
 * <p>
 * Le immagini (icone, valori già composti) le dà chi crea il nodo, come oggetti suoi ({@link ImmagineDelNodo}) con la loro misura: il modello non sa che cosa
 * siano. I testi li dà come testo, font e colore ({@link TestoDelNodo}) e il modello li fa andare a capo e ne calcola l'altezza. Lo usano Swing (con
 * <code>ComponenteScorrevole</code>, che li disegna in un'immagine) e libGDX.
 *
 * @param <T> il tipo dell'oggetto rappresentato da ciascun nodo
 */
public class ElencoScorrevole<T> {

	/**
	 * Un testo con il suo font e il suo colore
	 */
	public static final class TestoDelNodo {
		private final String testo;
		private final DoomdarkFont font;
		private final DoomdarkColorModel.Color colore;

		public TestoDelNodo(String testo, DoomdarkFont font, DoomdarkColorModel.Color colore) {
			this.testo = testo;
			this.font = font;
			this.colore = colore;
		}
	}

	/**
	 * Un'immagine di chi disegna (un'icona, un valore già composto) con la sua misura
	 */
	public static final class ImmagineDelNodo {
		private final Object immagine;
		private final Misura misura;

		public ImmagineDelNodo(Object immagine, Misura misura) {
			this.immagine = immagine;
			this.misura = misura;
		}

		public Object getImmagine() {
			return immagine;
		}

		public Misura getMisura() {
			return misura;
		}
	}

	private final int larghezza;
	private final int larghezzaIndentazione;
	private final int interlinea;
	private final List<Nodo> nodi = new ArrayList<>();

	public ElencoScorrevole(int larghezza, int larghezzaIndentazione, int interlinea) {
		this.larghezza = larghezza;
		this.larghezzaIndentazione = larghezzaIndentazione;
		this.interlinea = interlinea;
	}

	public int getLarghezza() {
		return larghezza;
	}

	public int getInterlinea() {
		return interlinea;
	}

	/**
	 * Aggiunge un nodo: tutto è facoltativo (null), tranne che un nodo senza niente è un nodo vuoto
	 *
	 * @param valoreTesto    il valore scritto (o null)
	 * @param valoreImmagine il valore già composto, se è fatto di più parti di colori diversi (o null: ne basta uno dei due)
	 */
	public Nodo creaNodo(TestoDelNodo chiave, TestoDelNodo valoreTesto, ImmagineDelNodo valoreImmagine, TestoDelNodo descrizione, ImmagineDelNodo icona,
						 T riferimento) {
		Nodo nodo = new Nodo(chiave, valoreTesto, valoreImmagine, descrizione, icona, 0, riferimento);
		nodi.add(nodo);
		return nodo;
	}

	/**
	 * Aggiunge un separatore orizzontale (un nodo che è solo un'immagine), centrato: se è più largo dell'elenco se ne vede la parte centrale, dove sta il
	 * suo motivo (chi disegna ritaglia l'elenco alla sua larghezza)
	 */
	public void creaSeparatore(ImmagineDelNodo separatore) {
		creaNodo(null, null, null, null, separatore, null).separatore = true;
	}

	private int calcolaAltezzaMassimaNodi() {
		int altezza = 0;
		for (Nodo nodo : nodi) {
			altezza += nodo.getAltezzaCompleta();
		}
		return altezza;
	}

	/**
	 * Di quanti pixel si può scorrere al massimo la lista in uno spazio alto altezzaMassima: 0 se ci sta tutta.
	 */
	public int getMassimoScorrimento(int altezzaMassima) {
		return Math.max(0, calcolaAltezzaMassimaNodi() - altezzaMassima);
	}

	/**
	 * Se la lista è più alta dello spazio in cui la si disegna, e quindi si scorre (con le frecce su e giù che lo segnalano)
	 */
	public boolean isScorrevole(int altezzaMassima) {
		return getMassimoScorrimento(altezzaMassima) > 0;
	}

	/**
	 * Riporta l'offset di scorrimento entro i limiti della lista: non si scorre sopra la prima riga né oltre l'ultima.
	 */
	public int limitaOffset(int altezzaMassima, int offset) {
		return Math.max(0, Math.min(offset, getMassimoScorrimento(altezzaMassima)));
	}

	/**
	 * L'altezza dell'immagine che contiene tutti i nodi, o quella dello spazio se è più alta
	 */
	public int altezzaDellElenco(int altezzaMassima) {
		return Math.max(calcolaAltezzaMassimaNodi(), altezzaMassima);
	}

	/**
	 * Se la freccia su si vede: la lista è scorsa
	 */
	public boolean isFrecciaSuVisibile(int offset) {
		return offset > 0;
	}

	/**
	 * Se la freccia giù si vede: sotto lo spazio c'è ancora lista
	 */
	public boolean isFrecciaGiuVisibile(int altezzaMassima, int offset) {
		return calcolaAltezzaMassimaNodi() - offset > altezzaMassima;
	}

	private List<Nodo> espandiNodi() {
		List<Nodo> listaRisultante = new ArrayList<>();
		for (Nodo nodo : nodi) {
			addNodoAListaRisultante(nodo, listaRisultante);
		}
		return listaRisultante;
	}

	private void addNodoAListaRisultante(Nodo nodo, List<Nodo> listaRisultante) {
		listaRisultante.add(nodo);
		if (nodo.isFigliVisibili()) {
			for (Nodo figlio : nodo.figli) {
				addNodoAListaRisultante(figlio, listaRisultante);
			}
		}
	}

	/**
	 * Quel che va disegnato della parte dell'elenco che sta nello spazio alto <code>altezzaMassima</code> scorso di <code>offset</code>: solo i nodi che
	 * intersecano lo spazio, ognuno con la sua icona, il titolo, il valore e, se i suoi figli sono visibili, la descrizione. Le coordinate sono quelle
	 * dell'elenco intero (vedi {@link ElementoDelElenco}).
	 */
	public List<ElementoDelElenco> elementiVisibili(int altezzaMassima, int offset) {
		altezzaMassima = Math.max(1, altezzaMassima);
		offset = limitaOffset(altezzaMassima, offset);
		List<ElementoDelElenco> elementi = new ArrayList<>();
		int altezzaRaggiunta = 0;
		for (Nodo nodo : espandiNodi()) {
			// Ogni riga viene disegnata alla propria posizione assoluta: si evita il disegno di quelle che non intersecano lo spazio, non lo spazio che
			// occupano.
			int altezzaNodo = nodo.getAltezzaSenzaFigli();
			if (altezzaRaggiunta + altezzaNodo > offset && altezzaRaggiunta < offset + altezzaMassima) {
				int x = nodo.separatore ? (larghezza - nodo.icona.misura.larghezza) / 2 : nodo.getIndentazione();
				if (nodo.icona != null) {
					elementi.add(new ElementoDelElenco(ElementoDelElenco.Tipo.ICONA, nodo.icona.immagine, nodo.icona.misura.larghezza, null, null, null,
							null, 0, 0, x, altezzaRaggiunta));
					x += nodo.icona.misura.larghezza + Finestra.SPACING;
				}
				if (nodo.chiave != null) {
					elementi.add(testo(nodo.chiave, nodo.larghezzaChiave, x, altezzaRaggiunta));
				}
				if (nodo.valoreImmagine != null) {
					elementi.add(new ElementoDelElenco(ElementoDelElenco.Tipo.VALORE_IMMAGINE, nodo.valoreImmagine.immagine, nodo.valoreImmagine.misura.larghezza,
							null, null, null, null, 0, 0, larghezza - nodo.valoreImmagine.misura.larghezza, altezzaRaggiunta));
				} else if (nodo.valoreTesto != null) {
					int larghezzaValore = FontTool.getWidth(nodo.valoreTesto.font, nodo.valoreTesto.testo);
					elementi.add(new ElementoDelElenco(ElementoDelElenco.Tipo.VALORE_TESTO, null, Math.max(1, larghezzaValore), nodo.valoreTesto.testo,
							java.util.Collections.singletonList(nodo.valoreTesto.testo), nodo.valoreTesto.font, nodo.valoreTesto.colore, 0,
							nodo.valoreTesto.font.getHeight(), larghezza - Math.max(1, larghezzaValore), altezzaRaggiunta));
				}
				if (nodo.isFigliVisibili() && nodo.descrizione != null) {
					int altezzaDescrizione = altezzaRaggiunta + (nodo.chiave != null ? nodo.altezzaChiave : 0) + interlinea;
					elementi.add(testo(nodo.descrizione, nodo.larghezzaDescrizione, x + larghezzaIndentazione, altezzaDescrizione));
				}
			}
			altezzaRaggiunta += altezzaNodo;
		}
		return elementi;
	}

	private ElementoDelElenco testo(TestoDelNodo testo, int larghezzaDiImpaginazione, int x, int y) {
		List<String> righe = FontTool.split(testo.font, testo.testo, larghezzaDiImpaginazione);
		return new ElementoDelElenco(ElementoDelElenco.Tipo.TESTO, null, larghezzaDiImpaginazione, testo.testo, righe, testo.font, testo.colore,
				larghezzaDiImpaginazione, testo.font.getHeight() + interlinea, x, y);
	}

	/**
	 * Riporta il riferimento del nodo che occupa la quota indicata (con la sua descrizione e i suoi spazi, non solo il titolo), oppure null se la quota è sopra
	 * la lista o oltre l'ultimo nodo.
	 *
	 * @param quota espressa in coordinate della lista, quindi comprensiva dell'offset di scorrimento con cui la lista è stata prodotta
	 */
	public T riferimentoTitoloAllaQuota(int quota) {
		if (quota < 0) {
			return null;
		}
		int altezzaRaggiunta = 0;
		for (Nodo nodo : espandiNodi()) {
			int altezzaNodo = nodo.getAltezzaSenzaFigli();
			if (quota < altezzaRaggiunta + altezzaNodo) {
				return nodo.riferimento;
			}
			altezzaRaggiunta += altezzaNodo;
		}
		return null;
	}

	/**
	 * Riporta la posizione (indentazione come x, quota assoluta nelle coordinate della lista come y, prima di sottrarre l'offset di scorrimento) a cui viene
	 * disegnata l'icona o il titolo del primo nodo il cui riferimento soddisfa il predicato indicato, oppure null se nessun nodo corrisponde.
	 */
	public Punto posizioneTitolo(Predicate<T> predicato) {
		int altezzaRaggiunta = 0;
		for (Nodo nodo : espandiNodi()) {
			if (nodo.riferimento != null && predicato.test(nodo.riferimento)) {
				return new Punto(nodo.getIndentazione(), altezzaRaggiunta);
			}
			altezzaRaggiunta += nodo.getAltezzaSenzaFigli();
		}
		return null;
	}

	/**
	 * Un nodo dell'elenco: titolo, valore, descrizione, icona e riferimento, più i nodi figli che si possono aprire e chiudere.
	 */
	public class Nodo {
		private final ImmagineDelNodo icona;
		private final TestoDelNodo chiave;
		private final TestoDelNodo valoreTesto;
		// Un separatore: solo un'immagine, centrata nell'elenco
		private boolean separatore;
		private final ImmagineDelNodo valoreImmagine;
		private final TestoDelNodo descrizione;
		private final int larghezzaChiave;
		private final int larghezzaDescrizione;
		private final int altezzaChiave;
		private final int altezzaDescrizione;
		private final int altezzaValore;
		private final int indentazione;
		private final T riferimento;
		private final List<Nodo> figli = new ArrayList<>();
		private boolean figliVisibili = true;
		private final String datiOriginali;

		private Nodo(TestoDelNodo chiave, TestoDelNodo valoreTesto, ImmagineDelNodo valoreImmagine, TestoDelNodo descrizione, ImmagineDelNodo icona,
					 int indentazione, T riferimento) {
			this.riferimento = riferimento;
			this.icona = icona;
			this.indentazione = indentazione;
			this.chiave = chiave;
			this.valoreTesto = valoreTesto;
			this.valoreImmagine = valoreImmagine;
			this.descrizione = descrizione;

			int larghezzaMassimaChiave = larghezza - indentazione;
			if (icona != null) {
				larghezzaMassimaChiave = larghezzaMassimaChiave - icona.misura.larghezza - Finestra.SPACING;
			}
			// La descrizione è indentata un livello in più
			int larghezzaMassimaDescrizione = larghezzaMassimaChiave - larghezzaIndentazione;

			// Se c'è anche un valore la larghezza massima per la chiave si riduce, ma per la descrizione rimane la stessa
			int larghezzaDelValore = 0;
			int altezzaDelValore = 0;
			if (valoreImmagine != null) {
				larghezzaDelValore = valoreImmagine.misura.larghezza;
				altezzaDelValore = valoreImmagine.misura.altezza;
			} else if (valoreTesto != null) {
				larghezzaDelValore = Math.max(1, FontTool.getWidth(valoreTesto.font, valoreTesto.testo));
				altezzaDelValore = valoreTesto.font.getHeight();
			}
			if (valoreImmagine != null || valoreTesto != null) {
				larghezzaMassimaChiave = larghezzaMassimaChiave - larghezzaDelValore - Finestra.SPACING;
			}
			this.altezzaValore = altezzaDelValore;
			// Un valore molto largo non deve lasciare alla chiave una larghezza negativa
			this.larghezzaChiave = chiave != null ? Math.max(larghezzaMassimaChiave, chiave.font.getHeight()) : 0;
			this.larghezzaDescrizione = descrizione != null ? Math.max(larghezzaMassimaDescrizione, descrizione.font.getHeight()) : 0;
			this.altezzaChiave = chiave != null ? (chiave.font.getHeight() + interlinea) * FontTool.split(chiave.font, chiave.testo, larghezzaChiave).size() : 0;
			this.altezzaDescrizione = descrizione != null
					? (descrizione.font.getHeight() + interlinea) * FontTool.split(descrizione.font, descrizione.testo, larghezzaDescrizione).size() : 0;
			this.datiOriginali = "Chiave: " + (chiave != null ? chiave.testo : null) + " Valore: " + (valoreTesto != null ? valoreTesto.testo : valoreImmagine)
					+ " Descrizione: " + (descrizione != null ? descrizione.testo : null);
		}

		/**
		 * Aggiunge a questo nodo un figlio
		 */
		public Nodo creaNodo(TestoDelNodo chiave, TestoDelNodo valoreTesto, ImmagineDelNodo valoreImmagine, TestoDelNodo descrizione, ImmagineDelNodo icona,
							 T riferimento) {
			Nodo nodo = new Nodo(chiave, valoreTesto, valoreImmagine, descrizione, icona, this.indentazione + larghezzaIndentazione, riferimento);
			figli.add(nodo);
			return nodo;
		}

		public int getAltezzaCompleta() {
			return getAltezza(true);
		}

		public int getAltezzaSenzaFigli() {
			return getAltezza(false);
		}

		private int getAltezza(boolean inclusiNodiFigli) {
			int altezza = 0;
			if (chiave != null) {
				altezza = altezzaChiave;
			}
			if (altezzaValore > 0) {
				altezza = Math.max(altezza, altezzaValore);
			}
			if (figliVisibili) {
				if (descrizione != null) {
					altezza += interlinea + altezzaDescrizione;
				}
				if (inclusiNodiFigli) {
					for (Nodo figlio : figli) {
						altezza += figlio.getAltezzaCompleta();
					}
				}
			}
			if (icona != null) {
				altezza = Math.max(altezza, icona.misura.altezza);
			}
			altezza += interlinea;
			return altezza;
		}

		public int getIndentazione() {
			return indentazione;
		}

		public void setFigliVisibili(boolean figliVisibili) {
			this.figliVisibili = figliVisibili;
		}

		public boolean isFigliVisibili() {
			return figliVisibili;
		}

		@Override
		public String toString() {
			return datiOriginali;
		}
	}
}
