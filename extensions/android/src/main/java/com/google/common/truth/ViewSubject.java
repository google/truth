package com.google.common.truth;

import android.annotation.TargetApi;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewParent;
import android.view.animation.Animation;

import javax.annotation.Nullable;

import static com.google.common.truth.Truth.assertThat;


/**
 * @author Kevin Leigh Crain
 */

/**
 * Todo: remove finals on method and make class abstract
 * Todo: Uncomment {@link TargetApi} versions after static importing all versions required for class
 */
public class ViewSubject extends Subject<ViewSubject, View> {

    public ViewSubject(FailureStrategy failureStrategy, @Nullable View actual) {
        super(failureStrategy, actual);
    }

    public final void hasAlpha(float alpha) {
        float actualAlpha = actual().getAlpha();
        if (actual().getAlpha() != alpha) fail("has alpha: " + alpha + ", actual: " + actualAlpha);
    }

    public final void hasAnimation(Animation animation) {
        assertThat(actual().getAnimation())
                .isSameAs(animation);
    }

    public void hasBackground(Drawable background) {
        Drawable actualDrawable = actual().getBackground();
        assertThat(actualDrawable)
                .isSameAs(background);
    }

    public void hasBaseline(int baseline) {
        int actualBaseline = actual().getBaseline();
        assertThat(actualBaseline)
                .isEqualTo(baseline);
    }

    public void hasBottom(int bottom) {
        int actualBottom = actual().getBottom();
        assertThat(actualBottom)
                .isEqualTo(bottom);
    }

    public void hasContentDescription(CharSequence contentDescription) {
        CharSequence actualContentDescription = actual().getContentDescription();
        assertThat(actualContentDescription)
                .isEqualTo(contentDescription);
    }

    public void hasContentDescription(int resId) {
        hasContentDescription(actual().getContext().getString(resId));
    }

    public void hasDrawingCacheBackgroundColor(int color) {
        int actualColor = actual().getDrawingCacheBackgroundColor();
        assertThat(actualColor)
                .isEqualTo(color);
    }

    public void hasDrawingCacheQuality(int quality) {
        int actualQuality = actual().getDrawingCacheQuality();
        assertThat(actualQuality)
                .isEqualTo(quality);
    }

    public void hasHeight(int height) {
        int actualHeight = actual().getHeight();
        assertThat(actualHeight)
                .isEqualTo(height);
    }

    public void hasHorizontalFadingEdgeLength(int length) {
        int actualLength = actual().getHorizontalFadingEdgeLength();
        assertThat(actualLength)
                .isEqualTo(length);
    }

    public void hasId(int id) {
        int actualId = actual().getId();
        assertThat(actualId)
                .isEqualTo(id);
    }

    public void isKeepingScreenOn() {
        assertThat(actual().getKeepScreenOn()) //
                .isTrue();
    }

    public void isNotKeepingScreenOn() {
        assertThat(actual().getKeepScreenOn())
                .isFalse();
    }

    //    @TargetApi(HONEYCOMB)
    public void hasLayerType(int type) {
        int actualType = actual().getLayerType();
        assertThat(actualType)
                .isEqualTo(type);
    }

    public void hasLeft(int left) {
        int actualLeft = actual().getLeft();
        assertThat(actualLeft)
                .isEqualTo(left);
    }

    public void hasMeasuredHeight(int height) {
        int actualHeight = actual().getMeasuredHeight();
        assertThat(actualHeight)
                .isEqualTo(height);
    }

    //    @TargetApi(HONEYCOMB)
    public void hasMeasuredHeightAndState(int heightAndState) {
        int actualHeightAndState = actual().getMeasuredHeightAndState();
        assertThat(actualHeightAndState)
                .isEqualTo(heightAndState);
    }

    //    @TargetApi(HONEYCOMB)
    public void hasMeasuredState(int state) {
        int actualState = actual().getMeasuredState();
        assertThat(actualState)
                .isEqualTo(state);
    }

