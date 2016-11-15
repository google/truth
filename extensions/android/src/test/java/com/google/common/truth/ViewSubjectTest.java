package com.google.common.truth;

import android.graphics.Bitmap;
import android.view.View;
import android.view.animation.Animation;
import com.google.common.collect.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Random;

import static com.google.common.truth.AndroidTruth.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Kevin Leigh Crain
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({View.class, Bitmap.class})
public class ViewSubjectTest {


    private static final String DUMMY_STRING = "DUMMY_STRING";
    private static final int DUMMY_INT = 99;
    private static final float DUMMY_FLOAT = 9.9f;

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
        randomGenerator = null;
    }

    @Test
    public void testHasAlpha() {
        float alpha = new Random(99).nextFloat();
        assertThat(whenReturn(view.getAlpha(), alpha))
                .hasAlpha(alpha);
    }

    @Test
    public void testHasAnimation() {
        Animation animation = mock(Animation.class);
        assertThat(whenReturn(view.getAnimation(), animation))
                .hasAnimation(animation);
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
    public void testHasYFailure() {
        try {
            assertThat(whenReturn(view.getY(), DUMMY_FLOAT))
                    .hasY(0);
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <9.9> is equal to <0.0>");
        }
    }

    @Test
    public void testHasXFailure() {
        try {
            assertThat(whenReturn(view.getX(), DUMMY_FLOAT))
                    .hasX(0);
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <9.9> is equal to <0.0>");
        }
    }

    @Test
    public void testIsNotVisibleFailure() {
        try {
            assertThat(whenReturn(view.getVisibility(), View.VISIBLE))
                    .isNotVisible();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <view> is visible");
        }
    }

    @Test
    public void testIsNotUsingDrawingCacheFailure() {
        try {
            PowerMockito.when(view.isDrawingCacheEnabled())
                    .thenReturn(true);
            assertThat(view).isNotUsingDrawingCache();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("The subject was expected to be false, but was true");
        }
    }

    @Test
    public void testHasRootViewFailure() {
        android.view.View value = mock(android.view.View.class);
        try {
            assertThat(whenReturn(view.getRootView(), null))
                    .hasRootView(value);
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage(String.format("Not true that <null> is the same instance as <Mock for View, hashCode: %d>", value.hashCode()));
        }
    }

    @Test
    public void testIsOpaqueFailure() {
        try {
            assertThat(whenReturn(view.isOpaque(), false))
                    .isOpaque();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("The subject was expected to be true, but was false");
        }
    }

    @Test
    public void testIsNotFocusableFailure() {
        try {
            assertThat(whenReturn(view.isFocusable(), true))
                    .isNotFocusable();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("The subject was expected to be false, but was true");
        }
    }

    @Test
    public void testHasVerticalFadingEdgeDisabledFailure() {
        try {
            assertThat(whenReturn(view.isVerticalFadingEdgeEnabled(), true))
                    .hasVerticalFadingEdgeDisabled();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("The subject was expected to be false, but was true");
        }
    }

    @Test
    public void testIsLongClickableFailure() {
        try {
            assertThat(whenReturn(view.isLongClickable(), false))
                    .isLongClickable();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("The subject was expected to be true, but was false");
        }
    }

    @Test
    public void testHasSolidColorFailure() {
        try {
            assertThat(whenReturn(view.getSolidColor(), DUMMY_INT))
                    .hasSolidColor(0);
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <99> is equal to <0>");
        }
    }

    @Test
    public void testHasRightFailure() {
        try {
            assertThat(whenReturn(view.getRight(), DUMMY_INT))
                    .hasRight(0);
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <99> is equal to <0>");
        }
    }

    @Test
    public void testIsNotOpaqueFailure() {
        try {
            assertThat(whenReturn(view.isOpaque(), true))
                    .isNotOpaque();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("The subject was expected to be false, but was true");
        }
    }

    @Test
    public void testHasMeasuredWidthFailure() {
        try {
            assertThat(whenReturn(view.getMeasuredWidth(), DUMMY_INT))
                    .hasMeasuredWidth(0);
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <99> is equal to <0>");
        }
    }

    @Test
    public void testHasMeasuredStateFailure() {
        try {
            assertThat(whenReturn(view.getMeasuredState(), DUMMY_INT))
                    .hasMeasuredState(0);
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <99> is equal to <0>");
        }
    }

    @Test
    public void testHasVerticalScrollbarPositionFailure() {
        try {
            assertThat(whenReturn(view.getVerticalScrollbarPosition(), DUMMY_INT))
                    .hasVerticalScrollbarPosition(0);
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <99> is equal to <0>");
        }
    }

    @Test
    public void testHasHorizontalScrollbarEnabledFailure() {
        try {
            assertThat(whenReturn(view.isHorizontalScrollBarEnabled(), false))
                    .hasHorizontalScrollbarEnabled();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("The subject was expected to be true, but was false");
        }
    }

    @Test
    public void testHasNoFocusFailure() {
        try {
            assertThat(whenReturn(view.hasFocus(), true))
                    .hasNoFocus();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("The subject was expected to be false, but was true");
        }
    }

    @Test
    public void testHasVerticalScrollBarEnabledFailure() {
        try {
            assertThat(whenReturn(view.isVerticalScrollBarEnabled(), false))
                    .hasVerticalScrollBarEnabled();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("The subject was expected to be true, but was false");
        }
    }

    @Test
    public void testHasDrawingCacheBackgroundColorFailure() {
        try {
            assertThat(whenReturn(view.getDrawingCacheBackgroundColor(), DUMMY_INT))
                    .hasDrawingCacheBackgroundColor(0);
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <99> is equal to <0>");
        }
    }

    @Test
    public void testIsFocusableInTouchModeFailure() {
        try {
            assertThat(whenReturn(view.isFocusableInTouchMode(), false))
                    .isFocusableInTouchMode();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("The subject was expected to be true, but was false");
        }
    }

    @Test
    public void testIsDisabledFailure() {
        try {
            assertThat(whenReturn(view.isEnabled(), true))
                    .isDisabled();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("The subject was expected to be false, but was true");
        }
    }

    @Test
    public void testHasBottomFailure() {
        try {
            assertThat(whenReturn(view.getBottom(), DUMMY_INT))
                    .hasBottom(0);
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <99> is equal to <0>");
        }
    }

    @Test
    public void testHasScrollBarFadeDurationFailure() {
        try {
            assertThat(whenReturn(view.getScrollBarFadeDuration(), DUMMY_INT))
                    .hasScrollBarFadeDuration(0);
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <99> is equal to <0>");
        }
    }

    @Test
    public void testIsNotClickableFailure() {
        try {
            assertThat(whenReturn(view.isClickable(), true))
                    .isNotClickable();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("The subject was expected to be false, but was true");
        }
    }

    @Test
    public void testHasPaddingRightFailure() {
        try {
            assertThat(whenReturn(view.getPaddingRight(), DUMMY_INT))
                    .hasPaddingRight(0);
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <99> is equal to <0>");
        }
    }

    @Test
    public void testHasVerticalFadingEdgeLengthFailure() {
        try {
            assertThat(whenReturn(view.getVerticalFadingEdgeLength(), DUMMY_INT))
                    .hasVerticalFadingEdgeLength(0);
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <99> is equal to <0>");
        }
    }

    @Test
    public void testIsNotDirtyFailure() {
        try {
            assertThat(whenReturn(view.isDirty(), true))
                    .isNotDirty();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("The subject was expected to be false, but was true");
        }
    }

    @Test
    public void testHasVerticalFadingEdgeEnabledFailure() {
        try {
            assertThat(whenReturn(view.isVerticalFadingEdgeEnabled(), false))
                    .hasVerticalFadingEdgeEnabled();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("The subject was expected to be true, but was false");
        }
    }

    @Test
    public void testHasContentDescriptionFailure() {
        try {
            assertThat(whenReturn(view.getContentDescription(), null))
                    .hasContentDescription(DUMMY_STRING);
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <null> is equal to <DUMMY_STRING>");
        }
    }

    @Test
    public void testHasMeasuredHeightAndStateFailure() {
        try {
            assertThat(whenReturn(view.getMeasuredHeightAndState(), DUMMY_INT))
                    .hasMeasuredHeightAndState(0);
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <99> is equal to <0>");
        }
    }

    @Test
    public void testIsEnabledFailure() {
        try {
            assertThat(whenReturn(view.isEnabled(), false))
                    .isEnabled();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("The subject was expected to be true, but was false");
        }
    }

    @Test
    public void testHasMinimumHeightFailure() {
        try {
            assertThat(whenReturn(view.getMinimumHeight(), DUMMY_INT))
                    .hasMinimumHeight(0);
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <99> is equal to <0>");
        }
    }

    @Test
    public void testHasSaveEnabledFailure() {
        try {
            assertThat(whenReturn(view.isSaveEnabled(), false))
                    .hasSaveEnabled();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("The subject was expected to be true, but was false");
        }
    }

    @Test
    public void testHasBaselineFailure() {
        try {
            assertThat(whenReturn(view.getBaseline(), DUMMY_INT))
                    .hasBaseline(0);
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <99> is equal to <0>");
        }
    }

    @Test
    public void testIsInTouchModeFailure() {
        try {
            assertThat(whenReturn(view.isInTouchMode(), false))
                    .isInTouchMode();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("The subject was expected to be true, but was false");
        }
    }

    @Test
    public void testIsHoveredFailure() {
        try {
            assertThat(whenReturn(view.isHovered(), false))
                    .isHovered();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("The subject was expected to be true, but was false");
        }
    }

    @Test
    public void testHasScaleXFailure() {
        try {
            assertThat(whenReturn(view.getScaleX(), DUMMY_FLOAT))
                    .hasScaleX(0);
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <9.9> is equal to <0.0>");
        }
    }

    @Test
    public void testHasScaleYFailure() {
        try {
            assertThat(whenReturn(view.getScaleY(), DUMMY_FLOAT))
                    .hasScaleY(0);
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <9.9> is equal to <0.0>");
        }
    }

    @Test
    public void testHasLeftFailure() {
        try {
            assertThat(whenReturn(view.getLeft(), DUMMY_INT))
                    .hasLeft(0);
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <99> is equal to <0>");
        }
    }

    @Test
    public void testHasHeightFailure() {
        try {
            assertThat(whenReturn(view.getHeight(), DUMMY_INT))
                    .hasHeight(0);
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <99> is equal to <0>");
        }
    }

    @Test
    public void testIsNotShownFailure() {
        try {
            assertThat(whenReturn(view.isShown(), true))
                    .isNotShown();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            // XXX: Todo
            Truth.assertThat(e).hasMessage("The subject was expected to be false, but was true");
        }
    }

    @Test
    public void testHasTranslationXFailure() {
        try {
            assertThat(whenReturn(view.getTranslationX(), DUMMY_FLOAT))
                    .hasTranslationX(0);
            fail("Should have thrown.");
        } catch (AssertionError e) {
            // XXX: Todo
            Truth.assertThat(e).hasMessage("Not true that <9.9> is equal to <0.0>");
        }
    }

    @Test
    public void testIsGoneFailure() {
        try {
            assertThat(whenReturn(view.getVisibility(), View.VISIBLE))
                    .isGone();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <0> is equal to <8>");
        }

        try {
            assertThat(whenReturn(view.getVisibility(), View.INVISIBLE))
                    .isGone();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <4> is equal to <8>");
        }
    }

    @Test
    public void testHasTranslationYFailure() {
        try {
            assertThat(whenReturn(view.getTranslationY(), DUMMY_FLOAT))
                    .hasTranslationY(0);
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <9.9> is equal to <0.0>");
        }
    }

    @Test
    public void testIsScrollContainerFailure() {
        try {
            assertThat(whenReturn(view.isScrollContainer(), false))
                    .isScrollContainer();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("The subject was expected to be true, but was false");
        }
    }

    @Test
    public void testIsDirtyFailure() {
        try {
            assertThat(whenReturn(view.isDirty(), false))
                    .isDirty();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("The subject was expected to be true, but was false");
        }
    }

    @Test
    public void testIsNotInFocusedWindowFailure() {
        try {
            assertThat(whenReturn(view.hasWindowFocus(), true))
                    .isNotInFocusedWindow();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("The subject was expected to be false, but was true");
        }
    }

    @Test
    public void testIsSelectedFailure() {
        try {
            assertThat(whenReturn(view.isSelected(), false))
                    .isSelected();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("The subject was expected to be true, but was false");
        }
    }

    @Test
    public void testIsNotFocusedFailure() {
        try {
            assertThat(whenReturn(view.isFocused(), true))
                    .isNotFocused();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("The subject was expected to be false, but was true");
        }
    }

    @Test
    public void testIsNotHardwareAcceleratedFailure() {
        try {
            assertThat(whenReturn(view.isHardwareAccelerated(), true))
                    .isNotHardwareAccelerated();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("The subject was expected to be false, but was true");
        }
    }

    @Test
    public void testHasFocusFailure() {
        try {
            assertThat(whenReturn(view.hasFocus(), false))
                    .hasFocus();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("The subject was expected to be true, but was false");
        }
    }

    @Test
    public void testHasVerticalScrollBarDisabledFailure() {
        try {
            assertThat(whenReturn(view.isVerticalScrollBarEnabled(), true))
                    .hasVerticalScrollBarDisabled();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("The subject was expected to be false, but was true");
        }
    }

    @Test
    public void testHasTopFailure() {
        try {
            assertThat(whenReturn(view.getTop(), DUMMY_INT))
                    .hasTop(0);
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <99> is equal to <0>");
        }
    }

    @Test
    public void testHasOverScrollModeFailure() {
        try {
            assertThat(whenReturn(view.getOverScrollMode(), DUMMY_INT))
                    .hasOverScrollMode(0);
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <99> is equal to <0>");
        }
    }

    @Test
    public void testIsShownFailure() {
        try {
            assertThat(whenReturn(view.isShown(), false))
                    .isShown();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("The subject was expected to be true, but was false");
        }
    }

    @Test
    public void testHasWindowVisibilityFailure() {
        try {
            assertThat(whenReturn(view.getWindowVisibility(), DUMMY_INT))
                    .hasWindowVisibility(0);
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <99> is equal to <0>");
        }
    }

    @Test
    public void testIsUsingDrawingCacheFailure() {
        try {
            assertThat(whenReturn(view.isDrawingCacheEnabled(), false))
                    .isUsingDrawingCache();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("The subject was expected to be true, but was false");
        }
    }

    @Test
    public void testHasSaveFromParentEnabledFailure() {
        try {
            assertThat(whenReturn(view.isSaveFromParentEnabled(), false))
                    .hasSaveFromParentEnabled();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("The subject was expected to be true, but was false");
        }
    }

    @Test
    public void testIsNotPressedFailure() {
        try {
            assertThat(whenReturn(view.isPressed(), true))
                    .isNotPressed();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("The subject was expected to be false, but was true");
        }
    }

    @Test
    public void testHasHorizontalFadingEdgesDisabledFailure() {
        try {
            assertThat(whenReturn(view.isHorizontalFadingEdgeEnabled(), true))
                    .hasHorizontalFadingEdgesDisabled();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("The subject was expected to be false, but was true");
        }
    }

    @Test
    public void testHasHapticFeedbackDisabledFailure() {
        try {
            assertThat(whenReturn(view.isHapticFeedbackEnabled(), true))
                    .hasHapticFeedbackDisabled();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("The subject was expected to be false, but was true");
        }
    }

    @Test
    public void testIsInFocusedWindowFailure() {
        try {
            assertThat(whenReturn(view.hasWindowFocus(), false))
                    .isInFocusedWindow();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("The subject was expected to be true, but was false");
        }
    }

    @Test
    public void testIsNotScrollContainerFailure() {
        try {
            assertThat(whenReturn(view.isScrollContainer(), true))
                    .isNotScrollContainer();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("The subject was expected to be false, but was true");
        }
    }

    @Test
    public void testHasNoLayoutRequestedFailure() {
        try {
            assertThat(whenReturn(view.isLayoutRequested(), true))
                    .hasNoLayoutRequested();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("The subject was expected to be false, but was true");
        }
    }

    @Test
    public void testIsFocusableFailure() {
        try {
            assertThat(whenReturn(view.isFocusable(), false))
                    .isFocusable();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("The subject was expected to be true, but was false");
        }
    }

    @Test
    public void testHasParentFailure() {
        android.view.ViewParent value = mock(android.view.ViewParent.class);
        try {
            assertThat(whenReturn(view.getParent(), null))
                    .hasParent(value);
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage(String.format("Not true that <null> is the same instance as <Mock for ViewParent, hashCode: %d>", value.hashCode()));
        }
    }

    @Test
    public void testHasMeasuredHeightFailure() {
        try {
            assertThat(whenReturn(view.getMeasuredHeight(), DUMMY_INT))
                    .hasMeasuredHeight(0);
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <99> is equal to <0>");
        }
    }

    @Test
    public void testHasSoundEffectsDisabledFailure() {
        try {
            assertThat(whenReturn(view.isSoundEffectsEnabled(), true))
                    .hasSoundEffectsDisabled();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            // XXX: Todo
            Truth.assertThat(e).hasMessage("The subject was expected to be false, but was true");
        }
    }

    @Test
    public void testHasVisibilityFailure() {
        try {
            assertThat(whenReturn(view.getVisibility(), DUMMY_INT))
                    .hasVisibility(0);
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <99> is equal to <0>");
        }
    }

    @Test
    public void testHasSoundEffectsEnabledFailure() {
        try {
            assertThat(whenReturn(view.isSoundEffectsEnabled(), false))
                    .hasSoundEffectsEnabled();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("The subject was expected to be true, but was false");
        }
    }

    @Test
    public void testHasNextFocusLeftIdFailure() {
        try {
            assertThat(whenReturn(view.getNextFocusLeftId(), DUMMY_INT))
                    .hasNextFocusLeftId(0);
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <99> is equal to <0>");
        }
    }

    @Test
    public void testHasHorizontalFadingEdgeLengthFailure() {
        try {
        assertThat(whenReturn(view.getHorizontalFadingEdgeLength(), DUMMY_INT))
                    .hasHorizontalFadingEdgeLength(0);
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <99> is equal to <0>");
        }
    }

    @Test
    public void testIsNotActivatedFailure() {
        try {
            assertThat(whenReturn(view.isActivated(), true))
                    .isNotActivated();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("The subject was expected to be false, but was true");
        }
    }

    @Test
    public void testHasLayoutRequestedFailure() {
        try {
            assertThat(whenReturn(view.isLayoutRequested(), false))
                    .hasLayoutRequested();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("The subject was expected to be true, but was false");
        }
    }

    @Test
    public void testHasHapticFeedbackEnabledFailure() {
        try {
            assertThat(whenReturn(view.isHapticFeedbackEnabled(), false))
                    .hasHapticFeedbackEnabled();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("The subject was expected to be true, but was false");
        }
    }

    @Test
    public void testHasNextFocusDownIdFailure() {
        try {
            assertThat(whenReturn(view.getNextFocusDownId(), DUMMY_INT))
                    .hasNextFocusDownId(0);
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <99> is equal to <0>");
        }
    }

    @Test
    public void testHasSaveFromParentDisabledFailure() {
        try {
            assertThat(whenReturn(view.isSaveFromParentEnabled(), true))
                    .hasSaveFromParentDisabled();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("The subject was expected to be false, but was true");
        }
    }

    @Test
    public void testHasPaddingBottomFailure() {
        try {
            assertThat(whenReturn(view.getPaddingBottom(), DUMMY_INT))
                    .hasPaddingBottom(0);
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <99> is equal to <0>");
        }
    }

    @Test
    public void testIsInEditModeFailure() {
        try {
            assertThat(whenReturn(view.isInEditMode(), false))
                    .isInEditMode();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("The subject was expected to be true, but was false");
        }
    }

    @Test
    public void testIsNotDuplicatingParentStateFailure() {
        try {
            assertThat(whenReturn(view.isDuplicateParentStateEnabled(), true))
                    .isNotDuplicatingParentState();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("The subject was expected to be false, but was true");
        }
    }

    @Test
    public void testHasVerticalScrollbarWidthFailure() {
        try {
            assertThat(whenReturn(view.getVerticalScrollbarWidth(), DUMMY_INT))
                    .hasVerticalScrollbarWidth(0);
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <99> is equal to <0>");
        }
    }

    @Test
    public void testHasPivotXFailure() {
        try {
            assertThat(whenReturn(view.getPivotX(), DUMMY_FLOAT))
                    .hasPivotX(0);
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <9.9> is equal to <0.0>");
        }
    }

    @Test
    public void testHasPivotYFailure() {
        try {
            assertThat(whenReturn(view.getPivotY(), DUMMY_FLOAT))
                    .hasPivotY(0);
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <9.9> is equal to <0.0>");
        }
    }

    @Test
    public void testHasWidthFailure() {
        try {
            assertThat(whenReturn(view.getWidth(), DUMMY_INT))
                    .hasWidth(0);
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <99> is equal to <0>");
        }
    }

    @Test
    public void testHasScrollBarStyleFailure() {
        try {
            assertThat(whenReturn(view.getScrollBarStyle(), DUMMY_INT))
                    .hasScrollBarStyle(0);
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <99> is equal to <0>");
        }
    }

    @Test
    public void testHasTagFailure() {
        try {
            assertThat(whenReturn(view.getTag(), null))
                    .hasTag(DUMMY_STRING);
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <null> is the same instance as <DUMMY_STRING>");
        }
    }

    @Test
    public void testIsKeepingScreenOnFailure() {
        try {
            assertThat(whenReturn(view.getKeepScreenOn(), false))
                    .isKeepingScreenOn();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("The subject was expected to be true, but was false");
        }
    }

    @Test
    public void testIsActivatedFailure() {
        try {
            assertThat(whenReturn(view.isActivated(), false))
                    .isActivated();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("The subject was expected to be true, but was false");
        }
    }

    @Test
    public void testIsFocusedFailure() {
        try {
            assertThat(whenReturn(view.isFocused(), false))
                    .isFocused();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("The subject was expected to be true, but was false");
        }
    }

    @Test
    public void testIsNotLongClickableFailure() {
        try {
            assertThat(whenReturn(view.isLongClickable(), true))
                    .isNotLongClickable();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("The subject was expected to be false, but was true");
        }
    }

    @Test
    public void testHasHorizontalScrollbarDisabledFailure() {
        try {
            assertThat(whenReturn(view.isHorizontalScrollBarEnabled(), true))
                    .hasHorizontalScrollbarDisabled();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("The subject was expected to be false, but was true");
        }
    }

    @Test
    public void testHasIdFailure() {
        try {
            assertThat(whenReturn(view.getId(), DUMMY_INT))
                    .hasId(0);
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <99> is equal to <0>");
        }
    }

    @Test
    public void testIsNotKeepingScreenOnFailure() {
        try {
            assertThat(whenReturn(view.getKeepScreenOn(), true))
                    .isNotKeepingScreenOn();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("The subject was expected to be false, but was true");
        }
    }

    @Test
    public void testHasScrollbarFadingEnabledFailure() {
        try {
            assertThat(whenReturn(view.isScrollbarFadingEnabled(), false))
                    .hasScrollbarFadingEnabled();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("The subject was expected to be true, but was false");
        }
    }

    @Test
    public void testHasScrollBarSizeFailure() {
        try {
            assertThat(whenReturn(view.getScrollBarSize(), DUMMY_INT))
                    .hasScrollBarSize(0);
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <99> is equal to <0>");
        }
    }

    @Test
    public void testHasLayerTypeFailure() {
        try {
            assertThat(whenReturn(view.getLayerType(), DUMMY_INT))
                    .hasLayerType(0);
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <99> is equal to <0>");
        }
    }

    @Test
    public void testHasNextFocusForwardIdFailure() {
        try {
            assertThat(whenReturn(view.getNextFocusForwardId(), DUMMY_INT))
                    .hasNextFocusForwardId(0);
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <99> is equal to <0>");
        }
    }

    @Test
    public void testHasScrollbarFadingDisabledFailure() {
        try {
            assertThat(whenReturn(view.isScrollbarFadingEnabled(), true))
                    .hasScrollbarFadingDisabled();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("The subject was expected to be false, but was true");
        }
    }

    @Test
    public void testHasPaddingLeftFailure() {
        try {
            assertThat(whenReturn(view.getPaddingLeft(), DUMMY_INT))
                    .hasPaddingLeft(0);
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <99> is equal to <0>");
        }
    }

    @Test
    public void testIsNotGoneFailure() {
        try {
            assertThat(whenReturn(view.getVisibility(), View.GONE))
                    .isNotGone();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <8> is not equal to <8>");
        }
    }

    @Test
    public void testHasScrollYFailure() {
        try {
            assertThat(whenReturn(view.getScrollY(), DUMMY_INT))
                    .hasScrollY(0);
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <99> is equal to <0>");
        }
    }

    @Test
    public void testHasPaddingTopFailure() {
        try {
            assertThat(whenReturn(view.getPaddingTop(), DUMMY_INT))
                    .hasPaddingTop(0);
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <99> is equal to <0>");
        }
    }

    @Test
    public void testHasScrollXFailure() {
        try {
            assertThat(whenReturn(view.getScrollX(), DUMMY_INT))
                    .hasScrollX(0);
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <99> is equal to <0>");
        }
    }

    @Test
    public void testHasNextFocusRightIdFailure() {
        try {
            assertThat(whenReturn(view.getNextFocusRightId(), DUMMY_INT))
                    .hasNextFocusRightId(0);
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <99> is equal to <0>");
        }
    }

    @Test
    public void testHasMeasuredWidthAndStateFailure() {
        try {
            assertThat(whenReturn(view.getMeasuredWidthAndState(), DUMMY_INT))
                    .hasMeasuredWidthAndState(0);
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <99> is equal to <0>");
        }
    }

    @Test
    public void testIsNotHoveredFailure() {
        try {
            assertThat(whenReturn(view.isHovered(), true))
                    .isNotHovered();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("The subject was expected to be false, but was true");
        }
    }

    @Test
    public void testHasRotationFailure() {
        try {
            assertThat(whenReturn(view.getRotation(), DUMMY_FLOAT))
                    .hasRotation(0);
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <9.9> is equal to <0.0>");
        }
    }

    @Test
    public void testHasBackgroundFailure() {
        android.graphics.drawable.Drawable value = mock(android.graphics.drawable.Drawable.class);
        try {
            assertThat(whenReturn(view.getBackground(), null))
                    .hasBackground(value);
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage(String.format("Not true that <null> is the same instance as <Mock for Drawable, hashCode: %d>", value.hashCode()));
        }
    }

    @Test
    public void testIsNotInEditModeFailure() {
        try {
            assertThat(whenReturn(view.isInEditMode(), true))
                    .isNotInEditMode();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("The subject was expected to be false, but was true");
        }
    }

    @Test
    public void testIsNotFocusableInTouchModeFailure() {
        try {
            assertThat(whenReturn(view.isFocusableInTouchMode(), true))
                    .isNotFocusableInTouchMode();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("The subject was expected to be false, but was true");
        }
    }

    @Test
    public void testIsPressedFailure() {
        try {
            assertThat(whenReturn(view.isPressed(), false))
                    .isPressed();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("The subject was expected to be true, but was false");
        }
    }

    @Test
    public void testHasFocusableFailure() {
        try {
            assertThat(whenReturn(view.hasFocusable(), false))
                    .hasFocusable();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("The subject was expected to be true, but was false");
        }
    }

    @Test
    public void testHasScrollBarDefaultDelayBeforeFadeFailure() {
        try {
            assertThat(whenReturn(view.getScrollBarDefaultDelayBeforeFade(), DUMMY_INT))
                    .hasScrollBarDefaultDelayBeforeFade(0);
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <99> is equal to <0>");
        }
    }

    @Test
    public void testIsDuplicatingParentStateFailure() {
        try {
            assertThat(whenReturn(view.isDuplicateParentStateEnabled(), false))
                    .isDuplicatingParentState();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("The subject was expected to be true, but was false");
        }
    }

    @Test
    public void testHasNextFocusUpIdFailure() {
        try {
            assertThat(whenReturn(view.getNextFocusUpId(), DUMMY_INT))
                    .hasNextFocusUpId(0);
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <99> is equal to <0>");
        }
    }

    @Test
    public void testHasRotationYFailure() {
        try {
            assertThat(whenReturn(view.getRotationY(), DUMMY_FLOAT))
                    .hasRotationY(0);
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <9.9> is equal to <0.0>");
        }
    }

    @Test
    public void testHasParentForAccessibilityFailure() {
        android.view.ViewParent value = mock(android.view.ViewParent.class);
        try {
            assertThat(whenReturn(view.getParentForAccessibility(), null))
                    .hasParentForAccessibility(value);
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage(String.format("Not true that <null> is the same instance as <Mock for ViewParent, hashCode: %d>", value.hashCode()));
        }
    }

    @Test
    public void testHasSystemUiVisibilityFailure() {
        try {
            assertThat(whenReturn(view.getSystemUiVisibility(), DUMMY_INT))
                    .hasSystemUiVisibility(0);
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <99> is equal to <0>");
        }
    }

    @Test
    public void testHasRotationXFailure() {
        try {
            assertThat(whenReturn(view.getRotationX(), DUMMY_FLOAT))
                    .hasRotationX(0);
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <9.9> is equal to <0.0>");
        }
    }

    @Test
    public void testHasMinimumWidthFailure() {
        try {
            assertThat(whenReturn(view.getMinimumWidth(), DUMMY_INT))
                    .hasMinimumWidth(0);
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <99> is equal to <0>");
        }
    }

    @Test
    public void testIsNotSelectedFailure() {
        try {
            assertThat(whenReturn(view.isSelected(), true))
                    .isNotSelected();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("The subject was expected to be false, but was true");
        }
    }

    @Test
    public void testHasSaveDisabledFailure() {
        try {
            assertThat(whenReturn(view.isSaveEnabled(), true))
                    .hasSaveDisabled();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("The subject was expected to be false, but was true");
        }
    }

    @Test
    public void testHasHorizontalFadingEdgesEnabledFailure() {
        try {
            assertThat(whenReturn(view.isHorizontalFadingEdgeEnabled(), false))
                    .hasHorizontalFadingEdgesEnabled();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("The subject was expected to be true, but was false");
        }
    }

    @Test
    public void testIsHardwareAcceleratedFailure() {
        try {
            assertThat(whenReturn(view.isHardwareAccelerated(), false))
                    .isHardwareAccelerated();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("The subject was expected to be true, but was false");
        }
    }

    @Test
    public void testIsClickableFailure() {
        try {
            assertThat(whenReturn(view.isClickable(), false))
                    .isClickable();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("The subject was expected to be true, but was false");
        }
    }

    @Test
    public void testHasDrawingCacheQualityFailure() {
        try {
            assertThat(whenReturn(view.getDrawingCacheQuality(), DUMMY_INT))
                    .hasDrawingCacheQuality(0);
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <99> is equal to <0>");
        }
    }

    @Test
    public void testIsNotInTouchModeFailure() {
        try {
            assertThat(whenReturn(view.isInTouchMode(), true))
                    .isNotInTouchMode();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("The subject was expected to be false, but was true");
        }
    }

    private <T> View whenReturn(T methodCall, T value) {
        return when(methodCall)
                .thenReturn(value)
                .getMock();
    }
}