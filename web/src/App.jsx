import { useEffect, useState } from 'react'
import { Routes, Route, NavLink, Link, useLocation } from 'react-router-dom'
import { AppContext } from './lib/context.js'
import { loadContent } from './lib/content.js'
import { useProgress, levelFor } from './lib/progress.js'
import { ProgressRing } from './components/UI.jsx'
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

function isWide() {
  return typeof window !== 'undefined' && window.innerWidth >= 900
}

export default function App() {
  const [content, setContent] = useState(null)
  const [error, setError] = useState(null)
  const [navOpen, setNavOpen] = useState(isWide())
  const progress = useProgress()
  const location = useLocation()

  useEffect(() => {
    loadContent().then(setContent).catch((e) => setError(e.message))
  }, [])

  // На узком экране закрываем панель при переходе по разделу
  useEffect(() => {
    if (!isWide()) setNavOpen(false)
  }, [location.pathname])

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
      <div className={'app' + (navOpen ? ' nav-open' : '')}>
        <header className="topbar">
          <button className="hamburger" onClick={() => setNavOpen((v) => !v)}
            title="Меню" aria-label="Меню">
            <span /><span /><span />
          </button>
          <Link to="/" className="brand">
            <span className="brand-mark">∑</span>
            <span className="brand-name">МатОлимп</span>
          </Link>
          <div className="topbar-right">
            <div className="chip chip-streak" title="Серия дней подряд">🔥 {progress.state.streak.current}</div>
            <button className="icon-btn" onClick={progress.toggleTheme} title="Тема оформления">
              {progress.state.theme === 'dark' ? '☀️' : '🌙'}
            </button>
          </div>
        </header>

        {/* Выдвижная панель-дашборд слева */}
        <aside className={'sidebar' + (navOpen ? ' open' : '')}>
          <div className="sidebar-dash">
            <ProgressRing value={lvl.progress} size={64} stroke={7}
              label={<b style={{ fontSize: 18 }}>{lvl.level}</b>} />
            <div className="sidebar-dash-info">
              <div className="sidebar-level">Уровень {lvl.level}</div>
              <div className="xp-bar wide"><span style={{ width: `${Math.round(lvl.progress * 100)}%` }} /></div>
              <div className="muted small">{progress.state.xp} XP · {lvl.inLevel}/{lvl.toNext}</div>
              <div className="muted small">🔥 серия {progress.state.streak.current} · рекорд {progress.state.streak.best}</div>
            </div>
          </div>

          <nav className="sidenav">
            {NAV.map((n) => (
              <NavLink key={n.to} to={n.to} end={n.end}
                className={({ isActive }) => 'sidenav-item' + (isActive ? ' active' : '')}>
                <span className="navicon">{n.icon}</span>
                <span className="navlabel">{n.label}</span>
              </NavLink>
            ))}
          </nav>

          <button className="sidebar-theme" onClick={progress.toggleTheme}>
            {progress.state.theme === 'dark' ? '☀️ Светлая тема' : '🌙 Тёмная тема'}
          </button>
        </aside>

        {/* Затемнение под панелью на узком экране */}
        <div className="scrim" onClick={() => setNavOpen(false)} />

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
      </div>
    </AppContext.Provider>
  )
}
