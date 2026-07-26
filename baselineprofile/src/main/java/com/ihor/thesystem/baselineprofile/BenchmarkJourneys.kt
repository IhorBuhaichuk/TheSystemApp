package com.ihor.thesystem.baselineprofile

import android.os.SystemClock
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until

internal const val TARGET_PACKAGE = "com.ihor.thesystem"

private const val ONBOARDING_TITLE = "Система: новий рівень"
private const val NEXT_LABEL = "Далі"
private const val FINISH_ONBOARDING_LABEL = "Перейти до плану"
private const val STATISTICS_LABEL = "Статистика"
private const val STATISTICS_SUBTITLE = "Підсумок і прогрес"
private const val UI_TIMEOUT_MILLIS = 20_000L

internal fun MacrobenchmarkScope.ensureReturningUser() {
    val deadline = SystemClock.uptimeMillis() + UI_TIMEOUT_MILLIS
    var onboarding = device.findObject(By.text(ONBOARDING_TITLE))
    while (onboarding == null && !device.hasObject(By.text(STATISTICS_LABEL))) {
        check(SystemClock.uptimeMillis() < deadline) {
            "Neither onboarding nor main navigation became visible"
        }
        SystemClock.sleep(100L)
        onboarding = device.findObject(By.text(ONBOARDING_TITLE))
    }
    onboarding ?: return

    check(onboarding.isEnabled) { "Onboarding screen is not interactive" }
    val nameField = device.wait(
        Until.findObject(By.clazz("android.widget.EditText")),
        UI_TIMEOUT_MILLIS
    )
    checkNotNull(nameField) { "Onboarding name field was not found" }
    nameField.text = "Benchmark"
    device.pressBack()

    repeat(4) {
        clickText(NEXT_LABEL)
    }
    clickText(FINISH_ONBOARDING_LABEL)

    check(device.wait(Until.hasObject(By.text(STATISTICS_LABEL)), UI_TIMEOUT_MILLIS)) {
        "Main navigation did not appear after onboarding"
    }
}

internal fun MacrobenchmarkScope.openStatistics() {
    check(device.wait(Until.hasObject(By.text(STATISTICS_LABEL)), UI_TIMEOUT_MILLIS)) {
        "Statistics destination was not found"
    }
    val statisticsDestination = device.findObjects(By.text(STATISTICS_LABEL))
        .maxByOrNull { it.visibleBounds.top }
    checkNotNull(statisticsDestination) { "Statistics destination disappeared before click" }
    statisticsDestination.click()

    check(device.wait(Until.hasObject(By.text(STATISTICS_SUBTITLE)), UI_TIMEOUT_MILLIS)) {
        "Statistics dashboard did not become visible"
    }
}

internal fun MacrobenchmarkScope.scrollStatistics() {
    val width = device.displayWidth
    val height = device.displayHeight
    device.swipe(
        width / 2,
        (height * 0.82f).toInt(),
        width / 2,
        (height * 0.28f).toInt(),
        24
    )
    device.waitForIdle()
}

private fun MacrobenchmarkScope.clickText(text: String) {
    val target = device.wait(Until.findObject(By.text(text)), UI_TIMEOUT_MILLIS)
    checkNotNull(target) { "Could not find onboarding action: $text" }
    target.click()
    device.waitForIdle()
}
