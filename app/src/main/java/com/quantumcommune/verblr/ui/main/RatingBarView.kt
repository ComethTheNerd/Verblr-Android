package com.quantumcommune.verblr.ui.main

import android.content.Context
import android.view.View
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet

data class RatingBarViewParams(
    val rating : Float,
    val fullIcon : Int,
    val halfIcon : Int? = null,
    val emptyIcon : Int
)

open class RatingBarView : ConstraintLayout {

    companion object {

        fun newInstance(context : Context?, params : RatingBarViewParams) : RatingBarView
        {
            val instance = RatingBarView(context)
            instance.init(params);
            instance.refresh(params)

            return instance;
        }
    }

    private constructor(context : Context?) : super(context) {
        id = View.generateViewId()
    }

    lateinit var star1 : ImageView
    lateinit var star2 : ImageView
    lateinit var star3 : ImageView
    lateinit var star4 : ImageView
    lateinit var star5 : ImageView

    fun init(params : RatingBarViewParams)
    {
        star1 = createStar()
        star2 = createStar()
        star3 = createStar()
        star4 = createStar()
        star5 = createStar()

        addView(star1)
        addView(star2)
        addView(star3)
        addView(star4)
        addView(star5)

        val constraints = ConstraintSet()
        constraints.clone(this)

        constraints.connect(star1.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraints.connect(star1.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)

        constraints.connect(star2.id, ConstraintSet.START, star1.id, ConstraintSet.END)
        constraints.connect(star2.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)

        constraints.connect(star3.id, ConstraintSet.START, star2.id, ConstraintSet.END)
        constraints.connect(star3.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)

        constraints.connect(star4.id, ConstraintSet.START, star3.id, ConstraintSet.END)
        constraints.connect(star4.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)

        constraints.connect(star5.id, ConstraintSet.START, star4.id, ConstraintSet.END)
        constraints.connect(star5.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)

        constraints.applyTo(this);
    }


    fun refresh(params : RatingBarViewParams) {
        updateStarIcon(params, star1, threshold = 1)
        updateStarIcon(params, star2, threshold = 2)
        updateStarIcon(params, star3, threshold = 3)
        updateStarIcon(params, star4, threshold = 4)
        updateStarIcon(params, star5, threshold = 5)
    }


    private fun createStar() =
        ImageView(context).apply {
            this.id = View.generateViewId()
            this.layoutParams = ConstraintLayout.LayoutParams(
                VRBTheme.contextualIconDim,
                VRBTheme.contextualIconDim
            )
        }

    private fun updateStarIcon(params : RatingBarViewParams, star : ImageView, threshold : Int)
    {
        val rating = params.rating

        val icon = when {
            rating >= threshold -> {
                params.fullIcon
            }
            rating >= (threshold - 0.5) -> {
                params.halfIcon ?: params.emptyIcon
            }
            else -> {
                params.emptyIcon
            }
        }

        star.setImageResource(icon)
    }
}
