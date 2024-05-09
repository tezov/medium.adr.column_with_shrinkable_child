package com.tezov.medium.adr.shrinkablebox

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.InspectorValueInfo
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.util.fastForEachIndexed
import kotlin.math.max

@Composable
fun ColumnWithShrinkable(
    modifier: Modifier = Modifier,
    content: @Composable ColumnWithShrinkableScope.() -> Unit
) {
    val measurePolicy = remember {
        columnWithShrinkableMeasurePolicy()
    }
    Layout(
        content = { ColumnWithShrinkableScopeImpl.content() },
        modifier = modifier,
        measurePolicy = measurePolicy
    )
}

interface ColumnWithShrinkableScope {
    fun Modifier.shrink(value: Float): Modifier
}

private fun columnWithShrinkableMeasurePolicy() = object : MeasurePolicy {
        val Measurable.columnWithShrinkableParentData get() = (parentData as? ColumnWithShrinkableParentData)
        val Measurable.shrink get() = columnWithShrinkableParentData?.shrink

        override fun MeasureScope.measure(
            measurables: List<Measurable>,
            constraints: Constraints
        ): MeasureResult {
            if (measurables.isEmpty()) {
                return layout(
                    constraints.minWidth,
                    constraints.minHeight
                ) {}
            }
            var boxWidth = 0
            var boxHeight = 0
            val placeables = arrayOfNulls<Placeable>(measurables.size)
            measurables.fastForEachIndexed { index, measurable ->
                val height = measurable.minIntrinsicHeight(constraints.maxWidth)
                val shrink = measurable.shrink
                val measureConstraints = shrink?.let { ratio ->
                    constraints.copy(
                        maxHeight = (height * ratio).toInt(),
                    )
                } ?: constraints
                val placeable = measurable.measure(measureConstraints)
                placeables[index] = placeable
                boxWidth = max(boxWidth, placeable.width)
                boxHeight += placeable.height
            }
            boxWidth = boxWidth.coerceIn(constraints.minWidth, constraints.maxWidth)
            boxHeight = boxHeight.coerceIn(constraints.minHeight, constraints.maxHeight)

            return layout(boxWidth, boxHeight) {
                var yOffset = 0
                placeables.forEach { placeable ->
                    placeable as Placeable
                    placeable.place(IntOffset(0,yOffset))
                    yOffset += placeable.height
                }
            }
        }

    }

private object ColumnWithShrinkableScopeImpl : ColumnWithShrinkableScope {

    override fun Modifier.shrink(value: Float): Modifier {
        require(value >= 0.0f) { "invalid heightFactor $value; must be greater or equal than zero" }
        require(value <= 1.0f) { "invalid heightFactor $value; must be lesser or equal than zero" }
        return this.then(
            ModifierShrinkImpl(
                value = value,
                inspectorInfo = debugInspectorInfo {
                    name = "shrink"
                    this.value = value
                    properties["value"] = value
                }
            )
        )
    }
}

private data class ColumnWithShrinkableParentData(
    var shrink: Float? = null,
)

private class ModifierShrinkImpl(
    val value: Float,
    inspectorInfo: InspectorInfo.() -> Unit
) : ParentDataModifier, InspectorValueInfo(inspectorInfo) {

    override fun Density.modifyParentData(parentData: Any?) =
        ((parentData as? ColumnWithShrinkableParentData) ?: ColumnWithShrinkableParentData()).also {
            it.shrink = value
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ModifierShrinkImpl) return false
        return value == other.value
    }

    override fun hashCode(): Int {
        var result = value.hashCode()
        result = 31 * result + value.hashCode()
        return result
    }

    override fun toString(): String =
        "ModifierShrinkImpl(value=$value)"
}