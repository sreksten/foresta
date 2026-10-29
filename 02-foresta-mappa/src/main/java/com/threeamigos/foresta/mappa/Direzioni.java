package com.threeamigos.foresta.mappa;

import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.testi.Numeri;

/**
 * La direzione e la distanza fra due punti della foresta, a parole ("a poco più di un giorno di cammino verso nordest."): per
 * le informazioni su città, castelli e artefatti, o per gli eventi.
 *
 * @author Stefano Reksten
 */
public final class Direzioni {

	private static final String NORD = "nord";
	private static final String EST = "est";
	private static final String SUD = "sud";
	private static final String OVEST = "ovest";

	private Direzioni() {
	}

	/**
	 * Riporta la direzione di un punto della foresta rispetto a un altro, di solito la posizione del gruppo
	 * (per le informazioni su città, castelli, artefatti, o per gli eventi)
	 */
	public static String getDirezione(CoordinateMD da, CoordinateMD coordinate) {
		return getDirezione(da.getX(), da.getY(), coordinate.getX(), coordinate.getY());
	}

	public static String getDirezione(int daX, int daY, int aX, int aY) {
		int dx = (daX > aX ? daX - aX : aX - daX);
		int dy = (daY > aY ? daY - aY : aY - daY);
		StringBuilder sb = new StringBuilder();
		int distanza = dx + dy;
		if (distanza == 0) {
			// Nello stesso punto non c'è una direzione: "a zero ore di cammino verso ovest" non vuol dire niente
			return "proprio qui.";
		}
		if (distanza > 36) {
			if (distanza > 50)
				sb.append("molto ");
			sb.append("lontano in direzione ");
		} else {
			int giorni = distanza / 6;
			int resto = distanza % 6;
			sb.append("a ");
			if (giorni == 0) {
				if (resto == 1)
					sb.append("un'ora");
				else
					sb.append(Numeri.getCardinaleM(resto)).append(" ore");
			} else {
				if (resto < 4) {
					if (resto > 0)
						sb.append("poco piu' di ");
					if (giorni == 1)
						sb.append("un giorno");
					else
						sb.append(Numeri.getCardinaleM(giorni)).append(" giorni");
				} else {
					sb.append("quasi ").append(Numeri.getCardinaleM(giorni + 1)).append(" giorni");
				}
			}
			sb.append(" di cammino verso ");
		}
		if (daY < aY) {
			if (dy >= (dx + 1) / 2) {
				sb.append(SUD);
			}
			if (dx >= (dy + 1) / 2) {
				if (aX > daX) {
					sb.append(EST);
				} else {
					sb.append(OVEST);
				}
			}
		} else if (daY > aY) {
			if (dy >= (dx + 1) / 2) {
				sb.append(NORD);
			}
			if (dx >= (dy + 1) / 2) {
				if (aX > daX) {
					sb.append(EST);
				} else {
					sb.append(OVEST);
				}
			}
		} else {
			if (aX > daX)
				sb.append(EST);
			else
				sb.append(OVEST);
		}
		sb.append('.');
		return sb.toString();
	}
}
