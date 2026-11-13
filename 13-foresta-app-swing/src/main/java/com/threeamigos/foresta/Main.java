package com.threeamigos.foresta;

import com.threeamigos.foresta.motore.AvvioDelMotore;
import com.threeamigos.foresta.motore.StrumentiDiSviluppoMotore;
import com.threeamigos.foresta.strumenti.ModalitaDiProva;
import com.threeamigos.foresta.strumenti.logger.Logger;
import com.threeamigos.foresta.strumenti.temporizzatore.ThreadPrincipale;
import com.threeamigos.foresta.ui.EsecutoreSwing;
import com.threeamigos.foresta.ui.ForestaSwing;
import com.threeamigos.foresta.tipi.PosizioneDellaBarra;
import com.threeamigos.foresta.ui.LivelloDiZoom;
import com.threeamigos.foresta.ui.MisuraDelloSchermo;
import com.threeamigos.foresta.ui.OpzioniDiAvvio;
import com.threeamigos.foresta.ui.SceltaDelLayout;
import com.threeamigos.foresta.ui.ScelteDiAvvio;
import com.threeamigos.foresta.ui.immagini.MisureDaiFile;
import com.threeamigos.foresta.ui.sviluppo.FinestraDelleOpzioniDiAvvio;
import com.threeamigos.foresta.ui.sviluppo.FinestraDiProva;

import java.util.Optional;

/**
 * Il punto d'ingresso del gioco con Swing: l'unico posto che conosce insieme motore e interfaccia grafica e li collega. Il motore lo
 * collega {@link AvvioDelMotore} (lo stesso di libGDX), con il thread di Swing come thread principale; qui si aggiungono il registro
 * di tutti gli eventi, la finestra e, in modalità di prova, la finestra di prova. Motore e interfaccia dialogano solo tramite eventi.
 * <p>
 * Gli argomenti sono quelli di {@link OpzioniDiAvvio}; con {@code MODALITA_DI_PROVA} prima del gioco si apre la finestra delle opzioni di avvio
 * ({@link FinestraDelleOpzioniDiAvvio}), che parte da quelli scritti. Con {@code SCHERMO=} Swing non rimpicciolisce: la finestra è grande quanto l'area
 * virtuale dello schermo simulato, senza scala, con il layout che gli tocca (vedi {@code layout_telefono.md} §3.2).
 */
public class Main {

	public static void main(String[] args) throws Exception {
		// In modalità di prova le opzioni si scelgono in una finestra, partendo da quelle scritte; prima di ogni messaggio di log, perché la finestra
		// sceglie anche la cartella del log. Da lì si può anche uscire
		if (ScelteDiAvvio.isModalitaDiProva(args)) {
			Optional<String[]> scelti = FinestraDelleOpzioniDiAvvio.chiedi(args);
			if (!scelti.isPresent()) {
				System.exit(0);
			}
			args = scelti.get();
		}
		OpzioniDiAvvio opzioni = OpzioniDiAvvio.leggi(args);
		for (String sconosciuto : opzioni.getAltri()) {
			Logger.log(() -> "Argomento sconosciuto ignorato: " + sconosciuto);
		}
		// Si registra per ascoltare qualsiasi evento venga generato
		new SnifferBusEventi();
		// Tutta la logica di gioco gira sull'Event Dispatch Thread di Swing; sul desktop si esce chiudendo il processo
		AvvioDelMotore motore = new AvvioDelMotore(new EsecutoreSwing(), e -> System.exit(e.getCodice()), opzioni.isLinguaDecisa());

		// La barra dove dicono gli argomenti, se lo dicono, altrimenti dove l'ha messa il giocatore nelle opzioni (o la decide il gioco)
		PosizioneDellaBarra posizione = opzioni.getPosizioneDellaBarra() != null ? opzioni.getPosizioneDellaBarra()
				: motore.getGestoreOpzioni().getPosizioneDellaBarra();
		MisuraDelloSchermo schermo = opzioni.getSchermoDiProva();
		if (schermo != null) {
			Logger.log(() -> "Schermo di prova " + schermo + ": " + SceltaDelLayout.per(schermo, new MisureDaiFile(), LivelloDiZoom.valore(), posizione));
		}

		// La finestra, quando è pronta, avvisa l'automa che può cominciare
		new ForestaSwing(posizione, schermo, opzioni.isTuttoSchermo(), opzioni.isSaltaLogo(),
				opzioni.isBarraDock(motore.getGestoreOpzioni().getStileDellaBarra()),
				opzioni.isJoystickVirtuale(), motore.getGestorePunteggi(), motore.getVistaPartita(), AvvioDelMotore.nuovoTemporizzatore());

		if (ModalitaDiProva.isAttiva()) {
			ThreadPrincipale.accoda(() -> new FinestraDiProva(motore.getVistaPartita(), new StrumentiDiSviluppoMotore()).mostra());
		}
	}
}
