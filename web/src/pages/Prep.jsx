import { Link } from 'react-router-dom'
import { useApp } from '../lib/context.js'
import { prepThemes, themeMeta } from '../lib/content.js'
import { ProgressRing } from '../components/UI.jsx'

export default function Prep() {
  const { content, state } = useApp()
  const themes = prepThemes(content)

  return (
    <div className="page">
      <h1>Подготовка к этапам</h1>
      <p className="muted">Материалы и реальные задачи муниципального и регионального этапов ВсОШ по годам.</p>
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
                <p className="muted">{t.subthemes.length} блоков · {probs.length} задач</p>
              </div>
              <ProgressRing value={frac} size={44} stroke={5}
                label={<span style={{ fontSize: 11 }}>{Math.round(frac * 100)}%</span>} />
            </Link>
          )
        })}
      </div>

      <div className="callout">
        <div className="callout-icon">🔎</div>
        <div>
          <b>Разбор олимпиад по годам</b>
          <p className="muted">Полные разборы всех задач туров: региональный этап 2016–2026 и муниципальный 2021–2024 — с приёмами.</p>
          <Link to="/razbor" className="btn btn-ghost">Открыть разбор →</Link>
        </div>
      </div>
    </div>
  )
}
