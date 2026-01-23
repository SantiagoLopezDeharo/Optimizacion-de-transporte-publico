"""
plot_pareto_evolution.py

Lee un CSV con formato `generation,solution_index,is_pareto,obj1,obj2,...`
y dibuja la evolución del frente de Pareto (objetivos 1 vs 2 por defecto).

Uso:
    python plotting/plot_pareto_evolution.py --file pareto_evolution_2026-01-22_18-00-00.csv

Dependencias: pandas, matplotlib, numpy
"""
import argparse
import os
import glob
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d import Axes3D  # noqa: F401 (required for 3D plots)
from matplotlib import cm


def find_file(pattern):
    matches = glob.glob(pattern)
    if not matches:
        return None
    # use newest file
    matches.sort(key=os.path.getmtime, reverse=True)
    return matches[0]


def _get_objective_cols(df):
    return [c for c in df.columns if c.startswith('obj')]


def _sample_generations(gens, stride):
    if not gens:
        return []
    if stride is None or stride <= 1:
        return gens
    sampled = gens[::stride]
    if gens[-1] not in sampled:
        sampled.append(gens[-1])
    return sampled


def _parse_maximize_list(arg: str):
    if not arg:
        return set()
    return {int(x.strip()) for x in arg.split(',') if x.strip().isdigit()}


def _to_minimization(values: np.ndarray, maximize_idx: set):
    out = values.copy()
    for i in maximize_idx:
        col = i - 1
        if 0 <= col < out.shape[1]:
            out[:, col] = -out[:, col]
    return out


def _compute_reference_point(all_points: np.ndarray):
    # reference point must be worse (larger for minimization) than all points
    max_vals = np.nanmax(all_points, axis=0)
    min_vals = np.nanmin(all_points, axis=0)
    span = np.where(np.isfinite(max_vals - min_vals), max_vals - min_vals, 1.0)
    return max_vals + 0.05 * span


def _hypervolume_monte_carlo(pareto_points: np.ndarray, ref: np.ndarray, samples: int = 50000, seed: int = 42):
    # assumes minimization and pareto_points already nondominated
    if pareto_points.size == 0:
        return 0.0
    d = pareto_points.shape[1]
    mins = np.min(pareto_points, axis=0)
    maxs = ref
    if np.any(maxs <= mins):
        return 0.0

    rng = np.random.default_rng(seed)
    pts = rng.uniform(low=mins, high=maxs, size=(samples, d))

    # dominated if any pareto point <= sample in all dims
    dominated = np.zeros(samples, dtype=bool)
    for p in pareto_points:
        dominated |= np.all(pts >= p, axis=1)
    volume = np.prod(maxs - mins)
    return volume * dominated.mean()


def _plot_2d_evolution(df, obj_x=1, obj_y=2, out=None, show=True, *, base_name='pareto_evolution', stride=20):

    # identify objective columns
    obj_cols = _get_objective_cols(df)
    if len(obj_cols) < max(obj_x, obj_y):
        raise ValueError('CSV does not have enough objective columns.')

    xcol = f'obj{obj_x}'
    ycol = f'obj{obj_y}'

    # parse generation order
    gens = sorted(df['generation'].unique())
    gens = _sample_generations(gens, stride)
    cmap = plt.get_cmap('viridis', len(gens))

    plt.figure(figsize=(9, 7))

    # plot population points per generation (light gray)
    for g in gens:
        sub = df[df['generation'] == g]
        plt.scatter(sub[xcol], sub[ycol], color='lightgray', s=8, alpha=0.35)

    # overlay Pareto front per generation with colormap and slight transparency
    for i, g in enumerate(gens):
        sub = df[(df['generation'] == g) & (df['is_pareto'] == 1)]
        if sub.empty:
            continue

        # sort pareto points for nicer lines
        pts = sub.sort_values(by=xcol)
        color = cmap(i)
        plt.plot(pts[xcol], pts[ycol], '-', color=color, alpha=0.6)
        plt.scatter(pts[xcol], pts[ycol], color=color, s=30, edgecolor='k', linewidth=0.4)

    sm = cm.ScalarMappable(cmap=cmap, norm=plt.Normalize(vmin=min(gens), vmax=max(gens)))
    sm.set_array([])
    ax = plt.gca()
    cbar = plt.colorbar(sm, ax=ax)
    cbar.set_label('generation')

    plt.xlabel(xcol)
    plt.ylabel(ycol)
    plt.title('Evolución del frente de Pareto (2D)')
    plt.grid(True, alpha=0.3)

    if out is None:
        out = base_name + f'_plot_{xcol}_{ycol}.png'

    plt.tight_layout()
    plt.savefig(out, dpi=200)
    if show:
        plt.show()
    print('Saved plot to', out)


