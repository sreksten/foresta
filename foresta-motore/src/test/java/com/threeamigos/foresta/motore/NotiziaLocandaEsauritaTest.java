package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.modellodati.NotiziaMD;
import com.threeamigos.foresta.testi.NotizieDelleLocande;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoLocazione;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Con le notizie proprie della locanda tutte fra le ultime pubblicate, NotizieDelleLocande trova comunque una notizia (fra le
 * generiche di NOTIZIE_LOCANDA o, se i tentativi vanno a vuoto, ammettendo le recenti), senza sollevare eccezioni: un tempo dopo
 * venti tentativi ne sollevava una, che fermava le partite lunghe.
 */
class NotiziaLocandaEsauritaTest {

    @Test
    void conTutteLeNotizieFraLeUltimeSiRipeteUnaNotiziaSenzaEccezioni() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(77)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            for (int i = 1; i <= 4; i++) {
                ModelloDati.getIstanza().getNotizieMD().aggiungiNotizia(new NotiziaMD("BC" + i, "già detta " + i));
            }
            for (int volta = 0; volta < 30; volta++) {
                NotiziaMD notizia = assertDoesNotThrow(() ->
                        NotizieDelleLocande.getNotiziaLocanda("BOCCALE_CAPOVOLTO", "il Boccale Capovolto", "Oste"));
                // Un Guerriero solo: le notizie che parlano di altre classi non si applicano, ma altre sì (EROE, GUERRIERO, quelle generiche)
                assertNotNull(notizia);
            }
        }
    }
}
