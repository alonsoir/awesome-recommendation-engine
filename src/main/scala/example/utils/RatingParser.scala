package example.utils

import java.io.File

import example.model.AmazonRating

import scala.io.Source

object RatingParser {
  def parse(file: File) = {
    for (line <- Source.fromFile(file).getLines()) yield {
      val Array(itemId, userId, scoreStr) = line.split(",")
      AmazonRating(itemId, userId, scoreStr.toDouble)
    }
  }
}
