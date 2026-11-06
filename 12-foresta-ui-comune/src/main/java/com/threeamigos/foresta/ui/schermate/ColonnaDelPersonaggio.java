package com.threeamigos.foresta.ui.schermate;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoSpesaPuntoAbilita;
import com.threeamigos.foresta.interfacce.VistaPersonaggio;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.ui.Finestra;
import com.threeamigos.foresta.ui.componenti.ContenutoDiColonna;
import com.threeamigos.foresta.ui.componenti.ElencoScorrevole;
import com.threeamigos.foresta.ui.testo.TestiDellaUI;

import java.util.ArrayList;
import java.util.List;

/**
 * La colonna centrale dell'inventario, senza disegno: la testata del personaggio, l'elenco delle sue caratteristiche ({@link ElencoDegliAttributi}) e quel che
 * il giocatore ci può fare: l'attributo sotto il mouse (bianco), l'aiuto a cartigli, il click che apre o chiude una voce e il doppio click che spende un punto
 * abilità. Sta su una schermata di scambio, da cui prende misure, mouse e scorrimento della zona centrale; chi disegna (Swing, libGDX) mostra gli elementi e l'elenco
 * che gli dà.
 */
public final class ColonnaDelPersonaggio {

	private final SchermataDiScambio schermata;
	private final ElencoDegliAttributi attributi = new ElencoDegliAttributi();
	private VistaPersonaggio personaggio;
	// Dove comincia l'elenco degli attributi, sotto la testata
	private int yAttributi = 0;

	public ColonnaDelPersonaggio(SchermataDiScambio schermata) {
		this.schermata = schermata;
	}

	public void impostaPersonaggio(VistaPersonaggio personaggio) {
		this.personaggio = personaggio;
		schermata.scorrimentoCentrale.reimposta();
	}

	public VistaPersonaggio getPersonaggio() {
		return personaggio;
	}

	/**
	 * La testata del personaggio; la sua altezza è dove comincia l'elenco degli attributi ({@link #getYAttributi()}).
	 */
	public ContenutoDiColonna testata() {
		ContenutoDiColonna testata = schermata.testataDelPersonaggio(personaggio);
		yAttributi = testata.getAltezza();
		return testata;
	}

	public int getYAttributi() {
		return yAttributi;
	}

	public int getXAttributi() {
		return schermata.larghezzaCornice + 2 * Finestra.SPACING;
	}

	public int getLarghezzaAttributi() {
		return schermata.width - 2 * schermata.larghezzaCornice - 4 * Finestra.SPACING;
	}

	/**
	 * L'albero degli attributi, con in bianco l'attributo primario sotto il mouse: va ricostruito a ogni disegno e a ogni click.
	 */
	public ElencoScorrevole<Object> elenco() {
		Object sotto = riferimentoA(schermata.mouseX, schermata.mouseY);
		return attributi.costruisci(personaggio, sotto instanceof TipoAttributo ? (TipoAttributo) sotto : null, getLarghezzaAttributi());
	}

	/**
	 * Riporta l'attributo il cui titolo occupa la posizione (x, y) in coordinate della finestra (oppure
	 * {@link ElencoDegliAttributi#MODIFICATORI_TEMPORANEI}, se è il titolo di quell'elenco), oppure null se il punto non cade sull'elenco o non su un titolo.
	 */
	public Object riferimentoA(int x, int y) {
		int xInterno = x - getXAttributi();
		if (xInterno < 0 || xInterno >= getLarghezzaAttributi() || y < yAttributi || y >= schermata.height) {
			return null;
		}
		int yInterno = y - yAttributi + schermata.offsetYZonaCentrale();
		return attributi.costruisci(personaggio, null, getLarghezzaAttributi()).riferimentoTitoloAllaQuota(yInterno);
	}

	/**
	 * Sopra un attributo, i cartigli che spiegano il colore: di quanto lo cambiano i modificatori temporanei (se ce ne sono) e, se è primario e ci sono punti
	 * abilità da spendere, il doppio click che lo aumenta.
	 */
	public List<String> righeAiuto() {
		List<String> righe = new ArrayList<>();
		Object riferimento = riferimentoA(schermata.mouseX, schermata.mouseY);
		if (!(riferimento instanceof TipoAttributo)) {
			return righe;
		}
		TipoAttributo attributo = (TipoAttributo) riferimento;
		long variazione = Math.round(personaggio.getVariazioneDaTemporanei(attributo));
		if (variazione != 0) {
			righe.add(TestiDellaUI.get("TEMPORANEAMENTE", (variazione > 0 ? "+" : "") + variazione));
		}
		if (attributo.isPrimario() && personaggio.getPuntiAbilitaDisponibili() > 0) {
			righe.add(TestiDellaUI.get("AIUTO_DOPPIO_CLICK_AUMENTA_ABILITA"));
		}
		return righe;
	}

	/**
	 * Un click sul titolo di una voce la apre o la chiude; dice se l'ha consumato.
	 */
	public boolean processaClick(int x, int y) {
		return attributi.invertiVisibilita(riferimentoA(x, y));
	}

	/**
	 * Un doppio click su un attributo spende un punto abilità; dice se l'ha consumato.
	 */
	public boolean processaDoppioClick(int x, int y) {
		Object riferimento = riferimentoA(x, y);
		if (riferimento instanceof TipoAttributo) {
			BusEventi.pubblica(new ComandoSpesaPuntoAbilita(personaggio.getUuid(), (TipoAttributo) riferimento));
			return true;
		}
		return false;
	}
}
