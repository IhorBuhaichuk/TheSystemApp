# R02: інвентар дій для Figma та Compose

Версія ACTION-R02-v1, 2026-10-12. HEAD `36d6c480da90438c1b5adaf363de7cfb6986f9d8` + current dirty tree.
Основа: [SC-R01-v1 A-D](../remaster/FEATURE_SCOPE.md); сценарії/стани: [UX_CONTRACT](UX_CONTRACT.md).
Джерело: статичні screen/callback/VM/route reads. Жодна позначка нижче не є device test.

## Читання й точність

2026-10-15/D30: accepted v2 target overrides old onboarding/finish proposals through
[R05_DATA_FLOW](R05_DATA_FLOW.md). Its G/H/S/E/B/P/PL/L0/L1/F/R/D/A/EW/SE/SB/RD rows
are the canonical new-control inventory with source aliases and implementation owners.
Existing ONB/WRK rows below still describe current code, not the new target APIs.
DF08 repairs G05; foreground DF09 partially repairs G03. Remaining runtime gates stay open.

- ID стабільні для R05/R06 та R08-R14; однакова повторювана дія в кількох entry points має спільний handler.
- `W`: callback підключений у прочитаному UI; `U`: contract є, живого entry/consumer не знайдено;
  `D`: disabled/no-op; `P`: planned behavior, не чинний API.
- `keep/move/merge/hide` задають presentation disposition за R01, не дозвіл видалити storage або змінити domain.
- Sources є ключами таблиці нижче; у колонці Event owner позначений цим же ключем (SV/WV/KV тощо).
  `UI` означає Compose local state, `nav` — NavController. Column State називає потрібні контексти;
  відсутні actual error/saving flags описані в UX_CONTRACT, а не вважаються реалізованими.
- Routes.Status/Calendar/Cycle/Statistics/Profile та їхній порядок залишаються.
  У рядках ім'я route скорочене без Routes.; dialog/pane не є новим route.
- Читати лише потрібну секцію і її source files. Перед implementation звірити HEAD/local diff.
  Розкладка і точні move destinations є hierarchy proposals H01-H06 до approval у R04.
- Рідкісний cancel/export/target edit не є необов'язковим лише тому, що його немає на першому frame.

## Точкові джерела

`A` = `app/src/main/java/com/ihor/thesystem/`. Sources/owner aliases лише для економії повторів у таблицях.

