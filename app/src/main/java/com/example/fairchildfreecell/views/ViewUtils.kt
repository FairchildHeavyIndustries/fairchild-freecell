package com.example.fairchildfreecell.views

import android.app.Activity
import android.content.Context
import android.widget.ImageView
import android.widget.LinearLayout
import com.example.fairchildfreecell.model.BOARD_COLUMN_IDS
import com.example.fairchildfreecell.model.CardLocation
import com.example.fairchildfreecell.model.GameSection
import com.example.fairchildfreecell.R
import com.example.fairchildfreecell.model.settings.SettingsManager
import kotlin.math.roundToInt

fun createPlaceholderView(context: Context, width: Int, height: Int): ImageView {
    val placeholder = ImageView(context)
    placeholder.setBackgroundResource(R.drawable.placeholder_background)
    placeholder.layoutParams = LinearLayout.LayoutParams(width, height)
    return placeholder
}

fun findParentLayout(activity: Activity, location: CardLocation): LinearLayout {
    return when (location.section) {
        GameSection.BOARD -> activity.findViewById(BOARD_COLUMN_IDS[location.columnIndex - 1])
        GameSection.FREECELL -> activity.findViewById(R.id.freeCellLayout)
        GameSection.FOUNDATION -> activity.findViewById(R.id.foundationLayout)
    }
}

 fun getBoardCardTopMargin (cardHeight: Int): Int {
    return -(cardHeight * SettingsManager.currentSettings.cardSpacing.overlapPercentage).roundToInt()
}