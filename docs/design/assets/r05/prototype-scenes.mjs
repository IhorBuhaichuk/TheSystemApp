// Native SVG transport for editable Figma layers, not a web/app UI implementation.
export const palette = Object.freeze({
  background: '#020407', surface: '#0B1119', border: '#293D49',
  primary: '#F6FAFF', secondary: '#A8B5C4', muted: '#788596',
  cyan: '#1FE1FF', success: '#35E6A8', warning: '#F2BA68', error: '#FF7187'
});

const escape = value => String(value).replaceAll('&', '&amp;').replaceAll('<', '&lt;').replaceAll('>', '&gt;').replaceAll('"', '&quot;');
const text = (id, value, x, y, size = 16, color = palette.primary, weight = 400) =>
  `<text id="${id}" x="${x}" y="${y}" font-family="Roboto" font-size="${size}" font-weight="${weight}" fill="${color}">${escape(value)}</text>`;
const group = (id, x, y, content) => `<g id="${id}" transform="translate(${x} ${y})">${content}</g>`;
const panel = (id, w, h, active = false) => `<rect id="${id}" x=".5" y=".5" width="${w - 1}" height="${h - 1}" rx="18" fill="url(#plate)" stroke="${active ? palette.cyan : palette.border}"/>`;
const lines = (id, values, x, y, size = 14, color = palette.secondary) => values.map((s, i) => text(`${id}-${i}`, s, x, y + i * (size + 7), size, color)).join('');
const choice = (id, title, subtitle, selected = false) => ({
  height: 76,
  draw: w => panel(`${id}-surface`, w, 76, selected)
    + `<circle cx="24" cy="26" r="9" fill="none" stroke="${selected ? palette.cyan : palette.muted}" stroke-width="1.5"/>`
    + (selected ? `<circle cx="24" cy="26" r="4" fill="${palette.cyan}"/>` : '')
    + text(`${id}-title`, title, 46, 29, 16, palette.primary, 600)
    + text(`${id}-subtitle`, subtitle, 46, 53, 12)
});
const note = (id, content, tone = 'secondary') => ({height: content.length * 21 + 8, draw: () => lines(id, content, 0, 17, 14, palette[tone])});
const fieldSlot = (id = 'NameFieldSlot') => ({height: 72, draw: w => `<rect id="${id}" width="${w}" height="72" rx="12" fill="${palette.surface}" stroke="${palette.border}"/>`});
const bullet = (id, title, detail) => ({height: 64, draw: w => panel(`${id}-surface`, w, 64) + text(`${id}-title`, title, 16, 26, 15, palette.primary, 500) + text(`${id}-detail`, detail, 16, 48, 12, palette.secondary)});

const goals = [
  ['Сила', '5 підходів по 5'], ["М'язи", '4 підходи по 10'],
  ['Форма', '3 підходи по 15'], ['Стабільність', '3 підходи по 8']
];
const experiences = [['Новачок', 'Перші регулярні тренування'], ['Повертаюся', 'Після перерви'], ['Досвідчений', 'Є регулярна практика']];
const cycles = [['3-денний цикл', 'Тренування та відновлення'], ['4-денний цикл', 'Збалансований ритм'], ['5-денний цикл', 'Щільніший ритм']];

