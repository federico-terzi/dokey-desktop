package net

import java.net.Socket

/**
 * The LinkManager manages the transmission of complex data between receivers.
 */
class LinkManager(val socket: Socket) : DEManager(socket) {

}