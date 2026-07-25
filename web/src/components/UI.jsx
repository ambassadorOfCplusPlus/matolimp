import { Link } from 'react-router-dom'
import { useApp } from '../lib/context.js'

export function DifficultyBadge({ value }) {
  const d = value || 3
  return (
    <span className="diff" title={`Сложность ${d}/5`}>
      {Array.from({ length: 5 }, (_, i) => (
        <span key={i} className={'diff-dot' + (i < d ? ' on' : '')} />
      ))}
    </span>
  )
}

export function PatternChip({ pattern }) {
  if (!pattern) return null
  return <span className="pattern-chip">{pattern}</span>
}

export function KindTag({ kind }) {
  return (
    <span className={'kind ' + (kind === 'CLOSED' ? 'kind-closed' : 'kind-open')}>
      {kind === 'CLOSED' ? 'ответ' : 'доказательство'}
    </span>
  )
}

export function ProgressRing({ value, size = 46, stroke = 5, label }) {
  const r = (size - stroke) / 2
  const c = 2 * Math.PI * r
  const off = c * (1 - Math.max(0, Math.min(1, value)))
  return (
    <div className="ring" style={{ width: size, height: size }}>
      <svg width={size} height={size}>
        <circle cx={size / 2} cy={size / 2} r={r} fill="none" stroke="var(--ring-bg)" strokeWidth={stroke} />
        <circle cx={size / 2} cy={size / 2} r={r} fill="none" stroke="var(--accent)" strokeWidth={stroke}
          strokeDasharray={c} strokeDashoffset={off} strokeLinecap="round"
          transform={`rotate(-90 ${size / 2} ${size / 2})`} />
      </svg>
      {label != null && <span className="ring-label">{label}</span>}
    </div>
  )
}

export function StatusDot({ id }) {
  const { state } = useApp()
  const st = state.solved[id]?.status
  const cls = st === 'solved' ? 'solved' : st === 'partial' ? 'partial' : st ? 'attempt' : 'none'
  const title = { solved: 'решено', partial: 'частично', attempt: 'начато', none: 'не начато' }[cls]
  return <span className={'sdot ' + cls} title={title} />
}

// Короткое превью текста задачи без LaTeX-маркеров
export function plainPreview(text, n = 120) {
  const t = String(text || '')
    .replace(/\$\$([\s\S]+?)\$\$/g, ' … ')
    .replace(/\[\[fig:[^\]]+\]\]/g, ' ')
    .replace(/\$([^$]+?)\$/g, (_, x) => x)
    .replace(/\\[a-zA-Z]+/g, ' ')
    .replace(/[{}\\]/g, '')
    .replace(/\s+/g, ' ')
    .trim()
  return t.length > n ? t.slice(0, n) + '…' : t
}

export function ProblemCard({ problem, index }) {
  const { state } = useApp()
  const st = state.solved[problem.id]?.status
  return (
    <Link to={`/problem/${problem.id}`} className={'pcard' + (st === 'solved' ? ' done' : '')}>
      <div className="pcard-top">
        <StatusDot id={problem.id} />
        <span className="pcard-num">№{index != null ? index + 1 : problem.order}</span>
        <KindTag kind={problem.kind} />
        {state.bookmarks[problem.id] && <span className="bm-mini" title="в закладках">★</span>}
        <span className="pcard-diff"><DifficultyBadge value={problem.difficulty} /></span>
      </div>
      <div className="pcard-body">{plainPreview(problem.statement)}</div>
      {problem.pattern && <PatternChip pattern={problem.pattern} />}
    </Link>
  )
}
