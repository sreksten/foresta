package com.threeamigos.foresta.ui.tattiche;

import com.threeamigos.foresta.ui.componenti.ComponenteScorrevole;
import com.threeamigos.foresta.ui.schermate.MenuADiscesa;
import com.threeamigos.foresta.ui.schermate.MisureDelleImmaginiAwt;

import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * Il menu a discesa della schermata delle tattiche in Swing: il riquadro e le scelte nel font Doomdark in un {@link ComponenteScorrevole}. Dove si apre,
 * quali scelte ha, lo scorrimento, i click e il resto sono di {@link MenuADiscesa}, nel modulo comune; qui c'è il disegno. Chi lo usa (la schermata) le passa i
 * click prima di tutto il resto e lo disegna per ultimo.
 *
 * @param <T> il tipo del valore di ogni scelta
 */
final class CasellaADiscesa<T> {

	private static final int INTERLINEA = 1;
	private static final Color SFONDO = new Color(MenuADiscesa.SFONDO);
	private static final Color BORDO = new Color(MenuADiscesa.BORDO);

	private final MenuADiscesa<T> menu;

	CasellaADiscesa(List<MenuADiscesa.Scelta<T>> scelte, T corrente, int ancoraX, int ancoraSotto, int ancoraSopra, int larghezzaSchermo, int altezzaSchermo,
					Consumer<T> alloScegliere) {
		this(new MenuADiscesa<>(scelte, corrente, ancoraX, ancoraSotto, ancoraSopra, larghezzaSchermo, altezzaSchermo, new MisureDelleImmaginiAwt(),
				alloScegliere));
	}

	CasellaADiscesa(MenuADiscesa<T> menu) {
		this.menu = menu;
	}

	boolean isAperta() {
		return menu.isAperto();
	}

	void chiudi() {
		menu.chiudi();
	}

	MenuADiscesa.Scelta<T> sceltaSotto(int px, int py) {
		return menu.sceltaSotto(px, py);
	}

	/**
	 * Disegna il menu aperto, con la scelta sotto il mouse evidenziata.
	 */
	public void disegna(Graphics2D graphics, int mouseX, int mouseY) {
		graphics.setColor(SFONDO);
		graphics.fillRect(menu.getX(), menu.getY(), menu.getLarghezza(), menu.getAltezza());
		graphics.setColor(BORDO);
		graphics.drawRect(menu.getX(), menu.getY(), menu.getLarghezza() - 1, menu.getAltezza() - 1);
		ComponenteScorrevole<T> componente = new ComponenteScorrevole<>(menu.elenco(menu.sceltaAlla(mouseX, mouseY)), Math.max(1, menu.getLarghezza() - 2 * MenuADiscesa.MARGINE),
				0, INTERLINEA, oggetto -> (Image) oggetto);
		menu.getScorrimento().aggiorna();
		Image immagine = componente.produci(menu.getAltezzaInterna(), menu.getScorrimento());
		graphics.drawImage(immagine, menu.getX() + MenuADiscesa.MARGINE, menu.getY() + MenuADiscesa.MARGINE, null);
	}
}
