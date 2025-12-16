import glob
import os
import re
from collections import defaultdict

import numpy as np

def leer_csv(path):
    points = []
    greedy = None

    with open(path, "r", encoding="utf-8") as f:
        f.readline()  # header
        for line in f:
            if not line.strip():
                continue
            tipo, f1, f2 = line.strip().split(",")
            f1 = float(f1)
            f2 = float(f2)
            if tipo.lower() == "greedy":
                greedy = (f1, f2)
            else:
                points.append((f1, f2))

    return points, greedy



def no_dominated(points):
    nd = []
    for i, p in enumerate(points):
        dominated = False
        for j, q in enumerate(points):
            if i == j:
                continue
            if (q[0] >= p[0] and q[1] >= p[1]) and (q[0] > p[0] or q[1] > p[1]):
                dominated = True
                break
        if not dominated:
            nd.append(p)
    return nd


def hypervolume_2d_max(points, ref):
    if not points:
        return 0.0

    rx, ry = ref
    pts = sorted(points, key=lambda p: p[0])

    hv = 0.0
    best_f2 = ry
    prev_f1 = rx

    for f1, f2 in reversed(pts):
        width = max(0.0, prev_f1 - f1)
        height = max(0.0, best_f2 - ry)
        hv += width * height

        best_f2 = max(best_f2, f2)
        prev_f1 = f1

    width = max(0.0, prev_f1 - rx)
    height = max(0.0, best_f2 - ry)
    hv += width * height

    return hv


def dominates_front_over_greedy(points, greedy):
    if greedy is None:
        return False
    g1, g2 = greedy
    for f1, f2 in points:
        if (f1 >= g1 and f2 >= g2) and (f1 > g1 or f2 > g2):
            return True
    return False


def main():
    base_dir = "resultados_finales_C25"
    instancias = ["pequena"," mediana", "grande"]

    resumen_global = []

    for inst in instancias:
        pattern = os.path.join(base_dir, inst, f"final_{inst}_run*.csv")
        files = sorted(glob.glob(pattern))

        if not files:
            print(f"[WARN] No se encontraron CSV para {inst}")
            continue

        rex = re.compile(rf"final_{inst}_run(\d+)\.csv$", re.IGNORECASE)

        all_points_for_ref = []
        runs_data = []

        # cargar datos
        for path in files:
            m = rex.search(os.path.basename(path))
            if not m:
                continue

            run = int(m.group(1))
            points, greedy = leer_csv(path)
            points_nd = no_dominated(points)

            all_points_for_ref.extend(points_nd)
            if greedy:
                all_points_for_ref.append(greedy)

            runs_data.append((run, points_nd, greedy))

        # punto de referencia automático
        min_f1 = min(p[0] for p in all_points_for_ref)
        min_f2 = min(p[1] for p in all_points_for_ref)
        ref = (min_f1 - 1e-6, min_f2 - 1e-6)

        hvs = []
        doms = []

        for run, points_nd, greedy in runs_data:
            hv = hypervolume_2d_max(points_nd, ref)
            dom = dominates_front_over_greedy(points_nd, greedy)
            hvs.append(hv)
            doms.append(dom)

        mean_hv = float(np.mean(hvs))
        std_hv = float(np.std(hvs, ddof=1)) if len(hvs) > 1 else 0.0
        dom_pct = 100.0 * sum(doms) / len(doms)

        resumen_global.append(
            (inst, mean_hv, std_hv, dom_pct, len(hvs))
        )

        print(f"\n=== {inst.upper()} ===")
        print(f"HV promedio: {mean_hv:.6f}")
        print(f"HV desvío:   {std_hv:.6f}")
        print(f"Domina greedy en {dom_pct:.1f}% de corridas (n={len(hvs)})")

    # guardar resumen final
    out = "resumen_final.csv"
    with open(out, "w", encoding="utf-8") as f:
        f.write("instancia,hv_mean,hv_std,domina_greedy_pct,n_runs\n")
        for inst, mean_hv, std_hv, dom_pct, n in resumen_global:
            f.write(f"{inst},{mean_hv:.8f},{std_hv:.8f},{dom_pct:.1f},{n}\n")

    print(f"\nResumen final guardado en: {out}")


if __name__ == "__main__":
    main()
