# R03 Current Foundations

Date: 2026-10-13. HEAD: `36d6c480da90438c1b5adaf363de7cfb6986f9d8` + existing local changes.
Status: **done; native foundations, shared materials and all 35 variants inspected**. [Components page](https://www.figma.com/design/Gjpbz08rE8dVNvQhHp9vHT?node-id=190-211).
Source paths use the `A` prefix from [FEATURE_MAP](../architecture/FEATURE_MAP.md).
The [state ledger](R03_FIGMA_STATE.json) stores actual returned IDs, values and node properties.

2026-10-14 cleanup/D26: all8 System master roots moved intact to page190:211, alongside
the2 shared nav masters. Demo review3:117 and mobile specimen12:43 were removed at the
owner's request. Their node links/captures below and in the ledger are historical evidence,
not current navigation targets. See [selected Status design](R04_SELECTED_DESIGN.md).

## Scope and Limits

Current dark foundations only; no screen redesign or Kotlin change. One Figma unit represents
one Compose dp/sp at font scale 1.0, not device pixels. Roboto named weights match the bundled
Roboto variable family semantically; device rasterization/font-version equivalence is not verified.

Seven reusable families, 35 variants, plus a supporting editable Material Add icon; 18 review instances.
Native text, vector, gradient, component and instance nodes; no captured web UI or raster layout.
Main components sit outside the review frame. Widths 328 and specimen content are review fixtures,
not approved phone layouts or new product copy. Progress variants are static examples, not domain logic.

All 35 master variants and review specimens were inspected in Chrome; state labels, field outlines,
loading ring and material assignments corrected. Current token values remain authoritative;
Figma effects approximate Compose directional/radial lighting and Android elevation, not pixel-perfect
renderer equivalence. Default M3 tracking and device font-version equivalence still require Android
comparison during implementation. Rounded large panels remain 24.
No new UI behavior, focus states for buttons or animation policy was introduced.

## Actual Nodes

| Family | Node | Variants | Kotlin source |
| --- | --- | --- | --- |
| System/Surface | [3:23](https://www.figma.com/design/Gjpbz08rE8dVNvQhHp9vHT?node-id=3-23) | Role=Panel, State=Normal; Role=Panel, State=Active; Role=Plate, State=Normal; Role=Plate, State=Active; Role=Dialog, State=Normal; Role=Dialog, State=Active | SystemPanels.kt: SystemPanel/techSurface/shape helpers |
| System/Button | [3:36](https://www.figma.com/design/Gjpbz08rE8dVNvQhHp9vHT?node-id=3-36) | Style=Tonal, State=Default; Style=Tonal, State=Active; Style=Tonal, State=Disabled; Style=Filled, State=Default; Style=Filled, State=Active; Style=Filled, State=Disabled | SystemGlassComponents.kt: SystemButton |
| System/IconButton | [3:45](https://www.figma.com/design/Gjpbz08rE8dVNvQhHp9vHT?node-id=3-45) | State=Default; State=Active | SystemGlassComponents.kt: SystemIconButton |
| System/StatusChip | [3:82](https://www.figma.com/design/Gjpbz08rE8dVNvQhHp9vHT?node-id=3-82) | Role=Progress, State=Inactive; Role=Progress, State=Active; Role=Recovery, State=Inactive; Role=Recovery, State=Active; Role=Danger, State=Inactive; Role=Danger, State=Active; Role=Reward, State=Inactive; Role=Reward, State=Active; Role=Neutral, State=Inactive; Role=Neutral, State=Active; Role=Ai, State=Inactive; Role=Ai, State=Active | SystemGlassComponents.kt: SystemStatusChip |
| System/ProgressBar | [3:89](https://www.figma.com/design/Gjpbz08rE8dVNvQhHp9vHT?node-id=3-89) | Progress=0; Progress=62; Progress=100 | SystemGlassComponents.kt: SystemProgressBar |
| System/OutlinedTextField | [3:102](https://www.figma.com/design/Gjpbz08rE8dVNvQhHp9vHT?node-id=3-102) | State=Default; State=Focused; State=Error | SystemDialogComponents.kt: systemOutlinedTextFieldColors; Theme.kt: systemShapes; OnboardingScreen.kt: NameStep |
| System/StatePanel | [3:116](https://www.figma.com/design/Gjpbz08rE8dVNvQhHp9vHT?node-id=3-116) | Kind=Loading; Kind=Empty; Kind=Error | SystemGlassComponents.kt: SystemStatePanel |

Review node: `3:117`; icon node: `3:2`. No Code Connect installation/publication.

## Variable Mapping

Primitives collection `VariableCollectionId:2:2` (Value), semantic collection `VariableCollectionId:2:3` (Dark).
Every semantic entry aliases a primitive; Android code syntax references the actual Kotlin symbol.
Scopes set explicitly. Browser UI confirms 52 primitives and 56 semantic aliases. The four text
color tokens also permit SHAPE_FILL/STROKE_COLOR for native glyphs; TEXT_FILL remains enabled.
The radius alias/instance path was tested; this is not an exhaustive mutation test of every token.

| Variable | Value | Android symbol | ID |
| --- | --- | --- | --- |
| color/background | #020407; alpha=1 | `SystemTheme.colors.background` | `VariableID:2:5` |
| color/backgroundSecondary | #071018; alpha=1 | `SystemTheme.colors.backgroundSecondary` | `VariableID:2:7` |
| color/backgroundElevated | #0A121C; alpha=1 | `SystemTheme.colors.backgroundElevated` | `VariableID:2:9` |
| color/surfaceGlass | #111821; alpha=0.752941 | `SystemTheme.colors.surfaceGlass` | `VariableID:2:11` |
| color/surfaceGlassStrong | #0D141D; alpha=0.941176 | `SystemTheme.colors.surfaceGlassStrong` | `VariableID:2:13` |
| color/surfaceGlassSoft | #101720; alpha=0.541176 | `SystemTheme.colors.surfaceGlassSoft` | `VariableID:2:15` |
| color/surfaceRaised | #0A1119; alpha=0.960784 | `SystemTheme.colors.surfaceRaised` | `VariableID:2:17` |
| color/borderActive | #20E8FF; alpha=0.439216 | `SystemTheme.colors.borderActive` | `VariableID:2:19` |
| color/accentPrimary | #1FE1FF; alpha=1 | `SystemTheme.colors.accentPrimary` | `VariableID:2:21` |
| color/accentPrimarySoft | #25E6FF; alpha=0.188235 | `SystemTheme.colors.accentPrimarySoft` | `VariableID:2:23` |
| color/accentAi | #9B6DFF; alpha=1 | `SystemTheme.colors.accentAi` | `VariableID:2:25` |
| color/accentAiSoft | #9B6DFF; alpha=0.2 | `SystemTheme.colors.accentAiSoft` | `VariableID:2:27` |
| color/accentInfo | #5CC8FF; alpha=1 | `SystemTheme.colors.accentInfo` | `VariableID:2:29` |
| color/accentSuccess | #35E6A8; alpha=1 | `SystemTheme.colors.accentSuccess` | `VariableID:2:31` |
| color/accentWarning | #FFC766; alpha=1 | `SystemTheme.colors.accentWarning` | `VariableID:2:33` |
| color/accentError | #FF5F7D; alpha=1 | `SystemTheme.colors.accentError` | `VariableID:2:35` |
| color/statusProgress | #20DCF7; alpha=1 | `SystemTheme.colors.statusProgress` | `VariableID:2:37` |
| color/statusRecovery | #35E6A8; alpha=1 | `SystemTheme.colors.statusRecovery` | `VariableID:2:38` |
| color/statusDanger | #FF5F7D; alpha=1 | `SystemTheme.colors.statusDanger` | `VariableID:2:39` |
| color/statusReward | #FFC766; alpha=1 | `SystemTheme.colors.statusReward` | `VariableID:2:40` |
| color/statusNeutral | #8391A2; alpha=1 | `SystemTheme.colors.statusNeutral` | `VariableID:2:42` |
| color/textPrimary | #F6FAFF; alpha=1 | `SystemTheme.colors.textPrimary` | `VariableID:2:44` |
| color/textSecondary | #A8B5C4; alpha=1 | `SystemTheme.colors.textSecondary` | `VariableID:2:46` |
| color/textMuted | #788596; alpha=1 | `SystemTheme.colors.textMuted` | `VariableID:2:48` |
| color/textDisabled | #596574; alpha=1 | `SystemTheme.colors.textDisabled` | `VariableID:2:50` |
| color/panelTop | #141B24; alpha=0.980392 | `SystemTheme.material.panelTop` | `VariableID:2:52` |
| color/panelMid | #0D141D; alpha=0.960784 | `SystemTheme.material.panelMid` | `VariableID:2:54` |
| color/panelBottom | #080D13; alpha=0.941176 | `SystemTheme.material.panelBottom` | `VariableID:2:56` |
| color/plateTop | #111820; alpha=0.929412 | `SystemTheme.material.plateTop` | `VariableID:2:58` |
| color/plateMid | #0C1219; alpha=0.909804 | `SystemTheme.material.plateMid` | `VariableID:2:60` |
| color/plateBottom | #070B10; alpha=0.890196 | `SystemTheme.material.plateBottom` | `VariableID:2:62` |
| color/buttonTop | #0D2029; alpha=0.94902 | `SystemTheme.material.buttonTop` | `VariableID:2:64` |
| color/buttonBottom | #081118; alpha=0.956863 | `SystemTheme.material.buttonBottom` | `VariableID:2:66` |
| color/edgeHighlight | #E7F0F7; alpha=0.22 | `SystemTheme.material.edgeHighlight` | `VariableID:2:68` |
| color/reflectedPrimary | #1FE1FF; alpha=0.07 | `SystemTheme.material.reflectedPrimary` | `VariableID:2:70` |
| color/reflectedAi | #9B6DFF; alpha=0.055 | `SystemTheme.material.reflectedAi` | `VariableID:2:72` |
| color/railInactive | #34414D; alpha=0.34 | `SystemTheme.material.railInactive` | `VariableID:2:74` |
| color/overlayLight | #FFFFFF; alpha=0.04 | `SystemTheme.colors.overlayLight` | `VariableID:2:76` |
| color/overlayMedium | #FFFFFF; alpha=0.064 | `SystemTheme.colors.overlayMedium` | `VariableID:2:78` |
| color/overlayStrong | #FFFFFF; alpha=0.105 | `SystemTheme.colors.overlayStrong` | `VariableID:2:80` |
| color/borderSubtle | #FFFFFF; alpha=0.105 | `SystemTheme.colors.borderSubtle` | `VariableID:2:81` |
| color/borderMuted | #FFFFFF; alpha=0.06 | `SystemTheme.colors.borderMuted` | `VariableID:2:83` |
| color/edgeShade | #000000; alpha=0.52 | `SystemTheme.material.edgeShade` | `VariableID:2:85` |
| color/innerHighlight | #FFFFFF; alpha=0.03 | `SystemTheme.material.innerHighlight` | `VariableID:2:87` |
| color/innerShade | #000000; alpha=0.28 | `SystemTheme.material.innerShade` | `VariableID:2:89` |
| color/ambientShadow | #000000; alpha=0.34 | `SystemTheme.depth.ambientShadow` | `VariableID:2:91` |
| color/contactShadow | #000000; alpha=0.48 | `SystemTheme.depth.contactShadow` | `VariableID:2:93` |
| radius/large | 24 dp | `SystemTheme.shapes.large` | `VariableID:2:95` |
| radius/medium | 18 dp | `SystemTheme.shapes.medium` | `VariableID:2:97` |
| radius/extraLarge | 30 dp | `SystemTheme.shapes.extraLarge` | `VariableID:2:99` |
| radius/pill | 999 dp | `SystemTheme.shapes.pill` | `VariableID:2:101` |
| spacing/card | 16 dp | `SystemCardPadding` | `VariableID:2:103` |
| spacing/screen | 20 dp | `SystemScreenPadding` | `VariableID:2:105` |
| spacing/item | 12 dp | `SystemItemSpacing` | `VariableID:2:107` |
| control/iconHeight | 48 dp | `SystemControlHeight` | `VariableID:2:109` |
| border/width | 1 dp | `SystemBorderWidth` | `VariableID:2:111` |

Raw primitives with equal values are shared. Edit a semantic token to limit changes to its role;
editing a shared primitive intentionally affects its aliases. Sources are SystemTokens.kt and Dimensions.kt.
The initial MCP radius test was rejected by the Starter limit. A later browser-editor test passed:
primitive float/24 was changed 24 -> 26 -> 24, with the radius/large alias kept intact.
Normal instance 3:122 and Active instance 3:125 both showed bound radius 26, then restored 24.
This verifies the tested radius path, not every color/effect binding in the library.

## Text Styles

| Style | Roboto weight | Size / line height | Source |
| --- | --- | --- | --- |
| System/displayLarge | Bold | 42 / 44 | Type.kt: TheSystemTypography.displayLarge |
| System/headlineLarge | Bold | 38 / 40 | Type.kt: TheSystemTypography.headlineLarge |
| System/headlineMedium | Bold | 34 / 36 | Type.kt: TheSystemTypography.headlineMedium |
| System/headlineSmall | Bold | 28 / 30 | Type.kt: TheSystemTypography.headlineSmall |
| System/titleLarge | Bold | 26 / 28 | Type.kt: TheSystemTypography.titleLarge |
| System/titleMedium | SemiBold | 16 / 20 | Type.kt: TheSystemTypography.titleMedium |
| System/titleSmall | SemiBold | 14 / 18 | Type.kt: TheSystemTypography.titleSmall |
| System/bodyLarge | Regular | 14 / 20 | Type.kt: TheSystemTypography.bodyLarge |
| System/bodyMedium | Regular | 12 / 18 | Type.kt: TheSystemTypography.bodyMedium |
| System/bodySmall | Regular | 11 / 16 | Type.kt: TheSystemTypography.bodySmall |
| System/labelLarge | SemiBold | 13 / 16 | Type.kt: TheSystemTypography.labelLarge |
| System/labelMedium | Medium | 11 / 14 | Type.kt: TheSystemTypography.labelMedium |
| System/labelSmall | Medium | 10 / 12 | Type.kt: TheSystemTypography.labelSmall |
| System/component/buttonFilled | Bold | 16 / 20 | SystemGlassComponents.kt overrides |
| System/component/buttonTonal | Bold | 13 / 16 | SystemGlassComponents.kt overrides |
| System/component/chip | Bold | 10 / 12 | SystemGlassComponents.kt overrides |
| System/component/stateTitle | Black | 16 / 20 | SystemGlassComponents.kt overrides |

Styles use zero letter spacing in the initial pass. Type.kt leaves some M3 tracking defaults implicit;
resolve those from the pinned dependency before claiming exact text fidelity.

## Shared Material Mapping

All names below are local Figma styles, reused by masters and their instances. Values are source-derived
preview recipes from SystemPanels.kt/techSurface and SystemTokens.kt, not new production tokens.

| Effect style under System/Material/ | Recipe and assignment |
| --- | --- |
| Edge | Inner highlight E7F0F7 20%, offset 1/1, blur 1; inner black shade 28%, offset -1/-1, blur 8. Normal panels/plates, tonal/icon controls, inactive chips, Loading/Empty. |
| Raised | Edge + primary ambient 8.5%, offset 0/1, blur 4; black contact 48%, offset 0/1, blur 1. Active panels/plates/icons; source active elevation 2 dp. |
| Dialog | Edge + black ambient 34%, offset 0/5, blur 10; contact 48%, offset 0/2, blur 2. Both dialog states; source 10 dp elevation approximation. |
| ButtonRaised | Edge + primary ambient 8.5%, offset 0/1, blur 2; contact 48%, offset 0/1, blur 1. Tonal Active and both enabled Filled states; source 1 dp elevation. |
| Disabled | No drop shadows. Edge highlight 10%, shade 18%; same offsets/blur. Both disabled buttons; values independently checked in style editor. |
| ProgressRaised, RecoveryRaised, DangerRaised, RewardRaised, NeutralRaised, AiRaised | Raised geometry with corresponding status RGB at 8.5%. Active role chips; DangerRaised also Error StatePanel. |

Eight shared paint styles: System/Border/Active (primary 22%), ButtonActive (primary 28%),
and ProgressActive/RecoveryActive/DangerActive/RewardActive/NeutralActive/AiActive (role 22%).
System/Fill/Filled has primary stops 68% at 0 and 48% at 50; stop 100 stays bound to buttonBottom.
Filled Default is active-looking by source design, not a duplicated Tonal state.
Inactive Add icon uses textSecondary; active uses primary. No heavy blur or shader was introduced.

Derived alpha colors in paint/effect styles cannot express Kotlin Color.copy(alpha) as a simple alias.
If source accent/role RGB changes, update those named styles once alongside semantic variables;
all assigned instances then inherit the style. Do not promise automatic RGB propagation for those
derived styles. Radius propagation is live and tested. Kotlin remains the renderer source of truth.

## Mobile Specimen and Verification

The 1080-wide review 3:117 is named "R03 Library sheet - not a phone screen" for comparison.
Separate native [mobile frame 12:43](https://www.figma.com/design/Gjpbz08rE8dVNvQhHp9vHT?node-id=12-43)
is fixed 360x640 with 20 horizontal padding and 320-wide content. It retains 18 reusable instances,
stacks content rows vertically, and uses Prototype Vertical overflow. Icons stay 48 square and chips
retain natural width. This is a component specimen, NOT a Status layout or R04 approval.
[Scrollable preview](https://www.figma.com/proto/Gjpbz08rE8dVNvQhHp9vHT/THE-SYSTEM-Remaster?node-id=12-43&scaling=min-zoom&content-scaling=fixed&page-id=0%3A1).

Field vectors use CutCornerShape(7.dp), Left+Right constraints and editable floating labels.
An absolute 36x4 background-bound "Label notch (native mask)" at 12/6 clears the specimen border
behind each label without changing auto layout. This Figma-only shim must NOT be ported to Compose:
native OutlinedTextField already draws its notch. Longer labels need corresponding fixture review.
Loading uses a thin native ring sector (start -90, sweep 75%, ratio 85.7%), not a filled wedge.
Catalog set backdrops are bound to background so text is inspected in dark context.

[35-variant observations](../remaster/evidence/R03-variant-audit.json) and
[completion evidence/screenshots](../remaster/evidence/R03.md) record all seven families, styles,
mobile top/bottom and actual scrolling. PNGs are browser evidence, not raster design nodes.
No Android/device/font-scale test, paid upgrade, sharing change, app build, commit or push occurred.
Next is R04; its direction/owner approval is still pending.

