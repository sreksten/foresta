package com.threeamigos.foresta.libgdx;

/**
 * Dove mettere i glifi di un font nella pagina di texture che li raccoglie: file di glifi uno accanto all'altro, a capo quando non ce
 * n'è più posto, con un pixel di margine attorno a ciascuno perché un filtro non faccia colare un glifo sul vicino. Non usa libGDX:
 * si prova senza scheda grafica.
 */
final class DisposizioneDeiGlifi {

	/**
	 * Dove sta un glifo nella pagina
	 */
	static final class Posto {
		final int x;
		final int y;

		Posto(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}

	private final int larghezza;
	private final int altezza;
	private final Posto[] posti;

	private DisposizioneDeiGlifi(int larghezza, int altezza, Posto[] posti) {
		this.larghezza = larghezza;
		this.altezza = altezza;
		this.posti = posti;
	}

	/**
	 * @param larghezze        la larghezza di ogni glifo
	 * @param altezzaDelGlifo  l'altezza, uguale per tutti
	 * @param larghezzaMassima la larghezza massima della pagina
	 */
	static DisposizioneDeiGlifi di(int[] larghezze, int altezzaDelGlifo, int larghezzaMassima) {
		Posto[] posti = new Posto[larghezze.length];
		int x = 1;
		int y = 1;
		int larghezzaUsata = 1;
		for (int i = 0; i < larghezze.length; i++) {
			if (larghezze[i] + 2 > larghezzaMassima) {
				throw new IllegalArgumentException("Un glifo largo " + larghezze[i] + " non sta in una pagina larga " + larghezzaMassima);
			}
			if (x + larghezze[i] + 1 > larghezzaMassima) {
				x = 1;
				y += altezzaDelGlifo + 1;
			}
			posti[i] = new Posto(x, y);
			x += larghezze[i] + 1;
			larghezzaUsata = Math.max(larghezzaUsata, x);
		}
		return new DisposizioneDeiGlifi(larghezzaUsata, y + altezzaDelGlifo + 1, posti);
	}

	int getLarghezza() {
		return larghezza;
	}

	int getAltezza() {
		return altezza;
	}

	Posto getPosto(int indice) {
		return posti[indice];
	}
}
