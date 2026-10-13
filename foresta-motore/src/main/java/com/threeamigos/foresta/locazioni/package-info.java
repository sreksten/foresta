/**
 * Le locazioni: i posti della Foresta in cui il gruppo si trova.
 * <p>
 * Una {@link com.threeamigos.foresta.locazioni.Locazione} è un piccolo automa a stati: quando il gruppo entra si creano mostri e oggetti,
 * poi il gruppo agisce (combatte, formula incantesimi, tenta amicizia o corruzione) finché la locazione non è finita.
 * {@link com.threeamigos.foresta.locazioni.LocazioneBase} ne contiene la parte comune, e le classi concrete aggiungono ciò che le distingue: chi
 * vi si incontra, che cosa vi si trova, come si descrive e come ci si riposa.
 * <p>
 * Le locazioni sono di tre generi (vedi {@code CategoriaLocazione}): le <b>ordinarie</b> (bosco, radura, palude, grotta, rovine, tempio, locanda),
 * le <b>città</b> e i <b>castelli</b> dei boss, che sono {@link com.threeamigos.foresta.locazioni.LocazioneUnica uniche}. L'istanza di una
 * locazione vive quanto la visita; ciò che deve restare (nome, se è stata visitata o completata) sta nel {@code LocazioneMD} della casella.
 * Le istanze si costruiscono con {@link com.threeamigos.foresta.locazioni.FabbricaLocazioni}.
 */
package com.threeamigos.foresta.locazioni;
