package com.github.galleog.pekko.chapter05.faulttolerance2.exception

import java.io.File
import java.io.Serializable

class ParseException(msg: String, val file: File) : Exception(msg), Serializable