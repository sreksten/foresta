package com.threeamigos.foresta.strumenti;

import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.interfacce.VistaGruppoGiocatore;
import com.threeamigos.foresta.testi.Numeri;

/**
 * Alcuni pezzi della storia (introduzione e finali) e le funzioni che indicano la direzione e la distanza
 * tra due punti all'interno della foresta. Gli articoli e i numeri in lettere stanno in {@code Articoli} e
 * {@code Numeri}, nel modulo {@code foresta-testi}.
 */
public class Misc {

	private static final String NORD = "nord";
	private static final String EST = "est";
	private static final String SUD = "sud";
	private static final String OVEST = "ovest";

	public static final String[] STORIA = {
			"molto, molto tempo fa, quando il mondo era da poco stato creato,",
			"un gruppo di eroi venne riunito per liberarlo dalla minaccia dei draghi.",
			"questi, invece di comportarsi da bravi ragazzi, presto cominciarono a degenerare",
			"andando per locande a sbevazzare e a cercare compagne di sbornie e avventure",
			"finche' un giorno il piu' scoppiato di tutti sbaglio' strada e si ritrovo' in un postaccio conosciuto come"
	};

	public static final String[] PERSO = {
			"con la morte del cacciatore, il drago ha finalmente via libera... la foresta e' perduta!",
	};

	public static final String[] VINTO = {
			"congratulazioni!",
			"la foresta e' stata liberata",
			"i tuoi amici sono venuti per offrirti doni come loro salvatore",
			"possa la pace oggi conquistata durare a lungo!",
			""
	};

	/**
	 * Riporta la direzione di un punto della foresta rispetto ad un gruppo
	 * (per le informazioni su città, castelli, artefatti, o per gli eventi)
	 */
	public static String getDirezione(VistaGruppoGiocatore g, CoordinateMD coordinate) {
		CoordinateMD da = g.getCoordinate();
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

	private Misc() {}

}
