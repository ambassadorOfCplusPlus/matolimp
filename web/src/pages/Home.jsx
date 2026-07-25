import { Link } from 'react-router-dom'
import { useApp } from '../lib/context.js'
import { levelFor } from '../lib/progress.js'
import { courseThemes, prepThemes, allProblems, themeMeta } from '../lib/content.js'
import { ProgressRing } from '../components/UI.jsx'

function countSolved(state, problems) {
  let solved = 0
  for (const p of problems) if (state.solved[p.id]?.status === 'solved') solved++
  return solved
}

export default function Home() {
  const { content, state } = useApp()
  const lvl = levelFor(state.xp)
  const all = allProblems(content)
  const solved = countSolved(state, all)
  const bookmarks = Object.keys(state.bookmarks).filter((id) => content.problemById.has(id))

  const lastId = Object.entries(state.solved)
    .sort((a, b) => (b[1].ts || 0) - (a[1].ts || 0))
    .map(([id]) => id)
    .find((id) => content.problemById.has(id))
  const last = lastId ? content.problemById.get(lastId) : null

  const course = courseThemes(content)
  const prep = prepThemes(content)

  const stats = [
    { label: 'Решено задач', value: solved, sub: `из ${all.length}` },
    { label: 'Опыт', value: state.xp, sub: `уровень ${lvl.level}` },
    { label: 'Серия', value: state.streak.current, sub: `рекорд ${state.streak.best}` },
    { label: 'Закладок', value: bookmarks.length, sub: 'сохранено' },
  ]

  return (
    <div className="page">
      <section className="hero">
        <div className="hero-ring">
          <ProgressRing value={lvl.progress} size={92} stroke={9}
            label={<b style={{ fontSize: 22 }}>{lvl.level}</b>} />
        </div>
        <div className="hero-text">
          <h1>Готовимся к олимпиадам</h1>
          <p className="muted">
            Уровень {lvl.level} · {lvl.inLevel}/{lvl.toNext} XP до следующего · серия {state.streak.current} 🔥
          </p>
          {last ? (
            <Link className="btn btn-primary" to={`/problem/${last.id}`}>Продолжить решать →</Link>
          ) : (
            <Link className="btn btn-primary" to="/themes">Начать с теории →</Link>
          )}
        </div>
      </section>

      <section className="stats-grid">
        {stats.map((s) => (
          <div className="stat" key={s.label}>
            <div className="stat-value">{s.value}</div>
            <div className="stat-label">{s.label}</div>
            <div className="stat-sub muted">{s.sub}</div>
          </div>
        ))}
      </section>

      <section>
        <div className="section-head">
          <h2>Темы курса</h2>
          <Link to="/themes" className="link">все {course.length} →</Link>
        </div>
        <div className="theme-row">
          {course.slice(0, 6).map((t) => {
            const probs = t.subthemes.flatMap((s) => s.problems)
            const done = countSolved(state, probs)
            const meta = themeMeta(t.id)
            return (
              <Link key={t.id} to={`/theme/${t.id}`} className="theme-mini" style={{ '--tc': meta.color }}>
                <span className="theme-mini-icon">{meta.icon}</span>
                <span className="theme-mini-title">{t.title}</span>
                <span className="theme-mini-sub muted">{done}/{probs.length}</span>
              </Link>
            )
          })}
        </div>
      </section>

      <section>
        <div className="section-head">
          <h2>Подготовка к этапам</h2>
          <Link to="/prep" className="link">открыть →</Link>
        </div>
        <div className="theme-row">
          {prep.map((t) => {
            const probs = t.subthemes.flatMap((s) => s.problems)
            const done = countSolved(state, probs)
            const meta = themeMeta(t.id)
            return (
              <Link key={t.id} to={`/theme/${t.id}`} className="theme-mini" style={{ '--tc': meta.color }}>
                <span className="theme-mini-icon">{meta.icon}</span>
                <span className="theme-mini-title">{t.title}</span>
                <span className="theme-mini-sub muted">{done}/{probs.length}</span>
              </Link>
            )
          })}
          <Link to="/razbor" className="theme-mini" style={{ '--tc': '#f5734c' }}>
            <span className="theme-mini-icon">🔎</span>
            <span className="theme-mini-title">Разбор олимпиад по годам</span>
            <span className="theme-mini-sub muted">регион 2016–2026</span>
          </Link>
        </div>
      </section>
    </div>
  )
}
