package com.threeamigos.foresta.strumenti;

/**
 * Il thread su cui gira tutta la logica di gioco (per Swing l'Event Dispatch Thread, per un altro front-end il suo
 * thread principale). Motore e bus eventi non conoscono il toolkit grafico: chiedono qui di eseguire un'operazione
 * sul thread principale, e chi assembla il gioco installa con {@link #imposta} l'esecutore del proprio toolkit.
 * Finche' non se ne installa uno, le operazioni girano subito sul thread chiamante.
 *
 * @author Stefano Reksten
 */
public final class ThreadPrincipale {

    /**
     * L'accesso al thread principale di un toolkit grafico.
     */
    public interface Esecutore {

        /**
         * @return true se il thread corrente è il thread principale
         */
        boolean eIlThreadCorrente();

        /**
         * Accoda l'operazione sul thread principale, anche se si è già lì.
         */
        void accoda(Runnable operazione);
    }

    private static final Esecutore SUBITO = new Esecutore() {
        @Override
        public boolean eIlThreadCorrente() {
            return true;
        }

        @Override
        public void accoda(Runnable operazione) {
            operazione.run();
        }
    };

    private static volatile Esecutore esecutore = SUBITO;

    private ThreadPrincipale() {
    }

    /**
     * Installa l'esecutore del toolkit grafico; con null si torna all'esecuzione immediata.
     */
    public static void imposta(Esecutore nuovoEsecutore) {
        esecutore = nuovoEsecutore == null ? SUBITO : nuovoEsecutore;
    }

    /**
     * Esegue l'operazione sul thread principale: subito se si è già lì, altrimenti accodata.
     */
    public static void esegui(Runnable operazione) {
        Esecutore corrente = esecutore;
        if (corrente.eIlThreadCorrente()) {
            operazione.run();
        } else {
            corrente.accoda(operazione);
        }
    }

    /**
     * Accoda l'operazione sul thread principale, anche se si è già lì: parte dopo che quella in corso è finita.
     */
    public static void accoda(Runnable operazione) {
        esecutore.accoda(operazione);
    }
}
