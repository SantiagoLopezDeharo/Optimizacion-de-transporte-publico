from mpl_toolkits.mplot3d import Axes3D
import matplotlib.pyplot as plt
import pandas as pd

points = pd.read_csv('approximated_pareto_front_mvd.csv')
points_sol = pd.read_csv('datos_mvd/FUN30.csv', header=None)

x = points['f1'].values
y = points['f2'].values
z = points['f3'].values

x_sol = points_sol[0].values;
y_sol = points_sol[1].values;
z_sol = points_sol[2].values;

################################ 3D PLOT ################################

fig = plt.figure()
ax = fig.add_subplot(111, projection='3d')

ax.scatter(x, y, z, c='r', marker='o', label="Frente de pareto aproximado")

ax.scatter(x_sol, y_sol, z_sol, c='b', marker='o', label='Frente de pareto de la solución')

ax.set_xlabel('F1')
ax.set_ylabel('F2')
ax.set_zlabel('F3')
ax.legend()

plt.savefig('pareto_comparison_3D.png')  # You can change the file format here

plt.show()

################################ 2D PLOT X,Y ################################

plt.figure()

plt.scatter(x, y, c='r', marker='o', label='Frente de pareto aproximado')

plt.scatter(x_sol, y_sol, c='b', marker='o', label='Frente de pareto de la solución')

plt.xlabel('F1')
plt.ylabel('F2')
plt.legend()

plt.savefig('pareto_comparison_2D_xy.png')  # You can change the file format here

plt.show()

################################ 2D PLOT X,Z ################################

plt.figure()

plt.scatter(x, z, c='r', marker='o', label='Frente de pareto aproximado')

plt.scatter(x_sol, z_sol, c='b', marker='o', label='Frente de pareto de la solución')

plt.xlabel('F1')
plt.ylabel('F3')
plt.legend()

plt.savefig('pareto_comparison_2D_xz.png')  # You can change the file format here

plt.show()

################################ 2D PLOT Y,Z ################################

plt.figure()

plt.scatter(y, z, c='r', marker='o', label='Frente de pareto aproximado')

plt.scatter(y_sol, z_sol, c='b', marker='o', label='Frente de pareto de la solución')

plt.xlabel('F2')
plt.ylabel('F3')
plt.legend()

plt.savefig('pareto_comparison_2D_yz.png')  # You can change the file format here

plt.show()