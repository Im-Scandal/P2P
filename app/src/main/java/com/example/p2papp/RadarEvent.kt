package com.example.p2papp

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object RadarEvent {
    private val _radarPings = MutableSharedFlow<WifiFrame>(extraBufferCapacity = 10)
    val radarPings = _radarPings.asSharedFlow()

    suspend fun emitRadarPing(frame: WifiFrame) {
        _radarPings.emit(frame)
    }
}