package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.modellodati.NotiziaMD;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoLocazione;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import com.threeamigos.foresta.testi.ProduttoreDiTestiCasuale;

/**
 * Il repertorio delle notizie di una locanda è di cinque: tornandoci spesso si esaurisce (tutte fra le ultime pubblicate) e
 * ProduttoreDiTestiCasuale azzera la grammatica e ripete una notizia, senza sollevare eccezioni (che fermavano le partite lunghe).
 */
class NotiziaLocandaEsauritaTest {

    @Test
    void conTutteLeNotizieFraLeUltimeSiRipeteUnaNotiziaSenzaEccezioni() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(77)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            for (int i = 1; i <= 5; i++) {
                ModelloDati.getIstanza().getNotizieMD().getUltimeNotizie().add(new NotiziaMD("BC" + i, "già detta " + i));
            }
            for (int volta = 0; volta < 30; volta++) {
                NotiziaMD notizia = assertDoesNotThrow(() ->
                        ProduttoreDiTestiCasuale.getNotiziaLocanda("BOCCALE_CAPOVOLTO", "il Boccale Capovolto", "Oste"));
                // Un Guerriero solo: le notizie che parlano di altre classi non si applicano, ma altre sì (EROE, GUERRIERO, quelle generiche)
                assertNotNull(notizia);
            }
        }
    }
}
