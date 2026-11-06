package com.threeamigos.foresta.ui.schermate;

import com.threeamigos.foresta.strumenti.TestataSalvataggio;
import com.threeamigos.foresta.ui.Finestra;

import java.util.Collection;

/**
 * Le schermate senza partita e l'intro (vedi {@link SchermateSenzaPartita} e {@link IntroDelGioco}) come le comanda lo schermo del
 * gioco: le implementano Swing e libGDX, che le disegnano.
 */
public interface SchermateIntroOutro extends Finestra {

	/** Fa ripartire la sequenza (dell'intro, della vittoria o della sconfitta) dall'inizio */
	void resettaSequenza();

	/** La prossima intro parte dalla classifica */
	void posizionaSuPunteggi();

	/** Fa avanzare il racconto della vittoria o della sconfitta, fino a quella lunghezza */
	void incrementaSequenza(int lunghezzaMassima);

	/** Il messaggio a tutto schermo in caratteri grandi */
	void impostaMessaggio(String messaggio);

	/** Le testate dei salvataggi da mostrare nella scelta del posto */
	void setSalvataggiDisponibili(Collection<TestataSalvataggio> salvataggiDisponibili);
}
