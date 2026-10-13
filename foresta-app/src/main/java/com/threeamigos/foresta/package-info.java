/**
 * L'applicazione: avvio del gioco e ascolto degli eventi a scopo diagnostico.
 * <p>
 * Unico modulo che dipende sia dal motore sia dall'interfaccia grafica ({@code foresta-swing}); contiene anche i test di
 * integrazione, che li fanno lavorare insieme.
 * <ul>
 * <li>{@link com.threeamigos.foresta.Main}: l'avvio e il collegamento fra motore e interfaccia;</li>
 * <li>{@link com.threeamigos.foresta.SnifferBusEventi}: scrive nel log gli eventi del bus.</li>
 * </ul>
 */
package com.threeamigos.foresta;
