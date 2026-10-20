package com.threeamigos.foresta.ui.schermate;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoAcquistoConsumabile;
import com.threeamigos.foresta.interfacce.VistaOffertaConsumabile;
import com.threeamigos.foresta.interfacce.VistaPersonaggio;
import com.threeamigos.foresta.motore.VistaPartitaMotore;
import com.threeamigos.foresta.tipi.TipoConsumabile;
import com.threeamigos.foresta.tipi.TipoIncantesimo;
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

    @BeforeEach
    void ascoltaGliAcquisti() {
        BusEventi.azzera();
        BusEventi.impostaConsegna(Runnable::run);
        acquisti = new ArrayList<>();
        BusEventi.iscriviti(ComandoAcquistoConsumabile.class, acquisti::add);
    }

    @AfterEach
    void ripristinaBus() {
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
}
