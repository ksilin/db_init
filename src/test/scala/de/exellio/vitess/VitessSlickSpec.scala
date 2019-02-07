package de.exellio.vitess
import de.exellio.vitess.schema.{ ProdSchema, Product }
import org.scalatest.concurrent.{ IntegrationPatience, ScalaFutures }
import org.scalatest.{ FreeSpec, MustMatchers }
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

class VitessSlickSpec
    extends FreeSpec
    with MustMatchers
    with ScalaFutures
    with IntegrationPatience
    with Timed {

  val dbName                              = "db.vt1"
  val config: DatabaseConfig[JdbcProfile] = DatabaseConfig.forConfig[JdbcProfile](dbName)

  "must connect to vitess server with slick conf" in {

    lazy val profile = config.profile
    import profile.api._
    lazy val db: Database = config.db

    val q = sql"""select p.sku, p.description
      from product p""".as[(String, String)]

    val res = db.run(q).futureValue
    res foreach println
  }

  "slick schema & generated queries " - {

    lazy val profile = config.profile
    import profile.api._
    lazy val db: Database = config.db

    val schema = ProdSchema(config)

    "must use to retrieve data" in {
      val getProductsQuery = schema.products.take(10)
      val res              = db.run(getProductsQuery.result).futureValue
      res foreach println

    }

    "write data" in {
      val newProd: Product = Product("testSKU", "testDescr", 99)

      // on duplicates insert:
      // com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException, with message:
      // vtgate: http://HETZ-TEST-1:15001/: execInsertUnsharded: target: commerce.0.master,
      // used tablet: zone1-100 (HETZ-TEST-1), vttablet: rpc error: code = AlreadyExists
      // desc = Duplicate entry 'testSKU' for key 'PRIMARY' (errno 1062) (sqlstate 23000) (CallerID: userData1):
      // Sql: "insert into product(sku, description, price) values (:vtg1, :vtg2, :vtg3)",
      // BindVars: {#maxLimit: "type:INT64 value:\"10001\" "vtg1: "type:VARBINARY value:\"testSKU\" "vtg2:
      // "type:VARBINARY value:\"testDescr\" "vtg3: "type:INT64 value:\"99\" "}.
      val inserted = db.run(schema.products += newProd).futureValue
      println(inserted)
    }

    "upsert data" in {
      val newProd: Product = Product("testSKU2", "testDescr", 99)

      // InsertOrUpdate is not supported on a table without PK.
      val upserted = timed("upsert single") {
        db.run(schema.products.insertOrUpdate(newProd)).futureValue
      }
      println(upserted) // always returns 1, even on update
    }

    "upsert multiple" in {

      val newProds: List[Product] = (1 to 1000) map { i =>
        Product(s"testSKU$i", "testDescr", 99)
      } toList

      // 150 to 300s - abysmal!
      val upserted = timed("upsert multiple") {
        newProds map { p =>
          db.run(schema.products.insertOrUpdate(p)).futureValue
        }
      }
      println(upserted)
    }
  }

}