export const scenes = [
  {key: 'N00', title: 'Як вас звати?', step: 1, field: {label: "Ім'я", value: '', state: 'Default'}, primary: 'Далі', disabled: true, blocks: [fieldSlot()]},
  {key: 'N01', title: 'Як вас звати?', step: 1, keyboard: 'name', field: {label: "Ім'я", value: 'Ігор', state: 'Focused'}, primary: 'Готово', blocks: [fieldSlot()]},
  {key: 'N02', title: 'Як вас звати?', step: 1, field: {label: "Ім'я", value: 'Ігор', state: 'Default'}, primary: 'Далі', blocks: [fieldSlot()]},
  {key: 'N03', title: 'Як вас звати?', step: 1, field: {label: "Ім'я", value: '', state: 'Error'}, primary: 'Ввести ім’я', blocks: [fieldSlot(), note('NameValidation', ['Ім’я не може бути порожнім.'], 'error')]},
  ...goals.map((_, selected) => ({key: `G${selected}`, title: 'Головна ціль', step: 2, primary: 'Далі', back: 'N02', blocks: goals.map(([title, subtitle], i) => choice(`Goal-${i}`, title, subtitle, selected === i))})),
  ...[0, 1, 2].map(selected => ({key: `E${selected}`, title: 'Доступне обладнання', step: 3, primary: 'Далі', back: 'G3', blocks: [
    choice('Equipment-0', 'Гантелі', 'Домашні або регульовані', selected > 0),
    choice('Equipment-1', 'Лава', 'Опора для жимів і тяг', selected === 2),
    choice('Equipment-2', 'Штанга', 'Базові силові вправи'),
    choice('Equipment-3', 'Турнік', 'Підтягування'),
    choice('Equipment-4', 'Резинки', 'Мобільність та активація'),
    choice('Equipment-5', 'Зал і тренажери', 'Тренажери та блоки')
  ]})),
  ...experiences.map((_, selected) => ({key: `X${selected}`, title: 'Рівень досвіду', step: 4, primary: 'Далі', back: 'E2', blocks: experiences.map(([title, subtitle], i) => choice(`Experience-${i}`, title, subtitle, selected === i))})),
  ...cycles.map((_, selected) => ({key: `C${selected}`, title: 'Розклад тренувань', step: 5, primary: 'Перейти до плану', back: 'X0', blocks: cycles.map(([title, subtitle], i) => choice(`Cycle-${i}`, title, subtitle, selected === i))})),
  {key: 'OS', title: 'Зберігаємо план', step: 5, primary: 'Збереження…', disabled: true, blocks: [note('SavingPlan', ['Зберігаємо налаштування.'])]},
  {key: 'OE', title: 'План ще не збережено', step: 5, primary: 'Спробувати ще раз', back: 'C0', blocks: [note('SavePlanError', ['Відповіді збережені у формі.', 'Спробуйте повторити збереження.'], 'error')]},
  {key: 'PLAN', title: 'Тренування A', subtitle: 'Планове тренування · 45 хв', primary: 'Відкрити тренування', back: 'STATUS', blocks: [
    note('PlanSummary', ['5 вправ · 15 підходів']),
    bullet('Exercise-1', 'Присідання з гантелею', '3 підходи · 8 повторень'),
    bullet('Exercise-2', 'Жим гантелей лежачи', '3 підходи · 8 повторень'),
    bullet('Exercise-3', 'Тяга гантелі з опорою на лаву', '3 підходи · 8 повторень'),
    bullet('Exercise-4', 'Румунська тяга з гантелями', '3 підходи · 8 повторень'),
    bullet('Exercise-5', 'Планка на передпліччях', '3 підходи · час')
  ]},
  {key: 'W0', title: 'Тренування A', subtitle: '0 / 15 підходів записано', primary: 'Завершити тренування', disabled: true, back: 'PLAN', workout: 0, blocks: []},
  {key: 'WK', title: 'Тренування A', subtitle: 'Значення ще не позначені виконаними', primary: 'Готово', keyboard: 'number', back: 'W0', workout: 0, blocks: []},
  {key: 'WV', title: 'Тренування A', subtitle: '0 / 15 підходів записано', primary: 'Завершити тренування', disabled: true, back: 'PLAN', workout: 0, filled: true, blocks: []},
  {key: 'W1', title: 'Тренування A', subtitle: '1 / 15 підходів записано', primary: 'Завершити · 1 / 15', back: 'PLAN', workout: 1, filled: true, blocks: []},
  {key: 'W2', title: 'Тренування A', subtitle: '2 / 15 підходів записано', primary: 'Завершити · 2 / 15', back: 'PLAN', workout: 2, filled: true, blocks: []},
  {key: 'W3', title: 'Тренування A', subtitle: '3 / 15 підходів записано', primary: 'Завершити · 3 / 15', back: 'PLAN', workout: 3, filled: true, blocks: []},
  {key: 'DR', title: 'Продовжити тренування?', primary: 'Продовжити', back: 'STATUS', blocks: [bullet('DraftWorkout', 'Тренування A', 'Чернетка · 3 / 15 підходів'), note('DraftNote', ['Введені дані збережені.', 'Тренування ще не завершено.'])]},
  {key: 'DC', title: 'Знайдено іншу чернетку', primary: 'Зберегти чернетку', back: 'PLAN', blocks: [note('ConflictNote', ['Чернетка не відповідає цьому плану.', 'Вона не буде змішана з новим', 'тренуванням.']), bullet('DiscardEntry', 'Видалити й відкрити поточне', 'Потрібне окреме підтвердження')]},
  {key: 'DD', title: 'Видалити чернетку?', primary: 'Видалити чернетку', back: 'DR', danger: true, blocks: [note('DiscardWarning', ['Незавершені підходи цієї', 'чернетки буде видалено.', 'Збережена історія не зміниться.'])]},
  {key: 'WS', title: 'Зберігаємо тренування', primary: 'Збереження…', disabled: true, blocks: [note('FinishSaving', ['Записуємо виконані підходи.', 'Підсумок з’явиться після збереження.'])]},
  {key: 'WE', title: 'Ще не збережено', primary: 'Спробувати ще раз', back: 'W3', blocks: [note('FinishError', ['Не вдалося зберегти тренування.', 'Введені дані та чернетка збережені.'], 'error')]},
  {key: 'RP', title: 'Тренування збережено', subtitle: 'Локальний підсумок', primary: 'Прийняти', back: 'STATUS', blocks: [
    bullet('CompletionFacts', 'Виконано 3 із 15 підходів', 'Часткове тренування · 1 із 5 вправ'),
    bullet('ProgressFacts', 'Зберегти навантаження', 'Поки недостатньо даних для збільшення'),
    note('Verdict', ['Рішення системи', 'Продовжити з поточним навантаженням.', 'Невиконані підходи не зараховані.']),
    note('NextTime', ['Наступного разу', 'Повторити запланований обсяг.']),
    note('LocalOnly', ['Підсумок сформовано на пристрої.', 'AI-аналіз недоступний.'])
  ]}
];

