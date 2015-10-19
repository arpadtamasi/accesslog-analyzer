package com.samebug.accesslog

import scala.io.Source

class DomainLookup(resource: String) {
  def host(clientIp: String) = iptable.get(clientIp) getOrElse lookup(clientIp)

  private def lookup(clientIp: String) = {
    val host = java.net.InetAddress.getByName(clientIp).getCanonicalHostName()
    synchronized(iptable) {
      iptable += (clientIp -> host)
      host
    }
  }

  private var iptable: Map[String, String] = {
    val is = classOf[DomainLookup].getClassLoader.getResourceAsStream(resource)
    try {
      val table = Source.fromInputStream(is).getLines() map { _.split("\t") } map {
        case Array(ip, host) => ip -> host
      }

      table.toMap
    } finally {
      is.close
    }
  }
}
