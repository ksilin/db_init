package de.exellio.vitess

import de.exellio.vitess.schema.{ Nation, NationSchema }
import org.scalatest.concurrent.{ IntegrationPatience, ScalaFutures }
import org.scalatest.{ FreeSpec, MustMatchers }
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.Await
import scala.concurrent.duration._

class TiDbSlickSpec
    extends FreeSpec
    with MustMatchers
    with ScalaFutures
    with IntegrationPatience
    with Timed {

  val dbName                              = "db.ti"
  val config: DatabaseConfig[JdbcProfile] = DatabaseConfig.forConfig[JdbcProfile](dbName)

  "must connect to tidb server with slick conf" in {

    lazy val profile = config.profile
    import profile.api._
    lazy val db: Database = config.db

    val q = sql"""select n.n_name, n.n_comment
      from nation n""".as[(String, String)]

    val res = timed("read small table") {
      Await.result(db.run(q), 1.second)
    }
    res foreach println
  }

  "slick schema & generated queries " - {

    lazy val profile = config.profile
    import profile.api._
    lazy val db: Database = config.db

    val schema = NationSchema(config)

    "must retrieve data" in {
      val getNationsQuery = schema.nations.take(1000)
      val res = timed("retrieve data") {
        Await.result(db.run(getNationsQuery.result), 1.second)
      }
      res foreach println

    }

    "write data" in {
//      val newProd: Product = Product("testSKU", "testDescr", 99)

      // on duplicates insert:
      // com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException, with message:
      // vtgate: http://HETZ-TEST-1:15001/: execInsertUnsharded: target: commerce.0.master,
      // used tablet: zone1-100 (HETZ-TEST-1), vttablet: rpc error: code = AlreadyExists
      // desc = Duplicate entry 'testSKU' for key 'PRIMARY' (errno 1062) (sqlstate 23000) (CallerID: userData1):
      // Sql: "insert into product(sku, description, price) values (:vtg1, :vtg2, :vtg3)",
      // BindVars: {#maxLimit: "type:INT64 value:\"10001\" "vtg1: "type:VARBINARY value:\"testSKU\" "vtg2:
      // "type:VARBINARY value:\"testDescr\" "vtg3: "type:INT64 value:\"99\" "}.
//      val inserted = db.run(schema.products += newProd).futureValue
//      println(inserted)
    }

    "upsert data" in {
      val newProd: Nation = Nation(99, "testNation", 99, "testComment")

      // InsertOrUpdate is not supported on a table without PK.
      val upserted = timed("upsert single") {
        Await.result(db.run(schema.nations.insertOrUpdate(newProd)), 1.second)
      }
      println(upserted) // always returns 1, even on update
    }

    "insert multiple" in {

      val newCountries: List[Nation] = (100 to 1100) map { i =>
        Nation(i, "testNation", 99, "testComment")
      } toList

      // 20ms per insert - abysmal!
      val inserted = timed("insert multiple") {
        newCountries map { p =>
          Await.result(db.run(schema.nations += p), 1.second)
        }
      }
      println(inserted)
    }

    "insert multiple at once" in {

      val newCountries: List[Nation] = (100 to 1100) map { i =>
        Nation(i, "testNation", 99, "testComment")
      } toList

      // 18s for bulk insert!
      val inserted = timed("insert multiple") {
        Await.result(db.run(schema.nations ++= newCountries), 10.minutes)
      }
      println(inserted)
    }

    "insert multiple plain sql" in {

      def insertQuery(c: Nation): DBIO[Int] =
        sqlu"insert into nation values (${c.n_nationkey}, ${c.n_name}, ${c.n_regionkey}, ${c.n_comment})"

      val queries = (100 to 200) map { i =>
        sqlu"insert into nation values ($i, 'testNation', 99, 'testComment')"
      } toList

      // plain sql still 20ms - same with or without connection pool
      val inserted = timed("insert multiple") {
        queries map { q =>
          timed("insert single") {
            Await.result(db.run(q), 1.second)
          }
        }
      }
      println(inserted)
    }
  }

}