for (const count of [1, 2]) {
  const report = scenes.find(scene => scene.key === 'RP');
  scenes.push({
    ...report, key: `RP${count}`,
    blocks: [bullet('CompletionFacts', `Виконано ${count} із 15 підходів`, 'Часткове тренування · 1 із 5 вправ'), ...report.blocks.slice(1)]
  });
  scenes.push({...scenes.find(scene => scene.key === 'WS'), key: `WS${count}`});
}

function keyboard(mode, width, height) {
  const numeric = mode === 'number';
  const rows = numeric ? ['1 2 3', '4 5 6', '7 8 9', ', 0 ⌫'] : ['Й Ц У К Е Н Г Ш Щ З Х', 'Ф І В А П Р О Л Д Ж Є', 'Я Ч С М И Т Ь Б Ю'];
  let out = `<rect id="KeyboardBackground" y="${height - 224}" width="${width}" height="224" fill="#111922"/>`;
  rows.forEach((row, r) => {
    const chars = row.split(' '), cell = (width - 16) / chars.length;
    chars.forEach((letter, c) => {out += text(`Key-${r}-${c}`, letter, 8 + c * cell + cell / 2 - 5, height - 188 + r * 43, 17);});
  });
  return group('Keyboard-simulated-not-native-IME', 0, 0, out);
}

