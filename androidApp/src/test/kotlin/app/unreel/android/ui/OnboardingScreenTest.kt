package app.unreel.android.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OnboardingScreenTest {

    @Test
    fun `OnboardingStep entries exist in correct sequence`() {
        val steps = OnboardingStep.entries
        assertEquals(3, steps.size)
        assertEquals(OnboardingStep.WELCOME, steps[0])
        assertEquals(OnboardingStep.ACCESSIBILITY, steps[1])
        assertEquals(OnboardingStep.SHIELDING, steps[2])
    }

    @Test
    fun `OnboardingStep metadata contains clear user-facing titles and privacy descriptions`() {
        val welcome = OnboardingStep.WELCOME
        assertEquals("Know Your Scroll", welcome.title)
        assertTrue(welcome.description.contains("privacy") || welcome.description.contains("Privacy"))

        val accessibility = OnboardingStep.ACCESSIBILITY
        assertEquals("Exact Reel Counting", accessibility.title)
        assertTrue(accessibility.description.contains("Accessibility"))

        val shielding = OnboardingStep.SHIELDING
        assertEquals("Shields & Survival", shielding.title)
        assertTrue(shielding.description.contains("Overlay") || shielding.description.contains("shield"))
    }

    @Test
    fun `calculateNextStep progresses through steps and clamps at end`() {
        assertEquals(1, calculateNextStep(currentIndex = 0, totalSteps = 3))
        assertEquals(2, calculateNextStep(currentIndex = 1, totalSteps = 3))
        assertEquals(2, calculateNextStep(currentIndex = 2, totalSteps = 3))
    }

    @Test
    fun `calculatePreviousStep moves backward and clamps at start`() {
        assertEquals(0, calculatePreviousStep(currentIndex = 0))
        assertEquals(0, calculatePreviousStep(currentIndex = 1))
        assertEquals(1, calculatePreviousStep(currentIndex = 2))
    }

    @Test
    fun `isLastStep returns true only on the final step`() {
        assertFalse(isLastStep(currentIndex = 0, totalSteps = 3))
        assertFalse(isLastStep(currentIndex = 1, totalSteps = 3))
        assertTrue(isLastStep(currentIndex = 2, totalSteps = 3))
    }

    @Test
    fun `KEY_HAS_COMPLETED_ONBOARDING matches expected key for multiplatform settings`() {
        assertEquals("has_completed_onboarding", KEY_HAS_COMPLETED_ONBOARDING)
    }

    @Test
    fun `OnboardingStep subtitles provide clear context for each screen`() {
        assertEquals("Reclaim your time from mindless feeds", OnboardingStep.WELCOME.subtitle)
        assertEquals("Precise scroll detection on Android", OnboardingStep.ACCESSIBILITY.subtitle)
        assertEquals("Overlay alerts and background protection", OnboardingStep.SHIELDING.subtitle)
    }

    @Test
    fun `navigation edge cases handle single step bounds gracefully`() {
        assertEquals(0, calculateNextStep(currentIndex = 0, totalSteps = 1))
        assertEquals(0, calculatePreviousStep(currentIndex = 0))
        assertTrue(isLastStep(currentIndex = 0, totalSteps = 1))
    }
}
