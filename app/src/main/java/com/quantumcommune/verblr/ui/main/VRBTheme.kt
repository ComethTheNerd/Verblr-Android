package com.quantumcommune.verblr.ui.main

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable

class VRBTheme {
    private constructor()

    companion object
    {
        // [dho] adapted from : https://medium.com/@elye.project/android-text-beyond-the-bold-and-normal-47110cc2cb59 - 22/06/20
        // [dho] adapted from : https://stackoverflow.com/a/31867144 - 22/06/20
        val TYPEFACE_regular = Typeface.create("sans-serif", Typeface.NORMAL)
        val TYPEFACE_semibold = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        val TYPEFACE_bold = Typeface.create("sans-serif", Typeface.BOLD)

        val COLOR_subWhite = Color.parseColor("#f5ffff");
        val COLOR_brandElectro = Color.parseColor("#3FEEE6");
        val COLOR_toolbarBG = Color.parseColor("#222324")
        val COLOR_toolbarCTA = COLOR_subWhite
        val COLOR_toolbarAccent = Color.parseColor("#1d1e1f")
        val COLOR_progressBarCompleted = Color.parseColor("#f5ffff")
        val COLOR_progressBarRemaining = Color.parseColor("#525354")
        val COLOR_paid = COLOR_brandElectro
        val COLOR_contentBG = Color.parseColor("#272829")
        val COLOR_contentShadow = Color.parseColor("#1f2021")
        val COLOR_contentAccent = COLOR_toolbarAccent
        val COLOR_listGradientTop = Color.parseColor("#363738")
        val COLOR_listGradientBottom = Color.parseColor("#111213")
        val COLOR_fontTitleMain = COLOR_subWhite
        val COLOR_fontBody = COLOR_subWhite
        val COLOR_fontDisabled = Color.parseColor("#4b4f56")
        val COLOR_primaryBG = Color.parseColor("#F8F9F9")
        val COLOR_secondaryBG = Color.parseColor("#f4ae3d")
        val COLOR_contextualInfo = Color.parseColor("#b2b3b4")
        val COLOR_divider = COLOR_contextualInfo //COLOR_toolbarAccent
        val COLOR_disabled = Color.parseColor("#898b8c")

        val GRAD_standard = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(COLOR_listGradientTop,COLOR_listGradientBottom)
        ).apply { this.cornerRadius = 0f }

        val GRAD_transparent = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(Color.TRANSPARENT, Color.TRANSPARENT)
        ).apply { this.cornerRadius = 0f }

        val gutter : Int = ViewUtils.toXScaledPX(20F)
        val smallGutter : Int = ViewUtils.toXScaledPX(8F)
        val maxiPlayerGutter : Int = ViewUtils.toXScaledPX(30F)
        val productCardGutter : Int = ViewUtils.toXScaledPX(30F)
        val contextualIconDim : Int = ViewUtils.toXScaledPX(14F)
        val dividerHeight : Int = ViewUtils.toPX(1F)
        val spacingBetweenArtworkAndMetaInList : Int = (gutter/2) + ViewUtils.toPX(2F)

        val articleListArtworkDim : Int = ViewUtils.toXScaledPX(100F)
        val articleListItemVerticalSpacing : Int = (gutter * 2)

        val minArticleDetailArtworkDim : Int = ViewUtils.toPX(10F)
        val articleDetailArtworkDim : Int = ViewUtils.toXScaledPX(125F)
        val articleDetailContextualIconDim : Int = contextualIconDim // ViewUtils.toPX(14)

        val headerViewIconDim : Int = ViewUtils.toPX(28F)

        val m9Size = 9F * ViewUtils.scalarX
        val m10Size = 10F * ViewUtils.scalarX
        val m11Size = 11F * ViewUtils.scalarX
        val m12Size = 12F * ViewUtils.scalarX
        val m13Size = 13F * ViewUtils.scalarX
        val m14Size = 14F * ViewUtils.scalarX
        val m15Size = 15F * ViewUtils.scalarX
        val m16Size = 16F * ViewUtils.scalarX
        val m17Size = 17F * ViewUtils.scalarX
        val m18Size = 18F * ViewUtils.scalarX
        val m20Size = 20F * ViewUtils.scalarX
        val m30Size = 30F * ViewUtils.scalarX

        val impactTitleLetterSpacing = 0.4f * ViewUtils.scalarX

        val buttonFontSize = m15Size

        val detailPageHeadingFontSize = m20Size
        val detailPageStraplineFontSize = m12Size
        val detailPageSubheadingFontSize = m18Size
        val detailPageBodyFontSize = m14Size
        val detailPageMetaFontSize = m13Size

//        val footerCardLabelFontSize = m18Size
//        val footerCardLinkLabelFontSize = m18Size
//        val footerCardLinkPriceFontSize = m14Size
//        val footerCardStraplineFontSize = m12Size

        val maxiPlayerDateLabelFontSize = m15Size
        val maxiPlayerProgressLabelFontSize = m11Size
        val maxiPlayerTitleFontSize = m20Size
        val maxiPlayerOwnerFontSize = m18Size

        val productCardLabelFontSize = m16Size
        val productCardLinkLabelFontSize = m16Size
        val productCardLinkPriceFontSize = m14Size
        val productCardStraplineFontSize = m12Size
        val productCardCategoryFontSize = m12Size

        val detailsLinkMaxCharCount = 30
        val progressBarHeight = ViewUtils.toYScaledPX(4F)
        val contentBorderDim = ViewUtils.toXScaledPX(1F)


        val overlayHeightPX = ViewUtils.screenHeightPX - gutter * 2
    }

}