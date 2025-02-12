package com.casper.sdk.types.cltypes

import com.casper.sdk.json.deserialize.URefDeserializer
import com.casper.sdk.util.HexUtils
import com.fasterxml.jackson.databind.annotation.JsonDeserialize

/**
 * Unforgeatable Reference
 * @param bytes
 */
@JsonDeserialize(`using` = classOf[URefDeserializer])
class URef(override val bytes: Array[Byte]) extends CLValue(bytes, CLType.URef) {
  var accessRights: AccessRight = null

  /**
   * Constructor using byte array and an AccessRight
   * @param bytes
   * @param rights
   */
  def this(bytes: Array[Byte], rights: AccessRight) = {
    this(bytes)
    accessRights = rights
  }

  /**
   * Constructor using s String Uref value
   * @param uref
   */
  def this(uref: String) = this(URef.parseUref(uref), URef.getAccessRight(uref))

  /**
   * format Uref objet into : uref-51215724cc359a60797f64d88543002a069176f3ea92d4c37d31304e2849ef13-004
   * @return
   */
  def format: String = String.format(URef.UREF_PREFIX+"-%s-%03d", HexUtils.toHex(bytes).drop(2), accessRights.bits)

}

/**
 * Companion object
 */
object URef {

  val UREF_PREFIX = "uref"
  /**
   *  extract AccessRight from Uref String
   * @param uref
   * @return
   */
  private def getAccessRight(uref: String): AccessRight = {
    AccessRight.values.find(_.bits == Integer.parseInt(uref.charAt(uref.length - 1).toString)) match {
      case None => AccessRight.ACCESS_NONE
      case Some(a) => a
    }
  }

  /**
   * parse a Uref String into A byte array
   * @param uref
   * @return
   */
  private def parseUref(uref: String): Array[Byte] = {
    val opt = uref.split("-")
    opt(0) match {
      case UREF_PREFIX => HexUtils.fromHex(opt(1))
      case _ => throw new IllegalArgumentException(uref + " is not a valid uref")
    }
  }
}



