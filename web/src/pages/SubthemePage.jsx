import { useParams, Link } from 'react-router-dom'
import { useApp } from '../lib/context.js'
import MathText from '../components/MathText.jsx'
import { ProblemCard } from '../components/UI.jsx'
import { useState } from 'react'

export default function SubthemePage() {
  const { themeId, subId } = useParams()
  const { content } = useApp()
  const theme = content.themeById.get(themeId)
  const sub = content.subthemeById.get(subId)
  const [showTheory, setShowTheory] = useState(true)

  if (!theme || !sub) return <div className="page"><h2>Не найдено</h2><Link to="/themes">К темам</Link></div>

  return (
    <div className="page">
      <div className="crumbs">
        <Link to={`/theme/${theme.id}`} className="link">← {theme.title}</Link>
      </div>
      <h1>{sub.title}</h1>

      {sub.theory && sub.theory.trim() && (
        <div className="theory">
          <button className="theory-toggle" onClick={() => setShowTheory((v) => !v)}>
            {showTheory ? '▾ Свернуть теорию' : '▸ Теория подтемы'}
          </button>
          {showTheory && <div className="theory-body"><MathText>{sub.theory}</MathText></div>}
        </div>
      )}

      <h2 className="subs-title">Задачи · {sub.problems.length}</h2>
      <div className="pcard-grid">
        {sub.problems.map((p, i) => <ProblemCard key={p.id} problem={p} index={i} />)}
      </div>
    </div>
  )
}
