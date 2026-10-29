package com.threeamigos.foresta.modellodati;

import com.threeamigos.foresta.canti.CantiImparati;
import com.threeamigos.foresta.economia.Cassa;
import com.threeamigos.foresta.economia.MagazziniDeiNegozi;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.intermezzi.RegistroIntermezzi;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.missioni.RegistroMissioni;
import com.threeamigos.foresta.mondo.RegistroArtefatti;
import com.threeamigos.foresta.mondo.RegistroPersonaggi;
import com.threeamigos.foresta.notizie.Notizie;
import com.threeamigos.foresta.statistiche.LineaTemporale;
import com.threeamigos.foresta.statistiche.Statistiche;
import com.threeamigos.foresta.tattiche.TatticheDeiPersonaggi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * {@link ModelloDati#installa()} dice a ogni parte del gioco dove sono i suoi dati: il fornitore di ciascuna deve dare il dato giusto del
 * modello dati in corso, anche dopo che il modello è stato sostituito (come quando si carica un salvataggio).
 */
class InstallazioneDeiFornitoriTest {

    private ModelloDati precedente;

    @BeforeEach
    void ricordaIlModelloInCorso() {
        precedente = ModelloDati.getIstanza();
    }

    @AfterEach
    void ripristinaIlModello() {
        ModelloDati.setIstanza(precedente);
    }

    // Per ogni parte, il dato che il suo fornitore deve dare
    private static Map<Class<?>, Function<ModelloDati, Object>> parti() {
        Map<Class<?>, Function<ModelloDati, Object>> parti = new LinkedHashMap<>();
        parti.put(Foresta.class, ModelloDati::getForestaMD);
        parti.put(GruppoGiocatore.class, ModelloDati::getGruppoGiocatoreMD);
        parti.put(TatticheDeiPersonaggi.class, ModelloDati::getTatticheMD);
        parti.put(CantiImparati.class, ModelloDati::getCantiImparatiMD);
        parti.put(RegistroIntermezzi.class, ModelloDati::getIntermezziMD);
        parti.put(Statistiche.class, ModelloDati::getStatisticheMD);
        parti.put(LineaTemporale.class, ModelloDati::getLineaTemporaleMD);
        parti.put(RegistroPersonaggi.class, ModelloDati::getRegistroPersonaggiMD);
        parti.put(RegistroArtefatti.class, ModelloDati::getRegistroArtefattiMD);
        parti.put(RegistroMissioni.class, ModelloDati::getRegistroMissioniMD);
        parti.put(MagazziniDeiNegozi.class, ModelloDati::getMagazziniMD);
        parti.put(Cassa.class, ModelloDati::getCassaMD);
        parti.put(Notizie.class, ModelloDati::getNotizieMD);
        return parti;
    }

    private static void verifica(ModelloDati atteso) throws Exception {
        for (Map.Entry<Class<?>, Function<ModelloDati, Object>> parte : parti().entrySet()) {
            Field campo = parte.getKey().getDeclaredField("fornitoreDiDati");
            campo.setAccessible(true);
            Supplier<?> fornitore = (Supplier<?>) campo.get(null);
            assertSame(parte.getValue().apply(atteso), fornitore.get(), parte.getKey().getSimpleName());
        }
    }

    @Test
    void ogniParteRiceveIlDatoDelModelloInCorso() throws Exception {
        ModelloDati.installa();

        ModelloDati uno = new ModelloDati();
        ModelloDati.setIstanza(uno);
        verifica(uno);

        ModelloDati due = new ModelloDati();
        ModelloDati.setIstanza(due);
        verifica(due);
    }

    @Test
    void installareDueVolteNonCambiaNiente() throws Exception {
        ModelloDati.installa();
        ModelloDati.installa();

        ModelloDati modello = new ModelloDati();
        ModelloDati.setIstanza(modello);
        verifica(modello);
    }
}
