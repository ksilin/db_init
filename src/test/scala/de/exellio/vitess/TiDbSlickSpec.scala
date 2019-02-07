package de.exellio.vitess

import de.exellio.vitess.schema.{ Nation, NationSchema, ProdSchema, Product }
import org.scalatest.concurrent.{ IntegrationPatience, ScalaFutures }
import org.scalatest.{ FreeSpec, MustMatchers }
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

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
      db.run(q).futureValue
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
        db.run(getNationsQuery.result).futureValue
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
        db.run(schema.nations.insertOrUpdate(newProd)).futureValue
      }
      println(upserted) // always returns 1, even on update
    }

    "insert multiple" in {

      val newProds: List[Nation] = (100 to 1100) map { i =>
        Nation(i, "testNation", 99, "testComment")
      } toList

      // 150 to 300s - abysmal!
      val inserted = timed("insert multiple") {
        newProds map { p =>
          db.run(schema.nations += p).futureValue
        }
      }
      println(inserted)
    }
  }

}
