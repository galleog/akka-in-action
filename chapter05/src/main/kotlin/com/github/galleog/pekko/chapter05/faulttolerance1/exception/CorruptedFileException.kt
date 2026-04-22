package com.github.galleog.pekko.chapter05.faulttolerance1.exception

import java.io.File
import java.io.Serializable

class CorruptedFileException(msg: String, val file: File) : Exception(msg), Serializable