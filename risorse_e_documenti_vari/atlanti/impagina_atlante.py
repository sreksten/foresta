#!/usr/bin/env python3
"""Propone l'impaginazione libera (MaxRects) di un atlante di immagini di misure diverse.

Uso: python3 impagina_atlante.py <cartella> [larghezza massima] [--fuori=nome,nome...]    (per esempio: oggetti)
--in-cima=nome,nome... mette quelle immagini nella fascia in alto (le altre sotto e negli spazi che restano accanto a loro)
--alias=nome:altro,... le immagini identiche ad altre (non entrano nell'atlante: ALIAS nel descrittore)
--fuori scrive nel descrittore i file che restano a parte nelle risorse (grandi, o usati uno alla volta), che non sono in sorgenti-immagini
Legge le immagini ORIGINALI di 12-foresta-ui-comune/sorgenti-immagini/<cartella>/ (che non finiscono nei jar: non si buttano via).
Prova varie larghezze, sceglie quella che occupa meno pixel e scrive img/<cartella>/atlante_<cartella>.txt con il formato LIBERO
(vedi costruisci_atlante.py). Se il descrittore esiste già lo SOVRASCRIVE: serve solo per partire; poi si ritocca a mano.
"""
import os
import sys
from PIL import Image

if len(sys.argv) < 2:
    sys.exit(__doc__)
NOME = sys.argv[1]
ARGOMENTI = [a for a in sys.argv[2:] if not a.startswith('--')]
IN_CIMA = [n for a in sys.argv[2:] if a.startswith('--in-cima=') for n in a[len('--in-cima='):].split(',') if n]
ALIAS = [tuple(c.split(':')) for a in sys.argv[2:] if a.startswith('--alias=') for c in a[len('--alias='):].split(',') if c]
FUORI = [n for a in sys.argv[2:] if a.startswith('--fuori=') for n in a[len('--fuori='):].split(',') if n]
LARGHEZZA_MASSIMA = int(ARGOMENTI[0]) if ARGOMENTI else 2048
MODULO = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', '..', '12-foresta-ui-comune')
CARTELLA = os.path.join(MODULO, 'sorgenti-immagini', NOME)
RISORSE = os.path.join(MODULO, 'src', 'main', 'resources', 'com', 'threeamigos', 'foresta', 'img', NOME)


def piazza(misure, libere):
    """Mette le immagini (ognuna con una linea rossa di 1 pixel attorno, condivisa con le vicine) negli spazi liberi (x, y, l, a),
    in cui ogni immagine occupa (l+1, a+1); restituisce (posti, spazi liberi rimasti), o None se non entrano."""
    posti = {}
    for nome, (l, a) in sorted(misure.items(), key=lambda t: (-max(t[1][1], t[1][0]), -t[1][0] * t[1][1], t[0])):
        pl, pa = l + 1, a + 1
        migliore = None
        for (x, y, fl, fa) in libere:
            if pl <= fl and pa <= fa:
                punteggio = (min(fl - pl, fa - pa), max(fl - pl, fa - pa))
                if migliore is None or punteggio < migliore[0]:
                    migliore = (punteggio, x, y)
        if migliore is None:
            return None
        _, px, py = migliore
        posti[nome] = (px + 1, py + 1)
        nuove = []
        for (x, y, fl, fa) in libere:
            if px >= x + fl or px + pl <= x or py >= y + fa or py + pa <= y:
                nuove.append((x, y, fl, fa))
                continue
            if px > x:
                nuove.append((x, y, px - x, fa))
            if px + pl < x + fl:
                nuove.append((px + pl, y, x + fl - px - pl, fa))
            if py > y:
                nuove.append((x, y, fl, py - y))
            if py + pa < y + fa:
                nuove.append((x, py + pa, fl, y + fa - py - pa))
        libere = [r for r in nuove if not any(
            o != r and o[0] <= r[0] and o[1] <= r[1] and o[0] + o[2] >= r[0] + r[2] and o[1] + o[3] >= r[1] + r[3] for o in nuove)]
    return posti, libere


