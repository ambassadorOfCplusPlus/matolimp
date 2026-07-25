import { useState, useMemo } from 'react'
import { useApp } from '../lib/context.js'
import { allProblems } from '../lib/content.js'
import { ProblemCard, plainPreview } from '../components/UI.jsx'

export default function SearchPage() {
  const { content, state } = useApp()
  const problems = useMemo(() => allProblems(content), [content])
  const index = useMemo(
    () => problems.map((p) => ({ p, hay: (plainPreview(p.statement, 400) + ' ' + (p.pattern || '')).toLowerCase() })),
    [problems]
  )

  const [q, setQ] = useState('')
  const [filter, setFilter] = useState('all') // all | closed | open | bookmarks

  const results = useMemo(() => {
    const terms = q.trim().toLowerCase().split(/\s+/).filter(Boolean)
    return index
      .filter(({ p }) => {
        if (filter === 'closed' && p.kind !== 'CLOSED') return false
        if (filter === 'open' && p.kind !== 'OPEN') return false
        if (filter === 'bookmarks' && !state.bookmarks[p.id]) return false
        return true
      })
      .filter(({ hay }) => terms.every((t) => hay.includes(t)))
      .slice(0, 120)
      .map(({ p }) => p)
  }, [index, q, filter, state.bookmarks])

  const bmCount = Object.keys(state.bookmarks).filter((id) => content.problemById.has(id)).length

  return (
    <div className="page">
      <h1>Поиск задач</h1>
      <input className="search-input" placeholder="Слово из условия или приём…"
        value={q} onChange={(e) => setQ(e.target.value)} autoFocus />
      <div className="chips">
        {[
          ['all', `Все · ${problems.length}`],
          ['closed', 'С ответом'],
          ['open', 'Доказательства'],
          ['bookmarks', `★ Закладки · ${bmCount}`],
        ].map(([k, label]) => (
          <button key={k} className={'chip-btn' + (filter === k ? ' on' : '')} onClick={() => setFilter(k)}>{label}</button>
        ))}
      </div>

      <p className="muted small">Найдено: {results.length}{results.length === 120 ? '+ (показаны первые 120)' : ''}</p>
      <div className="pcard-grid">
        {results.map((p, i) => <ProblemCard key={p.id} problem={p} index={p.order - 1} />)}
      </div>
      {results.length === 0 && <p className="muted">Ничего не найдено — попробуйте другое слово.</p>}
    </div>
  )
}