    public void hasMeasuredWidth(int width) {
        int actualWidth = actual().getMeasuredWidth();
        assertThat(actualWidth)
                .isEqualTo(width);
    }

    //    @TargetApi(HONEYCOMB)
    public void hasMeasuredWidthAndState(int widthAndState) {
        int actualWidthAndState = actual().getMeasuredWidthAndState();
        assertThat(actualWidthAndState)
                .isEqualTo(widthAndState);
    }

    //    @TargetApi(JELLY_BEAN)
    public void hasMinimumHeight(int height) {
        int actualHeight = actual().getMinimumHeight();
        assertThat(actualHeight)
                .isEqualTo(height);
    }

    //    @TargetApi(JELLY_BEAN)
    public void hasMinimumWidth(int width) {
        int actualWidth = actual().getMinimumWidth();
        assertThat(actualWidth)
                .isEqualTo(width);
    }

    public void hasNextFocusDownId(int id) {
        int actualId = actual().getNextFocusDownId();
        assertThat(actualId)
                .isEqualTo(id);
    }

    //    @TargetApi(HONEYCOMB)
    public void hasNextFocusForwardId(int id) {
        int actualId = actual().getNextFocusForwardId();
        assertThat(actualId)
                .isEqualTo(id);
    }

    public void hasNextFocusLeftId(int id) {
        int actualId = actual().getNextFocusLeftId();
        assertThat(actualId)
                .isEqualTo(id);
    }

    public void hasNextFocusRightId(int id) {
        int actualId = actual().getNextFocusRightId();
        assertThat(actualId)
                .isEqualTo(id);
    }

    public void hasNextFocusUpId(int id) {
        int actualId = actual().getNextFocusUpId();
        assertThat(actualId)
                .isEqualTo(id);
    }

    //    @TargetApi(GINGERBREAD)
    public void hasOverScrollMode(int mode) {
        int actualMode = actual().getOverScrollMode();
        assertThat(actualMode)
                .isEqualTo(mode);
    }

    public void hasPaddingBottom(int padding) {
        int actualPadding = actual().getPaddingBottom();
        assertThat(actualPadding)
                .isEqualTo(padding);
    }

    public void hasPaddingLeft(int padding) {
        int actualPadding = actual().getPaddingLeft();
        assertThat(actualPadding)
                .isEqualTo(padding);
    }

    public void hasPaddingRight(int padding) {
        int actualPadding = actual().getPaddingRight();
        assertThat(actualPadding)
                .isEqualTo(padding);
    }

    public void hasPaddingTop(int padding) {
        int actualPadding = actual().getPaddingTop();
        assertThat(actualPadding)
                .isEqualTo(padding);
    }

    public void hasParent(ViewParent parent) {
        ViewParent actualParent = actual().getParent();
        assertThat(actualParent)
                .isSameAs(parent);
    }

    //    @TargetApi(JELLY_BEAN)
    public void hasParentForAccessibility(ViewParent parent) {
        ViewParent actualParent = actual().getParentForAccessibility();
        assertThat(actualParent)
                .isSameAs(parent);
    }

    //    @TargetApi(HONEYCOMB)
    public void hasPivotX(float pivotX) {
        float actualPivotX = actual().getPivotX();
        assertThat(actualPivotX)
                .isEqualTo(pivotX);
    }

    //    @TargetApi(HONEYCOMB)
    public void hasPivotY(float pivotY) {
        float actualPivotY = actual().getPivotY();
        assertThat(actualPivotY)
                .isEqualTo(pivotY);
    }

    public void hasRight(int right) {
        int actualRight = actual().getRight();
        assertThat(actualRight)
                .isEqualTo(right);
    }

    public void hasRootView(View view) {
        View actualView = actual().getRootView();
        assertThat(actualView)
                .isSameAs(view);
    }

