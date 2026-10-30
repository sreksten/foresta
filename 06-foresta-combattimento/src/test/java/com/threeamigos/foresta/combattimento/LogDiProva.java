package com.threeamigos.foresta.combattimento;

import com.threeamigos.foresta.strumenti.logger.DestinazioneDiLog;
import com.threeamigos.foresta.strumenti.logger.Logger;

/**
 * Il registro di gioco per le prove: spento come di solito, o acceso su un pozzo che non scrive niente (per passare anche dalle strade del
 * codice che costruiscono i testi del registro solo se qualcuno li scrive).
 */
final class LogDiProva {

    private static final DestinazioneDiLog POZZO = new DestinazioneDiLog() {
        @Override
        public void scrivi(String messaggio) {
        }

        @Override
        public void scrivi(Exception e) {
        }
    };

    private LogDiProva() {
    }

    /** Alla seconda ripetizione di una prova il registro è acceso. */
    static void perRipetizione(int ripetizione) {
        Logger.impostaDestinazione(ripetizione == 2 ? POZZO : DestinazioneDiLog.NESSUNA);
    }

    static void spegni() {
        Logger.impostaDestinazione(DestinazioneDiLog.NESSUNA);
    }
}
