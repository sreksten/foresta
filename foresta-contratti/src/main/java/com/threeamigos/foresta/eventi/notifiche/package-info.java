/**
 * Gli eventi che vanno dal motore alla UI per dire che qualcosa è successo: un testo da mostrare, un attributo cambiato, un
 * artefatto trovato, una missione aggiornata, il gioco finito.
 * <p>
 * Le operazioni economiche e di equipaggiamento seguono il pattern richiesta e verdetto: il comando della UI non sposta nulla,
 * il motore verifica e pubblica una notifica di approvazione o di rifiuto (con il motivo), e solo dopo l'approvazione l'oggetto si sposta.
 */
package com.threeamigos.foresta.eventi.notifiche;
