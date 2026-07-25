// Проверка ответов — веб-порт domain/AnswerChecker.kt.
// Канонизируем и пользовательский ввод, и эталон, сравниваем.

function stripSpaces(s) {
  return s.replace(/\s+/g, '')
}

function normalizeNumberText(s) {
  return stripSpaces(String(s))
    .replace(',', '.')
    .replace(/^\+/, '')
}

function canonInteger(s) {
  const t = normalizeNumberText(s)
  if (!/^-?\d+$/.test(t)) return null
  try {
    // BigInt: корректно для длинных чисел, убирает ведущие нули и «-0»
    return BigInt(t).toString()
  } catch {
    return null
  }
}

function canonDecimal(s) {
  const t = normalizeNumberText(s)
  if (!/^-?\d*\.?\d+$/.test(t)) return null
  const v = Number(t)
  if (!isFinite(v)) return null
  // округление до 6 знаков, убираем хвостовые нули
  return String(Math.round(v * 1e6) / 1e6)
}

function gcd(a, b) {
  a = a < 0n ? -a : a
  b = b < 0n ? -b : b
  while (b) { [a, b] = [b, a % b] }
  return a
}

function canonRational(s) {
  const t = normalizeNumberText(s)
  const m = t.match(/^(-?\d+)\/(-?\d+)$/)
  if (m) {
    let num = BigInt(m[1]), den = BigInt(m[2])
    if (den === 0n) return null
    if (den < 0n) { num = -num; den = -den }
    const g = gcd(num, den) || 1n
    return `${num / g}/${den / g}`
  }
  // целое как дробь
  const asInt = canonInteger(t)
  if (asInt !== null) return `${asInt}/1`
  // десятичная как дробь
  const dec = canonDecimal(t)
  if (dec !== null) {
    const [ip, fp = ''] = dec.replace('-', '').split('.')
    const sign = dec.startsWith('-') ? -1n : 1n
    let den = 10n ** BigInt(fp.length)
    let num = sign * BigInt(ip + fp)
    const g = gcd(num, den) || 1n
    return `${num / g}/${den / g}`
  }
  return null
}

function canonString(s) {
  return stripSpaces(String(s)).toLowerCase().replace('ё', 'е')
}

function splitSet(s) {
  return String(s)
    .split(/[;,\s]+/)
    .map((x) => x.trim())
    .filter(Boolean)
}

// Канонизация одного значения по типу
function canonByType(type, value) {
  switch (type) {
    case 'INTEGER': return canonInteger(value)
    case 'DECIMAL': return canonDecimal(value)
    case 'RATIONAL': return canonRational(value)
    case 'STRING': return canonString(value)
    default: {
      // авто: пробуем число, потом строку
      return canonInteger(value) ?? canonRational(value) ?? canonDecimal(value) ?? canonString(value)
    }
  }
}

function matchOne(type, accepted, user) {
  if (type === 'SET') {
    const want = splitSet(accepted).map((x) => canonRational(x) ?? canonString(x)).sort()
    const got = splitSet(user).map((x) => canonRational(x) ?? canonString(x)).sort()
    if (want.length !== got.length || want.length === 0) return false
    return want.every((v, i) => v === got[i])
  }
  const a = canonByType(type, accepted)
  const b = canonByType(type, user)
  return a !== null && a === b
}

// Главная функция. answer = { type, accepted: [...] }
export function checkAnswer(answer, userInput) {
  if (!answer || !userInput || !String(userInput).trim()) return false
  const type = answer.type || 'INTEGER'
  const accepted = Array.isArray(answer.accepted) ? answer.accepted : [answer.accepted]
  return accepted.some((acc) => matchOne(type, acc, userInput))
}

export function answerHint(answer) {
  if (!answer) return ''
  const t = answer.type
  if (t === 'INTEGER') return 'целое число'
  if (t === 'DECIMAL') return 'десятичная дробь'
  if (t === 'RATIONAL') return 'дробь вида a/b'
  if (t === 'SET') return 'несколько значений через запятую'
  return 'ответ'
}
