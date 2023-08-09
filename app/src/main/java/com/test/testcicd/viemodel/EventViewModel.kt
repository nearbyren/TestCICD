import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class EventViewModel : ViewModel() {

    private val mutableStateFlow = MutableStateFlow(0)

    fun postEvent(state: Int) {
        mutableStateFlow.value = state
    }

    fun observeEvent(scope: CoroutineScope? = null, method: (Int) -> Unit = { _ -> }) {
        val eventScope = scope ?: viewModelScope
        eventScope.launch {
            mutableStateFlow.collect {
                method.invoke(it)
            }
        }
    }
}
