// Editable SVG transport for Figma review only. Not production UI or training rules.
import { writeFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { dirname, join } from 'node:path';
export const colors = {bg:'#020407',surface:'#0B1119',edge:'#293D49',text:'#F6FAFF',sub:'#A8B5C4',muted:'#788596',cyan:'#1FE1FF',green:'#35E6A8',amber:'#F2BA68',red:'#FF7187'};
const esc = s => String(s).replaceAll('&','&amp;').replaceAll('<','&lt;').replaceAll('>','&gt;').replaceAll('"','&quot;');
const t = (s,x,y,size=14,color=colors.text,weight=400,id='') => `<text ${id?`id="${esc(id)}"`:''} x="${x}" y="${y}" font-family="Roboto" font-weight="${weight}" font-size="${size}" fill="${color}">${esc(s)}</text>`;
const g = (id,x,y,body) => `<g id="${esc(id)}" transform="translate(${x} ${y})">${body}</g>`;
const rect = (x,y,w,h,fill=colors.surface,r=12,stroke='none') => `<rect x="${x}" y="${y}" width="${w}" height="${h}" rx="${r}" fill="${fill}" stroke="${stroke}"/>`;
const line = (x,y,x2,y2,color=colors.edge) => `<path d="M${x} ${y}H${x2}" fill="none" stroke="${color}"/>`.replace(`H${x2}`,`L${x2} ${y2}`);
const paths = {
  arrow:'M9 5l-7 7 7 7M2 12h20',chevron:'M9 5l7 7-7 7',check:'M5 12l4 4L19 6',
  plus:'M12 5v14M5 12h14',close:'M6 6l12 12M18 6L6 18',
  bolt:'M13 2L4 14h7l-1 8 10-13h-8z',calendar:'M5 5h14v16H5zM8 2v6m8-6v6M5 10h14M8 14h2m4 0h2m-8 4h2',
  clock:'M12 8v5l3 2M22 12a10 10 0 11-20 0 10 10 0 0120 0',
  dumbbell:'M6 6l12 12M3 7l4-4m-5 7l8-8m7 19l4-4m-7 5l8-8',
  target:'M20 12a8 8 0 11-8-8m4-2v6h6M12 12l8-8M16 12a4 4 0 11-4-4',
  shield:'M12 2l8 4v7c0 4-8 9-8 9s-8-5-8-9V6zM8 12l3 3 5-6',
  trend:'M3 17l6-6 4 4 8-10m-6 0h6v6',home:'M3 10l9-7 9 7M5 9v12h14V9m-10 12v-7h6v7',
  info:'M12 11v6m0-10v1M22 12a10 10 0 11-20 0 10 10 0 0120 0',
  settings:'M3 6h18M3 12h18M3 18h18M8 3v6m8 0v6M9 15v6',
  bookmark:'M6 3h12v19l-6-4-6 4z',rotate:'M3 11a9 9 0 111 6M3 4v7h7',
  flag:'M5 22V3m0 0c5-4 9 4 15 0v10c-6 4-10-4-15 0',heart:'M12 21S1 14 2 7c1-6 9-6 10 0 1-6 9-6 10 0 1 7-10 14-10 14',
  more:'M5 12h.01M12 12h.01M19 12h.01',play:'M8 4l13 8-13 8z',
  person:'M16 6a4 4 0 11-8 0 4 4 0 018 0M4 22v-3a8 8 0 0116 0v3',
  layers:'M12 2L2 7l10 5 10-5zM2 12l10 5 10-5M2 17l10 5 10-5'
};
const icon = (name,x,y,size=24,color=colors.sub) => `<svg x="${x}" y="${y}" width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><path d="${paths[name]||paths.target}"/></svg>`;
const plate = (x,y,w,h,selected=false) => rect(x,y,w,h,'url(#metal)',16,selected?colors.cyan:colors.edge)+line(x+16,y+1,x+w-16,y+1,selected?'#70EDFF':'#46525C');
const p = (s,x,y,lines,size=14,color=colors.sub) => lines.map((l,i)=>t(l,x,y+i*(size+7),size,color)).join('');
const choice = (w,y,name,detail,ico,selected=false,h=70) => g(`Choice-${name}`,16,y,
  plate(0,0,w-32,h,selected)+rect(14,15,36,36,selected?'#102C36':'#151D25',10)+icon(ico,20,21,24,selected?colors.cyan:colors.sub)
  +t(name,62,28,16,colors.text,600)+t(detail,62,50,12,colors.sub)
  +`<circle cx="${w-55}" cy="${h/2}" r="8" fill="none" stroke="${selected?colors.cyan:colors.muted}"/>`
  +(selected?`<circle cx="${w-55}" cy="${h/2}" r="3.5" fill="${colors.cyan}"/>`:''));
const tag = (s,x,y,w,selected=false) => rect(x,y,w,48,selected?'#102E38':'#0A1017',12,selected?colors.cyan:colors.edge)+t(s,x+w/2,y+29,15,selected?colors.cyan:colors.sub,500).replace('<text ','<text text-anchor="middle" ');
const note = (w,y,title,lines,ico='info',tone=colors.sub) => icon(ico,18,y,20,tone)+t(title,48,y+16,14,colors.text,500)+p('',48,y+40,lines,13,colors.sub);
const row = (w,y,title,detail,ico='dumbbell') => icon(ico,20,y+12,24)+t(title,58,y+24,15,colors.text,500)+t(detail,58,y+45,12,colors.sub)+icon('chevron',w-40,y+16,20)+line(18,y+60,w-18,y+60);
const pillText = (s,x,y,color=colors.sub,size=12) => t(s,x,y,size,color,500);
function button(w,y,label,variant='primary',id='PrimaryAction') {
  const disabled=variant==='disabled';
  return g(id,16,y,rect(0,0,w-32,52,disabled?'#14202A':variant==='primary'?'url(#button)':'url(#metal)',16,disabled?'#293D49':variant==='danger'?colors.red:variant==='primary'?'#2A8798':colors.edge)
    +line(17,1,w-49,1,variant==='primary'?'#77E9F4':'#42515B')
    +t(label,(w-32)/2,32,15,disabled?colors.muted:variant==='danger'?colors.red:colors.text,600).replace('<text ','<text text-anchor="middle" '));
}
function frame(s,w,h) {
  const fy=h-84,small=h===640;
  let head=rect(0,0,w,h,colors.bg,0)+t('9:41',20,23,12,colors.text,500)+t('▮▮  ▰',w-62,23,10,colors.sub);
  head+=g('Back',8,36,rect(0,0,48,48,colors.bg,0)+icon(s.key==='L0'||s.key==='L1'?'close':'arrow',12,12,24));
  head+=t(s.category,58,65,12,colors.cyan,600);
  if(s.step){head+=t(`${s.step} / 5`,w-54,65,12,colors.sub)+rect(16,84,w-32,2,'#18252E',1)+rect(16,84,(w-32)*s.step/5,2,colors.cyan,1);}
  head+=t(s.title,18,s.step?121:108,24,colors.text,700);
  if(s.desc) head+=p('',18,s.step?146:134,s.desc,13,colors.sub);
  const content=s.body(w,h,small);
  const foot=rect(0,fy-14,w,98,colors.bg,0)+line(16,fy-14,w-16,fy-14,'#15222B')+button(w,fy,s.primary,s.variant||'primary');
  return head+g('EditableBody',0,0,content)+g('Footer',0,0,foot)+rect(w/2-48,h-12,96,3,'#63717E',2);
}
export const scenes=[
 {key:'G',category:'ВАШ ПЛАН',step:1,title:'Що для вас головне?',desc:['Оберіть пріоритет. Його можна змінити.'],primary:'Далі',body:w=>
 choice(w,181,'Стати сильнішим','Впевненіше працювати з навантаженням','dumbbell')+
 choice(w,259,'Розвивати м’язи','Поступово збільшувати обсяг роботи','layers')+
 choice(w,337,'Покращити фізичну форму','Сила, рухливість і регулярність','heart',true)+
 choice(w,415,'Почати з простого','Допомога з першим планом','flag')+t('Налаштувати власний план',18,530,13,colors.cyan)},
 {key:'H',category:'ВАШ ПЛАН',step:2,title:'Як ви тренувалися?',desc:['Орієнтуйтеся на останні місяці.'],primary:'Далі',body:w=>
 choice(w,185,'Починаю вперше','Потрібні базові вправи й пояснення','person')+
 choice(w,265,'Повертаюся після перерви','Почати спокійніше й відновити ритм','rotate',true)+
 choice(w,345,'Тренуюся регулярно','Є актуальний досвід і записи','trend')+
 note(w,448,'Старт без перевірки максимуму',['Навантаження уточнимо поступово,','за вашими фактичними результатами.'],'shield')},
 {key:'S',category:'ВАШ ПЛАН',step:3,title:'Знайдемо ваш ритм',desc:['Оберіть розклад, якого реально триматися.'],primary:'Далі',body:w=>{
 const cell=(w-44)/3,dw=(w-44)/7;
 return t('Тренувань на тиждень',18,190,14,colors.text,500)+[2,3,4].map((n,i)=>tag(`${n}`,16+i*(cell+6),204,cell,n===3)).join('')+
 t('Зручні дні',18,282,14,colors.text,500)+['Пн','Вт','Ср','Чт','Пт','Сб','Нд'].map((d,i)=>tag(d,16+i*(dw+2),296,dw,[0,2,4].includes(i))).join('')+
 t('Час одного заняття',18,378,14,colors.text,500)+[20,35,50].map((n,i)=>tag(`${n} хв`,16+i*(cell+6),392,cell,n===35)).join('')+
 t('Включно з розминкою та відпочинком',18,463,12,colors.sub)+icon('calendar',18,488,20)+t('Пн, Ср, Пт · приблизно 35 хв',48,504,13,colors.cyan);
 }},
 {key:'E',category:'ВАШ ПЛАН',step:4,title:'Де ви тренуєтеся?',desc:['Покажемо вправи під ваше обладнання.'],primary:'Далі',body:w=>{
 const half=(w-40)/2;
 const tile=(x,y,title,sub,ico,sel)=>g(`Equipment-${title}`,x,y,plate(0,0,half,82,sel)+icon(ico,14,12,22,sel?colors.cyan:colors.sub)+t(title,14,51,14,colors.text,500)+t(sub,14,69,11,colors.sub));
 return tag('Вдома',16,182,half,true)+tag('У залі',24+half,182,half)+t('Доступне обладнання',18,266,14,colors.text,500)+
 tile(16,282,'Гантелі','Вибрано','dumbbell',true)+tile(24+half,282,'Лава','Не вибрано','layers',false)+
 tile(16,374,'Резинки','Не вибрано','rotate',false)+tile(24+half,374,'Турнік','Не вибрано','flag',false)+
 icon('person',18,478,20)+t('Без обладнання',48,495,14,colors.sub)+icon('settings',w-39,478,20)+t('Уточнити ваги гантелей',18,530,13,colors.cyan);
 }},
 {key:'B',category:'ВАШ ПЛАН',step:5,title:'Що варто врахувати?',desc:['Обмеження важливіші за складність плану.'],primary:'Переглянути план',body:w=>
 choice(w,185,'Немає відомих обмежень','Можу розглядати звичайний старт','shield',true)+
 choice(w,265,'Є обмеження або біль','Потрібно уточнити безпечний шлях','heart')+
 choice(w,345,'Не впевнений / не впевнена','Не будемо вгадувати навантаження','info')+
 note(w,444,'Стандартний план для дорослих',['Не замінює консультацію фахівця.','За болю чи нездужання не починайте','заняття через підказку додатка.'],'shield',colors.amber)},
 {key:'P',category:'ПЕРЕВІРКА ПЛАНУ',title:'Ваш перший план',desc:['Під ваш час, досвід та обладнання.'],primary:'Зберегти план',body:w=>{
 let b=plate(16,165,w-32,100)+icon('calendar',30,181,22,colors.cyan)+t('3 рази на тиждень',64,198,17,colors.text,600)+t('Пн · Ср · Пт',30,229,14,colors.sub)+icon('clock',w-124,215,18)+t('≈ 35 хв',w-99,229,14,colors.text,500);
 b+=t('СТАРТОВИЙ ТИЖДЕНЬ',18,299,11,colors.cyan,600)+row(w,311,'Понеділок · Тренування A','Усе тіло · 4 вправи')+row(w,374,'Середа · Тренування B','Усе тіло · 4 вправи')+row(w,437,'П’ятниця · Тренування A','Повторення базових рухів');
 b+=t('Вправи першого заняття',18,531,13,colors.cyan);
 return b;
 }},
 {key:'PL',category:'ПЛАН ТРЕНУВАННЯ',title:'Тренування A',desc:['Ознайомчий старт · близько 35 хв'],primary:'Відкрити тренування',body:w=>
 t('4 вправи · 8 робочих підходів',18,173,13,colors.cyan)+
 row(w,190,'Присідання з гантелею','2 підходи · 8–12 повторень')+
 row(w,254,'Жим гантелей на підлозі','2 підходи · 8–12 повторень')+
 row(w,318,'Тяга гантелі з опорою','2 підходи · 8–12 повторень')+
 row(w,382,'Румунська тяга з гантелями','2 підходи · 8–12 повторень')+
 icon('settings',18,473,20)+t('Змінити лише сьогодні',48,490,14,colors.cyan)+t('Розминка — перед робочими підходами',18,527,12,colors.sub)},
 ...[0,1].map(done=>({key:`L${done}`,category:'ТРЕНУВАННЯ A',title:'Запис підходів',desc:[`${done} із 8 підходів позначено виконаними`],primary:done?'Завершити тренування':'Поки немає виконаних підходів',variant:done?'primary':'disabled',body:(w,h,small)=>{
 let b=plate(16,small?150:159,w-32,small?178:225)+t('01',30,small?176:185,12,colors.cyan,600)+t('Присідання з гантелею',62,small?177:186,17,colors.text,600)+icon('more',w-48,small?157:166,24)+t('Ціль: 2 підходи по 8–12 повторень',30,small?202:212,12,colors.sub);
 b+=t('Підхід',30,small?222:242,11,colors.sub)+t('Минулого разу',85,small?222:242,11,colors.sub)+t('кг',w-140,small?222:242,11,colors.sub)+t('повт.',w-90,small?222:242,11,colors.sub);
 for(let i=0;i<2;i++){
 const y=(small?230:252)+i*(small?46:54),sel=done&&i===0;
 b+=t(String(i+1),36,y+30,16,colors.text,500)+t('—',108,y+30,16,colors.muted)+rect(w-149,y,46,46,'#071019',8,'#344450')+rect(w-97,y,46,46,'#071019',8,'#344450')+t(i===0?'8':'—',w-134,y+29,16,colors.text,500)+t(i===0?'10':'—',w-84,y+29,16,colors.text,500)+rect(w-44,y+12,24,24,sel?'#11392F':'#071019',7,sel?colors.green:colors.muted)+(sel?icon('check',w-44,y+12,24,colors.green):'');
 }
 b+=g('AddSet',16,small?329:387,icon('plus',12,10,22,colors.cyan)+t('Додати підхід',46,27,14,colors.cyan))+g('Technique',w-116,small?329:387,icon('play',0,10,20)+t('Техніка',26,27,13,colors.sub));
 if(small){
 ['Жим гантелей на підлозі','Тяга гантелі з опорою','Румунська тяга з гантелями'].forEach((name,i)=>{
 const y=383+i*52;b+=icon('dumbbell',18,y+7,20)+t(name,50,y+18,14,colors.text,500)+t('0 із 2 підходів',50,y+36,11,colors.sub)+icon('chevron',w-36,y+12,18)+line(18,y+47,w-18,y+47);
 });
 }else b+=row(w,461,'Жим гантелей на підлозі','0 із 2 підходів')+row(w,525,'Тяга гантелі з опорою','0 із 2 підходів')+row(w,589,'Румунська тяга з гантелями','0 із 2 підходів');
 if(!small)b+=note(w,681,done?'Підхід записано':'Значення ще не виконані',[done?'Наступний підхід можна змінити.':'Позначте виконання після підходу.'],done?'check':'info',done?colors.green:colors.sub);
 return b;
 }})),
 {key:'F',category:'ЗАВЕРШЕННЯ',title:'Завершити раніше?',desc:['Збережемо тільки виконане.'],primary:'Зберегти 1 підхід',body:w=>
 plate(16,177,w-32,121)+icon('bookmark',30,194,24,colors.cyan)+t('1 із 8 підходів',30,246,26,colors.text,700)+t('7 підходів залишаться невиконаними',30,276,12,colors.sub)+
 note(w,330,'Це часткове тренування',['Невиконані підходи не потраплять','до результату як виконані.'],'info')+
 button(w,446,'Повернутися до тренування','secondary','ReturnToWorkout')},
 {key:'R',category:'ПІДСУМОК',title:'Тренування збережено',desc:['Часткове заняття · 1 робочий підхід'],primary:'Повернутися до Статусу',body:w=>
 plate(16,170,w-32,100)+icon('check',30,188,24,colors.green)+t('Присідання з гантелею',64,207,16,colors.text,600)+t('8 кг × 10 повторень',30,247,22,colors.text,600)+
 t('ПРОГРЕС',18,311,11,colors.cyan,600)+t('Ще немає з чим порівняти',18,340,17,colors.text,600)+p('',18,366,['Перший запис стане точкою відліку.'],13,colors.sub)+
 t('НАСТУПНОГО РАЗУ',18,416,11,colors.cyan,600)+p('',18,444,['Перевіримо, чи підходить навантаження,','за наступними виконаними підходами.'],14,colors.text)+
 t('Деталі збереженого заняття',18,519,14,colors.cyan)},
 {key:'D',category:'ЧЕРНЕТКА',title:'Продовжити заняття?',desc:['Незавершені записи залишилися на місці.'],primary:'Продовжити тренування',body:w=>
 plate(16,184,w-32,118)+icon('bookmark',30,203,24,colors.cyan)+t('Тренування A',66,223,19,colors.text,600)+t('1 підхід виконано · є введені значення',30,260,12,colors.sub)+t('Ще не додано до завершеної історії',30,285,12,colors.sub)+
 note(w,336,'Можна повернутися пізніше',['Закриття екрана не видаляє записи.'],'shield')+button(w,446,'Видалити чернетку…','danger','RequestDiscard')},
 {key:'A',category:'ЛИШЕ СЬОГОДНІ',title:'Підлаштуємо заняття',desc:['Основний план залишиться без змін.'],primary:'Застосувати на сьогодні',body:w=>{
 const cw=(w-44)/3;
 return t('Скільки є часу?',18,185,15,colors.text,500)+[20,35,50].map((n,i)=>tag(`${n} хв`,16+i*(cw+6),203,cw,n===20)).join('')+
 row(w,288,'Сьогодні інше обладнання','Обрати доступне','dumbbell')+row(w,360,'Замінити вправу','Зберегти той самий рух','rotate')+
 note(w,458,'Біль або нездужання?',['Не підбираємо навантаження автоматично.'],'shield',colors.amber);
 }},
 {key:'EW',category:'ОБЛАДНАННЯ',title:'Які гантелі доступні?',desc:['Це можливості обладнання, не ваша сила.'],primary:'Зберегти обладнання',body:w=>
 choice(w,186,'Регульовані','Можна змінювати вагу','settings',true)+choice(w,266,'Фіксовані пари','Наприклад, 4 кг та 8 кг','dumbbell')+
 t('Максимальна вага однієї гантелі',18,379,14,colors.text,500)+rect(16,395,w-32,52,'#08121B',12,colors.edge)+t('Ще не знаю',32,427,15,colors.sub)+
 note(w,480,'Можна уточнити пізніше',['Не будемо вважати невідому вагу фактом.'],'info')},
 {key:'SE',category:'ЗБЕРЕЖЕННЯ',title:'Запис ще не збережено',desc:['Введені значення не втрачено.'],primary:'Спробувати ще раз',body:w=>
 note(w,197,'Не вдалося завершити запис',['Повторіть спробу або поверніться','до тренування. Чернетка збережена.'],'info',colors.amber)+button(w,365,'Повернутися до підходів','secondary','ReturnToWorkout')},
 {key:'SB',category:'ОБМЕЖЕННЯ',title:'Не будемо вгадувати',desc:['Стандартний план може не підійти.'],primary:'Повернутися до налаштувань',body:w=>
 icon('shield',18,191,40,colors.amber)+p('',18,264,['Якщо є біль, нездужання або рекомендації','фахівця, спочатку уточніть допустиме','навантаження з компетентним фахівцем.'],14,colors.text)+
 note(w,380,'Автоплан поки не підтверджений',['Перегляд і налаштування власного плану','не означають медичний допуск.'],'info')},
 {key:'RD',category:'ЗБЕРЕЖЕНИЙ ЗАПИС',title:'Деталі заняття',desc:['Понеділок · Тренування A'],primary:'До підсумку',body:w=>
 row(w,182,'Присідання з гантелею','Підхід 1 · 8 кг × 10 повторень')+
 row(w,254,'Інші заплановані підходи','Не позначені виконаними','layers')+
 note(w,366,'Збережено один підхід',['Це фактичний результат заняття,','а не повне виконання плану.'],'check',colors.green)}
];

export function renderBoard(width=390,height=844) {
 const gap=42,cols=6,cellW=width+gap,cellH=height+92;
 const bw=cols*cellW+40,bh=Math.ceil(scenes.length/cols)*cellH+150;
 const defs=`<defs><linearGradient id="metal" x1="0" y1="0" x2="1" y2="1"><stop stop-color="#141E28"/><stop offset=".45" stop-color="#0C131B"/><stop offset="1" stop-color="#060B10"/></linearGradient><linearGradient id="button" x1="0" y1="0" x2="1" y2="1"><stop stop-color="#168B9C"/><stop offset=".48" stop-color="#116677"/><stop offset="1" stop-color="#07313E"/></linearGradient></defs>`;
 let body=t(`R05 v2 · ${width} × ${height} · НА ПЕРЕГЛЯД`,20,36,24,colors.text,700)+t('Демонстраційні дані. Нові правила не реалізовані. Затверджений Status не змінено.',20,67,14,colors.sub);
 scenes.forEach((s,i)=>{let x=20+(i%cols)*cellW,y=123+Math.floor(i/cols)*cellH;body+=g(`R05v2-${s.key}-${width}`,x,y,frame(s,width,height));});
 return `<svg xmlns="http://www.w3.org/2000/svg" width="${bw}" height="${bh}" viewBox="0 0 ${bw} ${bh}">${defs}${body}</svg>`;
}
if(process.argv[1]===fileURLToPath(import.meta.url)){
 const base=dirname(fileURLToPath(import.meta.url));
 for(const [w,h] of [[390,844],[360,640]])writeFileSync(join(base,`review-v2-${w}.svg`),renderBoard(w,h));
 console.log(JSON.stringify({scenes:scenes.map(s=>s.key),boards:[390,360]}));
}
