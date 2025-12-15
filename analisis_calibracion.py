import glob
import math
import os
import re
from collections import defaultdict

import numpy as np


def leer_csv(path):
    """
    Lee un csv con formato:
    tipo,f1,f2
    nsga2,....
    ...
    greedy,....
    Retorna:
      points: lista de (f1,f2) de nsga2
      greedy: (f1,f2)
    """
    points = []
    greedy = None

    with open(path, "r", encoding="utf-8") as f:
        header = f.readline()
        for line in f:
            line = line.strip()
            if not line:
                continue
            tipo, f1, f2 = line.split(",")
            f1 = float(f1)
            f2 = float(f2)
            if tipo.strip().lower() == "greedy":
                greedy = (f1, f2)
            else:
                points.append((f1, f2))

    return points, greedy


def no_dominated(points):
    """Filtra puntos no dominados (maximización en ambos objetivos)."""
    nd = []
    for i, p in enumerate(points):
        dominated = False
        for j, q in enumerate(points):
            if j == i:
                continue
            # q domina a p si q>=p en todo y > en algo
            if (q[0] >= p[0] and q[1] >= p[1]) and (q[0] > p[0] or q[1] > p[1]):
                dominated = True
                break
        if not dominated:
            nd.append(p)
    return nd

# hipervolumen 2D 
def hypervolume_2d_max(points, ref):
    """
    HV 2D para maximización respecto a un punto de referencia ref=(rx,ry)
    Asume que los puntos ya son NO dominados.
    Implementación: ordenar por f1 asc y acumular rectángulos con 'skyline' en f2.
    """
    if not points:
        return 0.0

    rx, ry = ref

    # Ordenar por f1 asc
    pts = sorted(points, key=lambda p: p[0])

    hv = 0.0
    # vamos de derecha a izquierda para ir “subiendo” el techo en f2
    best_f2 = ry
    prev_f1 = rx

    for f1, f2 in reversed(pts):
        # ancho en f1: (prev_f1 -> f1)
        width = prev_f1 - f1
        if width < 0:
            width = 0

        # altura: (mejor f2 actual -> ref f2)
        height = max(0.0, best_f2 - ry)

        hv += width * height

        # actualizar skyline
        best_f2 = max(best_f2, f2)
        prev_f1 = f1

    # ultimo segmento desde el mejor f1 hasta ref.x
    width = prev_f1 - rx
    if width < 0:
        width = 0
    height = max(0.0, best_f2 - ry)
    hv += width * height

    return hv


def dominates_front_over_greedy(points, greedy):
    """True si existe algún punto en points que domina al greedy."""
    if greedy is None:
        return False
    g1, g2 = greedy
    for f1, f2 in points:
        if (f1 >= g1 and f2 >= g2) and (f1 > g1 or f2 > g2):
            return True
    return False

# main: analiza mediana C0..C26 runs 0..4
def main():
    inst = "mediana"
    pattern = f"resultados_{inst}_C*_run*.csv"


    files = sorted(glob.glob(pattern))

    if not files:
        print(f"No encontré archivos con patrón: {pattern}")
        print("Ejecutá esto desde la carpeta donde están los CSV.")
        return

    # Agrupar por config (C0..C26)
    # Extraemos config y run del nombre
    rex = re.compile(rf"resultados_{inst}_(C\d+)_run(\d+)\.csv$", re.IGNORECASE)

    by_config = defaultdict(list)

    all_points_for_ref = []

    # Cargar todo primero
    loaded = []
    for path in files:
        m = rex.search(os.path.basename(path))
        if not m:
            continue
        cfg = m.group(1).upper()
        run = int(m.group(2))
        points, greedy = leer_csv(path)
        points_nd = no_dominated(points)
        loaded.append((cfg, run, points_nd, greedy))
        all_points_for_ref.extend(points_nd)
        if greedy is not None:
            all_points_for_ref.append(greedy)

    if not loaded:
        print("Encontré archivos pero no matchean el patrón esperado.")
        return

    #Definir punto de referencia 
    #Para maximización, ref debe ser "peor" que todos (menor en ambos).
    min_f1 = min(p[0] for p in all_points_for_ref)
    min_f2 = min(p[1] for p in all_points_for_ref)

    # margen para asegurar que queda estrictamente peor
    ref = (min_f1 - 1e-6, min_f2 - 1e-6)

    # Calcular HV por corrida y agrupar
    for cfg, run, points_nd, greedy in loaded:
        hv = hypervolume_2d_max(points_nd, ref)
        dom = dominates_front_over_greedy(points_nd, greedy)
        by_config[cfg].append((run, hv, dom))

    #Resumen por config
    resumen = []
    for cfg in sorted(by_config.keys(), key=lambda x: int(x[1:])):
        runs_data = sorted(by_config[cfg], key=lambda t: t[0])
        hvs = [hv for _, hv, _ in runs_data]
        doms = [dom for _, _, dom in runs_data]

        mean_hv = float(np.mean(hvs))
        std_hv = float(np.std(hvs, ddof=1)) if len(hvs) > 1 else 0.0
        dom_pct = 100.0 * (sum(doms) / len(doms))

        resumen.append((cfg, mean_hv, std_hv, dom_pct, len(hvs)))

    #Imprimir ranking
    print(f"=== RESUMEN {inst.upper()} (ref={ref}) ===\n")
    resumen_sorted = sorted(resumen, key=lambda t: t[1], reverse=True)

    print("Top 10 por HV promedio:")
    for i, (cfg, mean_hv, std_hv, dom_pct, n) in enumerate(resumen_sorted[:10], start=1):
        print(f"{i:2d}) {cfg:>3}  HV={mean_hv:.6f}  std={std_hv:.6f}  dominaGreedy={dom_pct:5.1f}%  runs={n}")
    print()

    print("Detalle por configuración (C0..C26):\n")
    for cfg, mean_hv, std_hv, dom_pct, n in sorted(resumen, key=lambda t: int(t[0][1:])):
        print(f"=== {inst} – {cfg} ===")
        print(f"HV promedio: {mean_hv:.6f}")
        print(f"HV desvío:   {std_hv:.6f}")
        print(f"Domina greedy en {dom_pct:.1f}% de corridas (n={n})\n")

    #Guardar ranking
        out = f"resumen_calibracion_{inst}.csv"
    with open(out, "w", encoding="utf-8") as f:
        f.write("instancia,config,hv_mean,hv_std,domina_greedy_pct,n_runs\n")
        for cfg, mean_hv, std_hv, dom_pct, n in resumen_sorted:
            f.write(f"{inst},{cfg},{mean_hv:.8f},{std_hv:.8f},{dom_pct:.1f},{n}\n")
    print(f"Ranking guardado en: {out}")


if __name__ == "__main__":
    main()