    //    @TargetApi(HONEYCOMB)
    public void hasRotation(float rotation) {
        float actualRotation = actual().getRotation();
        assertThat(actualRotation)
                .isSameAs(rotation);
    }

    //    @TargetApi(HONEYCOMB)
    public void hasRotationX(float rotation) {
        float actualRotation = actual().getRotationX();
        assertThat(actualRotation)
                .isSameAs(rotation);
    }

    //    @TargetApi(HONEYCOMB)
    public void hasRotationY(float rotation) {
        float actualRotation = actual().getRotationY();
        assertThat(actualRotation)
                .isSameAs(rotation);
    }

    //    @TargetApi(HONEYCOMB)
    public void hasScaleX(float scale) {
        float actualScale = actual().getScaleX();
        assertThat(actualScale)
                .isSameAs(scale);
    }

    //    @TargetApi(HONEYCOMB)
    public void hasScaleY(float scale) {
        float actualScale = actual().getScaleY();
        assertThat(actualScale)
                .isSameAs(scale);
    }

    //    @TargetApi(JELLY_BEAN)
    public void hasScrollBarDefaultDelayBeforeFade(int fade) {
        int actualFade = actual().getScrollBarDefaultDelayBeforeFade();
        assertThat(actualFade)
                .isEqualTo(fade);
    }

    //    @TargetApi(JELLY_BEAN)
    public void hasScrollBarFadeDuration(int fade) {
        int actualFade = actual().getScrollBarFadeDuration();
        assertThat(actualFade)
                .isEqualTo(fade);
    }

    //    @TargetApi(JELLY_BEAN)
    public void hasScrollBarSize(int size) {
        int actualSize = actual().getScrollBarSize();
        assertThat(actualSize)
                .isEqualTo(size);
    }

    public void hasScrollBarStyle(int style) {
        int actualStyle = actual().getScrollBarStyle();
        assertThat(actualStyle)
                .isEqualTo(style);
    }

    public void hasScrollX(int scroll) {
        int actualScroll = actual().getScrollX();
        assertThat(actualScroll)
                .isEqualTo(scroll);
    }

    public void hasScrollY(int scroll) {
        int actualScroll = actual().getScrollY();
        assertThat(actualScroll)
                .isEqualTo(scroll);
    }

    public void hasSolidColor(int color) {
        int actualColor = actual().getSolidColor();
        assertThat(actualColor)
                .isEqualTo(color);
    }

    //    @TargetApi(HONEYCOMB)
    public void hasSystemUiVisibility(int visibility) {
        int actualVisibility = actual().getSystemUiVisibility();
        assertThat(actualVisibility)
                .isEqualTo(visibility);
    }

    public void hasTag(int key, Object tag) {
        Object actualTag = actual().getTag(key);
        assertThat(actualTag)
                .isSameAs(tag);
    }

    public void hasTag(Object tag) {
        Object actualTag = actual().getTag();
        assertThat(actualTag)
                .isSameAs(tag);
    }

    public void hasTop(int top) {
        int actualTop = actual().getTop();
        assertThat(actualTop)
                .isEqualTo(top);

    }

    //    @TargetApi(HONEYCOMB)
    public void hasTranslationX(float translation) {
        float actualTranslation = actual().getTranslationX();
        assertThat(actualTranslation)
                .isEqualTo(translation);
    }

    //    @TargetApi(HONEYCOMB)
    public void hasTranslationY(float translation) {
        float actualTranslation = actual().getTranslationY();
        assertThat(actualTranslation)
                .isEqualTo(translation);
    }

    public void hasVerticalFadingEdgeLength(int length) {
        int actualLength = actual().getVerticalFadingEdgeLength();
        assertThat(actualLength)
                .isEqualTo(length);
    }

    //    @TargetApi(HONEYCOMB)
    public void hasVerticalScrollbarPosition(int position) {
        int actualPosition = actual().getVerticalScrollbarPosition();
        assertThat(actualPosition)
                .isEqualTo(position);
    }

