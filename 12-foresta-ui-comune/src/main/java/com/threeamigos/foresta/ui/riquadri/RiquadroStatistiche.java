package com.threeamigos.foresta.ui.riquadri;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoCreazioneSpriteATempo;
import com.threeamigos.foresta.eventi.notifiche.NotificaVariazioneDisponibilitaMonete;
import com.threeamigos.foresta.eventi.notifiche.NotificaVariazioneDisponibilitaPreziosi;
import com.threeamigos.foresta.eventi.notifiche.NotificaVariazionePuntiEsperienzaPersonaggio;
import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.ui.Finestra;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.immagini.NomiDelleImmagini;
import com.threeamigos.foresta.ui.sprite.DescrizioneSpriteATempo;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;
import com.threeamigos.foresta.ui.testo.TestiDellaUI;

import java.util.ArrayList;
import java.util.List;

/**
 * Il riquadro con le statistiche del gruppo (monete, preziosi e punti esperienza), senza disegno: tre righe fisse (etichette senza accenti e numeri) nella
 * cornice piccola, con l'etichetta a sinistra e il valore allineato a destra. Chi disegna mette la cornice e le {@link #voci()}; le quote delle tre righe
 * e della colonna dei totali servono agli sprite delle variazioni.
 */
public abstract class RiquadroStatistiche implements Finestra {

	/**
	 * Lo spessore del bordo interno della cornice piccola
	 */
	public static final int BORDO_INTERNO = 6;
	private static final DoomdarkColorModel.Color COLORE = DoomdarkColorModel.Color.LIGHT_GRAY;

	protected final DoomdarkFont fontMedium = DoomdarkFont.medio();
	protected final int topLeftX;
	protected final int topLeftY;
	protected final int moneteY;
	protected final int preziosiY;
	protected final int puntiY;
	protected final int scrittaX;
	protected final int totaleX;
	protected final VistaPartita vistaPartita;

	/**
	 * @param larghezzaCornice la larghezza della cornice piccola, già con lo zoom
	 */
	public RiquadroStatistiche(int topLeftX, int topLeftY, int larghezzaCornice, VistaPartita vistaPartita) {
		this.vistaPartita = vistaPartita;
		this.topLeftX = topLeftX;
		this.topLeftY = topLeftY;
		// Il corpo del testo sta dov'era e il passo è quello del corpo, le righe in più per gli accenti restano libere sopra e sotto
		moneteY = topLeftY + BORDO_INTERNO + 4 - fontMedium.getRigheInCima();
		preziosiY = moneteY + fontMedium.getAltezzaDelCorpo() + 1;
		puntiY = preziosiY + fontMedium.getAltezzaDelCorpo() + 1;
		scrittaX = topLeftX + BORDO_INTERNO + 4;
		totaleX = topLeftX + larghezzaCornice - BORDO_INTERNO - 4;
		BusEventi.iscriviti(NotificaVariazioneDisponibilitaPreziosi.class, this::gestisciEventoVariazionePreziosi);
		BusEventi.iscriviti(NotificaVariazioneDisponibilitaMonete.class, this::gestisciEventoVariazioneMonete);
		BusEventi.iscriviti(NotificaVariazionePuntiEsperienzaPersonaggio.class, this::gestisciEventoVariazionePuntiEsperienza);
	}

	/**
	 * Quanto è grande l'icona di uno sprite (<code>icone/Moneta.png</code>…), come la tiene chi disegna
	 */
	protected abstract Misura misuraDellIcona(String risorsa);

	private void gestisciEventoVariazionePreziosi(NotificaVariazioneDisponibilitaPreziosi evento) {
		pubblica(spriteDellaVariazione(NomiDelleImmagini.SPRITE_PIETRA_PREZIOSA, evento.getNuovoValore() - evento.getValorePrecedente(), preziosiY,
				"Preziosi variati"));
	}

	private void gestisciEventoVariazioneMonete(NotificaVariazioneDisponibilitaMonete evento) {
		pubblica(spriteDellaVariazione(NomiDelleImmagini.SPRITE_MONETA, evento.getNuovoValore() - evento.getValorePrecedente(), moneteY, "Monete variate"));
	}

	private void gestisciEventoVariazionePuntiEsperienza(NotificaVariazionePuntiEsperienzaPersonaggio evento) {
		// I punti esperienza non hanno un'icona
		pubblica(spriteDellaVariazione(null, evento.getNuovoValore() - evento.getValorePrecedente(), puntiY, "Punti esperienza variati"));
	}

	private static void pubblica(DescrizioneSpriteATempo sprite) {
		if (sprite != null) {
			BusEventi.pubblica(new InternoCreazioneSpriteATempo(sprite));
		}
	}

	private DescrizioneSpriteATempo spriteDellaVariazione(String icona, int variazione, int y, String descrizione) {
		if (variazione == 0) {
			return null;
		}
		return DescrizioneSpriteATempo.conVariazione(icona, icona == null ? null : misuraDellIcona(icona), variazione, fontMedium, totaleX, y, false,
				descrizione);
	}

	public int getTopLeftX() {
		return topLeftX;
	}

	public int getTopLeftY() {
		return topLeftY;
	}

	/**
	 * Le sei scritte del riquadro, con i valori di adesso
	 */
	public List<VoceDiTesto> voci() {
		List<VoceDiTesto> voci = new ArrayList<>();
		voci.add(new VoceDiTesto(TestiDellaUI.get("MONETE"), scrittaX, moneteY, COLORE, false, 0));
		voci.add(new VoceDiTesto(String.valueOf(vistaPartita.getCassa().getMonete()), totaleX, moneteY, COLORE, true, 0));
		voci.add(new VoceDiTesto(TestiDellaUI.get("PREZIOSI"), scrittaX, preziosiY, COLORE, false, 0));
		voci.add(new VoceDiTesto(String.valueOf(vistaPartita.getCassa().getPreziosi()), totaleX, preziosiY, COLORE, true, 0));
		voci.add(new VoceDiTesto(TestiDellaUI.get("PUNTI"), scrittaX, puntiY, COLORE, false, 0));
		voci.add(new VoceDiTesto(vistaPartita.getPuntiEsperienza() + "/" + vistaPartita.getPuntiEsperienzaPerProssimoLivello(), totaleX, puntiY,
				COLORE, true, 0));
		return voci;
	}
}