| Source | Точний файл | Owner |
| --- | --- | --- |
| NV | [A/core/navigation/AppNavGraph.kt](../../app/src/main/java/com/ihor/thesystem/core/navigation/AppNavGraph.kt) | NavHost / Compose |
| RT | [A/core/navigation/Routes.kt](../../app/src/main/java/com/ihor/thesystem/core/navigation/Routes.kt) | Routes |
| BN | [A/core/ui/components/SystemBottomNavBar.kt](../../app/src/main/java/com/ihor/thesystem/core/ui/components/SystemBottomNavBar.kt) | Navigation |
| EN | [A/core/navigation/AppEntryViewModel.kt](../../app/src/main/java/com/ihor/thesystem/core/navigation/AppEntryViewModel.kt) | AppEntryViewModel |
| OU | [A/feature/onboarding/ui/OnboardingScreen.kt](../../app/src/main/java/com/ihor/thesystem/feature/onboarding/ui/OnboardingScreen.kt) | Onboarding screen |
| OV | [A/feature/onboarding/viewmodel/OnboardingViewModel.kt](../../app/src/main/java/com/ihor/thesystem/feature/onboarding/viewmodel/OnboardingViewModel.kt) | OnboardingViewModel |
| SS | [A/feature/status/ui/StatusScreen.kt](../../app/src/main/java/com/ihor/thesystem/feature/status/ui/StatusScreen.kt) | Status screen |
| SD | [A/feature/status/ui/RpgStatusDashboard.kt](../../app/src/main/java/com/ihor/thesystem/feature/status/ui/RpgStatusDashboard.kt) | Dashboard local state |
| TD | [A/feature/status/ui/RpgTodoBlock.kt](../../app/src/main/java/com/ihor/thesystem/feature/status/ui/RpgTodoBlock.kt) | Todos UI |
| SV | [A/feature/status/viewmodel/StatusViewModel.kt](../../app/src/main/java/com/ihor/thesystem/feature/status/viewmodel/StatusViewModel.kt) | StatusViewModel |
| TO | [A/feature/status/ui/RpgTodayOrderBlock.kt](../../app/src/main/java/com/ihor/thesystem/feature/status/ui/RpgTodayOrderBlock.kt) | TodayOrderBlock |
| CY | [A/feature/cycle/ui/CycleScreen.kt](../../app/src/main/java/com/ihor/thesystem/feature/cycle/ui/CycleScreen.kt) | Cycle screen |
| WV | [A/feature/status/viewmodel/WorkoutViewModel.kt](../../app/src/main/java/com/ihor/thesystem/feature/status/viewmodel/WorkoutViewModel.kt) | WorkoutViewModel |
| WH | [A/feature/status/ui/WorkoutDialogHost.kt](../../app/src/main/java/com/ihor/thesystem/feature/status/ui/WorkoutDialogHost.kt) | Workout dialog host / SAF / HC |
| WU | [A/feature/status/viewmodel/WorkoutUiState.kt](../../app/src/main/java/com/ihor/thesystem/feature/status/viewmodel/WorkoutUiState.kt) | Workout UI models |
| DS | [A/feature/status/viewmodel/StatusDialogState.kt](../../app/src/main/java/com/ihor/thesystem/feature/status/viewmodel/StatusDialogState.kt) | Dialog states |
| MW | [A/feature/status/ui/components/dialogs/MainQuestWorkoutDialog.kt](../../app/src/main/java/com/ihor/thesystem/feature/status/ui/components/dialogs/MainQuestWorkoutDialog.kt) | Workout modal |
| AL | [A/feature/status/ui/components/workout/ActiveDayCard.kt](../../app/src/main/java/com/ihor/thesystem/feature/status/ui/components/workout/ActiveDayCard.kt) | Logging UI |
| EI | [A/feature/status/ui/components/workout/ActiveDayExerciseInfoDialog.kt](../../app/src/main/java/com/ihor/thesystem/feature/status/ui/components/workout/ActiveDayExerciseInfoDialog.kt) | Technique/info modal |
| MT | [A/feature/statistics/ui/dialogs/MatrixDialogs.kt](../../app/src/main/java/com/ihor/thesystem/feature/statistics/ui/dialogs/MatrixDialogs.kt) | Setup/log modal |
| DD | [A/feature/status/ui/components/dialogs/WorkoutDraftDialogs.kt](../../app/src/main/java/com/ihor/thesystem/feature/status/ui/components/dialogs/WorkoutDraftDialogs.kt) | Draft confirmation/conflict |
| RP | [A/feature/status/ui/components/dialogs/WorkoutReportDialog.kt](../../app/src/main/java/com/ihor/thesystem/feature/status/ui/components/dialogs/WorkoutReportDialog.kt) | Report modal |
| WS | [A/feature/status/ui/components/dialogs/WorkoutScheduleSettingsDialog.kt](../../app/src/main/java/com/ihor/thesystem/feature/status/ui/components/dialogs/WorkoutScheduleSettingsDialog.kt) | Schedule settings / nested picker |
| BI | [A/feature/status/ui/components/dialogs/BackupImportConfirmationDialog.kt](../../app/src/main/java/com/ihor/thesystem/feature/status/ui/components/dialogs/BackupImportConfirmationDialog.kt) | Backup preview |
| AT | [A/feature/status/ui/components/dialogs/AddTaskDialog.kt](../../app/src/main/java/com/ihor/thesystem/feature/status/ui/components/dialogs/AddTaskDialog.kt) | Task form |
| CP | [A/feature/calendar/ui/CalendarScreen.kt](../../app/src/main/java/com/ihor/thesystem/feature/calendar/ui/CalendarScreen.kt) | Calendar screen |
| CV | [A/feature/calendar/viewmodel/CalendarViewModel.kt](../../app/src/main/java/com/ihor/thesystem/feature/calendar/viewmodel/CalendarViewModel.kt) | CalendarViewModel |
| KS | [A/feature/calendar/ui/CalendarSettingsScreen.kt](../../app/src/main/java/com/ihor/thesystem/feature/calendar/ui/CalendarSettingsScreen.kt) | Calendar settings |
| KV | [A/feature/calendar/viewmodel/CalendarSettingsViewModel.kt](../../app/src/main/java/com/ihor/thesystem/feature/calendar/viewmodel/CalendarSettingsViewModel.kt) | CalendarSettingsViewModel |
| ST | [A/feature/statistics/ui/StatisticsScreen.kt](../../app/src/main/java/com/ihor/thesystem/feature/statistics/ui/StatisticsScreen.kt) | Statistics screen |
| TV | [A/feature/statistics/viewmodel/StatisticsViewModel.kt](../../app/src/main/java/com/ihor/thesystem/feature/statistics/viewmodel/StatisticsViewModel.kt) | StatisticsViewModel |
| PS | [A/feature/profile/ui/ProfileScreen.kt](../../app/src/main/java/com/ihor/thesystem/feature/profile/ui/ProfileScreen.kt) | Profile / metrics dialogs |
| XS | [A/feature/exercise_search/ui/ExerciseSearchScreen.kt](../../app/src/main/java/com/ihor/thesystem/feature/exercise_search/ui/ExerciseSearchScreen.kt) | ExercisePickerScreen |
| XV | [A/feature/exercise_search/viewmodel/ExerciseSearchViewModel.kt](../../app/src/main/java/com/ihor/thesystem/feature/exercise_search/viewmodel/ExerciseSearchViewModel.kt) | ExerciseSearchViewModel |
| AR | [A/feature/architect/ui/ArchitectScreen.kt](../../app/src/main/java/com/ihor/thesystem/feature/architect/ui/ArchitectScreen.kt) | Architect screen |
| AC | [A/feature/architect/ui/ArchitectChatPanel.kt](../../app/src/main/java/com/ihor/thesystem/feature/architect/ui/ArchitectChatPanel.kt) | Architect/chat panel |
| LC | [A/feature/architect/ui/LiveChatView.kt](../../app/src/main/java/com/ihor/thesystem/feature/architect/ui/LiveChatView.kt) | Live chat field |
| AV | [A/feature/architect/viewmodel/ArchitectViewModel.kt](../../app/src/main/java/com/ihor/thesystem/feature/architect/viewmodel/ArchitectViewModel.kt) | ArchitectViewModel |
| AS | [A/feature/architect/viewmodel/ArchitectUiState.kt](../../app/src/main/java/com/ihor/thesystem/feature/architect/viewmodel/ArchitectUiState.kt) | Architect state |
| AP | [A/feature/architect/ui/AnnualProgressionPlanScreen.kt](../../app/src/main/java/com/ihor/thesystem/feature/architect/ui/AnnualProgressionPlanScreen.kt) | Annual generator |
| PV | [A/feature/architect/viewmodel/AnnualProgressionPlanViewModel.kt](../../app/src/main/java/com/ihor/thesystem/feature/architect/viewmodel/AnnualProgressionPlanViewModel.kt) | AnnualProgressionPlanViewModel |
| AD | [A/feature/statistics/ui/AnnualProgressionDetailsScreen.kt](../../app/src/main/java/com/ihor/thesystem/feature/statistics/ui/AnnualProgressionDetailsScreen.kt) | Annual details / manual host |
| DV | [A/feature/statistics/viewmodel/AnnualProgressionDetailsViewModel.kt](../../app/src/main/java/com/ihor/thesystem/feature/statistics/viewmodel/AnnualProgressionDetailsViewModel.kt) | AnnualProgressionDetailsViewModel |
| AM | [A/feature/statistics/ui/AnnualProgressionManualEditorScreen.kt](../../app/src/main/java/com/ihor/thesystem/feature/statistics/ui/AnnualProgressionManualEditorScreen.kt) | Manual annual editor |
| WA | [A/feature/architect/ui/WorkoutAnalysisScreen.kt](../../app/src/main/java/com/ihor/thesystem/feature/architect/ui/WorkoutAnalysisScreen.kt) | Workout analysis |
| AW | [A/feature/architect/viewmodel/WorkoutAnalysisViewModel.kt](../../app/src/main/java/com/ihor/thesystem/feature/architect/viewmodel/WorkoutAnalysisViewModel.kt) | WorkoutAnalysisViewModel |
| DU | [domain/src/main/java/com/ihor/thesystem/domain/usecase/OnboardingUseCases.kt](../../domain/src/main/java/com/ihor/thesystem/domain/usecase/OnboardingUseCases.kt) | Onboarding domain |
| LE | [domain/src/main/java/com/ihor/thesystem/domain/usecase/LogWorkoutSetsUseCase.kt](../../domain/src/main/java/com/ihor/thesystem/domain/usecase/LogWorkoutSetsUseCase.kt) | Targeted history write |
| SC | [A/feature/status/ui/components/dialogs/SystemConfigDialog.kt](../../app/src/main/java/com/ihor/thesystem/feature/status/ui/components/dialogs/SystemConfigDialog.kt) | Dormant config form |