def _plot_3d_evolution(df, out=None, show=True, *, stride=20):
    obj_cols = _get_objective_cols(df)
    if len(obj_cols) < 3:
        raise ValueError('CSV does not have at least 3 objective columns.')

    xcol, ycol, zcol = obj_cols[0], obj_cols[1], obj_cols[2]
    gens = sorted(df['generation'].unique())
    gens = _sample_generations(gens, stride)
    cmap = plt.get_cmap('viridis', len(gens))

    fig = plt.figure(figsize=(10, 8))
    ax = fig.add_subplot(111, projection='3d')

    # population points
    for g in gens:
        sub = df[df['generation'] == g]
        ax.scatter(sub[xcol], sub[ycol], sub[zcol], color='lightgray', s=10, alpha=0.25)

    # pareto fronts per generation
    for i, g in enumerate(gens):
        sub = df[(df['generation'] == g) & (df['is_pareto'] == 1)]
        if sub.empty:
            continue
        color = cmap(i)
        ax.scatter(sub[xcol], sub[ycol], sub[zcol], color=color, s=35, edgecolor='k', linewidth=0.4)

    sm = cm.ScalarMappable(cmap=cmap, norm=plt.Normalize(vmin=min(gens), vmax=max(gens)))
    sm.set_array([])
    cbar = fig.colorbar(sm, ax=ax, shrink=0.8, pad=0.1)
    cbar.set_label('generation')

    ax.set_xlabel(xcol)
    ax.set_ylabel(ycol)
    ax.set_zlabel(zcol)
    ax.set_title('Evolución del frente de Pareto (3D)')

    if out is None:
        out = 'pareto_evolution_3d.png'

    plt.tight_layout()
    plt.savefig(out, dpi=220)
    if show:
        plt.show()
    print('Saved plot to', out)


def _plot_3d_projections(df, out=None, show=True, *, stride=20):
    obj_cols = _get_objective_cols(df)
    if len(obj_cols) < 3:
        raise ValueError('CSV does not have at least 3 objective columns.')

    pairs = [(obj_cols[0], obj_cols[1]), (obj_cols[0], obj_cols[2]), (obj_cols[1], obj_cols[2])]
    gens = sorted(df['generation'].unique())
    gens = _sample_generations(gens, stride)
    cmap = plt.get_cmap('viridis', len(gens))

    fig, axes = plt.subplots(1, 3, figsize=(18, 6))
    for ax, (xcol, ycol) in zip(axes, pairs):
        # population points
        ax.scatter(df[xcol], df[ycol], color='lightgray', s=10, alpha=0.25)

        # pareto fronts per generation
        for i, g in enumerate(gens):
            sub = df[(df['generation'] == g) & (df['is_pareto'] == 1)]
            if sub.empty:
                continue
            color = cmap(i)
            pts = sub.sort_values(by=xcol)
            ax.plot(pts[xcol], pts[ycol], '-', color=color, alpha=0.6)
            ax.scatter(pts[xcol], pts[ycol], color=color, s=25, edgecolor='k', linewidth=0.4)

        ax.set_xlabel(xcol)
        ax.set_ylabel(ycol)
        ax.set_title(f'{xcol} vs {ycol}')
        ax.grid(True, alpha=0.3)

    sm = cm.ScalarMappable(cmap=cmap, norm=plt.Normalize(vmin=min(gens), vmax=max(gens)))
    sm.set_array([])
    fig.colorbar(sm, ax=axes, shrink=0.8, label='generation')

    if out is None:
        out = 'pareto_evolution_2d_projections.png'

    plt.tight_layout()
    plt.savefig(out, dpi=220)
    if show:
        plt.show()
    print('Saved plot to', out)


