package com.threeamigos.foresta.libgdx;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoCreazioneSpriteATempo;
import com.threeamigos.foresta.eventi.notifiche.NotificaAumentoLivelloPersonaggio;
import com.threeamigos.foresta.eventi.notifiche.NotificaVariazioneDisponibilitaMonete;
import com.threeamigos.foresta.eventi.notifiche.NotificaVariazioneStatistichePersonaggio;
import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.interfacce.VistaPersonaggio;
import com.threeamigos.foresta.libgdx.sviluppo.DatiDiProva;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.immagini.NomiDelleImmagini;
import com.threeamigos.foresta.ui.riquadri.RiquadroGruppo;
import com.threeamigos.foresta.ui.riquadri.RiquadroStatistiche;
import com.threeamigos.foresta.ui.sprite.DescrizioneSpriteATempo;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Gli sprite delle variazioni, creati dai modelli dei riquadri del modulo comune (gli stessi per Swing e libGDX), provati con la partita finta di chi
 * sviluppa: un riquadro che riceve una variazione pubblica sul bus la descrizione dello sprite.
 */
class SpriteDeiRiquadriTest {

	private final List<DescrizioneSpriteATempo> pubblicate = new ArrayList<>();
	private final VistaPartita partita = DatiDiProva.partita();

	private class Gruppo extends RiquadroGruppo {
		Gruppo() {
			super(20, 20, 368, 288, partita);
		}

		@Override
		protected Misura misuraDellIcona(String risorsa) {
			return new Misura(16, 16);
		}

		void sincronizzaPerLaProva() {
			sincronizzaScorrimento();
		}

		int xSalute() {
			return rightXOffsetSalute;
		}
	}

	private class Statistiche extends RiquadroStatistiche {
		Statistiche() {
			super(20, 20, 200, partita);
		}

		@Override
		protected Misura misuraDellIcona(String risorsa) {
			return new Misura(10, 10);
		}
	}

	@BeforeEach
	void ascolta() {
		BusEventi.azzera();
		BusEventi.impostaConsegna(Runnable::run);
		BusEventi.iscriviti(InternoCreazioneSpriteATempo.class, evento -> pubblicate.add(evento.getDescrizione()));
	}

	@AfterEach
	void smetti() {
		BusEventi.azzera();
	}

	@Test
	void laVariazioneDiSaluteDiUnPersonaggioDelGruppoPubblicaLoSpriteSopraIlSuoValore() {
		Gruppo gruppo = new Gruppo();
		gruppo.sincronizzaPerLaProva();
		VistaPersonaggio guerriero = partita.getGruppoGiocatore().getPersonaggio(0);
		gruppo.gestisciEventoVariazioneStatistichePersonaggio(new NotificaVariazioneStatistichePersonaggio(guerriero, TipoAttributo.SALUTE, 100, 105));
		assertEquals(1, pubblicate.size());
		DescrizioneSpriteATempo sprite = pubblicate.get(0);
		assertEquals("+5", sprite.getTestoVariazione());
		assertEquals(NomiDelleImmagini.SPRITE_COMBATTIMENTO, sprite.getIcona());
		assertEquals(DoomdarkColorModel.Color.GREEN, sprite.getColore());
		// Ancorato a destra: il testo finisce dove finisce la colonna del valore
		assertEquals(gruppo.xSalute(), sprite.getXIniziale() + sprite.getLarghezza(), 1e-6);
		assertEquals(gruppo.getAreaLista().getY(), sprite.getRitaglio().getY());
	}

	@Test
	void senzaVariazioneONonNelGruppoNonSiPubblicaNiente() {
		Gruppo gruppo = new Gruppo();
		gruppo.sincronizzaPerLaProva();
		VistaPersonaggio guerriero = partita.getGruppoGiocatore().getPersonaggio(0);
		gruppo.gestisciEventoVariazioneStatistichePersonaggio(new NotificaVariazioneStatistichePersonaggio(guerriero, TipoAttributo.SALUTE, 100, 100));
		// Un attributo che il riquadro non mostra
		gruppo.gestisciEventoVariazioneStatistichePersonaggio(new NotificaVariazioneStatistichePersonaggio(guerriero, TipoAttributo.FORZA, 1, 2));
		assertTrue(pubblicate.isEmpty());
	}

	@Test
	void piuStanchezzaEPeggioQuindiAlContrarioDegliAltri() {
		Gruppo gruppo = new Gruppo();
		gruppo.sincronizzaPerLaProva();
		VistaPersonaggio guerriero = partita.getGruppoGiocatore().getPersonaggio(0);
		gruppo.gestisciEventoVariazioneStatistichePersonaggio(new NotificaVariazioneStatistichePersonaggio(guerriero, TipoAttributo.STANCHEZZA, 5, 8));
		gruppo.gestisciEventoVariazioneStatistichePersonaggio(new NotificaVariazioneStatistichePersonaggio(guerriero, TipoAttributo.STANCHEZZA, 8, 3));
		assertEquals(DoomdarkColorModel.Color.RED, pubblicate.get(0).getColore());
		assertEquals(DoomdarkColorModel.Color.GREEN, pubblicate.get(1).getColore());
	}

	@Test
	void ilLivelloHaLaSuaIconaEIlTempoParteDallaCimaDelBlocco() {
		Gruppo gruppo = new Gruppo();
		gruppo.sincronizzaPerLaProva();
		VistaPersonaggio guerriero = partita.getGruppoGiocatore().getPersonaggio(0);
		gruppo.gestisciEventoAumentoLivelloPersonaggio(new NotificaAumentoLivelloPersonaggio(guerriero, 4, 5));
		gruppo.gestisciEventoVariazioneStatistichePersonaggio(new NotificaVariazioneStatistichePersonaggio(guerriero, TipoAttributo.TEMPO, 1, 2));
		assertEquals(NomiDelleImmagini.POZIONE_AUMENTO_LIVELLO, pubblicate.get(0).getIcona());
		assertEquals("Tempo variato", pubblicate.get(1).getDescrizione());
		assertEquals(NomiDelleImmagini.SPRITE_TEMPO, pubblicate.get(1).getIcona());
	}

	@Test
	void leMoneteDannoLoSpriteConLaMonetaSullaRigaDelleMonete() {
		Statistiche statistiche = new Statistiche();
		BusEventi.pubblica(new NotificaVariazioneDisponibilitaMonete(100, 90));
		assertEquals(1, pubblicate.size());
		DescrizioneSpriteATempo sprite = pubblicate.get(0);
		assertEquals("-10", sprite.getTestoVariazione());
		assertEquals(NomiDelleImmagini.SPRITE_MONETA, sprite.getIcona());
		assertEquals(DoomdarkColorModel.Color.RED, sprite.getColore());
		assertNotNull(statistiche);
	}
}
