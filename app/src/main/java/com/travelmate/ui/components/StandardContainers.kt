package com.travelmate.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.travelmate.ui.theme.Spacing

/**
 * Container standard pour tous les écrans avec padding horizontal cohérent
 */
@Composable
fun StandardScreenContainer(
    modifier: Modifier = Modifier,
    addBottomNavPadding: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                horizontal = Spacing.screenHorizontal,
                vertical = Spacing.screenVertical
            )
            .then(
                if (addBottomNavPadding) Modifier.padding(bottom = Spacing.bottomNavHeight)
                else Modifier
            ),
        content = content
    )
}

/**
 * LazyColumn standard avec spacing cohérent
 */
@Composable
fun StandardLazyColumn(
    modifier: Modifier = Modifier,
    addBottomNavPadding: Boolean = true,
    verticalSpacing: androidx.compose.ui.unit.Dp = Spacing.itemSpacing,
    content: LazyListScope.() -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Spacing.screenHorizontal,
            top = Spacing.screenVertical,
            end = Spacing.screenHorizontal,
            bottom = if (addBottomNavPadding) Spacing.bottomNavHeight else Spacing.screenVertical
        ),
        verticalArrangement = Arrangement.spacedBy(verticalSpacing),
        content = content
    )
}

/**
 * Container pour les cartes avec padding et spacing standard
 */
@Composable
fun StandardCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    androidx.compose.material3.Card(
        modifier = modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(Spacing.cardCornerRadius),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(
            defaultElevation = Spacing.cardElevation
        )
    ) {
        Column(
            modifier = Modifier.padding(Spacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            content = content
        )
    }
}

/**
 * Section spacing standard entre groupes de contenu
 */
@Composable
fun SectionSpacer() {
    Spacer(modifier = Modifier.height(Spacing.sectionSpacing))
}

/**
 * Item spacing standard entre éléments
 */
@Composable
fun ItemSpacer() {
    Spacer(modifier = Modifier.height(Spacing.itemSpacing))
}
