# Bilancio dell'economia di Foresta

> Stato al 2026-10-07. Numeri verificati nel codice; quelli per livello vengono da un modello con le ipotesi in fondo. Le decisioni prese per dare più monete col livello (più monete iniziali, preziosi e missioni che valgono col livello) sono nella sezione "Decisioni".

Le monete del gioco vengono dalla **rivendita all'armaiolo degli artefatti trovati per terra** alla prima visita di una cella, dai **preziosi** (che valgono quanto il livello del mondo) e dalle **missioni** (che pagano per il livello). Finché si esplorano celle nuove le entrate crescono col livello e coprono negozi e fusioni, così chi vuole provare a costruire armi nuove non resta senza soldi. Ma la mappa si esaurisce verso il **livello 13**, e da lì le entrate dalle celle si fermano mentre i prezzi continuano a salire. Nemici e cofani danno pochissime monete, e i nemici non ne lasciano per scelta: scheletri, spettri e simili non hanno niente da lasciare.

## Entrate

Gli oggetti per terra compaiono solo alla prima visita di una cella e non ricompaiono (`LocazioneBase`). I nemici sconfitti danno esperienza ma nessuna moneta. Le monete del mondo sono quindi finite: circa 1.500 dirette (la maggior parte dai preziosi, che valgono il livello di quando si vendono), più quel che rende la rivendita degli artefatti trovati e le missioni.

| Fonte | Quanto | Quante volte | Totale sulla mappa (stima) |
| --- | --- | --- | ---: |
| Monete iniziali | 150 monete + 5 preziosi (`Costanti.MONETE_INIZIALI`, `PREZIOSI_INIZIALI`) | una volta | 155 |
| Artefatti per terra, rivenduti all'armaiolo | ~60% di 5 + 5 × livello | 6 celle di bosco su 11, ~172 in tutto | ~2.500 fino al livello 10 |
| Monete per terra | 1-2 | 1 cella di bosco su 11 | ~45 |
| Pietre preziose e corone per terra | 1-2 preziosi, ognuno vale il livello del mondo quando si vende | 2 celle di bosco su 11 | ~1.200 contando anche quelli dei cofani, secondo il modello (circa 0,35 a cella, al livello di quando si vendono); il giocatore automatico ne trova di più, circa uno a scontro (vedi "Misure col giocatore automatico") |
| Cofano fuori dai castelli | 5-10 monete o 5-10 preziosi, 1/8 ciascuno | bosco 1/11, rovine sempre, grotte 1-2 | ~0,84 monete + 0,84 preziosi a cofano |
| Cofano nei castelli | 5-10 monete, 1/6 | 5 per castello, 4 castelli | ~22 |
| Artefatto o pergamena da un cofano | un artefatto casuale (10%) o un ingrediente magico (10%) a cofano, al livello del mondo | ogni cofano aperto | da rivendere o usare: 1 cofano su 5 |
| Compagno reclutato in locanda | 5-15 monete + 0-10 preziosi | fino a 4 volte | ~40 + ~20 preziosi |
| Missioni (gli incarichi in città, le storie di Fleena e Ruuna, il pianista) | la paga base della missione per il livello del mondo di quando si offre (20 al primo livello e 200 al decimo per le due storie) | le due storie una volta, gli incarichi senza città fissa si ripetono | cresce col livello |
| La sfida dei campioni (vedi [`carta_forbici_sasso.md`](carta_forbici_sasso.md)) | 100 monete per il livello del mondo di quando parte, più un leggendario | una volta, dopo tre sfide a carta, forbici e sasso vinte | ~100 × livello; non misurata col giocatore automatico |
| Aiuto del mercenario (offerta dopo corruzione o amicizia) | 1-6 monete | di rado | trascurabile |

- **Preziosi:** entrando in una città si vendono tutti da soli (`GruppoGiocatore.vendePreziosi`), ognuno per quanto vale il livello del mondo in quel momento: 1 moneta al primo livello, 6 al sesto. Con un ladro vivo nel gruppo se ne ricava in media la metà in più. Non servono ad altro.
- **Vendita:** l'armaiolo paga dal 50 al 75% del prezzo secondo la Contrattazione, di solito circa il 60% (`RegoleContrattazione`). Ricompra sempre a meno di quanto vende, quindi comprare e rivendere non rende mai.
- **Non danno monete:** nemici sconfitti (per scelta), trofei, corruzione riuscita, anelli non magici.

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

Prezzo medio di un ingrediente magico col listino nuovo, che si lascia com'è (misurato su 2000 ingredienti per tipo e livello):

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

