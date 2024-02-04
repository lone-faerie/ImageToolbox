/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2024 T8RIN (Malik Mukhametzyanov)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package ru.tech.imageresizershrinker.core.filters.presentation.model

import android.content.Context
import android.graphics.Bitmap
import androidx.annotation.StringRes
import ru.tech.imageresizershrinker.core.filters.domain.model.Filter
import ru.tech.imageresizershrinker.core.filters.domain.model.FilterParam
import kotlin.reflect.full.primaryConstructor

sealed class UiFilter<T>(
    @StringRes val title: Int,
    val paramsInfo: List<FilterParam> = listOf(),
    override val value: T
) : Filter<Bitmap, T> {

    constructor(
        @StringRes title: Int,
        valueRange: ClosedFloatingPointRange<Float>,
        value: T,
    ) : this(
        title = title,
        paramsInfo = listOf(
            FilterParam(valueRange = valueRange)
        ),
        value = value
    )

    fun <T : Any> copy(value: T): UiFilter<*> {
        if (this.value == null) return newInstance()
        if (this.value!!::class.simpleName != value::class.simpleName) return newInstance()
        return this::class.primaryConstructor!!.run { callBy(mapOf(parameters[0] to value)) }
    }

    fun newInstance(): UiFilter<*> = this::class.primaryConstructor!!.callBy(emptyMap())

    companion object {
        val groupedEntries by lazy {
            listOf(
                listOf(
                    UiHueFilter(),
                    UiColorFilter(),
                    UiSaturationFilter(),
                    UiVibranceFilter(),
                    UiRGBFilter(),
                    UiReplaceColorFilter(),
                    UiRemoveColorFilter(),
                    UiFalseColorFilter(),
                    UiCGAColorSpaceFilter(),
                    UiMonochromeFilter(),
                    UiSepiaFilter(),
                    UiNegativeFilter(),
                    UiBlackAndWhiteFilter(),
                    UiColorMatrixFilter(),
                    UiColorBalanceFilter()
                ),
                listOf(
                    UiBrightnessFilter(),
                    UiContrastFilter(),
                    UiExposureFilter(),
                    UiWhiteBalanceFilter(),
                    UiGammaFilter(),
                    UiHighlightsAndShadowsFilter(),
                    UiSolarizeFilter(),
                    UiHazeFilter()
                ),
                listOf(
                    UiSharpenFilter(),
                    UiCrosshatchFilter(),
                    UiSobelEdgeDetectionFilter(),
                    UiHalftoneFilter(),
                    UiEmbossFilter(),
                    UiLaplacianFilter(),
                    UiVignetteFilter(),
                    UiKuwaharaFilter(),
                    UiErodeFilter(),
                    UiDilationFilter(),
                    UiOpacityFilter(),
                    UiSideFadeFilter(),
                    UiToonFilter(),
                    UiSmoothToonFilter(),
                    UiSketchFilter(),
                    UiPosterizeFilter(),
                    UiLookupFilter(),
                    UiNonMaximumSuppressionFilter(),
                    UiWeakPixelFilter(),
                    UiConvolution3x3Filter(),
                    UiLuminanceThresholdFilter()
                ),
                listOf(
                    UiTiltShiftFilter(),
                    UiGaussianBlurFilter(),
                    UiNativeStackBlurFilter(),
                    UiBoxBlurFilter(),
                    UiBilaterialBlurFilter(),
                    UiTentBlurFilter(),
                    UiStackBlurFilter(),
                    UiFastBlurFilter(),
                    UiZoomBlurFilter(),
                    UiAnisotropicDiffusionFilter(),
                    UiMedianBlurFilter()
                ),
                listOf(
                    UiPixelationFilter(),
                    UiEnhancedPixelationFilter(),
                    UiDiamondPixelationFilter(),
                    UiEnhancedDiamondPixelationFilter(),
                    UiCirclePixelationFilter(),
                    UiEnhancedCirclePixelationFilter(),
                    UiStrokePixelationFilter()
                ),
                listOf(
                    UiEnhancedGlitchFilter(),
                    UiGlitchFilter(),
                    UiShuffleFilter(),
                    UiAnaglyphFilter(),
                    UiPixelSortFilter(),
                    UiHorizontalWindStaggerFilter(),
                    UiNoiseFilter(),
                    UiSwirlDistortionFilter(),
                    UiBulgeDistortionFilter(),
                    UiSphereRefractionFilter(),
                    UiGlassSphereRefractionFilter()
                ),
                listOf(
                    UiBayerTwoDitheringFilter(),
                    UiBayerThreeDitheringFilter(),
                    UiBayerFourDitheringFilter(),
                    UiBayerEightDitheringFilter(),
                    UiFloydSteinbergDitheringFilter(),
                    UiJarvisJudiceNinkeDitheringFilter(),
                    UiSierraDitheringFilter(),
                    UiTwoRowSierraDitheringFilter(),
                    UiSierraLiteDitheringFilter(),
                    UiAtkinsonDitheringFilter(),
                    UiStuckiDitheringFilter(),
                    UiBurkesDitheringFilter(),
                    UiFalseFloydSteinbergDitheringFilter(),
                    UiLeftToRightDitheringFilter(),
                    UiRandomDitheringFilter(),
                    UiSimpleThresholdDitheringFilter(),
                    UiQuantizierFilter()
                )
            )
        }

        fun groupedEntries(
            context: Context
        ) = groupedEntries.map { list ->
            list.sortedBy { context.getString(it.title) }
        }
    }

}

fun Filter<Bitmap, *>.toUiFilter(): UiFilter<*> = UiFilter::class.sealedSubclasses.first {
    it.java.isAssignableFrom(this::class.java)
}.primaryConstructor!!.run { callBy(mapOf(parameters[0] to value)) }

infix fun Int.paramTo(valueRange: ClosedFloatingPointRange<Float>) = FilterParam(
    title = this,
    valueRange = valueRange
)