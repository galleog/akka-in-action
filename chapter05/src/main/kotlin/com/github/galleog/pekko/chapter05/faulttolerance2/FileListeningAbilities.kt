package com.github.galleog.pekko.chapter05.faulttolerance2

// provides File watching API
interface FileListeningAbilities {
    fun register(uri: String)
}