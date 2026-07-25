import { Link } from 'react-router-dom'
import { useApp } from '../lib/context.js'
import { courseThemes, themeMeta } from '../lib/content.js'
import { ProgressRing } from '../components/UI.jsx'

export default function Themes() {
  const { content, state } = useApp()
  const themes = courseThemes(content)

  return (
    <div className="page">
      <h1>Темы курса</h1>
      <p className="muted">Теория «от топ-преподавателей» и задачи по 14 ключевым темам олимпиадной математики.</p>
      <div className="theme-grid">
        {themes.map((t) => {
          const probs = t.subthemes.flatMap((s) => s.problems)
          const done = probs.filter((p) => state.solved[p.id]?.status === 'solved').length
          const meta = themeMeta(t.id)
          const frac = probs.length ? done / probs.length : 0
          return (
            <Link key={t.id} to={`/theme/${t.id}`} className="theme-card" style={{ '--tc': meta.color }}>
              <div className="theme-card-icon">{meta.icon}</div>
              <div className="theme-card-main">
                <h3>{t.title}</h3>
                <p className="muted">{t.subthemes.length} подтем · {probs.length} задач</p>
              </div>
              <ProgressRing value={frac} size={44} stroke={5}
                label={<span style={{ fontSize: 11 }}>{Math.round(frac * 100)}%</span>} />
            </Link>
          )
        })}
      </div>
    </div>
  )
}
