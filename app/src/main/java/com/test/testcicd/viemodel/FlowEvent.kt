package com.test.testcicd.viemodel

import EventViewModel
import kotlinx.coroutines.CoroutineScope

object FlowEvent {

    //发送事件
    fun postEvent(state: Int) {
        ApplicationScopeViewModelProvider.getApplicationScopeViewModel(EventViewModel::class.java)
                .postEvent(state)
    }

    //订阅事件
    fun observeEvent(scope: CoroutineScope? = null, method: (Int) -> Unit = { _ -> }) {
        ApplicationScopeViewModelProvider.getApplicationScopeViewModel(EventViewModel::class.java)
                .observeEvent(scope, method)
    }
}
