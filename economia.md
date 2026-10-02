# Bilancio dell'economia di Foresta

> Stato al 2026-10-02. Numeri verificati nel codice; quelli per livello vengono da un modello con le ipotesi in fondo.

Quasi tutte le monete del gioco vengono dalla **rivendita all'armaiolo degli artefatti trovati per terra** alla prima visita di una cella. Finché si esplorano celle nuove le entrate crescono col livello e coprono negozi e fusioni. Ma la mappa si esaurisce verso il **livello 13**, e da lì le entrate si fermano mentre i prezzi continuano a salire. Nemici e cofani danno pochissime monete.

## Entrate

Gli oggetti per terra compaiono solo alla prima visita di una cella e non ricompaiono (`LocazioneBase`). I nemici sconfitti danno esperienza ma nessuna moneta. Le monete del mondo sono quindi finite: circa 300 dirette, più quel che rende la rivendita degli artefatti trovati.

| Fonte | Quanto | Quante volte | Totale sulla mappa (stima) |
| --- | --- | --- | ---: |
| Monete iniziali | 100 monete + 5 preziosi | una volta | 105 |
| Artefatti per terra, rivenduti all'armaiolo | ~60% di 5 + 5 × livello | 6 celle di bosco su 11, ~172 in tutto | ~2.500 fino al livello 10 |
| Monete per terra | 1-2 | 1 cella di bosco su 11 | ~45 |
| Pietre preziose e corone per terra | 1-2 preziosi | 2 celle di bosco su 11 | ~85 |
| Cofano fuori dai castelli | 5-10 monete o 5-10 preziosi, 1/8 ciascuno | bosco 1/11, rovine sempre, grotte 1-2 | ~0,84 monete + 0,84 preziosi a cofano |
| Cofano nei castelli | 5-10 monete, 1/6 | 5 per castello, 4 castelli | ~22 |
| Compagno reclutato in locanda | 5-15 monete + 0-10 preziosi | fino a 4 volte | ~40 + ~20 preziosi |
| Missioni (Medaglione, Derrate alimentari) | 20 monete | 2 | 40 |
| Aiuto del mercenario (offerta dopo corruzione o amicizia) | 1-6 monete | di rado | trascurabile |

- **Preziosi:** entrando in una città si vendono tutti da soli, 1 moneta l'uno, da 1 a 2 se nel gruppo c'è un ladro vivo (`GruppoGiocatore.vendePreziosi`). Non servono ad altro.
- **Vendita:** l'armaiolo paga dal 50 al 75% del prezzo secondo la Contrattazione, di solito circa il 60% (`RegoleContrattazione`). Ricompra sempre a meno di quanto vende, quindi comprare e rivendere non rende mai.
- **Non danno monete:** nemici sconfitti, trofei, corruzione riuscita, anelli non magici.

## Uscite

Gli acquisti nei negozi hanno uno sconto dovuto alla Contrattazione, di solito l'8-10%. Locande, offerte e corruzione non hanno sconto.

| Spesa | Prezzo | Note |
| --- | --- | --- |
| Pasto in locanda | 5 a personaggio vivo | **automatico** a ogni visita; per entrare servono almeno 5 monete |
| Pernottamento | 5 a personaggio vivo | facoltativo |
| Informazioni in locanda | gratis | al massimo 3 per locanda |
| Pozione della salute / grande | 5 / 15 | alchimista |
| Pozione della magia / grande | 10 / 30 | alchimista |
| Aumento della magia massima | 10 per uno; 10 + 7,5 × (n − 1) per il gruppo | alchimista |
| Pergamena d'incantesimo | 5; Fulmine e Resurrezione 15 | alchimista |
| Mappa di una zona / di tutta la foresta | 10 / 20 | alchimista; la mappa intera anche come offerta |
| Artefatto dell'armaiolo | 5 + 5 × livello, spadone × 1,5 | più il listino degli effetti se incantato |
| Ingrediente magico | 2-180 secondo tipo e livello | listino nuovo, vedi la tabella sotto |
| Fusione | 10 + 5 per effetto trasferito | incantatore |
| Corruzione | 2 × posti del gruppo | riesce il 70% delle volte; se fallisce non costa niente |
| Incantesimi offerti | quantità × prezzo / 2 | offerta dopo corruzione o amicizia |

Perdite:
- **amicizia fallita:** 1 volta su 5 si perdono 1-5 monete, 1 su 5 1-5 preziosi;
- **fuga:** si perde a caso fino a metà di monete, preziosi, pergamene e pozioni.

Prezzo medio di un ingrediente magico col listino nuovo (misurato su 2000 ingredienti per tipo e livello):

| Livello | Pergamena | Gingillo | Sigillo | Gemma | Monile |
| ---: | ---: | ---: | ---: | ---: | ---: |
| 1 | 4 | 8 | 7 | 2 | 2 |
| 2 | 7 | 16 | 14 | 8 | 8 |
| 3 | 23 | 51 | 43 | 38 | 36 |
| 4 | 38 | 80 | 67 | 75 | 72 |
| 6 | 38 | 79 | 68 | 112 | 109 |
| 8 | 38 | 80 | 68 | 148 | 144 |
| 10 | 38 | 81 | 68 | 184 | 179 |

## Per livello del mondo