    public void hasVerticalScrollbarWidth(int width) {
        int actualWidth = actual().getVerticalScrollbarWidth();
        assertThat(actualWidth)
                .isEqualTo(width);
    }

    public void hasVisibility(int visibility) {
        int actualVisibility = actual().getVisibility();
        assertThat(actualVisibility)
                .isEqualTo(visibility);
    }

    public final void isVisible() {
        if (actual().getVisibility() != View.VISIBLE) fail("is not visible");
    }

    public final void isNotVisible() {
        if (actual().getVisibility() == View.VISIBLE) fail("is visible");
    }

    public void isGone() {
        int actualVisibility = actual().getVisibility();
        assertThat(actualVisibility)
                .isEqualTo(View.GONE);
    }

    public void isNotGone() {
        int actualVisibility = actual().getVisibility();
        assertThat(actualVisibility)
                .isNotEqualTo(View.GONE);
    }

    public void hasWidth(int width) {
        int actualWidth = actual().getWidth();
        assertThat(actualWidth)
                .isEqualTo(width);
    }

    public void hasWindowVisibility(int visibility) {
        int actualVisibility = actual().getWindowVisibility();
        assertThat(actualVisibility)
                .isEqualTo(visibility);
    }

    //    @TargetApi(HONEYCOMB)
    public void hasX(float x) {
        float actualX = actual().getX();
        assertThat(actualX)
                .isEqualTo(x);
    }

    //    @TargetApi(HONEYCOMB)
    public void hasY(float y) {
        float actualY = actual().getY();
        assertThat(actualY)
                .isEqualTo(y);
    }

    public void hasFocus() {
        assertThat(actual().hasFocus())
                .isTrue();
    }

    public void hasNoFocus() {
        assertThat(actual().hasFocus())
                .isFalse();
    }

    public void hasFocusable() {
        assertThat(actual().hasFocusable())
                .isTrue();
    }

    public void isInFocusedWindow() {
        assertThat(actual().hasWindowFocus())
                .isTrue();
    }

    public void isNotInFocusedWindow() {
        assertThat(actual().hasWindowFocus())
                .isFalse();
    }

    //    @TargetApi(HONEYCOMB)
    public void isActivated() {
        assertThat(actual().isActivated())
                .isTrue();
    }

    //    @TargetApi(HONEYCOMB)
    public void isNotActivated() {
        assertThat(actual().isActivated())
                .isFalse();
    }

    public void isClickable() {
        assertThat(actual().isClickable())
                .isTrue();
    }

    public void isNotClickable() {
        assertThat(actual().isClickable())
                .isFalse();
    }

    public void isDirty() {
        assertThat(actual().isDirty())
                .isTrue();
    }

    //    @TargetApi(HONEYCOMB)
    public void isNotDirty() {
        assertThat(actual().isDirty())
                .isFalse();
    }

    public void isUsingDrawingCache() {
        assertThat(actual().isDrawingCacheEnabled())
                .isTrue();
    }

    public void isNotUsingDrawingCache() {
        assertThat(actual().isDrawingCacheEnabled())
                .isFalse();
    }

    public void isDuplicatingParentState() {
        assertThat(actual().isDuplicateParentStateEnabled())
                .isTrue();
    }

    public void isNotDuplicatingParentState() {
        assertThat(actual().isDuplicateParentStateEnabled())
                .isFalse();
    }

    public void isEnabled() {
        assertThat(actual().isEnabled())
                .isTrue();
    }

    public void isDisabled() {
        assertThat(actual().isEnabled())
                .isFalse();
    }

    public void isFocusable() {
        assertThat(actual().isFocusable())
                .isTrue();
    }

    public void isNotFocusable() {
        assertThat(actual().isFocusable())
                .isFalse();
    }

    public void isFocusableInTouchMode() {
        assertThat(actual().isFocusableInTouchMode())
                .isTrue();
    }

