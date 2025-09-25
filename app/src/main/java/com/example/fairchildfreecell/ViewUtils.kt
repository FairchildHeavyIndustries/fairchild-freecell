package com.example.fairchildfreecell

import android.app.Activity
import android.content.Context
import android.widget.ImageView
import android.widget.LinearLayout

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