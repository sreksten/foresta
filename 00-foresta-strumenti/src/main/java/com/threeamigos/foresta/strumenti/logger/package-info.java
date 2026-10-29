/**
 * Il registro del gioco: {@link com.threeamigos.foresta.strumenti.logger.Logger} manda i messaggi alla
 * {@link com.threeamigos.foresta.strumenti.logger.DestinazioneDiLog} installata. Quella predefinita del desktop
 * ({@link com.threeamigos.foresta.strumenti.logger.DestinazioneConsoleEFile}) scrive sulla console e, se si indica una cartella con
 * {@code -Dforesta.log=cartella}, anche su un file per sessione; una piattaforma ne può installare una sua.
 * <p>
 * Un testo che costa costruirlo si passa come fornitore, {@code Logger.log(() -> "...")}, così non si costruisce se il registro è spento.
 */
package com.threeamigos.foresta.strumenti.logger;
