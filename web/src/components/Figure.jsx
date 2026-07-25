// Нативные SVG-диаграммы (аналог MathFigure.kt на Android), маркер [[fig:name]].
import { useId } from 'react'

function Chessboard() {
  const cells = []
  for (let r = 0; r < 8; r++) for (let c = 0; c < 8; c++) {
    const dark = (r + c) % 2 === 1
    cells.push(<rect key={`${r}-${c}`} x={c * 24} y={r * 24} width={24} height={24}
      fill={dark ? 'var(--fig-ink)' : 'var(--fig-bg)'} />)
  }
  return (
    <svg viewBox="-1 -1 194 194" className="figure-svg" role="img" aria-label="Шахматная доска">
      {cells}
      <rect x={0} y={0} width={192} height={192} fill="none" stroke="var(--fig-line)" strokeWidth={2} />
    </svg>
  )
}

function Pigeonhole() {
  const holes = [0, 1, 2]
  const birds = [{ h: 0, n: 2 }, { h: 1, n: 1 }, { h: 2, n: 2 }]
  return (
    <svg viewBox="0 0 240 120" className="figure-svg" role="img" aria-label="Принцип Дирихле">
      {holes.map((h) => (
        <rect key={h} x={20 + h * 72} y={60} width={56} height={48} rx={6}
          fill="var(--fig-bg)" stroke="var(--fig-line)" strokeWidth={2} />
      ))}
      {birds.flatMap((b) => Array.from({ length: b.n }, (_, i) => (
        <circle key={`${b.h}-${i}`} cx={34 + b.h * 72 + i * 26} cy={40} r={11}
          fill="var(--fig-accent)" />
      )))}
    </svg>
  )
}

function Ramsey6() {
  const cx = 120, cy = 110, R = 92
  const pts = Array.from({ length: 6 }, (_, i) => {
    const a = (-90 + i * 60) * Math.PI / 180
    return [cx + R * Math.cos(a), cy + R * Math.sin(a)]
  })
  const edges = []
  for (let i = 0; i < 6; i++) for (let j = i + 1; j < 6; j++) {
    const red = (i + j) % 2 === 0
    edges.push(<line key={`${i}-${j}`} x1={pts[i][0]} y1={pts[i][1]} x2={pts[j][0]} y2={pts[j][1]}
      stroke={red ? '#e64980' : '#4c6ef5'} strokeWidth={1.6} opacity={0.7} />)
  }
  return (
    <svg viewBox="0 0 240 220" className="figure-svg" role="img" aria-label="Задача о шести знакомых">
      {edges}
      {pts.map((p, i) => <circle key={i} cx={p[0]} cy={p[1]} r={9} fill="var(--fig-ink)" />)}
    </svg>
  )
}

function Graph() {
  const nodes = [[40, 40], [140, 30], [200, 90], [150, 160], [50, 150], [110, 95]]
  const edges = [[0, 1], [1, 2], [2, 3], [3, 4], [4, 0], [5, 0], [5, 2], [5, 3]]
  return (
    <svg viewBox="0 0 240 190" className="figure-svg" role="img" aria-label="Граф">
      {edges.map(([a, b], i) => (
        <line key={i} x1={nodes[a][0]} y1={nodes[a][1]} x2={nodes[b][0]} y2={nodes[b][1]}
          stroke="var(--fig-line)" strokeWidth={2} />
      ))}
      {nodes.map((n, i) => <circle key={i} cx={n[0]} cy={n[1]} r={12} fill="var(--fig-accent)" />)}
    </svg>
  )
}

function Staircase() {
  const path = []
  let x = 10, y = 150
  for (let i = 0; i < 6; i++) { path.push(`${x},${y}`); x += 30; path.push(`${x},${y}`); y -= 22 }
  return (
    <svg viewBox="0 0 210 170" className="figure-svg" role="img" aria-label="Лесенка">
      <polyline points={path.join(' ')} fill="none" stroke="var(--fig-accent)" strokeWidth={3} />
      <line x1={10} y1={150} x2={200} y2={150} stroke="var(--fig-line)" strokeWidth={1.5} />
    </svg>
  )
}

function Consecutive() {
  return (
    <svg viewBox="0 0 260 60" className="figure-svg" role="img" aria-label="Последовательные числа">
      {Array.from({ length: 6 }, (_, i) => (
        <g key={i}>
          <rect x={10 + i * 40} y={14} width={34} height={34} rx={5}
            fill="var(--fig-bg)" stroke="var(--fig-line)" strokeWidth={2} />
          <text x={27 + i * 40} y={37} textAnchor="middle" fontSize="16" fill="var(--fig-ink)">{i + 1}</text>
        </g>
      ))}
    </svg>
  )
}

function Polygon() {
  const cx = 100, cy = 100, R = 80, n = 7
  const pts = Array.from({ length: n }, (_, i) => {
    const a = (-90 + i * 360 / n) * Math.PI / 180
    return `${cx + R * Math.cos(a)},${cy + R * Math.sin(a)}`
  }).join(' ')
  return (
    <svg viewBox="0 0 200 200" className="figure-svg" role="img" aria-label="Многоугольник">
      <polygon points={pts} fill="var(--fig-accent)" opacity={0.18} stroke="var(--fig-accent)" strokeWidth={2.5} />
    </svg>
  )
}

function Triangle() {
  return (
    <svg viewBox="0 0 200 170" className="figure-svg" role="img" aria-label="Треугольник">
      <polygon points="30,150 170,150 90,20" fill="var(--fig-accent)" opacity={0.18}
        stroke="var(--fig-accent)" strokeWidth={2.5} />
      <line x1="90" y1="20" x2="100" y2="150" stroke="var(--fig-line)" strokeWidth={1.5} strokeDasharray="4 4" />
    </svg>
  )
}

const FIGS = {
  chessboard: Chessboard,
  pigeonhole: Pigeonhole,
  ramsey6: Ramsey6,
  graph: Graph,
  staircase: Staircase,
  consecutive: Consecutive,
  polygon: Polygon,
  triangle: Triangle,
}

export default function Figure({ name }) {
  const Comp = FIGS[name]
  return (
    <figure className="figure">
      {Comp ? <Comp /> : <div className="figure-placeholder">рисунок: {name}</div>}
    </figure>
  )
}
