package com.threeamigos.foresta.ui.schermate;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoAcquistoConsumabile;
import com.threeamigos.foresta.interfacce.VistaOffertaConsumabile;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.interfacce.VistaPersonaggio;
import com.threeamigos.foresta.motore.VistaPartitaMotore;
import com.threeamigos.foresta.tipi.TipoConsumabile;
import com.threeamigos.foresta.tipi.TipoIncantesimo;
import com.threeamigos.foresta.ui.Finestra;
import com.threeamigos.foresta.ui.Finestra.Tasto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Il doppio click compra, vende o spende un punto abilità solo con il tasto sinistro, come il click singolo: un doppio click destro o
 * centrale non fa niente.
 */
class DoppioClickDelloScambiatoreTest {

    private List<ComandoAcquistoConsumabile> acquisti;
    private ModelloDati modelloDatiPrecedente;

    @BeforeEach
    void ascoltaGliAcquisti() {
        modelloDatiPrecedente = ModelloDati.getIstanza();
        ModelloDati.installa();
        ModelloDati.setIstanza(new ModelloDati());
        BusEventi.azzera();
        BusEventi.impostaConsegna(Runnable::run);
        acquisti = new ArrayList<>();
        BusEventi.iscriviti(ComandoAcquistoConsumabile.class, acquisti::add);
    }

    @AfterEach
    void ripristinaBus() {
        ModelloDati.setIstanza(modelloDatiPrecedente);
        BusEventi.azzera();
        BusEventi.impostaConsegna(BusEventi.CONSEGNA_SU_THREAD_PRINCIPALE);
    }

    private static VistaOffertaConsumabile pozioneDiSalute() {
        return new VistaOffertaConsumabile() {
            @Override public TipoConsumabile getTipo() { return TipoConsumabile.POZIONE_SALUTE; }
            @Override public TipoIncantesimo getClasseIncantesimo() { return null; }
            @Override public VistaPersonaggio getPersonaggio() { return null; }
            @Override public String getNome() { return "una pozione di salute"; }
            @Override public String getDescrizione() { return "che cura"; }
            @Override public int getCosto() { return 5; }
        };
    }

    @Test
    void alchimistaSoloIlTastoSinistroCompra() {
        DisplayableCanvasScambiatoreConsumabili alchimista = new DisplayableCanvasScambiatoreConsumabili(1024, 768, new VistaPartitaMotore());
        alchimista.impostaOfferte(Collections.singletonList(pozioneDiSalute()));

        // Si cerca dove sta la pozione nell'elenco del venditore: il punto in cui il doppio click sinistro compra
        int doveX = -1;
        int doveY = -1;
        for (int y = 0; y < 768 && doveX < 0; y += 2) {
            for (int x = 400; x < 1024 && doveX < 0; x += 4) {
                alchimista.processaDoppioClick(x, y, Tasto.SINISTRO);
                if (!acquisti.isEmpty()) {
                    doveX = x;
                    doveY = y;
                }
            }
        }
        assertTrue(doveX >= 0, "il doppio click sinistro compra da qualche parte");
        assertEquals(1, acquisti.size());

        alchimista.processaDoppioClick(doveX, doveY, Tasto.DESTRO);
        alchimista.processaDoppioClick(doveX, doveY, Tasto.CENTRALE);
        assertEquals(1, acquisti.size(), "il doppio click destro o centrale non compra");

        alchimista.processaDoppioClick(doveX, doveY, Tasto.SINISTRO);
        assertEquals(2, acquisti.size());
    }

    @Test
    void trascinareLElencoLoScorreESenzaComprare() {
        // Given: tante offerte, così l'elenco del venditore è più alto dello spazio e si può scorrere
        DisplayableCanvasScambiatoreConsumabili alchimista = new DisplayableCanvasScambiatoreConsumabili(1024, 768, new VistaPartitaMotore());
        List<VistaOffertaConsumabile> offerte = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            offerte.add(pozioneDiSalute());
        }
        alchimista.impostaOfferte(offerte);
        // Il disegno direbbe di quanto si può scorrere: qui lo si dice a mano
        alchimista.scorrimentoDestra.imposta(true, 500.0d);
        assertEquals(0, alchimista.offsetYZonaDestra());
        // Il punto dove il doppio click sinistro compra
        int doveX = -1;
        int doveY = -1;
        for (int y = 0; y < 768 && doveX < 0; y += 2) {
            for (int x = 400; x < 1024 && doveX < 0; x += 4) {
                alchimista.processaDoppioClick(x, y, Tasto.SINISTRO);
                if (!acquisti.isEmpty()) {
                    doveX = x;
                    doveY = y;
                }
            }
        }
        assertTrue(doveX >= 0);
        acquisti.clear();
        // When: si preme lì e si trascina verso l'alto di 100 pixel, e si lascia
        alchimista.processaPressione(doveX, doveY, Tasto.SINISTRO);
        alchimista.processaTrascinamento(doveX, doveY - 30);
        alchimista.processaTrascinamento(doveX, doveY - 100);
        alchimista.processaRilascio(doveX, doveY - 100, Tasto.SINISTRO);
        // Then: la lista è scorsa e il doppio click che Swing manda dopo il rilascio non compra
        assertTrue(alchimista.offsetYZonaDestra() > 0, "" + alchimista.offsetYZonaDestra());
        alchimista.processaDoppioClick(doveX, doveY - 100, Tasto.SINISTRO);
        assertTrue(acquisti.isEmpty(), "il rilascio di un trascinamento non compra: " + acquisti);
        // When: un tocco senza trascinare ricompra
        alchimista.processaPressione(doveX, doveY, Tasto.SINISTRO);
        alchimista.processaRilascio(doveX, doveY, Tasto.SINISTRO);
        alchimista.processaDoppioClick(doveX, doveY, Tasto.SINISTRO);
        assertEquals(1, acquisti.size());
    }

    @Test
    void laRotellaScorreLaZonaSottoIlCursore() {
        DisplayableCanvasScambiatoreConsumabili alchimista = new DisplayableCanvasScambiatoreConsumabili(1024, 768, new VistaPartitaMotore());
        List<VistaOffertaConsumabile> offerte = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            offerte.add(pozioneDiSalute());
        }
        alchimista.impostaOfferte(offerte);
        alchimista.scorrimentoDestra.imposta(true, 500.0d);
        alchimista.scorrimentoSinistra.imposta(true, 500.0d);
        alchimista.processaRotella(alchimista.xMinimaZonaDestra + 10, 300, 5, Finestra.MovimentoRotella.GIU);
        assertTrue(alchimista.offsetYZonaDestra() > 0);
        assertEquals(0, alchimista.offsetYZonaSinistra());
    }
}
