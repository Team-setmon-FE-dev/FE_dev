package com.setmon.feature.friend

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.setmon.core.data.friend.FriendError
import com.setmon.core.data.friend.FriendRepository
import com.setmon.core.data.friend.FriendRequestOutcome
import com.setmon.core.data.friend.FriendResult
import com.setmon.core.data.user.MyProfileError
import com.setmon.core.data.user.MyProfileResult
import com.setmon.core.data.user.UserRepository
import com.setmon.core.model.Friend
import com.setmon.core.model.FriendRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val MAX_CODE_LENGTH = 16

/** 입력창 아래에 보이는 결과 문구 */
internal data class FriendMessage(
    @param:StringRes val text: Int,
    val isError: Boolean,
)

internal data class FriendUiState(
    val isLoading: Boolean = true,
    @param:StringRes val loadError: Int? = null,
    val myCode: String = "",
    val requests: List<FriendRequest> = emptyList(),
    val friends: List<Friend> = emptyList(),
    val codeInput: String = "",
    val isSending: Boolean = false,
    val message: FriendMessage? = null,
    val removeTarget: Friend? = null,
) {
    val canSend: Boolean get() = codeInput.isNotEmpty() && !isSending
}

@HiltViewModel
internal class FriendViewModel @Inject constructor(
    private val friendRepository: FriendRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FriendUiState())
    val uiState: StateFlow<FriendUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(isLoading = true, loadError = null) }
        viewModelScope.launch {
            when (val profile = userRepository.getMyProfile()) {
                is MyProfileResult.Failure -> _uiState.update {
                    it.copy(isLoading = false, loadError = profile.error.messageRes())
                }
                is MyProfileResult.Success -> {
                    _uiState.update { it.copy(myCode = profile.profile.friendCode) }
                    val error = reloadLists()
                    _uiState.update { it.copy(isLoading = false, loadError = error?.messageRes()) }
                }
            }
        }
    }

    fun onCodeChange(value: String) {
        val code = value.uppercase().filter(Char::isLetterOrDigit).take(MAX_CODE_LENGTH)
        _uiState.update { it.copy(codeInput = code, message = null) }
    }

    fun sendRequest() {
        val state = _uiState.value
        if (!state.canSend) return
        _uiState.update { it.copy(isSending = true, message = null) }
        viewModelScope.launch {
            when (val result = friendRepository.sendRequest(state.codeInput)) {
                is FriendResult.Success -> {
                    val text = when (result.data) {
                        FriendRequestOutcome.REQUESTED -> R.string.friend_request_sent
                        FriendRequestOutcome.BECAME_FRIENDS -> R.string.friend_became_friends
                    }
                    _uiState.update { it.copy(codeInput = "", message = FriendMessage(text, isError = false)) }
                    reloadLists()
                }
                is FriendResult.Failure -> showError(result.error)
            }
            _uiState.update { it.copy(isSending = false) }
        }
    }

    fun accept(request: FriendRequest) = runAction { friendRepository.accept(request.requestId) }

    fun decline(request: FriendRequest) = runAction { friendRepository.reject(request.requestId) }

    fun askRemove(friend: Friend) = _uiState.update { it.copy(removeTarget = friend) }

    fun dismissRemove() = _uiState.update { it.copy(removeTarget = null) }

    fun confirmRemove() {
        val friend = _uiState.value.removeTarget ?: return
        _uiState.update { it.copy(removeTarget = null) }
        runAction { friendRepository.remove(friend.userId) }
    }

    private fun runAction(action: suspend () -> FriendResult<Unit>) {
        _uiState.update { it.copy(message = null) }
        viewModelScope.launch {
            when (val result = action()) {
                is FriendResult.Success -> reloadLists()
                is FriendResult.Failure -> showError(result.error)
            }
        }
    }

    /** 실패하면 그 원인을 돌려주고, 화면의 기존 목록은 그대로 둔다 */
    private suspend fun reloadLists(): FriendError? {
        val requests = friendRepository.getReceivedRequests()
        val friends = friendRepository.getFriends()
        return when {
            requests is FriendResult.Failure -> requests.error
            friends is FriendResult.Failure -> friends.error
            requests is FriendResult.Success && friends is FriendResult.Success -> {
                _uiState.update { it.copy(requests = requests.data, friends = friends.data) }
                null
            }
            else -> FriendError.UNKNOWN
        }
    }

    private fun showError(error: FriendError) =
        _uiState.update { it.copy(message = FriendMessage(error.messageRes(), isError = true)) }

    private fun FriendError.messageRes(): Int = when (this) {
        FriendError.CODE_NOT_FOUND -> R.string.friend_error_code_not_found
        FriendError.SELF_REQUEST -> R.string.friend_error_self
        FriendError.ALREADY_FRIEND -> R.string.friend_error_already
        FriendError.DUPLICATE_REQUEST -> R.string.friend_error_duplicate
        FriendError.NETWORK -> R.string.friend_error_network
        FriendError.UNKNOWN -> R.string.friend_error_unknown
    }

    private fun MyProfileError.messageRes(): Int = when (this) {
        MyProfileError.NETWORK -> R.string.friend_error_network
        MyProfileError.UNKNOWN -> R.string.friend_error_unknown
    }
}
