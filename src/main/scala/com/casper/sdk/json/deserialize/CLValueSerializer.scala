package com.casper.sdk.json.deserialize

import com.casper.sdk.types.cltypes.{CLOptionTypeInfo, _}
import com.fasterxml.jackson.core.{JsonParser, ObjectCodec, TreeNode}
import com.fasterxml.jackson.databind.{DeserializationContext, JsonDeserializer}
import com.fasterxml.jackson.databind.node.{NumericNode, TextNode}

import java.io.IOException

/**
 *   Custom fasterXml Deserializer for CLValue
 */
class CLValueSerializer extends JsonDeserializer[CLValue] {
  @throws[IOException]
  override def deserialize(parser: JsonParser, ctx: DeserializationContext): CLValue = {
    val codec: ObjectCodec = parser.getCodec
    val treeNode: TreeNode = codec.readTree(parser)
    clValue(treeNode)
  }

  /**
   * build CLValue
   * @param treeNode
   * @return
   */
  def clValue(treeNode: TreeNode): CLValue = {
    val bytesNode = treeNode.get("bytes").asInstanceOf[TextNode].asText()
    val typeNode = treeNode.get("cl_type")
    val clTypeInfo = cLTypeInfo(typeNode)
    val parsedValue =  parsed(treeNode.get("parsed"), clTypeInfo)
    clTypeInfo match {
      case  clTypeInfo :CLOptionTypeInfo  =>  CLOptionValue(bytesNode, clTypeInfo.asInstanceOf[CLOptionTypeInfo], parsedValue)
      case _=> CLValue(bytesNode, clTypeInfo, parsedValue)
    }
  }

  /**
   * get CLType info
   * @param typeNode
   * @return
   */
  def cLTypeInfo(typeNode: TreeNode): CLTypeInfo = {
    val cl_Type = clType(typeNode)
    cl_Type match {
      case CLType.ByteArray => {
        val sizeNode = typeNode.get(CLType.ByteArray.toString)
        var size = 0
        if (sizeNode.isInstanceOf[NumericNode]) size = sizeNode.asInstanceOf[NumericNode].asInt
        new CLByteArrayInfo(size)
      }
      case CLType.Option => {
        val optionNode = typeNode.get(CLType.Option.toString)
        val interType = cLTypeInfo(optionNode)
        new CLOptionTypeInfo(interType)
      }
      case _ => {
        new CLTypeInfo(cl_Type)
      }
    }
  }

  /**
   * get parsed value
   * @param typeNode
   * @param clTypeInfo
   * @return
   */
  def parsed(typeNode: TreeNode, clTypeInfo: CLTypeInfo): Any =
    typeNode match {
      case typeNode : TextNode =>  typeNode.asInstanceOf[TextNode].asText
      case typeNode : NumericNode => CLType.isNumeric(clTypeInfo.cl_Type) match {
        case true =>   typeNode.asInstanceOf[NumericNode].bigIntegerValue
        case false =>  null
      }
      case _ => null
    }

  /**
   * get CLType
   * @param typeNode
   * @return
   */
  def clType(typeNode: TreeNode): CLType =  typeNode match {
    case typeNode : TextNode =>  CLType.valueOf(typeNode.asInstanceOf[TextNode].asText())
    case _=> CLType.valueOf(typeNode.fieldNames.next)
  }
}