    public void isNotFocusableInTouchMode() {
        assertThat(actual().isFocusableInTouchMode())
                .isFalse();
    }

    public void isFocused() {
        assertThat(actual().isFocused())
                .isTrue();
    }

    public void isNotFocused() {
        assertThat(actual().isFocused())
                .isFalse();
    }

    public void hasHapticFeedbackEnabled() {
        assertThat(actual().isHapticFeedbackEnabled())
                .isTrue();
    }

    public void hasHapticFeedbackDisabled() {
        assertThat(actual().isHapticFeedbackEnabled())
                .isFalse();
    }

    //    @TargetApi(HONEYCOMB)
    public void isHardwareAccelerated() {
        assertThat(actual().isHardwareAccelerated())
                .isTrue();
    }

    //    @TargetApi(HONEYCOMB)
    public void isNotHardwareAccelerated() {
        assertThat(actual().isHardwareAccelerated())
                .isFalse();
    }

    public void hasHorizontalFadingEdgesEnabled() {
        assertThat(actual().isHorizontalFadingEdgeEnabled())
                .isTrue();
    }

    public void hasHorizontalFadingEdgesDisabled() {
        assertThat(actual().isHorizontalFadingEdgeEnabled())
                .isFalse();
    }

    public void hasHorizontalScrollbarEnabled() {
        assertThat(actual().isHorizontalScrollBarEnabled())
                .isTrue();
    }

    public void hasHorizontalScrollbarDisabled() {
        assertThat(actual().isHorizontalScrollBarEnabled())
                .isFalse();
    }

    //    @TargetApi(ICE_CREAM_SANDWICH)
    public void isHovered() {
        assertThat(actual().isHovered())
                .isTrue();
    }

    //    @TargetApi(ICE_CREAM_SANDWICH)
    public void isNotHovered() {
        assertThat(actual().isHovered())
                .isFalse();
    }

    public void isInEditMode() {
        assertThat(actual().isInEditMode())
                .isTrue();
    }

    public void isNotInEditMode() {
        assertThat(actual().isInEditMode())
                .isFalse();
    }

    public void isInTouchMode() {
        assertThat(actual().isInTouchMode())
                .isTrue();
    }

    public void isNotInTouchMode() {
        assertThat(actual().isInTouchMode())
                .isFalse();
    }

    public void hasLayoutRequested() {
        assertThat(actual().isLayoutRequested())
                .isTrue();
    }

    public void hasNoLayoutRequested() {
        assertThat(actual().isLayoutRequested())
                .isFalse();
    }

    public void isLongClickable() {
        assertThat(actual().isLongClickable())
                .isTrue();
    }

    public void isNotLongClickable() {
        assertThat(actual().isLongClickable())
                .isFalse();
    }

    public void isOpaque() {
        assertThat(actual().isOpaque())
                .isTrue();
    }

    public void isNotOpaque() {
        assertThat(actual().isOpaque())
                .isFalse();
    }

    public void isPressed() {
        assertThat(actual().isPressed())
                .isTrue();
    }

    public void isNotPressed() {
        assertThat(actual().isPressed())
                .isFalse();
    }

    public void hasSaveEnabled() {
        assertThat(actual().isSaveEnabled())
                .isTrue();
    }

    public void hasSaveDisabled() {
        assertThat(actual().isSaveEnabled())
                .isFalse();
    }

    public void hasSaveFromParentEnabled() {
        assertThat(actual().isSaveFromParentEnabled())
                .isTrue();
    }

    public void hasSaveFromParentDisabled() {
        assertThat(actual().isSaveFromParentEnabled())
                .isFalse();
    }

    public void isScrollContainer() {
        assertThat(actual().isScrollContainer())
                .isTrue();
    }

    public void isNotScrollContainer() {
        assertThat(actual().isScrollContainer())
                .isFalse();
    }

    public void hasScrollbarFadingEnabled() {
        assertThat(actual().isScrollbarFadingEnabled())
                .isTrue();
    }

