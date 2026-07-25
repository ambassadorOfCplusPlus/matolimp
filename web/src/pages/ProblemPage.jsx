import { useParams, Link, useNavigate } from 'react-router-dom'
import { useState, useMemo, useEffect } from 'react'
import { useApp } from '../lib/context.js'
import { checkAnswer, answerHint } from '../lib/answer.js'
import MathText from '../components/MathText.jsx'
import { DifficultyBadge, KindTag, PatternChip } from '../components/UI.jsx'

const MAX_ATTEMPTS = 3

export default function ProblemPage() {
  const { problemId } = useParams()
  const navigate = useNavigate()
  const { content, state, solve, attempt, toggleBookmark } = useApp()
  const problem = content.problemById.get(problemId)

  const sub = problem ? content.subthemeById.get(problem.subthemeId) : null
  const theme = problem ? content.themeById.get(problem.themeId) : null
  const siblings = sub?.problems || []
  const idx = siblings.findIndex((p) => p.id === problemId)
  const prev = idx > 0 ? siblings[idx - 1] : null
  const next = idx >= 0 && idx < siblings.length - 1 ? siblings[idx + 1] : null

  const [input, setInput] = useState('')
  const [tries, setTries] = useState(0)
  const [feedback, setFeedback] = useState(null) // 'correct'|'wrong'
  const [revealed, setRevealed] = useState(false)

  const status = state.solved[problemId]?.status
  const solvedAlready = status === 'solved'

  useEffect(() => {
    setInput(''); setTries(0); setFeedback(null); setRevealed(solvedAlready)
  }, [problemId]) // eslint-disable-line

  const locked = tries >= MAX_ATTEMPTS || solvedAlready

  function submit() {
    if (locked) return
    const ok = checkAnswer(problem.answer, input)
    if (ok) {
      setFeedback('correct'); setRevealed(true); solve(problem, 'solved')
    } else {
      const t = tries + 1
      setTries(t); setFeedback('wrong'); attempt(problem)
      if (t >= MAX_ATTEMPTS) setRevealed(true)
    }
  }

  function selfAssess(kind) {
    solve(problem, kind)
    setRevealed(true)
  }

  if (!problem) return <div className="page"><h2>Задача не найдена</h2><Link to="/">На главную</Link></div>

  return (
    <div className="page problem-page">
      <div className="crumbs">
        {sub && theme
          ? <Link to={`/theme/${theme.id}/${sub.id}`} className="link">← {sub.title}</Link>
          : <Link to="/" className="link">← Назад</Link>}
        <button className={'bm-btn' + (state.bookmarks[problemId] ? ' on' : '')}
          onClick={() => toggleBookmark(problemId)} title="Закладка">
          {state.bookmarks[problemId] ? '★ В закладках' : '☆ В закладки'}
        </button>
      </div>

      <div className="problem-meta">
        <KindTag kind={problem.kind} />
        <DifficultyBadge value={problem.difficulty} />
        {solvedAlready && <span className="solved-badge">✓ решено</span>}
      </div>

      <div className="statement card">
        <MathText>{problem.statement}</MathText>
      </div>

      {problem.kind === 'CLOSED' ? (
        <div className="answer-block">
          <label className="muted small">Ответ ({answerHint(problem.answer)})</label>
          <div className="answer-row">
            <input
              className={'answer-input' + (feedback === 'wrong' ? ' shake' : '')}
              value={input}
              disabled={locked}
              placeholder="Введите ответ"
              onChange={(e) => { setInput(e.target.value); setFeedback(null) }}
              onKeyDown={(e) => e.key === 'Enter' && submit()}
            />
            <button className="btn btn-primary" onClick={submit} disabled={locked || !input.trim()}>
              Проверить
            </button>
          </div>
          <div className="attempts">
            {!solvedAlready && Array.from({ length: MAX_ATTEMPTS }, (_, i) => (
              <span key={i} className={'attempt-dot' + (i < tries ? ' used' : '')} />
            ))}
            {feedback === 'correct' && <span className="fb ok">Верно! 🎉</span>}
            {feedback === 'wrong' && !locked && <span className="fb bad">Неверно, попробуйте ещё ({MAX_ATTEMPTS - tries})</span>}
            {feedback === 'wrong' && locked && <span className="fb bad">Попытки закончились — смотрите решение</span>}
          </div>
        </div>
      ) : (
        <div className="answer-block">
          <p className="muted small">Задача на доказательство. Решите, затем сверьтесь и оцените себя.</p>
          {!revealed && (
            <button className="btn btn-ghost" onClick={() => setRevealed(true)}>Показать решение</button>
          )}
        </div>
      )}

      {revealed && (
        <div className="solution card">
          <h3>Решение</h3>
          <MathText>{problem.solution}</MathText>
          {problem.pattern && (
            <div className="pattern-line">Приём: <PatternChip pattern={problem.pattern} /></div>
          )}
          {problem.kind === 'OPEN' && !solvedAlready && (
            <div className="self-assess">
              <span className="muted small">Как справились?</span>
              <div className="sa-row">
                <button className="btn sa-ok" onClick={() => selfAssess('solved')}>Решил сам</button>
                <button className="btn sa-part" onClick={() => selfAssess('partial')}>Частично</button>
                <button className="btn sa-no" onClick={() => selfAssess('attempt')}>Не вышло</button>
              </div>
            </div>
          )}
        </div>
      )}

      <div className="prevnext">
        <button className="btn btn-ghost" disabled={!prev} onClick={() => prev && navigate(`/problem/${prev.id}`)}>← Пред.</button>
        <span className="muted small">{idx >= 0 ? idx + 1 : '—'} / {siblings.length}</span>
        <button className="btn btn-ghost" disabled={!next} onClick={() => next && navigate(`/problem/${next.id}`)}>След. →</button>
      </div>
    </div>
  )
}
