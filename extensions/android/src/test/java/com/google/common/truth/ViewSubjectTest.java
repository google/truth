package com.google.common.truth;

import android.graphics.Bitmap;
import android.view.View;
import android.view.animation.Animation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Random;

import static com.google.common.truth.AndroidTruth.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

/**
 * @author Kevin Leigh Crain
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({View.class, Bitmap.class})
public class ViewSubjectTest {


    private static final String DUMMY_STRING = "DUMMY_STRING";

    private View view;

    private Random randomGenerator;

    @Before
    public void setup() {
        view = PowerMockito.mock(View.class);

        randomGenerator = new Random(99);
    }

    @After
    public void tearDown() {
        view = null;
        view = null;
    }

    @Test
    public void testHasAlpha() {
        float alpha = new Random(99).nextFloat();
        assertThat(whenReturn(view.getAlpha(), alpha))
                .hasAlpha(alpha);
    }

    @Test
    public void testHasAlphaFailure() {
        try {
            assertThat(whenReturn(view.getAlpha(), new Random(99).nextFloat()))
                    .hasAlpha(101);
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <view> has alpha: 101.0, actual: " + view.getAlpha());
        }
    }

    @Test
    public void testHasAnimation() {
        Animation animation = mock(Animation.class);
        assertThat(whenReturn(view.getAnimation(), animation))
                .hasAnimation(animation);
    }

    @Test
    public void testHasAnimationFailure() {
        Animation animation = mock(Animation.class);
        Animation expect = mock(Animation.class);
        try {
            assertThat(whenReturn(view.getAnimation(), animation))
                    .hasAnimation(expect);
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <Mock for Animation, hashCode: "
                    + animation.hashCode() + "> is the same instance as <Mock for Animation, hashCode: "
                    + expect.hashCode() + ">");
        }
    }

    @Test
    public void testIsVisible() {
        assertThat(whenReturn(view.getVisibility(), View.VISIBLE))
                .isVisible();
    }

    @Test
    public void testIsNotVisible() {
        assertThat(whenReturn(view.getVisibility(), View.INVISIBLE))
                .isNotVisible();
        assertThat(whenReturn(view.getVisibility(), View.GONE))
                .isNotVisible();
    }

    @Test
    public void testIsVisibleFailure_invisible() {
        try {
            assertThat(whenReturn(view.getVisibility(), View.INVISIBLE))
                    .isVisible();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <view> is not visible");
        }
    }

    @Test
    public void testIsNotUsingDrawingCache() {
        PowerMockito.when(view.getDrawingCache())
                .thenReturn(null);
        assertThat(view).isNotUsingDrawingCache();
    }

    @Test
    public void testHasRootView() {
        android.view.View value = mock(android.view.View.class);
        assertThat(whenReturn(view.getRootView(), value))
                .hasRootView(value);
    }

    @Test
    public void testIsOpaque() {
        assertThat(whenReturn(view.isOpaque(), true))
                .isOpaque();
    }

    @Test
    public void testIsNotFocusable() {
        assertThat(whenReturn(view.isFocusable(), false))
                .isNotFocusable();
    }

    @Test
    public void testHasVerticalFadingEdgeDisabled() {
        assertThat(whenReturn(view.isVerticalFadingEdgeEnabled(), false))
                .hasVerticalFadingEdgeDisabled();
    }

    @Test
    public void testIsLongClickable() {
        assertThat(whenReturn(view.isLongClickable(), true))
                .isLongClickable();
    }

    @Test
    public void testHasSolidColor() {
        int value = randomGenerator.nextInt();
        assertThat(whenReturn(view.getSolidColor(), value))
                .hasSolidColor(value);
    }

    @Test
    public void testHasRight() {
        int value = randomGenerator.nextInt();
        assertThat(whenReturn(view.getRight(), value))
                .hasRight(value);
    }

    @Test
    public void testIsNotOpaque() {
        assertThat(whenReturn(view.isOpaque(), false))
                .isNotOpaque();
    }

    @Test
    public void testHasMeasuredWidth() {
        int value = randomGenerator.nextInt();
        assertThat(whenReturn(view.getMeasuredWidth(), value))
                .hasMeasuredWidth(value);
    }

    @Test
    public void testHasMeasuredState() {
        int value = randomGenerator.nextInt();
        assertThat(whenReturn(view.getMeasuredState(), value))
                .hasMeasuredState(value);
    }

    @Test
    public void testHasVerticalScrollbarPosition() {
        int value = randomGenerator.nextInt();
        assertThat(whenReturn(view.getVerticalScrollbarPosition(), value))
                .hasVerticalScrollbarPosition(value);
    }

    @Test
    public void testHasHorizontalScrollbarEnabled() {
        assertThat(whenReturn(view.isHorizontalScrollBarEnabled(), true))
                .hasHorizontalScrollbarEnabled();
    }

    @Test
    public void testHasNoFocus() {
        assertThat(whenReturn(view.isFocused(), false))
                .hasNoFocus();
    }

    @Test
    public void testHasVerticalScrollBarEnabled() {
        assertThat(whenReturn(view.isVerticalScrollBarEnabled(), true))
                .hasVerticalScrollBarEnabled();
    }

    @Test
    public void testHasDrawingCacheBackgroundColor() {
        int value = randomGenerator.nextInt();
        assertThat(whenReturn(view.getDrawingCacheBackgroundColor(), value))
                .hasDrawingCacheBackgroundColor(value);
    }

    @Test
    public void testIsFocusableInTouchMode() {
        assertThat(whenReturn(view.isFocusableInTouchMode(), true))
                .isFocusableInTouchMode();
    }

    @Test
    public void testIsDisabled() {
        assertThat(whenReturn(view.isEnabled(), false))
                .isDisabled();
    }

    @Test
    public void testHasBottom() {
        int value = randomGenerator.nextInt();
        assertThat(whenReturn(view.getBottom(), value))
                .hasBottom(value);
    }

    @Test
    public void testHasScrollBarFadeDuration() {
        int value = randomGenerator.nextInt();
        assertThat(whenReturn(view.getScrollBarFadeDuration(), value))
                .hasScrollBarFadeDuration(value);
    }

    @Test
    public void testIsNotClickable() {
        assertThat(whenReturn(view.isClickable(), false))
                .isNotClickable();
    }

    @Test
    public void testHasPaddingRight() {
        int value = randomGenerator.nextInt();
        assertThat(whenReturn(view.getPaddingRight(), value))
                .hasPaddingRight(value);
    }

    @Test
    public void testHasVerticalFadingEdgeLength() {
        int value = randomGenerator.nextInt();
        assertThat(whenReturn(view.getVerticalFadingEdgeLength(), value))
                .hasVerticalFadingEdgeLength(value);
    }

    @Test
    public void testIsNotDirty() {
        assertThat(whenReturn(view.isDirty(), false))
                .isNotDirty();
    }

    @Test
    public void testHasVerticalFadingEdgeEnabled() {
        assertThat(whenReturn(view.isVerticalFadingEdgeEnabled(), true))
                .hasVerticalFadingEdgeEnabled();
    }

    @Test
    public void testHasContentDescription() {
        assertThat(whenReturn(view.getContentDescription(), DUMMY_STRING))
                .hasContentDescription(DUMMY_STRING);
    }

    @Test
    public void testHasMeasuredHeightAndState() {
        int value = randomGenerator.nextInt();
        assertThat(whenReturn(view.getMeasuredHeightAndState(), value))
                .hasMeasuredHeightAndState(value);
    }

    @Test
    public void testIsEnabled() {
        assertThat(whenReturn(view.isEnabled(), true))
                .isEnabled();
    }

    @Test
    public void testHasMinimumHeight() {
        int value = randomGenerator.nextInt();
        assertThat(whenReturn(view.getMinimumHeight(), value))
                .hasMinimumHeight(value);
    }

    @Test
    public void testHasSaveEnabled() {
        assertThat(whenReturn(view.isSaveEnabled(), true))
                .hasSaveEnabled();
    }

    @Test
    public void testHasBaseline() {
        int value = randomGenerator.nextInt();
        assertThat(whenReturn(view.getBaseline(), value))
                .hasBaseline(value);
    }

    @Test
    public void testIsInTouchMode() {
        assertThat(whenReturn(view.isInTouchMode(), true))
                .isInTouchMode();
    }

    @Test
    public void testIsHovered() {
        assertThat(whenReturn(view.isHovered(), true))
                .isHovered();
    }

    @Test
    public void testHasScaleX() {
        float value = randomGenerator.nextFloat();
        assertThat(whenReturn(view.getScaleX(), value))
                .hasScaleX(value);
    }

    @Test
    public void testHasScaleY() {
        float value = randomGenerator.nextFloat();
        assertThat(whenReturn(view.getScaleY(), value))
                .hasScaleY(value);
    }

    @Test
    public void testHasLeft() {
        int value = randomGenerator.nextInt();
        assertThat(whenReturn(view.getLeft(), value))
                .hasLeft(value);
    }

    @Test
    public void testHasHeight() {
        int value = randomGenerator.nextInt();
        assertThat(whenReturn(view.getHeight(), value))
                .hasHeight(value);
    }

    @Test
    public void testIsNotShown() {
        assertThat(whenReturn(view.isShown(), false))
                .isNotShown();
    }

    @Test
    public void testHasTranslationX() {
        float value = randomGenerator.nextFloat();
        assertThat(whenReturn(view.getTranslationX(), value))
                .hasTranslationX(value);
    }

    @Test
    public void testIsGone() {
        assertThat(whenReturn(view.getVisibility(), View.GONE))
                .isGone();
    }

    @Test
    public void testHasTranslationY() {
        float value = randomGenerator.nextFloat();
        assertThat(whenReturn(view.getTranslationY(), value))
                .hasTranslationY(value);
    }

    @Test
    public void testIsScrollContainer() {
        assertThat(whenReturn(view.isScrollContainer(), true))
                .isScrollContainer();
    }

    @Test
    public void testIsDirty() {
        assertThat(whenReturn(view.isDirty(), true))
                .isDirty();
    }

    @Test
    public void testIsNotInFocusedWindow() {
        assertThat(whenReturn(view.hasWindowFocus(), false))
                .isNotInFocusedWindow();
    }

    @Test
    public void testIsSelected() {
        assertThat(whenReturn(view.isSelected(), true))
                .isSelected();
    }

    @Test
    public void testIsNotFocused() {
        assertThat(whenReturn(view.isFocused(), false))
                .isNotFocused();
    }

    @Test
    public void testIsNotHardwareAccelerated() {
        assertThat(whenReturn(view.isHardwareAccelerated(), false))
                .isNotHardwareAccelerated();
    }

    @Test
    public void testHasFocus() {
        assertThat(whenReturn(view.hasFocus(), true))
                .hasFocus();
    }

    @Test
    public void testHasVerticalScrollBarDisabled() {
        assertThat(whenReturn(view.isVerticalScrollBarEnabled(), false))
                .hasVerticalScrollBarDisabled();
    }

    @Test
    public void testHasTop() {
        int value = randomGenerator.nextInt();
        assertThat(whenReturn(view.getTop(), value))
                .hasTop(value);
    }

    @Test
    public void testHasOverScrollMode() {
        int value = randomGenerator.nextInt();
        assertThat(whenReturn(view.getOverScrollMode(), value))
                .hasOverScrollMode(value);
    }

    @Test
    public void testIsShown() {
        assertThat(whenReturn(view.isShown(), true))
                .isShown();
    }

    @Test
    public void testHasWindowVisibility() {
        int value = randomGenerator.nextInt();
        assertThat(whenReturn(view.getWindowVisibility(), value))
                .hasWindowVisibility(value);
    }

    @Test
    public void testIsUsingDrawingCache() {
        assertThat(whenReturn(view.isDrawingCacheEnabled(), true))
                .isUsingDrawingCache();
    }

    @Test
    public void testHasSaveFromParentEnabled() {
        assertThat(whenReturn(view.isSaveFromParentEnabled(), true))
                .hasSaveFromParentEnabled();
    }

    @Test
    public void testIsNotPressed() {
        assertThat(whenReturn(view.isPressed(), false))
                .isNotPressed();
    }

    @Test
    public void testHasHorizontalFadingEdgesDisabled() {
        assertThat(whenReturn(view.isHorizontalFadingEdgeEnabled(), false))
                .hasHorizontalFadingEdgesDisabled();
    }


    @Test
    public void testHasHapticFeedbackDisabled() {
        assertThat(whenReturn(view.isHapticFeedbackEnabled(), false))
                .hasHapticFeedbackDisabled();
    }

    @Test
    public void testIsInFocusedWindow() {
        assertThat(whenReturn(view.hasWindowFocus(), true))
                .isInFocusedWindow();
    }

    @Test
    public void testIsNotScrollContainer() {
        assertThat(whenReturn(view.isScrollContainer(), false))
                .isNotScrollContainer();
    }

    @Test
    public void testHasNoLayoutRequested() {
        assertThat(whenReturn(view.isLayoutRequested(), false))
                .hasNoLayoutRequested();
    }

    @Test
    public void testIsFocusable() {
        assertThat(whenReturn(view.isFocusable(), true))
                .isFocusable();
    }

    @Test
    public void testHasParent() {
        android.view.ViewParent value = mock(android.view.ViewParent.class);
        assertThat(whenReturn(view.getParent(), value))
                .hasParent(value);
    }

    @Test
    public void testHasMeasuredHeight() {
        int value = randomGenerator.nextInt();
        assertThat(whenReturn(view.getMeasuredHeight(), value))
                .hasMeasuredHeight(value);
    }

    @Test
    public void testHasSoundEffectsDisabled() {
        assertThat(whenReturn(view.isSoundEffectsEnabled(), false))
                .hasSoundEffectsDisabled();
    }

    @Test
    public void testHasVisibility() {
        int value = randomGenerator.nextInt();
        assertThat(whenReturn(view.getVisibility(), value))
                .hasVisibility(value);
    }

    @Test
    public void testHasSoundEffectsEnabled() {
        assertThat(whenReturn(view.isSoundEffectsEnabled(), true))
                .hasSoundEffectsEnabled();
    }

    @Test
    public void testHasNextFocusLeftId() {
        int value = randomGenerator.nextInt();
        assertThat(whenReturn(view.getNextFocusLeftId(), value))
                .hasNextFocusLeftId(value);
    }

    @Test
    public void testHasHorizontalFadingEdgeLength() {
        int value = randomGenerator.nextInt();
        assertThat(whenReturn(view.getHorizontalFadingEdgeLength(), value))
                .hasHorizontalFadingEdgeLength(value);
    }

    @Test
    public void testIsNotActivated() {
        assertThat(whenReturn(view.isActivated(), false))
                .isNotActivated();
    }

    @Test
    public void testHasLayoutRequested() {
        assertThat(whenReturn(view.isLayoutRequested(), true))
                .hasLayoutRequested();
    }

    @Test
    public void testHasHapticFeedbackEnabled() {
        assertThat(whenReturn(view.isHapticFeedbackEnabled(), true))
                .hasHapticFeedbackEnabled();
    }


    @Test
    public void testHasNextFocusDownId() {
        int value = randomGenerator.nextInt();
        assertThat(whenReturn(view.getNextFocusDownId(), value))
                .hasNextFocusDownId(value);
    }

    @Test
    public void testHasSaveFromParentDisabled() {
        assertThat(whenReturn(view.isSaveFromParentEnabled(), false))
                .hasSaveFromParentDisabled();
    }

    @Test
    public void testHasPaddingBottom() {
        int value = randomGenerator.nextInt();
        assertThat(whenReturn(view.getPaddingBottom(), value))
                .hasPaddingBottom(value);
    }

    @Test
    public void testIsInEditMode() {
        assertThat(whenReturn(view.isInEditMode(), true))
                .isInEditMode();
    }

    @Test
    public void testIsNotDuplicatingParentState() {
        assertThat(whenReturn(view.isDuplicateParentStateEnabled(), false))
                .isNotDuplicatingParentState();
    }

    @Test
    public void testHasVerticalScrollbarWidth() {
        int value = randomGenerator.nextInt();
        assertThat(whenReturn(view.getVerticalScrollbarWidth(), value))
                .hasVerticalScrollbarWidth(value);
    }

    @Test
    public void testHasPivotX() {
        float value = randomGenerator.nextFloat();
        assertThat(whenReturn(view.getPivotX(), value))
                .hasPivotX(value);
    }

    @Test
    public void testHasPivotY() {
        float value = randomGenerator.nextFloat();
        assertThat(whenReturn(view.getPivotY(), value))
                .hasPivotY(value);
    }

    @Test
    public void testHasWidth() {
        int value = randomGenerator.nextInt();
        assertThat(whenReturn(view.getWidth(), value))
                .hasWidth(value);
    }

    @Test
    public void testHasScrollBarStyle() {
        int value = randomGenerator.nextInt();
        assertThat(whenReturn(view.getScrollBarStyle(), value))
                .hasScrollBarStyle(value);
    }

    @Test
    public void testHasTag() {
        assertThat(whenReturn(view.getTag(), DUMMY_STRING))
                .hasTag(DUMMY_STRING);
    }

    @Test
    public void testIsKeepingScreenOn() {
        assertThat(whenReturn(view.getKeepScreenOn(), true))
                .isKeepingScreenOn();
    }

    @Test
    public void testIsActivated() {
        assertThat(whenReturn(view.isActivated(), true))
                .isActivated();
    }

    @Test
    public void testIsFocused() {
        assertThat(whenReturn(view.isFocused(), true))
                .isFocused();
    }

    @Test
    public void testIsNotLongClickable() {
        assertThat(whenReturn(view.isLongClickable(), false))
                .isNotLongClickable();
    }

    @Test
    public void testHasHorizontalScrollbarDisabled() {
        assertThat(whenReturn(view.isHorizontalScrollBarEnabled(), false))
                .hasHorizontalScrollbarDisabled();
    }

    @Test
    public void testHasId() {
        int value = randomGenerator.nextInt();
        assertThat(whenReturn(view.getId(), value))
                .hasId(value);
    }

    @Test
    public void testIsNotKeepingScreenOn() {
        assertThat(whenReturn(view.getKeepScreenOn(), false))
                .isNotKeepingScreenOn();
    }

    @Test
    public void testHasScrollbarFadingEnabled() {
        assertThat(whenReturn(view.isScrollbarFadingEnabled(), true))
                .hasScrollbarFadingEnabled();
    }

    @Test
    public void testHasScrollBarSize() {
        int value = randomGenerator.nextInt();
        assertThat(whenReturn(view.getScrollBarSize(), value))
                .hasScrollBarSize(value);
    }

    @Test
    public void testHasLayerType() {
        int value = randomGenerator.nextInt();
        assertThat(whenReturn(view.getLayerType(), value))
                .hasLayerType(value);
    }

    @Test
    public void testHasNextFocusForwardId() {
        int value = randomGenerator.nextInt();
        assertThat(whenReturn(view.getNextFocusForwardId(), value))
                .hasNextFocusForwardId(value);
    }

    @Test
    public void testHasScrollbarFadingDisabled() {
        assertThat(whenReturn(view.isScrollbarFadingEnabled(), false))
                .hasScrollbarFadingDisabled();
    }

    @Test
    public void testHasPaddingLeft() {
        int value = randomGenerator.nextInt();
        assertThat(whenReturn(view.getPaddingLeft(), value))
                .hasPaddingLeft(value);
    }

    @Test
    public void testIsNotGone() {
        assertThat(whenReturn(view.getVisibility(), View.VISIBLE))
                .isNotGone();
    }

    @Test
    public void testHasScrollY() {
        int value = randomGenerator.nextInt();
        assertThat(whenReturn(view.getScrollY(), value))
                .hasScrollY(value);
    }

    @Test
    public void testHasPaddingTop() {
        int value = randomGenerator.nextInt();
        assertThat(whenReturn(view.getPaddingTop(), value))
                .hasPaddingTop(value);
    }

    @Test
    public void testHasScrollX() {
        int value = randomGenerator.nextInt();
        assertThat(whenReturn(view.getScrollX(), value))
                .hasScrollX(value);
    }

    @Test
    public void testHasX() {
        float value = randomGenerator.nextFloat();
        assertThat(whenReturn(view.getX(), value))
                .hasX(value);
    }

    @Test
    public void testHasY() {
        float value = randomGenerator.nextFloat();
        assertThat(whenReturn(view.getY(), value))
                .hasY(value);
    }

    @Test
    public void testHasNextFocusRightId() {
        int value = randomGenerator.nextInt();
        assertThat(whenReturn(view.getNextFocusRightId(), value))
                .hasNextFocusRightId(value);
    }

    @Test
    public void testHasMeasuredWidthAndState() {
        int value = randomGenerator.nextInt();
        assertThat(whenReturn(view.getMeasuredWidthAndState(), value))
                .hasMeasuredWidthAndState(value);
    }

    @Test
    public void testIsNotHovered() {
        assertThat(whenReturn(view.isHovered(), false))
                .isNotHovered();
    }

    @Test
    public void testHasRotation() {
        float value = randomGenerator.nextFloat();
        assertThat(whenReturn(view.getRotation(), value))
                .hasRotation(value);
    }

    @Test
    public void testHasBackground() {
        android.graphics.drawable.Drawable value = mock(android.graphics.drawable.Drawable.class);
        assertThat(whenReturn(view.getBackground(), value))
                .hasBackground(value);
    }

    @Test
    public void testIsNotInEditMode() {
        assertThat(whenReturn(view.isInEditMode(), false))
                .isNotInEditMode();
    }

    @Test
    public void testIsNotFocusableInTouchMode() {
        assertThat(whenReturn(view.isFocusableInTouchMode(), false))
                .isNotFocusableInTouchMode();
    }

    @Test
    public void testIsPressed() {
        assertThat(whenReturn(view.isPressed(), true))
                .isPressed();
    }

    @Test
    public void testHasFocusable() {
        assertThat(whenReturn(view.hasFocusable(), true))
                .hasFocusable();
    }

    @Test
    public void testHasScrollBarDefaultDelayBeforeFade() {
        int value = randomGenerator.nextInt();
        assertThat(whenReturn(view.getScrollBarDefaultDelayBeforeFade(), value))
                .hasScrollBarDefaultDelayBeforeFade(value);
    }

    @Test
    public void testIsDuplicatingParentState() {
        assertThat(whenReturn(view.isDuplicateParentStateEnabled(), true))
                .isDuplicatingParentState();
    }

    @Test
    public void testHasNextFocusUpId() {
        int value = randomGenerator.nextInt();
        assertThat(whenReturn(view.getNextFocusUpId(), value))
                .hasNextFocusUpId(value);
    }

    @Test
    public void testHasRotationY() {
        float value = randomGenerator.nextFloat();
        assertThat(whenReturn(view.getRotationY(), value))
                .hasRotationY(value);
    }

    @Test
    public void testHasParentForAccessibility() {
        android.view.ViewParent value = mock(android.view.ViewParent.class);
        assertThat(whenReturn(view.getParentForAccessibility(), value))
                .hasParentForAccessibility(value);
    }

    @Test
    public void testHasSystemUiVisibility() {
        int value = randomGenerator.nextInt();
        assertThat(whenReturn(view.getSystemUiVisibility(), value))
                .hasSystemUiVisibility(value);
    }

    @Test
    public void testHasRotationX() {
        float value = randomGenerator.nextFloat();
        assertThat(whenReturn(view.getRotationX(), value))
                .hasRotationX(value);
    }

    @Test
    public void testHasMinimumWidth() {
        int value = randomGenerator.nextInt();
        assertThat(whenReturn(view.getMinimumWidth(), value))
                .hasMinimumWidth(value);
    }

    @Test
    public void testIsNotSelected() {
        assertThat(whenReturn(view.isSelected(), false))
                .isNotSelected();
    }

    @Test
    public void testHasSaveDisabled() {
        assertThat(whenReturn(view.isSaveEnabled(), false))
                .hasSaveDisabled();
    }

    @Test
    public void testHasHorizontalFadingEdgesEnabled() {
        assertThat(whenReturn(view.isHorizontalFadingEdgeEnabled(), true))
                .hasHorizontalFadingEdgesEnabled();
    }

    @Test
    public void testIsHardwareAccelerated() {
        assertThat(whenReturn(view.isHardwareAccelerated(), true))
                .isHardwareAccelerated();
    }

    @Test
    public void testIsClickable() {
        assertThat(whenReturn(view.isClickable(), true))
                .isClickable();
    }

    @Test
    public void testHasDrawingCacheQuality() {
        int value = randomGenerator.nextInt();
        assertThat(whenReturn(view.getDrawingCacheQuality(), value))
                .hasDrawingCacheQuality(value);
    }

    @Test
    public void testIsNotInTouchMode() {
        assertThat(whenReturn(view.isInTouchMode(), false))
                .isNotInTouchMode();
    }

    private <T> View whenReturn(T methodCall, T value) {
        return when(methodCall)
                .thenReturn(value)
                .getMock();
    }
}