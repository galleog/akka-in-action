package com.github.galleog.pekko.chapter05.faulttolerance2.exception

import java.io.Serializable

class DbBrokenConnectionException(msg: String) : Exception(msg), Serializable