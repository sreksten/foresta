package com.threeamigos.foresta.ui.riquadri;

import com.threeamigos.foresta.ui.Finestra;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.Punto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Dove stanno i personaggi nel riquadro della locazione, senza disegno: lo spazio si divide in quattro, il gruppo si dispone attorno alla terza colonna e
 * gli avversari attorno alla prima, con il personaggio centrale (o lo spazio fra i due centrali) sulla colonna; una fila che non ci sta occupa tutta la
 * sua metà, con i centri a passo uguale; chi è più vicino al centro sta più in alto. In più, dove far partire gli sprite a tempo perché non si
 * sovrappongano. Lo usano Swing e libGDX.
 */
public final class DisposizioneNellaLocazione {

	/**
	 * Lo spazio fra un personaggio e il successivo nella sua fila, quando c'è posto: se non c'è si riduce, fino a farli
	 * sovrapporre un poco
	 */
	public static final int DISTANZA_FRA_PERSONAGGI = 20;
	/**
	 * Di quanto ogni personaggio è più in alto del successivo nel suo gruppo: più si va verso il bordo, più si scende
	 */
	private static final int PASSO_VERTICALE = 6;
	/**
	 * Due sprite a tempo (salute, magia, livello) più vicini di così in orizzontale si sovrappongono, e se lo sono
	 * anche in verticale il secondo parte più in basso (vedi primoPostoLibero)
	 */
	private static final int DISTANZA_ORIZZONTALE_SPRITE_A_TEMPO = 110;
	private static final int TENTATIVI_SPRITE_A_TEMPO = 30;

	private DisposizioneNellaLocazione() {
	}

	/**
	 * Dove sta il bordo sinistro di ogni personaggio di una fila, rispetto alla colonna attorno a cui la fila si
	 * dispone, con quello spazio fra uno e l'altro: se sono dispari il personaggio centrale è centrato sulla colonna, se
	 * sono pari la colonna cade a metà dello spazio fra i due centrali, e gli altri seguono ai due lati.
	 */
	public static double[] bordiSinistri(List<Misura> immagini, double spazio) {
		int numero = immagini.size();
		double[] bordi = new double[numero];
		if (numero == 0) {
			return bordi;
		}
		int centrale = numero / 2;
		if (numero % 2 == 1) {
			bordi[centrale] = -immagini.get(centrale).larghezza / 2.0d;
		} else {
			bordi[centrale] = spazio / 2;
		}
		for (int i = centrale + 1; i < numero; i++) {
			bordi[i] = bordi[i - 1] + immagini.get(i - 1).larghezza + spazio;
		}
		for (int i = centrale - 1; i >= 0; i--) {
			bordi[i] = bordi[i + 1] - spazio - immagini.get(i).larghezza;
		}
		return bordi;
	}

	/**
	 * Dove sta il bordo sinistro di ogni personaggio di una fila, nell'ordine dal centro verso il bordo, con quella
	 * direzione (1 verso destra, -1 verso sinistra).
	 * <p>
	 * Lo spazio della fila è la sua metà: comincia a metà riquadro più (o meno) metà cofano ed è largo metà riquadro meno
	 * metà cofano. Se le immagini, a DISTANZA_FRA_PERSONAGGI l'una dall'altra, ci stanno, si dispongono attorno alla
	 * colonna (vedi {@link #bordiSinistri}); così anche un personaggio solo, per quanto largo. Se non ci stanno, si
	 * dispongono per il loro centro: tolte a quello spazio metà della larghezza del primo e metà di quella dell'ultimo,
	 * quel che resta diviso il numero degli intervalli (i personaggi meno uno) è il passo da un centro al successivo. Il
	 * primo ha il centro a metà della sua larghezza dall'inizio dello spazio e l'ultimo a metà della sua dalla fine.
	 *
	 * @param immagini le immagini, dal personaggio più vicino al centro a quello più vicino al bordo
	 * @param inizio dove comincia lo spazio della fila, accanto allo spazio libero al centro
	 * @param larghezza quanto è largo lo spazio della fila
	 * @param colonna la colonna attorno a cui si dispone la fila che ci sta
	 */
	public static int[] fila(List<Misura> immagini, double inizio, double larghezza, double colonna, int direzione) {
		int numero = immagini.size();
		int[] posizioni = new int[numero];
		if (numero == 0) {
			return posizioni;
		}
		double larghezzaFila = DISTANZA_FRA_PERSONAGGI * (numero - 1.0d);
		for (Misura immagine : immagini) {
			larghezzaFila += immagine.larghezza;
		}
		if (larghezzaFila <= larghezza || numero == 1) {
			// Ci stanno: attorno alla colonna, calcolati da sinistra a destra
			List<Misura> daSinistra = new ArrayList<>(immagini);
			if (direzione < 0) {
				Collections.reverse(daSinistra);
			}
			double[] bordi = bordiSinistri(daSinistra, DISTANZA_FRA_PERSONAGGI);
			for (int i = 0; i < numero; i++) {
				int daSinistraIndice = direzione < 0 ? numero - 1 - i : i;
				posizioni[i] = (int) Math.round(colonna + bordi[daSinistraIndice]);
			}
			return posizioni;
		}
		// Non ci stanno: per il centro, a passo uguale
		double primo = immagini.get(0).larghezza / 2.0d;
		double ultimo = immagini.get(numero - 1).larghezza / 2.0d;
		double passo = (larghezza - primo - ultimo) / (numero - 1);
		for (int i = 0; i < numero; i++) {
			double centro = inizio + direzione * (primo + i * passo);
			posizioni[i] = (int) Math.round(centro - immagini.get(i).larghezza / 2.0d);
		}
		return posizioni;
	}

