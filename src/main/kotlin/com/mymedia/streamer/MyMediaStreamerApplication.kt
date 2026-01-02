package com.mymedia.streamer

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MyMediaStreamerApplication

fun main(args: Array<String>) {
	runApplication<MyMediaStreamerApplication>(*args)
}
