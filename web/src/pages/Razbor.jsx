import { useState, useMemo } from 'react'
import { useApp } from '../lib/context.js'
import { ProblemCard } from '../components/UI.jsx'

export default function Razbor() {
  const { content } = useApp()
  const region = content.themeById.get('prep-region')
  const muni = content.themeById.get('prep-muni')

  const [stage, setStage] = useState('region')
  const theme = stage === 'region' ? region : muni
  const years = useMemo(() => {
    const ys = (theme?.subthemes || []).filter((s) => s.problems.length)
    return ys
  }, [theme])

  const [subId, setSubId] = useState(null)
  const sub = years.find((s) => s.id === subId) || years[years.length - 1]
  const [pattern, setPattern] = useState(null)

  const patterns = useMemo(() => {
    const c = new Map()
    for (const p of sub?.problems || []) if (p.pattern) c.set(p.pattern, (c.get(p.pattern) || 0) + 1)
    return Array.from(c.entries()).sort((a, b) => b[1] - a[1])
  }, [sub])

  const problems = (sub?.problems || []).filter((p) => !pattern || p.pattern === pattern)

  function pick(s) { setSubId(s.id); setPattern(null) }

  return (
    <div className="page">
      <h1>Разбор олимпиады</h1>
      <p className="muted">Выбери этап и год — каждая задача тура разобрана и классифицирована по приёму.</p>

      <div className="seg">
        <button className={'seg-btn' + (stage === 'region' ? ' on' : '')}
          onClick={() => { setStage('region'); setSubId(null); setPattern(null) }}>Региональный</button>
        <button className={'seg-btn' + (stage === 'muni' ? ' on' : '')}
          onClick={() => { setStage('muni'); setSubId(null); setPattern(null) }}>Муниципальный</button>
      </div>

      <div className="chips">
        {years.map((s) => (
          <button key={s.id} className={'chip-btn' + (s.id === sub?.id ? ' on' : '')} onClick={() => pick(s)}>
            {s.title.replace(' год', '')}
          </button>
        ))}
      </div>

      {sub && (
        <>
          <div className="razbor-head">
            <h2>{sub.title} · {stage === 'region' ? 'региональный' : 'муниципальный'} этап</h2>
            <span className="muted">{sub.problems.length} задач</span>
          </div>

          {patterns.length > 0 && (
            <div className="pattern-filter">
              <button className={'pf-chip' + (!pattern ? ' on' : '')} onClick={() => setPattern(null)}>
                все · {sub.problems.length}
              </button>
              {patterns.map(([p, n]) => (
                <button key={p} className={'pf-chip' + (pattern === p ? ' on' : '')} onClick={() => setPattern(p)}>
                  {p} · {n}
                </button>
              ))}
            </div>
          )}

          <div className="pcard-grid">
            {problems.map((p, i) => <ProblemCard key={p.id} problem={p} index={i} />)}
          </div>
        </>
      )}
    </div>
  )
}
