package com.threeamigos.foresta.strumenti.temporizzatore;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ThreadPrincipaleTest {

    @AfterEach
    void tornaAllEsecuzioneImmediata() {
        ThreadPrincipale.imposta(null);
    }

    /**
     * Un thread principale finto che tiene in coda le operazioni, per vedere quando girano.
     */
    private static final class Coda implements ThreadPrincipale.Esecutore {
        private final List<Runnable> accodate = new ArrayList<>();
        private boolean sulThreadPrincipale;

        @Override
        public boolean eIlThreadCorrente() {
            return sulThreadPrincipale;
        }

        @Override
        public void accoda(Runnable operazione) {
            accodate.add(operazione);
        }
    }

    @Test
    void senzaEsecutoreLeOperazioniGiranoSubito() {
        List<String> eseguite = new ArrayList<>();
        ThreadPrincipale.esegui(() -> eseguite.add("esegui"));
        ThreadPrincipale.accoda(() -> eseguite.add("accoda"));
        assertEquals(2, eseguite.size());
    }

    @Test
    void sulThreadPrincipaleSiEsegueSubito() {
        Coda coda = new Coda();
        coda.sulThreadPrincipale = true;
        ThreadPrincipale.imposta(coda);
        List<String> eseguite = new ArrayList<>();
        ThreadPrincipale.esegui(() -> eseguite.add("subito"));
        assertEquals(1, eseguite.size());
        assertEquals(0, coda.accodate.size());
    }

    @Test
    void daUnAltroThreadSiAccoda() {
        Coda coda = new Coda();
        ThreadPrincipale.imposta(coda);
        List<String> eseguite = new ArrayList<>();
        ThreadPrincipale.esegui(() -> eseguite.add("dopo"));
        assertEquals(0, eseguite.size());
        assertEquals(1, coda.accodate.size());
        coda.accodate.get(0).run();
        assertEquals(1, eseguite.size());
    }

    @Test
    void accodaAccodaAncheSeSiELa() {
        Coda coda = new Coda();
        coda.sulThreadPrincipale = true;
        ThreadPrincipale.imposta(coda);
        ThreadPrincipale.accoda(() -> { });
        assertEquals(1, coda.accodate.size());
    }
}