La tabella mette a confronto le entrate di un gruppo di 4 che fa ogni scontro in una cella nuova (con i preziosi venduti al livello a cui si trovano) con tre spese tipiche:
- i pasti;
- un pezzo d'equipaggiamento nuovo a testa;
- un ingrediente medio con la sua fusione.

Le missioni non sono nel conto, e le monete iniziali (150) si aggiungono alle entrate del primo livello. **È un limite alto**: suppone di esplorare ogni cella e di rivendere ogni artefatto, e le misure col giocatore automatico (sezione seguente) danno 2-3 volte meno ai livelli 3-5.

| Livello | Scontri per salire | Scontri in tutto | Entrate nel livello | Pasti | Un pezzo a testa | Ingrediente + fusione |
| ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| 1 | 2 | 2 | 8 | 7 | 40 | 15 |
| 2 | 7 | 9 | 35 | 22 | 60 | 26 |
| 3 | 11 | 20 | 79 | 37 | 80 | 58 |
| 4 | 16 | 36 | 139 | 52 | 100 | 91 |
| 5 | 20 | 56 | 214 | 67 | 120 | 95 |
| 6 | 24 | 80 | 304 | 81 | 140 | 106 |
| 7 | 29 | 109 | 412 | 96 | 160 | 113 |
| 8 | 33 | 142 | 534 | 111 | 180 | 120 |
| 9 | 38 | 180 | 674 | 126 | 200 | 127 |
| 10 | 42 | 222 | 827 | 141 | 220 | 135 |

Rispetto al modello di prima (695 entrate al livello 10) i preziosi aggiungono 132 al livello 10 e 17 al 4: pesano circa un sesto delle entrate dal livello 8 in poi.

Le celle con qualcosa da trovare sono circa 365 (bosco, rovine, grotte, paludi): con questo ritmo finiscono al **livello 13**, dopo circa 376 scontri.

## Misure col giocatore automatico

`SimulazionePartiteTest` gioca partite intere (senza la modalità di prova) con un giocatore automatico che esplora, combatte, compra e vende; ora stampa anche le monete guadagnate per livello del mondo e per luogo, con le spese e le monete che restano. Si rilancia con `mvn test -Dtest=SimulazionePartiteTest#simulaPartite -Djunit.jupiter.conditions.deactivate='org.junit.*DisabledCondition'` (30 partite per classe, circa 25 secondi; `PARTITE_PER_CLASSE` le cambia).

Le misure qui sotto sono di 100 partite per ognuna delle 5 classi (500 in tutto), con gli stessi semi, sul codice prima (`b763fde`) e dopo (`dc487db`) la decisione di far valere col livello preziosi e missioni e di partire con 150 monete. Due lanci identici danno risultati che differiscono del 5-7%: sotto il 10% non si può dire niente.

| Per partita | Prima | Dopo |
| --- | ---: | ---: |
| Monete guadagnate | 95-102 | 147-151 |
| Monete spese | 176-183 | 265-271 |
| Monete spese nei negozi (guerriero) | 58 | 86 |
| Monete a fine partita | 18 | 30 |
| Vendite all'armaiolo | 30-34 | 30 |
| Preziosi venduti | 20 | 21 |

| Livello | Partite | Entrate prima | Entrate dopo |
| ---: | ---: | ---: | ---: |
| 1 | 500 | 35 | 38 |
| 2 | 426 | 35 | 54 |
| 3 | ~300 | 30 | 48 |
| 4 | ~140 | 38 | 53 |
| 5 | ~55 | 47 | 73 |
| 6 | 20 | 57 | 113 |

Cosa dicono:
- **Le entrate salgono di circa la metà**, e di più col livello: +55% al 2, +90% al 5. Anche le spese nei negozi salgono di circa la metà, quindi il giocatore ora può permettersi di più.
- **Il guadagno in più viene soprattutto dai preziosi.** Se ne vendono quanti prima (circa 20 a partita: il giocatore ne trova circa uno a scontro, molti più dei 0,35 a cella del modello) ma ora valgono il livello. Le vendite all'armaiolo restano circa 30 a partita, un quinto delle entrate, e non c'entrano con le decisioni. I conti per luogo (città, locanda, altrove) si attribuiscono al luogo in cui il gruppo era prima del comando, quindi la vendita dei preziosi all'ingresso in città finisce in "altrove".
- **Il modello di "Per livello del mondo" è un limite alto.** Per i livelli 3, 4 e 5 dà 79, 139 e 214 monete; il giocatore automatico ne fa 48, 53 e 73.

