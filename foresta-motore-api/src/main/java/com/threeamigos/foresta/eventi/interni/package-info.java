/**
 * Gli eventi tecnici, non rivolti al giocatore: la fase di gioco che la UI deve mostrare, gli aggiornamenti dei comandi disponibili, gli
 * errori, i comandi che il motore manda a sé stesso e la preparazione e la fine di una locazione.
 * <p>
 * Alcuni di questi eventi portano classi del dominio (le parti di uno scambio, il banco di lavoro, i personaggi) e per questo
 * stanno nel modulo del motore, non qui: il pacchetto è lo stesso, ma il modulo è diverso. Altri portano sprite e fumetti, e stanno
 * nel modulo dell'interfaccia.
 */
package com.threeamigos.foresta.eventi.interni;