def plot_evolution(csv_path, obj_x=1, obj_y=2, out=None, show=True, mode='both', stride=20,
                   hv=False, hv_samples=50000, hv_ref=None, hv_maximize=None, hv_out=None):
    df = pd.read_csv(csv_path)
    base_name = os.path.splitext(os.path.basename(csv_path))[0]

    obj_cols = _get_objective_cols(df)
    if len(obj_cols) < 2:
        raise ValueError('CSV does not have enough objective columns.')

    if mode in ('2d', 'both'):
        _plot_2d_evolution(df, obj_x=obj_x, obj_y=obj_y, out=out, show=show, base_name=base_name, stride=stride)

    if len(obj_cols) >= 3 and mode in ('3d', 'both'):
        _plot_3d_evolution(df, out='pareto_evolution_3d.png', show=show, stride=stride)
        _plot_3d_projections(df, out='pareto_evolution_2d_projections.png', show=show, stride=stride)

    if hv:
        obj_cols = _get_objective_cols(df)
        raw = df[obj_cols].to_numpy(dtype=float)
        maximize_idx = _parse_maximize_list(hv_maximize or '')
        raw = _to_minimization(raw, maximize_idx)

        if hv_ref:
            ref = np.array([float(x) for x in hv_ref.split(',')], dtype=float)
        else:
            ref = _compute_reference_point(raw)

        gens = sorted(df['generation'].unique())
        gens = _sample_generations(gens, stride)
        records = []
        for g in gens:
            sub = df[(df['generation'] == g) & (df['is_pareto'] == 1)]
            pts = sub[obj_cols].to_numpy(dtype=float)
            pts = _to_minimization(pts, maximize_idx)
            hv_val = _hypervolume_monte_carlo(pts, ref, samples=hv_samples)
            records.append({'generation': g, 'hypervolume': hv_val})

        hv_df = pd.DataFrame(records)
        if hv_out is None:
            hv_out = base_name + '_hypervolume.csv'
        hv_df.to_csv(hv_out, index=False)
        print('Saved hypervolume to', hv_out)

        plt.figure(figsize=(8, 4.5))
        plt.plot(hv_df['generation'], hv_df['hypervolume'], marker='o', linewidth=1.6)
        plt.xlabel('generation')
        plt.ylabel('hypervolume')
        plt.title('Evolución de hipervolumen')
        plt.grid(True, alpha=0.3)
        hv_plot = os.path.splitext(hv_out)[0] + '.png'
        plt.tight_layout()
        plt.savefig(hv_plot, dpi=200)
        if show:
            plt.show()
        print('Saved hypervolume plot to', hv_plot)


def main():
    parser = argparse.ArgumentParser(description='Plot Pareto front evolution from CSV')
    parser.add_argument('--file', '-f', help='CSV file path or glob pattern (default: pareto_evolution_*.csv)',
                        default='pareto_evolution_*.csv')
    parser.add_argument('--objx', type=int, default=1, help='Objective index for x axis (1-based)')
    parser.add_argument('--objy', type=int, default=2, help='Objective index for y axis (1-based)')
    parser.add_argument('--mode', choices=['2d', '3d', 'both'], default='both',
                        help='Plot mode: 2d, 3d, or both (default: both)')
    parser.add_argument('--stride', type=int, default=20,
                        help='Plot every N generations (always includes last). Default: 20')
    parser.add_argument('--hypervolume', action='store_true',
                        help='Compute hypervolume per generation (uses Pareto points).')
    parser.add_argument('--hv-samples', type=int, default=50000,
                        help='Monte Carlo samples for hypervolume (default: 50000).')
    parser.add_argument('--hv-ref', help='Reference point as comma-separated values (e.g., "1,2,3").')
    parser.add_argument('--hv-maximize',
                        help='Comma-separated objective indices to maximize (e.g., "1").')
    parser.add_argument('--hv-out', help='Output CSV path for hypervolume values.')
    parser.add_argument('--out', help='Output image path (PNG)')
    parser.add_argument('--noshow', action='store_true', help='Do not display the interactive window')

    args = parser.parse_args()

    pattern = args.file
    csv_path = pattern
    if any(c in pattern for c in '*?[]'):
        found = find_file(pattern)
        if not found:
            raise SystemExit(f'No files found matching pattern: {pattern}')
        csv_path = found

    plot_evolution(csv_path, obj_x=args.objx, obj_y=args.objy, out=args.out,
                   show=not args.noshow, mode=args.mode, stride=args.stride,
                   hv=args.hypervolume, hv_samples=args.hv_samples,
                   hv_ref=args.hv_ref, hv_maximize=args.hv_maximize,
                   hv_out=args.hv_out)


if __name__ == '__main__':
    main()
