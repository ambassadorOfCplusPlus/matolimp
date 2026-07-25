import katex from 'katex'
import 'katex/dist/katex.min.css'
import Figure from './Figure.jsx'

function renderTex(tex, displayMode) {
  try {
    return katex.renderToString(tex, {
      displayMode,
      throwOnError: false,
      strict: false,
      trust: true,
      macros: { '\\dfrac': '\\frac', '\\ne': '\\neq', '\\le': '\\leq', '\\ge': '\\geq' },
    })
  } catch {
    return null
  }
}

// Инлайн-математика внутри текстового куска: $...$
function InlineText({ text, keyBase }) {
  const parts = []
  const re = /\$([^$]+?)\$/g
  let last = 0, m, i = 0
  while ((m = re.exec(text)) !== null) {
    if (m.index > last) parts.push(<span key={`${keyBase}-t${i}`}>{text.slice(last, m.index)}</span>)
    const html = renderTex(m[1], false)
    parts.push(
      html
        ? <span key={`${keyBase}-m${i}`} dangerouslySetInnerHTML={{ __html: html }} />
        : <code key={`${keyBase}-m${i}`}>{m[1]}</code>
    )
    last = m.index + m[0].length
    i++
  }
  if (last < text.length) parts.push(<span key={`${keyBase}-t${i}`}>{text.slice(last)}</span>)
  return parts
}

// Текстовый блок с абзацами (двойной перевод строки) и инлайн-математикой
function TextBlock({ text, keyBase }) {
  const paras = text.split(/\n{2,}/).map((p) => p.trim()).filter(Boolean)
  return paras.map((para, pi) => (
    <p key={`${keyBase}-p${pi}`} className="mt-para">
      {para.split(/\n/).map((line, li, arr) => (
        <span key={li}>
          <InlineText text={line} keyBase={`${keyBase}-p${pi}-l${li}`} />
          {li < arr.length - 1 && <br />}
        </span>
      ))}
    </p>
  ))
}

// Основной компонент: разбивает на display-формулы, фигуры и текст
export default function MathText({ children, className = '' }) {
  const src = typeof children === 'string' ? children : ''
  if (!src) return null

  const nodes = []
  // Токенизируем по $$...$$ и [[fig:...]]
  const re = /\$\$([\s\S]+?)\$\$|\[\[fig:([a-zA-Z0-9_-]+)\]\]/g
  let last = 0, m, i = 0
  while ((m = re.exec(src)) !== null) {
    if (m.index > last) {
      nodes.push(<TextBlock key={`b${i}`} text={src.slice(last, m.index)} keyBase={`b${i}`} />)
    }
    if (m[1] !== undefined) {
      const html = renderTex(m[1].trim(), true)
      nodes.push(
        html
          ? <div key={`d${i}`} className="mt-display" dangerouslySetInnerHTML={{ __html: html }} />
          : <pre key={`d${i}`} className="mt-display">{m[1]}</pre>
      )
    } else if (m[2] !== undefined) {
      nodes.push(<Figure key={`f${i}`} name={m[2]} />)
    }
    last = m.index + m[0].length
    i++
  }
  if (last < src.length) nodes.push(<TextBlock key={`b${i}`} text={src.slice(last)} keyBase={`b${i}`} />)

  return <div className={`mathtext ${className}`}>{nodes}</div>
}