def minimo(prova, massimo=4096):
    """Il valore più piccolo (da 1) per cui prova(valore) non è None, o None"""
    basso, alto, trovato = 1, massimo, None
    while basso <= alto:
        meta = (basso + alto) // 2
        esito = prova(meta)
        if esito is not None:
            trovato = (meta, esito)
            alto = meta - 1
        else:
            basso = meta + 1
    return trovato


def impagina(misure, larghezza):
    """L'impaginazione più bassa in quella larghezza: (altezza, posti), o None. Con IN_CIMA prima la fascia in alto, poi le altre."""
    cima = {n: m for n, m in misure.items() if n in IN_CIMA}
    resto = {n: m for n, m in misure.items() if n not in IN_CIMA}
    if not cima:
        trovato = minimo(lambda h: piazza(resto, [(0, 0, larghezza - 1, h - 1)]))
        return None if trovato is None else (trovato[0], trovato[1][0])
    fascia = minimo(lambda h: piazza(cima, [(0, 0, larghezza - 1, h - 1)]))
    if fascia is None:
        return None
    altezza_fascia, (posti_cima, libere) = fascia

    def sotto(h):
        spazi = libere + [(0, altezza_fascia - 1, larghezza - 1, h - altezza_fascia)]
        esito = piazza(resto, spazi)
        return None if esito is None else esito[0]
    trovato = minimo(sotto)
    if trovato is None:
        return None
    posti = dict(posti_cima)
    posti.update(trovato[1])
    return trovato[0], posti


misure = {f[:-4]: Image.open(os.path.join(CARTELLA, f)).size for f in os.listdir(CARTELLA)
          if f.endswith('.png') and f[:-4] not in {a for a, _ in ALIAS}}
piu_larga = max(l for l, _ in misure.values()) + 2
migliore = None
for larghezza in range(piu_larga, LARGHEZZA_MASSIMA + 1, 2):
    esito = impagina(misure, larghezza)
    if esito and (migliore is None or larghezza * esito[0] < migliore[0] * migliore[1]):
        migliore = (larghezza, esito[0], esito[1])
larghezza, altezza, posti = migliore
area = sum(l * a for l, a in misure.values())
with open(os.path.join(RISORSE, 'atlante_%s.txt' % NOME), 'w', encoding='utf-8') as f:
    f.write("# L'atlante di img/%s (atlante_%s.png): impaginazione libera, ogni immagine col suo riquadro rosso attorno.\n" % (NOME, NOME))
    f.write("# Una riga per immagine: nome x y larghezza altezza (x e y sono l'angolo in alto a sinistra dell'immagine). Fra due immagini\n")
    f.write("# basta la linea rossa di 1 pixel. Per ricostruire l'immagine: python3 risorse_e_documenti_vari/atlanti/costruisci_atlante.py %s\n" % NOME)
    f.write("# Questo elenco l'ha proposto impagina_atlante.py; da qui si può spostare a mano qualunque immagine.\n")
    if IN_CIMA:
        f.write("# In cima: %s\n" % " ".join(IN_CIMA))
    f.write("LIBERO\nLARGHEZZA %d\nALTEZZA %d\n" % (larghezza, altezza))
    for nome, altro in ALIAS:
        f.write("ALIAS %s %s\n" % (nome, altro))
    if FUORI:
        f.write("FUORI %s\n" % " ".join(FUORI))
    for nome, (x, y) in sorted(posti.items(), key=lambda t: (t[1][1], t[1][0])):
        f.write("%s %d %d %d %d\n" % (nome, x, y, misure[nome][0], misure[nome][1]))
print('%dx%d, riempimento %.0f%%, %d immagini' % (larghezza, altezza, 100.0 * area / (larghezza * altezza), len(posti)))
