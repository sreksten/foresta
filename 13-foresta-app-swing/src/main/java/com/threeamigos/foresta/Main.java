package com.threeamigos.foresta;

import com.threeamigos.foresta.motore.AvvioDelMotore;
import com.threeamigos.foresta.motore.StrumentiDiSviluppoMotore;
import com.threeamigos.foresta.strumenti.ModalitaDiProva;
import com.threeamigos.foresta.strumenti.logger.Logger;
import com.threeamigos.foresta.strumenti.temporizzatore.ThreadPrincipale;
import com.threeamigos.foresta.ui.EsecutoreSwing;
import com.threeamigos.foresta.ui.ForestaSwing;
import com.threeamigos.foresta.ui.OpzioniDiAvvio;
import com.threeamigos.foresta.ui.sviluppo.FinestraDiProva;

/**
 * Il punto d'ingresso del gioco con Swing: l'unico posto che conosce insieme motore e interfaccia grafica e li collega. Il motore lo
 * collega {@link AvvioDelMotore} (lo stesso di libGDX), con il thread di Swing come thread principale; qui si aggiungono il registro
 * di tutti gli eventi, la finestra e, in modalità di prova, la finestra di prova. Motore e interfaccia dialogano solo tramite eventi.
 * <p>
 * Gli argomenti sono quelli di {@link OpzioniDiAvvio}.
 */
public class Main {

	public static void main(String[] args) throws Exception {
		OpzioniDiAvvio opzioni = OpzioniDiAvvio.leggi(args);
		for (String sconosciuto : opzioni.getAltri()) {
			Logger.log(() -> "Argomento sconosciuto ignorato: " + sconosciuto);
		}

		// Si registra per ascoltare qualsiasi evento venga generato
		new SnifferBusEventi();
		// Tutta la logica di gioco gira sull'Event Dispatch Thread di Swing; sul desktop si esce chiudendo il processo
		AvvioDelMotore motore = new AvvioDelMotore(new EsecutoreSwing(), e -> System.exit(e.getCodice()), opzioni.isLinguaDecisa());

		// La finestra, quando è pronta, avvisa l'automa che può cominciare
		new ForestaSwing(opzioni.getOrientamento(), opzioni.isTuttoSchermo(), opzioni.isSaltaLogo(), opzioni.isBarraDock(),
				opzioni.isJoystickVirtuale(), motore.getGestorePunteggi(), motore.getVistaPartita(), AvvioDelMotore.nuovoTemporizzatore());

		if (ModalitaDiProva.isAttiva()) {
			ThreadPrincipale.accoda(() -> new FinestraDiProva(motore.getVistaPartita(), new StrumentiDiSviluppoMotore()).mostra());
		}
	}
}
