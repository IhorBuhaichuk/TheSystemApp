// R05 remaining style/component bindings. Run only through the Figma Plugin API.
// Pending execution: the Starter MCP quota was exhausted during this session.
await figma.setCurrentPageAsync(await figma.getNodeByIdAsync('190:210'));
const IDS=["223:1016","223:1017","223:1018","223:1019","223:1020","225:1855","225:1856","225:1857","225:1858","225:1859","225:1860","225:1861","225:1862","225:1863","225:1864","225:1865","225:1866","225:1867","225:1868","225:1869","225:1870","225:1871"];

const created=[],mutated=[],removed=[],stats={texts:0,paints:0,buttons:0};
const vars=await figma.variables.getLocalVariablesAsync(),varById=new Map(vars.map(v=>[v.id,v]));
function value(v){let a=Object.values(v.valuesByMode)[0];return a&&a.type==='VARIABLE_ALIAS'?value(varById.get(a.id)):a;}
const colors=vars.filter(v=>v.resolvedType==='COLOR'&&!v.name.startsWith('color/__')).map(v=>({v,c:value(v)}));
const textStyles=await figma.getLocalTextStylesAsync(),paintStyles=await figma.getLocalPaintStylesAsync();
const shapeSig=p=>JSON.stringify(p,(k,v)=>k==='boundVariables'?undefined:v);
async function bind(root){
 const ns=[root,...('findAll' in root?root.findAll(()=>true):[])];
 const fonts=new Map();for(const n of ns)if(n.type==='TEXT')for(const seg of n.getStyledTextSegments(['fontName']))fonts.set(JSON.stringify(seg.fontName),seg.fontName);
 await Promise.all([...fonts.values()].map(f=>figma.loadFontAsync(f)));
 for(const n of ns){
  let changed=false;
  if(n.type==='TEXT'&&n.fontSize!==figma.mixed&&n.fontName!==figma.mixed){
   const sig=JSON.stringify([n.fontName,n.fontSize,n.lineHeight,n.letterSpacing]);
   let s=textStyles.find(s=>JSON.stringify([s.fontName,s.fontSize,s.lineHeight,s.letterSpacing])===sig);
   if(!s){s=figma.createTextStyle();s.name='System/R05/Text/'+n.fontSize+'/'+n.fontName.style+'/'+textStyles.length;s.fontName=n.fontName;s.fontSize=n.fontSize;s.lineHeight=n.lineHeight;s.letterSpacing=n.letterSpacing;textStyles.push(s);created.push(s.id);}
   n.textStyleId=s.id;stats.texts++;changed=true;
  }
  for(const field of ['fills','strokes']){
   if(!(field in n)||!Array.isArray(n[field])||!n[field].length)continue;
   let ps=n[field].map(p=>{
    if(p.type!=='SOLID')return p;
    const match=colors.find(e=>Math.abs(e.c.r-p.color.r)<.002&&Math.abs(e.c.g-p.color.g)<.002&&Math.abs(e.c.b-p.color.b)<.002&&Math.abs((e.c.a??1)-(p.opacity??1))<.002);
    return match?figma.variables.setBoundVariableForPaint({...p},'color',match.v):p;
   });
   const sig=shapeSig(ps);let s=paintStyles.find(x=>shapeSig(x.paints)===sig);
   if(!s){s=figma.createPaintStyle();s.name='System/R05/'+(field==='fills'?'Fill/':'Stroke/')+paintStyles.length;s.paints=ps;paintStyles.push(s);created.push(s.id);}
   if(field==='fills')n.fillStyleId=s.id;else n.strokeStyleId=s.id;
   stats.paints++;changed=true;
  }
  if(changed)mutated.push(n.id);
 }
}

const m1=await figma.getNodeByIdAsync('271:39'),m2=await figma.getNodeByIdAsync('271:43'),md=await figma.getNodeByIdAsync('271:47');
for(const id of IDS){
 const root=await figma.getNodeByIdAsync(id);if(!root)throw Error('Missing accepted frame '+id);
 await bind(root);
 const actions=root.findAll(n=>n.name==='PrimaryAction'||n.name==='ReturnToWorkout');
 for(const old of actions){if(old.type==='INSTANCE')continue;const label=old.findOne(n=>n.type==='TEXT');if(!label)continue;const master=old.name==='ReturnToWorkout'?m2:label.characters.startsWith('Поки')?md:m1;
 const parent=old.parent,index=parent.children.indexOf(old),x=old.x,y=old.y,w=old.width,h=old.height,rx=JSON.parse(JSON.stringify(old.reactions));
 const inst=master.createInstance();parent.insertChild(index,inst);inst.resize(w,h);inst.x=x;inst.y=y;inst.name=old.name;
 const prop=Object.keys(master.componentPropertyDefinitions).find(k=>k.startsWith('Label#'));inst.setProperties({[prop]:label.characters});
 if(rx.length)await inst.setReactionsAsync(rx);
 created.push(inst.id);removed.push(old.id);old.remove();stats.buttons++;
 }
}
return {createdNodeIds:created,mutatedNodeIds:mutated.join(','),removedNodeIds:removed,stats};
