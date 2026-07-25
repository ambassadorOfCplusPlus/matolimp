import { useParams, Link } from 'react-router-dom'
import { useApp } from '../lib/context.js'
import { themeMeta } from '../lib/content.js'
import MathText from '../components/MathText.jsx'
import { ProgressRing } from '../components/UI.jsx'
import { useState } from 'react'

export default function ThemePage() {
  const { themeId } = useParams()
  const { content, state } = useApp()
  const theme = content.themeById.get(themeId)
  const [showTheory, setShowTheory] = useState(true)

  if (!theme) return <div className="page"><h2>Тема не найдена</h2><Link to="/themes">К темам</Link></div>
  const meta = themeMeta(theme.id)

  return (
    <div className="page">
      <div className="crumbs">
        <Link to={theme.track === 'prep' ? '/prep' : '/themes'} className="link">← {theme.track === 'prep' ? 'Подготовка' : 'Темы'}</Link>
      </div>
      <div className="theme-hero" style={{ '--tc': meta.color }}>
        <span className="theme-hero-icon">{meta.icon}</span>
        <h1>{theme.title}</h1>
      </div>

      {theme.theory && (
        <div className="theory">
          <button className="theory-toggle" onClick={() => setShowTheory((v) => !v)}>
            {showTheory ? '▾ Свернуть теорию' : '▸ Основная теория'}
          </button>
          {showTheory && <div className="theory-body"><MathText>{theme.theory}</MathText></div>}
        </div>
      )}

      <h2 className="subs-title">{theme.track === 'prep' ? 'Блоки' : 'Подтемы'}</h2>
      <div className="sub-list">
        {theme.subthemes.map((s) => {
          const done = s.problems.filter((p) => state.solved[p.id]?.status === 'solved').length
          const frac = s.problems.length ? done / s.problems.length : 0
          return (
            <Link key={s.id} to={`/theme/${theme.id}/${s.id}`} className="sub-item">
              <div className="sub-item-main">
                <h3>{s.title}</h3>
                <p className="muted">{s.problems.length} задач · решено {done}</p>
              </div>
              <ProgressRing value={frac} size={40} stroke={4}
                label={<span style={{ fontSize: 10 }}>{Math.round(frac * 100)}%</span>} />
            </Link>
          )
        })}
      </div>
    </div>
  )
}
