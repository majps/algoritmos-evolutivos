import csv
import matplotlib.pyplot as plt

CSV_FILE = "resultados_nsga2_greedy.csv"

# para ver grafica: ejecutar runner para generar csv -> (tener instalado python y matplotlib) tirar py plot_nsga2_greedy.py

x_nsga2, y_nsga2 = [], []
x_greedy, y_greedy = [], []

# leer CSV
with open(CSV_FILE, newline="", encoding="utf-8") as csvfile:
    reader = csv.DictReader(csvfile)
    for row in reader:
        tipo = row["tipo"].strip().lower()
        f1 = float(row["f1"])
        f2 = float(row["f2"])

        if tipo == "nsga2":
            x_nsga2.append(f1)
            y_nsga2.append(f2)
        elif tipo == "greedy":
            x_greedy.append(f1)
            y_greedy.append(f2)

print(f"Leídas {len(x_nsga2)} soluciones NSGA-II y {len(x_greedy)} greedy.")

# grafica
plt.figure()

# frente de pareto
plt.scatter(x_nsga2, y_nsga2, label="NSGA-II")

# greedy
if x_greedy:
    plt.scatter(x_greedy, y_greedy, label="Greedy", marker="*", s=120)

plt.xlabel("f1 (satisfacción media)")
plt.ylabel("f2 (equidad)")
plt.title("Frente de Pareto – NSGA-II vs Greedy")
plt.grid(True)
plt.legend()
plt.tight_layout()

plt.xlim(1.0, 0.0)   # ejes de 1 a 0
plt.ylim(1.0, 0.0)

plt.show()
