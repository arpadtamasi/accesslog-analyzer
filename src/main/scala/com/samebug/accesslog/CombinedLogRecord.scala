package com.samebug.accesslog

import java.text.SimpleDateFormat
import java.util.{Locale, Date}
import java.util.regex.Pattern

import scala.util.Try


/**
 * @see http://httpd.apache.org/docs/2.2/logs.html for details
 */
case class CombinedLogRecord(
  clientIp: String,
  userId: String,
  userName: String,
  timestamp: Date,
  method: String,
  uri: String,
  httpVersion: String,
  statusCode: String,
  bytesSent: String,
  referer: String,
  userAgent: String) 

object CombinedLogRecord {

  def parseRecord: PartialFunction[String, CombinedLogRecord] = {
    case RecordPattern(
    clientIpAddress, rfc1413ClientIdentity, remoteUser,
    dateTime, RequestPattern(requestType, uri, httpVersion),
    httpStatusCode, bytesSent, referer, userAgent) =>
      val timestamp = parseDateField(dateTime)
      CombinedLogRecord(
        clientIpAddress, rfc1413ClientIdentity, remoteUser,
        timestamp, requestType, uri, httpVersion,
        httpStatusCode, bytesSent, referer, userAgent)
  }

  private def parseDateField: PartialFunction[String, Date] = {
    case TimestampPattern(ts) =>
      val dateFormat = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z", Locale.ENGLISH)
      dateFormat.parse(ts)
  }


  private val RecordPattern = {
    val ddd = "\\d{1,3}"
    // at least 1 but not more than 3 times (possessive)
    val ip = s"($ddd\\.$ddd\\.$ddd\\.$ddd)?"
    // like `123.456.7.89`
    val client = "(\\S+)"
    // '\S' is 'non-whitespace character'
    val user = "(\\S+)"
    val dateTime = "(\\[.+?\\])"
    // like `[21/Jul/2009:02:48:13 -0700]`
    val request = "\"(.*?)\""
    // any number of any character, reluctant
    val status = "(\\d{3})"
    val bytes = "(\\S+)"
    // this can be a "-"
    val referer = "\"(.*?)\""
    val agent = "\"(.*?)\""
    s"$ip $client $user $dateTime $request $status $bytes $referer $agent".r
  }

  private val RequestPattern = "(.*) (.*) (.*)".r
  private val TimestampPattern = "\\[(\\d+/\\w+/\\d+\\:\\d{2}\\:\\d{2}\\:\\d{2} [+-]\\d{4})\\]".r
}