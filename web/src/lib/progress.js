// Прогресс пользователя в localStorage: статусы задач, XP, уровень, серия, закладки.
import { useState, useEffect, useCallback } from 'react'

const KEY = 'matolimp:v1'

const DEFAULT = {
  solved: {},      // id -> { status: 'solved'|'partial', attempts, ts }
  bookmarks: {},   // id -> true
  xp: 0,
  streak: { current: 0, best: 0, last: null }, // last = 'YYYY-MM-DD'
  theme: 'light',
}

function load() {
  try {
    const raw = localStorage.getItem(KEY)
    if (!raw) return { ...DEFAULT }
    return { ...DEFAULT, ...JSON.parse(raw) }
  } catch {
    return { ...DEFAULT }
  }
}

function save(state) {
  try { localStorage.setItem(KEY, JSON.stringify(state)) } catch {}
}

function todayStr() {
  const d = new Date()
  const p = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`
}

function daysBetween(a, b) {
  const da = new Date(a + 'T00:00:00'), db = new Date(b + 'T00:00:00')
  return Math.round((db - da) / 86400000)
}

// XP за решение
export function xpFor(problem, status) {
  const d = problem.difficulty || 3
  if (problem.kind === 'CLOSED') return status === 'solved' ? 10 + d * 5 : 0
  // OPEN — самооценка
  if (status === 'solved') return 12 + d * 4
  if (status === 'partial') return 6 + d * 2
  return 0
}

export function levelFor(xp) {
  // растущий порог: L требует L*120 суммарно
  let level = 1, need = 120, acc = 0
  while (xp >= acc + need) { acc += need; level += 1; need = level * 120 }
  return { level, inLevel: xp - acc, toNext: need, progress: (xp - acc) / need }
}

function bumpStreak(streak) {
  const t = todayStr()
  if (streak.last === t) return streak
  if (streak.last && daysBetween(streak.last, t) === 1) {
    const current = streak.current + 1
    return { current, best: Math.max(current, streak.best), last: t }
  }
  return { current: 1, best: Math.max(1, streak.best), last: t }
}

// React-хук: единый стор
export function useProgress() {
  const [state, setState] = useState(load)

  useEffect(() => { save(state) }, [state])

  useEffect(() => {
    document.documentElement.dataset.theme = state.theme
  }, [state.theme])

  const solve = useCallback((problem, status) => {
    setState((s) => {
      const prev = s.solved[problem.id]
      const wasSolved = prev && prev.status === 'solved'
      const gain = wasSolved ? 0 : xpFor(problem, status)
      const attempts = (prev?.attempts || 0) + 1
      return {
        ...s,
        solved: { ...s.solved, [problem.id]: { status, attempts, ts: Date.now() } },
        xp: s.xp + gain,
        streak: bumpStreak(s.streak),
      }
    })
  }, [])

  const attempt = useCallback((problem) => {
    setState((s) => {
      const prev = s.solved[problem.id]
      if (prev?.status === 'solved') return s
      return {
        ...s,
        solved: { ...s.solved, [problem.id]: { status: prev?.status || 'attempt', attempts: (prev?.attempts || 0) + 1, ts: Date.now() } },
      }
    })
  }, [])

  const toggleBookmark = useCallback((id) => {
    setState((s) => {
      const bookmarks = { ...s.bookmarks }
      if (bookmarks[id]) delete bookmarks[id]; else bookmarks[id] = true
      return { ...s, bookmarks }
    })
  }, [])

  const toggleTheme = useCallback(() => {
    setState((s) => ({ ...s, theme: s.theme === 'light' ? 'dark' : 'light' }))
  }, [])

  const reset = useCallback(() => setState({ ...DEFAULT, theme: state.theme }), [state.theme])

  return { state, solve, attempt, toggleBookmark, toggleTheme, reset }
}
