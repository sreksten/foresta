package com.threeamigos.foresta.tipi;

import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;

import static com.threeamigos.foresta.tipi.TipoInterazioneConEffettiDiStato.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * I moltiplicatori del danno delle interazioni con gli stati stanno nell'enum (e non come numeri nel calcolo del danno):
 * questi sono quelli che c'erano nel calcolo prima di spostarli.
 */
class MoltiplicatoriDelleInterazioniTest {

    @Test
    void ogniInterazioneHaIlSuoMoltiplicatore() {
        Map<TipoInterazioneConEffettiDiStato, Double> attesi = new EnumMap<>(TipoInterazioneConEffettiDiStato.class);
        attesi.put(ELETTROCUZIONE, 1.5);
        attesi.put(VAPORIZZAZIONE, 0.5);
        attesi.put(ALIMENTAZIONE_FIAMMA, 1.5);
        attesi.put(SCIOGLIMENTO_TERMICO, 1.5);
        attesi.put(ESPLOSIONE_DI_GAS, 1.3);
        attesi.put(VAMPATA_TOSSICA, 1.3);
        attesi.put(REAZIONE_TOSSICA, 1.15);
        attesi.put(DILUIZIONE_EMATICA, 1.05);
        attesi.put(COAGULAZIONE_FORZATA, 1.05);
        attesi.put(MIETITURA, 2.0);
        attesi.put(DISGELO_VIOLENTO, 1.5);
        attesi.put(SUPERCONDUZIONE, 1.3);
        attesi.put(PURIFICAZIONE, 1.5);
        attesi.put(SCHIACCIAMENTO, 2.0);
        attesi.put(INCIAMPO, 1.3);
        attesi.put(INCENDIO_LIBERATORIO, 1.5);
        attesi.put(IMPATTO_RIGIDO, 1.3);
        attesi.put(SHOCK_DI_REALTA, 1.2);
        attesi.put(FOLLIA_COSMICA, 1.5);
        attesi.put(DISPERSIONE, 1.05);
        attesi.put(FANGO, 1.05);
        attesi.put(DISORIENTAMENTO, 1.05);
        attesi.put(ISOLAMENTO_SENSORIALE, 1.5);
        for (TipoInterazioneConEffettiDiStato interazione : TipoInterazioneConEffettiDiStato.values()) {
            assertEquals(attesi.getOrDefault(interazione, 1.0), interazione.getMoltiplicatore(), 1e-9, interazione.name());
        }
    }
}
