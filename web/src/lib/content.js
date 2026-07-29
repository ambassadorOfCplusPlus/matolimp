// Загрузка и индексация контента из того же seed.json, что и Android.
// seed.json импортируется через ?url — Vite добавляет хэш в имя файла,
// поэтому обновление контента гарантированно сбрасывает кэш у пользователей.
import seedUrl from '../seed.json?url'

let _cache = null

const THEME_META = {
  induction:    { icon: '🪜', color: '#6d5cf5' },
  dirichlet:    { icon: '🕳️', color: '#f5734c' },
  divisibility: { icon: '➗', color: '#12b886' },
  invariants:   { icon: '⚖️', color: '#e64980' },
  combinatorics:{ icon: '🎯', color: '#4c6ef5' },
  graphs:       { icon: '🕸️', color: '#7048e8' },
  inequalities: { icon: '📉', color: '#f59f00' },
  games:        { icon: '♟️', color: '#20c997' },
  logic:        { icon: '🧩', color: '#845ef7' },
  geometry:     { icon: '📐', color: '#22b8cf' },
  'word-problems': { icon: '📖', color: '#fa5252' },
  dissections:  { icon: '✂️', color: '#f76707' },
  diophantine:  { icon: '🔢', color: '#37b24d' },
  algebra:      { icon: '𝑥', color: '#4263eb' },
  'prep-muni':  { icon: '🏙️', color: '#0ca678' },
  'prep-region':{ icon: '🗺️', color: '#5c7cfa' },
  'prep-final': { icon: '🏆', color: '#e8b923' },
  'tournament-towns': { icon: '🏙️', color: '#e8590c' },
}

export function themeMeta(id) {
  return THEME_META[id] || { icon: '∑', color: '#6d5cf5' }
}

export async function loadContent() {
  if (_cache) return _cache
  const res = await fetch(seedUrl)
  if (!res.ok) throw new Error('Не удалось загрузить контент')
  const data = await res.json()
  const themes = (data.themes || []).slice().sort((a, b) => (a.order || 0) - (b.order || 0))
  const problemById = new Map()
  const subthemeById = new Map()
  const themeById = new Map()
  for (const t of themes) {
    themeById.set(t.id, t)
    t.subthemes = (t.subthemes || []).slice().sort((a, b) => (a.order || 0) - (b.order || 0))
    for (const s of t.subthemes) {
      s.themeId = t.id
      subthemeById.set(s.id, s)
      s.problems = (s.problems || []).slice().sort((a, b) => (a.order || 0) - (b.order || 0))
      for (const p of s.problems) {
        p.themeId = t.id
        p.subthemeId = s.id
        problemById.set(p.id, p)
      }
    }
  }
  _cache = { themes, themeById, subthemeById, problemById }
  return _cache
}

export function courseThemes(content) {
  return content.themes.filter((t) => (t.track || 'course') === 'course')
}
export function prepThemes(content) {
  return content.themes.filter((t) => t.track === 'prep')
}

// Уникальные приёмы (patterns) по всем задачам
export function allPatterns(content) {
  const set = new Set()
  for (const p of content.problemById.values()) if (p.pattern) set.add(p.pattern)
  return Array.from(set).sort()
}

export function allProblems(content) {
  return Array.from(content.problemById.values())
}