    public void hasScrollbarFadingDisabled() {
        assertThat(actual().isScrollbarFadingEnabled())
                .isFalse();
    }

    public void isSelected() {
        assertThat(actual().isSelected())
                .isTrue();
    }

    public void isNotSelected() {
        assertThat(actual().isSelected())
                .isFalse();
    }

    public void isShown() {
        assertThat(actual().isShown())
                .isTrue();
    }

    public void isNotShown() {
        assertThat(actual().isShown())
                .isFalse();
    }

    public void hasSoundEffectsEnabled() {
        assertThat(actual().isSoundEffectsEnabled())
                .isTrue();

    }

    public void hasSoundEffectsDisabled() {
        assertThat(actual().isSoundEffectsEnabled())
                .isFalse();

    }

    public void hasVerticalFadingEdgeEnabled() {
        assertThat(actual().isVerticalFadingEdgeEnabled())
                .isTrue();

    }

    public void hasVerticalFadingEdgeDisabled() {
        assertThat(actual().isVerticalFadingEdgeEnabled())
                .isFalse();
    }

    public void hasVerticalScrollBarEnabled() {
        assertThat(actual().isVerticalScrollBarEnabled())
                .isTrue();
    }

    public void hasVerticalScrollBarDisabled() {
        assertThat(actual().isVerticalScrollBarEnabled())
                .isFalse();
    }

    public static SubjectFactory<ViewSubject, View> views() {
        return FACTORY;
    }

    private static final SubjectFactory<ViewSubject, View> FACTORY = new SubjectFactory<ViewSubject, View>() {
        @Override
        public ViewSubject getSubject(FailureStrategy fs, View that) {
            return new ViewSubject(fs, that);
        }
    };
}

/**
 * Port commented out remaining assertion methods
 */

