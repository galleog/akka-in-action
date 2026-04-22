package com.github.galleog.pekko.chapter05.faulttolerance1.exception

import java.io.Serializable

class DbNodeDownException(msg: String) : Exception(msg), Serializable