import { useEffect, useState } from 'react'
import { Routes, Route, NavLink, Link } from 'react-router-dom'
import { AppContext } from './lib/context.js'
import { loadContent } from './lib/content.js'
import { useProgress, levelFor } from './lib/progress.js'
import Home from './pages/Home.jsx'
import Themes from './pages/Themes.jsx'
import Prep from './pages/Prep.jsx'
import Razbor from './pages/Razbor.jsx'
import ThemePage from './pages/ThemePage.jsx'
import SubthemePage from './pages/SubthemePage.jsx'
import ProblemPage from './pages/ProblemPage.jsx'
import SearchPage from './pages/SearchPage.jsx'

const NAV = [
  { to: '/', label: 'Обзор', icon: '🏠', end: true },
  { to: '/themes', label: 'Темы', icon: '📚' },
  { to: '/prep', label: 'Подготовка', icon: '🏋️' },
  { to: '/razbor', label: 'Разбор', icon: '🔎' },
  { to: '/search', label: 'Поиск', icon: '🔍' },
]

export default function App() {
  const [content, setContent] = useState(null)
  const [error, setError] = useState(null)
  const progress = useProgress()

  useEffect(() => {
    loadContent().then(setContent).catch((e) => setError(e.message))
  }, [])

  if (error) return <div className="loader"><p>Ошибка: {error}</p></div>
  if (!content) return (
    <div className="loader">
      <div className="spinner" />
      <p>Загружаем задачи…</p>
    </div>
  )

  const lvl = levelFor(progress.state.xp)

  return (
    <AppContext.Provider value={{ content, ...progress }}>
      <div className="app">
        <header className="topbar">
          <Link to="/" className="brand">
            <span className="brand-mark">∑</span>
            <span className="brand-name">МатОлимп</span>
          </Link>
          <div className="topbar-right">
            <div className="chip chip-xp" title="Опыт и уровень">
              <b>Ур. {lvl.level}</b>
              <span className="xp-bar"><span style={{ width: `${Math.round(lvl.progress * 100)}%` }} /></span>
              <span className="muted">{progress.state.xp} XP</span>
            </div>
            <div className="chip chip-streak" title="Серия дней подряд">🔥 {progress.state.streak.current}</div>
            <button className="icon-btn" onClick={progress.toggleTheme} title="Тема оформления">
              {progress.state.theme === 'dark' ? '☀️' : '🌙'}
            </button>
          </div>
        </header>

        <main className="content">
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/themes" element={<Themes />} />
            <Route path="/prep" element={<Prep />} />
            <Route path="/razbor" element={<Razbor />} />
            <Route path="/search" element={<SearchPage />} />
            <Route path="/theme/:themeId" element={<ThemePage />} />
            <Route path="/theme/:themeId/:subId" element={<SubthemePage />} />
            <Route path="/problem/:problemId" element={<ProblemPage />} />
            <Route path="*" element={<div className="page"><h2>Страница не найдена</h2><Link to="/">На главную</Link></div>} />
          </Routes>
        </main>

        <nav className="bottomnav">
          {NAV.map((n) => (
            <NavLink key={n.to} to={n.to} end={n.end}
              className={({ isActive }) => 'navitem' + (isActive ? ' active' : '')}>
              <span className="navicon">{n.icon}</span>
              <span className="navlabel">{n.label}</span>
            </NavLink>
          ))}
        </nav>
      </div>
    </AppContext.Provider>
  )
}