//
//
//    @TargetApi(KITKAT)
//    public void canResolveLayoutDirection() {
//
//        assertThat(actual().canResolveLayoutDirection()) //
//                .isTrue();
//
//    }
//
//    @TargetApi(KITKAT)
//    public void canNotResolveLayoutDirection() {
//
//        assertThat(actual().canResolveLayoutDirection()) //
//                .isFalse();
//
//    }
//
//    @TargetApi(KITKAT)
//    public void canResolveTextAlignment() {
//
//        assertThat(actual().canResolveLayoutDirection()) //
//                .isTrue();
//
//    }
//
//    @TargetApi(KITKAT)
//    public void canNotResolveTextAlignment() {
//
//        assertThat(actual().canResolveLayoutDirection()) //
//                .isFalse();
//
//    }
//
//    @TargetApi(KITKAT)
//    public void canResolveTextDirection() {
//
//        assertThat(actual().canResolveTextDirection()) //
//                .isTrue();
//
//    }
//
//    @TargetApi(KITKAT)
//    public void canNotResolveTextDirection() {
//
//        assertThat(actual().canResolveTextDirection()) //
//                .isFalse();
//
//    }
//
//    @TargetApi(KITKAT)
//    public void isAttachedToWindow() {
//
//        assertThat(actual().isAttachedToWindow()) //
//                .isTrue();
//
//    }
//
//    @TargetApi(KITKAT)
//    public void isNotAttachedToWindow() {
//
//        assertThat(actual().isAttachedToWindow()) //
//                .isFalse();
//
//    }
//
//    @TargetApi(KITKAT)
//    public void isLaidOut() {
//
//        assertThat(actual().isLaidOut()) //
//                .isTrue();
//
//    }
//
//    @TargetApi(KITKAT)
//    public void isNotLaidOut() {
//
//        assertThat(actual().isLaidOut()) //
//                .isFalse();
//
//    }
//
//    @TargetApi(KITKAT)
//    public void hasResolvedLayoutDirection() {
//
//        assertThat(actual().isLayoutDirectionResolved()) //
//                .isTrue();
//
//    }
//
//    @TargetApi(KITKAT)
//    public void hasNotResolvedLayoutDirection() {
//
//        assertThat(actual().isLayoutDirectionResolved()) //
//                .isFalse();
//
//    }
//
//    @TargetApi(KITKAT)
//    public void hasResolvedTextAlignment() {
//
//        assertThat(actual().isTextAlignmentResolved()) //
//                .isTrue();
//
//    }
//
//    @TargetApi(KITKAT)
//    public void hasNotResolvedTextAlignment() {
//
//        assertThat(actual().isTextAlignmentResolved()) //
//                .isFalse();
//
//    }
//
//    @TargetApi(KITKAT)
//    public void hasResolvedTextDirection() {
//
//        assertThat(actual().isTextDirectionResolved()) //
//                .isTrue();
//
//    }
//
//    @TargetApi(KITKAT)
//    public void hasNotResolvedTextDirection() {
//
//        assertThat(actual().isTextDirectionResolved()) //
//                .isFalse();
//
//    }
//
//    public static String visibilityToString(@ViewVisibility int visibility) {
//        return buildNamedValueString(visibility)
//                .value(VISIBLE, "visible")
//                .value(INVISIBLE, "invisible")
//                .value(GONE, "gone")
//                .get();
//    }
//
//    @TargetApi(HONEYCOMB)
//    public static String layerTypeToString(@ViewLayerType int type) {
//        return buildNamedValueString(type)
//                .value(LAYER_TYPE_NONE, "none")
//                .value(LAYER_TYPE_SOFTWARE, "software")
//                .value(LAYER_TYPE_HARDWARE, "hardware")
//                .get();
//    }
//
//    @TargetApi(JELLY_BEAN_MR1)
//    public static String layoutDirectionToString(@ViewLayoutDirection int direction) {
//        return buildNamedValueString(direction)
//                .value(LAYOUT_DIRECTION_RTL, "right_to_left")
//                .value(LAYOUT_DIRECTION_LTR, "left_to_right")
//                .value(LAYOUT_DIRECTION_INHERIT, "inherit")
//                .value(LAYOUT_DIRECTION_LOCALE, "locale")
//                .get();
//    }
//
//    @TargetApi(GINGERBREAD)
//    public static String overScrollModeToString(@ViewOverscrollMode int mode) {
//        return buildNamedValueString(mode)
//                .value(OVER_SCROLL_ALWAYS, "always")
//                .value(OVER_SCROLL_IF_CONTENT_SCROLLS, "ifContentScrolls")
//                .value(OVER_SCROLL_NEVER, "never")
//                .get();
//    }
//
//    public static String scrollBarStyleToString(@ViewScrollBarStyle int style) {
//        return buildNamedValueString(style)
//                .value(SCROLLBARS_INSIDE_INSET, "insideInset")
//                .value(SCROLLBARS_INSIDE_OVERLAY, "insideOverlay")
//                .value(SCROLLBARS_OUTSIDE_INSET, "outsideInset")
//                .value(SCROLLBARS_OUTSIDE_OVERLAY, "outsideOverlay")
//                .get();
//    }
//
//    @TargetApi(HONEYCOMB)
//    public static String verticalScrollBarPositionToString(@ViewScrollBarPosition int position) {
//        return buildNamedValueString(position)
//                .value(SCROLLBAR_POSITION_DEFAULT, "default")
//                .value(SCROLLBAR_POSITION_LEFT, "left")
//                .value(SCROLLBAR_POSITION_RIGHT, "right")
//                .get();
//    }
//
//    public static String textAlignmentToString(@ViewTextAlignment int alignment) {
//        return buildNamedValueString(alignment)
//                .value(TEXT_ALIGNMENT_INHERIT, "inherit")
//                .value(TEXT_ALIGNMENT_GRAVITY, "gravity")
//                .value(TEXT_ALIGNMENT_TEXT_START, "text_start")
//                .value(TEXT_ALIGNMENT_TEXT_END, "text_end")
//                .value(TEXT_ALIGNMENT_CENTER, "center")
//                .value(TEXT_ALIGNMENT_VIEW_START, "view_start")
//                .value(TEXT_ALIGNMENT_VIEW_END, "view_end")
//                .get();
//    }
//
//    public static String textDirectionToString(@ViewTextDirection int direction) {
//        return buildNamedValueString(direction)
//                .value(TEXT_DIRECTION_INHERIT, "inherit")
//                .value(TEXT_DIRECTION_FIRST_STRONG, "first_strong")
//                .value(TEXT_DIRECTION_ANY_RTL, "any_right_to_left")
//                .value(TEXT_DIRECTION_LTR, "left_to_right")
//                .value(TEXT_DIRECTION_RTL, "right_to_left")
//                .value(TEXT_DIRECTION_LOCALE, "locale")
//                .get();
//    }
//
//
//    //    @TargetApi(JELLY_BEAN_MR2)
//    public void isInLayout() {
//        assertThat(actual().isInLayout()) //
//                .isTrue();
//
//    }
//
//    //    @TargetApi(JELLY_BEAN_MR2)
//    public void isNotInLayout() {
//        assertThat(actual().isInLayout()) //
//                .isFalse();
//
//    }