## Entry і навігація

| ID | Route / host | Дія та доступність | Event -> owner | State | Disposition / ціль | Sources |
| --- | --- | --- | --- | --- | --- | --- |
| NAV-01 | App entry / Onboarding / Status | W: вибрати стартовий route | EN.startDestination -> DU.ObserveAppStartDestinationUseCase | null/loading, completion flag | keep; P05/P07 usable-data guard planned | EN,DU,NV |
| NAV-02 | 5 bottom tabs | W: обрати Статус/Календар/Система/Статистика/Профіль | BN.navigateTopLevel -> nav | selected/unselected/back stack | keep усі 5, порядок unchanged | BN,RT,NV |
| NAV-03 | 5 top-level routes | W: горизонтальний перехід між tabs | NV.topLevelSwipeNavigation -> nav | horizontal intent / vertical scroll | keep як optional, button access завжди є; P18/P19 | NV |
| NAV-04 | Secondary routes | W: back/close без завершення дії | popBackStack / onBack / onAcknowledge -> nav | view/cancel/saving guards за host | keep, не перетворювати back на save | NV,AR,KS,AD,AP,XS,WA |
| NAV-05 | Primary routes | W: foreground refresh | RefreshOnResume -> SV.refreshForCurrentDay / WV.refreshForCurrentDay / CV.refreshVisibleData / TV.refreshForCurrentDay / AV.refreshForCurrentData | loading/content/stale date | keep; date не reset мовчки для history, G02 | SS,CY,CP,ST,PS,AR |

## Onboarding

| ID | Route / host | Дія та доступність | Event -> owner | State | Disposition / ціль | Sources |
| --- | --- | --- | --- | --- | --- | --- |
| ONB-01 | Onboarding / NAME | W: ввести ім'я | OV.onNameChanged | answers, invalid/blank | keep коротку форму | OU,OV |
| ONB-02 | Onboarding / GOAL | W: обрати ціль | OV.onGoalSelected | selectedGoal | keep; P07 придатний план | OU,OV |
| ONB-03 | Onboarding / EQUIPMENT | W: toggle обладнання | OV.onEquipmentToggled | selectedEquipment | keep, без HC permission gate | OU,OV |
| ONB-04 | Onboarding / EXPERIENCE | W: обрати досвід | OV.onExperienceSelected | selectedExperience | keep; plan difficulty P07, не тільки RPG | OU,OV |
| ONB-05 | Onboarding / CYCLE | W: preset | OV.onCyclePresetSelected | selectedCyclePreset | keep, schedule creation planned P07 | OU,OV,DU |
| ONB-06 | Onboarding | W: назад на попередній крок | OV.onBack | canGoBack, !isSaving | keep answers | OU,OV |
| ONB-07 | Onboarding | W: далі / завершити / retry | OV.onContinue -> DU.CompleteOnboardingUseCase | canContinue/isSaving/errorMessage | keep; failure не Complete | OU,OV,DU |
| ONB-08 | Onboarding -> Status | W: completed transition | OnboardingEvent.Completed -> nav | successful config writes | keep; придатний schedule ще planned | OV,NV |

## Status і прості справи

