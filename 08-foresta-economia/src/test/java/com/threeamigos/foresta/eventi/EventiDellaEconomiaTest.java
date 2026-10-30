package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.artefatti.BancoDiLavoro;
import com.threeamigos.foresta.economia.MondoDiProva;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoAcquistoConsumabile;
import com.threeamigos.foresta.eventi.interni.InternoAcquistoArtefatto;
import com.threeamigos.foresta.eventi.interni.InternoIncantatura;
import com.threeamigos.foresta.eventi.interni.InternoPastoConsumatoInLocanda;
import com.threeamigos.foresta.eventi.interni.InternoVenditaArtefatto;
import com.threeamigos.foresta.eventi.notifiche.NotificaApprovazioneAcquistoArtefatto;
import com.threeamigos.foresta.eventi.notifiche.NotificaApprovazioneAcquistoConsumabile;
import com.threeamigos.foresta.eventi.notifiche.NotificaApprovazioneIncantatura;
import com.threeamigos.foresta.eventi.notifiche.NotificaApprovazioneVenditaArtefatto;
import com.threeamigos.foresta.eventi.notifiche.NotificaAvvisoIncantatura;
import com.threeamigos.foresta.eventi.notifiche.NotificaRifiutoAcquistoArtefatto;
import com.threeamigos.foresta.eventi.notifiche.NotificaRifiutoAcquistoConsumabile;
import com.threeamigos.foresta.eventi.notifiche.NotificaRifiutoIncantatura;
import com.threeamigos.foresta.eventi.notifiche.NotificaRifiutoVenditaArtefatto;
import com.threeamigos.foresta.eventi.richieste.RichiestaAperturaIncantatore;
import com.threeamigos.foresta.eventi.richieste.RichiestaAperturaInventarioCommerciante;
import com.threeamigos.foresta.eventi.richieste.RichiestaAperturaInventarioFornitore;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoConsumabile;
import com.threeamigos.foresta.tipi.TipoIncantesimo;
import com.threeamigos.foresta.tipi.TipoMotivoRifiutoIncantatura;
import com.threeamigos.foresta.tipi.TipoNegozio;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le richieste, i comandi e le notifiche del commercio: portano quel che gli si è dato.
 */
class EventiDellaEconomiaTest {

    @Test
    void gliEventiDegliArtefatti() {
        Artefatto spada = MondoDiProva.artefatto(TipoArtefatto.SPADA, 1);
        BancoDiLavoro banco = new BancoDiLavoro();
        InternoAcquistoArtefatto acquisto = new InternoAcquistoArtefatto(banco, banco, spada, null);
        assertSame(spada, acquisto.approvazione().getOggettoSpostato());
        assertEquals(acquisto.getUuid(), acquisto.approvazione().getUuidRichiesta());
        assertSame(spada, acquisto.rifiuto().getOggettoSpostato());
        InternoVenditaArtefatto vendita = new InternoVenditaArtefatto(banco, banco, spada, null);
        assertEquals(vendita.getUuid(), vendita.approvazione().getUuidRichiesta());
        assertEquals(vendita.getUuid(), vendita.rifiuto().getUuidRichiesta());
        assertNotNull(new NotificaApprovazioneAcquistoArtefatto(null, spada, "x"));
        assertNotNull(new NotificaRifiutoAcquistoArtefatto(null, spada, "x"));
        assertNotNull(new NotificaApprovazioneVenditaArtefatto(null, spada, "x"));
        assertNotNull(new NotificaRifiutoVenditaArtefatto(null, spada, "x"));
    }

    @Test
    void gliEventiDellIncantatura() {
        BancoDiLavoro banco = new BancoDiLavoro();
        InternoIncantatura incantatura = new InternoIncantatura(banco, "Diavolina");
        assertSame(banco, incantatura.getBanco());
        assertEquals("Diavolina", incantatura.getNomeProprio());
        Artefatto spada = MondoDiProva.artefatto(TipoArtefatto.SPADA, 1);
        NotificaApprovazioneIncantatura approvazione = new NotificaApprovazioneIncantatura(spada, 15);
        assertSame(spada, approvazione.getArtefatto());
        assertEquals(15, approvazione.getCosto());
        assertEquals(TipoMotivoRifiutoIncantatura.NESSUN_ARTEFATTO,
                new NotificaRifiutoIncantatura(TipoMotivoRifiutoIncantatura.NESSUN_ARTEFATTO).getMotivo());
        assertEquals("Che pasticcio", new NotificaAvvisoIncantatura("Che pasticcio").getFrase());
        assertNotNull(new InternoPastoConsumatoInLocanda());
    }

    @Test
    void ilComandoEIlSuoEsito() {
        ComandoAcquistoConsumabile comando = new ComandoAcquistoConsumabile(TipoConsumabile.INCANTESIMO, TipoIncantesimo.FUOCO, "u");
        assertEquals(TipoConsumabile.INCANTESIMO, comando.getTipoConsumabile());
        assertEquals(TipoIncantesimo.FUOCO, comando.getClasseIncantesimo());
        assertEquals("u", comando.getUuidPersonaggio());
        NotificaApprovazioneAcquistoConsumabile ok = new NotificaApprovazioneAcquistoConsumabile(comando, 7);
        assertSame(comando, ok.getEventoRichiestaAcquistoConsumabile());
        assertEquals(7, ok.getPrezzoPagato());
        assertSame(comando, new NotificaRifiutoAcquistoConsumabile(comando).getEventoRichiestaAcquistoConsumabile());
    }

    @Test
    void leRichiesteDiApertura() {
        RichiestaAperturaIncantatore incantatore = new RichiestaAperturaIncantatore(Collections.<Comando>emptyList(), null, "Benvenuti");
        assertEquals("Benvenuti", incantatore.getMessaggio());
        assertNull(incantatore.getBanco());
        RichiestaAperturaInventarioCommerciante commerciante =
                new RichiestaAperturaInventarioCommerciante(Collections.<Comando>emptyList(), TipoNegozio.ARMAIOLO, null);
        assertEquals(TipoNegozio.ARMAIOLO, commerciante.getNegozio());
        assertNull(commerciante.getScambio());
        RichiestaAperturaInventarioFornitore fornitore =
                new RichiestaAperturaInventarioFornitore(Collections.<Comando>emptyList(), Collections.emptyList());
        assertTrue(fornitore.getOfferte().isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> fornitore.getOfferte().add(null));
    }
}