function workoutBlocks(scene) {
  const done = scene.workout;
  const blocks = [
    note('ExerciseName', ['Присідання з гантелею', '3 підходи · вага та повторення']),
    ...[0, 1, 2].map(i => ({height: 68, draw: w => {
      const completed = i < done;
      const inputColor = completed || (scene.filled && i === done) || (scene.keyboard && i === done) ? palette.primary : palette.secondary;
      return panel(`Set-${i}-surface`, w, 68, completed)
        + text(`Set-${i}-number`, String(i + 1), 15, 40, 16, palette.secondary)
        + text(`Set-${i}-weight`, '8', 59, 31, 18, inputColor)
        + text(`Set-${i}-unit`, 'кг', 59, 52, 12, palette.secondary)
        + text(`Set-${i}-reps`, '8', 142, 31, 18, inputColor)
        + text(`Set-${i}-reps-label`, 'повт.', 142, 52, 12, palette.secondary)
        + `<circle id="Complete-${i}" cx="${w - 30}" cy="34" r="11" fill="none" stroke="${completed ? palette.success : palette.muted}" stroke-width="1.6"/>`
        + (completed ? `<path d="m${w - 36} 34 4 4 8-8" fill="none" stroke="${palette.success}" stroke-width="1.7" stroke-linecap="round"/>` : '');
    }})),
    bullet('AddSet', '+ Додати підхід', 'Підхід 4'),
    note('OtherExercises', ['Ще 4 вправи у плані', '0 із 12 підходів виконано'])
  ];
  return scene.keyboard ? blocks.slice(0, 2) : blocks;
}

export function renderScene(scene, width = 360, viewportHeight = 640) {
  const w = width - 40;
  const blocks = scene.workout !== undefined ? workoutBlocks(scene) : scene.blocks;
  let y = 0;
  const content = blocks.map((block, i) => {const value = group(`R05Block-${i}`, 0, y, block.draw(w)); y += block.height + 12; return value;}).join('');
  const primaryY = scene.keyboard ? viewportHeight - 292 : Math.max(126 + y + 8, viewportHeight - 76);
  const canvasHeight = Math.max(viewportHeight, primaryY + 76);
  const defs = '<defs><linearGradient id="plate" x1="0" y1="0" x2="1" y2="1"><stop stop-color="#101923"/><stop offset="1" stop-color="#080D13"/></linearGradient></defs>';
  const header = text('Brand', 'THE SYSTEM', scene.back ? 64 : 20, 32, 12, palette.cyan, 600)
    + text('SceneTitle', scene.title, 20, 73, 24, palette.primary, 700)
    + text('SceneSubtitle', scene.step ? `Крок ${scene.step} із 5` : (scene.subtitle ?? ''), 20, 99, 13, palette.secondary);
  const back = scene.back ? group('BackAction', 8, 8, `<rect width="48" height="48" fill="${palette.background}" fill-opacity=".01"/><path d="m24 12-8 8 8 8m-8-8h20" fill="none" stroke="${palette.secondary}" stroke-width="1.7"/>`) : '';
  const primarySlot = group('PrimarySlot-replace-with-SystemButton', 20, primaryY, `<rect width="${w}" height="56" rx="18" fill="#0C202A" stroke="${palette.border}"/>` + text('PrimaryTemporaryLabel', scene.primary, 16, 33, 15));
  return {key: scene.key, width, viewportHeight, canvasHeight, primaryY, bodyHeight: y - 12,
    svg: `<svg xmlns="http://www.w3.org/2000/svg" width="${width}" height="${canvasHeight}" viewBox="0 0 ${width} ${canvasHeight}">${defs}<rect id="Background" width="${width}" height="${canvasHeight}" fill="${palette.background}"/>${group('R05Header', 0, 0, header + back)}${group('R05AutoBody', 20, 126, content)}${primarySlot}${scene.keyboard ? keyboard(scene.keyboard, width, viewportHeight) : ''}</svg>`};
}