Limiti delle misure:
- **I bot muoiono tutti**, a livello 2-4 (il mago 4-5), con o senza le monete in più: non vivono più a lungo, e oltre il livello 5 le partite sono poche (20 al livello 6, 5-10 ai successivi). Non si sa quindi come vanno le entrate ai livelli alti, che sono quelli che preoccupavano.
- **Non fanno le missioni**, quindi la parte pagata col livello non è misurata.
- **Non provano a costruire armi nuove**: comprano pozioni, pergamene e la mappa, e rivendono gli artefatti. Quanto "sperimentare" costi lo dice il listino, non questa simulazione.

## Che cosa non torna

1. **Le entrate dalle celle dipendono dall'esplorazione, non dal livello.** Un gruppo che combatte in celle già visitate (i mostri ricompaiono) guadagna esperienza ma nemmeno una moneta. Esaurita la mappa, o le celle vicine, le entrate dalle celle si fermano mentre i prezzi salgono: restano le missioni, che si ripetono, e i preziosi che si portano dietro.
2. **L'inizio resta il punto più stretto.** Ai livelli 1 e 2 entrano circa 43 monete oltre alle 150 iniziali. I pasti obbligati ne costano già 30, e un'arma nuova per tutti 40-60: bastano per un paio di acquisti, non per sperimentare molto.
3. **Dal livello 4, esplorando, i soldi bastano.** Secondo il modello, al livello 4 entrano 139 monete contro 52 di pasti (il giocatore automatico ne fa 53, ma non esplora tutto), e resta abbastanza per un ingrediente con la sua fusione o per l'equipaggiamento nuovo. Al livello 10 entrano 827 contro circa 500 di spese tipiche. Il listino nuovo degli ingredienti non è troppo caro, ma solo finché si esplora.
4. **Le fonti sono poche.** Per il modello quasi tutto viene dagli artefatti per terra, ma le misure dicono altro: il giocatore automatico ricava circa un quinto dalle vendite all'armaiolo, e il resto da preziosi, compagni reclutati e monete trovate. Chi non raccoglie gli artefatti, o non può portarli per il peso, vive di preziosi e missioni.

## Decisioni

Cosa si è deciso, partendo dall'elenco di proposte fatto a suo tempo, per far arrivare più monete col livello senza toccare i prezzi dei negozi e delle locande (pasti e pernottamenti restano a 5: con più monete non sono più un problema):

- **Preziosi che valgono col livello (fatto).** Un prezioso venduto vale quanto il livello del mondo, invece di 1 moneta (`GruppoGiocatore.vendePreziosi`). Le pietre preziose e le corone seguono i prezzi che salgono col livello. Un test misura il valore al livello 1 e al 6.
- **Missioni pagate col livello (fatto).** Un incarico paga la sua paga base, quella che la missione scrive, per il livello del mondo di quando si è offerto: 20 × livello per il medaglione e le derrate, e allo stesso modo tutti gli altri (`IncaricoInCitta.getRicompensa`; il pianista lo fa da sé). La cifra si fissa all'offerta, così quella detta nelle scene e quella pagata sono la stessa anche se il livello sale nel frattempo.
- **Prezzi (invariati).** Il listino nuovo degli ingredienti resta con le costanti a 0,5. Si potrà ritararlo dopo aver misurato le nuove entrate.
- **Inizio (fatto).** Più monete iniziali, 150 invece di 100 (`Costanti.MONETE_INIZIALI`); i pasti restano a 5.
- **Bottino dei nemici (non fatto, per scelta).** I nemici non lasciano monete: scheletri, spettri e simili non hanno niente da lasciare, e un bottino solo per alcuni sarebbe incoerente. L'esperienza resta infinita e le monete no, ma ora c'è altro che cresce col livello.

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
- i preziosi (circa 0,35 a cella, ma il giocatore automatico ne trova circa uno a scontro) si vendono al livello a cui si trovano;
- nessun ladro nel gruppo.

Con meno esplorazione le entrate scendono in proporzione.

## Da fare / idee aperte

- **Misurare di più.** Le misure di "Misure col giocatore automatico" arrivano fino al livello 5. Per vedere le entrate ai livelli alti il giocatore automatico dovrebbe vivere più a lungo (o giocare partite in cui non muore), e per misurare le missioni dovrebbe farle. Poi, se servono più soldi o se si vuole sperimentare di più con le fusioni, ritarare i prezzi degli ingredienti (le costanti a 0,5). `TestMonteCarloMatrix` non c'entra: simula solo i combattimenti.
- **Oggetti che ricompaiono di rado** nelle celle già visitate, per esempio un cofano ogni tanto dopo qualche livello: allungherebbe la vita della mappa oltre il livello 13, quando le entrate dalle celle si fermano. Per ora non serve, perché preziosi e missioni crescono col livello.
