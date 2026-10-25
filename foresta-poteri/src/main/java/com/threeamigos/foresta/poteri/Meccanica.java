package com.threeamigos.foresta.poteri;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * La meccanica dello scontro con cui i poteri si esercitano. La installa chi fa partire il gioco; se nessuno l'ha fatto, si cerca fra i
 * servizi {@link MeccanicaDeiPoteri} presenti (un modulo che la implementa si dichiara in
 * {@code META-INF/services/com.threeamigos.foresta.poteri.MeccanicaDeiPoteri}).
 *
 * @author Stefano Reksten
 */
public final class Meccanica {

    private static volatile MeccanicaDeiPoteri installata;

    private Meccanica() {
    }

    /**
     * Fa usare ai poteri quella meccanica; null torna a cercarla fra i servizi.
     */
    public static void installa(MeccanicaDeiPoteri meccanica) {
        installata = meccanica;
    }

    /**
     * La meccanica con cui i poteri si esercitano.
     *
     * @throws IllegalStateException se nessuno l'ha installata e non c'è un servizio che la fornisca
     */
    public static MeccanicaDeiPoteri get() {
        MeccanicaDeiPoteri meccanica = installata;
        if (meccanica == null) {
            Iterator<MeccanicaDeiPoteri> servizi = ServiceLoader.load(MeccanicaDeiPoteri.class).iterator();
            if (!servizi.hasNext()) {
                throw new IllegalStateException("Nessuno ha detto ai poteri con quale meccanica esercitarsi (vedi Meccanica.installa)");
            }
            meccanica = servizi.next();
            installata = meccanica;
        }
        return meccanica;
    }
}
