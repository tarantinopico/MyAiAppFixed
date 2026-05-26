package com.example.ui.components

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun rememberSmartAutoScrollState(
    listState: LazyListState,
    itemCount: Int,
    isStreaming: Boolean
): SmartAutoScrollController {
    val coroutineScope = rememberCoroutineScope()
    val controller = remember { SmartAutoScrollController(listState, coroutineScope) }

    LaunchedEffect(isStreaming, itemCount) {
        if (isStreaming && controller.isAutoFollowEnabled) {
            if (itemCount > 0) {
                listState.animateScrollToItem(itemCount - 1)
            }
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex to listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collectLatest { (first, last) ->
                if (listState.isScrollInProgress) {
                    val isAtBottom = last != null && last >= itemCount - 1
                    if (!isAtBottom) {
                        controller.disableAutoFollow()
                    } else {
                        controller.enableAutoFollow()
                    }
                }
            }
    }

    return controller
}

class SmartAutoScrollController(
    val listState: LazyListState,
    private val scope: kotlinx.coroutines.CoroutineScope
) {
    var isAutoFollowEnabled by mutableStateOf(true)
        private set

    fun disableAutoFollow() {
        isAutoFollowEnabled = false
    }

    fun enableAutoFollow() {
        isAutoFollowEnabled = true
    }

    fun jumpToBottom(itemCount: Int) {
        enableAutoFollow()
        scope.launch {
            if (itemCount > 0) {
                listState.animateScrollToItem(itemCount - 1)
            }
        }
    }
}
