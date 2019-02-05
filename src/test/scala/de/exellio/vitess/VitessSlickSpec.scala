package de.exellio.vitess
import org.scalatest.concurrent.{ IntegrationPatience, ScalaFutures }
import org.scalatest.{ FreeSpec, MustMatchers }
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

class VitessSlickSpec
    extends FreeSpec
    with MustMatchers
    with ScalaFutures
    with IntegrationPatience {

  "must connect to vitess server with slick conf" in {

    val dbName                              = "db.vt1"
    val config: DatabaseConfig[JdbcProfile] = DatabaseConfig.forConfig[JdbcProfile](dbName)

    lazy val profile = config.profile
    import profile.api._
    lazy val db: Database = config.db

    val q = sql"""select p.sku, p.description
      from product p""".as[(String, String)]

    val res = db.run(q).futureValue
    res foreach println

  }

}