//     TODO API 17
//    public void isImportantForVisibility() {
//
//      assertThat(actual().getImportantForVisibility()) //
//          .overridingErrorMessage("Expected to be important for visibility but was not") //
//          .isTrue();
//
//    }
//
//    public void isNotImportantForVisibility() {
//
//      assertThat(actual().getImportantForVisibility()) //
//          .overridingErrorMessage("Expected to not be important for visibility but was") //
//          .isFalse();
//
//    }

//    @TargetApi(JELLY_BEAN_MR1)
//    public void hasLabelFor(int id) {
//
//        int actualId = actual().getLabelFor();
//        assertThat(actualId) //
//                .overridingErrorMessage("Expected to have label for ID <%s> but was <%s>", id, actualId) //
//                .isEqualTo(id);
//
//    }
//
//    @TargetApi(JELLY_BEAN_MR1)
//    public void hasTextAlignment(@ViewTextAlignment int alignment) {
//        int actualAlignment = actual().getTextAlignment();
//        assertThat(actualAlignment) //
//                .overridingErrorMessage("Expected text alignment <%s> but was <%s>",
//                        textAlignmentToString(alignment), textAlignmentToString(actualAlignment)) //
//                .isEqualTo(alignment);
//
//    }
//
//    @TargetApi(JELLY_BEAN_MR1)
//    public void hasTextDirection(@ViewTextDirection int direction) {
//        int actualDirection = actual().getTextDirection();
//        assertThat(actualDirection) //
//                .overridingErrorMessage("Expected text direction <%s> but was <%s>",
//                        textDirectionToString(direction), textDirectionToString(actualDirection)) //
//                .isEqualTo(direction);
//    }

//    @TargetApi(JELLY_BEAN_MR1)
//    public void hasLayoutDirection(int direction) {
//        int actualDirection = actual().getLayoutDirection();
//        assertThat(actualDirection)
//                .isEqualTo(direction);
//    }
//
//    //    @TargetApi(JELLY_BEAN_MR1)
//    public void hasPaddingEnd(int padding) {
//        int actualPadding = actual().getPaddingEnd();
//        assertThat(actualPadding)
//                .isEqualTo(padding);
//    }
//
//    //    @TargetApi(JELLY_BEAN_MR1)
//    public void hasPaddingStart(int padding) {
//        int actualPadding = actual().getPaddingStart();
//        assertThat(actualPadding)
//                .overridingErrorMessage("Expected padding start <%s> but was <%s>", padding,
//                        actualPadding) //
//                .isEqualTo(padding);
//
//    }