Il livello sale solo con l'esperienza: per passare dal livello L al L + 1 servono 100 × (2L − 1) punti. Con circa 45 punti a scontro servono 2 scontri per arrivare al livello 2, una ventina per il 4, circa 220 per il 10.

La tabella mette a confronto le entrate di un gruppo di 4 che fa ogni scontro in una cella nuova con tre spese tipiche:
- i pasti;
- un pezzo d'equipaggiamento nuovo a testa;
- un ingrediente medio con la sua fusione.

| Livello | Scontri per salire | Scontri in tutto | Entrate nel livello | Pasti | Un pezzo a testa | Ingrediente + fusione |
| ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| 1 | 2 | 2 | 8 | 7 | 40 | 15 |
| 2 | 7 | 9 | 33 | 22 | 60 | 26 |
| 3 | 11 | 20 | 71 | 37 | 80 | 58 |
| 4 | 16 | 36 | 122 | 52 | 100 | 91 |
| 5 | 20 | 56 | 186 | 67 | 120 | 95 |
| 6 | 24 | 80 | 262 | 81 | 140 | 106 |
| 7 | 29 | 109 | 351 | 96 | 160 | 113 |
| 8 | 33 | 142 | 453 | 111 | 180 | 120 |
| 9 | 38 | 180 | 568 | 126 | 200 | 127 |
| 10 | 42 | 222 | 695 | 141 | 220 | 135 |

Le celle con qualcosa da trovare sono circa 365 (bosco, rovine, grotte, paludi): con questo ritmo finiscono al **livello 13**, dopo circa 376 scontri.

## Che cosa non torna

1. **Le entrate dipendono dall'esplorazione, non dal livello.** Un gruppo che combatte in celle già visitate (i mostri ricompaiono) guadagna esperienza ma nemmeno una moneta. Esauriti la mappa, o le celle vicine, i soldi si fermano mentre i prezzi salgono.
2. **L'inizio è stretto.** Ai livelli 1 e 2 entrano circa 40 monete oltre alle 100 iniziali. I pasti obbligati ne costano già 30, e un'arma nuova per tutti 40-60.
3. **Dal livello 4, esplorando, i soldi bastano.** Al livello 4 entrano 122 monete contro 52 di pasti, e resta abbastanza per un ingrediente con la sua fusione o per metà dell'equipaggiamento nuovo. Al livello 10 entrano 695 contro circa 500 di spese tipiche. Il listino nuovo degli ingredienti non è troppo caro, ma solo finché si esplora.
4. **Una sola fonte vera.** Quasi tutto il guadagno viene dagli artefatti per terra. Chi non li raccoglie, o non può portarli per il peso, resta senza soldi.
5. **I preziosi non contano:** valgono 1 moneta a qualunque livello, e una cella di bosco ne dà in media 0,35.
6. **I nemici non lasciano niente.** È il motivo per cui l'esperienza è infinita e le monete no.

## Proposte

In ordine di efficacia:

1. **Bottino dei nemici.** Ogni nemico sconfitto lascia monete in proporzione alla sua esperienza, per esempio esperienza / 5: un goblin 3, un troll 8, un drago 50. Le entrate diventano proporzionali al combattimento, quindi crescono col livello e non finiscono, e i mostri che ricompaiono smettono di essere un problema. Con 45 punti a scontro sono circa 9 monete a scontro: al livello 4 aggiungono circa 140 monete, al 10 circa 380.
2. **Preziosi che valgono col livello.** Un prezioso venduto vale quanto il livello del mondo, invece di 1 moneta. Le pietre preziose diventano una fonte vera e seguono i prezzi.
3. **Missioni pagate col livello**, per esempio 20 × livello invece di 20 fisse.
4. **Oggetti che ricompaiono di rado** nelle celle già visitate, per esempio un cofano ogni tanto dopo qualche livello. Questo allunga la vita della mappa oltre il livello 13.
5. **Prezzi:** tenere il listino nuovo degli ingredienti con le costanti a 0,5 e ritararlo dopo aver deciso le entrate. Se si aggiunge il bottino, le entrate salgono di circa il 50-100%, e allora si possono alzare un po' anche i prezzi.
6. **Inizio:** il primo livello conviene ammorbidirlo, con più monete iniziali (150) o pasti meno cari ai primi livelli.

Il bottino dei nemici (1) risolve da solo i punti 1, 4 e 6 di "Che cosa non torna", e lo si misura col simulatore già esistente (`TestMonteCarloMatrix`).

## Ipotesi del modello

Misurato o letto nel codice:
- prezzi e probabilità delle due tabelle "Entrate" e "Uscite";
- prezzi degli ingredienti;
- formula del livello;
- composizione della mappa: 400 celle, di cui circa 315 di bosco, 20 rovine, 10 grotte, 20 paludi.

Stimato:
- circa 45 punti d'esperienza a scontro (1-2 mostri da 15-40 punti, incontro nel 90% dei casi);
- ogni scontro in una cella nuova;
- gli artefatti trovati si rivendono tutti al 60% del prezzo base, senza contare gli incantamenti;
- un pasto per un gruppo di 4 ogni 6 scontri;
- nessun ladro nel gruppo.

Con meno esplorazione le entrate scendono in proporzione.
