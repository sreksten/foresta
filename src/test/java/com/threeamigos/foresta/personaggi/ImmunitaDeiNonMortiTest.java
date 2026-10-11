package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.tipi.CategoriaPersonaggio;
import com.threeamigos.foresta.tipi.ClasseIncantesimo;
import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Quello che hanno in comune tutti i non morti (categoria NON_MORTO) lo dà PersonaggioBase; le classi aggiungono il loro.
 */
class ImmunitaDeiNonMortiTest {

    @BeforeEach
    void preparaModello() {
        ModelloDati.setIstanza(new ModelloDati());
    }

    private static Personaggio crea(TipoPersonaggio tipo) {
        return FabbricaPersonaggi.crea(tipo, 1);
    }

    @Test
    void tuttiINonMortiHannoLeStesseImmunitaEDebolezzeDiBase() {
        int controllati = 0;
        for (TipoPersonaggio tipo : TipoPersonaggio.values()) {
            if (!tipo.isDellaCategoria(CategoriaPersonaggio.NON_MORTO)) {
                continue;
            }
            controllati++;
            Personaggio p = crea(tipo);
            assertTrue(p.isImmuneAIncantesimo(ClasseIncantesimo.MORTE), tipo.name());
            assertTrue(p.isImmuneAIncantesimo(ClasseIncantesimo.VELENO), tipo.name());
            assertTrue(p.isImmuneATipoDanno(TipoDanno.VELENO), tipo.name());
            assertTrue(p.isImmuneAEffetto(TipoEffettoDiStato.SANGUINAMENTO), tipo.name());
            assertTrue(p.isImmuneAEffetto(TipoEffettoDiStato.INFETTATO), tipo.name());
            assertEquals(2.0d, p.getMoltiplicatoreDannoSubito(TipoDanno.SACRO), 1e-9, tipo.name());
            assertEquals(1.0d, p.getMoltiplicatoreDannoSubito(TipoDanno.FUOCO), 1e-9, tipo.name());
            assertFalse(p.isImmuneAIncantesimo(ClasseIncantesimo.FUOCO), tipo.name());
        }
        assertEquals(6, controllati);
    }

    @Test
    void chiNonEUnNonMortoNonHaNienteDiTutto() {
        for (TipoPersonaggio tipo : new TipoPersonaggio[] {TipoPersonaggio.GOBLIN, TipoPersonaggio.STREGA, TipoPersonaggio.DRAGO}) {
            Personaggio p = crea(tipo);
            assertFalse(p.isImmuneAIncantesimo(ClasseIncantesimo.MORTE), tipo.name());
            assertFalse(p.isImmuneAEffetto(TipoEffettoDiStato.SANGUINAMENTO), tipo.name());
            assertEquals(1.0d, p.getMoltiplicatoreDannoSubito(TipoDanno.SACRO), 1e-9, tipo.name());
        }
        // Il Drago ha le sue immunità e basta
        assertTrue(crea(TipoPersonaggio.DRAGO).isImmuneATipoDanno(TipoDanno.FUOCO));
        assertFalse(crea(TipoPersonaggio.DRAGO).isImmuneATipoDanno(TipoDanno.VELENO));
    }

    @Test
    void ognunoHaAncheLeSueImmunita() {
        // Scheletro e Lich: non si avvelenano e non si spaventano; solo il Lich non si silenzia
        for (TipoPersonaggio tipo : new TipoPersonaggio[] {TipoPersonaggio.SCHELETRO, TipoPersonaggio.LICH}) {
            Personaggio p = crea(tipo);
            assertTrue(p.isImmuneAEffetto(TipoEffettoDiStato.AVVELENATO), tipo.name());
            assertTrue(p.isImmuneAEffetto(TipoEffettoDiStato.SPAVENTATO), tipo.name());
            assertFalse(p.isImmuneATipoDanno(TipoDanno.ARIA), tipo.name());
        }
        assertTrue(crea(TipoPersonaggio.LICH).isImmuneAEffetto(TipoEffettoDiStato.SILENZIATO));
        assertFalse(crea(TipoPersonaggio.SCHELETRO).isImmuneAEffetto(TipoEffettoDiStato.SILENZIATO));
        // I fantasmi non si fanno atterrare e non sentono aria e terra
        for (TipoPersonaggio tipo : new TipoPersonaggio[] {TipoPersonaggio.FANTASMA, TipoPersonaggio.OMBRA_NERA,
                TipoPersonaggio.SPETTRO, TipoPersonaggio.SPIRITO}) {
            Personaggio p = crea(tipo);
            assertTrue(p.isImmuneAEffetto(TipoEffettoDiStato.ATTERRATO), tipo.name());
            assertTrue(p.isImmuneATipoDanno(TipoDanno.ARIA) && p.isImmuneATipoDanno(TipoDanno.TERRA), tipo.name());
            assertTrue(p.isImmuneAIncantesimo(ClasseIncantesimo.ARIA) && p.isImmuneAIncantesimo(ClasseIncantesimo.TERRA), tipo.name());
            assertFalse(p.isImmuneAEffetto(TipoEffettoDiStato.AVVELENATO), tipo.name());
        }
    }
}
