package me.milan.pubsub

import cats.Applicative
import cats.effect.ConcurrentEffect
import me.milan.domain.Record
import me.milan.serdes.AvroSerde
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.producer.Callback
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata

object Pub {

  def mock[F[_]: Applicative, V]: Pub[F, V] = new MockPub[F, V]

  def kafka[F[_]: ConcurrentEffect, V >: Null: AvroSerde](
    implicit
    kafkaProducer: KafkaProducer[String, GenericRecord]
  ): Pub[F, V] = new KafkaPub[F, V]

}

trait Pub[F[_], V] {

  def publish(record: Record[V]): F[Unit]

}

private[pubsub] class KafkaPub[F[_]: ConcurrentEffect, V >: Null: AvroSerde](
  implicit
  kafkaProducer: KafkaProducer[String, GenericRecord]
) extends Pub[F, V] {

  override def publish(record: Record[V]): F[Unit] =
    ConcurrentEffect[F].async { cb =>
      kafkaProducer
        .send(
          new ProducerRecord(
            record.topic.value,
            record.partitionId.orNull,
            record.timestamp,
            record.key.value,
            AvroSerde[V].encode(record.value)
          ),
          callback {
            case (_, throwable) =>
              cb(Option(throwable).toLeft(()))
          }
        )
      ()
    }

  private def callback(f: (RecordMetadata, Throwable) => Unit): Callback =
    new Callback {
      override def onCompletion(
        metadata: RecordMetadata,
        exception: Exception
      ): Unit =
        f(metadata, exception)
    }
}

private[pubsub] class MockPub[F[_]: Applicative, V] extends Pub[F, V] {

  override def publish(record: Record[V]): F[Unit] =
    Applicative[F].pure(())

}
