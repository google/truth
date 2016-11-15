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

    @Mock
    View view;
    View viewSpy;

    private Random randomGenerator;

    @Before
    public void setup() {
        view = PowerMockito.mock(View.class);
        viewSpy = PowerMockito.spy(view);

        randomGenerator = new Random(99);
    }

    @After
    public void tearDown() {
        view = null;
        viewSpy = null;
    }

    @Test
    public void testHasAlpha() {
        float alpha = new Random(99).nextFloat();
        assertThat(whenReturn(viewSpy.getAlpha(), alpha))
                .hasAlpha(alpha);
    }

    @Test
    public void testHasAlphaFailure() {
        try {
            assertThat(whenReturn(viewSpy.getAlpha(), new Random(99).nextFloat()))
                    .hasAlpha(101);
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <view> has alpha: 101.0, actual: " + viewSpy.getAlpha());
        }
    }

    @Test
    public void testHasAnimation() {
        Animation animation = mock(Animation.class);
        assertThat(whenReturn(viewSpy.getAnimation(), animation))
                .hasAnimation(animation);
    }

    @Test
    public void testHasAnimationFailure() {
        Animation animation = mock(Animation.class);
        Animation expect = mock(Animation.class);
        try {
            assertThat(whenReturn(viewSpy.getAnimation(), animation))
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
        assertThat(whenReturn(viewSpy.getVisibility(), View.VISIBLE))
                .isVisible();
    }

    @Test
    public void testIsNotVisible() {
        assertThat(whenReturn(viewSpy.getVisibility(), View.INVISIBLE))
                .isNotVisible();
        assertThat(whenReturn(viewSpy.getVisibility(), View.GONE))
                .isNotVisible();
    }

    @Test
    public void testIsVisibleFailure_invisible() {
        try {
            assertThat(whenReturn(viewSpy.getVisibility(), View.INVISIBLE))
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
        assertThat(whenReturn(viewSpy.getRootView(), value))
                .hasRootView(value);
    }

    @Test
    public void testIsOpaque() {
        assertThat(whenReturn(viewSpy.isOpaque(), true))
                .isOpaque();
    }

    @Test
    public void testIsNotFocusable() {
        assertThat(whenReturn(view.isFocusable(), false))
                .isNotFocusable();
    }

    @Test
    public void testHasVerticalFadingEdgeDisabled() {
        assertThat(whenReturn(viewSpy.isVerticalFadingEdgeEnabled(), false))
                .hasVerticalFadingEdgeDisabled();
    }

    @Test
    public void testIsLongClickable() {
        assertThat(whenReturn(viewSpy.isLongClickable(), true))
                .isLongClickable();
    }

    @Test
    public void testHasSolidColor() {
        int value = randomGenerator.nextInt();
        assertThat(whenReturn(viewSpy.getSolidColor(), value))
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
        assertThat(whenReturn(viewSpy.isOpaque(), false))
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
        assertThat(whenReturn(viewSpy.getVerticalScrollbarPosition(), value))
                .hasVerticalScrollbarPosition(value);
    }

    @Test
    public void testHasHorizontalScrollbarEnabled() {
        assertThat(whenReturn(viewSpy.isHorizontalScrollBarEnabled(), true))
                .hasHorizontalScrollbarEnabled();
    }

    @Test
    public void testHasNoFocus() {
        assertThat(whenReturn(viewSpy.isFocused(), false))
                .hasNoFocus();
    }

    @Test
    public void testHasVerticalScrollBarEnabled() {
        assertThat(whenReturn(viewSpy.isVerticalScrollBarEnabled(), true))
                .hasVerticalScrollBarEnabled();
    }

    @Test
    public void testHasDrawingCacheBackgroundColor() {
        int value = randomGenerator.nextInt();
        assertThat(whenReturn(viewSpy.getDrawingCacheBackgroundColor(), value))
                .hasDrawingCacheBackgroundColor(value);
    }

    @Test
    public void testIsFocusableInTouchMode() {
        assertThat(whenReturn(view.isFocusableInTouchMode(), true))
                .isFocusableInTouchMode();
    }

    @Test
    public void testIsDisabled() {
        assertThat(whenReturn(viewSpy.isEnabled(), false))
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
        assertThat(whenReturn(viewSpy.getScrollBarFadeDuration(), value))
                .hasScrollBarFadeDuration(value);
    }

    @Test
    public void testIsNotClickable() {
        assertThat(whenReturn(viewSpy.isClickable(), false))
                .isNotClickable();
    }

    @Test
    public void testHasPaddingRight() {
        int value = randomGenerator.nextInt();
        assertThat(whenReturn(viewSpy.getPaddingRight(), value))
                .hasPaddingRight(value);
    }

    @Test
    public void testHasVerticalFadingEdgeLength() {
        int value = randomGenerator.nextInt();
        assertThat(whenReturn(viewSpy.getVerticalFadingEdgeLength(), value))
                .hasVerticalFadingEdgeLength(value);
    }

    @Test
    public void testIsNotDirty() {
        assertThat(whenReturn(viewSpy.isDirty(), false))
                .isNotDirty();
    }

    @Test
    public void testHasVerticalFadingEdgeEnabled() {
        assertThat(whenReturn(viewSpy.isVerticalFadingEdgeEnabled(), true))
                .hasVerticalFadingEdgeEnabled();
    }

    @Test
    public void testHasContentDescription() {
        assertThat(whenReturn(viewSpy.getContentDescription(), DUMMY_STRING))
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
        assertThat(whenReturn(viewSpy.isEnabled(), true))
                .isEnabled();
    }

    @Test
    public void testHasMinimumHeight() {
        int value = randomGenerator.nextInt();
        assertThat(whenReturn(viewSpy.getMinimumHeight(), value))
                .hasMinimumHeight(value);
    }

    @Test
    public void testHasSaveEnabled() {
        assertThat(whenReturn(viewSpy.isSaveEnabled(), true))
                .hasSaveEnabled();
    }

    @Test
    public void testHasBaseline() {
        int value = randomGenerator.nextInt();
        assertThat(whenReturn(viewSpy.getBaseline(), value))
                .hasBaseline(value);
    }

    @Test
    public void testIsInTouchMode() {
        assertThat(whenReturn(viewSpy.isInTouchMode(), true))
                .isInTouchMode();
    }

    @Test
    public void testIsHovered() {
        assertThat(whenReturn(viewSpy.isHovered(), true))
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
        assertThat(whenReturn(viewSpy.getScaleY(), value))
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
        assertThat(whenReturn(viewSpy.isShown(), false))
                .isNotShown();
    }

    @Test
    public void testHasTranslationX() {
        float value = randomGenerator.nextFloat();
        assertThat(whenReturn(viewSpy.getTranslationX(), value))
                .hasTranslationX(value);
    }

    @Test
    public void testIsGone() {
        assertThat(whenReturn(viewSpy.getVisibility(), View.GONE))
                .isGone();
    }

    @Test
    public void testHasTranslationY() {
        float value = randomGenerator.nextFloat();
        assertThat(whenReturn(viewSpy.getTranslationY(), value))
                .hasTranslationY(value);
    }

    @Test
    public void testIsScrollContainer() {
        assertThat(whenReturn(viewSpy.isScrollContainer(), true))
                .isScrollContainer();
    }

    @Test
    public void testIsDirty() {
        assertThat(whenReturn(viewSpy.isDirty(), true))
                .isDirty();
    }

    @Test
    public void testIsNotInFocusedWindow() {
        assertThat(whenReturn(viewSpy.hasWindowFocus(), false))
                .isNotInFocusedWindow();
    }

    @Test
    public void testIsSelected() {
        assertThat(whenReturn(viewSpy.isSelected(), true))
                .isSelected();
    }

    @Test
    public void testIsNotFocused() {
        assertThat(whenReturn(viewSpy.isFocused(), false))
                .isNotFocused();
    }

    @Test
    public void testIsNotHardwareAccelerated() {
        assertThat(whenReturn(viewSpy.isHardwareAccelerated(), false))
                .isNotHardwareAccelerated();
    }

    @Test
    public void testHasFocus() {
        assertThat(whenReturn(viewSpy.hasFocus(), true))
                .hasFocus();
    }

    @Test
    public void testHasVerticalScrollBarDisabled() {
        assertThat(whenReturn(viewSpy.isVerticalScrollBarEnabled(), false))
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
        assertThat(whenReturn(viewSpy.getOverScrollMode(), value))
                .hasOverScrollMode(value);
    }

    @Test
    public void testIsShown() {
        assertThat(whenReturn(viewSpy.isShown(), true))
                .isShown();
    }

    @Test
    public void testHasWindowVisibility() {
        int value = randomGenerator.nextInt();
        assertThat(whenReturn(viewSpy.getWindowVisibility(), value))
                .hasWindowVisibility(value);
    }

    @Test
    public void testIsUsingDrawingCache() {
        assertThat(whenReturn(viewSpy.isDrawingCacheEnabled(), true))
                .isUsingDrawingCache();
    }

    @Test
    public void testHasSaveFromParentEnabled() {
        assertThat(whenReturn(viewSpy.isSaveFromParentEnabled(), true))
                .hasSaveFromParentEnabled();
    }

    @Test
    public void testIsNotPressed() {
        assertThat(whenReturn(viewSpy.isPressed(), false))
                .isNotPressed();
    }

    @Test
    public void testHasHorizontalFadingEdgesDisabled() {
        assertThat(whenReturn(viewSpy.isHorizontalFadingEdgeEnabled(), false))
                .hasHorizontalFadingEdgesDisabled();
    }


    @Test
    public void testHasHapticFeedbackDisabled() {
        assertThat(whenReturn(viewSpy.isHapticFeedbackEnabled(), false))
                .hasHapticFeedbackDisabled();
    }

    @Test
    public void testIsInFocusedWindow() {
        assertThat(whenReturn(viewSpy.hasWindowFocus(), true))
                .isInFocusedWindow();
    }

    @Test
    public void testIsNotScrollContainer() {
        assertThat(whenReturn(viewSpy.isScrollContainer(), false))
                .isNotScrollContainer();
    }

    @Test
    public void testHasNoLayoutRequested() {
        assertThat(whenReturn(viewSpy.isLayoutRequested(), false))
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
        assertThat(whenReturn(viewSpy.isSoundEffectsEnabled(), false))
                .hasSoundEffectsDisabled();
    }

    @Test
    public void testHasVisibility() {
        int value = randomGenerator.nextInt();
        assertThat(whenReturn(viewSpy.getVisibility(), value))
                .hasVisibility(value);
    }

    @Test
    public void testHasSoundEffectsEnabled() {
        assertThat(whenReturn(viewSpy.isSoundEffectsEnabled(), true))
                .hasSoundEffectsEnabled();
    }

    @Test
    public void testHasNextFocusLeftId() {
        int value = randomGenerator.nextInt();
        assertThat(whenReturn(viewSpy.getNextFocusLeftId(), value))
                .hasNextFocusLeftId(value);
    }

    @Test
    public void testHasHorizontalFadingEdgeLength() {
        int value = randomGenerator.nextInt();
        assertThat(whenReturn(viewSpy.getHorizontalFadingEdgeLength(), value))
                .hasHorizontalFadingEdgeLength(value);
    }

    @Test
    public void testIsNotActivated() {
        assertThat(whenReturn(viewSpy.isActivated(), false))
                .isNotActivated();
    }

    @Test
    public void testHasLayoutRequested() {
        assertThat(whenReturn(viewSpy.isLayoutRequested(), true))
                .hasLayoutRequested();
    }

    @Test
    public void testHasHapticFeedbackEnabled() {
        assertThat(whenReturn(viewSpy.isHapticFeedbackEnabled(), true))
                .hasHapticFeedbackEnabled();
    }


    @Test
    public void testHasNextFocusDownId() {
        int value = randomGenerator.nextInt();
        assertThat(whenReturn(viewSpy.getNextFocusDownId(), value))
                .hasNextFocusDownId(value);
    }

    @Test
    public void testHasSaveFromParentDisabled() {
        assertThat(whenReturn(viewSpy.isSaveFromParentEnabled(), false))
                .hasSaveFromParentDisabled();
    }

    @Test
    public void testHasPaddingBottom() {
        int value = randomGenerator.nextInt();
        assertThat(whenReturn(viewSpy.getPaddingBottom(), value))
                .hasPaddingBottom(value);
    }

    @Test
    public void testIsInEditMode() {
        assertThat(whenReturn(viewSpy.isInEditMode(), true))
                .isInEditMode();
    }

    @Test
    public void testIsNotDuplicatingParentState() {
        assertThat(whenReturn(viewSpy.isDuplicateParentStateEnabled(), false))
                .isNotDuplicatingParentState();
    }

    @Test
    public void testHasVerticalScrollbarWidth() {
        int value = randomGenerator.nextInt();
        assertThat(whenReturn(viewSpy.getVerticalScrollbarWidth(), value))
                .hasVerticalScrollbarWidth(value);
    }

    @Test
    public void testHasPivotX() {
        float value = randomGenerator.nextFloat();
        assertThat(whenReturn(viewSpy.getPivotX(), value))
                .hasPivotX(value);
    }

    @Test
    public void testHasPivotY() {
        float value = randomGenerator.nextFloat();
        assertThat(whenReturn(viewSpy.getPivotY(), value))
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
        assertThat(whenReturn(viewSpy.getScrollBarStyle(), value))
                .hasScrollBarStyle(value);
    }

    @Test
    public void testHasTag() {
        assertThat(whenReturn(viewSpy.getTag(), DUMMY_STRING))
                .hasTag(DUMMY_STRING);
    }

    @Test
    public void testIsKeepingScreenOn() {
        assertThat(whenReturn(viewSpy.getKeepScreenOn(), true))
                .isKeepingScreenOn();
    }

    @Test
    public void testIsActivated() {
        assertThat(whenReturn(viewSpy.isActivated(), true))
                .isActivated();
    }

    @Test
    public void testIsFocused() {
        assertThat(whenReturn(viewSpy.isFocused(), true))
                .isFocused();
    }

    @Test
    public void testIsNotLongClickable() {
        assertThat(whenReturn(viewSpy.isLongClickable(), false))
                .isNotLongClickable();
    }

    @Test
    public void testHasHorizontalScrollbarDisabled() {
        assertThat(whenReturn(viewSpy.isHorizontalScrollBarEnabled(), false))
                .hasHorizontalScrollbarDisabled();
    }

    @Test
    public void testHasId() {
        int value = randomGenerator.nextInt();
        assertThat(whenReturn(viewSpy.getId(), value))
                .hasId(value);
    }

    @Test
    public void testIsNotKeepingScreenOn() {
        assertThat(whenReturn(viewSpy.getKeepScreenOn(), false))
                .isNotKeepingScreenOn();
    }

    @Test
    public void testHasScrollbarFadingEnabled() {
        assertThat(whenReturn(viewSpy.isScrollbarFadingEnabled(), true))
                .hasScrollbarFadingEnabled();
    }

    @Test
    public void testHasScrollBarSize() {
        int value = randomGenerator.nextInt();
        assertThat(whenReturn(viewSpy.getScrollBarSize(), value))
                .hasScrollBarSize(value);
    }

    @Test
    public void testHasLayerType() {
        int value = randomGenerator.nextInt();
        assertThat(whenReturn(viewSpy.getLayerType(), value))
                .hasLayerType(value);
    }

    @Test
    public void testHasNextFocusForwardId() {
        int value = randomGenerator.nextInt();
        assertThat(whenReturn(viewSpy.getNextFocusForwardId(), value))
                .hasNextFocusForwardId(value);
    }

    @Test
    public void testHasScrollbarFadingDisabled() {
        assertThat(whenReturn(viewSpy.isScrollbarFadingEnabled(), false))
                .hasScrollbarFadingDisabled();
    }

    @Test
    public void testHasPaddingLeft() {
        int value = randomGenerator.nextInt();
        assertThat(whenReturn(viewSpy.getPaddingLeft(), value))
                .hasPaddingLeft(value);
    }

    @Test
    public void testIsNotGone() {
        assertThat(whenReturn(viewSpy.getVisibility(), View.VISIBLE))
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
        assertThat(whenReturn(viewSpy.getPaddingTop(), value))
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
        assertThat(whenReturn(viewSpy.getX(), value))
                .hasX(value);
    }

    @Test
    public void testHasY() {
        float value = randomGenerator.nextFloat();
        assertThat(whenReturn(viewSpy.getY(), value))
                .hasY(value);
    }

    @Test
    public void testHasNextFocusRightId() {
        int value = randomGenerator.nextInt();
        assertThat(whenReturn(viewSpy.getNextFocusRightId(), value))
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
        assertThat(whenReturn(viewSpy.isHovered(), false))
                .isNotHovered();
    }

    @Test
    public void testHasRotation() {
        float value = randomGenerator.nextFloat();
        assertThat(whenReturn(viewSpy.getRotation(), value))
                .hasRotation(value);
    }

    @Test
    public void testHasBackground() {
        android.graphics.drawable.Drawable value = mock(android.graphics.drawable.Drawable.class);
        assertThat(whenReturn(viewSpy.getBackground(), value))
                .hasBackground(value);
    }

    @Test
    public void testIsNotInEditMode() {
        assertThat(whenReturn(viewSpy.isInEditMode(), false))
                .isNotInEditMode();
    }

    @Test
    public void testIsNotFocusableInTouchMode() {
        assertThat(whenReturn(view.isFocusableInTouchMode(), false))
                .isNotFocusableInTouchMode();
    }

    @Test
    public void testIsPressed() {
        assertThat(whenReturn(viewSpy.isPressed(), true))
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
        assertThat(whenReturn(viewSpy.getScrollBarDefaultDelayBeforeFade(), value))
                .hasScrollBarDefaultDelayBeforeFade(value);
    }

    @Test
    public void testIsDuplicatingParentState() {
        assertThat(whenReturn(viewSpy.isDuplicateParentStateEnabled(), true))
                .isDuplicatingParentState();
    }

    @Test
    public void testHasNextFocusUpId() {
        int value = randomGenerator.nextInt();
        assertThat(whenReturn(viewSpy.getNextFocusUpId(), value))
                .hasNextFocusUpId(value);
    }

    @Test
    public void testHasRotationY() {
        float value = randomGenerator.nextFloat();
        assertThat(whenReturn(viewSpy.getRotationY(), value))
                .hasRotationY(value);
    }

    @Test
    public void testHasParentForAccessibility() {
        android.view.ViewParent value = mock(android.view.ViewParent.class);
        assertThat(whenReturn(viewSpy.getParentForAccessibility(), value))
                .hasParentForAccessibility(value);
    }

    @Test
    public void testHasSystemUiVisibility() {
        int value = randomGenerator.nextInt();
        assertThat(whenReturn(viewSpy.getSystemUiVisibility(), value))
                .hasSystemUiVisibility(value);
    }

    @Test
    public void testHasRotationX() {
        float value = randomGenerator.nextFloat();
        assertThat(whenReturn(viewSpy.getRotationX(), value))
                .hasRotationX(value);
    }

    @Test
    public void testHasMinimumWidth() {
        int value = randomGenerator.nextInt();
        assertThat(whenReturn(viewSpy.getMinimumWidth(), value))
                .hasMinimumWidth(value);
    }

    @Test
    public void testIsNotSelected() {
        assertThat(whenReturn(viewSpy.isSelected(), false))
                .isNotSelected();
    }

    @Test
    public void testHasSaveDisabled() {
        assertThat(whenReturn(viewSpy.isSaveEnabled(), false))
                .hasSaveDisabled();
    }

    @Test
    public void testHasHorizontalFadingEdgesEnabled() {
        assertThat(whenReturn(viewSpy.isHorizontalFadingEdgeEnabled(), true))
                .hasHorizontalFadingEdgesEnabled();
    }

    @Test
    public void testIsHardwareAccelerated() {
        assertThat(whenReturn(viewSpy.isHardwareAccelerated(), true))
                .isHardwareAccelerated();
    }

    @Test
    public void testIsClickable() {
        assertThat(whenReturn(viewSpy.isClickable(), true))
                .isClickable();
    }

    @Test
    public void testHasDrawingCacheQuality() {
        int value = randomGenerator.nextInt();
        assertThat(whenReturn(viewSpy.getDrawingCacheQuality(), value))
                .hasDrawingCacheQuality(value);
    }

    @Test
    public void testIsNotInTouchMode() {
        assertThat(whenReturn(viewSpy.isInTouchMode(), false))
                .isNotInTouchMode();
    }

    private <T> View whenReturn(T methodCall, T value) {
        return when(methodCall)
                .thenReturn(value)
                .getMock();
    }
}