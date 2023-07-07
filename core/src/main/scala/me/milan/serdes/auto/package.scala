package me.milan.serdes

import com.sksamuel.avro4s.Decoder
import com.sksamuel.avro4s.Encoder
import com.sksamuel.avro4s.SchemaFor

package object auto {
  implicit def autoSchema[T >: Null: SchemaFor: Encoder: Decoder]: AvroSerde[T] =
    AvroSerde[T](SchemaFor[T].schema)
}