	/**
	 * Il fondo di un personaggio di un gruppo di quel numero: il primo, accanto all'oggetto, sta più in alto, ogni altro
	 * scende di un passo, l'ultimo di un solo passo dal fondo della locazione.
	 */
	static int yDelPersonaggio(int altezzaLocazione, int numeroPersonaggi, int indice, Misura immagine) {
		return Finestra.SPACING + altezzaLocazione - (numeroPersonaggi - indice) * PASSO_VERTICALE - immagine.altezza;
	}

	/**
	 * Dove disegnare il gruppo: lo spazio si divide in quattro, e il gruppo si dispone attorno alla terza colonna (a tre
	 * quarti), nell'ordine da sinistra a destra; se non ci sta occupa tutta la sua metà, a passo uguale (vedi
	 * {@link #fila}). Il primo, più vicino al centro, sta più in alto, ogni altro un po' più in basso.
	 *
	 * @param larghezzaDisponibile la larghezza dello spazio, che comincia in topLeftX
	 * @param larghezzaCentro lo spazio libero al centro, fra le due file
	 * @param immagini le immagini dei personaggi, nell'ordine del gruppo
	 * @return la posizione dell'angolo in alto a sinistra di ognuna, nello stesso ordine
	 */
	public static List<Punto> disponiGruppo(int topLeftX, int larghezzaDisponibile, int larghezzaCentro, int altezzaLocazione,
			List<Misura> immagini) {
		int[] x = fila(immagini, topLeftX + larghezzaDisponibile / 2.0d + larghezzaCentro / 2.0d,
				larghezzaDisponibile / 2.0d - larghezzaCentro / 2.0d, topLeftX + larghezzaDisponibile * 3 / 4.0d, 1);
		List<Punto> posizioni = new ArrayList<>();
		for (int i = 0; i < immagini.size(); i++) {
			posizioni.add(new Punto(x[i], yDelPersonaggio(altezzaLocazione, immagini.size(), i, immagini.get(i))));
		}
		return posizioni;
	}

	/**
	 * Come {@link #disponiGruppo}, a specchio attorno alla prima colonna (a un quarto): il primo più vicino al centro,
	 * ogni altro più a sinistra.
	 */
	public static List<Punto> disponiAvversari(int topLeftX, int larghezzaDisponibile, int larghezzaCentro, int altezzaLocazione,
			List<Misura> immagini) {
		int[] x = fila(immagini, topLeftX + larghezzaDisponibile / 2.0d - larghezzaCentro / 2.0d,
				larghezzaDisponibile / 2.0d - larghezzaCentro / 2.0d, topLeftX + larghezzaDisponibile / 4.0d, -1);
		List<Punto> posizioni = new ArrayList<>();
		for (int i = 0; i < immagini.size(); i++) {
			posizioni.add(new Punto(x[i], yDelPersonaggio(altezzaLocazione, immagini.size(), i, immagini.get(i))));
		}
		return posizioni;
	}

	/**
	 * La prima altezza, a partire da y e scendendo di un passo alla volta, dove uno sprite ancorato in x non si
	 * sovrappone a quelli già attivi (posizioni di partenza di quelli che occupano un posto).
	 */
	public static int primoPostoLibero(List<Punto> occupati, int x, int y, int passo) {
		int candidato = y;
		for (int tentativo = 0; tentativo < TENTATIVI_SPRITE_A_TEMPO; tentativo++) {
			boolean libero = true;
			for (Punto occupato : occupati) {
				if (Math.abs(occupato.x - x) < DISTANZA_ORIZZONTALE_SPRITE_A_TEMPO && Math.abs(occupato.y - candidato) < passo) {
					candidato = occupato.y + passo;
					libero = false;
					break;
				}
			}
			if (libero) {
				return candidato;
			}
		}
		return candidato;
	}
}
