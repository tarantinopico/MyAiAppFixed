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

    // Use derived state to observe if we are at bottom
    val isAtBottom by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem?.index == listState.layoutInfo.totalItemsCount - 1
        }
    }

    LaunchedEffect(isStreaming, itemCount) {
        if (isStreaming && controller.isAutoFollowEnabled) {
            if (itemCount > 0) {
                // Large offset to scroll to the very bottom of the item
                listState.animateScrollToItem(itemCount - 1, Int.MAX_VALUE)
            }
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex to listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collectLatest { (first, last) ->
                if (listState.isScrollInProgress) {
                    val atBottom = last != null && last >= itemCount - 1
                    if (!atBottom) {
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
                listState.animateScrollToItem(itemCount - 1, Int.MAX_VALUE)
            }
        }
    }
}
