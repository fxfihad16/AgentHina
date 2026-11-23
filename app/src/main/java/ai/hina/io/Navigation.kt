package ai.hina.io

import androidx.compose.runtime.Composable
import ai.hina.io.ui.chat.ChatScreen
import ai.hina.io.ui.chat.ChatViewModel

@Composable
fun MainNavigation(viewModel: ChatViewModel? = null) {
    if (viewModel != null) {
        ChatScreen(viewModel = viewModel)
    } else {
        ChatScreen()
    }
}
