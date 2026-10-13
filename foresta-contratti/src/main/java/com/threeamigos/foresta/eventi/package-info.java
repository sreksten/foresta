/**
 * Il bus degli eventi, con cui motore e interfaccia si parlano senza conoscersi.
 * <p>
 * {@link com.threeamigos.foresta.eventi.BusEventi} è un publish/subscribe sincrono: chi vuole sapere cosa succede si iscrive a un
 * tipo di evento con {@code iscriviti}, chi ha qualcosa da dire lo {@code pubblica}. Ogni evento estende
 * {@link com.threeamigos.foresta.eventi.EventoBase} e ha un {@link com.threeamigos.foresta.eventi.TipoEvento}.
 * <p>
 * Gli eventi sono divisi per direzione e intento nei sottopacchetti:
 * <ul>
 * <li>{@code comandigiocatore}: dalla UI al motore (il giocatore ha scelto qualcosa);</li>
 * <li>{@code notifiche}: dal motore alla UI, per dire che qualcosa è successo;</li>
 * <li>{@code richieste}: dal motore alla UI, per chiedere un input o l'apertura di una schermata;</li>
 * <li>{@code interni}: eventi tecnici, non rivolti al giocatore.</li>
 * </ul>
 * Gli eventi non portano classi del dominio, ma solo viste in sola lettura, identificativi e dati: così l'interfaccia non può
 * modificare lo stato del gioco se non attraverso un comando.
 */
package com.threeamigos.foresta.eventi;