Approved design override2026-10-14/[D27](R04_SELECTED_DESIGN.md#progress-placement--d27):
окремий бокс «Деталі прогресу» вже прибрано з Figma Status; його інформація належить Statistics,
design R06/native R12, Status integration R08. Це не видалення даних чи інших actions із таблиці.
Історична назва Figma-групи ProgressDetails-STA03 не змінює зміст STA-03 (swipe modes) нижче.
D28: Status weekly summary caption removed from Figma; keep STA-02 date/calendar access,
all7 date/state cues and underlying weekly metrics. Native presentation follows in R08.

| ID | Route / host | Дія та доступність | Event -> owner | State | Disposition / ціль | Sources |
| --- | --- | --- | --- | --- | --- | --- |
| STA-01 | Status / Today | W: основна Today CTA | onStartWorkout -> nav Cycle | order.actionEnabled/type | keep; typed action mapping P08/R08, не auto-start | SS,TO,NV |
| STA-02 | Status / week/hero | W: відкрити календар | SV.onOpenCalendarTap -> nav Calendar | today/week/date | keep week та calendar entry | SS,SD,SV |
| STA-03 | Status / ACTIONS-STATUS | W: внутрішній горизонтальний swipe | UI.dashboardState / resolveSwipeTarget | ACTIONS/STATUS | merge доступ H01/F52; обидва набори дій зберегти | SD |
| STA-04 | Status / hero | W: вибрати аватар / SAF photo cancel | photo picker -> SV.updateAvatarUri | URI/cancel | keep, той самий handler Profile | SD,SS,SV |
| STA-05 | Status / hero | W: ім'я / confirm / cancel | SV.onEditNameTap/onNameConfirmed/onDismissDialog | EditName | keep, equivalent Profile entry | SS,SD,SV |
| STA-06 | Status / todos | W: створити просте завдання | SV.onAddTaskTap/onAddTaskConfirmed | AddTask / text / cancel | keep; аргумент questId перевірити G05 | TD,SS,SV,AT |
| STA-07 | Status / todos | W: toggle completed | SV.onTodoToggled | todo.isCompleted | keep, не тренувальний proof | TD,SS,SV |
| STA-08 | Status / todos | W: drag/reorder | SV.onTodosReordered | ordered IDs / cancel drag | keep, F35 не дозволяє втрату порядку | TD,SS,SV |
| STA-09 | Status / todos | W: видалити todo | SV.onRemoveTodo | selected todo id | keep current path; це не quest-task removal | TD,SS,SV |
| STA-10 | Status / AddMicrotask | U: entry визначений, TodoBlock його не викликає | SV.onAddMicrotaskTap/onAddMicrotaskConfirmed | AddMicrotask / parentTodoId | move advanced F35; чинні child дані зберегти, G06 | SS,TD,SV,AT |
| STA-11 | Status | W: level-up acknowledge | StatusOneOffEvent.ShowLevelUp -> UI dismiss | levelUpEvent | keep optional short notice R14/R15 | SS,SV |
| STA-12 | Status error | W: retry DB/status load | SV.refreshForCurrentDay | databaseStatus/UiState.Error | keep | SS,SV |
| STA-13 | Status / settings | U: callback та legacy block, у чинному ACTIONS/STATUS не використаний | onOpenWorkoutSettings -> nav Cycle | legacy MainQuestBlock | merge з видимим Cycle/Profile settings, не fake button | SS,SD |

## Calendar і CalendarSettings

| ID | Route / host | Дія та доступність | Event -> owner | State | Disposition / ціль | Sources |
| --- | --- | --- | --- | --- | --- | --- |
| CAL-01 | Calendar | W: попередній/наступний місяць | CV.onMonthChange | currentMonth/loading | keep | CP,CV |
| CAL-02 | Calendar | W: обрати день | CV.onDateSelected | selectedDate, plan/history/no-data | keep selected-date контекст | CP,CV |
| CAL-03 | Calendar -> Cycle/Status | W: відкрити обраний день | CV.onDateSelected -> conditional nav | hasTrainingPlan | keep; date reset G02, не historical editor | CP,CV,NV,WV |
| CAL-04 | Calendar -> CalendarSettings | W: налаштування | nav CalendarSettings | month/date retained | keep | CP,NV |
| CAL-05 | Calendar/day details | U: прямі todo mutations/history edit | callbacks не підключені у CP | read-only summaries | keep через Status/Statistics paths; P12/R10 target context | CP,SS,ST |
| CFG-01 | CalendarSettings | W: template | KV.onTemplateSelected | selectedTemplate/draft | keep | KS,KV |
| CFG-02 | CalendarSettings | W: назва/start date | KV.onCycleNameChanged/onStartDateChanged | isDirty/errorMessage | keep fields | KS,KV |
| CFG-03 | CalendarSettings | W: length +/- / repeats | KV.onCycleLengthIncreased/onCycleLengthDecreased/onRepeatsChanged | bounds/isDirty | keep | KS,KV |
| CFG-04 | CalendarSettings | W: назва/type дня | KV.onDayNameChanged/onDayTypeChanged | day draft/WORK/NIGHT/RECOVERY/OFF/CUSTOM | keep; business parity P10/P11 | KS,KV |
| CFG-05 | CalendarSettings | W: add/remove/up/down дня | KV.onAddDay/onRemoveDay/onMoveDayUp/onMoveDayDown | min cycle length/order | keep усі mutation paths | KS,KV |
| CFG-06 | CalendarSettings | W: сьогоднішній anchor | KV.onTodayCycleDaySelected | todayCycleDayIndex | keep, explicit action | KS,KV |
| CFG-07 | CalendarSettings | W: save / error retry | KV.onSaveCycle -> CalendarSettingsEvent.Saved -> onBack | isSaving/validation/error | keep; success після save | KS,KV,NV |
| CFG-08 | CalendarSettings | W: cancel/back | onBack -> popBackStack | draft, not saving | keep, не implicit save | KS,NV |

## Cycle і workout settings

| ID | Route / host | Дія та доступність | Event -> owner | State | Disposition / ціль | Sources |
| --- | --- | --- | --- | --- | --- | --- |
| CYC-01 | Cycle | W: вибрати день циклу | WV.onCycleDaySelected | cycleDays.isSelected/isActive | keep preview, не auto-activation | CY,WV |
| CYC-02 | Cycle | W: активувати selected день сьогодні | WV.onActivateSelectedCycleDayToday | selected != active | keep explicit anchor mutation | CY,WV |
| CYC-03 | Cycle | W: start/resume | WV.onOpenMainWorkout | activeWorkout/draft compatibility | keep, no empty start | CY,WV,WH |
| CYC-04 | Cycle -> settings | W: edit cycle/day | WV.onOpenWorkoutSettings | selected day/config/load | keep; той самий Profile settings owner | CY,WV,WH |
| CYC-05 | Cycle -> ExercisePicker | W: add exercise | nav ExercisePicker(cycle,cycleDay) | selected day | keep; save lifetime P13 | CY,NV,XV |
| CYC-06 | Cycle -> Architect | W: analyze/local assistant | nav Architect | local/remote availability | keep local benefit, не promise paid AI | CY,NV,AR |
| CYC-07 | Cycle / focus button | W: налаштувати фокус | WV.onOpenWorkoutSettings | settings | merge duplicate settings entry H03, не окремий focus API | CY,WV |
| CYC-08 | Cycle / attributes/bonuses | W: read-only presentation | statusData fields -> UI | RPG counters | hide/merge F37-F39 лише presentation A | CY,SV |
| SET-01 | Cycle/Profile settings | W: close/back/select day | WV.onDismissDialog/onSettingsSelectDay | settings selectedDay/isLoading | keep | WH,WS,WV |
| SET-02 | settings / name | W: name edit + explicit save | WV.onWorkoutNameChange/onSaveWorkoutName | workoutNameDraft | keep, не invent global Save | WH,WS,WV |
| SET-03 | settings / panes | W: Day/Library/Equipment tabs | UI.selectedPane | pane | keep equivalent доступ H03/H05 | WS |
| SET-04 | settings / nested picker | W: відкрити/скасувати picker | UI.showExercisePicker | nested Screen, parent retained | keep обидва picker paths | WS,XS |
| SET-05 | settings / picker | W: add existing / create custom | WV.onAddExerciseToDay/onCreateExercise | selectedDay, name | keep; create лише nested picker, P13 | WS,WH,WV,XS |
| SET-06 | settings / day | W: прибрати вправу з дня | WV.onRemoveExerciseFromDay | exerciseId/day | keep, не delete catalog | WS,WH,WV |
| SET-07 | settings / library | W: delete catalog exercise | WV.onDeleteExercise | exerciseId/catalog | keep scope-confirm semantics при R11; не плутати SET-06 | WS,WH,WV |
| SET-08 | settings / day/library | W: tracking mode | WV.onExerciseTrackingModeChanged | mode | keep weight/bodyweight/time | WS,WH,WV |
| SET-09 | settings / equipment | W: gym/home/equipment | WV.onEquipmentLocationChanged/onEquipmentAvailabilityChanged | equipmentProfile | keep | WS,WH,WV |
| SET-10 | settings / equipment | W: dumbbell max input | WV.onDumbbellMaxKgChanged | draft numeric value | keep actual auto-write contract, test R11 | WS,WH,WV |
| SET-11 | settings / delete-all | D/U: unused formal/no-op host | onDeleteAllExercises = {} | немає робочого bulk action | hide no-op, не заявляти shipped delete-all; G04 | WS,WH |

## Workout, draft, setup та report

| ID | Route / host | Дія та доступність | Event -> owner | State | Disposition / ціль | Sources |
| --- | --- | --- | --- | --- | --- | --- |
| WRK-01 | Cycle/MainQuestWorkout | W: weight/reps/time input | WV.onSetWeightChanged/onSetRepsChanged | raw set input/trackingMode | keep; time input через mode, не новий timer | WH,AL,WU,WV |
| WRK-02 | workout | W: input focus loss | WV.onSetFocusLost | validation/raw input | keep, не completion | WH,AL,WV |
| WRK-03 | workout | W: completed checkbox | WV.onSetCompletionChanged | isCompleted / partial | keep, previous values не auto-done | WH,AL,WV |
| WRK-04 | workout | W: add set | WV.onAddWorkoutSet | exerciseId/current inputs | keep, не invented remove-set CTA | WH,AL,WV |
| WRK-05 | workout | W: technique/info open/close | UI.showInfo | ExerciseInfoDialog | keep contextual help | AL,EI |
| WRK-06 | workout -> SetupMatrix | W: goals setup | WV.onOpenSetup/onConfirmSetup | start/target/usesExternalLoad | keep; cancel/success повертає workout за showWorkoutAfter | WH,AL,MT,WV |
| WRK-07 | workout | W: dismiss/back | WV.onDismissDialog | !isFinishing, durable draft | keep draft, не discard | MW,WH,WV |
| WRK-08 | workout | W: finish | WV.onFinishWorkout | loggingSummary.canFinish/isFinishing | keep commit/reward/report, failure G03, P03 device gate | MW,WH,WV |
| DRF-01 | workout | W: request discard | WV.onRequestDiscardWorkoutDraft | hasSavedDraft -> ConfirmDiscardWorkoutDraft | keep confirmation | MW,WH,WV,DD |
| DRF-02 | discard confirm | W: cancel / confirmed discard | WV.onOpenMainWorkout/onConfirmDiscardWorkoutDraft | explicit draftId/openCurrentWorkout | keep, cancel не delete | WH,DD,WV |
| DRF-03 | draft conflict | W: keep / discard-and-continue | WV.onKeepWorkoutDraft/onConfirmDiscardWorkoutDraft | incompatible date/plan, draftId | keep choice, no silent merge | WH,DD,WV |
| REP-01 | WorkoutReport | W: acknowledge/dismiss | WV.onDismissDialog | committed local/AI fallback report | keep | RP,WH,WV |
| REP-02 | report -> WorkoutAnalysis | W: details | onOpenWorkoutAnalysis(report.sessionId) -> nav | saved session ID | keep, не repeat save | RP,WH,CY,NV,WA |
| LOG-01 | LogWorkoutSets dialog | U entry; host wired: update/add/remove | WV.updateLogSetInput/addLogSet/removeLogSet | LogWorkoutSets/existingLogs | keep contract, restore entry G01; не active logger remove | WH,MT,WV |
| LOG-02 | LogWorkoutSets dialog | U entry; host wired: save/feedback/cancel | WV.onLogSetsConfirmed/onDismissDialog | showWorkoutAfter/selectedDate | keep manual log distinction; sessionId=null не target edit | WH,MT,WV |
| LOG-03 | Statistics historical correction | P: explicit session + exercise selection/edit | LE.LogWorkoutSetsUseCase(sessionId,exerciseId,sets) | target IDs, saving/error/saved | keep Free F16; visible entry R12/P12/P18, G01 | LE,TV,ST |
| LOG-04 | legacy Statistics dialogs | U: matrix/log/body/profile APIs | TV.onOpenSetup/onOpenLogSets/onLogSetsConfirmed | StatisticsDialogState | merge actual body actions into Profile; не дублювати unsafe manual write як edit | TV,ST,PS |

## Profile, HC та backup

| ID | Route / host | Дія та доступність | Event -> owner | State | Disposition / ціль | Sources |
| --- | --- | --- | --- | --- | --- | --- |
| PRO-01 | Profile | W: avatar pick/cancel | photo picker -> SV.updateAvatarUri | URI/cancel | keep shared with STA-04 | PS,SV |
| PRO-02 | Profile/EditName | W: name/open/confirm/cancel | SV.onEditNameTap/onNameConfirmed/onDismissDialog | EditName | keep shared | PS,SV |
| PRO-03 | Profile/LogWeight | W: weight/open/save/cancel | SV.onOpenLogWeight/onWeightConfirmed/onDismissDialog | optional metric/form | keep Free | PS,SV |
| PRO-04 | Profile/EditHeight | W: height/open/save/cancel | SV.onOpenEditHeight/onHeightConfirmed/onDismissDialog | optional metric/form | keep contextual | PS,SV |
| PRO-05 | Profile/EditAge | W: age/open/save/cancel | SV.onOpenEditAge/onAgeConfirmed/onDismissDialog | optional metric/form | keep contextual | PS,SV |
| PRO-06 | Profile -> settings | W: workout settings | UI.workoutSettingsRequestId -> WV.onOpenWorkoutSettings | lazy host/request | keep all SET/DAT actions; H05 location proposed | PS,WH,WV |
| PRO-07 | Profile -> CalendarSettings | W: calendar settings | nav CalendarSettings | settings | keep | PS,NV,KS |
| PRO-08 | Profile -> Statistics | W: profile statistics | nav Statistics | history/summary | keep | PS,NV,ST |
| DAT-01 | settings/Equipment/HC | W: connect permission | WH.healthPermissionLauncher.launch -> WV.healthConnectPermissionRequest | available/permission/loading | move visible permissions H05, keep callback | WS,WH,WV,WU |
| DAT-02 | HC result/refusal | W: refresh after grant/deny | WV.onHealthConnectPermissionsChanged | no-HC/denied/granted | keep Free; P15 rationale/revoke pending | WH,WV,WU |
| DAT-03 | settings/backup export | W: pick destination / export / cancel | CreateDocument -> WV.exportBackupJson -> output stream | SAF URI/null/isBusy | move data section H05; no success before file write | WS,WH,WV |
| DAT-04 | settings/backup import | W: select/read/preview/cancel | OpenDocument -> WV.previewBackupImportJson | SAF null/read failure/pendingImport | move data section, preserve preview | WS,WH,WV,WU |
| DAT-05 | BackupImportConfirmation | W: confirm/cancel | WV.confirmBackupImport/cancelBackupImportPreview | pendingImport/isBusy | keep validation/explicit confirm; P04 atomic payload pending | BI,WS,WH,WV |
| DAT-06 | backup file failure | W handler, feedback consumer gap | WV.onBackupFileOperationFailed/onBackupImportFileReadFailed | event/error/busy | keep visible failure planned G03/R13 | WH,WV |

## ExercisePicker: route і nested screen

| ID | Route / host | Дія та доступність | Event -> owner | State | Disposition / ціль | Sources |
| --- | --- | --- | --- | --- | --- | --- |
| PCK-01 | ExercisePicker / nested picker | W: query / clear | XV.onEvent(UpdateQuery) | filterState.query | keep, empty results distinguish from load | XS,XV |
| PCK-02 | picker | W: core/group/equipment filters | XV.onEvent(ToggleSystemCore/ToggleMuscleGroup/ToggleEquipment) | filterState/pickerItems | keep actual exposed filters | XS,XV |
| PCK-03 | picker cycle route | W: choose/add -> return | WV.onAddExerciseToDay(exercise.id,cycleDay) -> popBackStack | cycleDay/source | keep persistence completion before lifetime ends P13 | XS,NV,WV |
| PCK-04 | picker annual route | W: choose -> return result | previousBackStackEntry.savedStateHandle -> PV.onExerciseSelected | PICKER_RESULT_EXERCISE_ID, consumed once | keep return channel | XS,NV,PV |
| PCK-05 | nested settings picker | W: choose/create/back | SET-05 / UI.showExercisePicker=false | parent settings/day preserved | keep; route picker create bar absent by default | WS,XS,WV |
| PCK-06 | picker | W: cancel/back/unknown source | popBackStack / parent local back | no selected exercise | keep, no implicit add | XS,NV,WS |
| PCK-07 | picker filters | U: extra enums not exposed here | XV ToggleMuscle/Level/Mechanic/Force/Category events | repository filters | keep dormant contracts, не вигадувати додатковий UI | XV,XS |

## Statistics і annual artifacts

| ID | Route / host | Дія та доступність | Event -> owner | State | Disposition / ціль | Sources |
| --- | --- | --- | --- | --- | --- | --- |
| STS-01 | Statistics | W: retry | TV.refreshForCurrentDay | UiState.Error/Loading/Content | keep, no fake proof | ST,TV |
| STS-02 | Statistics -> Cycle | W: workout CTA | onLogWorkout -> nav Cycle | empty/weekly summary | keep, не history correction | ST,NV |
| STS-03 | Statistics -> AnnualProgressionDetails | W: details | onOpenAnnualProgression -> nav | annual/sparse/empty | move advanced F23; existing artifacts readable Free | ST,AD,NV |
| STS-04 | Statistics/beta block | W: supplemental metrics load (automatic) | TV.loadSupplementalMetrics | lazy block entry | move diagnostics F48, recorder keep | ST,TV |
| ANN-01 | Architect/AnnualProgressionPlan | W: back, today/start date picker confirm/cancel | nav back / PV.onUseTodayAsStartDate/onStartDateSelected | date/generatedPlan reset | move advanced F23, local generator | AP,PV,NV |
| ANN-02 | AnnualProgressionPlan -> picker | W: add exercise via picker | nav annual picker -> PV.onExerciseSelected | isLoadingExercise/error message | keep data/access, P13 return proof | AP,PV,NV |
| ANN-03 | annual plan | W: remove/expand exercise | PV.onRemoveExercise/onExerciseExpanded | exercises/draft | keep | AP,PV |
| ANN-04 | annual plan | W: target/inventory step input | PV.onTargetWeightChanged/onInventoryStepChanged | validation/generatedPlan reset | keep | AP,PV |
| ANN-05 | annual plan | W: generate / expand result | PV.onGeneratePlan/onResultExpanded | isGenerating/generatedPlan/message | keep local; defer promotion, no AI claim | AP,PV |
| ANN-06 | annual plan | W: save / failure retry | PV.onSavePlan/onMessageShown | isSaving/saved message | keep, P14 atomic proof required | AP,PV |
| DET-01 | AnnualProgressionDetails | W: back/select exercise | nav back / DV.onExerciseSelected | loading/hasData/selectedExercise | keep | AD,DV,NV |
| DET-02 | annual empty -> generator | W: create in planner | onCreateInAi -> nav AnnualProgressionPlan | empty | keep, label misleading AI is copy proposal | AD,NV,AP |
| DET-03 | annual empty -> nested manual editor | W: create manually | DV.onCreateManually | manualEditorState.isOpen/isLoading | keep existing flow; не new route | AD,DV,AM |
| DET-04 | manual editor | W: paginate/change monthly target | UI.page / DV.onManualTargetChanged | monthIndex/exerciseId/draft | keep units/required months | AM,DV |
| DET-05 | manual editor | W: save/retry/message shown | DV.onSaveManualPlan/onManualMessageShown | canSave/isSaving/message | keep, P14 multi-write proof | AM,AD,DV |
| DET-06 | manual editor | W: cancel/back | DV.onCloseManualEditor | no close while isSaving | keep returns details, not outer nav pop | AM,AD,DV |

## Architect і WorkoutAnalysis

| ID | Route / host | Дія та доступність | Event -> owner | State | Disposition / ціль | Sources |
| --- | --- | --- | --- | --- | --- | --- |
| ARC-01 | Architect -> annual | W: local annual planner | onOpenAnnualProgression -> nav | always enabled entry | move advanced F23, не dead paywall | AR,NV,AP |
| ARC-02 | Architect -> WorkoutAnalysis() | W gated entry | onOpenWorkoutAnalysis -> nav(sessionId=0) | CONFIGURED + lastWorkoutContext + !isLoading | keep optional local result access via REP-02; R13 fallback | AR,NV,WA |
| ARC-03 | Architect chat | W: Architect/Trainer mode | UI.selectedChatMode | messages/chatHistory | move optional advanced F43, no new promotion | AR,AC |
| ARC-04 | Architect thread | W gated: analyze | AV.sendForAnalysis | availability/loading/analysisAlreadySent | keep optional, no-AI brief first | AR,AC,AV |
| ARC-05 | Architect thread | W: apply suggestions | AV.applyRecommendations(recs) | typed recommendation/current signal | keep validated gate P16, no mutation from Compose | AR,AC,AV |
| ARC-06 | Live chat | W gated: message/edit/send | UI.messageText -> AV.sendMessage(0L,text) | availability/nonblank/history/loading | move/defer F43, preserve history | AR,LC,AV |
| ARC-07 | Architect | W: events/acknowledge/back | AV.uiEvents -> toast / onAcknowledge -> pop | ShowError/NavigateBack | keep honest feedback/local mode | AR,AV,NV |
| ARC-08 | Architect / cycle correction | D: disabled card, empty callback | enabled=false/onClick={} | unavailable | hide F45 approved A | AR |
| ARC-09 | Architect / tomorrow plan | D: disabled card, empty callback | enabled=false/onClick={} | unavailable | hide F46 approved A; Calendar/next step keep | AR |
| ANA-01 | WorkoutAnalysis(sessionId) | W: back/retry | nav back / AW.loadAnalysis | Loading/Error/Content(null/data) | keep per-session context | WA,AW,NV |
| ANA-02 | analysis/motivation info | W local info open/close | UI.showMotivationInfo | info dialog | hide/merge F38 presentation; real execution/result keep | WA |

## Dormant contracts та planned дії

| ID | Route / host | Дія та доступність | Event -> owner | State | Disposition / ціль | Sources |
| --- | --- | --- | --- | --- | --- | --- |
| DOR-01 | StatusDialogState.EditSystemConfig | U: enum/form/save method, no current host branch | SV.onSystemConfigConfirmed | existing config | keep model, expose лише потрібні settings за R01/R11 | DS,SC,SS,SV |
| DOR-02 | QuestChecklist / quest task | U: enum/methods, no current Status host branch | SV.onTaskToggled/onRemoveTask | questId/taskId | merge Main quest з реальною Today дією F32; not todo handlers | DS,SS,SV |
| DOR-03 | Status force day end | U: метод без current action | SV.onForceEndDay | daily sync | keep domain contract, не додавати новий compulsory button | SV,SS |
| PLN-01 | Onboarding -> usable schedule | P: відсутній Schedule write | P07 CompleteOnboarding contract | no-plan/saving/saved | keep core F05, R08 | DU,OV |
| PLN-02 | Status check-in | P: немає connected save UI | P09 ViewModel/use case contract | unknown/input/validation/saved | keep optional F10, не вигаданий existing event | SS,SV |
| PLN-03 | historical session edit | P: видимий target editor G01 | P12/R12 explicit IDs | selection/edit/saved/error | keep Free F16, ці рядки не означають implementation | ST,TV,LE |
| PLN-04 | return / recovery / next day | P: наскрізна єдність P10-P12 | existing domain owners via ViewModel | OFF/REST/missed/unknown/next | keep core, supportive R14 | SV,WV,CV |

## Виявлені прогалини: exact owner і перевірка

| ID | Статична знахідка / evidence | Наслідок / задача | Owner та доказ закриття |
| --- | --- | --- | --- |
| UX-R02-G01 | ST не споживає TV.dialogState/uiEvents; onOpenLogSets має лише declaration, WH log branch не має live opener. WV/TV manual save передають sessionId=null | Немає підтвердженого visible history correction entry; manual new record не є target edit | P12/R12, R09 legacy path; P18 select actual saved session/exercise -> edit -> only target changed. P01 domain proof не скасовується |
| UX-R02-G02 | CP selected-day -> Cycle/Status без date route arg; CY RefreshOnResume -> WV.refreshForCurrentDay -> selectToday | Обраний historical/future day не гарантовано лишається контекстом відкритої дії | R10/P12, R11; P18 date A -> open -> back має A/context або явно повертає today |
| UX-R02-G03 | 2026-10-15: shared UiErrorEffect added in SS/PS/WH; WH covers Cycle workout events | Foreground ShowError consumer now exists; durable save/retry state and device visibility still need proof | R09/R13/P18 failure-state tests; [DF09/evidence](../remaster/evidence/2026-10-15-r05-data-flow.md) |
| UX-R02-G04 | WH onDeleteAllExercises={} і WS formal не має поточного UI consumer | No-op contract не є діючою bulk-delete фічею; не будувати fake кнопку в Figma | R11 keep known working individual paths, no no-op CTA; P18 |
| UX-R02-G05 | 2026-10-15: TD/dashboard now use parameterless onAddTask; SS binds onAddTaskTap(0) for both controls | Removed count-as-questId chain; legitimate quest add API retained | TodoAddActionTest covers0/3 existing items; device execution distinct from compile; [DF08/evidence](../remaster/evidence/2026-10-15-r05-data-flow.md) |
| UX-R02-G06 | TD приймає onAddMicrotask, але не викликає; AddMicrotask host/method лишаються | Nested entry dormant; не вважати вже discoverable, не губити existing child дані | R08 за F35 approved advanced scope; preserve/read existing children, explicit disposition R06 |

Ці gaps відкриті, не виправлені R02. Прогалини UI знайдені при читанні цільових roots, не whole-repo runtime audit.
R08-R14 спочатку перечитують лише свій gap/event contract і current code; при потребі оформлюють repair
за DOC_MAINTENANCE. Не розширювати UI task до schema/AI redesign лише через таблицю.

## Передача до Figma та rollout

R03 використовує існуючі foundations. R04 затверджує конкретні H01-H06/frame changes із UX_CONTRACT.
R05 prototype має wired actions і явно planned business states; disabled/dormant entries не видаються за готові.
R06 для кожного secondary surface/dialog записує new-approved node або preserve-current baseline,
а для кожної action ID — equivalent destination чи approved hide із source F-ID.
R08-R14 у своїй секції позначають implemented path/evidence; P18 проходить реальні сценарії.
Після зміни contract/route/callback оновити конкретний рядок/source revision. Галочка існування MD
не доводить тесту logging, restore, cancel або accessibility.
